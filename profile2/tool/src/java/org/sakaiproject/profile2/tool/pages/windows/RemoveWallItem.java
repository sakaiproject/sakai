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
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.profile2.logic.ProfileWallLogic;
import org.sakaiproject.profile2.model.WallItem;
import org.sakaiproject.profile2.tool.components.FocusOnLoadBehaviour;
import org.sakaiproject.profile2.tool.models.WallAction;

/**
 * Confirmation dialog for removing wall item.
 * 
 * @author d.b.robinson@lancaster.ac.uk
 */
public class RemoveWallItem extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileWallLogic")
	private ProfileWallLogic wallLogic;
	
	public RemoveWallItem(String id, final ModalWindow window, final WallAction wallAction,
			final String userUuid, final WallItem wallItem) {
		
		super(id);
		
		window.setTitle(new ResourceModel("title.wall.remove")); 
		window.setInitialHeight(150);
		window.setInitialWidth(500);
		window.setResizable(false);
		
		final Label text = new Label("text", new StringResourceModel("text.wall.remove", null, new Object[]{ } ));
        text.setEscapeModelStrings(false);
        text.setOutputMarkupId(true);
        add(text);
        
        Form form = new Form("form");
		form.setOutputMarkupId(true);
		
		AjaxFallbackButton submitButton = new AjaxFallbackButton("submit", new ResourceModel("button.wall.remove"), form) {
			private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form form) {
				
				wallAction.setItemRemoved(wallLogic.removeWallItemFromWall(userUuid, wallItem));
				
            	window.close(target);
			}
		};
		submitButton.add(new FocusOnLoadBehaviour());
		submitButton.add(new AttributeModifier("title", true, new StringResourceModel("accessibility.wall.remove", null, new Object[]{ } )));
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
