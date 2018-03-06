/**
 * $Id$
 * $URL$
 * DeveloperHelperService.java - entity-broker - Apr 13, 2008 5:42:38 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.accountvalidator.tool.producers;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.accountvalidator.model.ValidationAccount;
import org.sakaiproject.accountvalidator.tool.otp.AcountValidationLocator;
import org.sakaiproject.accountvalidator.tool.params.ValidationViewParams;

import uk.org.ponder.messageutil.TargettedMessageList;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.DefaultView;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;

@Slf4j
public class MainProducer implements ViewComponentProducer, DefaultView, ActionResultInterceptor {

	public static final String VIEW_ID = "main";

	public String getViewID() {
		return VIEW_ID;
	}
	
	private TargettedMessageList tml;
	public void setTargettedMessageList(TargettedMessageList tml) {
		this.tml = tml;
	}
	
	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {
		UIForm form = UIForm.make(tofill, "vaildationForm");
		UIInput.make(form, "validationToken","accountValidationLocator." + AcountValidationLocator.NEW_PREFIX + ".validationToken");
		UICommand.make(form, "submit", "accountValidationLocator.getAccount");
		
	}


	public void interceptActionResult(ARIResult result,
			ViewParameters incoming, Object actionReturn) {
		if (actionReturn != null && (actionReturn instanceof ValidationAccount)) {
			log.info("Got the bean!");
			ValidationAccount va = (ValidationAccount) actionReturn;
			result.resultingView = new ValidationViewParams(ValidationProducer.VIEW_ID, va.getValidationToken());
		}
		
	}



}
