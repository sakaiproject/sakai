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

import lombok.extern.slf4j.Slf4j;
import org.radeox.Messages;
import org.radeox.api.macro.MacroParameter;

/*
 * Displays a file path. This is used to store a filepath in an OS independent
 * way and then display the file path as needed. This macro also solves the
 * problems with to many backslashes in Windows filepaths when they are entered
 * in Snipsnap. @author stephan @team sonicteam
 * 
 * @version $Id: FilePathMacro.java 7707 2006-04-12 17:30:19Z
 *          ian@caret.cam.ac.uk $
 */
@Slf4j
public class FilePathMacro extends LocalePreserved
{
	private String[] paramDescription = { Messages.getString("FilePathMacro.0") }; //$NON-NLS-1$

	public String getLocaleKey()
	{
		return "macro.filepath"; //$NON-NLS-1$
	}

	public FilePathMacro()
	{
		addSpecial('\\');
	}

	public String getDescription()
	{
		return Messages.getString("FilePathMacro.2"); //$NON-NLS-1$
	}

	public String[] getParamDescription()
	{
		return paramDescription;
	}

	public void execute(Writer writer, MacroParameter params)
			throws IllegalArgumentException, IOException
	{

		if (params.getLength() == 1)
		{
			String path = params.get("0").replace('/', '\\'); //$NON-NLS-1$
			writer.write(replace(path));
		}
		return;
	}
}
