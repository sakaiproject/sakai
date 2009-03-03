package org.sakaiproject.sitestats.test.mocks;

import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.CacheRefresher;
import org.sakaiproject.memory.api.Cacher;
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

}
