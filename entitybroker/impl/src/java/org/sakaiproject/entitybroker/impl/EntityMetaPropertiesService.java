/**
 * $Id$
 * $URL$
 * EntityMetaPropertiesService.java - entity-broker - Aug 4, 2008 10:29:48 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.entitybroker.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.entitybroker.EntityBrokerManager;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.dao.EntityBrokerDao;
import org.sakaiproject.entitybroker.dao.EntityProperty;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.entitybroker.entityprovider.capabilities.PropertyProvideable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Propertyable;
import org.sakaiproject.entitybroker.entityprovider.extension.PropertiesProvider;
import org.sakaiproject.genericdao.api.search.Restriction;
import org.sakaiproject.genericdao.api.search.Search;


/**
 * Handles calls through the system for entity properties,
 * can delegate to a central property storage service
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class EntityMetaPropertiesService implements PropertiesProvider {

   private EntityProviderManager entityProviderManager;
   public void setEntityProviderManager(EntityProviderManager entityProviderManager) {
      this.entityProviderManager = entityProviderManager;
   }

   private EntityBrokerManager entityBrokerManager;
   public void setEntityBrokerManager(EntityBrokerManager entityBrokerManager) {
      this.entityBrokerManager = entityBrokerManager;
   }

   private EntityBrokerDao dao;
   public void setDao(EntityBrokerDao dao) {
      this.dao = dao;
   }

   // PROPERTIES

   /* (non-Javadoc)
    * @see org.sakaiproject.entitybroker.entityprovider.extension.PropertiesProvider#findEntityRefs(java.lang.String[], java.lang.String[], java.lang.String[], boolean)
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
         PropertyProvideable provider = entityProviderManager.getProviderByPrefixAndCapability(prefix, PropertyProvideable.class);
         if (provider != null) {
            List<String> epList = provider.findEntityRefs(new String[] { prefix }, name, searchValue, exactMatch);
            if (epList != null) {
               results.addAll( epList );
            }
            prefixList.remove(i);
         }
      }

      // now fetch any remaining items if prefixes remain
      if (! prefixList.isEmpty()) {
         for (int i = prefixList.size() - 1; i >= 0; i--) {
            String prefix = (String) prefixList.get(i);
            // check to see if any of the remaining prefixes use Propertyable, if they do not then remove them
            Propertyable provider = entityProviderManager.getProviderByPrefixAndCapability(prefix, Propertyable.class);
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

   /* (non-Javadoc)
    * @see org.sakaiproject.entitybroker.entityprovider.extension.PropertiesProvider#getProperties(java.lang.String)
    */
   public Map<String, String> getProperties(String reference) {
      Map<String, String> m = new HashMap<String, String>();
      EntityReference ref = entityBrokerManager.parseReference(reference);
      if (ref != null) {
         if (! entityBrokerManager.entityExists(ref)) {
            throw new IllegalArgumentException("Invalid reference (" + reference
                  + "), entity does not exist");
         }

         PropertyProvideable provider = entityProviderManager.getProviderByPrefixAndCapability(ref.getPrefix(), PropertyProvideable.class);
         if (provider != null) {
            Map<String, String> epMap = provider.getProperties(reference);
            if (epMap != null) {
               m.putAll( epMap );
            }
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

   /* (non-Javadoc)
    * @see org.sakaiproject.entitybroker.entityprovider.extension.PropertiesProvider#getPropertyValue(java.lang.String, java.lang.String)
    */
   public String getPropertyValue(String reference, String name) {
      if (name == null || "".equals(name)) {
         throw new IllegalArgumentException("Invalid name argument, name must not be null or empty");
      }

      String value = null;
      EntityReference ref = entityBrokerManager.parseReference(reference);
      if (ref != null) {
         if (! entityBrokerManager.entityExists(ref)) {
            throw new IllegalArgumentException("Invalid reference (" + reference
                  + "), entity does not exist");
         }

         PropertyProvideable provider = entityProviderManager.getProviderByPrefixAndCapability(ref.getPrefix(), PropertyProvideable.class);
         if (provider != null) {
            value = provider.getPropertyValue(reference, name);
         } else {
            List<EntityProperty> properties = dao.findBySearch(EntityProperty.class, 
                  new Search( 
                        new String[] { "entityRef", "propertyName" }, 
                        new Object[] { reference, name } ) );
            if (properties.size() > 0) {
               value = properties.get(0).getPropertyValue();
            }
         }
      }
      return value;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entitybroker.entityprovider.extension.PropertiesProvider#setPropertyValue(java.lang.String, java.lang.String, java.lang.String)
    */
   public void setPropertyValue(String reference, String name, String value) {
      if (name == null && value != null) {
         throw new IllegalArgumentException(
               "Invalid params, name cannot be null unless value is also null");
      }

      EntityReference ref = entityBrokerManager.parseReference(reference);
      if (ref == null) {
         throw new IllegalArgumentException("Invalid reference (" + reference
               + "), entity type not handled");
      } else {
         if (! entityBrokerManager.entityExists(ref)) {
            throw new IllegalArgumentException("Invalid reference (" + reference
                  + "), entity does not exist");
         }

         PropertyProvideable provider = entityProviderManager.getProviderByPrefixAndCapability(ref.getPrefix(), PropertyProvideable.class);
         if (provider != null) {
            provider.setPropertyValue(reference, name, value);
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
               if (properties.size() == 0) {
                  // make new one
                  dao.create(new EntityProperty(reference, ref.getPrefix(), name, value));
               } else {
                  // update existing one
                  EntityProperty property = properties.get(0);
                  property.setPropertyValue(value);
                  dao.save(property);
               }
            }
         }
      }
   }

}
