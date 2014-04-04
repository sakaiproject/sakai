/******************************************************************************
 * $URL: https://source.sakaiproject.org/svn/master/trunk/header.java $
 * $Id: header.java 307632 2014-03-31 15:29:37Z azeckoski@unicon.net $
 ******************************************************************************
 *
 * Copyright (c) 2003-2014 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *****************************************************************************/

package org.sakaiproject.memory.mock;

import org.sakaiproject.memory.api.*;

import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.CacheRefresher;
import org.sakaiproject.memory.api.Cacher;
import org.sakaiproject.memory.api.GenericMultiRefCache;
import org.sakaiproject.memory.api.MultiRefCache;

/**
 * Mock MemoryService for use in testing
 * Partly functional
 */
public class MemoryService implements org.sakaiproject.memory.api.MemoryService {

    public long getAvailableMemory() {
        // TODO Auto-generated method stub
        return 0;
    }

    public String getStatus() {
        // TODO Auto-generated method stub
        return "Caches status";
    }

    public Cache newCache() {
        return new org.sakaiproject.memory.mock.Cache();
    }

    public Cache newCache(String arg0) {
        return new org.sakaiproject.memory.mock.Cache();
    }

    public Cache newCache(CacheRefresher arg0, String arg1) {
        return new org.sakaiproject.memory.mock.Cache();
    }

    public Cache newCache(String arg0, String arg1) {
        return new org.sakaiproject.memory.mock.Cache();
    }

    public Cache newCache(CacheRefresher arg0, long arg1) {
        return new org.sakaiproject.memory.mock.Cache();
    }

    public Cache newCache(String arg0, CacheRefresher arg1) {
        return new org.sakaiproject.memory.mock.Cache();
    }

    public Cache newCache(String arg0, CacheRefresher arg1, String arg2) {
        return new org.sakaiproject.memory.mock.Cache();
    }

    public Cache newHardCache() {
        return new org.sakaiproject.memory.mock.Cache();
    }

    public Cache newHardCache(CacheRefresher arg0, String arg1) {
        return new org.sakaiproject.memory.mock.Cache();
    }

    public Cache newHardCache(CacheRefresher arg0, long arg1) {
        return new org.sakaiproject.memory.mock.Cache();
    }

    public Cache newHardCache(long arg0, String arg1) {
        return new org.sakaiproject.memory.mock.Cache();
    }

    public MultiRefCache newMultiRefCache(long arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public MultiRefCache newMultiRefCache(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public void registerCacher(Cacher arg0) {
        // TODO Auto-generated method stub

    }

    public void resetCachers() {
        // TODO Auto-generated method stub

    }

    public void unregisterCacher(Cacher arg0) {
        // TODO Auto-generated method stub

    }

    public void evictExpiredMembers() {
        // TODO Auto-generated method stub

    }

    public GenericMultiRefCache newGenericMultiRefCache(String cacheName) {
        // TODO Auto-generated method stub
        return null;
    }

}
