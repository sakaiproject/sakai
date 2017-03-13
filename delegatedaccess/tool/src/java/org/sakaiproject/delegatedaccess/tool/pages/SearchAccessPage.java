/*
* The Trustees of Columbia University in the City of New York
* licenses this file to you under the Educational Community License,
* Version 2.0 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of the
* License at:
*
* http://opensource.org/licenses/ecl2.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.delegatedaccess.tool.pages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.repeater.DefaultItemReuseStrategy;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.delegatedaccess.model.AccessSearchResult;
import org.sakaiproject.delegatedaccess.model.HierarchyNodeSerialized;
import org.sakaiproject.delegatedaccess.model.ListOptionSerialized;
import org.sakaiproject.delegatedaccess.model.SelectOption;
import org.sakaiproject.delegatedaccess.util.AccessSearchResultComparator;
import org.sakaiproject.delegatedaccess.util.DelegatedAccessConstants;
import org.sakaiproject.user.api.User;

public class SearchAccessPage extends BasePage implements Serializable {
	
	public String selectedSearchType = "hierarchy";
	public static final String searchTypeHierarchy = "hierarchy";
	public static final String searchTypeEid = "eid";
	public String eid = "";
	public Map<String, List<SelectOption>> hierarchySelectOptions;
	public Map<String, SelectOption> nodeSelects;
	public List<String> nodeSelectOrder;
	private int orderBy = DelegatedAccessConstants.SEARCH_COMPARE_DEFAULT;
	private boolean orderAsc = true;
	private AccessSearchResultDataProvider provider;
	private DataView<AccessSearchResult> dataView;
	private int rowIndex = 0; 
	private boolean firstLoad = true;
	private boolean includeLowerPerms = true;
	
	public SearchAccessPage(){
		main();
	}
	
	public SearchAccessPage(boolean searchHierarchy, String userEid){
		selectedSearchType = searchHierarchy ? searchTypeHierarchy : searchTypeEid;
		this.eid = userEid;
		firstLoad = false;
		main();
	}
	
	private void main(){
		disableLink(searchAccessLink);

		final AbstractReadOnlyModel resultModel = new AbstractReadOnlyModel<String>(){

			@Override
			public String getObject() {
				if(firstLoad){
					return "";
				}else{
					if(provider.getData().size() == 0){
						if(searchTypeEid.equals(selectedSearchType)){
							if(eid == null || "".equals(eid.trim())){
								return new StringResourceModel("noEidEntered", null).getObject();
							}else{
								User u = getUser();
								if(u == null){
									return new StringResourceModel("eidDoesNotExist", null).getObject();
								}else{
									return new StringResourceModel("noResultsUser", null).getObject();
								}
							}
						}else{
							return new StringResourceModel("noResultsHierarchy", null).getObject();
						}
					}else{
						return "";
					}
				}
			}
			
		};
		add(new Label("resultInfo", resultModel){
			@Override
			public boolean isVisible() {
				return !"".equals(resultModel.getObject());
			}
		});

		
		//create form
		final Form<?> form = new Form("form"){
			protected void onSubmit() {
				super.onSubmit();
				firstLoad = false;
				if(provider != null){
					provider.detachManually();
				}
			}
		};
		form.setOutputMarkupId(true);
		
		//search by label:
		form.add(new Label("searchByLabel", new StringResourceModel("searchByLabel", null)));
		
		//setup radio buttons for search type:
		final RadioGroup group = new RadioGroup("searchGroup", new PropertyModel<String>(this, "selectedSearchType"));
		
		final Radio hierarchyRadio = new Radio("searchByHierarchy",  new Model<String>(searchTypeHierarchy));
		FormComponentLabel hierarchyLabel = new FormComponentLabel("searchByHierarchyLabel", hierarchyRadio);
		hierarchyLabel.add(new Label("searchByHierarchyLabelText", new StringResourceModel("searchByHierarchyLabel", null)));
		group.add(hierarchyRadio);
		group.add(hierarchyLabel);
		group.add(hierarchyRadio.add(new AjaxEventBehavior("onchange") {
			@Override
			protected void onEvent(AjaxRequestTarget arg0) {
				selectedSearchType = searchTypeHierarchy;
			}
		}));
		
		Radio eidRadio = new Radio("searchByEid",  new Model<String>(searchTypeEid));
		FormComponentLabel eidRadioLabel = new FormComponentLabel("searchByEidLabel", eidRadio);
		eidRadioLabel.add(new Label("searchByEidLabelText", new StringResourceModel("searchByEidLabel", null)));
		group.add(eidRadio);
		group.add(eidRadioLabel);
		group.add(eidRadio.add(new AjaxEventBehavior("onchange") {
			@Override
			protected void onEvent(AjaxRequestTarget arg0) {
				selectedSearchType = searchTypeEid;
			}
		}));

		form.add(group);
		
	
		//input for hierarchy fields:
		WebMarkupContainer hierarchyDiv = new WebMarkupContainer("hierarchyFields"){
			@Override
			protected void onComponentTag(ComponentTag tag) {
				if(!selectedSearchType.equals(searchTypeHierarchy)){
					//set to hidden
					tag.put("style", "display:none");
				}
			}
		};
		final Comparator<SelectOption> optionComparator = new SelectOptionComparator();
		if(hierarchySelectOptions == null || hierarchySelectOptions.size() == 0){
			hierarchySelectOptions = new HashMap<String, List<SelectOption>>();
			HierarchyNodeSerialized rootNode = projectLogic.getRootNodeId();
			nodeSelects = new HashMap<String, SelectOption>();
			nodeSelectOrder = new ArrayList<String>();
			if(rootNode != null && rootNode.id != null && !"".equals(rootNode.id)){
				Set<HierarchyNodeSerialized> nodes = projectLogic.getDirectNodes(rootNode.id);
				List<SelectOption> options = new ArrayList<SelectOption>();
				if(nodes != null){
					for(HierarchyNodeSerialized node : nodes){
						options.add(new SelectOption(node.description, node.id));
					}
				}
				Collections.sort(options, optionComparator);
				hierarchySelectOptions.put(rootNode.id, options);
				
				//since nothing is selected, set the node selection to null
				nodeSelects.put(rootNode.id, null);
				//add the root node as the first selection				
				nodeSelectOrder.add(rootNode.id);
			}
		}
		final ChoiceRenderer choiceRenderer = new ChoiceRenderer("label", "value");
		DataView dropdowns = new DataView("hierarchyDropdowns", new IDataProvider<String>(){

			@Override
			public void detach() {
				
			}

			@Override
			public Iterator<? extends String> iterator(long first, long count) {
				//should really check bounds here 
				int f = (int) first;
				int c = (int) count;
				return nodeSelectOrder.subList(f, f + c).iterator();
			}

			@Override
			public IModel<String> model(final String arg0) {
				return new AbstractReadOnlyModel<String>() {
					private static final long serialVersionUID = 1L;

					@Override
					public String getObject() {
						return arg0;
					}
				};
			}

			@Override
			public long size() {
				return nodeSelectOrder.size();
			}
			
		}) {

			@Override
			protected void populateItem(Item item) {
				final String itemNodeId = item.getModelObject().toString();
				final DropDownChoice choice = new DropDownChoice("hierarchyLevel", new NodeSelectModel(itemNodeId), hierarchySelectOptions.get(itemNodeId), choiceRenderer);
				//keeps the null option (choose one) after a user selects an option
				choice.setNullValid(true);
				choice.add(new AjaxFormComponentUpdatingBehavior("onchange"){
					protected void onUpdate(AjaxRequestTarget target) {
						List<String> newOrder = new ArrayList<String>();
						for(String nodeId : nodeSelectOrder){
							newOrder.add(nodeId);
							if(nodeId.equals(itemNodeId)){
								break;
							}
						}
						if(choice.getModelObject() != null && !"".equals(((SelectOption) choice.getModelObject()).getValue())){
							String value = ((SelectOption) choice.getModelObject()).getValue();
							//check if options list exist for newly selected node
							if(!hierarchySelectOptions.containsKey(value)){
								Set<HierarchyNodeSerialized> nodes = projectLogic.getDirectNodes(value);
								List<SelectOption> options = new ArrayList<SelectOption>();
								if(nodes != null){
									for(HierarchyNodeSerialized node : nodes){
										options.add(new SelectOption(node.description, node.id));
									}
									Collections.sort(options, optionComparator);
								}
								hierarchySelectOptions.put(value, options);
							}
							//check to see if there are any additional direct children, or if
							//this is the last node
							if(hierarchySelectOptions.containsKey(value)
									&& hierarchySelectOptions.get(value).size() > 0){
								//update node select order
								newOrder.add(value);
							}
						}
						nodeSelectOrder = newOrder;
						//refresh everything:
						target.add(form);
					}
				});
				item.add(choice);
			}

			
		};
		hierarchyDiv.add(dropdowns);
		
		//include lower perms checkbox:
		CheckBox checkbox = new CheckBox("includeLowerPerms", new PropertyModel(this, "includeLowerPerms"));
		FormComponentLabel checkboxLabel = new FormComponentLabel("includeLowerPermsLabel", checkbox);
		checkboxLabel.add(new Label("includeLowerPermsLabelText", new StringResourceModel("includeLowerPermsLabel", null)));
		hierarchyDiv.add(checkboxLabel);
		hierarchyDiv.add(checkbox);
		
		form.add(hierarchyDiv);
		
		
		//input for eid fields:
		WebMarkupContainer eidDiv = new WebMarkupContainer("eidFields"){
			@Override
			protected void onComponentTag(ComponentTag tag) {
				if(!selectedSearchType.equals(searchTypeEid)){
					//set to hidden
					tag.put("style", "display:none");
				}
			}
		};
		final PropertyModel<String> eidModel = new PropertyModel<String>(this, "eid");
		TextField<String> eidText = new TextField<String>("eid", eidModel);
		eidDiv.add(eidText);
		
		form.add(eidDiv);
		
		add(form);
		
		
		
		//Display Results:
		
		//Headers:
		Link<Void> userIdSort = new Link<Void>("userIdSortLink"){
			private static final long serialVersionUID = 1L;
			public void onClick() {
				changeOrder(DelegatedAccessConstants.SEARCH_COMPARE_EID);
			}

			@Override
			public boolean isVisible() {
				return provider.size() > 0 && searchTypeHierarchy.equals(selectedSearchType);
			}
		};
		add(userIdSort);
		Link<Void> nameSort = new Link<Void>("nameSortLink"){
			private static final long serialVersionUID = 1L;
			public void onClick() {
				changeOrder(DelegatedAccessConstants.SEARCH_COMPARE_SORT_NAME);
			}

			@Override
			public boolean isVisible() {
				return provider.size() > 0 && searchTypeHierarchy.equals(selectedSearchType);
			}
		};
		add(nameSort);
		Link<Void> typeSort = new Link<Void>("typeSortLink"){
			private static final long serialVersionUID = 1L;
			public void onClick() {
				changeOrder(DelegatedAccessConstants.SEARCH_COMPARE_TYPE);
			}

			@Override
			public boolean isVisible() {
				return provider.size() > 0;
			}
		};
		add(typeSort);
		Link<Void> levelSort = new Link<Void>("levelSortLink"){
			private static final long serialVersionUID = 1L;
			public void onClick() {
				changeOrder(DelegatedAccessConstants.SEARCH_COMPARE_LEVEL);
			}

			@Override
			public boolean isVisible() {
				return provider.size() > 0;
			}
		};
		add(levelSort);
		Link<Void> accessSort = new Link<Void>("accessSortLink"){
			private static final long serialVersionUID = 1L;
			public void onClick() {
				changeOrder(DelegatedAccessConstants.SEARCH_COMPARE_ACCESS);
			}
		};
		add(accessSort);
		Label restrictedToolsHeader = new Label("restrictedToolsHeader", new StringResourceModel("restrictedToolsHeader", null)){
			@Override
			public boolean isVisible() {
				return provider.size() > 0;
			}
		};
		add(restrictedToolsHeader);
		Label hierarchyHeader = new Label("hierarchyHeader", new StringResourceModel("hierarchyHeader", null)){
			@Override
			public boolean isVisible() {
				return provider.size() > 0;
			}
		};
		add(hierarchyHeader);
	
		//Display user (if available)
		final AbstractReadOnlyModel userModel = new AbstractReadOnlyModel(){
			@Override
			public Object getObject() {
				if(searchTypeEid.equals(selectedSearchType) && eid != null && !"".equals(eid.trim())){
					User u = getUser();
					if(u != null){
						return u.getDisplayName();
					}
				}
				return "";
			}
		};
		Label userName = new Label("userName", userModel){
			@Override
			public boolean isVisible() {
				return searchTypeEid.equals(selectedSearchType) && eid != null && !"".equals(eid) && !"".equals(userModel.getObject());
			}
		};
		add(userName);
		
		add(new Link("editUserLink"){
			private static final long serialVersionUID = 1L;
			public void onClick() {
				User u = getUser();
				if(u != null){
					setResponsePage(new UserEditPage(u.getId(), u.getDisplayName()));
				}
			}
		});
		
		Link removeAllPermsLink = new Link("removeAllPerms"){
			private static final long serialVersionUID = 1L;
			public void onClick() {
				User u = getUser();
				if(u != null){
					projectLogic.removeAllPermsForUser(u.getId());
					provider.detachManually();
				}
			}
			@Override
			public boolean isVisible() {
				return sakaiProxy.isSuperUser();
			}
		};
		String confirm = new StringResourceModel("confirmRemoveAll", null).getObject();
		removeAllPermsLink.add( new AttributeModifier("onclick", "return confirm('" + confirm + "');"));
		add(removeAllPermsLink);
		
		//tool id=>title map:
		final Map<String, String> toolTitleMap = new HashMap<String, String>();
		final List<ListOptionSerialized> blankRestrictedTools = projectLogic.getEntireToolsList();
		for(ListOptionSerialized opt : blankRestrictedTools){
			toolTitleMap.put(opt.getId(), opt.getName());
		}
		
		//Data
		String[] tmpHierarchy = sakaiProxy.getServerConfigurationStrings(DelegatedAccessConstants.HIERARCHY_SITE_PROPERTIES);
		if(tmpHierarchy == null || tmpHierarchy.length == 0){
			tmpHierarchy = DelegatedAccessConstants.DEFAULT_HIERARCHY;
		}
		final String[] hierarchy = new String[tmpHierarchy.length + 1];
		//include the root as part of the hierarchy:
		hierarchy[0] = sakaiProxy.getRootName();
		for(int i = 1; i < hierarchy.length; i++){
			hierarchy[i] = tmpHierarchy[i -1];
		}
		provider = new AccessSearchResultDataProvider();
		dataView = new DataView<AccessSearchResult>("searchResult", provider) {
			@Override
			public void populateItem(final Item item) {
				final AccessSearchResult searchResult = (AccessSearchResult) item.getModelObject();
				item.add(new Label("userId", searchResult.getEid()){
					@Override
					public boolean isVisible() {
						return searchTypeHierarchy.equals(selectedSearchType);
					}
				});
				item.add(new Label("name", searchResult.getSortName()));
				item.add(new Label("type", new StringResourceModel("accessType" + searchResult.getType(), null)));
				String level = "";
				if(hierarchy != null && searchResult.getLevel() < hierarchy.length){
					level = hierarchy[searchResult.getLevel()];
				}else{
					level = new StringResourceModel("site", null).getObject();
				}
				item.add(new Label("level", level));
				AbstractReadOnlyModel<String> accessModel = new AbstractReadOnlyModel<String>(){
					@Override
					public String getObject() {
						String returnVal = "";
						if(searchResult.getAccess() != null && searchResult.getAccess().length == 2){
							returnVal = searchResult.getAccess()[0] + ":" + searchResult.getAccess()[1];
							if(":".equals(returnVal)){
								returnVal = "";
							}
						}
						return returnVal;
					}
				};
				item.add(new Label("access", accessModel));
				item.add(new ListView<String>("restrictedTools", searchResult.getRestrictedTools()){

					@Override
					protected void populateItem(ListItem<String> arg0) {
						String toolTitle = arg0.getDefaultModelObject().toString();
						if(toolTitleMap.containsKey(toolTitle)){
							toolTitle = toolTitleMap.get(toolTitle);
						}
						arg0.add(new Label("restrictedTool", toolTitle));
					}
				});
				item.add(new ListView<String>("hierarchy", searchResult.getHierarchyNodes()) {

					@Override
					protected void populateItem(ListItem<String> arg0) {
						String hierarchyStr = "|-";
						for(String hierarchyLevel : searchResult.getHierarchyNodes()){
							if(hierarchyLevel.equals(arg0.getDefaultModelObject())){
								break;
							}
							hierarchyStr += "-";
						}
						hierarchyStr += arg0.getDefaultModelObject();
						arg0.add(new Label("hierarchyTitle", hierarchyStr));
					}
					
				});
				Link<Void> viewLink = new Link("view"){
					private static final long serialVersionUID = 1L;
					public void onClick() {
						setResponsePage(new SearchAccessPage(false, searchResult.getEid()));
					}
					
					@Override
					public boolean isVisible() {
						return searchTypeHierarchy.equals(selectedSearchType);
					}
				};
				item.add(viewLink);
				Link<Void> userIdLink = new Link("edit"){
					private static final long serialVersionUID = 1L;
					public void onClick() {
						setResponsePage(new UserEditPage(searchResult.getId(), searchResult.getDisplayName()));
					}
					
					@Override
					public boolean isVisible() {
						return searchTypeHierarchy.equals(selectedSearchType);
					}
				};
				item.add(userIdLink);
				Link<Void> removeLink = new Link("remove"){
					@Override
					public void onClick() {
						projectLogic.removeAccess(searchResult.getNodeId(), searchResult.getId(), searchResult.getType());
						provider.detachManually();
					}
					
					@Override
					public boolean isVisible() {
						return searchResult.isCanEdit();
					}
				};
				String confirm = new StringResourceModel("confirmRemove", null).getObject();
				removeLink.add( new AttributeModifier("onclick", "return confirm('" + confirm + "');"));
				item.add(removeLink);
				
				
				//add css class
				if(rowIndex == 100){
					rowIndex = 0;
				}
				item.add(new AttributeAppender("class", true, new Model<String>(rowIndex % 2 == 0 ? "even" : "odd"), ";"));
				rowIndex++;
			}
			@Override
			public boolean isVisible() {
				return provider.size() > 0;
			}
			
			@Override
			protected void onComponentTag(ComponentTag arg0) {
				
			}
			
		};
		dataView.setOutputMarkupId(true);
		dataView.setItemReuseStrategy(new DefaultItemReuseStrategy());
		dataView.setItemsPerPage(DelegatedAccessConstants.SEARCH_RESULTS_PAGE_SIZE);
		add(dataView);
		//Navigation
		//add a pager to our table, only visible if we have more than SEARCH_RESULTS_PAGE_SIZE items
		add(new PagingNavigator("navigatorTop", dataView) {

			@Override
			public boolean isVisible() {
				if(provider.size() > DelegatedAccessConstants.SEARCH_RESULTS_PAGE_SIZE) {
					return true;
				}
				return false;
			}

			@Override
			public void onBeforeRender() {
				super.onBeforeRender();

				//clear the feedback panel messages
				clearFeedback(feedbackPanel);
			}
		});
		add(new PagingNavigator("navigatorBottom", dataView) {

			@Override
			public boolean isVisible() {
				if(provider.size() > DelegatedAccessConstants.SEARCH_RESULTS_PAGE_SIZE) {
					return true;
				}
				return false;
			}

			@Override
			public void onBeforeRender() {
				super.onBeforeRender();

				//clear the feedback panel messages
				clearFeedback(feedbackPanel);
			}
		});
		
	}
	
	
	
	private class NodeSelectModel implements IModel<SelectOption>, Serializable{

		private String nodeId;
		
		public NodeSelectModel(String nodeId){
			this.nodeId = nodeId;
		}

		@Override
		public void detach() {
		
		}

		@Override
		public SelectOption getObject() {
			return nodeSelects.get(nodeId);
		}

		@Override
		public void setObject(SelectOption arg0) {
			nodeSelects.put(nodeId, arg0);
		}
	}
	
	private class SelectOptionComparator implements Comparator<SelectOption>, Serializable{
		
		@Override
		public int compare(SelectOption o1, SelectOption o2) {
			return o1.getLabel().compareTo(o2.getLabel());
		}
	}
	
	/**
	 * changes order by desc or asc
	 * 
	 * @param sortByColumn
	 */
	private void changeOrder(int sortByColumn){
		if(sortByColumn == orderBy){
			orderAsc = !orderAsc;
		}else{
			orderBy = sortByColumn;
		}
	}
	
	private class AccessSearchResultDataProvider implements IDataProvider<AccessSearchResult>{

		private boolean lastOrderAsc = true;
		private int lastOrderBy = DelegatedAccessConstants.SEARCH_COMPARE_DEFAULT;

		private List<AccessSearchResult> list;
		public void detach() {
			
		}
		public void detachManually(){
			this.list = null;
		}
		public Iterator<? extends AccessSearchResult> iterator(long first, long count) {
			//should really check bounds here 
			int f = (int) first;
			int c = (int) count;
			return getData().subList(f, f + c).iterator();
		}

		public IModel<AccessSearchResult> model(final AccessSearchResult object) {
			return new AbstractReadOnlyModel<AccessSearchResult>() {
				private static final long serialVersionUID = 1L;

				@Override
				public AccessSearchResult getObject() {
					return object;
				}
			};
		}

		public long size() {
			return getData().size();
		}

		private List<AccessSearchResult> getData(){
			if(firstLoad){
				return new ArrayList<AccessSearchResult>();
			}
			if(list == null){
				list = new ArrayList<AccessSearchResult>();
				if(eid != null && !"".equals(eid.trim()) && selectedSearchType.equals(searchTypeEid)){
					User u = getUser();
					if(u != null){
						list = projectLogic.getAccessForUser(u);
					}
				}else if(selectedSearchType.equals(searchTypeHierarchy) && nodeSelectOrder != null && nodeSelectOrder.size() > 0){
					list = projectLogic.getAccessAtLevel(nodeSelectOrder, includeLowerPerms);
				}
			}else if(lastOrderAsc != orderAsc || lastOrderBy != orderBy){
				sortList();
			}

			return list;
		}

		private void sortList(){
			Collections.sort(list, new AccessSearchResultComparator(orderBy));
			if(!orderAsc){
				Collections.reverse(list);
			}
			this.lastOrderAsc = orderAsc;
			this.lastOrderBy = orderBy;
		}

	}

	public User getUser(){
		User u = sakaiProxy.getUserByEid(eid);
		if(u == null){
			//couldn't find the user by eid, try internal id
			u = sakaiProxy.getUser(eid);
		}
		return u;
	}
}
