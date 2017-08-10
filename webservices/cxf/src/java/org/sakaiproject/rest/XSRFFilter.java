package org.sakaiproject.rest;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * Check that XSRF tokens match the one set in the session. If they don't match then throw an error.
 */
@Provider
public class XSRFFilter implements ContainerRequestFilter {

    @Context
    private HttpServletRequest servletRequest;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        Session session = ComponentManager.get(SessionManager.class).getCurrentSession();
        Object token = session.getAttribute("XSRF-TOKEN");
        String sent = requestContext.getHeaderString("X-XSRF-TOKEN");
        if (token == null || sent == null || !token.equals(sent)) {
            throw new BadRequestException("XSRF token doesn't match");
        }
    }
}
