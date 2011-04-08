package org.sakaiproject.dashboard.tool.pages.windows;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.extensions.yui.calendar.DatePicker;
import org.apache.wicket.extensions.yui.calendar.DateTimeField;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ComponentPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.dashboard.logic.ExternalLogic;
import org.sakaiproject.dashboard.logic.DashboardLogic;
import org.sakaiproject.dashboard.model.DashboardItem;
import org.sakaiproject.dashboard.tool.Locator;
import org.sakaiproject.dashboard.tool.components.FocusOnLoadBehaviour;

public class AddEditItem extends Panel {

	private static final long serialVersionUID = 1L;
	private boolean addMode = true;
	
	public AddEditItem(String id, final ModalWindow window, DashboardItem item){
        super(id);

        //window setup
		window.setTitle(new ResourceModel("item.add")); 
		window.setInitialHeight(400);
		window.setInitialWidth(500);
		window.setResizable(true);
		
		//edit or new?
		if(item == null) {
			item = new DashboardItem();
		} else {
			addMode = false;
		}
		
		Form form = new Form("form", new Model(item));
		
		//feedback
		final Label feedback = new Label("feedback");
		feedback.setOutputMarkupPlaceholderTag(true);
		form.add(feedback);
		
		//title text
		Label titleText = new Label("item-title-text", new ResourceModel("item_add.title"));
		form.add(titleText);
		
		//title field
		TextField title = new TextField("item-title", new PropertyModel(item, "title"));
		title.add(new FocusOnLoadBehaviour());
		form.add(title);
		
		//itemType text
		Label itemTypeText = new Label("item-itemType-text", new ResourceModel("item_add.itemType"));
		form.add(itemTypeText);
		
		//itemType field
		TextField itemType = new TextField("item-itemType", new PropertyModel(item, "itemType"));
		form.add(itemType);
		
		//description text
		Label descriptionText = new Label("item-description-text", new ResourceModel("item_add.description"));
		form.add(descriptionText);
		
		//description field
		TextField<String> description = new TextField<String>("item-description", new PropertyModel<String>(item, "description"));
		form.add(description);
		
		//entityId text
		Label entityIdText = new Label("item-entityId-text", new ResourceModel("item_add.entityId"));
		form.add(entityIdText);
		
		//entityId field
		TextField<String> entityId = new TextField<String>("item-entityId", new PropertyModel<String>(item, "entityId"));
		form.add(entityId);
		
		//entityType text
		Label entityTypeText = new Label("item-entityType-text", new ResourceModel("item_add.entityType"));
		form.add(entityTypeText);
		
		//entityType field
		TextField<String> entityType = new TextField<String>("item-entityType", new PropertyModel<String>(item, "entityType"));
		form.add(entityType);
		
		//accessUrl text
		Label accessUrlText = new Label("item-accessUrl-text", new ResourceModel("item_add.accessUrl"));
		form.add(accessUrlText);
		
		//accessUrl field
		TextField<String> accessUrl = new TextField<String>("item-accessUrl", new PropertyModel<String>(item, "accessUrl"));
		form.add(accessUrl);
		
		//dueDate text
		Label dueDateText = new Label("item-dueDate-text", new ResourceModel("item_add.dueDate"));
		form.add(dueDateText);
		
		//dueDate field
		DateTimeField dueDate = new DateTimeField("item-dueDate", new PropertyModel<Date>(item, "dueDate"));
		form.add(dueDate);
		
		//status text
		Label statusText = new Label("item-status-text", new ResourceModel("item_add.status"));
		form.add(statusText);
		
		//status field
		TextField<String> status = new TextField<String>("item-status", new PropertyModel<String>(item, "status"));
		form.add(status);
		
		//locationId text
		Label locationIdText = new Label("item-locationId-text", new ResourceModel("item_add.locationId"));
		form.add(locationIdText);
		
		//locationId field
		TextField locationId = new TextField("item-locationId", new PropertyModel(item, "locationId"));
		form.add(locationId);
		
		//locationName text
		Label locationNameText = new Label("item-locationName-text", new ResourceModel("item_add.locationName"));
		form.add(locationNameText);
		
		//locationName field
		TextField locationName = new TextField("item-locationName", new PropertyModel(item, "locationName"));
		form.add(locationName);
		
		//locationUrl text
		Label locationUrlText = new Label("item-locationUrl-text", new ResourceModel("item_add.locationUrl"));
		form.add(locationUrlText);
		
		//locationUrl field
		TextField locationUrl = new TextField("item-locationUrl", new PropertyModel(item, "locationUrl"));
		form.add(locationUrl);
				
		//submit button
		AjaxFallbackButton submit = new AjaxFallbackButton("submit", new ResourceModel("button.save"), form) {
			private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form form) {
				
				//get form model
				DashboardItem item = (DashboardItem)form.getModelObject();
				
				//validate title
				if(StringUtils.isBlank(item.getTitle())) {
					feedback.setDefaultModel(new ResourceModel("title_required"));
					feedback.add(new AttributeModifier("class", true, new Model("alertMessage")));
					target.addComponent(feedback);
					return;
				}
				
				//set other parts of the item
				if(addMode) {
					item.setLocationId(getExternalLogic().getCurrentLocationId());
					item.setCreatorId(getExternalLogic().getCurrentUserId());
					item.setCreatorName(getExternalLogic().getCurrentUserId());
				}
				
				//save it and close window
				getDashboardLogic().saveItem(item);
				window.close(target);
				
            }
		};
		form.add(submit);
		
		//for editing, update the labels with the appropriate values
		if(!addMode) {
			window.setTitle(new ResourceModel("item.edit")); 
			submit.setLabel(new ResourceModel("button.edit"));
		}
		
        
        //add form
        add(form);
    }

	protected DashboardLogic getDashboardLogic()
    {
    	return Locator.getDashboardLogic();
    }
	
	protected ExternalLogic getExternalLogic()
    {
    	return Locator.getExternalLogic();
    }
}



