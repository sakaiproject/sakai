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

package org.sakaiproject.poll.tool.validators;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.sakaiproject.poll.logic.ExternalLogic;
import org.sakaiproject.poll.model.Option;
import org.sakaiproject.poll.util.PollUtils;


@Slf4j
public class OptionValidator implements Validator {

	public String submissionStatus;
	@Setter private ExternalLogic externalLogic;
	
	public boolean supports(Class clazz) {
		return clazz.equals(Option.class);
	}

	public void validate(Object obj, Errors errors) {

		Option option = (Option) obj;
		
		// SAK-14725 : BugFix
		String stripText = null;
		
		if(null != option.getText()) {
			stripText = externalLogic.convertFormattedTextToPlaintext(option.getText()).trim();
		}
		
		log.debug("validating Option with id {} and status {}.", option.getOptionId(), option.getStatus());
		if (option.getStatus()!=null && (option.getStatus().equals("cancel") || option.getStatus().equals("delete") || option.getStatus().equals("batch")))
			return;

		if (option.getText() == null || option.getText().trim().length()==0 ||
				stripText == null || stripText.length()==0) {
			log.debug("OptionText is empty!");
			errors.reject("option_empty","option empty");
			return;
		}

		//if where here option is not null or empty but could be something like "&nbsp;&nbsp;"
		String text = option.getText();
		text = PollUtils.cleanupHtmlPtags(text);
		text = text.replace("&nbsp;", "");
		text = StringEscapeUtils.unescapeHtml4(text).trim();
		log.debug("text to validate is: " + text);
		if (text.trim().length()==0) {
			log.debug("OptionText is empty! (after excaping html)");
			errors.reject("option_empty","option empty");
			return;
		}
	}
}
