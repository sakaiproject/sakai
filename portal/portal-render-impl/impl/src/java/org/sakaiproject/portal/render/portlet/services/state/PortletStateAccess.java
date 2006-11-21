package org.sakaiproject.portal.render.portlet.services.state;

import org.sakaiproject.portal.render.portlet.services.state.PortletState;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.HashMap;

public class PortletStateAccess {

    private static final String PORTLET_STATE_PARAM =
        "org.sakaiproject.portal.pluto.PORTLET_STATE";

    public static PortletState getPortletState(HttpServletRequest request,
                                               String windowId) {
        HttpSession session = request.getSession(true);
        return getContainer(session).get(windowId);
    }

    public static void setPortletState(HttpServletRequest request,
                                       PortletState state) {
        HttpSession session = request.getSession(true);
        getContainer(session).add(state);
    }


    private static PortletStateContainer getContainer(HttpSession session) {
        PortletStateContainer container = (PortletStateContainer)
            session.getAttribute(PORTLET_STATE_PARAM);
        if(container == null) {
            container = new PortletStateContainer();
            session.setAttribute(PORTLET_STATE_PARAM, container);
        }
        return container;
    }

    static class PortletStateContainer {
        private Map stateMap = new HashMap();

        public void add(PortletState state) {
            stateMap.put(state.getId(), state);
        }

        public PortletState get(String windowId) {
            return (PortletState)stateMap.get(windowId);
        }
    }
}
