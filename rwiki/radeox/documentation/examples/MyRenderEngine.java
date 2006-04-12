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

import org.radeox.api.engine.RenderEngine;
import org.radeox.api.engine.context.RenderContext;

import java.io.Writer;
import java.io.IOException;

/**
 * Example for a RenderEngine
 *
 * @author Stephan J. Schmidt
 * @version $Id$
 */

// cut:start-1
public class MyRenderEngine implements RenderEngine {
  public String getName() {
     return "my";
  }
  public String render(String content, RenderContext context) {
     return content.replace('X', 'Y'); 
  }
// cut:end-1

  public void render(Writer out, String content, RenderContext context) throws IOException {
    out.write(render(content, context));
  }
}


