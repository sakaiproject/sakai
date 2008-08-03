/**
 * $Id$
 * $URL$
 * EntityBrokerManager.java - entity-broker - Jul 22, 2008 11:33:39 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.impl;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.EntityViewUrlCustomizable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ReferenceParseable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.util.reflect.ReflectUtil;


/**
 * This is the internal service for handling entities,
 * most of the work done by entity broker is handled here<br/>
 * This should be used in
 * preference to the EntityBroker directly by implementation classes 
 * that are part of the EntityBroker system, 
 * rather than the user-facing EntityBroker directly.
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 */
public class EntityBrokerManager {

   /**
    * must match the name of the direct servlet
    */
   protected static final String DIRECT = "/direct";
   protected static final String POST_METHOD = "_method";

   private EntityProviderManager entityProviderManager;
   public void setEntityProviderManager(EntityProviderManager entityProviderManager) {
      this.entityProviderManager = entityProviderManager;
   }

   private ServerConfigurationService serverConfigurationService;
   public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
      this.serverConfigurationService = serverConfigurationService;
   }

   private ReflectUtil reflectUtil = new ReflectUtil();
   public ReflectUtil getReflectUtil() {
      return reflectUtil;
   }

   /**
    * Determines if an entity exists based on the reference
    * 
    * @param reference an entity reference object
    * @return true if entity exists, false otherwise
    */
   public boolean entityExists(EntityReference ref) {
      boolean exists = false;
      if (ref != null) {
         EntityProvider provider = entityProviderManager.getProviderByPrefix(ref.getPrefix());
         if (provider == null) {
            // no provider found so no entity can't exist
            exists = false;
         } else if (!(provider instanceof CoreEntityProvider)) {
            // no core provider so assume it does exist
            exists = true;
         } else {
            if (ref.getId() == null) {
               // currently we assume exists if it is only a prefix
               exists = true;
            } else {
               exists = ((CoreEntityProvider) provider).entityExists( ref.getId() );
            }
         }
      }
      return exists;
   }

   /**
    * Creates the full URL to an entity using the sakai {@link ServerConfigurationService}, 
    * (e.g. http://server:8080/direct/entity/123/)<br/>
    * <br/>
    * <b>Note:</b> the webapp name (relative URL path) of the direct servlet, of "/direct" 
    * is hardcoded into this method, and the
    * {@link org.sakaiproject.entitybroker.servlet.DirectServlet} must be deployed there on this
    * server.
    * 
    * @param reference a globally unique reference to an entity, 
    * consists of the entity prefix and optionally the local id
    * @param viewKey the specific view type to get the URL for,
    * can be null to determine the key automatically
    * @param extension the optional extension to add to the end,
    * can be null to use no extension
    * @return the full URL to a specific entity or space
    */
   public String getEntityURL(String reference, String viewKey, String extension) {
      // ensure this is a valid reference first
      EntityReference ref = parseReference(reference);
      EntityView view = makeEntityView(ref, viewKey, extension);
      String url = makeFullURL(view.toString());
      return url;
   }

   /**
    * Make a full URL (http://....) from just a path URL (/prefix/id.xml)
    */
   protected String makeFullURL(String pathURL) {
      String url = serverConfigurationService.getServerUrl() + DIRECT + pathURL;
      return url;
   }

   /**
    * Reduce code duplication and ensure custom templates are used
    */
   public EntityView makeEntityView(EntityReference ref, String viewKey, String extension) {
      EntityView view = new EntityView();
      EntityViewUrlCustomizable custom = (EntityViewUrlCustomizable) entityProviderManager
            .getProviderByPrefixAndCapability(ref.getPrefix(), EntityViewUrlCustomizable.class);
      if (custom == null) {
         view.setEntityReference(ref);
      } else {
         // use the custom parsing templates
         view.loadParseTemplates( custom.getParseTemplates() );
      }
      view.setEntityReference(ref);
      if (viewKey != null) {
         view.setViewKey(viewKey);
      }
      if (extension != null) {
         view.setExtension(extension);
      }
      return view;
   }

   /**
    * Parses an entity reference into the appropriate reference form
    * 
    * @param reference a unique entity reference
    * @return the entity reference object or 
    * null if there is no provider found for the prefix parsed out
    * @throws IllegalArgumentException if there is a failure during parsing
    */
   public EntityReference parseReference(String reference) {
      String prefix = EntityReference.getPrefix(reference);
      EntityReference ref = null;
      if (entityProviderManager.getProviderByPrefix(prefix) != null) {
         ReferenceParseable provider = entityProviderManager.getProviderByPrefixAndCapability(prefix, ReferenceParseable.class);
         if (provider == null) {
            ref = new EntityReference(reference);
         } else {
            EntityReference exemplar = provider.getParsedExemplar();
            if (exemplar.getClass() == EntityReference.class) {
               ref = new EntityReference(reference);
            } else {
               // construct the custom class and then return it
               try {
                  Constructor<? extends Object> m = exemplar.getClass().getConstructor(String.class);
                  ref = (EntityReference) m.newInstance(reference);
               } catch (Exception e) {
                  throw new RuntimeException("Failed to invoke a constructor which takes a single string "
                        + "(reference="+reference+") for class: " + exemplar.getClass(), e);
               }
            }
         }
      }
      return ref;
   }

   /**
    * Parses an entity URL into an entity view object,
    * handles custom parsing templates
    * 
    * @param entityURL an entity URL
    * @return the entity view object representing this URL or 
    * null if there is no provider found for the prefix parsed out
    * @throws IllegalArgumentException if there is a failure during parsing
    */
   public EntityView parseEntityURL(String entityURL) {
      EntityView view = null;
      // first get the prefix
      String prefix = EntityReference.getPrefix(entityURL);
      // get the basic provider to see if this prefix is valid
      EntityProvider provider = entityProviderManager.getProviderByPrefix(prefix);
      if (provider != null) {
         // this prefix is valid so check for custom entity templates
         EntityViewUrlCustomizable custom = (EntityViewUrlCustomizable) entityProviderManager
         .getProviderByPrefixAndCapability(prefix, EntityViewUrlCustomizable.class);
         if (custom == null) {
            view = new EntityView(entityURL);
         } else {
            // use the custom parsing templates to build the object
            view = new EntityView();
            view.loadParseTemplates( custom.getParseTemplates() );
            view.parseEntityURL(entityURL);
         }
      }
      return view;
   }

   /**
    * Get an entity object of some kind for this reference if it has an id,
    * will simply return null if no id is available in this reference
    * 
    * @param reference a unique string representing an entity
    * @return the entity object for this reference or null if none can be retrieved
    */
   public Object getEntityObject(EntityReference ref) {
      Object entity = null;
      EntityProvider provider = entityProviderManager.getProviderByPrefixAndCapability(ref.getPrefix(), Resolvable.class);
      if (provider != null) {
         entity = ((Resolvable)provider).getEntity(ref);
      }
      return entity;
   }


   /**
    * Get the list of entities based on a reference and supplied search,
    * passes through to the EP methods if available
    * 
    * @param ref an entity reference
    * @param search an optional search
    * @return the list of entities if they can be retrieved or null these entities cannot be resolved
    */
   @SuppressWarnings("unchecked")
   protected List<?> fetchEntityList(EntityReference ref, Search search, Map<String, Object> params) {
      List entities = null;
      if (ref.getId() == null) {
         // encoding a collection of entities
         EntityProvider provider = entityProviderManager.getProviderByPrefixAndCapability(ref.getPrefix(), CollectionResolvable.class);
         if (provider != null) {
            // attempt to cleanup the search a little bit
            if (search == null) {
               search = new Search();
            } else {
               translateSearchReference(search, CollectionResolvable.SEARCH_USER_REFERENCE, 
                     new String[] {"userId","userEid","user"}, "/user/");
               translateSearchReference(search, CollectionResolvable.SEARCH_LOCATION_REFERENCE, 
                     new String[] {"locationId","location","siteId", "site"}, "/site/");
               translateSearchReference(search, CollectionResolvable.SEARCH_TAGS, 
                     new String[] {"tag","tags"}, "");
            }
            entities = new ArrayList( ((CollectionResolvable)provider).getEntities(ref, search, params) );
         }
      } else {
         // encoding a single entity
         Object entity = getEntityObject(ref);
         if (entity == null) {
            throw new EntityException("Failed to retrieve entity (" + ref + "), entity object could not be found",
                  ref.toString(), HttpServletResponse.SC_METHOD_NOT_ALLOWED);
         }
         entities = new ArrayList();
         entities.add(entity);
      }
      return entities;
   }

   /**
    * Adds in a search restriction based on existing restrictions,
    * this is ideally setup to convert restrictions into one that the developers expect
    */
   private boolean translateSearchReference(Search search, String key, String[] keys, String valuePrefix) {
      boolean added = false;
      if (search.getRestrictionByProperty(key) == null) {
         Object value = findSearchValue(search, keys);
         if (value != null) {
            if (valuePrefix != null) {
               String sval = value.toString();
               if (!sval.startsWith(valuePrefix)) {
                  value = valuePrefix + sval;
               }
            }
            search.addRestriction( new Restriction(CollectionResolvable.SEARCH_USER_REFERENCE, value) );
         }
      }
      return added;
   }

   /**
    * Finds if there are any search restrictions with the given properties, if so it returns the value,
    * otherwise returns null
    */
   private Object findSearchValue(Search search, String[] keys) {
      Object value = null;
      for (int i = 0; i < keys.length; i++) {
         String key = keys[i];
         Restriction r = search.getRestrictionByProperty(key);
         if (r != null) {
            if (r.getValue() != null) {
               value = r.getValue();
               break;
            }
         }
      }
      return value;
   }

}
