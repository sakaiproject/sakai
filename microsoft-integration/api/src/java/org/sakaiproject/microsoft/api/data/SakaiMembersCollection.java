package org.sakaiproject.microsoft.api.data;

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
		if(mmc != null) {
			Set<String> sakaiMembers = getMemberIds();
			Set<String> sakaiOwners = getOwnerIds();
			if(forced) {
				sakaiMembers.removeAll(mmc.getGuestIds());
				sakaiOwners.removeAll(mmc.getGuestIds());
				return sakaiMembers.equals(mmc.getMemberIds()) && sakaiOwners.equals(mmc.getOwnerIds());
			} else {
				sakaiMembers.removeAll(mmc.getMemberIds());
				sakaiMembers.removeAll(mmc.getGuestIds());
				
				sakaiOwners.removeAll(mmc.getOwnerIds());
				sakaiOwners.removeAll(mmc.getGuestIds());
				
				return sakaiMembers.size() == 0 && sakaiOwners.size() == 0;
			}
		}
		return false;
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
