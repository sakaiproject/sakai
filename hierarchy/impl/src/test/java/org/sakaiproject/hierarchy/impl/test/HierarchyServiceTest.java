package org.sakaiproject.hierarchy.impl.test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.hierarchy.impl.HierarchyServiceImpl;
import org.sakaiproject.hierarchy.impl.test.data.TestDataPreload;
import org.sakaiproject.hierarchy.model.HierarchyNode;
import org.sakaiproject.hierarchy.repository.HierarchyNodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@ContextConfiguration(classes = {HierarchyTestConfiguration.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class HierarchyServiceTest {

    @Autowired private HierarchyNodeRepository nodeRepository;
    @Autowired private HierarchyServiceImpl hierarchyService;
    @Autowired private TestDataPreload tdp;

    /**
     * Builds an 8-node hierarchy mirroring the HIERARCHYA preloaded structure.
     * Returns [rootId, c1Id, c2Id, c3Id, gc1Id, gc2Id, gc3Id, gc4Id]
     */
    private String[] buildHierarchyA(String id) {
        HierarchyNode root = hierarchyService.createHierarchy(id);
        HierarchyNode c1 = hierarchyService.addNode(id, root.getId().toString());
        hierarchyService.saveNodeMetaData(c1.getId().toString(), null, null, TestDataPreload.PERM_TOKEN_1);
        HierarchyNode c2 = hierarchyService.addNode(id, root.getId().toString());
        hierarchyService.saveNodeMetaData(c2.getId().toString(), null, null, TestDataPreload.PERM_TOKEN_1);
        HierarchyNode c3 = hierarchyService.addNode(id, root.getId().toString());
        hierarchyService.saveNodeMetaData(c3.getId().toString(), null, null, TestDataPreload.PERM_TOKEN_2);
        HierarchyNode gc1 = hierarchyService.addNode(id, c2.getId().toString());
        hierarchyService.saveNodeMetaData(gc1.getId().toString(), null, null, TestDataPreload.PERM_TOKEN_1);
        HierarchyNode gc2 = hierarchyService.addNode(id, c3.getId().toString());
        HierarchyNode gc3 = hierarchyService.addNode(id, c3.getId().toString());
        HierarchyNode gc4 = hierarchyService.addNode(id, c3.getId().toString());
        return new String[]{root.getId().toString(), c1.getId().toString(), c2.getId().toString(),
                c3.getId().toString(), gc1.getId().toString(), gc2.getId().toString(),
                gc3.getId().toString(), gc4.getId().toString()};
    }

    /**
     * Builds a 3-node hierarchy mirroring the HIERARCHYB preloaded structure.
     * Returns [rootId, orphanId, childId]
     */
    private String[] buildHierarchyB(String id) {
        HierarchyNode root = hierarchyService.createHierarchy(id);
        HierarchyNode child = hierarchyService.addNode(id, root.getId().toString());
        hierarchyService.saveNodeMetaData(child.getId().toString(), null, null, TestDataPreload.PERM_TOKEN_1);
        HierarchyNode orphan = new HierarchyNode();
        orphan.setHierarchyId(id);
        orphan.setIsRootNode(Boolean.FALSE);
        orphan.setIsDisabled(Boolean.FALSE);
        nodeRepository.save(orphan);
        hierarchyService.addChildRelation(orphan.getId().toString(), child.getId().toString());
        return new String[]{root.getId().toString(), orphan.getId().toString(), child.getId().toString()};
    }

    private Set<String> directChildIds(String nodeId) {
        return hierarchyService.getChildNodes(nodeId, true).stream()
                .map(n -> n.getId().toString()).collect(Collectors.toSet());
    }

    private Set<String> directParentIds(String nodeId) {
        return hierarchyService.getParentNodes(nodeId, true).stream()
                .map(n -> n.getId().toString()).collect(Collectors.toSet());
    }

    @Test
    public void testValidTestData() {
        assertEquals(Long.valueOf(1), tdp.pNode1.getId());
        assertEquals(Long.valueOf(6), tdp.pNode6.getId());
        assertEquals(Long.valueOf(9), tdp.pNode9.getId());
    }

    @Test
    public void testCreateHierarchy() {
        HierarchyNode node = hierarchyService.createHierarchy("hierarchyC");
        assertNotNull(node);
        assertEquals("hierarchyC", node.getHierarchyId());
        assertTrue(directParentIds(node.getId().toString()).isEmpty());
        assertTrue(directChildIds(node.getId().toString()).isEmpty());

        try {
            hierarchyService.createHierarchy(TestDataPreload.HIERARCHYA);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }

        try {
            hierarchyService.createHierarchy("1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testSetHierarchyRootNode() {
        String idA = UUID.randomUUID().toString();
        String[] nodesA = buildHierarchyA(idA);

        String idB = UUID.randomUUID().toString();
        String[] nodesB = buildHierarchyB(idB);

        HierarchyNode node = hierarchyService.setHierarchyRootNode(idA, nodesA[0]);
        assertNotNull(node);
        assertEquals(idA, node.getHierarchyId());
        assertEquals(nodesA[0], node.getId().toString());

        HierarchyNode orphanNode = nodeRepository.findById(Long.parseLong(nodesB[1])).orElseThrow();
        HierarchyNode rootNode = nodeRepository.findById(Long.parseLong(nodesB[0])).orElseThrow();
        assertEquals(Boolean.FALSE, orphanNode.getIsRootNode());
        assertEquals(Boolean.TRUE, rootNode.getIsRootNode());
        node = hierarchyService.setHierarchyRootNode(idB, nodesB[1]);
        assertNotNull(node);
        assertEquals(idB, node.getHierarchyId());
        assertEquals(nodesB[1], node.getId().toString());

        try {
            hierarchyService.setHierarchyRootNode(idA, nodesA[2]);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }

        try {
            hierarchyService.setHierarchyRootNode(idB, nodesA[0]);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testDestroyHierarchy() {
        String id = UUID.randomUUID().toString();
        buildHierarchyA(id);

        hierarchyService.destroyHierarchy(id);
        long count = nodeRepository.countByHierarchyId(id);
        assertEquals(0, count);

        try {
            hierarchyService.destroyHierarchy(id);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testGetRootLevelNode() {
        HierarchyNode node;

        node = hierarchyService.getRootNode(TestDataPreload.HIERARCHYB);
        assertNotNull(node);
        assertEquals(tdp.node9, node);
        assertEquals(TestDataPreload.HIERARCHYB, node.getHierarchyId());

        node = hierarchyService.getRootNode(TestDataPreload.HIERARCHYA);
        assertNotNull(node);
        assertEquals(tdp.node1, node);
        assertEquals(TestDataPreload.HIERARCHYA, node.getHierarchyId());

        node = hierarchyService.getRootNode(TestDataPreload.INVALID_HIERARCHY);
        assertNull(node);
    }

    @Test
    public void testGetNodeById() {
        HierarchyNode node;

        node = hierarchyService.getNodeById(tdp.node4.getId().toString());
        assertNotNull(node);
        assertEquals(tdp.node4, node);
        assertEquals(tdp.node4.getId().toString(), node.getId().toString());

        node = hierarchyService.getNodeById(tdp.node6.getId().toString());
        assertNotNull(node);
        assertEquals(tdp.node6, node);
        assertEquals(tdp.node6.getId().toString(), node.getId().toString());

        try {
            node = hierarchyService.getNodeById(TestDataPreload.INVALID_NODE_ID);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testGetChildNodes() {
        Set<HierarchyNode> nodes;

        nodes = hierarchyService.getChildNodes(tdp.node1.getId().toString(), true);
        assertNotNull(nodes);
        assertEquals(3, nodes.size());
        assertTrue(nodes.contains(tdp.node2));
        assertTrue(nodes.contains(tdp.node3));
        assertTrue(nodes.contains(tdp.node4));

        nodes = hierarchyService.getChildNodes(tdp.node1.getId().toString(), false);
        assertNotNull(nodes);
        assertEquals(7, nodes.size());
        assertTrue(nodes.contains(tdp.node2));
        assertTrue(nodes.contains(tdp.node3));
        assertTrue(nodes.contains(tdp.node4));
        assertTrue(nodes.contains(tdp.node5));
        assertTrue(nodes.contains(tdp.node6));
        assertTrue(nodes.contains(tdp.node7));
        assertTrue(nodes.contains(tdp.node8));

        nodes = hierarchyService.getChildNodes(tdp.node4.getId().toString(), true);
        assertNotNull(nodes);
        assertEquals(3, nodes.size());
        assertTrue(nodes.contains(tdp.node6));
        assertTrue(nodes.contains(tdp.node7));
        assertTrue(nodes.contains(tdp.node8));

        nodes = hierarchyService.getChildNodes(tdp.node4.getId().toString(), false);
        assertNotNull(nodes);
        assertEquals(3, nodes.size());
        assertTrue(nodes.contains(tdp.node6));
        assertTrue(nodes.contains(tdp.node7));
        assertTrue(nodes.contains(tdp.node8));

        nodes = hierarchyService.getChildNodes(tdp.node5.getId().toString(), true);
        assertNotNull(nodes);
        assertEquals(0, nodes.size());

        nodes = hierarchyService.getChildNodes(tdp.node7.getId().toString(), true);
        assertNotNull(nodes);
        assertEquals(0, nodes.size());

        try {
            nodes = hierarchyService.getChildNodes(TestDataPreload.INVALID_NODE_ID, true);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testGetParentNodes() {
        Set<HierarchyNode> nodes;

        nodes = hierarchyService.getParentNodes(tdp.node7.getId().toString(), false);
        assertNotNull(nodes);
        assertEquals(2, nodes.size());
        assertTrue(nodes.contains(tdp.node1));
        assertTrue(nodes.contains(tdp.node4));

        nodes = hierarchyService.getParentNodes(tdp.node7.getId().toString(), true);
        assertNotNull(nodes);
        assertEquals(1, nodes.size());
        assertTrue(nodes.contains(tdp.node4));

        nodes = hierarchyService.getParentNodes(tdp.node5.getId().toString(), false);
        assertNotNull(nodes);
        assertEquals(2, nodes.size());
        assertTrue(nodes.contains(tdp.node1));
        assertTrue(nodes.contains(tdp.node3));

        nodes = hierarchyService.getParentNodes(tdp.node10.getId().toString(), false);
        assertNotNull(nodes);
        assertEquals(2, nodes.size());
        assertTrue(nodes.contains(tdp.node9));
        assertTrue(nodes.contains(tdp.node11));

        nodes = hierarchyService.getParentNodes(tdp.node10.getId().toString(), true);
        assertNotNull(nodes);
        assertEquals(2, nodes.size());
        assertTrue(nodes.contains(tdp.node9));
        assertTrue(nodes.contains(tdp.node11));

        nodes = hierarchyService.getParentNodes(tdp.node1.getId().toString(), true);
        assertNotNull(nodes);
        assertEquals(0, nodes.size());

        nodes = hierarchyService.getParentNodes(tdp.node9.getId().toString(), true);
        assertNotNull(nodes);
        assertEquals(0, nodes.size());

        try {
            nodes = hierarchyService.getParentNodes(TestDataPreload.INVALID_NODE_ID, true);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testGetParentNodeIds() {
        String node1Id = tdp.node1.getId().toString();
        String node5Id = tdp.node5.getId().toString();
        String node7Id = tdp.node7.getId().toString();
        String node10Id = tdp.node10.getId().toString();

        // batch resolves transitive ancestors per node in one call
        Map<String, Set<String>> result = hierarchyService.getParentNodeIds(
                new String[]{node5Id, node7Id, node10Id, node1Id});
        assertEquals(4, result.size());
        // node5 -> node3, node1
        assertEquals(Set.of(tdp.node3.getId().toString(), node1Id), result.get(node5Id));
        // node7 -> node4, node1
        assertEquals(Set.of(tdp.node4.getId().toString(), node1Id), result.get(node7Id));
        // node10 has two parents (node9, node11)
        assertEquals(Set.of(tdp.node9.getId().toString(), tdp.node11.getId().toString()), result.get(node10Id));
        // root node has no ancestors but is still present with an empty set
        assertEquals(Set.of(), result.get(node1Id));

        // empty input yields an empty map
        assertTrue(hierarchyService.getParentNodeIds(new String[]{}).isEmpty());
    }

    @Test
    public void testGetChildNodeIds() {
        String node1Id = tdp.node1.getId().toString();
        String node4Id = tdp.node4.getId().toString();
        String node5Id = tdp.node5.getId().toString();

        Map<String, Set<String>> result = hierarchyService.getChildNodeIds(
                new String[]{node1Id, node4Id, node5Id});
        assertEquals(3, result.size());
        // node1 -> all 7 descendants
        assertEquals(Set.of(tdp.node2.getId().toString(), tdp.node3.getId().toString(),
                node4Id, node5Id, tdp.node6.getId().toString(),
                tdp.node7.getId().toString(), tdp.node8.getId().toString()), result.get(node1Id));
        // node4 -> node6, node7, node8
        assertEquals(Set.of(tdp.node6.getId().toString(), tdp.node7.getId().toString(),
                tdp.node8.getId().toString()), result.get(node4Id));
        // leaf node has no descendants but is still present with an empty set
        assertEquals(Set.of(), result.get(node5Id));
    }

    @Test
    public void testGetDirectParentNodeIds() {
        String node5Id = tdp.node5.getId().toString();
        String node7Id = tdp.node7.getId().toString();
        String node10Id = tdp.node10.getId().toString();

        Map<String, Set<String>> result = hierarchyService.getDirectParentNodeIds(
                new String[]{node5Id, node7Id, node10Id});
        assertEquals(3, result.size());
        assertEquals(Set.of(tdp.node3.getId().toString()), result.get(node5Id));
        assertEquals(Set.of(tdp.node4.getId().toString()), result.get(node7Id));
        // node10 has two direct parents
        assertEquals(Set.of(tdp.node9.getId().toString(), tdp.node11.getId().toString()), result.get(node10Id));
    }

    @Test
    public void testGetDirectChildNodeIds() {
        String node1Id = tdp.node1.getId().toString();
        String node4Id = tdp.node4.getId().toString();
        String node5Id = tdp.node5.getId().toString();

        Map<String, Set<String>> result = hierarchyService.getDirectChildNodeIds(
                new String[]{node1Id, node4Id, node5Id});
        assertEquals(3, result.size());
        // node1 -> node2, node3, node4 (direct only)
        assertEquals(Set.of(tdp.node2.getId().toString(), tdp.node3.getId().toString(), node4Id),
                result.get(node1Id));
        assertEquals(Set.of(tdp.node6.getId().toString(), tdp.node7.getId().toString(),
                tdp.node8.getId().toString()), result.get(node4Id));
        // leaf has no children but is present with an empty set
        assertEquals(Set.of(), result.get(node5Id));
    }

    @Test
    public void testAddNode() {
        String idA = UUID.randomUUID().toString();
        String[] nodesA = buildHierarchyA(idA);

        HierarchyNode node = hierarchyService.addNode(idA, nodesA[1]);
        assertNotNull(node);
        String newNodeId = node.getId().toString();
        assertNotNull(newNodeId);
        Set<String> newNodeDirectParents = directParentIds(newNodeId);
        assertEquals(1, newNodeDirectParents.size());
        assertTrue(newNodeDirectParents.contains(nodesA[1]));
        Set<String> allParentIds = hierarchyService.getParentNodes(newNodeId, false)
                .stream().map(n -> n.getId().toString()).collect(Collectors.toSet());
        assertNotNull(allParentIds);
        assertEquals(2, allParentIds.size());
        assertTrue(allParentIds.contains(nodesA[1]));
        assertTrue(allParentIds.contains(nodesA[0]));
        assertTrue(directChildIds(newNodeId).isEmpty());
        assertTrue(hierarchyService.getChildNodes(newNodeId, false).isEmpty());

        node = hierarchyService.getNodeById(nodesA[1]);
        assertNotNull(node);
        assertEquals(nodesA[1], node.getId().toString());
        Set<String> c1DirectChildIds = directChildIds(nodesA[1]);
        assertEquals(1, c1DirectChildIds.size());
        assertTrue(c1DirectChildIds.contains(newNodeId));
        Set<String> c1AllChildIds = hierarchyService.getChildNodes(nodesA[1], false)
                .stream().map(n -> n.getId().toString()).collect(Collectors.toSet());
        assertNotNull(c1AllChildIds);
        assertEquals(1, c1AllChildIds.size());
        assertTrue(c1AllChildIds.contains(newNodeId));

        node = hierarchyService.getNodeById(nodesA[0]);
        assertNotNull(node);
        assertEquals(nodesA[0], node.getId().toString());
        Set<String> rootDirectChildIds = directChildIds(nodesA[0]);
        assertEquals(3, rootDirectChildIds.size());
        assertTrue(rootDirectChildIds.contains(nodesA[1]));
        assertTrue(rootDirectChildIds.contains(nodesA[2]));
        assertTrue(rootDirectChildIds.contains(nodesA[3]));
        Set<String> rootAllChildIds = hierarchyService.getChildNodes(nodesA[0], false)
                .stream().map(n -> n.getId().toString()).collect(Collectors.toSet());
        assertNotNull(rootAllChildIds);
        assertEquals(8, rootAllChildIds.size());
        assertTrue(rootAllChildIds.contains(newNodeId));
        assertTrue(rootAllChildIds.contains(nodesA[1]));
        assertTrue(rootAllChildIds.contains(nodesA[2]));
        assertTrue(rootAllChildIds.contains(nodesA[3]));
        assertTrue(rootAllChildIds.contains(nodesA[4]));
        assertTrue(rootAllChildIds.contains(nodesA[5]));
        assertTrue(rootAllChildIds.contains(nodesA[6]));
        assertTrue(rootAllChildIds.contains(nodesA[7]));

        String idB = UUID.randomUUID().toString();
        String[] nodesB = buildHierarchyB(idB);

        node = hierarchyService.addNode(idB, nodesB[2]);
        assertNotNull(node);
        newNodeId = node.getId().toString();
        assertNotNull(newNodeId);
        Set<String> newNodeBDirectParents = directParentIds(newNodeId);
        assertEquals(1, newNodeBDirectParents.size());
        assertTrue(newNodeBDirectParents.contains(nodesB[2]));
        Set<String> newNodeAllParents = hierarchyService.getParentNodes(newNodeId, false)
                .stream().map(n -> n.getId().toString()).collect(Collectors.toSet());
        assertNotNull(newNodeAllParents);
        assertEquals(3, newNodeAllParents.size());
        assertTrue(newNodeAllParents.contains(nodesB[2]));
        assertTrue(newNodeAllParents.contains(nodesB[0]));
        assertTrue(newNodeAllParents.contains(nodesB[1]));
        assertTrue(directChildIds(newNodeId).isEmpty());
        assertTrue(hierarchyService.getChildNodes(newNodeId, false).isEmpty());

        node = hierarchyService.getNodeById(nodesB[2]);
        assertNotNull(node);
        assertEquals(nodesB[2], node.getId().toString());
        Set<String> childBDirectChildIds = directChildIds(nodesB[2]);
        assertEquals(1, childBDirectChildIds.size());
        assertTrue(childBDirectChildIds.contains(newNodeId));
        Set<String> childAllChildIds = hierarchyService.getChildNodes(nodesB[2], false)
                .stream().map(n -> n.getId().toString()).collect(Collectors.toSet());
        assertNotNull(childAllChildIds);
        assertEquals(1, childAllChildIds.size());
        assertTrue(childAllChildIds.contains(newNodeId));

        node = hierarchyService.getNodeById(nodesB[0]);
        assertNotNull(node);
        assertEquals(nodesB[0], node.getId().toString());
        Set<String> rootBDirectChildIds = directChildIds(nodesB[0]);
        assertEquals(1, rootBDirectChildIds.size());
        assertTrue(rootBDirectChildIds.contains(nodesB[2]));
        Set<String> rootBAllChildIds = hierarchyService.getChildNodes(nodesB[0], false)
                .stream().map(n -> n.getId().toString()).collect(Collectors.toSet());
        assertNotNull(rootBAllChildIds);
        assertEquals(2, rootBAllChildIds.size());
        assertTrue(rootBAllChildIds.contains(newNodeId));
        assertTrue(rootBAllChildIds.contains(nodesB[2]));

        node = hierarchyService.getNodeById(nodesB[1]);
        assertNotNull(node);
        assertEquals(nodesB[1], node.getId().toString());
        Set<String> orphanBDirectChildIds = directChildIds(nodesB[1]);
        assertEquals(1, orphanBDirectChildIds.size());
        assertTrue(orphanBDirectChildIds.contains(nodesB[2]));
        Set<String> orphanAllChildIds = hierarchyService.getChildNodes(nodesB[1], false)
                .stream().map(n -> n.getId().toString()).collect(Collectors.toSet());
        assertNotNull(orphanAllChildIds);
        assertEquals(2, orphanAllChildIds.size());
        assertTrue(orphanAllChildIds.contains(newNodeId));
        assertTrue(orphanAllChildIds.contains(nodesB[2]));

        node = hierarchyService.addNode(idA, nodesA[2]);
        assertNotNull(node);
        newNodeId = node.getId().toString();
        assertNotNull(newNodeId);
        Set<String> newNodeA2DirectParents = directParentIds(newNodeId);
        assertEquals(1, newNodeA2DirectParents.size());
        assertTrue(newNodeA2DirectParents.contains(nodesA[2]));
        Set<String> newNodeParents2 = hierarchyService.getParentNodes(newNodeId, false)
                .stream().map(n -> n.getId().toString()).collect(Collectors.toSet());
        assertNotNull(newNodeParents2);
        assertEquals(2, newNodeParents2.size());
        assertTrue(newNodeParents2.contains(nodesA[2]));
        assertTrue(newNodeParents2.contains(nodesA[0]));
        assertTrue(directChildIds(newNodeId).isEmpty());
        assertTrue(hierarchyService.getChildNodes(newNodeId, false).isEmpty());

        node = hierarchyService.getNodeById(nodesA[2]);
        assertNotNull(node);
        assertEquals(nodesA[2], node.getId().toString());
        Set<String> c2DirectChildIds = directChildIds(nodesA[2]);
        assertEquals(2, c2DirectChildIds.size());
        assertTrue(c2DirectChildIds.contains(newNodeId));
        assertTrue(c2DirectChildIds.contains(nodesA[4]));
        Set<String> c2AllChildIds = hierarchyService.getChildNodes(nodesA[2], false)
                .stream().map(n -> n.getId().toString()).collect(Collectors.toSet());
        assertNotNull(c2AllChildIds);
        assertEquals(2, c2AllChildIds.size());
        assertTrue(c2AllChildIds.contains(newNodeId));
        assertTrue(c2AllChildIds.contains(nodesA[4]));

        try {
            hierarchyService.addNode(idA, null);
            fail("Should have thrown exception");
        } catch (RuntimeException e) {
            assertNotNull(e);
        }

        try {
            hierarchyService.addNode(idA, TestDataPreload.INVALID_NODE_ID);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testRemoveNode() {
        String idA = UUID.randomUUID().toString();
        String[] nodesA = buildHierarchyA(idA);

        String idB = UUID.randomUUID().toString();
        String[] nodesB = buildHierarchyB(idB);

        HierarchyNode node = hierarchyService.removeNode(nodesA[7]);
        assertNotNull(node);
        Set<String> c3AfterRemove = directChildIds(nodesA[3]);
        assertEquals(2, c3AfterRemove.size());
        assertTrue(c3AfterRemove.contains(nodesA[5]));
        assertTrue(c3AfterRemove.contains(nodesA[6]));

        node = hierarchyService.getNodeById(nodesA[0]);
        assertNotNull(node);
        assertEquals(nodesA[0], node.getId().toString());
        Set<String> rootAfterFirstRemove = directChildIds(nodesA[0]);
        assertEquals(3, rootAfterFirstRemove.size());
        assertTrue(rootAfterFirstRemove.contains(nodesA[1]));
        assertTrue(rootAfterFirstRemove.contains(nodesA[2]));
        assertTrue(rootAfterFirstRemove.contains(nodesA[3]));
        Set<String> rootAllChildIds = hierarchyService.getChildNodes(nodesA[0], false)
                .stream().map(n -> n.getId().toString()).collect(Collectors.toSet());
        assertNotNull(rootAllChildIds);
        assertEquals(6, rootAllChildIds.size());
        assertTrue(rootAllChildIds.contains(nodesA[1]));
        assertTrue(rootAllChildIds.contains(nodesA[2]));
        assertTrue(rootAllChildIds.contains(nodesA[3]));
        assertTrue(rootAllChildIds.contains(nodesA[4]));
        assertTrue(rootAllChildIds.contains(nodesA[5]));
        assertTrue(rootAllChildIds.contains(nodesA[6]));

        node = hierarchyService.removeNode(nodesA[1]);
        assertNotNull(node);
        Set<String> rootAfterSecondRemove = directChildIds(nodesA[0]);
        assertEquals(2, rootAfterSecondRemove.size());
        Set<String> rootAllChildIds2 = hierarchyService.getChildNodes(nodesA[0], false)
                .stream().map(n -> n.getId().toString()).collect(Collectors.toSet());
        assertTrue(rootAllChildIds2.contains(nodesA[2]));
        assertTrue(rootAllChildIds2.contains(nodesA[3]));

        node = hierarchyService.getNodeById(nodesA[0]);
        assertNotNull(node);
        assertEquals(nodesA[0], node.getId().toString());
        Set<String> rootDirectAfterBothRemoves = directChildIds(nodesA[0]);
        assertEquals(2, rootDirectAfterBothRemoves.size());
        assertTrue(rootDirectAfterBothRemoves.contains(nodesA[2]));
        assertTrue(rootDirectAfterBothRemoves.contains(nodesA[3]));
        Set<String> rootAllChildIds3 = hierarchyService.getChildNodes(nodesA[0], false)
                .stream().map(n -> n.getId().toString()).collect(Collectors.toSet());
        assertNotNull(rootAllChildIds3);
        assertEquals(5, rootAllChildIds3.size());
        assertTrue(rootAllChildIds3.contains(nodesA[2]));
        assertTrue(rootAllChildIds3.contains(nodesA[3]));
        assertTrue(rootAllChildIds3.contains(nodesA[4]));
        assertTrue(rootAllChildIds3.contains(nodesA[5]));
        assertTrue(rootAllChildIds3.contains(nodesA[6]));

        try {
            hierarchyService.removeNode(nodesA[0]);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        try {
            hierarchyService.removeNode(nodesA[3]);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        try {
            hierarchyService.removeNode(nodesA[2]);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        try {
            hierarchyService.removeNode(nodesB[2]);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        try {
            hierarchyService.removeNode(TestDataPreload.INVALID_NODE_ID);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        try {
            hierarchyService.removeNode(null);
            fail("Should have thrown exception");
        } catch (NullPointerException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testSaveNodeMetaData() {
        String id = UUID.randomUUID().toString();
        HierarchyNode fresh = hierarchyService.createHierarchy(id);
        String targetId = fresh.getId().toString();

        HierarchyNode node = hierarchyService.saveNodeMetaData(targetId, "Node TWO", "this is a description!", "TOKEN2");
        assertNotNull(node);
        assertEquals(node.getId().toString(), targetId);
        assertEquals("Node TWO", node.getTitle());
        assertEquals("this is a description!", node.getDescription());
        assertEquals("TOKEN2", node.getPermToken());

        node = hierarchyService.saveNodeMetaData(targetId, null, "DESC", "");
        assertNotNull(node);
        assertEquals(node.getId().toString(), targetId);
        assertEquals("Node TWO", node.getTitle());
        assertEquals("DESC", node.getDescription());
        assertNull(node.getPermToken());

        node = hierarchyService.saveNodeMetaData(targetId, null, null, null);
        assertNotNull(node);
        assertEquals(node.getId().toString(), targetId);
        assertEquals("Node TWO", node.getTitle());
        assertEquals("DESC", node.getDescription());
        assertNull(node.getPermToken());

        node = hierarchyService.saveNodeMetaData(targetId, "", "", "");
        assertNotNull(node);
        assertEquals(node.getId().toString(), targetId);
        assertNull(node.getTitle());
        assertNull(node.getDescription());
        assertNull(node.getPermToken());

        try {
            hierarchyService.saveNodeMetaData(TestDataPreload.INVALID_NODE_ID, null, null, null);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        try {
            hierarchyService.saveNodeMetaData(null, null, null, null);
            fail("Should have thrown exception");
        } catch (NullPointerException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testAddChildRelation() {
        String id = UUID.randomUUID().toString();
        String[] nodes = buildHierarchyA(id);

        HierarchyNode node = hierarchyService.addChildRelation(nodes[1], nodes[5]);
        assertNotNull(node);
        Set<String> c1After = directChildIds(nodes[1]);
        assertEquals(1, c1After.size());
        assertTrue(c1After.contains(nodes[5]));
        Set<String> c1AllChildIds = hierarchyService.getChildNodes(nodes[1], false)
                .stream().map(n -> n.getId().toString()).collect(Collectors.toSet());
        assertNotNull(c1AllChildIds);
        assertEquals(1, c1AllChildIds.size());
        assertTrue(c1AllChildIds.contains(nodes[5]));

        hierarchyService.addChildRelation(nodes[2], nodes[6]);
        Set<String> c2After1 = directChildIds(nodes[2]);
        assertEquals(2, c2After1.size());
        assertTrue(c2After1.contains(nodes[4]));
        assertTrue(c2After1.contains(nodes[6]));

        hierarchyService.addChildRelation(nodes[2], nodes[4]);
        Set<String> c2After2 = directChildIds(nodes[2]);
        assertEquals(2, c2After2.size());
        assertTrue(c2After2.contains(nodes[4]));
        assertTrue(c2After2.contains(nodes[6]));

        hierarchyService.addChildRelation(nodes[3], nodes[6]);
        Set<String> c3After = directChildIds(nodes[3]);
        assertEquals(3, c3After.size());
        assertTrue(c3After.contains(nodes[5]));

        try {
            hierarchyService.addChildRelation(nodes[6], nodes[6]);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        try {
            hierarchyService.addChildRelation(nodes[6], nodes[3]);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        try {
            hierarchyService.addChildRelation(nodes[6], nodes[0]);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        try {
            hierarchyService.addChildRelation(nodes[4], nodes[2]);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        try {
            hierarchyService.addChildRelation(TestDataPreload.INVALID_NODE_ID, nodes[5]);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        try {
            hierarchyService.addChildRelation(nodes[1], TestDataPreload.INVALID_NODE_ID);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        try {
            hierarchyService.addChildRelation(null, nodes[5]);
            fail("Should have thrown exception");
        } catch (NullPointerException e) {
            assertNotNull(e.getMessage());
        }

        try {
            hierarchyService.addChildRelation(nodes[1], null);
            fail("Should have thrown exception");
        } catch (NullPointerException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testRemoveChildRelation() {
        String idA = UUID.randomUUID().toString();
        String[] nodesA = buildHierarchyA(idA);

        String idB = UUID.randomUUID().toString();
        String[] nodesB = buildHierarchyB(idB);

        hierarchyService.addChildRelation(nodesA[1], nodesA[5]);

        HierarchyNode node = hierarchyService.removeChildRelation(nodesB[1], nodesB[2]);
        assertNotNull(node);
        assertTrue(directChildIds(nodesB[1]).isEmpty());
        Set<String> orphanAllChildIds = hierarchyService.getChildNodes(nodesB[1], false)
                .stream().map(n -> n.getId().toString()).collect(Collectors.toSet());
        assertNotNull(orphanAllChildIds);
        assertEquals(0, orphanAllChildIds.size());

        node = hierarchyService.removeChildRelation(nodesA[3], nodesA[5]);
        assertNotNull(node);
        Set<String> c3AfterRemove = directChildIds(nodesA[3]);
        assertEquals(2, c3AfterRemove.size());
        assertTrue(c3AfterRemove.contains(nodesA[6]));
        assertTrue(c3AfterRemove.contains(nodesA[7]));
        Set<String> c3AllChildIds = hierarchyService.getChildNodes(nodesA[3], false)
                .stream().map(n -> n.getId().toString()).collect(Collectors.toSet());
        assertNotNull(c3AllChildIds);
        assertEquals(2, c3AllChildIds.size());
        assertTrue(c3AllChildIds.contains(nodesA[6]));
        assertTrue(c3AllChildIds.contains(nodesA[7]));

        hierarchyService.removeChildRelation(nodesA[2], nodesA[5]);
        hierarchyService.removeChildRelation(nodesA[2], nodesA[1]);

        try {
            hierarchyService.removeChildRelation(nodesA[1], nodesA[1]);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        try {
            hierarchyService.removeChildRelation(nodesA[0], nodesA[2]);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        try {
            hierarchyService.removeChildRelation(nodesA[2], nodesA[4]);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        try {
            hierarchyService.removeChildRelation(TestDataPreload.INVALID_NODE_ID, nodesA[5]);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        try {
            hierarchyService.removeChildRelation(nodesA[1], TestDataPreload.INVALID_NODE_ID);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        try {
            hierarchyService.removeChildRelation(null, nodesA[5]);
            fail("Should have thrown exception");
        } catch (NullPointerException e) {
            assertNotNull(e.getMessage());
        }

        try {
            hierarchyService.removeChildRelation(nodesA[1], null);
            fail("Should have thrown exception");
        } catch (NullPointerException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testGetNodesWithToken() {
        Set<String> nodeIds;

        nodeIds = hierarchyService.getNodesWithToken(TestDataPreload.HIERARCHYA, TestDataPreload.PERM_TOKEN_1);
        assertNotNull(nodeIds);
        assertEquals(3, nodeIds.size());
        assertTrue(nodeIds.contains(tdp.node2.getId().toString()));
        assertTrue(nodeIds.contains(tdp.node3.getId().toString()));
        assertTrue(nodeIds.contains(tdp.node5.getId().toString()));

        nodeIds = hierarchyService.getNodesWithToken(TestDataPreload.HIERARCHYB, TestDataPreload.PERM_TOKEN_1);
        assertNotNull(nodeIds);
        assertEquals(1, nodeIds.size());
        assertTrue(nodeIds.contains(tdp.node10.getId().toString()));

        nodeIds = hierarchyService.getNodesWithToken(TestDataPreload.HIERARCHYA, TestDataPreload.PERM_TOKEN_2);
        assertNotNull(nodeIds);
        assertEquals(1, nodeIds.size());
        assertTrue(nodeIds.contains(tdp.node4.getId().toString()));

        nodeIds = hierarchyService.getNodesWithToken(TestDataPreload.HIERARCHYA, TestDataPreload.INVALID_PERM_TOKEN);
        assertNotNull(nodeIds);
        assertEquals(0, nodeIds.size());

        try {
            hierarchyService.getNodesWithToken(TestDataPreload.INVALID_HIERARCHY, TestDataPreload.PERM_TOKEN_1);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        try {
            hierarchyService.getNodesWithToken(TestDataPreload.HIERARCHYA, null);
            fail("Should have thrown exception");
        } catch (NullPointerException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testGetNodesWithTokens() {
        Set<String> nodeIds;
        Map<String, Set<String>> tokenNodes;

        tokenNodes = hierarchyService.getNodesWithTokens(TestDataPreload.HIERARCHYA, new String[]{TestDataPreload.PERM_TOKEN_1, TestDataPreload.PERM_TOKEN_2});
        assertNotNull(tokenNodes);
        assertEquals(2, tokenNodes.size());
        nodeIds = tokenNodes.get(TestDataPreload.PERM_TOKEN_1);
        assertEquals(3, nodeIds.size());
        assertTrue(nodeIds.contains(tdp.node2.getId().toString()));
        assertTrue(nodeIds.contains(tdp.node3.getId().toString()));
        assertTrue(nodeIds.contains(tdp.node5.getId().toString()));
        nodeIds = tokenNodes.get(TestDataPreload.PERM_TOKEN_2);
        assertEquals(1, nodeIds.size());
        assertTrue(nodeIds.contains(tdp.node4.getId().toString()));

        tokenNodes = hierarchyService.getNodesWithTokens(TestDataPreload.HIERARCHYB, new String[]{TestDataPreload.PERM_TOKEN_1, TestDataPreload.PERM_TOKEN_2});
        assertNotNull(tokenNodes);
        assertEquals(2, tokenNodes.size());
        nodeIds = tokenNodes.get(TestDataPreload.PERM_TOKEN_1);
        assertEquals(1, nodeIds.size());
        assertTrue(nodeIds.contains(tdp.node10.getId().toString()));
        nodeIds = tokenNodes.get(TestDataPreload.PERM_TOKEN_2);
        assertEquals(0, nodeIds.size());

        tokenNodes = hierarchyService.getNodesWithTokens(TestDataPreload.HIERARCHYA, new String[]{TestDataPreload.INVALID_PERM_TOKEN});
        assertNotNull(tokenNodes);
        assertEquals(1, tokenNodes.size());
        nodeIds = tokenNodes.get(TestDataPreload.INVALID_PERM_TOKEN);
        assertEquals(0, nodeIds.size());

        try {
            hierarchyService.getNodesWithTokens(TestDataPreload.INVALID_HIERARCHY, new String[]{TestDataPreload.PERM_TOKEN_1});
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        try {
            hierarchyService.getNodesWithTokens(TestDataPreload.HIERARCHYA, null);
            fail("Should have thrown exception");
        } catch (NullPointerException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testCheckUserNodePerm() {
        assertFalse(hierarchyService.checkUserNodePerm(TestDataPreload.USER_ID, tdp.node1.getId().toString(), TestDataPreload.PERM_ONE));
        assertFalse(hierarchyService.checkUserNodePerm(TestDataPreload.USER_ID, tdp.node2.getId().toString(), TestDataPreload.PERM_ONE));
        assertFalse(hierarchyService.checkUserNodePerm(TestDataPreload.USER_ID, tdp.node3.getId().toString(), TestDataPreload.PERM_ONE));
        assertFalse(hierarchyService.checkUserNodePerm(TestDataPreload.USER_ID, tdp.node4.getId().toString(), TestDataPreload.PERM_ONE));
        assertFalse(hierarchyService.checkUserNodePerm(TestDataPreload.USER_ID, tdp.node5.getId().toString(), TestDataPreload.PERM_ONE));
        assertFalse(hierarchyService.checkUserNodePerm(TestDataPreload.USER_ID, tdp.node6.getId().toString(), TestDataPreload.PERM_ONE));
        assertFalse(hierarchyService.checkUserNodePerm(TestDataPreload.USER_ID, tdp.node7.getId().toString(), TestDataPreload.PERM_ONE));
        assertFalse(hierarchyService.checkUserNodePerm(TestDataPreload.USER_ID, tdp.node8.getId().toString(), TestDataPreload.PERM_ONE));
        assertFalse(hierarchyService.checkUserNodePerm(TestDataPreload.ACCESS_USER_ID, tdp.node1.getId().toString(), TestDataPreload.PERM_ONE));
        assertFalse(hierarchyService.checkUserNodePerm(TestDataPreload.ACCESS_USER_ID, tdp.node2.getId().toString(), TestDataPreload.PERM_ONE));
        assertFalse(hierarchyService.checkUserNodePerm(TestDataPreload.ACCESS_USER_ID, tdp.node3.getId().toString(), TestDataPreload.PERM_ONE));
        assertFalse(hierarchyService.checkUserNodePerm(TestDataPreload.ACCESS_USER_ID, tdp.node4.getId().toString(), TestDataPreload.PERM_ONE));
        assertTrue(hierarchyService.checkUserNodePerm(TestDataPreload.ACCESS_USER_ID, tdp.node5.getId().toString(), TestDataPreload.PERM_ONE));
        assertFalse(hierarchyService.checkUserNodePerm(TestDataPreload.ACCESS_USER_ID, tdp.node6.getId().toString(), TestDataPreload.PERM_ONE));
        assertTrue(hierarchyService.checkUserNodePerm(TestDataPreload.ACCESS_USER_ID, tdp.node7.getId().toString(), TestDataPreload.PERM_ONE));
        assertFalse(hierarchyService.checkUserNodePerm(TestDataPreload.ACCESS_USER_ID, tdp.node8.getId().toString(), TestDataPreload.PERM_ONE));
        assertFalse(hierarchyService.checkUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node1.getId().toString(), TestDataPreload.PERM_ONE));
        assertTrue(hierarchyService.checkUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node2.getId().toString(), TestDataPreload.PERM_ONE));
        assertFalse(hierarchyService.checkUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node3.getId().toString(), TestDataPreload.PERM_ONE));
        assertTrue(hierarchyService.checkUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node4.getId().toString(), TestDataPreload.PERM_ONE));
        assertFalse(hierarchyService.checkUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node5.getId().toString(), TestDataPreload.PERM_ONE));
        assertTrue(hierarchyService.checkUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node6.getId().toString(), TestDataPreload.PERM_ONE));
        assertTrue(hierarchyService.checkUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node7.getId().toString(), TestDataPreload.PERM_ONE));
        assertTrue(hierarchyService.checkUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node8.getId().toString(), TestDataPreload.PERM_ONE));
        assertFalse(hierarchyService.checkUserNodePerm(TestDataPreload.USER_ID, tdp.node1.getId().toString(), TestDataPreload.PERM_TWO));
        assertFalse(hierarchyService.checkUserNodePerm(TestDataPreload.USER_ID, tdp.node2.getId().toString(), TestDataPreload.PERM_TWO));
        assertTrue(hierarchyService.checkUserNodePerm(TestDataPreload.USER_ID, tdp.node3.getId().toString(), TestDataPreload.PERM_TWO));
        assertFalse(hierarchyService.checkUserNodePerm(TestDataPreload.USER_ID, tdp.node4.getId().toString(), TestDataPreload.PERM_TWO));
        assertFalse(hierarchyService.checkUserNodePerm(TestDataPreload.USER_ID, tdp.node5.getId().toString(), TestDataPreload.PERM_TWO));
        assertTrue(hierarchyService.checkUserNodePerm(TestDataPreload.USER_ID, tdp.node6.getId().toString(), TestDataPreload.PERM_TWO));
        assertFalse(hierarchyService.checkUserNodePerm(TestDataPreload.USER_ID, tdp.node7.getId().toString(), TestDataPreload.PERM_TWO));
        assertFalse(hierarchyService.checkUserNodePerm(TestDataPreload.USER_ID, tdp.node8.getId().toString(), TestDataPreload.PERM_TWO));
        assertFalse(hierarchyService.checkUserNodePerm(TestDataPreload.ACCESS_USER_ID, tdp.node1.getId().toString(), TestDataPreload.PERM_TWO));
        assertFalse(hierarchyService.checkUserNodePerm(TestDataPreload.ACCESS_USER_ID, tdp.node2.getId().toString(), TestDataPreload.PERM_TWO));
        assertFalse(hierarchyService.checkUserNodePerm(TestDataPreload.ACCESS_USER_ID, tdp.node3.getId().toString(), TestDataPreload.PERM_TWO));
        assertFalse(hierarchyService.checkUserNodePerm(TestDataPreload.ACCESS_USER_ID, tdp.node4.getId().toString(), TestDataPreload.PERM_TWO));
        assertFalse(hierarchyService.checkUserNodePerm(TestDataPreload.ACCESS_USER_ID, tdp.node5.getId().toString(), TestDataPreload.PERM_TWO));
        assertFalse(hierarchyService.checkUserNodePerm(TestDataPreload.ACCESS_USER_ID, tdp.node6.getId().toString(), TestDataPreload.PERM_TWO));
        assertFalse(hierarchyService.checkUserNodePerm(TestDataPreload.ACCESS_USER_ID, tdp.node7.getId().toString(), TestDataPreload.PERM_TWO));
        assertTrue(hierarchyService.checkUserNodePerm(TestDataPreload.ACCESS_USER_ID, tdp.node8.getId().toString(), TestDataPreload.PERM_TWO));
        assertFalse(hierarchyService.checkUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node1.getId().toString(), TestDataPreload.PERM_TWO));
        assertTrue(hierarchyService.checkUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node2.getId().toString(), TestDataPreload.PERM_TWO));
        assertTrue(hierarchyService.checkUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node3.getId().toString(), TestDataPreload.PERM_TWO));
        assertFalse(hierarchyService.checkUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node4.getId().toString(), TestDataPreload.PERM_TWO));
        assertTrue(hierarchyService.checkUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node5.getId().toString(), TestDataPreload.PERM_TWO));
        assertFalse(hierarchyService.checkUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node6.getId().toString(), TestDataPreload.PERM_TWO));
        assertFalse(hierarchyService.checkUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node7.getId().toString(), TestDataPreload.PERM_TWO));
        assertFalse(hierarchyService.checkUserNodePerm(TestDataPreload.MAINT_USER_ID, tdp.node8.getId().toString(), TestDataPreload.PERM_TWO));

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

    @Test
    public void testGetNodesForUserPerm() {
        Set<HierarchyNode> nodes;

        nodes = hierarchyService.getNodesForUserPerm(TestDataPreload.USER_ID, TestDataPreload.PERM_ONE);
        assertNotNull(nodes);
        assertEquals(0, nodes.size());

        nodes = hierarchyService.getNodesForUserPerm(TestDataPreload.USER_ID, TestDataPreload.PERM_TWO);
        assertNotNull(nodes);
        assertEquals(2, nodes.size());
        assertTrue(nodes.contains(tdp.node3));
        assertTrue(nodes.contains(tdp.node6));

        nodes = hierarchyService.getNodesForUserPerm(TestDataPreload.ACCESS_USER_ID, TestDataPreload.PERM_ONE);
        assertNotNull(nodes);
        assertEquals(2, nodes.size());
        assertTrue(nodes.contains(tdp.node5));
        assertTrue(nodes.contains(tdp.node7));

        nodes = hierarchyService.getNodesForUserPerm(TestDataPreload.ACCESS_USER_ID, TestDataPreload.PERM_TWO);
        assertNotNull(nodes);
        assertEquals(1, nodes.size());
        assertTrue(nodes.contains(tdp.node8));

        nodes = hierarchyService.getNodesForUserPerm(TestDataPreload.MAINT_USER_ID, TestDataPreload.PERM_ONE);
        assertNotNull(nodes);
        assertEquals(5, nodes.size());
        assertTrue(nodes.contains(tdp.node2));
        assertTrue(nodes.contains(tdp.node4));
        assertTrue(nodes.contains(tdp.node6));
        assertTrue(nodes.contains(tdp.node7));
        assertTrue(nodes.contains(tdp.node8));

        nodes = hierarchyService.getNodesForUserPerm(TestDataPreload.MAINT_USER_ID, TestDataPreload.PERM_TWO);
        assertNotNull(nodes);
        assertEquals(3, nodes.size());
        assertTrue(nodes.contains(tdp.node2));
        assertTrue(nodes.contains(tdp.node3));
        assertTrue(nodes.contains(tdp.node5));

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

    @Test
    public void testGetUserIdsForNodesPerm() {
        Set<String> userIds;

        userIds = hierarchyService.getUserIdsForNodesPerm(new String[]{tdp.node1.getId().toString()}, TestDataPreload.PERM_ONE);
        assertNotNull(userIds);
        assertEquals(0, userIds.size());

        userIds = hierarchyService.getUserIdsForNodesPerm(new String[]{tdp.node2.getId().toString()}, TestDataPreload.PERM_ONE);
        assertNotNull(userIds);
        assertEquals(1, userIds.size());
        assertTrue(userIds.contains(TestDataPreload.MAINT_USER_ID));

        userIds = hierarchyService.getUserIdsForNodesPerm(new String[]{tdp.node3.getId().toString()}, TestDataPreload.PERM_ONE);
        assertNotNull(userIds);
        assertEquals(0, userIds.size());

        userIds = hierarchyService.getUserIdsForNodesPerm(new String[]{tdp.node4.getId().toString()}, TestDataPreload.PERM_ONE);
        assertNotNull(userIds);
        assertEquals(1, userIds.size());
        assertTrue(userIds.contains(TestDataPreload.MAINT_USER_ID));

        userIds = hierarchyService.getUserIdsForNodesPerm(new String[]{tdp.node5.getId().toString()}, TestDataPreload.PERM_ONE);
        assertNotNull(userIds);
        assertEquals(1, userIds.size());
        assertTrue(userIds.contains(TestDataPreload.ACCESS_USER_ID));

        userIds = hierarchyService.getUserIdsForNodesPerm(new String[]{tdp.node6.getId().toString()}, TestDataPreload.PERM_ONE);
        assertNotNull(userIds);
        assertEquals(1, userIds.size());
        assertTrue(userIds.contains(TestDataPreload.MAINT_USER_ID));

        userIds = hierarchyService.getUserIdsForNodesPerm(new String[]{tdp.node7.getId().toString()}, TestDataPreload.PERM_ONE);
        assertNotNull(userIds);
        assertEquals(2, userIds.size());
        assertTrue(userIds.contains(TestDataPreload.MAINT_USER_ID));
        assertTrue(userIds.contains(TestDataPreload.ACCESS_USER_ID));

        userIds = hierarchyService.getUserIdsForNodesPerm(new String[]{tdp.node8.getId().toString()}, TestDataPreload.PERM_ONE);
        assertNotNull(userIds);
        assertEquals(1, userIds.size());
        assertTrue(userIds.contains(TestDataPreload.MAINT_USER_ID));

        userIds = hierarchyService.getUserIdsForNodesPerm(new String[]{tdp.node1.getId().toString()}, TestDataPreload.PERM_TWO);
        assertNotNull(userIds);
        assertEquals(0, userIds.size());

        userIds = hierarchyService.getUserIdsForNodesPerm(new String[]{tdp.node2.getId().toString()}, TestDataPreload.PERM_TWO);
        assertNotNull(userIds);
        assertEquals(1, userIds.size());
        assertTrue(userIds.contains(TestDataPreload.MAINT_USER_ID));

        userIds = hierarchyService.getUserIdsForNodesPerm(new String[]{tdp.node3.getId().toString()}, TestDataPreload.PERM_TWO);
        assertNotNull(userIds);
        assertEquals(2, userIds.size());
        assertTrue(userIds.contains(TestDataPreload.MAINT_USER_ID));
        assertTrue(userIds.contains(TestDataPreload.USER_ID));

        userIds = hierarchyService.getUserIdsForNodesPerm(new String[]{tdp.node4.getId().toString()}, TestDataPreload.PERM_TWO);
        assertNotNull(userIds);
        assertEquals(0, userIds.size());

        userIds = hierarchyService.getUserIdsForNodesPerm(new String[]{tdp.node5.getId().toString()}, TestDataPreload.PERM_TWO);
        assertNotNull(userIds);
        assertEquals(1, userIds.size());
        assertTrue(userIds.contains(TestDataPreload.MAINT_USER_ID));

        userIds = hierarchyService.getUserIdsForNodesPerm(new String[]{tdp.node6.getId().toString()}, TestDataPreload.PERM_TWO);
        assertNotNull(userIds);
        assertEquals(1, userIds.size());
        assertTrue(userIds.contains(TestDataPreload.USER_ID));

        userIds = hierarchyService.getUserIdsForNodesPerm(new String[]{tdp.node7.getId().toString()}, TestDataPreload.PERM_TWO);
        assertNotNull(userIds);
        assertEquals(0, userIds.size());

        userIds = hierarchyService.getUserIdsForNodesPerm(new String[]{tdp.node8.getId().toString()}, TestDataPreload.PERM_TWO);
        assertNotNull(userIds);
        assertEquals(1, userIds.size());
        assertTrue(userIds.contains(TestDataPreload.ACCESS_USER_ID));
    }

    @Test
    public void testGetUserIdsForNodesPermMultiple() {
        Set<String> userIds;

        userIds = hierarchyService.getUserIdsForNodesPerm(new String[]{tdp.node2.getId().toString(), tdp.node3.getId().toString(), tdp.node4.getId().toString(), tdp.node5.getId().toString()}, TestDataPreload.PERM_ONE);
        assertNotNull(userIds);
        assertEquals(2, userIds.size());
        assertTrue(userIds.contains(TestDataPreload.ACCESS_USER_ID));
        assertTrue(userIds.contains(TestDataPreload.MAINT_USER_ID));

        userIds = hierarchyService.getUserIdsForNodesPerm(new String[]{tdp.node2.getId().toString(), tdp.node3.getId().toString(), tdp.node4.getId().toString(), tdp.node5.getId().toString()}, TestDataPreload.PERM_TWO);
        assertNotNull(userIds);
        assertEquals(2, userIds.size());
        assertTrue(userIds.contains(TestDataPreload.USER_ID));
        assertTrue(userIds.contains(TestDataPreload.MAINT_USER_ID));
    }

    @Test
    public void testGetUserIdsForNodesPermInvalids() {
        Set<String> userIds;

        userIds = hierarchyService.getUserIdsForNodesPerm(new String[]{}, TestDataPreload.PERM_ONE);
        assertNotNull(userIds);
        assertEquals(0, userIds.size());

        userIds = hierarchyService.getUserIdsForNodesPerm(new String[]{}, TestDataPreload.PERM_TWO);
        assertNotNull(userIds);
        assertEquals(0, userIds.size());

        try {
            hierarchyService.getUserIdsForNodesPerm(null, "XXXXXXXXX");
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        try {
            hierarchyService.getUserIdsForNodesPerm(new String[]{"XXXXXXXX"}, null);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testGetPermsForUserNodes() {
        Set<String> perms;

        perms = hierarchyService.getPermsForUserNodes(TestDataPreload.MAINT_USER_ID, new String[]{tdp.node1.getId().toString()});
        assertNotNull(perms);
        assertEquals(0, perms.size());

        perms = hierarchyService.getPermsForUserNodes(TestDataPreload.MAINT_USER_ID, new String[]{tdp.node2.getId().toString()});
        assertNotNull(perms);
        assertEquals(2, perms.size());
        assertTrue(perms.contains(TestDataPreload.PERM_ONE));
        assertTrue(perms.contains(TestDataPreload.PERM_TWO));

        perms = hierarchyService.getPermsForUserNodes(TestDataPreload.MAINT_USER_ID, new String[]{tdp.node3.getId().toString()});
        assertNotNull(perms);
        assertEquals(1, perms.size());
        assertTrue(perms.contains(TestDataPreload.PERM_TWO));

        perms = hierarchyService.getPermsForUserNodes(TestDataPreload.MAINT_USER_ID, new String[]{tdp.node4.getId().toString()});
        assertNotNull(perms);
        assertEquals(1, perms.size());
        assertTrue(perms.contains(TestDataPreload.PERM_ONE));

        perms = hierarchyService.getPermsForUserNodes(TestDataPreload.MAINT_USER_ID, new String[]{tdp.node5.getId().toString()});
        assertNotNull(perms);
        assertEquals(1, perms.size());
        assertTrue(perms.contains(TestDataPreload.PERM_TWO));

        perms = hierarchyService.getPermsForUserNodes(TestDataPreload.MAINT_USER_ID, new String[]{tdp.node6.getId().toString()});
        assertNotNull(perms);
        assertEquals(1, perms.size());
        assertTrue(perms.contains(TestDataPreload.PERM_ONE));

        perms = hierarchyService.getPermsForUserNodes(TestDataPreload.MAINT_USER_ID, new String[]{tdp.node7.getId().toString()});
        assertNotNull(perms);
        assertEquals(1, perms.size());
        assertTrue(perms.contains(TestDataPreload.PERM_ONE));

        perms = hierarchyService.getPermsForUserNodes(TestDataPreload.MAINT_USER_ID, new String[]{tdp.node8.getId().toString()});
        assertNotNull(perms);
        assertEquals(1, perms.size());
        assertTrue(perms.contains(TestDataPreload.PERM_ONE));

        perms = hierarchyService.getPermsForUserNodes(TestDataPreload.MAINT_USER_ID, new String[]{tdp.node3.getId().toString(), tdp.node4.getId().toString(), tdp.node5.getId().toString(), tdp.node6.getId().toString()});
        assertNotNull(perms);
        assertEquals(2, perms.size());
        assertTrue(perms.contains(TestDataPreload.PERM_ONE));
        assertTrue(perms.contains(TestDataPreload.PERM_TWO));

        perms = hierarchyService.getPermsForUserNodes(TestDataPreload.ACCESS_USER_ID, new String[]{});
        assertNotNull(perms);
        assertEquals(0, perms.size());

        perms = hierarchyService.getPermsForUserNodes(TestDataPreload.MAINT_USER_ID, new String[]{});
        assertNotNull(perms);
        assertEquals(0, perms.size());

        perms = hierarchyService.getPermsForUserNodes(TestDataPreload.USER_ID, new String[]{});
        assertNotNull(perms);
        assertEquals(0, perms.size());

        try {
            hierarchyService.getPermsForUserNodes(null, new String[]{"XXXXXXXX"});
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
        Map<String, Map<String, Set<String>>> map;

        map = hierarchyService.getUsersAndPermsForNodes(tdp.node3.getId().toString());
        assertNotNull(map);
        assertEquals(1, map.size());
        Map<String, Set<String>> userPerms = map.get(tdp.node3.getId().toString());
        assertEquals(2, userPerms.size());
        assertEquals(userPerms.get(TestDataPreload.USER_ID).size(), 1);
        assertEquals(userPerms.get(TestDataPreload.MAINT_USER_ID).size(), 1);

        try {
            hierarchyService.getUsersAndPermsForNodes((String[]) null);
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
        Map<String, Map<String, Set<String>>> map;

        map = hierarchyService.getNodesAndPermsForUser(TestDataPreload.ACCESS_USER_ID);
        assertNotNull(map);
        assertEquals(1, map.size());
        assertEquals(3, map.get(TestDataPreload.ACCESS_USER_ID).size());

        try {
            hierarchyService.getNodesAndPermsForUser((String[]) null);
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

    @Test
    public void testAssignUserNodePerm() {
        String id = UUID.randomUUID().toString();
        String[] nodes = buildHierarchyA(id);
        String maintUser = UUID.randomUUID().toString();
        String user = UUID.randomUUID().toString();

        hierarchyService.assignUserNodePerm(maintUser, nodes[1], TestDataPreload.PERM_ONE, false);
        hierarchyService.assignUserNodePerm(maintUser, nodes[3], TestDataPreload.PERM_ONE, false);
        hierarchyService.assignUserNodePerm(maintUser, nodes[5], TestDataPreload.PERM_ONE, false);
        hierarchyService.assignUserNodePerm(maintUser, nodes[6], TestDataPreload.PERM_ONE, false);
        hierarchyService.assignUserNodePerm(maintUser, nodes[7], TestDataPreload.PERM_ONE, false);

        Set<HierarchyNode> nodeSet = hierarchyService.getNodesForUserPerm(maintUser, TestDataPreload.PERM_ONE);
        assertEquals(5, nodeSet.size());

        hierarchyService.assignUserNodePerm(maintUser, nodes[1], TestDataPreload.PERM_ONE, false);
        nodeSet = hierarchyService.getNodesForUserPerm(maintUser, TestDataPreload.PERM_ONE);
        assertEquals(5, nodeSet.size());

        hierarchyService.assignUserNodePerm(maintUser, nodes[3], TestDataPreload.PERM_ONE, true);
        nodeSet = hierarchyService.getNodesForUserPerm(maintUser, TestDataPreload.PERM_ONE);
        assertEquals(5, nodeSet.size());

        hierarchyService.assignUserNodePerm(maintUser, nodes[2], TestDataPreload.PERM_ONE, false);
        nodeSet = hierarchyService.getNodesForUserPerm(maintUser, TestDataPreload.PERM_ONE);
        assertEquals(6, nodeSet.size());
        assertTrue(hierarchyService.checkUserNodePerm(maintUser, nodes[2], TestDataPreload.PERM_ONE));

        hierarchyService.assignUserNodePerm(maintUser, nodes[2], TestDataPreload.PERM_ONE, true);
        nodeSet = hierarchyService.getNodesForUserPerm(maintUser, TestDataPreload.PERM_ONE);
        assertEquals(7, nodeSet.size());
        assertTrue(hierarchyService.checkUserNodePerm(maintUser, nodes[4], TestDataPreload.PERM_ONE));

        nodeSet = hierarchyService.getNodesForUserPerm(user, TestDataPreload.PERM_THREE);
        assertEquals(0, nodeSet.size());

        hierarchyService.assignUserNodePerm(user, nodes[0], TestDataPreload.PERM_THREE, false);
        nodeSet = hierarchyService.getNodesForUserPerm(user, TestDataPreload.PERM_THREE);
        assertEquals(1, nodeSet.size());
        assertTrue(hierarchyService.checkUserNodePerm(user, nodes[0], TestDataPreload.PERM_THREE));

        hierarchyService.assignUserNodePerm(user, nodes[0], TestDataPreload.PERM_THREE, true);
        nodeSet = hierarchyService.getNodesForUserPerm(user, TestDataPreload.PERM_THREE);
        assertEquals(8, nodeSet.size());

        try {
            hierarchyService.assignUserNodePerm(null, nodes[2], TestDataPreload.PERM_ONE, false);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
        try {
            hierarchyService.assignUserNodePerm(maintUser, null, TestDataPreload.PERM_ONE, false);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
        try {
            hierarchyService.assignUserNodePerm(maintUser, nodes[2], null, false);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testRemoveUserNodePerm() {
        String id = UUID.randomUUID().toString();
        String[] nodes = buildHierarchyA(id);
        String maintUser = UUID.randomUUID().toString();

        hierarchyService.assignUserNodePerm(maintUser, nodes[1], TestDataPreload.PERM_ONE, false);
        hierarchyService.assignUserNodePerm(maintUser, nodes[3], TestDataPreload.PERM_ONE, false);
        hierarchyService.assignUserNodePerm(maintUser, nodes[5], TestDataPreload.PERM_ONE, false);
        hierarchyService.assignUserNodePerm(maintUser, nodes[6], TestDataPreload.PERM_ONE, false);
        hierarchyService.assignUserNodePerm(maintUser, nodes[7], TestDataPreload.PERM_ONE, false);

        Set<HierarchyNode> nodeSet = hierarchyService.getNodesForUserPerm(maintUser, TestDataPreload.PERM_ONE);
        assertEquals(5, nodeSet.size());

        hierarchyService.removeUserNodePerm(maintUser, nodes[1], TestDataPreload.PERM_ONE, false);
        nodeSet = hierarchyService.getNodesForUserPerm(maintUser, TestDataPreload.PERM_ONE);
        assertEquals(4, nodeSet.size());
        assertFalse(hierarchyService.checkUserNodePerm(maintUser, nodes[1], TestDataPreload.PERM_ONE));

        hierarchyService.removeUserNodePerm(maintUser, nodes[3], TestDataPreload.PERM_ONE, true);
        nodeSet = hierarchyService.getNodesForUserPerm(maintUser, TestDataPreload.PERM_ONE);
        assertEquals(0, nodeSet.size());
        assertFalse(hierarchyService.checkUserNodePerm(maintUser, nodes[3], TestDataPreload.PERM_ONE));

        hierarchyService.removeUserNodePerm(maintUser, nodes[1], "XXXXX", false);
        hierarchyService.removeUserNodePerm(maintUser, "XXXX", "XXXXX", false);
        hierarchyService.removeUserNodePerm("XXX", "XXXX", "XXXXX", false);

        try {
            hierarchyService.removeUserNodePerm(null, nodes[2], TestDataPreload.PERM_ONE, false);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
        try {
            hierarchyService.removeUserNodePerm(maintUser, null, TestDataPreload.PERM_ONE, false);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
        try {
            hierarchyService.removeUserNodePerm(maintUser, nodes[2], null, false);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testSetNodeDisabled() {
        String id = UUID.randomUUID().toString();
        HierarchyNode fresh = hierarchyService.createHierarchy(id);
        String targetId = fresh.getId().toString();

        HierarchyNode node = hierarchyService.saveNodeMetaData(targetId, "Test Node", "desc", "TOKEN");
        assertNotNull(node);
        assertEquals(node.getId().toString(), targetId);
        assertEquals(node.getIsDisabled(), Boolean.FALSE);

        node = hierarchyService.setNodeDisabled(targetId, Boolean.TRUE);
        assertNotNull(node);
        assertEquals(node.getId().toString(), targetId);
        assertEquals(node.getIsDisabled(), Boolean.TRUE);

        node = hierarchyService.setNodeDisabled(targetId, Boolean.TRUE);
        assertNotNull(node);
        assertEquals(node.getId().toString(), targetId);
        assertEquals(node.getIsDisabled(), Boolean.TRUE);
    }

    @Test
    public void testGetNodesByTitles() {
        String id = "hierarchyTitles_" + UUID.randomUUID().toString().substring(0, 8);
        HierarchyNode root = hierarchyService.createHierarchy(id);

        HierarchyNode siteA = hierarchyService.addNode(id, root.getId().toString());
        hierarchyService.saveNodeMetaData(siteA.getId().toString(), "/site/aaa", null, null);
        // a second node carrying the same title - the result groups both ids under that title
        HierarchyNode siteAdup = hierarchyService.addNode(id, root.getId().toString());
        hierarchyService.saveNodeMetaData(siteAdup.getId().toString(), "/site/aaa", null, null);
        HierarchyNode siteB = hierarchyService.addNode(id, root.getId().toString());
        hierarchyService.saveNodeMetaData(siteB.getId().toString(), "/site/bbb", null, null);
        // disabled node with a matching title is excluded
        HierarchyNode siteC = hierarchyService.addNode(id, root.getId().toString());
        hierarchyService.saveNodeMetaData(siteC.getId().toString(), "/site/ccc", null, null);
        hierarchyService.setNodeDisabled(siteC.getId().toString(), Boolean.TRUE);

        Map<String, List<String>> result = hierarchyService.getNodesByTitles(id,
                new String[]{"/site/aaa", "/site/bbb", "/site/ccc", "/site/zzz"});

        assertEquals(2, result.size());
        assertEquals(Set.of(siteA.getId().toString(), siteAdup.getId().toString()),
                Set.copyOf(result.get("/site/aaa")));
        assertEquals(List.of(siteB.getId().toString()), result.get("/site/bbb"));
        assertFalse("disabled node must be excluded", result.containsKey("/site/ccc"));
        assertFalse("unmatched title must be absent", result.containsKey("/site/zzz"));

        // empty / null titles yield an empty map
        assertTrue(hierarchyService.getNodesByTitles(id, new String[]{}).isEmpty());
        assertTrue(hierarchyService.getNodesByTitles(id, null).isEmpty());
    }

    @Test
    public void testGetEmptyNonSiteNodes() {
        String id = "hierarchyEmpty_" + UUID.randomUUID().toString().substring(0, 8);
        HierarchyNode root = hierarchyService.createHierarchy(id);

        // a non-site node that still has a child -> not a leaf, excluded
        HierarchyNode department = hierarchyService.addNode(id, root.getId().toString());
        hierarchyService.saveNodeMetaData(department.getId().toString(), "Department", null, null);
        HierarchyNode siteLeaf = hierarchyService.addNode(id, department.getId().toString());
        hierarchyService.saveNodeMetaData(siteLeaf.getId().toString(), "/site/xyz", null, null);
        // a childless non-site node -> the one we expect back
        HierarchyNode emptyLeaf = hierarchyService.addNode(id, root.getId().toString());
        hierarchyService.saveNodeMetaData(emptyLeaf.getId().toString(), "EmptySubject", null, null);
        // a childless non-site node that is disabled -> excluded
        HierarchyNode disabledLeaf = hierarchyService.addNode(id, root.getId().toString());
        hierarchyService.saveNodeMetaData(disabledLeaf.getId().toString(), "DisabledSubject", null, null);
        hierarchyService.setNodeDisabled(disabledLeaf.getId().toString(), Boolean.TRUE);

        List<String> emptyNodes = hierarchyService.getEmptyNonSiteNodes(id);

        assertEquals(List.of(emptyLeaf.getId().toString()), emptyNodes);
    }

    @Test
    public void testGetNodePermsForUser() {
        // ACCESS_USER_ID holds: node5->PERM_ONE, node7->PERM_ONE, node8->PERM_TWO
        Map<String, Set<String>> perms = hierarchyService.getNodePermsForUser(
                TestDataPreload.ACCESS_USER_ID,
                new String[]{tdp.node5.getId().toString(), tdp.node7.getId().toString(), tdp.node8.getId().toString()});
        assertEquals(3, perms.size());
        assertEquals(Set.of(TestDataPreload.PERM_ONE), perms.get(tdp.node5.getId().toString()));
        assertEquals(Set.of(TestDataPreload.PERM_ONE), perms.get(tdp.node7.getId().toString()));
        assertEquals(Set.of(TestDataPreload.PERM_TWO), perms.get(tdp.node8.getId().toString()));

        // a node the user has no permission on is absent from the map
        perms = hierarchyService.getNodePermsForUser(TestDataPreload.ACCESS_USER_ID,
                new String[]{tdp.node5.getId().toString(), tdp.node1.getId().toString()});
        assertEquals(1, perms.size());
        assertEquals(Set.of(TestDataPreload.PERM_ONE), perms.get(tdp.node5.getId().toString()));
        assertFalse(perms.containsKey(tdp.node1.getId().toString()));

        // multiple permissions on a single node are grouped together (MAINT holds both on node2)
        perms = hierarchyService.getNodePermsForUser(TestDataPreload.MAINT_USER_ID,
                new String[]{tdp.node2.getId().toString()});
        assertEquals(Set.of(TestDataPreload.PERM_ONE, TestDataPreload.PERM_TWO),
                perms.get(tdp.node2.getId().toString()));

        // empty nodeIds yields an empty map
        assertTrue(hierarchyService.getNodePermsForUser(TestDataPreload.MAINT_USER_ID, new String[]{}).isEmpty());

        try {
            hierarchyService.getNodePermsForUser(null, new String[]{"XXXX"});
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
        try {
            hierarchyService.getNodePermsForUser(TestDataPreload.MAINT_USER_ID, null);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testGetUserIdsForPerms() {
        // getUserIdsForPerms is a global query across all nodes/hierarchies, so this test uses
        // permission names and user ids unique to itself to stay isolated from other tests' grants.
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String permA = "perm.a." + suffix;
        String permB = "perm.b." + suffix;
        String userAlpha = "alpha-" + suffix;
        String userBeta = "beta-" + suffix;

        String id = "hierarchyPerms_" + suffix;
        HierarchyNode root = hierarchyService.createHierarchy(id);
        HierarchyNode node = hierarchyService.addNode(id, root.getId().toString());
        String nodeId = node.getId().toString();

        hierarchyService.assignUserNodePerm(userAlpha, nodeId, permA, false);
        hierarchyService.assignUserNodePerm(userBeta, nodeId, permA, false);
        hierarchyService.assignUserNodePerm(userBeta, nodeId, permB, false);

        // permA is held by both users, permB only by beta
        assertEquals(Set.of(userAlpha, userBeta), hierarchyService.getUserIdsForPerms(permA));
        assertEquals(Set.of(userBeta), hierarchyService.getUserIdsForPerms(permB));

        // the union across multiple permissions is returned, distinctly
        assertEquals(Set.of(userAlpha, userBeta), hierarchyService.getUserIdsForPerms(permA, permB));

        // unknown permission, no permissions, and null all yield an empty set
        assertTrue(hierarchyService.getUserIdsForPerms("no.such.permission." + suffix).isEmpty());
        assertTrue(hierarchyService.getUserIdsForPerms().isEmpty());
        assertTrue(hierarchyService.getUserIdsForPerms((String[]) null).isEmpty());
    }
}
