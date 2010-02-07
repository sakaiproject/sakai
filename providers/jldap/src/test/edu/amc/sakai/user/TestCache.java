package edu.amc.sakai.user;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.DerivedCache;


/**
 * Quickly hacked up cache which just uses a map so we can run the tests.
 * @author buckett
 *
 */
public class TestCache implements Cache {

	Map map = Collections.synchronizedMap(new HashMap());

	
	// ============ Delegate to the map ======= //
	public void clear() {
		map.clear();
	}

	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	public boolean equals(Object o) {
		return map.equals(o);
	}

	public Object get(Object key) {
		return map.get(key);
	}

	public int hashCode() {
		return map.hashCode();
	}

	public void put(Object key, Object value) {
		map.put(key, value);
	}

	public void remove(Object key) {
		map.remove(key);
	}

	// ============ All the extra Cache API crap ======= //
	public void attachDerivedCache(DerivedCache cache) {
		throw new UnsupportedOperationException();
	}

	public boolean containsKeyExpiredOrNot(Object key) {
		return containsKey(key);
	}

	public void destroy() {
		
	}

	public void disable() {
		
	}

	public boolean disabled() {
		return false;
	}

	public void enable() {

	}

	public void expire(Object key) {
		map.remove(key);
	}

	public List getAll() {
		return Collections.list(Collections.enumeration(map.values()));
	}

	public List getAll(String path) {
		throw new UnsupportedOperationException();
	}

	public Object getExpiredOrNot(Object key) {
		return get(key);
	}

	public List getIds() {
		throw new UnsupportedOperationException();
	}

	public List getKeys() {
		return Collections.list(Collections.enumeration(map.keySet()));
	}

	public void holdEvents() {
		
	}

	public boolean isComplete() {
		return false;
	}

	public boolean isComplete(String path) {
		return false;
	}

	public void processEvents() {
		
	}

	public void put(Object key, Object payload, int duration) {
		put(key, payload);
	}

	public void setComplete() {

	}

	public void setComplete(String path) {
		
	}

	public String getDescription() {
		return getClass().getName();
	}

	public long getSize() {
		return map.size();
	}

	public void resetCache() {
		map.clear();
	}
	

}
