package org.sakaiproject.scorm.client;

import org.sakaiproject.scorm.client.api.ScormClientFacade;

public class SimpleBean {

	private String value;
	private ScormClientFacade scormClientService;
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public ScormClientFacade getScormClientService() {
		return scormClientService;
	}

	public void setScormClientService(ScormClientFacade scormClientService) {
		this.scormClientService = scormClientService;
	}
	
}
