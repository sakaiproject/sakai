/**
 * $URL:$
 * $Id:$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
		if(refString != null) {
			String[] parts = refString.split("/");
			return new FakeReference(refString, parts[parts.length-1]);
		}
		return null;
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
