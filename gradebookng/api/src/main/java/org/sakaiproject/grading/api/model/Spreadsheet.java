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
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

// TODO: Check this against SAK-46484. I cut this code before that patch.

@Entity
@Table(name = "GB_SPREADSHEET_T")
@Getter @Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Spreadsheet implements Serializable {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    @EqualsAndHashCode.Include
    @ToString.Include
    protected Long id;

    @ManyToOne
    @JoinColumn(name = "GRADEBOOK_ID", nullable = false)
    @EqualsAndHashCode.Include
    protected Gradebook gradebook;

    @Column(name = "VERSION")
    protected Integer version = 1;

    @Lob
    @Column(name = "CONTENT", length = 16777215, nullable = false)
    protected String content;

    @Column(name = "CREATOR", nullable = false)
    protected String creator;

    @Column(name = "NAME", nullable = false)
    @EqualsAndHashCode.Include
    @ToString.Include
    protected String name;

    @Column(name = "DATE_CREATED", nullable = false)
    protected Date dateCreated;

    public Spreadsheet() { }

    public Spreadsheet(Gradebook gradebook, String content, String creator, String name, Date dateCreated) {

        this.gradebook = gradebook;
        this.content = content;
        this.creator = creator;
        this.name = name;
        this.dateCreated = dateCreated;
    }
}
