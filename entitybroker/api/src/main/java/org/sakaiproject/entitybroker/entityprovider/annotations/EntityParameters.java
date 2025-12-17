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
import java.util.Map;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Createable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Deleteable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Updateable;
import org.sakaiproject.entitybroker.entityprovider.extension.CustomAction;


/**
 * This annotation indicates that this method is accepts or expects certain parameters to be present
 * before it can be executed, these parameters are accessible in the params map <br/>
 * Any parameters included in this annotation can be documented using the i18n properties files
 * by placing a message at: prefix.action.actionname.field.fieldname OR
 * prefix.view.viewkey.field.fieldname <br/>
 * NOTE: there is no need to list the field names of the fields which are included in the
 * entity class or map here, this is for additional parameters only <br/>
 * <br/>
 * @see EntityCustomAction
 * @see CustomAction
 * @see Createable
 * @see Updateable
 * @see Deleteable
 * @see Resolvable
 * @see CollectionResolvable
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface EntityParameters {
    /**
     * Indicates the valid parameters for a custom action or standard entity method 
     * (e.g. {@link Createable#createEntity(EntityReference, Object, Map)}),
     * this will not stop parameters from coming in which are not included here
     * but it serves as an indicator of the ones which are expected and handled <br/>
     * By default this indicates no parameters are known <br/>
     * Any parameter included here can be documented using the i18n properties files
     * by placing a message at: prefix.action.actionname.field.fieldname OR
     * prefix.view.viewkey.field.fieldname <br/>
     * NOTE: there is no need to list the field names of the fields which are included in the
     * entity class or map here, this is for additional parameters only <br/>
     * @return an array of all handled parameters
     */
    String[] accepted() default {};
    /**
     * Indicates the parameters that must be included for the method this is on to succeed <br/>
     * this will stop the method from executing if any of these methods are not present and will
     * generate an approriate failure which indicates that fields are missing and which ones <br/>
     * By default this indicates no parameters are required <br/>
     * Any parameter included here can be documented using the i18n properties files
     * by placing a message at: prefix.action.actionname.field.fieldname OR
     * prefix.view.viewkey.field.fieldname <br/>
     * NOTE: there is no need to list the field names of the fields which are included in the
     * entity class or map here, this is for additional parameters only <br/>
     * @return an array of all required parameters
     */
    String[] required() default {};
}
