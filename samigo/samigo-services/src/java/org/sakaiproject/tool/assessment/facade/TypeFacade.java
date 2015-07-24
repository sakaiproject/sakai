/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008, 2009 The Sakai Foundation
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


package org.sakaiproject.tool.assessment.facade;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;

public class TypeFacade implements Serializable, TypeIfc{

  // please make sure that the value match with the one in the Type table. Also see
  // 02_TypeData.sql when generate the table
  // these are question type available in this site,

  /**
	 * 
	 */
	private static final long serialVersionUID = -3400550482739705371L;
	  public static final Long MULTIPLE_CHOICE =  Long.valueOf(1);
	  public static final Long MULTIPLE_CORRECT = Long.valueOf(2);
	  public static final Long MULTIPLE_CHOICE_SURVEY = Long.valueOf(3);
	  public static final Long TRUE_FALSE = Long.valueOf(4);
	  public static final Long ESSAY_QUESTION = Long.valueOf(5);
	  public static final Long FILE_UPLOAD = Long.valueOf(6);
	  public static final Long AUDIO_RECORDING = Long.valueOf(7);
	  public static final Long FILL_IN_BLANK = Long.valueOf(8);
	  public static final Long FILL_IN_NUMERIC = Long.valueOf(11);
	  public static final Long MATCHING = Long.valueOf(9);
	  public static final Long MULTIPLE_CORRECT_SINGLE_SELECTION = Long.valueOf(12);
	  public static final Long IMAGEMAP_QUESTION = Long.valueOf(16); // IMAGEMAP_QUESTION
	  // these are section type available in this site,
	  public static final Long DEFAULT_SECTION = Long.valueOf(21);
	  // these are assessment template type available in this site,
	  public static final Long TEMPLATE_QUIZ = Long.valueOf(41);
	  public static final Long TEMPLATE_HOMEWORK = Long.valueOf(42);
	  public static final Long TEMPLATE_MIDTERM = Long.valueOf(43);
	  public static final Long TEMPLATE_FINAL = Long.valueOf(44);
	  // these are assessment type available in this site,
	  public static final Long QUIZ = Long.valueOf(61);
	  public static final Long HOMEWORK = Long.valueOf(62);
	  public static final Long MIDTERM = Long.valueOf(63);
	  public static final Long FINAL = Long.valueOf(64);

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
