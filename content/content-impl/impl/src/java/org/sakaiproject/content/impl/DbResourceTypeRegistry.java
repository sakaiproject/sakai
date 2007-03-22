package org.sakaiproject.content.impl;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ResourceType;

public class DbResourceTypeRegistry extends ResourceTypeRegistryImpl 
{
	/** Our logger. */
	protected static Log M_log = LogFactory.getLog(ResourceTypeRegistryImpl.class);

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ResourceTypeRegistry#getTypes(java.lang.String)
	 */
	public Collection<ResourceType> getTypes(String context) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ResourceTypeRegistry#setResourceTypesForContext(java.lang.String, java.util.Map)
	 */
	public void setResourceTypesForContext(String context, Map<String, Boolean> enabled) 
	{
		
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		try
		{
			M_log.info("init()");
		}
		catch (Throwable t)
		{
		}
	}

}
