/**
 * 
 */
package org.sakaiproject.dash.tool.panels;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.DefaultItemReuseStrategy;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.dash.logic.DashboardConfig;
import org.sakaiproject.dash.logic.DashboardLogic;
import org.sakaiproject.dash.logic.SakaiProxy;
import org.sakaiproject.dash.model.NewsItem;
import org.sakaiproject.dash.model.NewsLink;
import org.sakaiproject.dash.tool.util.JsonHelper;
import org.sakaiproject.dash.util.DateUtil;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;

/**
 * 
 *
 */
public class MOTDPanel extends Panel {

	private static final Logger logger = Logger.getLogger(MOTDPanel.class);
	
	public static final int MOTD_MODE_HIDDEN = 0;
	public static final int MOTD_MODE_TEXT = 1;
	public static final int MOTD_MODE_LIST = 2;
	
	protected static final String DATE_FORMAT = "dd-MMM-yyyy";
	protected static final String TIME_FORMAT = "HH:mm";
	protected static final String DATETIME_FORMAT = "dd-MMM-yyyy HH:mm";
	
	@SpringBean(name="org.sakaiproject.dash.logic.SakaiProxy")
	protected SakaiProxy sakaiProxy;
	
	@SpringBean(name="org.sakaiproject.dash.logic.DashboardLogic")
	protected DashboardLogic dashboardLogic;

	@SpringBean(name="org.sakaiproject.dash.logic.DashboardConfig")
	protected DashboardConfig dashboardConfig;

	protected NewsLinksDataProvider motdProvider = null;
	protected String motdDivId = null;
	protected String motdCountId = null;
	//protected int pageSize = 5;
	protected int pageSize = dashboardConfig.getConfigValue(DashboardConfig.PROP_DEFAULT_ITEMS_IN_PANEL, 5);
	protected int motdMode = dashboardConfig.getConfigValue(DashboardConfig.PROP_MOTD_MODE, 1);

	public MOTDPanel(String id) {
		super(id);
		
		initText();
		initPanel();
	}
	
	protected void initText() {
		
		WebMarkupContainer motdDiv = new WebMarkupContainer("motdTextDiv");
		add(motdDiv);
		List<NewsItem> motdList = dashboardLogic.getMOTD();
		if(motdList == null || motdList.isEmpty()) {
			motdDiv.add(new Label("motdText", "No new messages"));
		} else {
			NewsItem motd = motdList.get(0);
			Map<String, Object> info = dashboardLogic.getEntityMapping(motd.getSourceType().getIdentifier(), motd.getEntityReference(), getLocale());
			motdDiv.add(new Label("motdTitle", (String) info.get("title")));
			motdDiv.add(new Label("motdText", (String) info.get("description")));
		}
		if(motdMode != MOTD_MODE_TEXT) {
			motdDiv.setVisibilityAllowed(false);
			motdDiv.setVisible(false);
		}
	}

	protected void initPanel() {
		
		if(this.motdDivId != null) {
			this.remove(motdDivId );
		}
		
		//get list of items from db, wrapped in a dataprovider
		motdProvider = new NewsLinksDataProvider();
		
        final WebMarkupContainer motdDiv = new WebMarkupContainer("motdDiv");
        motdDiv.setOutputMarkupId(true);
        add(motdDiv);
        this.motdDivId = motdDiv.getId();
        
        ResourceLoader rl = new ResourceLoader("dash_entity");
        WebMarkupContainer haveLinks = new WebMarkupContainer("haveLinks");
        motdDiv.add(haveLinks);
        
		//present the news data in a table
		final DataView<NewsItem> newsDataView = new DataView<NewsItem>("motd", motdProvider) {

			@Override
			public void populateItem(final Item item) {
				item.setOutputMarkupId(true);
				final NewsItem nItem = (NewsItem) item.getModelObject();
                
                if(logger.isDebugEnabled()) {
                	logger.debug(this + "populateItem()  item: " + item);
                }
                
                String itemType = nItem.getSourceType().getIdentifier();
                item.add(new Label("itemType", itemType));
                item.add(new Label("itemCount", Integer.toString(nItem.getItemCount())));
                item.add(new Label("entityReference", nItem.getEntityReference()));

                String siteTitle = nItem.getContext().getContextTitle();
				StringBuilder errorMessages = new StringBuilder();
				String title = FormattedText.processFormattedText(nItem.getTitle(), errorMessages  , true, true);
				if(errorMessages != null && errorMessages.length() > 0) {
					logger.warn("Error(s) encountered while processing newsItem title:\n" + errorMessages);
				}
                item.add(new ExternalLink("itemLink", "#", title));
                
                Image icon = new Image("icon");
                icon.add(new AttributeModifier("src", true, new AbstractReadOnlyModel(){

					@Override
					public Object getObject() {
						// TODO Auto-generated method stub
						return dashboardLogic.getEntityIconUrl(nItem.getSourceType().getIdentifier(), nItem.getSubtype());
					}
                	
                }));
                item.add(icon);
                String newsItemLabel = dashboardLogic.getString(nItem.getNewsTimeLabelKey(), "", itemType);
                if(newsItemLabel == null) {
                	newsItemLabel = "";
                }
				item.add(new Label("itemLabel", newsItemLabel));
                item.add(new ExternalLink("siteLink", nItem.getContext().getContextUrl(), siteTitle));
                Component timeLabel = new Label("newsTime", DateUtil.getNewsTimeString(nItem.getNewsTime()));
                timeLabel.add(new AttributeModifier("title", true, new AbstractReadOnlyModel(){

					@Override
					public Object getObject() {
						// TODO Auto-generated method stub
						return DateUtil.getFullDateString(nItem.getNewsTime());
					}
                	
                }));
				item.add(timeLabel );
                
	                AjaxLink<NewsLink> starringAction = new AjaxLink<NewsLink>("starringAction") {
	                	protected long newsItemId = nItem.getId();
	                	protected Component thisRow = item;
	                	
						@Override
						public void onClick(AjaxRequestTarget target) {
							logger.info("starringAction onClick() called -- star ");
							// need to keep one item
							logger.info(newsItemId);
							//logger.info(this.getModelObject());
							
							String sakaiUserId = sakaiProxy.getCurrentUserId();
							boolean success = dashboardLogic.keepNewsItem(sakaiUserId, newsItemId);
							
							// if success adjust UI, else report failure?
							if(success) {
								target.addComponent(MOTDPanel.this);
								//String javascript = "alert('success. (" + thisRow.getMarkupId() + ")');";
								//target.appendJavascript(javascript );
							}
							target.appendJavascript("resizeFrame('grow');");
						}
						
	                };
					Image starringActionIcon = new Image("starringActionIcon");
					starringActionIcon.add(new AttributeModifier("src", true, new AbstractReadOnlyModel(){

						@Override
						public Object getObject() {
							return "/dashboard-tool/css/img/star-inact.png";
						}
						
					}));
					starringActionIcon.add(new AttributeModifier("title", true, new AbstractReadOnlyModel(){

						@Override
						public Object getObject() {
							ResourceLoader rl = new ResourceLoader("dash_entity");
							return rl.getString("dash.star");
						}
						
					}));
					starringAction.add(starringActionIcon);
	                // starringAction.add(new Label("starringActionLabel", "Star"));
	                item.add(starringAction);

	                }
		};
       
        
        
        newsDataView.setItemReuseStrategy(new DefaultItemReuseStrategy());
        newsDataView.setItemsPerPage(pageSize);
        haveLinks.add(newsDataView);

        //add a pager to our table, only visible if we have more than 5 items
        motdDiv.add(new PagingNavigator("newsNavigator", newsDataView) {
        	
        	@Override
        	public boolean isVisible() {
        		if(motdProvider.size() > pageSize) {
        			return true;
        		}
        		return false;
        	}
        	
        	@Override
        	public void onBeforeRender() {
        		super.onBeforeRender();

                renderItemCounter(motdDiv, newsDataView);

        		//clear the feedback panel messages
        		//clearFeedback(feedbackPanel);
        	}
        	
        });
        
        WebMarkupContainer haveNoLinks = new WebMarkupContainer("haveNoLinks");
        motdDiv.add(haveNoLinks);
        
        String noNewsLinksLabel = null;
		noNewsLinksLabel = rl.getString("dash.news.nocurrent");
        haveNoLinks.add(new Label("message", noNewsLinksLabel));
        
        renderItemCounter(motdDiv, newsDataView);
        int itemCount = 0;
        if(newsDataView != null) {
        	itemCount = newsDataView.getItemCount();
        }
       
        if(itemCount > 0) {
        	// show the haveLinks
        	haveLinks.setVisible(true);
        	// hide the noNewsLinksDiv
        	haveNoLinks.setVisible(false);
        } else {
        	// show the noNewsLinksDiv
        	haveNoLinks.setVisible(true);
        	// hide the haveLinks
        	haveLinks.setVisible(false);
        }
		if(motdMode != MOTD_MODE_LIST) {
			motdDiv.setVisibilityAllowed(false);
			motdDiv.setVisible(false);
		}

	}
	
	/**
	 * @param rl
	 * @param motdDiv
	 * @param newsDataView
	 */
	protected void renderItemCounter(
			final WebMarkupContainer motdDiv,
			final DataView<NewsItem> newsDataView) {
		
		ResourceLoader rl = new ResourceLoader("dash_entity");
				
		if(motdCountId != null) {
			Iterator itx = motdDiv.iterator();
			while(itx.hasNext()) {
				Component child = (Component) itx.next();
				if(motdCountId.equals(child.getId())) {
					motdDiv.remove(child);
					break;
				}
			}
		}

		int itemCount = 0;
		String pagerStatus = "";
		if(newsDataView != null) {
		    int first = 0;
		    int last = 0;
			itemCount = newsDataView.getItemCount();
		    int pageSize = newsDataView.getItemsPerPage();
			if(itemCount > pageSize) {
				int page = newsDataView.getCurrentPage();
				first = page * pageSize + 1;
				last = Math.min(itemCount, (page + 1) * pageSize);
				if(first == last) {
		    		pagerStatus = rl.getFormattedMessage("dash.news.linksCount2", new Object[]{new Integer(first), new Integer(itemCount)});
				} else {
		    		pagerStatus = rl.getFormattedMessage("dash.news.linksCount3", new Object[]{new Integer(first), new Integer(last), new Integer(itemCount)});
				}
			} else if(itemCount > 1) {
				pagerStatus = rl.getFormattedMessage("dash.news.linksCount3", new Object[]{new Integer(1), new Integer(itemCount), new Integer(itemCount)});
			} else if(itemCount > 0) {
				pagerStatus = rl.getString("dash.news.linksCount1");
			} else {
				pagerStatus = rl.getString("dash.news.linksCount0");
			}
		} 
		Label motdCount = new Label("motdCount", pagerStatus);
		motdCount.setOutputMarkupId(true);
		// add the count to the motdDiv
		motdDiv.add(motdCount);
		
		motdCountId = motdCount.getId();
	}

	/**
	 * DataProvider to manage our list
	 * 
	 */
	private class NewsLinksDataProvider implements IDataProvider<NewsItem> {
	   
		protected List<NewsItem> motd;
		protected String newsTabId = null;
		
		public NewsLinksDataProvider() {
			super();
		}
				
		public void setNewsTab(String newsTabId) {
			if(this.newsTabId == null || ! this.newsTabId.equals(newsTabId)) {
				// force refresh of dataProvider
				this.motd = null;
			}
			this.newsTabId = newsTabId;
			
		}

		private List<NewsItem> getData() {
			if(motd == null) {
				motd = dashboardLogic.getMOTD();
			}
			
			if(logger.isDebugEnabled()) {
				logger.debug("NewsLinksDataProvider selectedNewsTab=" + this.newsTabId + " motd=" + motd);
			}
			if(motd == null) {
				logger.warn("Error getting news items");
				return new ArrayList<NewsItem>();
			}
			return motd;
		}
		
		public Iterator<NewsItem> iterator(int first, int count){
			return getData().subList(first, first + count).iterator();
		}
		
		public int size(){
			return getData().size();
		}
		
		public IModel<NewsItem> model(NewsItem object){
			return new DetachableNewsLinkModel(object);
		}

		public void detach(){
			motd = null;
		}
	}

	/**
	 * Detachable model to wrap a NewsLink
	 * 
	 */
	private class DetachableNewsLinkModel extends LoadableDetachableModel<NewsItem>{

		private NewsItem newsLink = null;
		
		/**
		 * @param m
		 */
		public DetachableNewsLinkModel(NewsItem t){
			this.newsLink  = new NewsItem(t); 
		}
		
		/**
		 * @param id
		 */
//		public DetachableNewsLinkModel(long id){
//			this.id = id;
//		}
		
		/**
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			if(this.newsLink == null || this.newsLink.getId() == null) {
				return Long.valueOf(0L).hashCode();
			}
			return Long.valueOf(this.newsLink.getId()).hashCode();
		}
		
		/**
		 * used for dataview with ReuseIfModelsEqualStrategy item reuse strategy
		 * 
		 * @see org.apache.wicket.markup.repeater.ReuseIfModelsEqualStrategy
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(final Object obj){
			if (obj == this){
				return true;
			}
			else if (obj == null){
				return false;
			} 
			else if(this.newsLink == null) {
				return false;
			}
			else if (obj instanceof DetachableNewsLinkModel) {
				DetachableNewsLinkModel other = (DetachableNewsLinkModel)obj;
				if(other.newsLink == null) {
					return false;
				}
				return other.newsLink.getId() == this.newsLink.getId();
			}
			return false;
		}
		
		/**
		 * @see org.apache.wicket.model.LoadableDetachableModel#load()
		 */
		protected NewsItem load(){
			
			// get the news item
			return new NewsItem(this.newsLink);
		}
	}

}
