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
 * Macro to display quotations from other sources. The output is wrapped usually
 * in <blockquote> to look like a quotation. @author stephan @team sonicteam
 * 
 * @version $Id$
 */
@Slf4j
public class QuoteMacro extends LocalePreserved
{
	private String[] paramDescription = { Messages.getString("QuoteMacro.0"), //$NON-NLS-1$
			Messages.getString("QuoteMacro.1") }; //$NON-NLS-1$

	public String[] getParamDescription()
	{
		return paramDescription;
	}

	public QuoteMacro()
	{
	}

	public String getLocaleKey()
	{
		return "macro.quote"; //$NON-NLS-1$
	}

	public void execute(Writer writer, MacroParameter params)
			throws IllegalArgumentException, IOException
	{

		writer.write("<blockquote class=\"quote\"><p class=\"paragraph\">"); //$NON-NLS-1$
		writer.write(params.getContent());
		String source = Messages.getString("QuoteMacro.4"); // i18n //$NON-NLS-1$
		if (params.getLength() == 2)
		{
			source = params.get(1);
		}
		// if more than one was present, we
		// should show a description for the link
		if (params.getLength() > 0)
		{
			writer.write("<a href=\"" + params.get(0) + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
			writer.write(source);
			writer.write("</a>"); //$NON-NLS-1$
		}
		writer.write("</p></blockquote>"); //$NON-NLS-1$
		return;
	}
}
