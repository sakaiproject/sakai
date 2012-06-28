package org.sakaiproject.delegatedaccess.model;

import java.util.Date;

public class AccessNode {
	private String userId;
	private String siteRef;
	private String[] access;
	private String[] deniedTools;
	private String auth;
	private Date startDate;
	private Date endDate;
	private Date modified;
	private String modifiedBy;
	
	public AccessNode(String userId, String siteRef, String[] access, String[] deniedTools, String auth, Date startDate,
			Date endDate, Date modified, String modifiedBy){
		this.siteRef = siteRef;
		this.access = access;
		this.deniedTools = deniedTools;
		this.setAuth(auth);
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

	public void setDeniedTools(String[] deniedTools) {
		this.deniedTools = deniedTools;
	}

	public String[] getDeniedTools() {
		return deniedTools;
	}

	public void setAuth(String auth) {
		this.auth = auth;
	}

	public String getAuth() {
		return auth;
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
}
