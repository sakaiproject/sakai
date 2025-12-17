/**
 * $Id$
 * $URL$
 * EntityCustomAction.java - entity-broker - Jul 28, 2008 11:09:39 AM - azeckoski
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

package org.sakaiproject.entitybroker.entityprovider.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

import org.sakaiproject.entitybroker.EntityView.Method;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Redirectable;
import org.sakaiproject.entitybroker.util.TemplateParseUtil;


/**
 * This annotation indicates that this method will handle an incoming URL and 
 * do some processing to turn it into an outgoing URL OR just do some processing OR
 * indicate that a failure has occurred<br/>
 * Define the URL pattern to match AFTER the /prefix using {name} to indicate variables <br/>
 * Example: /{thing}/site/{siteId} will match the following URL: <br/>
 * /myprefix/123/site/456, the variables will be {prefix => myprefix, thing => 123, siteId => 456}<br/>
 * NOTE: all incoming URL templates must start with "/{prefix}" ({@link TemplateParseUtil#TEMPLATE_PREFIX}) <br/>
 * <br/>
 * NOTE: The method template patterns will be compared in the order they appear in your
 * source code so be careful that you do not have a really simple redirect pattern as the
 * first one as this can cause the other patterns to never be reached<br/>
 * The methods that this annotates should return a {@link String} or void<br/>
 * They can have the following parameter types: <br/>
 * (type => data which will be given to the method) <br/>
 * {@link String} : incoming URL <br/>
 * {@link Method} : the submission method (GET,POST,etc) <br/>
 * String[] : incoming URL segments, Example: /myprefix/123/apple => {'prefix','123','apple'}  <br/>
 * {@link Map} ({@link String} => {@link String}) : a map of the variable values in the {}, 
 * Example: pattern: /{prefix}/{thing}/apple, url: /myprefix/123/apple, would yield: {'thing' => '123','prefix' => 'mypreifx'} <br/>
 * Don't forget to handle the extensions as they will not automatically pass through,
 * use the {@link TemplateParseUtil#DOT_EXTENSION} and {@link TemplateParseUtil#EXTENSION} values from the variable map
 * which will contain the extension that was passed in <br/>
 * <br/>
 * Return should be one of the following: <br/>
 * 1) the URL to redirect to, will be processed as an external redirect if it starts with "http" or "/" 
 * (unless it starts with "/{prefix}"), otherwise it will be processed as an internal forward <br/>
 * 2) "" (empty string) to not redirect and return an empty success response <br/>
 * 3) null to not redirect and allow standard processing of the URL to continue <br/>
 * For failures: if there is a failure you should throw an IllegalStateException to indicate failure <br/>
 * This is the convention part of the {@link Redirectable} capability<br/>
 *
 * @see Redirectable
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface EntityURLRedirect {
   /**
    * @return the URL template pattern to match including the /prefix using {name} to indicate variables <br/>
    * Example: /{prefix}/{thing}/site/{siteId} will match the following URL: <br/>
    * /myprefix/123/site/456, the variables will be {prefix => myprefix, thing => 123, siteId => 456}
    * NOTE: all incoming URL templates must start with "/{prefix}" ({@link TemplateParseUtil#TEMPLATE_PREFIX})
    */
   String value();
}
