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



package org.sakaiproject.tool.assessment.data.ifc.shared;
import java.util.Date;

public interface TypeIfc extends java.io.Serializable {
  // This has the exact same list as TypeFacade. Please keep both list updated
  public static final Long MULTIPLE_CHOICE = 1L;
  public static final Long MULTIPLE_CORRECT = 2L;
  public static final Long MULTIPLE_CHOICE_SURVEY = 3L;
  public static final Long TRUE_FALSE = 4L;
  public static final Long ESSAY_QUESTION = 5L;
  public static final Long FILE_UPLOAD = 6L;
  public static final Long AUDIO_RECORDING = 7L;
  public static final Long FILL_IN_BLANK = 8L;
  public static final Long MATCHING = 9L;
  public static final Long FILL_IN_NUMERIC = 11L;
  public static final Long MULTIPLE_CORRECT_SINGLE_SELECTION = 12L;
  public static final Long MATRIX_CHOICES_SURVEY = 13L;
  public static final Long EXTENDED_MATCHING_ITEMS = 14L;
  public static final Long CALCULATED_QUESTION = 15L; // CALCULATED_QUESTION
  public static final Long IMAGEMAP_QUESTION = 16L; // IMAGEMAP_QUESTION
  // these are section type available in this site,
  public static final Long DEFAULT_SECTION = 21L;
  // these are assessment template type available in this site,
  public static final Long TEMPLATE_SYSTEM_DEFINED = 142L;
  public static final Long TEMPLATE_QUIZ = 41L;
  public static final Long TEMPLATE_HOMEWORK = 42L;
  public static final Long TEMPLATE_MIDTERM = 43L;
  public static final Long TEMPLATE_FINAL = 44L;
  // these are assessment type available in this site,
  public static final Long QUIZ = 61L;
  public static final Long HOMEWORK = 62L;
  public static final Long MIDTERM = 63L;
  public static final Long FINAL = 64L;
  public static final String SITE_AUTHORITY = "stanford.edu";
  public static final String DOMAIN_ASSESSMENT_ITEM = "assessment.item";
  
  enum TypeId{
	  MULTIPLE_CHOICE_ID(MULTIPLE_CHOICE);
	  int id;
	  TypeId(Long l){
		  id = l.intValue();
	  }
	  public int id(){
		  return id;
	  }
  }

  Long getTypeId();

  void setTypeId(Long typeId);

  String getAuthority();

  void setAuthority(String authority);

  String getDomain();

  void setDomain(String domain);

  String getKeyword();

  void setKeyword(String keyword);

  String getDescription();

  void setDescription(String description);

  int getStatus();

  void setStatus(int status);

  String getCreatedBy();

  void setCreatedBy (String createdBy);

  Date getCreatedDate();

  void setCreatedDate (Date createdDate);

  String getLastModifiedBy();

  void setLastModifiedBy (String lastModifiedBy);

  Date getLastModifiedDate();

  void setLastModifiedDate (Date lastModifiedDate);

}
