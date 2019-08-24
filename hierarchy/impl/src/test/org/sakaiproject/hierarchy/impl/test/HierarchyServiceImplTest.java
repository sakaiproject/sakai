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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Map;
import java.util.Set;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.genericdao.api.search.Search;
import org.sakaiproject.hierarchy.dao.HierarchyDao;
import org.sakaiproject.hierarchy.dao.model.HierarchyNodeMetaData;
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
public class HierarchyServiceImplTest extends AbstractTransactionalJUnit4SpringContextTests {

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
     * ADD unit tests below here, use testMethod as the name of the unit test,
     * Note that if a method is overloaded you should include the arguments in the
     * test name like so: testMethodClassInt (for method(Class, int);
     */
    @Test
    public void testValidTestData() {
        // ensure the test data is setup the way we think
        assertEquals(new Long(1), tdp.pNode1.getId());
        assertEquals(new Long(6), tdp.pNode6.getId());
        assertEquals(new Long(9), tdp.pNode9.getId());
    }

    /**
     * Test method for {@link org.sakaiproject.hierarchy.impl.HierarchyServiceImpl#createHierarchy(java.lang.String)}.
     */
    @Test
    public void testCreateHierarchy() {
        // test creating a valid hierarchy
        HierarchyNode node = hierarchyService.createHierarchy("hierarchyC");
        assertNotNull(node);
        assertEquals("hierarchyC", node.hierarchyId);
        assertNotNull(node.parentNodeIds);
        assertNotNull(node.childNodeIds);
        assertTrue(node.parentNodeIds.isEmpty());
        assertTrue(node.childNodeIds.isEmpty());

        // test creating a hierarchy that already exists
        try {
            hierarchyService.createHierarchy(TestDataPreload.HIERARCHYA);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }

        // test creating a hierarchy with too long an id
        try {
            hierarchyService.createHierarchy("1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }

    }

    /**
     * Test method for {@link org.sakaiproject.hierarchy.impl.HierarchyServiceImpl#setHierarchyRootNode(java.lang.String, java.lang.String)}.
     */
    @Test
    public void testSetHierarchyRootNode() {
        HierarchyNode node = null;

        // test reassigning existing rootnode is no problem
        node = hierarchyService.setHierarchyRootNode(TestDataPreload.HIERARCHYA, tdp.node1.id);
        assertNotNull(node);
        assertEquals(TestDataPreload.HIERARCHYA, node.hierarchyId);
        assertEquals(tdp.node1.id, node.id);

        // test reassigning a new node to be the parent node
        assertEquals(Boolean.FALSE, tdp.meta11.getIsRootNode());
        assertEquals(Boolean.TRUE, tdp.meta9.getIsRootNode());
        node = hierarchyService.setHierarchyRootNode(TestDataPreload.HIERARCHYB, tdp.node11.id);
        assertNotNull(node);
        assertEquals(TestDataPreload.HIERARCHYB, node.hierarchyId);
        assertEquals(tdp.node11.id, node.id);

        // test assigning a node which has parents causes failure
        try {
            hierarchyService.setHierarchyRootNode(TestDataPreload.HIERARCHYA, tdp.node3.id);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }

        // test assigning a root node from another hierarchy to this root causes failure
        try {
            hierarchyService.setHierarchyRootNode(TestDataPreload.HIERARCHYB, tdp.node1.id);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }
    }

    /**
     * Test method for {@link org.sakaiproject.hierarchy.impl.HierarchyServiceImpl#destroyHierarchy(java.lang.String)}.
     */
    @Test
    public void testDestroyHierarchy() {
        hierarchyService.destroyHierarchy(TestDataPreload.HIERARCHYB);
        long count = dao.countBySearch(HierarchyNodeMetaData.class, 
                new Search("hierarchyId", TestDataPreload.HIERARCHYB) );
        assertEquals(0, count);

        // test removing a non-existent hierarchy fails
        try {
            hierarchyService.destroyHierarchy(TestDataPreload.HIERARCHYB);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }
    }

    /**
     * Test method for {@link org.sakaiproject.hierarchy.impl.HierarchyServiceImpl#getRootLevelNode(java.lang.String)}.
     */
    @Test
    public void testGetRootLevelNode() {
        HierarchyNode node = null;

        node = hierarchyService.getRootNode(TestDataPreload.HIERARCHYB);
        assertNotNull(node);
        assertEquals(tdp.node9, node);
        assertEquals(TestDataPreload.HIERARCHYB, node.hierarchyId);

        node = hierarchyService.getRootNode(TestDataPreload.HIERARCHYA);
        assertNotNull(node);
        assertEquals(tdp.node1, node);
        assertEquals(TestDataPreload.HIERARCHYA, node.hierarchyId);

        // fetching root from invalid hierarchy gets null
        node = hierarchyService.getRootNode(TestDataPreload.INVALID_HIERARCHY);
        assertNull(node);

    }

    /**
     * Test method for {@link org.sakaiproject.hierarchy.impl.HierarchyServiceImpl#getNodeById(java.lang.String)}.
     */
    @Test
    public void testGetNodeById() {
        HierarchyNode node = null;

        initializeMockCache();

        node = hierarchyService.getNodeById(tdp.node4.id);
        assertNotNull(node);
        assertEquals(tdp.node4, node);
        assertEquals(tdp.node4.id, node.id);

        node = hierarchyService.getNodeById(tdp.node6.id);
        assertNotNull(node);
        assertEquals(tdp.node6, node);
        assertEquals(tdp.node6.id, node.id);

        // fetching node with invalid id should fail
        try {
            node = hierarchyService.getNodeById(TestDataPreload.INVALID_NODE_ID);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }  
    }

    /**
     * Test method for {@link org.sakaiproject.hierarchy.impl.HierarchyServiceImpl#getChildNodes(java.lang.String, boolean)}.
     */
    @Test
    public void testGetChildNodes() {
        Set<HierarchyNode> nodes;

        initializeMockCache();

        // check children for the root
        nodes = hierarchyService.getChildNodes(tdp.node1.id, true);
        assertNotNull(nodes);
        assertEquals(3, nodes.size());
        assertTrue(nodes.contains(tdp.node2));
        assertTrue(nodes.contains(tdp.node3));
        assertTrue(nodes.contains(tdp.node4));

        nodes = hierarchyService.getChildNodes(tdp.node1.id, false);
        assertNotNull(nodes);
        assertEquals(7, nodes.size());
        assertTrue(nodes.contains(tdp.node2));
        assertTrue(nodes.contains(tdp.node3));
        assertTrue(nodes.contains(tdp.node4));
        assertTrue(nodes.contains(tdp.node5));
        assertTrue(nodes.contains(tdp.node6));
        assertTrue(nodes.contains(tdp.node7));
        assertTrue(nodes.contains(tdp.node8));

        // check children for the mid level nodes
        nodes = hierarchyService.getChildNodes(tdp.node4.id, true);
        assertNotNull(nodes);
        assertEquals(3, nodes.size());
        assertTrue(nodes.contains(tdp.node6));
        assertTrue(nodes.contains(tdp.node7));
        assertTrue(nodes.contains(tdp.node8));

        nodes = hierarchyService.getChildNodes(tdp.node4.id, false);
        assertNotNull(nodes);
        assertEquals(3, nodes.size());
        assertTrue(nodes.contains(tdp.node6));
        assertTrue(nodes.contains(tdp.node7));
        assertTrue(nodes.contains(tdp.node8));

        // leaf nodes have no children
        nodes = hierarchyService.getChildNodes(tdp.node5.id, true);
        assertNotNull(nodes);
        assertEquals(0, nodes.size());

        nodes = hierarchyService.getChildNodes(tdp.node7.id, true);
        assertNotNull(nodes);
        assertEquals(0, nodes.size());

        // fetching children for invalid node id should fail
        try {
            nodes = hierarchyService.getChildNodes(TestDataPreload.INVALID_NODE_ID, true);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }  
    }

    /**
     * Test method for {@link org.sakaiproject.hierarchy.impl.HierarchyServiceImpl#getParentNodes(java.lang.String, boolean)}.
     */
    @Test
    public void testGetParentNodes() {
        Set<HierarchyNode> nodes;

        // check parents for leaf nodes first
        nodes = hierarchyService.getParentNodes(tdp.node7.id, false);
        assertNotNull(nodes);
        assertEquals(2, nodes.size());
        assertTrue(nodes.contains(tdp.node1));
        assertTrue(nodes.contains(tdp.node4));

        nodes = hierarchyService.getParentNodes(tdp.node7.id, true);
        assertNotNull(nodes);
        assertEquals(1, nodes.size());
        assertTrue(nodes.contains(tdp.node4));

        nodes = hierarchyService.getParentNodes(tdp.node5.id, false);
        assertNotNull(nodes);
        assertEquals(2, nodes.size());
        assertTrue(nodes.contains(tdp.node1));
        assertTrue(nodes.contains(tdp.node3));

        // check one with multiple parents
        nodes = hierarchyService.getParentNodes(tdp.node10.id, false);
        assertNotNull(nodes);
        assertEquals(2, nodes.size());
        assertTrue(nodes.contains(tdp.node9));
        assertTrue(nodes.contains(tdp.node11));

        nodes = hierarchyService.getParentNodes(tdp.node10.id, true);
        assertNotNull(nodes);
        assertEquals(2, nodes.size());
        assertTrue(nodes.contains(tdp.node9));
        assertTrue(nodes.contains(tdp.node11));

        // root nodes have no parents
        nodes = hierarchyService.getParentNodes(tdp.node1.id, true);
        assertNotNull(nodes);
        assertEquals(0, nodes.size());

        nodes = hierarchyService.getParentNodes(tdp.node9.id, true);
        assertNotNull(nodes);
        assertEquals(0, nodes.size());

        // fetching children for invalid node id should fail
        try {
            nodes = hierarchyService.getParentNodes(TestDataPreload.INVALID_NODE_ID, true);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }  
    }

    /**
     * Test method for {@link org.sakaiproject.hierarchy.impl.HierarchyServiceImpl#addNode(java.lang.String, java.lang.String)}.
     */
    @Test
    public void testAddNode() {
        HierarchyNode node = null;
        String newNodeId = null;

        initializeMockCache();

        // check we can insert a node in a normal tree and that the links are created correctly in this node
        node = hierarchyService.addNode(TestDataPreload.HIERARCHYA, tdp.node2.id);
        assertNotNull(node);
        newNodeId = node.id;
        assertNotNull(newNodeId);
        assertNotNull(node.directParentNodeIds);
        assertEquals(1, node.directParentNodeIds.size());
        assertTrue(node.directParentNodeIds.contains(tdp.node2.id));
        assertNotNull(node.parentNodeIds);
        assertEquals(2, node.parentNodeIds.size());
        assertTrue(node.parentNodeIds.contains(tdp.node2.id));
        assertTrue(node.parentNodeIds.contains(tdp.node1.id));
        assertNotNull(node.directChildNodeIds);
        assertTrue(node.directChildNodeIds.isEmpty());
        assertNotNull(node.childNodeIds);
        assertTrue(node.childNodeIds.isEmpty());

        // now check that the child links were updated correctly for the parent
        node = hierarchyService.getNodeById(tdp.node2.id);
        assertNotNull(node);
        assertEquals(tdp.node2.id, node.id);
        assertNotNull(node.directChildNodeIds);
        assertEquals(1, node.directChildNodeIds.size());
        assertTrue(node.directChildNodeIds.contains(newNodeId));
        assertNotNull(node.childNodeIds);
        assertEquals(1, node.childNodeIds.size());
        assertTrue(node.childNodeIds.contains(newNodeId));

        // and the root node
        node = hierarchyService.getNodeById(tdp.node1.id);
        assertNotNull(node);
        assertEquals(tdp.node1.id, node.id);
        assertNotNull(node.directChildNodeIds);
        assertEquals(3, node.directChildNodeIds.size());
        assertTrue(node.directChildNodeIds.contains(tdp.node2.id));
        assertTrue(node.directChildNodeIds.contains(tdp.node3.id));
        assertTrue(node.directChildNodeIds.contains(tdp.node4.id));
        assertNotNull(node.childNodeIds);
        assertEquals(8, node.childNodeIds.size());
        assertTrue(node.childNodeIds.contains(newNodeId));
        assertTrue(node.childNodeIds.contains(tdp.node2.id));
        assertTrue(node.childNodeIds.contains(tdp.node3.id));
        assertTrue(node.childNodeIds.contains(tdp.node4.id));
        assertTrue(node.childNodeIds.contains(tdp.node5.id));
        assertTrue(node.childNodeIds.contains(tdp.node6.id));
        assertTrue(node.childNodeIds.contains(tdp.node7.id));
        assertTrue(node.childNodeIds.contains(tdp.node8.id));


        // check we can insert a node in an upward tree and that the links are created correctly in this node
        node = hierarchyService.addNode(TestDataPreload.HIERARCHYB, tdp.node10.id);
        assertNotNull(node);
        newNodeId = node.id;
        assertNotNull(newNodeId);
        assertNotNull(node.directParentNodeIds);
        assertEquals(1, node.directParentNodeIds.size());
        assertTrue(node.directParentNodeIds.contains(tdp.node10.id));
        assertNotNull(node.parentNodeIds);
        assertEquals(3, node.parentNodeIds.size());
        assertTrue(node.parentNodeIds.contains(tdp.node10.id));
        assertTrue(node.parentNodeIds.contains(tdp.node9.id));
        assertTrue(node.parentNodeIds.contains(tdp.node11.id));
        assertNotNull(node.directChildNodeIds);
        assertTrue(node.directChildNodeIds.isEmpty());
        assertNotNull(node.childNodeIds);
        assertTrue(node.childNodeIds.isEmpty());

        // now check that the child links were updated correctly for the parent
        node = hierarchyService.getNodeById(tdp.node10.id);
        assertNotNull(node);
        assertEquals(tdp.node10.id, node.id);
        assertNotNull(node.directChildNodeIds);
        assertEquals(1, node.directChildNodeIds.size());
        assertTrue(node.directChildNodeIds.contains(newNodeId));
        assertNotNull(node.childNodeIds);
        assertEquals(1, node.childNodeIds.size());
        assertTrue(node.childNodeIds.contains(newNodeId));

        // and the root node
        node = hierarchyService.getNodeById(tdp.node9.id);
        assertNotNull(node);
        assertEquals(tdp.node9.id, node.id);
        assertNotNull(node.directChildNodeIds);
        assertEquals(1, node.directChildNodeIds.size());
        assertTrue(node.directChildNodeIds.contains(tdp.node10.id));
        assertNotNull(node.childNodeIds);
        assertEquals(2, node.childNodeIds.size());
        assertTrue(node.childNodeIds.contains(newNodeId));
        assertTrue(node.childNodeIds.contains(tdp.node10.id));

        // and the other higher parent node
        node = hierarchyService.getNodeById(tdp.node11.id);
        assertNotNull(node);
        assertEquals(tdp.node11.id, node.id);
        assertNotNull(node.directChildNodeIds);
        assertEquals(1, node.directChildNodeIds.size());
        assertTrue(node.directChildNodeIds.contains(tdp.node10.id));
        assertNotNull(node.childNodeIds);
        assertEquals(2, node.childNodeIds.size());
        assertTrue(node.childNodeIds.contains(newNodeId));
        assertTrue(node.childNodeIds.contains(tdp.node10.id));


        // check we can insert a node next to others and that the links are created correctly in this node
        node = hierarchyService.addNode(TestDataPreload.HIERARCHYA, tdp.node3.id);
        assertNotNull(node);
        newNodeId = node.id;
        assertNotNull(newNodeId);
        assertNotNull(node.directParentNodeIds);
        assertEquals(1, node.directParentNodeIds.size());
        assertTrue(node.directParentNodeIds.contains(tdp.node3.id));
        assertNotNull(node.parentNodeIds);
        assertEquals(2, node.parentNodeIds.size());
        assertTrue(node.parentNodeIds.contains(tdp.node3.id));
        assertTrue(node.parentNodeIds.contains(tdp.node1.id));
        assertNotNull(node.directChildNodeIds);
        assertTrue(node.directChildNodeIds.isEmpty());
        assertNotNull(node.childNodeIds);
        assertTrue(node.childNodeIds.isEmpty());

        // now check that the child links were updated correctly for the parent
        node = hierarchyService.getNodeById(tdp.node3.id);
        assertNotNull(node);
        assertEquals(tdp.node3.id, node.id);
        assertNotNull(node.directChildNodeIds);
        assertEquals(2, node.directChildNodeIds.size());
        assertTrue(node.directChildNodeIds.contains(newNodeId));
        assertTrue(node.directChildNodeIds.contains(tdp.node5.id));
        assertNotNull(node.childNodeIds);
        assertEquals(2, node.childNodeIds.size());
        assertTrue(node.childNodeIds.contains(newNodeId));
        assertTrue(node.childNodeIds.contains(tdp.node5.id));

        // check that adding a node without a parent puts the node at the top of the hierarchy
        // NOTE: not currently supported, so this should die
        try {
            node = hierarchyService.addNode(TestDataPreload.HIERARCHYA, null);
            fail("Should have thrown exception");
        } catch (RuntimeException e) {
            assertNotNull(e);
        }

        // check that attempting to add a node to a non-existent node fails
        try {
            node = hierarchyService.addNode(TestDataPreload.HIERARCHYA, TestDataPreload.INVALID_NODE_ID);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }
    }

    /**
     * Test method for {@link org.sakaiproject.hierarchy.impl.HierarchyServiceImpl#removeNode(java.lang.String)}.
     */
    @Test
    public void testRemoveNode() {
        HierarchyNode node = null;

        initializeMockCache();

        // remove a node with no children
        node = hierarchyService.removeNode(tdp.node8.id);
        assertNotNull(node);
        assertNotNull(node.directChildNodeIds);
        assertEquals(2, node.directChildNodeIds.size());
        assertTrue(node.directChildNodeIds.contains(tdp.node6.id));
        assertTrue(node.directChildNodeIds.contains(tdp.node7.id));

        // also check the root was updated correctly
        node = hierarchyService.getNodeById(tdp.node1.id);
        assertNotNull(node);
        assertEquals(tdp.node1.id, node.id);
        assertNotNull(node.directChildNodeIds);
        assertEquals(3, node.directChildNodeIds.size());
        assertTrue(node.directChildNodeIds.contains(tdp.node2.id));
        assertTrue(node.directChildNodeIds.contains(tdp.node3.id));
        assertTrue(node.directChildNodeIds.contains(tdp.node4.id));
        assertNotNull(node.childNodeIds);
        assertEquals(6, node.childNodeIds.size());
        assertTrue(node.childNodeIds.contains(tdp.node2.id));
        assertTrue(node.childNodeIds.contains(tdp.node3.id));
        assertTrue(node.childNodeIds.contains(tdp.node4.id));
        assertTrue(node.childNodeIds.contains(tdp.node5.id));
        assertTrue(node.childNodeIds.contains(tdp.node6.id));
        assertTrue(node.childNodeIds.contains(tdp.node7.id));

        // remove another node
        node = hierarchyService.removeNode(tdp.node2.id);
        assertNotNull(node);
        assertNotNull(node.directChildNodeIds);
        assertEquals(2, node.directChildNodeIds.size());
        assertTrue(node.childNodeIds.contains(tdp.node3.id));
        assertTrue(node.childNodeIds.contains(tdp.node4.id));

        // also check the root was updated correctly
        node = hierarchyService.getNodeById(tdp.node1.id);
        assertNotNull(node);
        assertEquals(tdp.node1.id, node.id);
        assertNotNull(node.directChildNodeIds);
        assertEquals(2, node.directChildNodeIds.size());
        assertTrue(node.directChildNodeIds.contains(tdp.node3.id));
        assertTrue(node.directChildNodeIds.contains(tdp.node4.id));
        assertNotNull(node.childNodeIds);
        assertEquals(5, node.childNodeIds.size());
        assertTrue(node.childNodeIds.contains(tdp.node3.id));
        assertTrue(node.childNodeIds.contains(tdp.node4.id));
        assertTrue(node.childNodeIds.contains(tdp.node5.id));
        assertTrue(node.childNodeIds.contains(tdp.node6.id));
        assertTrue(node.childNodeIds.contains(tdp.node7.id));

        // cannot remove root node
        try {
            node = hierarchyService.removeNode(tdp.node1.id);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        // cannot remove nodes with children
        try {
            node = hierarchyService.removeNode(tdp.node4.id);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        try {
            node = hierarchyService.removeNode(tdp.node3.id);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        // cannot remove nodes with multiple parents
        try {
            node = hierarchyService.removeNode(tdp.node10.id);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        // cannot use invalid node id (exception)
        try {
            node = hierarchyService.removeNode(TestDataPreload.INVALID_NODE_ID);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        // cannot use null node id (exception)
        try {
            node = hierarchyService.removeNode(null);
            fail("Should have thrown exception");
        } catch (NullPointerException e) {
            assertNotNull(e.getMessage());
        }
    }

    /**
     * Test method for {@link org.sakaiproject.hierarchy.impl.HierarchyServiceImpl#saveNodeMetaData(java.lang.String, java.lang.String, java.lang.String)}.
     */
    @Test
    public void testSaveNodeMetaData() {
        HierarchyNode node = null;

        // saving node data
        node = hierarchyService.saveNodeMetaData(tdp.node2.id, "Node TWO", "this is a description!", "TOKEN2");
        assertNotNull(node);
        assertEquals(node.id, tdp.node2.id);
        assertEquals("Node TWO", node.title);
        assertEquals("this is a description!", node.description);
        assertEquals("TOKEN2", node.permToken);

        // saving some nulls (should be ok)
        node = hierarchyService.saveNodeMetaData(tdp.node2.id, null, "DESC", "");
        assertNotNull(node);
        assertEquals(node.id, tdp.node2.id);
        assertEquals("Node TWO", node.title);
        assertEquals("DESC", node.description);
        assertNull(node.permToken);

        // saving all nulls (should be save as previous values)
        node = hierarchyService.saveNodeMetaData(tdp.node2.id, null, null, null);
        assertNotNull(node);
        assertEquals(node.id, tdp.node2.id);
        assertEquals("Node TWO", node.title);
        assertEquals("DESC", node.description);
        assertNull(node.permToken);

        // saving empty strings (should blank everything out)
        node = hierarchyService.saveNodeMetaData(tdp.node2.id, "", "", "");
        assertNotNull(node);
        assertEquals(node.id, tdp.node2.id);
        assertNull(node.title);
        assertNull(node.description);
        assertNull(node.permToken);

        // cannot use invalid node id (exception) 
        try {
            node = hierarchyService.saveNodeMetaData(TestDataPreload.INVALID_NODE_ID, null, null, null);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        // cannot use null node id (exception)
        try {
            node = hierarchyService.saveNodeMetaData(null, null, null, null);
            fail("Should have thrown exception");
        } catch (NullPointerException e) {
            assertNotNull(e.getMessage());
        }
    }

    /**
     * Test method for {@link org.sakaiproject.hierarchy.impl.HierarchyServiceImpl#addChildRelation(java.lang.String, java.lang.String)}.
     */
    @Test
    public void testAddChildRelation() {
        HierarchyNode node = null;

        // add new children
        node = hierarchyService.addChildRelation(tdp.node2.id, tdp.node6.id);
        assertNotNull(node);
        assertNotNull(node.directChildNodeIds);
        assertEquals(1, node.directChildNodeIds.size());
        assertTrue(node.directChildNodeIds.contains(tdp.node6.id));
        assertNotNull(node.childNodeIds);
        assertEquals(1, node.childNodeIds.size());
        assertTrue(node.childNodeIds.contains(tdp.node6.id));

        node = hierarchyService.addChildRelation(tdp.node3.id, tdp.node7.id);
        assertNotNull(node);
        assertNotNull(node.directChildNodeIds);
        assertEquals(2, node.directChildNodeIds.size());
        assertTrue(node.directChildNodeIds.contains(tdp.node5.id));
        assertTrue(node.directChildNodeIds.contains(tdp.node7.id));

        // add children which are already there
        node = hierarchyService.addChildRelation(tdp.node3.id, tdp.node5.id);
        assertNotNull(node);
        assertNotNull(node.directChildNodeIds);
        assertEquals(2, node.directChildNodeIds.size());
        assertTrue(node.directChildNodeIds.contains(tdp.node5.id));
        assertTrue(node.directChildNodeIds.contains(tdp.node7.id));

        node = hierarchyService.addChildRelation(tdp.node4.id, tdp.node7.id);
        assertNotNull(node);
        assertNotNull(node.directChildNodeIds);
        assertEquals(3, node.directChildNodeIds.size());
        assertTrue(node.directChildNodeIds.contains(tdp.node6.id));

        // cannot add this node as a child of itself
        try {
            node = hierarchyService.addChildRelation(tdp.node7.id, tdp.node7.id);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        // cannot create a cycle by adding a child which is already a child or parent of this node
        // (should probably check distance from the root...)
        try {
            node = hierarchyService.addChildRelation(tdp.node7.id, tdp.node4.id);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        try {
            node = hierarchyService.addChildRelation(tdp.node7.id, tdp.node1.id);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        try {
            node = hierarchyService.addChildRelation(tdp.node5.id, tdp.node3.id);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        // cannot use invalid node ids (exception)
        try {
            node = hierarchyService.addChildRelation(TestDataPreload.INVALID_NODE_ID, tdp.node6.id);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        try {
            node = hierarchyService.addChildRelation(tdp.node2.id, TestDataPreload.INVALID_NODE_ID);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        // cannot use null node id (exception)
        try {
            node = hierarchyService.addChildRelation(null, tdp.node6.id);
            fail("Should have thrown exception");
        } catch (NullPointerException e) {
            assertNotNull(e.getMessage());
        }

        try {
            node = hierarchyService.addChildRelation(tdp.node2.id, null);
            fail("Should have thrown exception");
        } catch (NullPointerException e) {
            assertNotNull(e.getMessage());
        }

        //fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.sakaiproject.hierarchy.impl.HierarchyServiceImpl#removeChildRelation(java.lang.String, java.lang.String)}.
     */
    
    @Test
    public void testRemoveChildRelation() {
        HierarchyNode node = null;

        // create extra relation first
        node = hierarchyService.addChildRelation(tdp.node2.id, tdp.node6.id);

        // remove a child
        node = hierarchyService.removeChildRelation(tdp.node11.id, tdp.node10.id);
        assertNotNull(node);
        assertNotNull(node.directChildNodeIds);
        assertEquals(0, node.directChildNodeIds.size());
        assertNotNull(node.childNodeIds);
        assertEquals(0, node.childNodeIds.size());

        node = hierarchyService.removeChildRelation(tdp.node4.id, tdp.node6.id);
        assertNotNull(node);
        assertNotNull(node.directChildNodeIds);
        assertEquals(2, node.directChildNodeIds.size());
        assertTrue(node.directChildNodeIds.contains(tdp.node7.id));
        assertTrue(node.directChildNodeIds.contains(tdp.node8.id));
        assertNotNull(node.childNodeIds);
        assertEquals(2, node.childNodeIds.size());
        assertTrue(node.childNodeIds.contains(tdp.node7.id));
        assertTrue(node.childNodeIds.contains(tdp.node8.id));

        // remove child which is not a child (this is ok)
        node = hierarchyService.removeChildRelation(tdp.node3.id, tdp.node6.id);

        node = hierarchyService.removeChildRelation(tdp.node3.id, tdp.node2.id);

        // cannot remove myself as a child of myself
        try {
            node = hierarchyService.removeChildRelation(tdp.node2.id, tdp.node2.id);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        // cannot orphan nodes by removing a child relation (must use remove node)
        try {
            node = hierarchyService.removeChildRelation(tdp.node1.id, tdp.node3.id);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        try {
            node = hierarchyService.removeChildRelation(tdp.node3.id, tdp.node5.id);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        // cannot use invalid node ids (exception)
        try {
            node = hierarchyService.removeChildRelation(TestDataPreload.INVALID_NODE_ID, tdp.node6.id);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        try {
            node = hierarchyService.removeChildRelation(tdp.node2.id, TestDataPreload.INVALID_NODE_ID);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        // cannot use null node id (exception)
        try {
            node = hierarchyService.removeChildRelation(null, tdp.node6.id);
            fail("Should have thrown exception");
        } catch (NullPointerException e) {
            assertNotNull(e.getMessage());
        }

        try {
            node = hierarchyService.removeChildRelation(tdp.node2.id, null);
            fail("Should have thrown exception");
        } catch (NullPointerException e) {
            assertNotNull(e.getMessage());
        }

        //    fail("Not yet implemented");
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
