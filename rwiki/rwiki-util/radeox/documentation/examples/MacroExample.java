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

import lombok.extern.slf4j.Slf4j;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.radeox.macro.Macro;
import org.radeox.macro.parameter.BaseMacroParameter;
import org.radeox.macro.parameter.MacroParameter;
import org.radeox.api.engine.context.InitialRenderContext;
import org.radeox.engine.context.BaseInitialRenderContext;

import java.io.*;

/**
 * Example for a HelloWorldMacro
 *
 * @author Stephan J. Schmidt
 * @version $Id$
 */
@Slf4j
public class MacroExample extends RadeoxTestSupport {
  public MacroExample(String name) {
    super(name);
  }

  public static Test suite() {
    return new TestSuite(MacroExample.class);
  }

  public void testRenderHelloWorld() {
    Macro macro = new HelloWorldMacro();
    StringWriter writer = new StringWriter();
    try {
      macro.execute(writer, new BaseMacroParameter());
    } catch (IllegalArgumentException e) {
      log.error(e.getMessage(), e);  //To change body of catch statement use Options | File Templates.
    } catch (IOException e) {
      //
    }
    assertEquals("Hello world rendered", "hello world", writer.toString());

  }

//  public void testGroovyMacro() {
//    GroovyMacroCompiler compiler = new GroovyMacroCompiler();
//    StringBuffer contentOfFile = new StringBuffer();
//    try {
//      BufferedReader br = new BufferedReader(
//          new InputStreamReader(
//              new FileInputStream("GroovyMacro.groovy")));
//
//      String line;
//      while ((line = br.readLine()) != null) {
//        contentOfFile.append(line);
//      }
//    } catch (IOException e) {
//      log.error(e.getMessage(), e);
//    }
//    String content = contentOfFile.toString();
//    Macro macro = compiler.compileMacro(content);
//    assertNotNull("Groovy Macro did compile.", macro);
//  }

  public void testCompiledGroovyMacro() {
    Macro macro = new GroovyMacro();

    StringWriter writer = new StringWriter();
    try {
      MacroParameter params = new BaseMacroParameter();
      macro.execute(writer, params);
    } catch (IllegalArgumentException e) {
      log.error(e.getMessage(), e);  //To change body of catch statement use Options | File Templates.
    } catch (IOException e) {
      //
    }
    assertEquals("Hello world from Groovy is rendered", "Yipee ay ey, schweinebacke", writer.toString());
  }

  public void testRenderHelloWorldWithIntialRenderContext() {
    Macro macro = new InitialRenderContextHelloWorldMacro();
    StringWriter writer = new StringWriter();
    try {
      MacroParameter params = new BaseMacroParameter();
      InitialRenderContext context = new BaseInitialRenderContext();
      context.set("hello.name", "stephan");
      macro.setInitialContext(context);
      macro.execute(writer, params);
    } catch (IllegalArgumentException e) {
      log.error(e.getMessage(), e);  //To change body of catch statement use Options | File Templates.
    } catch (IOException e) {
      //
    }
    assertEquals("Hello world with InitialRenderContext rendered", "hello stephan", writer.toString());

  }

  public void testRenderHelloWorldWithParameter() {
    Macro macro = new ParameterHelloWorldMacro();
    StringWriter writer = new StringWriter();
    try {
      MacroParameter params = new BaseMacroParameter();
      params.setParams("stephan");
      macro.execute(writer, params);
    } catch (IllegalArgumentException e) {
      log.error(e.getMessage(), e);  //To change body of catch statement use Options | File Templates.
    } catch (IOException e) {
      //
    }
    assertEquals("Hello world with parameter rendered", "Hello <b>stephan</b>", writer.toString());

  }

}
