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

import lombok.extern.slf4j.Slf4j;
import org.radeox.api.engine.ImageRenderEngine;
import org.radeox.api.engine.RenderEngine;
import org.radeox.api.macro.MacroParameter;
import org.radeox.macro.BaseLocaleMacro;
import org.radeox.util.Encoder;

import uk.ac.cam.caret.sakai.rwiki.component.Messages;
import uk.ac.cam.caret.sakai.rwiki.component.radeox.service.impl.SpecializedRenderContext;

/**
 * This is a reimplementation of the LinkMacro but made aware of the sakai://
 * and worksite:// url formats
 * 
 * @author andrew
 */
@Slf4j
public class SakaiLinkMacro extends BaseLocaleMacro
{
	public String[] getParamDescription()
	{
		return new String[] {
			Messages.getString("SakaiLinkMacro.0"), //$NON-NLS-1$
			Messages.getString("SakaiLinkMacro.1"), //$NON-NLS-1$
			Messages.getString("SakaiLinkMacro.2"), //$NON-NLS-1$
			Messages.getString("SakaiLinkMacro.3"), //$NON-NLS-1$
			Messages.getString("SakaiLinkMacro.4") }; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.radeox.macro.Macro#getDescription()
	 */
	public String getDescription()
	{
		return Messages.getString("SakaiLinkMacro.5"); //$NON-NLS-1$
	}

	public String getLocaleKey()
	{
		return "macro.link"; //$NON-NLS-1$
	}

	public void execute(Writer writer, MacroParameter params)
			throws IllegalArgumentException, IOException
	{

		SpecializedRenderContext context = (SpecializedRenderContext) params
				.getContext();
		RenderEngine engine = context.getRenderEngine();

		String text = params.get("text", 0); //$NON-NLS-1$
		String url = params.get("url", 1); //$NON-NLS-1$
		String img = params.get("img", 2); //$NON-NLS-1$
		String target = params.get("target", 3); //$NON-NLS-1$

		// check for single url argument (text == url)
		if (params.getLength() == 1)
		{
			url = text;
			text = Encoder.escape(text);
		}

		if (url != null && text != null)
		{
			if (target == null)
			{
				// FIXME make the context have a method to do this check
				if (url.startsWith("sakai:") || url.startsWith("worksite:/") || url.startsWith("saka-dropbox:/") || url.startsWith("dropbox:/") || url.indexOf(":/") < 0 || url.indexOf(":/") > 10) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				{
					target = "none"; //$NON-NLS-1$
				}
				else
				{
					target = "rwikiexternal"; //$NON-NLS-1$
				}

			}

			writer.write("<span class=\"nobr\">"); //$NON-NLS-1$
			if (!"none".equals(img) && engine instanceof ImageRenderEngine) //$NON-NLS-1$
			{
				writer.write(((ImageRenderEngine) engine)
						.getExternalImageLink());
			}
			
			url = context.convertLink(url);
			
            //Trim url
            url=url.trim();

			// SAK-20449 XSS protection
			if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("ftp://") &&
				!url.startsWith("mailto:")) {
				log.warn("RWiki URL (" + url + ") looks invalid so we're removing it from the display.");
				url = "";
			}

			writer.write("<a href=\"" + url + "\""); //$NON-NLS-1$ //$NON-NLS-2$
			if (!"none".equals(target)) //$NON-NLS-1$
			{
				writer.write(" target=\"" + target + "\""); //$NON-NLS-1$ //$NON-NLS-2$
			}
			writer.write(">"); //$NON-NLS-1$
			writer.write(text);
			writer.write("</a></span>"); //$NON-NLS-1$
		}
		else
		{
			throw new IllegalArgumentException(
					Messages.getString("SakaiLinkMacro.28")); //$NON-NLS-1$
		}
		return;
	}
}
