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

import java.util.Date;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.profile2.logic.ProfileConnectionsLogic;
import org.sakaiproject.profile2.logic.ProfilePrivacyLogic;
import org.sakaiproject.profile2.logic.ProfileWallLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.WallItem;
import org.sakaiproject.profile2.tool.components.ErrorLevelsFeedbackMessageFilter;
import org.sakaiproject.profile2.tool.dataproviders.WallItemDataProvider;
import org.sakaiproject.profile2.types.PrivacyType;
import org.sakaiproject.profile2.util.ProfileConstants;


/**
 * Container for viewing the wall of someone else.
 * 
 * @author d.b.robinson@lancaster.ac.uk
 */
public class ViewWallPanel extends Panel {

	private static final long serialVersionUID = 1L;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.SakaiProxy")
	private SakaiProxy sakaiProxy;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileWallLogic")
	private ProfileWallLogic wallLogic;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfilePrivacyLogic")
	private ProfilePrivacyLogic privacyLogic;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileConnectionsLogic")
	private ProfileConnectionsLogic connectionsLogic;
	
	public ViewWallPanel(String panelId, final String userUuid) {

		super(panelId);

		setOutputMarkupId(true);
		
		final String currentUserId = sakaiProxy.getCurrentUserId();
		
		// container which wraps list
		final WebMarkupContainer wallItemsContainer = new WebMarkupContainer(
				"wallItemsContainer");

		wallItemsContainer.setOutputMarkupId(true);
		add(wallItemsContainer);
		
		WallItem wallItem = new WallItem();
		wallItem.setUserUuid(userUuid);
		// always post to my wall as current user, to ensure super users cannot
		// make posts as other users
		wallItem.setCreatorUuid(sakaiProxy.getCurrentUserId());
		wallItem.setType(ProfileConstants.WALL_ITEM_TYPE_POST);
		
		// form for posting to my wall
		Form<WallItem> form = new Form<WallItem>("viewWallPostForm", new Model<WallItem>(wallItem));
		form.setOutputMarkupId(true);
		add(form);
		
		if (false == privacyLogic.isActionAllowed(userUuid, sakaiProxy.getCurrentUserId(), PrivacyType.PRIVACY_OPTION_MYWALL)) {
			form.setEnabled(false);
			form.setVisible(false);
		}
		
		// form submit feedback
		final Label formFeedback = new Label("formFeedback");
		formFeedback.setOutputMarkupPlaceholderTag(true);
		form.add(formFeedback);
		
        final FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        form.add(feedback);
        
        int[] filteredErrorLevels = new int[]{FeedbackMessage.ERROR};
        feedback.setFilter(new ErrorLevelsFeedbackMessageFilter(filteredErrorLevels));
		
		// container for posting to my wall
		WebMarkupContainer viewWallPostContainer = new WebMarkupContainer("viewWallPostContainer");
		final TextArea myWallPost = new TextArea("viewWallPost", new PropertyModel<String>(wallItem, "text"));
		
		viewWallPostContainer.add(myWallPost);
		
		form.add(viewWallPostContainer);
		
		IndicatingAjaxButton submitButton = new IndicatingAjaxButton("viewWallPostSubmit", form) {
			private static final long serialVersionUID = 1L;

			@SuppressWarnings("unchecked")
			protected void onSubmit(AjaxRequestTarget target, Form form) {
				
				if (myWallPost.getValue().equals("")) {
					formFeedback.setDefaultModel(new ResourceModel(
							"error.wall.post.empty"));
					formFeedback.add(new AttributeModifier("class", true,
							new Model<String>("alertMessage")));
					target.add(formFeedback);
					return;
				}
				
				if (false == save(form, userUuid)) {
					formFeedback.setDefaultModel(new ResourceModel("error.wall.post.failed"));
					formFeedback.add(new AttributeModifier("class", true, new Model<String>("alertMessage")));
					target.add(formFeedback);
				} else {
					ViewWallPanel newPanel = new ViewWallPanel(ViewWallPanel.this.getId(), userUuid);
					newPanel.setOutputMarkupId(true);
					ViewWallPanel.this.replaceWith(newPanel);
					if (null != target) {
						target.add(newPanel);
						target.appendJavaScript("setMainFrameHeight(window.name);");
					}
				}
			}
			
			//@Override
			//protected IAjaxCallDecorator getAjaxCallDecorator() {
			//	return CKEditorTextArea.getAjaxCallDecoratedToUpdateElementForAllEditorsOnPage();
			//}
		};
		submitButton.setModel(new ResourceModel("button.wall.post"));
		viewWallPostContainer.add(submitButton);
		
		// note: privacy check is handled by the logic component
		WallItemDataProvider provider = new WallItemDataProvider(userUuid);
				
		// if no wall items, display a message
		if (0 == provider.size()) {
			if (privacyLogic.isActionAllowed(userUuid, currentUserId,PrivacyType.PRIVACY_OPTION_MYWALL)) {
				
				// this user has no items on their wall
				add(new Label("wallInformationMessage",
						new StringResourceModel("text.view.wall.nothing", null, new Object[]{ sakaiProxy.getUserDisplayName(userUuid) } )).setEscapeModelStrings(false));				
			} else {
				// wall privacy is set to connections
				add(new Label("wallInformationMessage",
						new StringResourceModel("text.view.wall.restricted", null, new Object[]{ sakaiProxy.getUserDisplayName(userUuid) } )).setEscapeModelStrings(false));
			}
		} else {
			// blank label when there are items to display
			add(new Label("wallInformationMessage"));
		}

		DataView<WallItem> wallItemsDataView = new DataView<WallItem>(
				"wallItems", provider) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(Item<WallItem> item) {

				WallItem wallItem = (WallItem) item.getDefaultModelObject();
				item.add(new WallItemPanel("wallItemPanel", userUuid, wallItem));
				
				if (ProfileConstants.WALL_ITEM_TYPE_STATUS == wallItem.getType()) {					
					// only show if a super user or non-super user is permitted
					if (!sakaiProxy.isSuperUser() && !privacyLogic.isActionAllowed(wallItem.getCreatorUuid(), currentUserId, PrivacyType.PRIVACY_OPTION_MYSTATUS)) {
						
						item.setVisible(false);
					}
				}
			}
		};

		wallItemsDataView.setOutputMarkupId(true);

		if (provider.size() <= ProfileConstants.MAX_WALL_ITEMS_PER_PAGE) {
			wallItemsContainer.add(new AjaxPagingNavigator("navigator", wallItemsDataView).setVisible(false));
		} else {
			wallItemsContainer.add(new AjaxPagingNavigator("navigator", wallItemsDataView));
		}
		
		wallItemsDataView.setItemsPerPage(ProfileConstants.MAX_WALL_ITEMS_PER_PAGE);

		wallItemsContainer.add(wallItemsDataView);
	}
	
	// called when form is saved
	@SuppressWarnings("unchecked")
	private boolean save(Form form, String userUuid) {
		
		WallItem wallItem = (WallItem) form.getModelObject();
		wallItem.setDate(new Date());
		
		return wallLogic.postWallItemToWall(userUuid, wallItem);
	}
}
