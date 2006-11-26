package org.sakaiproject.portal.render.portlet.services.state;

import junit.framework.TestCase;
import org.sakaiproject.portal.render.portlet.services.state.PortletState;

import javax.portlet.WindowState;
import javax.portlet.PortletMode;

public class Base64RecoderTest extends TestCase {

    private Base64Recoder encoder;

    public void setUp() {
        encoder = new Base64Recoder();
    }

    public void testEncodeDecode() {
        String testString = "abcdefg, :!?+_-^%$";

        String uriSafe = encoder.encode(testString.getBytes());
        assertNotNull(uriSafe);
        assertEquals(-1, uriSafe.indexOf(" "));
        assertEquals(-1, uriSafe.indexOf("/"));
        assertEquals(-1, uriSafe.indexOf(":"));
        assertEquals(-1, uriSafe.indexOf("+"));
        assertEquals(-1, uriSafe.indexOf("="));
        assertEquals(-1, uriSafe.indexOf("?"));
        assertEquals(-1, uriSafe.indexOf("&"));
        byte[] bits = encoder.decode(uriSafe);

        assertEquals(testString, new String(bits));
    }


}
