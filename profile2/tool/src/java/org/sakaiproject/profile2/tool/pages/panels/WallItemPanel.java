/**
 * Copyright (c) 2008-2010 The Sakai Foundation
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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.profile2.logic.ProfileWallLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.WallItem;
import org.sakaiproject.profile2.tool.components.ProfileImageRenderer;
import org.sakaiproject.profile2.tool.pages.MyProfile;
import org.sakaiproject.profile2.tool.pages.ViewProfile;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.profile2.util.ProfileUtils;

/**
 * Wall item container.
 * 
 * TODO may make different WallItemPanel types for different wall item types.
 * 
 * @author d.b.robinson@lancaster.ac.uk
 */
public class WallItemPanel extends Panel {

	private static final long serialVersionUID = 1L;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.SakaiProxy")
	protected SakaiProxy sakaiProxy;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileWallLogic")
	private ProfileWallLogic wallLogic;
	
	/**
	 * Creates a new instance of <code>WallItemPanel</code>.
	 * 
	 * @param id
	 * @param userUuid the id of the user whose wall this item panel is on.
	 * @param wallItem
	 */
	public WallItemPanel(String id, final String userUuid, WallItem wallItem) {
		super(id);

		// image wrapper, links to profile
		Link<String> wallItemPhoto = new Link<String>("wallItemPhotoWrap",
				new Model<String>(wallItem.getCreatorUuid())) {

			private static final long serialVersionUID = 1L;

			public void onClick() {
				setResponsePage(new ViewProfile(getModelObject()));
			}
		};

		// image
		wallItemPhoto.add(new ProfileImageRenderer("wallItemPhoto", wallItem
				.getCreatorUuid()));
		add(wallItemPhoto);

		// name and link to profile
		Link<String> wallItemProfileLink = new Link<String>(
				"wallItemProfileLink", new Model<String>(wallItem
						.getCreatorUuid())) {

			private static final long serialVersionUID = 1L;

			public void onClick() {
				setResponsePage(new ViewProfile(getModelObject()));
			}

		};
		wallItemProfileLink.add(new Label("wallItemName", sakaiProxy
				.getUserDisplayName(wallItem.getCreatorUuid())));
		add(wallItemProfileLink);

		// TODO date has scope for internationalization?
		add(new Label("wallItemDate", ProfileUtils.convertDateToString(wallItem
				.getDate(), "dd MMMMM, HH:mm")));

		// wall item actions (TODO container?)
		
		// delete link
		final AjaxLink<WallItem> removeItemLink = new AjaxLink<WallItem>(
				"removeWallItemLink", new Model<WallItem>(wallItem)) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {

				wallLogic.removeWallItemFromWall(userUuid, this
						.getModelObject());

				// delete link should only appear when viewing own profile
				setResponsePage(new MyProfile());
			}
		};

		removeItemLink.add(new Label("removeWallItemLabel", new ResourceModel("link.wall.item.remove")));
		removeItemLink.add(new AttributeModifier("title", true, new ResourceModel("link.title.wall.remove")));

		// not visible when viewing another user's wall
		if (false == sakaiProxy.getCurrentUserId().equals(userUuid)) {
			removeItemLink.setVisible(false);
		}
		
		add(removeItemLink);

		if (ProfileConstants.WALL_ITEM_TYPE_EVENT == wallItem.getType()) {
			add(new Label("wallItemText", new ResourceModel(wallItem.getText())));
			
		} else if (ProfileConstants.WALL_ITEM_TYPE_POST == wallItem.getType()) {
			add(new Label("wallItemText", ProfileUtils.processHtml(wallItem
					.getText())).setEscapeModelStrings(false));
			
		} else if (ProfileConstants.WALL_ITEM_TYPE_STATUS == wallItem.getType()) {
			add(new Label("wallItemText", wallItem.getText()));

		}
		
	}

}
