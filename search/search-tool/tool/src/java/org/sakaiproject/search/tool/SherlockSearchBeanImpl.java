/**
 * 
 */
package org.sakaiproject.search.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.tool.api.SherlockSearchBean;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.FormattedText;

/**
 * @author ieb
 */
public class SherlockSearchBeanImpl implements SherlockSearchBean
{

	private static final String IMAGE_ICON = "/images/sherlock.gif";

	static final String UPDATE_URL = "/sakai.src";

	static final String UPDATE_IMAGE = "/sakai.gif";

	private SiteService siteService;

	private String placementId;

	private String siteId;

	private Site currentSite;

	private String baseURL;
	
	private String portalBaseURL;

	private ServletContext context;

	public SherlockSearchBeanImpl(HttpServletRequest request, ServletContext context,
			SearchService searchService, SiteService siteService,
			ToolManager toolManager) throws IdUnusedException
	{
		this.context = context;
		this.siteService = siteService;
		this.placementId = toolManager.getCurrentPlacement().getId();
		this.siteId = toolManager.getCurrentPlacement().getContext();
		this.currentSite = this.siteService.getSite(this.siteId);
		baseURL = getBaseURL();
		portalBaseURL = getPortalBaseURL();
	}

	private String getBaseURL()
	{
		return ServerConfigurationService.getPortalUrl() + "/tool/"
				+ placementId;
	}
	private String getPortalBaseURL()
	{
		return ServerConfigurationService.getPortalUrl() + "/directtool/"
				+ placementId;
	}

	public String getSearchURL()
	{
		return portalBaseURL + "/index";
	}

	public String getSiteName()
	{
		return FormattedText.escapeHtml(currentSite.getTitle(),false);
	}

	public String getUpdateIcon()
	{
		return FormattedText.escapeHtml(baseURL + UPDATE_IMAGE,false);
	}

	public String getUpdateURL()
	{
		return FormattedText.escapeHtml(baseURL + UPDATE_URL,false);
	}

	public void sendIcon(HttpServletResponse response)
	{
		String realPath = context.getRealPath(IMAGE_ICON);
		File f = new File(realPath);
		int nbytes = (int) f.length();
		response.setContentLength(nbytes);
		InputStream is = null;
		try
		{
			is = new FileInputStream(f);
			byte[] b = new byte[4096];
			OutputStream out = response.getOutputStream();
			for (; nbytes > 0;)
			{
				int nb = is.read(b);
				if (nb > 0)
				{
					out.write(b, 0, nb);
					nbytes -= nb;
				}
				else
				{
					Thread.yield();
				}
			}
		}
		catch (IOException e)
		{

			e.printStackTrace();
		}
		finally
		{
			try
			{
				is.close();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();

			}
		}

	}
	public String getSystemName()
	{
		return FormattedText.escapeHtml(ServerConfigurationService.getString("ui.service","Sakai"),false);
	}



}
