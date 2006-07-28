package org.sakaiproject.rights.api;

import java.util.Collection;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.rights.api.Copyright;

public interface RightsAssignment extends Entity
{
	public boolean hasCopyright();
	public Copyright getCopyright();

	public boolean hasCopyrightAlert();

	public boolean hasLicense();
	public int countLicenses();
	public Collection getLicenses();

//	public boolean 


}	// interface RightsAssignment

