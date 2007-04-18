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

import org.radeox.api.macro.MacroParameter;
import org.radeox.macro.BaseMacro;
import org.sakaiproject.site.api.Site;

import uk.ac.cam.caret.sakai.rwiki.component.Messages;
import uk.ac.cam.caret.sakai.rwiki.component.radeox.service.impl.SpecializedRenderContext;
import uk.ac.cam.caret.sakai.rwiki.component.radeox.service.impl.SpecializedRenderEngine;

/**
 * Provides access to worksite information
 * 
 * @author ieb
 */
public class WorksiteInfoMacro extends BaseMacro
{
	private static final String DESCRIPTION = "description"; //$NON-NLS-1$

	private static final String SHORTDESCRIPTION = "shortdescription"; //$NON-NLS-1$

	private static final String WIKISPACE = "wikispace"; //$NON-NLS-1$


	public String[] getParamDescription()
	{
		return new String[] {
			Messages.getString("WorksiteInfoMacro.3") //$NON-NLS-1$
					+ Messages.getString("WorksiteInfoMacro.4") //$NON-NLS-1$
					+ Messages.getString("WorksiteInfoMacro.5") //$NON-NLS-1$
					+ Messages.getString("WorksiteInfoMacro.6"), //$NON-NLS-1$
			Messages.getString("WorksiteInfoMacro.7") }; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.radeox.macro.Macro#getDescription()
	 */
	public String getDescription()
	{
		return Messages.getString("WorksiteInfoMacro.8"); //$NON-NLS-1$
	}

	public String getName()
	{
		return "worksiteinfo"; //$NON-NLS-1$
	}

	public void execute(Writer writer, MacroParameter params)
			throws IllegalArgumentException, IOException
	{

		SpecializedRenderContext context = (SpecializedRenderContext) params
				.getContext();
		SpecializedRenderEngine spRe = (SpecializedRenderEngine) context
				.getRenderEngine();

		String infotype = params.get("info", 0); //$NON-NLS-1$
		Site s = context.getSite();
		if (s != null)
		{
			if (DESCRIPTION.equals(infotype))
			{
				String desc = s.getDescription();
				if ( desc == null ) {
					desc = "";
				}
				writer.write(desc);
			}
			else if (SHORTDESCRIPTION.equals(infotype))
			{
				String desc = s.getShortDescription();
				if ( desc == null ) {
					desc = "";
				}
				writer.write(desc);
			}
			else if (WIKISPACE.equals(infotype))
			{
				String desc = spRe.getSpace();
				if ( desc == null ) {
					desc = "";
				}
				writer.write(desc);
			}
			else
			{
				String desc = s.getTitle();
				if ( desc == null ) {
					desc = "";
				}
				writer.write(desc);
			}
		}
		else
		{
			writer.write(Messages.getString("WorksiteInfoMacro.11")); //$NON-NLS-1$
		}
	}
}
