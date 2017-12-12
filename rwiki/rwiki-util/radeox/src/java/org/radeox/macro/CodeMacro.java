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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import lombok.extern.slf4j.Slf4j;
import org.radeox.Messages;
import org.radeox.api.engine.context.InitialRenderContext;
import org.radeox.api.engine.context.RenderContext;
import org.radeox.api.macro.MacroParameter;
import org.radeox.filter.context.BaseFilterContext;
import org.radeox.filter.context.FilterContext;
import org.radeox.macro.code.SourceCodeFormatter;
import org.radeox.util.Service;

/*
 * Macro for displaying programming language source code. CodeMacro knows about
 * different source code formatters which can be plugged into radeox to display
 * more languages. CodeMacro displays Java, Ruby or SQL code. @author stephan
 * @team sonicteam
 * 
 * @version $Id$
 */
@Slf4j
public class CodeMacro extends LocalePreserved
{

	private Map formatters;

	private FilterContext nullContext = new BaseFilterContext();

	private String start;

	private String end;

	private String[] paramDescription = { Messages.getString("CodeMacro.0") }; //$NON-NLS-1$

	public String[] getParamDescription()
	{
		return paramDescription;
	}

	public String getLocaleKey()
	{
		return "macro.code"; //$NON-NLS-1$
	}

	public void setInitialContext(InitialRenderContext context)
	{
		super.setInitialContext(context);
		Locale outputLocale = (Locale) context.get(RenderContext.OUTPUT_LOCALE);
		String outputName = (String) context
				.get(RenderContext.OUTPUT_BUNDLE_NAME);
		ResourceBundle outputMessages = ResourceBundle.getBundle(outputName,
				outputLocale);

		start = outputMessages.getString(getLocaleKey() + ".start"); //$NON-NLS-1$
		end = outputMessages.getString(getLocaleKey() + ".end"); //$NON-NLS-1$
	}

	public CodeMacro()
	{
		formatters = new HashMap();

		Iterator formatterIt = Service.providers(SourceCodeFormatter.class);
		while (formatterIt.hasNext())
		{
			try
			{
				SourceCodeFormatter formatter = (SourceCodeFormatter) formatterIt
						.next();
				String name = formatter.getName();
				if (formatters.containsKey(name))
				{
					SourceCodeFormatter existing = (SourceCodeFormatter) formatters
							.get(name);
					if (existing.getPriority() < formatter.getPriority())
					{
						formatters.put(name, formatter);
						log.debug("Replacing formatter: " //$NON-NLS-1$
								+ formatter.getClass() + " (" + name + ")"); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
				else
				{
					formatters.put(name, formatter);
					log.debug("Loaded formatter: " + formatter.getClass() //$NON-NLS-1$
							+ " (" + name + ")"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			catch (Exception e)
			{
				log.warn("CodeMacro: unable to load code formatter", e); //$NON-NLS-1$
			}
		}

		addSpecial('[');
		addSpecial(']');
		addSpecial('{');
		addSpecial('}');
		addSpecial('*');
		addSpecial('-');
		addSpecial('\\');
	}

	public void execute(Writer writer, MacroParameter params)
			throws IllegalArgumentException, IOException
	{

		SourceCodeFormatter formatter = null;

		if (params.getLength() == 0 || !formatters.containsKey(params.get("0"))) //$NON-NLS-1$
		{
			formatter = (SourceCodeFormatter) formatters.get(initialContext
					.get(RenderContext.DEFAULT_FORMATTER));
			if (null == formatter)
			{
				log.error("Formatter not found."); //$NON-NLS-1$
				formatter = (SourceCodeFormatter) formatters.get("java"); //$NON-NLS-1$
			}
		}
		else
		{
			formatter = (SourceCodeFormatter) formatters.get(params.get("0")); //$NON-NLS-1$
		}

		String result = formatter.filter(params.getContent(), nullContext);
		//SAK-20920 result could be null here
		if (result == null)
		{
			result = "";
		}
		result = replace(result.trim());
		
		writer.write(start);
		writer.write(result.replaceAll("\n","&#x0a;")); //$NON-NLS-1$ //$NON-NLS-2$
		writer.write(end);
		return;
	}
}
