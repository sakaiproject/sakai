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
package org.sakaiproject.profile2.tool.pages.panels;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.extensions.markup.html.form.palette.Palette;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.util.CollectionModel;
import org.apache.wicket.model.util.ListModel;
import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.profile2.tool.components.ErrorLevelsFeedbackMessageFilter;
import org.sakaiproject.profile2.tool.components.IconWithClueTip;
import org.sakaiproject.profile2.util.ProfileConstants;

/**
 * Panel for creating a worksite from a group of people.
 * 
 * @author d.b.robinson@lancaster.ac.uk
 *
 */
public class CreateWorksitePanel extends Panel {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Creates an instance of <code>CreateWorksitePanel</code>.
	 * 
	 * @param id the wicket id.
	 * @param persons list of users e.g. from connections or search results.
	 */
	public CreateWorksitePanel(String id, List<Person> persons) {
		super(id);

		Form<?> form = new Form("form") {

			private static final long serialVersionUID = 1L;
			
			@Override
			protected void onSubmit() {
				// TODO create worksite and take user there if requested
			}
		};
		form.setOutputMarkupId(true);
		add(form);
		
		// form submit feedback
		final Label formFeedback = new Label("formFeedback");
		formFeedback.setOutputMarkupPlaceholderTag(true);
		form.add(formFeedback);
		
		final FeedbackPanel feedback = new FeedbackPanel("feedback");
		feedback.setOutputMarkupId(true);
		form.add(feedback);

		int[] filteredErrorLevels = new int[] { FeedbackMessage.ERROR };
		feedback.setFilter(new ErrorLevelsFeedbackMessageFilter(
				filteredErrorLevels));
				
		IChoiceRenderer<Person> renderer = new ChoiceRenderer<Person>("displayName", "uuid");
		
		Palette<Person> palette = new Palette<Person>("palette", new ListModel<Person>(
				new ArrayList<Person>()), new CollectionModel<Person>(persons), renderer, 10, true);

		form.add(palette);
		
		Label siteNameLabel = new Label("siteNameLabel", new ResourceModel("worksite.name"));
		form.add(siteNameLabel);
		
		//siteNameField.getValue() to get value
		TextField<String> siteNameField = new TextField<String>("siteNameField", new Model<String>());
		form.add(siteNameField);
		
		AjaxButton cancelButton = new AjaxButton("cancelButton") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				target.appendJavascript("$('#" + CreateWorksitePanel.this.getMarkupId() + "').slideUp();");
				target.appendJavascript("fixWindowVertical();");
			}
			
		};
		cancelButton.setModel(new ResourceModel("button.worksite.cancel"));
		form.add(cancelButton);
		
		IndicatingAjaxButton createButton = new IndicatingAjaxButton("createButton", form) {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				// TODO create worksite from 'Selected' palette list
				
			}
			
		};
		createButton.setModel(new ResourceModel("button.worksite.create"));
		form.add(createButton);
		
		form.add(new IconWithClueTip("createWorksiteToolTip", ProfileConstants.INFO_IMAGE, new ResourceModel("text.worksite.create")));
	}
}
