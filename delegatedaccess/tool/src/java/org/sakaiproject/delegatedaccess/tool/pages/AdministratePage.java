package org.sakaiproject.delegatedaccess.tool.pages;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;

public class AdministratePage extends BasePage{
	private SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy hh:mm aaa");
	
	public AdministratePage(){
		disableLink(administrateLink);
		
		//Form Feedback (Saved/Error)
		final Label formFeedback = new Label("formFeedback");
		formFeedback.setOutputMarkupPlaceholderTag(true);
		final String formFeedbackId = formFeedback.getMarkupId();
		add(formFeedback);
		
		//Add Delegated Access to My Workspaces:
		final Label addDaMyworkspaceStatusLabel = new Label("lastRanInfo", new AbstractReadOnlyModel<String>() {
			@Override
			public String getObject() {
				String lastRanInfoStr = projectLogic.getAddDAMyworkspaceJobStatus();
				if(lastRanInfoStr == null){
					lastRanInfoStr = new ResourceModel("addDaMyworkspace.job.status.none").getObject();
				}else{
					try{
						long lastRanInfoInt = Long.parseLong(lastRanInfoStr);
						if(lastRanInfoInt == -1){
							return new ResourceModel("addDaMyworkspace.job.status.failed").getObject();
						}else if(lastRanInfoInt == 0){
							return new ResourceModel("addDaMyworkspace.job.status.scheduled").getObject();
						}else{
							Date successDate = new Date(lastRanInfoInt);
							return new ResourceModel("addDaMyworkspace.job.status.success").getObject() + " " + format.format(successDate);
						}
					}catch (Exception e) {
						return new ResourceModel("na").getObject();
					}
				}
				return lastRanInfoStr;
			}
		});
		addDaMyworkspaceStatusLabel.setOutputMarkupPlaceholderTag(true);
		final String addDaMyworkspaceStatusLabelId = addDaMyworkspaceStatusLabel.getMarkupId();
		
		add(addDaMyworkspaceStatusLabel);
		
		Form<?> addDaMyworkspaceForm = new Form("addDaMyworkspaceForm");
		
		AjaxButton addDaMyworkspaceButton = new AjaxButton("addDaMyworkspace", new StringResourceModel("addDaMyworkspaceTitle", null)){
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> arg1) {
				projectLogic.scheduleAddDAMyworkspaceJobStatus();
				
				//display a "saved" message
				formFeedback.setDefaultModel(new ResourceModel("success.addDaMyworkspace"));
				formFeedback.add(new AttributeModifier("class", true, new Model("success")));
				target.addComponent(formFeedback);
				
				target.appendJavascript("hideFeedbackTimer('" + formFeedbackId + "');");
				
				target.addComponent(addDaMyworkspaceStatusLabel,addDaMyworkspaceStatusLabelId);
			}
		};
		addDaMyworkspaceForm.add(addDaMyworkspaceButton);
		
		add(addDaMyworkspaceForm);
	}
}
