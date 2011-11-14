package org.sakaiproject.delegatedaccess.tool.pages;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.TreeModel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
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
import org.sakaiproject.delegatedaccess.model.NodeModel;
import org.sakaiproject.delegatedaccess.util.DelegatedAccessConstants;
import org.sakaiproject.delegatedaccess.util.NodeModelComparator;
import org.sakaiproject.site.api.Site;

public class UserPageSiteSearch extends BasePage {

	private String searchModel = "";
	private int orderBy = DelegatedAccessConstants.SEARCH_COMPARE_DEFAULT;
	private boolean orderAsc = true;
	private NodeModelDataProvider provider;
	private String search = "";
	private TreeModel treeModel;
	
	
	public UserPageSiteSearch(final String search, TreeModel treeModel){
		this.search = search;
		this.treeModel = treeModel;
		//Create Search Form:
		final PropertyModel<String> messageModel = new PropertyModel<String>(this, "search");
		Form<?> form = new Form("form");
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
							//ensure the access for this user has been granted
							projectLogic.grantAccessToSite(nodeModel);
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
				list = projectLogic.searchUserSites(getSearch(), treeModel);
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

	public String getSearch() {
		return search;
	}

	public void setSearch(String search) {
		this.search = search;
	}
	
}
