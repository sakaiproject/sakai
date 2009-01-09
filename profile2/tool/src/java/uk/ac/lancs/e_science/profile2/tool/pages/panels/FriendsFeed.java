package uk.ac.lancs.e_science.profile2.tool.pages.panels;



import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;

import uk.ac.lancs.e_science.profile2.api.Profile;
import uk.ac.lancs.e_science.profile2.api.SakaiProxy;
import uk.ac.lancs.e_science.profile2.tool.ProfileApplication;


public class FriendsFeed extends Panel {
	
	private transient Logger log = Logger.getLogger(MyInfoDisplay.class);
	
	private static final String UNAVAILABLE_IMAGE = "images/no_image.gif";

	
	public FriendsFeed(String id, String userId) {
		super(id);
		
		//get SakaiProxy
		final SakaiProxy sakaiProxy = ProfileApplication.get().getSakaiProxy();
		
		//get Profile
		Profile profile = ProfileApplication.get().getProfile();
		

		
		//get randomised set of friends for this user, in future, list may be according to some rules		
		Label heading = new Label("heading", new ResourceModel("heading.feed.friends"));
		add(heading);
		
		/*
		
		//get our list of friends as an IDataProvider
		FriendDataProvider provider = new FriendDataProvider(userId, profile);

		
		
		
		GridView dataView = new GridView("rows", provider)
		{
			@Override
			protected void populateEmptyItem(Item item)
			{
				//maybe wrap in a webmarkupcontainer
				add(new ContextImage("photo",new Model(UNAVAILABLE_IMAGE)));
				add(new Label("name","empty"));
				//c.setVisible(false);
			}
			
			protected void populateItem(Item item)
			{
				Friend friend = (Friend)item.getModelObject();
				
				//setup basic values
				String displayName = sakaiProxy.getUserDisplayName(friend.getUserUuid());
		    	final byte[] photo = friend.getPhoto();
			
		    	//name
		    	Label nameLabel = new Label("name", displayName);
		    	item.add(nameLabel);
		
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
			}
		};
		
		dataView.setColumns(3);
		add(dataView);
		*/
		
		//if no friends, hide
		this.setVisible(false);
	}
	
	
	
}
