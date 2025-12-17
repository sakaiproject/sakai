
package org.sakaiproject.entitybroker.rest;

import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entitybroker.config.EntityRestTestConfiguration;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.mocks.data.TestData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@ContextConfiguration(classes = {EntityRestTestConfiguration.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class EntityDescriptionManagerTest {

    @Autowired private EntityDescriptionManager entityDescriptionManager;
    @Autowired private EntityProviderManager entityProviderManager;
    @Autowired private ServerConfigurationService serverConfigurationService;

    private TestData td;

    @Before
    public void onSetUp() {
        td = new TestData(entityProviderManager);
        Mockito.when(serverConfigurationService.getServerUrl()).thenReturn("http://localhost:8080");
        Locale.setDefault(Locale.ENGLISH);
    }

    @After
    public void tearDown() {
        entityProviderManager.unRegistrarAllProvidersAndListeners();
        td = null;
    }

    @Test
    public void testReplacePrefix() {
        String outgoingTemplate = "/{prefix}/hello";
        String prefix = "myprefix";
        String result = entityDescriptionManager.replacePrefix(outgoingTemplate, prefix);
        assertNotNull(result);
        assertEquals("/myprefix/hello", result);
    }

    @Test
    public void testDescribeAll() {

        // test describe all entities
        String content = entityDescriptionManager.makeDescribeAll(null, null);
        assertNotNull(content);
        assertTrue(content.length() > 80);
        assertTrue(content.contains(TestData.PREFIX1));
        assertTrue(content.contains(TestData.PREFIX2));
        assertTrue(content.contains(TestData.PREFIX3));
        assertTrue(content.contains(TestData.PREFIX4));
        assertTrue(content.contains(TestData.PREFIX5));
        assertTrue(content.contains(TestData.PREFIX6));
        assertTrue(content.contains(TestData.PREFIX7));
        assertTrue(content.contains(TestData.PREFIX8));

        content = entityDescriptionManager.makeDescribeAll(Formats.XML, null);
        assertNotNull(content);
        assertTrue(content.length() > 80);
        assertTrue(content.contains(TestData.PREFIX1));
        assertTrue(content.contains(TestData.PREFIX2));
        assertTrue(content.contains(TestData.PREFIX3));
        assertTrue(content.contains(TestData.PREFIX4));
        assertTrue(content.contains(TestData.PREFIX5));
        assertTrue(content.contains(TestData.PREFIX6));
        assertTrue(content.contains(TestData.PREFIX7));
        assertTrue(content.contains(TestData.PREFIX8));
    }

    @Test
    public void testDescribeEntity() {

        // test describe single entity space
        String content = entityDescriptionManager.makeDescribeEntity(TestData.PREFIX1, "test", null, null);
        assertNotNull(content);
        assertTrue(content.length() > 80);
        assertTrue(content.contains(TestData.PREFIX1));

        // XML
        content = entityDescriptionManager.makeDescribeEntity(TestData.PREFIX1, "test", Formats.XML, null);
        assertNotNull(content);
        assertTrue(content.length() > 80);
        assertTrue(content.contains(TestData.PREFIX1));

        // prefix 4
        content = entityDescriptionManager.makeDescribeEntity(TestData.PREFIX4, "test", null, null);
        assertNotNull(content);
        assertTrue(content.length() > 80);
        assertTrue(content.contains(TestData.PREFIX4));

        content = entityDescriptionManager.makeDescribeEntity(TestData.PREFIX6, "test", null, null);
        assertNotNull(content);
        assertTrue(content.length() > 80);
        assertTrue(content.contains(TestData.PREFIX6));

        // XML
        content = entityDescriptionManager.makeDescribeEntity(TestData.PREFIX6, "test", Formats.XML, null);
        assertNotNull(content);
        assertTrue(content.length() > 80);
        assertTrue(content.contains(TestData.PREFIX6));

        // test an entity which is describable
        content = entityDescriptionManager.makeDescribeEntity(TestData.PREFIX7, "test", Formats.HTML, null);
        assertNotNull(content);
        assertTrue(content.length() > 80);
        assertTrue(content.contains(TestData.PREFIX7));
        assertTrue(content.contains("describe-prefix test description of an entity"));
        assertTrue(content.contains("This is a test description of Createable"));

        // XML
        content = entityDescriptionManager.makeDescribeEntity(TestData.PREFIX7, "test", Formats.XML, null);
        assertNotNull(content);
        assertTrue(content.length() > 80);
        assertTrue(content.contains(TestData.PREFIX7));
        assertTrue(content.contains("<description>"));
        assertTrue(content.contains("describe-prefix test description of an entity"));
        assertTrue(content.contains("This is a test description of Createable"));

        // test an entity which is DescribeProperties-able
        content = entityDescriptionManager.makeDescribeEntity(TestData.PREFIX8, "test", Formats.HTML, null);
        assertNotNull(content);
        assertTrue(content.length() > 80);
        assertTrue(content.contains(TestData.PREFIX8));
        assertTrue(content.contains("CUSTOM description"));
        assertTrue(content.contains("CUSTOM Deleteable"));

        // XML
        content = entityDescriptionManager.makeDescribeEntity(TestData.PREFIX8, "test", Formats.XML, null);
        assertNotNull(content);
        assertTrue(content.length() > 80);
        assertTrue(content.contains(TestData.PREFIX8));
        assertTrue(content.contains("<description>"));
        assertTrue(content.contains("CUSTOM description"));
        assertTrue(content.contains("CUSTOM Deleteable"));

        // test invalid describe
        assertThrows(IllegalArgumentException.class,
                () ->entityDescriptionManager.makeDescribeEntity(TestData.PREFIX9, "test", null, null));
    }

}
