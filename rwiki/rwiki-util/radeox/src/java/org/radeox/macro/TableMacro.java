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
import org.radeox.api.macro.MacroParameter;
import org.radeox.macro.table.Table;
import org.radeox.macro.table.TableBuilder;

/*
 * Macro for defining and displaying tables. The rows of the table are devided
 * by newlins and the columns are divided by pipe symbols "|". The first line of
 * the table is rendered as column headers. {table} A|B|C 1|2|3 {table} @author
 * stephan @team sonicteam
 * 
 * @version $Id$
 */

public class TableMacro extends BaseLocaleMacro
{
	private String[] paramDescription = {};

	public String[] getParamDescription()
	{
		return paramDescription;
	}

	public String getLocaleKey()
	{
		return "macro.table"; //$NON-NLS-1$
	}

	public void execute(Writer writer, MacroParameter params)
			throws IllegalArgumentException, IOException
	{

		String content = params.getContent();

		if (null == content)
			throw new IllegalArgumentException(
					Messages.getString("TableMacro.1")); //$NON-NLS-1$

		content = content.trim() + "\n"; //$NON-NLS-1$

		Table table = TableBuilder.build(content);
		table.calc(); // calculate macros like =SUM(A1:A3)
		table.appendTo(writer);
		return;
	}
}
