/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.memory.api;

/**
 * This is here to allow the old caches which have weird rules and checks to still work
 * These should be migrated away from the old way of doing things as soon as possible
 *
 * @deprecated Do NOT USE THIS - it will be removed as soon as possible
 */
public interface CompletionCache {

    /**
     * Cache an object
     *
     * @param key
     *        The key with which to find the object.
     * @param payload
     *        The object to cache.
     * @param duration
     *        The time to cache the object (seconds).
     * @deprecated Since Sakai 2.5.0
     * @see Cache#put(Object, Object)
     */
    void put(Object key, Object payload, int duration);

    /**
     * Disable the cache.
     * @deprecated Since Sakai 2.9
     */
    void disable();

    /**
     * Enable the cache.
     * @deprecated Since Sakai 2.9
     */
    void enable();

    /**
     * Is the cache disabled?
     *
     * @return true if the cache is disabled, false if it is enabled.
     * @deprecated Since Sakai 2.9
     */
    boolean disabled();

    /**
     * Are we complete?
     *
     * @return true if we have all the possible entries cached, false if not.
     * @deprecated Since Sakai 2.9
     */
    boolean isComplete();

    /**
     * Set the cache to be complete for one level of the reference hierarchy.
     *
     * @param path
     *        The reference to the completion level.
     * @deprecated Since Sakai 2.9
     */
    void setComplete(String path);

    /**
     * Set the cache to be complete, containing all possible entries.
     * @deprecated Since Sakai 2.9
     */
    void setComplete();

    /**
     * Are we complete for one level of the reference hierarchy?
     *
     * @param path
     *        The reference to the completion level.
     * @return true if we have all the possible entries cached, false if not.
     * @deprecated Since Sakai 2.9
     */
    boolean isComplete(String path);

}
