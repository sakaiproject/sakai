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

import org.sakaiproject.site.api.Site;

public class SiteSerialized implements Serializable{
	private String url;
	private String id;
	private String title;
	private String term;
	private String reference;
	private boolean published = false;

	public SiteSerialized(Site site, String termProp){
		this.id = site.getId();
		this.url = site.getUrl();
		this.title = site.getTitle();
		Object prop = site.getProperties().get(termProp);
		term = "";
		if(prop != null){
			term = prop.toString();
		}
		this.reference = site.getReference();
		this.setPublished(site.isPublished());
	}
	
	public SiteSerialized(String id, String title, String term, boolean published){
		this.id = id;
		this.title = title;
		this.url = "/portal/site/" + id;
		this.term = term;
		this.reference = "/site/" + id;
		this.setPublished(published);
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public boolean isPublished() {
		return published;
	}

	public void setPublished(boolean published) {
		this.published = published;
	}
}
