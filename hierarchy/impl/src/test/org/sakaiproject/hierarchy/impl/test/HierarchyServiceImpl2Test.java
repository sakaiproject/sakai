/******************************************************************************
 * HierarchyServiceImplTest.java - created by aaronz on Jul 1, 2007
 * 
 * Copyright (c) 2007 Centre for Academic Research in Educational Technologies
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 *****************************************************************************/
package org.sakaiproject.hierarchy.impl.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Map;
import java.util.Set;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.hierarchy.dao.HierarchyDao;
import org.sakaiproject.hierarchy.impl.HierarchyServiceImpl;
import org.sakaiproject.hierarchy.impl.test.data.TestDataPreload;
import org.sakaiproject.hierarchy.model.HierarchyNode;
import org.sakaiproject.memory.api.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

/**
 * Testing the hierarchy service
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
@ContextConfiguration(locations={
        "/hibernate-test.xml",
        "/spring-hibernate.xml"})
public class HierarchyServiceImpl2Test extends AbstractTransactionalJUnit4SpringContextTests {

    protected HierarchyServiceImpl hierarchyService;

    @Autowired
    @Qualifier("org.sakaiproject.hierarchy.dao.HierarchyDao")
    private HierarchyDao dao;
    @Autowired
    @Qualifier("org.sakaiproject.hierarchy.test.data.TestDataPreload")
    private TestDataPreload tdp;
    private Cache cache;

    // run this before each test starts
    @Before
    public void onSetUp()  {

        //create and setup the object to be tested
        hierarchyService = new HierarchyServiceImpl();
        hierarchyService.setDao(dao);
        //    hierarchyService.setSessionManager(sessionManager);
    }


    /**
     * Test method for {@link org.sakaiproject.hierarchy.impl.HierarchyServiceImpl#getNodesWithToken(java.lang.String)}.
     */
    @Test
    public void testGetNodesWithToken() {
        Set<String> nodeIds;

        // get all the nodes with a specific token
        nodeIds = hierarchyService.getNodesWithToken(TestDataPreload.HIERARCHYA, TestDataPreload.PERM_TOKEN_1);
        assertNotNull(nodeIds);
        assertEquals(3, nodeIds.size());
        assertTrue(nodeIds.contains(tdp.node2.id));
        assertTrue(nodeIds.contains(tdp.node3.id));
        assertTrue(nodeIds.contains(tdp.node5.id));

        nodeIds = hierarchyService.getNodesWithToken(TestDataPreload.HIERARCHYB, TestDataPreload.PERM_TOKEN_1);
        assertNotNull(nodeIds);
        assertEquals(1, nodeIds.size());
        assertTrue(nodeIds.contains(tdp.node10.id));

        nodeIds = hierarchyService.getNodesWithToken(TestDataPreload.HIERARCHYA, TestDataPreload.PERM_TOKEN_2);
        assertNotNull(nodeIds);
        assertEquals(1, nodeIds.size());
        assertTrue(nodeIds.contains(tdp.node4.id));

        // attempt to get nodes for invalid token
        nodeIds = hierarchyService.getNodesWithToken(TestDataPreload.HIERARCHYA, TestDataPreload.INVALID_PERM_TOKEN);
        assertNotNull(nodeIds);
        assertEquals(0, nodeIds.size());

        // cannot use invalid hierarchy
        try {
            hierarchyService.getNodesWithToken(TestDataPreload.INVALID_HIERARCHY, TestDataPreload.PERM_TOKEN_1);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        // cannot get null token
        try {
            hierarchyService.getNodesWithToken(TestDataPreload.HIERARCHYA, null);
            fail("Should have thrown exception");
        } catch (NullPointerException e) {
            assertNotNull(e.getMessage());
        }
    }

    /**
     * Test method for {@link org.sakaiproject.hierarchy.impl.HierarchyServiceImpl#getNodesWithTokens(java.lang.String[])}.
     */
    @Test
    public void testGetNodesWithTokens() {
        Set<String> nodeIds;
        Map<String, Set<String>> tokenNodes;

        // get nodes for tokens
        tokenNodes = hierarchyService.getNodesWithTokens(TestDataPreload.HIERARCHYA, 
                new String[] {TestDataPreload.PERM_TOKEN_1, TestDataPreload.PERM_TOKEN_2});
        assertNotNull(tokenNodes);
        assertEquals(2, tokenNodes.size());
        nodeIds = tokenNodes.get(TestDataPreload.PERM_TOKEN_1);
        assertEquals(3, nodeIds.size());
        assertTrue(nodeIds.contains(tdp.node2.id));
        assertTrue(nodeIds.contains(tdp.node3.id));
        assertTrue(nodeIds.contains(tdp.node5.id));
        nodeIds = tokenNodes.get(TestDataPreload.PERM_TOKEN_2);
        assertEquals(1, nodeIds.size());
        assertTrue(nodeIds.contains(tdp.node4.id));

        // mix valid and invalid tokens
        tokenNodes = hierarchyService.getNodesWithTokens(TestDataPreload.HIERARCHYB, 
                new String[] {TestDataPreload.PERM_TOKEN_1, TestDataPreload.PERM_TOKEN_2});
        assertNotNull(tokenNodes);
        assertEquals(2, tokenNodes.size());
        nodeIds = tokenNodes.get(TestDataPreload.PERM_TOKEN_1);
        assertEquals(1, nodeIds.size());
        assertTrue(nodeIds.contains(tdp.node10.id));
        nodeIds = tokenNodes.get(TestDataPreload.PERM_TOKEN_2);
        assertEquals(0, nodeIds.size());

        // attempt to get nodes for invalid token
        tokenNodes = hierarchyService.getNodesWithTokens(TestDataPreload.HIERARCHYA, 
                new String[] {TestDataPreload.INVALID_PERM_TOKEN});
        assertNotNull(tokenNodes);
        assertEquals(1, tokenNodes.size());
        nodeIds = tokenNodes.get(TestDataPreload.INVALID_PERM_TOKEN);
        assertEquals(0, nodeIds.size());

        // cannot use invalid hierarchy
        try {
            hierarchyService.getNodesWithTokens(TestDataPreload.INVALID_HIERARCHY, 
                    new String[] {TestDataPreload.PERM_TOKEN_1});
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        // cannot get null token
        try {
            hierarchyService.getNodesWithTokens(TestDataPreload.HIERARCHYA, null);
            fail("Should have thrown exception");
        } catch (NullPointerException e) {
            assertNotNull(e.getMessage());
        }      
    }

    /**
     * Test method for {@link org.sakaiproject.hierarchy.impl.HierarchyServiceImpl#checkUserNodePerm(java.lang.String, java.lang.String, java.lang.String)}.
     */
    @Test
    public void testCheckUserNodePerm() {
        assertFalse( hierarchyService.checkUserNodePerm(TestDataPreload.USER_ID, tdp.node1.id, TestDataPreload.PERM_ONE) );
        assertFalse( hierarchyService.checkUserNodePerm(TestDataPreload.USER_ID, tdp.node2.id, TestDataPreload.PERM_ONE) );
        assertFalse( hierarchyService.checkUserNodePerm(TestDataPreload.USER_ID, tdp.node3.id, TestDataPreload.PERM_ONE) );
        assertFalse( hierarchyService.checkUserNodePerm(TestDataPreload.USER_ID, tdp.node4.id, TestDataPreload.PERM_ONE) );
        assertFalse( hierarchyService.checkUserNodePerm(TestDataPreload.USER_ID, tdp.node5.id, TestDataPreload.PERM_ONE) );
        assertFalse( hierarchyService.checkUserNodePerm(TestDataPreload.USER_ID, tdp.node6.id, TestDataPreload.PERM_ONE) );
        assertFalse( hierarchyService.checkUserNodePerm(TestDataPreload.USER_ID, tdp.node7.id, TestDataPreload.PERM_ONE) );
        assertFalse( hierarchyService.checkUserNodePerm(TestDataPreload.USER_ID, tdp.node8.id, TestDataPreload.PERM_ONE) );
        assertFalse( hierarchyService.checkUserNodePerm(TestDataPreload.ACCESS_USER_ID, tdp.node1.id, TestDataPreload.PERM_ONE) );
        assertFalse( hierarchyService.checkUserNodePerm(TestDataPreload.ACCESS_USER_ID, tdp.node2.id, TestDataPreload.PERM_ONE) );
        assertFalse( hierarchyService.checkUserNodePerm(TestDataPreload.ACCESS_USER_ID, tdp.node3.id, TestDataPreload.PERM_ONE) );
        assertFalse( hierarchyService.checkUserNodePerm(TestDataPreload.ACCESS_USER_ID, tdp.node4.id, TestDataPreload.PERM_ONE) );
        assertTrue( hierarchyService.checkUserNodePerm(TestDataPreload.ACCESS_USER_ID, tdp.node5.id, TestDataPreload.PERM_ONE) );
        assertFalse( hierarchyService.checkUserNodePerm(TestDataPreload.ACCESS_USER_ID, tdp.node6.id, TestDataPreload.PERM_ONE) );
        assertTrue( hierarchyService.checkUserNodePerm(TestDataPreload.ACCESS_USER_ID, tdp.node7.id, TestDataPreload.PERM_ONE) );
        assertFalse( hierarchyService.checkUserNodePerm(TestDataPreload.ACCESS_USER_ID, tdp.node8.id, TestDataPreload.PERM_ONE) );
        assertFalse( hierarchyService.checkUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node1.id, TestDataPreload.PERM_ONE) );
        assertTrue( hierarchyService.checkUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node2.id, TestDataPreload.PERM_ONE) );
        assertFalse( hierarchyService.checkUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node3.id, TestDataPreload.PERM_ONE) );
        assertTrue( hierarchyService.checkUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node4.id, TestDataPreload.PERM_ONE) );
        assertFalse( hierarchyService.checkUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node5.id, TestDataPreload.PERM_ONE) );
        assertTrue( hierarchyService.checkUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node6.id, TestDataPreload.PERM_ONE) );
        assertTrue( hierarchyService.checkUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node7.id, TestDataPreload.PERM_ONE) );
        assertTrue( hierarchyService.checkUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node8.id, TestDataPreload.PERM_ONE) );
        assertFalse( hierarchyService.checkUserNodePerm(TestDataPreload.USER_ID, tdp.node1.id, TestDataPreload.PERM_TWO) );
        assertFalse( hierarchyService.checkUserNodePerm(TestDataPreload.USER_ID, tdp.node2.id, TestDataPreload.PERM_TWO) );
        assertTrue( hierarchyService.checkUserNodePerm(TestDataPreload.USER_ID, tdp.node3.id, TestDataPreload.PERM_TWO) );
        assertFalse( hierarchyService.checkUserNodePerm(TestDataPreload.USER_ID, tdp.node4.id, TestDataPreload.PERM_TWO) );
        assertFalse( hierarchyService.checkUserNodePerm(TestDataPreload.USER_ID, tdp.node5.id, TestDataPreload.PERM_TWO) );
        assertTrue( hierarchyService.checkUserNodePerm(TestDataPreload.USER_ID, tdp.node6.id, TestDataPreload.PERM_TWO) );
        assertFalse( hierarchyService.checkUserNodePerm(TestDataPreload.USER_ID, tdp.node7.id, TestDataPreload.PERM_TWO) );
        assertFalse( hierarchyService.checkUserNodePerm(TestDataPreload.USER_ID, tdp.node8.id, TestDataPreload.PERM_TWO) );
        assertFalse( hierarchyService.checkUserNodePerm(TestDataPreload.ACCESS_USER_ID, tdp.node1.id, TestDataPreload.PERM_TWO) );
        assertFalse( hierarchyService.checkUserNodePerm(TestDataPreload.ACCESS_USER_ID, tdp.node2.id, TestDataPreload.PERM_TWO) );
        assertFalse( hierarchyService.checkUserNodePerm(TestDataPreload.ACCESS_USER_ID, tdp.node3.id, TestDataPreload.PERM_TWO) );
        assertFalse( hierarchyService.checkUserNodePerm(TestDataPreload.ACCESS_USER_ID, tdp.node4.id, TestDataPreload.PERM_TWO) );
        assertFalse( hierarchyService.checkUserNodePerm(TestDataPreload.ACCESS_USER_ID, tdp.node5.id, TestDataPreload.PERM_TWO) );
        assertFalse( hierarchyService.checkUserNodePerm(TestDataPreload.ACCESS_USER_ID, tdp.node6.id, TestDataPreload.PERM_TWO) );
        assertFalse( hierarchyService.checkUserNodePerm(TestDataPreload.ACCESS_USER_ID, tdp.node7.id, TestDataPreload.PERM_TWO) );
        assertTrue( hierarchyService.checkUserNodePerm(TestDataPreload.ACCESS_USER_ID, tdp.node8.id, TestDataPreload.PERM_TWO) );
        assertFalse( hierarchyService.checkUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node1.id, TestDataPreload.PERM_TWO) );
        assertTrue( hierarchyService.checkUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node2.id, TestDataPreload.PERM_TWO) );
        assertTrue( hierarchyService.checkUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node3.id, TestDataPreload.PERM_TWO) );
        assertFalse( hierarchyService.checkUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node4.id, TestDataPreload.PERM_TWO) );
        assertTrue( hierarchyService.checkUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node5.id, TestDataPreload.PERM_TWO) );
        assertFalse( hierarchyService.checkUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node6.id, TestDataPreload.PERM_TWO) );
        assertFalse( hierarchyService.checkUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node7.id, TestDataPreload.PERM_TWO) );
        assertFalse( hierarchyService.checkUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node8.id, TestDataPreload.PERM_TWO) );

        try {
            hierarchyService.checkUserNodePerm(null, "BBBBBB", "CCCCCCCCCCCCCCC");
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
        try {
            hierarchyService.checkUserNodePerm("AAAAAAAAAA", null, "CCCCCCCCCCCCCCC");
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
        try {
            hierarchyService.checkUserNodePerm("AAAAAAAAAA", "BBBBBB", null);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
    }

    /**
     * Test method for {@link org.sakaiproject.hierarchy.impl.HierarchyServiceImpl#getNodesForUserPerm(java.lang.String, java.lang.String)}.
     */
     @Test
    public void testGetNodesForUserPerm() {
        Set<HierarchyNode> nodes = null;

        nodes = hierarchyService.getNodesForUserPerm(TestDataPreload.USER_ID, TestDataPreload.PERM_ONE);
        assertNotNull(nodes);
        assertEquals(0, nodes.size());

        nodes = hierarchyService.getNodesForUserPerm(TestDataPreload.USER_ID, TestDataPreload.PERM_TWO);
        assertNotNull(nodes);
        assertEquals(2, nodes.size());
        assertTrue( nodes.contains(tdp.node3) );
        assertTrue( nodes.contains(tdp.node6) );

        nodes = hierarchyService.getNodesForUserPerm(TestDataPreload.ACCESS_USER_ID, TestDataPreload.PERM_ONE);
        assertNotNull(nodes);
        assertEquals(2, nodes.size());
        assertTrue( nodes.contains(tdp.node5) );
        assertTrue( nodes.contains(tdp.node7) );

        nodes = hierarchyService.getNodesForUserPerm(TestDataPreload.ACCESS_USER_ID, TestDataPreload.PERM_TWO);
        assertNotNull(nodes);
        assertEquals(1, nodes.size());
        assertTrue( nodes.contains(tdp.node8) );

        nodes = hierarchyService.getNodesForUserPerm(TestDataPreload.MAINT_USER_ID, TestDataPreload.PERM_ONE);
        assertNotNull(nodes);
        assertEquals(5, nodes.size());
        assertTrue( nodes.contains(tdp.node2) );
        assertTrue( nodes.contains(tdp.node4) );
        assertTrue( nodes.contains(tdp.node6) );
        assertTrue( nodes.contains(tdp.node7) );
        assertTrue( nodes.contains(tdp.node8) );

        nodes = hierarchyService.getNodesForUserPerm(TestDataPreload.MAINT_USER_ID, TestDataPreload.PERM_TWO);
        assertNotNull(nodes);
        assertEquals(3, nodes.size());
        assertTrue( nodes.contains(tdp.node2) );
        assertTrue( nodes.contains(tdp.node3) );
        assertTrue( nodes.contains(tdp.node5) );

        // invalids

        nodes = hierarchyService.getNodesForUserPerm(TestDataPreload.USER_ID, "XXXXXXXXX");
        assertNotNull(nodes);
        assertEquals(0, nodes.size());

        nodes = hierarchyService.getNodesForUserPerm("XXXXXXX", TestDataPreload.PERM_ONE);
        assertNotNull(nodes);
        assertEquals(0, nodes.size());

        try {
            hierarchyService.getNodesForUserPerm(null, "XXXXXXXXX");
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        try {
            hierarchyService.getNodesForUserPerm("XXXXXXXX", null);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
    }

    /**
     * Test method for {@link org.sakaiproject.hierarchy.impl.HierarchyServiceImpl#getUserIdsForNodesPerm(java.lang.String[], java.lang.String)}.
     */
     @Test
    public void testGetUserIdsForNodesPerm() {
        Set<String> userIds = null;

        userIds = hierarchyService.getUserIdsForNodesPerm(new String[] {tdp.node1.id}, TestDataPreload.PERM_ONE);
        assertNotNull(userIds);
        assertEquals(0, userIds.size());

        userIds = hierarchyService.getUserIdsForNodesPerm(new String[] {tdp.node2.id}, TestDataPreload.PERM_ONE);
        assertNotNull(userIds);
        assertEquals(1, userIds.size());
        assertTrue(userIds.contains(TestDataPreload.MAINT_USER_ID));

        userIds = hierarchyService.getUserIdsForNodesPerm(new String[] {tdp.node3.id}, TestDataPreload.PERM_ONE);
        assertNotNull(userIds);
        assertEquals(0, userIds.size());

        userIds = hierarchyService.getUserIdsForNodesPerm(new String[] {tdp.node4.id}, TestDataPreload.PERM_ONE);
        assertNotNull(userIds);
        assertEquals(1, userIds.size());
        assertTrue(userIds.contains(TestDataPreload.MAINT_USER_ID));

        userIds = hierarchyService.getUserIdsForNodesPerm(new String[] {tdp.node5.id}, TestDataPreload.PERM_ONE);
        assertNotNull(userIds);
        assertEquals(1, userIds.size());
        assertTrue(userIds.contains(TestDataPreload.ACCESS_USER_ID));

        userIds = hierarchyService.getUserIdsForNodesPerm(new String[] {tdp.node6.id}, TestDataPreload.PERM_ONE);
        assertNotNull(userIds);
        assertEquals(1, userIds.size());
        assertTrue(userIds.contains(TestDataPreload.MAINT_USER_ID));

        userIds = hierarchyService.getUserIdsForNodesPerm(new String[] {tdp.node7.id}, TestDataPreload.PERM_ONE);
        assertNotNull(userIds);
        assertEquals(2, userIds.size());
        assertTrue(userIds.contains(TestDataPreload.MAINT_USER_ID));
        assertTrue(userIds.contains(TestDataPreload.ACCESS_USER_ID));

        userIds = hierarchyService.getUserIdsForNodesPerm(new String[] {tdp.node8.id}, TestDataPreload.PERM_ONE);
        assertNotNull(userIds);
        assertEquals(1, userIds.size());
        assertTrue(userIds.contains(TestDataPreload.MAINT_USER_ID));

        userIds = hierarchyService.getUserIdsForNodesPerm(new String[] {tdp.node1.id}, TestDataPreload.PERM_TWO);
        assertNotNull(userIds);
        assertEquals(0, userIds.size());

        userIds = hierarchyService.getUserIdsForNodesPerm(new String[] {tdp.node2.id}, TestDataPreload.PERM_TWO);
        assertNotNull(userIds);
        assertEquals(1, userIds.size());
        assertTrue(userIds.contains(TestDataPreload.MAINT_USER_ID));

        userIds = hierarchyService.getUserIdsForNodesPerm(new String[] {tdp.node3.id}, TestDataPreload.PERM_TWO);
        assertNotNull(userIds);
        assertEquals(2, userIds.size());
        assertTrue(userIds.contains(TestDataPreload.MAINT_USER_ID));
        assertTrue(userIds.contains(TestDataPreload.USER_ID));

        userIds = hierarchyService.getUserIdsForNodesPerm(new String[] {tdp.node4.id}, TestDataPreload.PERM_TWO);
        assertNotNull(userIds);
        assertEquals(0, userIds.size());

        userIds = hierarchyService.getUserIdsForNodesPerm(new String[] {tdp.node5.id}, TestDataPreload.PERM_TWO);
        assertNotNull(userIds);
        assertEquals(1, userIds.size());
        assertTrue(userIds.contains(TestDataPreload.MAINT_USER_ID));

        userIds = hierarchyService.getUserIdsForNodesPerm(new String[] {tdp.node6.id}, TestDataPreload.PERM_TWO);
        assertNotNull(userIds);
        assertEquals(1, userIds.size());
        assertTrue(userIds.contains(TestDataPreload.USER_ID));

        userIds = hierarchyService.getUserIdsForNodesPerm(new String[] {tdp.node7.id}, TestDataPreload.PERM_TWO);
        assertNotNull(userIds);
        assertEquals(0, userIds.size());

        userIds = hierarchyService.getUserIdsForNodesPerm(new String[] {tdp.node8.id}, TestDataPreload.PERM_TWO);
        assertNotNull(userIds);
        assertEquals(1, userIds.size());
        assertTrue(userIds.contains(TestDataPreload.ACCESS_USER_ID));

        // multiple

        userIds = hierarchyService.getUserIdsForNodesPerm(new String[] {tdp.node2.id, tdp.node3.id, tdp.node4.id, tdp.node5.id}, TestDataPreload.PERM_ONE);
        assertNotNull(userIds);
        assertEquals(2, userIds.size());
        assertTrue(userIds.contains(TestDataPreload.ACCESS_USER_ID));
        assertTrue(userIds.contains(TestDataPreload.MAINT_USER_ID));

        userIds = hierarchyService.getUserIdsForNodesPerm(new String[] {tdp.node2.id, tdp.node3.id, tdp.node4.id, tdp.node5.id}, TestDataPreload.PERM_TWO);
        assertNotNull(userIds);
        assertEquals(2, userIds.size());
        assertTrue(userIds.contains(TestDataPreload.USER_ID));
        assertTrue(userIds.contains(TestDataPreload.MAINT_USER_ID));

        // invalids
        userIds = hierarchyService.getUserIdsForNodesPerm(new String[] {}, TestDataPreload.PERM_ONE);
        assertNotNull(userIds);
        assertEquals(0, userIds.size());

        userIds = hierarchyService.getUserIdsForNodesPerm(new String[] {}, TestDataPreload.PERM_TWO);
        assertNotNull(userIds);
        assertEquals(0, userIds.size());

        try {
            hierarchyService.getUserIdsForNodesPerm(null, "XXXXXXXXX");
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        try {
            hierarchyService.getUserIdsForNodesPerm(new String[] {"XXXXXXXX"}, null);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
    }

    /**
     * Test method for {@link org.sakaiproject.hierarchy.impl.HierarchyServiceImpl#getPermsForUserNodes(java.lang.String, java.lang.String[])}.
     */
     @Test
    public void testGetPermsForUserNodes() {
        Set<String> perms = null;

        perms = hierarchyService.getPermsForUserNodes(TestDataPreload.MAINT_USER_ID, new String[] {tdp.node1.id});
        assertNotNull(perms);
        assertEquals(0, perms.size());

        perms = hierarchyService.getPermsForUserNodes(TestDataPreload.MAINT_USER_ID, new String[] {tdp.node2.id});
        assertNotNull(perms);
        assertEquals(2, perms.size());
        assertTrue(perms.contains(TestDataPreload.PERM_ONE));
        assertTrue(perms.contains(TestDataPreload.PERM_TWO));

        perms = hierarchyService.getPermsForUserNodes(TestDataPreload.MAINT_USER_ID, new String[] {tdp.node3.id});
        assertNotNull(perms);
        assertEquals(1, perms.size());
        assertTrue(perms.contains(TestDataPreload.PERM_TWO));

        perms = hierarchyService.getPermsForUserNodes(TestDataPreload.MAINT_USER_ID, new String[] {tdp.node4.id});
        assertNotNull(perms);
        assertEquals(1, perms.size());
        assertTrue(perms.contains(TestDataPreload.PERM_ONE));

        perms = hierarchyService.getPermsForUserNodes(TestDataPreload.MAINT_USER_ID, new String[] {tdp.node5.id});
        assertNotNull(perms);
        assertEquals(1, perms.size());
        assertTrue(perms.contains(TestDataPreload.PERM_TWO));

        perms = hierarchyService.getPermsForUserNodes(TestDataPreload.MAINT_USER_ID, new String[] {tdp.node6.id});
        assertNotNull(perms);
        assertEquals(1, perms.size());
        assertTrue(perms.contains(TestDataPreload.PERM_ONE));

        perms = hierarchyService.getPermsForUserNodes(TestDataPreload.MAINT_USER_ID, new String[] {tdp.node7.id});
        assertNotNull(perms);
        assertEquals(1, perms.size());
        assertTrue(perms.contains(TestDataPreload.PERM_ONE));

        perms = hierarchyService.getPermsForUserNodes(TestDataPreload.MAINT_USER_ID, new String[] {tdp.node8.id});
        assertNotNull(perms);
        assertEquals(1, perms.size());
        assertTrue(perms.contains(TestDataPreload.PERM_ONE));

        // multiple
        perms = hierarchyService.getPermsForUserNodes(TestDataPreload.MAINT_USER_ID, new String[] {tdp.node3.id, tdp.node4.id, tdp.node5.id, tdp.node6.id});
        assertNotNull(perms);
        assertEquals(2, perms.size());
        assertTrue(perms.contains(TestDataPreload.PERM_ONE));
        assertTrue(perms.contains(TestDataPreload.PERM_TWO));

        // invalids
        perms = hierarchyService.getPermsForUserNodes(TestDataPreload.ACCESS_USER_ID, new String[] {});
        assertNotNull(perms);
        assertEquals(0, perms.size());

        perms = hierarchyService.getPermsForUserNodes(TestDataPreload.MAINT_USER_ID, new String[] {});
        assertNotNull(perms);
        assertEquals(0, perms.size());

        perms = hierarchyService.getPermsForUserNodes(TestDataPreload.USER_ID, new String[] {});
        assertNotNull(perms);
        assertEquals(0, perms.size());

        try {
            hierarchyService.getPermsForUserNodes(null, new String[] {"XXXXXXXX"});
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        try {
            hierarchyService.getPermsForUserNodes("XXXXXXXXXXX", null);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
    }

     @Test
     public void testGetUsersAndPermsForNodes() {
        Map<String, Map<String, Set<String>>> map = null;

        map = hierarchyService.getUsersAndPermsForNodes(tdp.node3.id);
        assertNotNull(map);
        assertEquals(1, map.size());
        Map<String, Set<String>> userPerms = map.get(tdp.node3.id);
        assertEquals(2, userPerms.size());
        assertEquals(userPerms.get(TestDataPreload.USER_ID).size(), 1);
        assertEquals(userPerms.get(TestDataPreload.MAINT_USER_ID).size(), 1);

        try {
            hierarchyService.getUsersAndPermsForNodes((String[])null);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
        try {
            hierarchyService.getUsersAndPermsForNodes();
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
    }

     @Test
     public void testGetNodesAndPermsForUser() {
        Map<String, Map<String, Set<String>>> map = null;

        map = hierarchyService.getNodesAndPermsForUser(TestDataPreload.ACCESS_USER_ID);
        assertNotNull(map);
        assertEquals(1, map.size());
        assertEquals(3, map.get(TestDataPreload.ACCESS_USER_ID).size());

        try {
            hierarchyService.getNodesAndPermsForUser((String[])null);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
        try {
            hierarchyService.getNodesAndPermsForUser();
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
    }

    /**
     * Test method for {@link org.sakaiproject.hierarchy.impl.HierarchyServiceImpl#assignUserNodePerm(java.lang.String, java.lang.String, java.lang.String, boolean)}.
     */
     @Test
    public void testAssignUserNodePerm() {
        Set<HierarchyNode> nodes = hierarchyService.getNodesForUserPerm(TestDataPreload.MAINT_USER_ID, TestDataPreload.PERM_ONE);
        assertEquals(5, nodes.size());

        // add existing one - should be no change
        hierarchyService.assignUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node2.id, TestDataPreload.PERM_ONE, false);
        nodes = hierarchyService.getNodesForUserPerm(TestDataPreload.MAINT_USER_ID, TestDataPreload.PERM_ONE);
        assertEquals(5, nodes.size());

        initializeMockCache();

        // add existing one - should be no change
        hierarchyService.assignUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node4.id, TestDataPreload.PERM_ONE, true);
        nodes = hierarchyService.getNodesForUserPerm(TestDataPreload.MAINT_USER_ID, TestDataPreload.PERM_ONE);
        assertEquals(5, nodes.size());

        // now add some that do not exist already
        hierarchyService.assignUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node3.id, TestDataPreload.PERM_ONE, false);
        nodes = hierarchyService.getNodesForUserPerm(TestDataPreload.MAINT_USER_ID, TestDataPreload.PERM_ONE);
        assertEquals(6, nodes.size());
        assertTrue( hierarchyService.checkUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node3.id, TestDataPreload.PERM_ONE) );

        hierarchyService.assignUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node3.id, TestDataPreload.PERM_ONE, true);
        nodes = hierarchyService.getNodesForUserPerm(TestDataPreload.MAINT_USER_ID, TestDataPreload.PERM_ONE);
        assertEquals(7, nodes.size());
        assertTrue( hierarchyService.checkUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node5.id, TestDataPreload.PERM_ONE) );

        // now test adding a completely different permission
        nodes = hierarchyService.getNodesForUserPerm(TestDataPreload.USER_ID, TestDataPreload.PERM_THREE);
        assertEquals(0, nodes.size());

        hierarchyService.assignUserNodePerm(TestDataPreload.USER_ID, tdp.node1.id, TestDataPreload.PERM_THREE, false);
        nodes = hierarchyService.getNodesForUserPerm(TestDataPreload.USER_ID, TestDataPreload.PERM_THREE);
        assertEquals(1, nodes.size());
        assertTrue( hierarchyService.checkUserNodePerm(TestDataPreload.USER_ID, tdp.node1.id, TestDataPreload.PERM_THREE) );

        hierarchyService.assignUserNodePerm(TestDataPreload.USER_ID, tdp.node1.id, TestDataPreload.PERM_THREE, true);
        nodes = hierarchyService.getNodesForUserPerm(TestDataPreload.USER_ID, TestDataPreload.PERM_THREE);
        assertEquals(8, nodes.size());


        try {
            hierarchyService.assignUserNodePerm(null, tdp.node3.id, TestDataPreload.PERM_ONE, false);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
        try {
            hierarchyService.assignUserNodePerm(TestDataPreload.MAINT_USER_ID, null, TestDataPreload.PERM_ONE, false);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
        try {
            hierarchyService.assignUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node3.id, null, false);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
    }

    /**
     * Test method for {@link org.sakaiproject.hierarchy.impl.HierarchyServiceImpl#removeUserNodePerm(java.lang.String, java.lang.String, java.lang.String, boolean)}.
     */
     @Test
    public void testRemoveUserNodePerm() {
        Set<HierarchyNode> nodes = hierarchyService.getNodesForUserPerm(TestDataPreload.MAINT_USER_ID, TestDataPreload.PERM_ONE);
        assertEquals(5, nodes.size());

        hierarchyService.removeUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node2.id, TestDataPreload.PERM_ONE, false);
        nodes = hierarchyService.getNodesForUserPerm(TestDataPreload.MAINT_USER_ID, TestDataPreload.PERM_ONE);
        assertEquals(4, nodes.size());
        assertFalse( hierarchyService.checkUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node2.id, TestDataPreload.PERM_ONE) );

        initializeMockCache();

        hierarchyService.removeUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node4.id, TestDataPreload.PERM_ONE, true);
        nodes = hierarchyService.getNodesForUserPerm(TestDataPreload.MAINT_USER_ID, TestDataPreload.PERM_ONE);
        assertEquals(0, nodes.size());
        assertFalse( hierarchyService.checkUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node4.id, TestDataPreload.PERM_ONE) );

        // invalids don't cause failure
        hierarchyService.removeUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node2.id, "XXXXX", false);
        hierarchyService.removeUserNodePerm(TestDataPreload.MAINT_USER_ID, "XXXX", "XXXXX", false);
        hierarchyService.removeUserNodePerm("XXX", "XXXX", "XXXXX", false);

        try {
            hierarchyService.removeUserNodePerm(null, tdp.node3.id, TestDataPreload.PERM_ONE, false);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
        try {
            hierarchyService.removeUserNodePerm(TestDataPreload.MAINT_USER_ID, null, TestDataPreload.PERM_ONE, false);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
        try {
            hierarchyService.removeUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node3.id, null, false);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
    }
     
     /**
      * Test method for {@link org.sakaiproject.hierarchy.impl.HierarchyServiceImpl#setNodeDisabled(java.lang.String, java.lang.Boolean)}.
      */
     @Test
     public void testSetNodeDisabled() {
     	
     	HierarchyNode node = null;
     	
     	// basic node creation, default is enabled (i.e. isDisabled is false)
         node = hierarchyService.saveNodeMetaData(tdp.node2.id, "Node TWO", "this is a description!", "TOKEN2");
         assertNotNull(node);
         assertEquals(node.id, tdp.node2.id);
         assertEquals(node.isDisabled, Boolean.FALSE);
         
         // disabling a node
         node = hierarchyService.setNodeDisabled(tdp.node2.id, Boolean.TRUE);
         assertNotNull(node);
         assertEquals(node.id, tdp.node2.id);
         assertEquals(node.isDisabled, Boolean.TRUE);
         
         // disabling an already-disabled node
         node = hierarchyService.setNodeDisabled(tdp.node2.id, Boolean.TRUE);
         assertNotNull(node);
         assertEquals(node.id, tdp.node2.id);
         assertEquals(node.isDisabled, Boolean.TRUE);
         
     }
     
     private void initializeMockCache(){
         Cache mock = EasyMock.createMock(Cache.class);
         hierarchyService.setCache(mock);
         EasyMock.expect(mock.containsKey(EasyMock.anyObject())).andReturn(false).anyTimes();
         EasyMock.expect(mock.remove(EasyMock.anyObject())).andReturn(false).anyTimes();
         mock.put(EasyMock.anyObject(),EasyMock.anyObject());
         EasyMock.expectLastCall().anyTimes();
         EasyMock.replay(mock);
     }
}
