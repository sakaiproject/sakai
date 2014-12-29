package uk.ac.cam.caret.sakai.rwiki.bean.test;

import junit.framework.TestCase;

import org.apache.commons.jrcs.diff.DifferentiationFailedException;
import org.apache.commons.jrcs.diff.Revision;
import org.apache.commons.jrcs.diff.myers.MyersDiff;

import uk.ac.cam.caret.sakai.rwiki.tool.bean.GenericDiffBean;

public class GenericDiffBeanTest extends TestCase
{

	private static final String RIGHT_CONTENT = "1\n2\n4\n6\n5";

	private static final String LEFT_CONTENT = "1\n23\n4\n5";

	// private static final String colorDiffExpected = "<table
	// class=\"colordiff\"><tr><td class=\"unchangedLeft\">1<br/></td><td
	// class=\"unchangedRight\">1<br/></td></tr><tr><td
	// class=\"changeLeft\">23<br/></td><td
	// class=\"changeRight\">2<br/></td></tr><tr><td
	// class=\"unchangedLeft\">4<br/></td><td
	// class=\"unchangedRight\">4<br/></td></tr><tr><td
	// class=\"addLeft\"></td><td class=\"addRight\">6<br/></td></tr><tr><td
	// class=\"unchangedLeft\">5<br/></td><td
	// class=\"unchangedRight\">5<br/></td></tr></table>";
	public GenericDiffBeanTest(String name)
	{
		super(name);
	}

	// FIXME must make test for color diff and color diff table
	/*
	 * Test method for
	 * 'uk.ac.cam.caret.sakai.rwiki.tool.DiffBean.getColorDiffString()'
	 */
	/*
	 * public void testGetColorDiffString() { GenericDiffBean diffBean = new
	 * GenericDiffBean(LEFT_CONTENT, RIGHT_CONTENT); diffBean.init();
	 * assertTrue(colorDiffExpected.equals(diffBean.getColorDiffString())); }
	 */

	/*
	 * Test method for
	 * 'uk.ac.cam.caret.sakai.rwiki.tool.DiffBean.getUnixDiffString()'
	 */
	public void testGetUnixDiffString()
	{
		GenericDiffBean diffBean = new GenericDiffBean(LEFT_CONTENT,
				RIGHT_CONTENT);
		diffBean.init();

		MyersDiff diffAlgorithm = new MyersDiff();

		try
		{
			Object[] objectifiedLeft = LEFT_CONTENT.split("\n");

			Object[] objectifiedRight = RIGHT_CONTENT.split("\n");

			Revision difference = diffAlgorithm.diff(objectifiedLeft,
					objectifiedRight);

			assertTrue(difference.toString().equals(
					diffBean.getUnixDiffString()));

		}
		catch (DifferentiationFailedException e)
		{
			throw new RuntimeException(
					"DifferentiationFailedException occured: This should never happen!",
					e);
		}

	}

}
