package uk.ac.lancs.e_science.profile2.tool.pages.windows;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;

public class RemoveFriend extends Panel {

	public RemoveFriend(String id, String userId, String friendId){
        super(id);

        
        
        
        
        //heading
        Label heading = new Label("heading", new ResourceModel("heading.friend.remove"));
        add(heading);
        
        //text
        Label text = new Label("text", userId + friendId);
        add(text);
        
        
        
    }
	
	
	
	
}
