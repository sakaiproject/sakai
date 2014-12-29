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

package org.sakaiproject.hierarchy.impl.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.sakaiproject.hierarchy.dao.model.HierarchyNodeMetaData;
import org.sakaiproject.hierarchy.dao.model.HierarchyPersistentNode;
import org.sakaiproject.hierarchy.model.HierarchyNode;

/**
 * Utility class for the hierarchy service
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class HierarchyImplUtils {

    public static final char SEPERATOR = ':';

    /**
     * Create a {@link HierarchyNode} from the persistent data,
     * exception if the data is not persisted or data is missing
     * @param pNode a {@link HierarchyPersistentNode} which has been persisted
     * @param metaData a {@link HierarchyNodeMetaData} which has been persisted
     * @return a {@link HierarchyNode} which contains data from the 2 inputs
     */
    public static HierarchyNode makeNode(HierarchyPersistentNode pNode, HierarchyNodeMetaData metaData) {
        if (pNode == null || pNode.getId() == null) {
            throw new IllegalArgumentException("pNode cannot be null and id of pNode must be set");
        }

        HierarchyNode hNode = new HierarchyNode();
        hNode.id = pNode.getId().toString();

        hNode.directParentNodeIds = makeNodeIdSet(pNode.getDirectParentIds());
        hNode.parentNodeIds = makeNodeIdSet(pNode.getParentIds());
        hNode.directChildNodeIds = makeNodeIdSet(pNode.getDirectChildIds());
        hNode.childNodeIds = makeNodeIdSet(pNode.getChildIds());

        hNode.hierarchyId = metaData.getHierarchyId();
        hNode.title = metaData.getTitle();
        hNode.description = metaData.getDescription();
        hNode.permToken = metaData.getPermToken();
        hNode.isDisabled = metaData.getIsDisabled();

        return hNode;
    }

    /**
     * Convenience method to create a user-facing node object from a combination metaData/Node object,
     * will return back a null if given a null
     * @param metaData a {@link HierarchyNodeMetaData} which has been persisted and contains a {@link HierarchyPersistentNode}
     * @return a {@link HierarchyNode} which contains data from the persistent object
     */
    public static HierarchyNode makeNode(HierarchyNodeMetaData metaData) {
        if (metaData == null) { return null; }
        if (metaData.getNode() == null || 
                metaData.getNode().getId() == null) {
            throw new IllegalArgumentException("Invalid metaData object: Must contain a complete HierarchyPersistentNode object");
        }
        return makeNode(metaData.getNode(), metaData);
    }

    /**
     * Make a Set of node Ids from an encoded string of nodeIds,
     * will not throw exception or return null
     * @param encodedNodeIds an encoded string of nodeIds
     * @return a {@link Set} with the nodeIds in it, ordered by nodeId
     */
    public static Set<String> makeNodeIdSet(String encodedNodeIds) {
        Set<String> s = new TreeSet<String>();
        if (encodedNodeIds != null) {
            String[] split = encodedNodeIds.split( String.valueOf(SEPERATOR) );
            if (split.length > 0) {
                for (int i = 0; i < split.length; i++) {
                    if (split[i] != null && !split[i].equals("")) {
                        s.add(split[i]);
                    }
                }
            }
        }
        return s;
    }

    /**
     * Make an encoded string of nodeIds from a Set of nodeIds
     * @param nodeIds a {@link Set} with the nodeIds in it
     * @return an encoded string of nodeIds
     */
    public static String makeEncodedNodeIdString(Set<String> nodeIds) {
        if (nodeIds == null || nodeIds.size() <= 0) {
            return null;
        }
        // make sure the order written into the database is natural node order
        List<String> l = new ArrayList<String>(nodeIds);
        Collections.sort(l);
        // encode the string
        StringBuilder coded = new StringBuilder();
        coded.append(HierarchyImplUtils.SEPERATOR);
        for (String nodeId : l) {
            coded.append(nodeId);
            coded.append(HierarchyImplUtils.SEPERATOR);            
        }
        return coded.toString();
    }

    /**
     * Method to allow us to easily build an encoded string for a single node without having to create a set first
     * @param nodeId unique id string for a node
     * @return an encoded string of nodeIds
     */
    public static String makeSingleEncodedNodeIdString(String nodeId) {
        if (nodeId == null || nodeId.length() == 0) {
            return null;
        }
        return HierarchyImplUtils.SEPERATOR + nodeId + HierarchyImplUtils.SEPERATOR;
    }

    /**
     * Method to allows us to add a single nodeId to an encoded string of nodeIds without creating a set,
     * will maintain the correct order for nodeIds, 
     * (do not run this over and over, use a set if you need to add more than one node)
     * @param encodedNodeIds an encoded string of nodeIds
     * @param nodeId unique id string for a node
     * @return an encoded string of nodeIds
     */
    public static String addSingleNodeIdToEncodedString(String encodedNodeIds, String nodeId) {
        if (encodedNodeIds == null || encodedNodeIds.length() == 0) {
            return makeSingleEncodedNodeIdString(nodeId);
        }
        if (nodeId == null || nodeId.length() == 0) {
            return encodedNodeIds;
        }
        if (encodedNodeIds.indexOf(makeSingleEncodedNodeIdString(nodeId)) != -1) {
            // this nodeId is already in the encoded string
            return encodedNodeIds;
        }
        int thisSeparator = 0;
        int lastIndex = encodedNodeIds.length()-1;
        while (thisSeparator < lastIndex ) {
            int nextSeparator = encodedNodeIds.indexOf(HierarchyImplUtils.SEPERATOR, thisSeparator+1);
            String thisNodeId = encodedNodeIds.substring(thisSeparator+1, nextSeparator);
            if (thisNodeId.compareTo(nodeId) > 0) {
                // thisNodeId comes after nodeId
                break;
            } else {
                thisSeparator = nextSeparator;
            }
        }

        String newEncodedNodeIds = null;
        if (thisSeparator == 0) {
            // put the node at the front of the string
            newEncodedNodeIds = HierarchyImplUtils.SEPERATOR + nodeId + encodedNodeIds;
        } else if (thisSeparator == lastIndex) {
            // put node at the end
            newEncodedNodeIds = encodedNodeIds + nodeId + HierarchyImplUtils.SEPERATOR;
        } else {
            // put the node at the location indicated by thisSeparator
            newEncodedNodeIds = encodedNodeIds.substring(0, thisSeparator)
                + HierarchyImplUtils.SEPERATOR + nodeId + encodedNodeIds.substring(thisSeparator);
        }
        return newEncodedNodeIds;
    }

}
