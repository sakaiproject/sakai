package uk.ac.lancs.e_science.profile2.tool.pages;


import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.resource.BufferedDynamicImageResource;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.api.common.edu.person.SakaiPerson;

import uk.ac.lancs.e_science.profile2.api.ProfileImageManager;
import uk.ac.lancs.e_science.profile2.hbm.ProfileImage;
import uk.ac.lancs.e_science.profile2.hbm.ProfilePrivacy;
import uk.ac.lancs.e_science.profile2.tool.components.AjaxIndicator;
import uk.ac.lancs.e_science.profile2.tool.components.ErrorLevelsFeedbackMessageFilter;
import uk.ac.lancs.e_science.profile2.tool.components.FeedbackLabel;
import uk.ac.lancs.e_science.profile2.tool.components.IconWithClueTip;
import uk.ac.lancs.e_science.profile2.tool.models.Search;


public class MySearch extends BasePage {

	private transient Logger log = Logger.getLogger(MySearch.class);
	private transient Search search;
	private List<String> results = new ArrayList<String>();
	
	public MySearch() {
		
		if(log.isDebugEnabled()) log.debug("MyPrivacy()");
		
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
		
		//search results
		ListView resultsListView = new ListView("results-list", resultsModel) {
		    protected void populateItem(ListItem item) {
		        
		    	//get userUuid string, 
		    	//then get a SakaiPerson for each, 
		    	//their Privacy record, 
		    	//and if authorised, their ProfileImage record
		    	//also figure out if they are a friend already.
		    	
		    	//get userUuid
		    	String userUuid = (String)item.getModelObject();
		    	
		    	//setup basic values
		    	String displayName = sakaiProxy.getUserDisplayName(userUuid);
		    	final byte[] photo;
		    		
		    	//get objects for this userUuid
				SakaiPerson sakaiPerson = sakaiProxy.getSakaiPerson(userUuid);

				//is this user a friend of the current user?
				boolean friend = profile.isUserFriendOfCurrentUser(userUuid, currentUserUuid);
				
		    	//should they be skipped from this search result?
		    	if(!profile.isUserVisibleInSearchesByCurrentUser(userUuid, currentUserUuid, friend)) {
		    		return;
		    	}
		    	
		    	//check privacy on this user's profile/image
		    	//if its disabled, their profile will not be linked and their image will be the default one
		    	boolean profileAllowed = profile.isUserProfileVisibleByCurrentUser(userUuid, currentUserUuid, friend);
		    	
		    	
		    	if(profileAllowed) {
		    		photo = profile.getCurrentProfileImageForUser(userUuid, ProfileImageManager.PROFILE_IMAGE_THUMBNAIL);
		    	} else {
		    		photo = null;
		    	}
		    	
		    	
		    	//name
		    	Label nameLabel = new Label("result-name", displayName);
		    	item.add(nameLabel);
		    	
		    	//photo
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
		
		    	
		    	//action - confirm friend
		    	/*
		    	AjaxLink confirmLink = new AjaxLink("friendRequest-confirmLink") {
		    		public void onClick(AjaxRequestTarget target) {
		    			
	    			}
	    		};
	    		confirmLink.add(new Label("friendRequest-confirm",new ResourceModel("link.friend.request.confirm")));
	    		item.add(confirmLink);
	    		*/
	    		
	    		
		    }
		};
		resultsContainer.add(resultsListView);
		
		//add friend container
		add(resultsContainer);
		
		//hide if no results
		if(results.size() == 0) {
			resultsContainer.setVisible(false);
		}
		
		
		
		
		
		
		
		
		
			
		 
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
					results = new ArrayList(profile.findUsersByNameOrEmail(searchText));
	
					if(log.isDebugEnabled()) log.debug("MySearch() results: " + results.toString());
					
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
					target.addComponent(numSearchResults);
					target.addComponent(numSearchResults);
					target.addComponent(resultsContainer);
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
					results = new ArrayList(profile.findUsersByInterest(searchText));

					if(log.isDebugEnabled()) log.debug("MySearch() results: " + results.toString());
					
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




