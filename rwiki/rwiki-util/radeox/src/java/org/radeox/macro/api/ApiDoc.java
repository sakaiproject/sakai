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

package org.radeox.macro.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Stores information and links to api documentation, e.g. for Java, Ruby, JBoss
 *
 * @author Stephan J. Schmidt
 * @version $Id$
 */

public class ApiDoc {

  private static Log log = LogFactory.getLog(ApiDoc.class);

  private static ApiDoc instance;
  private Map apiDocs;

  public static synchronized ApiDoc getInstance() {
    if (null == instance) {
      instance = new ApiDoc();
    }
    return instance;
  }

  public ApiDoc() {
    apiDocs = new HashMap();

    boolean fileNotFound = false;
    try {
      BufferedReader br = new BufferedReader(
          new InputStreamReader(
              new FileInputStream("conf/apidocs.txt")));
      addApiDoc(br);
    } catch (IOException e) {
      log.warn("Unable to read conf/apidocs.txt");
      fileNotFound = true;
    }

    if (fileNotFound) {
      BufferedReader br = null;
      try {
        br = new BufferedReader(
            new InputStreamReader(
                ApiDoc.class.getResourceAsStream("/conf/apidocs.txt")
            )
        );
        addApiDoc(br);
      } catch (Exception e) {
        log.warn("Unable to read conf/apidocs.txt from jar");
      }
    }
  }

  public void addApiDoc(BufferedReader reader) throws IOException {
    String line;
    while ((line = reader.readLine()) != null) {
      StringTokenizer tokenizer = new StringTokenizer(line, " ");
      String mode = tokenizer.nextToken();
      String baseUrl = tokenizer.nextToken();
      String converterName = tokenizer.nextToken();
      ApiConverter converter = null;
      try {
        converter = (ApiConverter) Class.forName("org.radeox.macro.api." + converterName + "ApiConverter").newInstance();
      } catch (Exception e) {
        log.warn("Unable to load converter: " + converterName + "ApiConverter", e);
      }
      converter.setBaseUrl(baseUrl);
      apiDocs.put(mode.toLowerCase(), converter);
    }
  }

  public boolean contains(String external) {
    return apiDocs.containsKey(external);
  }

  public Writer expand(Writer writer, String className, String mode) throws IOException {
    mode = mode.toLowerCase();
    if (apiDocs.containsKey(mode)) {
      writer.write("<a href=\"");
      ((ApiConverter) apiDocs.get(mode)).appendUrl(writer, className);
      writer.write("\">");
      writer.write(className);
      writer.write("</a>");
    } else {
      log.warn(mode + " not found");
    }
    return writer;
  }

  public Writer appendTo(Writer writer) throws IOException {
    writer.write("{table}\n");
    writer.write("Binding|BaseUrl|Converter Name\n");
    Iterator iterator = apiDocs.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry entry = (Map.Entry) iterator.next();
      writer.write((String) entry.getKey());
      ApiConverter converter = (ApiConverter) entry.getValue();
      writer.write("|");
      writer.write(converter.getBaseUrl());
      writer.write("|");
      writer.write(converter.getName());
      writer.write("\n");
    }
    writer.write("{table}");
    return writer;
  }

}
