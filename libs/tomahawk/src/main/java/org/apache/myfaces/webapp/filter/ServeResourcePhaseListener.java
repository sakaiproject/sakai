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
package org.apache.myfaces.webapp.filter;

import jakarta.faces.FacesException;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.renderkit.html.util.AddResource;
import org.apache.myfaces.renderkit.html.util.AddResourceFactory;
import org.apache.myfaces.tomahawk.util.ExternalContextUtils;

/**
 * This listener is used for serve resources, as a replacement of 
 * ExtensionsFilter serve resources feature.
 * <p>
 * The idea is map FacesServlet to org.apache.myfaces.RESOURCE_VIRTUAL_PATH
 * (Default is "/faces/myFacesExtensionResource), so this
 * listener can receive the request.
 * </p>
 * 
 * @author Martin Marinschek (latest modification by $Author: lu4242 $)
 * @version $Revision: 685725 $ $Date: 2008-08-13 18:14:31 -0500 (mié, 13 ago 2008) $
 */
public class ServeResourcePhaseListener implements PhaseListener {

    /**
     * 
     */
    private static final long serialVersionUID = -1044924474445136434L;

    private Log log = LogFactory.getLog(ServeResourcePhaseListener.class);

    public static final String DOLISTENER_CALLED = "org.apache.myfaces.component.html.util.ExtensionFilter.doListenerCalled";

    public void afterPhase(PhaseEvent event) {
    }

    public void beforePhase(PhaseEvent event) {
        if(event.getPhaseId()==PhaseId.RESTORE_VIEW || event.getPhaseId()==PhaseId.RENDER_RESPONSE) {

            FacesContext fc = event.getFacesContext();
            ExternalContext externalContext = event.getFacesContext().getExternalContext();

            if(externalContext.getRequestMap().containsKey(ExtensionsFilter.DOFILTER_CALLED) ||
               externalContext.getRequestMap().containsKey(DOLISTENER_CALLED))
            {
                //we have already been called (before-restore-view, and we are now in render-response),
                // no need to do everything again...
                return;
            }

            externalContext.getRequestMap().put(DOLISTENER_CALLED,"true");

            //Use ExternalContextUtils to find if this is a portled request
            //if(externalContext.getRequest() instanceof PortletRequest) {            
            if(ExternalContextUtils.getRequestType(externalContext).isPortlet()) {
                //we are in portlet-world! in portlet 1.0 (JSR-168), we cannot do anything here, but
                //TODO in portlet 2.0 (JSR-286), we will write the resource to the stream here if we
                //get a resource-request (resource-requests are only available in 286)
                if(log.isDebugEnabled()) {
                    log.debug("We are in portlet-space, but we cannot do anything here in JSR-168 - " +
                            "for resource-serving, our resource-servlet has to be registered.");
                }
            }
            else if(externalContext.getResponse() instanceof HttpServletResponse) {

                HttpServletResponse response = (HttpServletResponse) fc.getExternalContext().getResponse();
                HttpServletRequest request = (HttpServletRequest) fc.getExternalContext().getRequest();
                ServletContext context = (ServletContext) fc.getExternalContext().getContext();

                // Serve resources
                AddResource addResource;

                try
                {
                    addResource= AddResourceFactory.getInstance(request, context);
                    if( addResource.isResourceUri(context, request ) ){
                        addResource.serveResource(context, request, response);
                        event.getFacesContext().responseComplete();
                        return;
                    }
                }
                catch(Throwable th)
                {
                    log.error("Exception wile retrieving addResource",th);
                    throw new FacesException(th);
                }
            }
            else {
                if(log.isDebugEnabled()) {
                    log.debug("Response of type : "+(
                            externalContext.getResponse()==null?"null":externalContext.getResponse().getClass().getName())+" not handled so far.");
                }
            }
        }
    }

    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE;
    }
}
