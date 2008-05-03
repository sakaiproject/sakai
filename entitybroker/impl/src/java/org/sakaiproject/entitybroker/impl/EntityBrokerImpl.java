/**
 * $Id$
 * $URL$
 * EntityBrokerImpl.java - entity-broker - Apr 6, 2008 9:03:03 AM - azeckoski
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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.dao.EntityBrokerDao;
import org.sakaiproject.entitybroker.dao.EntityProperty;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.entitybroker.entityprovider.capabilities.InputTranslatable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Inputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.OutputFormattable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.PropertyProvideable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Propertyable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.TagSearchable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Taggable;
import org.sakaiproject.entitybroker.entityprovider.extension.PropertiesProvider;
import org.sakaiproject.entitybroker.util.reflect.ReflectUtil;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.genericdao.api.search.Restriction;
import org.sakaiproject.genericdao.api.search.Search;

/**
 * The default implementation of the EntityBroker interface
 * 
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EntityBrokerImpl implements EntityBroker, PropertiesProvider {

   private static Log log = LogFactory.getLog(EntityBrokerImpl.class);

   private EntityProviderManager entityProviderManager;
   public void setEntityProviderManager(EntityProviderManager entityProviderManager) {
      this.entityProviderManager = entityProviderManager;
   }

   private EventTrackingService eventTrackingService;
   public void setEventTrackingService(EventTrackingService eventTrackingService) {
      this.eventTrackingService = eventTrackingService;
   }

   private EntityHandlerImpl entityHandler;
   public void setEntityHandler(EntityHandlerImpl entityHandler) {
      this.entityHandler = entityHandler;
   }

   private EntityManager entityManager;
   public void setEntityManager(EntityManager entityManager) {
      this.entityManager = entityManager;
   }

   private EntityBrokerDao dao;
   public void setDao(EntityBrokerDao dao) {
      this.dao = dao;
   }

   public void init() {
      log.info("init");
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entitybroker.EntityBroker#entityExists(java.lang.String)
    */
   public boolean entityExists(String reference) {
      EntityReference ref = entityHandler.parseReference(reference);
      return entityHandler.entityExists(ref);
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entitybroker.EntityBroker#getEntityURL(java.lang.String)
    */
   public String getEntityURL(String reference) {
      return entityHandler.getEntityURL(reference, null, null);
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entitybroker.EntityBroker#getEntityURL(java.lang.String, java.lang.String, java.lang.String)
    */
   public String getEntityURL(String reference, String viewKey, String extension) {
      return entityHandler.getEntityURL(reference, viewKey, extension);
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entitybroker.EntityBroker#getRegisteredPrefixes()
    */
   public Set<String> getRegisteredPrefixes() {
      return entityProviderManager.getRegisteredPrefixes();
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entitybroker.EntityBroker#parseReference(java.lang.String)
    */
   public EntityReference parseReference(String reference) {
      return entityHandler.parseReference(reference);
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entitybroker.EntityBroker#fireEvent(java.lang.String, java.lang.String)
    */
   public void fireEvent(String eventName, String reference) {
      if (eventName == null || "".equals(eventName)) {
         throw new IllegalArgumentException("Cannot fire event if name is null or empty");
      }
      if (reference == null || "".equals(reference)) {
         throw new IllegalArgumentException("Cannot fire event if reference is null or empty");
      }
      String refName = reference;
      try {
         // parse the reference string to validate it and remove any extra bits
         EntityReference ref = entityHandler.parseReference(reference);
         if (ref != null) {
            refName = ref.toString();
         } else {
            // fallback to simple parsing
            refName = new EntityReference(reference).toString();
         }
      } catch (Exception e) {
         refName = reference;
         log.warn("Invalid reference ("+reference+") for eventName ("+eventName+"), could not parse the reference correctly, continuing to create event with original reference");
      }
      // had to take out the exists check because it makes firing events for removing entities very annoying -AZ
      Event event = eventTrackingService.newEvent(eventName, refName, true,
            NotificationService.PREF_IMMEDIATE);
      eventTrackingService.post(event);
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entitybroker.EntityBroker#fetchEntity(java.lang.String)
    */
   public Object fetchEntity(String reference) {
      Object entity = null;
      EntityReference ref = entityHandler.parseReference(reference);
      if (ref == null) {
         // not handled in EB so attempt to parse out a prefix and try to get entity from the legacy system
         try {
            // cannot test this in a meaningful way so the tests are designed to not get here -AZ
            entity = entityManager.newReference(reference).getEntity();
         } catch (Exception e) {
            log.warn("Failed to look up reference '" + reference
                  + "' to an entity in legacy entity system", e);
         }
      } else {
         // this is a registered prefix
         EntityProvider provider = entityProviderManager.getProviderByPrefixAndCapability(ref.getPrefix(), Resolvable.class);
         if (provider != null) {
            // no exists check here since we are trying to reduce extra load
            entity = ((Resolvable) provider).getEntity(ref);
         }
      }
      return entity;
   }

   public void formatAndOutputEntity(String reference, String format, List<?> entities, OutputStream output) {
      if (reference == null || format == null || output == null) {
         throw new IllegalArgumentException("reference, format, and output cannot be null");
      }
      EntityReference ref = entityHandler.parseReference(reference);
      if (ref == null) {
         throw new IllegalArgumentException("Cannot output formatted entity, entity reference is invalid: " + reference);
      }
      String prefix = ref.getPrefix();
      Outputable outputable = entityProviderManager.getProviderByPrefixAndCapability(prefix, Outputable.class);
      if (outputable != null) {
         String[] formats = outputable.getHandledOutputFormats();
         if ( ReflectUtil.contains(formats, format) ) {
            OutputFormattable formattable = entityProviderManager.getProviderByPrefixAndCapability(prefix, OutputFormattable.class);
            if (formattable == null) {
               // handle internally or fail
               entityHandler.internalOutputFormatter(ref, format, entities, output, null);
            } else {
               // use provider's formatter
               formattable.formatOutput(ref, format, entities, output);
            }
         } else {
            throw new IllegalArgumentException("This entity ("+reference+") is not outputable in this format ("+format+")," +
            		" only the following formats are supported: " + ReflectUtil.arrayToString(formats));
         }
      } else {
         throw new IllegalArgumentException("This entity ("+reference+") is not outputable");
      }
   }

   public Object translateInputToEntity(String reference, String format, InputStream input) {
      if (reference == null || format == null || input == null) {
         throw new IllegalArgumentException("reference, format, and input cannot be null");
      }
      EntityReference ref = entityHandler.parseReference(reference);
      if (ref == null) {
         throw new IllegalArgumentException("Cannot output formatted entity, entity reference is invalid: " + reference);
      }
      Object entity = null;
      String prefix = ref.getPrefix();
      Inputable inputable = entityProviderManager.getProviderByPrefixAndCapability(prefix, Inputable.class);
      if (inputable != null) {
         String[] formats = inputable.getHandledInputFormats();
         if ( ReflectUtil.contains(formats, format) ) {
            InputTranslatable translatable = entityProviderManager.getProviderByPrefixAndCapability(prefix, InputTranslatable.class);
            if (translatable == null) {
               // handle internally or fail
               entity = entityHandler.internalInputTranslator(ref, format, input, null);
            } else {
               // use provider's formatter
               entity = translatable.translateFormattedData(ref, format, input);
            }
         } else {
            throw new IllegalArgumentException("This entity ("+reference+") is not inputable in this format ("+format+")," +
                  " only the following formats are supported: " + ReflectUtil.arrayToString(formats));
         }
      } else {
         throw new IllegalArgumentException("This entity ("+reference+") is not inputable");
      }
      return entity;
   }


   // PROPERTIES

   /**
    * Allows searching for entities by property values, at least one of the params (prefix, name,
    * searchValue) must be set in order to do a search, (searches which return all references to all
    * entities with properties are not allowed) <br/> <b>WARNING:</b> this search is very fast but
    * will not actually limit by properties that should have been placed on the entity itself or
    * return the entity itself and is not a substitute for an API which allows searches of your
    * entities
    * 
    * @param prefix
    *           limit the search to a specific entity prefix, this must be set and cannot be an
    *           empty array
    * @param name
    *           limit the property names to search for, can be null to return all names
    * @param searchValue
    *           limit the search by property values can be null to return all values, must be the
    *           same size as the name array if it is not null, (i.e. this cannot be set without
    *           setting at least one name)
    * @param exactMatch
    *           if true then only match property values exactly, otherwise use a "like" search
    * @return a list of entity references for all entities matching the search
    */
   public List<String> findEntityRefs(String[] prefixes, String[] name, String[] searchValue,
         boolean exactMatch) {
      // check for valid inputs
      if (prefixes == null || prefixes.length == 0) {
         throw new IllegalArgumentException(
               "At least one prefix must be supplied to this search, prefixes cannot be null or empty");
      }

      List<String> results = new ArrayList<String>();

      // first get the results from any entity providers which supply property searches
      List<String> prefixList = new ArrayList<String>(Arrays.asList(prefixes));
      for (int i = prefixList.size() - 1; i >= 0; i--) {
         String prefix = (String) prefixList.get(i);
         EntityProvider provider = entityProviderManager.getProviderByPrefixAndCapability(prefix, PropertyProvideable.class);
         if (provider != null) {
            results.addAll( ((PropertyProvideable) provider).findEntityRefs(new String[] { prefix },
                  name, searchValue, exactMatch) );
            prefixList.remove(i);
         }
      }

      // now fetch any remaining items if prefixes remain
      if (! prefixList.isEmpty()) {
         for (int i = prefixList.size() - 1; i >= 0; i--) {
            String prefix = (String) prefixList.get(i);
            // check to see if any of the remaining prefixes use Propertyable, if they do not then remove them
            EntityProvider provider = entityProviderManager.getProviderByPrefixAndCapability(prefix, Propertyable.class);
            if (provider == null) {
               prefixList.remove(i);
            }
         }

         // now search the internal list of properties if any prefixes remain
         if (! prefixList.isEmpty()) {
            prefixes = prefixList.toArray(new String[prefixList.size()]);

            List<String> props = new ArrayList<String>();
            List<String> values = new ArrayList<String>();
            List<Integer> comparisons = new ArrayList<Integer>();
            List<String> relations = new ArrayList<String>();

            for (int i = 0; i < prefixes.length; i++) {
               props.add("entityPrefix");
               values.add(prefixes[i]);
               comparisons.add(Integer.valueOf(Restriction.EQUALS));
               relations.add(i == 0 ? "and" : "or");
            }

            if (name != null && name.length > 0) {
               for (int i = 0; i < name.length; i++) {
                  props.add("propertyName");
                  values.add(name[i]);
                  comparisons.add(Integer.valueOf(Restriction.EQUALS));
                  relations.add(i == 0 ? "and" : "or");
               }
            }

            if (searchValue != null && searchValue.length > 0) {
               if (name == null || name.length != searchValue.length) {
                  throw new IllegalArgumentException(
                        "name and searchValue arrays must be the same length if not null");
               }
               for (int i = 0; i < searchValue.length; i++) {
                  props.add("propertyValue");
                  values.add(searchValue[i]);
                  comparisons.add(exactMatch ? Restriction.EQUALS : Restriction.LIKE);
                  relations.add(i == 0 ? "and" : "or");
               }
            }

            if (props.isEmpty()) {
               throw new IllegalArgumentException(
                     "At least one of prefix, name, or searchValue has to be a non-empty array");
            }

            List<String> refs = dao.getEntityRefsForSearch(props, values, comparisons, relations);
            results.addAll(refs);
         }
      }
      return results;
   }

   @SuppressWarnings("unchecked")
   public Map<String, String> getProperties(String reference) {
      if (! entityExists(reference)) {
         throw new IllegalArgumentException("Invalid reference (" + reference
               + "), entity does not exist");
      }

      Map<String, String> m = new HashMap<String, String>();
      EntityReference ref = entityHandler.parseReference(reference);
      if (ref != null) {
         EntityProvider provider = entityProviderManager.getProviderByPrefixAndCapability(ref.getPrefix(), PropertyProvideable.class);
         if (provider != null) {
            m = ((PropertyProvideable) provider).getProperties(reference);
         } else {
            List<EntityProperty> properties = dao.findBySearch(EntityProperty.class,
                  new Search( "entityRef", reference ) );
            for (EntityProperty property : properties) {
               m.put(property.getPropertyName(), property.getPropertyValue());
            }
         }
      }
      return m;
   }

   @SuppressWarnings("unchecked")
   public String getPropertyValue(String reference, String name) {
      if (name == null || "".equals(name)) {
         throw new IllegalArgumentException("Invalid name argument, name must not be null or empty");
      }

      if (! entityExists(reference)) {
         throw new IllegalArgumentException("Invalid reference (" + reference
               + "), entity does not exist");
      }

      String value = null;
      EntityReference ref = entityHandler.parseReference(reference);
      if (ref != null) {
         EntityProvider provider = entityProviderManager.getProviderByPrefixAndCapability(ref.getPrefix(), PropertyProvideable.class);
         if (provider != null) {
            value = ((PropertyProvideable) provider).getPropertyValue(reference, name);
         } else {
            List<EntityProperty> properties = dao.findBySearch(EntityProperty.class, 
                  new Search( 
                        new String[] { "entityRef", "propertyName" }, 
                        new Object[] { reference, name } ) );
            if (properties.size() == 1) {
               value = properties.get(0).getPropertyValue();
            }
         }
      }
      return value;
   }

   @SuppressWarnings("unchecked")
   public void setPropertyValue(String reference, String name, String value) {
      if (name == null && value != null) {
         throw new IllegalArgumentException(
               "Invalid params, name cannot be null unless value is also null");
      }

      if (! entityExists(reference)) {
         throw new IllegalArgumentException("Invalid reference (" + reference
               + "), entity does not exist");
      }

      EntityReference ref = entityHandler.parseReference(reference);
      if (ref == null) {
         throw new IllegalArgumentException("Invalid reference (" + reference
               + "), entity type not handled");
      } else {
         EntityProvider provider = entityProviderManager.getProviderByPrefixAndCapability(ref.getPrefix(), PropertyProvideable.class);
         if (provider != null) {
            ((PropertyProvideable) provider).setPropertyValue(reference, name, value);
         } else {
            if (value == null) {
               // remove all properties from this entity if name also null, otherwise just remove this one
               dao.deleteProperties(reference, name);
            } else {
               // add or update property
               List<EntityProperty> properties = dao.findBySearch(EntityProperty.class,
                     new Search(
                           new String[] { "entityRef", "propertyName" }, 
                           new Object[] { reference, name }) );
               if (properties.isEmpty()) {
                  dao.create(new EntityProperty(reference, ref.getPrefix(), name, value));
               } else {
                  EntityProperty property = properties.get(0);
                  property.setPropertyValue(value);
                  dao.save(property);
               }
            }
         }
      }
   }

   
   // TAGS

   public Set<String> getTags(String reference) {
      if (! entityExists(reference)) {
         throw new IllegalArgumentException("Invalid reference (" + reference
               + "), entity does not exist");
      }

      Set<String> tags = new HashSet<String>();

      EntityReference ref = entityHandler.parseReference(reference);
      if (ref != null) {
         EntityProvider provider = entityProviderManager.getProviderByPrefixAndCapability(ref.getPrefix(), Taggable.class);
         if (provider != null) {
            tags.addAll( ((Taggable) provider).getTags(reference) );
         } else {
            // put in call to central tag system here if desired
   
            throw new UnsupportedOperationException("Cannot get tags from this entity ("+reference+"), it has no support for tagging in its entity provider");
         }
      }
      return tags;
   }

   public void setTags(String reference, String[] tags) {
      if (tags == null) {
         throw new IllegalArgumentException(
               "Invalid params, tags cannot be null");
      }

      if (! entityExists(reference)) {
         throw new IllegalArgumentException("Invalid reference (" + reference
               + "), entity does not exist");
      }

      EntityReference ref = entityHandler.parseReference(reference);
      if (ref != null) {
         EntityProvider provider = entityProviderManager.getProviderByPrefixAndCapability(ref.getPrefix(), Taggable.class);
         if (provider != null) {
            ((Taggable) provider).setTags(reference, tags);
         } else {
            // put in call to central tag system here if desired
   
            throw new UnsupportedOperationException("Cannot set tags for this entity ("+reference+"), it has no support for tagging in its entity provider");
         }
      }
   }

   public List<String> findEntityRefsByTags(String[] tags) {
      // check for valid inputs
      if (tags == null || tags.length == 0) {
         throw new IllegalArgumentException(
               "At least one tag must be supplied to this search, tags cannot be null or empty");
      }

      Set<String> results = new HashSet<String>();

      // get the results from any entity providers which supply tag search results
      Set<String> prefixes = entityProviderManager.getRegisteredPrefixes();
      for (String prefix : prefixes) {
         EntityProvider provider = entityProviderManager.getProviderByPrefixAndCapability(prefix, TagSearchable.class);
         if (provider != null) {
            results.addAll( ((TagSearchable) provider).findEntityRefsByTags(tags) );
         }
      }

      // fetch results from a central system instead here if desired

      return new ArrayList<String>( results );
   }

}
