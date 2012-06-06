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
package org.sakaiproject.profile2.tool.pages.windows;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.Message;
import org.sakaiproject.profile2.tool.models.NewMessageModel;

/**
 * ModalWindow panel used when we know who we want to send the message to, ie when link is clicked from a user's profile or connection list
 * Doesn't contain the AutoComplete Field as we already know who it is.
 */
public class QuickMessageTo extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name="org.sakaiproject.profile2.logic.SakaiProxy")
	private SakaiProxy sakaiProxy;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileLogic")
	private ProfileLogic profileLogic;
	
	
	public QuickMessageTo(String id, final ModalWindow window, final String uuidTo){
        super(id);

        //window setup
		window.setTitle(new ResourceModel("title.message.compose")); 
		//window.setInitialHeight(150);
		//window.setInitialWidth(500);
		window.setResizable(false);
		
		//current userId
		final String userId = sakaiProxy.getCurrentUserId();
		
		//setup model
		NewMessageModel messageHelper = new NewMessageModel();
		messageHelper.setTo(uuidTo);
		messageHelper.setFrom(userId);
		
		//setup form	
		Form<Message> form = new Form<Message>("form");
		
		//to label
		form.add(new Label("toLabel", new ResourceModel("message.to")));
		//to label
		form.add(new Label("toContent", new Model<String>(sakaiProxy.getUserDisplayName(uuidTo))));
		
		
		//subject
		form.add(new Label("subjectLabel", new ResourceModel("message.subject")));
		TextField<String> subjectField = new TextField<String>("subjectField");
		form.add(subjectField);
		
		//body
		form.add(new Label("messageLabel", new ResourceModel("message.message")));
		final TextArea<String> messageField = new TextArea<String>("messageField");
		messageField.setRequired(true);
		form.add(messageField);
		
		//send button
		IndicatingAjaxButton sendButton = new IndicatingAjaxButton("sendButton", form) {
			private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form form) {
				
				
            }
			
			protected void onError(AjaxRequestTarget target, Form form) {
				
			
			}
		};
		form.add(sendButton);
		sendButton.setModel(new ResourceModel("button.message.send"));
		
		add(form);
		
        
        
    }
		
}



