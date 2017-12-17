/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.api.common.manager;

/**
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon</a>
 */
public interface LifeCycleAware
{
	/**
	 * If other Managers are dependent on this manager, they may need to be aware of the initialization state. This can help resolve circular dependencies at startup.
	 * 
	 * @return
	 */
	public boolean isInitialized();

}
