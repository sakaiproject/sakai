/**
 * $Id$
 * $URL$
 * URLconfigurable.java - entity-broker - Jul 29, 2008 2:11:58 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
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
 * The convention interface is at {@link URLConfigurable}, there is also a capability
 * for handling simple redirects at {@link URLConfigDefinable}
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public interface URLConfigControllable extends URLConfigurable {

   /**
    * Defines all the URL patterns that will be matched and passed through to
    * {@link #handleRedirects(String, String[], Map)}
    * @return a list of all the handled URL patterns
    */
   public String[] defineHandledPatterns();

   /**
    * Explicitly handles all the incoming URLs which match the patterns given by {@link #defineHandledPatterns()}
    * do some processing to turn it into an outgoing URL OR just do some processing OR
    * indicate that a failure has occurred<br/>
    *
    * @param matchedPattern the pattern that was matched
    * @param incomingURL the incoming URL the was matched by a URL pattern
    * @param incomingSegments incoming URL segments, Example: /prefix/123/apple => {'prefix','123','apple'}
    * @param incomingVariables a map of the values in the {}, 
    * Example: pattern: /prefix/{thing}/apple, url: /prefix/123/apple, would yield: 'thing' => '123'
    * @return the URL to redirect to (relative) OR null/"" to not redirect and return OK
    * @throws IllegalStateException if there is a failure, this will cause the server to return a redirect failure
    */
   public String handleRedirects(String matchedPattern, String incomingURL, String[] incomingSegments, Map<String, String> incomingVariables);

}
