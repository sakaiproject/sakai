/*
 * Created on 06-Mar-2006
 */
package uk.ac.cam.caret.sakai.rsf.state;

import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolSession;

import uk.org.ponder.rsf.state.TokenStateHolder;

/**
 * A TokenStateHolder that stores flow state in the Sakai Tool-specific session,
 * falling back to the global Sakai Session if no ToolSession is active
 * (in the case we are operating without a tool placement for some reason).
 * 
 * NB Expiryseconds not yet implemented. Would require *extra* server-side
 * storage of map of tokens to sessions, in order to save long-term storage
 * within sessions - awaiting research from performance clients.
 * 
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 * 
 */
public class InSakaiSessionTSH implements TokenStateHolder {
  // NB - this is a proxy of the request-scope session!!!
  private SessionManager sessionmanager;
  private int expiryseconds;

  public void setSessionManager(SessionManager sessionmanager) {
    this.sessionmanager = sessionmanager;
  }

  public Object getTokenState(String tokenID) {
    ToolSession toolSession = sessionmanager.getCurrentToolSession();
    // In the case ToolSession is not available (perhaps this is an access
    // request) fall back to the global Session.
    return toolSession == null ? sessionmanager.getCurrentSession()
        .getAttribute(tokenID)
        : toolSession.getAttribute(tokenID);
  }

  public void putTokenState(String tokenID, Object trs) {
    ToolSession toolSession = sessionmanager.getCurrentToolSession();
    if (toolSession == null) {
      sessionmanager.getCurrentSession().setAttribute(tokenID, trs);
    }
    else {
      toolSession.setAttribute(tokenID, trs);
    }
  }

  public void clearTokenState(String tokenID) {
    ToolSession toolSession = sessionmanager.getCurrentToolSession();
    if (toolSession == null) {
      sessionmanager.getCurrentSession().removeAttribute(tokenID);
    }
    else {
      sessionmanager.getCurrentToolSession().removeAttribute(tokenID);
    }
  }

  public void setExpirySeconds(int seconds) {
    this.expiryseconds = seconds;
  }

  public String getId() {
    ToolSession toolSession = sessionmanager.getCurrentToolSession();
    return toolSession == null ? sessionmanager.getCurrentSession().getId()
        : toolSession.getId();
  }

}
