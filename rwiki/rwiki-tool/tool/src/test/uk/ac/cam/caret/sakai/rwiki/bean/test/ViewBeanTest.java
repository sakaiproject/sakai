package uk.ac.cam.caret.sakai.rwiki.bean.test;

import junit.framework.TestCase;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.ViewBean;

public class ViewBeanTest extends TestCase
{

	String localPageName = "Foo";

	String globalPageName = "/bar/bar/foo";

	String realm = "/bar/bar";

	String otherRealm = "/realm";

	String viewUrl = "?pageName=%2Fbar%2Fbar%2Ffoo&action=view&panel=Main&realm=%2Fbar%2Fbar";

	String editUrl = "?pageName=%2Fbar%2Fbar%2Ffoo&action=edit&panel=Main&realm=%2Fbar%2Fbar";

	String infoUrl = "?pageName=%2Fbar%2Fbar%2Ffoo&action=info&panel=Main&realm=%2Fbar%2Fbar";

	public ViewBeanTest(String test)
	{
		super(test);
	}

	/*
	 * Test method for
	 * 'uk.ac.cam.caret.sakai.rwiki.tool.bean.ViewBean.getViewUrl()'
	 */
	public void testGetViewUrl()
	{
		ViewBean vb = new ViewBean(localPageName, realm);
		assertTrue("ViewBean doesn't create ViewUrls properly.", viewUrl
				.equals(vb.getViewUrl()));
	}

	/*
	 * Test method for
	 * 'uk.ac.cam.caret.sakai.rwiki.tool.bean.ViewBean.getEditUrl()'
	 */
	public void testGetEditUrl()
	{
		ViewBean vb = new ViewBean(localPageName, realm);
		assertTrue("ViewBean doesn't create EditUrls properly.", editUrl
				.equals(vb.getEditUrl()));
	}

	/*
	 * Test method for
	 * 'uk.ac.cam.caret.sakai.rwiki.tool.bean.ViewBean.getInfoUrl()'
	 */
	public void testGetInfoUrl()
	{

		ViewBean vb = new ViewBean(localPageName, realm);
		assertTrue("ViewBean doesn't create InfoUrls properly.", infoUrl
				.equals(vb.getInfoUrl()));

	}

	/*
	 * Test method for
	 * 'uk.ac.cam.caret.sakai.rwiki.tool.bean.ViewBean.setPageName(String)'
	 */
	public void testPageName()
	{
		ViewBean vb = new ViewBean(localPageName, realm);
		assertTrue("View Bean doesn't globalise names properly", globalPageName
				.equals(vb.getPageName()));

		vb = new ViewBean(globalPageName, otherRealm);
		assertTrue("View Bean doesn't retain global names properly",
				globalPageName.equals(vb.getPageName()));
	}

	/*
	 * Test method for
	 * 'uk.ac.cam.caret.sakai.rwiki.tool.bean.ViewBean.getLocalRealm()'
	 */
	public void testGetLocalRealm()
	{
		ViewBean vb = new ViewBean(localPageName, realm);
		assertTrue("ViewBean doesn't set local realm properly", realm.equals(vb
				.getLocalSpace()));
		vb = new ViewBean(globalPageName, otherRealm);
		assertTrue("ViewBean doesn't set local realm properly", otherRealm
				.equals(vb.getLocalSpace()));
	}

	/*
	 * Test method for
	 * 'uk.ac.cam.caret.sakai.rwiki.tool.bean.ViewBean.getPageRealm()'
	 */
	public void testGetPageRealm()
	{
		ViewBean vb = new ViewBean(localPageName, realm);
		assertTrue("ViewBean doesn't set page realm properly", realm.equals(vb
				.getPageSpace()));
		vb = new ViewBean(globalPageName, otherRealm);
		assertTrue("ViewBean doesn't set page realm properly", realm.equals(vb
				.getPageSpace()));

	}

	/*
	 * Test method for
	 * 'uk.ac.cam.caret.sakai.rwiki.tool.bean.ViewBean.setAnchor(String)'
	 */
	public void testSetAnchor()
	{
		ViewBean vb = new ViewBean(localPageName, realm);
		String anchor = "anchor";
		vb.setAnchor(anchor);
		assertTrue("ViewBean doesn't set Anchor properly",
				(viewUrl + "#" + anchor).equals(vb.getViewUrl()));
	}

	public void testGetPageName()
	{
		ViewBean vb = new ViewBean(localPageName, realm);
		assertTrue("ViewBean doesn't set pageName properly", globalPageName
				.equals(vb.getPageName()));
		vb = new ViewBean(globalPageName, otherRealm);
		assertTrue("ViewBean doesn't set local realm properly", globalPageName
				.equals(vb.getPageName()));

	}
}
