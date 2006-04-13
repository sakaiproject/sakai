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

import java.io.IOException;
import java.io.Writer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.radeox.api.engine.RenderEngine;
import org.radeox.api.engine.WikiRenderEngine;
import org.radeox.filter.context.FilterContext;
import org.radeox.filter.interwiki.InterWiki;
import org.radeox.filter.regex.LocaleRegexTokenFilter;
import org.radeox.regex.MatchResult;
import org.radeox.util.Encoder;
import org.radeox.util.StringBufferWriter;

/*
 * LinkTestFilter finds [text] in its input and transforms this to <a
 * href="text">...</a> if the wiki page exists. If not it adds a [create text]
 * to the output. @author stephan @team sonicteam
 * 
 * @version $Id: LinkTestFilter.java 7707 2006-04-12 17:30:19Z
 *          ian@caret.cam.ac.uk $
 */

public class LinkTestFilter extends LocaleRegexTokenFilter
{
	private static Log log = LogFactory.getLog(LinkTestFilter.class);

	/**
	 * The regular expression for detecting WikiLinks. Overwrite in subclass to
	 * support other link styles like OldAndUglyWikiLinking :-)
	 * /[A-Z][a-z]+([A-Z][a-z]+)+/ wikiPattern = "\\[(.*?)\\]";
	 */

	protected String getLocaleKey()
	{
		return "filter.linktest";
	}

	protected void setUp(FilterContext context)
	{
		context.getRenderContext().setCacheable(true);
	}

	public void handleMatch(StringBuffer buffer, MatchResult result,
			FilterContext context)
	{
		RenderEngine engine = context.getRenderContext().getRenderEngine();

		if (engine instanceof WikiRenderEngine)
		{
			WikiRenderEngine wikiEngine = (WikiRenderEngine) engine;
			Writer writer = new StringBufferWriter(buffer);

			String name = result.group(1);
			if (name != null)
			{
				// User probably wrote [http://radeox.org] instead of
				// http://radeox.org
				if (name.indexOf("http://") != -1)
				{
					try
					{
						writer
								.write("<div class=\"error\">Do not surround URLs with [...].</div>");
					}
					catch (IOException e)
					{
						// Do nothing. Give up.
					}
					return;
				}

				// trim the name and unescape it
				name = Encoder.unescape(name.trim());

				// Is there an alias like [alias|link] ?
				int pipeIndex = name.indexOf('|');
				String alias = "";
				if (-1 != pipeIndex)
				{
					alias = name.substring(0, pipeIndex);
					name = name.substring(pipeIndex + 1);
				}

				int hashIndex = name.lastIndexOf('#');

				String hash = "";
				if (-1 != hashIndex && hashIndex != name.length() - 1)
				{
					char[] hashChars = name.substring(hashIndex + 1)
							.toCharArray();
					int end = 0;
					for (int i = 0; i < hashChars.length; i++)
					{
						if (Character.isLetterOrDigit(hashChars[i]))
						{
							hashChars[end++] = hashChars[i];
						}
					}
					if (end > 0)
					{
						hash = new String(hashChars, 0, end);
					}

					name = name.substring(0, hashIndex);
				}

				int colonIndex = name.indexOf(':');
				// typed link ?
				if (-1 != colonIndex)
				{
					// for now throw away the type information
					name = name.substring(colonIndex + 1);
				}

				int atIndex = name.lastIndexOf('@');
				// InterWiki link ?
				if (-1 != atIndex)
				{
					String extSpace = name.substring(atIndex + 1);
					// known extarnal space ?
					InterWiki interWiki = InterWiki.getInstance();
					if (interWiki.contains(extSpace))
					{
						String view = name;
						if (-1 != pipeIndex)
						{
							view = alias;
						}

						name = name.substring(0, atIndex);
						try
						{
							if (-1 != hashIndex)
							{
								interWiki.expand(writer, extSpace, name, view,
										hash);
							}
							else
							{
								interWiki.expand(writer, extSpace, name, view);
							}
						}
						catch (IOException e)
						{
							log.debug("InterWiki " + extSpace + " not found.");
						}
					}
					else
					{
						buffer.append("&#91;<span class=\"error\">");
						buffer.append(result.group(1));
						buffer.append("?</span>&#93;");
					}
				}
				else
				{
					// internal link

					if (wikiEngine.exists(name))
					{
						String view = getWikiView(name, hash);
						if (-1 != pipeIndex)
						{
							view = alias;
						}

						// Do not add hash if an alias was given
						if (-1 != hashIndex)
						{
							wikiEngine.appendLink(buffer, name, view, hash);
						}
						else
						{
							wikiEngine.appendLink(buffer, name, view);
						}
					}
					else if (wikiEngine.showCreate())
					{

						String view = getWikiView(name, "");
						if (-1 != pipeIndex)
						{
							view = alias;
						}
						wikiEngine.appendCreateLink(buffer, name, view);
						// links with "create" are not cacheable because
						// a missing wiki could be created
						context.getRenderContext().setCacheable(false);
					}
					else
					{
						// cannot display/create wiki, so just display the text
						buffer.append(name);
					}
				}
			}
			else
			{
				buffer.append(Encoder.escape(result.group(0)));
			}
		}
	}

	/**
	 * Returns the view of the wiki name that is shown to the user. Overwrite to
	 * support other views for example transform "WikiLinking" to "Wiki
	 * Linking". Does nothing by default.
	 * 
	 * @return view The view of the wiki name
	 */

	protected String getWikiView(String name, String hash)
	{
		if (!hash.equals(""))
		{
			return name + "#" + hash;
		}

		return name;
	}
}
