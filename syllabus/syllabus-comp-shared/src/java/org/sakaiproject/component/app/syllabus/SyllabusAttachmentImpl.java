/**********************************************************************************
*
* $Header$
*
***********************************************************************************
*
* Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.component.app.syllabus;

import org.sakaiproject.api.app.syllabus.SyllabusAttachment;
import org.sakaiproject.api.app.syllabus.SyllabusData;
import org.sakaiproject.service.legacy.content.ContentResource;
import org.sakaiproject.service.legacy.content.cover.ContentHostingService;

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

/**********************************************************************************
*
* $Header$
*
**********************************************************************************/