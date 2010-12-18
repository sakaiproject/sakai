/*
 * Created on 22-Nov-2006
 */
package uk.ac.cam.caret.sakai.rsf.locale;

import java.util.Locale;

import org.sakaiproject.tool.api.SessionManager;

import uk.org.ponder.localeutil.LocaleSetter;

public class SakaiLocaleSetter implements LocaleSetter {

  private SessionManager sessionManager;

  public void setLocale(Locale toset) {
    sessionManager.getCurrentSession().setAttribute("locale", toset);
  }

  public void setSessionManager(SessionManager sessionManager) {
    this.sessionManager = sessionManager;
  }

}
