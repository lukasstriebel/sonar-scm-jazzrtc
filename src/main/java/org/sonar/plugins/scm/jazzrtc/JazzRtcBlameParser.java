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

import org.json.JSONArray;
import org.json.JSONObject;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.api.batch.scm.BlameLine;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class JazzRtcBlameParser {

  private static final Logger LOG = Loggers.get(JazzRtcBlameParser.class);
  private static final String JAZZ_TIMESTAMP_PATTERN = "yyyy-MM-dd hh:mm a";

  private DateFormat format;
  private final String filename;
  private final UserNameResolver userNameResolver;

  public JazzRtcBlameParser(String filename, JazzRtcConfiguration configuration) {
    this.filename = filename;
    this.format = new SimpleDateFormat(JAZZ_TIMESTAMP_PATTERN, Locale.ENGLISH);
    this.userNameResolver = new UserNameResolver(configuration);
  }

  public List<BlameLine> parse(String output) {
    List<BlameLine> lines = new ArrayList<>();
    //LOG.info("Output: " + output);
    JSONObject outputObject = new JSONObject(output);
    JSONArray annotations = outputObject.getJSONArray("annotations");

    for (int i = 0; i < annotations.length(); ++i) {
        JSONObject annotation = annotations.getJSONObject(i);
        int lineNumber = annotation.getInt("line-no");
        String author = annotation.getString("author");
        Date modifiedDate = parseDate(annotation.getString("modified"));
        String changesetId = annotation.getString("uuid");
        String workitem = annotation.getString("workitem");

        int expectedLine = i + 1;
        if(lineNumber != expectedLine) {
          throw new IllegalStateException("Unable to blame file " + filename + ". Expecting blame info for line " + expectedLine + " but was " + lineNumber);
        }

        String changeset = String.format("%s (%s)", workitem, changesetId);
        String username = userNameResolver.findUserIdByName(author);
        BlameLine line = new BlameLine().date(modifiedDate).revision(changeset).author(username);
        lines.add(line);
    }

    return lines;
  }

  /**
   * Converts the date timestamp from the output into a date object.
   *
   * @return A date representing the timestamp of the log entry.
   */
  private Date parseDate(String date) {
    try {
      return format.parse(date);
    } catch (ParseException e) {
      LOG.warn(
        "skip ParseException: " + e.getMessage() + " during parsing date " + date
          + " with pattern " + JAZZ_TIMESTAMP_PATTERN + " with Locale " + Locale.ENGLISH, e);
      return null;
    }
  }
}
