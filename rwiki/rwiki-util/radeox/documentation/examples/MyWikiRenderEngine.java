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

import org.radeox.engine.BaseRenderEngine;
import org.radeox.api.engine.WikiRenderEngine;

// cut:start-1
public class MyWikiRenderEngine
    extends BaseRenderEngine
    implements WikiRenderEngine {

  public boolean exists(String name) {
    // make a lookup in your wiki if the page exists
    return "SnipSnap".equals(name) || "stephan".equals(name);
  }

  public boolean showCreate() {
    // we always want to show a create link, not only e.g.
    // if a user is registered
    return true;
  }

  public void appendLink(StringBuffer buffer,
                         String name,
                         String view) {
    buffer.append("<a href=\"/show?wiki=");
    buffer.append(name);
    buffer.append("\">");
    buffer.append(view);
    buffer.append("</a>");
  }

  public void appendLink(StringBuffer buffer,
                         String name,
                         String view,
                         String anchor) {
    buffer.append("<a href=\"/show?wiki=");
    buffer.append(name);
    buffer.append("#");
    buffer.append(anchor);
    buffer.append("\">");
    buffer.append(view);
    buffer.append("</a>");
  }

  public void appendCreateLink(StringBuffer buffer,
                               String name,
                               String view) {
    buffer.append(name);
    buffer.append("<a href=\"/create?wiki=");
    buffer.append(name);
    buffer.append("\">");
    buffer.append("?</a>");
  }

  public String getName() {
    return "my-wiki";
  }
}
// cut:end-1