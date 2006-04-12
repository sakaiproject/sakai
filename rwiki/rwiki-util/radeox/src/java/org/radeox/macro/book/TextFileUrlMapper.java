/*
 * This file is part of "SnipSnap Radeox Rendering Engine".
 *
 * Copyright (c) 2002 Stephan J. Schmidt, Matthias L. Jugel
 * All Rights Reserved.
 *
 * Please visit http://radeox.org/ for updates and contact.
 *
 * --LICENSE NOTICE--
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * --LICENSE NOTICE--
 */

package org.radeox.macro.book;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.radeox.util.Encoder;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Manages links to keys, mapping is read from a text file
 *
 * @author Stephan J. Schmidt
 * @version $Id$
 */

public abstract class TextFileUrlMapper implements UrlMapper {
  private static Log log = LogFactory.getLog(TextFileUrlMapper.class);

  private Map services;

  public abstract String getFileName();

  public abstract String getKeyName();

  public TextFileUrlMapper(Class klass) {
    services = new HashMap();

    boolean fileNotFound = false;
    try {
      BufferedReader br = new BufferedReader(
          new InputStreamReader(
              new FileInputStream(getFileName())));
      addMapping(br);
    } catch (IOException e) {
      log.warn("Unable to read " + getFileName());
      fileNotFound = true;
    }

    if (fileNotFound) {
      BufferedReader br = null;
      try {
        br = new BufferedReader(
            new InputStreamReader(
                klass.getResourceAsStream("/"+getFileName())));
        addMapping(br);
      } catch (Exception e) {
        log.warn("Unable to read /" + getFileName() + " from jar");
      }
    }
  }

  public void addMapping(BufferedReader reader) throws IOException {
    String line;
    while ((line = reader.readLine()) != null) {
      if (!line.startsWith("#")) {
        int index = line.indexOf(" ");
        services.put(line.substring(0, index), Encoder.escape(line.substring(index + 1)));
      }
    }
  }

  public Writer appendTo(Writer writer) throws IOException {
    Iterator iterator = services.entrySet().iterator();
    writer.write("{table}\n");
    writer.write("Service|Url\n");
    while (iterator.hasNext()) {
      Map.Entry entry = (Map.Entry) iterator.next();
      writer.write((String) entry.getKey());
      writer.write("|");
      writer.write((String) entry.getValue());
      writer.write("\n");
    }
    writer.write("{table}");
    return writer;
  }

  public boolean contains(String
      external) {
    return services.containsKey(external);
  }

  public Writer appendUrl(Writer writer, String key) throws IOException {
    if (services.size() == 0) {
      writer.write(getKeyName());
      writer.write(":");
      writer.write(key);
    } else {
      //SnipLink.appendImage(writer, "external-link", "&gt;&gt;");
      writer.write("(");
      Iterator iterator = services.entrySet().iterator();
      while (iterator.hasNext()) {
        Map.Entry entry = (Map.Entry) iterator.next();
        writer.write("<a href=\"");
        writer.write((String) entry.getValue());
        writer.write(key);
        writer.write("\">");
        writer.write((String) entry.getKey());
        writer.write("</a>");
        if (iterator.hasNext()) {
          writer.write(" &#x7c; ");
        }
      }
      writer.write(")");
    }
    return writer;
  }
}
