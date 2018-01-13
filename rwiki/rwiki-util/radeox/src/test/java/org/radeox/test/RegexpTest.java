/*
 * This file is part of "SnipSnap Wiki/Weblog".
 *
 * Copyright (c) 2002 Stephan J. Schmidt, Matthias L. Jugel
 * All Rights Reserved.
 *
 * Please visit http://snipsnap.org/ for updates and contact.
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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import lombok.extern.slf4j.Slf4j;
import org.radeox.EngineManager;
import org.radeox.api.engine.RenderEngine;
import org.radeox.api.engine.context.RenderContext;
import org.radeox.engine.context.BaseRenderContext;

@Slf4j
public class RegexpTest
{
	public static void main(String[] args)
	{
		// log.info("Press enter ...");
		// try {
		// new BufferedReader(new InputStreamReader(System.in)).readLine();
		// } catch (IOException e) {
		// // ignore errors
		// }

		String file = args.length > 0 ? args[0] : "conf/wiki.txt";
		try
		{
			System.setOut(new PrintStream(System.out, true, "UTF-8"));
		}
		catch (UnsupportedEncodingException e)
		{
			// this should never happen
		}

		StringBuffer tmp = new StringBuffer();
		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), "UTF-8"));
			char[] buffer = new char[1024];
			int n = 0;
			while ((n = reader.read(buffer)) != -1)
			{
				tmp.append(buffer, 0, n);
			}
		}
		catch (Exception e)
		{
			log.error("File not found: " + e.getMessage());
		}

		String content = tmp.toString();

		log.info(content);

		RenderContext context = new BaseRenderContext();
		RenderEngine engine = EngineManager.getInstance();

		log.info(engine.render(content, context));
	}
}
