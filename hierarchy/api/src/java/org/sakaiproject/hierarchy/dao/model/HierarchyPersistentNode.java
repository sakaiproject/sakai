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

package org.sakaiproject.hierarchy.dao.model;

/**
 * This is the persistent object for storing Hierarchy Nodes
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class HierarchyPersistentNode {

    /**
     * The unique internal id for this hierarchy node
     */
    private Long id;
    /**
     * the ids of parent nodes that touch this node directly,
     * similar treatment to the way the it works for the {@link #parentIds}
     */
    private String directParentIds;
    /**
     * the ids of all parents of this node, 
     * this goes all the way up the hierarchy to the root node, 
     * expect this to be only one parent in most cases, 
     * the path to the root is determined using the directParentId only<br/>
     * Uses a ":" separator between each id, also includes the separator in front
     * of and behind every id.<br/>
     * Examples: ":123:432:43:", ":38:", "" (no parent)
     */
    private String parentIds;
    /**
     * the ids of child nodes that touch this node directly,
     * similar treatment to the way the it works for the {@link #parentIds}
     */
    private String directChildIds;
    /**
     * the ids of all children of this node, 
     * this goes all the way down the hierarchy to the leaf nodes,
     * similar treatment to the way the it works for the {@link #parentIds}
     */
    private String childIds;


    /**
     * Empty constructor
     */
    public HierarchyPersistentNode() {
    }

    /**
     * Leaf constructor
     */
    public HierarchyPersistentNode(String directParentIds, String parentIds) {
        this.directParentIds = directParentIds;
        this.parentIds = parentIds;
    }

    /**
     * Full constructor
     */
    public HierarchyPersistentNode(String directParentIds, String parentIds, String directChildIds, String childIds) {
        this.directParentIds = directParentIds;
        this.directChildIds = directChildIds;
        this.parentIds = parentIds;
        this.childIds = childIds;
    }


    /**
     * Getters and Setters
     */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getParentIds() {
        return parentIds;
    }

    public void setParentIds(String parentIds) {
        this.parentIds = parentIds;
    }

    public String getDirectChildIds() {
        return directChildIds;
    }

    public void setDirectChildIds(String directChildIds) {
        this.directChildIds = directChildIds;
    }

    public String getDirectParentIds() {
        return directParentIds;
    }

    public void setDirectParentIds(String directParentIds) {
        this.directParentIds = directParentIds;
    }

    public String getChildIds() {
        return childIds;
    }

    public void setChildIds(String childIds) {
        this.childIds = childIds;
    }

}
