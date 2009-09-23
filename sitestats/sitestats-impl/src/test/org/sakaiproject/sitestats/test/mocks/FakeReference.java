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

import java.util.Collection;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;

public class FakeReference implements Reference {
	String ref;
	String id;
	FakeResourceProperties rp;

	public FakeReference(String ref, String id) {
		this.ref = ref;
		this.id = id;
		rp = new FakeResourceProperties(
				ref+"-name", 
				ref.endsWith("/"), 
				ref.endsWith("/")? "folder" : "image/png" 
			);
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
		return rp;
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
		return "http://localhost:8080"+ref;
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
