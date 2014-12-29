/******************************************************************************
 * HierarchyUtilsTest.java - created by aaronz on Jul 31, 2007
 * 
 * Copyright (c) 2007 Centre for Academic Research in Educational Technologies
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 *****************************************************************************/

package org.sakaiproject.hierarchy.impl.test;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.sakaiproject.hierarchy.dao.model.HierarchyNodeMetaData;
import org.sakaiproject.hierarchy.dao.model.HierarchyPersistentNode;
import org.sakaiproject.hierarchy.impl.utils.HierarchyImplUtils;
import org.sakaiproject.hierarchy.model.HierarchyNode;

/**
 * Testing the utils class to make sure we can count on it
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class HierarchyImplUtilsTest extends TestCase {

    private char s = HierarchyImplUtils.SEPERATOR;

    private String NODE1 = "1";
    private String NODE2 = "2";
    private String NODE3 = "3";
    private String NODE30 = "30";
    private String NODE41 = "41";
    private String NODE42 = "42";
    private String NODE50 = "50";

    private String ENCODED_NODE1 = s + NODE1 + s;

    private String ENCODED_1 = s + NODE1 + s + NODE3 + s + NODE30 + s;
    private String ENCODED_2 = s + NODE2 + s + NODE30 + s + NODE41 + s + NODE50 + s;
    private String ENCODED_3 = s + NODE30 + s + NODE42 + s;

    private String ENCODED_1_41 = ENCODED_1 + NODE41 + s;
    private String ENCODED_2_1 = s + NODE1 + ENCODED_2;
    private String ENCODED_3_41 = s + NODE30 + s + NODE41 + s + NODE42 + s;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Test method for {@link org.sakaiproject.hierarchy.impl.utils.HierarchyImplUtils#makeNode(org.sakaiproject.hierarchy.dao.model.HierarchyPersistentNode, org.sakaiproject.hierarchy.dao.model.HierarchyNodeMetaData)}.
     */
    public void testMakeNodeHierarchyPersistentNodeHierarchyNodeMetaData() {
        HierarchyNode node = null;

        HierarchyNodeMetaData metaData = new HierarchyNodeMetaData(null, "HID", Boolean.FALSE, "aaronz", "Title", "Desc", "KEY", Boolean.FALSE);
        HierarchyPersistentNode pNode = new HierarchyPersistentNode(ENCODED_NODE1, ENCODED_1);
        pNode.setId( new Long(100) );

        node = HierarchyImplUtils.makeNode(pNode, metaData);
        assertNotNull(node);
        assertEquals("100", node.id);
        assertEquals("HID", node.hierarchyId);
        assertEquals("Title", node.title);
        assertEquals("Desc", node.description);
        assertEquals("KEY", node.permToken);
        assertNotNull(node.directParentNodeIds);
        assertTrue(node.directParentNodeIds.contains(NODE1));
        assertNotNull(node.parentNodeIds);
        assertTrue(node.parentNodeIds.contains(NODE1));
        assertTrue(node.parentNodeIds.contains(NODE3));
        assertNotNull(node.directChildNodeIds);
        assertTrue(node.directChildNodeIds.isEmpty());
        assertNotNull(node.childNodeIds);
        assertTrue(node.childNodeIds.isEmpty());
    }

    /**
     * Test method for {@link org.sakaiproject.hierarchy.impl.utils.HierarchyImplUtils#makeNode(org.sakaiproject.hierarchy.dao.model.HierarchyNodeMetaData)}.
     */
    public void testMakeNodeHierarchyNodeMetaData() {
        HierarchyNode node = null;

        // test that null meta causes null
        node = HierarchyImplUtils.makeNode(null);
        assertNull(node);

        HierarchyPersistentNode pNode = null;
        HierarchyNodeMetaData metaData = new HierarchyNodeMetaData(pNode, "HID", Boolean.FALSE, "aaronz", "Title", "Desc", null, Boolean.FALSE);

        // test that invalid pNode causes death
        try {
            node = HierarchyImplUtils.makeNode(metaData);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }

        pNode = new HierarchyPersistentNode(ENCODED_NODE1, ENCODED_1);
        pNode.setId( new Long(100) );
        metaData.setNode(pNode);
        node = HierarchyImplUtils.makeNode(metaData);
        assertNotNull(node);
        assertEquals("100", node.id);
        assertEquals("HID", node.hierarchyId);
        assertEquals("Title", node.title);
        assertEquals("Desc", node.description);
        assertNotNull(node.directParentNodeIds);
        assertTrue(node.directParentNodeIds.contains(NODE1));
        assertNotNull(node.parentNodeIds);
        assertTrue(node.parentNodeIds.contains(NODE1));
        assertTrue(node.parentNodeIds.contains(NODE3));
        assertNotNull(node.directChildNodeIds);
        assertTrue(node.directChildNodeIds.isEmpty());
        assertNotNull(node.childNodeIds);
        assertTrue(node.childNodeIds.isEmpty());
    }

    /**
     * Test method for {@link org.sakaiproject.hierarchy.impl.utils.HierarchyImplUtils#makeNodeIdSet(java.lang.String)}.
     */
    public void testMakeNodeIdSet() {
        Set<String> nodeIds = null;

        nodeIds = HierarchyImplUtils.makeNodeIdSet(null);
        assertNotNull(nodeIds);
        assertTrue(nodeIds.isEmpty());

        nodeIds = HierarchyImplUtils.makeNodeIdSet(ENCODED_1);
        assertNotNull(nodeIds);
        assertEquals(3, nodeIds.size());
        assertTrue(nodeIds.contains(NODE1));
        assertTrue(nodeIds.contains(NODE3));
        assertTrue(nodeIds.contains(NODE30));
    }

    /**
     * Test method for {@link org.sakaiproject.hierarchy.impl.utils.HierarchyImplUtils#makeEncodedNodeIdString(java.util.Set)}.
     */
    public void testMakeEncodedNodeIdString() {
        String encoded = null;

        Set<String> nodeIds = new HashSet<String>();

        // check that empty set generates null string
        encoded = HierarchyImplUtils.makeEncodedNodeIdString(nodeIds);
        assertNull(encoded);

        nodeIds.add(NODE1);
        nodeIds.add(NODE30);
        nodeIds.add(NODE3);

        // test converting set to string
        encoded = HierarchyImplUtils.makeEncodedNodeIdString(nodeIds);
        assertNotNull(encoded);
        assertEquals(ENCODED_1, encoded);

        nodeIds.add(NODE41);

        // test converting set to string
        encoded = HierarchyImplUtils.makeEncodedNodeIdString(nodeIds);
        assertNotNull(encoded);
        assertEquals(ENCODED_1_41, encoded);
    }

    /**
     * Test method for {@link org.sakaiproject.hierarchy.impl.utils.HierarchyImplUtils#makeSingleEncodedNodeIdString(java.lang.String)}.
     */
    public void testMakeSingleEncodedNodeIdString() {
        String encoded = null;

        // encoding a string
        encoded = HierarchyImplUtils.makeSingleEncodedNodeIdString(NODE1);
        assertNotNull(encoded);
        assertEquals(ENCODED_NODE1, encoded);

        encoded = HierarchyImplUtils.makeSingleEncodedNodeIdString(null);
        assertNull(encoded);
    }

    /**
     * Test method for {@link org.sakaiproject.hierarchy.impl.utils.HierarchyImplUtils#addSingleNodeIdToEncodedString(java.lang.String, java.lang.String)}.
     */
    public void testAddSingleNodeIdToEncodedString() {
        String encoded = null;

        // try adding strings to various points in the encoded string
        encoded = HierarchyImplUtils.addSingleNodeIdToEncodedString(ENCODED_2, NODE1);
        assertNotNull(encoded);
        assertEquals(ENCODED_2_1, encoded);

        encoded = HierarchyImplUtils.addSingleNodeIdToEncodedString(ENCODED_3, NODE41);
        assertNotNull(encoded);
        assertEquals(ENCODED_3_41, encoded);

        encoded = HierarchyImplUtils.addSingleNodeIdToEncodedString(ENCODED_1, NODE41);
        assertNotNull(encoded);
        assertEquals(ENCODED_1_41, encoded);

        // try adding string that already exist
        encoded = HierarchyImplUtils.addSingleNodeIdToEncodedString(ENCODED_1, NODE3);
        assertNotNull(encoded);
        assertEquals(ENCODED_1, encoded);

        encoded = HierarchyImplUtils.addSingleNodeIdToEncodedString(ENCODED_2, NODE2);
        assertNotNull(encoded);
        assertEquals(ENCODED_2, encoded);

        encoded = HierarchyImplUtils.addSingleNodeIdToEncodedString(ENCODED_3, NODE42);
        assertNotNull(encoded);
        assertEquals(ENCODED_3, encoded);

        // now try out the edge cases
        encoded = HierarchyImplUtils.addSingleNodeIdToEncodedString(null, NODE1);
        assertNotNull(encoded);
        assertEquals(ENCODED_NODE1, encoded);

        encoded = HierarchyImplUtils.addSingleNodeIdToEncodedString(ENCODED_1, null);
        assertNotNull(encoded);
        assertEquals(ENCODED_1, encoded);
    }

}
