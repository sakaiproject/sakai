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

@Data
@Entity
@Table(name = "ENTITY_PROPERTIES",
    indexes = { @Index(name = "entity_property_idx", columnList = "entityRef, entityPrefix, propertyName") })
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EntityProperty implements PersistableEntity<Long> {

   @Id
   @SequenceGenerator(name = "entity_property_id_sequence",
                      sequenceName = "ENTITY_PROPERTIES_S",
                      allocationSize = 1)
   @GeneratedValue(strategy = GenerationType.AUTO, generator = "entity_property_id_sequence")
   @Column(name = "ID")
   @EqualsAndHashCode.Include
   private Long id;

   @Column(name = "entityRef", nullable = false)
   private String entityRef;

   @Column(name = "entityPrefix", nullable = false)
   private String entityPrefix;

   @Column(name = "propertyName", nullable = false)
   private String propertyName;

   @Column(name = "propertyValue", length = 4096, nullable = false)
   private String propertyValue;

   public EntityProperty(String entityRef, String entityPrefix, String propertyName, String propertyValue) {
      this.entityRef = entityRef;
      this.entityPrefix = entityPrefix;
      this.propertyName = propertyName;
      this.propertyValue = propertyValue;
   }
}
