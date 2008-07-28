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

import java.io.OutputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.CustomAction;


/**
 * This annotation indicates that this method is a custom action from {@link ActionsExecutable},
 * this should not be placed on any methods defined by a capability but should be placed on methods
 * which you want to be exposed as custom actions<br/>
 * By default the name of the method is used as the action key and this will work for read requests 
 * ({@link EntityView#VIEW_SHOW}), you can add in action and viewKey annotation params to change those settings<br/>
 * The methods annotated by this can have the following parameter types: <br/>
 * (type => data which will be given to the method) <br/>
 * {@link EntityView} : the current entity view for this request (contains extension, url, segments) <br/>
 * {@link EntityReference} : the current entity reference (prefix and optional id) <br/>
 * {@link String} : entity prefix <br/>
 * {@link OutputStream} : stream to place outbound data into <br/>
 * {@link Map} ({@link String} => {@link Object}) : a map of the actions parameters (params from the action request) <br/>
 * <br/>
 * These methods should return one of the following: <br/>
 * 1) null (this is ok in most circumstances to indicate the method is done, use an exception to indicate failure) <br/>
 * 2) an {@link ActionReturn} (this is a special object used to indicate return states and handle binary data) <br/>
 * 3) a UTF-8 encoded OutputStream or String <br/>
 * 4) a List of entity objects <br/>
 * 5) an entity object <br/>
 *
 * @see CustomAction
 * @see ActionsExecutable
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface EntityCustomAction {
   String action() default "";
   String viewKey() default EntityView.VIEW_SHOW;
}
