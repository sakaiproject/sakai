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

package examples;

import groovy.lang.GroovyClassLoader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import lombok.extern.slf4j.Slf4j;
import org.radeox.macro.Macro;

// cut:start-1
@Slf4j
public class GroovyMacroCompiler  {
 public Macro compileMacro(String macroSource) {
    Macro macro = null;
    try {
      GroovyClassLoader gcl = new GroovyClassLoader();
      InputStream is = new ByteArrayInputStream(
          macroSource.getBytes());
      Class clazz = gcl.parseClass(is, "Macro.groovy");
      Object aScript = clazz.newInstance();
      macro = (Macro) aScript;
    } catch (Exception e) {
      log.error("Cannot compile groovy macro.");
    }
    return macro;
  }
}
// cut:end-1

