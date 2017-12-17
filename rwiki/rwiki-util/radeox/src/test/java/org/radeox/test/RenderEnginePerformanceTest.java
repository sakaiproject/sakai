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
package org.radeox.test;

import lombok.extern.slf4j.Slf4j;
import junit.framework.TestCase;
import org.radeox.EngineManager;
import org.radeox.api.engine.RenderEngine;
import org.radeox.api.engine.context.RenderContext;
import org.radeox.engine.context.BaseRenderContext;

@Slf4j
public class RenderEnginePerformanceTest extends TestCase
{
	private RenderContext context;

	private String wikiMarkup;

	private RenderEngine engine;

	public RenderEnginePerformanceTest(String text)
	{
		super("testPerformance");
		context = new BaseRenderContext();
		wikiMarkup = text;
		engine = EngineManager.getInstance();
	}

	public void testPerformance()
	{
		log.info("Test Size: " + wikiMarkup.length());
		engine.render(wikiMarkup, context);
	}
}