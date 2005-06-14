/**********************************************************************************
*
* $Header: /cvs/sakai2/syllabus/syllabus-api/src/java/org/sakaiproject/api/app/syllabus/SyllabusManager.java,v 1.1 2005/05/19 14:25:58 cwen.iupui.edu Exp $
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

import java.util.Set;


public interface SyllabusManager
{
  /**
   * creates an SyllabusItem
   */
  public SyllabusItem createSyllabusItem(String userId, String contextId,
      String redirectURL);

  public SyllabusItem getSyllabusItemByUserAndContextIds(final String userId,
      final String contextId);

  public void saveSyllabusItem(SyllabusItem item);
  
  public void addSyllabusToSyllabusItem(final SyllabusItem syllabusItem, final SyllabusData syllabusData);
  
  public void removeSyllabusFromSyllabusItem(final SyllabusItem syllabusItem, final SyllabusData syllabusData);
  
  public SyllabusData createSyllabusDataObject(String title, Integer position,
      String assetId, String view, String status, String emailNotification);
  
  public Set getSyllabiForSyllabusItem(final SyllabusItem syllabusItem);
  
  public void swapSyllabusDataPositions(final SyllabusItem syllabusItem, final SyllabusData d1, final SyllabusData d2);
  
  public void saveSyllabus(SyllabusData data);
  
  public Integer findLargestSyllabusPosition(final SyllabusItem syllabusItem);
  
  public SyllabusItem getSyllabusItemByContextId(final String contextId);
}

/**************************************************************************************************************************************************************************************************************************************************************
 * $Header: /cvs/sakai2/syllabus/syllabus-api/src/java/org/sakaiproject/api/app/syllabus/SyllabusManager.java,v 1.1 2005/05/19 14:25:58 cwen.iupui.edu Exp $
 *************************************************************************************************************************************************************************************************************************************************************/
