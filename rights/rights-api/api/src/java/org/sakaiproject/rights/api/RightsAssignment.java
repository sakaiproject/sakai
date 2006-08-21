package org.sakaiproject.rights.api;

import java.util.Collection;

import org.sakaiproject.rights.api.Copyright;
import org.sakaiproject.rights.api.CreativeCommonsLicense;

public interface RightsAssignment 
{
	public boolean hasCopyright();
	public Copyright getCopyright();

	public boolean hasCopyrightAlert();

	public boolean hasLicense();
	public int countLicenses();
	public Collection getLicenses();

//	public boolean 
	public void setCopyright(Copyright copyright);
	
	public void addLicense(CreativeCommonsLicense license);
	public void setLicenses(Collection licenses);

}	// interface RightsAssignment

