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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.search.model;

import java.util.Date;

/**
 * @author ieb
 */
public interface SearchWriterLock
{
	String getId();

	void setId(String id);


	/**
	 * the name of the node holding the lock
	 * 
	 * @return
	 */
	String getNodename();

	/**
	 * the name of the node holding the lock
	 * 
	 * @param nodeName
	 */
	void setNodename(String nodeName);

	/**
	 * The name of the lock
	 * 
	 * @param lockkey
	 */
	void setLockkey(String lockkey);

	/**
	 * The name of the lock
	 * 
	 * @return
	 */
	String getLockkey();

	/**
	 * The date when the lock will expire
	 * @param expires
	 */
	void setExpires(Date expires);
	
	/**
	 * The date when the lock will expire
	 * @return
	 */
	Date getExpires();

}
