/**
 * Copyright (c) 2007 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.scorm.ui.console.components;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.scorm.exceptions.LearnerNotDefinedException;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.model.api.Learner;
import org.sakaiproject.scorm.service.api.LearningManagementSystem;
import org.sakaiproject.wicket.model.DecoratedPropertyModel;
import org.sakaiproject.wicket.model.SimpleDateFormatPropertyModel;

public class ContentPackageDetailPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean
	LearningManagementSystem lms;
	
	public ContentPackageDetailPanel(String id, ContentPackage contentPackage) {
		super(id, new TypeAwareCompoundPropertyModel(contentPackage));

		String createdByName = "Unknown";
		String modifiedByName = "Unknown";
		
		if (contentPackage != null) {
			try {
				createdByName = getLearnerDisplay(lms.getLearner(contentPackage.getCreatedBy()));
				modifiedByName = getLearnerDisplay(lms.getLearner(contentPackage.getModifiedBy()));
			} catch (LearnerNotDefinedException e) {
				// Doesn't matter.
			}
		}
		
		add(new Label("title"));
		add(new Label("releaseOn"));
//		add(new Label("numberOffPoints"));
//		add(new Label("synchronizeSCOWithGradebook"));
		add(new Label("dueOn"));
		add(new Label("acceptUntil"));
		add(new Label("numberOfTries", new TriesDecoratedPropertyModel(contentPackage, "numberOfTries")));
		add(new Label("createdBy", new Model(createdByName)));
		add(new Label("createdOn"));
		add(new Label("modifiedBy", new Model(modifiedByName)));
		add(new Label("modifiedOn"));
		
	}
	
	
	private String getLearnerDisplay(Learner learner) {
		return new StringBuilder(learner.getDisplayName()).append(" (").append(learner.getDisplayId())
			.append(")").toString();
	}
	

	private Label newPropertyLabel(ContentPackage contentPackage, String expression) {
		return new Label(expression, new PropertyModel(contentPackage, expression));
	}
	
	private Label newDatePropertyLabel(ContentPackage contentPackage, String expression) {
		return new Label(expression, new SimpleDateFormatPropertyModel(contentPackage, expression));
	}
	
	private Label newTriesPropertyLabel(ContentPackage contentPackage, String expression) {
		return new Label(expression, new TriesDecoratedPropertyModel(contentPackage, expression));
	}
	
	public class TriesDecoratedPropertyModel extends DecoratedPropertyModel {

		private static final long serialVersionUID = 1L;

		public TriesDecoratedPropertyModel(Object modelObject, String expression) {
			super(modelObject, expression);
		}

		@Override
		public Object convertObject(Object object) {
			String str = String.valueOf(object);
			
			if (str.equals("-1"))
				return "Unlimited";
			
			return str;
		}
		
	}
	
}
