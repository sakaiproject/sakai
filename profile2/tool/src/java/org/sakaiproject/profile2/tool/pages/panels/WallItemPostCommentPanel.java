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
/**
 * 
 */
package org.sakaiproject.profile2.tool.pages.panels;

import java.util.Date;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.profile2.logic.ProfileWallLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.WallItem;
import org.sakaiproject.profile2.model.WallItemComment;

/**
 * Panel for commenting on a wall item.
 * 
 * @author d.b.robinson@lancaster.ac.uk
 */
public class WallItemPostCommentPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileWallLogic")
	private ProfileWallLogic wallLogic;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.SakaiProxy")
	protected SakaiProxy sakaiProxy;
	
	public WallItemPostCommentPanel(String id, final String userUuid,
			final WallItem wallItem, final WallItemPanel wallItemPanel,
			final MyWallPanel myWallPanel) {
		
		super(id);
		
		String commentString = "";
		IModel<String> commentModel = new Model<String>(commentString);
		Form<String> form = new Form<String>("form", commentModel);
		form.setOutputMarkupId(true);
		add(form);
		
		// form submit feedback
		final Label formFeedback = new Label("formFeedback");
		formFeedback.setOutputMarkupPlaceholderTag(true);
		form.add(formFeedback);
		
		WebMarkupContainer commentContainer = new WebMarkupContainer("commentContainer");
		final TextArea<String> commentTextArea = new TextArea<String>("comment", commentModel);
		
		commentContainer.add(commentTextArea);
		
		form.add(commentContainer);
		
		IndicatingAjaxButton submitButton = new IndicatingAjaxButton("submit", new ResourceModel("button.wall.comment"), form) {
			private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				
				// don't allow empty posts
				if (null == form.getModelObject()) {
					formFeedback.setVisible(true);
					formFeedback.setDefaultModel(new ResourceModel(
							"error.wall.comment.empty"));
					formFeedback.add(new AttributeModifier("class", true,
							new Model<String>("alertMessage")));
					target.add(formFeedback);
					return;
				}
				
				// create and add comment to wall item
				WallItemComment wallItemComment = new WallItemComment();
				// always post as current user
				wallItemComment.setCreatorUuid(sakaiProxy.getCurrentUserId());
				wallItemComment.setDate(new Date());
				wallItemComment.setText(form.getModelObject().toString());
				wallItemComment.setWallItem(wallItem);
				wallItem.addComment(wallItemComment);
				
				// update wall item
				if (false == wallLogic.addNewCommentToWallItem(wallItemComment)) {
					formFeedback.setVisible(true);
					formFeedback.setDefaultModel(new ResourceModel(
							"error.wall.comment.failed"));
					formFeedback.add(new AttributeModifier("class", true,
							new Model<String>("alertMessage")));
					target.add(formFeedback);
					return;
				}
								
				// replace wall item panel now comment has been added
				WallItemPanel newPanel = new WallItemPanel(wallItemPanel.getId(), userUuid, wallItem, myWallPanel);
				newPanel.setOutputMarkupId(true);
				wallItemPanel.replaceWith(newPanel);
				if (null != target) {
					target.add(newPanel);
					target.appendJavaScript("setMainFrameHeight(window.name);");
				}
			}
		};
		//submitButton.add(new FocusOnLoadBehaviour());
		
		AttributeModifier accessibilityLabel = new AttributeModifier(
					"title", true, new StringResourceModel("accessibility.wall.comment", null, new Object[]{ sakaiProxy.getUserDisplayName(wallItem.getCreatorUuid()) } ));
		
		submitButton.add(accessibilityLabel);
		form.add(submitButton);
		
		AjaxFallbackButton cancelButton = new AjaxFallbackButton("cancel", new ResourceModel("button.cancel"), form) {
            private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				commentTextArea.clearInput();
				formFeedback.setVisible(false);
				target.appendJavaScript("$('#" + WallItemPostCommentPanel.this.getMarkupId() + "').slideUp();");
            }
        };
        
        cancelButton.setDefaultFormProcessing(false);
        form.add(cancelButton);
	}

}
