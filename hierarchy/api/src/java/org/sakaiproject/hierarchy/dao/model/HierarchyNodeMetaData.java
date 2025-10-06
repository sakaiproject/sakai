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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.sakaiproject.springframework.data.PersistableEntity;

/**
 * This is the persistent object for storing Hierarchy Node meta data
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
@Entity
@Table(name = "HIERARCHY_NODE_META", indexes = {
      @Index(name = "HIERARCHY_HID", columnList = "hierarchyId"),
      @Index(name = "HIERARCHY_PERMTOKEN", columnList = "permToken")
})
public class HierarchyNodeMetaData implements PersistableEntity<Long> {

   /**
    * The unique internal id for this meta data
    */
   @Id
   @Column(name = "ID")
   private Long id;

   /**
    * this is the node that this meta data is associated with
    */
   @OneToOne(fetch = FetchType.EAGER, optional = false)
   @JoinColumn(name = "ID")
   @MapsId
   private HierarchyPersistentNode node;

   /**
    * The assigned unique id for this hierarchy (used for lookup)
    */
   @Column(name = "hierarchyId", length = 255)
   private String hierarchyId;

   /**
    * true if this is the rootnode for this hierarchy
    */
   @Column(name = "isRootNode", nullable = false)
   private Boolean isRootNode;

   /**
    * the userId of the owner (creator) of the associated node
    */
   @Column(name = "ownerId", length = 99)
   private String ownerId;

   /**
    * the title for the associated node
    */
   @Column(name = "title", length = 255)
   private String title;

   /**
    * the description for the associated node
    */
   @Lob
   @Column(name = "description")
   private String description;

   /**
    * the permissions token for the associated node
    */
   @Column(name = "permToken", length = 255)
   private String permToken;
   
   /**
    * true if this node is disabled, i.e. left in hierarchy for historical purposes (default is false)
    */
   @Column(name = "isDisabled", nullable = false)
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
      this.id = node != null ? node.getId() : null;
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
