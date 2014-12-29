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
package org.sakaiproject.profile2.model;

import java.io.Serializable;

import lombok.Data;

/**
 * This is the base model for a Person, containing a limited set of fields. It is extended by Person.
 * 
 * <p>Note about serialisation. The User object is not serialisable and does not contain a no-arg constructor
 * so cannot be manually serialised via the serializable methods (readObject, writeObject).</p> 
 * 
 * <p>Hence why it is not used instead. So the most useful values it provides are extracted and set into this object.</p>
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
@Data
public class BasicPerson implements Serializable, Comparable<Object> {

	private static final long serialVersionUID = 1L;
	
	private String uuid;
	private String displayName;
	private String type;
	
	//default sort
	public int compareTo(Object o) {
		String field = ((BasicPerson)o).getDisplayName();
        int lastCmp = displayName.compareTo(field);
        return (lastCmp != 0 ? lastCmp : displayName.compareTo(field));
	}
}
