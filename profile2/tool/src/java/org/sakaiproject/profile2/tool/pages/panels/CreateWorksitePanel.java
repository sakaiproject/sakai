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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.AttributeModifier;
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
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.model.util.CollectionModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.profile2.logic.ProfileWorksiteLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.profile2.tool.components.ErrorLevelsFeedbackMessageFilter;
import org.sakaiproject.profile2.tool.components.IconWithClueTip;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.profile2.util.ProfileUtils;
import static org.sakaiproject.site.api.SiteService.SITE_TITLE_MAX_LENGTH;
import org.sakaiproject.site.api.SiteService.SiteTitleValidationStatus;

/**
 * Panel for creating a worksite from a group of people.
 * 
 * @author d.b.robinson@lancaster.ac.uk
 */
public class CreateWorksitePanel extends Panel {

	private static final long serialVersionUID = 1L;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileWorksiteLogic")
	private ProfileWorksiteLogic worksiteLogic;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.SakaiProxy")
	private SakaiProxy sakaiProxy;
	
	/**
	 * Creates an instance of <code>CreateWorksitePanel</code>.
	 * 
	 * @param id the wicket id.
	 * @param persons list of users e.g. from connections (and possibly search
	 *            results in the future).
	 */
	public CreateWorksitePanel(final String id, final List<Person> persons) {
		super(id);
		
		// sort persons
		Collections.sort(persons);
		
		Form<Void> form = new Form<Void>("form");
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
		
		final Palette<Person> palette = new Palette<Person>("palette", new ListModel<Person>(
				new ArrayList<Person>()), new CollectionModel<Person>(persons), renderer, 10, true);
		palette.setOutputMarkupId(true);
		
		form.add(palette);
		
		Label siteNameLabel = new Label("siteNameLabel", new ResourceModel("worksite.name"));
		form.add(siteNameLabel);
		
		final TextField<String> siteNameField = new TextField<String>("siteNameField", new Model<String>());
		siteNameField.setOutputMarkupId(true);
		form.add(siteNameField);

		AjaxButton cancelButton = new AjaxButton("cancelButton") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				
				resetPanel(target, siteNameField, palette, formFeedback);
				
			}
		};
		cancelButton.setModel(new ResourceModel("button.worksite.cancel"));
		form.add(cancelButton);
		
		IndicatingAjaxButton createButton = new IndicatingAjaxButton("createButton", form) {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {

				// Site title is editable; cannot but null/empty after HTML stripping, and cannot exceed max length
				String titleOrig = siteNameField.getValue();
				String titleStripped = ProfileUtils.stripHtmlFromText(titleOrig, true, true);
				SiteTitleValidationStatus status = sakaiProxy.validateSiteTitle(titleOrig, titleStripped);

				boolean titleError = false;
				IModel errorMsg = null;
				if (SiteTitleValidationStatus.STRIPPED_TO_EMPTY.equals(status)) {
					errorMsg = new ResourceModel("error.worksite.title.htmlStrippedToEmpty");
					titleError = true;
				} else if (SiteTitleValidationStatus.EMPTY.equals(status)) {
					errorMsg = new ResourceModel("error.worksite.no.title");
					titleError = true;
				} else if (SiteTitleValidationStatus.TOO_LONG.equals(status)) {
					errorMsg = new StringResourceModel("error.worksite.title.maxLength", null, new Object[] {SITE_TITLE_MAX_LENGTH});
					titleError = true;
				}

				if (titleError) {
					formFeedback.setDefaultModel(errorMsg);
					formFeedback.add(new AttributeModifier("class", true, new Model<>("alertMessage")));
					target.add(formFeedback);
					return;
				}

				if (true == worksiteLogic.createWorksite(titleStripped, sakaiProxy.getCurrentUserId(),
						new ArrayList<Person>(palette.getModelCollection()),
						true)) {

					resetPanel(target, siteNameField, palette, formFeedback);

				} else {
					formFeedback.setDefaultModel(new ResourceModel(
							"error.worksite.create.failed"));
					formFeedback.add(new AttributeModifier("class", true,
							new Model<String>("alertMessage")));
					target.add(formFeedback);
					return;
				}
			}
			
		};
		createButton.setModel(new ResourceModel("button.worksite.create"));
		form.add(createButton);
		
		form.add(new IconWithClueTip("createWorksiteToolTip", ProfileConstants.INFO_IMAGE, new ResourceModel("text.worksite.create")));
		
		Label refreshLabel = new Label("refreshLabel", new ResourceModel("text.worksite.refresh"));
		form.add(refreshLabel);
	}
	
	// clears site name field and palette, and slides up
	private void resetPanel(AjaxRequestTarget target,
			final TextField<String> siteNameField,
			final Palette<Person> palette, Label formFeedback) {

		siteNameField.setModelObject("");

		// there is quite possibly a better way of doing this
		List<Person> remove = new ArrayList<Person>();
		for (Person person : palette.getModelCollection()) {
			remove.add(person);
		}
		palette.getModelCollection().removeAll(remove);

		formFeedback.setVisible(false);
		
		target.add(siteNameField);
		target.appendJavaScript("$('#" + CreateWorksitePanel.this.getMarkupId()
				+ "').slideUp();");
		target.appendJavaScript("fixWindowVertical();");
	}
}
