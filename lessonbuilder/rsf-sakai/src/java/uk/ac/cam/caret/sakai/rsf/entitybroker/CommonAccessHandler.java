/*
 * Created on 21 Jul 2008
 */
package uk.ac.cam.caret.sakai.rsf.entitybroker;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.entitybroker.EntityReference;

import uk.org.ponder.beanutil.WBLAcceptor;

public interface CommonAccessHandler {
  public void handleAccess(HttpServletRequest req, HttpServletResponse res,
      EntityReference reference, WBLAcceptor acceptor);
}
