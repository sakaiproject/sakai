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

import java.util.Map;

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;


/**
 * This entity type has the ability to define and handle configurable URLs<br/>
 * This adds the ability to control all redirects via a central method<br/>
 * URLs like this can be handled and supported:<br/>
 * /gradebook/7890/student/70987 to view all the grades for a student from a course <br/>
 * /gradebook/6758/item/Quiz1 to view a particular item in a gradebook by it's human readable name <br/>
 * /gradebook/item/6857657 to maybe just a view an item by its unique id. <br/>
 * This is one of the capability extensions for the {@link EntityProvider} interface<br/>
 * The convention interface is at {@link Redirectable}, there is also a capability
 * for handling simple redirects at {@link RedirectDefinable}
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public interface RedirectControllable extends Redirectable {

   /**
    * Defines all the URL patterns that will be matched and passed through to
    * {@link #handleRedirects(String, String[], Map)}
    * NOTE: /{prefix}/ must be included as the start of the template<br/>
    * 
    * @return a list of all the handled URL patterns
    */
   public String[] defineHandledTemplatePatterns();

   /**
    * Explicitly handles all the incoming URLs which match the patterns given by {@link #defineHandledTemplatePatterns()}
    * do some processing to turn it into an outgoing URL OR just do some processing OR
    * indicate that a failure has occurred<br/>
    *
    * @param matchedPattern the template pattern that was matched
    * @param incomingURL the incoming URL that was matched by a URL pattern
    * @param incomingSegments incoming URL segments, Example: /prefix/123/apple => {'prefix','123','apple'}
    * @param incomingVariables a map of the values in the {} (prefix is always included), 
    * Example: pattern: /prefix/{thing}/apple, url: /prefix/123/apple, would yield: 'thing' => '123'
    * @return should be one of the following: <br/>
    * 1) the URL to redirect to, will be processed as an external redirect if it starts with "http" or "/" 
    * (unless it starts with "/{prefix}"), otherwise it will be processed as an internal forward <br/>
    * 2) "" (empty string) to not redirect and return an empty success response <br/>
    * 3) null to not redirect and allow standard processing of the URL to continue <br/>
    * @throws IllegalStateException if there is a failure, this will cause the server to return a redirect failure
    */
   public String handleRedirects(String matchedTemplate, String incomingURL, String[] incomingSegments, Map<String, String> incomingVariables);

}
