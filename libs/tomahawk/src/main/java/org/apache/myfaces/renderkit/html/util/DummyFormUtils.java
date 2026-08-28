/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.renderkit.html.util;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import jakarta.faces.application.StateManager;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HTML;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.util.FormInfo;

/**
 * Many JSF components can be used without an enclosing h:form. In this
 * case, they need a dummy form to store data into and submit when
 * communication with the server is necessary. These components can use
 * methods on the <code>DummyFormRequestInfo</code> object accessable via this
 * class to register parameters and get the name of a form to submit.
 * <p/>
 * Only one dummy form will be rendered into the response.
 *
 * @author Manfred Geiler (latest modification by $Author: paulsp $)
 * @author Bruno Aranda
 * @version $Revision: 491777 $ $Date: 2007-01-02 06:27:11 -0500 (Tue, 02 Jan 2007) $
 */
public class DummyFormUtils {
    private static final Log log = LogFactory.getLog(DummyFormUtils.class);

    public static final String DUMMY_FORM_NAME = "linkDummyForm";
    private static final String DUMMY_FORM_ID = "linkDummyForm";

    /**
     * Used to store the instance of <code>DummyFormRequestInfo</code> in the request map
     */
    public static final String DUMMY_FORM_INFO = DummyFormUtils.class.getName() + ".DUMMY_FORM_INFO";


    public static String getDummyFormName() {
        return DUMMY_FORM_NAME;
    }

    /**
     * When writeDummyForm is set to true, a <code>DummyFormRequestInfo</code> object will be instantiated
     * and put in the request. Later, if this object is found, the dummyForm javascript will be rendered in the page
     * before the end of the <code>body</code> tag
     *
     * @param facesContext
     * @param writeDummyForm
     */
    public static void setWriteDummyForm(FacesContext facesContext, boolean writeDummyForm) {
        if (!writeDummyForm) {
            return;
        }

        if (!isWriteDummyForm(facesContext)) {
            DummyFormRequestInfo dummyFormInfo = new DummyFormRequestInfo();
            facesContext.getExternalContext().getRequestMap().put(DUMMY_FORM_INFO, dummyFormInfo);
        }
    }

    /**
     * Checks if the DummyFormRequestInfo is already in the request map.
     *
     * @param facesContext
     * @return boolean true, if dummy form is to be written
     */
    public static boolean isWriteDummyForm(FacesContext facesContext) {
        return facesContext.getExternalContext().getRequestMap().containsKey(DUMMY_FORM_INFO);
    }

    /**
     * Delegator method to add a parameter to the DummyFormRequestInfo object in the request
     *
     * @param facesContext
     * @param paramName
     */
    public static void addDummyFormParameter(FacesContext facesContext, String paramName) {
        if (isWriteDummyForm(facesContext)) {
            DummyFormRequestInfo dummyFormInfo = (DummyFormRequestInfo) facesContext.getExternalContext().getRequestMap().get(DUMMY_FORM_INFO);
            dummyFormInfo.addDummyFormParameter(paramName);
        }
        else {
            if (log.isWarnEnabled()) {
                log.warn("Dummy Form parameter was not added because dummy form is not written");
            }
        }
    }

    public static Set getDummyFormParameters(FacesContext facesContext) {
        if (isWriteDummyForm(facesContext)) {
            DummyFormRequestInfo dummyFormInfo = (DummyFormRequestInfo) facesContext.getExternalContext().getRequestMap().get(DUMMY_FORM_INFO);
            return dummyFormInfo.getDummyFormParams();
        }

        return new HashSet();
    }


    public static void writeDummyForm(ResponseWriter writer,
                                      Set dummyFormParams) throws IOException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ViewHandler viewHandler = facesContext.getApplication().getViewHandler();
        String viewId = facesContext.getViewRoot().getViewId();
        String actionURL = viewHandler.getActionURL(facesContext, viewId);

        //write out dummy form
        writer.startElement(org.apache.myfaces.shared_tomahawk.renderkit.html.HTML.FORM_ELEM, null);
        writer.writeAttribute(org.apache.myfaces.shared_tomahawk.renderkit.html.HTML.ID_ATTR, DUMMY_FORM_ID, null);
        writer.writeAttribute(HTML.NAME_ATTR, DUMMY_FORM_NAME, null);
        writer.writeAttribute(HTML.STYLE_ATTR, "display:inline", null);
        writer.writeAttribute(HTML.METHOD_ATTR, "post", null);
        writer.writeURIAttribute(org.apache.myfaces.shared_tomahawk.renderkit.html.HTML.ACTION_ATTR,
                                 facesContext.getExternalContext().encodeActionURL(actionURL),
                                 null);
        writer.flush();

        StateManager stateManager = facesContext.getApplication().getStateManager();
        org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils.writePrettyLineSeparator(facesContext);

        //TODO: Optimize saveSerializedView call, because serialized view is built twice!
        StateManager.SerializedView serializedView = stateManager.saveSerializedView(facesContext);
        // Adam Winer - TOMAHAWK-253: Ideally, this code should be refactored so that the server-side code is also calling StateManager.writeState() too
        //    it's a significant problem that DummyFormUtils has hardcoded knowledge of how the StateManager works.
        if (stateManager.isSavingStateInClient(facesContext)) {
            //render state parameters
            stateManager.writeState(facesContext, serializedView);
        }

        if (org.apache.myfaces.shared_tomahawk.config.MyfacesConfig.getCurrentInstance(facesContext.getExternalContext()).isAutoScroll())
        {
            HtmlRendererUtils.renderAutoScrollHiddenInput(facesContext, writer);
        }

        org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils.writePrettyLineSeparator(facesContext);
        if (dummyFormParams != null) {
            org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils.renderHiddenCommandFormParams(writer, dummyFormParams);
            org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils.renderClearHiddenCommandFormParamsFunction(writer,
                                                                                                                           DUMMY_FORM_NAME,
                                                                                                                           dummyFormParams, null);
        }
        org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils.writePrettyLineSeparator(facesContext);

        writer.endElement(HTML.FORM_ELEM);
    }

    public static FormInfo findNestingForm(UIComponent uiComponent, FacesContext facesContext) {
        FormInfo formInfo = RendererUtils.findNestingForm(uiComponent, facesContext);
        if (formInfo != null) {
            return formInfo;
        }

        DummyFormUtils.setWriteDummyForm(facesContext, true);
        return new FormInfo(null, DUMMY_FORM_NAME);
    }
}
