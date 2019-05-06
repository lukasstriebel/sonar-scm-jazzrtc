/*
 * SonarQube :: Plugins :: SCM :: Jazz RTC
 * Copyright (C) 2014-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.scm.jazzrtc;

import org.sonar.api.utils.System2;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.scm.BlameCommand;
import org.sonar.api.batch.scm.BlameLine;
import org.sonar.api.utils.command.Command;
import org.sonar.api.utils.command.CommandExecutor;
import org.sonar.api.utils.command.StreamConsumer;
import org.sonar.api.utils.command.StringStreamConsumer;
import org.sonar.api.utils.command.TimeoutException;

public class JazzRtcBlameCommand extends BlameCommand {

  private static final Logger LOG = Loggers.get(JazzRtcBlameCommand.class);
  private static final List<Integer> UNTRACKED_BLAME_RETURN_CODES = Arrays.asList(1, 3, 30);
  private final CommandExecutor commandExecutor;
  private final JazzRtcConfiguration config;
  private final JazzLoginHandler jazzLoginHandler;
  private final LscmCommandCreator lscmCommandCreator;


  JazzRtcBlameCommand(CommandExecutor commandExecutor, JazzRtcConfiguration configuration) {
    this(commandExecutor, configuration, System2.INSTANCE);
  }

  public JazzRtcBlameCommand(JazzRtcConfiguration configuration) {
    this(CommandExecutor.create(), configuration);
  }
  
  JazzRtcBlameCommand(CommandExecutor commandExecutor, JazzRtcConfiguration configuration, System2 system) {
    this.commandExecutor = commandExecutor;
    this.config = configuration;
    this.lscmCommandCreator = new LscmCommandCreator(system, configuration);
    this.jazzLoginHandler = new JazzLoginHandler(system, commandExecutor, config);
  }

  @Override
  public void blame(BlameInput input, BlameOutput output) {
    final FileSystem fs = input.fileSystem();
    LOG.info("Working directory SCM JAZZ: " + fs.baseDir().getAbsolutePath());

    jazzLoginHandler.login();

    for (InputFile inputFile : input.filesToBlame()) {
      try {
        blame(fs, inputFile, output);
      } catch (Exception ex) {
        LOG.warn("Exception blaming {}. {}", inputFile, ex.getMessage());
      }

    }
  }

  private void blame(FileSystem fs, InputFile inputFile, BlameOutput output) {
    String filename = inputFile.relativePath();
    LOG.info("SCM JAZZ: Blame " + filename);
    Command cl = createAnnotateCommand(fs.baseDir(), filename);
    JazzRtcBlameParser parser = new JazzRtcBlameParser(filename, config);

    StringStreamConsumer stdout = new StringStreamConsumer();
    StringStreamConsumer stderr = new StringStreamConsumer();

    int exitCode = executeAnnotate(cl, stdout, stderr);
    if (UNTRACKED_BLAME_RETURN_CODES.contains(exitCode)) {
      LOG.info("Skipping untracked file: {}. Annotate command exit code: {}. Error: {}", filename, exitCode, stderr.getOutput());
      return;
    } else if (exitCode != 0) {
      throw new IllegalStateException("The jazz annotate command [" + cl.toString() + "] failed: " + stderr.getOutput());
    }

    List<BlameLine> lines = parser.parse(stdout.getOutput());
    if (lines.size() == inputFile.lines() - 1) {
      // SONARPLUGINS-3097 JazzRTC does not report blame on last empty line
      lines.add(lines.get(lines.size() - 1));
    }

    output.blameResult(inputFile, lines);
  }

  public int executeAnnotate(Command command, StreamConsumer consumer, StreamConsumer stderr) {
    LOG.debug("Executing: " + command);

    try {
      return commandExecutor.execute(command, consumer, stderr, config.commandTimeout());
    } catch (TimeoutException t) {
      String errorMsg = "The jazz annotate command timed out";

      if (config.username() != null || config.password() != null) {
        throw new IllegalStateException(errorMsg, t);
      } else {
        throw new IllegalStateException(errorMsg + ". Please check if you are logged in or provide username and password", t);
      }
    }
  }

  private Command createAnnotateCommand(File workingDirectory, String filename) {
    Command command = lscmCommandCreator.createLscmCommand("annotate", filename, "-j");
    command.setDirectory(workingDirectory);
    return command;
  }
}