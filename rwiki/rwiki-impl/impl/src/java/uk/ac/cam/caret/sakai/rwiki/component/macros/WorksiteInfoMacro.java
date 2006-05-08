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

import org.radeox.macro.BaseMacro;
import org.radeox.macro.parameter.MacroParameter;
import org.sakaiproject.site.api.Site;

import uk.ac.cam.caret.sakai.rwiki.component.radeox.service.impl.SpecializedRenderContext;
import uk.ac.cam.caret.sakai.rwiki.component.radeox.service.impl.SpecializedRenderEngine;

/**
 * Provides access to worksite information
 * 
 * @author ieb
 */
public class WorksiteInfoMacro extends BaseMacro
{
	private static final String DESCRIPTION = "description";

	private static final String SHORTDESCRIPTION = "shortdescription";

	private static final String WIKISPACE = "wikispace";

	private static String[] paramDescription = {
			"1,info: The type of info to provide, worksiteinfo:title gives Title (default), "
					+ " worksiteinfo:description, "
					+ " worksiteinfo:shortdescription, "
					+ " worksiteinfo:wikispace ",
			"Remember if using positional parameters, you must include dummies for the optional parameters" };

	private static String description = "Generates worksite information";

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
		return "worksiteinfo";
	}

	public void execute(Writer writer, MacroParameter params)
			throws IllegalArgumentException, IOException
	{

		SpecializedRenderContext context = (SpecializedRenderContext) params
				.getContext();
		SpecializedRenderEngine spRe = (SpecializedRenderEngine) context
				.getRenderEngine();

		String infotype = params.get("info", 0);
		Site s = context.getSite();
		if (s != null)
		{
			if (DESCRIPTION.equals(infotype))
			{
				writer.write(s.getDescription());
			}
			else if (SHORTDESCRIPTION.equals(infotype))
			{
				writer.write(s.getShortDescription());
			}
			else if (WIKISPACE.equals(infotype))
			{
				writer.write(spRe.getSpace());
			}
			else
			{
				writer.write(s.getTitle());
			}
		}
		else
		{
			writer.write("No Site Found for page");
		}
	}
}
