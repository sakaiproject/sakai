/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package uk.ac.cam.caret.sakai.rwiki.utils.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import lombok.extern.slf4j.Slf4j;
import junit.framework.TestCase;
import uk.ac.cam.caret.sakai.rwiki.utils.NameHelper;

@Slf4j
public class NameHelperTest extends TestCase
{

	private final String globalName = "/site/test/some page name";

	private final String localizedName = "/site/test/Some Page Name";

	private final String space = "/site/test";

	private final String otherSpace = "/other/space";

	private final String name = " sOme paGe   name ";

	private final String localName = "Some Page Name";

	private final String similarGlobalName = "/site/testa";

	private final String similarLocalName = "/site/Testa";

	private final String[] shouldGlobaliseToEachOther = new String[] { "/site/test/Page Name", "/site/test/Page NAme", "/site/test/PAge Name", "/site/test/page name" };

	private final String[] shouldNotGlobaliseToEachOther = new String[] { "/site/tEst/Page Name", "/site/test/Page NAme", "/site/tesT/PAge Name", "/site/TEST/page name" };
	
	private final String defaultName = "/site/test"
			+ NameHelper.SPACE_SEPARATOR + NameHelper.DEFAULT_PAGE;

	public final String testBundleName = "/uk/ac/cam/caret/sakai/rwiki/utils/test/NameHelperTest.test";

	private HashMap allTests;

	public void setUp()
	{
		allTests = new HashMap();
		BufferedReader br = new BufferedReader(new InputStreamReader(this
				.getClass().getResourceAsStream(testBundleName)));

		try
		{
			int read;
			while ((read = br.read()) > -1)
			{
				char delimeter = (char) read;
				String line = br.readLine();
				String[] array = line.split("" + delimeter);
				ArrayList tests = (ArrayList) allTests.get(array[0]);
				if (tests == null)
				{
					tests = new ArrayList();
					allTests.put(array[0], tests);
				}
				tests.add(array);
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException("Couldn't load testBundle: "
					+ testBundleName + " correctly. Will ignore.");
		}
	}

	/*
	 * Test method for
	 * 'uk.ac.cam.caret.sakai.rwiki.model.impl.NameHelper.isGlobal(String)'
	 */
	public void testIsGlobal()
	{
		assertTrue(NameHelper.isGlobalised(globalName));
		assertFalse(NameHelper.isGlobalised(name));
		assertTrue(NameHelper.isGlobalised(defaultName));
		assertTrue(NameHelper.isGlobalised(space));
		assertFalse(NameHelper.isGlobalised(space + NameHelper.SPACE_SEPARATOR));

		ArrayList tests = (ArrayList) allTests.get("isGlobal");
		if (tests != null)
		{
			for (Iterator it = tests.iterator(); it.hasNext();)
			{
				String[] test = (String[]) it.next();
				String name = test[1];
				boolean result = true;
				if (test.length > 2)
				{
					result = Boolean.valueOf(test[2]).booleanValue();
				}
				boolean actualResult = NameHelper.isGlobalised(name);
				assertTrue("NameHelper.isGlobalised(\"" + name
						+ "\") should return " + result + " but returns "
						+ actualResult, actualResult == result);
			}
		}
	}

	/*
	 * Test method for
	 * 'uk.ac.cam.caret.sakai.rwiki.model.impl.NameHelper.globaliseName(String,
	 * String)'
	 */
	public void testGlobaliseName()
	{
		String globalisedName = NameHelper.globaliseName(name, space);
		assertTrue("Globalised Name: " + globalisedName + " does not equal "
				+ globalName, globalName.equals(globalisedName));

		String globalisedOtherSpace = NameHelper.globaliseName(globalName,
				otherSpace);
		assertTrue(
				"Global name should remain the same under any globalisation, but was globalised to this: "
						+ globalisedOtherSpace, globalName
						.equals(globalisedOtherSpace));

		String testDefaultName = NameHelper.globaliseName("", space);
		assertTrue("Default name should be: " + defaultName, defaultName
				.equals(testDefaultName));

		ArrayList tests = (ArrayList) allTests.get("globaliseName");
		if (tests != null)
		{
			for (Iterator it = tests.iterator(); it.hasNext();)
			{
				String[] test = (String[]) it.next();
				String name = test[1];
				String space = test[2];
				String result = test[3];
				String actualResult = NameHelper.globaliseName(name, space);
				assertTrue("NameHelper.globaliseName(\"" + name + "\", \""
						+ space + "\") should return " + result
						+ " but returns " + actualResult, result
						.equals(actualResult));
			}
		}
	}

	/*
	 * Test method for
	 * 'uk.ac.cam.caret.sakai.rwiki.model.impl.NameHelper.localizeName(String,
	 * String)'
	 */
	public void testLocalizeName()
	{
		String testLocalName = NameHelper.localizeName(globalName, space);
		assertTrue("LocalizeName(\"" + globalName + "\", \"" + space
				+ "\") should equal " + localName + " but equals "
				+ testLocalName, localName.equals(testLocalName));
		testLocalName = NameHelper.localizeName(globalName, otherSpace);
		assertTrue("LocalizeName(\"" + globalName + "\", \"" + otherSpace
				+ "\")  should equal " + localizedName + " but equals "
				+ testLocalName, localizedName.equals(testLocalName));
		testLocalName = NameHelper.localizeName(similarGlobalName, space);
		log.info(testLocalName);
		assertTrue("LocalizeName(\"" + similarGlobalName + "\", \"" + space
				+ "\")  should not equal " + similarLocalName + " but equals "
				+ testLocalName, !similarLocalName.equals(testLocalName));
		ArrayList tests = (ArrayList) allTests.get("localizeName");
		if (tests != null)
		{
			for (Iterator it = tests.iterator(); it.hasNext();)
			{
				String[] test = (String[]) it.next();
				String name = test[1];
				String space = test[2];
				String result = test[3];
				String actualResult = NameHelper.localizeName(name, space);
				assertTrue("NameHelper.localizeName(\"" + name + "\", \""
						+ space + "\") should return " + result
						+ " but returns " + actualResult, result
						.equals(actualResult));
			}
		}
	}

	/*
	 * Test method for
	 * 'uk.ac.cam.caret.sakai.rwiki.model.impl.NameHelper.localizeSpace(String,
	 * String)'
	 */
	public void testLocalizeSpace()
	{
		String testLocalSpace = NameHelper
				.localizeSpace(globalName, otherSpace);
		assertTrue("LocalizeSpace(\"" + globalName + "\", \"" + otherSpace
				+ "\") should equal " + space, space.equals(testLocalSpace));
		testLocalSpace = NameHelper.localizeSpace(name, otherSpace);
		assertTrue("LocalizeSpace(\"" + name + "\", \"" + otherSpace
				+ "\") should equal " + otherSpace, otherSpace
				.equals(testLocalSpace));

		ArrayList tests = (ArrayList) allTests.get("localizeSpace");
		if (tests != null)
		{
			for (Iterator it = tests.iterator(); it.hasNext();)
			{
				String[] test = (String[]) it.next();
				String name = test[1];
				String space = test[2];
				String result = test[3];
				String actualResult = NameHelper.localizeSpace(name, space);
				assertTrue("NameHelper.localizeSpace(\"" + name + "\", \""
						+ space + "\") should return " + result
						+ " but returns " + actualResult, result
						.equals(actualResult));
			}
		}
		
	}

	public void testGlobaliseToEachOther() {
		for (int i = 0; i < shouldGlobaliseToEachOther.length; i++) {
			assertEquals(NameHelper.globaliseName(shouldGlobaliseToEachOther[0], space), NameHelper.globaliseName(shouldGlobaliseToEachOther[i], space));
		}
		
		HashMap map = new HashMap();
		for (int i = 0; i < shouldNotGlobaliseToEachOther.length; i++) {
			String unglobalName = shouldNotGlobaliseToEachOther[i];
			String globalName = NameHelper.globaliseName(unglobalName, space);
			assertNull(unglobalName + " globalises to the same global name as  " + map.get(globalName), map.get(globalName));
			map.put(globalName, unglobalName);
		}
	}
	
}
