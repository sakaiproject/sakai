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
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.util.ProfileConstants;

@Slf4j
public class KudosPanel extends Panel {

	private static final long serialVersionUID = 1L;

	
	@SpringBean(name="org.sakaiproject.profile2.logic.SakaiProxy")
	private SakaiProxy sakaiProxy;
	
	
	public KudosPanel(String id, final String ownerUserId, final String viewingUserId, final int score) {
		super(id);
		
		log.debug("KudosPanel()");
		
		//heading	
		Label heading = new Label("heading");
		
		if(viewingUserId.equals(ownerUserId)) {
			heading.setDefaultModel(new ResourceModel("heading.widget.my.kudos"));
		} else {
			String displayName = sakaiProxy.getUserDisplayName(ownerUserId);
			heading.setDefaultModel(new StringResourceModel("heading.widget.view.kudos", null, new Object[]{ displayName } ));
		}
		add(heading);
		
		//score
		add(new Label("kudosRating", String.valueOf(score)));
		
		String img = getImage(score);
		
		//images
		add(new ContextImage("kudosImgLeft", img));
		add(new ContextImage("kudosImgRight", img));

	}
	
	private String getImage(int score) {
		
		if(score >= 8) {
			return ProfileConstants.AWARD_GOLD_IMG;
		}
		
		if(score == 7) {
			return ProfileConstants.AWARD_SILVER_IMG;
		}
		if(score >= 5) {
			return ProfileConstants.AWARD_BRONZE_IMG;
		}
		return ProfileConstants.AWARD_NORMAL_IMG;

	}
	
	
	
	/*
	private String getImage(BigDecimal score) {
		
		BigDecimal fifty = new BigDecimal(50);
		BigDecimal seventy = new BigDecimal(70);
		BigDecimal ninety = new BigDecimal(90);

		
		if(score.compareTo(ninety) >= 0) {
			return ProfileConstants.AWARD_GOLD_IMG;
		}
		if(score.compareTo(seventy) >= 0) {
			return ProfileConstants.AWARD_SILVER_IMG;
		}
		if(score.compareTo(fifty) >= 0) {
			return ProfileConstants.AWARD_BRONZE_IMG;
		}
		return ProfileConstants.AWARD_NORMAL_IMG;
	}
	*/
	
	

}
