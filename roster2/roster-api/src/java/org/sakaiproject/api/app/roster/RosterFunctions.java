/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.api.app.roster;

public interface RosterFunctions {
   public static final String ROSTER_FUNCTION_PREFIX = "roster.";
   
   public static final String ROSTER_FUNCTION_VIEWALL = ROSTER_FUNCTION_PREFIX + "viewallmembers";
   public static final String ROSTER_FUNCTION_VIEWOFFICIALPHOTO = ROSTER_FUNCTION_PREFIX + "viewofficialphoto";
   public static final String ROSTER_FUNCTION_VIEWHIDDEN = ROSTER_FUNCTION_PREFIX + "viewhidden";
   public static final String ROSTER_FUNCTION_EXPORT = ROSTER_FUNCTION_PREFIX + "export";
   public static final String ROSTER_FUNCTION_VIEWGROUP = ROSTER_FUNCTION_PREFIX + "viewgroup";
   public static final String ROSTER_FUNCTION_VIEWENROLLMENTSTATUS = ROSTER_FUNCTION_PREFIX + "viewenrollmentstatus";
   public static final String ROSTER_FUNCTION_VIEWPROFILE = ROSTER_FUNCTION_PREFIX + "viewprofile";
}
