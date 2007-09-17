package org.sakaiproject.poll.tool.validators;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.poll.model.Poll;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class PollValidator implements Validator {

	protected final Log log = LogFactory.getLog(PollValidator.class);
	
	public String textOpen;
	public String textClose;
	
	public boolean supports(Class clazz) {
		
		return clazz.equals(Poll.class);
	}

	public void validate(Object obj, Errors errors) {
		
		Poll poll = (Poll)obj;
		
		log.debug("Validating poll: " + poll.getPollId());
		log.debug("poll opens: "  + poll.getVoteOpen() + " and closes " + poll.getVoteClose());
		
		
		//the close date needs to be after the open date
		/* doesn't work with date widgets
		if (poll.getVoteOpen().after(poll.getVoteClose())) {
			log.debug("Poll closes before it opens");
			errors.reject("close_before_open","close before opening");
			
		}
		*/
		
		if (poll.getMinOptions() > poll.getMaxOptions()) {
			log.debug("Min options greater than max options");
			errors.reject("min_greater_than_max"," min greater than max");
		}
		
	}

}
