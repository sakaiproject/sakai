package uk.ac.lancs.e_science.profile2.tool.pages;


import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.image.resource.BufferedDynamicImageResource;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;

import uk.ac.lancs.e_science.profile2.api.ProfileImageManager;
import uk.ac.lancs.e_science.profile2.api.ProfileUtilityManager;
import uk.ac.lancs.e_science.profile2.hbm.SearchResult;
import uk.ac.lancs.e_science.profile2.tool.components.AjaxIndicator;
import uk.ac.lancs.e_science.profile2.tool.components.ErrorLevelsFeedbackMessageFilter;
import uk.ac.lancs.e_science.profile2.tool.components.FeedbackLabel;
import uk.ac.lancs.e_science.profile2.tool.components.IconWithClueTip;
import uk.ac.lancs.e_science.profile2.tool.models.FriendAction;
import uk.ac.lancs.e_science.profile2.tool.models.Search;
import uk.ac.lancs.e_science.profile2.tool.pages.windows.AddFriend;


public class MySearch extends BasePage {

	private transient Logger log = Logger.getLogger(MySearch.class);
	private transient Search search;
	private List<SearchResult> results = new ArrayList<SearchResult>();

	
	public MySearch() {
		
		if(log.isDebugEnabled()) log.debug("MySearch()");
		
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
		sbnForm.add(new IconWithClueTip("sbnNameToolTip", IconWithClueTip.INFO_IMAGE, new ResourceModel("text.search.byname.tooltip")));
		
		//search feedback
        final FeedbackLabel sbnNameFeedback = new FeedbackLabel("searchNameFeedback", sbnNameField, new ResourceModel("text.search.nothing"));
        sbnNameFeedback.setOutputMarkupId(true);
        //sbnNameField.add(new ComponentVisualErrorBehavior("onblur", sbnNameFeedback)); //removed for now
        sbnForm.add(sbnNameFeedback);
		
		//form indicator - need to use IAjaxIndicatorAware TODO
		final AjaxIndicator sbnIndicator = new AjaxIndicator("sbnIndicator");
		sbnIndicator.setOutputMarkupId(true);
		sbnIndicator.setVisible(false);
		sbnForm.add(sbnIndicator);
		
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
		sbiForm.add(new IconWithClueTip("sbiInterestToolTip", IconWithClueTip.INFO_IMAGE, new ResourceModel("text.search.byinterest.tooltip")));
		
		//search feedback
        final FeedbackLabel sbiInterestFeedback = new FeedbackLabel("searchInterestFeedback", sbiInterestField, new ResourceModel("text.search.nothing"));
        sbiInterestFeedback.setOutputMarkupId(true);
        //sbnNameField.add(new ComponentVisualErrorBehavior("onblur", sbnNameFeedback)); //removed for now
        sbiForm.add(sbiInterestFeedback);
		
		//form indicator - need to use IAjaxIndicatorAware TODO
		final AjaxIndicator sbiIndicator = new AjaxIndicator("sbiIndicator");
		sbiIndicator.setOutputMarkupId(true);
		sbiIndicator.setVisible(false);
		sbiForm.add(sbiIndicator);
		
		
		
		
		
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
		    	final byte[] photo;
		    		
		    	//is profile and profile image allowed to be viewed by this user/friend?
				final boolean isProfileAllowed = searchResult.isProfileAllowed();
				
		    	if(isProfileAllowed) {
		    		photo = profile.getCurrentProfileImageForUser(userUuid, ProfileImageManager.PROFILE_IMAGE_THUMBNAIL);
		    	} else {
		    		photo = null;
		    	}
		    	
		    	//photo (if allowed or default)
		    	if(photo != null && photo.length > 0){
		    		
					BufferedDynamicImageResource photoResource = new BufferedDynamicImageResource(){
						private static final long serialVersionUID = 1L;

						protected byte[] getImageData() {
							return photo;
						}
					};
				
					//so it always refreshes between searches
					item.add(new NonCachingImage("result-photo",photoResource));
				} else {
					item.add(new ContextImage("result-photo",new Model(ProfileImageManager.UNAVAILABLE_IMAGE)));
				}
		    	
		    	
		    	
		    	
		    	
		    	//name and link to profile (if allowed or no link)
		    	Link profileLink = new Link("result-profileLink") {
					private static final long serialVersionUID = 1L;

					public void onClick() {
						//if user found themself, go to own profile, else show other profile
						if(userUuid.equals(currentUserUuid)) {
							setResponsePage(new MyProfile());
						} else {
							//gets it from the model because we set the model later
							setResponsePage(new ViewProfile((String)getModelObject()));
						}
					}
					
					
				};
				
				if(isProfileAllowed) {
					profileLink.setModel(new Model(userUuid));
				} else {
					profileLink.setEnabled(false);
				}

				profileLink.add(new Label("result-name", displayName));
		    	item.add(profileLink);
		    	
		    	
		    	/* ACTIONS */
		    	
		    	//setup state of this User-result pair
		    	boolean friend = searchResult.isFriend();
		    	boolean friendRequestToThisPerson = searchResult.isFriendRequestToThisPerson();
				boolean friendRequestFromThisPerson = searchResult.isFriendRequestFromThisPerson();
				boolean isFriendsListVisible = searchResult.isFriendsListVisible();
		    	
		    	//ADD FRIEND MODAL WINDOW
				final ModalWindow connectionWindow = new ModalWindow("result-connectionWindow");
		    	connectionWindow.setContent(new AddFriend(connectionWindow.getContentId(), connectionWindow, friendActionModel, currentUserUuid, userUuid, photo)); 

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
				if(friend) {
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
				} else if (userUuid.equals(currentUserUuid)) {
					connectionLink.setEnabled(false);
					c1.setVisible(false);
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
		
		AjaxFallbackButton sbnSubmitButton = new AjaxFallbackButton("sbnSubmit", new ResourceModel("button.search.byname"), sbnForm) {
			protected void onSubmit(AjaxRequestTarget target, Form form) {
				//need to show the busyindicator here TODO

				if(target != null) {
					//get the model
					Search search = (Search) form.getModelObject();
					
					//get search field
					String searchText = search.getSearchName();
					
					if(log.isDebugEnabled()) { log.debug("MySearch() search.getSearchName(): " + searchText);}
					
					//clear the interest search field in model and repaint to clear value
					search.setSearchInterest("");
					
					//search both UDB and Sakaiperson for matches.
					results = new ArrayList<SearchResult>(profile.findUsersByNameOrEmail(searchText, currentUserUuid));
	
					int numResults = results.size();
					int maxResults = ProfileUtilityManager.MAX_SEARCH_RESULTS;
					
					//text
					if(numResults == 0) {
						numSearchResults.setModel(new StringResourceModel("text.search.byname.no.results", null, new Object[]{ searchText } ));
						resultsContainer.setVisible(false);
					} else if (numResults == 1) {
						numSearchResults.setModel(new StringResourceModel("text.search.byname.one.result", null, new Object[]{ searchText } ));
						resultsContainer.setVisible(true);
					} else if (numResults >= maxResults) {
						numSearchResults.setModel(new StringResourceModel("text.search.toomany.results", null, new Object[]{ searchText, maxResults } ));
						resultsContainer.setVisible(true);
					} else {
						numSearchResults.setModel(new StringResourceModel("text.search.byname.all.results", null, new Object[]{ numResults, searchText } ));
						resultsContainer.setVisible(true);
					}
					
					//repaint components
					target.addComponent(sbiInterestField);
					target.addComponent(resultsContainer);
					target.addComponent(numSearchResults);
					target.appendJavascript("setMainFrameHeight(window.name);");	

				}
				
				
            }
		};
		sbnForm.add(sbnSubmitButton);
        add(sbnForm);
        
        
        
        /* 
		 * 
		 * SEARCH BY INTEREST SUBMIT
		 * 
		 */
		
		AjaxFallbackButton sbiSubmitButton = new AjaxFallbackButton("sbiSubmit", new ResourceModel("button.search.byinterest"), sbiForm) {
			protected void onSubmit(AjaxRequestTarget target, Form form) {
				//need to show the busyindicator here TODO

				if(target != null) {
					//get the model
					Search search = (Search) form.getModelObject();
					
					//get search field
					String searchText = search.getSearchInterest();
					
					if(log.isDebugEnabled()) log.debug("MySearch() search.getSearchInterest(): " + searchText);
					
					//clear the name search field in model and repaint to clear value
					search.setSearchName("");
					
					//search SakaiPerson for matches
					results = new ArrayList<SearchResult>(profile.findUsersByInterest(searchText, currentUserUuid));
										
					int numResults = results.size();
					int maxResults = ProfileUtilityManager.MAX_SEARCH_RESULTS;

					//text
					if(numResults == 0) {
						numSearchResults.setModel(new StringResourceModel("text.search.byinterest.no.results", null, new Object[]{ searchText } ));
						resultsContainer.setVisible(false);
					} else if (numResults == 1) {
						numSearchResults.setModel(new StringResourceModel("text.search.byinterest.one.result", null, new Object[]{ searchText } ));
						resultsContainer.setVisible(true);
					} else if (numResults >= maxResults) {
						numSearchResults.setModel(new StringResourceModel("text.search.toomany.results", null, new Object[]{ searchText, maxResults } ));
						resultsContainer.setVisible(true);
					} else {
						numSearchResults.setModel(new StringResourceModel("text.search.byinterest.all.results", null, new Object[]{ numResults, searchText } ));
						resultsContainer.setVisible(true);
					}
					
					
					
					
					//repaint components
					target.addComponent(sbnNameField);
					target.addComponent(numSearchResults);
					target.addComponent(resultsContainer);
					target.appendJavascript("setMainFrameHeight(window.name);");	

				}
				
				
            }
		};
		sbiForm.add(sbiSubmitButton);
        add(sbiForm);
   	
	}

	
	
}




