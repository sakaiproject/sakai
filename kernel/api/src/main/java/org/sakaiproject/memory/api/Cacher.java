/**********************************************************************************
 * $URL$
 * $Id$
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

package org.sakaiproject.memory.api;

/**
 * Cacher is an interface for any object that uses memory caches<br/>
 * this is the lowest level of caching, {@link Cache} is more functional and
 * should be used instead of this in almost every case
 *
 * @deprecated since 2.9, this will be removed in the next release
 */
public interface Cacher
{
	/**
	 * Clear all entries from the cache (this effectively resets the cache),
     * without notifying listeners
	 * (works like <b>clear</b> from JSR-107 spec)
	 */
	void resetCache();

	/**
	 * Return the size of this cache<br/>
	 * <b>NOTE:</b> This is approximate and may not be completely accurate
	 * 
	 * @return the number of items in this cache
	 */
	long getSize();

	/**
	 * Return a description of this cache<br/>
	 * <b>WARNING:</b> This is costly and should not be called often
	 * 
	 * @return a string which summarizes the state of the cache
	 */
	String getDescription();
}
