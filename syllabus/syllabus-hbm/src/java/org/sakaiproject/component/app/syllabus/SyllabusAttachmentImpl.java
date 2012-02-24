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

import org.sakaiproject.api.app.syllabus.SyllabusAttachment;
import org.sakaiproject.api.app.syllabus.SyllabusData;

/**
 * @author <a href="mailto:cwen.iupui.edu">Chen Wen</a>
 * @version $Id$
 * 
 */
public class SyllabusAttachmentImpl implements SyllabusAttachment, Comparable
{
  private Integer lockId;
  private String attachmentId;
  private Long syllabusAttachId;
  private SyllabusData syllabusData;
  private String name;
  private String size;
  private String type;
  private String createdBy;
  private String lastModifiedBy;
  private String url;

  public Integer getLockId()
  {
    return lockId;
  }

  public void setLockId(Integer lockId)
  {
    this.lockId = lockId;
  }

  public String getAttachmentId()
  {
    return attachmentId;
  }
  
  public void setAttachmentId(String attachId)
  {
    this.attachmentId = attachId;
  }
  
  public Long getSyllabusAttachId()
  {
    return syllabusAttachId;
  }
  
  public void setSyllabusAttachId(Long syllabusAttachId)
  {
    this.syllabusAttachId = syllabusAttachId;
  }

  public int hashCode()
  {         
    return syllabusAttachId.hashCode();           
  }
  
  public boolean equals(Object obj)
  {
    if (this == obj) return true;
    if (!(obj instanceof SyllabusAttachmentImpl)) return false;
    SyllabusAttachmentImpl other = (SyllabusAttachmentImpl) obj;

    if ((syllabusAttachId == null ? other.syllabusAttachId == null : syllabusAttachId
        .equals(other.syllabusAttachId)))
    {
      return true;
    }
    return false;
  }
  
  public int compareTo(Object obj)
  {
    return this.syllabusAttachId.compareTo(((SyllabusAttachment) obj).getSyllabusAttachId());  
  }
  
  public String toString()
  {
	StringBuilder sb = new StringBuilder();
    sb.append("{syllabusAttachId=");
    sb.append(syllabusAttachId);
    sb.append(", attachmentId=");
    sb.append(attachmentId);    
    sb.append(", lockId=");
    sb.append(lockId);
    sb.append("}");
    return sb.toString();
  } 

  public SyllabusData getSyllabusData()
  {
    return syllabusData;
  }

  public void setSyllabusData(SyllabusData syllabusData)
  {
    this.syllabusData = syllabusData;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getSize()
  {
    return this.size;
  }

  public String getType()
  {
    return this.type;
  }

  public String getCreatedBy()
  {
    return this.createdBy;
  }

  public String getLastModifiedBy()
  {
    return this.lastModifiedBy;
  }
  
  public void setSize(String size)
  {
    this.size = size;
  }
  
  public void setType(String type)
  {
    this.type = type;
  }
  
  public void setCreatedBy(String createdBy)
  {
    this.createdBy = createdBy;
  }
  
  public void setLastModifiedBy(String lastModifiedBy)
  {
    this.lastModifiedBy = lastModifiedBy;
  }

  public void setUrl(String url)
  {
    this.url = url;
  }
  
  public String getUrl()
  {
    return url;
  }
}