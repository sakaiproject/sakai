/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.data.dao.assessment;

import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import java.io.Serializable;
import java.util.Date;


public class PublishedSectionAttachment
    extends PublishedAttachmentData
    implements Serializable, SectionAttachmentIfc
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 3811461383814780684L;
private SectionDataIfc section;

  public PublishedSectionAttachment()  { }

  public PublishedSectionAttachment(Long attachmentId, SectionDataIfc section, String resourceId,
                   String filename, String mimeType,
                   Long fileSize, String description, String location,
                   Boolean isLink, Integer status, String createdBy, Date createdDate,
                   String lastModifiedBy, Date lastModifiedDate){
    super(attachmentId, resourceId, SectionAttachmentIfc.SECTION_ATTACHMENT,
          filename, mimeType,
          fileSize, description, location, isLink, status,
          createdBy, createdDate, lastModifiedBy, lastModifiedDate);
    this.section = section;
  }

  public PublishedSectionAttachment(Long attachmentId, String resourceId,
                   String filename, String mimeType,
                   Long fileSize, String description, String location,
                   Boolean isLink, Integer status, String createdBy, Date createdDate,
                   String lastModifiedBy, Date lastModifiedDate){
    super(attachmentId, resourceId, SectionAttachmentIfc.SECTION_ATTACHMENT,
          filename, mimeType,
          fileSize, description, location, isLink, status,
          createdBy, createdDate, lastModifiedBy, lastModifiedDate);
  }

  public SectionDataIfc getSection() {
    return section;
  }

  public void setSection(SectionDataIfc section) {
    this.section = section;
  }

  public Long getAttachmentType() {
    return SectionAttachmentIfc.SECTION_ATTACHMENT;
  }

  public void setAttachmentType(Long attachmentType) {
  }


}
