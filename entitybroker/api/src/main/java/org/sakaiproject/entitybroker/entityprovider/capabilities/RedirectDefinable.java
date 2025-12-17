/**
 * $Id$
 * $URL$
 * URLconfigurable.java - entity-broker - Jul 29, 2008 2:11:58 PM - azeckoski
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

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.extension.TemplateMap;
import org.sakaiproject.entitybroker.util.TemplateParseUtil;


/**
 * This entity type has the ability to define and handle configurable URLs<br/>
 * This adds the ability to supply a large set of simple redirects<br/>
 * URLs like this can be handled and supported:<br/>
 * /gradebook/7890/student/70987 to view all the grades for a student from a course <br/>
 * /gradebook/6758/item/Quiz1 to view a particular item in a gradebook by it's human readable name <br/>
 * /gradebook/item/6857657 to maybe just a view an item by its unique id. <br/>
 * This is one of the capability extensions for the {@link EntityProvider} interface<br/>
 * The convention interface is at {@link Redirectable}
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public interface RedirectDefinable extends Redirectable {

   /**
    * Defines the set of simple URL rewrites for this prefix<br/>
    * Simple rewrites require no processing logic to handle the redirect and
    * the redirect is always processed before anything validity checks happen<br/>
    * Some examples:<br/>
    * /myprefix/item/{id} => /my-item/{id} <br/>
    * /myprefix/{year}/{month}/{day} => /myprefix/?date={year}-{month}-{day}<br/>
    * <b>incomingURL</b> is the URL template pattern to match including the /prefix using {name} to indicate variables <br/>
    * Example: /{prefix}/{thing}/site/{siteId} will match the following URL: <br/>
    * /myprefix/123/site/456, the variables will be {prefix => myprefix, thing => 123, siteId => 456} <br/>
    * NOTE: all incoming URL templates must start with "/{prefix}" ({@link TemplateParseUtil#TEMPLATE_PREFIX}) <br/>
    * <b>outgoingURL</b> is the URL template pattern to fill with values from the incoming pattern,
    * this can start with anything, but will be processed as an external redirect if it starts with "http" or "/" 
    * (unless it starts with "/{prefix}"), otherwise it will be processed as an internal forward <br/>
    * NOTE: the special variables which are available to all outgoing URLs from the system are:<br/>
    * {prefix} = the entity prefix <br/>
    * {extension} = the extension if one is available or '' if none <br/>
    * {dot-extension} = the extension with a '.' prepended if one is set or '' if no extension <br/>
    * {query-string} = the query string (e.g auto=true) or '' if none <br/>
    * {question-query-string} = the query string with a '?' prepended (e.g ?auto=true) or '' if none <br/>
    * 
    * @return the array of template mappings (incomingURL pattern => outgoingURL pattern) 
    * OR null/empty if you have no simple mappings
    */
   public TemplateMap[] defineURLMappings();

}
