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
