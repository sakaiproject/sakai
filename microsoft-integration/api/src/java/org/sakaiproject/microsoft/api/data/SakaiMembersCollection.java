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

import java.util.HashSet;
import java.util.Set;

import org.sakaiproject.user.api.User;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SakaiMembersCollection extends GenericMembersCollection{
	public void addMember(String identifier, User member) {
		super.addMember(identifier, member);
	}

	public void addOwner(String identifier, User owner) {
		super.addOwner(identifier, owner);
	}

	public Boolean compareWith(MicrosoftMembersCollection mmc, boolean forced) {
		if (mmc == null) {
			return false;
		}

		Set<String> sakaiMembers = new HashSet<>(getMemberIds());
		Set<String> sakaiOwners = new HashSet<>(getOwnerIds());
		Set<String> guestIds = mmc.getGuestIds();

		sakaiMembers.removeAll(guestIds);
		sakaiOwners.removeAll(guestIds);

		if(forced) {
			return sakaiMembers.equals(mmc.getMemberIds()) && sakaiOwners.equals(mmc.getOwnerIds());
		}

		sakaiMembers.removeAll(mmc.getMemberIds());
		sakaiOwners.removeAll(mmc.getOwnerIds());

		return sakaiMembers.isEmpty() && sakaiOwners.isEmpty();
	}

	public SakaiMembersCollection diffWith(MicrosoftMembersCollection mmc) {
		SakaiMembersCollection ret = new SakaiMembersCollection();
		ret.initFrom(this);
		
		if(mmc != null) {
			if(!ret.members.isEmpty()) {
				ret.members.keySet().removeAll(mmc.getMemberIds());
				
				//also remove guest users
				ret.members.keySet().removeAll(mmc.getGuestIds());
			}

			if(!ret.owners.isEmpty()) {
				ret.owners.keySet().removeAll(mmc.getOwnerIds());
				
				//also remove guest users
				ret.owners.keySet().removeAll(mmc.getGuestIds());
			}
		}

		
		return ret;
	}
}
