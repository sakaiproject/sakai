/**********************************************************************************
 * $URL: $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2006,2007 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.poll.tool.validators;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.poll.logic.PollVoteManager;
import org.sakaiproject.poll.model.Option;
import org.sakaiproject.tool.api.ToolManager;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.messageutil.TargettedMessageList;


public class OptionValidator implements Validator {

	/** Logger for this class and subclasses */
	protected final Log logger = LogFactory.getLog(getClass());
	private MessageLocator messageLocator;
	private TargettedMessageList tml = new TargettedMessageList();
	private PollVoteManager pollVoteManager;
	private PollListManager manager;
	public String submissionStatus;

	public void setPollListManager(PollListManager manager) {
		this.manager = manager;
	}

	public void setPollVoteManager(PollVoteManager pvm){
		this.pollVoteManager = pvm;
	}
	public void setMessageLocator(MessageLocator messageLocator) {

		this.messageLocator = messageLocator;
	}

	private ToolManager toolManager;
	public void setToolManager(ToolManager toolManager) {
		this.toolManager = toolManager;
	}

	public boolean supports(Class clazz) {
		// TODO Auto-generated method stub
		return clazz.equals(Option.class);
	}

	public void validate(Object obj, Errors errors) {


		Option option = (Option) obj;

		logger.debug("validating Option with id:" + option.getOptionId());
		if (option.getStatus()!=null && (option.getStatus().equals("cancel") || option.getStatus().equals("delete")))
			return;


		if (option.getOptionText() == null || option.getOptionText().trim().length()==0) {
			logger.debug("OptionText is empty!");
			errors.reject("option_empty","option empty");
			return;
		}

		//if where here option is not null or empty but could be something like "&nbsp;&nbsp;"
		String text = option.getOptionText();


		String check = text.substring(3, text.length());

		//if the only <p> tag is the one at the start we will trim off the start and end <p> tags
		if (!org.sakaiproject.util.StringUtil.containsIgnoreCase(check, "<p>")) {
			//this is a single para block
			text = text.substring(3, text.length());
			text = text.substring(0,text.length()- 4);
		}
		
		text = text.replace("&nbsp;", "");
		text = StringEscapeUtils.unescapeHtml(text).trim();
		if (text.trim().length()==0) {
			logger.debug("OptionText is empty! (after excaping html)");
			errors.reject("option_empty","option empty");
			return;
		}



	}




}
