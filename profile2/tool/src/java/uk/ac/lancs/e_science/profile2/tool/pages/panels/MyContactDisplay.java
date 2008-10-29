package uk.ac.lancs.e_science.profile2.tool.pages.panels;

import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;

import uk.ac.lancs.e_science.profile2.tool.models.UserProfile;

public class MyContactDisplay extends Panel {
	
	private transient Logger log = Logger.getLogger(MyInfoDisplay.class);
	
	private String nickname = null;
	private Date dateOfBirth = null;
	private String birthday = null;

	
	public MyContactDisplay(String id, final IModel userProfileModel) {
		super(id, userProfileModel);
		
		//this panel stuff
		final Component thisPanel = this;
		final String thisPanelId = "myContact"; //wicket:id not markupId
		
		//get userProfile from userProfileModel
		UserProfile userProfile = (UserProfile) this.getModelObject();
		
		//get info from userProfile since we need to validate it and turn things off if not set.
		//otherwise we could just use a propertymodel
		String email = userProfile.getEmail();
		
		//heading
		add(new Label("heading", new ResourceModel("heading.contact")));
		
		//nickname
		WebMarkupContainer emailContainer = new WebMarkupContainer("emailContainer");
		emailContainer.add(new Label("emailLabel", new ResourceModel("profile.email")));
		emailContainer.add(new Label("email", email));
		add(emailContainer);
		
		
		//edit button
		AjaxFallbackLink editButton = new AjaxFallbackLink("editButton", new ResourceModel("button.edit")) {
			public void onClick(AjaxRequestTarget target) {
				Component newPanel = new MyContactEdit(thisPanelId, userProfileModel);
				newPanel.setOutputMarkupId(true);
				thisPanel.replaceWith(newPanel);
				if(target != null) {
					target.addComponent(newPanel);
				}
			}
						
		};
		editButton.setOutputMarkupId(true);
		add(editButton);
		
	}
	
}
