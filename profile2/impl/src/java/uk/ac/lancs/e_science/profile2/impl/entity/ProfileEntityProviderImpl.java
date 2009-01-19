package uk.ac.lancs.e_science.profile2.impl.entity;

import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import uk.ac.lancs.e_science.profile2.api.entity.ProfileEntityProvider;

public class ProfileEntityProviderImpl implements ProfileEntityProvider, CoreEntityProvider, AutoRegisterEntityProvider  {
	
	public boolean entityExists(String id) {
	    return true;
	}

	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}
}
