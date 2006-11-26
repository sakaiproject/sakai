package org.sakaiproject.portal.render.portlet.services.state;

import junit.framework.TestCase;
import org.sakaiproject.portal.render.portlet.services.state.PortletState;

import javax.portlet.WindowState;
import javax.portlet.PortletMode;

public class SimplePortletStateEncoderTest extends TestCase {

    private SimplePortletStateEncoder encoder;

    public void setUp() {
        encoder = new SimplePortletStateEncoder();
        encoder.setUrlSafeEncoder(new Base64Recoder());
    }

    public void testEncodeDecode() {
        PortletState state = new PortletState("id");
        state.setAction(true);
        state.setSecure(true);
        state.setWindowState(WindowState.MAXIMIZED);
        state.setPortletMode(PortletMode.EDIT);

        String uriSafe = encoder.encode(state);
        PortletState read = encoder.decode(uriSafe);
        assertEquals(state.getId(), read.getId());
        assertEquals(state.isAction(), read.isAction());
        assertEquals(state.isSecure(), read.isSecure());
        assertEquals(state.getWindowState(), read.getWindowState());
        assertEquals(state.getPortletMode(), read.getPortletMode());
    }


}
