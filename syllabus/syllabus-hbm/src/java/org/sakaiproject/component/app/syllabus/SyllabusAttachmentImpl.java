/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/syllabus/trunk/syllabus-hbm/src/java/org/sakaiproject/component/app/syllabus/SyllabusAttachmentImpl.java $
 * $Id: SyllabusAttachmentImpl.java 8122 2006-05-01 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
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
    StringBuffer sb = new StringBuffer();
    sb.append("{syllabusAttachId=");
    sb.append(syllabusAttachId);
    sb.append(", attachmentId=");
    sb.append(attachmentId);    
    sb.append(", lockId=");
    sb.append(lockId);
    sb.append("}");
    return sb.toString();
  } 

  public final SyllabusData getSyllabusData()
  {
    return syllabusData;
  }

  public final void setSyllabusData(SyllabusData syllabusData)
  {
    this.syllabusData = syllabusData;
  }

  public final String getName()
  {
    return name;
  }

  public final void setName(String name)
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