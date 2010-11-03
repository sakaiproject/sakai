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

import org.radeox.api.macro.MacroParameter;
import org.radeox.macro.BaseMacro;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;

import uk.ac.cam.caret.sakai.rwiki.component.Messages;
import uk.ac.cam.caret.sakai.rwiki.component.radeox.service.impl.SpecializedRenderContext;

/**
 * This is a reimplementation of the LinkMacro but made aware of the sakai://
 * and worksite:// url formats
 * 
 * @author andrew
 */
public class SectionsMacro extends BaseMacro
{


	public String[] getParamDescription()
	{
		return new String[] {
			Messages.getString("SectionsMacro.0"), //$NON-NLS-1$
			Messages.getString("SectionsMacro.1") }; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.radeox.macro.Macro#getDescription()
	 */
	public String getDescription()
	{
		return Messages.getString("SectionsMacro.2"); //$NON-NLS-1$
	}

	public String getName()
	{
		return "sakai-sections"; //$NON-NLS-1$
	}

	public void execute(Writer writer, MacroParameter params)
			throws IllegalArgumentException, IOException
	{

		SpecializedRenderContext context = (SpecializedRenderContext) params
				.getContext();

		String useids = params.get("useids", 0); //$NON-NLS-1$

		String siteId = context.getSiteId();
		
		Collection groups = null;
		Site site;
		try
		{
			site = SiteService.getSite(siteId);
		}
		catch (IdUnusedException e)
		{
			throw new IllegalArgumentException(Messages.getString("SectionsMacro.5")+ siteId + " : "+e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
		}
		//Site can't be null because the not found would have thrown an exception
		groups = site.getGroups();

		for (Iterator is = groups.iterator(); is.hasNext();)
		{
			Group group = (Group) is.next();
			String pageName = ""; //$NON-NLS-1$

			if ("true".equals(useids)) //$NON-NLS-1$
			{
				pageName = group.getId() + "/Home"; //$NON-NLS-1$
			}
			else
			{
				pageName = group.getReference() + "/"; //$NON-NLS-1$
				pageName += "section/" + group.getTitle() + "/Home"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			writer.write("\n"); //$NON-NLS-1$
			writer.write("* [ Section: "); //$NON-NLS-1$
			writer.write(group.getTitle());
			writer.write("|"); //$NON-NLS-1$
			writer.write(pageName);
			writer.write("]"); //$NON-NLS-1$
		}
		writer.write("\n"); //$NON-NLS-1$
		return;
	}
}
