/**
 * EntityBrokerHttpAccess.java - created by antranig on 14 May 2007
 */

package org.sakaiproject.entitybroker.impl.oldentity;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.entity.api.EntityNotDefinedException;
import org.sakaiproject.entity.api.EntityPermissionException;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.access.HttpServletAccessProvider;
import org.sakaiproject.entitybroker.access.HttpServletAccessProviderManager;
import org.sakaiproject.entitybroker.impl.EntityHandler;
import org.sakaiproject.tool.api.SessionManager;

/**
 * Implementation of the single HttpAccess instance handling all requests from the AccessServlet to
 * entities managed by the EntityBroker system. This class is no longer used by default since
 * EntityBroker no longer uses the Sakai AccessServlet for exposing entities over Http.
 * 
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 */
public class EntityBrokerHttpAccess implements HttpAccess {

   private static Log log = LogFactory.getLog(EntityBrokerHttpAccess.class);

   private HttpServletAccessProviderManager accessProviderManager;

   public void setAccessProviderManager(HttpServletAccessProviderManager accessProviderManager) {
      this.accessProviderManager = accessProviderManager;
   }

   private SessionManager sessionManager;

   public void setSessionManager(SessionManager sessionManager) {
      this.sessionManager = sessionManager;
   }

   private EntityHandler entityHandler;

   public void setEntityHandler(EntityHandler entityHandler) {
      this.entityHandler = entityHandler;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.sakaiproject.entity.api.HttpAccess#handleAccess(javax.servlet.http.HttpServletRequest,
    *      javax.servlet.http.HttpServletResponse, org.sakaiproject.entity.api.Reference,
    *      java.util.Collection)
    */
   public void handleAccess(HttpServletRequest req, HttpServletResponse res, Reference ref,
         Collection copyrightAcceptedRefs) throws EntityPermissionException,
         EntityNotDefinedException {

      String reference = ref.getReference();
      HttpServletAccessProvider provider = null;
      EntityReference entityref = null;
      try {
         entityref = entityHandler.parseReference(reference);
         String prefix = EntityReference.getPrefix(reference);
         provider = accessProviderManager.getProvider(prefix);
         if (provider == null) {
            throw new IllegalArgumentException("No access provider found for reference "
                  + reference);
         }

      } catch (Exception e) {
         // Log this here since Access Servlet performs no logging
         log.warn("Unable to locate entity for reference " + reference, e);
         throw new EntityNotDefinedException(e.getMessage());
      }

      try {
         provider.handleAccess(req, res, entityref);
      } catch (SecurityException e) {
         throw new EntityPermissionException(sessionManager.getCurrentSessionUserId(), "read",
               reference);
      }

   }

}
