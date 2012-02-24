/**
 * $Id$
 * $URL$
 * DescribeDefineable.java - entity-broker - Jul 18, 2008 5:46:15 PM - azeckoski
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

package org.sakaiproject.entitybroker.entityprovider.capabilities;

import java.util.Locale;

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;


/**
 * Allows an entity to define the description of itself in code rather than using properties,
 * this will be called each time a description is needed so it should be efficient<br/>
 * This is the configuration interface<br/>
 * This is one of the capability extensions for the {@link EntityProvider} interface<br/>
 * @see Describeable
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public interface DescribeDefineable extends Describeable {

   /**
    * Allows for complete control over the descriptions of entities<br/>
    * This will always be called first if it is defined, returning a null will
    * default to attempting to get the value from the properties (if any are defined),
    * returning an empty string will cause nothing to be shown for the description
    * 
    * @param locale this is the locale that the description should be created for
    * @param descriptionKey (optional) if null then the general description of the entity should be created,
    * otherwise provide the description for the capability that was provided (e.g. Resolveable) OR
    * the custom action, starts with action.&lt;actionKey&gt; (e.g. action.promote),
    * see the {@link Describeable} interface for information about the other keys that will be passed in
    * @return the string which describes this entity or this capability for this entity OR '' for no description
    * OR return null to allow this to attempt to get the value from the properties file
    */
   public String getDescription(Locale locale, String descriptionKey);

}
