/**********************************************************************************
* $URL: https://source.sakaiproject.org/svn/osp/trunk/reports/api/src/java/org/theospi/portfolio/reports/model/ReportFunctions.java $
* $Id: ReportFunctions.java 9134 2006-05-08 20:28:42Z chmaurer@iupui.edu $
***********************************************************************************
*
* Copyright (c) 2005, 2006 The Sakai Foundation.
*
* Licensed under the Educational Community License, Version 1.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.opensource.org/licenses/ecl1.php
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
**********************************************************************************/
package org.sakaiproject.chat2.model;

/**
 *   These are the permissions for reporting.  Reporting uses the sakai permission manager
 *   and not the osp permission manager.  Meta object uses the sakai permission manager as well.
 *   The labels on the page are drawn from the function string minus the prefix.
 *   
 *   Apparently, when labelling a permission with more than one word, then a period is used as 
 *   the spacer.
 *
 * User: John Ellis
 * Date: Jan 7, 2006
 * Time: 12:43:30 AM
 * To change this template use File | Settings | File Templates.
 */
public interface ChatFunctions {
   public static final String CHAT_FUNCTION_PREFIX = "chat.";
   
   public static final String CHAT_FUNCTION_READ = CHAT_FUNCTION_PREFIX + "read";
   public static final String CHAT_FUNCTION_NEW = CHAT_FUNCTION_PREFIX + "new";
   public static final String CHAT_FUNCTION_DELETE_OWN = CHAT_FUNCTION_PREFIX + "delete.own";
   public static final String CHAT_FUNCTION_DELETE_ANY = CHAT_FUNCTION_PREFIX + "delete.any";
}
