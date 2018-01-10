/**********************************************************************************
 * $URL: $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008, 2009 The Sakai Foundation
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
 *
 **********************************************************************************/

package org.sakaiproject.poll.tool.producers;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.poll.tool.params.VoteCollectionViewParameters;

import uk.org.ponder.localeutil.LocaleGetter;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

@Slf4j
public class ConfirmProducer implements ViewComponentProducer, ViewParamsReporter {

	public static final String VIEW_ID = "voteThanks";

	private MessageLocator messageLocator;
	private LocaleGetter localeGetter;

	public String getViewID() {
		// TODO Auto-generated method stub
		return VIEW_ID;
	}

	  public void setMessageLocator(MessageLocator messageLocator) {
			  
		  this.messageLocator = messageLocator;
	  }

    public void setLocaleGetter(LocaleGetter localeGetter) {
        this.localeGetter = localeGetter;
    }
    

	  
	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker arg2) {
		log.debug("fillComponents()");
		
		VoteCollectionViewParameters params = (VoteCollectionViewParameters) viewparams;
		
		
		String voteId; 
		if (params.id != null)
			voteId = params.id;
		else 
			voteId="VoteId is missing!";

		String locale = localeGetter.get().toString();
        Map<String, String> langMap = new HashMap<String, String>();
        langMap.put("lang", locale);
        langMap.put("xml:lang", locale);

		UIOutput.make(tofill, "polls-html", null).decorate(new UIFreeAttributeDecorator(langMap));
		
		UIOutput.make(tofill,"confirm-msg",messageLocator.getMessage("thanks_msg"));
		UIOutput.make(tofill,"confirm-ref-msg",messageLocator.getMessage("thanks_ref"));
		UIOutput.make(tofill,"ref-number",voteId);
		UIForm form = UIForm.make(tofill,"back", new SimpleViewParameters(PollToolProducer.VIEW_ID));
		UICommand.make(form,"cancel",messageLocator.getMessage("thanks_done"),"#{pollToolBean.cancel}");
	}

	


	public ViewParameters getViewParameters() {
		// TODO Auto-generated method stub
		return new VoteCollectionViewParameters(); 
	}


	
}
