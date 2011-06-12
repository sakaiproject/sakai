/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.test.mocks;

import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.CacheRefresher;
import org.sakaiproject.memory.api.Cacher;
import org.sakaiproject.memory.api.GenericMultiRefCache;
import org.sakaiproject.memory.api.MemoryPermissionException;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.memory.api.MultiRefCache;

public class FakeMemoryService implements MemoryService {

	public long getAvailableMemory() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	public Cache newCache() {
		// TODO Auto-generated method stub
		return null;
	}

	public Cache newCache(String arg0) {
		return new FakeCache();
	}

	public Cache newCache(CacheRefresher arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public Cache newCache(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public Cache newCache(CacheRefresher arg0, long arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public Cache newCache(String arg0, CacheRefresher arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public Cache newCache(String arg0, CacheRefresher arg1, String arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	public Cache newHardCache() {
		// TODO Auto-generated method stub
		return null;
	}

	public Cache newHardCache(CacheRefresher arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public Cache newHardCache(CacheRefresher arg0, long arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public Cache newHardCache(long arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
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

	public void resetCachers() throws MemoryPermissionException {
		// TODO Auto-generated method stub

	}

	public void unregisterCacher(Cacher arg0) {
		// TODO Auto-generated method stub

	}

	public void evictExpiredMembers() throws MemoryPermissionException {
		// TODO Auto-generated method stub
		
	}

	public GenericMultiRefCache newGenericMultiRefCache(String cacheName) {
		// TODO Auto-generated method stub
		return null;
	}

}
