/**********************************************************************************
 * $URL$
 * $Id$
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

package org.sakaiproject.poll.tool.locators;

import java.util.HashMap;
import java.util.Map;

import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.poll.model.Poll;

import uk.org.ponder.beanutil.BeanLocator;

public class PollBeanLocator implements BeanLocator {
	public static final String NEW_PREFIX = "new ";
	public static final String NEW_1 = NEW_PREFIX + "1";
	private Map<String, Object> delivered = new HashMap<String, Object>();

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
