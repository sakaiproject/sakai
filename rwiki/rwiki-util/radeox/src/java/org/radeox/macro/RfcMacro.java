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
import org.radeox.util.Encoder;

/*
 * Macro to easily link to RFCs. The website to link to is currently hard wired.
 * @author stephan @team sonicteam
 * 
 * @version $Id$
 */

public class RfcMacro extends BaseLocaleMacro
{

	public String getLocaleKey()
	{
		return "macro.rfc"; //$NON-NLS-1$
	}

	public void execute(Writer writer, MacroParameter params)
			throws IllegalArgumentException, IOException
	{

		String number = params.get("0"); //$NON-NLS-1$
		try {
			Integer.parseInt(number);
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException(Messages.getString("RfcMacro.3")); //$NON-NLS-1$
		}

		if (params.getLength() == 1)
		{
			String view = "RFC" + number; //$NON-NLS-1$
			appendRfc(writer, number, view);
			return;
		}
		else if (params.getLength() == 2)
		{
			String view = params.get(1);
			appendRfc(writer, number, view);
		}
		else
		{
			throw new IllegalArgumentException(Messages.getString("RfcMacro.3")); //$NON-NLS-1$
		}
	}

	public void appendRfc(Writer writer, String number, String view)
			throws IOException, IllegalArgumentException
	{
		writer.write("<a href=\"https://www.rfc-editor.org/info/rfc"); //$NON-NLS-1$
		writer.write(number);
		writer.write("\">"); //$NON-NLS-1$
		writer.write(Encoder.escape(view));
		writer.write("</a>"); //$NON-NLS-1$
		return;
	}
}
