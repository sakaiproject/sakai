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

package org.sakaiproject.profile2.tool.pages;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.servlet.http.Cookie;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.profile2.model.ProfileSearchTerm;
import org.sakaiproject.profile2.tool.components.IconWithClueTip;
import org.sakaiproject.profile2.tool.components.ProfileImageRenderer;
import org.sakaiproject.profile2.tool.components.ProfileStatusRenderer;
import org.sakaiproject.profile2.tool.models.FriendAction;
import org.sakaiproject.profile2.tool.models.StringModel;
import org.sakaiproject.profile2.tool.pages.windows.AddFriend;
import org.sakaiproject.profile2.types.PrivacyType;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.profile2.util.ProfileUtils;
import org.sakaiproject.util.FormattedText;


public class MySearch extends BasePage {

	private List<Person> results = new ArrayList<Person>();
	private static final Logger log = Logger.getLogger(MySearch.class); 
	
	private WebMarkupContainer numSearchResultsContainer;
	private Label numSearchResults;
	private WebMarkupContainer resultsContainer;
	private AjaxButton clearButton;
	private AjaxButton clearHistoryButton;
	private TextField<String> sbiInterestField;
	private TextField<String> sbnNameField;
	
	// Used independently of search history for current search, and
	// transient because Cookie isn't serializable	 
    private transient Cookie searchCookie = null;
    
	public MySearch() {
		
		log.debug("MySearch()");
		
		disableLink(searchLink);
		
		//check for current search cookie	 
		searchCookie = getWebRequestCycle().getWebRequest().getCookie(ProfileConstants.SEARCH_COOKIE);
		
		//setup model to store the actions in the modal windows
		final FriendAction friendActionModel = new FriendAction();
		
		//get current user info
		final String currentUserUuid = sakaiProxy.getCurrentUserId();
		final String currentUserType = sakaiProxy.getUserType(currentUserUuid);
				
		/* 
		 * 
		 * SEARCH BY NAME FORM
		 * 
		 */
		
        //heading	
		Label sbnHeading = new Label("sbnHeading", new ResourceModel("heading.search.byname"));
		add(sbnHeading);
		
		//setup form	
        final StringModel sbnStringModel = new StringModel();        
        Form<StringModel> sbnForm = new Form<StringModel>("sbnForm", new Model<StringModel>(sbnStringModel));
        sbnForm.setOutputMarkupId(true);
		
		//search field
        sbnForm.add(new Label("sbnNameLabel", new ResourceModel("text.search.byname")));
        sbnNameField = new TextField<String>("searchName", new PropertyModel<String>(sbnStringModel, "string"));
		sbnNameField.setRequired(true);
		sbnNameField.setOutputMarkupId(true);
		sbnForm.add(sbnNameField);
		sbnForm.add(new IconWithClueTip("sbnNameToolTip", ProfileConstants.INFO_IMAGE, new ResourceModel("text.search.byname.tooltip")));
		
		
		
		/* 
		 * 
		 * SEARCH BY INTEREST FORM
		 * 
		 */
		
        //heading	
		Label sbiHeading = new Label("sbiHeading", new ResourceModel("heading.search.byinterest"));
		add(sbiHeading);
		
		
		//setup form
        final StringModel sbiStringModel = new StringModel();
        Form<StringModel> sbiForm = new Form<StringModel>("sbiForm", new Model<StringModel>(sbiStringModel));
        sbiForm.setOutputMarkupId(true);
		
		//search field
        sbiForm.add(new Label("sbiInterestLabel", new ResourceModel("text.search.byinterest")));
        sbiInterestField = new TextField<String>("searchInterest", new PropertyModel<String>(sbiStringModel, "string"));
		sbiInterestField.setRequired(true);
		sbiInterestField.setOutputMarkupId(true);
		sbiForm.add(sbiInterestField);
		sbiForm.add(new IconWithClueTip("sbiInterestToolTip", ProfileConstants.INFO_IMAGE, new ResourceModel("text.search.byinterest.tooltip")));
		
		/* 
		 * 
		 * RESULTS
		 * 
		 */
		
		//search results label/container
		numSearchResultsContainer = new WebMarkupContainer("numSearchResultsContainer");
		numSearchResultsContainer.setOutputMarkupPlaceholderTag(true);
		numSearchResults = new Label("numSearchResults");
		numSearchResults.setOutputMarkupId(true);
		numSearchResults.setEscapeModelStrings(false);
		numSearchResultsContainer.add(numSearchResults);
		
		//clear results button
		Form<Void> clearResultsForm = new Form<Void>("clearResults");
		clearResultsForm.setOutputMarkupPlaceholderTag(true);

		clearButton = new AjaxButton("clearButton", clearResultsForm) {
			private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				
				// clear cookie if present	 
                if (null != searchCookie) {	 
                        getWebRequestCycle().getWebResponse().clearCookie(searchCookie);	 
                }
                
				//clear the fields, hide self, then repaint
				sbnNameField.clearInput();
				sbnNameField.updateModel();
				
				sbiInterestField.clearInput();
				sbiInterestField.updateModel();
				
				numSearchResultsContainer.setVisible(false);
				resultsContainer.setVisible(false);
				clearButton.setVisible(false);
				
				target.addComponent(sbnNameField);
				target.addComponent(sbiInterestField);
				target.addComponent(numSearchResultsContainer);
				target.addComponent(resultsContainer);
				target.addComponent(this);
			}				
		};
		clearButton.setOutputMarkupPlaceholderTag(true);
		if (null == searchCookie) {
			clearButton.setVisible(false); //invisible until we have something to clear
		}
		clearButton.setModel(new ResourceModel("button.search.clear"));
		clearResultsForm.add(clearButton);
		numSearchResultsContainer.add(clearResultsForm);
		
		add(numSearchResultsContainer);
		
		// model to wrap search results
		LoadableDetachableModel<List<Person>> resultsModel = new LoadableDetachableModel<List<Person>>(){
			private static final long serialVersionUID = 1L;

			protected List<Person> load() {
				return results;
			}
		};	
				
		//container which wraps list
		resultsContainer = new WebMarkupContainer("searchResultsContainer");
		resultsContainer.setOutputMarkupPlaceholderTag(true);
		if (null == searchCookie) {
			resultsContainer.setVisible(false); //hide initially
		}
		
		//connection window
		final ModalWindow connectionWindow = new ModalWindow("connectionWindow");
		
		//search results
		final PageableListView<Person> resultsListView = new PageableListView<Person>("searchResults",
				resultsModel, sakaiProxy.getMaxSearchResultsPerPage()) {
			
			private static final long serialVersionUID = 1L;

			protected void populateItem(final ListItem<Person> item) {
		        
		    	Person person = (Person)item.getModelObject();
		    	
		    	//get basic values
		    	final String userUuid = person.getUuid();
		    	final String displayName = person.getDisplayName();
		    	final String userType = person.getType();

		    	//get connection status
		    	int connectionStatus = connectionsLogic.getConnectionStatus(currentUserUuid, userUuid);
		    	boolean friend = (connectionStatus == ProfileConstants.CONNECTION_CONFIRMED) ? true : false;
		    	
		    	//image wrapper, links to profile
		    	Link<String> friendItem = new Link<String>("searchResultPhotoWrap") {
					private static final long serialVersionUID = 1L;
					public void onClick() {
						setResponsePage(new ViewProfile(userUuid));
					}
				};
				
				//image
				friendItem.add(new ProfileImageRenderer("searchResultPhoto", person, ProfileConstants.PROFILE_IMAGE_THUMBNAIL, false));
				item.add(friendItem);
		    	
		    	//name and link to profile (if allowed or no link)
		    	Link<String> profileLink = new Link<String>("searchResultProfileLink", new Model<String>(userUuid)) {
					private static final long serialVersionUID = 1L;

					public void onClick() {
						//if user found themself, go to own profile, else show other profile
						if(userUuid.equals(currentUserUuid)) {
							setResponsePage(new MyProfile());
						} else {
							//gets userUuid of other user from the link's model
							setResponsePage(new ViewProfile((String)getModelObject()));
						}
					}
				};
				
				profileLink.add(new Label("searchResultName", displayName));
		    	item.add(profileLink);
		    	
		    	//status component
		    	ProfileStatusRenderer status = new ProfileStatusRenderer("searchResultStatus", person, "search-result-status-msg", "search-result-status-date");
				status.setOutputMarkupId(true);
				item.add(status);
		    	
		    	
		    	/* ACTIONS */
				boolean isFriendsListVisible = privacyLogic.isActionAllowed(userUuid, currentUserUuid, PrivacyType.PRIVACY_OPTION_MYFRIENDS);
				boolean isConnectionAllowed = sakaiProxy.isConnectionAllowedBetweenUserTypes(userType, currentUserType);
		    	

		    	//ADD CONNECTION LINK
		    	final WebMarkupContainer c1 = new WebMarkupContainer("connectionContainer");
		    	c1.setOutputMarkupId(true);

				if(!isConnectionAllowed){
					//add blank components - TODO turn this into an EmptyLink component
					AjaxLink<Void> emptyLink = new AjaxLink<Void>("connectionLink"){
						private static final long serialVersionUID = 1L;
						public void onClick(AjaxRequestTarget target) {}
					};
					emptyLink.add(new Label("connectionLabel"));
					c1.add(emptyLink);
					c1.setVisible(false);
				} else {
					//render the link
			    	final Label connectionLabel = new Label("connectionLabel");
					connectionLabel.setOutputMarkupId(true);
					
			    	final AjaxLink<String> connectionLink = new AjaxLink<String>("connectionLink", new Model<String>(userUuid)) {
						private static final long serialVersionUID = 1L;
						public void onClick(AjaxRequestTarget target) {
							
							//get this item, reinit some values and set content for modal
					    	final String userUuid = (String)getModelObject();
					    	connectionWindow.setContent(new AddFriend(connectionWindow.getContentId(), connectionWindow, friendActionModel, currentUserUuid, userUuid)); 
							
					    	// connection modal window handler 
							connectionWindow.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
								private static final long serialVersionUID = 1L;
								public void onClose(AjaxRequestTarget target){
					            	if(friendActionModel.isRequested()) { 
					            		connectionLabel.setDefaultModel(new ResourceModel("text.friend.requested"));
										add(new AttributeModifier("class", true, new Model<String>("instruction icon connection-request")));
					            		setEnabled(false);
					            		target.addComponent(c1);
					            	}
					            }
					        });						
							//in preparation for the window being closed, update the text. this will only
							//be put into effect if its a successful model update from the window close
					    	//connectionLabel.setModel(new ResourceModel("text.friend.requested"));
							//this.add(new AttributeModifier("class", true, new Model("instruction")));
							//this.setEnabled(false);
							//friendActionModel.setUpdateThisComponentOnSuccess(this);
							
							connectionWindow.show(target);
							target.appendJavascript("fixWindowVertical();"); 
			            	
						}
					};
					
					connectionLink.add(connectionLabel);
					
					//setup 'add connection' link
					if(StringUtils.equals(userUuid, currentUserUuid)) {
						connectionLabel.setDefaultModel(new ResourceModel("text.friend.self"));
						connectionLink.add(new AttributeModifier("class", true, new Model<String>("instruction icon profile")));
						connectionLink.setEnabled(false);
					} else if(friend) {
						connectionLabel.setDefaultModel(new ResourceModel("text.friend.confirmed"));
						connectionLink.add(new AttributeModifier("class", true, new Model<String>("instruction icon connection-confirmed")));
						connectionLink.setEnabled(false);
					} else if (connectionStatus == ProfileConstants.CONNECTION_REQUESTED) {
						connectionLabel.setDefaultModel(new ResourceModel("text.friend.requested"));
						connectionLink.add(new AttributeModifier("class", true, new Model<String>("instruction icon connection-request")));
						connectionLink.setEnabled(false);					
					} else if (connectionStatus == ProfileConstants.CONNECTION_INCOMING) {
						connectionLabel.setDefaultModel(new ResourceModel("text.friend.pending"));
						connectionLink.add(new AttributeModifier("class", true, new Model<String>("instruction icon connection-request")));
						connectionLink.setEnabled(false);
					} else {
						connectionLabel.setDefaultModel(new ResourceModel("link.friend.add"));
					}
					connectionLink.setOutputMarkupId(true);
					c1.add(connectionLink);
				}
				
				item.add(c1);
				
				
				//VIEW FRIENDS LINK
				WebMarkupContainer c2 = new WebMarkupContainer("viewFriendsContainer");
		    	c2.setOutputMarkupId(true);
		    	
		    	final AjaxLink<String> viewFriendsLink = new AjaxLink<String>("viewFriendsLink") {
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
						//if user found themself, go to MyFriends, else, ViewFriends
						if(userUuid.equals(currentUserUuid)) {
							setResponsePage(new MyFriends());
						} else {
							setResponsePage(new ViewFriends(userUuid));
						}
					}
				};
				final Label viewFriendsLabel = new Label("viewFriendsLabel", new ResourceModel("link.view.friends"));
				viewFriendsLink.add(viewFriendsLabel);
				
				//hide if not allowed
				if(!isFriendsListVisible) {
					viewFriendsLink.setEnabled(false);
					c2.setVisible(false);
				}
				viewFriendsLink.setOutputMarkupId(true);
				c2.add(viewFriendsLink);
				item.add(c2);
				
				WebMarkupContainer c3 = new WebMarkupContainer("emailContainer");
		    	c3.setOutputMarkupId(true);
		    	
		    	ExternalLink emailLink = new ExternalLink("emailLink",
						"mailto:" + person.getProfile().getEmail(),
						new ResourceModel("profile.email").getObject());
		    	
				c3.add(emailLink);
				
				if (StringUtils.isBlank(person.getProfile().getEmail()) ||
						false == privacyLogic.isActionAllowed(person.getUuid(), currentUserUuid, PrivacyType.PRIVACY_OPTION_CONTACTINFO)) {
					c3.setVisible(false);
				}
				item.add(c3);
				
				WebMarkupContainer c4 = new WebMarkupContainer("websiteContainer");
		    	c4.setOutputMarkupId(true);
		    	
		    	// TODO home page, university profile URL or academic/research URL (see PRFL-35)
		    	ExternalLink websiteLink = new ExternalLink("websiteLink", person.getProfile()
						.getHomepage(), new ResourceModel(
						"profile.homepage").getObject()).setPopupSettings(new PopupSettings());
		    	
		    	c4.add(websiteLink);
		    	
				if (StringUtils.isBlank(person.getProfile().getHomepage()) || 
						false == privacyLogic.isActionAllowed(person.getUuid(), currentUserUuid, PrivacyType.PRIVACY_OPTION_CONTACTINFO)) {
					
					c4.setVisible(false);
				}
				item.add(c4);
				
				// TODO personal, academic or business (see PRFL-35)
				
				if (true == privacyLogic.isActionAllowed(
						person.getUuid(), currentUserUuid,  PrivacyType.PRIVACY_OPTION_BASICINFO)) {
					
					item.add(new Label("searchResultSummary",
							StringUtils.abbreviate(ProfileUtils.stripHtml(
									person.getProfile().getPersonalSummary()), 200)));
				} else {
					item.add(new Label("searchResultSummary", ""));
				}
		    }
		};
		
		resultsListView.add(new MySearchCookieBehavior(resultsListView));
		resultsContainer.add(resultsListView);

		final PagingNavigator searchResultsNavigator = new PagingNavigator("searchResultsNavigator", resultsListView);
		searchResultsNavigator.setOutputMarkupId(true);
		searchResultsNavigator.setVisible(false);

		resultsContainer.add(searchResultsNavigator);

		add(connectionWindow);
		
		//add results container
		add(resultsContainer);
		
		/*
		 * SEARCH HISTORY
		 */
		
		final WebMarkupContainer searchHistoryContainer = new WebMarkupContainer("searchHistoryContainer");
		searchHistoryContainer.setOutputMarkupPlaceholderTag(true);
		
		Label searchHistoryLabel = new Label("searchHistoryLabel", new ResourceModel("text.search.history"));
		searchHistoryContainer.add(searchHistoryLabel);
		
		IModel<List<ProfileSearchTerm>> searchHistoryModel =  new LoadableDetachableModel<List<ProfileSearchTerm>>() {

			private static final long serialVersionUID = 1L;

			@Override
			protected List<ProfileSearchTerm> load() {
				List<ProfileSearchTerm> searchHistory = searchLogic.getSearchHistory(currentUserUuid);
				if (null == searchHistory) {
					return new ArrayList<ProfileSearchTerm>();
				} else {
					return searchHistory;
				}
			}
			
		};
		ListView<ProfileSearchTerm> searchHistoryList = new ListView<ProfileSearchTerm>("searchHistoryList", searchHistoryModel) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(final ListItem<ProfileSearchTerm> item) {

				AjaxLink<String> link = new AjaxLink<String>("previousSearchLink") {

					private static final long serialVersionUID = 1L;

					@Override
					public void onClick(AjaxRequestTarget target) {
						if (null != target) {
														
							// post view event
							sakaiProxy.postEvent(ProfileConstants.EVENT_SEARCH_BY_NAME, "/profile/"+currentUserUuid, false);
							
							ProfileSearchTerm searchTerm = item.getModelObject();
							// this will update its position in list
							searchLogic.addSearchTermToHistory(currentUserUuid, searchTerm);
							
							if (ProfileConstants.SEARCH_TYPE_NAME.equals(searchTerm.getSearchType())) {
								sbnStringModel.setString(searchTerm.getSearchTerm());
								searchByName(resultsListView, searchResultsNavigator,
										searchHistoryContainer, target, searchTerm.getSearchTerm());
								
							} else if (ProfileConstants.SEARCH_TYPE_INTEREST.equals(searchTerm.getSearchType())) {
								sbiStringModel.setString(searchTerm.getSearchTerm());
								searchByInterest(resultsListView, searchResultsNavigator,
										searchHistoryContainer, target, searchTerm.getSearchTerm());
							}
						}
					}
					
				};
				link.add(new Label("previousSearchLabel", item.getModelObject().getSearchTerm()));
				item.add(link);
			}
		};
		
		searchHistoryContainer.add(searchHistoryList);
		add(searchHistoryContainer);
		
		if (null == searchLogic.getSearchHistory(currentUserUuid)) {
			searchHistoryContainer.setVisible(false);
		}
		
		//clear button
		Form<Void> clearHistoryForm = new Form<Void>("clearHistory");
		clearHistoryForm.setOutputMarkupPlaceholderTag(true);

		clearHistoryButton = new AjaxButton("clearHistoryButton", clearHistoryForm) {
			private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				
				searchLogic.clearSearchHistory(currentUserUuid);
				
				//clear the fields, hide self, then repaint
				sbnNameField.clearInput();
				sbnNameField.updateModel();
				
				sbiInterestField.clearInput();
				sbiInterestField.updateModel();
				
				searchHistoryContainer.setVisible(false);
				clearHistoryButton.setVisible(false);
				
				target.addComponent(sbnNameField);
				target.addComponent(sbiInterestField);
				target.addComponent(searchHistoryContainer);
				target.addComponent(this);
			}				
		};
		clearHistoryButton.setOutputMarkupPlaceholderTag(true);

		if (null == searchLogic.getSearchHistory(currentUserUuid)) {
			clearHistoryButton.setVisible(false); //invisible until we have something to clear
		}
		clearHistoryButton.setModel(new ResourceModel("button.search.history.clear"));
		clearHistoryForm.add(clearHistoryButton);
		searchHistoryContainer.add(clearHistoryForm);
		
		/* 
		 * 
		 * SEARCH BY NAME SUBMIT
		 * 
		 */
		
		IndicatingAjaxButton sbnSubmitButton = new IndicatingAjaxButton("sbnSubmit", sbnForm) {
			
			private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {

				if(target != null) {
					
					//get the model and text entered
					StringModel model = (StringModel) form.getModelObject();
					String searchText = ProfileUtils.stripHtml(model.getString());
										
					log.debug("MySearch() search.getSearchName(): " + searchText);
					
					if(StringUtils.isBlank(searchText)){
						return;
					}
				
					// save search terms
					ProfileSearchTerm searchTerm = new ProfileSearchTerm();
					searchTerm.setUserUuid(currentUserUuid);
					searchTerm.setSearchType(ProfileConstants.SEARCH_TYPE_NAME);
					searchTerm.setSearchTerm(searchText);
					searchTerm.setSearchPageNumber(0);
					searchTerm.setSearchDate(new Date());
					
					searchLogic.addSearchTermToHistory(currentUserUuid, searchTerm);
					
					// set cookie for current search (page 0 when submitting new search)
					setSearchCookie(ProfileConstants.SEARCH_TYPE_NAME, searchText, 0);
					
					//post view event
					sakaiProxy.postEvent(ProfileConstants.EVENT_SEARCH_BY_NAME, "/profile/"+currentUserUuid, false);
					
					searchByName(resultsListView, searchResultsNavigator, searchHistoryContainer,
							target, searchText);	
				}				
            }
		};
		sbnSubmitButton.setModel(new ResourceModel("button.search.byname"));
		sbnForm.add(sbnSubmitButton);
        add(sbnForm);
        
        /* 
		 * 
		 * SEARCH BY INTEREST SUBMIT
		 * 
		 */
		
        IndicatingAjaxButton sbiSubmitButton = new IndicatingAjaxButton("sbiSubmit", sbiForm) {
			
			private static final long serialVersionUID = 1L;
        	
        	protected void onSubmit(AjaxRequestTarget target, Form<?> form) {

				if(target != null) {
					
					//get the model and text entered
					StringModel model = (StringModel) form.getModelObject();
					String searchText = ProfileUtils.stripHtml(model.getString());

					log.debug("MySearch() search.getSearchInterest(): " + searchText);
					
					if(StringUtils.isBlank(searchText)){
						return;
					}
					
					// save search terms
					ProfileSearchTerm searchTerm = new ProfileSearchTerm();
					searchTerm.setUserUuid(currentUserUuid);
					searchTerm.setSearchType(ProfileConstants.SEARCH_TYPE_INTEREST);
					searchTerm.setSearchTerm(searchText);
					searchTerm.setSearchPageNumber(0);
					searchTerm.setSearchDate(new Date());
					
					searchLogic.addSearchTermToHistory(currentUserUuid, searchTerm);
					
					// set cookie for current search (page 0 when submitting new search)
					setSearchCookie(ProfileConstants.SEARCH_TYPE_INTEREST, searchText, 0);
					
					//post view event
					sakaiProxy.postEvent(ProfileConstants.EVENT_SEARCH_BY_INTEREST, "/profile/"+currentUserUuid, false);
					
					searchByInterest(resultsListView, searchResultsNavigator,
							searchHistoryContainer,	target, searchText);
				}
            }
		};
		sbiSubmitButton.setModel(new ResourceModel("button.search.byinterest"));
		sbiForm.add(sbiSubmitButton);
        add(sbiForm);
        
        if (null != searchCookie) {
        	
        	String searchString = getCookieSearchString(searchCookie.getValue());
        	
        	if (searchCookie.getValue().startsWith(ProfileConstants.SEARCH_TYPE_NAME)) {
				searchByName(resultsListView, searchResultsNavigator, searchHistoryContainer, null, searchString);
				sbnStringModel.setString(searchString);
        	} else if (searchCookie.getValue().startsWith(ProfileConstants.SEARCH_TYPE_INTEREST)) {
        		searchByInterest(resultsListView, searchResultsNavigator, searchHistoryContainer, null, searchString);
        		sbiStringModel.setString(searchString);
        	}
        }
	}
	
	// use null target when using cookie
	private void searchByName(
			final PageableListView<Person> resultsListView,
			final PagingNavigator searchResultsNavigator,
			final WebMarkupContainer searchHistoryContainer,
			AjaxRequestTarget target, String searchTerm) {
		
		//clear the interest search field
		sbiInterestField.clearInput();
		sbiInterestField.updateModel();
				
		//search both UDP and SakaiPerson for matches.
		results = new ArrayList<Person>(searchLogic.findUsersByNameOrEmail(searchTerm));
		Collections.sort(results);
		
		int numResults = results.size();
		int maxResults = sakaiProxy.getMaxSearchResults();
		int maxResultsPerPage = sakaiProxy.getMaxSearchResultsPerPage();
		
		// set current page if previously-viewed search
		int currentPage = getCurrentPageNumber();
				
		//show the label wrapper
		numSearchResultsContainer.setVisible(true);
		
		//text
		if(numResults == 0) {
			numSearchResults.setDefaultModel(new StringResourceModel("text.search.byname.no.results", null, new Object[]{ searchTerm } ));
			resultsContainer.setVisible(false);
			searchResultsNavigator.setVisible(false);
		} else if (numResults == 1) {
			numSearchResults.setDefaultModel(new StringResourceModel("text.search.byname.one.result", null, new Object[]{ searchTerm } ));
			resultsContainer.setVisible(true);
			searchResultsNavigator.setVisible(false);
		} else if (numResults == maxResults) {
			resultsListView.setCurrentPage(currentPage);
			numSearchResults.setDefaultModel(new StringResourceModel("text.search.toomany.results", null, new Object[]{ searchTerm, maxResults, maxResults } ));
			resultsContainer.setVisible(true);
			searchResultsNavigator.setVisible(true);
		} else if (numResults > maxResultsPerPage) {
	        resultsListView.setCurrentPage(currentPage);
			numSearchResults.setDefaultModel(new StringResourceModel("text.search.byname.paged.results", null, new Object[]{ numResults, resultsListView.getViewSize(), searchTerm } ));
			resultsContainer.setVisible(true);
			searchResultsNavigator.setVisible(true);
		} else {
			resultsListView.setCurrentPage(currentPage);
			numSearchResults.setDefaultModel(new StringResourceModel("text.search.byname.all.results", null, new Object[]{ numResults, searchTerm } ));
			resultsContainer.setVisible(true);
			searchResultsNavigator.setVisible(false);
		}
		
		if (null != target) {
			//repaint components
			target.addComponent(sbnNameField);
			target.addComponent(sbiInterestField);
			target.addComponent(clearButton);
			target.addComponent(numSearchResultsContainer);
			clearButton.setVisible(true);
			target.addComponent(resultsContainer);
			clearHistoryButton.setVisible(true);
			searchHistoryContainer.setVisible(true);
			target.addComponent(searchHistoryContainer);
			target.appendJavascript("setMainFrameHeight(window.name);");
		}
	}
	
	// use null target when using cookie
	private void searchByInterest(
			final PageableListView<Person> resultsListView,
			final PagingNavigator searchResultsNavigator,
			WebMarkupContainer searchHistoryContainer,
			AjaxRequestTarget target, String searchTerm) {
		
		//clear the name search field
		sbnNameField.clearInput();
		sbnNameField.updateModel();
		
		//search SakaiPerson for matches
		results = new ArrayList<Person>(searchLogic.findUsersByInterest(searchTerm, sakaiProxy.isBusinessProfileEnabled()));
		Collections.sort(results);
		
		int numResults = results.size();
		int maxResults = sakaiProxy.getMaxSearchResults();
		int maxResultsPerPage = sakaiProxy.getMaxSearchResultsPerPage();

		// set current page if previously-viewed search
		int currentPage = getCurrentPageNumber();
		
		//show the label wrapper
		numSearchResultsContainer.setVisible(true);
		
		//text
		if(numResults == 0) {
			numSearchResults.setDefaultModel(new StringResourceModel("text.search.byinterest.no.results", null, new Object[]{ searchTerm } ));
			resultsContainer.setVisible(false);
			searchResultsNavigator.setVisible(false);
		} else if (numResults == 1) {
			numSearchResults.setDefaultModel(new StringResourceModel("text.search.byinterest.one.result", null, new Object[]{ searchTerm } ));
			resultsContainer.setVisible(true);
			searchResultsNavigator.setVisible(false);
		} else if (numResults == maxResults) {
			resultsListView.setCurrentPage(currentPage);
			numSearchResults.setDefaultModel(new StringResourceModel("text.search.toomany.results", null, new Object[]{ searchTerm, maxResults, maxResults } ));
			resultsContainer.setVisible(true);
			searchResultsNavigator.setVisible(true);
		} else if (numResults > maxResultsPerPage) {
			resultsListView.setCurrentPage(currentPage);
			numSearchResults.setDefaultModel(new StringResourceModel("text.search.byinterest.paged.results", null, new Object[]{ numResults, resultsListView.getViewSize(), searchTerm } ));
			resultsContainer.setVisible(true);
			searchResultsNavigator.setVisible(true);
		} else {
			resultsListView.setCurrentPage(currentPage);
			numSearchResults.setDefaultModel(new StringResourceModel("text.search.byinterest.all.results", null, new Object[]{ numResults, searchTerm } ));
			resultsContainer.setVisible(true);
			searchResultsNavigator.setVisible(false);
		}
		
		if (null != target) {
			//repaint components
			target.addComponent(sbnNameField);
			target.addComponent(sbiInterestField);
			target.addComponent(clearButton);
			target.addComponent(numSearchResultsContainer);
			clearButton.setVisible(true);
			target.addComponent(resultsContainer);
			clearHistoryButton.setVisible(true);
			searchHistoryContainer.setVisible(true);
			target.addComponent(searchHistoryContainer);
			target.appendJavascript("setMainFrameHeight(window.name);");
		}
	}
	
	private int getCurrentPageNumber() {
		if (null == searchCookie) {
			return 0;
		} else {
			return getCookiePageNumber();
		}
	}
	
	private int getCookiePageNumber() {
		return Integer.parseInt(searchCookie.getValue().substring(
			searchCookie.getValue().indexOf(ProfileConstants.SEARCH_COOKIE_VALUE_PREFIX_PAGE_MARKER) + 1,
			searchCookie.getValue().indexOf(ProfileConstants.SEARCH_COOKIE_VALUE_PREFIX_TERMINATOR)));
	}
	
	private void updatePageNumber(int pageNumber, String cookieString) {
		String searchType = getCookieSearchType(cookieString);
		String searchString = getCookieSearchString(cookieString);

		setSearchCookie(searchType, searchString, pageNumber);
	}
	
	private void setSearchCookie(String searchCookieValuePrefix,
			String searchText, int searchPageNumber) {

		searchCookie = new Cookie(
				ProfileConstants.SEARCH_COOKIE,
				searchCookieValuePrefix
						+ ProfileConstants.SEARCH_COOKIE_VALUE_PREFIX_PAGE_MARKER
						+ searchPageNumber
						+ ProfileConstants.SEARCH_COOKIE_VALUE_PREFIX_TERMINATOR
						+ searchText);
		// don't persist indefinitely
		searchCookie.setMaxAge(-1);
		getWebRequestCycle().getWebResponse().addCookie(searchCookie);
	}
	
	private String getCookieSearchString(String cookieString) {
		return cookieString.substring(cookieString.indexOf(ProfileConstants.SEARCH_COOKIE_VALUE_PREFIX_TERMINATOR) + 1);
	}

	private String getCookieSearchType(String cookieString) {
		return cookieString.substring(0, cookieString.indexOf(ProfileConstants.SEARCH_COOKIE_VALUE_PREFIX_PAGE_MARKER));
	}
		         
	// behaviour so we can set the current search cookie when the navigator page changes	 
	private class MySearchCookieBehavior extends AbstractBehavior {

		private static final long serialVersionUID = 1L;

		private PageableListView<Person> view;

		public MySearchCookieBehavior(PageableListView<Person> view) {
			this.view = view;
		}

		@Override
		public void beforeRender(Component component) {
			if (searchCookie != null) {
				updatePageNumber(view.getCurrentPage(), searchCookie.getValue());
			}
		}
	}
		
}