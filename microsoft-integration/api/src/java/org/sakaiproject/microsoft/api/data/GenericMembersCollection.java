/**
 * Copyright (c) 2024 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.microsoft.api.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Getter;


public class GenericMembersCollection {

	@Getter protected Map<String, Object> members = new HashMap<>();
	@Getter protected Map<String, Object> owners = new HashMap<>();
	
	public void initFrom(GenericMembersCollection gc) {
		this.members = new HashMap<>(gc.members);
		this.owners = new HashMap<>(gc.owners);
	}
	
	public void addMember(String identifier, Object member) {
		members.put(identifier, member);
	}
	
	public void removeMember(String id) {
		if(!members.isEmpty()) {
			members.remove(id);
		}
	}
	
	public Set<String> getMemberIds() {
		Set<String> ret = new HashSet<>();
		if(!members.isEmpty()) {
			ret = members.keySet();
		}
		return ret;
	}
	
	public void addOwner(String identifier, Object owner) {
		owners.put(identifier, owner);
	}
	
	public void removeOwner(String id) {
		if(!owners.isEmpty()) {
			owners.remove(id);
		}
	}
	
	public Set<String> getOwnerIds() {
		Set<String> ret = new HashSet<>();
		if(!owners.isEmpty()) {
			ret = owners.keySet();
		}
		return ret;
	}
}
