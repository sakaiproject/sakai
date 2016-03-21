/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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



package org.sakaiproject.tool.assessment.data.ifc.assessment;


public interface ItemMetaDataIfc
    extends java.io.Serializable
{
  public static final String OBJECTIVE = "OBJECTIVE";
  public static final String KEYWORD= "KEYWORD";
  public static final String RUBRIC= "RUBRIC";
  public static final String RANDOMIZE= "RANDOMIZE";
  public static final String MCMS_PARTIAL_CREDIT = "MCMS_PARTIAL_CREDIT";
  public static final String SCALENAME= "SCALENAME";
  public static final String PARTID= "PARTID";
  public static final String POOLID= "POOLID";
  public static final String TIMEALLOWED= "TIMEALLOWED";
  public static final String NUMATTEMPTS= "NUMATTEMPTS";
  public static final String CASE_SENSITIVE_FOR_FIB= "CASE_SENSITIVE";
  public static final String MUTUALLY_EXCLUSIVE_FOR_FIB= "MUTUALLY_EXCLUSIVE";
  public static final String IGNORE_SPACES_FOR_FIB= "IGNORE_SPACES";
  //public static final String CASE_SENSITIVE_FOR_FIN= "CASE_SENSITIVE_FOR_FIN";
  //public static final String MUTUALLY_EXCLUSIVE_FOR_FIN= "MUTUALLY_EXCLUSIVE_FOR_FIN";
  //sam-939
  public static final String ADD_TO_FAVORITES_MATRIX = "ADD_TO_FAVORITES";
  public static final String ADD_COMMENT_MATRIX = "ADD_COMMENT_MATRIX";
  public static final String FORCE_RANKING = "FORCE_RANKING";
  public static final String MX_SURVEY_QUESTION_COMMENTFIELD = "MX_SURVEY_QUESTION_COMMENTFIELD";
  public static final String MX_SURVEY_RELATIVE_WIDTH = "MX_SURVEY_RELATIVE_WIDTH";

  public static final String REQUIRE_ALL_OK = "REQUIRE_ALL_OK";  //this is for Image map questions.
  public static final String IMAGE_MAP_SRC = "IMAGE_MAP_SRC";  //this is for Image map questions.
  
  // used in QTI import/export
  // possible entries YES, AGREE, UNDECIDED, AVERAGE, STRONGLY_AGREE, EXCELLENT, 5, 10 
 
  public static final String PREDEFINED_SCALE= "PREDEFINED_SCALE";
  public static final String SURVEY_YES= "YES";
  public static final String SURVEY_AGREE= "AGREE";
  public static final String SURVEY_UNDECIDED= "UNDECIDED";
  public static final String SURVEY_AVERAGE= "AVERAGE";
  public static final String SURVEY_STRONGLY_AGREE= "STRONGLY_AGREE";
  public static final String SURVEY_EXCELLENT= "EXCELLENT";
  public static final String SURVEY_5= "5";
  public static final String SURVEY_10= "10";

  public static final String SURVEY_YESNO= "YESNO";
  public static final String SURVEY_SCALEFIVE= "SCALEFIVE";
  public static final String SURVEY_SCALETEN= "SCALETEN";


  Long getId();

  void setId(Long id);

  ItemDataIfc getItem();

  void setItem(ItemDataIfc item);

  String getLabel();

  void setLabel(String label);

  String getEntry();

  void setEntry(String entry);

}
