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

import java.io.Writer;

import lombok.extern.slf4j.Slf4j;
import org.radeox.Messages;
import org.radeox.api.engine.IncludeRenderEngine;
import org.radeox.api.engine.RenderEngine;
import org.radeox.api.engine.context.InitialRenderContext;
import org.radeox.api.macro.Macro;
import org.radeox.api.macro.MacroParameter;
import org.radeox.filter.context.FilterContext;
import org.radeox.filter.regex.RegexTokenFilter;
import org.radeox.macro.MacroRepository;
import org.radeox.macro.Repository;
import org.radeox.regex.MatchResult;
import org.radeox.util.StringBufferWriter;

/*
 * Class that finds snippets (macros) like {link:neotis|http://www.neotis.de}
 * ---> <a href="....> {!neotis} -> include neotis object, e.g. a wiki page
 * Macros can built with a start and an end, e.g. {code} ... {code} @author
 * stephan @team sonicteam
 * 
 * @version $Id$
 */
@Slf4j
public class MacroFilter extends RegexTokenFilter
{

	// private static MacroFilter instance;

	// Map of known macros with name and macro object
	private MacroRepository macros;

	// private static Object monitor = new Object();
	// private static Object[] noArguments = new Object[]{};

	public MacroFilter()
	{
		// optimized by Jeffrey E.F. Friedl
		super("\\{([^:}]+)(?::([^\\}]*))?\\}(.*?)\\{\\1\\}", SINGLELINE); //$NON-NLS-1$
		addRegex("\\{([^:}]+)(?::([^\\}]*))?\\}", "", MULTILINE); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void setInitialContext(InitialRenderContext context)
	{
		macros = MacroRepository.getInstance();
		macros.setInitialContext(context);
	}

	protected Repository getMacroRepository()
	{
		return macros;
	}

	public void handleMatch(StringBuffer buffer, MatchResult result,
			FilterContext context)
	{
		String command = result.group(1);

		if (command != null)
		{
			// {$peng} are variables not macros.
			if (!command.startsWith("$")) //$NON-NLS-1$
			{
				MacroParameter mParams = context.getMacroParameter();
				// log.info("count="+result.groups());
				// log.info("1: "+result.group(1));
				// log.info("2: "+result.group(2));
				switch (result.groups())
				{
					case 3:
						mParams.setContent(result.group(3));
						mParams.setContentStart(result.beginOffset(3));
						mParams.setContentEnd(result.endOffset(3));
					case 2:
						mParams.setParams(result.group(2));
				// Still left from ORO
				// case 2: log.info(result.group(1));
				// case 1: log.info(result.group(0));
				}
				mParams.setStart(result.beginOffset(0));
				mParams.setEnd(result.endOffset(0));

				// @DANGER: recursive calls may replace macros in included
				// source code
				try
				{
					if (getMacroRepository().containsKey(command))
					{
						Macro macro = (Macro) getMacroRepository().get(command);
						// recursively filter macros within macros
						if (null != mParams.getContent())
						{
							mParams.setContent(filter(mParams.getContent(),
									context));
						}
						Writer writer = new StringBufferWriter(buffer);
						macro.execute(writer, mParams);
					}
					else if (command.startsWith("!")) //$NON-NLS-1$
					{
						// @TODO including of other snips
						RenderEngine engine = context.getRenderContext()
								.getRenderEngine();
						if (engine instanceof IncludeRenderEngine)
						{
							String include = ((IncludeRenderEngine) engine)
									.include(command.substring(1),context.getRenderContext());
							if (null != include)
							{
								// Filter paramFilter = new
								// ParamFilter(mParams);
								// included = paramFilter.filter(included,
								// null);
								buffer.append(include);
							}
							else
							{
								buffer.append(command.substring(1)
										+ Messages.getString("MacroFilter.5")); //$NON-NLS-1$
							}
						}
						return;
					}
					else
					{
						buffer.append(result.group(0));
						return;
					}
				}
				catch (IllegalArgumentException e)
				{
					buffer.append("<div class=\"error\">" + command + ": " //$NON-NLS-1$ //$NON-NLS-2$
							+ e.getMessage() + "</div>"); //$NON-NLS-1$
				}
				catch (Throwable e)
				{
					log.warn("MacroFilter: unable to format macro: " //$NON-NLS-1$
							+ result.group(1), e);
					buffer.append("<div class=\"error\">" + command + ": " //$NON-NLS-1$ //$NON-NLS-2$
							+ e.getMessage() + "</div>"); //$NON-NLS-1$
					return;
				}
			}
			else
			{
				buffer.append("<"); //$NON-NLS-1$
				buffer.append(command.substring(1));
				buffer.append(">"); //$NON-NLS-1$
			}
		}
		else
		{
			buffer.append(result.group(0));
		}
	}
}
