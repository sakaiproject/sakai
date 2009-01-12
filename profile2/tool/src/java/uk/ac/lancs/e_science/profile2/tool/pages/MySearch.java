package uk.ac.lancs.e_science.profile2.tool.pages;


import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.validation.validator.StringValidator;

import uk.ac.lancs.e_science.profile2.tool.components.AjaxIndicator;
import uk.ac.lancs.e_science.profile2.tool.components.ComponentVisualErrorBehavior;
import uk.ac.lancs.e_science.profile2.tool.components.ErrorLevelsFeedbackMessageFilter;
import uk.ac.lancs.e_science.profile2.tool.components.FeedbackLabel;
import uk.ac.lancs.e_science.profile2.tool.components.IconWithClueTip;
import uk.ac.lancs.e_science.profile2.tool.models.Search;


public class MySearch extends BasePage {

	private transient Logger log = Logger.getLogger(MySearch.class);
	private transient Search search;
	
	public MySearch() {
		
		if(log.isDebugEnabled()) log.debug("MyPrivacy()");
		

		//get current user
		String userId = sakaiProxy.getCurrentUserId();
		
		// FeedbackPanel - so we activate feedback
        final FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        add(feedback);
     
        //create model
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
       // sbnNameField.add(new ComponentVisualErrorBehavior("onblur", sbnNameFeedback)); //removed for now
        sbnForm.add(sbnNameFeedback);
		
		//form indicator - need to use IAjaxIndicatorAware TODO
		final AjaxIndicator sbnIndicator = new AjaxIndicator("sbnIndicator");
		sbnIndicator.setOutputMarkupId(true);
		sbnIndicator.setVisible(false);
		sbnForm.add(sbnIndicator);
		
		//submit button
		AjaxFallbackButton sbnSubmitButton = new AjaxFallbackButton("sbnSubmit", new ResourceModel("button.search.byname"), sbnForm) {
			protected void onSubmit(AjaxRequestTarget target, Form form) {
				//need to show the busyindicator here TODO

				//get the model
				Search search = (Search) form.getModelObject();
				
				//get text
				String searchText = search.getSearchName();
				
				
				
				
				
				
				System.out.println(searchText);
				
				
				
				
				
            }
		};
		sbnForm.add(sbnSubmitButton);

		
		

		
	
		
        
        
        add(sbnForm);
   	
	}

	
	
}




