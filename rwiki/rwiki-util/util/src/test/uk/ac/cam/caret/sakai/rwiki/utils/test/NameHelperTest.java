package uk.ac.cam.caret.sakai.rwiki.utils.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import junit.framework.TestCase;
import uk.ac.cam.caret.sakai.rwiki.utils.NameHelper;

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

	private final String defaultName = "/site/test"
			+ NameHelper.SPACE_SEPARATOR + NameHelper.getDefaultPage();

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
		System.out.println(testLocalName);
		assertTrue("LocalizeName(\"" + similarGlobalName + "\", \"" + space
				+ "\")  should equal " + similarLocalName + " but equals "
				+ testLocalName, similarLocalName.equals(testLocalName));
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

}
