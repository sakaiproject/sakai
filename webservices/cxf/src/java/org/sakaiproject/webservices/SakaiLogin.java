package org.sakaiproject.webservices;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.sakaiproject.component.api.ServerConfigurationService;

import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

@WebService
@SOAPBinding(style= SOAPBinding.Style.RPC, use= SOAPBinding.Use.LITERAL)
public class SakaiLogin extends AbstractWebService {

    private static final Log LOG = LogFactory.getLog(SakaiLogin.class);

    private ServerConfigurationService serverConfigurationService;
    private UsageSessionService usageSessionService;
    private UserDirectoryService userDirectoryService;


    /**
     * Login with the supplied credentials and return the session string which can be used in subsequent web service calls, ie via SakaiScript
     *
     * @param id eid, eg jsmith26
     * @param pw password for the user
     * @return session string
     */
    @WebMethod
    @Path("/login")
    @Produces("text/plain")
    @GET
    public java.lang.String login(
            @WebParam(partName = "id", name = "id")
            @QueryParam("id")
            java.lang.String id,
            @WebParam(partName = "pw", name = "pw")
            @QueryParam("pw")
            java.lang.String pw) {
        Message message = PhaseInterceptorChain.getCurrentMessage();
        HttpServletRequest request = (HttpServletRequest) message.get(AbstractHTTPDestination.HTTP_REQUEST);
        String ipAddress = request.getRemoteAddr();

        boolean allowLogin = serverConfigurationService.getBoolean("webservices.allowlogin", false);

        if (!allowLogin) {
            throw new RuntimeException("Web Services Login Disabled");
        }

        User user = userDirectoryService.authenticate(id, pw);
        if (user != null) {
            Session s = sessionManager.startSession();
            sessionManager.setCurrentSession(s);
            if (s == null) {
                LOG.warn("Web Services Login failed to establish session for id=" + id + " ip=" + ipAddress);
                throw new RuntimeException("Unable to establish session");
            } else {

                // We do not care too much on the off-chance that this fails - folks simply won't show up in presense
                // and events won't be trackable back to people / IP Addresses - but if it fails - there is nothing
                // we can do anyways.

                usageSessionService.login(user.getId(), id, ipAddress, "SakaiLogin.jws", UsageSessionService.EVENT_LOGIN_WS);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Sakai Web Services Login id=" + id + " ip=" + ipAddress + " session=" + s.getId());
                }
                return s.getId();
            }
        }
        LOG.warn("Failed Web Services Login id=" + id + " ip=" + ipAddress);
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
    @Produces("text/plain")
    @GET
    @Path("/logout")
    public boolean logout(
            @QueryParam("sessionid")
            @WebParam(partName = "sessionid", name = "sessionid")
            java.lang.String sessionid) {
        Session s = sessionManager.getSession(sessionid);

        if (s == null) {
            throw new RuntimeException("Session " + sessionid + " is not active");
        }

        sessionManager.setCurrentSession(s);
        usageSessionService.logout();

        return true;
    }

    @WebMethod
    @Produces("text/plain")
    @GET
    @Path("/loginToServer")
    public java.lang.String loginToServer(
            @WebParam(partName = "id", name = "id")
            @QueryParam("id")
            java.lang.String id,
            @WebParam(partName = "pw", name = "pw")
            @QueryParam("pw")
            java.lang.String pw) {
        return login(id, pw) + "," + serverConfigurationService.getString("webservices.directurl", serverConfigurationService.getString("serverUrl"));
    }

    @WebMethod(exclude = true)
    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    @WebMethod(exclude = true)
    public void setUsageSessionService(UsageSessionService usageSessionService) {
        this.usageSessionService = usageSessionService;
    }

    @WebMethod(exclude = true)
    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }
}
