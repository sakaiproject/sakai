package org.sakaiproject.portal.render.portlet.services.state;

import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import junit.framework.TestCase;

public class EnhancedPortletStateEncoderTest extends TestCase
{

	private EnhancedPortletStateEncoder encoder;

	@Override
	public void setUp()
	{
		encoder = new EnhancedPortletStateEncoder();
		encoder.setUrlSafeEncoder(new BasicWebRecoder());
	}

	public void testEncodeDecode()
	{
		Map parms = new HashMap();
		parms.put("one", "oneValue");
		parms.put("two", "twoValue");
		parms.put("three", new String[] { "threeOne", "threeTwo", null });

		PortletState state = new PortletState("id");
		state.setAction(true);
		state.setSecure(true);
		state.setWindowState(WindowState.MAXIMIZED);
		state.setPortletMode(PortletMode.EDIT);
		state.setParameters(parms);

		String uriSafe = encoder.encode(state);
		
		System.err.println("URI is "+uriSafe);
		
		assertNotNull(uriSafe);
		assertEquals(-1, uriSafe.indexOf(" "));
		assertEquals(-1, uriSafe.indexOf("/"));
		assertEquals(-1, uriSafe.indexOf(":"));
		assertEquals(-1, uriSafe.indexOf("="));
		assertEquals(-1, uriSafe.indexOf("?"));
		assertEquals(-1, uriSafe.indexOf("&"));

		PortletState read = encoder.decode(uriSafe);

		assertEquals(state.getId(), read.getId());
		assertEquals(state.isAction(), read.isAction());
		assertEquals(state.isSecure(), read.isSecure());
		assertEquals(state.getWindowState(), read.getWindowState());
		assertEquals(state.getPortletMode(), read.getPortletMode());
		assertEquals(3, read.getParameters().size());

		String[] one = (String[]) read.getParameters().get("one");
		assertEquals(1, one.length);
		assertEquals("oneValue", one[0]);

		String[] two = (String[]) read.getParameters().get("two");
		assertEquals(1, two.length);
		assertEquals("twoValue", two[0]);

		String[] three = (String[]) read.getParameters().get("three");
		assertEquals(2, three.length);
		assertEquals("threeOne", three[0]);
		assertEquals("threeTwo", three[1]);

	}
}
