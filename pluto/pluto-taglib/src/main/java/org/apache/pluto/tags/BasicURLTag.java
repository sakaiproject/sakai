/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pluto.tags;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.PortletModeException;
import javax.portlet.PortletSecurityException;
import javax.portlet.PortletURL;
import javax.portlet.WindowState;
import javax.portlet.WindowStateException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.tagext.VariableInfo;

/**
 * Supporting class for the <CODE>actionURL</CODE> and <CODE>renderURL</CODE>
 * tag. Creates a url that points to the current Portlet and triggers an action
 * request with the supplied parameters.
 */
public abstract class BasicURLTag extends TagSupport {

    public static class TEI extends TagExtraInfo {
        public final static Hashtable definedWindowStates = getDefinedWindowStates();
        public final static Hashtable portletModes = getDefinedPortletModes();

        /**
         * Provides a list of all static PortletMode available in the
         * specifications by using introspection
         *
         * @return Hashtable
         */
        private static Hashtable getDefinedPortletModes() {
            Hashtable portletModes = new Hashtable();
            Field[] f = PortletMode.class.getDeclaredFields();

            for (int i = 0; i < f.length; i++) {
                if (f[i].getType().isAssignableFrom(
                    javax.portlet.PortletMode.class)) {
                    try {
                        portletModes.put(
                            f[i].get(f[i]).toString().toUpperCase(),
                            f[i].get(f[i]));
                    } catch (IllegalAccessException e) {
                    }
                }
            }

            return portletModes;
        }

        /**
         * Provides a list of all static WindowsStates available in the
         * specifications by using introspection
         *
         * @return Hashtable
         */
        private static Hashtable getDefinedWindowStates() {
            Hashtable definedWindowStates = new Hashtable();
            Field[] f = WindowState.class.getDeclaredFields();

            for (int i = 0; i < f.length; i++) {
                if (f[i].getType().isAssignableFrom(
                    javax.portlet.WindowState.class)) {
                    try {
                        definedWindowStates.put(
                            f[i].get(f[i]).toString().toUpperCase(),
                            f[i].get(f[i]));
                    } catch (IllegalAccessException e) {

                    }
                }
            }
            return definedWindowStates;
        }

        public VariableInfo[] getVariableInfo(TagData tagData) {
            VariableInfo vi[] = null;
            String var = tagData.getAttributeString("var");
            if (var != null) {
                vi = new VariableInfo[1];
                vi[0] =
                    new VariableInfo(var, "java.lang.String", true,
                        VariableInfo.AT_BEGIN);
            }
            return vi;
        }

    }

    protected String portletMode;
    protected String secure;
    protected Boolean secureBoolean;
    protected String windowState;
    protected PortletURL url;
    protected String var;
    protected Map parameters;

    /**
     * Processes the <CODE>actionURL</CODE> or <CODE>renderURL</CODE> tag.
     *
     * @return int
     */

    /* (non-Javadoc)
    * @see javax.servlet.jsp.tagext.Tag#doStartTag()
    */
    public int doStartTag() throws JspException {
        if (var != null) {
            pageContext.removeAttribute(var, PageContext.PAGE_SCOPE);
        }

        url = createPortletURL();
        parameters = new LinkedHashMap();

        if (portletMode != null) {
            try {
                PortletMode mode = (PortletMode)
                    TEI.portletModes.get(portletMode.toUpperCase());
                if (mode == null) {
                    mode = new PortletMode(portletMode);
                }
                url.setPortletMode(mode);
            } catch (PortletModeException e) {
                throw new JspException(e);
            }
        }
        if (windowState != null) {
            try {
                WindowState state = (WindowState)
                    TEI.definedWindowStates.get(windowState.toUpperCase());
                if (state == null) {
                    state = new WindowState(windowState);
                }
                url.setWindowState(state);
            } catch (WindowStateException e) {
                throw new JspException(e);
            }
        }
        if (secure != null) {
            try {
                url.setSecure(getSecureBoolean());
            } catch (PortletSecurityException e) {
                throw new JspException(e);
            }
        }

        return EVAL_BODY_INCLUDE;
    }

    protected abstract PortletURL createPortletURL();
    
    protected void addParameter(String name, String value) {
        List values = (List)this.parameters.get(name);
        if (values == null) {
            values = new LinkedList();
            this.parameters.put(name, values);
        }
        values.add(value);
    }

    /**
     * @return int
     */
    public int doEndTag() throws JspException {
        //Add all parameters to the URL
        for (final Iterator paramEntryItr = this.parameters.entrySet().iterator(); paramEntryItr.hasNext();) {
            final Map.Entry paramEntry = (Map.Entry)paramEntryItr.next();
            final String name = (String)paramEntry.getKey();
            final List values = (List)paramEntry.getValue();
            url.setParameter(name, (String[])values.toArray(new String[values.size()]));
        }
        
        HttpServletResponse response = (HttpServletResponse)pageContext.getResponse();
        if (var == null) {
            try {
                JspWriter writer = pageContext.getOut();
                writer.print(response.encodeURL(url.toString()));
            } catch (IOException ioe) {
                throw new JspException(
                    "actionURL/renderURL Tag Exception: cannot write to the output writer.");
            }
        } else {
            pageContext.setAttribute(var, response.encodeURL(url.toString()),
                PageContext.PAGE_SCOPE);
        }
        
        this.url = null;
        this.parameters = null;
        
        return EVAL_PAGE;
    }

    /**
     * Returns the portletMode.
     *
     * @return String
     */
    public String getPortletMode() {
        return portletMode;
    }

    /**
     * @return secure as String
     */
    public String getSecure() {
        return secure;
    }

    /**
     * @return secure as Boolean
     */
    public boolean getSecureBoolean() {
        return this.secureBoolean.booleanValue();
    }

    /**
     * Returns the windowState.
     *
     * @return String
     */
    public String getWindowState() {
        return windowState;
    }

    /**
     * @return PortletURL
     */
    public PortletURL getUrl() {
        return url;
    }

    /**
     * Returns the var.
     *
     * @return String
     */
    public String getVar() {
        return var;
    }

    /**
     * Sets the portletMode.
     *
     * @param portletMode The portletMode to set
     */
    public void setPortletMode(String portletMode) {
        this.portletMode = portletMode;
    }

    /**
     * Sets secure to boolean value of the string
     *
     * @param secure
     */
    public void setSecure(String secure) {
        this.secure = secure;
        this.secureBoolean = new Boolean(secure);
    }

    /**
     * Sets the windowState.
     *
     * @param windowState The windowState to set
     */
    public void setWindowState(String windowState) {
        this.windowState = windowState;
    }

    /**
     * Sets the var.
     *
     * @param var The var to set
     */
    public void setVar(String var) {
        this.var = var;
    }
}
