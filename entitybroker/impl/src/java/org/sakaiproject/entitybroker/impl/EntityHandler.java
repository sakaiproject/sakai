/**
 * EntityHandler.java - created by antranig on 17 May 2007
 **/

package org.sakaiproject.entitybroker.impl;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entitybroker.IdEntityReference;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ReferenceParseable;

/**
 * Common implementation of the handler for the EntityBroker system. This should be used in
 * preference to the EntityBroker directly by implementation classes part of the EntityBroker
 * scheme, rather than the user-facing EntityBroker directly.
 * 
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EntityHandler {

   private EntityProviderManager entityProviderManager;

   public void setEntityProviderManager(EntityProviderManager entityProviderManager) {
      this.entityProviderManager = entityProviderManager;
   }

   private ServerConfigurationService serverConfigurationService;

   public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
      this.serverConfigurationService = serverConfigurationService;
   }

   /**
    * Creates the full URL to an entity using the sakai {@link ServerConfigurationService}, (e.g.
    * http://server:8080/direct/entity/123/)<br/> <br/> <b>Note:</b> the webapp name (relative URL
    * path) of the direct servlet, of "/direct" is hardcoded into this method, and the
    * {@link org.sakaiproject.entitybroker.servlet.DirectServlet} must be deployed there on this
    * server.
    * 
    * @param reference
    *           a globally unique reference to an entity, consists of the entity prefix and the
    *           local id
    * @return the full URL to a specific entity
    */
   public String getEntityURL(String reference) {
      // try to get the prefix to ensure this is at least a valid formatted reference, should this
      // make sure the entity exists?
      EntityReference.getPrefix(reference);
      String togo = serverConfigurationService.getServerUrl() + "/direct" + reference;
      return togo;
   }

   /**
    * Returns the provider, if any, responsible for handling a reference
    */
   public EntityProvider getProvider(String reference) {
      String prefix = EntityReference.getPrefix(reference);
      EntityProvider provider = entityProviderManager.getProviderByPrefix(prefix);
      return provider;
   }

   private EntityReference parseDefaultReference(String prefix, String reference) {
      EntityReference ref = null;
      try {
         ref = new IdEntityReference(reference);
      }
      catch (IllegalArgumentException e) {
         // fall back to the simplest reference type
         ref = new EntityReference(prefix);
      }
      return ref;
   }

   /**
    * Parses an entity reference into the appropriate reference form
    */
   public EntityReference parseReference(String reference) {
      String prefix = EntityReference.getPrefix(reference);
      ReferenceParseable provider = (ReferenceParseable) entityProviderManager
            .getProviderByPrefixAndCapability(prefix, ReferenceParseable.class);
      if (provider == null) {
         return null;
      }
      else if (provider instanceof BlankReferenceParseable) {
         return parseDefaultReference(prefix, reference);
      }
      else {
         Object exemplar = provider.getParsedExemplar();
         if (exemplar.getClass() == EntityReference.class) {
            return new EntityReference(provider.getEntityPrefix());
         }
         else {
            // cannot test this in a meaningful way so the tests are designed to not get here -AZ
            throw new UnsupportedOperationException(
                  "Support for custom EntityReference classes is not yet supported");
         }
      }

   }

}
