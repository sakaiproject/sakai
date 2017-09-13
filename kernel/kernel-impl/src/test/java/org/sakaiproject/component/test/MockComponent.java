/**
 * Copyright (c) 2003-2012 The Apereo Foundation
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

package org.sakaiproject.component.test;

import java.util.List;
import java.util.Map;

/**
 *
 */
public class MockComponent implements ITestComponent {
	private String overrideString1;
	private String placeholderString1;
	private String[] stringArrayPlaceholder1;
	private List<String> listOverride1;
	private Map<String, String> mapOverride1;
	private ITestProvider testProvider;
	private String serverId;
	
	public void init() {
	}

	public String getOverrideString1() {
		return overrideString1;
	}

	public void setOverrideString1(String overrideString1) {
		this.overrideString1 = overrideString1;
	}

	public String getPlaceholderString1() {
		return placeholderString1;
	}

	public void setPlaceholderString1(String placeholderString1) {
		this.placeholderString1 = placeholderString1;
	}

	public String[] getStringArrayPlaceholder1() {
		return stringArrayPlaceholder1;
	}

	public void setStringArrayPlaceholder1(String[] stringArrayPlaceholder1) {
		this.stringArrayPlaceholder1 = stringArrayPlaceholder1;
	}

	public List<String> getListOverride1() {
		return listOverride1;
	}

	public void setListOverride1(List<String> listOverride1) {
		this.listOverride1 = listOverride1;
	}

	public Map<String, String> getMapOverride1() {
		return mapOverride1;
	}

	public void setMapOverride1(Map<String, String> mapOverride1) {
		this.mapOverride1 = mapOverride1;
	}

	public ITestProvider getTestProvider() {
		return testProvider;
	}

	public void setTestProvider(ITestProvider testProvider) {
		this.testProvider = testProvider;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

}
