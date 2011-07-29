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
 *       http://www.osedu.org/licenses/ECL-2.0
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
  public static Long MULTIPLE_CHOICE = Long.valueOf(1);
  public static Long MULTIPLE_CORRECT = Long.valueOf(2);
  public static Long MULTIPLE_CHOICE_SURVEY = Long.valueOf(3);
  public static Long TRUE_FALSE = Long.valueOf(4);
  public static Long ESSAY_QUESTION = Long.valueOf(5);
  public static Long FILE_UPLOAD = Long.valueOf(6);
  public static Long AUDIO_RECORDING = Long.valueOf(7);
  public static Long FILL_IN_BLANK = Long.valueOf(8);
  public static Long MATCHING = Long.valueOf(9);
  public static Long FILL_IN_NUMERIC = Long.valueOf(11);
  public static Long MULTIPLE_CORRECT_SINGLE_SELECTION = Long.valueOf(12);
  public static Long MATRIX_CHOICES_SURVEY = Long.valueOf(13);
  // these are section type available in this site,
  public static Long DEFAULT_SECTION = Long.valueOf(21);
  // these are assessment template type available in this site,
  public static Long TEMPLATE_QUIZ = Long.valueOf(41);
  public static Long TEMPLATE_HOMEWORK = Long.valueOf(42);
  public static Long TEMPLATE_MIDTERM = Long.valueOf(43);
  public static Long TEMPLATE_FINAL = Long.valueOf(44);
  // these are assessment type available in this site,
  public static Long QUIZ = Long.valueOf(61);
  public static Long HOMEWORK = Long.valueOf(62);
  public static Long MIDTERM = Long.valueOf(63);
  public static Long FINAL = Long.valueOf(64);
  public static String SITE_AUTHORITY = "stanford.edu";
  public static String DOMAIN_ASSESSMENT_ITEM = "assessment.item";

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
