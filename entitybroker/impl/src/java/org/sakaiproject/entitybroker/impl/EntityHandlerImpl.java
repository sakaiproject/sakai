/**
 * $Id$
 * $URL$
 * EntityHandler.java - entity-broker - Apr 6, 2008 9:03:03 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entitybroker.EntityRequestHandler;
import org.sakaiproject.entitybroker.IdEntityReference;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.access.HttpServletAccessProvider;
import org.sakaiproject.entitybroker.access.HttpServletAccessProviderManager;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ReferenceParseable;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.util.ClassLoaderReporter;

/**
 * Common implementation of the handler for the EntityBroker system. This should be used in
 * preference to the EntityBroker directly by implementation classes part of the EntityBroker
 * scheme, rather than the user-facing EntityBroker directly.
 * 
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EntityHandlerImpl implements EntityRequestHandler {

   private EntityProviderManager entityProviderManager;
   public void setEntityProviderManager(EntityProviderManager entityProviderManager) {
      this.entityProviderManager = entityProviderManager;
   }

   private HttpServletAccessProviderManager accessProviderManager;
   public void setAccessProviderManager(HttpServletAccessProviderManager accessProviderManager) {
      this.accessProviderManager = accessProviderManager;
   }

   private ServerConfigurationService serverConfigurationService;
   public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
      this.serverConfigurationService = serverConfigurationService;
   }


   /* (non-Javadoc)
    * @see org.sakaiproject.entitybroker.EntityRequestHandler#handleEntityAccess(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
    */
   public String handleEntityAccess(HttpServletRequest req, HttpServletResponse res, String path) {
      // get the path info if not set
      if (path == null) {
         path = req.getPathInfo();
      }

      
      EntityReference ref;
      try {
         ref = parseReference(path);
      } catch (IllegalArgumentException e) {
         // indicates we could not parse the reference
         throw new EntityException("Could not parse entity path ("+path+"): " + e.getMessage(), path, HttpServletResponse.SC_BAD_REQUEST);
      }

      if (ref == null) {
         // no provider for this entity prefix
         throw new EntityException( "No entity provider could be found to handle the prefix in this path: " + path, 
               path, HttpServletResponse.SC_NOT_IMPLEMENTED );
      } else if (! entityExists(ref.toString())) {
         // reference parsing failure
         String message = "Attempted to access an entity URL path (" + path + ") for an entity ("
            + ref.toString() + ") that does not exist";
         throw new EntityException( message, ref.toString(), HttpServletResponse.SC_NOT_FOUND );
      } else {
         // reference successfully parsed
         
         // TODO check for special handling

         // no special handling so send on to the standard access provider if one can be found
         HttpServletAccessProvider accessProvider = 
            accessProviderManager.getProvider(ref.prefix);
         if (accessProvider == null) {
            String message = "Attempted to access an entity URL path ("
                        + path + ") for an entity (" + ref.toString()
                        + ") when there is no HttpServletAccessProvider to handle the request for prefix ("
                        + ref.prefix + ")";
            throw new EntityException( message, ref.toString(), HttpServletResponse.SC_METHOD_NOT_ALLOWED );
         } else {
            ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
            try {
               ClassLoader newClassLoader = accessProvider.getClass().getClassLoader();
               // check to see if this access provider reports the correct classloader
               if (accessProvider instanceof ClassLoaderReporter) {
                  newClassLoader = ((ClassLoaderReporter) accessProvider).getSuitableClassLoader();
               }
               Thread.currentThread().setContextClassLoader(newClassLoader);
               // send request to the access provider which will route it on to the correct entity world
               accessProvider.handleAccess(req, res, ref);
            } finally {
               Thread.currentThread().setContextClassLoader(currentClassLoader);
            }
         }
      }

      return ref.toString();
   }

   /**
    * Determines if an entity exists based on the reference
    * 
    * @param reference a globally unique reference to an entity, 
    * consists of the entity prefix and optionaly the local id
    * @return true if entity exists, false otherwise
    */
   public boolean entityExists(String reference) {
      String prefix = IdEntityReference.getPrefix(reference);
      EntityProvider provider = entityProviderManager.getProviderByPrefix(prefix);
      if (provider == null) {
         // no provider found so no entity can exist
         return false;
      } else if (!(provider instanceof CoreEntityProvider)) {
         return true;
      }
      return ((CoreEntityProvider) provider).entityExists(IdEntityReference.getID(reference));
   }

   /**
    * Creates the full URL to an entity using the sakai {@link ServerConfigurationService}, (e.g.
    * http://server:8080/direct/entity/123/)<br/>
    * <br/>
    * <b>Note:</b> the webapp name (relative URL path) of the direct servlet, of "/direct" 
    * is hardcoded into this method, and the
    * {@link org.sakaiproject.entitybroker.servlet.DirectServlet} must be deployed there on this
    * server.
    * 
    * @param reference a globally unique reference to an entity, 
    * consists of the entity prefix and optionaly the local id
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

   /**
    * Parses an entity reference into the appropriate reference form
    * 
    * @param reference a unique entity reference
    * @return null if there is no provider found for the prefix parsed out
    * @throws IllegalArgumentException if there is a failure during parsing
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

   /**
    * Standard way to parse the reference, attempts to get the id and
    * if fails then just uses the prefix only
    * 
    * @param prefix only pass valid prefixes to this method
    * @param reference a unique entity reference
    * @return an {@link EntityReference}
    */
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


}
