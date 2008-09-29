package uk.ac.lancs.e_science.profile2.tool.pages;

import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.api.common.edu.person.SakaiPerson;

import uk.ac.lancs.e_science.profile2.api.Profile;
import uk.ac.lancs.e_science.profile2.api.SakaiProxy;

public class MyInfoPanel extends Panel {
	
	private transient Logger log = Logger.getLogger(MyInfoPanel.class);
	
	public MyInfoPanel(String id, SakaiProxy sakaiProxy, Profile profile, SakaiPerson sakaiPerson) {
		
		super(id);
		
		//edit button
		add(new AjaxFallbackLink("editButton") {
            public void onClick(AjaxRequestTarget target) {
                // setup JS that will be executed
            	String js = 
            		"$(document).ready(function(){ " +
            		"$('#myInfo').slideUp('normal', function() {" +
            		"$('#myInfoEdit').slideDown();" +
            		"});" +
            		"});";
            	target.prependJavascript(js);
            }
        });
		
		//labels
		add(new Label("profileNicknameLabel", new ResourceModel("profile.nickname")));
		add(new Label("profilePositionLabel", new ResourceModel("profile.position")));
		add(new Label("profileDepartmentLabel", new ResourceModel("profile.department")));
		//add(new Label("profileNicknameLabel", new ResourceModel("profile.department")));
		//add(new Label("profileNicknameLabel", new ResourceModel("profile.school")));
		//add(new Label("profileNicknameLabel", new ResourceModel("profile.room")));
		//add(new Label("profileNicknameLabel", new ResourceModel("profile.phone.work")));
		//add(new Label("profileNicknameLabel", new ResourceModel("profile.phone.home")));
		//add(new Label("profileNicknameLabel", new ResourceModel("profile.phone.mobile")));
		
		//get nickname
		String nickname = "MickeyMouse";
		if(sakaiPerson != null) {
			nickname = sakaiPerson.getNickname();
		}
		
		//content
		add(new Label("profileNicknameContent", nickname));
		add(new Label("profilePositionContent", "hello"));
		add(new Label("profileDepartmentContent", sakaiProxy.getUserDisplayName(sakaiProxy.getCurrentUserId())));

		
	}
	
	
	
	
	
	
}
