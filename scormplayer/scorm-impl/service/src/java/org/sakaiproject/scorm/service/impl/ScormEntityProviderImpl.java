package org.sakaiproject.scorm.service.impl;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.IdEntityReference;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ReferenceParseable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.scorm.entity.api.ScormEntityReference;
import org.sakaiproject.scorm.service.api.ScormEntityProvider;

public class ScormEntityProviderImpl implements ScormEntityProvider, CoreEntityProvider,
		AutoRegisterEntityProvider, ReferenceParseable, Resolvable {
	
	public boolean entityExists(String id) {
		
		//for (int i = 0;i<VALID_IDS.length;i++)
		//	if (VALID_IDS[i].equals(id))
		//		return true;
			
		return true;
	}

	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}

	public Object getEntity(EntityReference reference) {
		String blah = "This is an entity";	
		
		return blah;
	}

	public Class getEntityType() {
		return String.class;
	}

	public EntityReference getParsedExemplar() {
		return new ScormEntityReference("scorm", "resource", "123");
	}

}