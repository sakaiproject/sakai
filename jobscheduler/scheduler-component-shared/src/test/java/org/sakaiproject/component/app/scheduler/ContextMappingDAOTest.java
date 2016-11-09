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
 * Test the ContextMapping DAO
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ContextMappingConfiguration.class})
@Transactional
public class ContextMappingDAOTest {

    @Autowired
    private ContextMappingDAO dao;

    @Test
    public void testAddAndFind() {
        dao.add("uuid", "componentId", "contextId");
        Assert.assertEquals("uuid", dao.get("componentId", "contextId"));
    }

    @Test(expected = NonUniqueObjectException.class)
    public void testAddAdd() {
        dao.add("uuid", "componentId", "contextId1");
        dao.add("uuid", "componentId", "contextId2");
    }

    @Test
    public void testNotFound() {
        Assert.assertNull(dao.get("componentId", "doesNotExist"));
    }

    @Test
    public void testAddRemove() {
        dao.add("uuid", "componentId", "contextId");
        dao.remove("componentId", "contextId");
    }

    @Test
    public void testAddRemoveByUuid() {
        dao.add("uuid", "componentId", "contextId");
        dao.remove("uuid");
    }

    @Test
    public void testRemoveNotFound() {
        dao.remove("componentId", "contextId");
    }

    @Test
    public void testFind() {

    }


}
