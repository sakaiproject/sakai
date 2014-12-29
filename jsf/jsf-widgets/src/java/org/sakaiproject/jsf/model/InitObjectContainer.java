/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/
package org.sakaiproject.jsf.model;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: John Ellis
 * Date: Nov 21, 2005
 * Time: 4:19:36 PM
 * To change this template use File | Settings | File Templates.
 */

/**
 * Implement this interface for a component that collapses a div.
 * Any children that require re-init during div collapsing will pass a script
 * These scripts must be executed during collapsing and expanding the div
 */
public interface InitObjectContainer {

   public void addInitScript(String script);

   public List getInitScripts();

}
