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
