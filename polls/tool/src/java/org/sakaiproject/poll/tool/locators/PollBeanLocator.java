package org.sakaiproject.poll.tool.locators;

import java.util.HashMap;
import java.util.Map;

import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.poll.model.Poll;

import uk.org.ponder.beanutil.BeanLocator;

public class PollBeanLocator implements BeanLocator {
	public static final String NEW_PREFIX = "new ";
	public static String NEW_1 = NEW_PREFIX + "1";
	private Map delivered = new HashMap();

	private PollListManager pollListManager;
	public void setPollListManager(PollListManager p){
		this.pollListManager = p;
	}

	public Object locateBean(String name) {
		Object togo=delivered.get(name);
		if (togo == null){
			if(name.startsWith(NEW_PREFIX)){
				togo = new Poll();
			}
			else { 
				togo = pollListManager.getPollById(Long.valueOf(name));
			}
			delivered.put(name, togo);
		}
		return togo;
	}


}
