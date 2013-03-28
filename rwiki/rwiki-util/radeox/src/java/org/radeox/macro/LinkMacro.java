/*
 * This file is part of "SnipSnap Radeox Rendering Engine".
 *
 * Copyright (c) 2002 Stephan J. Schmidt, Matthias L. Jugel
 * All Rights Reserved.
 *
 * Please visit http://radeox.org/ for updates and contact.
 *
 * --LICENSE NOTICE--
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * --LICENSE NOTICE--
 */
package org.radeox.macro;

import java.io.IOException;
import java.io.Writer;

import org.radeox.Messages;
import org.radeox.api.engine.ImageRenderEngine;
import org.radeox.api.engine.RenderEngine;
import org.radeox.api.engine.context.RenderContext;
import org.radeox.api.macro.MacroParameter;
import org.radeox.util.Encoder;

/*
 * Macro for displaying external links with a name. The normal UrlFilter takes
 * the url as a name. @author stephan @team sonicteam
 * 
 * @version $Id$
 */

public class LinkMacro extends BaseLocaleMacro
{
	private static String[] paramDescription = {
			Messages.getString("LinkMacro.0"), //$NON-NLS-1$
			Messages.getString("LinkMacro.1"), //$NON-NLS-1$
			Messages.getString("LinkMacro.2"), //$NON-NLS-1$
			Messages.getString("LinkMacro.3"), //$NON-NLS-1$
			Messages.getString("LinkMacro.4") }; //$NON-NLS-1$

	private static String description = Messages.getString("LinkMacro.5"); //$NON-NLS-1$

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

	public String getLocaleKey()
	{
		return "macro.link"; //$NON-NLS-1$
	}

	public void execute(Writer writer, MacroParameter params)
			throws IllegalArgumentException, IOException
	{

		RenderContext context = params.getContext();
		RenderEngine engine = context.getRenderEngine();

		String text = params.get("text", 0); //$NON-NLS-1$
		String url = params.get("url", 1); //$NON-NLS-1$
		String img = params.get("img", 2); //$NON-NLS-1$
		String target = params.get("target", 3); //$NON-NLS-1$

		// check for single url argument (text == url)
		if (params.getLength() == 1)
		{
			url = text;
			text = Encoder.toEntity(text.charAt(0))
					+ Encoder.escape(text.substring(1));
		}

		if (url != null && text != null)
		{
			if (target == null)
			{
				if (url.indexOf("://") >= 0 && url.indexOf("://") < 6) //$NON-NLS-1$ //$NON-NLS-2$
				{
					target = "rwikiexternal"; //$NON-NLS-1$
				}
				else
				{
					target = "none"; //$NON-NLS-1$
				}

			}

            //Trim these elements to eliminate whitespace
            url=url.trim();
            target=target.trim();
            text=text.trim();

			writer.write("<span class=\"nobr\">"); //$NON-NLS-1$
			if (!"none".equals(img) && engine instanceof ImageRenderEngine) //$NON-NLS-1$
			{
				writer.write(((ImageRenderEngine) engine)
						.getExternalImageLink());
			}
			writer.write("<a href=\""); //$NON-NLS-1$
			writer.write(url);
			writer.write("\""); //$NON-NLS-1$
			if (!"none".equals(target)) //$NON-NLS-1$
			{
				writer.write(" target=\""); //$NON-NLS-1$
				writer.write(target);
				writer.write("\" "); //$NON-NLS-1$
			}
			writer.write(">"); //$NON-NLS-1$
			writer.write(text);
			writer.write("</a></span>"); //$NON-NLS-1$
		}
		else
		{
			throw new IllegalArgumentException(
					Messages.getString("LinkMacro.23")); //$NON-NLS-1$
		}
		return;
	}
}
