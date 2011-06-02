package edu.amc.sakai.user;

import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.CacheRefresher;
import org.sakaiproject.memory.api.Cacher;
import org.sakaiproject.memory.api.MemoryPermissionException;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.memory.api.MultiRefCache;
import org.sakaiproject.memory.api.GenericMultiRefCache;

/**
 * Quickly hacked up cache so we can run the tests.
 * @author buckett
 *
 */
public class TestMemoryService implements MemoryService {

	public GenericMultiRefCache newGenericMultiRefCache(String cacheName) {
		return null;
	}
	
	public long getAvailableMemory() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	public Cache newCache(CacheRefresher refresher, String pattern) {
		return new TestCache();
	}

	public Cache newCache(String cacheName, CacheRefresher refresher,
			String pattern) {
		return new TestCache();
	}

	public Cache newCache(String cacheName, String pattern) {
		return new TestCache();
	}

	public Cache newCache(CacheRefresher refresher, long sleep) {
		return new TestCache();
	}

	public Cache newCache(String cacheName, CacheRefresher refresher) {
		return new TestCache();
	}

	public Cache newCache() {
		return new TestCache();
	}

	public Cache newCache(String cacheName) {
		return new TestCache();
	}

	public Cache newHardCache(CacheRefresher refresher, String pattern) {
		return new TestCache();
	}

	public Cache newHardCache(CacheRefresher refresher, long sleep) {
		return new TestCache();
	}

	public Cache newHardCache(long sleep, String pattern) {
		return new TestCache();
	}

	public Cache newHardCache() {
		return new TestCache();
	}

	public MultiRefCache newMultiRefCache(long sleep) {
		throw new UnsupportedOperationException();
	}

	public MultiRefCache newMultiRefCache(String cacheName) {
		throw new UnsupportedOperationException();
	}

	public void registerCacher(Cacher cacher) {
		
	}

	public void resetCachers() throws MemoryPermissionException {
	}

	public void unregisterCacher(Cacher cacher) {
		
	}

	public void evictExpiredMembers() throws MemoryPermissionException {
		// TODO Auto-generated method stub
		
	}

}
