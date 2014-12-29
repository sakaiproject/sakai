/**
 * $Id$
 * $URL$
 * EntityCustomAction.java - entity-broker - Jul 28, 2008 11:09:39 AM - azeckoski
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

package org.sakaiproject.entitybroker.entityprovider.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation indicates that this method parameter should be replaced by the value 
 * in the HTTP query parameter, header, or form parameter whose name matches the name set in the annotation <br/>
 * Binds the value(s) of a HTTP query parameter to a resource method parameter. 
 * A default value can be specified for this annotation. <br/>
 * <br/>
 * The type T of the annotated parameter, field, or property must either: <br/>
 *  1. Be a primitive type <br/>
 *  2. Have a constructor that accepts a single String argument <br/>
 *  3. Have a static method named valueOf that accepts a single String argument (see, for example, Integer.valueOf(String)) <br/>
 *  4. Be List<T>, Set<T> or SortedSet<T>, where T satisfies 2 or 3 above. The resulting collection is read-only. <br/>
 * <br/>
 * If the type is not one of those listed in 4 above then the first value (lexically) of the parameter is used.
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface EntityHttpParam {
    public static String NULL = "?.NULL.="; // this is lame but we cannot use null here so this will have to do, it should be almost impossible for a param to have this value
    /**
     * Defines the name of the HTTP query parameter, header, or form parameter 
     * whose value will be used to initialize the value of the annotated method argument
     * @return the name of the parameter
     */
    String value();
    /**
     * Defines the default value for this parameter (the value to use when no value is found in the request),
     * by default the method parameter will be set to null if this is not set
     * @return the default value to use (will be converted as needed)
     */
    String deflt() default NULL;
}
