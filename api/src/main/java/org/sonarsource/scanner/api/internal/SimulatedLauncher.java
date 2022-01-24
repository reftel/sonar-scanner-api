/*
 * SonarQube Scanner API
 * Copyright (C) 2011-2022 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
package org.sonarsource.scanner.api.internal;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeSet;
import org.sonarsource.scanner.api.internal.batch.IsolatedLauncher;
import org.sonarsource.scanner.api.internal.batch.LogOutput;
import org.sonarsource.scanner.api.internal.cache.Logger;

public class SimulatedLauncher implements IsolatedLauncher {
  private final String version;
  private final Logger logger;

  SimulatedLauncher(String version, Logger logger) {
    this.version = version;
    this.logger = logger;
  }

  @Override
  public void execute(Map<String, String> props, LogOutput logOutput) {
    String filePath = props.get(InternalProperties.SCANNER_DUMP_TO_FILE);
    writeProperties(filePath, props);
    logger.info("Simulation mode. Configuration written to " + new File(filePath).getAbsolutePath());
  }

  private static void writeProperties(String filePath, Map<String, String> p) {
    // This is to have output file content sorted by key
    Properties props = new Properties() {
      @Override
      public synchronized Enumeration<Object> keys() {
        return Collections.enumeration(new TreeSet<Object>(super.keySet()));
      }
    };
    props.putAll(p);
    try (OutputStream outputStream = Files.newOutputStream(Paths.get(filePath))) {
      props.store(outputStream, "# Generated by a SonarScanner");
    } catch (Exception e) {
      throw new IllegalStateException("Fail to export scanner properties", e);
    }
  }

  static void writeProp(BufferedWriter output, Entry<String, String> e) {
    try {
      output.write(e.getKey() + "=" + e.getValue());
      output.newLine();
    } catch (IOException e1) {
      throw new IllegalStateException("Fail to export scanner properties", e1);
    }
  }

  @Override
  public String getVersion() {
    return version;
  }

}
