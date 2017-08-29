/**
 * Copyright (c) 2005-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.webservices.interceptor;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.security.AccessDeniedException;
import org.apache.cxf.logging.FaultListener;
import org.apache.cxf.logging.NoOpFaultListener;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.invoker.MethodDispatcher;
import org.apache.cxf.service.model.BindingOperationInfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.ResourceBundle;

/**
 * This checks SOAP requests to make sure that they are permitted based on the IP restrictions and also checks the
 * target of the request to see if it's annotated to mark it as a public method. This is used rather than a servlet
 * filter so that we can look for an annotation on the endpoint that's being accessed.
 *
 * @see NoIPRestriction
 * @see RemoteHostMatcher
 */
public class NetworkAddressValidatingInterceptor extends AbstractPhaseInterceptor<Message> {

    private RemoteHostMatcher remoteHostMatcher;

    public NetworkAddressValidatingInterceptor(RemoteHostMatcher remoteHostHandler) {
        // This needs to be registered to a late phase so that the lookup to the final method has been done.
        super(Phase.USER_LOGICAL);
        this.remoteHostMatcher = remoteHostHandler;
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        // JAX-RS
        Method method = getTargetMethod(message);
        HttpServletRequest request = (HttpServletRequest) message.getContextualProperty("HTTP.REQUEST");
        if (!hasAnnotation(method) && (request == null || !remoteHostMatcher.isAllowed(request))) {
            // This is to prevent a full stack trace getting logged for a denied request
            message.put(FaultListener.class.getName(), new NoOpFaultListener());
            Fault fault = new Fault(
                    new org.apache.cxf.common.i18n.Message("Not permitted", (ResourceBundle) null),
                    Fault.FAULT_CODE_CLIENT
            );
            fault.setStatusCode(HttpServletResponse.SC_FORBIDDEN);
            throw fault;
        }
    }

    protected boolean hasAnnotation(Method method) {
        return method.getAnnotation(NoIPRestriction.class) != null;
    }

    protected Method getTargetMethod(Message m) {
        // Used the SOAP
        BindingOperationInfo bop = m.getExchange().get(BindingOperationInfo.class);
        if (bop != null) {
            MethodDispatcher md = (MethodDispatcher)
                    m.getExchange().get(Service.class).get(MethodDispatcher.class.getName());
            return md.getMethod(bop);
        }
        // Used for JAX-RS
        // This doesn't work for JAX-RS sub-resources as the lookup is only done on the original method, not the
        // sub-resource
        Method method = (Method) m.get("org.apache.cxf.resource.method");
        if (method != null) {
            return method;
        }
        throw new AccessDeniedException("Method is not available : Unauthorized");
    }
}
