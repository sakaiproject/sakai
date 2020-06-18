/**
 * Copyright (c) 2005 The Apereo Foundation
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
package org.sakaiproject.webservices;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import javax.ws.rs.core.MediaType;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.user.api.Authentication;
import org.sakaiproject.user.api.AuthenticationException;
import org.sakaiproject.user.api.Evidence;
import org.sakaiproject.util.IdPwEvidence;
import org.sakaiproject.util.RequestFilter;

@WebService
@SOAPBinding(style= SOAPBinding.Style.RPC, use= SOAPBinding.Use.LITERAL)
@Slf4j
public class SakaiLogin extends AbstractWebService {

    protected boolean m_displayModJkWarning = true;
    protected String cookieName = "JSESSIONID";

    //I don't see a simpler way of doing this 
    //https://stackoverflow.com/a/13408147/3708872
    
    /**
     * Login with the supplied credentials and return the session string which can be used in subsequent web service calls, ie via SakaiScript
     *
     * @param id eid, eg jsmith26
     * @param pw password for the user
     * @return session string
     */
    @WebMethod
    @Path("/login")
    @Produces(MediaType.TEXT_PLAIN)
    @GET
    public java.lang.String loginGET(
            @WebParam(partName = "id", name = "id")
            @QueryParam("id")
            java.lang.String id,
            @WebParam(partName = "pw", name = "pw")
            @QueryParam("pw")
            java.lang.String pw) {
    	return login(id,pw);
    }

    /**
     * Login with the supplied credentials and return the session string which can be used in subsequent web service calls, ie via SakaiScript
     *
     * @param id eid, eg jsmith26
     * @param pw password for the user
     * @return session string
     */
    @WebMethod(operationName="login")
    @Path("/login")
    @Produces(MediaType.TEXT_PLAIN)
    //Can't get MediaType.MULTIPART_FORM_DATA to work
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @POST
    public java.lang.String loginPOST(
            @WebParam(partName = "id", name = "id")
            @FormParam("id")
            java.lang.String id,
            @WebParam(partName = "pw", name = "pw")
            @FormParam("pw")
            java.lang.String pw) {
    	return login (id,pw);
    }
    
    /**
     * Actual login method
     * @param id
     * @param pw
     * @return
     */
    private java.lang.String login(java.lang.String id, java.lang.String pw) {

        Message message = PhaseInterceptorChain.getCurrentMessage();
        HttpServletRequest request = (HttpServletRequest) message.get(AbstractHTTPDestination.HTTP_REQUEST);
        String ipAddress = request.getRemoteAddr();

        boolean allowLogin = serverConfigurationService.getBoolean("webservices.allowlogin", false);

        if (!allowLogin) {
            throw new RuntimeException("Web Services Login Disabled");
        }

        try {
            if ("GET".equals(request.getMethod())) {
                log.info("This endpoint {} should use POST instead of GET, GET will be deprecated in a future release", request.getRequestURI());
            }

            Evidence e = new IdPwEvidence(id, pw, ipAddress);
            Authentication a = authenticationManager.authenticate(e);

            Session s = sessionManager.startSession();
            sessionManager.setCurrentSession(s);

            if (s == null) {
                log.warn("Web Services Login failed to establish session for id=" + id + " ip=" + ipAddress);
                throw new RuntimeException("Unable to establish session");
            } else {
                // We do not care too much on the off-chance that this fails - folks simply won't show up in presense
                // and events won't be trackable back to people / IP Addresses - but if it fails - there is nothing
                // we can do anyways.

                usageSessionService.login(a.getUid(), id, ipAddress, "SakaiLogin", UsageSessionService.EVENT_LOGIN_WS);

                log.debug("Sakai Web Services Login id={} ip={} session={}", id, ipAddress, s.getId());

                // retrieve the configured cookie name, if any
                if (System.getProperty(RequestFilter.SAKAI_COOKIE_PROP) != null) {
                    cookieName = System.getProperty(RequestFilter.SAKAI_COOKIE_PROP);
                }

                // retrieve the configured cookie domain, if any

                // compute the session cookie suffix, based on this configured server id
                String suffix = System.getProperty(RequestFilter.SAKAI_SERVERID);
                if (StringUtils.isEmpty(suffix)) {
                    if (m_displayModJkWarning) {
                        log.warn("no sakai.serverId system property set - mod_jk load balancing will not function properly");
                    }
                    m_displayModJkWarning = false;
                    suffix = "sakai";
                }

                Cookie c = new Cookie(cookieName, s.getId() + "." + suffix);
                c.setPath("/");
                c.setMaxAge(-1);
                if (System.getProperty(RequestFilter.SAKAI_COOKIE_DOMAIN) != null) {
                    c.setDomain(System.getProperty(RequestFilter.SAKAI_COOKIE_DOMAIN));
                }
                if (request.isSecure() == true) {
                    c.setSecure(true);
                }

                HttpServletResponse res = (HttpServletResponse) message.get(AbstractHTTPDestination.HTTP_RESPONSE);

                if (res != null) {
                    res.addCookie(c);
                }

                log.debug("Sakai Web Services Login id={} ip={} session={}", id, ipAddress, s.getId());
                return s.getId();
            }
        } catch (AuthenticationException ex) {
            log.warn("Failed Web Services Login id=" + id + " ip=" + ipAddress + ": " + ex.getMessage());
        }

        throw new RuntimeException("Unable to login");
    }

    /**
     * Logout of the given session
     *
     * @param sessionid sessionid to logout
     * @return
     * @throws InterruptedException
     */
    @WebMethod
    @Produces(MediaType.TEXT_PLAIN)
    @GET
    @Path("/logout")
    public boolean logoutGET(
            @QueryParam("sessionid")
            @WebParam(partName = "sessionid", name = "sessionid")
            java.lang.String sessionid) {
    	return logout(sessionid);
    }
    
    /**
     * Logout of the given session
     *
     * @param sessionid sessionid to logout
     * @return
     * @throws InterruptedException
     */
    @WebMethod(operationName="logout")
    @Produces(MediaType.TEXT_PLAIN)
    //Can't get MediaType.MULTIPART_FORM_DATA to work
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @POST
    @Path("/logout")
    public boolean logoutPOST(
            @FormParam("sessionid")
            @WebParam(partName = "sessionid", name = "sessionid")
            java.lang.String sessionid) {
    	return logout (sessionid);
    }
    
    /**
     * Actual logout method
     *
     * @param sessionid sessionid to logout
     * @return
     * @throws InterruptedException
     */
    private boolean logout(java.lang.String sessionid) {
        Session s = sessionManager.getSession(sessionid);

        if (s == null) {
            throw new RuntimeException("Session " + sessionid + " is not active");
        }

        sessionManager.setCurrentSession(s);
        usageSessionService.logout();

        return true;
    }

    @WebMethod
    @Produces(MediaType.TEXT_PLAIN)
    @GET
    @Path("/loginToServer")
    public java.lang.String loginToServerGET(
            @WebParam(partName = "id", name = "id")
            @QueryParam("id")
            java.lang.String id,
            @WebParam(partName = "pw", name = "pw")
            @QueryParam("pw")
            java.lang.String pw) {
        return login(id, pw) + "," + serverConfigurationService.getString("webservices.directurl", serverConfigurationService.getString("serverUrl"));
    }

    @WebMethod(operationName="loginToServer")
    @Produces(MediaType.TEXT_PLAIN)
    //Can't get MediaType.MULTIPART_FORM_DATA to work
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @POST
    @Path("/loginToServer")
    public java.lang.String loginToServerPOST(
            @WebParam(partName = "id", name = "id")
            @FormParam("id")
            java.lang.String id,
            @WebParam(partName = "pw", name = "pw")
            @FormParam("pw")
            java.lang.String pw) {
        return login(id, pw) + "," + serverConfigurationService.getString("webservices.directurl", serverConfigurationService.getString("serverUrl"));
    }
}
