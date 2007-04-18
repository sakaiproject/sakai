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

import org.radeox.api.macro.MacroParameter;
import org.radeox.filter.interwiki.InterWiki;

/*
 * Macro that shows all know interwiki mappings. @author stephan @team sonicteam
 * 
 * @version $Id: InterWikiMacro.java 7707 2006-04-12 17:30:19Z
 *          ian@caret.cam.ac.uk $
 */

public class InterWikiMacro extends BaseLocaleMacro
{
	private String[] paramDescription = { "none" };

	public String[] getParamDescription()
	{
		return paramDescription;
	}

	public String getLocaleKey()
	{
		return "macro.interwiki";
	}

	public void execute(Writer writer, MacroParameter params)
			throws IllegalArgumentException, IOException
	{
		InterWiki interWiki = InterWiki.getInstance();
		interWiki.appendTo(writer);
		return;
	}
}
