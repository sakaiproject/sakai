/**********************************************************************************
* $HeadURL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
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
package org.sakaiproject.tool.assessment.facade;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;

public class TypeFacade implements Serializable, TypeIfc{

  // please make sure that the value match with the one in the Type table. Also see
  // 02_TypeData.sql when generate the table
  // these are qestion type available in this site,

  public static Long MULTIPLE_CHOICE = new Long(1);
  public static Long MULTIPLE_CORRECT = new Long(2);
  public static Long MULTIPLE_CHOICE_SURVEY = new Long(3);
  public static Long TRUE_FALSE = new Long(4);
  public static Long ESSAY_QUESTION = new Long(5);
  public static Long FILE_UPLOAD = new Long(6);
  public static Long AUDIO_RECORDING = new Long(7);
  public static Long FILL_IN_BLANK = new Long(8);
  public static Long MATCHING = new Long(9);
  // these are section type available in this site,
  public static Long DEFAULT_SECTION = new Long(21);
  // these are assessment template type available in this site,
  public static Long TEMPLATE_QUIZ = new Long(41);
  public static Long TEMPLATE_HOMEWORK = new Long(42);
  public static Long TEMPLATE_MIDTERM = new Long(43);
  public static Long TEMPLATE_FINAL = new Long(44);
  // these are assessment type available in this site,
  public static Long QUIZ = new Long(61);
  public static Long HOMEWORK = new Long(62);
  public static Long MIDTERM = new Long(63);
  public static Long FINAL = new Long(64);

  private Long typeId;
  private String authority;
  private String domain;
  private String keyword;
  private String description;
  private int status;
  private String createdBy;
  private Date createdDate;
  private String lastModifiedBy;
  private Date lastModifiedDate;

  public TypeFacade() {
  }

  public TypeFacade(String authority, String domain, String keyword)
  {
    this.authority = authority;
    this.domain = domain;
    this.keyword = keyword;
  }

  public TypeFacade(
    String authority, String domain, String keyword, String description)
  {
    this.authority = authority;
    this.domain = domain;
    this.keyword = keyword;
    this.description = description;
  }

  public TypeFacade(String authority, String domain,
                  String keyword, String description,
                  int status,
                  String createdBy, Date createdDate,
                  String lastModifiedBy, Date lastModifiedDate) {
    this.authority = authority;
    this.domain = domain;
    this.keyword = keyword;
    this.description = description;
    this.status = status;
    this.createdBy = createdBy;
    this.createdDate = createdDate;
    this.lastModifiedBy = lastModifiedBy;
    this.lastModifiedDate = lastModifiedDate;
  }

  public TypeFacade(TypeIfc typeData){
    this.typeId = typeData.getTypeId();
    this.authority = typeData.getAuthority();
    this.domain = typeData.getDomain();
    this.keyword = typeData.getKeyword();
    this.description = typeData.getDescription();
    this.status = typeData.getStatus();
    this.createdBy = typeData.getCreatedBy();
    this.createdDate = typeData.getCreatedDate();
    this.lastModifiedBy = typeData.getLastModifiedBy();
    this.lastModifiedDate = typeData.getLastModifiedDate();
  }

  public void setTypeId(Long typeId){
    this.typeId = typeId;
  }

  public Long getTypeId(){
    return this.typeId;
  }

  public String getAuthority(){
      return this.authority;
  }

  public void setAuthority(String authority){
      this.authority = authority;
  }
  public String getDomain(){
      return this.domain;
  }

  public void setDomain(String domain){
      this.domain = domain;
  }

  /**
   * Return the keyword for this ItemType.
   */
  public String getKeyword(){
      return this.keyword;
  }

  /**
   * Set the keyword for this ItemType.
   */
  public void setKeyword(String keyword){
      this.keyword = keyword;
  }

  public String getDescription(){
      return this.description;
  }

  public void setDescription(String description){
      this.description = description;
  }

  /**
   * Return the status for this ItemType.
   */
  public int getStatus(){
      return this.status;
  }

  /**
   * Set the status for this ItemType.
   */
  public void setStatus(int status){
      this.status = status;
  }

  /**
   * Return the createdBy for this ItemType.
   */
  public String getCreatedBy(){
      return this.createdBy;
  }

  /**
   * Set the createdBy for this ItemType.
   */
  public void setCreatedBy (String createdBy){
      this.createdBy = createdBy;
  }

  /**
   * Return the createdDate for this ItemType.
   */
  public Date getCreatedDate(){
      return this.createdDate;
  }

  /**
   * Set the createdDate for this ItemType.
   */
  public void setCreatedDate (Date createdDate){
      this.createdDate = createdDate;
  }

  /**
   * Return the lastModifiedBy for this ItemType.
   */
  public String getLastModifiedBy(){
      return this.lastModifiedBy;
  }
  /**
   * Set the lastModifiedBy for this ItemType.
   */
  public void setLastModifiedBy (String lastModifiedBy){
      this.lastModifiedBy = lastModifiedBy;
  }

  /**
   * Return the lastModifiedDate for this ItemType.
   */
  public Date getLastModifiedDate(){
      return this.lastModifiedDate;
  }

  /**
   * Set the lastModifiedDate for this ItemType.
   */
  public void setLastModifiedDate (Date lastModifiedDate){
      this.lastModifiedDate = lastModifiedDate;
  }

  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
  }

  private void readObject(java.io.ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    in.defaultReadObject();
  }

}