/**********************************************************************************
 *
 * $Header$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2005 University of Cambridge
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 * 
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
import org.sakaiproject.tool.cover.ToolManager;

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

		String siteId = ToolManager.getCurrentPlacement().getContext();
		Site s = null;
		try
		{
			s = SiteService.getSite(siteId);
		}
		catch (Exception ex)
		{

		}

		Collection groups = null;
		Site site;
		try
		{
			site = SiteService.getSite(siteId);
		}
		catch (IdUnusedException e)
		{
			throw new IllegalArgumentException("Invalid Site Id "+e.getMessage());
		}
		groups = site.getGroups();

		for (Iterator is = groups.iterator(); is.hasNext();)
		{
			Group g = (Group) is.next();
			String pageName = "";

			if ("true".equals(useids))
			{
				pageName = g.getId() + "/Home";
			}
			else
			{
				if (s != null)
				{
					pageName = g.getReference() + "/";
				}
				pageName += "section/" + g.getTitle() + "/Home";
			}
			writer.write("\n");
			writer.write("* [ Section: ");
			writer.write(g.getTitle());
			writer.write("|");
			writer.write(pageName);
			writer.write("]");
		}
		writer.write("\n");
		return;
	}
}
