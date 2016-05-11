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
package org.radeox.test.macro.code;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.radeox.EngineManager;
import org.radeox.test.macro.MacroTestSupport;

public class XmlCodeMacroTest extends MacroTestSupport
{
	final String S_CODE = "<div class=\"code\"><pre>";

	final String E_CODE = "</pre></div>";

	final String S_XML_TAG = "<span class=\"xml&#45;tag\">&#60;";

	final String E_XML_TAG = "&#62;</span>";

	final String S_XML_KEYWORD = "<span class=\"xml&#45;keyword\">";

	final String E_XML_KEYWORD = "</span>";

	final String S_XML_QUOTE = "<span class=\"xml&#45;quote\">\"";

	final String E_XML_QUOTE = "\"</span>";

	public XmlCodeMacroTest(String name)
	{
		super(name);
	}

	public static Test suite()
	{
		return new TestSuite(XmlCodeMacroTest.class);
	}

	public void testXmlCodeXmlElement()
	{
		String result = EngineManager.getInstance().render(
				"{code:xml}<xml a=\"attr\"><node>text</node></xml>{code}",
				context);
		assertEquals(S_CODE + S_XML_TAG + "xml a=" + S_XML_QUOTE + "attr"
				+ E_XML_QUOTE + E_XML_TAG + S_XML_TAG + "node" + E_XML_TAG
				+ "text" + S_XML_TAG + "/node" + E_XML_TAG + S_XML_TAG + "/xml"
				+ E_XML_TAG + E_CODE, result);
	}

	public void testXmlCodeXsl()
	{
		String sInput = "{code:xml}<xsl:anytag/>{code}";
		String sExpected = S_CODE + S_XML_TAG + S_XML_KEYWORD + "xsl:anytag"
				+ E_XML_KEYWORD + "/" + E_XML_TAG + E_CODE;
		String sResult = EngineManager.getInstance().render(sInput, context);
		assertEquals(sExpected, sResult);
	}

	public void testXmlCodeXslWithAttr()
	{
		String sInput = "{code:xml}<xsl:anytag attr=\"1\"/>{code}";
		String sExpected = S_CODE + S_XML_TAG + S_XML_KEYWORD + "xsl:anytag"
				+ E_XML_KEYWORD + " attr=" + S_XML_QUOTE + "1" + E_XML_QUOTE
				+ "/" + E_XML_TAG + E_CODE;
		String sResult = EngineManager.getInstance().render(sInput, context);
		assertEquals(sExpected, sResult);
	}
}
