package org.sakaiproject.portal.render.portlet.services.state.encode;

import org.sakaiproject.portal.render.portlet.services.state.PortletState;

public interface PortletStateEncoder {
    String encode(PortletState portletState);

    PortletState decode(String uri);
}
