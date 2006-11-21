package org.sakaiproject.portal.render.portlet.services;

import org.apache.pluto.PortletWindow;

import javax.servlet.http.HttpServletRequest;

public class PortletAttributesAccess {
    private static final String PORTLET_ATTRIBUTE_PARAM =
            "org.sakaiproject.portal.pluto.PORTLET_ATTRIBUTES";

    public static PortletAttributes getPortletAttributes(HttpServletRequest request, PortletWindow window) {
        String keys = PORTLET_ATTRIBUTE_PARAM + ":" + window.getId();
        PortletAttributes attrs = (PortletAttributes) request.getAttribute(keys);

        if (attrs == null) {
            attrs = new PortletAttributes();
            request.setAttribute(keys, attrs);
        }

        return attrs;
    }
}
