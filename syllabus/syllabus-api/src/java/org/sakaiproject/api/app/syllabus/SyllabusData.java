/**********************************************************************************
*
* $Header: /cvs/sakai2/syllabus/syllabus-api/src/java/org/sakaiproject/api/app/syllabus/SyllabusData.java,v 1.1 2005/05/19 14:25:58 cwen.iupui.edu Exp $
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

public interface SyllabusData
{
  /**
   * @return Returns the emailNotification.
   */
  public String getEmailNotification();

  /**
   * @param emailNotification The emailNotification to set.
   */
  public void setEmailNotification(String emailNotification);

  /**
   * @return Returns the status.
   */
  public String getStatus();

  /**
   * @param status The status to set.
   */
  public void setStatus(String status);

  /**
   * @return Returns the title.
   */
  public String getTitle();

  /**
   * @param title The title to set.
   */
  public void setTitle(String title);

  /**
   * @return Returns the view.
   */
  public String getView();

  /**
   * @param view The view to set.
   */
  public void setView(String view);

  /**
   * @return Returns the assetId.
   */
  public String getAsset();

  /**
   * @param assetId The assetId to set.
   */
  public void setAsset(String assetId);

  /**
   * @return Returns the lockId.
   */
  public Integer getLockId();

  /**
   * @return Returns the position.
   */
  public Integer getPosition();

  /**
   * @param position The position to set.
   */
  public void setPosition(Integer position);

  /**
   * @return Returns the syllabusId.
   */
  public Long getSyllabusId();

  /**
   * @return Returns the syllabusItem.
   */
  public SyllabusItem getSyllabusItem();

  /**
   * @param syllabusItem The syllabusItem to set.
   */
  public void setSyllabusItem(SyllabusItem syllabusItem);
}

/**************************************************************************************************************************************************************************************************************************************************************
 * $Header: /cvs/sakai2/syllabus/syllabus-api/src/java/org/sakaiproject/api/app/syllabus/SyllabusData.java,v 1.1 2005/05/19 14:25:58 cwen.iupui.edu Exp $
 *************************************************************************************************************************************************************************************************************************************************************/
