/*
* The Trustees of Columbia University in the City of New York
* licenses this file to you under the Educational Community License,
* Version 2.0 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of the
* License at:
*
* http://opensource.org/licenses/ecl2.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.delegatedaccess.model;

import java.util.Date;

public class AccessNode {
	private String userId;
	private String siteRef;
	private String[] access;
	private String[] deniedAuthTools;
	private String[] deniedPublicTools;
	private Date startDate;
	private Date endDate;
	private Date modified;
	private String modifiedBy;
	
	public AccessNode(String userId, String siteRef, String[] access, String[] deniedAuthTools, String[] deniedPublicTools, Date startDate,
			Date endDate, Date modified, String modifiedBy){
		this.siteRef = siteRef;
		this.access = access;
		this.deniedAuthTools = deniedAuthTools;
		this.setDeniedPublicTools(deniedPublicTools);
		this.setStartDate(startDate);
		this.setEndDate(endDate);
		this.setModified(modified);
		this.setModifiedBy(modifiedBy);
		this.userId = userId;
	}

	public void setSiteRef(String siteRef) {
		this.siteRef = siteRef;
	}

	public String getSiteRef() {
		return siteRef;
	}

	public void setAccess(String[] access) {
		this.access = access;
	}

	public String[] getAccess() {
		return access;
	}

	public void setDeniedAuthTools(String[] deniedAuthTools) {
		this.deniedAuthTools = deniedAuthTools;
	}

	public String[] getDeniedAuthTools() {
		return deniedAuthTools;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserId() {
		return userId;
	}

	public void setDeniedPublicTools(String[] deniedPublicTools) {
		this.deniedPublicTools = deniedPublicTools;
	}

	public String[] getDeniedPublicTools() {
		return deniedPublicTools;
	}
}
