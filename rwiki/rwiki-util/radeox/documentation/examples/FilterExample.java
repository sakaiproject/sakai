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
import org.radeox.api.engine.context.InitialRenderContext;
import org.radeox.api.engine.context.RenderContext;
import org.radeox.engine.context.BaseInitialRenderContext;
import org.radeox.filter.Filter;
import org.radeox.filter.context.BaseFilterContext;

import java.util.Locale;

/**
 * Example for filters
 *
 * @author Stephan J. Schmidt
 * @version $Id$
 */

public class FilterExample extends RadeoxTestSupport {
  public FilterExample(String name) {
    super(name);
  }

  public static Test suite() {
    return new TestSuite(MacroExample.class);
  }

  public void testRenderSquare() {
    Filter filter = new SquareFilter();
    String result = filter.filter(" $3 ", new BaseFilterContext());
    assertEquals("Number squared", " 9 ", result);
  }

  public void testRenderSmiley() {
    Filter filter = new SmileyFilter();
    String result = filter.filter(":-(", new BaseFilterContext());
    assertEquals("Smiley  rendered", ":-)", result);
  }

  public void testRenderSmileyFromLocale() {
    Filter filter = new LocaleSmileyFilter();
    InitialRenderContext context = new BaseInitialRenderContext();
    context.set(RenderContext.INPUT_LOCALE, new Locale("mywiki", "mywiki"));
    context.set(RenderContext.OUTPUT_LOCALE, new Locale("mywiki", "mywiki"));
    filter.setInitialContext(context);

    String result = filter.filter(":-(", new BaseFilterContext());
    assertEquals("Smiley  rendered", ":->", result);
  }
}
