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

import java.io.Serializable;

import org.sakaiproject.user.api.User;

/**
 * A serialized class for wicket to keep track of the user's information when searching for users
 * @author Bryan Holladay (holladay@longsight.com)
 *
 */
public class SearchResult implements Serializable{

	private String eid;
	private String sortName;
	private String email;
	private String type;
	private String id;
	private String displayName;


	public SearchResult(User user){
		if(user != null){
			setId(user.getId());
			setEid(user.getEid());
			setSortName(user.getSortName());
			setEmail(user.getEmail());
			setType(user.getType());
			setDisplayName(user.getDisplayName());
		}
	}

	public void setEid(String eid) {
		this.eid = eid;
	}

	public String getEid() {
		return eid;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSortName() {
		return sortName;
	}

	public void setSortName(String sortName) {
		this.sortName = sortName;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

}
