/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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
 *
 **********************************************************************************/
package org.sakaiproject.api.app.syllabus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import org.springframework.util.comparator.NullSafeComparator;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "SAKAI_SYLLABUS_ATTACH",
        indexes = {
                @Index(name = "SYLLABUS_ATTACH_ID_I", columnList = "syllabusId")
        }
)

@Data
@EqualsAndHashCode(of = {"syllabusAttachId", "attachmentId"})
@ToString(of = {"syllabusAttachId", "attachmentId", "lockId"})
public class SyllabusAttachment implements Comparable<SyllabusAttachment> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "syllabus_attachment_sequence")
    @SequenceGenerator(name = "syllabus_attachment_sequence", sequenceName = "SyllabusAttachImpl_SEQ")
    private Long syllabusAttachId;

    @Column(length = 256, nullable = false)
    private String attachmentId;

    @Version
    private Integer lockId;

    @Column(length = 256)
    private String createdBy;

    @Column(length = 256)
    private String lastModifiedBy;

    @Column(name = "syllabusAttachName", length = 256, nullable = false)
    private String name;

    @Column(name = "syllabusAttachSize", length = 256)
    private String size;

    @Column(name = "syllabusAttachType", length = 256)
    private String type;

    @Column(name = "syllabusAttachUrl", length = 256, nullable = false)
    private String url;

    @ManyToOne
    @JoinColumn(name = "syllabusId")
    private SyllabusData syllabusData;

    public int compareTo(SyllabusAttachment other) {
        int result = NullSafeComparator.NULLS_LOW.compare(this.name, other.getName());
        if (result == 0) NullSafeComparator.NULLS_LOW.compare(this.syllabusAttachId, other.getSyllabusAttachId());
        if (result == 0) result = NullSafeComparator.NULLS_LOW.compare(this.attachmentId, other.getAttachmentId());
        return result;
    }
}
