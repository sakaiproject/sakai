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

import org.apache.wicket.model.LoadableDetachableModel;

/**
 * Detachable model for an instance of a String, when a List/Dataview uses simple Strings only.
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
public class DetachableStringModel extends LoadableDetachableModel {
	
	private static final long serialVersionUID = 1L;
	private String s;
	
	public DetachableStringModel(String s) {
		if (s == null)
        {
            throw new IllegalArgumentException();
        }
        this.s = s;
	}

	@Override
	protected Object load() {
		return s;
	}


}
