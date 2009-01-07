package uk.ac.lancs.e_science.profile2.tool.pages.panels;



import org.apache.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.api.common.edu.person.SakaiPerson;

import uk.ac.lancs.e_science.profile2.api.SakaiProxy;
import uk.ac.lancs.e_science.profile2.tool.ProfileApplication;
import uk.ac.lancs.e_science.profile2.tool.models.UserProfile;

public class MyInterestsEdit extends Panel {
	
	private transient Logger log = Logger.getLogger(MyInterestsEdit.class);
	
	public MyInterestsEdit(final String id, final UserProfile userProfile) {
		super(id);
		
		//this panel stuff
		final Component thisPanel = this;
				
		//create model
		CompoundPropertyModel userProfileModel = new CompoundPropertyModel(userProfile);
		
		//heading
		add(new Label("heading", new ResourceModel("heading.interests.edit")));
		
		//setup form		
		Form form = new Form("form", userProfileModel);
		form.setOutputMarkupId(true);
		
		//We don't need to get the info from userProfile, we load it into the form with a property model
	    //just make sure that the form element id's match those in the model
	   		
		//favourite books
		WebMarkupContainer booksContainer = new WebMarkupContainer("booksContainer");
		booksContainer.add(new Label("booksLabel", new ResourceModel("profile.favourite.books")));
		TextArea favouriteBooks = new TextArea("favouriteBooks", new PropertyModel(userProfile, "favouriteBooks"));
		booksContainer.add(favouriteBooks);
		form.add(booksContainer);
		
		//favourite tv shows
		WebMarkupContainer tvContainer = new WebMarkupContainer("tvContainer");
		tvContainer.add(new Label("tvLabel", new ResourceModel("profile.favourite.tv")));
		TextArea favouriteTvShows = new TextArea("favouriteTvShows", new PropertyModel(userProfile, "favouriteTvShows"));
		tvContainer.add(favouriteTvShows);
		form.add(tvContainer);
		
		//favourite movies
		WebMarkupContainer moviesContainer = new WebMarkupContainer("moviesContainer");
		moviesContainer.add(new Label("moviesLabel", new ResourceModel("profile.favourite.movies")));
		TextArea favouriteMovies = new TextArea("favouriteMovies", new PropertyModel(userProfile, "favouriteMovies"));
		moviesContainer.add(favouriteMovies);
		form.add(moviesContainer);
		
		//favourite quotes
		WebMarkupContainer quotesContainer = new WebMarkupContainer("quotesContainer");
		quotesContainer.add(new Label("quotesLabel", new ResourceModel("profile.favourite.quotes")));
		TextArea favouriteQuotes = new TextArea("favouriteQuotes", new PropertyModel(userProfile, "favouriteQuotes"));
		quotesContainer.add(favouriteQuotes);
		form.add(quotesContainer);
		
		//other information
		WebMarkupContainer otherContainer = new WebMarkupContainer("otherContainer");
		otherContainer.add(new Label("otherLabel", new ResourceModel("profile.other")));
		TextArea otherInformation = new TextArea("otherInformation", new PropertyModel(userProfile, "otherInformation"));
		otherContainer.add(otherInformation);
		form.add(otherContainer);
		
		//submit button
		AjaxButton submitButton = new AjaxButton("submit") {
			protected void onSubmit(AjaxRequestTarget target, Form form) {
				//save() form, show message, then load display panel
				if(save(form)) {
					Component newPanel = new MyInterestsDisplay(id, userProfile);
					newPanel.setOutputMarkupId(true);
					thisPanel.replaceWith(newPanel);
					if(target != null) {
						target.addComponent(newPanel);
						//resize iframe
						target.appendJavascript("setMainFrameHeight(window.name);");
					}
				
				} else {
					String js = "alert('crap!');";
					target.prependJavascript(js);
				}
            }
		};
		form.add(submitButton);
		
        
		//cancel button
		AjaxFallbackButton cancelButton = new AjaxFallbackButton("cancel", new ResourceModel("button.cancel"), form) {
            protected void onSubmit(AjaxRequestTarget target, Form form) {
            	//System.out.println("cancel clicked");
            	Component newPanel = new MyInterestsDisplay(id, userProfile);
				newPanel.setOutputMarkupId(true);
				thisPanel.replaceWith(newPanel);
				if(target != null) {
					target.addComponent(newPanel);
					//resize iframe
					target.appendJavascript("setMainFrameHeight(window.name);");
					//need a scrollTo action here, to scroll down the page to the section
				}
            	
            }
        };
        cancelButton.setDefaultFormProcessing(false);
        form.add(cancelButton);
		
        //feedback stuff - make this a class and insance it with diff params
        //WebMarkupContainer formFeedback = new WebMarkupContainer("formFeedback");
		//formFeedback.add(new Label("feedbackMsg", "some message"));
		//formFeedback.add(new AjaxIndicator("feedbackImg"));
		//form.add(formFeedback);
        
        
		
		//add form to page
		add(form);
		
	}
	
	//called when the form is to be saved
	private boolean save(Form form) {
		
       // System.out.println(getModelObject());

		//get the backing model
		UserProfile userProfile = (UserProfile) form.getModelObject();
		
		//get sakaiProxy, then get userId from sakaiProxy, then get sakaiperson for that userId
		SakaiProxy sakaiProxy = ProfileApplication.get().getSakaiProxy();
		
		String userId = sakaiProxy.getCurrentUserId();
		SakaiPerson sakaiPerson = sakaiProxy.getSakaiPerson(userId);

		//get values and set into SakaiPerson
		sakaiPerson.setFavouriteBooks(userProfile.getFavouriteBooks());
		sakaiPerson.setFavouriteTvShows(userProfile.getFavouriteTvShows());
		sakaiPerson.setFavouriteMovies(userProfile.getFavouriteMovies());
		sakaiPerson.setFavouriteQuotes(userProfile.getFavouriteQuotes());
		sakaiPerson.setNotes(userProfile.getOtherInformation());

		//update SakaiPerson
		if(sakaiProxy.updateSakaiPerson(sakaiPerson)) {
			log.info("Saved SakaiPerson for: " + userId );
			return true;
		} else {
			log.info("Couldn't save SakaiPerson for: " + userId);
			return false;
		}
	}

	
}
