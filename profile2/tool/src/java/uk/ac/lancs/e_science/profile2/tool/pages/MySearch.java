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

import uk.ac.lancs.e_science.profile2.api.ProfileImageManager;
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
		String userId = sakaiProxy.getCurrentUserId();
		
		// FeedbackPanel - so we activate feedback
        final FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        add(feedback);
     
        //create model for form
		search = new Search();
		CompoundPropertyModel searchModel = new CompoundPropertyModel(search);
        
		/* 
		 * 
		 * SEARCH BY NAME
		 * 
		 */
		
		
        //heading	
		Label heading = new Label("sbnHeading", new ResourceModel("heading.search.byname"));
		add(heading);
		
		
		//setup form		
		Form sbnForm = new Form("sbnForm", searchModel);
		sbnForm.setOutputMarkupId(true);
		
		// filteredErrorLevels will not be shown in the FeedbackPanel
        int[] filteredErrorLevels = new int[]{FeedbackMessage.ERROR};
        feedback.setFilter(new ErrorLevelsFeedbackMessageFilter(filteredErrorLevels));
		
		//search field
        sbnForm.add(new Label("sbnNameLabel", new ResourceModel("text.search.byname")));
		TextField sbnNameField = new TextField("searchName");
		sbnNameField.setRequired(true);
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
		
		//search results label
		final Label numSearchResults = new Label("numSearchResults");
		numSearchResults.setOutputMarkupId(true);
		numSearchResults.setEscapeModelStrings(false);
		add(numSearchResults);
		
		// model to wrap search results
		LoadableDetachableModel resultsModel = new LoadableDetachableModel(){
			protected Object load() {
				System.out.println("loading results 1...");
				return results;
			}
			
		};
		
		
		//search results
		/*
		final SearchResults searchResults = new SearchResults("searchResults", resultsModel);
		searchResults.setOutputMarkupId(true);
		add(searchResults);
		*/
		
		
		//container which wraps list
		final WebMarkupContainer resultsContainer = new WebMarkupContainer("searchResultsContainer");
		resultsContainer.setOutputMarkupId(true);
		
		ListView resultsListView = new ListView("results-list", resultsModel) {
		    protected void populateItem(ListItem item) {
		        
		    	//get userUuid string, then get a SakaiPerson for each, their Privacy record, and if authorised, their photo etc
		    	//also figure out if they are a friend already.
		    	
		    	//get userUuid
		    	String userUuid = (String)item.getModelObject();
		    	
		    	System.out.println("item: " + userUuid);
		    	
		    	
		    	
		    	//setup basic values
		    	String displayName = sakaiProxy.getUserDisplayName(userUuid);
		    			    			    	
		    	//name
		    	Label nameLabel = new Label("result-name", displayName);
		    	item.add(nameLabel);
		    	
		    	final byte[] photo = null;
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
		//if(results.size() == 0) {
			//resultsContainer.setVisible(false);
		//}
		
		
		
		
		
		
		
		
		
				
		//submit
		AjaxFallbackButton sbnSubmitButton = new AjaxFallbackButton("sbnSubmit", new ResourceModel("button.search.byname"), sbnForm) {
			protected void onSubmit(AjaxRequestTarget target, Form form) {
				//need to show the busyindicator here TODO

				
				//get the model
				Search search = (Search) form.getModelObject();
				
				//get search field
				String searchText = search.getSearchName();
				
				//search both UDB and Sakaiperson for matches.
				results = new ArrayList(profile.findUsersByNameOrEmail(searchText));

				//text
				if(results.isEmpty()) {
					numSearchResults.setModel(new StringResourceModel("text.search.no.results", null, new Object[]{ searchText } ));
				} else {
					numSearchResults.setModel(new StringResourceModel("text.search.all.results", null, new Object[]{ results.size(), searchText } ));
				}
				
				
				
				//update components
				if(target != null) {
					target.addComponent(numSearchResults);
					target.addComponent(resultsContainer);
					target.appendJavascript("setMainFrameHeight(window.name);");	
				}
				
				
				
				
            }
		};
		sbnForm.add(sbnSubmitButton);

		
		
		
	
		
        
        
        add(sbnForm);
   	
	}

	
	
}




