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

package uk.ac.cam.caret.sakai.rwiki.tool.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.sakaiproject.entity.api.Entity;

import uk.ac.cam.caret.sakai.rwiki.service.api.EntityHandler;
import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
import uk.ac.cam.caret.sakai.rwiki.utils.XmlEscaper;

/**
 * Bean that uses the object service and a current rwikiObject to find the
 * referencing pages and the pages referenced by the current rwikiObject.
 * 
 * @author andrew
 */
public class ReferencesBean
{

	private RWikiObject rwikiObject;

	private RWikiObjectService objectService;

	private String defaultSpace;

	/**
	 * Create new ReferencesBean.
	 */
	public ReferencesBean(RWikiObject rwikiObject,
			RWikiObjectService objectService, String localSpace)
	{
		this.rwikiObject = rwikiObject;
		this.objectService = objectService;
		this.defaultSpace = localSpace;
	}

	/**
	 * Get links to the pages referenced by the current RWikiObject.
	 * 
	 * @return list of xhtml links
	 */
	public List getReferencedPageLinks()
	{
		String referenced = rwikiObject.getReferenced();
		String[] references = referenced.split("::");
		List referenceLinks = new ArrayList(references.length);
		TreeMap <String, String> tmLinks = new TreeMap <String, String> ();
		ViewBean vb = new ViewBean(rwikiObject.getName(), defaultSpace);
		vb.setLocalSpace(vb.getPageSpace());
		for (int i = 0; i < references.length; i++)
		{
			String pageName = references[i];
			if (pageName != null && !"".equals(pageName))
			{
				vb.setPageName(pageName);
				/*
				String link = "<a href=\""
						+ XmlEscaper.xmlEscape(vb.getViewUrl()) + "\">"
						+ XmlEscaper.xmlEscape(vb.getLocalName()) + "</a>";
				referenceLinks.add(link);
				*/
				tmLinks.put(vb.getLocalName(),vb.getViewUrl());
			}
		}
		
		Iterator<Entry<String, String>> tmiter = tmLinks.entrySet().iterator();
		while (tmiter.hasNext()) {
			Entry<String, String> entry = tmiter.next();
			String objLocalName = entry.getKey();
			String objViewUrl = entry.getValue();
			String link = "<a href=\""
				+ XmlEscaper.xmlEscape(objViewUrl) + "\">"
				+ XmlEscaper.xmlEscape(objLocalName) + "</a>";
			referenceLinks.add(link);
		}

		return referenceLinks;
	}

	public List getFeedsLinks()
	{
		List feedsLinks = new ArrayList();
		Map m = objectService.getHandlers();
		for (Iterator<Entry<String, EntityHandler>> ii = m.entrySet().iterator(); ii.hasNext();)
		{
			Entry<String, EntityHandler> entry = ii.next();
			EntityHandler eh = entry.getValue();
			Entity e = objectService.getEntity(rwikiObject);
			String displayLink = eh.getHTML(e);
			if (displayLink != null)
			{
				feedsLinks.add(displayLink);
			}
		}
		return feedsLinks;
	}

	/**
	 * Gets links to the pages referencing the current RWikiObject.
	 * 
	 * @return list of xhtml links
	 */
	public List getReferencingPageLinks()
	{
		List pages = objectService.findReferencingPages(rwikiObject.getName());
		List referencingLinks = new ArrayList(pages.size());
		ViewBean vb = new ViewBean(rwikiObject.getName(), defaultSpace);
		vb.setLocalSpace(vb.getPageSpace());
		for (Iterator it = pages.iterator(); it.hasNext();)
		{
			vb.setPageName((String) it.next());
			String link = "<a href=\"" + XmlEscaper.xmlEscape(vb.getViewUrl())
					+ "\">" + XmlEscaper.xmlEscape(vb.getLocalName()) + "</a>";
			referencingLinks.add(link);
		}
		return referencingLinks;
	}

}
