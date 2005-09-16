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

package org.sakaiproject.api.app.syllabus;

/**
 * @author <a href="mailto:cwen.iupui.edu">Chen Wen</a>
 * @version $Id$
 * 
 */
public interface SyllabusAttachment
{
  public Integer getLockId();
  
  public void setLockId(Integer lockId);
  
  public String getAttachmentId();
  
  public void setAttachmentId(String attachId);
  
  public Long getSyllabusAttachId();
  
  public void setSyllabusAttachId(Long syllabusAttachId);
  
  public String getName();
  
  public void setName(String name);
  
  public String getSize();
  
  public String getType();
  
  public String getCreatedBy();
  
  public String getLastModifiedBy();

  public void setSize(String size);
  
  public void setType(String type);
  
  public void setCreatedBy(String createdBy);
  
  public void setLastModifiedBy(String lastMOdifiedBy);
  
  public void setUrl(String url);
  
  public String getUrl();
}

/**********************************************************************************
*
* $Header$
*
**********************************************************************************/