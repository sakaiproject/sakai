/**
 * Copyright (c) 2008-2012 The Sakai Foundation
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
package org.sakaiproject.profile2.tool.pages.panels;

import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.sakaiproject.profile2.model.MyProfilePanelState;
import org.sakaiproject.profile2.model.UserProfile;

/**
 * Container for viewing user's own profile.
 */
public class MyProfilePanel extends Panel {
	
	private static final long serialVersionUID = 1L;

	public MyProfilePanel(String id, UserProfile userProfile,
			MyProfilePanelState panelState) {
		
		super(id);
		
		//info panel - load the display version by default
		Panel myInfoDisplay = new MyInfoDisplay("myInfo", userProfile);
		myInfoDisplay.setOutputMarkupId(true);
		add(myInfoDisplay);
		
		//contact panel - load the display version by default
		Panel myContactDisplay = new MyContactDisplay("myContact", userProfile);
		myContactDisplay.setOutputMarkupId(true);
		add(myContactDisplay);
		
		//university staff panel - load the display version by default
		Panel myStaffDisplay;
		if (panelState.showStaffDisplay) {
			myStaffDisplay = new MyStaffDisplay("myStaff", userProfile);
			myStaffDisplay.setOutputMarkupId(true);
		} else {
			myStaffDisplay = new EmptyPanel("myStaff");
		}
		add(myStaffDisplay);
		
		//business panel - load the display version by default
		Panel myBusinessDisplay;
		if (panelState.showBusinessDisplay) {
			myBusinessDisplay = new MyBusinessDisplay("myBusiness", userProfile);
			myBusinessDisplay.setOutputMarkupId(true);
		} else {
			myBusinessDisplay = new EmptyPanel("myBusiness");
		}
		add(myBusinessDisplay);
		
		//student panel
		Panel myStudentDisplay;
		if (panelState.showStudentDisplay) {
			myStudentDisplay = new MyStudentDisplay("myStudent", userProfile);
			myStudentDisplay.setOutputMarkupId(true);
		} else {
			myStudentDisplay = new EmptyPanel("myStudent");
		}
		add(myStudentDisplay);
		
		//social networking panel
		Panel mySocialNetworkingDisplay;
		if (panelState.showSocialNetworkingDisplay) {
			mySocialNetworkingDisplay = new MySocialNetworkingDisplay("mySocialNetworking", userProfile);
			mySocialNetworkingDisplay.setOutputMarkupId(true);
		} else {
			mySocialNetworkingDisplay = new EmptyPanel("mySocialNetworking");
		}
		add(mySocialNetworkingDisplay);
		
		//interests panel - load the display version by default
		Panel myInterestsDisplay;
		if (panelState.showInterestsDisplay) {
			myInterestsDisplay = new MyInterestsDisplay("myInterests", userProfile);
			myInterestsDisplay.setOutputMarkupId(true);
		} else {
			myInterestsDisplay = new EmptyPanel("myInterests");
		}
		add(myInterestsDisplay);
	}

}
