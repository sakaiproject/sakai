package org.sakaiproject.delegatedaccess.tool.pages;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.repeater.DefaultItemReuseStrategy;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.delegatedaccess.model.ListOptionSerialized;
import org.sakaiproject.delegatedaccess.model.NodeModel;
import org.sakaiproject.delegatedaccess.model.SelectOption;
import org.sakaiproject.delegatedaccess.util.DelegatedAccessConstants;
import org.sakaiproject.delegatedaccess.util.NodeModelComparator;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.User;

/**
 * 
 * This page sorts and searches a user's access sites
 * 
 * @author Bryan Holladay (holladay@longsight.com)
 *
 */
public class UserPageSiteSearch extends BasePage {

	private int orderBy = DelegatedAccessConstants.SEARCH_COMPARE_DEFAULT;
	private boolean orderAsc = true;
	private NodeModelDataProvider provider;
	private String search = "";
	private String instructorField = "";
	private SelectOption termField;;
	private TreeModel treeModel;
	private List<SelectOption> termOptions;
	private boolean statistics = false;
	private SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
	private Map<String, String> toolsMap;

	public UserPageSiteSearch(final String search, final Map<String, String> advancedFields, TreeModel treeModel, final boolean statistics, final boolean currentStatisticsFlag){
		this.search = search;
		this.treeModel = treeModel;
		this.statistics = statistics;
		if(statistics){
			disableLink(shoppingStatsLink);
		}
		List<ListOptionSerialized> blankRestrictedTools = projectLogic.getEntireToolsList();
		toolsMap = new HashMap<String, String>();
		for(ListOptionSerialized option : blankRestrictedTools){
			toolsMap.put(option.getId(), option.getName());
		}
		
		//Setup Statistics Links:
		Link<Void> currentLink = new Link<Void>("currentLink") {
			private static final long serialVersionUID = 1L;
			public void onClick() {
				TreeModel treeModel = projectLogic.getTreeModelForShoppingPeriod(true);
				if(treeModel != null && ((DefaultMutableTreeNode) treeModel.getRoot()).getChildCount() == 0){
					treeModel = null;
				}
				setResponsePage(new UserPageSiteSearch("", null, treeModel, true, true));
			}
			@Override
			public boolean isVisible() {
				return statistics;
			}
		};
		currentLink.add(new Label("currentLinkLabel",new ResourceModel("link.current")).setRenderBodyOnly(true));
		currentLink.add(new AttributeModifier("title", true, new ResourceModel("link.current.tooltip")));
		add(currentLink);
		
		if(currentStatisticsFlag){
			disableLink(currentLink);
		}
		
		Link<Void> allLink = new Link<Void>("allLink") {
			private static final long serialVersionUID = 1L;
			public void onClick() {
				TreeModel treeModel = projectLogic.createEntireTreeModelForUser(DelegatedAccessConstants.SHOPPING_PERIOD_USER, false, true);
				if(treeModel != null && ((DefaultMutableTreeNode) treeModel.getRoot()).getChildCount() == 0){
					treeModel = null;
				}
				setResponsePage(new UserPageSiteSearch("", null, treeModel, true, false));
			}
			@Override
			public boolean isVisible() {
				return statistics;
			}
		};
		allLink.add(new Label("allLinkLabel",new ResourceModel("link.all")).setRenderBodyOnly(true));
		allLink.add(new AttributeModifier("title", true, new ResourceModel("link.all.tooltip")));
		add(allLink);
		if(!currentStatisticsFlag){
			disableLink(allLink);
		}
		
		
		termOptions = new ArrayList<SelectOption>();
		for(String[] entry : sakaiProxy.getTerms()){
			termOptions.add(new SelectOption(entry[1], entry[0]));
		}
		if(advancedFields != null){
			for(Entry<String, String> entry : advancedFields.entrySet()){
				if(DelegatedAccessConstants.ADVANCED_SEARCH_INSTRUCTOR.equals(entry.getKey())){
					instructorField = entry.getValue();
				}
				if(DelegatedAccessConstants.ADVANCED_SEARCH_TERM.equals(entry.getKey())){
					for(SelectOption option : termOptions){
						if(entry.getValue().equals(option.getValue())){
							termField = option;
							break;
						}
					}
				}
			}
		}
		//Create Search Form:
		final PropertyModel<String> searchModel = new PropertyModel<String>(this, "search");
		final PropertyModel<String> instructorFieldModel = new PropertyModel<String>(this, "instructorField");
		final PropertyModel<SelectOption> termFieldModel = new PropertyModel<SelectOption>(this, "termField");
		final IModel<String> searchStringModel = new IModel<String>() {
			
			public void detach() {
			}
			
			public void setObject(String arg0) {
			}
			
			public String getObject() {
				String searchString = "";
				if(searchModel.getObject() != null){
					searchString += new StringResourceModel("siteIdTitleField", null).getString() + " " + searchModel.getObject();
				}
				if(instructorFieldModel.getObject() != null && !"".equals(instructorFieldModel.getObject())){
					if(!"".equals(searchString))
						searchString += ", ";
					searchString += new StringResourceModel("instructorField", null).getString() + " " + instructorFieldModel.getObject();
				}
				if(termFieldModel.getObject() != null && !"".equals(termFieldModel.getObject())){
					if(!"".equals(searchString))
						searchString += ", ";
					searchString += new StringResourceModel("termField", null).getString() + " " + termFieldModel.getObject().getLabel();
				}
				return searchString;
			}
		};
		Form<?> form = new Form("form");
		form.add(new TextField<String>("search", searchModel));
		form.add(new TextField<String>("instructorField", instructorFieldModel));
		ChoiceRenderer choiceRenderer = new ChoiceRenderer("label", "value");
		DropDownChoice termFieldDropDown = new DropDownChoice("termField", termFieldModel, termOptions, choiceRenderer);
		//keeps the null option (choose one) after a user selects an option
		termFieldDropDown.setNullValid(true);
		form.add(termFieldDropDown);
		add(form);

		//show user's search (if not null)
		add(new Label("searchResultsTitle", new StringResourceModel("searchResultsTitle", null)){
			@Override
			public boolean isVisible() {
				return (searchModel.getObject() != null && !"".equals(searchModel.getObject()))
				|| (instructorFieldModel.getObject() != null && !"".equals(instructorFieldModel.getObject()))
				|| (termFieldModel.getObject() != null && !"".equals(termFieldModel.getObject()));
			}
		});
		add(new Label("searchString",searchStringModel){
			@Override
			public boolean isVisible() {
				return (searchModel.getObject() != null && !"".equals(searchModel.getObject()))
				|| (instructorFieldModel.getObject() != null && !"".equals(instructorFieldModel.getObject()))
				|| (termFieldModel.getObject() != null && !"".equals(termFieldModel.getObject()));
			}
		});

		//search result table:
		//Headers
		Link<Void> siteTitleSort = new Link<Void>("siteTitleSortLink"){
			private static final long serialVersionUID = 1L;
			public void onClick() {
				changeOrder(DelegatedAccessConstants.SEARCH_COMPARE_SITE_TITLE);
			}

			@Override
			public boolean isVisible() {
				return provider.size() > 0;
			}
		};
		add(siteTitleSort);
		Link<Void> siteIdSort = new Link<Void>("siteIdSortLink"){
			private static final long serialVersionUID = 1L;
			public void onClick() {
				changeOrder(DelegatedAccessConstants.SEARCH_COMPARE_SITE_ID);
			}

			@Override
			public boolean isVisible() {
				return provider.size() > 0;
			}
		};
		add(siteIdSort);
		Link<Void> termSort = new Link<Void>("termSortLink"){
			private static final long serialVersionUID = 1L;
			public void onClick() {
				changeOrder(DelegatedAccessConstants.SEARCH_COMPARE_TERM);
			}

			@Override
			public boolean isVisible() {
				return provider.size() > 0;
			}
		};
		add(termSort);
		Link<Void> instructorSort = new Link<Void>("instructorSortLink"){
			private static final long serialVersionUID = 1L;
			public void onClick() {
				changeOrder(DelegatedAccessConstants.SEARCH_COMPARE_INSTRUCTOR);
			}

			@Override
			public boolean isVisible() {
				return provider.size() > 0 && instructorField != null && !"".equals(instructorField);
			}
		};
		add(instructorSort);
		
		Link<Void> authorizationSort = new Link<Void>("authorizationSortLink"){
			private static final long serialVersionUID = 1L;
			public void onClick() {
				changeOrder(DelegatedAccessConstants.SEARCH_COMPARE_AUTHORIZATION);
			}
			@Override
			public boolean isVisible() {
				//this helps with the wicket:enlosure
				return statistics;
			}
		};
		add(authorizationSort);
		
		Link<Void> shoppersBecomeSort = new Link<Void>("shoppersBecomeSortLink"){
			private static final long serialVersionUID = 1L;
			public void onClick() {
				changeOrder(DelegatedAccessConstants.SEARCH_COMPARE_SHOPPERS_BECOME);
			}
		};
		add(shoppersBecomeSort);
		
		Link<Void> startDateSort = new Link<Void>("startDateSortLink"){
			private static final long serialVersionUID = 1L;
			public void onClick() {
				changeOrder(DelegatedAccessConstants.SEARCH_COMPARE_START_DATE);
			}
		};
		add(startDateSort);
		
		Link<Void> endDateSort = new Link<Void>("endDateSortLink"){
			private static final long serialVersionUID = 1L;
			public void onClick() {
				changeOrder(DelegatedAccessConstants.SEARCH_COMPARE_END_DATE);
			}
		};
		add(endDateSort);

		//Data:
		provider = new NodeModelDataProvider();
		final DataView<NodeModel> dataView = new DataView<NodeModel>("searchResult", provider) {
			@Override
			public void populateItem(final Item item) {
				final NodeModel nodeModel = (NodeModel) item.getModelObject();
				AjaxLink<Void> siteTitleLink = new AjaxLink("siteTitleLink"){
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
						Site site = sakaiProxy.getSiteByRef(nodeModel.getNode().description);
						if(site != null){
							if(!isShoppingPeriodTool()){
								//ensure the access for this user has been granted
								projectLogic.grantAccessToSite(nodeModel);
							}
							//redirect the user to the site
							target.appendJavascript("top.location='" + site.getUrl() + "'");
						}
					}
				};
				siteTitleLink.add(new Label("siteTitle", nodeModel.getNode().title));
				item.add(siteTitleLink);
				String siteId = nodeModel.getNode().description;
				if(siteId.startsWith("/site/")){
					siteId = siteId.substring(6);
				}

				item.add(new Label("siteId", siteId));
				item.add(new Label("term", nodeModel.getSiteTerm()));
				item.add(new Label("instructor", nodeModel.getSiteInstructors()){
					@Override
					public boolean isVisible() {
						return instructorField != null && !"".equals(instructorField);
					}
				});
				item.add(new Label("authorization", getAuthString(nodeModel)){
					@Override
					public boolean isVisible() {
						//this helps hide all the extra columns with the wicket:enclosure in the html
						return statistics;
					}
				});
				item.add(new Label("shoppersBecome", getAccessRealmRoleString(nodeModel)));
				item.add(new Label("startDate", getDateString(nodeModel, true)));
				item.add(new Label("endDate", getDateString(nodeModel, false)));
				item.add(new Label("showTools", getToolsString(nodeModel)));
			}
			@Override
			public boolean isVisible() {
				return provider.size() > 0;
			}
		};
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

	private String getDateString(NodeModel nodeModel, boolean startDate){
		Date date = null;
		if(startDate)
			date = nodeModel.getNodeShoppingPeriodStartDate();
		else
				date = nodeModel.getNodeShoppingPeriodEndDate();
		if(date == null){
			return "";
		}else{
			return format.format(date);
		}
	}
	
	private String getAuthString(NodeModel nodeModel){
		String auth = nodeModel.getNodeShoppingPeriodAuth();
		if(auth != null && !"".equals(auth)){
			return new StringResourceModel(auth, null).getString();
		}else{
			return "";
		}
	}
	
	private String getAccessRealmRoleString(NodeModel nodeModel){
		String[] inheritedAccess;
		inheritedAccess = nodeModel.getNodeAccessRealmRole();
		if("".equals(inheritedAccess[0])){
			return "";
		}else{
			return inheritedAccess[0] + " : " + inheritedAccess[1];
		}
	}
	
	private String getToolsString(NodeModel nodeModel){
		String restrictedTools = "";
		for(String tool : nodeModel.getNodeRestrictedTools()){
			if(!"".equals(restrictedTools)){
				restrictedTools += ", ";
			}
			String toolName = tool;
			if(toolsMap.containsKey(toolName)){
				toolName = toolsMap.get(toolName);
			}
			restrictedTools += toolName;
		}
		return restrictedTools;
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

	/**
	 * A data provider for the search results table.  This calls the search functions in Sakai
	 *
	 */
	private class NodeModelDataProvider implements IDataProvider<NodeModel>{

		private static final long serialVersionUID = 1L;

		private boolean lastOrderAsc = true;
		private int lastOrderBy = DelegatedAccessConstants.SEARCH_COMPARE_DEFAULT;

		private List<NodeModel> list;
		public void detach() {
			list = null;
		}

		public Iterator<? extends NodeModel> iterator(int first, int count) {
			return getData().subList(first, first + count).iterator();
		}

		public IModel<NodeModel> model(final NodeModel object) {
			return new AbstractReadOnlyModel<NodeModel>() {
				private static final long serialVersionUID = 1L;

				@Override
				public NodeModel getObject() {
					return object;
				}
			};
		}

		public int size() {
			return getData().size();
		}

		private List<NodeModel> getData(){
			if(list == null){
				Map<String, String> advancedOptions = new HashMap<String,String>();
				if(termField != null && !"".equals(termField)){
					advancedOptions.put(DelegatedAccessConstants.ADVANCED_SEARCH_TERM, termField.getValue());
				}
				if(instructorField != null && !"".equals(instructorField)){
					advancedOptions.put(DelegatedAccessConstants.ADVANCED_SEARCH_INSTRUCTOR, instructorField);
				}
				if(search == null){
					search = "";
				}
				if(!"".equals(search) || (advancedOptions != null && !advancedOptions.isEmpty())){
					list = projectLogic.searchUserSites(getSearch(), treeModel, advancedOptions.isEmpty() ? null : advancedOptions);
				}else {
					if(treeModel != null && treeModel.getRoot() != null){
						list = getAllSites((DefaultMutableTreeNode) treeModel.getRoot());
					}else{
						list = new ArrayList<NodeModel>();
					}
				}
				sortList();
			}else if(lastOrderAsc != orderAsc || lastOrderBy != orderBy){
				sortList();
			}
			return list;
		}

		private void sortList(){
			Collections.sort(list, new NodeModelComparator(orderBy));
			if(!orderAsc){
				Collections.reverse(list);
			}
			this.lastOrderAsc = orderAsc;
			this.lastOrderBy = orderBy;
		}

	}
	
	private List<NodeModel> getAllSites(DefaultMutableTreeNode node){
		List<NodeModel> returnList = new ArrayList<NodeModel>();
		if(node != null){
			NodeModel nodeModel = (NodeModel) node.getUserObject();
			if(nodeModel.getNode().description.startsWith("/site/")){
				//don't bother showing the term until they search for it
				nodeModel.setSiteTerm("-");
				returnList.add(nodeModel);
			}
			for(int i = 0; i < node.getChildCount(); i++){
				returnList.addAll(getAllSites((DefaultMutableTreeNode) node.getChildAt(i)));
			}
		}

		return returnList;
	}

	public String getSearch() {
		return search;
	}

	public void setSearch(String search) {
		this.search = search;
	}

	public boolean isStatistics() {
		return statistics;
	}

	public void setStatistics(boolean statistics) {
		this.statistics = statistics;
	}

}
