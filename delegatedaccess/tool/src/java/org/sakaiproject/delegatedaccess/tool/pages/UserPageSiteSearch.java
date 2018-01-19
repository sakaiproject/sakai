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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.link.ExternalLink;
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
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;

import org.sakaiproject.delegatedaccess.model.ListOptionSerialized;
import org.sakaiproject.delegatedaccess.model.SelectOption;
import org.sakaiproject.delegatedaccess.model.SiteSearchResult;
import org.sakaiproject.delegatedaccess.util.DelegatedAccessConstants;
import org.sakaiproject.delegatedaccess.util.SiteSearchResultComparator;
import org.sakaiproject.user.api.User;

/**
 * 
 * This page sorts and searches a user's access sites
 * 
 * @author Bryan Holladay (holladay@longsight.com)
 *
 */
@Slf4j
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
	public Map<String, List<SelectOption>> hierarchySelectOptions;
	public Map<String, SelectOption> hierarchySearchMap;
	public Map<String, String> hierarchyLabels = new HashMap<String, String>();
	public List<String> nodeSelectOrder;

	public UserPageSiteSearch(PageParameters params){
		String search = "";
		if(params.getNamedKeys().contains("search")){
			search = params.get("search").toString();
		}
		Map<String, Object> advancedFields = new HashMap<String, Object>();
		if(params.getNamedKeys().contains("term")){
			advancedFields.put(DelegatedAccessConstants.ADVANCED_SEARCH_TERM, params.get("term").toString());
		}
		if(params.getNamedKeys().contains("instructor")){
			advancedFields.put(DelegatedAccessConstants.ADVANCED_SEARCH_INSTRUCTOR, params.get("instructor").toString());
			//set type:
			String instructorType = DelegatedAccessConstants.ADVANCED_SEARCH_INSTRUCTOR_TYPE_INSTRUCTOR;
			if(params.getNamedKeys().contains("instructorType") && DelegatedAccessConstants.ADVANCED_SEARCH_INSTRUCTOR_TYPE_MEMBER.equals(params.get("instructorType").toString())){
				instructorType = DelegatedAccessConstants.ADVANCED_SEARCH_INSTRUCTOR_TYPE_MEMBER;
			}
			advancedFields.put(DelegatedAccessConstants.ADVANCED_SEARCH_INSTRUCTOR_TYPE, instructorType);
		}
		//we have at least one  hierarchy key/value:
		Map<String, String> hierarchyParams = new HashMap<String, String>();
		int i = 0;
		while(params.getNamedKeys().contains("hierarchyKey" + i) && params.getNamedKeys().contains("hierarchyValue" + i)){
			hierarchyParams.put(params.get("hierarchyKey").toString() + i, params.get("hierarchyValue").toString() + i);
			i++;
		}
		if(hierarchyParams.size() > 0){
			advancedFields.put(DelegatedAccessConstants.ADVANCED_SEARCH_HIERARCHY_FIELDS, hierarchyParams);
		}
		
		buildPage(search, advancedFields, false, false);
	}
	
	public UserPageSiteSearch(final String search, final Map<String, Object> advancedFields, final boolean statistics, final boolean currentStatisticsFlag){
		buildPage(search, advancedFields, statistics, currentStatisticsFlag);
	}
		
	@SuppressWarnings("unchecked")
	public void buildPage(final String search, final Map<String, Object> advancedFields, final boolean statistics, final boolean currentStatisticsFlag){
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
		Map<String, String> hierarchySearchFields = new HashMap<String, String>();
		if(advancedFields != null){
			for(Entry<String, Object> entry : advancedFields.entrySet()){
				if(DelegatedAccessConstants.ADVANCED_SEARCH_INSTRUCTOR.equals(entry.getKey())){
					instructorField = entry.getValue().toString();
					selectedInstructorOption = advancedFields.get(DelegatedAccessConstants.ADVANCED_SEARCH_INSTRUCTOR_TYPE).toString();
				}
				if(DelegatedAccessConstants.ADVANCED_SEARCH_TERM.equals(entry.getKey())){
					for(SelectOption option : termOptions){
						if(entry.getValue().equals(option.getValue())){
							termField = option;
							break;
						}
					}
				}
				if(DelegatedAccessConstants.ADVANCED_SEARCH_HIERARCHY_FIELDS.equals(entry.getKey())){
					hierarchySearchFields = (Map<String, String>) entry.getValue();
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
				if(searchModel.getObject() != null && !"".equals(searchModel.getObject().toString().trim())){
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
				//hierarchy params:
				if(hierarchySearchMap != null){
					for(Entry<String, SelectOption> entry : hierarchySearchMap.entrySet()){
						if(entry.getValue() != null && !"".equals(entry.getValue().getValue().trim())){
							if(!"".equals(searchString))
								searchString += ", ";
							searchString += hierarchyLabels.get(entry.getKey()) + ": " + entry.getValue().getValue().trim();
						}
					}
				}
				return searchString;
			}
		};
		final IModel<String> permaLinkModel = new IModel<String>(){

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				String path = "/shopping";
				
				Map<String, String> params = new HashMap<String, String>();
				//Search
				if(searchModel.getObject() != null){
					params.put("search", searchModel.getObject());
				}
				//term:
				if(termFieldModel.getObject() != null && !"".equals(termFieldModel.getObject())){
					params.put("term", termFieldModel.getObject().getValue());
				}
				//instructor
				if(instructorFieldModel.getObject() != null && !"".equals(instructorFieldModel.getObject())){
					params.put("instructor", instructorFieldModel.getObject());
					if(DelegatedAccessConstants.ADVANCED_SEARCH_INSTRUCTOR_TYPE_MEMBER.equals(selectedInstructorOption)){
						params.put("instructorType", DelegatedAccessConstants.ADVANCED_SEARCH_INSTRUCTOR_TYPE_MEMBER);
					}else{
						params.put("instructorType", DelegatedAccessConstants.ADVANCED_SEARCH_INSTRUCTOR_TYPE_INSTRUCTOR);
					}
				}
				//hierarchy params:
				if(hierarchySearchMap != null){
					int i = 0;
					for(Entry<String, SelectOption> entry : hierarchySearchMap.entrySet()){
						if(entry.getValue() != null && !"".equals(entry.getValue().getValue().trim())){
							params.put("hierarchyKey" + i, entry.getKey());
							params.put("hierarchyValue" + i, entry.getValue().getValue());
							i++;
						}
					}
				}
				
				String context = sakaiProxy.siteReference(sakaiProxy.getCurrentPlacement().getContext());
				
				String url = "";
				try{
					String tool = "sakai.delegatedaccess";
					if(isShoppingPeriodTool()){
						tool += ".shopping";
					}
					url = developerHelperService.getToolViewURL(tool, path, params, context);
				}catch (Exception e) {

				}
				return url;
			}

			@Override
			public void setObject(String arg0) {			
			}
			
		};
		final Form<?> form = new Form("form"){
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
		
		final ChoiceRenderer choiceRenderer = new ChoiceRenderer("label", "value");
		DropDownChoice termFieldDropDown = new DropDownChoice("termField", termFieldModel, termOptions, choiceRenderer){
			@Override
			public boolean isVisible() {
				return !sakaiProxy.isSearchHideTerm();
			}
		};
		//keeps the null option (choose one) after a user selects an option
		termFieldDropDown.setNullValid(true);
		form.add(termFieldDropDown);
		add(form);
		
		//hierarchy dropdown:
		String[] hierarchyTmp = sakaiProxy.getServerConfigurationStrings(DelegatedAccessConstants.HIERARCHY_SITE_PROPERTIES);
		if(hierarchyTmp == null || hierarchyTmp.length == 0){
			hierarchyTmp = DelegatedAccessConstants.DEFAULT_HIERARCHY;
		}
		final String[] hierarchy = hierarchyTmp;
		WebMarkupContainer hierarchyDiv = new WebMarkupContainer("hierarchyFields");
		final Comparator<SelectOption> optionComparator = new SelectOptionComparator();
		if(hierarchySelectOptions == null || hierarchySelectOptions.size() == 0){
			nodeSelectOrder = new ArrayList<String>();
			hierarchySearchMap = new HashMap<String, SelectOption>();
			for(String s : hierarchy){
				hierarchySearchMap.put(s, null);
				nodeSelectOrder.add(s);
				hierarchyLabels.put(s, sakaiProxy.getHierarchySearchLabel(s));
			}
			Map<String, String> searchParams = new HashMap<String, String>();
			for(Entry<String, SelectOption> entry : hierarchySearchMap.entrySet()){
				String value = entry.getValue() == null ? "" : entry.getValue().getValue();
				//in case user passed in a parameter, set it:
				if(hierarchySearchFields.containsKey(entry.getKey())){
					value = hierarchySearchFields.get(entry.getKey());
				}
				searchParams.put(entry.getKey(), value);
			}
			Map<String, Set<String>> hierarchyOptions = projectLogic.getHierarchySearchOptions(searchParams);
			hierarchySelectOptions = new HashMap<String, List<SelectOption>>();
			for(Entry<String, Set<String>> entry : hierarchyOptions.entrySet()){
				List<SelectOption> options = new ArrayList<SelectOption>();
				for(String s : entry.getValue()){
					SelectOption o = new SelectOption(s, s);
					options.add(o);
					if(searchParams.containsKey(entry.getKey()) && s.equals(searchParams.get(entry.getKey()))){
						hierarchySearchMap.put(entry.getKey(), o);
					}
				}
				Collections.sort(options, optionComparator);
				hierarchySelectOptions.put(entry.getKey(), options);
			}
		}
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
				final String hierarchyLevel = item.getModelObject().toString();
				item.add(new Label("hierarchyLabel", hierarchyLabels.containsKey(hierarchyLevel) ? hierarchyLabels.get(hierarchyLevel) : hierarchyLevel));
				final DropDownChoice choice = new DropDownChoice("hierarchyLevel", new NodeSelectModel(hierarchyLevel), hierarchySelectOptions.get(hierarchyLevel), choiceRenderer);
				
				//keeps the null option (choose one) after a user selects an option
				choice.setNullValid(true);
				choice.add(new AjaxFormComponentUpdatingBehavior("onchange"){
					protected void onUpdate(AjaxRequestTarget target) {
						Map<String, String> searchParams = new HashMap<String, String>();
						for(Entry<String, SelectOption> entry : hierarchySearchMap.entrySet()){
							searchParams.put(entry.getKey(), entry.getValue() == null ? "" : entry.getValue().getValue());
						}
						Map<String, Set<String>> hierarchyOptions = projectLogic.getHierarchySearchOptions(searchParams);
						hierarchySelectOptions = new HashMap<String, List<SelectOption>>();
						for(Entry<String, Set<String>> entry : hierarchyOptions.entrySet()){
							List<SelectOption> options = new ArrayList<SelectOption>();
							for(String s : entry.getValue()){
								options.add(new SelectOption(s, s));
							}
							Collections.sort(options, optionComparator);
							hierarchySelectOptions.put(entry.getKey(), options);
						}

						//refresh everything:
						target.add(form);
					}
				});
				item.add(choice);
			}


		};
		hierarchyDiv.add(dropdowns);
		form.add(hierarchyDiv);


		//show user's search (if not null)
		add(new Label("searchResultsTitle", new StringResourceModel("searchResultsTitle", null)){
			@Override
			public boolean isVisible() {
				return (searchModel.getObject() != null && !"".equals(searchModel.getObject()))
				|| (instructorFieldModel.getObject() != null && !"".equals(instructorFieldModel.getObject()))
				|| (termFieldModel.getObject() != null && !"".equals(termFieldModel.getObject()))
				|| hierarchyOptionSelected();
			}
		});
		add(new Label("searchString",searchStringModel){
			@Override
			public boolean isVisible() {
				return (searchModel.getObject() != null && !"".equals(searchModel.getObject()))
				|| (instructorFieldModel.getObject() != null && !"".equals(instructorFieldModel.getObject()))
				|| (termFieldModel.getObject() != null && !"".equals(termFieldModel.getObject()))
				|| hierarchyOptionSelected();
			}
		});
		add(new TextField("permaLink", permaLinkModel){
			@Override
			public boolean isVisible() {
				return (searchModel.getObject() != null && !"".equals(searchModel.getObject()))
				|| (instructorFieldModel.getObject() != null && !"".equals(instructorFieldModel.getObject()))
				|| (termFieldModel.getObject() != null && !"".equals(termFieldModel.getObject())) || hierarchyOptionSelected();
			}
		});
		//search result table:
		//Headers
		final Link<Void> siteTitleSort = new Link<Void>("siteTitleSortLink"){
			private static final long serialVersionUID = 1L;
			public void onClick() {
				changeOrder(DelegatedAccessConstants.SEARCH_COMPARE_SITE_TITLE);
			}

			@Override
			public boolean isVisible() {
				return provider.size() > 0;
			}
		};
		final Label siteTitleLabel = new Label("siteTitleLabel", new StringResourceModel("siteTitleHeader", null));
		siteTitleSort.add(siteTitleLabel);
		add(siteTitleSort);
		
		final Link<Void> siteIdSort = new Link<Void>("siteIdSortLink"){
			private static final long serialVersionUID = 1L;
			public void onClick() {
				changeOrder(DelegatedAccessConstants.SEARCH_COMPARE_SITE_ID);
			}

			@Override
			public boolean isVisible() {
				return provider.size() > 0;
			}
		};
		final Label siteIdSortLabel = new Label("siteIdSortLabel", new StringResourceModel("siteIdHeader", null));
		siteIdSort.add(siteIdSortLabel);
		add(siteIdSort);
		
		final Link<Void> termSort = new Link<Void>("termSortLink"){
			private static final long serialVersionUID = 1L;
			public void onClick() {
				changeOrder(DelegatedAccessConstants.SEARCH_COMPARE_TERM);
			}

			@Override
			public boolean isVisible() {
				return provider.size() > 0;
			}
		};
		final Label termSortLabel = new Label("termSortLabel", new StringResourceModel("termHeader", null));
		termSort.add(termSortLabel);
		add(termSort);
		
		AbstractReadOnlyModel<String> instructorSortLabelModel = new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				if(DelegatedAccessConstants.ADVANCED_SEARCH_INSTRUCTOR_TYPE_MEMBER.equals(selectedInstructorOption)){
					return new StringResourceModel("member", null).getObject();
				}else{
					return new StringResourceModel("instructor", null).getObject();
				}
			}
		};
		final Link<Void> instructorSort = new Link<Void>("instructorSortLink"){
			private static final long serialVersionUID = 1L;
			public void onClick() {
				changeOrder(DelegatedAccessConstants.SEARCH_COMPARE_INSTRUCTOR);
			}

			@Override
			public boolean isVisible() {
				return provider.size() > 0;
			}
		};
		final Label instructorSortLabel = new Label("instructorSortLabel", instructorSortLabelModel);
		instructorSort.add(instructorSortLabel);
		add(instructorSort);
		
		final Link<Void> providersSort = new Link<Void>("providersSortLink"){
			private static final long serialVersionUID = 1L;
			public void onClick() {
				changeOrder(DelegatedAccessConstants.SEARCH_COMPARE_PROVIDERS);
			}
			@Override
			public boolean isVisible() {
				//this helps hide all the extra columns with the wicket:enclosure in the html
				return !isShoppingPeriodTool() && sakaiProxy.isProviderIdLookupEnabled();
			}
		};
		final Label providersSortLabel = new Label("providersSortLabel", new StringResourceModel("providers", null));
		providersSort.add(providersSortLabel);
		add(providersSort);
		
		final Link<Void> publishedSort = new Link<Void>("publishedSortLink"){
			private static final long serialVersionUID = 1L;
			public void onClick() {
				changeOrder(DelegatedAccessConstants.SEARCH_COMPARE_PUBLISHED);
			}
			@Override
			public boolean isVisible() {
				//this helps hide all the extra columns with the wicket:enclosure in the html
				return !isShoppingPeriodTool();
			}
		};
		final Label publishedSortLabel = new Label("publishedSortLabel", new StringResourceModel("published", null));
		publishedSort.add(publishedSortLabel);
		add(publishedSort);
		
		final Link<Void> accessSort = new Link<Void>("accessSortLink"){
			private static final long serialVersionUID = 1L;
			public void onClick() {
				changeOrder(DelegatedAccessConstants.SEARCH_COMPARE_ACCESS);
			}
		};
		final Label accessSortLabel = new Label("accessSortLabel", new StringResourceModel("access", null));
		accessSort.add(accessSortLabel);
		add(accessSort);
		
		final Link<Void> startDateSort = new Link<Void>("startDateSortLink"){
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
		final Label startDateSortLabel = new Label("startDateSortLabel", new StringResourceModel("startDate", null));
		startDateSort.add(startDateSortLabel);
		add(startDateSort);
		
		final Link<Void> endDateSort = new Link<Void>("endDateSortLink"){
			private static final long serialVersionUID = 1L;
			public void onClick() {
				changeOrder(DelegatedAccessConstants.SEARCH_COMPARE_END_DATE);
			}
			@Override
			public boolean isVisible() {
				return statistics;
			}
		};
		final Label endDateSortLabel = new Label("endDateSortLabel", new StringResourceModel("endDate", null));
		endDateSort.add(endDateSortLabel);
		add(endDateSort);
		
		final Label showAuthToolsHeader = new Label("showAuthToolsHeader", new StringResourceModel("showAuthToolsHeader", null)){
			@Override
			public boolean isVisible() {
				return statistics;
			}
		};
		add(showAuthToolsHeader);
		
		final Label showPublicToolsHeader = new Label("showPublicToolsHeader", new StringResourceModel("showPublicToolsHeader", null)){
			@Override
			public boolean isVisible() {
				return statistics;
			}
		};
		add(showPublicToolsHeader);
		
		final Link<Void> accessModifiedBySort = new Link<Void>("accessModifiedBySortLink"){
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
		final Label accessModifiedBySortLabel = new Label("accessModifiedBySortLabel", new StringResourceModel("accessModifiedBy", null));
		accessModifiedBySort.add(accessModifiedBySortLabel);
		add(accessModifiedBySort);
		
		final Link<Void> accessModifiedOnSort = new Link<Void>("accessModifiedOnSortLink"){
			private static final long serialVersionUID = 1L;
			public void onClick() {
				changeOrder(DelegatedAccessConstants.SEARCH_COMPARE_ACCESS_MODIFIED);
			}
			@Override
			public boolean isVisible() {
				//this helps hide all the extra columns with the wicket:enclosure in the html
				return !isShoppingPeriodTool();
			}
		};
		final Label accessModifiedOnSortLabel = new Label("accessModifiedOnSortLabel", new StringResourceModel("accessModifiedOn", null));
		accessModifiedOnSort.add(accessModifiedOnSortLabel);
		add(accessModifiedOnSort);

		//Data:
		provider = new SiteSearchResultDataProvider();
		final DataView<SiteSearchResult> dataView = new DataView<SiteSearchResult>("searchResult", provider) {
			@Override
			public void populateItem(final Item item) {
				final SiteSearchResult siteSearchResult = (SiteSearchResult) item.getModelObject();
				ExternalLink siteTitleLink = new ExternalLink("siteTitleLink", siteSearchResult.getSiteUrl());
				siteTitleLink.add(new Label("siteTitle", siteSearchResult.getSiteTitle()));
				item.add(siteTitleLink);
				final String siteRef = siteSearchResult.getSiteReference();
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
				item.add(new Label("providers", new AbstractReadOnlyModel<String>(){
			            @Override
			            public String getObject(){
			            	return siteSearchResult.getProviders();
			            }
					}){
					@Override
					public boolean isVisible() {
						return !isShoppingPeriodTool() && sakaiProxy.isProviderIdLookupEnabled();
					}
				});
				item.add(new Link<Void>("providersLookupLink"){
					private static final long serialVersionUID = 1L;
					public void onClick() {
						String providers = sakaiProxy.getProviderId(siteRef);
						if(providers == null || "".equals(providers)){
							//set it to a empty space so that the link will hide itself
							providers = " ";
						}
						siteSearchResult.setProviders(providers);
					}

					@Override
					public boolean isVisible() {
						return !isShoppingPeriodTool() && sakaiProxy.isProviderIdLookupEnabled() && "".equals(siteSearchResult.getProviders());
					}
				});
				
				StringResourceModel publishedModel = siteSearchResult.isSitePublished() ? new StringResourceModel("yes", null) : new StringResourceModel("no", null);
				item.add(new Label("published", publishedModel){
					@Override
					public boolean isVisible() {
						return !isShoppingPeriodTool();
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
		
		IModel<File> exportSearchModel = new AbstractReadOnlyModel<File>() {

			@Override
			public File getObject() {
				List<SiteSearchResult> data = provider.getData();
				try{
					String seperator = ",";
					String lineBreak = "\n";
					File file = File.createTempFile(new StringResourceModel("searchExportFileName", null).getObject(), ".csv");
					FileWriter writer = new FileWriter(file.getAbsolutePath());
					//write headers:
					StringBuffer sb = new StringBuffer();
					if(siteTitleSort.isVisible()){
						sb.append("\"");
						sb.append(siteTitleLabel.getDefaultModelObjectAsString());
						sb.append("\"");
						sb.append(seperator);
					}
					if(instructorSort.isVisible()){
						sb.append("\"");
						sb.append(instructorSortLabel.getDefaultModelObjectAsString());
						sb.append("\"");
						sb.append(seperator);
					}
					if(termSort.isVisible()){
						sb.append("\"");
						sb.append(termSortLabel.getDefaultModelObjectAsString());
						sb.append("\"");
						sb.append(seperator);
					}
					if(siteIdSort.isVisible()){
						sb.append("\"");
						sb.append(siteIdSortLabel.getDefaultModelObjectAsString());
						sb.append("\"");
						sb.append(seperator);
					}
					if(providersSort.isVisible()){
						sb.append("\"");
						sb.append(providersSortLabel.getDefaultModelObjectAsString());
						sb.append("\"");
						sb.append(seperator);
					}
					if(publishedSort.isVisible()){
						sb.append("\"");
						sb.append(publishedSortLabel.getDefaultModelObjectAsString());
						sb.append("\"");
						sb.append(seperator);
					}
					if(accessSort.isVisible()){
						sb.append("\"");
						sb.append(accessSortLabel.getDefaultModelObjectAsString());
						sb.append("\"");
						sb.append(seperator);
					}
					if(startDateSort.isVisible()){
						sb.append("\"");
						sb.append(startDateSortLabel.getDefaultModelObjectAsString());
						sb.append("\"");
						sb.append(seperator);
					}
					if(endDateSort.isVisible()){
						sb.append("\"");
						sb.append(endDateSortLabel.getDefaultModelObjectAsString());
						sb.append("\"");
						sb.append(seperator);
					}
					if(showAuthToolsHeader.isVisible()){
						sb.append("\"");
						sb.append(showAuthToolsHeader.getDefaultModelObjectAsString());
						sb.append("\"");
						sb.append(seperator);
					}
					if(showPublicToolsHeader.isVisible()){
						sb.append("\"");
						sb.append(showPublicToolsHeader.getDefaultModelObjectAsString());
						sb.append("\"");
						sb.append(seperator);
					}
					if(accessModifiedBySort.isVisible()){
						sb.append("\"");
						sb.append(accessModifiedBySortLabel.getDefaultModelObjectAsString());
						sb.append("\"");
						sb.append(seperator);
					}
					if(accessModifiedOnSort.isVisible()){
						sb.append("\"");
						sb.append(accessModifiedOnSortLabel.getDefaultModelObjectAsString());
						sb.append("\"");
						sb.append(seperator);
					}
					sb.append(lineBreak);

					String yes = new StringResourceModel("yes", null).getObject();
					String no = new StringResourceModel("no", null).getObject();
					
					for(SiteSearchResult siteSearchResult : data){
						if(siteTitleSort.isVisible()){
							sb.append("\"");
							sb.append(siteSearchResult.getSiteTitle());
							sb.append("\"");
							sb.append(seperator);
						}
						if(instructorSort.isVisible()){
							if(siteSearchResult.isHasInstructor() && siteSearchResult.getInstructors().size() == 0){
								//we need to look up instructor:
								boolean foundInstructors = false;
								for(User user : sakaiProxy.getInstructorsForSite(siteSearchResult.getSiteId())){
									siteSearchResult.addInstructor(user);
									foundInstructors = true;
								}
								if(!foundInstructors){
									siteSearchResult.setHasInstructor(false);
								}
							}
							sb.append("\"");
							sb.append(siteSearchResult.getInstructorsString());
							sb.append("\"");
							sb.append(seperator);
						}
						if(termSort.isVisible()){
							sb.append("\"");
							sb.append(siteSearchResult.getSiteTerm());
							sb.append("\"");
							sb.append(seperator);
						}
						if(siteIdSort.isVisible()){
							sb.append("\"");
							sb.append(siteSearchResult.getSiteId());
							sb.append("\"");
							sb.append(seperator);
						}
						if(providersSort.isVisible()){
							if("".equals(siteSearchResult.getProviders())){
								//look up providers if it isn't already set
								siteSearchResult.setProviders(sakaiProxy.getProviderId(siteSearchResult.getSiteReference()));
							}
							sb.append("\"");
							sb.append(siteSearchResult.getProviders());
							sb.append("\"");
							sb.append(seperator);
						}
						if(publishedSort.isVisible()){
							sb.append("\"");
							sb.append((siteSearchResult.isSitePublished() ? yes: no) );
							sb.append("\"");
							sb.append(seperator);
						}
						if(accessSort.isVisible()){
							sb.append("\"");
							sb.append((isShoppingPeriodTool() ? siteSearchResult.getAccessRoleString() :siteSearchResult.getAccessString()));
							sb.append("\"");
							sb.append(seperator);
						}
						if(startDateSort.isVisible()){
							sb.append("\"");
							sb.append(siteSearchResult.getShoppingPeriodStartDateStr());
							sb.append("\"");
							sb.append(seperator);
						}
						if(endDateSort.isVisible()){
							sb.append("\"");
							sb.append(siteSearchResult.getShoppingPeriodEndDateStr());
							sb.append("\"");
							sb.append(seperator);
						}
						if(showAuthToolsHeader.isVisible()){
							sb.append("\"");
							sb.append(siteSearchResult.getAuthToolsString(toolsMap));
							sb.append("\"");
							sb.append(seperator);
						}
						if(showPublicToolsHeader.isVisible()){
							sb.append("\"");
							sb.append(siteSearchResult.getPublicToolsString(toolsMap));
							sb.append("\"");
							sb.append(seperator);
						}
						if(accessModifiedBySort.isVisible()){
							sb.append("\"");
							sb.append(siteSearchResult.getModifiedBySortName());
							sb.append("\"");
							sb.append(seperator);
						}
						if(accessModifiedOnSort.isVisible()){
							sb.append("\"");
							sb.append(siteSearchResult.getModifiedStr());
							sb.append("\"");
							sb.append(seperator);
						}
						sb.append(lineBreak);
					}
					
					writer.append(sb.toString());
					writer.flush();
					writer.close();

					return file;
				}catch(IOException e){
					log.error(e.getMessage(), e);
					return null;
				}
			}
		};

		add(new DownloadLink("exportData", exportSearchModel){
			@Override
			public void onClick(){
				Object fileObj = getModelObject();
				if(fileObj != null && fileObj instanceof File){
					File file = (File) fileObj;
					IResourceStream resourceStream = new FileResourceStream(new org.apache.wicket.util.file.File(file));
					//TODO: FIX THIS
					log.error("TODO: Fix code to exportData removed in migration.");
//					getRequestCycle().setRequestTarget(new ResourceStreamRequestTarget(resourceStream, file.getName()).setFileName(new StringResourceModel("searchExportFileName", null).getObject() + ".csv"));
				}
			}
		});
		
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
		public Iterator<? extends SiteSearchResult> iterator(long first, long count) {
			//should really check bounds here 
			int f = (int) first;
			int c = (int) count;
			return getData().subList(f, f + c).iterator();
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

		public long size() {
			return getData().size();
		}

		private List<SiteSearchResult> getData(){
			if(list == null){
				Map<String, Object> advancedOptions = new HashMap<String,Object>();
				if(termField != null && !"".equals(termField)){
					advancedOptions.put(DelegatedAccessConstants.ADVANCED_SEARCH_TERM, termField.getValue());
				}
				if(instructorField != null && !"".equals(instructorField)){
					advancedOptions.put(DelegatedAccessConstants.ADVANCED_SEARCH_INSTRUCTOR, instructorField);
					advancedOptions.put(DelegatedAccessConstants.ADVANCED_SEARCH_INSTRUCTOR_TYPE, selectedInstructorOption);
				}
				//hierarchy params
				Map<String, String> hierarchyParams = new HashMap<String, String>();
				for(Entry<String, SelectOption> entry : hierarchySearchMap.entrySet()){
					if(entry.getValue() != null && !"".equals(entry.getValue().getValue().trim())){
						hierarchyParams.put(entry.getKey(), entry.getValue().getValue().trim());
					}
				}
				if(hierarchyParams.size() > 0){
					advancedOptions.put(DelegatedAccessConstants.ADVANCED_SEARCH_HIERARCHY_FIELDS, hierarchyParams);
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

	public boolean hierarchyOptionSelected(){
		boolean hierachySelected = false;
		for(Entry<String, SelectOption> entry : hierarchySearchMap.entrySet()){
			if(entry.getValue() != null && !"".equals(entry.getValue().getValue().trim())){
				hierachySelected = true;
				break;
			}
				
		}
		return hierachySelected;
	}
	
	private class SelectOptionComparator implements Comparator<SelectOption>, Serializable{

		@Override
		public int compare(SelectOption o1, SelectOption o2) {
			return o1.getLabel().compareTo(o2.getLabel());
		}
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
			return hierarchySearchMap.get(nodeId);
		}

		@Override
		public void setObject(SelectOption arg0) {
			hierarchySearchMap.put(nodeId, arg0);
		}
	}
}
