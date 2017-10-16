/**
 * Copyright (c) 2005-2016 The Apereo Foundation
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
package org.sakaiproject.tool.assessment.ui.listener.select;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelectRecordedListener implements ValueChangeListener {

	private static Logger log = LoggerFactory.getLogger(SelectRecordedListener.class);

	public void processValueChange(ValueChangeEvent event) throws AbortProcessingException {
		log.debug("Processing ... ValueChangeListener");

		SelectActionListener action = new SelectActionListener();
		action.processAction(new ActionEvent(event.getComponent()));
	}

}
