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

import org.sonar.api.utils.command.Command;
import org.sonar.api.utils.System2;

/**
 * Created by yevhenii.andrushchak on 2018-12-21.
 */
public class LscmCommandCreator {
    private final System2 system;
    private final JazzRtcConfiguration config;

    public LscmCommandCreator(System2 system, JazzRtcConfiguration config){
        this.system = system;
        this.config = config;
    }

    public Command createLscmCommand(String ...args) {
        String lscmPath = config.lscmPath() == null ?  "lscm" : config.lscmPath();
        Command command = Command.create(lscmPath);
        // SONARSCRTC-3 and SONARSCRTC-6
        if(system.isOsWindows()) {
            command.setNewShell(true);
        }
        if (config.proxy() != null) {
        	command.setEnvironmentVariable("https_proxy", config.proxy());
        }
        // Disable aliasing
        command.addArgument("-a");
        command.addArgument("n");
        command.addArguments(args);

        return command;
    }
}
