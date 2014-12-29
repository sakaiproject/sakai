package org.sakaiproject.news.tool.entityproviders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.exception.EntityNotFoundException;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.news.api.NewsConnectionException;
import org.sakaiproject.news.api.NewsFormatException;
import org.sakaiproject.news.api.NewsService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;

/**
 * Entity provider for the News tool
 */
@CommonsLog
public class NewsEntityProvider extends AbstractEntityProvider implements
		EntityProvider, AutoRegisterEntityProvider, ActionsExecutable,
		Outputable, Describeable
{

	private static final String SAKAI_NEWS_TOOL_ID = "sakai.news";

	private static final String NEWS_CHANNEL_URL_PROP = "channel-url";

	public final static String ENTITY_PREFIX = "news";

	@Override
	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}

	/**
	 * site/siteId
	 */
	@EntityCustomAction(action = "site", viewKey = EntityView.VIEW_LIST)
	public List<NewsToolInfo> getNewsForSite(EntityView view) {

		// get siteId
		String siteId = view.getPathSegment(2);

		if(log.isDebugEnabled()) {
			log.debug("news for site " + siteId);
		}

		// check siteId supplied
		if (StringUtils.isBlank(siteId)) {
			throw new IllegalArgumentException(
					"siteId must be set in order to get the news feeds for a site, via the URL /news/site/siteId");
		}

		//user being logged in and having access to the site is handled in the API
		Site site;
		try {
			site = siteService.getSiteVisit(siteId);
		} catch (IdUnusedException e) {
			throw new EntityNotFoundException("Invalid siteId: " + siteId, siteId);
		} catch (PermissionException e) {
			throw new EntityNotFoundException("No access to site: " + siteId, siteId);
		}

		Collection<ToolConfiguration> newsTools = site.getTools(SAKAI_NEWS_TOOL_ID);

		List<NewsToolInfo> result = new ArrayList<NewsToolInfo>();
		for (ToolConfiguration t : newsTools) {
			NewsToolInfo info = new NewsToolInfo(t.getId(), t.getTitle(), t.getConfig().getProperty(NEWS_CHANNEL_URL_PROP));
			result.add(info);
		}

		return result;
	}

	/**
	 * channel/toolId
	 * 
	 * @throws NewsFormatException
	 * @throws NewsConnectionException
	 */
	@EntityCustomAction(action = "channel", viewKey = EntityView.VIEW_LIST)
	public List<?> getNewsItems(EntityView view) throws NewsConnectionException, NewsFormatException {

		// get toolId
		String toolId = view.getPathSegment(2);

		//user being logged in and having access to the site is to be handled in the API

		// get channel from the tool id
		ToolConfiguration toolConfig = siteService.findTool(toolId);
		if (toolConfig == null) {
			throw new EntityNotFoundException("Invalid newsId: " + toolId, toolId);
		}

		String channel = toolConfig.getConfig().getProperty( NEWS_CHANNEL_URL_PROP);

		if(log.isDebugEnabled()){
			log.debug("tool " + toolId + ", page title = " + toolConfig.getTitle() + ", channel " + channel);
		}

		return newsService.getNewsitems(channel);
	}

	@Override
	public String[] getHandledOutputFormats() {
		return new String[] { Formats.XML, Formats.JSON};
	}

	@Setter
	private NewsService newsService;

	@Setter
	private SiteService siteService;

	@AllArgsConstructor
	public static class NewsToolInfo {
		@Getter
		private String toolId;
		@Getter
		private String title;
		@Getter
		private String channelURL;

	}

}
