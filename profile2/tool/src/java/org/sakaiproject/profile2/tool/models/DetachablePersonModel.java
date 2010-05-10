/**
 * Copyright (c) 2008-2010 The Sakai Foundation
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

package org.sakaiproject.profile2.tool.models;

import org.apache.wicket.model.LoadableDetachableModel;
import org.sakaiproject.profile2.model.Person;

/**
 * Detachable model for an instance of a Person
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class DetachablePersonModel extends LoadableDetachableModel<Person> {
	
	private static final long serialVersionUID = 1L;
	private Person p;
	
	public DetachablePersonModel(Person p) {
		if (p == null)
        {
            throw new IllegalArgumentException();
        }
        this.p = p;
	}

	@Override
	protected Person load() {
		return p;
	}


}
