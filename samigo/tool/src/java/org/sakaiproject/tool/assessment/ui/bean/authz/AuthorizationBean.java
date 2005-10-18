/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
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

package org.sakaiproject.tool.assessment.ui.bean.authz;

import java.util.HashMap;

public class AuthorizationBean {
  private HashMap map = new HashMap();

  public AuthorizationBean(){
    map = new HashMap();
  }

  public void setPrivilege(String functionName, boolean privilege){
    map.put(functionName, new Boolean(privilege));
  } 

  public boolean getPrivilege(String functionName){
    return ((Boolean)map.get(functionName)).booleanValue();
  } 

    /**
  public boolean getCreateAssessment(String siteId){
    return ((Boolean)map.get("createAssessment_"+siteId)).booleanValue();
  } 
  public void setCreateAssessment(String siteId, boolean createAssessment){
    map.put("createAssessment_"+siteId, new Boolean(createAssessment));
  } 

  public boolean getEditAnyAssessment(String siteId) {
    return ((Boolean)map.get("editAnyAssessment_"+siteId)).booleanValue();
  } 

  public void setEditAnyAssessment(String siteId, boolean editAnyAssessment){
    map.put("editAnyAssessment_"+siteId, new Boolean(editAnyAssessment));
  } 

  public boolean getEditOwnAssessment(String siteId) {
    return ((Boolean)map.get("editOwnAssessment_"+siteId)).booleanValue();
  } 
  public void setEditOwnAssessment(String siteId, boolean editOwnAssessment){
    map.put("editOwnAssessment_"+siteId, new Boolean(editOwnAssessment));
  } 

  public boolean getDeleteAnyAssessment(String siteId) {
    return ((Boolean)map.get("deleteAnyAssessment_"+siteId)).booleanValue();
  } 

  public void setDeleteAnyAssessment(String siteId, boolean deleteAnyAssessment){
    map.put("deleteAnyAssessment_"+siteId, new Boolean(deleteAnyAssessment));
  } 

  public boolean getDeleteOwnAssessment(String siteId) {
    return ((Boolean)map.get("deleteOwnAssessment_"+siteId)).booleanValue();
  } 
  public void setDeleteOwnAssessment(String siteId, boolean deleteOwnAssessment){
    map.put("deleteOwnAssessment_"+siteId, new Boolean(deleteOwnAssessment));
  } 

  public boolean getPublishAnyAssessment(String siteId) {
    return ((Boolean)map.get("publishAnyAssessment_"+siteId)).booleanValue();
  } 
  public void setPublishAnyAssessment(String siteId, boolean publishAnyAssessment){
    map.put("publishAnyAssessment_"+siteId, new Boolean(publishAnyAssessment));
  } 

  public boolean getPublishOwnAssessment(String siteId) {
    return ((Boolean)map.get("publishOwnAssessment_"+siteId)).booleanValue();
  } 

  public void setPublishOwnAssessment(String siteId, boolean publishOwnAssessment){
    map.put("publishOwnAssessment_"+siteId, new Boolean(publishOwnAssessment));
  } 

  public boolean getGradeAnyAssessment(String siteId) {
    return ((Boolean)map.get("gradeAnyAssessment_"+siteId)).booleanValue();
  } 
  public void setGradeAnyAssessment(String siteId, boolean gradeAnyAssessment){
    map.put("gradeAnyAssessment_"+siteId, new Boolean(gradeAnyAssessment));
  } 

  public boolean getGradeOwnAssessment(String siteId) {
    return ((Boolean)map.get("gradeOwnAssessment_"+siteId)).booleanValue();
  } 

  public void setGradeOwnAssessment(String siteId, boolean gradeOwnAssessment){
    map.put("gradeOwnAssessment_"+siteId, new Boolean(gradeOwnAssessment));
  } 

  public boolean getCreateQuestionPool(String siteId) {
    return ((Boolean)map.get("createQuestionPool_"+siteId)).booleanValue();
  } 

  public void setCreateQuestionPool(String siteId, boolean createQuestionPool){
    map.put("createQuestionPool_"+siteId, new Boolean(createQuestionPool));
  } 

  public boolean getEditOwnQuestionPool(String siteId) {
    return ((Boolean)map.get("editOwnQuestionPool_"+siteId)).booleanValue();
  } 

  public void setEditOwnQuestionPool(String siteId, boolean editOwnQuestionPool){
    map.put("editOwnQuestionPool_"+siteId, new Boolean(editOwnQuestionPool));
  } 

  public boolean getDeleteOwnQuestionPool(String siteId) {
    return ((Boolean)map.get("deleteOwnQuestionPool_"+siteId)).booleanValue();
  } 

  public void setDeleteOwnQuestionPool(String siteId, boolean deleteOwnQuestionPool){
    map.put("deleteOwnQuestionPool_"+siteId, new Boolean(deleteOwnQuestionPool));
  } 

  public boolean getCopyOwnQuestionPool(String siteId) {
    return ((Boolean)map.get("copyOwnQuestionPool_"+siteId)).booleanValue();
  } 

  public void setCopyOwnQuestionPool(String siteId, boolean copyOwnQuestionPool){
    map.put("copyOwnQuestionPool_"+siteId, new Boolean(copyOwnQuestionPool));
  } 
    */

}
