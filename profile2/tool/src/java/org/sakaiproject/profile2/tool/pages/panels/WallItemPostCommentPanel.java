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
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.profile2.logic.ProfileWallLogic;
import org.sakaiproject.profile2.model.WallItem;
import org.sakaiproject.profile2.model.WallItemComment;
import org.sakaiproject.profile2.tool.components.FocusOnLoadBehaviour;
import org.sakaiproject.profile2.tool.models.WallAction;

/**
 * Panel for commenting on a wall item.
 * 
 * @author d.b.robinson@lancaster.ac.uk
 */
public class WallItemPostCommentPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileWallLogic")
	private ProfileWallLogic wallLogic;
	
	public WallItemPostCommentPanel(String id, final String userUuid, final WallItem wallItem, final WallItemPanel wallItemPanel) {
		
		super(id);
		
		String commentString = "";
		IModel<String> commentModel = new Model<String>(commentString);
        // TODO check this works
		Form<String> form = new Form<String>("form", commentModel);
		form.setOutputMarkupId(true);
		add(form);
		
		WebMarkupContainer commentContainer = new WebMarkupContainer("commentContainer");
		final TextArea<String> commentTextArea = new TextArea<String>("comment", commentModel);
		
		commentContainer.add(commentTextArea);
		
		form.add(commentContainer);
		
		IndicatingAjaxButton submitButton = new IndicatingAjaxButton("submit", new ResourceModel("button.wall.comment"), form) {
			private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form form) {
				
				// don't allow empty posts
				if (null == form.getModelObject()) {
					// TODO user warning
					return;
				}
				
				// create and add comment to wall item
				WallItemComment wallItemComment = new WallItemComment();
				wallItemComment.setCreatorUuid(userUuid);
				wallItemComment.setDate(new Date());
				// TODO sanitize?
				wallItemComment.setText(form.getModelObject().toString());
				wallItemComment.setWallItemId(wallItem.getId());
				wallItem.addComment(wallItemComment);
				
				// update wall item
				if (false == wallLogic.updateWallItem(wallItem)) {
					// TODO user warning
				}
				
				WallItemPanel newPanel = new WallItemPanel(wallItemPanel.getId(), userUuid, wallItem);
				newPanel.setOutputMarkupId(true);
				wallItemPanel.replaceWith(newPanel);
				if (null != target) {
					target.addComponent(newPanel);
					target.appendJavascript("setMainFrameHeight(window.name);");
				}
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
				commentTextArea.clearInput();
				target.appendJavascript("$('#" + WallItemPostCommentPanel.this.getMarkupId() + "').slideUp();");
            }
        };
        
        cancelButton.setDefaultFormProcessing(false);
        form.add(cancelButton);
	}

}
