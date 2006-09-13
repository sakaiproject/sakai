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

package uk.ac.cam.caret.sakai.rwiki.component.macros;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;

import org.radeox.api.engine.RenderEngine;
import org.radeox.macro.BaseMacro;
import org.radeox.macro.parameter.MacroParameter;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;

import uk.ac.cam.caret.sakai.rwiki.component.radeox.service.impl.SpecializedRenderContext;

/**
 * This is a reimplementation of the LinkMacro but made aware of the sakai://
 * and worksite:// url formats
 * 
 * @author andrew
 */
public class SectionsMacro extends BaseMacro
{
	private static String[] paramDescription = {
			"1,useids: (optional) if true will generate with ID's otherwise will use names, names it the default ",
			"Remember if using positional parameters, you must include dummies for the optional parameters" };

	private static String description = "Generate a list of links that point to section subsites";

	public String[] getParamDescription()
	{
		return paramDescription;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.radeox.macro.Macro#getDescription()
	 */
	public String getDescription()
	{
		return description;
	}

	public String getName()
	{
		return "sakai-sections";
	}

	public void execute(Writer writer, MacroParameter params)
			throws IllegalArgumentException, IOException
	{

		SpecializedRenderContext context = (SpecializedRenderContext) params
				.getContext();
		RenderEngine engine = context.getRenderEngine();

		String useids = params.get("useids", 0);

		String siteId = context.getSiteId();
		
		Collection groups = null;
		Site site;
		try
		{
			site = SiteService.getSite(siteId);
		}
		catch (IdUnusedException e)
		{
			throw new IllegalArgumentException("Invalid Site Id "+ siteId + " : "+e.getMessage());
		}
		groups = site.getGroups();

		for (Iterator is = groups.iterator(); is.hasNext();)
		{
			Group group = (Group) is.next();
			String pageName = "";

			if ("true".equals(useids))
			{
				pageName = group.getId() + "/Home";
			}
			else
			{
				if (site != null)
				{
					pageName = group.getReference() + "/";
				}
				pageName += "section/" + group.getTitle() + "/Home";
			}
			writer.write("\n");
			writer.write("* [ Section: ");
			writer.write(group.getTitle());
			writer.write("|");
			writer.write(pageName);
			writer.write("]");
		}
		writer.write("\n");
		return;
	}
}
