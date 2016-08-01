package org.sakaiproject.component.app.scheduler;

import org.hibernate.NonUniqueObjectException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test the ContextMapping DAI
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfiguration.class})
@Transactional
public class ContextMappingDAOTest {

    @Autowired
    private ContextMappingDAO dao;

    @Test
    public void testAddAndFind() {
        dao.add("uuid", "contextId");
        Assert.assertEquals("contextId", dao.find("uuid"));
    }

    @Test(expected = NonUniqueObjectException.class)
    public void testAddAddFind() {
        dao.add("uuid", "contextId1");
        dao.add("uuid", "contextId2");
    }

    @Test
    public void testNotFound() {
        Assert.assertNull(dao.find("doesNotExist"));
    }


}
