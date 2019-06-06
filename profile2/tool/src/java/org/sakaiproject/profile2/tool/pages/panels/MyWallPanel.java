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

import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.RestartResponseException;
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
import org.apache.wicket.spring.injection.annot.SpringBean;

import org.sakaiproject.profile2.logic.ProfileWallLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.WallItem;
import org.sakaiproject.profile2.tool.components.ErrorLevelsFeedbackMessageFilter;
import org.sakaiproject.profile2.tool.dataproviders.WallItemDataProvider;
import org.sakaiproject.profile2.tool.pages.MyProfile;
import org.sakaiproject.profile2.util.ProfileConstants;

/**
 * Container for viewing user's own wall.
 * 
 * @author d.b.robinson@lancaster.ac.uk
 */
@Slf4j
public class MyWallPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name="org.sakaiproject.profile2.logic.SakaiProxy")
	private SakaiProxy sakaiProxy;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileWallLogic")
	private ProfileWallLogic wallLogic;
	
	/**
	 * Creates a new instance of <code>MyWallPanel</code>.
	 * 
	 * This method is used when a super user is viewing the profile of another
	 * user.
	 */
	public MyWallPanel(String panelId, String userUuid) {

		super(panelId);

		// double check for super user
		if (false == sakaiProxy.isSuperUser()) {
			log.error("MyWallPanel: user " + sakaiProxy.getCurrentUserId()
					+ " attempted to access MyWallPanel for " + userUuid
					+ ". Redirecting...");

			throw new RestartResponseException(new MyProfile());
		}
		
		renderWallPanel(userUuid);
	}
	
	/**
	 * Creates a new instance of <code>MyWallPanel</code>.
	 */
	public MyWallPanel(String panelId) {

		super(panelId);

		renderWallPanel(sakaiProxy.getCurrentUserId());
	}
		
	private void renderWallPanel(final String userUuid) {

		setOutputMarkupId(true);
		
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
		Form<WallItem> form = new Form<WallItem>("myWallPostForm",
				new Model<WallItem>(wallItem));
		form.setOutputMarkupId(true);
		add(form);

		// form submit feedback
		final Label formFeedback = new Label("formFeedback");
		formFeedback.setOutputMarkupPlaceholderTag(true);
		form.add(formFeedback);

		final FeedbackPanel feedback = new FeedbackPanel("feedback");
		feedback.setOutputMarkupId(true);
		form.add(feedback);

		int[] filteredErrorLevels = new int[] { FeedbackMessage.ERROR };
		feedback.setFilter(new ErrorLevelsFeedbackMessageFilter(
				filteredErrorLevels));

		// container for posting to my wall
		WebMarkupContainer myWallPostContainer = new WebMarkupContainer(
				"myWallPostContainer");
		final TextArea myWallPost = new TextArea("myWallPost",new PropertyModel<String>(wallItem, "text"));
		myWallPost.setMarkupId("wallpostinput");
		myWallPost.setOutputMarkupId(true);

		myWallPostContainer.add(myWallPost);

		form.add(myWallPostContainer);

		IndicatingAjaxButton submitButton = new IndicatingAjaxButton(
				"myWallPostSubmit", form) {
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
					formFeedback.setDefaultModel(new ResourceModel(
							"error.wall.post.failed"));
					formFeedback.add(new AttributeModifier("class", true,
							new Model<String>("alertMessage")));
					target.add(formFeedback);
				} else {
					
					replaceSelf(target, userUuid);
				}
			}
			
			//@Override
			//protected IAjaxCallDecorator getAjaxCallDecorator() {
			//	return CKEditorTextArea.getAjaxCallDecoratedToUpdateElementForAllEditorsOnPage();
			//}
		};
		submitButton.setModel(new ResourceModel("button.wall.post"));
		myWallPostContainer.add(submitButton);

		WallItemDataProvider provider = new WallItemDataProvider(userUuid);

		// if no wall items, display a message
		if (0 == provider.size()) {
			add(new Label("wallInformationMessage", new ResourceModel(
					"text.wall.no.items")));
		} else {
			// blank label when there are items to display
			add(new Label("wallInformationMessage"));
		}

		final DataView<WallItem> wallItemsDataView = new DataView<WallItem>(
				"wallItems", provider) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(Item<WallItem> item) {

				WallItem wallItem = (WallItem) item.getDefaultModelObject();

				// pass reference to MyWallPanel for updating when posts are removed
				item
						.add(new WallItemPanel("wallItemPanel", userUuid,
								wallItem, MyWallPanel.this));
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

	// this is used to replace the panel so the paging is updated
	protected void replaceSelf(AjaxRequestTarget target, String userUuid) {
		MyWallPanel newPanel;
		if (true == sakaiProxy.isSuperUser()) {
			newPanel= new MyWallPanel(MyWallPanel.this.getId(), userUuid);
		} else {
			newPanel= new MyWallPanel(MyWallPanel.this.getId());
		}
		newPanel.setOutputMarkupId(true);
		MyWallPanel.this.replaceWith(newPanel);
		if (null != target) {
			target.add(newPanel);
			target.appendJavaScript("setMainFrameHeight(window.name);");
		}
		
	}
	
}
