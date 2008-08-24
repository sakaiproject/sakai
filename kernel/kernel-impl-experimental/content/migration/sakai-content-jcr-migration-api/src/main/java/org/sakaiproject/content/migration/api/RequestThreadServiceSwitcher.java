/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.content.migration.api;

/**
 * A RequestTreadServiceSwitcher is a class that posts a request to switch the
 * implementation of a service. The request is acted on by each thread when at
 * the start of the next request processing cycle.
 * 
 * This avoids pausing the service while the change is taking place, and avoids
 * changing the service mid request lifecycle
 * 
 * @author ieb
 */
public interface RequestThreadServiceSwitcher
{

	/**
	 * @param nextService
	 */
	void setNextService(String nextService);

	/**
	 * @return
	 */
	String getNextService();

}
