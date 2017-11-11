package uk.ac.cam.caret.sakai.rwiki.bean.test;

import junit.framework.TestCase;

import org.easymock.EasyMock;

import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
import uk.ac.cam.caret.sakai.rwiki.tool.api.ToolRenderService;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.RenderBean;

public class RenderBeanTest extends TestCase
{

	String localName = "Foo";

	String realm = "bar";

	String globalName = "bar.Foo";

	String otherRealm = "realm";

	String value = "value";

	ToolRenderService mockToolRenderService;

	RWikiObjectService mockObjectService;

	RWikiObject mockObject;

	RenderBean rb;


	public RenderBeanTest(String test)
	{
		super(test);
	}

	protected void setUp() throws Exception
	{
		super.setUp();

		mockToolRenderService = EasyMock.mock(ToolRenderService.class);
		mockObjectService = EasyMock.mock(RWikiObjectService.class);
		mockObject = EasyMock.mock(RWikiObject.class);

		EasyMock.expect(mockObjectService.checkUpdate(mockObject)).andReturn(true);
		EasyMock.expect(mockObjectService.checkCreate(mockObject)).andReturn(true);
		EasyMock.expect(mockObjectService.checkRead(mockObject)).andReturn(true);

		EasyMock.replay(mockObjectService);

		rb = new RenderBean(mockObject, mockToolRenderService,
				mockObjectService, true);

	}

	/*
	 * Test method for
	 * 'uk.ac.cam.caret.sakai.rwiki.tool.bean.RenderBean.renderPage()'
	 */
	public void testRenderPage()
	{
		EasyMock.expect(mockToolRenderService.renderPage(mockObject))
				.andReturn(value);

		EasyMock.replay(mockToolRenderService, mockObject);

		assertTrue(value.equals(rb.renderPage()));
		EasyMock.verify(mockToolRenderService, mockObject);
	}

	/*
	 * Test method for
	 * 'uk.ac.cam.caret.sakai.rwiki.tool.bean.RenderBean.renderPage(String,
	 * String)'
	 */
	public void testRenderPageStringString()
	{
		return;
		/*
		 * try { mockObjectService.getRWikiObject(globalName,user,realm); }
		 * catch (PermissionException e) { // EMPTY }
		 * objectServiceControl.setReturnValue(mockObject);
		 * mockRenderService.renderPage(mockObject,user,otherRealm);
		 * renderServiceControl.setReturnValue(value);
		 * objectServiceControl.replay(); rwikiObjectControl.replay();
		 * renderServiceControl.replay();
		 * assertTrue(value.equals(rb.renderPage(globalName, otherRealm)));
		 * objectServiceControl.verify(); renderServiceControl.verify();
		 * rwikiObjectControl.verify();
		 */
	}

}
