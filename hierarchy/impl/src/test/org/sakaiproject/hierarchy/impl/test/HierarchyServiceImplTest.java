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

import java.util.Map;
import java.util.Set;

import org.sakaiproject.genericdao.api.search.Search;
import org.sakaiproject.hierarchy.dao.HierarchyDao;
import org.sakaiproject.hierarchy.dao.model.HierarchyNodeMetaData;
import org.sakaiproject.hierarchy.impl.HierarchyServiceImpl;
import org.sakaiproject.hierarchy.impl.test.data.TestDataPreload;
import org.sakaiproject.hierarchy.model.HierarchyNode;
import org.springframework.test.AbstractTransactionalSpringContextTests;

/**
 * Testing the hierarchy service
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class HierarchyServiceImplTest extends AbstractTransactionalSpringContextTests {

    protected HierarchyServiceImpl hierarchyService;

    private HierarchyDao dao;
    private TestDataPreload tdp;

    // private SessionManager sessionManager;
    // private MockControl sessionManagerControl;


    protected String[] getConfigLocations() {
        // point to the needed spring config files, must be on the classpath
        // (add component/src/webapp/WEB-INF to the build path in Eclipse),
        // they also need to be referenced in the project.xml file
        return new String[] {"hibernate-test.xml", "spring-hibernate.xml"};
    }

    // run this before each test starts
    protected void onSetUpBeforeTransaction() throws Exception {
        // load the spring created dao class bean from the Spring Application Context
        dao = (HierarchyDao) applicationContext.getBean("org.sakaiproject.hierarchy.dao.HierarchyDao");
        if (dao == null) {
            throw new NullPointerException("Dao could not be retrieved from spring context");
        }

        // load up the test data preloader from spring
        tdp = (TestDataPreload) applicationContext.getBean("org.sakaiproject.hierarchy.test.data.TestDataPreload");
        if (tdp == null) {
            throw new NullPointerException("TestDatePreload could not be retrieved from spring context");
        }

        // load up any other needed spring beans

        //    // setup the mock objects if needed
        //    sessionManagerControl = MockControl.createControl(SessionManager.class);
        //    sessionManager = (SessionManager) sessionManagerControl.getMock();

        //    //this mock object is simply keeping us from getting a null when getCurrentSessionUserId is called 
        //    sessionManager.getCurrentSessionUserId(); // expect this to be called
        //    sessionManagerControl.setDefaultMatcher(MockControl.ALWAYS_MATCHER);
        //    sessionManagerControl.setReturnValue(TestDataPreload.USER_ID, MockControl.ZERO_OR_MORE);
        //    sessionManagerControl.replay();

        //create and setup the object to be tested
        hierarchyService = new HierarchyServiceImpl();
        hierarchyService.setDao(dao);
        //    hierarchyService.setSessionManager(sessionManager);
    }

    // run this before each test starts and as part of the transaction
    protected void onSetUpInTransaction() {
        // preload additional data if desired
    }

    /**
     * ADD unit tests below here, use testMethod as the name of the unit test,
     * Note that if a method is overloaded you should include the arguments in the
     * test name like so: testMethodClassInt (for method(Class, int);
     */

    public void testValidTestData() {
        // ensure the test data is setup the way we think
        assertEquals(new Long(1), tdp.pNode1.getId());
        assertEquals(new Long(6), tdp.pNode6.getId());
        assertEquals(new Long(9), tdp.pNode9.getId());
    }

    /**
     * Test method for {@link org.sakaiproject.hierarchy.impl.HierarchyServiceImpl#createHierarchy(java.lang.String)}.
     */
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
    public void testGetNodeById() {
        HierarchyNode node = null;

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
    public void testGetChildNodes() {
        Set<HierarchyNode> nodes;

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
    public void testAddNode() {
        HierarchyNode node = null;
        String newNodeId = null;

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
    public void testRemoveNode() {
        HierarchyNode node = null;

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
     * Test method for {@link org.sakaiproject.hierarchy.impl.HierarchyServiceImpl#setNodeDisabled(java.lang.String, java.lang.Boolean)}.
     */
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

    /**
     * Test method for {@link org.sakaiproject.hierarchy.impl.HierarchyServiceImpl#addChildRelation(java.lang.String, java.lang.String)}.
     */
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


    /**
     * Test method for {@link org.sakaiproject.hierarchy.impl.HierarchyServiceImpl#addParentRelation(java.lang.String, java.lang.String)}.
     *//**
    public void testAddParentRelation() {
        // add new parents

        // add parents which are already there

        // cannot remove all parents (must leave at least one)

        // cannot add parents to the root node

        // cannot create a cycle by adding a parent which is already a child or parent of this node

        // cannot add parents nodes which do not exist (should fail)

        // cannot use invalid node id (exception)

        // cannot use invalid parent node id (exception)

        // cannot use null node id (exception)

        fail("Not yet implemented");
    }**/

    /**
     * Test method for {@link org.sakaiproject.hierarchy.impl.HierarchyServiceImpl#removeParentRelation(java.lang.String, java.lang.String)}.
     *//**
    public void testRemoveParentRelation() {
        // cannot remove all parents (must leave at least one)

        fail("Not yet implemented");
    }**/


    /**
     * Test method for {@link org.sakaiproject.hierarchy.impl.HierarchyServiceImpl#getNodesWithToken(java.lang.String)}.
     */
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
    public void testAssignUserNodePerm() {
        Set<HierarchyNode> nodes = hierarchyService.getNodesForUserPerm(TestDataPreload.MAINT_USER_ID, TestDataPreload.PERM_ONE);
        assertEquals(5, nodes.size());

        // add existing one - should be no change
        hierarchyService.assignUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node2.id, TestDataPreload.PERM_ONE, false);
        nodes = hierarchyService.getNodesForUserPerm(TestDataPreload.MAINT_USER_ID, TestDataPreload.PERM_ONE);
        assertEquals(5, nodes.size());

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
    public void testRemoveUserNodePerm() {
        Set<HierarchyNode> nodes = hierarchyService.getNodesForUserPerm(TestDataPreload.MAINT_USER_ID, TestDataPreload.PERM_ONE);
        assertEquals(5, nodes.size());

        hierarchyService.removeUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node2.id, TestDataPreload.PERM_ONE, false);
        nodes = hierarchyService.getNodesForUserPerm(TestDataPreload.MAINT_USER_ID, TestDataPreload.PERM_ONE);
        assertEquals(4, nodes.size());
        assertFalse( hierarchyService.checkUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node2.id, TestDataPreload.PERM_ONE) );

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



    /*
      HierarchyNode node = null;
      Set<String> children = new HashSet<String>();;

      // add new children
      children.add(tdp.node6.id);
      node = hierarchyService.updateChildren(tdp.node2.id, children);
      assertNotNull(node);
      assertNotNull(node.directChildNodeIds);
      assertEquals(1, node.directChildNodeIds.size());
      assertTrue(node.directChildNodeIds.contains(tdp.node6.id));

      children.add(tdp.node7.id);
      children.add(tdp.node8.id);
      node = hierarchyService.updateChildren(tdp.node2.id, children);
      assertNotNull(node);
      assertNotNull(node.directChildNodeIds);
      assertEquals(3, node.directChildNodeIds.size());
      assertTrue(node.directChildNodeIds.contains(tdp.node6.id));
      assertTrue(node.directChildNodeIds.contains(tdp.node7.id));
      assertTrue(node.directChildNodeIds.contains(tdp.node8.id));

      // remove some children
      children.clear();
      children.add(tdp.node7.id);
      children.add(tdp.node8.id);
      node = hierarchyService.updateChildren(tdp.node4.id, children);
      assertNotNull(node);
      assertNotNull(node.directChildNodeIds);
      assertEquals(2, node.directChildNodeIds.size());
      assertTrue(node.directChildNodeIds.contains(tdp.node7.id));
      assertTrue(node.directChildNodeIds.contains(tdp.node8.id));

      // remove all children
      children.clear();
      node = hierarchyService.updateChildren(tdp.node4.id, children);
      assertNotNull(node);
      assertNotNull(node.directChildNodeIds);
      assertEquals(0, node.directChildNodeIds.size());

      // update children to the identical set
      children.clear();
      children.add(tdp.node5.id);
      node = hierarchyService.updateChildren(tdp.node3.id, children);
      assertNotNull(node);
      assertNotNull(node.directChildNodeIds);
      assertEquals(1, node.directChildNodeIds.size());
      assertTrue(node.directChildNodeIds.contains(tdp.node5.id));

      // cannot add children nodes which do not exist (even if some are valid)
      children.add(TestDataPreload.INVALID_NODE_ID);
      try {
         node = hierarchyService.updateChildren(tdp.node3.id, children);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      // cannot add child node which is equal to this node
      children.clear();
      children.add(tdp.node5.id);
      children.add(tdp.node3.id);
      try {
         node = hierarchyService.updateChildren(tdp.node3.id, children);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      children.clear();
      children.add(tdp.node3.id);
      try {
         node = hierarchyService.updateChildren(tdp.node3.id, children);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      // cannot remove child node so that it becomes orphaned
      children.clear();
      children.add(tdp.node2.id);
      children.add(tdp.node4.id);
      try {
         node = hierarchyService.updateChildren(tdp.node1.id, children);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      children.clear();
      children.add(tdp.node3.id);
      children.add(tdp.node4.id);
      try {
         node = hierarchyService.updateChildren(tdp.node1.id, children);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      // cannot use invalid node id (exception)
      children.clear();
      children.add(tdp.node6.id);
      try {
         node = hierarchyService.updateChildren(TestDataPreload.INVALID_NODE_ID, children);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      // cannot use invalid child node id (exception)
      children.clear();
      children.add(tdp.node6.id);
      children.add(TestDataPreload.INVALID_NODE_ID);
      try {
         node = hierarchyService.updateChildren(tdp.node2.id, children);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      // cannot use null node id (exception)
      children.clear();
      try {
         node = hierarchyService.updateChildren(null, children);
         fail("Should have thrown exception");
      } catch (NullPointerException e) {
         assertNotNull(e.getMessage());
      }

     */

}
