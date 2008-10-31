package uk.ac.lancs.e_science.profile2.tool.pages;


import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.resource.BufferedDynamicImageResource;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.api.common.edu.person.SakaiPerson;

import uk.ac.lancs.e_science.profile2.tool.components.SelectModalWindow;
import uk.ac.lancs.e_science.profile2.tool.models.UserProfile;
import uk.ac.lancs.e_science.profile2.tool.pages.panels.MyContactDisplay;
import uk.ac.lancs.e_science.profile2.tool.pages.panels.MyInfoDisplay;
import uk.ac.lancs.e_science.profile2.tool.pages.panels.MyStatusPanel;


public class MyProfile extends BasePage {

	private transient Logger log = Logger.getLogger(MyProfile.class);
	private static final String UNAVAILABLE_IMAGE = "images/no_image.gif";
	private transient byte[] pictureBytes;

	
	public MyProfile() {
		
		if(log.isDebugEnabled()) log.debug("MyProfile()");
		
		//add the feedback panel for any error messages
		FeedbackPanel feedbackPanel = new FeedbackPanel("feedbackPanel");
		add(feedbackPanel);
		feedbackPanel.setVisible(false); //hide by default

		//get current user
		String userId = sakaiProxy.getCurrentUserId();

		//get SakaiPerson for this user
		SakaiPerson sakaiPerson = sakaiProxy.getSakaiPerson(userId);
		//if null, create one.
		if(sakaiPerson == null) {
			log.warn("No SakaiPerson for " + userId + ". Creating one.");
			sakaiPerson = sakaiProxy.createSakaiPerson(userId);
			//if its still null, log an error - ideally, don't proceed.
			if(sakaiPerson == null) {
				log.error("Couldn't create a SakaiPerson for " + userId + ". Given up.");
			}
		} 
		
		
		//get some values from SakaiPerson or SakaiProxy if empty
		//SakaiPerson returns NULL strings if value is not set, not blank ones
	
		String userDisplayName = sakaiPerson.getDisplayName();
		if(userDisplayName == null) {
			log.info("userDisplayName for " + userId + " was null in SakaiPerson. Using UDP value.");
			userDisplayName = sakaiProxy.getUserDisplayName(userId);
		}
		
		String userEmail = sakaiPerson.getMail();
		if(userEmail == null) {
			log.info("userEmail for " + userId + " was null in SakaiPerson. Using UDP value.");
			userEmail = sakaiProxy.getUserEmail(userId);
		}
		
		//create instance of the UserProfile class
		UserProfile userProfile = new UserProfile();
		
		//get rest of values from SakaiPerson and set into UserProfile
		userProfile.setNickname(sakaiPerson.getNickname());
		userProfile.setDateOfBirth(sakaiPerson.getDateOfBirth());
		userProfile.setDisplayName(userDisplayName);
		userProfile.setEmail(userEmail);
		userProfile.setHomepage(sakaiPerson.getLabeledURI());
		userProfile.setHomephone(sakaiPerson.getHomePhone());
		userProfile.setWorkphone(sakaiPerson.getTelephoneNumber());
		userProfile.setMobilephone(sakaiPerson.getMobile());
		
		
		
		//get photo and add to page, otherwise add default image
		pictureBytes = sakaiPerson.getJpegPhoto();
		
		if(pictureBytes != null && pictureBytes.length > 0){
		
			BufferedDynamicImageResource photoResource = new BufferedDynamicImageResource(){
				protected byte[] getImageData() {
					return pictureBytes;
				}
			};
		
			add(new Image("photo",photoResource));
		} else {
			log.info("No photo for " + userId + ". Using blank image.");
			add(new ContextImage("photo",new Model(UNAVAILABLE_IMAGE)));
		}
		
		// The ModalWindow, showing some choices for the user to select.
		
		// The label that shows the result from the ModalWindow
        final Label resultLabel = new Label("resultlabel", new Model(""));
        resultLabel.setOutputMarkupId(true);
        add(resultLabel);
		
		
        final ModalWindow selectModalWindow = new SelectModalWindow("modalwindow"){

            void onSelect(AjaxRequestTarget target, String selection) {
                // Handle Select action
                resultLabel.setModelObject(selection);
                target.addComponent(resultLabel);
                close(target);
            }

            void onCancel(AjaxRequestTarget target) {
                // Handle Cancel action
                resultLabel.setModelObject("ModalWindow cancelled.");
                target.addComponent(resultLabel);
                close(target);
            }

        };
        add(selectModalWindow);

		
		
		
		
		//change profile image button
		AjaxFallbackLink changeProfileImage = new AjaxFallbackLink("changeProfileImage", new ResourceModel("link.change.profile.image")) {
			public void onClick(AjaxRequestTarget target) {
				selectModalWindow.show(target);
			}
						
		};
		changeProfileImage.setOutputMarkupId(true);
		add(changeProfileImage);
		
		//configure userProfile as the model for our page
		//we then pass the userProfileModel in the constructor to the child panels
		CompoundPropertyModel userProfileModel = new CompoundPropertyModel(userProfile);
		
		//status panel
		Panel myStatusPanel = new MyStatusPanel("myStatusPanel", userProfileModel);
		add(myStatusPanel);
		
		//info panel - load the display version by default
		Panel myInfoDisplay = new MyInfoDisplay("myInfo", userProfileModel);
		myInfoDisplay.setOutputMarkupId(true);
		add(myInfoDisplay);
		
		//contact panel - load the display version by default
		Panel myContactDisplay = new MyContactDisplay("myContact", userProfileModel);
		myContactDisplay.setOutputMarkupId(true);
		add(myContactDisplay);
		
		
	}
	
	
	
	
}
