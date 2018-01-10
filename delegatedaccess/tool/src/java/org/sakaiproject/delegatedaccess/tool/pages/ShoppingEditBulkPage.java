package org.sakaiproject.delegatedaccess.tool.pages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;

import org.sakaiproject.delegatedaccess.model.ListOptionSerialized;
import org.sakaiproject.delegatedaccess.model.NodeModel;
import org.sakaiproject.delegatedaccess.model.SelectOption;
import org.sakaiproject.delegatedaccess.util.DelegatedAccessConstants;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.util.DateFormatterUtil;

public class ShoppingEditBulkPage extends BasePage{

	private SelectOption role = null;
	private List<DecoratedSiteModel> deleteSites = new ArrayList<DecoratedSiteModel>();
	private List<DecoratedSiteModel> addSites = new ArrayList<DecoratedSiteModel>();
	private String deleteSitesInput = "", addSitesInput = "";
	private AjaxFallbackDefaultDataTable deleteTable, addTable;
	private TextArea<String> deleteSitesInputField, addSitesInputField;
	private Date startDate, endDate;
	private boolean singleRoleOptions = false;
	private List<ListOptionSerialized> selectedAnonTools = new ArrayList<ListOptionSerialized>();
	private List<ListOptionSerialized> selectedAuthTools = new ArrayList<ListOptionSerialized>();
	private Boolean revokeInstructorOverride = Boolean.FALSE;
	private Boolean revokePublicOpt = Boolean.FALSE;

	private static String HIDDEN_SHOPPINGVISIBILITYSTART_ISO8601 = "shoppingVisibilityStartISO8601";
	private static String HIDDEN_SHOPPINGVISIBILITYEND_ISO8601 = "shoppingVisibilityEndISO8601";

	public ShoppingEditBulkPage(){
		disableLink(shoppingAdminLink);
		//Form Feedback (Saved/Error)
		final Label formFeedback = new Label("formFeedback");
		formFeedback.setOutputMarkupPlaceholderTag(true);
		final String formFeedbackId = formFeedback.getMarkupId();
		add(formFeedback);
		//Form Feedback2 (Saved/Error)
		final Label formFeedback2 = new Label("formFeedback2");
		formFeedback2.setOutputMarkupPlaceholderTag(true);
		final String formFeedback2Id = formFeedback2.getMarkupId();
		add(formFeedback2);

		//FORM:
		Form form = new Form("form");
		add(form);
		
		//Add Delete Site:
		deleteSitesInputField = new TextArea<String>("deleteSitesInput", new PropertyModel<String>(this, "deleteSitesInput"));
		deleteSitesInputField.setOutputMarkupId(true);
		form.add(deleteSitesInputField);
		form.add(new AjaxButton("addDeleteSites", form){

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> arg1) {
				IModel errorMessage = Model.of("");
				List<DecoratedSiteModel> deleteSitesList = getValidSitesFromInput(deleteSites, errorMessage, deleteSitesInput);
				deleteSites.addAll(deleteSitesList);
				if(deleteSitesList.size() > 0 || errorMessage == null){
					//need to update list:
					target.add(deleteTable);
					deleteSitesInput = "";
					target.add(deleteSitesInputField);
				}
				
				if(errorMessage != null && !"".equals(errorMessage.getObject().toString())){
					formFeedback.setDefaultModel(errorMessage);
					formFeedback.add(new AttributeModifier("class", true, new Model("alertMessage")));
					target.add(formFeedback);
					formFeedback2.setDefaultModel(errorMessage);
					formFeedback2.add(new AttributeModifier("class", true, new Model("alertMessage")));
					target.add(formFeedback2);

					//call a js function to hide the message in 5 seconds
					target.appendJavaScript("hideFeedbackTimer('" + formFeedbackId + "');");
					target.appendJavaScript("hideFeedbackTimer('" + formFeedback2Id + "');");
				}
			}
		});
		
		//Delete Sites Data View:
		List<IColumn> deleteSitesColumns = new ArrayList<IColumn>();
		//Site Id:
		deleteSitesColumns.add(new PropertyColumn(new ResourceModel("siteId"),"siteId", "siteId"){
			public void populateItem(Item item, String componentId, IModel rowModel) {
				DecoratedSiteModel site = (DecoratedSiteModel) rowModel.getObject();
				item.add(new Label(componentId, site.getSiteId()));
			}
		});
		//Site Id:
		deleteSitesColumns.add(new PropertyColumn(new ResourceModel("siteTitle"),"siteTitle", "siteTitle"){
			public void populateItem(Item item, String componentId, IModel rowModel) {
				DecoratedSiteModel site = (DecoratedSiteModel) rowModel.getObject();
				item.add(new Label(componentId, site.getSiteTitle()));
			}
		});
		//Remove Link Id:
		deleteSitesColumns.add(new AbstractColumn(null){

			@Override
			public void populateItem(Item item, String componentId, final IModel rowModel) {
				DecoratedSiteModel site = (DecoratedSiteModel) rowModel.getObject();
				item.add(new LinkPanel(componentId, new ResourceModel("remove")){
					@Override
					public void clicked(AjaxRequestTarget target) {
						DecoratedSiteModel site = (DecoratedSiteModel) rowModel.getObject();
						for (Iterator iterator = deleteSites.iterator(); iterator
								.hasNext();) {
							DecoratedSiteModel decoratedSiteModel = (DecoratedSiteModel) iterator.next();
							if(site.getSiteId().equals(decoratedSiteModel.getSiteId())){
								iterator.remove();
								break;
							}
						}
						target.add(deleteTable);
					}
					
				});
			}
			
		});
		//Delete Data table:
		deleteTable = new AjaxFallbackDefaultDataTable("deleteSites", deleteSitesColumns, (ISortableDataProvider) new DeleteSitesDataProvider(), 20){

		};
		deleteTable.setOutputMarkupId(true);
		form.add(deleteTable);
		
		
		
		//Add/Update Site List:
		addSitesInputField = new TextArea<String>("addSitesInput", new PropertyModel<String>(this, "addSitesInput"));
		addSitesInputField.setOutputMarkupId(true);
		form.add(addSitesInputField);
		form.add(new AjaxButton("addAddSites", form){

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> arg1) {
				IModel errorMessage = Model.of("");
				List<DecoratedSiteModel> addSitesList = getValidSitesFromInput(addSites, errorMessage, addSitesInput);
				addSites.addAll(addSitesList);
				if(addSitesList.size() > 0 || errorMessage == null){
					//need to update list:
					target.add(addTable);
					addSitesInput = "";
					target.add(addSitesInputField);
				}

				if(errorMessage != null && !"".equals(errorMessage.getObject().toString())){
					formFeedback.setDefaultModel(errorMessage);
					formFeedback.add(new AttributeModifier("class", true, new Model("alertMessage")));
					target.add(formFeedback);
					formFeedback2.setDefaultModel(errorMessage);
					formFeedback2.add(new AttributeModifier("class", true, new Model("alertMessage")));
					target.add(formFeedback2);

					//call a js function to hide the message in 5 seconds
					target.appendJavaScript("hideFeedbackTimer('" + formFeedbackId + "');");
					target.appendJavaScript("hideFeedbackTimer('" + formFeedback2Id + "');");
				}
			}
		});

		//add Sites Data View:
		List<IColumn> addSitesColumns = new ArrayList<IColumn>();
		//Site Id:
		addSitesColumns.add(new PropertyColumn(new ResourceModel("siteId"),"siteId", "siteId"){
			public void populateItem(Item item, String componentId, IModel rowModel) {
				DecoratedSiteModel site = (DecoratedSiteModel) rowModel.getObject();
				item.add(new Label(componentId, site.getSiteId()));
			}
		});
		//Site Id:
		addSitesColumns.add(new PropertyColumn(new ResourceModel("siteTitle"),"siteTitle", "siteTitle"){
			public void populateItem(Item item, String componentId, IModel rowModel) {
				DecoratedSiteModel site = (DecoratedSiteModel) rowModel.getObject();
				item.add(new Label(componentId, site.getSiteTitle()));
			}
		});
		//Remove Link Id:
		addSitesColumns.add(new AbstractColumn(null){

			@Override
			public void populateItem(Item item, String componentId, final IModel rowModel) {
				DecoratedSiteModel site = (DecoratedSiteModel) rowModel.getObject();
				item.add(new LinkPanel(componentId, new ResourceModel("remove")){
					@Override
					public void clicked(AjaxRequestTarget target) {
						DecoratedSiteModel site = (DecoratedSiteModel) rowModel.getObject();
						for (Iterator iterator = addSites.iterator(); iterator
								.hasNext();) {
							DecoratedSiteModel decoratedSiteModel = (DecoratedSiteModel) iterator.next();
							if(site.getSiteId().equals(decoratedSiteModel.getSiteId())){
								iterator.remove();
								break;
							}
						}
						target.add(addTable);
					}

				});
			}

		});
		//add Data table:
		addTable = new AjaxFallbackDefaultDataTable("addSites", addSitesColumns, (ISortableDataProvider) new AddSitesDataProvider(), 20){

		};
		addTable.setOutputMarkupId(true);
		form.add(addTable);
		
		//Start Date:
		form.add(new TextField<String>("shoppingVisibilityStart", Model.of("")));
		
		//End Date:
		form.add(new TextField<String>("shoppingVisibilityEnd", Model.of("")));
		//Roles:
		//create a map of the realms and their roles for the Role column
		final Map<String, String> roleMap = projectLogic.getRealmRoleDisplay(true);
		String largestRole = "";
		for(String role : roleMap.values()){
			if(role.length() > largestRole.length()){
				largestRole = role;
			}
		}
		if(roleMap.size() == 1){
			String[] split = null;
			for(String key : roleMap.keySet()){
				split = key.split(":");
			}
			if(split != null && split.length == 2){
				//only one option for role, so don't bother showing it in the table
				singleRoleOptions = true;
			}
		}
		SelectOption[] options = new SelectOption[roleMap.size()];
		int i = 0;
		//now sort the map
		List<String> sortList = new ArrayList<String>(roleMap.values());
		Collections.sort(sortList, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return o1.compareToIgnoreCase(o2);
			}
		});
		Map<String, String> sortedReturnMap = new HashMap<String, String>();
		for(String value : sortList){
			for(Entry<String, String> entry : roleMap.entrySet()){
				if(value.equals(entry.getValue())){
					options[i] = new SelectOption(entry.getValue(), entry.getKey());
					if(singleRoleOptions){
						role = options[i];
					}
					i++;
					break;
				}
			}
		}
		ChoiceRenderer choiceRenderer = new ChoiceRenderer("label", "value");
		form.add(new DropDownChoice("shoppingRole", new PropertyModel(this, "role"), Arrays.asList(options), choiceRenderer){
			@Override
			public boolean isVisible() {
				return !singleRoleOptions;
			}
		});
		
		//public tools:
		ChoiceRenderer toolChoiceRenderer = new ChoiceRenderer("name", "id");
		List<ListOptionSerialized> toolOptions = projectLogic.getEntireToolsList();
		form.add(new ListMultipleChoice<ListOptionSerialized>("showPublicTools", new PropertyModel(this, "selectedAnonTools"), toolOptions, toolChoiceRenderer){
			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				tag.append("title", new Model("selectTools").getObject().toString(), "");
			}
		});
		//private tools:
		form.add(new ListMultipleChoice<ListOptionSerialized>("showAuthTools", new PropertyModel(this, "selectedAuthTools"), toolOptions, toolChoiceRenderer){
			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				tag.append("title", new Model("selectTools").getObject().toString(), "");
			}
		});
		
		//Advanced Options
		//Revoke Instructor Override:
		form.add(new CheckBox("revokeInstructorOverrideCheckbox", new PropertyModel<Boolean>(this, "revokeInstructorOverride")));
		form.add(new CheckBox("revokePublicOptCheckbox", new PropertyModel<Boolean>(this, "revokePublicOpt")));
		
		//updateButton button:
		AjaxButton updateButton = new AjaxButton("update", form) {
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form arg1) {
				IModel errorMessage = null;
				setISODates();
				//first check that all the settings are set:
				if(deleteSites.size() == 0 && addSites.size() == 0){
					//at least one site must be added to the delete or add list
					errorMessage = new ResourceModel("noSitesToaddOrDelete");
				}else if(addSites.size() > 0){
					//check status of the add parameters:
					if(startDate == null && endDate == null){
						errorMessage = new ResourceModel("oneDateRequired");	
					}else if(startDate != null && endDate != null && (endDate.before(startDate) || startDate.equals(endDate))){
						errorMessage = new ResourceModel("startDateMustBeFirst");
					}else if(selectedAnonTools.size() == 0 && selectedAuthTools.size() == 0){
						errorMessage = new ResourceModel("oneToolMustBeSelected");
					}else if(role == null || role.getValue().split(":").length != 2){
						errorMessage = new ResourceModel("roleRequired");
					}
				}
				
				

				if(errorMessage == null){
					//start by deleting the sites first:				
					for(DecoratedSiteModel siteModel : deleteSites){
						NodeModel nodeModel = projectLogic.getNodeModel(siteModel.getNodeId(), DelegatedAccessConstants.SHOPPING_PERIOD_USER);
						//simply set direct = false to delete this node:
						nodeModel.setDirectAccess(false);
						projectLogic.updateNodePermissionsForUser(nodeModel, DelegatedAccessConstants.SHOPPING_PERIOD_USER);
					}
					//Now update/add new sites:
					for(DecoratedSiteModel siteModel : addSites){
						NodeModel nodeModel = projectLogic.getNodeModel(siteModel.getNodeId(), DelegatedAccessConstants.SHOPPING_PERIOD_USER);
						//make sure direct access is selected
						nodeModel.setDirectAccess(true);
						nodeModel.setShoppingPeriodStartDate(startDate);
						nodeModel.setShoppingPeriodEndDate(endDate);
						String[] realmRole = role.getValue().split(":");
						nodeModel.setRealm(realmRole[0]);
						nodeModel.setRole(realmRole[1]);
						//filter out any duplicate selected tools in "auth" list that is set in "anon" list:
						for (Iterator iterator = selectedAuthTools.iterator(); iterator.hasNext();) {
							ListOptionSerialized authListOptionSerialized = (ListOptionSerialized) iterator.next();
							for(ListOptionSerialized anonListOptionSerialized : selectedAnonTools){
								if(authListOptionSerialized.getId().equals(anonListOptionSerialized.getId())){
									iterator.remove();
									break;
								}
							}
						}
						//now set all tools to false:
						for(ListOptionSerialized tool : nodeModel.getRestrictedAuthTools()){
							nodeModel.setAuthToolRestricted(tool.getId(), false);
						}
						for(ListOptionSerialized tool : nodeModel.getRestrictedPublicTools()){
							nodeModel.setPublicToolRestricted(tool.getId(), false);
						}
						//now set selected tools:
						for(ListOptionSerialized anonListOptionSerialized : selectedAnonTools){
							nodeModel.setPublicToolRestricted(anonListOptionSerialized.getId(), true);
						}
						for(ListOptionSerialized authListOptionSerialized : selectedAuthTools){
							nodeModel.setAuthToolRestricted(authListOptionSerialized.getId(), true);
						}
						//now update advanced options:
						nodeModel.setShoppingPeriodRevokeInstructorEditable(revokeInstructorOverride);
						nodeModel.setShoppingPeriodRevokeInstructorPublicOpt(revokePublicOpt);
						//save node
						projectLogic.updateNodePermissionsForUser(nodeModel, DelegatedAccessConstants.SHOPPING_PERIOD_USER);
					}
					
					setResponsePage(new ShoppingEditPage());
				}else{
					formFeedback.setDefaultModel(errorMessage);
					formFeedback.add(new AttributeModifier("class", true, new Model("alertMessage")));
					target.add(formFeedback);
					formFeedback2.setDefaultModel(errorMessage);
					formFeedback2.add(new AttributeModifier("class", true, new Model("alertMessage")));
					target.add(formFeedback2);

					//call a js function to hide the message in 5 seconds
					target.appendJavaScript("hideFeedbackTimer('" + formFeedbackId + "');");
					target.appendJavaScript("hideFeedbackTimer('" + formFeedback2Id + "');");
				}
			}
		};
		form.add(updateButton);
		
		//cancelButton button:
		Button cancelButton = new Button("cancel") {
			@Override
			public void onSubmit() {
				setResponsePage(new ShoppingEditPage());
			}
		};
		form.add(cancelButton);
	}


	private class DeleteSitesDataProvider extends SortableDataProvider{

		public DeleteSitesDataProvider() {
			setSort("siteId", SortOrder.DESCENDING);
		}
		
		@Override
		public Iterator<? extends DecoratedSiteModel> iterator(long first, long count) {
			Collections.sort(deleteSites, new Comparator<DecoratedSiteModel>(){

				@Override
				public int compare(DecoratedSiteModel o1, DecoratedSiteModel o2) {
					int dir = getSort().isAscending() ? 1 : -1;
					if("siteId".equals(getSort().getProperty())){
						return dir * o1.getSiteId().compareTo(o2.getSiteId());
					}else if("siteTitle".equals(getSort().getProperty())){
						return dir * o1.getSiteTitle().compareTo(o2.getSiteTitle());
					}
					return 0;
				}
				
			});
			return deleteSites.subList(Math.min((int) first, (int) (deleteSites.size() - 1)), (int) Math.min(first + count, deleteSites.size())).iterator();
		}

		@Override
		public IModel model(Object arg0) {
			return new DeleteSitesDetachableModel((DecoratedSiteModel) arg0);
		}

		@Override
		public long size() {
			return deleteSites.size();
		}

	}
	
	private class DeleteSitesDetachableModel extends LoadableDetachableModel{

		private DecoratedSiteModel site;
		
		public DeleteSitesDetachableModel(DecoratedSiteModel site){
			this.site = site;
		}
		@Override
		protected Object load() {
			return site;
		}
		
	}
	
	private class AddSitesDataProvider extends SortableDataProvider{

		public AddSitesDataProvider() {
			setSort("siteId", SortOrder.DESCENDING);
		}
		
		@Override
		public Iterator<? extends DecoratedSiteModel> iterator(long first, long count) {
			Collections.sort(addSites, new Comparator<DecoratedSiteModel>(){

				@Override
				public int compare(DecoratedSiteModel o1, DecoratedSiteModel o2) {
					int dir = getSort().isAscending() ? 1 : -1;
					if("siteId".equals(getSort().getProperty())){
						return dir * o1.getSiteId().compareTo(o2.getSiteId());
					}else if("siteTitle".equals(getSort().getProperty())){
						return dir * o1.getSiteTitle().compareTo(o2.getSiteTitle());
					}
					return 0;
				}
				
			});
			return addSites.subList(Math.min((int) first, (int) (addSites.size() - 1)), (int) Math.min(first + count, addSites.size())).iterator();
		}

		@Override
		public IModel model(Object arg0) {
			return new AddSitesDetachableModel((DecoratedSiteModel) arg0);
		}

		@Override
		public long size() {
			return addSites.size();
		}

	}
	
	private class AddSitesDetachableModel extends LoadableDetachableModel{

		private DecoratedSiteModel site;
		
		public AddSitesDetachableModel(DecoratedSiteModel site){
			this.site = site;
		}
		@Override
		protected Object load() {
			return site;
		}
		
	}
	
	private class DecoratedSiteModel implements Serializable{
		private String siteId = "", siteTitle = "", nodeId = "";
		public DecoratedSiteModel(String siteId, String siteTitle, String nodeId){
			this.setSiteId(siteId);
			this.setSiteTitle(siteTitle);
			this.setNodeId(nodeId);
		}
		public String getSiteId() {
			return siteId;
		}
		public void setSiteId(String siteId) {
			this.siteId = siteId;
		}
		public String getSiteTitle() {
			return siteTitle;
		}
		public void setSiteTitle(String siteTitle) {
			this.siteTitle = siteTitle;
		}
		public String getNodeId() {
			return nodeId;
		}
		public void setNodeId(String nodeId) {
			this.nodeId = nodeId;
		}
	}
	
	private abstract class LinkPanel extends Panel {
		public LinkPanel(String id, final IModel labelModel) {
			super(id);
			AjaxLink link = new AjaxLink("link") {
				@Override
				public void onClick(AjaxRequestTarget target) {
					clicked(target);
				}
			};
			link.add(new Label("linkLabel", labelModel));
			add(link);
		}
		
		public abstract void clicked(AjaxRequestTarget target);
	}
	

	private List<DecoratedSiteModel> getValidSitesFromInput(List<DecoratedSiteModel> existingSitesFilter, IModel errorMessage, String input){
		List<DecoratedSiteModel> returnList = new ArrayList<DecoratedSiteModel>();
		if(input != null && !"".equals(input)){
			String[] split = input.split("\n");
			boolean anyAdded = false;
			//first remove any site that is already in the existing list and created a siteRef list to look up nodes with:
			List<String> lookupSiteIds = new ArrayList<String>(Arrays.asList(split));
			List<String> lookupSiteRefs = new ArrayList<String>();
			for (Iterator iterator = lookupSiteIds.iterator(); iterator.hasNext();) {
				String siteId = ((String) iterator.next()).trim();
				boolean found = false;
				for(DecoratedSiteModel siteModel : existingSitesFilter){
					if(siteId.equals(siteModel.getSiteId())){
						found = true;
						break;
					}
				}
				if(found){
					iterator.remove();
				}else{
					lookupSiteRefs.add("/site/" + siteId);
				}
			}
			//filter out any sites that do not have nodes
			Map<String, List<String>> nodes = projectLogic.getNodesBySiteRef(lookupSiteRefs.toArray(new String[lookupSiteRefs.size()]), DelegatedAccessConstants.HIERARCHY_ID);
			Set<String> shoppingEditableNodes = new HashSet<String>();
			//get list of node ids to check whether the user can modify the settings
			for(Entry<String, List<String>> entry : nodes.entrySet()){
				shoppingEditableNodes.addAll(entry.getValue());
			}
			if(!sakaiProxy.isSuperUser()){
				//Admin users can always edit any site, so only filter for non admins
				shoppingEditableNodes = projectLogic.filterShoppingPeriodEditNodes(shoppingEditableNodes);
			}
			String notFound = "";
			String noAccess = "";
			for(String siteId : lookupSiteIds){
				siteId = siteId.trim();
				//check that this site id doesn't already exist:
				boolean exist = nodes.containsKey("/site/" + siteId) && nodes.get("/site/" + siteId) != null && nodes.get("/site/" + siteId).size() > 0;
				if(exist){
					boolean hasAccess = shoppingEditableNodes.contains(nodes.get("/site/" + siteId).get(0));
					if(hasAccess){
						Site site = sakaiProxy.getSiteById(siteId);
						if(site != null){
							returnList.add(new DecoratedSiteModel(site.getId(), site.getTitle(), nodes.get("/site/" + siteId).get(0)));
							anyAdded = true;
						}else{
							if(!"".equals(notFound)){
								notFound += ", ";
							}
							notFound += siteId;
						}
					}else{
						if(!"".equals(noAccess)){
							noAccess += ", ";
						}
						noAccess += siteId;
					}
				}else{
					if(!"".equals(notFound)){
						notFound += ", ";
					}
					notFound += siteId;
				}
			}
			String errorMessageStr = "";
			if(!"".equals(notFound)){
				errorMessageStr += new StringResourceModel("sitesNotFound", null, new String[]{notFound}).getObject();
			}
			if(!"".equals(noAccess)){
				if(!"".equals(errorMessageStr)){
					errorMessageStr += " ";
				}
				errorMessageStr += new StringResourceModel("sitesNoAccess", null, new String[]{noAccess}).getObject();
			}
			if(!"".equals(errorMessageStr)){
				errorMessage.setObject(errorMessageStr);
			}
		}else{
			errorMessage.setObject(new ResourceModel("noSitesInInput").getObject());
		}
		return returnList;
	}

	private void setISODates(){
		String shoppingVisibilityStart = getRequest().getRequestParameters().getParameterValue(HIDDEN_SHOPPINGVISIBILITYSTART_ISO8601).toString("");
		String shoppingVisibilityEnd = getRequest().getRequestParameters().getParameterValue(HIDDEN_SHOPPINGVISIBILITYEND_ISO8601).toString("");
		if(DateFormatterUtil.isValidISODate(shoppingVisibilityStart)){
			startDate = DateFormatterUtil.parseISODate(shoppingVisibilityStart);
		}

		if(DateFormatterUtil.isValidISODate(shoppingVisibilityEnd)){
			endDate = DateFormatterUtil.parseISODate(shoppingVisibilityEnd);
		}
	}
}

