package org.sakaiproject.messagebundle.impl.test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.messagebundle.api.MessageBundleService;
import org.sakaiproject.messagebundle.impl.MessageBundleServiceImpl;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {CachingMessageBundleTestConfiguration.class})
public class CachingMessageBundleTest {

    private static final String CACHE_NAME = "org.sakaiproject.messagebundle.cache.bundles";

    @Resource
    private MemoryService memoryService;
    @Resource(name = "CachingMessageBundleServiceImpl")
    private MessageBundleService messageBundleService;
    @Resource(name = "MessageBundleServiceImpl")
    private MessageBundleService dbMessageBundleService;

    private Cache<String, Map<String, String>> cache;

    @Before
    public void setup() {
        cache = memoryService.getCache(CACHE_NAME);
        cache.clear();
        reset(dbMessageBundleService);
    }

    @Test
    public void testGetBundle() {
        Locale locale = Locale.getDefault();
        String key = MessageBundleServiceImpl.getIndexKeyName("BASENAME", "MODULENAME", locale.toString());
        Map<String, String> bundle1 = messageBundleService.getBundle("BASENAME", "MODULENAME", locale);
        Assert.assertNotNull(bundle1);
        Assert.assertTrue(bundle1.isEmpty());
        Assert.assertTrue(cache.containsKey(key));
        // we should have at least one call to the dbMessageBundleService
        verify(dbMessageBundleService).getBundle("BASENAME", "MODULENAME", locale);
    }

    @Test
    public void testGetBundleWithNulls() {
        String key = MessageBundleServiceImpl.getIndexKeyName(null, null, null);
        Map<String, String> bundle1 = messageBundleService.getBundle(null, null, null);
        Assert.assertNotNull(bundle1);
        Assert.assertTrue(bundle1.isEmpty());
        Assert.assertTrue(cache.containsKey(key));
        // we should have at least one call to the dbMessageBundleService
        verify(dbMessageBundleService).getBundle(null, null, null);
    }

    @Test
    public void testGetBundleNegativeLookups() {
        String key = MessageBundleServiceImpl.getIndexKeyName(null, null, null);
        Map<String, String> bundle1 = messageBundleService.getBundle(null, null, null);
        Assert.assertNotNull(bundle1);
        Assert.assertTrue(bundle1.isEmpty());
        Assert.assertTrue(cache.containsKey(key));

        // tell mockito to throw an exception if it calls the dbMessageBundleService.getBundle as this should be now be cached
        when(dbMessageBundleService.getBundle(null, null, null)).thenThrow(new IllegalArgumentException());
        Map<String, String> bundle2 = messageBundleService.getBundle(null, null, null);
        Assert.assertNotNull(bundle2);
        Assert.assertTrue(bundle2.isEmpty());
        Assert.assertSame(bundle1, bundle2);
    }
}
