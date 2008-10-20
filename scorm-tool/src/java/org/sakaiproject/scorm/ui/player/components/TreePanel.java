/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
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
package org.sakaiproject.scorm.ui.player.components;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.service.api.LearningManagementSystem;
import org.sakaiproject.scorm.service.api.ScormResourceService;
import org.sakaiproject.scorm.service.api.ScormSequencingService;

public class TreePanel extends Panel {
	private static final long serialVersionUID = 1L;
	
	private ActivityTree tree;
	private LaunchPanel launchPanel;
		
	@SpringBean
	transient LearningManagementSystem lms;
	@SpringBean
	transient ScormResourceService resourceService;
	@SpringBean
	transient ScormSequencingService sequencingService;
	
	public TreePanel(String id, final SessionBean sessionBean, LaunchPanel launchPanel) {
		super(id);
		this.launchPanel = launchPanel;
		
		tree = new ActivityTree("tree", sessionBean, launchPanel); /* {

			private static final long serialVersionUID = 1L;

			@Override
			protected LearningManagementSystem lms() {
				return lms;
			}
			
			@Override
			protected ScormResourceService resourceService() {
				return resourceService;
			}
			
			@Override
			protected ScormSequencingService sequencingService() {
				return sequencingService;
			}
			
		};*/
		tree.setOutputMarkupId(true);
		add(tree);
	}
	
	public void setTreeVisible(boolean isVisible, AjaxRequestTarget target) {
		if (null != tree && tree.isVisible() != isVisible) {
			tree.setVisible(true);
		
			if (target != null)
				target.addComponent(this);
		}
	}

	public LaunchPanel getLaunchPanel() {
		return launchPanel;
	}
	
	public ActivityTree getActivityTree() {
		return tree;
	}
	
}
