/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation.
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

package org.sakaiproject.email.impl;

import org.sakaiproject.component.api.ServerConfigurationService;

// import org.sakaiproject.content.api.ContentHostingService;

/**
 * <p>
 * EmailServiceTest extends the basic alias service providing the dependency injectors for testing.
 * This class is here to ensure that abstract methods not implemented by BasicEmailService are not
 * overlooked somehow and create problems on server start. BasicEmailService has abstract methods
 * that are implemented by Spring using lookup-method.
 * </p>
 */
public class EmailServiceTest extends BasicEmailService
{
	/**
	 * @return the ServerConfigurationService collaborator.
	 */
	protected ServerConfigurationService serverConfigurationService()
	{
		return null;
	}
}