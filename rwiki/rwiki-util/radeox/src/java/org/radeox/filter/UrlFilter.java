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

package org.radeox.filter;

import java.text.MessageFormat;

import org.radeox.api.engine.ImageRenderEngine;
import org.radeox.api.engine.RenderEngine;
import org.radeox.api.engine.context.InitialRenderContext;
import org.radeox.filter.context.FilterContext;
import org.radeox.filter.regex.LocaleRegexTokenFilter;
import org.radeox.regex.MatchResult;
import org.radeox.util.Encoder;

/*
 * UrlFilter finds http:// style URLs in its input and transforms this to <a
 * href="url">url</a> @author stephan @team sonicteam
 * 
 * @version $Id$
 */

public class UrlFilter extends LocaleRegexTokenFilter implements CacheFilter
{
	private MessageFormat formatter;

	protected String getLocaleKey()
	{
		return "filter.url";
	}

	public void setInitialContext(InitialRenderContext context)
	{
		super.setInitialContext(context);
		String outputTemplate = outputMessages.getString(getLocaleKey()
				+ ".print");
		formatter = new MessageFormat("");
		formatter.applyPattern(outputTemplate);
	}

	public void handleMatch(StringBuffer buffer, MatchResult result,
			FilterContext context)
	{
		buffer.append(result.group(1));
		// Does our engine know images?
		RenderEngine engine = context.getRenderContext().getRenderEngine();
		String externalImage = "";
		if (engine instanceof ImageRenderEngine)
		{
			buffer.append(((ImageRenderEngine) engine).getExternalImageLink());
		}

		buffer.append(formatter.format(new Object[] {
				externalImage,
				result.group(2),
				Encoder.toEntity(result.group(2).charAt(0))
						+ result.group(2).substring(1) }));
		return;
	}
}
