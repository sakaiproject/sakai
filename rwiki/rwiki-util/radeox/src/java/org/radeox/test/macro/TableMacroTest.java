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

public class TableMacroTest extends MacroTestSupport
{
	public TableMacroTest(String name)
	{
		super(name);
	}

	public static Test suite()
	{
		return new TestSuite(TableMacroTest.class);
	}

	public void testTable()
	{
		String result = EngineManager.getInstance().render(
				"{table}1|2\n3|4{table}", context);
		assertEquals(
				"<table class=\"wiki-table\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><th>1</th><th>2</th></tr><tr class=\"table-odd\"><td>3</td><td>4</td></tr></table>",
				result);
	}

	public void testEmptyHeader()
	{
		String result = EngineManager.getInstance().render(
				"{table}|\n3|4{table}", context);
		assertEquals(
				"<table class=\"wiki-table\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><th>&#160;</th><th>&#160;</th></tr><tr class=\"table-odd\"><td>3</td><td>4</td></tr></table>",
				result);
	}

	public void testMultiTable()
	{
		String result = EngineManager.getInstance().render(
				"{table}1|2\n3|4{table}\n{table}5|6\n7|8{table}", context);
		assertEquals(
				"<table class=\"wiki-table\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><th>1</th><th>2</th></tr><tr class=\"table-odd\"><td>3</td><td>4</td></tr></table>\n"
						+ "<table class=\"wiki-table\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><th>5</th><th>6</th></tr><tr class=\"table-odd\"><td>7</td><td>8</td></tr></table>",
				result);
	}

	public void testCalcIntSum()
	{
		String result = EngineManager.getInstance().render(
				"{table}1|2\n3|=SUM(A1:A2){table}", context);
		assertEquals(
				"<table class=\"wiki-table\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><th>1</th><th>2</th></tr><tr class=\"table-odd\"><td>3</td><td>4</td></tr></table>",
				result);
	}

	public void testCalcFloatSum()
	{
		String result = EngineManager.getInstance().render(
				"{table}1|2\n3.0|=SUM(A1:A2){table}", context);
		assertEquals(
				"<table class=\"wiki-table\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><th>1</th><th>2</th></tr><tr class=\"table-odd\"><td>3.0</td><td>4.0</td></tr></table>",
				result);
	}

	public void testFloatAvg()
	{
		String result = EngineManager.getInstance().render(
				"{table}1|2\n4|=AVG(A1:A2){table}", context);
		assertEquals(
				"<table class=\"wiki-table\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><th>1</th><th>2</th></tr><tr class=\"table-odd\"><td>4</td><td>2.5</td></tr></table>",
				result);
	}

}
