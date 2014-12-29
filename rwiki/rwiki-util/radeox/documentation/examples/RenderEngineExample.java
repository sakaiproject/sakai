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

import junit.framework.Test;
import junit.framework.TestSuite;
import org.radeox.api.engine.RenderEngine;
import org.radeox.api.engine.context.InitialRenderContext;
import org.radeox.api.engine.context.RenderContext;
import org.radeox.engine.BaseRenderEngine;
import org.radeox.engine.context.BaseInitialRenderContext;
import org.radeox.engine.context.BaseRenderContext;

import java.util.Locale;

/**
 * Example which shows howto use Radeox with PicoContainer
 *
 * @author Stephan J. Schmidt
 * @version $Id$
 */

public class RenderEngineExample extends RadeoxTestSupport {

  public RenderEngineExample(String name) {
    super(name);
  }

  protected void setUp() throws Exception {
    super.setUp();
  }

  public static Test suite() {
    return new TestSuite(RenderEngineExample.class);
  }

  public void testRender() {
// cut:start-2
    RenderContext context = new BaseRenderContext();
    RenderEngine engine = new BaseRenderEngine();
    String result = engine.render("__Radeox__", context);
// cut:end-2
    assertEquals("Rendered correctly.", "<b class=\"bold\">Radeox</b>", result);

  }

//  public String getName() {
//     return super.getName().substring(4).replaceAll("([A-Z])", " $1").toLowerCase();
//  }

  public void testRenderWithContext() {
// cut:start-1
    InitialRenderContext initialContext =
        new BaseInitialRenderContext();
    initialContext.set(RenderContext.INPUT_LOCALE,
                       new Locale("mywiki", "mywiki"));
    RenderEngine engineWithContext =
      new BaseRenderEngine(initialContext);
    String result = engineWithContext.render(
        "__Radeox__",
        new BaseRenderContext());
// cut:end-1
    assertEquals("Rendered with context.", "<b class=\"bold\">Radeox</b>", result);
  }
}
