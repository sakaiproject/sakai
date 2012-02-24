/**
 * $Id$
 * $URL$
 * EntityData.java - entity-broker - Aug 3, 2008 6:03:53 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 The Sakai Foundation
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

package org.sakaiproject.entitybroker.entityprovider.extension;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;


/**
 * This is an object to hold entity data (e.g. from a search which would normally return entity references),
 * This is basically a POJO which allows us to return a few results instead of only the reference,
 * it helps us get the entity data back more efficiently and makes it easier on developers who
 * need to search for entities
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class EntityData {

    /**
     * (OPTIONAL - may be null)
     * This is the entity data object itself (if there is one),
     * this is included at the discretion of the entity provider author,
     * if this is null then the entity data is not available or would be prohibitively large (i.e. typically left out for efficiency)
     */
    private Object data;
    public void setData(Object entity) {
        this.data = entity;
        //      if (data != null) {
        //      this.entity = new WeakReference<Object>(data);
        //      } else {
        //      this.entity = null;
        //      }
    }
    /**
     * (OPTIONAL - may be null)
     * This is the entity data object itself (if there is one),
     * this is included at the discretion of the entity provider author,
     * if this is null then the entity data is not available or would be prohibitively large (i.e. typically left out for efficiency)
     */
    public Object getData() {
        return this.data;
        //      if (this.entity == null) {
        //      return null;
        //      } else {
        //      return this.entity.get();
        //      }
    }

    private String entityId = null;
    /**
     * @return the unique local id of the entity (null if there is none)
     */
    public String getEntityId() {
        return entityId;
    }

    /**
     * The entity reference -  a globally unique reference to an entity, 
     * consists of the entity prefix and optional segments (normally the id at least),
     * this should be set by the constructor only
     */
    private String entityReference;
    /**
     * The entity reference -  a globally unique reference to an entity, 
     * consists of the entity prefix and optional segments (normally the id at least)
     */
    public String getEntityReference() {
        return entityReference;
    }

    /**
     * The entity reference object which makes it easy to get to the prefix or id of this entity,
     * this should be set by the constructor only
     */
    private transient EntityReference entityRef;
    /**
     * The entity reference object which makes it easy to get to the prefix or id of this entity if needed
     */
    public EntityReference getEntityRef() {
        return entityRef;
    }

    /**
     * A string which is suitable for display and provides a short summary of the entity,
     * typically 100 chars or less, this may be the name or title of the data represented by an entity
     */
    private String entityDisplayTitle;
    /**
     * A string which is suitable for display and provides a short summary of the entity,
     * typically 100 chars or less, this may be the name or title of the data represented by an entity
     */
    public void setDisplayTitle(String displayTitle) {
        this.entityDisplayTitle = displayTitle;
    }
    /**
     * A string which is suitable for display and provides a short summary of the entity,
     * typically 100 chars or less, this may be the name or title of the data represented by an entity
     */
    public String getDisplayTitle() {
        if (this.entityDisplayTitle == null) {
            if (this.entityRef != null) {
                return this.entityRef.getPrefix() + " : " + entityReference;
            } else {
                return "data";
            }
        }
        return entityDisplayTitle;
    }
    private transient boolean displayTitleSet = false; // needed to avoid encoding
    /**
     * @return true if the display title is actually set, false if it is null and will return an auto-generated value
     */
    public boolean isDisplayTitleSet() {
        displayTitleSet = entityDisplayTitle != null;
        return displayTitleSet;
    }

    /**
     * (OPTIONAL - may be null)
     * The entityURL to the entity represented by this reference,
     * should be an absolute entityURL (server name optional),
     * if this is null then the entityURL is formed from the reference
     */
    private String entityURL;
    /**
     * WARNING: for internal use only
     * @param url the url to access this entity
     */
    public void setEntityURL(String url) {
        entityURL = url;
    }
    /**
     * The entityURL to the entity represented by this reference,
     * should be an absolute entityURL (server name optional)
     */
    public String getEntityURL() {
        return entityURL;
    }

    /**
     * (OPTIONAL - may be null)
     * A set of properties to return along with the entity information,
     * this may be presented and used for filtering,
     * this will be null or empty if it is not used
     */
    private Map<String, Object> entityProperties;
    /**
     * (OPTIONAL - may be null)
     * A set of properties to return along with the entity information,
     * this may be presented and used for filtering,
     * should be null or empty if not used
     * @param entityProperties a map of property name => value
     */
    public void setEntityProperties(Map<String, Object> entityProperties) {
        this.entityProperties = entityProperties;
    }
    /**
     * A set of properties to return along with the entity information,
     * this may be presented and used for filtering,
     * this will be empty if it is not used
     */
    public Map<String, Object> getEntityProperties() {
        if (entityProperties == null) {
            entityProperties = new HashMap<String, Object>(0);
        }
        return entityProperties;
    }

    /**
     * used to ensure that we do not accidently attempt to populate this twice
     */
    private transient boolean populated = false;
    /**
     * FOR INTERNAL USE ONLY - do not use
     */
    public void setPopulated(boolean populated) {
        this.populated = populated;
    }
    /**
     * @return true if this object was populated, false otherwise
     */
    public boolean isPopulated() {
        return populated;
    }

    /**
     * indicates that this is a holder and should be discarded and the data
     * rendered into the given format without it
     */
    private transient boolean dataOnly = false;
    /**
     * FOR INTERNAL USE ONLY - do not use
     */
    public void setDataOnly(boolean dataOnly) {
        this.dataOnly = dataOnly;
    }
    /**
     * @return true if this is a data holder and the data inside it should be rendered alone without this wrapper
     */
    public boolean isDataOnly() {
        return dataOnly;
    }

    /**
     * Minimal constructor - used for most basic cases<br/>
     * Use the setters to add in properties or the entity if desired
     * 
     * @param reference a globally unique reference to an entity, 
     * consists of the entity prefix and id (e.g. /prefix/id)
     * @param entityDisplayTitle a string which is suitable for display and provides a short summary of the entity,
     * typically 100 chars or less, this may be the name or title of the entity represented by an entity
     */
    public EntityData(String reference, String displayTitle) {
        this(reference, displayTitle, null, null);
    }

    /**
     * Basic constructor<br/>
     * Use this to construct a search result using the typical minimal amount of information,
     * Use the setters to add in properties or the entity if desired
     * 
     * @param reference a globally unique reference to an entity, 
     * consists of the entity prefix and id (e.g. /prefix/id)
     * @param entityDisplayTitle a string which is suitable for display and provides a short summary of the entity,
     * typically 100 chars or less, this may be the name or title of the entity represented by an entity
     * @param data an entity data object, see {@link Resolvable}
     */
    public EntityData(String reference, String displayTitle, Object entity) {
        this(reference, displayTitle, entity, null);
    }

    /**
     * Full constructor<br/>
     * Use this if you want to return the entity itself along with the key meta data and properties
     * 
     * @param reference a globally unique reference to an entity, 
     * consists of the entity prefix and id (e.g. /prefix/id)
     * @param entityDisplayTitle a string which is suitable for display and provides a short summary of the entity,
     * typically 100 chars or less, this may be the name or title of the entity represented by an entity
     * @param data an entity data object, see {@link Resolvable}
     * @param entityProperties a set of properties to return along with the entity information,
     * this may be presented and used for filtering,
     */
    public EntityData(String reference, String displayTitle, Object entity, Map<String, Object> entityProperties) {
        this.entityRef = new EntityReference(reference);
        this.entityReference = this.entityRef.getReference();
        this.entityId = this.entityRef.getId();
        this.entityDisplayTitle = displayTitle;
        this.entityURL = EntityView.DIRECT_PREFIX + this.entityReference;
        setData(entity);
        setEntityProperties(entityProperties);
    }


    /**
     * Minimal constructor - used for most basic cases<br/>
     * Use the setters to add in properties or the entity data if desired
     * 
     * @param ref an object which represents a globally unique reference to an entity, 
     * consists of the entity prefix and id
     * @param entityDisplayTitle a string which is suitable for display and provides a short summary of the entity,
     * typically 100 chars or less, this may be the name or title of the entity represented by an entity
     */
    public EntityData(EntityReference ref, String displayTitle) {
        this(ref, displayTitle, null, null);
    }

    /**
     * Basic constructor<br/>
     * Use this to construct a search result using the typical minimal amount of information,
     * Use the setters to add in properties or the entity data if desired
     * 
     * @param ref an object which represents a globally unique reference to an entity, 
     * consists of the entity prefix and id
     * @param entityDisplayTitle a string which is suitable for display and provides a short summary of the entity,
     * typically 100 chars or less, this may be the name or title of the entity represented by an entity
     * @param data an entity data object, see {@link Resolvable}
     */
    public EntityData(EntityReference ref, String displayTitle, Object entity) {
        this(ref, displayTitle, entity, null);
    }

    /**
     * Full constructor<br/>
     * Use this if you want to return the entity itself along with the key meta data and properties
     * 
     * @param ref an object which represents a globally unique reference to an entity, 
     * consists of the entity prefix and id
     * @param entityDisplayTitle a string which is suitable for display and provides a short summary of the entity,
     * typically 100 chars or less, this may be the name or title of the entity represented by an entity
     * @param data an entity data object, see {@link Resolvable}
     * @param entityProperties a set of properties to return along with the entity information,
     * this may be presented and used for filtering,
     */
    public EntityData(EntityReference ref, String displayTitle,
            Object entity, Map<String, Object> entityProperties) {
        if (ref == null || ref.isEmpty()) {
            throw new IllegalArgumentException("reference object cannot be null and must have values set");
        }
        this.entityRef = ref;
        this.entityReference = this.entityRef.getReference();
        this.entityId = this.entityRef.getId();
        this.entityDisplayTitle = displayTitle;
        this.entityURL = EntityView.DIRECT_PREFIX + this.entityReference;
        this.entityDisplayTitle = displayTitle;
        setData(entity);
        setEntityProperties(entityProperties);
    }

    /**
     * Using this as a data wrapper only
     * @param data any data to wrap this in
     */
    public EntityData(Object data) {
        this(data, null);
    }

    /**
     * Using this as a data wrapper only
     * @param data any data to wrap this in
     * @param entityProperties a set of properties to return along with the entity information,
     * this may be presented and used for filtering,
     */
    public EntityData(Object data, Map<String, Object> entityProperties) {
        setData(data);
        setEntityProperties(entityProperties);
        this.dataOnly = true;
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj)
            return false;
        if (!(obj instanceof EntityData))
            return false;
        else {
            EntityData castObj = (EntityData) obj;
            if (null == this.entityReference || null == castObj.entityReference)
                return false;
            else
                return (this.entityReference.equals(castObj.entityReference));
        }
    }

    @Override
    public int hashCode() {
        String hashStr = this.getClass().getName() + ":" + this.entityReference.hashCode();
        return hashStr.hashCode();
    }

    @Override
    public String toString() {
        return "ED: ref="+entityReference+":display="+entityDisplayTitle+":url="+entityURL+":props("+getEntityProperties().size()+"):dataOnly="+dataOnly+":data="+data;
    }

    public static class ReferenceComparator implements Comparator<EntityData>, Serializable {
        public final static long serialVersionUID = 1l;
        public int compare(EntityData o1, EntityData o2) {
            return o1.entityReference.compareTo(o2.entityReference);
        }
    }

    public static class TitleComparator implements Comparator<EntityData>, Serializable {
        public final static long serialVersionUID = 1l;
        public int compare(EntityData o1, EntityData o2) {
            return o1.getDisplayTitle().compareTo(o2.getDisplayTitle());
        }
    }

}
