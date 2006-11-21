package org.sakaiproject.portal.render.portlet.services.state.encode;

import org.sakaiproject.portal.render.portlet.services.state.PortletState;

import java.io.*;

/**
 * Simple implementation of the PortletStateEncoder.
 * This implementation simply serializes the portlet state
 * and encodes the bits in a url safe maner.
 *
 * @since 2.2.3
 * @version $Id$
 *
 */
public class SimplePortletStateEncoder implements PortletStateEncoder {

    private UrlSafeEncoder urlSafeEncoder;

    public UrlSafeEncoder getUrlSafeEncoder() {
        return urlSafeEncoder;
    }

    public void setUrlSafeEncoder(UrlSafeEncoder urlSafeEncoder) {
        this.urlSafeEncoder = urlSafeEncoder;
    }


    public String encode(PortletState state) {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();

        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(bao);
            out.writeObject(state);
            out.flush();
        } catch (IOException e) {
            throw new IllegalStateException("Should never happen");
        } finally {
            try { if(out != null) out.close(); } catch (IOException e) { }
        }

        byte[] bits = bao.toByteArray();
        return urlSafeEncoder.encode(bits);
    }

    public PortletState decode(String encodedState) {
        byte[] decoded = urlSafeEncoder.decode(encodedState);
        ByteArrayInputStream bai = new ByteArrayInputStream(decoded);
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(bai);
            return (PortletState) in.readObject();
        } catch (IOException e) {
            throw new IllegalStateException("Should never happen");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Stale PortletState; Class not found.");
        } finally {
            if(in != null) try { in.close(); } catch(IOException io) {}
        }
    }

}
