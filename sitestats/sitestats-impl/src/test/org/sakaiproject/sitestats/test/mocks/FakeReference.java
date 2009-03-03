package org.sakaiproject.sitestats.test.mocks;

import java.util.Collection;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;

public class FakeReference implements Reference {
	String ref;
	String id;

	public FakeReference(String ref, String id) {
		this.ref = ref;
		this.id = id;
	}
	
	public void addSiteContextAuthzGroup(Collection arg0) {
		// TODO Auto-generated method stub

	}

	public void addUserAuthzGroup(Collection arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	public void addUserTemplateAuthzGroup(Collection arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	public Collection getAuthzGroups() {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection getAuthzGroups(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getContainer() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getContext() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	public Entity getEntity() {
		// TODO Auto-generated method stub
		return null;
	}

	public EntityProducer getEntityProducer() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getId() {
		return id;
	}

	public ResourceProperties getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getReference() {
		return ref;
	}

	public String getSubType() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getType() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isKnownType() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean set(String arg0, String arg1, String arg2, String arg3, String arg4) {
		// TODO Auto-generated method stub
		return false;
	}

	public void updateReference(String arg0) {
		// TODO Auto-generated method stub

	}

}
