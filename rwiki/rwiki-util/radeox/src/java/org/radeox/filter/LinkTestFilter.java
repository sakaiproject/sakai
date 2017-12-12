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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import org.radeox.Messages;
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
@Slf4j
public class LinkTestFilter extends LocaleRegexTokenFilter
{
	/**
	 * The regular expression for detecting WikiLinks. Overwrite in subclass to
	 * support other link styles like OldAndUglyWikiLinking :-)
	 * /[A-Z][a-z]+([A-Z][a-z]+)+/ wikiPattern = "\\[(.*?)\\]";
	 */

	protected String getLocaleKey()
	{
		return "filter.linktest"; //$NON-NLS-1$
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
				if (name.indexOf("http://") != -1) //$NON-NLS-1$
				{
					try
					{
						writer
								.write(Messages.getString("LinkTestFilter.2")); //$NON-NLS-1$
					}
					catch (IOException e)
					{
						// Do nothing. Give up.
					}
					return;
				}

				// Is there an alias like [alias|link] ?
				int pipeIndex = name.indexOf('|');
				String alias = ""; //$NON-NLS-1$
				if (-1 != pipeIndex)
				{
					alias = Encoder.unescape(name.substring(0, pipeIndex).trim());
					name = name.substring(pipeIndex + 1).trim();
				}

				Pattern p = Pattern.compile("(.*)(?<!\\&)\\#(.*)"); //$NON-NLS-1$
				Matcher m = p.matcher(name);
				String hash = ""; //$NON-NLS-1$
				
				if (m.matches()) {
					hash = Encoder.unescape(m.group(2));
					char[] hashChars =  hash.toCharArray();
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
					
					name = m.group(1);
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
					String extSpace = Encoder.unescape(name.substring(atIndex + 1));
					// known external space ?
					InterWiki interWiki = InterWiki.getInstance();
					if (interWiki.contains(extSpace))
					{

						name = Encoder.unescape(name.substring(0, atIndex));
						String view;
						if (-1 != pipeIndex)
						{
							view = alias;
						} 
						else {
							view = name + "@" + extSpace; //$NON-NLS-1$
						}


						try
						{
							if (name.indexOf('@') > -1) 
							{
								addAtSignError(buffer);
							} 
							else if (hash.length() > 0)
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
							log.debug("InterWiki " + extSpace + " not found."); //$NON-NLS-1$ //$NON-NLS-2$
						}
					}
					else
					{
						buffer.append("&#91;<span class=\"error\">"); //$NON-NLS-1$
						buffer.append(result.group(1));
						buffer.append("?</span>&#93;"); //$NON-NLS-1$
					}
				}
				else
				{
					// internal link

					name = Encoder.unescape(name);
					
					if (name.indexOf('@') > -1) 
					{
						addAtSignError(buffer);
					} 
					else if (wikiEngine.exists(name) || ("".equals(name) && !("".equals(hash)))) //$NON-NLS-1$ //$NON-NLS-2$
					{
						if ("".equals(name) && !("".equals(hash))) { //$NON-NLS-1$ //$NON-NLS-2$
							name = (String) context.getRenderContext().get("uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject.name"); //$NON-NLS-1$
						}
						
						String view = getWikiView(name, hash);
						if (-1 != pipeIndex)
						{
							view = alias;
						}

						// Do not add hash if an alias was given
						if (hash.length() > 0)
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

						String view = getWikiView(name, ""); //$NON-NLS-1$
						if (-1 != pipeIndex)
						{
							view = alias;
						}
						wikiEngine.appendCreateLink(buffer, name, view);
					}
					else
					{
						// cannot display/create wiki, so just display the text
						buffer.append(Encoder.escape(name));
					}
				}
			}
			else
			{
				buffer.append(Encoder.escape(result.group(0)));
			}
		}
	}

	private void addAtSignError(StringBuffer buffer) {
		buffer.append("<span class=\"error\">"); //$NON-NLS-1$
		buffer.append(Messages.getString("LinkTestFilter.1")); 
		buffer.append("</span>"); //$NON-NLS-1$
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
		if (!"".equals(hash)) //$NON-NLS-1$
		{
			return name + "#" + hash; //$NON-NLS-1$
		}

		return name;
	}
}
