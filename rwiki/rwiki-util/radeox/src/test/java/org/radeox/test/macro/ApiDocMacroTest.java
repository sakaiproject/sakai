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
package org.radeox.test.macro;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.radeox.EngineManager;

public class ApiDocMacroTest extends MacroTestSupport
{
	public ApiDocMacroTest(String name)
	{
		super(name);
	}

	public static Test suite()
	{
		return new TestSuite(ApiDocMacroTest.class);
	}

	public void testApi()
	{
		String result = EngineManager.getInstance().render("{api-docs}",
				context);
		// This must be moved to IoC to better test ApiDoc directly.
		assertEquals(
				"ApiDocs are rendered",
				"<table class=\"wiki-table\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><th>Binding</th><th>BaseUrl</th><th>Converter"
						+ " Name</th></tr><tr class=\"table-odd\"><td>java131</td><td><span class=\"nobr\"><a href=\"http://java.sun.com/j2se/1.3.1/docs/api/\">&#104;ttp://java.sun.com/j2se/1.3.1/docs/api/"
						+ "</a></span></td><td>Java</td></tr><tr class=\"table-even\"><td>java</td><td><span class=\"nobr\"><a href=\"http://java.sun.com/j2se/1.4.1/docs/api/\">&#104;ttp://java.sun.com/j2s"
						+ "e/1.4.1/docs/api/</a></span></td><td>Java</td></tr><tr class=\"table-odd\"><td>ruby</td><td><span class=\"nobr\"><a href=\"http://www.rubycentral.com/book/ref_c_\">&#104;ttp://ww"
						+ "w.rubycentral.com/book/ref_c_</a></span></td><td>Ruby</td></tr><tr class=\"table-even\"><td>radeox</td><td><span class=\"nobr\"><a href=\"http://snipsnap.org/docs/api/\">&#104;tt"
						+ "p://snipsnap.org/docs/api/</a></span></td><td>Java</td></tr><tr class=\"table-odd\"><td>nanning</td><td><span class=\"nobr\"><a href=\"http://nanning.sourceforge.net/apidocs/\">&"
						+ "#104;ttp://nanning.sourceforge.net/apidocs/</a></span></td><td>Java</td></tr><tr class=\"table-even\"><td>java12</td><td><span class=\"nobr\"><a href=\"http://java.sun.com/j2se/"
						+ "1.2/docs/api/\">&#104;ttp://java.sun.com/j2se/1.2/docs/api/</a></span></td><td>Java</td></tr><tr class=\"table-odd\"><td>j2ee</td><td><span class=\"nobr\"><a href=\"http://java.s"
						+ "un.com/j2ee/sdk_1.3/techdocs/api/\">&#104;ttp://java.sun.com/j2ee/sdk_1.3/techdocs/api/</a></span></td><td>Java</td></tr></table>",
				result);
	}

}
