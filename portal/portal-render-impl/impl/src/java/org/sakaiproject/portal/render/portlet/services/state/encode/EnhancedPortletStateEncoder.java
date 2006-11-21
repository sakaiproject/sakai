package org.sakaiproject.portal.render.portlet.services.state.encode;

import org.sakaiproject.portal.render.portlet.services.state.PortletState;

import javax.portlet.WindowState;
import javax.portlet.PortletMode;
import java.util.Properties;
import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;

/**
 * Enhanced version of the PortletStateEncoder.
 * This implementation translates the PortletState
 * into a properties file format for serialization. This
 * results in a url parameters which is significatly shorter
 * than ther simple version. This may be desirable in a cases
 * where the url query string is allready significant and may
 * surpass the limit of 2083 characters imposed by some browsers.
 *
 * @since 2.2.3
 * @version $Id$
 */
public class EnhancedPortletStateEncoder implements PortletStateEncoder {

//
// Implementation Note:
// - Map Keys below are qualified in order to prevent
//   conflicts with portlet parameters
// - Keys have been abbreviated to shorten the query
//   string parameter resulting
//

    private static final String ID_PARAM =
        "o.s.p.r.s.ID";

     private static final String ACTION_PARAM =
        "o.s.p.r.s.ACTION";

    private static final String SECURE_PARAM =
        "o.s.p.r.s.SECURE";

    private static final String PORTLET_MODE_PARAM =
        "o.s.p.r.s.PORTLET_MODE";

    private static final String WINDOW_STATE_PARAM =
        "o.s.p.r.s.WINDOW_STATE";

    private static final String PROP_PARAM_PREFIX =
        "o.s.p.r.s.PARAM";


    private UrlSafeEncoder urlSafeEncoder
        = new UrlSafeBase64Encoder();

    public UrlSafeEncoder getUrlSafeEncoder() {
        return urlSafeEncoder;
    }

    public void setUrlSafeEncoder(UrlSafeEncoder urlSafeEncoder) {
        this.urlSafeEncoder = urlSafeEncoder;
    }

    public String encode(PortletState portletState) {
        Properties p = new Properties();
        p.setProperty(ID_PARAM, portletState.getId());
        p.setProperty(ACTION_PARAM, String.valueOf(portletState.isAction()));
        p.setProperty(SECURE_PARAM, String.valueOf(portletState.isSecure()));

        p.setProperty(WINDOW_STATE_PARAM, portletState.getWindowState().toString());
        p.setProperty(PORTLET_MODE_PARAM, portletState.getPortletMode().toString());

        Map parms = portletState.getParameters();
        Iterator it = parms.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            p.setProperty(PROP_PARAM_PREFIX + entry.getKey(), entry.getValue().toString());
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            p.store(out, "Sakai Portlet State");
        } catch (IOException e) {
            throw new IllegalStateException("This should never occor");
        }

        return urlSafeEncoder.encode(out.toByteArray());
    }

    public PortletState decode(String uri) {
        byte[] bits = urlSafeEncoder.decode(uri);
        ByteArrayInputStream in = new ByteArrayInputStream(bits);

        Properties p = new Properties();
        try {
            p.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("This should never occor");
        }

        String id = p.getProperty(ID_PARAM);
        PortletState state = new PortletState(id);
        state.setAction(Boolean.valueOf(p.getProperty(ACTION_PARAM)).booleanValue());
        state.setSecure(Boolean.valueOf(p.getProperty(SECURE_PARAM)).booleanValue());
        state.setWindowState(new WindowState(p.getProperty(WINDOW_STATE_PARAM)));
        state.setPortletMode(new PortletMode(p.getProperty(PORTLET_MODE_PARAM)));

        p.remove(ID_PARAM);
        p.remove(ACTION_PARAM);
        p.remove(SECURE_PARAM);
        p.remove(WINDOW_STATE_PARAM);
        p.remove(PORTLET_MODE_PARAM);

        state.setParameters(new HashMap(p));

        return state;
    }

}
