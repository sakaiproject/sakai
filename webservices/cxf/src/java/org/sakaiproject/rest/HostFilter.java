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
package org.sakaiproject.rest;

import org.sakaiproject.webservices.interceptor.RemoteHostMatcher;
import org.sakaiproject.webservices.interceptor.NoIPRestriction;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * Checks to see if a request should be allowed because it's a public method or if it should be
 * blocked because it's not on the allowed IP range.
 */
@Provider
public class HostFilter implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    @Context
    private RemoteHostMatcher remoteHostMatcher;

    @Context
    private HttpServletRequest request;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (resourceInfo != null) {
            if (resourceInfo.getResourceMethod().getAnnotation(NoIPRestriction.class) == null) {
                requestContext.abortWith(Response.serverError().build());
            }
            if (!remoteHostMatcher.isAllowed(request)) {
                requestContext.abortWith(Response.serverError().build());
            }
        }

    }
}
