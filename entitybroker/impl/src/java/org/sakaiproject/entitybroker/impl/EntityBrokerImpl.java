/**
 * EntityBrokerManagerImpl.java - created by antranig on 12 May 2007
 **/

package org.sakaiproject.entitybroker.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.IdEntityReference;
import org.sakaiproject.entitybroker.dao.EntityBrokerDao;
import org.sakaiproject.entitybroker.dao.model.EntityProperty;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.entitybroker.entityprovider.capabilities.PropertyProvideable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Propertyable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.extension.PropertiesProvider;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.genericdao.api.finders.ByPropsFinder;

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

   private EntityHandler entityHandler;

   public void setEntityHandler(EntityHandler entityHandler) {
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

   /*
    * (non-Javadoc)
    * 
    * @see org.sakaiproject.entitybroker.EntityBroker#entityExists(java.lang.String)
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

   /*
    * (non-Javadoc)
    * 
    * @see org.sakaiproject.entitybroker.EntityBroker#getEntityURL(java.lang.String)
    */
   public String getEntityURL(String reference) {
      return entityHandler.getEntityURL(reference);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.sakaiproject.entitybroker.EntityBroker#getRegisteredPrefixes()
    */
   public Set<String> getRegisteredPrefixes() {
      return entityProviderManager.getRegisteredPrefixes();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.sakaiproject.entitybroker.EntityBroker#fireEvent(java.lang.String, java.lang.String)
    */
   public void fireEvent(String eventName, String reference) {
      if (eventName == null || "".equals(eventName)) {
         throw new IllegalArgumentException("Cannot fire event if name is null or empty");
      }
      // had to take out this check because it makes firing events for removing entities very annoying -AZ
//    if (!entityExists(reference)) {
//       throw new IllegalArgumentException("Cannot fire event for nonexistent entity " + reference);
//    }
      Event event = eventTrackingService.newEvent(eventName, reference, true,
            NotificationService.PREF_IMMEDIATE);
      eventTrackingService.post(event);
   }

   public EntityReference parseReference(String reference) {
      return entityHandler.parseReference(reference);
   }

   public Object fetchEntity(String reference) {
      String prefix = IdEntityReference.getPrefix(reference);
      EntityProvider provider = entityProviderManager.getProviderByPrefix(prefix);
      if (provider instanceof Resolvable) {
         EntityReference ref = entityHandler.parseReference(reference);
         return ((Resolvable) provider).getEntity(ref);
      } else if (provider != null) {
         throw new IllegalArgumentException("Unable to look up reference '" + reference
               + "' to EntityProvider " + provider.getClass()
               + " which does not implement the Resolvable capability");
      } else {
         try {
            // cannot test this in a meaningful way so the tests are designed to not get here -AZ
            return entityManager.newReference(reference).getEntity();
         } catch (Exception e) {
            throw new IllegalArgumentException("Failed to look up reference '" + reference
                  + "' to an entity in legacy entity system", e);
         }
      }
   }

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
         EntityProvider provider = entityProviderManager.getProviderByPrefix(prefix);
         if (provider instanceof PropertyProvideable) {
            results.addAll(((PropertyProvideable) provider).findEntityRefs(new String[] { prefix },
                  name, searchValue, exactMatch));
            prefixList.remove(i);
         }
      }

      // now fetch any remaining items if prefixes remain
      if (!prefixList.isEmpty()) {
         for (int i = prefixList.size() - 1; i >= 0; i--) {
            String prefix = (String) prefixList.get(i);
            EntityProvider provider = entityProviderManager.getProviderByPrefix(prefix);
            if (!(provider instanceof Propertyable)) {
               prefixList.remove(i);
            }
         }

         if (!prefixList.isEmpty()) {
            prefixes = prefixList.toArray(new String[prefixList.size()]);

            List<String> props = new ArrayList<String>();
            List<String> values = new ArrayList<String>();
            List<Integer> comparisons = new ArrayList<Integer>();
            List<String> relations = new ArrayList<String>();

            for (int i = 0; i < prefixes.length; i++) {
               props.add("entityPrefix");
               values.add(prefixes[i]);
               comparisons.add(Integer.valueOf(ByPropsFinder.EQUALS));
               relations.add(i == 0 ? "and" : "or");
            }

            if (name != null && name.length > 0) {
               for (int i = 0; i < name.length; i++) {
                  props.add("propertyName");
                  values.add(name[i]);
                  comparisons.add(Integer.valueOf(ByPropsFinder.EQUALS));
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
                  comparisons.add(exactMatch ? ByPropsFinder.EQUALS : ByPropsFinder.LIKE);
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
      if (!entityExists(reference)) {
         throw new IllegalArgumentException("Invalid reference (" + reference
               + "), entity does not exist");
      }

      EntityProvider provider = entityProviderManager.getProviderByReference(reference);
      if (provider instanceof PropertyProvideable) {
         return ((PropertyProvideable) provider).getProperties(reference);
      }

      Map<String, String> m = new HashMap<String, String>();
      List<EntityProperty> properties = dao.findByProperties(EntityProperty.class,
            new String[] { "entityRef" }, new Object[] { reference });
      for (EntityProperty property : properties) {
         m.put(property.getPropertyName(), property.getPropertyValue());
      }
      return m;
   }

   @SuppressWarnings("unchecked")
   public String getPropertyValue(String reference, String name) {
      if (name == null || "".equals(name)) {
         throw new IllegalArgumentException("Invalid name argument, name must not be null or empty");
      }

      if (!entityExists(reference)) {
         throw new IllegalArgumentException("Invalid reference (" + reference
               + "), entity does not exist");
      }

      EntityProvider provider = entityProviderManager.getProviderByReference(reference);
      if (provider instanceof PropertyProvideable) {
         return ((PropertyProvideable) provider).getPropertyValue(reference, name);
      }

      List<EntityProperty> properties = dao.findByProperties(EntityProperty.class, new String[] {
            "entityRef", "propertyName" }, new Object[] { reference, name });
      if (properties.size() == 1) {
         return properties.get(0).getPropertyValue();
      }
      return null;
   }

   @SuppressWarnings("unchecked")
   public void setPropertyValue(String reference, String name, String value) {
      if (name == null && value != null) {
         throw new IllegalArgumentException(
               "Invalid params, name cannot be null unless value is also null");
      }

      if (!entityExists(reference)) {
         throw new IllegalArgumentException("Invalid reference (" + reference
               + "), entity does not exist");
      }

      EntityProvider provider = entityProviderManager.getProviderByReference(reference);
      if (provider instanceof PropertyProvideable) {
         ((PropertyProvideable) provider).setPropertyValue(reference, name, value);
      } else {
         if (value == null) {
            // remove all properties from this entity if name also null, otherwise just remove this
            // one
            dao.deleteProperties(reference, name);
         } else {
            // add or update property
            String prefix = EntityReference.getPrefix(reference);
            List<EntityProperty> properties = dao.findByProperties(EntityProperty.class,
                  new String[] { "entityRef", "propertyName" }, new Object[] { reference, name });
            if (properties.isEmpty()) {
               dao.create(new EntityProperty(reference, prefix, name, value));
            } else {
               EntityProperty property = properties.get(0);
               property.setPropertyValue(value);
               dao.save(property);
            }
         }
      }
   }

}
