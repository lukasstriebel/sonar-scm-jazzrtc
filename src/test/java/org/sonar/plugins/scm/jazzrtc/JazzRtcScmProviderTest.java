/*
 * SonarQube :: Plugins :: SCM :: Jazz RTC
 * Copyright (C) 2014 SonarSource
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.scm.jazzrtc;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.fest.assertions.Assertions.assertThat;

public class JazzRtcScmProviderTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void sanityCheck() {
    assertThat(new JazzRtcScmProvider(null).key()).isEqualTo("jazz");
  }

  @Test
  public void testAutodetection() throws IOException {
    File baseDirEmpty = temp.newFolder();
    assertThat(new JazzRtcScmProvider(null).supports(baseDirEmpty)).isFalse();

    File jarrBaseDir = temp.newFolder();
    new File(jarrBaseDir, ".jazz5").mkdir();
    assertThat(new JazzRtcScmProvider(null).supports(jarrBaseDir)).isTrue();
  }

}
