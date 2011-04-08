package org.sakaiproject.dashboard.tool.pages;


import java.text.DateFormat;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.dashboard.model.DashboardItem;
import org.sakaiproject.dashboard.tool.dataproviders.ItemDataProvider;
import org.sakaiproject.dashboard.tool.pages.windows.AddEditItem;


public class Items extends BasePage{

	private static final Logger log = Logger.getLogger(Items.class); 
	private final String REMOVE_IMG = "images/cross.png";
	private final String EDIT_IMG = "images/pencil.png";
	private final int MAX_ITEMS_PER_PAGE = 6;

	private transient DashboardItem w;
	
	public Items() {
		
		//get current user
		final String currentUserId = externalLogic.getCurrentUserId();
		
		//simple labels
		add(new Label("page-title", new ResourceModel("item_add.page_title")));
		add(new Label("hello-user-name", new StringResourceModel("project.greet_user", null, new Object[] {externalLogic.getUserDisplayName(currentUserId)})));

		//date format
		final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
		
		 //item add/edit modal window
        final ModalWindow addEditWindow = new ModalWindow("item-addedit-window");
		
		//encapsulate the DataView in a WebMarkupContainer in order for it to update
        final WebMarkupContainer container = new WebMarkupContainer("container");
        container.setOutputMarkupId(true);
		
        ItemDataProvider provider = new ItemDataProvider();
        
		//list
		DataView dataView = new DataView("pageable", provider)
        {

            @Override
            protected void populateItem(final Item item)
            {
            	w = (DashboardItem)item.getModelObject();
            	
            	//remove item
		    	Link removeLink = new Link("item-delete") {
					public void onClick() {
						//get item and remove it
						DashboardItem this_w = (DashboardItem)getParent().getDefaultModelObject();
						logic.removeItem(this_w);
						//refresh. this could also be done via AJAX.
						setResponsePage(new Items());
					}
				};
				ContextImage removeIcon = new ContextImage("item-remove-icon",new Model(REMOVE_IMG));
				removeLink.add(removeIcon);
				removeLink.add(new AttributeModifier("title", true,new ResourceModel("item.remove")));
				item.add(removeLink);
				
				//edit item
		    	AjaxLink editLink = new AjaxLink("item-edit") {
		    		public void onClick(AjaxRequestTarget target) {
		    			//get item, set content in modalwindow appropriately, then show the window
		    			DashboardItem this_w = (DashboardItem)getParent().getDefaultModelObject();
				        addEditWindow.setContent(new AddEditItem(addEditWindow.getContentId(), addEditWindow, this_w)); 
						addEditWindow.show(target);
					}
				};
				ContextImage editIcon = new ContextImage("item-edit-icon",new Model(EDIT_IMG));
				editLink.add(editIcon);
				editLink.add(new AttributeModifier("title", true,new ResourceModel("item.edit")));
				item.add(editLink);
				
				//check if user has access to edit/delete
				if(!StringUtils.equals(currentUserId, w.getCreatorId())) {
					removeLink.setVisible(false);
					editLink.setVisible(false);
				}
				
            	DateFormat df = DateFormat.getDateTimeInstance();
				//item information
            	//item.add(new Label("item-title", w.getTitle()));
               	ExternalLink entityLink = new ExternalLink("item-entityLink", w.getAccessUrl(), w.getTitle());
//                PopupSettings popupSettings1 = new PopupSettings();
//                popupSettings1.setTarget("_blank");
//				entityLink.setPopupSettings(popupSettings1 );
				item.add(entityLink);
            	item.add(new Label("item-description", w.getDescription()));
            	item.add(new Label("item-entityId", w.getEntityId()));
            	item.add(new Label("item-entityType", w.getEntityType()));
            	//item.add(new Label("item-accessUrl", w.getAccessUrl()));
            	item.add(new Label("item-dueDate", df.format(w.getDueDate())));
            	item.add(new Label("item-status", w.getStatus()));
            	ExternalLink locationLink = new ExternalLink("item-LocationLink", w.getLocationUrl(), w.getLocationName());
//            	PopupSettings popupSettings2 = new PopupSettings();
//            	popupSettings2.setTarget("_blank");
//            	locationLink.setPopupSettings(popupSettings2);
            	item.add(locationLink);
            	item.add(new Label("item-locationId", w.getLocationId()));
            	//item.add(new Label("item-locationName", w.getLocationName()));
            	//item.add(new Label("item-locationUrl", w.getLocationUrl()));
            	
            	item.add(new Label("item-creatorName", w.getCreatorName()));
            	item.add(new Label("item-createdDate", df.format(w.getCreatedDate())));
            	item.add(new Label("item-creatorId", w.getCreatorId()));
            	item.add(new Label("item-id", Long.toString(w.getId())));
            	String itemType = "unknown";
            	if(w.getItemType() != null) {
            		itemType = w.getItemType().toString();
            	}
            	item.add(new Label("item-itemType", itemType));
            	

            	//row styling
                item.add(new AttributeModifier("class", true, new AbstractReadOnlyModel()
                {
                    @Override
                    public String getObject()
                    {
                        return (item.getIndex() % 2 == 1) ? "even" : "odd";
                    }
                }));
                
                item.setOutputMarkupId(true);
            }
        };
        dataView.setItemsPerPage(MAX_ITEMS_PER_PAGE);
        add(dataView);
        
        //pager
        AjaxPagingNavigator navigator = new AjaxPagingNavigator("navigator", dataView);
        navigator.setOutputMarkupPlaceholderTag(true);
        add(navigator);
        
        if(provider.size() <= MAX_ITEMS_PER_PAGE) {
        	navigator.setVisible(false);
        }
        
        //add list to container so we can update it
        container.add(dataView);
        
        //add whole thing to page
        add(container);
        
		//add item link
    	final AjaxLink addLink = new AjaxLink("item-add") {
			public void onClick(AjaxRequestTarget target) {
				
				//set content for window and show it
		        addEditWindow.setContent(new AddEditItem(addEditWindow.getContentId(), addEditWindow, null)); 
				addEditWindow.show(target);
			}
		};
		addLink.add(new Label("item-add-label", new ResourceModel("item.add")));
		add(addLink);
		
		//modal window callback
		addEditWindow.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
			
			public void onClose(AjaxRequestTarget target){
				//refresh page. could also be done via AJAX
				setResponsePage(new Items());
			}
			
		});
		add(addEditWindow);
		
	}
	
	
}



