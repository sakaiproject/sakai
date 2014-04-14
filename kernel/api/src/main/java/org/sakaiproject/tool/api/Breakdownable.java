/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2014 The Apereo Foundation.
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
 *
 **********************************************************************************/
package org.sakaiproject.tool.api;

import java.io.Serializable;

/**
 * Allows a class type to be broken down for caching or export and then later
 * rebuilt into an equivalent object in memory.
 *
 * This follow the typical pattern which is used in EntityBroker for providers
 * but is currently generally independent from that system.
 *
 * Breakdownable classes are classes that are not by themselves serializable,
 * but they can be converted into a Serializable format using the makeBreakdown.
 * In addition, the class can be reconstituted using the doRebuild
 *
 * @author Aaron Zeckoski (azeckoski @ unicon.net)
 * @author mgillian
 */
public interface Breakdownable<T> {
    
    /**
     * @return the class which is managed by this breakdown and rebuild processor
     */
    public Class<T> defineHandledClass();

    /**
     * Return the simplest amount of data needed to rebuild the implementing object later.
     * e.g. For a Site this might only be the id since trying to store the whole site is dangerous (would get out of date)
     * and too large and non-serializable, the id would be enough to rebuild it later via a lookup
     *
     * @param sizeHint a hint about the size to make the returned serialized data
     * @return serialized version of the data needed to rebuild this object later
     */
    public Serializable makeBreakdown(T object, BreakdownableSize sizeHint);

    /**
     * Rebuilds an object using breakdown data.
     * Should return a fully formed version of the implementing object.
     * Can assume the current ClassLoader is the one for this Class.
     *
     * @param objectData serialized breakdown data (whatever was returned from #makeBreakdown)
     * @param sizeHint a hint about the size of the serialized data
     * @return the actual object (this should never be null)
     * @throws java.lang.IllegalArgumentException if the inputs are invalid
     * @throws java.lang.RuntimeException if there is a failure attempting to create the object
     */
    public T doRebuild(Serializable objectData, BreakdownableSize sizeHint);

    /**
     * OPTIONAL
     * Allow override of the default handling, if this returns null then the ClassLoader will be the one for this handler.
     * All invalid outputs are simply ignored and the default is used.
     *
     * @return the ClassLoader for the class type handled by this Breakdownable
     */
    public ClassLoader defineClassLoader();

    /**
     * BreakdownableSize is additional information about how the class should be serialized
     */
    public enum BreakdownableSize {
        /**
         * Make the resulting data as small as possible,
         * fast to breakdown but slow to rebuild,
         * used for data replication and cache store with other objects
         */
        TINY,
        /**
         * Make the object data complete,
         * used for generating JSON or XML formatted data
         */
        EXPORT
    }

}
