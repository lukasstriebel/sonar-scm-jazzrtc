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

import org.sonar.api.CoreProperties;
import org.sonar.api.PropertyType;
import org.sonar.api.batch.InstantiationStrategy;
import org.sonar.api.batch.ScannerSide;
import org.sonar.api.config.Configuration;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

import javax.annotation.CheckForNull;

import java.util.ArrayList;
import java.util.List;

@InstantiationStrategy(InstantiationStrategy.PER_BATCH)
@ScannerSide()
public class JazzRtcConfiguration {

  private static final String CATEGORY_JAZZ = "Jazz RTC";
  private static final long CMD_TIMEOUT = 60_000;
  public static final String USER_PROP_KEY = "sonar.jazzrtc.username";
  public static final String REPOSITORY_PROP_KEY = "sonar.jazzrtc.repository";
  public static final String LSCM_PROP_KEY = "sonar.jazzrtc.lscm";
  public static final String PASSWORD_PROP_KEY = "sonar.jazzrtc.password.secured";
  public static final String FILE_PATH_PROP_KEY = "sonar.jazzrtc.mapfile";
  public static final String PROXY_PROP_KEY = "sonar.jazzrtc.proxy";
  
  private static String decodedPassword = null;

  private final Configuration  settings;

  public JazzRtcConfiguration(Configuration settings) {
    this.settings = settings;
  }

  public static List<PropertyDefinition> getProperties() {
    ArrayList<PropertyDefinition> properties = new ArrayList<>();

    properties.add(PropertyDefinition.builder(USER_PROP_KEY)
        .name("Username")
        .description("Username to be used for Jazz RTC authentication")
        .type(PropertyType.STRING)
        .onQualifiers(Qualifiers.PROJECT)
        .category(CoreProperties.CATEGORY_SCM)
        .subCategory(CATEGORY_JAZZ)
        .index(0)
        .build());

    properties.add(PropertyDefinition.builder(PASSWORD_PROP_KEY)
        .name("Password")
        .description("Password to be used for Jazz RTC authentication")
        .type(PropertyType.PASSWORD)
        .onQualifiers(Qualifiers.PROJECT)
        .category(CoreProperties.CATEGORY_SCM)
        .subCategory(CATEGORY_JAZZ)
        .index(1)
        .build());

    properties.add(PropertyDefinition.builder(REPOSITORY_PROP_KEY)
        .name("Repository URL")
        .description("Repository URL to be used for Jazz RTC authentication")
        .type(PropertyType.STRING)
        .onQualifiers(Qualifiers.PROJECT)
        .category(CoreProperties.CATEGORY_SCM)
        .subCategory(CATEGORY_JAZZ)
        .index(2)
        .build());

    properties.add(PropertyDefinition.builder(LSCM_PROP_KEY)
        .name("LSCM Path")
        .description("Path to the lscm tool to use.")
        .type(PropertyType.STRING)
        .onQualifiers(Qualifiers.PROJECT)
        .category(CoreProperties.CATEGORY_SCM)
        .subCategory(CATEGORY_JAZZ)
        .index(3)
        .build());
    
    properties.add(PropertyDefinition.builder(FILE_PATH_PROP_KEY)
            .name("File Path")
            .description("Path to the mapping file.")
            .type(PropertyType.STRING)
            .onQualifiers(Qualifiers.PROJECT)
            .category(CoreProperties.CATEGORY_SCM)
            .subCategory(CATEGORY_JAZZ)
            .index(4)
            .build());
    
    properties.add(PropertyDefinition.builder(PROXY_PROP_KEY)
            .name("Proxy Server")
            .description("URL of the proxy Server.")
            .type(PropertyType.STRING)
            .onQualifiers(Qualifiers.PROJECT)
            .category(CoreProperties.CATEGORY_SCM)
            .subCategory(CATEGORY_JAZZ)
            .index(5)
            .build());

    return properties;
  }

  @CheckForNull
  public String username() {
    return settings.get(USER_PROP_KEY).orElse(null);
  }

  @CheckForNull
  public String password() {
	  if (decodedPassword == null) {
		  decodedPassword = decode(settings.get(PASSWORD_PROP_KEY).orElse(null));
	  }
      return decodedPassword;
  }
  
  private String decode(String password) {
		if (password == null)
			return null;
		char[] hash = "ReA11>L0ngEXtR€ml3Yséc5èThasHke1".toCharArray();
		char[] passwordArray = password.toCharArray();
		StringBuffer encoded = new StringBuffer();
		for (int i = 0; i < Math.min(passwordArray.length, hash.length); i++) {
			int a = (int) passwordArray[i];
			int b = (int) hash[i];
			int result = a ^ b;
			encoded.append((char) result);
		}
		return encoded.toString();
  }

  @CheckForNull
  public String repository() {
    return settings.get(REPOSITORY_PROP_KEY).orElse(null);
  }

  @CheckForNull
  public String lscmPath() {
    return settings.get(LSCM_PROP_KEY).orElse(null);
  }
  
  @CheckForNull
  public String filePath() {
    return settings.get(FILE_PATH_PROP_KEY).orElse(null);
  }
  
  @CheckForNull
  public String proxy() {
    return settings.get(PROXY_PROP_KEY).orElse(null);
  }
  
  
  public long commandTimeout() {
    return CMD_TIMEOUT;
  }

}
