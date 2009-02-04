package org.sakaiproject.emailtemplateservice.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EmailTemplateLocaleUsers {

	private List<String> userIds = new ArrayList<String>();
	private Locale locale;
	
	public List<String> getUserIds() {
		return userIds;
	}
	public void setUserIds(List<String> userIds) {
		this.userIds = userIds;
	}
	public Locale getLocale() {
		return locale;
	}
	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	
	public void addUser(String userRef) {
		this.userIds.add(userRef);
	}
	

}
