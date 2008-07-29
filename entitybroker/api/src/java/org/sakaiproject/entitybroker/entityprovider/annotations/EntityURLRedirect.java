/**
 * $Id$
 * $URL$
 * EntityCustomAction.java - entity-broker - Jul 28, 2008 11:09:39 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.entityprovider.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

import org.sakaiproject.entitybroker.entityprovider.capabilities.URLConfigurable;


/**
 * This annotation indicates that this method will handle an incoming URL and 
 * do some processing to turn it into an outgoing URL OR just do some processing OR
 * indicate that a failure has occurred<br/>
 * The methods that this annotates should return a {@link String} or void<br/>
 * They can have the following parameter types: <br/>
 * (type => data which will be given to the method) <br/>
 * {@link String} : incoming URL <br/>
 * String[] : incoming URL segments, Example: /prefix/123/apple => {'prefix','123','apple'}  <br/>
 * {@link Map} ({@link String} => {@link String}) : a map of the values in the {}, 
 * Example: pattern: /prefix/{thing}/apple, url: /prefix/123/apple, would yield: 'thing' => '123' <br/>
 * <br/>
 * Return should be the URL to redirect to (relative) OR null/"" to not redirect and return OK,
 * if there is a failure you should throw an IllegalStateException to indicate failure
 * This is the convention part of the {@link URLConfigurable} capability<br/>
 *
 * @see URLConfigurable
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface EntityURLRedirect {
   String incomingPattern();
}
