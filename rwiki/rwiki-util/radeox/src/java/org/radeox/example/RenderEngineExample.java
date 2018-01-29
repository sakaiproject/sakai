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

package org.radeox.example;

import java.util.Locale;

import lombok.extern.slf4j.Slf4j;
import org.radeox.api.engine.RenderEngine;
import org.radeox.api.engine.context.InitialRenderContext;
import org.radeox.api.engine.context.RenderContext;
import org.radeox.engine.BaseRenderEngine;
import org.radeox.engine.context.BaseInitialRenderContext;
import org.radeox.engine.context.BaseRenderContext;

/*
 * Example how to use BaseRenderEngine @author Stephan J. Schmidt
 * 
 * @version $Id: RenderEngineExample.java 7707 2006-04-12 17:30:19Z
 *          ian@caret.cam.ac.uk $
 */
@Slf4j
public class RenderEngineExample
{
	public static void main(String[] args)
	{
		String test = "__SnipSnap__ {link:Radeox|http://radeox.org} ==Other Bold==";

		RenderContext context = new BaseRenderContext();
		RenderEngine engine = new BaseRenderEngine();
		log.info("Rendering with default:");
		log.info(engine.render(test, context));

		log.info("Rendering with alternative Wiki:");
		InitialRenderContext initialContext = new BaseInitialRenderContext();
		initialContext.set(RenderContext.INPUT_LOCALE, new Locale("otherwiki",
				""));
		RenderEngine engineWithContext = new BaseRenderEngine(initialContext);
		log.info(engineWithContext.render(test, context));
	}
}
