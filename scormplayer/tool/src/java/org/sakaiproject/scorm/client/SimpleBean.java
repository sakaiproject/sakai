package org.sakaiproject.scorm.client;

import org.sakaiproject.scorm.client.api.ScormClientService;

public class SimpleBean {

	private String value;
	private ScormClientService scormClientService;
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public ScormClientService getScormClientService() {
		return scormClientService;
	}

	public void setScormClientService(ScormClientService scormClientService) {
		this.scormClientService = scormClientService;
	}
	
}
