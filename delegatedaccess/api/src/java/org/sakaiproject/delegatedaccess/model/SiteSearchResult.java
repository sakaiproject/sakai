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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.User;

public class SiteSearchResult implements Serializable {
	private SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
	private SimpleDateFormat formatWithTime = new SimpleDateFormat("MM/dd/yyyy h:mm a");
	private SiteSerialized site;
	private List<UserSerialized> instructors = new ArrayList();
	private String[] access;
	private Date shoppingPeriodStartDate;
	private Date shoppingPeriodEndDate;
	private String[] restrictedAuthTools;
	private String[] restrictedPublicTools;
	private Date modified;
	private String modifiedBy;
	private String modifiedBySortName;
	private boolean hasInstructor = true;
	private String providers = "";
	
	public SiteSearchResult(Site site, List<User> instructors, String termProp){
		this.site = new SiteSerialized(site, termProp);
		this.instructors = new ArrayList<UserSerialized>();
		for(User user : instructors){
			this.instructors.add(new UserSerialized(user));
		}
	}
	
	public SiteSearchResult(SiteSerialized site, List<User> instructors, String termProp){
		this.site = site;
		this.instructors = new ArrayList<UserSerialized>();
		for(User user : instructors){
			this.instructors.add(new UserSerialized(user));
		}
	}
	
	public SiteSerialized getSite() {
		return site;
	}
	public List<UserSerialized> getInstructors() {
		return instructors;
	}
	public void setInstructors(List<UserSerialized> instructors) {
		this.instructors = instructors;
	}
	
	public void addInstructor(User user){
		instructors.add(new UserSerialized(user));
	}
	
	public String getInstructorsString(){
		String instructors = "";
		for(UserSerialized user : getInstructors()){
			if(!"".equals(instructors)){
				instructors += "; ";
			}
			instructors += user.getSortName();
		}
		return instructors;
	}
	public String[] getAccess() {
		return access;
	}
	public void setAccess(String[] access) {
		this.access = access;
	}
	public String getAccessString(){
		return getAccess() != null && getAccess().length == 2 ? getAccess()[0] + ":" + getAccess()[1] : "";
	}

	public String getAccessRoleString(){
		return getAccess() != null && getAccess().length == 2 ? getAccess()[1] : "";
	}
	
	public Date getShoppingPeriodStartDate() {
		return shoppingPeriodStartDate;
	}
	public void setShoppingPeriodStartDate(Date shoppingPeriodStartDate) {
		this.shoppingPeriodStartDate = shoppingPeriodStartDate;
	}
	public Date getShoppingPeriodEndDate() {
		return shoppingPeriodEndDate;
	}
	public void setShoppingPeriodEndDate(Date shoppingPeriodEndDate) {
		this.shoppingPeriodEndDate = shoppingPeriodEndDate;
	}
	
	public String getShoppingPeriodStartDateStr(){
		if(getShoppingPeriodStartDate() == null){
			return "";
		}else{
			return format.format(getShoppingPeriodStartDate());
		}
	}

	public String getShoppingPeriodEndDateStr(){
		if(getShoppingPeriodEndDate() == null){
			return "";
		}else{
			return format.format(getShoppingPeriodEndDate());
		}
	}

	public String getSiteTerm(){
		Object prop = getSite().getTerm();
		return prop == null ? "" : prop.toString();
	}
	public String[] getRestrictedAuthTools() {
		return restrictedAuthTools;
	}
	public void setRestrictedAuthTools(String[] restrictedTools) {
		this.restrictedAuthTools = restrictedTools;
	}
	
	public String getAuthToolsString(Map<String, String> toolsMap){
		String restrictedToolsStr = "";
		if(getRestrictedAuthTools() != null){
			for(String tool : getRestrictedAuthTools()){
				if(!"".equals(restrictedToolsStr)){
					restrictedToolsStr += ", ";
				}
				String toolName = tool;
				if(toolsMap.containsKey(toolName)){
					toolName = toolsMap.get(toolName);
				}
				restrictedToolsStr += toolName;
			}
		}
		return restrictedToolsStr;
	}
	public String[] getRestrictedPublicTools() {
		return restrictedPublicTools;
	}
	public void setRestrictedPublicTools(String[] restrictedTools) {
		this.restrictedPublicTools = restrictedTools;
	}
	
	public String getPublicToolsString(Map<String, String> toolsMap){
		String restrictedToolsStr = "";
		if(getRestrictedPublicTools() != null){
			for(String tool : getRestrictedPublicTools()){
				if(!"".equals(restrictedToolsStr)){
					restrictedToolsStr += ", ";
				}
				String toolName = tool;
				if(toolsMap.containsKey(toolName)){
					toolName = toolsMap.get(toolName);
				}
				restrictedToolsStr += toolName;
			}
		}
		return restrictedToolsStr;
	}
	
	public String getSiteUrl(){
		return getSite().getUrl();
	}
	
	public String getSiteTitle(){
		return getSite().getTitle();
	}
	
	public String getSiteId(){
		return getSite().getId();
	}
	
	public String getSiteReference(){
		return getSite().getReference();
	}
	
	public boolean isSitePublished(){
		return getSite().isPublished();
	}
	
	public class UserSerialized implements Serializable{

		private String userId;
		private String displayName;
		private String sortName;
		
		public UserSerialized(User user){
			this.userId = user.getId();
			this.displayName = user.getDisplayName();
			this.sortName = user.getSortName();
		}

		public String getUserId() {
			return userId;
		}

		public void setUserId(String userId) {
			this.userId = userId;
		}

		public String getDisplayName() {
			return displayName;
		}

		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}

		public String getSortName() {
			return sortName;
		}

		public void setSortName(String sortName) {
			this.sortName = sortName;
		}
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
	public String getModifiedBySortName() {
		return modifiedBySortName;
	}
	public void setModifiedBySortName(String modifiedBySortName) {
		this.modifiedBySortName = modifiedBySortName;
	}
	
	public String getModifiedStr(){
		if(getModified() == null){
			return "";
		}else{
			return formatWithTime.format(getModified());
		}
	}
	public void setHasInstructor(boolean hasInstructor) {
		this.hasInstructor = hasInstructor;
	}
	public boolean isHasInstructor() {
		return hasInstructor;
	}

	public String getProviders() {
		return providers;
	}

	public void setProviders(String providers) {
		if(providers == null){
			providers = "";
		}
		this.providers = providers;
	}
}
