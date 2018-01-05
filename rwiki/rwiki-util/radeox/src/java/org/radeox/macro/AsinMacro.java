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
import org.radeox.macro.book.AsinServices;

/*
 * Macro for displaying links to external DVD/CD services or dealers. AsinMacro
 * reads the mapping from names to urls from a configuration file and then maps
 * an ASIN number like {asin:1234} to the DVD/CD e.g. on Amazon. @author stephan
 * @team sonicteam
 * 
 * @version $Id$
 */
@Slf4j
public class AsinMacro extends BaseLocaleMacro
{
	private String[] paramDescription = { Messages.getString("AsinMacro.0") }; //$NON-NLS-1$

	public String[] getParamDescription()
	{
		return paramDescription;
	}

	public String getLocaleKey()
	{
		return "macro.asin"; //$NON-NLS-1$
	}

	public void execute(Writer writer, MacroParameter params)
			throws IllegalArgumentException, IOException
	{

		if (params.getLength() == 1)
		{
			AsinServices.getInstance().appendUrl(writer, params.get("0")); //$NON-NLS-1$
			return;
		}
		else
		{
			log.warn("needs an ASIN number as argument"); //$NON-NLS-1$
			throw new IllegalArgumentException(
					Messages.getString("AsinMacro.4")); //$NON-NLS-1$
		}
	}
}
