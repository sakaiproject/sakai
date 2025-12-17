/**
 * $Id$
 * $URL$
 * AutoRegister.java - entity-broker - 31 May 2007 7:01:11 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

package org.sakaiproject.entitybroker.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.sakaiproject.springframework.data.PersistableEntity;

/**
 * This is the persistent object for storing entity tag applications,
 * i.e. the combination of an entity and a tag, or the instance of a tag on an entity
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
@Data
@Entity
@Table(name = "ENTITY_TAG_APPLICATIONS",
    indexes = { @Index(name = "entity_tag_app_idx", columnList = "entityRef, entityPrefix, tag") })
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EntityTagApplication implements PersistableEntity<Long> {

   @Id
   @SequenceGenerator(name = "entity_tag_app_id_sequence",
                      sequenceName = "ENTITY_TAG_APPLICATIONS_S",
                      allocationSize = 1)
   @GeneratedValue(strategy = GenerationType.AUTO, generator = "entity_tag_app_id_sequence")
   @Column(name = "ID")
   @EqualsAndHashCode.Include
   private Long id;

   @Column(name = "entityRef", nullable = false)
   private String entityRef;

   @Column(name = "entityPrefix", nullable = false)
   private String entityPrefix;

   @Column(name = "tag", nullable = false)
   private String tag;

   public EntityTagApplication(String entityRef, String entityPrefix, String tag) {
      this.entityRef = entityRef;
      this.entityPrefix = entityPrefix;
      this.tag = tag;
   }
}
