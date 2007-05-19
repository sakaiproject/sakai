package org.sakaiproject.scorm.helper.components;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.extensions.markup.html.form.select.Select;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
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
	
	private String selectedCopyrightType;
	
	/*<option value="Material is in public domain.">Material is in public domain.</option>
	<option value="I hold copyright.">I hold copyright.</option>
	<option value="Material is subject to fair use exception.">Material is subject to fair use exception.</option>
	<option value="I have obtained permission to use this material.">I have obtained permission to use this material.</option>

	<option value="Copyright status is not yet determined.">Copyright status is not yet determined.</option>
	<option value="Use copyright below.">Use copyright below.</option>*/
	
	private Form form;
	
	public DetailsPanel(String id, Form form, IModel model) {
		super(id, model);
		this.form = form;
		
		FormComponent descriptionInput = new TextArea("description");
		Select copyrightInput = new Select("copyright");
		DropDownChoice choice = new DropDownChoice("ddc", new PropertyModel(this, "selectedCopyrightType"), copyrightTypes);
		FormComponent copyrightAlertCheckbox = new CheckBox("copyrightAlert");
		
		/*MarkupContainer includeCopyright = new MarkupContainer("includeCopyright") {
			private static final long serialVersionUID = 1L;

			public String getMarkupType() {
				return "div";
			}
		};*/
		
		final UserCopyrightPanel userCopyrightPanel = new UserCopyrightPanel("includeCopyright", form, model);
		userCopyrightPanel.setVisible(false);
		add(userCopyrightPanel);
		
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
		add(copyrightInput);
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

}
