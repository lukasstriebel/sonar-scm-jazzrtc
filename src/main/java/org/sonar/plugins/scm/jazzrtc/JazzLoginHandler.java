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
import org.sonar.api.utils.command.Command;
import org.sonar.api.utils.command.CommandExecutor;
import org.sonar.api.utils.command.StringStreamConsumer;
import org.sonar.api.utils.command.TimeoutException;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

/**
 * Created by yevhenii.andrushchak on 2018-12-21.
 */
public class JazzLoginHandler {
    private static final Logger LOG = Loggers.get(JazzRtcBlameCommand.class);
    private static boolean isLoggedIn = false;

    private final LscmCommandCreator commandCreator;
    private final CommandExecutor commandExecutor;
    private final JazzRtcConfiguration config;

    public JazzLoginHandler(System2 system, CommandExecutor commandExecutor, JazzRtcConfiguration config) {
        commandCreator = new LscmCommandCreator(system, config);
        this.commandExecutor = commandExecutor;
        this.config = config;
    }

    public void login() {
        if(isLoggedIn) {
            return;
        }

        if(config.username() == null || config.password() == null || config.repository() == null) {
            LOG.warn("Unable to log in. Please provide username, password and jazz server URL.");
            return;
        }
        try {
            this.executeLogout();
        } catch (Exception ex) {
            LOG.info("Error performing logout. {}", ex.getMessage());
        }

        this.executeLogin();

        isLoggedIn = true;
    }

    private void executeLogin() {
        Command loginCommand = createLoginCommandLine();
        LOG.info("Executing Login");
        executeLscmCommand(loginCommand);
    }

    private void executeLogout() {
        Command logoutCommand = createLogoutCommandLine();
        LOG.info("Executing Logout");
        executeLscmCommand(logoutCommand);
    }

    private Command createLoginCommandLine() {
        String username = config.username();
        String password = config.password();
        String repository = config.repository();
        return commandCreator.createLscmCommand("login", "-c", "-u", username, "-P", password, "-r", repository);
    }

    private Command createLogoutCommandLine() {
        String repository = config.repository();
        return commandCreator.createLscmCommand("logout", "-r", repository);
    }

    private void executeLscmCommand(Command command) {
        LOG.debug("Executing: " + command);

        try {
            StringStreamConsumer stderr = new StringStreamConsumer();
            StringStreamConsumer stdout = new StringStreamConsumer();
            int result = commandExecutor.execute(command, stdout, stderr, config.commandTimeout());

            if(result != 0) {
                throw new IllegalStateException("Command failed. Message: " + stderr.getOutput());
            }
        } catch (TimeoutException t) {
            throw new IllegalStateException("The jazz command timed out");
        }
    }
}
