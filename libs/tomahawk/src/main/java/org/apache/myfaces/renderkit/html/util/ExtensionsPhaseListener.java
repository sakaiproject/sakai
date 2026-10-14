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
import java.util.List;
import java.util.Map;

import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.shared_tomahawk.config.MyfacesConfig;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HTML;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.util.JavascriptUtils;

/**
 * This phase listener puts in the request the javascript code needed to render the dummyForm
 * and the autoscroll feature.
 *
 * The ExtensionsFilter will put this code before the closing &tt;/body&gt; tag.
 *
 * @author Bruno Aranda (latest modification by $Author: lu4242 $)
 * @version $Revision: 778948 $ $Date: 2009-05-26 20:14:09 -0500 (Tue, 26 May 2009) $
 */
public class ExtensionsPhaseListener implements PhaseListener {

    private static final Log log = LogFactory.getLog(ExtensionsPhaseListener.class);


    public  static final String ORG_APACHE_MYFACES_MY_FACES_JAVASCRIPT = "org.apache.myfaces.myFacesJavascript";
    public static final String LISTENERS_MAP = "_MyFaces_inputAjax_listenersMap";

    public PhaseId getPhaseId()
    {
        return PhaseId.RENDER_RESPONSE;
    }

    public void beforePhase(PhaseEvent event)
    {
    }

    public void afterPhase(PhaseEvent event)
    {
        FacesContext facesContext = event.getFacesContext();

        try
        {
            getJavaScriptCodeAndStoreInRequest(facesContext);
        } catch (IOException e)
        {
            log.error("Exception while rendering extension filter code.",e);
        }
    }

    /**
     * Creates javascript-code such as the dummy form and the autoscroll javascript, which goes before the closing &lt;/body&gt;
     *
     * The extension filter will then finally process it and render it into the page.
     * 
     * @throws IOException an exception if writer cannot be written to
     * @param facesContext The current faces-context
     */
    private void getJavaScriptCodeAndStoreInRequest(FacesContext facesContext) throws IOException
    {
        Object myFacesJavascript = facesContext.getExternalContext().getRequestMap().get(ORG_APACHE_MYFACES_MY_FACES_JAVASCRIPT);

        if (myFacesJavascript != null)
        {
            return;
        }

        facesContext.getExternalContext().getRequestMap().put(ORG_APACHE_MYFACES_MY_FACES_JAVASCRIPT, getCodeBeforeBodyEnd(facesContext));
    }
    
    private static String getCodeBeforeBodyEnd(FacesContext facesContext) throws IOException
    {
        ResponseWriter responseWriter = facesContext.getResponseWriter();
        HtmlBufferResponseWriterWrapper writerWrapper = HtmlBufferResponseWriterWrapper
                    .getInstance(responseWriter);
        facesContext.setResponseWriter(writerWrapper);

        writeCodeBeforeBodyEnd(facesContext);

        //todo: this is just a quick-fix - Sun RI screams if the old responsewriter is null
        //but how to reset the old response-writer then?
        if(responseWriter!=null)
            facesContext.setResponseWriter(responseWriter);

        //return "<!-- MYFACES JAVASCRIPT -->\n"+writerWrapper.toString()+"\n";
        return writerWrapper.toString();
    }

    /**In case of StreamingAddResource and a documentBody-Tag, this method will be called with the
     * normal response writer set.
     *
     * It will directly render out the javascript-text at the current position (immediately before body-closing).
     *
     * In the case of DefaultAddResource, this method will be called with a wrapped response writer - and we'll
     * buffer the javascript-text in the request, for the ExtensionFilter to catch and render it.
     *
     * @param facesContext The current faces-context.
     * @throws IOException Exception if writing to the output-stream fails.
     */
    public static void writeCodeBeforeBodyEnd(FacesContext facesContext) throws IOException
    {
        ResponseWriter writer = facesContext.getResponseWriter();
        
        MyfacesConfig myfacesConfig = MyfacesConfig.getCurrentInstance(facesContext.getExternalContext());
        if (myfacesConfig.isDetectJavascript())
        {
            if (! JavascriptUtils.isJavascriptDetected(facesContext.getExternalContext()))
            {

                writer.startElement("script",null);
                writer.writeAttribute("attr", HTML.SCRIPT_TYPE_TEXT_JAVASCRIPT,null);
                StringBuffer script = new StringBuffer();
                script.append("document.location.replace('").
                        append(facesContext.getApplication().getViewHandler().getResourceURL(facesContext, "/_javascriptDetector_")).append("?goto=").append(facesContext.getApplication().getViewHandler().getActionURL(facesContext, facesContext.getViewRoot().getViewId())).append("');");
                writer.writeText(script.toString(),null);
                writer.endElement(HTML.SCRIPT_ELEM);
            }
        }

        if (myfacesConfig.isAutoScroll())
        {
            HtmlRendererUtils.renderAutoScrollFunction(facesContext, writer);
        }

        // now write out listeners
        // todo: change the get entry below to use the static field in Listener if/when the listeners move to Tomahawk from sandbox
        try
        {
            List listeners = (List) facesContext.getExternalContext().getRequestMap().get("org.apache.myfaces.Listener");
            //System.out.println("listeners: " + listeners);
            if(listeners != null && listeners.size() > 0){
                writer.startElement(HTML.SCRIPT_ELEM,null);
                writer.writeAttribute("attr", HTML.SCRIPT_TYPE_TEXT_JAVASCRIPT,null);
                StringBuffer buff = new StringBuffer();
                String mapName = LISTENERS_MAP;
                buff.append("var ").append(mapName).append(" = new Object();\n");
                for (int i = 0; i < listeners.size(); i++)
                {
                    Map listenerItem = (Map) listeners.get(i);
                    String listenerId = (String) listenerItem.get("listenerId");
                    String listenOn = (String) listenerItem.get("listenOn");
                    String action = (String) listenerItem.get("action");
                    String eventType = (String) listenerItem.get("eventType");
                    // todo: Should use Listener object for more flexibility, but only when it moves to tomahawk
                    buff.append("var _MyFaces_listenerItem = ").append(mapName).append("['").append(listenOn).append("'];\n");
                    buff.append("if(!_MyFaces_listenerItem) {\n");
                    buff.append("    _MyFaces_listenerItem = new Array();\n");
                    buff.append("    ").append(mapName).append("['").append(listenOn).append("'] = _MyFaces_listenerItem;\n");
                    buff.append("}\n");
                    buff.append("var _MyFaces_listener = new Object();\n");
                    buff.append("_MyFaces_listener['id'] = '").append(listenerId).append("';\n");
                    buff.append("_MyFaces_listener['action'] = '").append(action).append("';\n");
                    buff.append("_MyFaces_listener['eventType'] = '").append(eventType).append("';\n");
                    buff.append("_MyFaces_listenerItem[_MyFaces_listenerItem.length] = _MyFaces_listener;\n");
                }
                writer.write(buff.toString());
                writer.endElement(HTML.SCRIPT_ELEM);
            }
        }
        catch (Exception e)
        {
            log.error("Exception while rendering code for listeners.",e);
        }
    }
}
