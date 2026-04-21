package org.sakaiproject.hierarchy.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.hierarchy.HierarchyService;
import org.sakaiproject.hierarchy.model.HierarchyNode;
import org.sakaiproject.hierarchy.model.HierarchyNodePermission;
import org.sakaiproject.hierarchy.repository.HierarchyNodePermissionRepository;
import org.sakaiproject.hierarchy.repository.HierarchyNodeRepository;

import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
public class HierarchyServiceImpl implements HierarchyService {

    @Setter private HierarchyNodeRepository nodeRepository;
    @Setter private HierarchyNodePermissionRepository permissionRepository;

    public void init() {
        log.info("init");
    }

    public HierarchyNode createHierarchy(String hierarchyId) {
        if (StringUtils.isBlank(hierarchyId) || hierarchyId.length() > 250) {
            throw new IllegalArgumentException("Invalid hierarchyId ("
                    + hierarchyId
                    + "): length must be 1 to 250 chars");
        }

        long count = nodeRepository.countByHierarchyId(hierarchyId);
        if (count > 0) {
            throw new IllegalArgumentException("Invalid hierarchyId ("
                    + hierarchyId
                    + "): this id is already in use, you must use a unique id when creating a new hierarchy");
        }

        HierarchyNode node = new HierarchyNode();
        node.setHierarchyId(hierarchyId);
        node.setIsRootNode(Boolean.TRUE);
        node.setIsDisabled(Boolean.FALSE);
        return nodeRepository.save(node);
    }

    public HierarchyNode setHierarchyRootNode(String hierarchyId, String nodeId) {
        HierarchyNode node = getNode(nodeId);
        HierarchyNode rootNode = nodeRepository.findByHierarchyIdAndIsRootNode(hierarchyId, Boolean.TRUE).orElse(null);

        if (rootNode != null) {
            if (node.equals(rootNode)) {
                return node;
            } else if (!node.getHierarchyId().equals(rootNode.getHierarchyId())) {
                throw new IllegalArgumentException("Cannot move a node from one hierarchy ("
                        + node.getHierarchyId()
                        + ") to another ("
                        + hierarchyId
                        + ") and replace the root node, this could orphan nodes");
            }
            rootNode.setIsRootNode(Boolean.FALSE);
            nodeRepository.save(rootNode);
        }

        if (!node.getParents().isEmpty()) {
            throw new IllegalArgumentException("Cannot assign a node ("
                    + nodeId
                    + ") to the hierarchy rootNode when it has parents");
        }

        node.setIsRootNode(Boolean.TRUE);
        return nodeRepository.save(node);
    }

    public void destroyHierarchy(String hierarchyId) {
        List<HierarchyNode> nodes = nodeRepository.findByHierarchyId(hierarchyId);
        if (nodes.isEmpty()) {
            throw new IllegalArgumentException("Could not find hierarchy to remove with the following id: "
                    + hierarchyId);
        }
        List<Long> ids = nodes.stream().map(HierarchyNode::getId).collect(Collectors.toList());
        nodeRepository.deleteRelationsByNodeIds(ids);
        nodeRepository.deleteAll(nodes);
    }

    @Transactional(readOnly = true)
    public HierarchyNode getRootNode(String hierarchyId) {
        return nodeRepository.findByHierarchyIdAndIsRootNode(hierarchyId, Boolean.TRUE)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public HierarchyNode getNodeById(String nodeId) {
        return getNode(nodeId);
    }

    @Transactional(readOnly = true)
    public Map<String, HierarchyNode> getNodesByIds(String[] nodeIds) {
        List<Long> ids = Arrays.stream(nodeIds).map(Long::parseLong).collect(Collectors.toList());
        Map<String, HierarchyNode> m = new HashMap<>();
        for (HierarchyNode node : nodeRepository.findByIdIn(ids)) {
            m.put(node.getId().toString(), node);
        }
        return m;
    }

    @Transactional(readOnly = true)
    public Set<HierarchyNode> getChildNodes(String nodeId, boolean directOnly) {
        HierarchyNode parentNode = getNode(nodeId);
        if (parentNode == null) {
            throw new IllegalArgumentException("Invalid node id, cannot find node with id: " + nodeId);
        }
        if (directOnly) {
            return new HashSet<>(parentNode.getChildren());
        } else {
            return nodeRepository.findAllDescendants(parentNode.getId());
        }
    }

    @Transactional(readOnly = true)
    public Set<HierarchyNode> getParentNodes(String nodeId, boolean directOnly) {
        HierarchyNode childNode = getNode(nodeId);
        if (childNode == null) {
            throw new IllegalArgumentException("Invalid node id, cannot find node with id: " + nodeId);
        }
        if (directOnly) {
            return new HashSet<>(childNode.getParents());
        } else {
            return nodeRepository.findAllAncestors(childNode.getId());
        }
    }

    public HierarchyNode addNode(String hierarchyId, String parentNodeId) {
        if (parentNodeId == null) {
            throw new RuntimeException("Setting parentNodeId to null is not yet supported");
        }

        HierarchyNode parentNode = getNode(parentNodeId);
        if (parentNode == null) {
            throw new IllegalArgumentException("Invalid parent node id, cannot find node with id: "
                    + parentNodeId);
        }
        if (!parentNode.getHierarchyId().equals(hierarchyId)) {
            throw new IllegalArgumentException("Invalid hierarchy id, cannot find node ("
                    + parentNodeId
                    + ") in this hierarchy: "
                    + hierarchyId);
        }

        HierarchyNode node = new HierarchyNode();
        node.setHierarchyId(hierarchyId);
        node.setIsRootNode(Boolean.FALSE);
        node.setIsDisabled(Boolean.FALSE);
        node.getParents().add(parentNode);
        return nodeRepository.save(node);
    }

    public HierarchyNode removeNode(String nodeId) {
        if (nodeId == null) {
            throw new NullPointerException("nodeId to remove cannot be null");
        }

        HierarchyNode node = getNode(nodeId);
        if (node == null) {
            throw new IllegalArgumentException("Invalid node id, cannot find node with id: " + nodeId);
        }
        if (node.getIsRootNode()) {
            throw new IllegalArgumentException("Cannot remove the root node ("
                    + nodeId + "), "
                    + "you must remove the entire hierarchy ("
                    + node.getHierarchyId()
                    + ") to remove this root node");
        }

        if (!node.getChildren().isEmpty()) {
            throw new IllegalArgumentException("Cannot remove a node with children nodes, "
                    + "reduce the children on this node from "
                    + node.getChildren().size()
                    + " to 0 before attempting to remove it");
        }

        if (node.getParents().size() > 1) {
            throw new IllegalArgumentException("Cannot remove a node with multiple parents, "
                    + "reduce the parents on this node to 1 before attempting to remove it");
        }

        HierarchyNode parentNode = node.getParents().isEmpty() ? null : node.getParents().iterator().next();

        if (parentNode != null) {
            parentNode.getChildren().remove(node);
        }
        node.getParents().clear();
        nodeRepository.delete(node);

        return parentNode;
    }

    public HierarchyNode saveNodeMetaData(String nodeId, String title, String description, String permToken) {
        if (nodeId == null) {
            throw new NullPointerException("nodeId to remove cannot be null");
        }

        HierarchyNode node = getNode(nodeId);
        if (node == null) {
            throw new IllegalArgumentException("Invalid node id, cannot find node with id: " + nodeId);
        }

        if (title != null) {
            node.setTitle(title.isEmpty() ? null : title);
        }
        if (description != null) {
            node.setDescription(description.isEmpty() ? null : description);
        }
        if (permToken != null) {
            node.setPermToken(permToken.isEmpty() ? null : permToken);
        }

        return nodeRepository.save(node);
    }

    public HierarchyNode setNodeDisabled(String nodeId, Boolean isDisabled) {
        if (nodeId == null) {
            throw new NullPointerException("nodeId cannot be null");
        }

        HierarchyNode node = getNode(nodeId);
        if (node == null) {
            throw new IllegalArgumentException("Invalid node id, cannot find node with id: " + nodeId);
        }

        if (isDisabled != null) {
            node.setIsDisabled(isDisabled);
        }

        return nodeRepository.save(node);
    }

    public HierarchyNode addChildRelation(String nodeId, String childNodeId) {
        if (nodeId == null || childNodeId == null) {
            throw new NullPointerException("nodeId ("
                    + nodeId
                    + ") and childNodeId ("
                    + childNodeId
                    + ") cannot be null");
        }

        if (nodeId.equals(childNodeId)) {
            throw new IllegalArgumentException("nodeId and childNodeId cannot be the same: " + nodeId);
        }

        HierarchyNode pNode = getNode(nodeId);
        if (pNode == null) {
            throw new IllegalArgumentException("Invalid nodeId: " + nodeId);
        }

        HierarchyNode addPNode = getNode(childNodeId);
        if (addPNode == null) {
            throw new IllegalArgumentException("Invalid childNodeId: " + childNodeId);
        }

        if (!pNode.getChildren().contains(addPNode)) {
            Set<String> descendantIds = nodeRepository.findAllDescendants(pNode.getId())
                    .stream().map(n -> n.getId().toString()).collect(Collectors.toSet());
            Set<String> ancestorIds = nodeRepository.findAllAncestors(pNode.getId())
                    .stream().map(n -> n.getId().toString()).collect(Collectors.toSet());

            if (descendantIds.contains(childNodeId) || ancestorIds.contains(childNodeId)) {
                throw new IllegalArgumentException("Cannot add " + childNodeId + " as a child of " + nodeId
                        + " because it is already in the node tree directly above or below this node");
            }

            addPNode.getParents().add(pNode);
            pNode.getChildren().add(addPNode);
            pNode = nodeRepository.save(pNode);
        }

        return pNode;
    }

    public HierarchyNode removeChildRelation(String nodeId, String childNodeId) {
        if (nodeId == null || childNodeId == null) {
            throw new NullPointerException("nodeId ("
                    + nodeId
                    + ") and childNodeId ("
                    + childNodeId
                    + ") cannot be null");
        }

        if (nodeId.equals(childNodeId)) {
            throw new IllegalArgumentException("nodeId and childNodeId cannot be the same: " + nodeId);
        }

        HierarchyNode pNode = getNode(nodeId);
        if (pNode == null) {
            throw new IllegalArgumentException("Invalid nodeId: " + nodeId);
        }

        HierarchyNode removePNode = getNode(childNodeId);
        if (removePNode == null) {
            throw new IllegalArgumentException("Invalid childNodeId: " + childNodeId);
        }

        if (pNode.getChildren().contains(removePNode)) {
            if (removePNode.getParents().size() <= 1) {
                throw new IllegalArgumentException("Cannot remove "
                        + childNodeId
                        + " as a child of "
                        + nodeId
                        + " because it would orphan the child node, you need to use the remove method"
                        + "if you want to remove a node or add this node as the child of another node first");
            }

            final HierarchyNode parent = pNode;
            removePNode.getParents().removeIf(p -> p.equals(parent));
            pNode.getChildren().removeIf(c -> c.equals(removePNode));
            pNode = nodeRepository.save(pNode);
        }

        return pNode;
    }

    public HierarchyNode addParentRelation(String nodeId, String parentNodeId) {
        throw new RuntimeException("This method is not implemented yet");
    }

    public HierarchyNode removeParentRelation(String nodeId, String parentNodeId) {
        throw new RuntimeException("This method is not implemented yet");
    }

    @Transactional(readOnly = true)
    public Set<String> getNodesWithToken(String hierarchyId, String permToken) {
        if (permToken == null || permToken.isEmpty()) {
            throw new NullPointerException("permToken cannot be null or empty string");
        }

        List<HierarchyNode> l = nodeRepository.findByHierarchyId(hierarchyId);
        if (l.isEmpty()) {
            throw new IllegalArgumentException("Could not find hierarchy with the following id: " + hierarchyId);
        }

        Set<String> nodeIds = new HashSet<>();
        for (HierarchyNode node : nodeRepository.findByHierarchyIdAndPermToken(hierarchyId, permToken)) {
            nodeIds.add(node.getId().toString());
        }
        return nodeIds;
    }

    @Transactional(readOnly = true)
    public Map<String, Set<String>> getNodesWithTokens(String hierarchyId, String[] permTokens) {
        if (permTokens == null) {
            throw new NullPointerException("permTokens cannot be null");
        }

        Map<String, Set<String>> tokenNodes = new HashMap<>();
        for (String permToken : permTokens) {
            tokenNodes.put(permToken, getNodesWithToken(hierarchyId, permToken));
        }
        return tokenNodes;
    }

    public void assignUserNodePerm(String userId, String nodeId, String hierarchyPermission, boolean cascade) {
        if (userId == null || userId.isEmpty()
                || nodeId == null || nodeId.isEmpty()
                || hierarchyPermission == null || hierarchyPermission.isEmpty()) {
            throw new IllegalArgumentException("Invalid arguments to assignUserNodePerm, no arguments can be null or blank: userId="
                    + userId
                    + ", nodeId="
                    + nodeId
                    + ", hierarchyPermission="
                    + hierarchyPermission);
        }
        HierarchyNodePermission nodePerm = permissionRepository
                .findByUserIdAndNodeIdAndPermission(userId, nodeId, hierarchyPermission)
                .orElse(null);
        if (nodePerm == null) {
            long nodeIdNum;
            try {
                nodeIdNum = Long.parseLong(nodeId);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Node id ("
                        + nodeId
                        + ") provided is invalid, must be a valid identifier from an existing node");
            }
            if (!nodeRepository.existsById(nodeIdNum)) {
                throw new IllegalArgumentException("Node id ("
                        + nodeId
                        + ") provided is invalid, node does not exist");
            }
            permissionRepository.save(new HierarchyNodePermission(userId, nodeId, hierarchyPermission));
        }
        if (cascade) {
            Set<HierarchyNode> descendants = nodeRepository.findAllDescendants(Long.parseLong(nodeId));
            if (!descendants.isEmpty()) {
                Set<String> childNodeIds = descendants.stream()
                        .map(n -> n.getId().toString())
                        .collect(Collectors.toSet());
                List<HierarchyNodePermission> nodePerms = permissionRepository
                        .findByUserIdAndPermissionAndNodeIdIn(userId, hierarchyPermission, childNodeIds);
                Set<String> existingPermNodeIds = nodePerms.stream()
                        .map(HierarchyNodePermission::getNodeId)
                        .collect(Collectors.toSet());
                Set<HierarchyNodePermission> allPerms = new HashSet<>();
                for (String childNodeId : childNodeIds) {
                    if (!existingPermNodeIds.contains(childNodeId)) {
                        allPerms.add(new HierarchyNodePermission(userId, childNodeId, hierarchyPermission));
                    }
                }
                if (!allPerms.isEmpty()) {
                    permissionRepository.saveAll(allPerms);
                }
            }
        }
    }

    public void removeUserNodePerm(String userId, String nodeId, String hierarchyPermission, boolean cascade) {
        if (userId == null || userId.isEmpty()
                || nodeId == null || nodeId.isEmpty()
                || hierarchyPermission == null || hierarchyPermission.isEmpty()) {
            throw new IllegalArgumentException("Invalid arguments to removeUserNodePerm, no arguments can be null or blank: userId=" + userId + ", nodeId=" + nodeId + ", hierarchyPermission=" + hierarchyPermission);
        }
        if (!cascade) {
            permissionRepository.findByUserIdAndNodeIdAndPermission(userId, nodeId, hierarchyPermission)
                    .ifPresent(permissionRepository::delete);
        } else {
            Set<HierarchyNode> descendants;
            try {
                descendants = nodeRepository.findAllDescendants(Long.parseLong(nodeId));
            } catch (NumberFormatException e) {
                return;
            }
            Set<String> nodeIdsSet = new HashSet<>();
            nodeIdsSet.add(nodeId);
            descendants.stream().map(n -> n.getId().toString()).forEach(nodeIdsSet::add);
            List<HierarchyNodePermission> nodePerms = permissionRepository
                    .findByUserIdAndPermissionAndNodeIdIn(userId, hierarchyPermission, nodeIdsSet);
            if (!nodePerms.isEmpty()) {
                permissionRepository.deleteAll(nodePerms);
            }
        }
    }

    @Transactional(readOnly = true)
    public boolean checkUserNodePerm(String userId, String nodeId, String hierarchyPermission) {
        if (userId == null || userId.isEmpty()
                || nodeId == null || nodeId.isEmpty()
                || hierarchyPermission == null || hierarchyPermission.isEmpty()) {
            throw new IllegalArgumentException("Invalid arguments to checkUserNodePerm, no arguments can be null or blank: userId=" + userId + ", nodeId=" + nodeId + ", hierarchyPermission=" + hierarchyPermission);
        }
        return permissionRepository.findByUserIdAndNodeIdAndPermission(userId, nodeId, hierarchyPermission).isPresent();
    }

    @Transactional(readOnly = true)
    public Set<HierarchyNode> getNodesForUserPerm(String userId, String hierarchyPermission) {
        if (userId == null || userId.isEmpty()
                || hierarchyPermission == null || hierarchyPermission.isEmpty()) {
            throw new IllegalArgumentException("Invalid arguments to getNodesForUserPerm, no arguments can be null or blank: userId=" + userId + ", hierarchyPermission=" + hierarchyPermission);
        }
        List<HierarchyNodePermission> nodePerms = permissionRepository.findByUserIdAndPermission(userId, hierarchyPermission);
        Set<Long> nodeIds = nodePerms.stream()
                .map(p -> Long.parseLong(p.getNodeId()))
                .collect(Collectors.toSet());
        return new HashSet<>(nodeRepository.findByIdIn(nodeIds));
    }

    @Transactional(readOnly = true)
    public Set<String> getUserIdsForNodesPerm(String[] nodeIds, String hierarchyPermission) {
        if (nodeIds == null || hierarchyPermission == null || hierarchyPermission.isEmpty()) {
            throw new IllegalArgumentException("Invalid arguments to getUserIdsForNodesPerm, no arguments can be null or blank: hierarchyPermission=" + hierarchyPermission);
        }
        Set<String> userIds = new HashSet<>();
        if (nodeIds.length > 0) {
            List<HierarchyNodePermission> nodePerms = permissionRepository
                    .findByPermissionAndNodeIdIn(hierarchyPermission, Arrays.asList(nodeIds));
            for (HierarchyNodePermission nodePerm : nodePerms) {
                userIds.add(nodePerm.getUserId());
            }
        }
        return userIds;
    }

    @Transactional(readOnly = true)
    public Set<String> getPermsForUserNodes(String userId, String[] nodeIds) {
        if (userId == null || userId.isEmpty() || nodeIds == null) {
            throw new IllegalArgumentException("Invalid arguments to getPermsForUserNodes, no arguments can be null or blank: userId=" + userId);
        }
        Set<String> perms = new HashSet<>();
        if (nodeIds.length > 0) {
            List<HierarchyNodePermission> nodePerms = permissionRepository
                    .findByUserIdAndNodeIdIn(userId, Arrays.asList(nodeIds));
            for (HierarchyNodePermission nodePerm : nodePerms) {
                perms.add(nodePerm.getPermission());
            }
        }
        return perms;
    }

    @Transactional(readOnly = true)
    public Map<String, Map<String, Set<String>>> getUsersAndPermsForNodes(String... nodeIds) {
        if (nodeIds == null || nodeIds.length == 0) {
            throw new IllegalArgumentException("Invalid arguments to getUsersAndPermsForNodes, no arguments can be null or blank");
        }
        Map<String, Map<String, Set<String>>> m = new HashMap<>();
        for (String nodeId : nodeIds) {
            m.put(nodeId, new HashMap<>());
        }
        List<HierarchyNodePermission> nodePerms = permissionRepository
                .findByNodeIdIn(Arrays.asList(nodeIds));

        for (HierarchyNodePermission nodePerm : nodePerms) {
            String nodeId = nodePerm.getNodeId();
            if (!m.containsKey(nodeId)) {
                continue;
            }
            m.get(nodeId).computeIfAbsent(nodePerm.getUserId(), k -> new HashSet<>()).add(nodePerm.getPermission());
        }
        return m;
    }

    @Transactional(readOnly = true)
    public Map<String, Map<String, Set<String>>> getNodesAndPermsForUser(String... userIds) {
        if (userIds == null || userIds.length == 0) {
            throw new IllegalArgumentException("Invalid arguments to getNodesAndPermsForUser, no arguments can be null or blank");
        }
        Map<String, Map<String, Set<String>>> m = new HashMap<>();
        for (String userId : userIds) {
            m.put(userId, new HashMap<>());
        }
        List<HierarchyNodePermission> nodePerms = permissionRepository
                .findByUserIdIn(Arrays.asList(userIds));

        for (HierarchyNodePermission nodePerm : nodePerms) {
            String userId = nodePerm.getUserId();
            if (!m.containsKey(userId)) {
                continue;
            }
            m.get(userId).computeIfAbsent(nodePerm.getNodeId(), k -> new HashSet<>()).add(nodePerm.getPermission());
        }
        return m;
    }

    private HierarchyNode getNode(String nodeId) {
        try {
            return nodeRepository.findById(Long.parseLong(nodeId)).orElse(null);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid node id: " + nodeId);
        }
    }

}
