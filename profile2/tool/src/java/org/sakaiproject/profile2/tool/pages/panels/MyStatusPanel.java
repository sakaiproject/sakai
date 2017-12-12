/**
 * Copyright (c) 2008-2012 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.profile2.tool.pages.panels;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import org.sakaiproject.profile2.logic.ProfileExternalIntegrationLogic;
import org.sakaiproject.profile2.logic.ProfileMessagingLogic;
import org.sakaiproject.profile2.logic.ProfilePreferencesLogic;
import org.sakaiproject.profile2.logic.ProfileStatusLogic;
import org.sakaiproject.profile2.logic.ProfileWallLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.UserProfile;
import org.sakaiproject.profile2.tool.components.ProfileStatusRenderer;
import org.sakaiproject.profile2.tool.models.StringModel;
import org.sakaiproject.profile2.util.ProfileConstants;

@Slf4j
public class MyStatusPanel extends Panel {
	
	private static final long serialVersionUID = 1L;

    private ProfileStatusRenderer status;
    
    @SpringBean(name="org.sakaiproject.profile2.logic.SakaiProxy")
	private SakaiProxy sakaiProxy;
	
    @SpringBean(name="org.sakaiproject.profile2.logic.ProfileStatusLogic")
	private ProfileStatusLogic statusLogic;
    
    @SpringBean(name="org.sakaiproject.profile2.logic.ProfilePreferencesLogic")
	private ProfilePreferencesLogic preferencesLogic;
    
    @SpringBean(name="org.sakaiproject.profile2.logic.ProfileMessagingLogic")
	private ProfileMessagingLogic messagingLogic;
    
    @SpringBean(name="org.sakaiproject.profile2.logic.ProfileExternalIntegrationLogic")
	protected ProfileExternalIntegrationLogic externalIntegrationLogic;
    
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileWallLogic")
	protected ProfileWallLogic wallLogic;
	
    //get default text that fills the textField
	String defaultStatus = new ResourceModel("text.no.status", "Say something").getObject().toString();

	public MyStatusPanel(String id, UserProfile userProfile) {
		super(id);
		
		log.debug("MyStatusPanel()");
	
		//get info
		final String displayName = userProfile.getDisplayName();
		final String userId = userProfile.getUserUuid();
		
		//if superUser and proxied, can't update
		boolean editable = true;
		if(sakaiProxy.isSuperUserAndProxiedToUser(userId)) {
			editable = false;
		}
		
		
		
		//name
		Label profileName = new Label("profileName", displayName);
		add(profileName);
		
		//status component
		status = new ProfileStatusRenderer("status", userId, null, "tiny") {
			@Override
			public boolean isVisible(){
			   return this.hasStatusSet() && sakaiProxy.isProfileStatusEnabled();
			}
		};
		status.setOutputMarkupId(true);
		add(status);
		
		 //clear link
		final AjaxFallbackLink clearLink = new AjaxFallbackLink("clearLink") {
			private static final long serialVersionUID = 1L;

			public void onClick(AjaxRequestTarget target) {
				//clear status, hide and repaint
				if(statusLogic.clearUserStatus(userId)) {
					status.setVisible(false); //hide status
					this.setVisible(false); //hide clear link
					target.add(status);
					target.add(this);
				}
			}
			
			@Override
			public boolean isVisible(){
			   return status.isVisible(); //if there is text to show
			}
		};
		clearLink.setOutputMarkupPlaceholderTag(true);
		clearLink.add(new Label("clearLabel",new ResourceModel("link.status.clear")));
	
		add(clearLink);
        
        
		
		WebMarkupContainer statusFormContainer = new WebMarkupContainer("statusFormContainer") {
			@Override
			public boolean isVisible(){
			   return sakaiProxy.isProfileStatusEnabled();
			}
		};
		
				
		//setup SimpleText object to back the single form field 
		StringModel stringModel = new StringModel();
				
		//status form
		Form form = new Form("form", new Model(stringModel));
		form.setOutputMarkupId(true);
        		
		//status field
		final TextField statusField = new TextField("message", new PropertyModel(stringModel, "string"));
		statusField.setMarkupId("messageinput");
		statusField.setOutputMarkupId(true);
        statusField.setOutputMarkupPlaceholderTag(true);
        statusField.add(new StatusFieldCounterBehaviour());
        form.add(statusField);
        
        //link the status textfield field with the focus/blur function via this dynamic js 
        //also link with counter
        //add(new StatusFieldCounterBehaviour());
        
        
        //submit button
		IndicatingAjaxButton submitButton = new IndicatingAjaxButton("submit", form) {

			private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form form) {
				
				//get the backing model
        		StringModel stringModel = (StringModel) form.getModelObject();
				
				//get userId from sakaiProxy
				String userId = sakaiProxy.getCurrentUserId();
				
				//get the status. if its the default text, do not update, although we should clear the model
				String statusMessage = StringUtils.trim(stringModel.getString());
				if(StringUtils.isBlank(statusMessage) || StringUtils.equals(statusMessage, defaultStatus)) {
					log.warn("Status for userId: " + userId + " was not updated because they didn't enter anything.");
					return;
				}

				//save status from userProfile
				if(statusLogic.setUserStatus(userId, statusMessage)) {
					log.info("Saved status for: " + userId);
					
					//post update event
					sakaiProxy.postEvent(ProfileConstants.EVENT_STATUS_UPDATE, "/profile/"+userId, true);

					//update twitter
					externalIntegrationLogic.sendMessageToTwitter(userId, statusMessage);
					
					// post to walls if wall enabled
					if (true == sakaiProxy.isWallEnabledGlobally()) {
						wallLogic.addNewStatusToWall(statusMessage, sakaiProxy.getCurrentUserId());
					}
					
					//repaint status component
					ProfileStatusRenderer newStatus = new ProfileStatusRenderer("status", userId, null, "tiny");
					newStatus.setOutputMarkupId(true);
					status.replaceWith(newStatus);
					newStatus.setVisible(true);
					
					//also show the clear link
					clearLink.setVisible(true);
					
					if(target != null) {
						target.add(newStatus);
						target.add(clearLink);
						status=newStatus; //update reference
						
						//reset the field
						target.appendJavaScript("autoFill('#" + statusField.getMarkupId() + "', '" + defaultStatus + "');");
						
						//reset the counter
						target.appendJavaScript("countChars('#" + statusField.getMarkupId() + "');");

					}
					
				} else {
					log.error("Couldn't save status for: " + userId);
					String js = "alert('Failed to save status. If the problem persists, contact your system administrator.');";
					target.prependJavaScript(js);	
				}
				
            }
		};
		submitButton.setModel(new ResourceModel("button.sayit"));
		form.add(submitButton);
		
        //add form to container
		statusFormContainer.add(form);
		
		//if not editable, hide the entire form
		if(!editable) {
			statusFormContainer.setVisible(false);
		}
		
		
		add(statusFormContainer);
		
	}
	
	public class StatusFieldCounterBehaviour extends Behavior {
	 		
	    public void renderHead(Component component, IHeaderResponse response) {
	    	response.render(StringHeaderItem.forString("<script type=\"text/javascript\">" +
					"$(document).ready( function(){" +
					"autoFill('#" + component.getMarkupId() + "', '" + defaultStatus + "');" +
					"countChars('#" + component.getMarkupId() + "');" +
					"});" +
				"</script>"));
	    }
	}
	
}
