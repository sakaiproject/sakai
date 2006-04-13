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

package org.radeox.engine.context;

import java.util.Locale;

import org.radeox.api.engine.context.InitialRenderContext;
import org.radeox.api.engine.context.RenderContext;

/**
 * Base impementation for InitialRenderContext
 * 
 * @author Stephan J. Schmidt
 * @version $Id: BaseInitialRenderContext.java 7707 2006-04-12 17:30:19Z
 *          ian@caret.cam.ac.uk $
 */

public class BaseInitialRenderContext extends BaseRenderContext implements
		InitialRenderContext
{
	public BaseInitialRenderContext()
	{
		Locale languageLocale = Locale.getDefault();
		Locale locale = new Locale("Basic", "basic");
		set(RenderContext.INPUT_LOCALE, locale);
		set(RenderContext.OUTPUT_LOCALE, locale);
		set(RenderContext.LANGUAGE_LOCALE, languageLocale);
		set(RenderContext.INPUT_BUNDLE_NAME, "radeox_markup");
		set(RenderContext.OUTPUT_BUNDLE_NAME, "radeox_markup");
		set(RenderContext.LANGUAGE_BUNDLE_NAME, "radeox_messages");

		set(RenderContext.DEFAULT_FORMATTER, "java");
	}
}
