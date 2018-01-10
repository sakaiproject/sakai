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

import java.util.Locale;
import java.util.Enumeration;
import java.util.ResourceBundle;

import lombok.extern.slf4j.Slf4j;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.picocontainer.PicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;
import org.radeox.api.engine.RenderEngine;
import org.radeox.api.engine.context.InitialRenderContext;
import org.radeox.api.engine.context.RenderContext;
import org.radeox.engine.BaseRenderEngine;
import org.radeox.engine.context.BaseRenderContext;
import org.radeox.engine.context.BaseInitialRenderContext;

/**
 * Example which shows howto use Radeox with PicoContainer
 *
 * @author Stephan J. Schmidt
 * @version $Id$
 */
@Slf4j
public class PicoContainerExample extends RadeoxTestSupport {
  public PicoContainerExample(String name) {
    super(name);
  }

  protected void setUp() throws Exception {
    super.setUp();
  }

  public static Test suite() {
    return new TestSuite(PicoContainerExample.class);
  }

  public void testPicoContainer() {
// cut:start-1
    DefaultPicoContainer dc = new DefaultPicoContainer();
    try {
      // Register BaseRenderEngine as an Implementation
      // of RenderEngine
      dc.registerComponentImplementation(
          RenderEngine.class,
          BaseRenderEngine.class);
    } catch (Exception e) {
      log.error("Could not register component.");
    }

    // now only work with container
    PicoContainer container = dc;

    // Only ask for RenderEngine, we automatically
    // get an available object
    // that implements RenderEngine
    RenderEngine engine = (RenderEngine)
      container.getComponentInstance(RenderEngine.class);
    RenderContext context = new BaseRenderContext();
    String result = engine.render("__SnipSnap__", context);
// cut:end-1
    assertEquals("Rendered with PicoContainer.", "<b class=\"bold\">SnipSnap</b>", result);
  }

  public void testPicoWithInitialRenderContext() {
// cut:start-2
    DefaultPicoContainer dc = new DefaultPicoContainer();
    try {
      InitialRenderContext initialContext =
          new BaseInitialRenderContext();
      initialContext.set(RenderContext.OUTPUT_LOCALE,
                         new Locale("mywiki", "mywiki"));
      dc.registerComponentInstance(InitialRenderContext.class,
                                   initialContext);
      dc.registerComponentImplementation(RenderEngine.class,
                                         BaseRenderEngine.class);
    } catch (Exception e) {
      log.error("Could not register component.");
    }
// cut:end-2
    // now only work with container
    PicoContainer container = dc;

    // Only ask for RenderEngine, we automatically
    // get an available object
    // that implements RenderEngine
    RenderEngine engine = (RenderEngine)
      container.getComponentInstance(RenderEngine.class);

    assertNotNull("Component found.", engine);
    RenderContext context = new BaseRenderContext();
    String result = engine.render("__SnipSnap__", context);
    assertEquals("Rendered with PicoContainer and otherwiki Locale.",
                 "<b class=\"mybold\">SnipSnap</b>", result);

  }
}
