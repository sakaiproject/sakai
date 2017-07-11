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
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
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
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.delegatedaccess.model.SearchResult;
import org.sakaiproject.delegatedaccess.util.DelegatedAccessConstants;
import org.sakaiproject.delegatedaccess.util.SearchResultComparator;

/**
 * This page searches for user's in Sakai and redirects the user to their edit page to assign access
 * 
 * @author Bryan Holladay (holladay@longsight.com)
 *
 */
public class SearchUsersPage extends BasePage {

	Link<Void> toThirdPageLink;
	private String search = "";
	private int orderBy = DelegatedAccessConstants.SEARCH_COMPARE_DEFAULT;
	private boolean orderAsc = true;
	SearchResultDataProvider provider;

	public SearchUsersPage() {

		disableLink(searchUsersLink);

		//Create Search Form:
		final PropertyModel<String> messageModel = new PropertyModel<String>(this, "search");
		Form<?> form = new Form("form"){
			@Override
			protected void onSubmit() {
				super.onSubmit();
				if(provider != null){
					provider.detachManually();
				}
			}
		};
		form.add(new TextField<String>("search", messageModel));
		add(form);

		//show user's search (if not null)
		add(new Label("searchResultsTitle", new StringResourceModel("searchResultsTitle", null)){
			@Override
			public boolean isVisible() {
				return messageModel.getObject() != null && !"".equals(messageModel.getObject());
			}
		});
		add(new Label("searchString",messageModel){
			@Override
			public boolean isVisible() {
				return messageModel.getObject() != null && !"".equals(messageModel.getObject());
			}
		});

		//search result table:
		//Headers
		Link<Void> userIdSort = new Link<Void>("userIdSortLink"){
			private static final long serialVersionUID = 1L;
			public void onClick() {
				changeOrder(DelegatedAccessConstants.SEARCH_COMPARE_EID);
			}

			@Override
			public boolean isVisible() {
				return provider.size() > 0;
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
				return provider.size() > 0;
			}
		};
		add(nameSort);
		Link<Void> emailSort = new Link<Void>("emailSortLink"){
			private static final long serialVersionUID = 1L;
			public void onClick() {
				changeOrder(DelegatedAccessConstants.SEARCH_COMPARE_EMAIL);
			}

			@Override
			public boolean isVisible() {
				return provider.size() > 0;
			}
		};
		add(emailSort);
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
		//Data:
		provider = new SearchResultDataProvider();
		final DataView<SearchResult> dataView = new DataView<SearchResult>("searchResult", provider) {
			@Override
			public void populateItem(final Item item) {
				final SearchResult searchResult = (SearchResult) item.getModelObject();
				item.add(new Label("userId", searchResult.getEid()));
				Link<Void> userEditLink = new Link("editLink"){
					private static final long serialVersionUID = 1L;
					public void onClick() {
						setResponsePage(new UserEditPage(searchResult.getId(), searchResult.getDisplayName()));
					}
				};
				item.add(userEditLink);
				Link<Void> userViewLink = new Link("viewLink"){
					private static final long serialVersionUID = 1L;
					public void onClick() {
						setResponsePage(new SearchAccessPage(false, searchResult.getEid()));
					}
				};
				item.add(userViewLink);
				item.add(new Label("name", searchResult.getSortName()));
				item.add(new Label("email", searchResult.getEmail()));
				item.add(new Label("type", searchResult.getType()));
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
	private class SearchResultDataProvider implements IDataProvider<SearchResult>{

		private boolean lastOrderAsc = true;
		private int lastOrderBy = DelegatedAccessConstants.SEARCH_COMPARE_DEFAULT;

		private List<SearchResult> list;
		public void detach() {
			
		}
		public void detachManually(){
			this.list = null;
		}
		public Iterator<? extends SearchResult> iterator(long first, long count) {
			//should really check bounds here 
			int f = (int) first;
			int c = (int) count;
			return getData().subList(f, f + c).iterator();
		}

		public IModel<SearchResult> model(final SearchResult object) {
			return new AbstractReadOnlyModel<SearchResult>() {
				private static final long serialVersionUID = 1L;

				@Override
				public SearchResult getObject() {
					return object;
				}
			};
		}

		public long size() {
			return getData().size();
		}

		private List<SearchResult> getData(){
			if(list == null){
				if(getSearch() != null && !"".equals(getSearch())){
					list = projectLogic.searchUsers(getSearch());
					if(!sakaiProxy.isSuperUser()){
						//only allow super admins to modify their own permissions,
						//otherwise, remove the current user's id
						String userId = sakaiProxy.getCurrentUserId();
						for (Iterator userItr = list.iterator(); userItr
								.hasNext();) {
							SearchResult user = (SearchResult) userItr.next();
							if(userId.equals(user.getId())){
								userItr.remove();
								break;
							}
						}
					}
					sortList();
				}else{
					list = new ArrayList<SearchResult>();
				}
			}else if(lastOrderAsc != orderAsc || lastOrderBy != orderBy){
				sortList();
			}
			return list;
		}

		private void sortList(){
			Collections.sort(list, new SearchResultComparator(orderBy));
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


}
