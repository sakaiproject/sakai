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
package org.sakaiproject.scorm.ui.reporting.components;

import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.sakaiproject.scorm.model.api.Objective;

public class ObjectivesPanel extends Panel {

	private static final long serialVersionUID = 1L;

	private final RepeatingView objectivesView;
	
	
	public ObjectivesPanel(String id, List<Objective> objectives) {
		super(id);

		add(objectivesView = new RepeatingView("objectivesView"));
		
		for (Objective objective : objectives) {
			addObjective(objective, objectivesView);
		}
		
	}
	
	/*
	 * Copied the basic organization of this method from an Apache Wicket class
	 * 	org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable
	 * originally authored by Igor Vaynberg (ivaynberg)
	 */
	private void addObjective(Objective objective, RepeatingView container) {
		WebMarkupContainer item = new WebMarkupContainer(container.newChildId(), new CompoundPropertyModel(objective));
		item.setRenderBodyOnly(true);
		
		Label descLabel = new Label("description");
		Label successLabel = new Label("successStatus");
		Label completionLabel = new Label("completionStatus");
		
		successLabel.setVisible(objective.getSuccessStatus() != null && objective.getSuccessStatus().trim().length() != 0);
		completionLabel.setVisible(objective.getCompletionStatus() != null && objective.getCompletionStatus().trim().length() != 0);
		
		item.add(descLabel);
		item.add(new ScorePanel("score", objective.getScore()));
		item.add(successLabel);
		item.add(completionLabel);
		
		container.add(item);
	}
	
	
	

}
