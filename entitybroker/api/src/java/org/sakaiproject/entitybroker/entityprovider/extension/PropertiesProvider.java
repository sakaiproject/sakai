/**
 * $Id$
 * $URL$
 * AutoRegister.java - entity-broker - 31 May 2007 7:01:11 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2007, 2008 The Sakai Foundation
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
 **/

package org.sakaiproject.entitybroker.entityprovider.extension;

import java.util.List;
import java.util.Map;

import org.sakaiproject.entitybroker.entityprovider.capabilities.PropertyProvideable;

/**
 * This simple defines the methods correctly which are shared between a set of interfaces, see
 * {@link PropertyProvideable} for more information
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface PropertiesProvider {

   /**
    * Retrieve a meta property value for a specific property name on a specific entity
    * 
    * @param reference
    *           a globally unique reference to an entity
    * @param name
    *           the name (key) for this property
    * @return the property value for this name and entity, null if none is set
    */
   public String getPropertyValue(String reference, String name);

   /**
    * Retrieve all meta properties for this entity as a map of name->value
    * 
    * @param reference
    *           a globally unique reference to an entity
    * @return a map of String (name) -> String (value)
    */
   public Map<String, String> getProperties(String reference);

   /**
    * Set a meta property value on a specific entity, setting a value to null will remove the related
    * value from persistence, passing the name and value as null will remove all the properties for
    * this entity from persistence <br/> <b>Note:</b> Do not use this as a substitute for storing
    * core meta data on your actual persistent entities, this is meant to provide for the case where
    * runtime properties need to be added to an entity, persisted, and later retrieved and should be
    * seen as a lazy way to expand the fields of a persistent entity
    * 
    * @param reference
    *           a globally unique reference to an entity
    * @param name
    *           the name (key) for this property, if this and the value are set to null then remove
    *           all the properties for this entity from persistence
    * @param value
    *           the value to store for this property, if null then remove the related value from
    *           persistence
    */
   public void setPropertyValue(String reference, String name, String value);

   /**
    * Allows searching for entities by meta property values, at least one of the params (prefix, name,
    * searchValue) must be set in order to do a search, (searches which return all references to all
    * entities with properties are not allowed) <br/> 
    * <b>WARNING:</b> this search is very fast but
    * will not actually limit by properties that are placed on the entity itself or
    * return the entity itself and is not a substitute for an API which allows searches of your
    * entities (e.g List<YourEntity> getYourStuff(Search search); )
    * 
    * @param prefixes
    *           limit the search to a specific entity prefix or set of prefixes, 
    *           this must be set and cannot be an empty array
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
   public List<String> findEntityRefs(String[] prefixes, String[] name, String[] searchValue, boolean exactMatch);

}
