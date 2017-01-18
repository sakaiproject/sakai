/**
 * Copyright (c) 2007 The Apereo Foundation
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
package org.sakaiproject.scorm.navigation.impl;

import org.sakaiproject.scorm.navigation.INavigationEvent;

public class NavigationEvent implements INavigationEvent {

	private int event;

	private String choiceEvent;

	public NavigationEvent() {
	}

	public String getChoiceEvent() {
		return choiceEvent;
	}

	public int getEvent() {
		return event;
	}

	public boolean isChoiceEvent() {
		return event == -1;
	}

	public void setChoiceEvent(String choiceEvent) {
		this.choiceEvent = choiceEvent;
	}

	public void setEvent(int event) {
		this.event = event;
	}

}
