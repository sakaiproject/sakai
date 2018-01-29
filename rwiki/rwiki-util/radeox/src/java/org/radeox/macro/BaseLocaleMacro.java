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

import java.util.Locale;
import java.util.ResourceBundle;

import lombok.extern.slf4j.Slf4j;
import org.radeox.api.engine.context.InitialRenderContext;
import org.radeox.api.engine.context.RenderContext;

/*
 * Class that implements base functionality to write macros and reads it's name
 * from a locale file @author stephan
 * 
 * @version $Id: BaseLocaleMacro.java 7707 2006-04-12 17:30:19Z
 *          ian@caret.cam.ac.uk $
 */
@Slf4j
public abstract class BaseLocaleMacro extends BaseMacro implements LocaleMacro
{
	private String name;

	public String getName()
	{
		return name;
	}

	public void setInitialContext(InitialRenderContext context)
	{
		super.setInitialContext(context);
		Locale languageLocale = (Locale) context
				.get(RenderContext.LANGUAGE_LOCALE);
		String languageName = (String) context
				.get(RenderContext.LANGUAGE_BUNDLE_NAME);
		ResourceBundle messages = ResourceBundle.getBundle(languageName,
				languageLocale);

		Locale inputLocale = (Locale) context.get(RenderContext.INPUT_LOCALE);
		String inputName = (String) context
				.get(RenderContext.INPUT_BUNDLE_NAME);
		ResourceBundle inputMessages = ResourceBundle.getBundle(inputName,
				inputLocale);

		name = inputMessages.getString(getLocaleKey() + ".name");

		try
		{
			description = messages.getString(getLocaleKey() + ".description");
		}
		catch (Exception e)
		{
			log.warn("Cannot read description from properties " + inputName
					+ " for " + getLocaleKey());
		}
	}
}
