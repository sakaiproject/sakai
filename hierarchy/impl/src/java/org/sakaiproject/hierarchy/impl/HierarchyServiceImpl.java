/**
 * Copyright (c) 2007-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational
* Community License, Version 2.0 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of the
* License at:
*
* http://opensource.org/licenses/ecl2.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.hierarchy.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.db.api.SqlService;
import org.springframework.transaction.annotation.Transactional;
import org.sakaiproject.hierarchy.HierarchyService;
import org.sakaiproject.hierarchy.api.repository.HierarchyNodeMetaDataRepository;
import org.sakaiproject.hierarchy.api.repository.HierarchyNodePermissionRepository;
import org.sakaiproject.hierarchy.api.repository.HierarchyPersistentNodeRepository;
import org.sakaiproject.hierarchy.dao.model.HierarchyNodeMetaData;
import org.sakaiproject.hierarchy.dao.model.HierarchyNodePermission;
import org.sakaiproject.hierarchy.dao.model.HierarchyPersistentNode;
import org.sakaiproject.hierarchy.impl.utils.HierarchyImplUtils;
import org.sakaiproject.hierarchy.model.HierarchyNode;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;

/**
 * The default implementation of the Hierarchy interface
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
@Slf4j
@Transactional(readOnly = true)
public class HierarchyServiceImpl implements HierarchyService {

    private static int ORACLE_IN_CLAUSE_SIZE_LIMIT = 1000;
    private boolean oracle = false;

    private HierarchyNodeMetaDataRepository nodeMetaRepository;
    private HierarchyPersistentNodeRepository nodeRepository;
    private HierarchyNodePermissionRepository permissionRepository;

    public void setNodeMetaRepository(HierarchyNodeMetaDataRepository nodeMetaRepository) {
        this.nodeMetaRepository = nodeMetaRepository;
    }

    public void setNodeRepository(HierarchyPersistentNodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }

    public void setPermissionRepository(HierarchyNodePermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    private SqlService sqlService;

    public void setSqlService(SqlService sqlService) {
        this.sqlService = sqlService;
    }
    
    private MemoryService memoryService;
    public void setMemoryService(MemoryService memoryService){
    	this.memoryService = memoryService;
    }
    
    private Cache cache;
    public void setCache(Cache cache){
        this.cache = cache;
    }
    private final String CACHE_NAME = "org.sakaiproject.hierarchy.cache";


    // private SessionManager sessionManager;
    // public void setSessionManager(SessionManager sessionManager) {
    // this.sessionManager = sessionManager;
    // }

    public void init() {
        log.info("init");

        if(sqlService != null && "oracle".equalsIgnoreCase(sqlService.getVendor())){
            this.oracle = true;
        }

        // no runtime DB fixups; schema migration should enforce invariants
        
        cache = memoryService.getCache(CACHE_NAME);
    }

    @Transactional
    public HierarchyNode createHierarchy(String hierarchyId) {
        if (hierarchyId.length() < 1 || hierarchyId.length() > 250) {
            throw new IllegalArgumentException("Invalid hierarchyId (" + hierarchyId
                    + "): length must be 1 to 250 chars");
        }

        long count = nodeMetaRepository.countByHierarchyId(hierarchyId);
        if (count > 0) {
            throw new IllegalArgumentException("Invalid hierarchyId (" + hierarchyId
                    + "): this id is already in use, you must use a unique id when creating a new hierarchy");
        }

        HierarchyPersistentNode pNode = new HierarchyPersistentNode(); // no children or parents to
        // start
        HierarchyNodeMetaData metaData = new HierarchyNodeMetaData(pNode, hierarchyId, Boolean.TRUE, null); // getCurrentUserId());
        saveNodeAndMetaData(pNode, metaData);

        return HierarchyImplUtils.makeNode(pNode, metaData);
    }

    @Transactional
    public HierarchyNode setHierarchyRootNode(String hierarchyId, String nodeId) {
        HierarchyNodeMetaData metaData = getNodeMeta(nodeId);
        HierarchyNodeMetaData rootMetaData = getRootNodeMetaByHierarchy(hierarchyId);

        Set<HierarchyNodeMetaData> entities = new HashSet<HierarchyNodeMetaData>();

        if (rootMetaData != null) {
            if (metaData.getId().equals(rootMetaData.getId())) {
                // this node is already the root node
                return HierarchyImplUtils.makeNode(metaData);
            } else if (!metaData.getHierarchyId().equals(rootMetaData.getHierarchyId())) {
                throw new IllegalArgumentException("Cannot move a node from one hierarchy ("
                        + metaData.getHierarchyId() + ") to another (" + hierarchyId
                        + ") and replace the root node, this could orphan nodes");
            }
            rootMetaData.setIsRootNode(Boolean.FALSE);
            entities.add(metaData);
        }

        if (metaData.getNode().getParentIds() != null) {
            throw new IllegalArgumentException("Cannot assign a node (" + nodeId
                    + ") to the hierarchy rootNode when it has parents");
        }

        metaData.setIsRootNode(Boolean.TRUE);
        entities.add(metaData);

        nodeMetaRepository.saveAll(entities);
        return HierarchyImplUtils.makeNode(metaData);
    }

    @SuppressWarnings("rawtypes")
    @Transactional
    public void destroyHierarchy(String hierarchyId) {
        List<HierarchyNodeMetaData> l = nodeMetaRepository.findByHierarchyId(hierarchyId);
        if (l.isEmpty()) {
            throw new IllegalArgumentException("Could not find hierarchy to remove with the following id: "
                    + hierarchyId);
        }

        Set<HierarchyPersistentNode> nodes = new HashSet<HierarchyPersistentNode>();
        Set<HierarchyNodeMetaData> nodesMetaData = new HashSet<HierarchyNodeMetaData>();
        List<String> nodeIdStrings = new ArrayList<>();
        for (int i = 0; i < l.size(); i++) {
            HierarchyNodeMetaData nmd = (HierarchyNodeMetaData) l.get(i);
            nodesMetaData.add(nmd);
            nodes.add(nmd.getNode());
            nodeIdStrings.add(nmd.getNode().getId().toString());
        }

        // delete related permissions, then metadata, then nodes
        if (!nodeIdStrings.isEmpty()) {
            List<HierarchyNodePermission> perms = permissionRepository.findByNodeIdIn(nodeIdStrings);
            if (!perms.isEmpty()) {
                permissionRepository.deleteAll(new HashSet<>(perms));
            }
        }

        // delete metadata first, then nodes
        nodeMetaRepository.deleteAll(nodesMetaData);
        nodeRepository.deleteAll(nodes);
    }

    public HierarchyNode getRootNode(String hierarchyId) {
        HierarchyNodeMetaData metaData = getRootNodeMetaByHierarchy(hierarchyId);
        if (metaData == null) {
            return null;
        }
        return HierarchyImplUtils.makeNode(metaData);
    }

    public HierarchyNode getNodeById(String nodeId) {
        String cacheKey = "n"+nodeId;
        if(cache.containsKey(cacheKey)){
            log.debug("--- Fetching getNodeById record from cache for: {}", cacheKey);
            HierarchyNode ret = (HierarchyNode)cache.get(cacheKey);
            if(ret != null) {
                  return ret;
            }
        }
        HierarchyNodeMetaData metaData = getNodeMeta(nodeId);
        HierarchyNode ret = HierarchyImplUtils.makeNode(metaData);
        log.debug("+++ Adding getNodeById record to cache for: {}", cacheKey);
        cache.put(cacheKey, ret);
        return ret;
    }


    public Map<String, HierarchyNode> getNodesByIds(String[] nodeIds) {
        List<HierarchyNodeMetaData> nodeMetas = getNodeMetas(nodeIds);
        Map<String, HierarchyNode> m = new HashMap<String, HierarchyNode>();
        for (HierarchyNodeMetaData metaData : nodeMetas) {
            HierarchyNode node = HierarchyImplUtils.makeNode(metaData);
            m.put(node.id, node);
        }
        return m;
    }

    public Set<HierarchyNode> getChildNodes(String nodeId, boolean directOnly) {
      String cacheKey = "cn"+nodeId;
      if(cache.containsKey(cacheKey)){
            log.debug("--- Fetching ChildNodes record from cache for: {}", cacheKey);
            Set<HierarchyNode> set = (Set<HierarchyNode>)cache.get(cacheKey);
            if(set != null) {
                  return set;
            }
        }
      
        Set<HierarchyNode> children = new HashSet<HierarchyNode>();

        HierarchyNodeMetaData parentMetaData = getNodeMeta(nodeId);
        String childIdString = null;
        if (directOnly) {
            childIdString = parentMetaData.getNode().getDirectChildIds();
        } else {
            childIdString = parentMetaData.getNode().getChildIds();
        }

        if (childIdString == null) {
            log.debug("+++ Adding Empty ChildNodes record to cache for: {}", cacheKey);
            cache.put(cacheKey, children);
            return children;
        }

        Set<String> childrenIds = HierarchyImplUtils.makeNodeIdSet(childIdString);
        List<HierarchyNodeMetaData> childNodeMetas = getNodeMetas(childrenIds);
        for (HierarchyNodeMetaData metaData : childNodeMetas) {
            children.add(HierarchyImplUtils.makeNode(metaData));
        }
        log.debug("+++ Adding ChildNodes record to cache for: {}", cacheKey);
        cache.put(cacheKey, children);
        return children;
    }

    public Set<HierarchyNode> getParentNodes(String nodeId, boolean directOnly) {
        Set<HierarchyNode> parents = new HashSet<HierarchyNode>();

        HierarchyNodeMetaData parentMetaData = getNodeMeta(nodeId);
        String parentIdString = null;
        if (directOnly) {
            parentIdString = parentMetaData.getNode().getDirectParentIds();
        } else {
            parentIdString = parentMetaData.getNode().getParentIds();
        }

        if (parentIdString == null) {
            return parents;
        }

        Set<String> parentsIds = HierarchyImplUtils.makeNodeIdSet(parentIdString);
        List<HierarchyNodeMetaData> parentNodeMetas = getNodeMetas(parentsIds);
        for (HierarchyNodeMetaData metaData : parentNodeMetas) {
            parents.add(HierarchyImplUtils.makeNode(metaData));
        }


        return parents;
    }

    @Transactional
    public HierarchyNode addNode(String hierarchyId, String parentNodeId) {
        if (parentNodeId == null) {
            throw new RuntimeException("Setting parentNodeId to null is not yet supported");
        }

        // validate the parent node and hierarchy (this needs to be cached for sure)
        HierarchyNodeMetaData parentNodeMeta = getNodeMeta(parentNodeId);
        if (parentNodeMeta == null) {
            throw new IllegalArgumentException("Invalid parent node id, cannot find node with id: "
                    + parentNodeId);
        }
        if (!parentNodeMeta.getHierarchyId().equals(hierarchyId)) {
            throw new IllegalArgumentException("Invalid hierarchy id, cannot find node (" + parentNodeId
                    + ") in this hierarchy: " + hierarchyId);
        }

        // get the set of all nodes above the new node (these will have to be updated)
        Set<String> parentNodeIds = HierarchyImplUtils.makeNodeIdSet(parentNodeMeta.getNode().getParentIds());
        parentNodeIds.add(parentNodeId);

        // create the new node and assign the new parents from our parent
        HierarchyPersistentNode pNode = new HierarchyPersistentNode(HierarchyImplUtils
                .makeSingleEncodedNodeIdString(parentNodeId), HierarchyImplUtils
                .makeEncodedNodeIdString(parentNodeIds));
        HierarchyNodeMetaData metaData = new HierarchyNodeMetaData(pNode, hierarchyId, Boolean.FALSE, null); // getCurrentUserId());
        // save this new node (perhaps we should be saving all of these in one massive update?) -AZ
        saveNodeAndMetaData(pNode, metaData);
        String newNodeId = pNode.getId().toString();

        // update all the links in the tree for this new node
        List<HierarchyPersistentNode> pNodesList = getNodes(parentNodeIds);
        Set<HierarchyPersistentNode> pNodes = new HashSet<HierarchyPersistentNode>();
        for (HierarchyPersistentNode node : pNodesList) {
            if (node.getId().toString().equals(parentNodeId)) {
                // special case for our parent, update direct children
                node.setDirectChildIds(
                        HierarchyImplUtils.addSingleNodeIdToEncodedString(
                                node.getDirectChildIds(), newNodeId));
            }

            // update the children for each node
            node.setChildIds(
                    HierarchyImplUtils.addSingleNodeIdToEncodedString(node.getChildIds(), newNodeId));

            // add to the set of node to be saved
            pNodes.add(node);

            // invalidate cache entry so the next get includes this new child
            cache.remove("cn"+node.getId().toString());
        }
        nodeRepository.saveAll(pNodes);

        return HierarchyImplUtils.makeNode(pNode, metaData);
    }

    @Transactional
    public HierarchyNode removeNode(String nodeId) {
        if (nodeId == null) {
            throw new NullPointerException("nodeId to remove cannot be null");
        }

        // validate the node
        HierarchyNodeMetaData metaData = getNodeMeta(nodeId);
        if (metaData == null) {
            throw new IllegalArgumentException("Invalid node id, cannot find node with id: " + nodeId);
        }
        if (metaData.getIsRootNode().booleanValue()) {
            throw new IllegalArgumentException("Cannot remove the root node (" + nodeId + "), "
                    + "you must remove the entire hierarchy (" + metaData.getHierarchyId()
                    + ") to remove this root node");
        }

        // get the set of all nodes above the current node (these will have to be updated)
        HierarchyNode currentNode = HierarchyImplUtils.makeNode(metaData);
        if (currentNode.childNodeIds.size() != 0) {
            throw new IllegalArgumentException("Cannot remove a node with children nodes, "
                    + "reduce the children on this node from " + currentNode.childNodeIds.size()
                    + " to 0 before attempting to remove it");
        }

        if (currentNode.directParentNodeIds.size() > 1) {
            throw new IllegalArgumentException("Cannot remove a node with multiple parents, "
                    + "reduce the parents on this node to 1 before attempting to remove it");
        }

        // get the "main" parent node
        String currentParentNodeId = getParentNodeId(currentNode);

        // update all the links in the tree for this removed node
        List<HierarchyPersistentNode> pNodesList = getNodes(currentNode.parentNodeIds);
        Set<HierarchyPersistentNode> pNodes = new HashSet<HierarchyPersistentNode>();
        for (HierarchyPersistentNode pNode : pNodesList) {
            if (pNode.getId().toString().equals(currentParentNodeId)) {
                // special case for our parent, update direct children
                Set<String> nodeChildren = HierarchyImplUtils.makeNodeIdSet(pNode.getDirectChildIds());
                nodeChildren.remove(nodeId);
                pNode.setDirectChildIds(HierarchyImplUtils.makeEncodedNodeIdString(nodeChildren));
            }

            // update the children for each node
            Set<String> nodeChildren = HierarchyImplUtils.makeNodeIdSet(pNode.getChildIds());
            nodeChildren.remove(nodeId);
            pNode.setChildIds(HierarchyImplUtils.makeEncodedNodeIdString(nodeChildren));

            // add to the set of nodes to be saved
            pNodes.add(pNode);

            // invalidate cache entry so the next get doesn't include this removed child
            cache.remove("cn"+pNode.getId().toString());
        }
        nodeRepository.saveAll(pNodes);

        return HierarchyImplUtils.makeNode(getNodeMeta(currentParentNodeId));
    }

    @Transactional
    public HierarchyNode saveNodeMetaData(String nodeId, String title, String description, String permToken) {
        if (nodeId == null) {
            throw new NullPointerException("nodeId to remove cannot be null");
        }

        // validate the node
        HierarchyNodeMetaData metaData = getNodeMeta(nodeId);
        if (metaData == null) {
            throw new IllegalArgumentException("Invalid node id, cannot find node with id: " + nodeId);
        }

        // update the node meta data
        if (title != null) {
            if (title.equals("")) {
                metaData.setTitle(null);
            } else {
                metaData.setTitle(title);
            }
        }
        if (description != null) {
            if (description.equals("")) {
                metaData.setDescription(null);
            } else {
                metaData.setDescription(description);
            }
        }
        if (permToken != null) {
            if (permToken.equals("")) {
                metaData.setPermToken(null);
            } else {
                metaData.setPermToken(permToken);
            }
        }

        // save the node meta data
        nodeMetaRepository.save(metaData);

        return HierarchyImplUtils.makeNode(metaData);
    }

    @Transactional
    public HierarchyNode setNodeDisabled(String nodeId, Boolean isDisabled) {

        if (nodeId == null) {
            throw new NullPointerException("nodeId cannot be null");
        }

        // validate the node
        HierarchyNodeMetaData metaData = getNodeMeta(nodeId);

        if (metaData == null) {
            throw new IllegalArgumentException("Invalid node id, cannot find node with id: " + nodeId);
        }

        // update the node's isDisabled setting
        if (isDisabled != null) {
            metaData.setIsDisabled(isDisabled);
        }

        // save the node meta data
        nodeMetaRepository.save(metaData);

        return HierarchyImplUtils.makeNode(metaData);

    }

    @Transactional
    public HierarchyNode addChildRelation(String nodeId, String childNodeId) {
        if (nodeId == null || childNodeId == null) {
            throw new NullPointerException("nodeId (" + nodeId + ") and childNodeId (" + childNodeId
                    + ") cannot be null");
        }

        if (nodeId.equals(childNodeId)) {
            throw new IllegalArgumentException("nodeId and childNodeId cannot be the same: " + nodeId);
        }

        HierarchyNodeMetaData metaData = getNodeMeta(nodeId);
        if (metaData == null) {
            throw new IllegalArgumentException("Invalid nodeId: " + nodeId);
        }

        HierarchyNodeMetaData addMetaData = getNodeMeta(childNodeId);
        if (addMetaData == null) {
            throw new IllegalArgumentException("Invalid childNodeId: " + childNodeId);
        }

        HierarchyNode currentNode = HierarchyImplUtils.makeNode(metaData);
        // only add this if it is not already in there
        if (!currentNode.directChildNodeIds.contains(childNodeId)) {
            // first check for a cycle
            if (currentNode.childNodeIds.contains(childNodeId)
                    || currentNode.parentNodeIds.contains(childNodeId)) {
                throw new IllegalArgumentException("Cannot add " + childNodeId + " as a child of " + nodeId
                        + " because it is already in the node tree directly above or below this node");
            }

            // now we go ahead and update this node and all the related nodes
            HierarchyNode addNode = HierarchyImplUtils.makeNode(addMetaData);
            Set<HierarchyPersistentNode> pNodes = new HashSet<HierarchyPersistentNode>();

            // update the current node
            metaData.getNode().setDirectChildIds(
                    HierarchyImplUtils.addSingleNodeIdToEncodedString(
                            metaData.getNode().getDirectChildIds(), childNodeId));
            metaData.getNode().setChildIds(
                    HierarchyImplUtils.addSingleNodeIdToEncodedString(
                            metaData.getNode().getChildIds(), childNodeId));
            pNodes.add(metaData.getNode());

            // update the add node
            addMetaData.getNode().setDirectParentIds(
                    HierarchyImplUtils.addSingleNodeIdToEncodedString(
                            addMetaData.getNode().getDirectParentIds(), nodeId));
            addMetaData.getNode().setParentIds(
                    HierarchyImplUtils.addSingleNodeIdToEncodedString(
                            addMetaData.getNode().getParentIds(),nodeId));
            pNodes.add(addMetaData.getNode());

            // update the parents of the current node (they have new children)
            List<HierarchyPersistentNode> pNodesList = getNodes(currentNode.parentNodeIds);
            Set<String> nodesToAdd = addNode.childNodeIds;
            nodesToAdd.add(addNode.id);
            for (HierarchyPersistentNode pNode : pNodesList) {
                // update the children for each node
                Set<String> nodeChildren = HierarchyImplUtils.makeNodeIdSet(pNode.getChildIds());
                nodeChildren.addAll(nodesToAdd);
                pNode.setChildIds(HierarchyImplUtils.makeEncodedNodeIdString(nodeChildren));

                // add to the set of nodes to be saved
                pNodes.add(pNode);
            }

            // update the children of the add node (they have new parants)
            pNodesList = getNodes(addNode.childNodeIds);
            nodesToAdd = currentNode.parentNodeIds;
            nodesToAdd.add(currentNode.id);
            for (HierarchyPersistentNode pNode : pNodesList) {
                // update the parents for each node
                Set<String> parents = HierarchyImplUtils.makeNodeIdSet(pNode.getParentIds());
                parents.addAll(nodesToAdd);
                pNode.setParentIds(HierarchyImplUtils.makeEncodedNodeIdString(parents));

                // add to the set of nodes to be saved
                pNodes.add(pNode);
            }

            nodeRepository.saveAll(pNodes);
        }

        return HierarchyImplUtils.makeNode(metaData);
    }

    @Transactional
    public HierarchyNode removeChildRelation(String nodeId, String childNodeId) {
        if (nodeId == null || childNodeId == null) {
            throw new NullPointerException("nodeId (" + nodeId + ") and childNodeId (" + childNodeId
                    + ") cannot be null");
        }

        if (nodeId.equals(childNodeId)) {
            throw new IllegalArgumentException("nodeId and childNodeId cannot be the same: " + nodeId);
        }

        HierarchyNodeMetaData metaData = getNodeMeta(nodeId);
        if (metaData == null) {
            throw new IllegalArgumentException("Invalid nodeId: " + nodeId);
        }

        HierarchyNodeMetaData removeMetaData = getNodeMeta(childNodeId);
        if (removeMetaData == null) {
            throw new IllegalArgumentException("Invalid childNodeId: " + childNodeId);
        }

        HierarchyNode currentNode = HierarchyImplUtils.makeNode(metaData);
        // only do something if this child is a direct child of this node
        if (currentNode.directChildNodeIds.contains(childNodeId)) {
            // first check for orphaning
            HierarchyNode removeNode = HierarchyImplUtils.makeNode(removeMetaData);
            if (removeNode.directParentNodeIds.size() <= 1) {
                throw new IllegalArgumentException("Cannot remove " + childNodeId + " as a child of " + nodeId
                        + " because it would orphan the child node, you need to use the remove method" +
                "if you want to remove a node or add this node as the child of another node first");
            }

            // now we go ahead and update this node and all the related nodes
            Set<HierarchyPersistentNode> pNodes = new HashSet<HierarchyPersistentNode>();
            Set<String> nodes = null;

            // update the current node
            nodes = HierarchyImplUtils.makeNodeIdSet(metaData.getNode().getChildIds());
            nodes.remove(childNodeId);
            metaData.getNode().setChildIds(HierarchyImplUtils.makeEncodedNodeIdString(nodes));
            nodes = HierarchyImplUtils.makeNodeIdSet(metaData.getNode().getDirectChildIds());
            nodes.remove(childNodeId);
            metaData.getNode().setDirectChildIds(HierarchyImplUtils.makeEncodedNodeIdString(nodes));
            pNodes.add(metaData.getNode());

            // update the remove node
            nodes = HierarchyImplUtils.makeNodeIdSet(removeMetaData.getNode().getParentIds());
            nodes.remove(nodeId);
            removeMetaData.getNode().setParentIds(HierarchyImplUtils.makeEncodedNodeIdString(nodes));
            nodes = HierarchyImplUtils.makeNodeIdSet(removeMetaData.getNode().getDirectParentIds());
            nodes.remove(nodeId);
            removeMetaData.getNode().setDirectParentIds(HierarchyImplUtils.makeEncodedNodeIdString(nodes));
            pNodes.add(removeMetaData.getNode());

            // update the parents of the current node (they have less children)
            List<HierarchyPersistentNode> pNodesList = getNodes(currentNode.parentNodeIds);
            Set<String> nodesToRemove = removeNode.childNodeIds;
            nodesToRemove.add(removeNode.id);
            for (HierarchyPersistentNode pNode : pNodesList) {
                // update the children for each node
                Set<String> children = HierarchyImplUtils.makeNodeIdSet(pNode.getChildIds());
                children.removeAll(nodesToRemove);
                // add back in all the children of the currentNode because we may have 
                // taken out part of the tree below where if it connects to the children of removeNode
                children.addAll(currentNode.childNodeIds);
                pNode.setChildIds(HierarchyImplUtils.makeEncodedNodeIdString(children));

                // add to the set of nodes to be saved
                pNodes.add(pNode);
            }

            // update the children of the remove node (they have lost parents)
            pNodesList = getNodes(removeNode.childNodeIds);
            nodesToRemove = currentNode.parentNodeIds;
            nodesToRemove.add(currentNode.id);
            for (HierarchyPersistentNode pNode : pNodesList) {
                // update the parents for each node
                Set<String> parents = HierarchyImplUtils.makeNodeIdSet(pNode.getParentIds());
                parents.removeAll(nodesToRemove);
                // add back in all the parents of the removeNode because we will have 
                // taken out part of the tree above where it reconnects on the way to the root
                parents.addAll(removeNode.parentNodeIds);
                pNode.setParentIds(HierarchyImplUtils.makeEncodedNodeIdString(parents));

                // add to the set of nodes to be saved
                pNodes.add(pNode);
            }

            nodeRepository.saveAll(pNodes);

        }

        return HierarchyImplUtils.makeNode(metaData);
    }

    @Transactional
    public HierarchyNode addParentRelation(String nodeId, String parentNodeId) {
        // TODO Not implemented yet - not sure we even want to allow this
        throw new RuntimeException("This method is not implemented yet");
    }

    @Transactional
    public HierarchyNode removeParentRelation(String nodeId, String parentNodeId) {
        // TODO Not implemented yet - not sure this is even a good idea
        throw new RuntimeException("This method is not implemented yet");
    }


    public Set<String> getNodesWithToken(String hierarchyId, String permToken) {
        if (permToken == null || permToken.equals("")) {
            throw new NullPointerException("permToken cannot be null or empty string");
        }

        List<HierarchyNodeMetaData> l = nodeMetaRepository.findByHierarchyId(hierarchyId);
        if (l.isEmpty()) {
            throw new IllegalArgumentException("Could not find hierarchy with the following id: "
                    + hierarchyId);
        }

        List<HierarchyNodeMetaData> nodeIdsList = nodeMetaRepository.findByHierarchyIdAndPermTokenOrdered(hierarchyId, permToken);

        Set<String> nodeIds = new TreeSet<String>();
        for (Iterator<HierarchyNodeMetaData> iter = nodeIdsList.iterator(); iter.hasNext();) {
            HierarchyNodeMetaData metaData = iter.next();
            nodeIds.add(metaData.getNode().getId().toString());
        }

        return nodeIds;
    }

    public Map<String, Set<String>> getNodesWithTokens(String hierarchyId, String[] permTokens) {
        // it would be better if this were more efficient...
        if (permTokens == null) {
            throw new NullPointerException("permTokens cannot be null");
        }

        Map<String, Set<String>> tokenNodes = new HashMap<String, Set<String>>();
        for (int i = 0; i < permTokens.length; i++) {
            Set<String> nodeIds = getNodesWithToken(hierarchyId, permTokens[i]);
            tokenNodes.put(permTokens[i], nodeIds);
        }

        return tokenNodes;
    }

    // PERMISSIONS

    @Transactional
    public void assignUserNodePerm(String userId, String nodeId, String hierarchyPermission, boolean cascade) {
        if (userId == null || "".equals(userId)
                || nodeId == null || "".equals(nodeId)
                || hierarchyPermission == null || "".equals(hierarchyPermission)) {
            throw new IllegalArgumentException("Invalid arguments to assignUserNodePerm, no arguments can be null or blank: userId="+userId+", nodeId="+nodeId+", hierarchyPermission="+hierarchyPermission);
        }
        HierarchyNodePermission nodePerm = permissionRepository.findByUserIdAndNodeIdAndPermission(userId, nodeId, hierarchyPermission);
        if (nodePerm == null) {
            // validate the nodeId
            Long nodeIdeNum;
            try {
                nodeIdeNum = new Long(nodeId);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Node id ("+nodeId+") provided is invalid, must be a valid identifier from an existing node");
            }
            // check it exists
            HierarchyPersistentNode pNode = nodeRepository.findById(nodeIdeNum).orElse(null);
            if (pNode == null) {
                throw new IllegalArgumentException("Node id ("+nodeId+") provided is invalid, node does not exist");
            }
            // create the perm
            permissionRepository.save( new HierarchyNodePermission(userId, nodeId, hierarchyPermission) );
        } else {
            // permission already set, do nothing
        }
        if (cascade) {
            // cascade the permission creation
            HierarchyNode node = getNodeById(nodeId);
            if (node != null 
                    && node.childNodeIds != null 
                    && node.childNodeIds.size() > 0) {
                List<String> nodeIdsList = new ArrayList<String>(node.childNodeIds);

                int i = 0;
                List<HierarchyNodePermission> nodePerms = new ArrayList<HierarchyNodePermission>();
                do{ 
                    int arraySize = nodeIdsList.size() - i;
                    if(oracle && arraySize > ORACLE_IN_CLAUSE_SIZE_LIMIT){
                        arraySize = ORACLE_IN_CLAUSE_SIZE_LIMIT;
                    }
                    // get all the permissions which are related to the nodes under this one
                    List<HierarchyNodePermission> nodePermsItteration = permissionRepository.findByUserIdAndPermissionAndNodeIds(userId, hierarchyPermission, nodeIdsList.subList(i, i + arraySize));
                    nodePerms.addAll(nodePermsItteration);
                    i += arraySize;
                }while(i < nodeIdsList.size());

                Set<HierarchyNodePermission> allPerms = new HashSet<HierarchyNodePermission>();
                if (nodePerms.size() == 0) {
                    // add all new ones
                    for (String childNodeId : node.childNodeIds) {
                        allPerms.add( new HierarchyNodePermission(userId, childNodeId, hierarchyPermission) );
                    }
                } else {
                    // only add the missing ones
                    Set<String> existingPermNodeIds = new HashSet<String>();
                    for (HierarchyNodePermission hierNodePerm : nodePerms) {
                        existingPermNodeIds.add( hierNodePerm.getNodeId() );
                    }
                    for (String childNodeId : node.childNodeIds) {
                        if (! existingPermNodeIds.contains(nodeId)) {
                            allPerms.add( new HierarchyNodePermission(userId, childNodeId, hierarchyPermission) );
                        }
                    }
                }
                if (nodePerms.size() == node.childNodeIds.size()
                        || allPerms.size() == 0) {
                    // nothing to do here, all permissions already exist or there are none to add
                } else {
                    // save the new permissions
                    permissionRepository.saveAll( new HashSet<HierarchyNodePermission>(allPerms) );
                }
            }
        }
    }

    @Transactional
    public void removeUserNodePerm(String userId, String nodeId, String hierarchyPermission, boolean cascade) {
        if (userId == null || "".equals(userId)
                || nodeId == null || "".equals(nodeId)
                || hierarchyPermission == null || "".equals(hierarchyPermission)) {
            throw new IllegalArgumentException("Invalid arguments to removeUserNodePerm, no arguments can be null or blank: userId="+userId+", nodeId="+nodeId+", hierarchyPermission="+hierarchyPermission);
        }
        if (! cascade) {
            // delete the current permission if it can be found
            HierarchyNodePermission nodePerm = permissionRepository.findByUserIdAndNodeIdAndPermission(userId, nodeId, hierarchyPermission);
            if (nodePerm == null) {
                // not found, nothing to do
            } else {
                permissionRepository.delete(nodePerm);
            }
        } else {
            // cascade the permission removal and delete current one as well
            HierarchyNode node = getNodeById(nodeId);
            if (node != null) {
                HashSet<String> nodeIdsSet = new HashSet<String>();
                nodeIdsSet.add(nodeId);
                // add in child nodes if there are any
                if (node.childNodeIds != null 
                        && node.childNodeIds.size() > 0) {
                    nodeIdsSet.addAll(node.childNodeIds);
                } 
                List<String> nodeIdsList = new ArrayList<String>(nodeIdsSet);
                int i = 0;
                do{
                    int arraySize = nodeIdsList.size() - i;
                    if(oracle && arraySize > ORACLE_IN_CLAUSE_SIZE_LIMIT){
                        arraySize = ORACLE_IN_CLAUSE_SIZE_LIMIT;
                    }
                    // get all the permissions which are related to the nodes under this one
                    List<HierarchyNodePermission> nodePerms = permissionRepository.findByUserIdAndPermissionAndNodeIds(userId, hierarchyPermission, nodeIdsList.subList(i, i + arraySize));
                    if (nodePerms.size() > 0) {
                        // delete all as one operation
                        permissionRepository.deleteAll( new HashSet<HierarchyNodePermission>(nodePerms) );
                    }
                    i += arraySize;
                }while(i < nodeIdsList.size());
            }
        }
    }

    public boolean checkUserNodePerm(String userId, String nodeId, String hierarchyPermission) {
        if (userId == null || "".equals(userId)
                || nodeId == null || "".equals(nodeId)
                || hierarchyPermission == null || "".equals(hierarchyPermission)) {
            throw new IllegalArgumentException("Invalid arguments to checkUserNodePerm, no arguments can be null or blank: userId="+userId+", nodeId="+nodeId+", hierarchyPermission="+hierarchyPermission);
        }
        boolean allowed = false;
        HierarchyNodePermission nodePerm = permissionRepository.findByUserIdAndNodeIdAndPermission(userId, nodeId, hierarchyPermission);
        if (nodePerm != null) {
            allowed = true;
        }
        return allowed;
    }

    public Set<HierarchyNode> getNodesForUserPerm(String userId, String hierarchyPermission) {
        if (userId == null || "".equals(userId)
                || hierarchyPermission == null || "".equals(hierarchyPermission)) {
            throw new IllegalArgumentException("Invalid arguments to getNodesForUserPerm, no arguments can be null or blank: userId="+userId+", hierarchyPermission="+hierarchyPermission);
        }
        Set<HierarchyNode> nodes = new HashSet<HierarchyNode>();
        List<HierarchyNodePermission> nodePerms = permissionRepository.findByUserIdAndPermission(userId, hierarchyPermission);
        Set<String> nodeIds = new HashSet<String>();
        for (HierarchyNodePermission nodePerm : nodePerms) {
            nodeIds.add( nodePerm.getNodeId() );
        }
        List<HierarchyNodeMetaData> nodeMetas = getNodeMetas(nodeIds);
        for (HierarchyNodeMetaData metaData : nodeMetas) {
            nodes.add( HierarchyImplUtils.makeNode(metaData) );
        }
        return nodes;
    }

    public Set<String> getUserIdsForNodesPerm(String[] nodeIds, String hierarchyPermission) {
        if (nodeIds == null 
                || hierarchyPermission == null || "".equals(hierarchyPermission)) {
            throw new IllegalArgumentException("Invalid arguments to getUserIdsForNodesPerm, no arguments can be null or blank: hierarchyPermission="+hierarchyPermission);
        }
        Set<String> userIds = new HashSet<String>();
        if (nodeIds.length > 0) {
            List<String> nodeIdsList = new ArrayList<String>(Arrays.asList(nodeIds));
            int i = 0;
            do{
                int arraySize = nodeIdsList.size() - i;
                if(oracle && arraySize > ORACLE_IN_CLAUSE_SIZE_LIMIT){
                    arraySize = ORACLE_IN_CLAUSE_SIZE_LIMIT;
                }
                // Filter by permission token for the specified nodes
                List<HierarchyNodePermission> nodePerms = permissionRepository.findByPermissionAndNodeIds(hierarchyPermission, nodeIdsList.subList(i, i + arraySize));
                for (HierarchyNodePermission nodePerm : nodePerms) {
                    userIds.add( nodePerm.getUserId() );
                }
                i += arraySize;
            }while(i < nodeIdsList.size());
        }
        return userIds;
    }

    public Set<String> getPermsForUserNodes(String userId, String[] nodeIds) {
        if (userId == null || "".equals(userId)
                || nodeIds == null ) {
            throw new IllegalArgumentException("Invalid arguments to getPermsForUserNodes, no arguments can be null or blank: userId="+userId);
        }
        Set<String> perms = new HashSet<String>();
        if (nodeIds.length > 0) {
            List<String> nodeIdsList = new ArrayList<String>(Arrays.asList(nodeIds));
            int i = 0;
            do{
                int arraySize = nodeIdsList.size() - i;
                if(oracle && arraySize > ORACLE_IN_CLAUSE_SIZE_LIMIT){
                    arraySize = ORACLE_IN_CLAUSE_SIZE_LIMIT;
                }
                List<HierarchyNodePermission> nodePerms = permissionRepository.findByUserIdAndNodeIds(userId, nodeIdsList.subList(i, i + arraySize));
                for (HierarchyNodePermission nodePerm : nodePerms) {
                    perms.add( nodePerm.getPermission() );
                }
                i += arraySize;
            }while(i < nodeIdsList.size());
        }
        return perms;
    }

    public Map<String, Map<String, Set<String>>> getUsersAndPermsForNodes(String... nodeIds) {
        if (nodeIds == null || nodeIds.length == 0) {
            throw new IllegalArgumentException("Invalid arguments to getUsersAndPermsForNodes, no arguments can be null or blank: nodeIds="+nodeIds);
        }
        Map<String, Map<String, Set<String>>> m = new HashMap<String, Map<String,Set<String>>>();
        for (String nodeId : nodeIds) {
            m.put(nodeId, new HashMap<String, Set<String>>());
        }
        List<HierarchyNodePermission> nodePerms = new ArrayList<HierarchyNodePermission>();
        List<String> nodeIdsList = new ArrayList<String>(Arrays.asList(nodeIds));
        int i = 0;
        do{
            int arraySize = nodeIdsList.size() - i;
            if(oracle && arraySize > ORACLE_IN_CLAUSE_SIZE_LIMIT){
                arraySize = ORACLE_IN_CLAUSE_SIZE_LIMIT;
            }
            List<HierarchyNodePermission> nodePermsItteration = permissionRepository.findByNodeIdIn(nodeIdsList.subList(i, i + arraySize));
            nodePerms.addAll(nodePermsItteration);
            i += arraySize;
        }while(i < nodeIdsList.size());

        // nodeId -> (map of userId -> Set(permission))
        for (HierarchyNodePermission nodePerm : nodePerms) {
            String nodeId = nodePerm.getNodeId();
            if (! m.containsKey(nodeId)) {
                continue; // this should not really happen but better safe than sorry
            }
            String userId = nodePerm.getUserId();
            if (! m.get(nodeId).containsKey(userId) ) {
                m.get(nodeId).put(userId, new HashSet<String>() );
            }
            m.get(nodeId).get(userId).add( nodePerm.getPermission() );
        }
        return m;
    }

    public Map<String, Map<String, Set<String>>> getNodesAndPermsForUser(String... userIds) {
        if (userIds == null || userIds.length == 0) {
            throw new IllegalArgumentException("Invalid arguments to getNodesAndPermsForUser, no arguments can be null or blank: userIds="+userIds);
        }
        Map<String, Map<String, Set<String>>> m = new HashMap<String, Map<String,Set<String>>>();
        for (String userId : userIds) {
            m.put(userId, new HashMap<String, Set<String>>());
        }
        List<String> userIdsList = new ArrayList<String>(Arrays.asList(userIds));
        int i = 0;
        List<HierarchyNodePermission> nodePerms = new ArrayList<HierarchyNodePermission>();
        do{ 
            int arraySize = userIdsList.size() - i;
            if(oracle && arraySize > ORACLE_IN_CLAUSE_SIZE_LIMIT){
                arraySize = ORACLE_IN_CLAUSE_SIZE_LIMIT;
            }
            List<HierarchyNodePermission> nodePermsItteration = permissionRepository.findByUserIdIn(userIdsList.subList(i, i + arraySize));
            nodePerms.addAll(nodePermsItteration);
            i += arraySize;
        }while(i < userIdsList.size());

        // userId -> (map of nodeId -> Set(permission))
        for (HierarchyNodePermission nodePerm : nodePerms) {
            String userId = nodePerm.getUserId();
            if (! m.containsKey(userId)) {
                continue; // this should not really happen but better safe than sorry
            }
            String nodeId = nodePerm.getNodeId();
            if (! m.get(userId).containsKey(nodeId) ) {
                m.get(userId).put(nodeId, new HashSet<String>() );
            }
            m.get(userId).get(nodeId).add( nodePerm.getPermission() );
        }
        return m;
    }

    // PRIVATE


    /**
     * Convenience method to save a node and metadata in one transaction
     * 
     * @param pNode
     * @param metaData
     */
    @SuppressWarnings("rawtypes")
    private void saveNodeAndMetaData(HierarchyPersistentNode pNode, HierarchyNodeMetaData metaData) {
        Set<HierarchyPersistentNode> pNodes = new HashSet<HierarchyPersistentNode>();
        pNodes.add(pNode);
        Set<HierarchyNodeMetaData> metaDatas = new HashSet<HierarchyNodeMetaData>();
        metaDatas.add(metaData);
        // Save both node and metadata
        nodeRepository.saveAll(pNodes);
        nodeMetaRepository.saveAll(metaDatas);
        /* NORMALLY the code below should not be needed, however, 
         * we are seeing weird cases where the line above fails to create the metadata
         * so the code below is meant to detect that case and correct it by saving
         * each separately and realigning the ids manually
         */
        if (metaData.getId() == null) {
            // something went wrong and we're not sure what so delete pNode
            if (pNode.getId() != null) {
                nodeRepository.delete(pNode);
            }
            throw new RuntimeException("Metadata didn't save, node was removed: "+pNode);
        } else if (pNode.getId() == null) {
            // something went wrong and we're not sure what so delete metadata
            if (metaData.getId() != null) {
                nodeMetaRepository.delete(metaData);
            }
            throw new RuntimeException("Metadata didn't save, metaData was removed: "+metaData);
        } else if (!metaData.getId().equals(pNode.getId())) {
            // the indexes are off... let's try to get them back in sync
            int i = 0;
            if (pNode.getId() > metaData.getId()) {
                while (i < 100 && metaData.getId() != null && pNode.getId() != metaData.getId()) {
                    // need to keep saving metaData until it's sequence has caught up
                    nodeMetaRepository.delete(metaData);
                    // set ID back to null to make it save with a new incremented ID
                    metaData.setId(null);
                    nodeMetaRepository.save(metaData);
                    i++;
                }
            } else {
                while (i < 100 && pNode.getId() != null && pNode.getId() != metaData.getId()) {
                    // need to keep saving node until it's sequence has caught up
                    nodeRepository.delete(pNode);
                    // set ID back to null to make it save with a new incremented ID
                    pNode.setId(null);
                    nodeRepository.save(pNode);
                    i++;
                }
            }
            if (pNode.getId() == null || metaData.getId() == null || pNode.getId() != metaData.getId()) {
                // ok we tried, it didn't work, so throw the exception
                throw new RuntimeException("Node ID: " + pNode.getId() + " doesn't match Metadata ID: " + metaData.getId());
            }
        }
    }

    /**
     * Fetch node data from storage
     * 
     * @param nodeId
     * @return a {@link HierarchyNodeMetaData} or null if not found
     */
    private HierarchyNodeMetaData getNodeMeta(String nodeId) {
        return nodeMetaRepository.findByNodeId(Long.valueOf(nodeId));
    }

    /**
     * Find the current root node
     * 
     * @param hierarchyId
     * @return the root {@link HierarchyNodeMetaData} of the hierarchy
     */
    private HierarchyNodeMetaData getRootNodeMetaByHierarchy(String hierarchyId) {
        HierarchyNodeMetaData root = nodeMetaRepository.findRootByHierarchyId(hierarchyId);
        if (root == null) {
            return null;
        }
        // safeguard: ensure uniqueness
        List<HierarchyNodeMetaData> l = nodeMetaRepository.findByHierarchyId(hierarchyId);
        long roots = l.stream().filter(m -> Boolean.TRUE.equals(m.getIsRootNode())).count();
        if (roots > 1) {
            throw new IllegalStateException("Invalid hierarchy state: more than one root node for hierarchyId: "
                    + hierarchyId);
        }
        return root;
    }

    /**
     * Get all nodes and meta data based on a set of nodeIds
     * 
     * @param nodeIds
     * @return
     */
    private List<HierarchyNodeMetaData> getNodeMetas(Set<String> nodeIds) {
        return getNodeMetas(nodeIds.toArray(new String[] {}));
    }

    private List<HierarchyNodeMetaData> getNodeMetas(String[] nodeIds) {
        List<HierarchyNodeMetaData> l = null;
        if (nodeIds == null || nodeIds.length == 0) {
            l = new ArrayList<HierarchyNodeMetaData>();
        } else {
            Long[] pNodeIds = new Long[nodeIds.length];
            for (int i = 0; i < nodeIds.length; i++) {
                pNodeIds[i] = new Long(nodeIds[i]);
            }
            l = new ArrayList<HierarchyNodeMetaData>();
            List<Long> nodeIdsList = new ArrayList<Long>(Arrays.asList(pNodeIds));
            int i = 0;
            do{ 
                int arraySize = nodeIdsList.size() - i;
                if(oracle && arraySize > ORACLE_IN_CLAUSE_SIZE_LIMIT){
                    arraySize = ORACLE_IN_CLAUSE_SIZE_LIMIT;
                }
                List<HierarchyNodeMetaData> lIterration = nodeMetaRepository.findByNodeIds(nodeIdsList.subList(i, i + arraySize));
                l.addAll(lIterration);
                i += arraySize;
            }while(i < nodeIdsList.size());
        }
        return l;
    }

    /**
     * Get all nodes only based on a set of nodeIds
     * 
     * @param nodeIds
     * @return
     */
    private List<HierarchyPersistentNode> getNodes(Set<String> nodeIds) {
        return getNodes(nodeIds.toArray(new String[] {}));
    }

    private List<HierarchyPersistentNode> getNodes(String[] nodeIds) {
        List<HierarchyPersistentNode> l = null;
        if (nodeIds == null || nodeIds.length == 0) {
            l = new ArrayList<HierarchyPersistentNode>();
        } else {
            Long[] pNodeIds = new Long[nodeIds.length];
            for (int i = 0; i < nodeIds.length; i++) {
                pNodeIds[i] = new Long(nodeIds[i]);
            }
            l = new ArrayList<HierarchyPersistentNode>();
            List<Long> nodeIdsList = new ArrayList<Long>(Arrays.asList(pNodeIds));
            int i = 0;
            do{ 
                int arraySize = nodeIdsList.size() - i;
                if(oracle && arraySize > ORACLE_IN_CLAUSE_SIZE_LIMIT){
                    arraySize = ORACLE_IN_CLAUSE_SIZE_LIMIT;
                }
                List<HierarchyPersistentNode> lIterration = nodeRepository.findByIdIn(nodeIdsList.subList(i, i + arraySize));
                l.addAll(lIterration);
                i += arraySize;
            }while(i < nodeIdsList.size());
        }
        return l;
    }

    /**
     * Find the direct parent node id for a node
     * @param node
     * @return the node if or null if none exists
     */
    private String getParentNodeId(HierarchyNode node) {
        String parentNodeId = null;
        if (node.directParentNodeIds != null &&
                node.directParentNodeIds.size() > 0) {
            parentNodeId = node.directParentNodeIds.iterator().next();
        }
        return parentNodeId;
    }

}
