/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 Sakai Foundation
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

package org.sakaiproject.rights.api;

import java.util.Collection;

import org.sakaiproject.rights.api.Copyright;
import org.sakaiproject.rights.api.CreativeCommonsLicense;

public interface RightsAssignment 
{
	public String getRightsId();
	public String getEntityRef();
	
	public boolean hasCopyright();
	public Copyright getCopyright();

	public boolean hasCopyrightAlert();

	public boolean hasLicense();
	public int countLicenses();
	public Collection<CreativeCommonsLicense> getLicenses();

	public void setCopyright(Copyright copyright);
	
	public void addLicense(CreativeCommonsLicense license);
	public void setLicenses(Collection<CreativeCommonsLicense> licenses);

}	// interface RightsAssignment

