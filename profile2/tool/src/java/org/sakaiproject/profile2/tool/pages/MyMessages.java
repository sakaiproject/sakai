/**
 * Copyright (c) 2008-2010 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.profile2.tool.pages;


import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.panel.Panel;
import org.sakaiproject.profile2.tool.pages.panels.MessageThreadsView;

public class MyMessages extends BasePage {

	private static final Logger log = Logger.getLogger(MyMessages.class);
	
	public MyMessages() {
		renderMyMessages(null);
	}
	
	
	public MyMessages(final String threadId) {
		renderMyMessages(threadId);
	}
	
	private void renderMyMessages(final String threadId) {

		log.debug("MyMessages( " + threadId + ")");
		
		//get user 
		final String currentUserUuid = sakaiProxy.getCurrentUserId();
		
		
		//show confirmed friends panel for the given user
		//Panel threadsView = new ConfirmedFriends("confirmedFriends", userUuid);
		//confirmedFriends.setOutputMarkupId(true);
		//add(confirmedFriends);
		
		Panel threadsView = new MessageThreadsView("threadsView");
		add(threadsView);
	}
}



