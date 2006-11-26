package org.sakaiproject.portal.render.portlet.services.state;

import org.sakaiproject.portal.render.portlet.services.state.PortletState;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import java.io.*;

import junit.framework.TestCase;


public class PortletStateTest extends TestCase {

    public void testSerialization() throws IOException, ClassNotFoundException {
        PortletState state = new PortletState("id");
        state.setPortletMode(PortletMode.VIEW);
        state.setWindowState(WindowState.MAXIMIZED);

        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bao);

        out.writeObject(state);

        ByteArrayInputStream bai = new ByteArrayInputStream(bao.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bai);
        PortletState alter = (PortletState) in.readObject();

        assertEquals(state, alter);

    }

}
