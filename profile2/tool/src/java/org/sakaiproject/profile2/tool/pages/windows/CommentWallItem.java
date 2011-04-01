/**
 * 
 */
package org.sakaiproject.profile2.tool.pages.windows;

import java.util.Date;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
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
import org.sakaiproject.profile2.model.WallItemComment;
import org.sakaiproject.profile2.tool.components.FocusOnLoadBehaviour;
import org.sakaiproject.profile2.tool.components.ProfileImageRenderer;
import org.sakaiproject.profile2.tool.models.WallAction;
import org.sakaiproject.profile2.util.ProfileConstants;

/**
 * Panel for commenting on a wall item.
 * 
 * @author d.b.robinson@lancaster.ac.uk
 */
public class CommentWallItem extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileWallLogic")
	private ProfileWallLogic wallLogic;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfilePreferencesLogic")
	private ProfilePreferencesLogic preferencesLogic;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfilePrivacyLogic")
	private ProfilePrivacyLogic privacyLogic;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.SakaiProxy")
	protected SakaiProxy sakaiProxy;
	
	public CommentWallItem(String id, final ModalWindow window,
			final WallAction wallAction, final String userUuid,
			final WallItem wallItem) {
		super(id);

		window.setTitle(new StringResourceModel(
					"title.wall.comment", null, new Object[]{ sakaiProxy.getUserDisplayName(wallItem.getCreatorUuid()) } ));
		// TODO TinyMCE height = 200
		window.setInitialHeight(150);
		window.setInitialWidth(500);
		window.setResizable(false);

		// add profile image of wall post creator
		ProfilePreferences prefs = preferencesLogic
				.getPreferencesRecordForUser(wallItem.getCreatorUuid());
		ProfilePrivacy privacy = privacyLogic.getPrivacyRecordForUser(wallItem
				.getCreatorUuid());

		add(new ProfileImageRenderer("image", wallItem.getCreatorUuid(), prefs,
				privacy, ProfileConstants.PROFILE_IMAGE_THUMBNAIL, false));

		String commentString = "";
		IModel<String> commentModel = new Model<String>(commentString);
        Form form = new Form("form", commentModel);
		form.setOutputMarkupId(true);
		add(form);
		
		WebMarkupContainer commentContainer = new WebMarkupContainer("commentContainer");
		TextArea<String> comment = new TextArea<String>("comment", commentModel);

		// TODO TinyMce doesn't work properly on a modal window
		// comment.setOutputMarkupId(true);
		// comment.add(new TinyMceBehavior(new TextareaTinyMceSettings()));
		commentContainer.add(comment);
		
		form.add(commentContainer);
		
		AjaxFallbackButton submitButton = new AjaxFallbackButton("submit", new ResourceModel("button.wall.comment"), form) {
			private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form form) {
				
				// TODO don't allow empty posts
				
				// create and add comment to wall item
				WallItemComment comment = new WallItemComment();
				comment.setCreatorUuid(userUuid);
				comment.setDate(new Date());
				// TODO sanitize if we get TinyMCE working?
				comment.setText(form.getModelObject().toString());
				comment.setWallItemId(wallItem.getId());
				wallItem.addComment(comment);
				
				// update wall item
				wallAction.setItemCommented(wallLogic.updateWallItem(wallItem));
				
            	window.close(target);
			}
		};
		submitButton.add(new FocusOnLoadBehaviour());
		
		AttributeModifier accessibilityLabel = new AttributeModifier(
					"title", true, new StringResourceModel("accessibility.wall.comment", null, new Object[]{ } ));
		
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
	}
	
}
