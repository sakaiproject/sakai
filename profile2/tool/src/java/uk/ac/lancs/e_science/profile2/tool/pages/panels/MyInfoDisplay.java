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
import org.apache.wicket.model.ResourceModel;

import uk.ac.lancs.e_science.profile2.tool.models.UserProfile;
import uk.ac.lancs.e_science.profile2.tool.pages.panels.views.TestPanelFullReplace2;

public class MyInfoDisplay extends Panel {
	
	private transient Logger log = Logger.getLogger(MyInfoDisplay.class);
	
	private String nickname = null;
	private Date dateOfBirth = null;
	private String birthday = null;

	
	public MyInfoDisplay(String id, final IModel userProfileModel) {
		super(id, userProfileModel);
		
		//get handle on this panel
		final Component thisPanel = this;
		
		//get userProfile from userProfileModel
		UserProfile userProfile = (UserProfile) this.getModelObject();
		
		//get info from userProfile
		String nickname = userProfile.getNickname();

		
		//nickname
		WebMarkupContainer profileNickname = new WebMarkupContainer("nickname");
		profileNickname.add(new Label("nicknameLabel", new ResourceModel("profile.nickname")));
		profileNickname.add(new Label("nicknameContent", nickname));
		add(profileNickname);
		if("".equals(nickname) || nickname == null) {
			profileNickname.setVisible(false);
		}
		
		//birthday
		WebMarkupContainer profileBirthday = new WebMarkupContainer("birthday");
		profileBirthday.add(new Label("birthdayLabel", new ResourceModel("profile.birthday")));
		profileBirthday.add(new Label("birthdayContent", birthday));
		add(profileBirthday);
		if(birthday == null) {
			profileBirthday.setVisible(false);
		}
		
		
		//edit button
		AjaxFallbackLink editButton = new AjaxFallbackLink("editButton", new ResourceModel("button.edit")) {
			public void onClick(AjaxRequestTarget target) {
				Component newPanel = new MyInfoEdit("myInfo", userProfileModel);
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
