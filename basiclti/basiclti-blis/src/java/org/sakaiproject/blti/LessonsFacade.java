/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.blti;

import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Properties;
import java.util.UUID;

import org.sakaiproject.component.cover.ComponentManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageComment;
import org.sakaiproject.lessonbuildertool.SimplePageGroup;
import org.sakaiproject.lessonbuildertool.SimplePageItem;

import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.util.foorm.SakaiFoorm;

public class LessonsFacade {

	private static Log M_log = LogFactory.getLog(LessonsFacade.class);

    protected static SakaiFoorm foorm = new SakaiFoorm();

    protected static LTIService ltiService = null;
    protected static SimplePageToolDao simplePageToolDao = null;

	public static void init() {
        if ( ltiService == null ) ltiService = (LTIService) ComponentManager.get("org.sakaiproject.lti.api.LTIService");
        if ( simplePageToolDao == null ) simplePageToolDao = (SimplePageToolDao)ComponentManager.get(SimplePageToolDao.class);
    }

    public static List<SimplePageItem> findItemsInSite(String context_id)
    {
        return simplePageToolDao.findItemsInSite(context_id);
    }

    public static List<SimplePageItem> findItemsOnPage(Long pageNum)
    {
        return simplePageToolDao.findItemsOnPage(pageNum);
    }

    public static SimplePageItem findItem(long pageId)
    {
        return simplePageToolDao.findItem(pageId);
    }

	public static SimplePageItem findFolder(List<SimplePageItem> sitePages, Long folderId, 
		 List<Long> structureList, int depth)
	{
        if ( folderId == null ) return null;
		if ( depth > 10 ) return null;
		for (SimplePageItem i : sitePages) {
			if ( structureList.size() > 100 ) return null;
			if (i.getType() != SimplePageItem.PAGE) continue;
            // System.out.println("d="+depth+" o="+structureList.size()+" Id="+i.getId()+" SakaiId="+i.getSakaiId()+" title="+i.getName());
			Long pageNum = Long.valueOf(i.getSakaiId());
            if ( folderId.equals(pageNum) ) return i;
			structureList.add(i.getId());

			List<SimplePageItem> items = simplePageToolDao.findItemsOnPage(pageNum);
            SimplePageItem retval = findFolder(items, folderId, structureList, depth+1);
            if ( retval != null ) return retval;
		}
        return null;
	}

	public static SimplePageItem addLessonsFolder(SimplePageItem thePage, String nameStr, int startPos)
    {
		// System.out.println("item="+ thePage.getName()+" id="+ thePage.getId()+" sakaiId="+ thePage.getSakaiId());
		Long parent = Long.valueOf(thePage.getSakaiId());
		// System.out.println("Parent="+parent);

		SimplePage actualPage = simplePageToolDao.getPage(parent);
		// System.out.println("Simple Page="+actualPage);
		Long topParent = actualPage.getTopParent();
		if ( topParent == null ) topParent = parent;
		// System.out.println("topParent="+topParent);
		// System.out.println("toolId="+actualPage.getToolId()+" siteId="+actualPage.getSiteId());

		SimplePage subPage = simplePageToolDao.makePage(actualPage.getToolId(), actualPage.getSiteId(), nameStr, parent, topParent);
		List<String>elist = new ArrayList<String>();
		simplePageToolDao.saveItem(subPage,  elist, "ERROR WAS HERE", false);
		System.out.println("Page Saved "+elist);

		// System.out.println("subPage="+subPage);
		String selectedEntity = String.valueOf(subPage.getPageId());
		// System.out.println("selectedEntity="+selectedEntity);

		SimplePageItem subPageItem = simplePageToolDao.makeItem(parent, startPos, SimplePageItem.PAGE, Long.toString(subPage.getPageId()), nameStr);
		subPageItem.setFormat("");
		elist = new ArrayList<String>();
		simplePageToolDao.saveItem(subPageItem,  elist, "ERROR WAS HERE", false);
		System.out.println("Item Saved "+elist);
		// System.out.println("subItem = "+subPageItem);
		return subPageItem;
	}

    public static String doImportTool(String siteId, String launchUrl, String bltiTitle, String strXml, String custom)
    {
		if ( ltiService == null ) return null;

		String toolUrl = launchUrl;
		int pos = toolUrl.indexOf("?");
		if ( pos > 0 ) {
			toolUrl = toolUrl.substring(0, pos);
		}

		String toolName = toolUrl;
		try {
			URL launch = new URL(launchUrl);
			toolName = launch.getProtocol() + "://" + launch.getAuthority();
		} catch ( Exception e ) {
			toolName = toolUrl;
		}

		Map<String,Object> theTool = null;
		List<Map<String,Object>> tools = ltiService.getToolsDao(null,null,0,0,siteId);
        String lastLaunch = "";
		for ( Map<String,Object> tool : tools ) {
			String toolLaunch = (String) tool.get(LTIService.LTI_LAUNCH);
			if ( toolUrl.startsWith(toolLaunch) && toolLaunch.length() > lastLaunch.length()) {
				theTool = tool;
                lastLaunch = toolLaunch;
				break;
			}
		}

		if ( theTool == null ) {
            M_log.debug("Inserting tool - "+toolUrl);
			Properties props = new Properties ();
			props.setProperty(LTIService.LTI_LAUNCH,toolUrl);
			props.setProperty(LTIService.LTI_TITLE, toolName);
			props.setProperty(LTIService.LTI_PAGETITLE, toolName);
			props.setProperty(LTIService.LTI_CONSUMERKEY, LTIService.LTI_SECRET_INCOMPLETE);
			props.setProperty(LTIService.LTI_SECRET, LTIService.LTI_SECRET_INCOMPLETE);

			props.setProperty(LTIService.LTI_ALLOWCUSTOM, "1");
			props.setProperty(LTIService.LTI_SENDNAME, "1");
			props.setProperty(LTIService.LTI_SENDEMAILADDR, "1");
			props.setProperty(LTIService.LTI_ALLOWTITLE, "1");
			props.setProperty(LTIService.LTI_ALLOWPAGETITLE, "1");
			props.setProperty(LTIService.LTI_ALLOWLAUNCH, "1");
			props.setProperty(LTIService.LTI_ALLOWOUTCOMES, "1");
			props.setProperty(LTIService.LTI_ALLOWROSTER, "1");

			props.setProperty(LTIService.LTI_SITE_ID,siteId);

            // Go ahead and throw up...
            Object result = ltiService.insertToolDao(props, siteId);

			if ( result instanceof String ) {
				M_log.error("Could not insert tool - "+result);
			}
			if ( result instanceof Long ) theTool = ltiService.getToolDao((Long) result, siteId);
		}
	
		Map<String,Object> theContent = null;
		Long contentKey = null;
		if ( theTool != null ) {
			Properties props = new Properties ();
			props.setProperty(LTIService.LTI_TOOL_ID,foorm.getLong(theTool.get(LTIService.LTI_ID)).toString());
            props.setProperty(LTIService.LTI_PLACEMENTSECRET, UUID.randomUUID().toString());
			props.setProperty(LTIService.LTI_TITLE, bltiTitle);
			props.setProperty(LTIService.LTI_PAGETITLE, bltiTitle);
			props.setProperty(LTIService.LTI_LAUNCH,launchUrl);
			if ( strXml != null) props.setProperty(LTIService.LTI_XMLIMPORT,strXml);
			if ( custom != null ) props.setProperty(LTIService.LTI_CUSTOM,custom);

            // Throw upwards..
            Object result = ltiService.insertContentDao(props, siteId);
			if ( result instanceof String ) {
				M_log.error("Could not insert content - "+result);
			} else {
				M_log.debug("Adding LTI tool "+result);
			}
			if ( result instanceof Long ) theContent = ltiService.getContentDao((Long) result, siteId);
		}
	
		String sakaiId = null;
		if ( theContent != null ) {
			sakaiId = "/blti/" + theContent.get(LTIService.LTI_ID);
		}
		return sakaiId;
	}

	public static boolean addLessonsLaunch(SimplePageItem thePage, String sakaiId, String nameStr, int startPos) 
	{
            M_log.debug("Adding LTI content item "+sakaiId);
			Long pageNum = Long.valueOf(thePage.getSakaiId());
            M_log.debug("Page ="+thePage.getSakaiId()+" title="+thePage.getName());

			SimplePageItem item = simplePageToolDao.makeItem(thePage.getPageId(), startPos, SimplePageItem.BLTI, sakaiId, nameStr);
			item.setHeight(""); // default depends upon format, so it's supplied at runtime
			item.setPageId(pageNum.longValue());

			List<String>elist = new ArrayList<String>();
            simplePageToolDao.saveItem(item,  elist, "ERROR WAS HERE", false);
			M_log.debug("Saved "+elist);
			return true;
	}

}
