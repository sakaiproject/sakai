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
