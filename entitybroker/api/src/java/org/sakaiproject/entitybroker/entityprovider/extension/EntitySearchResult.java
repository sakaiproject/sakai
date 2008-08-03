/**
 * $Id$
 * $URL$
 * EntitySearchResult.java - entity-broker - Aug 3, 2008 6:03:53 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.entityprovider.extension;

import java.lang.ref.WeakReference;
import java.util.Map;

import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;


/**
 * This is an object to hold data from a search which would normally return entity references,
 * This is basically a POJO which allows us to return a few results instead of only the reference,
 * it helps us get the data back more efficiently and makes it easier on developers who
 * need to search for entities
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class EntitySearchResult {

   /**
    * The entity reference -  a globally unique reference to an entity, 
    * consists of the entity prefix and optional segments (normally the id at least),
    * this should be set by the constructor only
    */
   private String reference;
   /**
    * The entity reference -  a globally unique reference to an entity, 
    * consists of the entity prefix and optional segments (normally the id at least)
    */
   public String getReference() {
      return reference;
   }

   /**
    * The entity reference object which makes it easy to get to the prefix or id of this entity,
    * this should be set by the constructor only
    */
   private EntityReference entityReference;
   /**
    * The entity reference object which makes it easy to get to the prefix or id of this entity is needed
    */
   public EntityReference getEntityReference() {
      return entityReference;
   }

   /**
    * (OPTIONAL - may be null)
    * The URL to the entity represented by this reference,
    * should be an absolute URL (server name optional),
    * if this is null then the URL is formed from the reference
    */
   private String URL;
   /**
    * The URL to the entity represented by this reference,
    * should be an absolute URL (server name optional)
    */
   public String getEntityURL() {
      return URL;
   }

   /**
    * (OPTIONAL - may be null)
    * A string which is suitable for display and provides a short summary of the entity,
    * typically 100 chars or less, this may the name or title of the data represented by an entity
    */
   private String displayTitle;
   /**
    * (OPTIONAL - may be null)
    * A string which is suitable for display and provides a short summary of the entity,
    * typically 100 chars or less, this may the name or title of the data represented by an entity
    */
   public String getDisplayTitle() {
      return displayTitle;
   }

   /**
    * (OPTIONAL - may be null)
    * A set of properties to return along with the entity information,
    * this may be presented and used for filtering,
    * this will be null or empty if it is not used
    */
   private Map<String, String> entityProperties;
   /**
    * A set of properties to return along with the entity information,
    * this may be presented and used for filtering,
    * should be null or empty if not used
    * @param entityProperties a map of property name => value
    */
   public void setEntityProperties(Map<String, String> entityProperties) {
      this.entityProperties = entityProperties;
   }
   /**
    * (OPTIONAL - may be null)
    * A set of properties to return along with the entity information,
    * this may be presented and used for filtering,
    * this will be null or empty if it is not used
    */
   public Map<String, String> getEntityProperties() {
      return entityProperties;
   }

   /**
    * (OPTIONAL - may be null)
    * This is the entity object itself (if there is one),
    * this is included at the discretion of the entity provider author,
    * if this is null then the entity data is not available or would be prohibitively large (i.e. typically left out for efficiency)
    */
   private WeakReference<Object> entity;
   public void setEntity(Object entity) {
      if (entity != null) {
         this.entity = new WeakReference<Object>(entity);
      } else {
         this.entity = null;
      }
   }
   /**
    * (OPTIONAL - may be null)
    * This is the entity object itself (if there is one),
    * this is included at the discretion of the entity provider author,
    * if this is null then the entity data is not available or would be prohibitively large (i.e. typically left out for efficiency)
    */
   public Object getEntity() {
      if (this.entity == null) {
         return null;
      } else {
         return this.entity.get();
      }
   }

   /**
    * Absolute minimal constructor - should only be used when no other information is available
    * @param reference a globally unique reference to an entity, 
    * consists of the entity prefix and optional segments (normally the id at least)
    * @param displayTitle a string which is suitable for display and provides a short summary of the entity,
    * typically 100 chars or less, this may the name or title of the data represented by an entity
    */
   public EntitySearchResult(String reference, String displayTitle) {
      this.entityReference = new EntityReference(reference);
      this.reference = reference;
      if (displayTitle == null) {
         displayTitle = "Entity:" + reference;
      }
      this.displayTitle = displayTitle;
      this.URL = EntityView.DIRECT_PREFIX + this.entityReference.getReference();
   }

   /**
    * Basic constructor
    * Use this to construct a search result using the typical minimal amount of information,
    * Use the setters to add in properties or the entity if desired
    * 
    * @param reference a globally unique reference to an entity, 
    * consists of the entity prefix and optional segments (normally the id at least)
    * @param displayTitle a string which is suitable for display and provides a short summary of the entity,
    * typically 100 chars or less, this may the name or title of the data represented by an entity
    * @param entityURL the URL to the current entity, typically generated using {@link EntityBroker#getEntityURL(String)}
    */
   public EntitySearchResult(String reference, String displayTitle, String entityURL) {
      this(reference, displayTitle);
      if (entityURL != null) {
         this.URL = entityURL;
      }
   }

   /**
    * Full constructor
    * Use this if you want to return the entity itself along with the key meta data
    * Use the setters to add in properties if desired
    * 
    * @param reference a globally unique reference to an entity, 
    * consists of the entity prefix and optional segments (normally the id at least)
    * @param displayTitle a string which is suitable for display and provides a short summary of the entity,
    * typically 100 chars or less, this may the name or title of the data represented by an entity
    * @param entityURL the URL to the current entity, typically generated using {@link EntityBroker#getEntityURL(String)}
    * @param entity an entity object, see {@link Resolvable}
    */
   public EntitySearchResult(String reference, String displayTitle, String entityURL, Object entity) {
      this(reference, displayTitle, entityURL);
      setEntity(entity);
   }

   /**
    * Constructor for use internally, this will primarily be used by the internal system to construct the search
    * result from a fully qualified entity
    * @param ref an entity reference object
    * @param displayTitle a string which is suitable for display and provides a short summary of the entity,
    * typically 100 chars or less, this may the name or title of the data represented by an entity
    * @param entityURL the URL to the current entity, typically generated using {@link EntityBroker#getEntityURL(String)}
    * @param entity an entity object, see {@link Resolvable}
    */
   public EntitySearchResult(EntityReference ref, String displayTitle, String entityURL, Object entity) {
      this.reference = ref.getReference();
      if (displayTitle == null) {
         displayTitle = "Entity:" + reference;
      }
      this.displayTitle = displayTitle;
      if (entityURL == null) {
         entityURL = EntityView.DIRECT_PREFIX + this.entityReference.getReference();
      }
      this.URL = entityURL;
      setEntity(entity);
   }

}
