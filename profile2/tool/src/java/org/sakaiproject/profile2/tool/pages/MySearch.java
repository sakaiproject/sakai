package org.sakaiproject.profile2.tool.pages;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.profile2.model.SearchResult;
import org.sakaiproject.profile2.tool.components.ErrorLevelsFeedbackMessageFilter;
import org.sakaiproject.profile2.tool.components.FeedbackLabel;
import org.sakaiproject.profile2.tool.components.IconWithClueTip;
import org.sakaiproject.profile2.tool.components.ProfileImageRenderer;
import org.sakaiproject.profile2.tool.components.ProfileStatusRenderer;
import org.sakaiproject.profile2.tool.models.FriendAction;
import org.sakaiproject.profile2.tool.models.Search;
import org.sakaiproject.profile2.tool.pages.windows.AddFriend;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.util.FormattedText;


public class MySearch extends BasePage {

	private transient Search search;
	private List<SearchResult> results = new ArrayList<SearchResult>();
	private static final Logger log = Logger.getLogger(MySearch.class); 
	
	public MySearch() {
		
		log.debug("MySearch()");
		
		//setup model to store the actions in the modal windows
		final FriendAction friendActionModel = new FriendAction();
		
		//get current user
		final String currentUserUuid = sakaiProxy.getCurrentUserId();
		
		// FeedbackPanel - so we activate feedback
        final FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        add(feedback);
        // filteredErrorLevels will not be shown in the FeedbackPanel
        int[] filteredErrorLevels = new int[]{FeedbackMessage.ERROR};
        feedback.setFilter(new ErrorLevelsFeedbackMessageFilter(filteredErrorLevels));
     
        //create model for form
		search = new Search();
		CompoundPropertyModel searchModel = new CompoundPropertyModel(search);
        
		/* 
		 * 
		 * SEARCH BY NAME FORM
		 * 
		 */
		
        //heading	
		Label sbnHeading = new Label("sbnHeading", new ResourceModel("heading.search.byname"));
		add(sbnHeading);
		
		//setup form		
		Form sbnForm = new Form("sbnForm", searchModel);
		sbnForm.setOutputMarkupId(true);
		
		//search field
        sbnForm.add(new Label("sbnNameLabel", new ResourceModel("text.search.byname")));
		final TextField sbnNameField = new TextField("searchName");
		sbnNameField.setRequired(true);
		sbnNameField.setOutputMarkupId(true);
		sbnForm.add(sbnNameField);
		sbnForm.add(new IconWithClueTip("sbnNameToolTip", ProfileConstants.INFO_IMAGE, new ResourceModel("text.search.byname.tooltip")));
		
		//search feedback
        final FeedbackLabel sbnNameFeedback = new FeedbackLabel("searchNameFeedback", sbnNameField, new ResourceModel("text.search.nothing"));
        sbnNameFeedback.setOutputMarkupId(true);
        //sbnNameField.add(new ComponentVisualErrorBehavior("onblur", sbnNameFeedback)); //removed for now
        sbnForm.add(sbnNameFeedback);
		
		
		/* 
		 * 
		 * SEARCH BY INTEREST FORM
		 * 
		 */
		
        //heading	
		Label sbiHeading = new Label("sbiHeading", new ResourceModel("heading.search.byinterest"));
		add(sbiHeading);
		
		
		//setup form		
		Form sbiForm = new Form("sbiForm", searchModel);
		sbiForm.setOutputMarkupId(true);
		
		//search field
        sbiForm.add(new Label("sbiInterestLabel", new ResourceModel("text.search.byinterest")));
		final TextField sbiInterestField = new TextField("searchInterest");
		sbiInterestField.setRequired(true);
		sbiInterestField.setOutputMarkupId(true);
		sbiForm.add(sbiInterestField);
		sbiForm.add(new IconWithClueTip("sbiInterestToolTip", ProfileConstants.INFO_IMAGE, new ResourceModel("text.search.byinterest.tooltip")));
		
		//search feedback
        final FeedbackLabel sbiInterestFeedback = new FeedbackLabel("searchInterestFeedback", sbiInterestField, new ResourceModel("text.search.nothing"));
        sbiInterestFeedback.setOutputMarkupId(true);
        //sbnNameField.add(new ComponentVisualErrorBehavior("onblur", sbnNameFeedback)); //removed for now
        sbiForm.add(sbiInterestFeedback);
		
		
		
		
		/* 
		 * 
		 * RESULTS
		 * 
		 */
		
		//search results label
		final Label numSearchResults = new Label("numSearchResults");
		numSearchResults.setOutputMarkupId(true);
		numSearchResults.setEscapeModelStrings(false);
		add(numSearchResults);
		
		// model to wrap search results
		LoadableDetachableModel resultsModel = new LoadableDetachableModel(){
			protected Object load() {
				return results;
			}
		};
		
		
				
		//container which wraps list
		final WebMarkupContainer resultsContainer = new WebMarkupContainer("searchResultsContainer");
		resultsContainer.setOutputMarkupPlaceholderTag(true);
		resultsContainer.setVisible(false); //hide initially
		
		//search results
		ListView resultsListView = new ListView("results-list", resultsModel) {
			private static final long serialVersionUID = 1L;

			protected void populateItem(ListItem item) {
		        
		    	//get SearchResult object
		    	//this contains info like if they are a friend and if their profile is visible etc
		    	SearchResult searchResult = (SearchResult)item.getModelObject();
		    	
		    	//get userUuid
		    	final String userUuid = searchResult.getUserUuid();
		    		
		    	//setup basic values
		    	String displayName = sakaiProxy.getUserDisplayName(userUuid);
		    		
		    	//is profile image allowed to be viewed by this user/friend?
				final boolean isProfileImageAllowed = searchResult.isProfileImageAllowed();
				
				//image
				item.add(new ProfileImageRenderer("result-photo", userUuid, isProfileImageAllowed, ProfileConstants.PROFILE_IMAGE_THUMBNAIL, false));
		    	
		    	//name and link to profile (if allowed or no link)
		    	Link profileLink = new Link("result-profileLink") {
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
				profileLink.setModel(new Model(userUuid));
				
				/* DEPRECATED, we always link now because of PRFL-24 
				if(isProfileAllowed) {
					profileLink.setModel(new Model(userUuid));
				} else {
					profileLink.setEnabled(false);
				}
				*/

				profileLink.add(new Label("result-name", displayName));
		    	item.add(profileLink);
		    	
		    	//status component
				ProfileStatusRenderer status = new ProfileStatusRenderer("result-status", userUuid, currentUserUuid, "friendsListInfoStatusMessage", "friendsListInfoStatusDate");
				status.setOutputMarkupId(true);
				item.add(status);
		    	
		    	
		    	
		    	/* ACTIONS */
		    	
		    	//setup state of this User-result pair
		    	boolean friend = searchResult.isFriend();
		    	boolean friendRequestToThisPerson = searchResult.isFriendRequestToThisPerson();
				boolean friendRequestFromThisPerson = searchResult.isFriendRequestFromThisPerson();
				boolean isFriendsListVisible = searchResult.isFriendsListVisible();
		    	
		    	//ADD FRIEND MODAL WINDOW
				final ModalWindow connectionWindow = new ModalWindow("result-connectionWindow");
		    	connectionWindow.setContent(new AddFriend(connectionWindow.getContentId(), connectionWindow, friendActionModel, currentUserUuid, userUuid)); 

		    	//ADD FRIEND LINK
		    	WebMarkupContainer c1 = new WebMarkupContainer("result-connectionContainer");
		    	c1.setOutputMarkupId(true);
		    	
		    	final AjaxLink connectionLink = new AjaxLink("result-connectionLink") {
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
						
						//target.appendJavascript("Wicket.Window.get().window.style.width='800px';");
						connectionWindow.show(target);
						target.appendJavascript("fixWindowVertical();"); 

					}
				};
				final Label connectionLabel = new Label("result-connectionLabel");
				connectionLabel.setOutputMarkupId(true);
				connectionLink.add(connectionLabel);
				
				//setup 'add friend' link
				if(StringUtils.equals(userUuid, currentUserUuid)) {
					connectionLabel.setModel(new ResourceModel("text.friend.self"));
					connectionLink.add(new AttributeModifier("class", true, new Model("instruction")));
					connectionLink.setEnabled(false);
				} else if(friend) {
					connectionLabel.setModel(new ResourceModel("text.friend.confirmed"));
					connectionLink.add(new AttributeModifier("class", true, new Model("instruction")));
					connectionLink.setEnabled(false);
				} else if (friendRequestToThisPerson) {
					connectionLabel.setModel(new ResourceModel("text.friend.requested"));
					connectionLink.add(new AttributeModifier("class", true, new Model("instruction")));
					connectionLink.setEnabled(false);					
				} else if (friendRequestFromThisPerson) {
					connectionLabel.setModel(new ResourceModel("text.friend.pending"));
					connectionLink.add(new AttributeModifier("class", true, new Model("instruction")));
					connectionLink.setEnabled(false);
				} else {
					connectionLabel.setModel(new ResourceModel("link.friend.add"));
				}
				connectionLink.setOutputMarkupId(true);
				c1.add(connectionLink);
				item.add(c1);
				
				
				//VIEW FRIENDS LINK
				WebMarkupContainer c2 = new WebMarkupContainer("result-friendsContainer");
		    	c2.setOutputMarkupId(true);
		    	
		    	final AjaxLink viewFriendsLink = new AjaxLink("result-viewFriendsLink") {
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
				final Label viewFriendsLabel = new Label("result-viewFriendsLabel", new ResourceModel("link.view.friends"));
				viewFriendsLink.add(viewFriendsLabel);
				
				//hide if not allowed
				if(!isFriendsListVisible) {
					viewFriendsLink.setEnabled(false);
					c2.setVisible(false);
				}
				viewFriendsLink.setOutputMarkupId(true);
				c2.add(viewFriendsLink);
				item.add(c2);
				
				
				// ADD FRIEND MODAL WINDOW HANDLER 
				connectionWindow.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
					private static final long serialVersionUID = 1L;

					public void onClose(AjaxRequestTarget target){
						
		            	if(friendActionModel.isRequested()) { 
		            		connectionLabel.setModel(new ResourceModel("text.friend.requested"));
							connectionLink.add(new AttributeModifier("class", true, new Model("instruction")));
							connectionLink.setEnabled(false);
		            		
							//TODO: recalculate if we can see this person's friend list and show the link if so
		            		
		            		//repaint
		            		target.addComponent(connectionLink);
		            	}
		            }
		        });
				item.add(connectionWindow);
				
				
		    }
		};
		resultsContainer.add(resultsListView);
		
		
		
		//add results container
		add(resultsContainer);
		
		
		
		
		
		
		
		
		
			
		 
		/* 
		 * 
		 * SEARCH BY NAME SUBMIT
		 * 
		 */
		
		IndicatingAjaxButton sbnSubmitButton = new IndicatingAjaxButton("sbnSubmit", sbnForm) {
			
			private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form form) {

				if(target != null) {
					//get the model
					Search search = (Search) form.getModelObject();
					
					//get search field
					String searchText = FormattedText.processFormattedText(search.getSearchName(), new StringBuffer());
					
					log.debug("MySearch() search.getSearchName(): " + searchText);
				
					//clear the interest search field in model and repaint to clear value
					search.setSearchInterest("");
					
					//search both UDB and Sakaiperson for matches.
					results = new ArrayList<SearchResult>(profileLogic.findUsersByNameOrEmail(searchText, currentUserUuid));
	
					int numResults = results.size();
					int maxResults = ProfileConstants.MAX_SEARCH_RESULTS;
					
					//text
					if(numResults == 0) {
						numSearchResults.setModel(new StringResourceModel("text.search.byname.no.results", null, new Object[]{ searchText } ));
						resultsContainer.setVisible(false);
					} else if (numResults == 1) {
						numSearchResults.setModel(new StringResourceModel("text.search.byname.one.result", null, new Object[]{ searchText } ));
						resultsContainer.setVisible(true);
					} else if (numResults == maxResults) {
						numSearchResults.setModel(new StringResourceModel("text.search.toomany.results", null, new Object[]{ searchText, maxResults } ));
						resultsContainer.setVisible(true);
					} else {
						numSearchResults.setModel(new StringResourceModel("text.search.byname.all.results", null, new Object[]{ numResults, searchText } ));
						resultsContainer.setVisible(true);
					}
					
					//post view event
					sakaiProxy.postEvent(ProfileConstants.EVENT_SEARCH_BY_NAME, "/profile/"+currentUserUuid, false);
					
					//repaint components
					target.addComponent(sbiInterestField);
					target.addComponent(resultsContainer);
					target.addComponent(numSearchResults);
					target.appendJavascript("setMainFrameHeight(window.name);");	

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
        	
        	protected void onSubmit(AjaxRequestTarget target, Form form) {

				if(target != null) {
					//get the model
					Search search = (Search) form.getModelObject();
					
					//get search field
					String searchText = FormattedText.processFormattedText(search.getSearchInterest(), new StringBuffer());

					log.debug("MySearch() search.getSearchInterest(): " + searchText);
					
					//clear the name search field in model and repaint to clear value
					search.setSearchName("");
					
					//search SakaiPerson for matches
					results = new ArrayList<SearchResult>(profileLogic.findUsersByInterest(searchText, currentUserUuid));
										
					int numResults = results.size();
					int maxResults = ProfileConstants.MAX_SEARCH_RESULTS;

					//text
					if(numResults == 0) {
						numSearchResults.setModel(new StringResourceModel("text.search.byinterest.no.results", null, new Object[]{ searchText } ));
						resultsContainer.setVisible(false);
					} else if (numResults == 1) {
						numSearchResults.setModel(new StringResourceModel("text.search.byinterest.one.result", null, new Object[]{ searchText } ));
						resultsContainer.setVisible(true);
					} else if (numResults == maxResults) {
						numSearchResults.setModel(new StringResourceModel("text.search.toomany.results", null, new Object[]{ searchText, maxResults } ));
						resultsContainer.setVisible(true);
					} else {
						numSearchResults.setModel(new StringResourceModel("text.search.byinterest.all.results", null, new Object[]{ numResults, searchText } ));
						resultsContainer.setVisible(true);
					}
					
					//post view event
					sakaiProxy.postEvent(ProfileConstants.EVENT_SEARCH_BY_INTEREST, "/profile/"+currentUserUuid, false);
					
					//repaint components
					target.addComponent(sbnNameField);
					target.addComponent(numSearchResults);
					target.addComponent(resultsContainer);
					target.appendJavascript("setMainFrameHeight(window.name);");	

				}
				
				
            }
		};
		sbiSubmitButton.setModel(new ResourceModel("button.search.byinterest"));
		sbiForm.add(sbiSubmitButton);
        add(sbiForm);
   	
	}

	/* reinit for deserialisation (ie back button) */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		log.debug("MySearch has been deserialized.");
		//re-init our transient objects
		profileLogic = getProfileLogic();
		sakaiProxy = getSakaiProxy();
	}
	
	
}




