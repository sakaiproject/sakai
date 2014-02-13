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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.repeater.DefaultItemReuseStrategy;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.User;
import org.sakaiproject.delegatedaccess.model.ListOptionSerialized;
import org.sakaiproject.delegatedaccess.model.NodeModel;
import org.sakaiproject.delegatedaccess.model.SelectOption;
import org.sakaiproject.delegatedaccess.model.SiteSearchResult;
import org.sakaiproject.delegatedaccess.util.DelegatedAccessConstants;
import org.sakaiproject.delegatedaccess.util.SiteSearchResultComparator;

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
	private SiteSearchResultDataProvider provider;
	private String search = "";
	private String instructorField = "";
	private String selectedInstructorOption = DelegatedAccessConstants.ADVANCED_SEARCH_INSTRUCTOR_TYPE_INSTRUCTOR;
	private SelectOption termField;;
	private List<SelectOption> termOptions;
	private boolean statistics = false;
	private boolean currentStatisticsFlag = false;
	private Map<String, String> toolsMap;

	public UserPageSiteSearch(final String search, final Map<String, String> advancedFields, final boolean statistics, final boolean currentStatisticsFlag){
		this.search = search;
		this.statistics = statistics;
		this.currentStatisticsFlag = currentStatisticsFlag;
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
				setResponsePage(new UserPageSiteSearch("", null, true, true));
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
				setResponsePage(new UserPageSiteSearch("", null, true, false));
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
					selectedInstructorOption = advancedFields.get(DelegatedAccessConstants.ADVANCED_SEARCH_INSTRUCTOR_TYPE);
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
					String userType = new StringResourceModel("instructor", null).getString();
					if(DelegatedAccessConstants.ADVANCED_SEARCH_INSTRUCTOR_TYPE_MEMBER.equals(selectedInstructorOption)){
						userType = new StringResourceModel("member", null).getString();
					}
					searchString += userType + " " + instructorFieldModel.getObject();
				}
				if(termFieldModel.getObject() != null && !"".equals(termFieldModel.getObject())){
					if(!"".equals(searchString))
						searchString += ", ";
					searchString += new StringResourceModel("termField", null).getString() + " " + termFieldModel.getObject().getLabel();
				}
				return searchString;
			}
		};
		Form<?> form = new Form("form"){
			@Override
			protected void onSubmit() {
				super.onSubmit();
				if(provider != null){
					provider.detachManually();
				}
			}
		};
		form.add(new TextField<String>("search", searchModel));
		AbstractReadOnlyModel<String> instructorFieldLabelModel = new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				if(isShoppingPeriodTool()){
					return new StringResourceModel("instructor", null).getObject() + ":";
				}else{
					return new StringResourceModel("user", null).getObject() + ":";
				}
			}
		};
		form.add(new Label("instructorFieldLabel", instructorFieldLabelModel));
		form.add(new TextField<String>("instructorField", instructorFieldModel));
		//Instructor Options:
		RadioGroup group = new RadioGroup("instructorOptionsGroup", new PropertyModel<String>(this, "selectedInstructorOption")){
			@Override
			public boolean isVisible() {
				//only show if its not shopping period
				return !isShoppingPeriodTool();
			}
		};
		group.add(new Radio("instructorOption", Model.of(DelegatedAccessConstants.ADVANCED_SEARCH_INSTRUCTOR_TYPE_INSTRUCTOR)));
		group.add(new Radio("memberOption", Model.of(DelegatedAccessConstants.ADVANCED_SEARCH_INSTRUCTOR_TYPE_MEMBER)));
		form.add(group);
		
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
				return provider.size() > 0;
			}
		};
		AbstractReadOnlyModel<String> instructorSortLabel = new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				if(DelegatedAccessConstants.ADVANCED_SEARCH_INSTRUCTOR_TYPE_MEMBER.equals(selectedInstructorOption)){
					return new StringResourceModel("member", null).getObject();
				}else{
					return new StringResourceModel("instructor", null).getObject();
				}
			}
		};
		instructorSort.add(new Label("instructorSortLinkLabel", instructorSortLabel));
		add(instructorSort);
		
		Link<Void> accessSort = new Link<Void>("accessSortLink"){
			private static final long serialVersionUID = 1L;
			public void onClick() {
				changeOrder(DelegatedAccessConstants.SEARCH_COMPARE_ACCESS);
			}
		};
		add(accessSort);
		
		Link<Void> startDateSort = new Link<Void>("startDateSortLink"){
			private static final long serialVersionUID = 1L;
			public void onClick() {
				changeOrder(DelegatedAccessConstants.SEARCH_COMPARE_START_DATE);
			}
			@Override
			public boolean isVisible() {
				//this helps with the wicket:enlosure
				return statistics;
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
		
		Link<Void> accessModifiedBySort = new Link<Void>("accessModifiedBySortLink"){
			private static final long serialVersionUID = 1L;
			public void onClick() {
				changeOrder(DelegatedAccessConstants.SEARCH_COMPARE_ACCESS_MODIFIED_BY);
			}
			@Override
			public boolean isVisible() {
				//this helps hide all the extra columns with the wicket:enclosure in the html
				return !isShoppingPeriodTool();
			}
		};
		add(accessModifiedBySort);
		
		Link<Void> accessModifiedOnSort = new Link<Void>("accessModifiedOnSortLink"){
			private static final long serialVersionUID = 1L;
			public void onClick() {
				changeOrder(DelegatedAccessConstants.SEARCH_COMPARE_ACCESS_MODIFIED);
			}
		};
		add(accessModifiedOnSort);

		//Data:
		provider = new SiteSearchResultDataProvider();
		final DataView<SiteSearchResult> dataView = new DataView<SiteSearchResult>("searchResult", provider) {
			@Override
			public void populateItem(final Item item) {
				final SiteSearchResult siteSearchResult = (SiteSearchResult) item.getModelObject();
				AjaxLink<Void> siteTitleLink = new AjaxLink("siteTitleLink"){
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
						if(siteSearchResult.getSiteUrl() != null){
							//redirect the user to the site
							target.appendJavascript("popupWindow('" + siteSearchResult.getSiteUrl() + "', '" + new StringResourceModel("popupBlockWarning", null).getObject() + "')");
						}
					}
				};
				siteTitleLink.add(new Label("siteTitle", siteSearchResult.getSiteTitle()));
				item.add(siteTitleLink);
				final String siteId = siteSearchResult.getSiteId();
				item.add(new Label("siteId", siteId));
				item.add(new Label("term", siteSearchResult.getSiteTerm()));
				item.add(new Label("instructor", new AbstractReadOnlyModel<String>(){
		            @Override
		            public String getObject(){
		            	return siteSearchResult.getInstructorsString();
		            }
		            
				}));
				item.add(new Link<Void>("instructorLookupLink"){
					private static final long serialVersionUID = 1L;
					public void onClick() {
						boolean foundInstructors = false;
						for(User user : sakaiProxy.getInstructorsForSite(siteId)){
							siteSearchResult.addInstructor(user);
							foundInstructors = true;
						}
						if(!foundInstructors){
							siteSearchResult.setHasInstructor(false);
						}
					}

					@Override
					public boolean isVisible() {
						return (instructorField == null || "".equals(instructorField)) 
							&& siteSearchResult.isHasInstructor() && siteSearchResult.getInstructors().size() == 0;
					}
				});
				String access = isShoppingPeriodTool() ? siteSearchResult.getAccessRoleString() :siteSearchResult.getAccessString(); 
				item.add(new Label("access", access));
				item.add(new Label("startDate", siteSearchResult.getShoppingPeriodStartDateStr()){
					@Override
					public boolean isVisible() {
						//this helps hide all the extra columns with the wicket:enclosure in the html
						return statistics;
					}
				});
				item.add(new Label("endDate", siteSearchResult.getShoppingPeriodEndDateStr()));
				item.add(new Label("showAuthTools", siteSearchResult.getAuthToolsString(toolsMap)));
				item.add(new Label("showPublicTools", siteSearchResult.getPublicToolsString(toolsMap)));
				item.add(new Label("accessModifiedBy", siteSearchResult.getModifiedBySortName()){
					@Override
					public boolean isVisible() {
						//this helps hide all the extra columns with the wicket:enclosure in the html
						return !isShoppingPeriodTool();
					}
				});
				item.add(new Label("accessModified", siteSearchResult.getModifiedStr()));
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
	private class SiteSearchResultDataProvider implements IDataProvider<SiteSearchResult>{

		private static final long serialVersionUID = 1L;

		private boolean lastOrderAsc = true;
		private int lastOrderBy = DelegatedAccessConstants.SEARCH_COMPARE_DEFAULT;
		private List<SiteSearchResult> list;
		public void detach() {

		}
		public void detachManually(){
			this.list = null;
		}
		public Iterator<? extends SiteSearchResult> iterator(int first, int count) {
			return getData().subList(first, first + count).iterator();
		}

		public IModel<SiteSearchResult> model(final SiteSearchResult object) {
			return new AbstractReadOnlyModel<SiteSearchResult>() {
				private static final long serialVersionUID = 1L;

				@Override
				public SiteSearchResult getObject() {
					return object;
				}
			};
		}

		public int size() {
			return getData().size();
		}

		private List<SiteSearchResult> getData(){
			if(list == null){
				Map<String, String> advancedOptions = new HashMap<String,String>();
				if(termField != null && !"".equals(termField)){
					advancedOptions.put(DelegatedAccessConstants.ADVANCED_SEARCH_TERM, termField.getValue());
				}
				if(instructorField != null && !"".equals(instructorField)){
					advancedOptions.put(DelegatedAccessConstants.ADVANCED_SEARCH_INSTRUCTOR, instructorField);
					advancedOptions.put(DelegatedAccessConstants.ADVANCED_SEARCH_INSTRUCTOR_TYPE, selectedInstructorOption);
				}
				if(search == null){
					search = "";
				}
				if(!"".equals(search) || (advancedOptions != null && !advancedOptions.isEmpty())){
					 list = projectLogic.searchUserSites(getSearch(), advancedOptions.isEmpty() ? null : advancedOptions, (isShoppingPeriodTool() || statistics), isShoppingPeriodTool() || (statistics && currentStatisticsFlag));
//					if(currentStatisticsFlag){
//						//need to filter out the results and find only current shopping period results:
//						for (Iterator iterator = list.iterator(); iterator
//								.hasNext();) {
//							SiteSearchResult result = (SiteSearchResult) iterator.next();
//							Map<String, String> nodes = projectLogic.getNodesBySiteRef(new String[]{result.getSiteReference()}, DelegatedAccessConstants.SHOPPING_PERIOD_HIERARCHY_ID);
//							if(nodes == null || !nodes.containsKey(result.getSiteReference())){
//								//this site doesn't exist in the current shopping period hierarchy, so remove it
//								iterator.remove();
//							}
//						}
//						
//					}
				}else {
					list = new ArrayList<SiteSearchResult>();
				}
				sortList();
			}else if(lastOrderAsc != orderAsc || lastOrderBy != orderBy){
				sortList();
			}
			return list;
		}

		private void sortList(){
			Collections.sort(list, new SiteSearchResultComparator(orderBy));
			if(!orderAsc){
				Collections.reverse(list);
			}
			this.lastOrderAsc = orderAsc;
			this.lastOrderBy = orderBy;
		}

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
