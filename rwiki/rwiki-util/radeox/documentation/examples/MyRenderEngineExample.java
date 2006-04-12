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
import org.radeox.api.engine.context.RenderContext;
import org.radeox.engine.context.BaseRenderContext;

/**
 * Example which shows howto use Radeox with PicoContainer
 *
 * @author Stephan J. Schmidt
 * @version $Id$
 */

public class MyRenderEngineExample extends RadeoxTestSupport {
  public MyRenderEngineExample(String name) {
    super(name);
  }

  protected void setUp() throws Exception {
    super.setUp();
  }

  public static Test suite() {
    return new TestSuite(MyRenderEngineExample.class);
  }

  public void testWikiRenderEngine() {
    RenderContext context = new BaseRenderContext();
    RenderEngine engine = new MyWikiRenderEngine();
    context.setRenderEngine(engine);
    String result = engine.render("[stephan] and [leo]", context);
    assertEquals("Rendered wiki correctly.", "<a href=\"/show?wiki=stephan\">stephan</a> and leo<a href=\"/create?wiki=leo\">?</a>", result);
  }

  public void testRenderWithMyRenderEngine() {
// cut:start-1
    RenderContext context = new BaseRenderContext();
    RenderEngine engine = new MyRenderEngine();
    String result = engine.render("X and Y", context);
// cut:end-1
    assertEquals("Rendered correctly.", "Y and Y", result);

  }
}
