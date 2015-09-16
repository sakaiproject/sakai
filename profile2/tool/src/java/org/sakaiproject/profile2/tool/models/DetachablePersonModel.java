/**
 * Copyright (c) 2008-2012 The Sakai Foundation
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

import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.model.Person;

/**
 * Detachable model for an instance of a Person
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class DetachablePersonModel extends LoadableDetachableModel<Person> {
	
	private static final long serialVersionUID = 1L;
	private String userUuid;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileLogic")
	protected ProfileLogic profileLogic;
	
	public DetachablePersonModel(Person p) {
		super(p);
		this.userUuid = p.getUuid();
		Injector.get().inject(this);
	}
	
	public DetachablePersonModel(String userUuid) {
		this.userUuid = userUuid;
		Injector.get().inject(this);
	}

	@Override
	protected Person load() {
		return profileLogic.getPerson(userUuid);
	}


}
