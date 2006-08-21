package org.sakaiproject.rights.api;

import java.util.Collection;
import java.util.List;
import java.util.Stack;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Creative Commons Licenses are described by their characteristics, which come in three types:
 * 
 *	Permissions (rights granted by the license)
 *	
 *		Reproduction
 *		    the work may be reproduced
 *		Distribution
 *	    	the work (and, if authorized, derivative works) may be distributed, publicly displayed, and publicly performed
 *		DerivativeWorks
 *		    derivative works may be created and reproduced
 *	
 *	Prohibitions (things prohibited by the license)
 *	
 *		CommercialUse
 *	    	rights may be exercised for commercial purposes
 *	
 *	Requirements (restrictions imposed by the license)
 *	
 *		Notice
 *		    copyright and license notices must be kept intact
 *		Attribution
 *		    credit must be given to copyright holder and/or author
 *		ShareAlike
 *		    derivative works must be licensed under the same terms as the original work
 *		SourceCode
 *		    source code (the preferred form for making modifications) must be provided for all derivative works 
 * 
 *
 */
public interface CreativeCommonsLicense 
{
	public String getIdentifier();
	public String getUri();
	
	public boolean hasPermissions();
	public Collection getPermissions();
	
	public boolean hasProhibitions();
	public Collection getProhibitions();
	
	public boolean hasRequirements();
	public Collection getRequirements();

	public void addPermission(String permission);
	public void setPermissions(Collection permissions);
	
	public void addProhibition(String prohibition);
	public void setProhibitions(Collection prohibitions);
	
	public void addRequirement();
	public void setRequirements(Collection requirements);
	
	// public Element toXml(Document doc, Stack stack);
	
}	// interface CreativeCommonsLicense
