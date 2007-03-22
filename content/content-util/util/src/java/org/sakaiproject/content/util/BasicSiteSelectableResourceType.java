package org.sakaiproject.content.util;

import org.sakaiproject.content.api.SiteSpecificResourceType;

public class BasicSiteSelectableResourceType extends BasicResourceType
		implements SiteSpecificResourceType 
{

	protected boolean isEnabledByDefault;

	public BasicSiteSelectableResourceType(String id) 
	{
		super(id);
		// TODO Auto-generated constructor stub
	}

	public boolean isEnabledByDefault() 
	{
		// TODO Auto-generated method stub
		return this.isEnabledByDefault;
	}

	public void setEnabledByDefault(boolean isEnabledByDefault) 
	{
		this.isEnabledByDefault = isEnabledByDefault;
	}

}
