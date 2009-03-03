package org.sakaiproject.sitestats.test.mocks;

import java.util.List;

import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.sitestats.test.data.FakeData;

public class FakeEntityManager implements EntityManager {

	public boolean checkReference(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public List getEntityProducers() {
		// TODO Auto-generated method stub
		return null;
	}

	public Reference newReference(String refString) {
		String[] parts = refString.split("/");
		return new FakeReference(refString, parts[parts.length-1]);
	}

	public Reference newReference(Reference arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public List newReferenceList() {
		// TODO Auto-generated method stub
		return null;
	}

	public List newReferenceList(List arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public void registerEntityProducer(EntityProducer arg0, String arg1) {
		// TODO Auto-generated method stub

	}

}
