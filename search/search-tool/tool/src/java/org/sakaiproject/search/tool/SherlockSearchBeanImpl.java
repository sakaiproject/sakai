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
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.ToolManager;

/**
 * @author ieb
 */
public class SherlockSearchBeanImpl implements SherlockSearchBean
{

	private static final String IMAGE_ICON = "/images/sherlock.gif";

	private static final String UPDATE_URL = "/sakai.scr";

	private static final String UPDATE_IMAGE = "/sakai.gif";

	private HttpServletRequest request;

	private SearchService searchService;

	private SiteService siteService;

	private Placement placement;

	private String placementId;

	private String toolId;

	private String siteId;

	private Site currentSite;

	private String baseURL;

	private ServletContext context;

	public SherlockSearchBeanImpl(HttpServletRequest request, ServletContext context,
			SearchService searchService, SiteService siteService,
			ToolManager toolManager) throws IdUnusedException
	{
		this.context = context;
		this.request = request;
		this.searchService = searchService;
		this.siteService = siteService;
		this.placement = toolManager.getCurrentPlacement();
		this.placementId = toolManager.getCurrentPlacement().getId();
		this.toolId = toolManager.getCurrentTool().getId();
		this.siteId = toolManager.getCurrentPlacement().getContext();
		this.currentSite = this.siteService.getSite(this.siteId);
		String siteCheck = currentSite.getReference();
		baseURL = getBaseURL();
	}

	private String getBaseURL()
	{
		return ServerConfigurationService.getPortalUrl() + "/directtool/"
				+ placementId;
	}

	public String getSearchURL()
	{
		return baseURL + "/index";
	}

	public String getSiteName()
	{
		return currentSite.getTitle();
	}

	public String getUpdateIcon()
	{
		return baseURL + UPDATE_IMAGE;
	}

	public String getUpdateURL()
	{
		return baseURL + UPDATE_URL;
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

			}
		}

	}
	public String getSystemName()
	{
		return ServerConfigurationService.getString("ui.service","Sakai");
	}



}
