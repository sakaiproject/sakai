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
package org.sakaiproject.profile2.tool.components;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.profile2.logic.ProfileConnectionsLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.util.ProfileConstants;

/**
 * Component to render the online status of a user
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class OnlinePresenceIndicator extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileConnectionsLogic")
	private ProfileConnectionsLogic connectionsLogic;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.SakaiProxy")
	private SakaiProxy sakaiProxy;
	
	public OnlinePresenceIndicator(String id, String userUuid) {
		super(id);
		
		//get user's firstname
		String firstname = sakaiProxy.getUserFirstName(userUuid);
		if(StringUtils.isBlank(firstname)){
			firstname = new StringResourceModel("profile.name.first.none", null).getString();
		}
		
		//get user's online status
		int status = connectionsLogic.getOnlineStatus(userUuid);
		
		//get the mapping
		Map<String,String> m = mapStatus(status);
		
		//tooltip text
		Label text = new Label("text", new StringResourceModel(m.get("text"), null, new Object[]{ firstname } ));
		text.setOutputMarkupId(true);
		add(text);
		
		//we need to id of the text span so that we can map it to the link.
		//the cluetip functions automatically hide it for us.
		StringBuilder textId = new StringBuilder();
		textId.append("#");
		textId.append(text.getMarkupId());
		
		//link
		AjaxFallbackLink link = new AjaxFallbackLink("link") {
			public void onClick(AjaxRequestTarget target) {
				//nothing
			}
		};
		link.add(new AttributeModifier("rel", true, new Model(textId)));
		link.add(new AttributeModifier("href", true, new Model(textId)));
		
		//image
		ContextImage image = new ContextImage("icon",new Model(m.get("url")));
		link.add(image);
		
		add(link);
	
	}

	/**
	 * Map an online status to an image url
	 * 
	 * @param status
	 * @return map of relative image url and text. There are two keys, "url" and "text". The text is a key that needs to be used though.
	 */
	private Map<String,String> mapStatus(int status){
		
		Map<String,String> m = new HashMap<String,String>();
		
		if(status == ProfileConstants.ONLINE_STATUS_OFFLINE){
			m.put("url", ProfileConstants.ONLINE_STATUS_OFFLINE_IMG);
			m.put("text", "text.profile.presence.offline");
		}
		else if(status == ProfileConstants.ONLINE_STATUS_ONLINE){
			m.put("url", ProfileConstants.ONLINE_STATUS_ONLINE_IMG);
			m.put("text", "text.profile.presence.online");
		}
		else if(status == ProfileConstants.ONLINE_STATUS_AWAY){
			m.put("url", ProfileConstants.ONLINE_STATUS_AWAY_IMG);
			m.put("text", "text.profile.presence.away");
		} 
		//status will only ever be these three values.
		
		return m;
		
	}
	

}
