/**
 * $Id$
 * $URL$
 * RequestStorage.java - entity-broker - Jul 24, 2008 1:55:12 PM - azeckoski
 **************************************************************************
 * Copyright 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
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
 * This allows access to values which are stored in the current request thread,
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
public interface RequestStorage {

   /**
    * Indicates the origin of the current request
    */
   public static enum RequestOrigin { REST, EXTERNAL, INTERNAL };

   /**
    * Reserved keys with special data in them,
    * see {@link RequestStorage}
    */
   public static enum ReservedKeys { _locale, _requestEntityReference, _requestActive, _requestOrigin };

   /**
    * Get the data as a map for easy access to the full set of keys/values, 
    * this is a copy and changing it has no effect on the data in the request
    * @return a copy of the internal storage of request keys and values as a map, 
    * may be empty but will not be null
    */
   public Map<String, Object> getStorageMapCopy();

   /**
    * Get a value that is stored in the request for a specific key
    * @param key a key for a stored value
    * @return the stored value if found OR null if not found
    * @throws IllegalArgumentException if the key is null
    */
   public Object getStoredValue(String key);

   /**
    * Store a value in the request storage with an associated key
    * @param key a key for a stored value
    * @param value an object to store
    * @throws IllegalArgumentException if the key OR value are null, 
    * also if an attempt is made to change a reserved value (see {@link RequestStorage})
    */
   public void setStoredValue(String key, Object value);

}
