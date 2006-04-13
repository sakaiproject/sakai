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

import org.radeox.macro.api.ApiDoc;
import org.radeox.macro.parameter.MacroParameter;

/*
 * Macro that replaces {api} with external URLS to api documentation @author
 * stephan @team sonicteam
 * 
 * @version $Id$
 */

public class ApiMacro extends BaseLocaleMacro
{
	private String[] paramDescription = {
			"1: class name, e.g. java.lang.Object or java.lang.Object@Java131",
			"?2: mode, e.g. Java12, Ruby, defaults to Java" };

	public String[] getParamDescription()
	{
		return paramDescription;
	}

	public String getLocaleKey()
	{
		return "macro.api";
	}

	public void execute(Writer writer, MacroParameter params)
			throws IllegalArgumentException, IOException
	{
		String mode;
		String klass;

		if (params.getLength() == 1)
		{
			klass = params.get("0");
			int index = klass.indexOf("@");
			if (index > 0)
			{
				mode = klass.substring(index + 1);
				klass = klass.substring(0, index);
			}
			else
			{
				mode = "java";
			}
		}
		else if (params.getLength() == 2)
		{
			mode = params.get("1").toLowerCase();
			klass = params.get("0");
		}
		else
		{
			throw new IllegalArgumentException(
					"api macro needs one or two paramaters");
		}

		ApiDoc.getInstance().expand(writer, klass, mode);
		return;
	}
}
