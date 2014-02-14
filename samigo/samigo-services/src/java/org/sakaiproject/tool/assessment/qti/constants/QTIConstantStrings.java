/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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



package org.sakaiproject.tool.assessment.qti.constants;

/**
 * <p>This class contains all qti tag names and attribute names  frequently used
 * in the java code.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author rshastri
 * @author Ed Smiley esmiley@stanford.edu
 * @version $Id$
 */
public class QTIConstantStrings
{
  // QUESTESTINTEROP ELEMENT
  public static final String QUESTESTINTEROP = "questestinterop";
  public static final String OBJECTBANK = "objectbank";
  public static final String ASSESSMENT = "assessment";
  public static final String SECTION = "section";
  public static final String ITEM = "item"; // v 1.2
  public static final String ASSESSMENTITEM = "assessmentItem"; // v 2.0


  // Assessment Element
  public static final String QTICOMMENT = "qticomment";
  public static final String DURATION = "duration";
  public static final String QTIMETADATA = "qtimetadata";
  public static final String OBJECTIVES = "objectives";
  public static final String RUBRIC = "rubric";
  public static final String ASSESSMENTCONTROL = "assessmentcontrol";
  public static final String PRESENTATION_MATERIAL = "presentation_material";
  public static final String OUTCOMES_PROCESSING = "outcomes_processing";
  public static final String ASSESSPROC_EXTENSION = "assessproc_extension";
  public static final String ASSESSFEEDBACK = "assessfeedback";
  public static final String SELECTION_ORDERING = "selection_ordering";
  public static final String REFERENCE = "reference";

  // Section Element
  public static final String SECTIONREF = "sectionref";
  public static final String SECTIONPRECONDITION = "sectionprecondition";
  public static final String SECTIONPOSTCONDITION = "sectionpostcondition";
  public static final String SECTIONCONTROL = "sectioncontrol";
  public static final String SECTIONPROC_EXTENSTION = "sectionproc_extension";
  public static final String SECTIONFEEDBACK = "sectionfeedback";

  // Item Element, version 1.2
  public static final  String ITEMIDENT = "ident";
  public static final  String ITEMREF = "itemref";
  public static final  String ITEMMETADATA = "itemmetadata";
  public static final  String ITEMPRECONDITION = "itemprecondition";
  public static final  String ITEMPOSTCONDITION = "itempostcondition";
  public static final  String ITEMCONTROL = "itemcontrol";
  public static final  String ITEMRUBRIC = "itemrubric";
  public static final  String PRESENTATON = "presentation";
  public static final  String FLOW = "flow";
  public static final  String RESPONSE_LID = "response_lid";
  public static final  String RESPONSE_XY = "response_xy";
  public static final  String RESPONSE_STR = "response_str";
  public static final  String RESPONSE_NUM = "response_num";
  public static final  String RESPONSE_GRP = "response_grp";
  public static final  String RENDER_CHOICE = "render_choice";
  public static final  String RENDER_HOTSPOT = "render_hotspot";
  public static final  String RENDER_FIB = "render_fib";
  public static final  String RENDER_FIN = "render_fin";
  public static final  String RENDER_SLIDER = "render_slider";
  public static final  String RESPONSE_LABEL = "response_label";
  public static final  String FLOW_LABEL = "flow_label";
  public static final  String RESPROCESSING = "resprocessing";
  public static final  String OUTCOMES = "outcomes";
  public static final  String RESPCONDITION = "respcondition";
  public static final  String ITEMPROC_EXTENSION = "itemfeedback";
  public static final  String ITEMFEEDBACK = "itemfeedback";
  public static final  String SOLUTION = "solution";
  public static final  String SOLUTIONMATERIAL = "solutionmaterial";
  public static final  String HINT = "hint";
  public static final  String HINTMATERIAL = "hintmaterial";

  // assessmentItem element, version 2.0
  public static final  String AITEM_IDENT = "identifier";
  public static final  String AITEM_ADAPT = "adaptive";
  public static final  String AITEM_TDEPT = "timeDependent";
  public static final  String AITEM_SHOWHIDE = "showHide";
  public static final  String AITEM_RESPONSIF = "responseIf";
  public static final  String AITEM_RESPONSE_ELIF = "responseElseIf";
  public static final  String AITEM_OUTCOME = "outcomeDeclaration";
  public static final  String AITEM_DEFAULT = "defaultValue";
  public static final  String AITEM_LOWER = "lowerBound";
  public static final  String AITEM_UPPER = "upperBound";
  public static final  String AITEM_MODAL = "modalFeedback";
  public static final  String AITEM_BODY = "itemBody";
  public static final  String AITEM_RESPONSEDECL = "responseDeclarations ";
  public static final  String AITEM_INTER = "interactions";
  public static final  String AITEM_RESPCOND = "responseCondition";
  public static final  String AITEM_MATCHGRP = "matchGroup";
  public static final  String AITEM_ACHOICE = "associableChoice";
  public static final  String AITEM_MATCHMAX = "matchMax";

  // Common Data Elements
  public static final  String MATERIAL = "material";
  public static final  String ALTMATERIAL = "altmaterial";
  public static final  String MATTEXT = "mattext";
  public static final  String MATEMTEXT = "matemtext";
  public static final  String MATBREAK = "matbreak";
  public static final  String MATIMAGE = "matimage";
  public static final  String MATAUDIO = "mataudio";
  public static final  String MATVIDEO = "matvideo";
  public static final  String MATAPPLET = "matapplet";
  public static final  String MATAPPLICATION = "matapplication";
  public static final  String MATREF = "matref";
  public static final  String MAT_EXTENSION = "mat_extension";
  public static final  String FLOW_MAT = "flow_mat";
  public static final  String DECVAR = "decvar";
  public static final  String INTERPRETVAR = "interpretvar";
  public static final  String SETVAR = "setvar";
  public static final  String DISPLAYFEEDBACK = "displayfeedback";
  public static final  String CONDITIONVAR = "conditionvar";
  public static final  String MATERIAL_REF = "material_ref";

  // Attribute names
  public static final  String IDENT = "ident";
  public static final  String TITLE = "title";
  public static final  String LINKREFID = "linkrefid";
  public static final  String VIEW = "view";
  public static final  String SOLUTIONSWITCH = "solutionswitch";
  public static final  String HINTSWITCH = "hintswitch";
  public static final  String FEEDBACKSWITCH = "feedbackswitch";
  public static final  String LABEL = "label";
  public static final  String MAXATTEMPTS = "maxattempts";
  public static final  String XML_LANG = "xml:lang";
  public static final  String WIDTH = "width";
  public static final  String HEIGHT = "height";
  public static final  String X0 = "x0";
  public static final  String Y0 = "y0";
  public static final  String RCARDINALITY = "rcardinality";
  public static final  String RTIMING = "rtiming";
  public static final  String NUMTYPE = "numtype";
  public static final  String SHUFFLE = "shuffle";
  public static final  String MINNUMBER = "minnumber";
  public static final  String MAXNUMBER = "maxnumber";
  public static final  String SHOWDRAW = "showdraw";
  public static final  String CHARSET = "charset";
  public static final  String ENCODING = "encoding";
  public static final  String ROWS = "rows";
  public static final  String COLUMNS = "columns";
  public static final  String MAXCHARS = "maxchars";
  public static final  String PROMPT = "prompt";
  public static final  String FIBTYPE = "fibtype";
  public static final  String FINTYPE = "fintype";
  public static final  String EMITYPE = "emitype";
  
  public static final  String ORIENTATION = "orientation";
  public static final  String LOWERBOUND = "lowerbound";
  public static final  String UPPERBOUND = "upperbound";
  public static final  String STEP = "step";
  public static final  String STARTVAL = "startval";
  public static final  String STEPLABEL = "steplabel";

  // response_label attribute
  public static final  String LABELREFID = "labelrefid";
  public static final  String RSHUFFLE = "rshuffle";
  public static final  String RAREA = "rarea";
  public static final  String RRANGE = "rrange";
  public static final  String MATCH_GROUP = "match_group";
  public static final  String MATCH_MAX = "match_max";
  public static final  String CLASS = "class";
  public static final  String SCOREMODEL = "scoremodel";
  public static final  String CONTINUE = "continue";
  public static final  String FEEDBACKSTYLE = "feedbackstyle";
  public static final  String TEXTTYPLE = "texttype";
  public static final  String URI = "uri";
  public static final  String ENTITYREF = "entityref";
  public static final  String XML_SPACE = "xml:space";
  public static final  String EMBEDDED = "embedded";
  public static final  String AUDIOTYPE = "audiotype";
  public static final  String VOCAB_TYPE = "vocab_type";
  public static final  String VARTYPE = "vartype";
  public static final  String DEFAULTVAL = "defaultval";
  public static final  String CUTVALUE = "CUTVALUE";
  public static final  String MINVALUE = "minvalue";
  public static final  String MAXVALUE = "maxvalue";
  public static final  String MEMBERS = "members";
  public static final  String ACTION = "action";
  public static final  String RESPIDENT = "respident";
  public static final  String CASE = "case";
  public static final  String INDEX = "index";
  public static final  String AREATYPE = "areatype";
  public static final  String SETMATCH = "setmatch";

  /**
   * Result Report
   */

  // Assessment Result Elements
  public static final  String QTI_RESULT_REPORT = "qti_result_report";
  public static final  String RESULT = "result";
  public static final  String CONTEXT = "context";
  public static final  String SUMMARY_RESULT = "summary_result";
  public static final  String ASSESSMENT_RESULT = "assessment_result";
  public static final  String SECTION_RESULT = "section_result";
  public static final  String ITEM_RESULT = "item_result";
  public static final  String EXTENSION_RESULT = "extension_result";
  public static final  String NAME = "name";
  public static final  String GENERIC_IDENTIFIER = "generic_identifier";
  public static final  String DATE = "date";
  public static final  String TYPE_LABEL = "type_label";
  public static final  String STATUS = "status";
  public static final  String SCORE = "score";
  public static final  String DISCOUNT = "discount";
  public static final  String EXTENSION_SUMMARY_RESULT = "extension_summary_result";
  public static final  String GRADE = "grade";
  public static final  String ASI_METADATA = "asi_metadata";
  public static final  String ASI_DESCRIPTION = "asi_description";
  public static final  String CONTROL = "control";
  public static final  String FEEDBACK_DISPLAYED = "feedback_displayed";
  public static final  String NUM_ITEMS = "num_items";
  public static final  String NUM_SECTIONS = "num_sections";
  public static final  String NUM_ITEMS_PRESENTED = "num_items_presented";
  public static final  String NUM_IMTES_ATTEMPTED = "num_items_attempted";
  public static final  String EXTENSION_ASSESSMENT_RESULT =
    "extension_assessment_result";

  // Section Result Elements
  // Item Result Elements
  public static final  String RESPONSE = "response";
  public static final  String RESPONSE_FORM = "response_form";
  public static final  String NUM_ATTEMPTS = "num_attempts";
  public static final  String RESPONSE_VALUE = "response_value";
  public static final  String EXTENSION_ITEM_RESULT = "extension_item_result";

  // Common Data Elements
  // Result Report Attribute
  public static final  String ASI_TITLE = "asi_title";
  public static final  String IDENT_REF = "ident_ref";
  public static final  String PRESENTED = "presented";

  //Selection and Ordering
  public static final  String SELECTION = "selection";
  public static final  String ORDER = "order";
  public static final  String SOURCEBANK_REF = "sourcebank_ref";
  public static final  String SELECTION_NUMBER = "selection_number";
  public static final  String ORDER_TYPE = "order_type";

  //QTIMetaData
  public static final  String QTIMETADATAFIELD = "qtimetadatafield";
  public static final  String FIELDLABEL = "fieldlabel";
  public static final  String FIELDENTRY = "fieldentry";
}


