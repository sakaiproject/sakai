/**
 * $Id$
 * $URL$
 * EntityDataUtils.java - entity-broker - Aug 11, 2008 2:58:03 PM - azeckoski
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

package org.sakaiproject.entitybroker.util;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.azeckoski.reflectutils.ReflectUtils;
import org.azeckoski.reflectutils.exceptions.FieldnameNotFoundException;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityId;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.EntityData;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.util.model.EntityContent;


/**
 * Utilities which are useful when working with {@link EntityData} objects and their properties
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class EntityDataUtils {

    private final static Log log = LogFactory.getLog(EntityDataUtils.class);
    // Property names
    private final static Set<String> directPropertyNames = Collections.unmodifiableSet(new HashSet<String>(){
        private static final long serialVersionUID = 1L;
        {
            add(ResourceProperties.PROP_DISPLAY_NAME);
            add(ResourceProperties.PROP_DESCRIPTION);
            add(ResourceProperties.PROP_CREATOR);
            add(ResourceProperties.PROP_MODIFIED_BY);
            add(ResourceProperties.PROP_CREATION_DATE);
            add(ResourceProperties.PROP_MODIFIED_DATE);
            add(ResourceProperties.PROP_RESOURCE_TYPE);
            add(ResourceProperties.PROP_CONTENT_TYPE);
            add(ResourceProperties.PROP_CONTENT_PRIORITY);
            add(ResourceProperties.PROP_CONTENT_LENGTH);
            add(ResourceProperties.PROP_IS_COLLECTION);
        }
    });
    /**
     * Convert a list of objects to entity data objects (DOES NOT populate them),
     * will preserve null (i.e. null in => null out)
     */
    public static List<EntityData> convertToEntityData(List<?> entities, EntityReference ref) {
        List<EntityData> l = null;
        if (entities != null) {
            l = new ArrayList<EntityData>();
            for (Object entity : entities) {
                l.add( makeEntityData(ref, entity) );
            }
        }
        return l;
    }

    /**
     * Convert a single object to an entity data object (DOES NOT populate it),
     * will preserve null (i.e. null in => null out)
     */
    public static EntityData convertToEntityData(Object entity, EntityReference ref) {
        EntityData ed = null;
        if (entity != null) {
            ed = makeEntityData(ref, entity);
        }
        return ed;
    }

    /**
     * Convert a list of entities / entity data to just entities,
     * will preserve null (i.e. null in => null out)
     */
    public static List<?> convertToEntities(List<?> entities) {
        List<Object> l = null;
        if (entities != null) {
            l = new ArrayList<Object>();
            for (Object object : entities) {
                if (object != null) {
                    l.add( EntityDataUtils.convertToEntity(object) );
                }
            }
        }
        return l;
    }

    /**
     * Convert an entity/data to just an entity,
     * will preserve null (i.e. null in => null out)
     */
    public static Object convertToEntity(Object object) {
        Object togo = null;
        if (object != null) {
            if (EntityData.class.isAssignableFrom(object.getClass())) {
                EntityData ed = (EntityData)object;
                if (ed.getData() != null) {
                    togo = ed.getData();
                } else {
                    togo = ed;
                }
            } else {
                togo = object;
            }
        }
        return togo;
    }

    /**
     * Make an entity data object out of whatever entity is given,
     * use the given reference, if there is no id then this will attempt to get one otherwise it will use prefix only
     */
    public static EntityData makeEntityData(EntityReference ref, Object entity) {
        EntityData ed = null;
        if (entity != null) {
            if (ref == null) {
                throw new IllegalArgumentException("ref must not be null or no entity data object can be created");
            }
            Class<?> resultClass = entity.getClass();
            if (EntityData.class.isAssignableFrom(resultClass)) {
                ed = (EntityData) entity;
            } else {
                if (ref.getId() == null) {
                    // attempt to get the id if it was not provided
                    String entityId = getEntityId(entity);
                    if (entityId != null) {
                        ref = new EntityReference(ref.getPrefix(), entityId);
                    }
                }
                Object entityObject = entity;
                if (ActionReturn.class.isAssignableFrom(resultClass)) {
                    ActionReturn ar = (ActionReturn) entity;
                    if (ar.entityData == null) {
                        // make entity data from AR
                        if (ar.outputString != null) {
                            entityObject = ar.outputString;
                        } else if (ar.output != null) {
                            entityObject = ar.output;
                        }
                    } else {
                        ed = ar.entityData;
                    }
// removed because it makes a mess of the output, maybe add this in later though -AZ
//                } else if (Map.class.isAssignableFrom(resultClass)) {
//                    props = EntityDataUtils.extractMapProperties((Map)entity);
                }
                if (ed == null) {
                    ed = new EntityData(ref, null, entityObject, null);
                }
            }
        }
        return ed;
    }

    /**
     * Gets the id field value from an entity if possible
     * @param entity any entity object
     * @return the id value OR null if it cannot be found
     */
    public static String getEntityId(Object entity) {
        String entityId = null;
        try {
            entityId = ReflectUtils.getInstance().getFieldValueAsString(entity, "entityId", EntityId.class);
        } catch (FieldnameNotFoundException e) {
            try {
                // try just id only as well
                entityId = ReflectUtils.getInstance().getFieldValueAsString(entity, "id", null);
            } catch (FieldnameNotFoundException e1) {
                entityId = null;
            }
        }
        return entityId;
    }

    /**
     * Gets the fieldname of the identifier field for an entity class type
     * @param type any entity class type
     * @return the name of the identifier field for this entity OR null if it cannot be determined
     */
    public static String getEntityIdField(Class<?> type) {
        String entityIdField = ReflectUtils.getInstance().getFieldNameWithAnnotation(type, EntityId.class);
        if (entityIdField == null) {
            try {
                ReflectUtils.getInstance().getFieldType(type, "id");
                entityIdField = "id";
            } catch (FieldnameNotFoundException e) {
                entityIdField = null;
            }
        }
        return entityIdField;
    }

    /**
     * Translate the search into one using the standard search params
     * @param search
     * @return the translated search
     */
    public static Search translateStandardSearch(Search search) {
        Search togo = search;
        if (search == null) {
            togo = new Search();
        } else {
            EntityDataUtils.translateSearchReference(search, CollectionResolvable.SEARCH_USER_REFERENCE, 
                    new String[] {"userReference","userId","userEid","user"}, "/user/");
            EntityDataUtils.translateSearchReference(search, CollectionResolvable.SEARCH_LOCATION_REFERENCE, 
                    new String[] {"locationReference","locationId","location","siteReference","siteId","site","groupReference","groupId","group"}, "/site/");
            EntityDataUtils.translateSearchReference(search, CollectionResolvable.SEARCH_TAGS, 
                    new String[] {"tag","tags"}, "");
        }
        return togo;
    }

    /**
     * Adds in a search restriction based on existing restrictions,
     * this is ideally setup to convert restrictions into one that the developers expect
     */
    public static boolean translateSearchReference(Search search, String key, String[] keys, String valuePrefix) {
        boolean added = false;
        if (search.getRestrictionByProperty(key) == null) {
            Restriction r = findSearchRestriction(search, key, keys, valuePrefix);
            if (r != null) {
                search.addRestriction( r );
            }
        }
        return added;
    }

    /**
     * Finds if there are any search restrictions with the given properties, if so it returns the Restriction,
     * the value will have the given prefix appended to it's string value if it does not have it,
     * the returned restriction will have the key set to the input key,
     * otherwise returns null
     */
    public static Restriction findSearchRestriction(Search search, String key, String[] keys, String valuePrefix) {
        Restriction r = search.getRestrictionByProperties(keys);
        if (r != null) {
            Object value = r.getValue();
            if (valuePrefix != null) {
                String sval = r.getStringValue();
                if (!sval.startsWith(valuePrefix)) {
                    value = valuePrefix + sval;
                }
            }
            r = new Restriction(key, value);
        }
        return r;
    }

    /**
     * Finds a map value for a key (or set of keys) if it exists in the map and returns the string value of it
     * @param map any map with strings as keys
     * @param keys an array of keys to try to find in order
     * @return the string value OR null if it could not be found for any of the given keys
     */
    public static String findMapStringValue(Map<String, ?> map, String[] keys) {
        String value = null;
        Object val = findMapValue(map, keys);
        if (val != null) {
            try {
                value = val.toString();
            } catch (RuntimeException e) {
                // in case the to string fails
                value = null;
            }
        }
        return value;
    }

    /**
     * Finds a map value for a key (or set of keys) if it exists in the map and returns the value of it
     * @param map any map with strings as keys
     * @param keys an array of keys to try to find in order
     * @return the value OR null if it could not be found for any of the given keys
     */
    public static Object findMapValue(Map<String, ?> map, String[] keys) {
        if (map == null || keys == null) {
            return null;
        }
        Object value = null;
        try {
            for (int i = 0; i < keys.length; i++) {
                String key = keys[i];
                if (map.containsKey(key)) {
                    Object oVal = map.get(key);
                    if (oVal != null) {
                        value = oVal;
                        break;
                    }
                }
            }
        } catch (RuntimeException e) {
            // in case the given map is not actually of the right types at runtime
            value = null;
        }
        return value;
    }

    /**
     * Puts the values of 2 maps together such that the values from the newStuff map are added to the
     * original map only if they are not already in the the original
     * @param <T>
     * @param <S>
     * @param original
     * @param newStuff
     */
    public static <T,S> void putAllNewInMap(Map<T,S> original, Map<T,S> newStuff) {
        if (original == null) {
            throw new IllegalArgumentException("original map cannot be null");
        }
        if (newStuff != null) {
            if (original.isEmpty()) {
                original.putAll( newStuff );
            } else {
                for (Entry<T,S> entry : newStuff.entrySet()) {
                    if (original.containsKey(entry.getKey())) {
                        continue;
                    }
                    original.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    /**
     * Get the values from a map and convert them to strings,
     * nulls pass through
     * @param m
     * @return the keys of any map as strings and the values as they were
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> extractMapProperties(Map m) {
        Map<String, Object> props = null;
        if (m == null || m.isEmpty()) {
            props = null;
        } else {
            props = new HashMap<String, Object>();
            for (Entry entry : (Set<Entry>) m.entrySet()) {
                Object key = entry.getKey();
                if (key != null) {
                    Object value = entry.getValue();
                    if (value != null) {
                        if (Serializable.class.isAssignableFrom(key.getClass())) {
                            // only use simple types that can be serialized
                            props.put(key.toString(), value);
                        }
                    }
                }
            }
        }
        return props;
    }
    /**
     * Produces a summary of an content entity for display in entitybroker.
     * @param entity The entity to display.
     * @return An EntityContent matching the supplied entity.
     */
    public static EntityContent getResourceDetails(ContentEntity entity) {

        EntityContent tempRd = new EntityContent();

        ResourceProperties properties = entity.getProperties();
        Iterator propertyNames = properties.getPropertyNames();
        while (propertyNames.hasNext()) {
            String key = (String) propertyNames.next();
            if (!directPropertyNames.contains(key)) {
                String value = properties.getProperty(key);
                if (null != value) {
                    tempRd.setProperty(key, value);
                }
            }
        }

        tempRd.setResourceId(entity.getId());
        tempRd.setName(properties
                .getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME));
        tempRd.setDescription(properties
                .getProperty(ResourceProperties.PROP_DESCRIPTION));
        tempRd.setCreator(properties
                .getProperty(ResourceProperties.PROP_CREATOR));

        tempRd.setModifiedBy(properties
                .getProperty(ResourceProperties.PROP_MODIFIED_BY));
        tempRd.setMimeType(properties
                .getProperty(ResourceProperties.PROP_CONTENT_TYPE));
        tempRd.setPriority(properties
                .getProperty(ResourceProperties.PROP_CONTENT_PRIORITY));
        tempRd.setSize(properties
                .getProperty(ResourceProperties.PROP_CONTENT_LENGTH));
        tempRd.setReference(entity.getReference());
        tempRd.setType(entity.getResourceType());
        tempRd.setUrl(entity.getUrl());
        tempRd.setRelease(entity.getReleaseDate());
        tempRd.setRetract(entity.getRetractDate());
        tempRd.setHidden(entity.isHidden());
        try {
            tempRd.setCreated(properties
                    .getTimeProperty(ResourceProperties.PROP_CREATION_DATE));
            tempRd.setModified(properties
                    .getTimeProperty(ResourceProperties.PROP_MODIFIED_DATE));

        } catch (EntityPropertyNotDefinedException e) {
            log.warn("Failed to get property on " + entity.getId(), e);
        } catch (EntityPropertyTypeException e) {
            log.warn("Incorrect property type on " + entity.getId(), e);
        }

        return tempRd;
    }

}
