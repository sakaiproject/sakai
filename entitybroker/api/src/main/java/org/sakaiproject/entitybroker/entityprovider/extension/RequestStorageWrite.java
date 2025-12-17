/**
 * $Id$
 * $URL$
 * RequestStorage.java - entity-broker - Jul 24, 2008 1:55:12 PM - azeckoski
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

package org.sakaiproject.entitybroker.entityprovider.extension;

import java.util.Locale;
import java.util.Map;

/**
 * This allows write access to values which are stored in the current request thread,
 * these values are inaccessible outside of a request and will be destroyed
 * when the thread ends<br/>
 * This also "magically" exposes all the values in the request (attributes and params)
 * as if they were stored in the map as well, if there are conflicts then locally stored data always wins
 * over data from the request<br/>
 * Standard reserved keys have values that are always available:<br/>
 * <ul>
 * <li><b>_locale</b> : {@link Locale}</li>
 * <li><b>_requestEntityReference</b> : String</li>
 * <li><b>_requestActive</b> : [true,false]</li>
 * <li><b>_requestOrigin</b> : ['REST','EXTERNAL','INTERNAL']</li>
 * </ul>
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public interface RequestStorageWrite extends RequestStorage {

    /**
     * Store a value in the request storage with an associated key
     * @param key a key for a stored value
     * @param value an object to store
     * @throws IllegalArgumentException if the key OR value are null, 
     * also if an attempt is made to change a reserved value (see {@link RequestStorageWrite})
     */
    public void setStoredValue(String key, Object value);

    /**
     * Place all these params into the request storage
     * @param params map of string -> value params
     * @throws IllegalArgumentException if the key OR value are null, 
     * also if an attempt is made to change a reserved value (see {@link RequestStorageWrite})
     */
    public void setRequestValues(Map<String, Object> params);

    /**
     * Allows user to set the value of a key directly, including reserved keys
     * @param key the name of the value
     * @param value the value to store
     * @throws IllegalArgumentException if the key OR value are null, 
     * also if an attempt is made to change a reserved value (see {@link RequestStorageWrite})
     */
    public void setRequestValue(String key, Object value);

    /**
     * Clear all values in the request storage (does not wipe the values form the request itself)
     */
    public void reset();

}
