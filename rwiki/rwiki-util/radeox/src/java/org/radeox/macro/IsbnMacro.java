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
import org.radeox.macro.book.BookServices;

/*
 * Macro for displaying links to external book services, book dealers or
 * intranet libraries. IsbnMacro reads the mapping from names to urls from a
 * configuration file and then maps an ISBN number like {isbn:1234} to the book
 * e.g. on Amazon. @author stephan @team sonicteam
 * 
 * @version $Id$
 */

public class IsbnMacro extends BaseLocaleMacro
{
	private String[] paramDescription = { Messages.getString("IsbnMacro.0") }; //$NON-NLS-1$

	private String NEEDS_ISBN_ERROR;

	public String[] getParamDescription()
	{
		return paramDescription;
	}

	public String getLocaleKey()
	{
		return "macro.isbn"; //$NON-NLS-1$
	}

	public void execute(Writer writer, MacroParameter params)
			throws IllegalArgumentException, IOException
	{

		if (params.getLength() == 1)
		{
			BookServices.getInstance().appendUrl(writer, params.get("0")); //$NON-NLS-1$
			return;
		}
		else
		{
			throw new IllegalArgumentException(
					Messages.getString("IsbnMacro.3")); //$NON-NLS-1$
		}
	}
}
