/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2013 The Sakai Foundation
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

package org.sakaiproject.userauditservice.api;

/**
 * <p>
 * UserAuditRegistrationService controls calls relating to tracking user auditing.
 * </p>
 */
public interface UserAuditRegistration
{
	/** One character key a tool will use to show where the change came from */
	public String getDatabaseSourceKey();
	
	/** The text to register that will be associated with the databaseSourceKey */
	String getSourceText(String parameter);
	
	/**
	 * This method will allow registering tools to supply their own location for resource loaders
	 * @param location
	 * @return
	 */
	public Object getResourceLoader(String location);
} 
