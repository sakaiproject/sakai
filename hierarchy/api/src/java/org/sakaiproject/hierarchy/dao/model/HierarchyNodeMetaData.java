/**
 * Copyright (c) 2007-2014 The Apereo Foundation
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

package org.sakaiproject.hierarchy.dao.model;

/**
 * This is the persistent object for storing Hierarchy Node meta data
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class HierarchyNodeMetaData {

   /**
    * The unique internal id for this meta data
    */
   private Long id;

   /**
    * this is the node that this meta data is associated with
    */
   private HierarchyPersistentNode node;

   /**
    * The assigned unique id for this hierarchy (used for lookup)
    */
   private String hierarchyId;

   /**
    * true if this is the rootnode for this hierarchy
    */
   private Boolean isRootNode;

   /**
    * the userId of the owner (creator) of the associated node
    */
   private String ownerId;

   /**
    * the title for the associated node
    */
   private String title;

   /**
    * the description for the associated node
    */
   private String description;

   /**
    * the permissions token for the associated node
    */
   private String permToken;
   
   /**
    * true if this node is disabled, i.e. left in hierarchy for historical purposes (default is false)
    */
   private Boolean isDisabled;

   /**
    * Empty constructor
    */
   public HierarchyNodeMetaData() {
   }

   /**
    * minimal
    */
   public HierarchyNodeMetaData(HierarchyPersistentNode node, String hierarchyId, Boolean isRootNode,
         String ownerId) {
      this.node = node;
      this.hierarchyId = hierarchyId;
      this.isRootNode = isRootNode;
      this.ownerId = ownerId;
      this.isDisabled = false; // default is false and needs to be set
   }

   /**
    * full
    */
   public HierarchyNodeMetaData(HierarchyPersistentNode node, String hierarchyId, Boolean isRootNode,
         String ownerId, String title, String description, String permToken, Boolean isDisabled) {
      this.node = node;
      this.hierarchyId = hierarchyId;
      this.isRootNode = isRootNode;
      this.ownerId = ownerId;
      this.title = title;
      this.description = description;
      this.permToken = permToken;
      this.isDisabled = isDisabled;
   }

   public String getDescription() {
      return description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public String getHierarchyId() {
      return hierarchyId;
   }

   public void setHierarchyId(String hierarchyId) {
      this.hierarchyId = hierarchyId;
   }

   public Long getId() {
      return id;
   }

   public void setId(Long id) {
      this.id = id;
   }

   public Boolean getIsRootNode() {
      return isRootNode;
   }

   public void setIsRootNode(Boolean isRootNode) {
      this.isRootNode = isRootNode;
   }

   public HierarchyPersistentNode getNode() {
      return node;
   }

   public void setNode(HierarchyPersistentNode node) {
      this.node = node;
   }

   public String getOwnerId() {
      return ownerId;
   }

   public void setOwnerId(String ownerId) {
      this.ownerId = ownerId;
   }

   public String getTitle() {
      return title;
   }

   public void setTitle(String title) {
      this.title = title;
   }

   public String getPermToken() {
      return permToken;
   }

   public void setPermToken(String permToken) {
      this.permToken = permToken;
   }
   
   public Boolean getIsDisabled() {
      return isDisabled;
   }
   
   public void setIsDisabled(Boolean isDisabled) {
      this.isDisabled = isDisabled;
   }

}
