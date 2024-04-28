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
