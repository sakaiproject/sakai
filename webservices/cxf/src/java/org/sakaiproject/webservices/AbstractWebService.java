package org.sakaiproject.webservices;

import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

import javax.jws.WebMethod;
import javax.jws.WebService;

/**
 * Created by jbush on 2/11/14.
 */
@WebService
public class AbstractWebService {
    protected SessionManager sessionManager;

    @WebMethod(exclude = true)
    public void init() {
    }

    /**
     * Get the Session related to the given sessionid
     *
     * @param sessionid the id of the session to retrieve
     * @return the session, if it is active
     * @	if session is inactive
     */
    protected Session establishSession(String sessionid) {
        Session s = sessionManager.getSession(sessionid);

        if (s == null) {
            throw new RuntimeException("Session \"" + sessionid + "\" is not active");
        }
        s.setActive();
        sessionManager.setCurrentSession(s);
        return s;
    }

    @WebMethod(exclude = true)
    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }
}
