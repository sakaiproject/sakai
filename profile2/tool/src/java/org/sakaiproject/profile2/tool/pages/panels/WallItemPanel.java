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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.WallItem;
import org.sakaiproject.profile2.model.WallItemComment;
import org.sakaiproject.profile2.tool.components.ProfileImage;
import org.sakaiproject.profile2.tool.models.WallAction;
import org.sakaiproject.profile2.tool.pages.ViewProfile;
import org.sakaiproject.profile2.tool.pages.windows.RemoveWallItem;
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
	
	/**
	 * Creates a new instance of <code>WallItemPanel</code>.
	 * 
	 * @param id
	 * @param userUuid the id of the user whose wall this item panel is on.
	 * @param wallItem
	 */
	public WallItemPanel(String id, final String userUuid, final WallItem wallItem) {
		this (id, userUuid, wallItem, null);
	}
	
	/**
	 * Creates a new instance of <code>WallItemPanel</code>.
	 * 
	 * @param id
	 * @param userUuid the id of the user whose wall this item panel is on.
	 * @param wallItem
	 * @param myWallPanel a reference to my wall panel for repainting.
	 */
	public WallItemPanel(String id, final String userUuid, final WallItem wallItem, final MyWallPanel myWallPanel) {
		super(id);

		setOutputMarkupId(true);
		
		// image wrapper, links to profile
		Link<String> wallItemPhoto = new Link<String>("wallItemPhotoWrap",
				new Model<String>(wallItem.getCreatorUuid())) {

			private static final long serialVersionUID = 1L;

			public void onClick() {
				setResponsePage(new ViewProfile(getModelObject()));
			}
		};

		// image
		ProfileImage photo = new ProfileImage("wallItemPhoto", new Model<String>(wallItem.getCreatorUuid()));
		photo.setSize(ProfileConstants.PROFILE_IMAGE_THUMBNAIL);
		wallItemPhoto.add(photo);
		
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

		add(new Label("wallItemDate", ProfileUtils.convertDateToString(wallItem
				.getDate(), ProfileConstants.WALL_DISPLAY_DATE_FORMAT)));
		
		// ACTIONS
		
		final ModalWindow wallItemActionWindow = new ModalWindow("wallItemActionWindow");
		add(wallItemActionWindow);
		
		final WallAction wallAction = new WallAction();
		// delete link
		final AjaxLink<WallItem> removeItemLink = new AjaxLink<WallItem>(
				"removeWallItemLink", new Model<WallItem>(wallItem)) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {

				wallItemActionWindow.setContent(new RemoveWallItem(wallItemActionWindow.getContentId(),
						wallItemActionWindow, wallAction, userUuid, this.getModelObject()));

				wallItemActionWindow.setWindowClosedCallback(new ModalWindow.WindowClosedCallback(){

					private static final long serialVersionUID = 1L;

					@Override
					public void onClose(AjaxRequestTarget target) {
						if (wallAction.isItemRemoved()) {
							myWallPanel.replaceSelf(target, userUuid);
						}
					}
				});
				
				wallItemActionWindow.show(target);
				target.appendJavaScript("fixWindowVertical();"); 
			}
		};

		removeItemLink.add(new Label("removeWallItemLabel", new ResourceModel("link.wall.item.remove")));
		removeItemLink.add(new AttributeModifier("title", true, new ResourceModel("link.title.wall.remove")));

		// not visible when viewing another user's wall
		if (false == sakaiProxy.getCurrentUserId().equals(userUuid)) {
			removeItemLink.setVisible(false);
		}
		
		add(removeItemLink);
		
		// panel for posting a comment that slides up/down
		final WallItemPostCommentPanel postCommentPanel = new WallItemPostCommentPanel("wallItemPostCommentPanel", userUuid, wallItem, this, myWallPanel);
		postCommentPanel.setOutputMarkupPlaceholderTag(true);
		postCommentPanel.setVisible(false);
		add(postCommentPanel);
		
		final AjaxLink<WallItem> commentItemLink = new AjaxLink<WallItem>(
				"commentWallItemLink", new Model<WallItem>(wallItem)) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
		
				postCommentPanel.setVisible(true);
				target.add(postCommentPanel);
				target.appendJavaScript("$('#" + postCommentPanel.getMarkupId() + "').slideDown();");
			}
		};

		commentItemLink.add(new Label("commentWallItemLabel", new ResourceModel("link.wall.item.comment")));
		commentItemLink.add(new AttributeModifier("title", true, new StringResourceModel("link.title.wall.comment", null, new Object[] { sakaiProxy.getUserDisplayName(wallItem.getCreatorUuid()) })));
		
		add(commentItemLink);
		
		if (ProfileConstants.WALL_ITEM_TYPE_EVENT == wallItem.getType()) {
			add(new Label("wallItemText", new ResourceModel(wallItem.getText())));
			
		} else if (ProfileConstants.WALL_ITEM_TYPE_POST == wallItem.getType()) {
			add(new Label("wallItemText", ProfileUtils.processHtml(wallItem
					.getText())).setEscapeModelStrings(false));
			
		} else if (ProfileConstants.WALL_ITEM_TYPE_STATUS == wallItem.getType()) {
			add(new Label("wallItemText", wallItem.getText()));

		}
				
		// COMMENTS
		
		ListView<WallItemComment> wallItemCommentsListView = new ListView<WallItemComment>(
				"wallItemComments", wallItem.getComments()) {

					private static final long serialVersionUID = 1L;

					@Override
					protected void populateItem(ListItem<WallItemComment> item) {
						
						WallItemComment comment = (WallItemComment) item.getDefaultModelObject();

						item.add(new WallItemCommentPanel("wallItemCommentPanel", comment));
					}
			
		};
		wallItemCommentsListView.setOutputMarkupId(true);
		add(wallItemCommentsListView);
		
	}

}
