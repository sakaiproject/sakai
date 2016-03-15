package org.sakai.memory.impl.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.memory.util.CacheInitializer;

import net.sf.ehcache.config.CacheConfiguration;

public class TestCacheInitializer {

	private CacheConfiguration config;
	private CacheInitializer initializer;
	
	@Before
	public void setUp() {
		config = new CacheConfiguration();
		initializer = new CacheInitializer();
	}

	@Test
	public void testSingleConfig() {
		initializer.configure("timeToLiveSeconds=400").initialize(config);
		Assert.assertEquals(400, config.getTimeToLiveSeconds());
	}
	
	@Test
	public void testMultipleConfig() {
		initializer.configure("timeToLiveSeconds=300,timeToIdleSeconds=150")
			.initialize(config);
		Assert.assertEquals(300, config.getTimeToLiveSeconds());
		Assert.assertEquals(150, config.getTimeToIdleSeconds());
	}
	
	@Test
	public void testDuplicateConfig() {
		initializer.configure("timeToLiveSeconds=300,timeToIdleSeconds=150,timeToLiveSeconds=10")
			.initialize(config);
		Assert.assertEquals(10, config.getTimeToLiveSeconds());
		Assert.assertEquals(150, config.getTimeToIdleSeconds());
	}
	
	@Test
	public void testBadKey() {
		initializer.configure("doesNotExist=300,timeToIdleSeconds=150")
			.initialize(config);
		Assert.assertEquals(150, config.getTimeToIdleSeconds());
	}
	
	@Test
	public void testBadValue() {
		initializer.configure("timeToLiveSeconds=300a,timeToIdleSeconds=150")
			.initialize(config);
		Assert.assertEquals(150, config.getTimeToIdleSeconds());
	}
	
	@Test
	public void testStringValue() {
		initializer.configure("name=other")
			.initialize(config);
		Assert.assertEquals("other", config.getName());
	}
	
	@Test
	public void testIntValue() {
		initializer.configure("maxElementsOnDisk=150")
			.initialize(config);
		Assert.assertEquals(150, config.getMaxElementsOnDisk());
	}
	
	@Test
	public void testBooleanValue() {
		initializer.configure("eternal=true")
			.initialize(config);
		Assert.assertEquals(true, config.isEternal());
	}
}
