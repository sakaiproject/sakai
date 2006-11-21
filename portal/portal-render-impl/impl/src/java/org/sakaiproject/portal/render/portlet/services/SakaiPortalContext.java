package org.sakaiproject.portal.render.portlet.services;

import javax.portlet.PortalContext;
import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import java.util.*;


public class SakaiPortalContext implements PortalContext {

    private ArrayList modes;
    private ArrayList states;

    private Map properties;

    public SakaiPortalContext() {
        properties = new HashMap();
        modes = new ArrayList();
        states = new ArrayList();

        modes.add(PortletMode.VIEW);
        modes.add(PortletMode.HELP);
        modes.add(PortletMode.EDIT);

        states.add(WindowState.MAXIMIZED);
        states.add(WindowState.MINIMIZED);
        states.add(WindowState.NORMAL);
    }


    public SakaiPortalContext(Map properties) {
        this.properties = properties;
    }

    public String getProperty(String key) {
        return (String)properties.get(key);
    }

    public Enumeration getPropertyNames() {
        return new IteratorEnumeration(properties.keySet().iterator());
    }

    public Enumeration getSupportedPortletModes() {
        return new IteratorEnumeration(modes.iterator());
    }

    public Enumeration getSupportedWindowStates() {
        return new IteratorEnumeration(states.iterator());
    }

    public String getPortalInfo() {
        return "Charon/2.3-SNAPSHOT";
    }

    class IteratorEnumeration implements Enumeration {

        private Iterator iterator;


        public IteratorEnumeration(Iterator iterator) {
            this.iterator = iterator;
        }

        public boolean hasMoreElements() {
            return iterator.hasNext();
        }

        public Object nextElement() {
            return iterator.next();
        }
    }
}
