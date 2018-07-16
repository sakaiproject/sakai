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

@Data
@EqualsAndHashCode(of = "syllabusAttachId")
@ToString(of = {"syllabusAttachId", "attachmentId", "lockId"})
public class SyllabusAttachmentImpl implements SyllabusAttachment, Comparable {
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

    public int compareTo(Object obj) {
        if (this.equals(obj)) return 0;
        if (this.syllabusAttachId == null) return -1;
        return this.syllabusAttachId.compareTo(((SyllabusAttachment) obj).getSyllabusAttachId());
    }
}