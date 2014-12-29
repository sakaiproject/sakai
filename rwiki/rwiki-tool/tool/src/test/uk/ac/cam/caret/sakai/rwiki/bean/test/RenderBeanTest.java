package uk.ac.cam.caret.sakai.rwiki.bean.test;

import junit.framework.TestCase;

import org.easymock.MockControl;

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

	MockControl renderServiceControl, objectServiceControl, rwikiObjectControl;

	public RenderBeanTest(String test)
	{
		super(test);
	}

	protected void setUp() throws Exception
	{
		super.setUp();
		renderServiceControl = MockControl
				.createControl(ToolRenderService.class);
		objectServiceControl = MockControl
				.createControl(RWikiObjectService.class);
		rwikiObjectControl = MockControl.createControl(RWikiObject.class);

		mockToolRenderService = (ToolRenderService) renderServiceControl
				.getMock();
		mockObjectService = (RWikiObjectService) objectServiceControl.getMock();
		mockObject = (RWikiObject) rwikiObjectControl.getMock();
		// mockObject = new RWikiObjectImpl();

		mockObjectService.checkUpdate(mockObject);
		objectServiceControl.setReturnValue(false);
		mockObjectService.checkRead(mockObject);
		objectServiceControl.setReturnValue(false);
		objectServiceControl.replay();

		rb = new RenderBean(mockObject, mockToolRenderService,
				mockObjectService, true);

	}

	/*
	 * Test method for
	 * 'uk.ac.cam.caret.sakai.rwiki.tool.bean.RenderBean.renderPage()'
	 */
	public void testRenderPage()
	{
		mockToolRenderService.renderPage(mockObject);
		renderServiceControl.setReturnValue(value);
		rwikiObjectControl.replay();
		renderServiceControl.replay();

		assertTrue(value.equals(rb.renderPage()));
		objectServiceControl.verify();
		renderServiceControl.verify();
		rwikiObjectControl.verify();
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
