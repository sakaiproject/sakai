package org.sakaiproject.tool.resetpass;

import org.sakaiproject.user.api.User;

public class RetUser {

	
	private String email;
	public void setEmail(String e) {
		this.email=e;
	}
	
	public String getEmail(){
		return this.email;
	}
	
	private User user;
	public void setUser(User ue){
		this.user=ue;
	}
	public User getUser() {
		return user;
	}
	
}
