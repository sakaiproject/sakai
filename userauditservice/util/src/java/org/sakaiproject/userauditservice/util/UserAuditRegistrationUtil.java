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

package org.sakaiproject.userauditservice.util;

import lombok.Getter;
import lombok.Setter;

import org.sakaiproject.userauditservice.api.UserAuditRegistration;
import org.sakaiproject.userauditservice.api.UserAuditService;
import org.sakaiproject.util.ResourceLoader;

/**
 * UserAuditUtil
 */
public class UserAuditRegistrationUtil implements UserAuditRegistration
{

	// Services needed
	@Setter
    @Getter
    protected UserAuditService userAuditService;

    // Other variables
	@Getter
    @Setter
    private String bundleLocation = "";
	private ResourceLoader rl = null;
	@Setter
    @Getter
    private String databaseSourceKey = "";
	@Setter
    private String sourceText = "";
	
	// flag for telling the UI there's parameters to consider
	@Setter
    @Getter
    public boolean hasParameters = false;
	
	/** UserAuditService init() */
	public void init()
	{
		ResourceLoader loader = getLocalResourceLoader();
		if (loader != null) {
			this.sourceText = loader.getString(getDatabaseSourceKey());
		}
		getUserAuditService().register(this);
	}

    public ResourceLoader getResourceLoader(String location) {
		return new ResourceLoader(location);
	}
	
	/**
	 * Gets the ResourceLoader specified by the bundleLocation.
	 * @return
	 */
	private ResourceLoader getLocalResourceLoader() {
		if (rl == null) {
			rl = (ResourceLoader)getResourceLoader(getBundleLocation());
		}
		return rl;
	}

    public String getSourceText(String parameter) {
		if (isHasParameters())
		{
			return rl.getFormattedMessage(getDatabaseSourceKey(), parameter);
		}
		
		return sourceText;
	}
}
