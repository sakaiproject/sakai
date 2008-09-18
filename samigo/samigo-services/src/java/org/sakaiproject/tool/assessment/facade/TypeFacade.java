/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/facade/TypeFacade.java $
 * $Id: TypeFacade.java 9273 2006-05-10 22:34:28Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

  /**
	 * 
	 */
	private static final long serialVersionUID = -3400550482739705371L;
public static final Long MULTIPLE_CHOICE = new Long(1);
  public static final Long MULTIPLE_CORRECT = new Long(2);
  public static final Long MULTIPLE_CHOICE_SURVEY = new Long(3);
  public static final Long TRUE_FALSE = new Long(4);
  public static final Long ESSAY_QUESTION = new Long(5);
  public static final Long FILE_UPLOAD = new Long(6);
  public static final Long AUDIO_RECORDING = new Long(7);
  public static final Long FILL_IN_BLANK = new Long(8);
  public static final Long FILL_IN_NUMERIC = new Long(11);
  public static final Long MATCHING = new Long(9);
  public static final Long MULTIPLE_CORRECT_SINGLE_SELECTION = new Long(12);
  // these are section type available in this site,
  public static final Long DEFAULT_SECTION = new Long(21);
  // these are assessment template type available in this site,
  public static final Long TEMPLATE_QUIZ = new Long(41);
  public static final Long TEMPLATE_HOMEWORK = new Long(42);
  public static final Long TEMPLATE_MIDTERM = new Long(43);
  public static final Long TEMPLATE_FINAL = new Long(44);
  // these are assessment type available in this site,
  public static final Long QUIZ = new Long(61);
  public static final Long HOMEWORK = new Long(62);
  public static final Long MIDTERM = new Long(63);
  public static final Long FINAL = new Long(64);

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
