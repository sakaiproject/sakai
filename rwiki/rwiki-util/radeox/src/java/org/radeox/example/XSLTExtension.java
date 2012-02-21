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
package org.radeox.example;

import org.radeox.EngineManager;
import org.radeox.api.engine.context.RenderContext;
import org.radeox.engine.context.BaseRenderContext;

/**
 * An xslt extension function to render snip content using radeox. Example
 * usage: <xsl:stylesheet version="1.0"
 * xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 * xmlns:radeox="http://snipsnap.org/org.radeox.example.XSLTExtension"> É
 * <xsl:template match="content"> <content><xsl:value-of
 * select="radeox:render(.)" disable-output-escaping="yes"/></content>
 * </xsl:template> É </xsl:stylesheet>
 * 
 * @author Micah Dubinko
 * @version $Id: XSLTExtension.java 7707 2006-04-12 17:30:19Z
 *          ian@caret.cam.ac.uk $
 */
public class XSLTExtension
{
	public XSLTExtension()
	{
		// needed for recognition as extension class?
	}

	public static String render(String arg)
	{
		// for maximum robustness don't put any text in that isn't there
		if (arg == null)
		{
			arg = "";
		}

		RenderContext context = new BaseRenderContext();
		return (EngineManager.getInstance().render(arg, context));
	}
}
