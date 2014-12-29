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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.radeox.Messages;
import org.radeox.api.macro.Macro;
import org.radeox.api.macro.MacroParameter;

/*
 * MacroListMacro displays a list of all known macros of the EngineManager with
 * their name, parameters and a description. @author Matthias L. Jugel
 * 
 * @version $Id: MacroListMacro.java 7707 2006-04-12 17:30:19Z
 *          ian@caret.cam.ac.uk $
 */

public class MacroListMacro extends BaseLocaleMacro
{
	public String getLocaleKey()
	{
		return Messages.getString("MacroListMacro.0"); //$NON-NLS-1$
	}

	public void execute(Writer writer, MacroParameter params)
			throws IllegalArgumentException, IOException
	{
		if (params.getLength() == 0)
		{
			appendTo(writer);
		}
		else
		{
			throw new IllegalArgumentException(
					"MacroListMacro: number of arguments does not match"); //$NON-NLS-1$
		}
	}

	public Writer appendTo(Writer writer) throws IOException
	{
		List macroList = MacroRepository.getInstance().getPlugins();
		Collections.sort(macroList);
		Iterator iterator = macroList.iterator();
		writer.write(Messages.getString("MacroListMacro.2")); //$NON-NLS-1$
		writer.write("Macro|Description|Parameters\n"); //$NON-NLS-1$
		while (iterator.hasNext())
		{
			Macro macro = (Macro) iterator.next();
			writer.write(macro.getName());
			writer.write("|"); //$NON-NLS-1$
			writer.write(macro.getDescription());
			writer.write("|"); //$NON-NLS-1$
			String[] params = macro.getParamDescription();
			if (params.length == 0)
			{
				writer.write("none"); //$NON-NLS-1$
			}
			else
			{
				for (int i = 0; i < params.length; i++)
				{
					String description = params[i];
					if (description.startsWith("?")) //$NON-NLS-1$
					{
						writer.write(description.substring(1));
						writer.write(" (optional)"); //$NON-NLS-1$
					}
					else
					{
						writer.write(params[i]);
					}
					writer.write("\\\\"); //$NON-NLS-1$
				}
			}
			writer.write("\n"); //$NON-NLS-1$
		}
		writer.write(Messages.getString("MacroListMacro.11")); //$NON-NLS-1$
		return writer;
	}

}
