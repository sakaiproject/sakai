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
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class MicrosoftMembersCollection extends GenericMembersCollection{
	@Getter protected Map<String, MicrosoftUser> guests = new HashMap<>();
	
	@Override
	public void initFrom(GenericMembersCollection gc) {
		super.initFrom(gc);
		if(gc instanceof MicrosoftMembersCollection) {
			this.guests = new HashMap<>(((MicrosoftMembersCollection)gc).guests);
		}
	}
	
	public void addMember(String identifier, MicrosoftUser member) {
		super.addMember(identifier, member);
	}
	
	public void addOwner(String identifier, MicrosoftUser owner) {
		super.addOwner(identifier, owner);
	}
	
	public void addGuest(String identifier, MicrosoftUser member) {
		guests.put(identifier, member);
	}
	
	public void removeGuest(String id) {
		if(!guests.isEmpty()) {
			guests.remove(id);
		}
	}
	
	public Set<String> getGuestIds() {
		Set<String> ret = new HashSet<>();
		if(!guests.isEmpty()) {
			ret = guests.keySet();
		}
		return ret;
	}
	
	public MicrosoftMembersCollection diffWith(SakaiMembersCollection smc) {
		MicrosoftMembersCollection ret = new MicrosoftMembersCollection();
		ret.initFrom(this);
		
		if(smc != null) {
			if(!ret.members.isEmpty()) {
				ret.members.keySet().removeAll(smc.getMemberIds());
				//sakai users collection does not contain guest users, so nothing here
			}

			if(!ret.owners.isEmpty()) {
				ret.owners.keySet().removeAll(smc.getOwnerIds());
				//sakai users collection does not contain guest users, so nothing here
			}
			
			if(!ret.guests.isEmpty()) {
				//remove all members and owners from guest
				ret.guests.keySet().removeAll(smc.getMemberIds());
				ret.guests.keySet().removeAll(smc.getOwnerIds());
			}
		}

		return ret;
	}
}
