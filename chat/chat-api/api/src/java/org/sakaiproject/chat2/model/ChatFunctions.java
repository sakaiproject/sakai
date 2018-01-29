/**
 * Copyright (c) 2003-2012 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
   public static final String CHAT_FUNCTION_DELETE_PREFIX = CHAT_FUNCTION_PREFIX + "delete.";
   public static final String CHAT_FUNCTION_DELETE_OWN = CHAT_FUNCTION_DELETE_PREFIX + "own";
   public static final String CHAT_FUNCTION_DELETE_ANY = CHAT_FUNCTION_DELETE_PREFIX + "any";
   
   public static final String CHAT_FUNCTION_DELETE_CHANNEL = CHAT_FUNCTION_DELETE_PREFIX + "channel";
   public static final String CHAT_FUNCTION_NEW_CHANNEL = CHAT_FUNCTION_PREFIX + "new.channel";
   public static final String CHAT_FUNCTION_EDIT_CHANNEL = CHAT_FUNCTION_PREFIX + "revise.channel";
}
