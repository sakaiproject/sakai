/*******************************************************************************
 * Copyright (c) 2006, 2008 The Sakai Foundation, The MIT Corporation
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
 ******************************************************************************/

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
import javax.persistence.UniqueConstraint;

import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

// TODO: Check this against SAK-46484. I cut this code before that patch.

@Entity
@Table(name = "GB_COMMENT_T", uniqueConstraints = @UniqueConstraint(name = "G_O_STUDENT", columnNames = {"STUDENT_ID", "GRADABLE_OBJECT_ID"}))
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter @Setter
public class Comment implements PersistableEntity<Long>, Serializable {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "VERSION")
    private Integer version = 1;

    @Column(name = "STUDENT_ID", nullable = false)
    @ToString.Include(name = "studentid")
    private String studentId;

    @Column(name = "GRADER_ID", nullable = false)
    @ToString.Include(name = "grader")
    private String graderId;

    @Column(name = "DATE_RECORDED", nullable = false)
    private Date dateRecorded;

    @Column(name = "COMMENT_TEXT")
    @Lob
    @ToString.Include(name = "comment")
    @EqualsAndHashCode.Include
    private String commentText;

    @ManyToOne
    @JoinColumn(name = "GRADABLE_OBJECT_ID", nullable = false)
    @EqualsAndHashCode.Include
    private GradableObject gradableObject;

    public Comment(String studentId, String comment, GradableObject gradableObject) {

        this.gradableObject = gradableObject;
        this.studentId = studentId;
        this.commentText = comment;
    }

    public Comment() { }
}

