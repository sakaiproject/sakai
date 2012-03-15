/********************************************************************************** 
 * $URL$ 
 * $Id$ 
 *********************************************************************************** 
 * 
 * Copyright (c) 2011 The Sakai Foundation 
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.osedu.org/licenses/ECL-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **********************************************************************************/ 

package org.sakaiproject.dash.entity;

/**
 * 
 * @deprecated
 */
public enum EntityLinkStrategy {
	
	/** 
	 * Open a disclosure in the dashboard with a little info (description, date, etc) 
	 * and possibly a navigation link.
	 * Requires an implementation of org.sakaiproject.dash.entity.EntityType be registered
	 * with org.sakaiproject.dash.logic.DashboardLogic and that org.sakaiproject.dash.entity.EntityType.getProperties() 
	 * returns a mapping of specific key-value pairs.
	 */
	SHOW_PROPERTIES,
	
	/** 
	 * Open a dialog in the dashboard with an HTML fragment provided by some other code. 
	 * Requires that the entity URL provide an HTML fragment that will can be retrieved 
	 * with an AJAX request and  
	 */
	SHOW_DIALOG,
	
	/**  
	 * Open the access-url, which may navigate away from the dashboard. 
	 *  
	 */
	ACCESS_URL
	
}
