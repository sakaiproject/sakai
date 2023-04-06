package org.sakaiproject.microsoft.controller.auxiliar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.site.api.Site;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class AutoConfigSessionBean {
	private boolean running = false;
	private int total = 0;
	private int count = -1;
	private List<AutoConfigError> errorList = new ArrayList<>();
	
	private boolean newChannel = false;
	
	@JsonIgnore
	Map<String, Object> confirmMap = new HashMap<>();
	@JsonIgnore
	Map<String, Site> sitesMap = new HashMap<>();
	
	public void startRunning(int total) {
		this.running = true;
		this.total = total;
		this.count = 0;
		this.errorList = new ArrayList<>();
	}
	
	public void finishRunning() {
		this.running = false;
		newChannel = false;
		this.confirmMap = new HashMap<>();
		this.sitesMap = new HashMap<>();
	}
	
	public void increaseCounter() {
		this.count++;
	}
	
	public void addError(String siteId, String siteTitle, String errorMessage) {
		AutoConfigError error = new AutoConfigError();
		error.setSiteId(siteId);
		error.setSiteTitle(siteTitle);
		error.setErrorMessage(errorMessage);
		errorList.add(error);
		increaseCounter();
	}
}
