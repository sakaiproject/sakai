package org.sakaiproject.scorm.client.components;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.extensions.markup.html.form.select.Select;
import org.apache.wicket.extensions.markup.html.form.select.SelectOption;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.scorm.client.ClientPanel;

public class DetailsPanel extends ClientPanel {
	private static final long serialVersionUID = 1L;
	private static final List copyrightTypes = Arrays.asList(new String[] {
		"publicDomainOption",
		"userOwnedOption",
		"fairUseOption",
		"permissionObtainedOption",
		"undeterminedOption",
		"useCopyrightOption"
	});
	
	protected String selectedCopyrightType;
			
	public DetailsPanel(String id, final Form form, IModel model) {
		super(id, model);
		
		FormComponent descriptionInput = new TextArea("description");
		PropertyModel selectionModel = new PropertyModel(this, "selectedCopyrightType");
		
		ChoiceRenderer choiceRenderer = new ChoiceRenderer("displayText", "id");
		
		FormComponent copyrightAlertCheckbox = new CheckBox("copyrightAlert");
		
		final UserCopyrightPanel userCopyrightPanel = new UserCopyrightPanel("includeCopyright", form, model);
		userCopyrightPanel.setVisible(false);
		add(userCopyrightPanel);
		
		
		DetailSelectOption[] options = new DetailSelectOption[] {
			new DetailSelectOption("copyrightType.publicDomainOption", this),
			new DetailSelectOption("copyrightType.userOwnedOption", this),
			new DetailSelectOption("copyrightType.fairUseOption", this),
			new DetailSelectOption("copyrightType.permissionObtainedOption", this),
			new DetailSelectOption("copyrightType.undeterminedStatus", this),
			new DetailSelectOption("copyrightType.useCopyrightOption", this)
		};
		
		final DropDownChoice choice = new DropDownChoice("copyrightType", selectionModel, Arrays.asList(options), choiceRenderer);
		
		choice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            protected void onUpdate(AjaxRequestTarget target) {
            	
            	System.out.println("onUpdate: " + selectedCopyrightType);
            	
            	target.appendJavascript("alert('hello new world');");
            	
            	if (selectedCopyrightType.equals("copyrightType.useCopyrightOption")) {
            		userCopyrightPanel.setVisible(!userCopyrightPanel.isVisible());
	                target.appendJavascript("setMainFrameHeight( window.name )");
            	}

                target.addComponent(form);
            }
        });
		
		
		
		
		
		/*DropDownChoice choice = new DropDownChoice("ddc", copyrightTypes) {
			protected void onSelectionChanged(final Object newSelection) {
				String selectionString = (String)newSelection;
				
				if ("useCopyrightOption".equals(selectionString)) {
					userCopyrightPanel.setVisible(!userCopyrightPanel.isVisible());
	                target.appendJavascript("setMainFrameHeight( window.name )");
	                target.addComponent(userCopyrightPanel);
				}
					
			}
		};*/
		
		
		choice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			private static final long serialVersionUID = 1L;

			protected void onUpdate(AjaxRequestTarget target) {
				if (selectedCopyrightType.equals("useCopyrightOption")) {
	            	userCopyrightPanel.setVisible(!userCopyrightPanel.isVisible());
	                target.appendJavascript("setMainFrameHeight( window.name )");
	                target.addComponent(userCopyrightPanel);
				}
            }
        });
		
		/*AjaxFallbackLink includeCopyrightLink = new AjaxFallbackLink("includeCopyright") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				userCopyrightPanel.setVisible(!userCopyrightPanel.isVisible());
				target.appendJavascript("setMainFrameHeight( window.name )");
				target.addComponent(this);
			}
		};*/
		
		//includeCopyrightLink.add(newResourceLabel("detailsLabel", this));
		
		//includeCopyright.add(newResourceLabel("userCopyrightLabel", this));
		//includeCopyright.add(userCopyrightInput);
		//includeCopyright.setVisible(false);
		
		add(newResourceLabel("descriptionLabel", this));
		add(descriptionInput);
		add(newResourceLabel("copyrightLabel", this));
		add(choice);
		//add(includeCopyright);
		add(newResourceLabel("copyrightAlertLabel", this));
		add(newResourceLabel("copyrightAlertCaption", this));
		add(copyrightAlertCheckbox);
	}

	public String getSelectedCopyrightType() {
		return selectedCopyrightType;
	}

	public void setSelectedCopyrightType(String selectedCopyrightType) {
		this.selectedCopyrightType = selectedCopyrightType;
	}
	
	public class DetailSelectOption implements Serializable {
		private String id;
		private StringResourceModel model;
		
		public DetailSelectOption(String id, Component parent) {
			this.id = id;
			this.model = new StringResourceModel(id, parent, null);
		}
		
		public String getId() {
			return id;
		}
		
		public String getDisplayText() {
			return model.getString();
		}
		
	}
	
	
}


