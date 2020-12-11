/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.component.app.syllabus;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.sakaiproject.api.app.syllabus.SyllabusAttachment;
import org.sakaiproject.api.app.syllabus.SyllabusData;
import org.springframework.util.comparator.NullSafeComparator;

@Data
@EqualsAndHashCode(of = {"syllabusAttachId", "attachmentId"})
@ToString(of = {"syllabusAttachId", "attachmentId", "lockId"})
public class SyllabusAttachmentImpl implements SyllabusAttachment, Comparable<SyllabusAttachment> {
    private Long syllabusAttachId;
    private String attachmentId;
    private Integer lockId;
    private SyllabusData syllabusData;
    private String name;
    private String size;
    private String type;
    private String createdBy;
    private String lastModifiedBy;
    private String url;

    public int compareTo(SyllabusAttachment other) {
        int result = NullSafeComparator.NULLS_LOW.compare(this.syllabusAttachId, other.getSyllabusAttachId());
        if (result == 0) result = NullSafeComparator.NULLS_LOW.compare(this.attachmentId, other.getAttachmentId());
        return result;
    }

}
