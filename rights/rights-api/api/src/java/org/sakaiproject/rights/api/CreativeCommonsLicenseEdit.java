package org.sakaiproject.rights.api;

import java.util.Collection;

public interface CreativeCommonsLicenseEdit extends CreativeCommonsLicense 
{
	public void addPermission(String permission);
	public void setPermissions(Collection permissions);
	
	public void addProhibition(String prohibition);
	public void setProhibitions(Collection prohibitions);
	
	public void addRequirement();
	public void setRequirements(Collection requirements);

}	// interface CreativeCommonsLicenseEdit
