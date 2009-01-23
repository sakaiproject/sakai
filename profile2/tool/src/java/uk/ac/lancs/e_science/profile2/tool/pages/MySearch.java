package uk.ac.lancs.e_science.profile2.tool.pages;


import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
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
import org.apache.wicket.markup.html.image.Image;
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
import uk.ac.lancs.e_science.profile2.hbm.SearchResult;
import uk.ac.lancs.e_science.profile2.tool.components.AjaxIndicator;
import uk.ac.lancs.e_science.profile2.tool.components.ErrorLevelsFeedbackMessageFilter;
import uk.ac.lancs.e_science.profile2.tool.components.FeedbackLabel;
import uk.ac.lancs.e_science.profile2.tool.components.IconWithClueTip;
import uk.ac.lancs.e_science.profile2.tool.models.Search;
import uk.ac.lancs.e_science.profile2.tool.pages.windows.AddFriend;
import uk.ac.lancs.e_science.profile2.tool.pages.windows.ConfirmFriend;
import uk.ac.lancs.e_science.profile2.tool.pages.windows.IgnoreFriend;
import uk.ac.lancs.e_science.profile2.tool.pages.windows.RemoveFriend;


public class MySearch extends BasePage {

	private transient Logger log = Logger.getLogger(MySearch.class);
	private transient Search search;
	private List<SearchResult> results = new ArrayList<SearchResult>();
	
	public MySearch() {
		
		if(log.isDebugEnabled()) log.debug("MySearch()");
		
		//get basePage
		final BasePage basePage = getBasePage();
		
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
						protected byte[] getImageData() {
							return photo;
						}
					};
				
					item.add(new Image("result-photo",photoResource));
				} else {
					item.add(new ContextImage("result-photo",new Model(ProfileImageManager.UNAVAILABLE_IMAGE)));
				}
		    	
		    	
		    	//name and link to profile (if allowed or no link)
		    	Link profileLink = new Link("result-profileLink") {

					public void onClick() {
						//if user found themself, go to own profile, else show other profile
						if(userUuid.equals(currentUserUuid)) {
							setResponsePage(new MyProfile());
						} else {
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
		    	
		    	//CONNECTION MODAL WINDOWS
				final ModalWindow addConnectionWindow = new ModalWindow("result-addConnectionWindow");
				final ModalWindow confirmConnectionWindow = new ModalWindow("result-confirmConnectionWindow");
				final ModalWindow ignoreConnectionWindow = new ModalWindow("result-ignoreConnectionWindow");
				final ModalWindow removeConnectionWindow = new ModalWindow("result-removeConnectionWindow");

				//CONNECTION STATUS LABEL
				final Label connectionStatusLabel = new Label("result-connectionStatus");
				connectionStatusLabel.setOutputMarkupId(true);
				item.add(connectionStatusLabel);
				
				//ADD CONNECTION LINK + LABEL
				final AjaxLink addConnectionLink = new AjaxLink("result-addConnectionLink") {
		    		public void onClick(AjaxRequestTarget target) {
		    			addConnectionWindow.show(target);
					}
				};
				final Label addConnectionLabel = new Label("result-addConnectionLabel");
				addConnectionLink.add(addConnectionLabel);
				addConnectionLink.setOutputMarkupId(true);
				item.add(addConnectionLink);

		    					
				//CONFIRM CONNECTION LINK + LABEL
				final AjaxLink confirmConnectionLink = new AjaxLink("result-confirmConnectionLink") {
		    		public void onClick(AjaxRequestTarget target) {
		    			confirmConnectionWindow.show(target);
					}
				};
				final Label confirmConnectionLabel = new Label("result-confirmConnectionLabel");
				confirmConnectionLink.add(confirmConnectionLabel);
				confirmConnectionLink.setOutputMarkupId(true);
				item.add(confirmConnectionLink);

				
				//IGNORE CONNECTION LINK + LABEL
				final AjaxLink ignoreConnectionLink = new AjaxLink("result-ignoreConnectionLink") {
		    		public void onClick(AjaxRequestTarget target) {
		    			ignoreConnectionWindow.show(target);
					}
				};
				final Label ignoreConnectionLabel = new Label("result-ignoreConnectionLabel");
				ignoreConnectionLink.add(ignoreConnectionLabel);
				ignoreConnectionLink.setOutputMarkupId(true);
				item.add(ignoreConnectionLink);	

				
				//REMOVE CONNECTION LINK + LABEL
				final AjaxLink removeConnectionLink = new AjaxLink("result-removeConnectionLink") {
		    		public void onClick(AjaxRequestTarget target) {
		    			removeConnectionWindow.show(target);
					}
				};
				final Label removeConnectionLabel = new Label("result-removeConnectionLabel");
				removeConnectionLink.add(removeConnectionLabel);
				removeConnectionLink.setOutputMarkupId(true);
				item.add(removeConnectionLink);	

				
				
				
				//setup state of this User-SearchResult pair
		    	boolean friend = searchResult.isFriend();
		    	boolean friendRequestToThisPerson = searchResult.isFriendRequestToThisPerson();
				boolean friendRequestFromThisPerson = searchResult.isFriendRequestFromThisPerson();
		    	
				
				//setup link/label and windows
				if(friend) {
					
					//set label to 'you are friends'
					connectionStatusLabel.setModel(new ResourceModel("text.friend.confirmed"));
					//hide add&confirm
					addConnectionLink.setVisible(false);
					confirmConnectionLink.setVisible(false);
					//allow remove
					removeConnectionLabel.setModel(new ResourceModel("link.friend.remove"));
					removeConnectionWindow.setContent(new RemoveFriend(removeConnectionWindow.getContentId(), removeConnectionWindow, basePage, currentUserUuid, userUuid)); 

				
				} else if (friendRequestToThisPerson) {
					
					//set label to 'Friend requested'
					connectionStatusLabel.setModel(new ResourceModel("text.friend.requested"));
					//hide add&confirm
					addConnectionLink.setVisible(false);
					confirmConnectionLink.setVisible(false);
					//allow remove
					removeConnectionLabel.setModel(new ResourceModel("link.friend.request.cancel"));
					removeConnectionWindow.setContent(new RemoveFriend(removeConnectionWindow.getContentId(), removeConnectionWindow, basePage, currentUserUuid, userUuid)); 

				} else if (friendRequestFromThisPerson) {
					
					//set label to pending
					connectionStatusLabel.setModel(new ResourceModel("text.friend.pending"));
					//hide add
					addConnectionLink.setVisible(false);
					//allow confirm and ignore
					confirmConnectionLabel.setModel(new ResourceModel("link.friend.request.confirm"));
					confirmConnectionWindow.setContent(new ConfirmFriend(confirmConnectionWindow.getContentId(), confirmConnectionWindow, basePage, currentUserUuid, userUuid)); 
					ignoreConnectionLabel.setModel(new ResourceModel("link.friend.request.ignore"));
					ignoreConnectionWindow.setContent(new IgnoreFriend(ignoreConnectionWindow.getContentId(), ignoreConnectionWindow, basePage, currentUserUuid, userUuid)); 

					
				}  else {
					//hide label
					connectionStatusLabel.setVisible(false);
					//hide confirm & remove
					confirmConnectionLink.setVisible(false);
					removeConnectionLink.setVisible(false);
					//allow add
					addConnectionLabel.setModel(new ResourceModel("link.friend.add"));
					addConnectionWindow.setContent(new AddFriend(addConnectionWindow.getContentId(), addConnectionWindow, basePage, currentUserUuid, userUuid)); 

				}
				
				
				
				//ADD CONNECTION MODAL WINDOW HANDLER 
				addConnectionWindow.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
					private static final long serialVersionUID = 1L;

					public void onClose(AjaxRequestTarget target){
		            	if(basePage.isFriendRequestedResult()) { 
		            		//update main label
		            		connectionStatusLabel.setModel(new ResourceModel("text.friend.requested"));
		            		//remove add link
		            		addConnectionLink.setVisible(false);
		            		//show remove link
		            		removeConnectionLink.setVisible(true);
		            		
		            		//repaint affected components
		            		target.addComponent(connectionStatusLabel);
		            		target.addComponent(addConnectionLink);
		            		target.addComponent(removeConnectionLink);
		            	}
		            }
		        });
				
				//CONFIRM CONNECTION MODAL WINDOW HANDLER 
				confirmConnectionWindow.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
					private static final long serialVersionUID = 1L;
					
					public void onClose(AjaxRequestTarget target){
		            	if(basePage.isFriendConfirmedResult()){ 
		            		//update main label
		            		connectionStatusLabel.setModel(new ResourceModel("text.friend.confirmed"));
		            		//remove confirm link
		            		confirmConnectionLink.setVisible(false);
		            		//show remove link
		            		removeConnectionLink.setVisible(true);
		            		
		            		//repaint affected components
		            		target.addComponent(connectionStatusLabel);
		            		target.addComponent(confirmConnectionLink);
		            		target.addComponent(removeConnectionLink);
		            	}
		            }
		        });
				
				//IGNORE CONNECTION MODAL WINDOW HANDLER 
				ignoreConnectionWindow.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
					private static final long serialVersionUID = 1L;
					
					public void onClose(AjaxRequestTarget target){
						if(basePage.isFriendRemovedResult()){ 
		            		//remove main label
		            		connectionStatusLabel.setVisible(false);
		            		//remove ignore link
		            		ignoreConnectionLink.setVisible(false);
		            		//show add link
		            		addConnectionLink.setVisible(true);
		            		
		            		//repaint affected components
		            		target.addComponent(connectionStatusLabel);
		            		target.addComponent(ignoreConnectionLink);
		            		target.addComponent(addConnectionLink);
		            	}
		            }
		        });
				
				
				//REMOVE CONNECTION MODAL WINDOW HANDLER 
				removeConnectionWindow.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
					private static final long serialVersionUID = 1L;
					
					public void onClose(AjaxRequestTarget target){
						if(basePage.isFriendRemovedResult()){ 
		            		//remove main label
		            		connectionStatusLabel.setVisible(false);
		            		//remove ignore link
		            		ignoreConnectionLink.setVisible(false);
		            		//show add link
		            		addConnectionLink.setVisible(true);
		            		
		            		//repaint affected components
		            		target.addComponent(connectionStatusLabel);
		            		target.addComponent(ignoreConnectionLink);
		            		target.addComponent(addConnectionLink);
		            	}
		            }
		        });
				
				item.add(addConnectionWindow);
				item.add(confirmConnectionWindow);
				item.add(ignoreConnectionWindow);
				item.add(removeConnectionWindow);
				
	    
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
		
		//sbn submit
		AjaxFallbackButton sbnSubmitButton = new AjaxFallbackButton("sbnSubmit", new ResourceModel("button.search.byname"), sbnForm) {
			protected void onSubmit(AjaxRequestTarget target, Form form) {
				//need to show the busyindicator here TODO

				if(target != null) {
					//get the model
					Search search = (Search) form.getModelObject();
					
					//get search field
					String searchText = search.getSearchName();
					
					if(log.isDebugEnabled()) log.debug("MySearch() search.getSearchName(): " + searchText);
					
					//clear the interest search field in model and repaint to clear value
					search.setSearchInterest("");
					
					//search both UDB and Sakaiperson for matches.
					results = new ArrayList(profile.findUsersByNameOrEmail(searchText, currentUserUuid));
	
					//text
					if(results.isEmpty()) {
						numSearchResults.setModel(new StringResourceModel("text.search.byname.no.results", null, new Object[]{ searchText } ));
						resultsContainer.setVisible(false);
					} else {
						numSearchResults.setModel(new StringResourceModel("text.search.byname.all.results", null, new Object[]{ results.size(), searchText } ));
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
		
		//sbn submit
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
					results = new ArrayList(profile.findUsersByInterest(searchText, currentUserUuid));
					
					//text
					if(results.isEmpty()) {
						numSearchResults.setModel(new StringResourceModel("text.search.byinterest.no.results", null, new Object[]{ searchText } ));
						resultsContainer.setVisible(false);
					} else {
						numSearchResults.setModel(new StringResourceModel("text.search.byinterest.all.results", null, new Object[]{ results.size(), searchText } ));
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




