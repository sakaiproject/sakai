/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package uk.ac.cam.caret.sakai.rwiki.tool.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.ResourceLoader;

import uk.ac.cam.caret.sakai.rwiki.service.api.PageLinkRenderer;
import uk.ac.cam.caret.sakai.rwiki.service.api.RenderService;
import uk.ac.cam.caret.sakai.rwiki.service.api.dao.RWikiCurrentObjectDao;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiCurrentObject;
import uk.ac.cam.caret.sakai.rwiki.service.exception.PermissionException;
import uk.ac.cam.caret.sakai.rwiki.tool.api.PopulateService;
import uk.ac.cam.caret.sakai.rwiki.utils.NameHelper;

/**
 * @author andrew
 */
@Slf4j
public class PopulateServiceImpl implements PopulateService
{

	private List seedPages;

	private RWikiCurrentObjectDao dao;

	private RenderService renderService = null;

	private SiteService siteService = null;

	public void init() throws IOException
	{
		ComponentManager cm = org.sakaiproject.component.cover.ComponentManager
				.getInstance();

		renderService = (RenderService) load(cm, RenderService.class.getName());
		siteService = (SiteService) load(cm, SiteService.class.getName());

		for (Iterator i = seedPages.iterator(); i.hasNext();)
		{
			RWikiCurrentObject seed = (RWikiCurrentObject) i.next();
			if (seed.getSource().startsWith("bundle:"))
			{
				String[] source = seed.getSource().split(":");
				ResourceLoader rl = new ResourceLoader(source[1]);
				seed.setContent(rl.getString(source[2]));
			}
			else
			{
				BufferedReader br = null;
				StringBuffer sb = new StringBuffer();
				try {
					br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(seed.getSource()),
					"UTF-8"));
					char[] c = new char[2048];
					
					for (int ic = br.read(c); ic >= 0; ic = br.read(c))
					{
						if (ic == 0)
							Thread.yield();
						else
							sb.append(c, 0, ic);
					}
				}
				finally {
					br.close();
				}
				seed.setContent(sb.toString());
			}
		}
	}

	private Object load(ComponentManager cm, String name)
	{
		Object o = cm.get(name);
		if (o == null)
		{
			log.error("Cant find Spring component named " + name);
		}
		return o;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.PopulateService#populateRealm(java.lang.String,
	 *      java.lang.String)
	 */
	// SAK-2514
	public void populateRealm(String user, String space, String group)
			throws PermissionException
	{
		String owner = user;
		Site s = null;
		try
		{
			s = siteService.getSite(ToolManager.getCurrentPlacement()
					.getContext());
			owner = s.getCreatedBy().getId();
		}
		catch (Exception e)
		{
			log
					.warn("Cant find who created this site, defaulting to current user for prepopulate ownership :"
							+ owner);
		}
		if (s == null)
		{
			log
					.error("Cant Locate current site, will populate only global pages with no restrictions");
		}
		if (log.isDebugEnabled())
		{
			log.debug("Populating space: " + space);
		}

		for (Iterator i = seedPages.iterator(); i.hasNext();)
		{

			RWikiCurrentObject seed = (RWikiCurrentObject) i.next();
			List targetTypes = seed.getTargetSiteTypes();
			if (ignoreSeedPage(s, targetTypes))
			{
				log.debug("Ignoring Seed page " + seed.getName());
				continue;
			}

			String name = NameHelper.globaliseName(seed.getName(), space);
			log.debug("Populating Space with " + seed.getName());
			if (dao.findByGlobalName(name) == null)
			{
				if (log.isDebugEnabled())
				{
					log.debug("Creating Page: " + name);
				}
				log.debug("Creating Page :" + name);

				RWikiCurrentObject rwo = dao.createRWikiObject(name, space);
				seed.copyTo(rwo);
				// SAK-2513

				log.debug("Populate with Owner " + owner);
				rwo.setUser(owner);
				rwo.setOwner(owner);
				updateReferences(rwo, space);
				rwo.setName(name);
				rwo.setRealm(group);
				dao.update(rwo, null);
				log.debug("Page Created ");
				//Post an event
				EventTrackingService.post(EventTrackingService.newEvent("wiki.new","/wiki" + rwo.getName() + ".", true));
				
			}
			else
			{
				log.debug("Page Already exists ");
				// This is the fast exit route, but if the list of seed pages
				// changes, and there are new ones before the olde ones
				// then the new ones will be added to the space.
				break;
			}
		}
	}

	/**
	 * returns true if the the page should be ignored
	 * 
	 * @param s
	 *        the site
	 * @param targetTypes
	 *        a list of lines that specify which site matches
	 * @return
	 */
	private boolean ignoreSeedPage(Site s, List targetTypes)
	{
		if (targetTypes == null || targetTypes.size() == 0) return false;
		if (s == null)
		{
			// if all the types are not, then dont ignore
			for (Iterator i = targetTypes.iterator(); i.hasNext();)
			{
				String ttype = (String) i.next();
				String[] ttypeGroup = ttype.split(",");
				for (int j = 0; j < ttypeGroup.length; j++)
				{
					if (!ttypeGroup[j].startsWith("!")) return true;
				}
			}
			return false;
		}
		else
		{
			String type = s.getType();
			if (type == null) type = "";
			type = type.toLowerCase();
			log.debug("Checking Site " + type);
			// each line is anded together and each line is ored with other
			// lines
			for (Iterator i = targetTypes.iterator(); i.hasNext();)
			{
				String ttype = (String) i.next();
				String[] ttypeGroup = ttype.split(",");
				boolean bline = true;
				for (int j = 0; j < ttypeGroup.length; j++)
				{
					if (ttypeGroup[j].startsWith("!"))
					{
						bline = bline
								& (!type.startsWith(ttype.substring(1)
										.toLowerCase()));
						log.debug("Checking not " + ttypeGroup[j] + " was "
								+ bline);
					}
					else
					{
						bline = bline & (type.startsWith(ttype.toLowerCase()));
						log
								.debug("Checking " + ttypeGroup[j] + " was "
										+ bline);
					}
				}
				if (bline) return false;
			}
			return true;
		}

	}

	// SAK-2470
	private void updateReferences(RWikiCurrentObject rwo, String space)
	{

		// render to get a list of links
		final HashSet referenced = new HashSet();
		final String currentRealm = rwo.getRealm();

		PageLinkRenderer plr = new PageLinkRenderer()
		{
			public void appendLink(StringBuffer buffer, String name, String view)
			{
				referenced.add(NameHelper.globaliseName(name, currentRealm));
			}

			public void appendLink(StringBuffer buffer, String name,
					String view, String anchor)
			{
				referenced.add(NameHelper.globaliseName(name, currentRealm));
			}

			public void appendCreateLink(StringBuffer buffer, String name,
					String view)
			{
				referenced.add(NameHelper.globaliseName(name, currentRealm));
			}

			public void appendLink(StringBuffer buffer, String name,
					String view, String anchor, boolean autoGenerated)
			{
				if (!autoGenerated)
				{
					this.appendLink(buffer, name, view, anchor);
				}
			}

			public boolean isCachable()
			{
				return false; // should not cache this render op
			}

			public boolean canUseCache()
			{
				return false;
			}

			public void setCachable(boolean cachable)
			{
				// do nothing
			}

			public void setUseCache(boolean b)
			{
				// do nothing
			}

		};

		renderService.renderPage(rwo, space, plr);

		// process the references
		StringBuffer sb = new StringBuffer();
		Iterator i = referenced.iterator();
		while (i.hasNext())
		{
			sb.append("::").append(i.next());
		}
		sb.append("::");
		rwo.setReferenced(sb.toString());

	}

	public List getSeedPages()
	{
		return seedPages;
	}

	public void setSeedPages(List seedPages)
	{
		this.seedPages = seedPages;
	}

	public RWikiCurrentObjectDao getRWikiCurrentObjectDao()
	{
		return dao;
	}

	public void setRWikiCurrentObjectDao(RWikiCurrentObjectDao dao)
	{
		this.dao = dao;
	}

}
