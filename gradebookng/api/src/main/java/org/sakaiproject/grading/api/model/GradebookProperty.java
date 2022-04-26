/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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

package org.sakaiproject.grading.api.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "GB_PROPERTY_T")
@Getter @Setter
@ToString(onlyExplicitlyIncluded = true)
public class GradebookProperty implements PersistableEntity<Long>, Comparable<Object>, Serializable {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    @ToString.Include
    private Long id;

    @Column(name = "VERSION")
    private Integer version = 1;

    @Column(name = "NAME", unique = true, nullable = false)
    @ToString.Include
    private String name;

    @Column(name = "VALUE")
    private String value;

    public GradebookProperty() { }

    public GradebookProperty(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(Object o) {
        return this.name.compareTo(((GradebookProperty)o).getName());
    }
}
