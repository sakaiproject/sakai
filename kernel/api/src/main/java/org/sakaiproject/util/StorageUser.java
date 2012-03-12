/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/kernel-util/src/main/java/org/sakaiproject/util/StorageUser.java $
 * $Id: StorageUser.java 74692 2010-03-16 13:58:08Z david.horwitz@uct.ac.za $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.util;


/**
 * This interface has had it's members extracted into two parent interfaces and 
 * code should use one of those interfaces instead.
 * This interface is deprecated to ease the cleanup and to rename the interfaces
 * so that they are more clearly associated with the relevant implementations.
 * 
 * @deprecated Use {@link DoubleStorageUser}/{@link SingleStorageUser}.
 */
public interface StorageUser extends DoubleStorageUser
{

}
