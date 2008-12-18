package uk.ac.lancs.e_science.profile2.tool.pages;


import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;

import uk.ac.lancs.e_science.profile2.tool.pages.windows.RemoveFriend;


public class MyFriends extends BasePage {

	private transient Logger log = Logger.getLogger(MyFriends.class);
	private static final String UNAVAILABLE_IMAGE = "http://blog.makezine.com/who_tall.jpg";
	
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
		
		//we need to get teh parameters into the window here, we may need to implement our own ModalWindow extension that has getters and setters so we can set items into it?
		
		removeFriendWindow.setContent(new RemoveFriend(removeFriendWindow.getContentId(), userId, "DO THIS"));
		removeFriendWindow.setTitle(new ResourceModel("window.title.friend.remove"));
		removeFriendWindow.setCookieName("profileModalWindow");

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
		
		
		
		
		
		
		//get friends for user
		List friends = new ArrayList();
		friends = (ArrayList)profile.getFriendsForUser(userId, true);
		System.out.println(friends.size());
		System.out.println(friends.toString());
		
		
				
		ListView listview = new ListView("friendsList", friends) {
		    protected void populateItem(ListItem item) {
		        
		    	//get a friend object for each user, containing items that we need to list here
		    	//we also need their privacy settings
		    	
		    	//name
		    	Label nameLabel = new Label("name", item.getModel());
		    	item.add(nameLabel);
		    	
		    	//status
		    	Label statusMessageLabel = new Label("statusMessage", "status update goes here");
		    	item.add(statusMessageLabel);
		    	
		    	//status
		    	Label statusDateLabel = new Label("statusDate", "on Tuesday");
		    	item.add(statusDateLabel);
		    	
		    	//photo
		    	item.add(new ContextImage("photo",new Model(UNAVAILABLE_IMAGE)));
		    	
		    	
		    	
		    	//action - remove friend
		    	AjaxLink removeLink = new AjaxLink("removeLink") {
		    		public void onClick(AjaxRequestTarget target) {
		    			//get the friendId and set up the data here.
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



