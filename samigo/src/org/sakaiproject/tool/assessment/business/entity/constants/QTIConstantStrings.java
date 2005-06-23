/**********************************************************************************
* $HeadURL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2003-2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.assessment.business.entity.constants;

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
  public static String QUESTESTINTEROP = "questestinterop";
  public static String OBJECTBANK = "objectbank";
  public static String ASSESSMENT = "assessment";
  public static String SECTION = "section";
  public static String ITEM = "item"; // v 1.2
  public static String ASSESSMENTITEM = "assessmentItem"; // v 2.0


  // Assessment Element
  public static String QTICOMMENT = "qticomment";
  public static String DURATION = "duration";
  public static String QTIMETADATA = "qtimetadata";
  public static String OBJECTIVES = "objectives";
  public static String RUBRIC = "rubric";
  public static String ASSESSMENTCONTROL = "assessmentcontrol";
  public static String PRESENTATION_MATERIAL = "presentation_material";
  public static String OUTCOMES_PROCESSING = "outcomes_processing";
  public static String ASSESSPROC_EXTENSION = "assessproc_extension";
  public static String ASSESSFEEDBACK = "assessfeedback";
  public static String SELECTION_ORDERING = "selection_ordering";
  public static String REFERENCE = "reference";

  // Section Element
  public static String SECTIONREF = "sectionref";
  public static String SECTIONPRECONDITION = "sectionprecondition";
  public static String SECTIONPOSTCONDITION = "sectionpostcondition";
  public static String SECTIONCONTROL = "sectioncontrol";
  public static String SECTIONPROC_EXTENSTION = "sectionproc_extension";
  public static String SECTIONFEEDBACK = "sectionfeedback";

  // Item Element, version 1.2
  public static String ITEMIDENT = "ident";
  public static String ITEMREF = "itemref";
  public static String ITEMMETADATA = "itemmetadata";
  public static String ITEMPRECONDITION = "itemprecondition";
  public static String ITEMPOSTCONDITION = "itempostcondition";
  public static String ITEMCONTROL = "itemcontrol";
  public static String ITEMRUBRIC = "itemrubric";
  public static String PRESENTATON = "presentation";
  public static String FLOW = "flow";
  public static String RESPONSE_LID = "response_lid";
  public static String RESPONSE_XY = "response_xy";
  public static String RESPONSE_STR = "response_str";
  public static String RESPONSE_NUM = "response_num";
  public static String RESPONSE_GRP = "response_grp";
  public static String RENDER_CHOICE = "render_choice";
  public static String RENDER_HOTSPOT = "render_hotspot";
  public static String RENDER_FIB = "render_fib";
  public static String RENDER_SLIDER = "render_slider";
  public static String RESPONSE_LABEL = "response_label";
  public static String FLOW_LABEL = "flow_label";
  public static String RESPROCESSING = "resprocessing";
  public static String OUTCOMES = "outcomes";
  public static String RESPCONDITION = "respcondition";
  public static String ITEMPROC_EXTENSION = "itemfeedback";
  public static String ITEMFEEDBACK = "itemfeedback";
  public static String SOLUTION = "solution";
  public static String SOLUTIONMATERIAL = "solutionmaterial";
  public static String HINT = "hint";
  public static String HINTMATERIAL = "hintmaterial";

  // assessmentItem element, version 2.0
  public static String AITEM_IDENT = "identifier";
  public static String AITEM_ADAPT = "adaptive";
  public static String AITEM_TDEPT = "timeDependent";
  public static String AITEM_SHOWHIDE = "showHide";
  public static String AITEM_RESPONSIF = "responseIf";
  public static String AITEM_RESPONSE_ELIF = "responseElseIf";
  public static String AITEM_OUTCOME = "outcomeDeclaration";
  public static String AITEM_DEFAULT = "defaultValue";
  public static String AITEM_LOWER = "lowerBound";
  public static String AITEM_UPPER = "upperBound";
  public static String AITEM_MODAL = "modalFeedback";
  public static String AITEM_BODY = "itemBody";
  public static String AITEM_RESPONSEDECL = "responseDeclarations ";
  public static String AITEM_INTER = "interactions";
  public static String AITEM_RESPCOND = "responseCondition";
  public static String AITEM_MATCHGRP = "matchGroup";
  public static String AITEM_ACHOICE = "associableChoice";
  public static String AITEM_MATCHMAX = "matchMax";

  // Common Data Elements
  public static String MATERIAL = "material";
  public static String ALTMATERIAL = "altmaterial";
  public static String MATTEXT = "mattext";
  public static String MATEMTEXT = "matemtext";
  public static String MATBREAK = "matbreak";
  public static String MATIMAGE = "matimage";
  public static String MATAUDIO = "mataudio";
  public static String MATVIDEO = "matvideo";
  public static String MATAPPLET = "matapplet";
  public static String MATAPPLICATION = "matapplication";
  public static String MATREF = "matref";
  public static String MAT_EXTENSION = "mat_extension";
  public static String FLOW_MAT = "flow_mat";
  public static String DECVAR = "decvar";
  public static String INTERPRETVAR = "interpretvar";
  public static String SETVAR = "setvar";
  public static String DISPLAYFEEDBACK = "displayfeedback";
  public static String CONDITIONVAR = "conditionvar";
  public static String MATERIAL_REF = "material_ref";

  // Attribute names
  public static String IDENT = "ident";
  public static String TITLE = "title";
  public static String LINKREFID = "linkrefid";
  public static String VIEW = "view";
  public static String SOLUTIONSWITCH = "solutionswitch";
  public static String HINTSWITCH = "hintswitch";
  public static String FEEDBACKSWITCH = "feedbackswitch";
  public static String LABEL = "label";
  public static String MAXATTEMPTS = "maxattempts";
  public static String XML_LANG = "xml:lang";
  public static String WIDTH = "width";
  public static String HEIGHT = "height";
  public static String X0 = "x0";
  public static String Y0 = "y0";
  public static String RCARDINALITY = "rcardinality";
  public static String RTIMING = "rtiming";
  public static String NUMTYPE = "numtype";
  public static String SHUFFLE = "shuffle";
  public static String MINNUMBER = "minnumber";
  public static String MAXNUMBER = "maxnumber";
  public static String SHOWDRAW = "showdraw";
  public static String CHARSET = "charset";
  public static String ENCODING = "encoding";
  public static String ROWS = "rows";
  public static String COLUMNS = "columns";
  public static String MAXCHARS = "maxchars";
  public static String PROMPT = "prompt";
  public static String FIBTYPE = "fibtype";
  public static String ORIENTATION = "orientation";
  public static String LOWERBOUND = "lowerbound";
  public static String UPPERBOUND = "upperbound";
  public static String STEP = "step";
  public static String STARTVAL = "startval";
  public static String STEPLABEL = "steplabel";

  // response_label attribute
  public static String LABELREFID = "labelrefid";
  public static String RSHUFFLE = "rshuffle";
  public static String RAREA = "rarea";
  public static String RRANGE = "rrange";
  public static String MATCH_GROUP = "match_group";
  public static String MATCH_MAX = "match_max";
  public static String CLASS = "class";
  public static String SCOREMODEL = "scoremodel";
  public static String CONTINUE = "continue";
  public static String FEEDBACKSTYLE = "feedbackstyle";
  public static String TEXTTYPLE = "texttype";
  public static String URI = "uri";
  public static String ENTITYREF = "entityref";
  public static String XML_SPACE = "xml:space";
  public static String EMBEDDED = "embedded";
  public static String AUDIOTYPE = "audiotype";
  public static String VOCAB_TYPE = "vocab_type";
  public static String VARTYPE = "vartype";
  public static String DEFAULTVAL = "defaultval";
  public static String CUTVALUE = "CUTVALUE";
  public static String MINVALUE = "minvalue";
  public static String MAXVALUE = "maxvalue";
  public static String MEMBERS = "members";
  public static String ACTION = "action";
  public static String RESPIDENT = "respident";
  public static String CASE = "case";
  public static String INDEX = "index";
  public static String AREATYPE = "areatype";
  public static String SETMATCH = "setmatch";

  /**
   * Result Report
   */

  // Assessment Result Elements
  public static String QTI_RESULT_REPORT = "qti_result_report";
  public static String RESULT = "result";
  public static String CONTEXT = "context";
  public static String SUMMARY_RESULT = "summary_result";
  public static String ASSESSMENT_RESULT = "assessment_result";
  public static String SECTION_RESULT = "section_result";
  public static String ITEM_RESULT = "item_result";
  public static String EXTENSION_RESULT = "extension_result";
  public static String NAME = "name";
  public static String GENERIC_IDENTIFIER = "generic_identifier";
  public static String DATE = "date";
  public static String TYPE_LABEL = "type_label";
  public static String STATUS = "status";
  public static String SCORE = "score";
  public static String EXTENSION_SUMMARY_RESULT = "extension_summary_result";
  public static String GRADE = "grade";
  public static String ASI_METADATA = "asi_metadata";
  public static String ASI_DESCRIPTION = "asi_description";
  public static String CONTROL = "control";
  public static String FEEDBACK_DISPLAYED = "feedback_displayed";
  public static String NUM_ITEMS = "num_items";
  public static String NUM_SECTIONS = "num_sections";
  public static String NUM_ITEMS_PRESENTED = "num_items_presented";
  public static String NUM_IMTES_ATTEMPTED = "num_items_attempted";
  public static String EXTENSION_ASSESSMENT_RESULT =
    "extension_assessment_result";

  // Section Result Elements
  // Item Result Elements
  public static String RESPONSE = "response";
  public static String RESPONSE_FORM = "response_form";
  public static String NUM_ATTEMPTS = "num_attempts";
  public static String RESPONSE_VALUE = "response_value";
  public static String EXTENSION_ITEM_RESULT = "extension_item_result";

  // Common Data Elements
  // Result Report Attribute
  public static String ASI_TITLE = "asi_title";
  public static String IDENT_REF = "ident_ref";
  public static String PRESENTED = "presented";

  //Selection and Ordering
  public static String SELECTION = "selection";
  public static String ORDER = "order";
  public static String SOURCEBANK_REF = "sourcebank_ref";
  public static String SELECTION_NUMBER = "selection_number";
  public static String ORDER_TYPE = "order_type";

  //QTIMetaData
  public static String QTIMETADATAFIELD = "qtimetadatafield";
  public static String FIELDLABEL = "fieldlabel";
  public static String FIELDENTRY = "fieldentry";
}
/**********************************************************************************
 *
 * $Header: /cvs/sakai2/sam/src/org/sakaiproject/tool/assessment/business/entity/constants/QTIConstantStrings.java,v 1.4 2005/05/17 23:00:36 esmiley.stanford.edu Exp $
 *
 ***********************************************************************************/
