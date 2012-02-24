/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
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
package org.sakaiproject.section.api.facade.manager;

/**
 * Gets the context for the current request.  This corresponds to the Site context
 * in Sakai.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public interface Context {
	/**
	 * Gets the site context.
	 * 
	 * @param request The object containing the context information.  This will
	 * usually be something like a HttpServletRequest.  In Sakai, the object
	 * can be null.
	 * 
	 * @return
	 */
	public String getContext(Object request);
	
	/**
	 * Gets the label or title for this context.
	 */
	public String getContextTitle(Object request);
}
