package org.sakaiproject.scorm.service.api;

public interface ScormPermissionService {
	
	public boolean canModify();
	
	public boolean canConfigure();
	
	public boolean canViewResults();
	
	public boolean canLaunch();
	
	public boolean canDelete();
	
	public boolean canUpload();
	
	public boolean canValidate();
	
	public boolean isOwner();
	
}
