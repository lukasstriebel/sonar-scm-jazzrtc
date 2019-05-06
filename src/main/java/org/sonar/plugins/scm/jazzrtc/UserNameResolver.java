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

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Created by yevhenii.andrushchak on 2018-12-31.
 */
public class UserNameResolver {
    private static final Logger LOG = Loggers.get(UserNameResolver.class);
    private static final Map<String, String> displayNameToLoginMap = new HashMap<String,String>();


    public UserNameResolver(final JazzRtcConfiguration config) {
    	//JSON parser object to parse read file
        if (displayNameToLoginMap.isEmpty()) {
	    	JSONParser jsonParser = new JSONParser();
	        String pathToFile = config.filePath();
	         
	        try (FileReader reader = new FileReader(pathToFile))
	        {
	            //Read JSON file
	        	JSONArray team = (JSONArray) jsonParser.parse(reader);             
	            //Iterate over team array
	            for (Object ob : team) {
	            	parseMemberObject((JSONObject) ob);
	            }
	 
	        } catch (FileNotFoundException e) {
	        	LOG.error(e.getMessage());
	        } catch (IOException e) {
	        	LOG.error(e.getMessage());
	        } catch (ParseException e) {
	        	LOG.error(e.getMessage());
	        }
        }
    }
 
    private void parseMemberObject(JSONObject member)
    {
        String name = (String) member.get("name");
        String email = (String) member.get("email");
        LOG.info("Added Mapping from '{}' to '{}'", name, email);
        displayNameToLoginMap.put(name, email);
    }

    public String resolveUserId(String username) {
        if(displayNameToLoginMap.containsKey(username)) {
            return displayNameToLoginMap.get(username);
        } 
        return displayNameToLoginMap.get("default");        
    }

    public String findUserIdByName(String name) {
        LOG.debug("Resolving username {}", name);
        String userId = resolveUserId(name);
        LOG.debug("User {} has been resolved to the following login: {}", name, userId);
        return userId;
    }
}
