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
	private String shoppingPeriodAuth;
	private Date shoppingPeriodStartDate;
	private Date shoppingPeriodEndDate;
	private String[] restrictedTools;
	private Date modified;
	private String modifiedBy;
	private String modifiedBySortName;
	
	public SiteSearchResult(Site site, List<User> instructors, String termProp){
		this.site = new SiteSerialized(site, termProp);
		this.instructors = new ArrayList<UserSerialized>();
		for(User user : instructors){
			this.instructors.add(new UserSerialized(user));
		}
	}
	private SiteSerialized getSite() {
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
	
	public String getShoppingPeriodAuth() {
		return shoppingPeriodAuth;
	}
	public void setShoppingPeriodAuth(String shoppingPeriodAuth) {
		this.shoppingPeriodAuth = shoppingPeriodAuth;
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
	public String[] getRestrictedTools() {
		return restrictedTools;
	}
	public void setRestrictedTools(String[] restrictedTools) {
		this.restrictedTools = restrictedTools;
	}
	
	public String getToolsString(Map<String, String> toolsMap){
		String restrictedToolsStr = "";
		if(getRestrictedTools() != null){
			for(String tool : getRestrictedTools()){
				if(!"".equals(restrictedTools)){
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
	
	public class SiteSerialized implements Serializable{
		private String url;
		private String id;
		private String title;
		private String term;
		private String reference;

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
}
