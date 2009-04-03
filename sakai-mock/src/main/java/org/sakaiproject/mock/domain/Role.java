/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.mock.domain;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Role implements org.sakaiproject.authz.api.Role {
	private static final long serialVersionUID = 1L;

	String id;
	String description;
	Set<String> locks;

	public Role() {
		locks = new HashSet<String>();
	}
	
	public Role(String id) {
		this();
		this.id = id;
	}
	
	public void allowFunction(String lock) {
		locks.add(lock);
	}

	public void allowFunctions(Collection functions) {
		locks.addAll(functions);
	}

	public boolean allowsNoFunctions() {
		return locks.isEmpty();
	}

	public void disallowAll() {
		locks.clear();
	}

	public void disallowFunction(String lock) {
		locks.remove(lock);
	}

	public void disallowFunctions(Collection functions) {
		locks.removeAll(functions);
	}

	public Set getAllowedFunctions() {
		return locks;
	}

	public boolean isAllowed(String function) {
		return locks.contains(function);
	}

	public boolean isProviderOnly() {
		return false;
	}

	public void setProviderOnly(boolean providerOnly) {
	}

	public int compareTo(Object o) {
		return id.compareTo((String)o);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Set<String> getLocks() {
		return locks;
	}

	public void setLocks(Set<String> locks) {
		this.locks = locks;
	}

}
