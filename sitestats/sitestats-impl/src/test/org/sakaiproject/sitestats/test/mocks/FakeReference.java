/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.test.mocks;

import org.mockito.Mockito;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;

public abstract class FakeReference implements Reference {
	String ref;
	String id;
	FakeResourceProperties rp;

	public FakeReference set(String ref, String id) {
		this.ref = ref;
		this.id = id;
		rp = Mockito.spy(FakeResourceProperties.class);
		rp.set(
				ref+"-name", 
				ref.endsWith("/"), 
				ref.endsWith("/")? "folder" : "image/png" 
			);
		return this;
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

	public String getUrl() {
		return "http://localhost:8080"+ref;
	}

}
