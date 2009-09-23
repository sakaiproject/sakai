/**
 * $URL:$
 * $Id:$
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.DerivedCache;

public class FakeCache implements Cache {
	private Map<Object, Object>	map = new HashMap<Object, Object>();
	
	public void attachDerivedCache(DerivedCache arg0) {
	}

	public void clear() {
		map.clear();
	}

	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	public boolean containsKeyExpiredOrNot(Object key) {
		return containsKey(key);
	}

	public void destroy() {
		map = null;
	}

	public void disable() {		
	}

	public boolean disabled() {
		return false;
	}

	public void enable() {
	}

	public void expire(Object o) {
		map.remove(o);
	}

	public Object get(Object key) {
		return map.get(key);
	}

	public List getAll() {
		List<Object> all = new ArrayList<Object>();
		for(Object o : map.values()) {
			all.add(o);
		}
		return all;
	}

	public List getAll(String key) {
		List<Object> all = new ArrayList<Object>();
		all.add(get(key));
		return all;
	}

	public Object getExpiredOrNot(Object key) {
		return get(key);
	}

	public List getIds() {
		return getKeys();
	}

	public List getKeys() {
		List<Object> keys = new ArrayList<Object>();
		for(Object k : map.keySet()) {
			keys.add(k);
		}
		return keys;
	}

	public void holdEvents() {
	}

	public boolean isComplete() {
		return false;
	}

	public boolean isComplete(String arg0) {
		return false;
	}

	public void processEvents() {
	}

	public void put(Object key, Object object) {
		map.put(key, object);
	}

	public void put(Object key, Object object, int arg2) {
		map.put(key, object);
	}

	public void remove(Object key) {
		map.remove(key);
	}

	public void setComplete() {
	}

	public void setComplete(String arg0) {
	}

	public String getDescription() {
		return null;
	}

	public long getSize() {
		return map.size();
	}

	public void resetCache() {
		clear();
	}

}
