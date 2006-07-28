package org.sakaiproject.rights.api;

import java.util.Collection;

import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.rights.api.Copyright;
import org.sakaiproject.rights.api.CopyrightEdit;
import org.sakaiproject.rights.api.CreativeCommonsLicense;

import org.sakaiproject.rights.api.RightsAssignment;

public interface RightsAssignmentEdit extends Edit, RightsAssignment 
{
	public void setCopyright(Copyright copyright);
	public CopyrightEdit editCopyright();
	
	public void addLicense(CreativeCommonsLicense license);
	public void setLicenses(Collection licenses);
	public CreativeCommonsLicenseEdit editLicense(String identifier);

}	// interface RightsAssignmentEdit
