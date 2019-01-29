package org.tsugi.lti13;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;


import java.util.HashMap;
import java.util.Map;

// Test some Java features to make sure we are running a version of Java that works the
// way we expect
public class LTI13JavaTest {

	public Object[] twoObjects() {
		Object [] retval = new Object[2];
		
		Map<String,String> mp1 = new HashMap<> ();
		mp1.put("name", "mp1");
		Map<String,String> mp2 = new HashMap<> ();
		mp2.put("name", "mp2");
		retval[0] = mp1;
		retval[1] = mp2;
		return retval;
	}

	@Test
	public void testFakeTuple()
	{
		Object [] retval = twoObjects();
		assertNotNull(retval);
		Map<String, String> mp1 = (Map<String, String>) retval[0];
		assertEquals("mp1", mp1.get("name"));
		mp1.put("new", "mp1");
		assertEquals("mp1", mp1.get("new"));

		Map<String, String> mp2 = (Map<String, String>) retval[1];
		assertEquals("mp2", mp2.get("name"));
		mp2.put("new", "mp2");
		assertEquals("mp2", mp2.get("new"));
	}
	
	public Map[] twoMaps() {
		Map [] retval = new Map[2];
		
		Map<String,String> mp1 = new HashMap<> ();
		mp1.put("name", "mp1");
		retval[0] = mp1;

		Map<String,String> mp2 = new HashMap<> ();
		mp2.put("name", "mp2");
		retval[1] = mp2;
		return retval;
	}

	@Test
	public void testFakeMapTuple()
	{
		Map [] retval = twoMaps();
		assertNotNull(retval);
		
		Map mp1 = retval[0];
		assertEquals("mp1", mp1.get("name"));
		mp1.put("new", "mp1");
		assertEquals("mp1", mp1.get("new"));

		Map mp2 = retval[1];
		assertEquals("mp2", mp2.get("name"));
		mp2.put("new", "mp2");
		assertEquals("mp2", mp2.get("new"));
	}
}
