/*
 *  Copyright (c) 2017, University of Dayton
 *
 *  Licensed under the Educational Community License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *              http://opensource.org/licenses/ecl2
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.sakaiproject.attendance.cache;

import lombok.Setter;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;

/**
 * A shell to eventually implement caching
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 */
public class CacheManagerImpl implements CacheManager {

    /**
     * {@inheritDoc}
     */
    public Cache createCache(String cacheName) {
        return memoryService.newCache(cacheName);
    }

    @Setter
    private MemoryService memoryService;
}
