package uk.ac.lancs.e_science.profile2.tool.pages.panels;

import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;

import uk.ac.lancs.e_science.profile2.tool.models.UserProfile;

public class MyInterestsDisplay extends Panel {
	
	private transient Logger log = Logger.getLogger(MyInterestsDisplay.class);
	
	private int visibleFieldCount = 0;

	
	public MyInterestsDisplay(final String id, final UserProfile userProfile) {
		super(id);
		
		//this panel stuff
		final Component thisPanel = this;
		
		//get userProfile from userProfileModel
		//UserProfile userProfile = (UserProfile) this.getModelObject();
		
		//get info from userProfile since we need to validate it and turn things off if not set.
		//otherwise we could just use a propertymodel
		String favouriteBooks = userProfile.getFavouriteBooks();
		String favouriteTvShows = userProfile.getFavouriteTvShows();
		String favouriteMovies = userProfile.getFavouriteMovies();
		String favouriteQuotes = userProfile.getFavouriteQuotes();
		
		//heading
		add(new Label("heading", new ResourceModel("heading.interests")));
		
		//favourite books
		WebMarkupContainer booksContainer = new WebMarkupContainer("booksContainer");
		booksContainer.add(new Label("booksLabel", new ResourceModel("profile.favourite.books")));
		booksContainer.add(new Label("favouriteBooks", favouriteBooks));
		add(booksContainer);
		if("".equals(favouriteBooks) || favouriteBooks == null) {
			booksContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}
		
		//favourite tv shows
		WebMarkupContainer tvContainer = new WebMarkupContainer("tvContainer");
		tvContainer.add(new Label("tvLabel", new ResourceModel("profile.favourite.tv")));
		tvContainer.add(new Label("favouriteTvShows", favouriteTvShows));
		add(tvContainer);
		if("".equals(favouriteTvShows) || favouriteTvShows == null) {
			tvContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}
		
		//favourite movies
		WebMarkupContainer moviesContainer = new WebMarkupContainer("moviesContainer");
		moviesContainer.add(new Label("moviesLabel", new ResourceModel("profile.favourite.movies")));
		moviesContainer.add(new Label("favouriteMovies", favouriteMovies));
		add(moviesContainer);
		if("".equals(favouriteMovies) || favouriteMovies == null) {
			moviesContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}
		
		//favourite quotes
		WebMarkupContainer quotesContainer = new WebMarkupContainer("quotesContainer");
		quotesContainer.add(new Label("quotesLabel", new ResourceModel("profile.favourite.quotes")));
		quotesContainer.add(new Label("favouriteQuotes", favouriteQuotes));
		add(quotesContainer);
		if("".equals(favouriteQuotes) || favouriteQuotes == null) {
			quotesContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}
		
		
				
		//edit button
		AjaxFallbackLink editButton = new AjaxFallbackLink("editButton", new ResourceModel("button.edit")) {
			public void onClick(AjaxRequestTarget target) {
				Component newPanel = new MyInterestsEdit(id, userProfile);
				newPanel.setOutputMarkupId(true);
				thisPanel.replaceWith(newPanel);
				if(target != null) {
					target.addComponent(newPanel);
					//resize iframe
					target.appendJavascript("setMainFrameHeight(window.name);");
				}
				
			}
						
		};
		editButton.setOutputMarkupId(true);
		add(editButton);
		
		//no fields message
		Label noFieldsMessage = new Label("noFieldsMessage", new ResourceModel("text.no.fields"));
		add(noFieldsMessage);
		if(visibleFieldCount > 0) {
			noFieldsMessage.setVisible(false);
		}
		
	}
	
}
