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
package org.sakaiproject.profile2.tool.pages.windows;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.profile2.logic.ProfilePreferencesLogic;
import org.sakaiproject.profile2.logic.ProfilePrivacyLogic;
import org.sakaiproject.profile2.logic.ProfileWallLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.ProfilePreferences;
import org.sakaiproject.profile2.model.ProfilePrivacy;
import org.sakaiproject.profile2.model.WallItem;
import org.sakaiproject.profile2.tool.components.ProfileImage;
import org.sakaiproject.profile2.tool.models.WallAction;
import org.sakaiproject.profile2.util.ProfileConstants;

/**
 * Confirmation dialog for removing wall item.
 * 
 * @author d.b.robinson@lancaster.ac.uk
 */
public class RemoveWallItem extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileWallLogic")
	private ProfileWallLogic wallLogic;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfilePreferencesLogic")
	private ProfilePreferencesLogic preferencesLogic;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfilePrivacyLogic")
	private ProfilePrivacyLogic privacyLogic;

	@SpringBean(name="org.sakaiproject.profile2.logic.SakaiProxy")
	protected SakaiProxy sakaiProxy;
	
	public RemoveWallItem(String id, final ModalWindow window, final WallAction wallAction,
			final String userUuid, final WallItem wallItem) {
		
		super(id);
		
		window.setTitle(new ResourceModel("title.wall.remove")); 
		window.setInitialHeight(150);
		window.setInitialWidth(500);
		window.setResizable(false);
		
		// add profile image of wall post creator
		ProfilePreferences prefs = preferencesLogic.getPreferencesRecordForUser(wallItem.getCreatorUuid());
		ProfilePrivacy privacy = privacyLogic.getPrivacyRecordForUser(wallItem.getCreatorUuid());
		
		ProfileImage image = new ProfileImage("image", new Model<String>(wallItem.getCreatorUuid()));
		image.setSize(ProfileConstants.PROFILE_IMAGE_THUMBNAIL);
		add(image);
				
		final Label text;
		if (false == wallItem.getCreatorUuid().equals(userUuid)) {
			text = new Label("text", new StringResourceModel(
					"text.wall.remove.other", null, new Object[]{ sakaiProxy.getUserDisplayName(wallItem.getCreatorUuid()) } ));
		} else {
			text = new Label("text", new StringResourceModel("text.wall.remove.mine", null, new Object[]{ } ));
		}
        text.setEscapeModelStrings(false);
        text.setOutputMarkupId(true);
        add(text);
        
        Form form = new Form("form");
		form.setOutputMarkupId(true);
		
		AjaxFallbackButton submitButton = new AjaxFallbackButton("submit", new ResourceModel("button.wall.remove"), form) {
			private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form form) {
				
				wallAction.setItemRemoved(wallLogic.removeWallItemFromWall(wallItem));
				
            	window.close(target);
			}
		};
		//submitButton.add(new FocusOnLoadBehaviour());
		
		final AttributeModifier accessibilityLabel;
		if (false == wallItem.getCreatorUuid().equals(userUuid)) {
			accessibilityLabel = new AttributeModifier(
					"title", true, new StringResourceModel("accessibility.wall.remove.other", null, new Object[]{ } ));
		} else {
			accessibilityLabel = new AttributeModifier(
					"title", true, new StringResourceModel("accessibility.wall.remove.mine", null, new Object[]{ } ));
		}
		submitButton.add(accessibilityLabel);
		form.add(submitButton);
		
		AjaxFallbackButton cancelButton = new AjaxFallbackButton("cancel", new ResourceModel("button.cancel"), form) {
            private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form form) {			
            	window.close(target);
            }
        };
        
        cancelButton.setDefaultFormProcessing(false);
        form.add(cancelButton);
        
        add(form);
	}
}
