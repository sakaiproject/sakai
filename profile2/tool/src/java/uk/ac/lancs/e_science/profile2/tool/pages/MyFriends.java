package uk.ac.lancs.e_science.profile2.tool.pages;


import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.resource.BufferedDynamicImageResource;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;

import uk.ac.lancs.e_science.profile2.hbm.Friend;
import uk.ac.lancs.e_science.profile2.tool.pages.windows.RemoveFriend;


public class MyFriends extends BasePage {

	private transient Logger log = Logger.getLogger(MyFriends.class);
	private static final String UNAVAILABLE_IMAGE = "images/no_image.gif";
	
	public MyFriends() {
		
		if(log.isDebugEnabled()) log.debug("MyFriends()");
		
		//add the feedback panel for any error messages
		FeedbackPanel feedbackPanel = new FeedbackPanel("feedbackPanel");
		add(feedbackPanel);
		feedbackPanel.setVisible(false); //hide by default

		//get current user
		final String userId = sakaiProxy.getCurrentUserId();

		Label heading = new Label("heading", new ResourceModel("heading.friends"));
		add(heading);
		
		//remove friend modal window
		final ModalWindow removeFriendWindow = new ModalWindow("removeFriendWindow");
		add(removeFriendWindow);
		
		//the setup for this modal window is done in the AjaxLink below. see there for info.
		
		removeFriendWindow.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {
            public boolean onCloseButtonClicked(AjaxRequestTarget target) {
            	System.out.println("window closed 1");
            	return true;
            }
        });

		removeFriendWindow.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
            public void onClose(AjaxRequestTarget target){
            	System.out.println("window closed 2");
            }
        });
		
		//get photo and add to page, otherwise add default image
		/*
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
		*/
		
		
		
		//get friends for user
		List<Friend> friends = new ArrayList<Friend>(profile.getFriendsForUser(userId, 0));
		
		
		ListView listview = new ListView("friendsList", friends) {
		    protected void populateItem(ListItem item) {
		        
		    	//get a friend object for each user, containing items that we need to list here
		    	//we also need their privacy settings
		    	
		    	//in the friend object add in the photo stream as a param
		    	
		    	
		    	//get Friend object
		    	Friend friend = (Friend)item.getModelObject();
		    	
		    	//setup basic values
		    	String displayName = sakaiProxy.getUserDisplayName(friend.getUserUuid());
		    	String statusMessage = friend.getStatusMessage();
		    	Date statusDate = friend.getStatusDate();
		    	boolean confirmed = friend.isConfirmed();
		    	final byte[] photo = friend.getPhoto();
		    	
		    	//name
		    	Label nameLabel = new Label("name", displayName);
		    	item.add(nameLabel);
		    	
		    	//status - no default value, set it later
		    	Label statusMessageLabel = new Label("statusMessage");
		    	item.add(statusMessageLabel);
		    	
		    	//statusDate - no default value, set it later
		    	Label statusDateLabel = new Label("statusDate");
		    	item.add(statusDateLabel);
		    			    	
		    	//photo
		    	if(photo != null && photo.length > 0){
		    		
					BufferedDynamicImageResource photoResource = new BufferedDynamicImageResource(){
						protected byte[] getImageData() {
							return photo;
						}
					};
				
					item.add(new Image("photo",photoResource));
				} else {
					item.add(new ContextImage("photo",new Model(UNAVAILABLE_IMAGE)));
				}
		
		    	//now set the models on the above objects depending on whether they are filled or not
		    	if(statusMessage == null) {
		    		statusMessageLabel.setVisible(false);
		    	} else 	{
		    		statusMessageLabel.setModel(new Model(statusMessage));
		    	}
		    	
		    	if(statusDate == null) {
		    		statusDateLabel.setVisible(false);
		    	} else 	{
		    		statusDateLabel.setModel(new Model(profile.convertDateForStatus(statusDate)));
		    	}
		    	
		    	
		    	//action - remove friend
		    	AjaxLink removeLink = new AjaxLink("removeLink") {
		    		public void onClick(AjaxRequestTarget target) {
		    			
		    			//setup content panel for removeFriendWindow. This is a custom panel with setters for the data to be used inside
		    			//we set the data into this Panel when the ajax button is clicked.
		    			RemoveFriend removeFriend = new RemoveFriend(removeFriendWindow.getContentId(), userId);
		    			removeFriendWindow.setContent(removeFriend);
		    			removeFriendWindow.setTitle(new ResourceModel("window.title.friend.remove"));
		    			removeFriendWindow.setCookieName("profileModalWindow");
		    			
		    			
		    			System.out.println(userId);
		    			
		    			removeFriendWindow.show(target);
	    			}
	    		};
	    		removeLink.add(new Label("remove",new ResourceModel("link.friend.remove")));
	    		item.add(removeLink);
	    		
	    		
	    		
		    }
		};
		add(listview);
		
		
       
	    		
	    		
		
		
	}
}



