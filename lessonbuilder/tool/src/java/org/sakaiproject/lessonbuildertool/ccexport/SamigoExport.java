/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Author: Charles Hedrick, hedrick@rutgers.edu
 *
 * Copyright (c) 2013 Rutgers, the State University of New Jersey
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

package org.sakaiproject.lessonbuildertool.ccexport;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringEscapeUtils;

import uk.org.ponder.messageutil.MessageLocator;

import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.service.LessonEntity;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.questionpool.QuestionPoolDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.facade.QuestionPoolFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.facade.QuestionPoolFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.util.FormattedText;                                     
import org.sakaiproject.user.cover.UserDirectoryService;

/*
 * set up as a singleton. But CCexport is not.
 */
@Slf4j
public class SamigoExport {
    PublishedAssessmentService pubService = new PublishedAssessmentService();
    AssessmentService assessmentService = new AssessmentService();

    private static SimplePageToolDao simplePageToolDao;

    public void setSimplePageToolDao(Object dao) {
	simplePageToolDao = (SimplePageToolDao) dao;
    }

    static PublishedAssessmentFacadeQueriesAPI publishedAssessmentFacadeQueries;

    public void setPublishedAssessmentFacadeQueries(
        PublishedAssessmentFacadeQueriesAPI p) {
    	this.publishedAssessmentFacadeQueries = p;
    }

    static QuestionPoolFacadeQueriesAPI questionPoolFacadeQueries;

    public void setQuestionPoolFacadeQueries(
	QuestionPoolFacadeQueriesAPI p) {
    	this.questionPoolFacadeQueries = p;
    }

    static MessageLocator messageLocator = null;
    public void setMessageLocator(MessageLocator m) {
	messageLocator = m;
    }

    public void init () {
	// currently nothing to do

	log.info("init()");

    }

    public void destroy()
    {
	log.info("destroy()");
    }

    // to create bean. the bean is used only to call the pseudo-static
    // methods such as getEntitiesInSite. So type, id, etc are left uninitialized

    protected SamigoExport() {
    }

    // find topics in site, but organized by forum
    public List<String> getEntitiesInSite(String siteId) {

	List<PublishedAssessmentFacade> plist = pubService.getBasicInfoOfAllPublishedAssessments2("title", true, siteId);

	List<String> ret = new ArrayList<String>();

	// security. assume this is only used in places where it's OK, so skip security checks
	for (PublishedAssessmentFacade assessment: plist) {

	    if (assessment.getStatus().equals(AssessmentIfc.ACTIVE_STATUS)) {
		ret.add(LessonEntity.SAM_PUB + "/" + assessment.getPublishedAssessmentId().toString());
	    }


	}

	return ret;
    }

    // are there any items in a pool?

    public List<Long> getAllPools() {

	List<QuestionPoolFacade>pools = questionPoolFacadeQueries.getBasicInfoOfAllPools(UserDirectoryService.getCurrentUser().getId());

	List<Long> ret = new ArrayList<Long>();

	if (pools != null && pools.size() > 0) {
	    for (QuestionPoolDataIfc pool: pools) {
		List<ItemDataIfc> itemList = questionPoolFacadeQueries.getAllItems(pool.getQuestionPoolId());
		if (itemList != null && itemList.size() > 0)
		    ret.add(pool.getQuestionPoolId());
	    }
	}

	return ret;

    }

    public boolean outputEntity(String samigoId, ZipPrintStream out, PrintStream errStream, CCExport ccExport, CCExport.Resource resource, int version) {
	int i = samigoId.indexOf("/");
	String publishedAssessmentString = samigoId.substring(i+1);
	Long publishedAssessmentId = new Long(publishedAssessmentString);

	PublishedAssessmentFacade assessment = pubService.getPublishedAssessment(publishedAssessmentString);

	List<ItemDataIfc> publishedItemList = preparePublishedItemList(assessment);

	// boolean anonymousGrading = assessment.getEvaluationModel().getAnonymousGrading().equals(EvaluationModelIfc.ANONYMOUS_GRADING);

	String assessmentTitle = FormattedText.convertFormattedTextToPlaintext(assessment.getTitle());

	// SortedMap<Long,String> questions = new TreeMap<Long,String>();

	out.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");

	switch (version) {
	case CCExport.V11:
	    out.println("<questestinterop xmlns=\"http://www.imsglobal.org/xsd/ims_qtiasiv1p2\"");
	    out.println("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.imsglobal.org/xsd/ims_qtiasiv1p2 http://www.imsglobal.org/profile/cc/ccv1p1/ccv1p1_qtiasiv1p2p1_v1p0.xsd\">");
	    break;
	case CCExport.V13:
	    out.println("<questestinterop xmlns=\"http://www.imsglobal.org/xsd/ims_qtiasiv1p2\"");
	    out.println("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.imsglobal.org/xsd/ims_qtiasiv1p2 http://www.imsglobal.org/profile/cc/ccv1p3/ccv1p3_qtiasiv1p2p1_v1p0.xsd\">");
	    break;
	default:
	    out.println("<questestinterop xmlns=\"http://www.imsglobal.org/xsd/ims_qtiasiv1p2\"");
	    out.println("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.imsglobal.org/xsd/ims_qtiasiv1p2 http://www.imsglobal.org/profile/cc/ccv1p2/ccv1p2_qtiasiv1p2p1_v1p0.xsd\">");
	}

	out.println("  <assessment ident=\"QDB_1\" title=\"" + StringEscapeUtils.escapeXml(assessmentTitle) + "\">");
	out.println("    <section ident=\"S_1\">");

	outputQuestions(publishedItemList, null, assessmentTitle, out, errStream, ccExport, resource, version);

	out.println("    </section>");
	out.println("  </assessment>");
	out.println("</questestinterop>");

	return true;
   }

    public boolean outputBank(Long poolId, ZipPrintStream out, PrintStream errStream, CCExport ccExport, CCExport.Resource resource, int version) {

	out.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");

	switch (version) {
	case CCExport.V11:
	    out.println("<questestinterop xmlns=\"http://www.imsglobal.org/xsd/ims_qtiasiv1p2\"");
	    out.println("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.imsglobal.org/xsd/ims_qtiasiv1p2 http://www.imsglobal.org/profile/cc/ccv1p1/ccv1p1_qtiasiv1p2p1_v1p0.xsd\">");
	    break;
	case CCExport.V13:
	    out.println("<questestinterop xmlns=\"http://www.imsglobal.org/xsd/ims_qtiasiv1p2\"");
	    out.println("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.imsglobal.org/xsd/ims_qtiasiv1p2 http://www.imsglobal.org/profile/cc/ccv1p3/ccv1p3_qtiasiv1p2p1_v1p0.xsd\">");
	    break;
	default:
	    out.println("<questestinterop xmlns=\"http://www.imsglobal.org/xsd/ims_qtiasiv1p2\"");
	    out.println("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.imsglobal.org/xsd/ims_qtiasiv1p2 http://www.imsglobal.org/profile/cc/ccv1p2/ccv1p2_qtiasiv1p2p1_v1p0.xsd\">");
	}

	out.println("  <objectbank ident=\"QDB_1\">");

	if (version >= CCExport.V13) {
	    // 1.3 or later, specific pool
	    QuestionPoolFacade pool = questionPoolFacadeQueries.getPoolById(poolId);
	    if (pool != null) {
		List<ItemDataIfc> itemList = questionPoolFacadeQueries.getAllItems(poolId);
		if (itemList != null && itemList.size() > 0)
		    outputQuestions(itemList, "pool" + poolId, pool.getTitle(), out, errStream, ccExport, resource, version);
	    }
	} else {
	    // older. all pools at once
	    List<QuestionPoolFacade>pools = questionPoolFacadeQueries.getBasicInfoOfAllPools(UserDirectoryService.getCurrentUser().getId());

	    log.info("pools " + pools.size());

	    if (pools != null && pools.size() > 0) {
		int poolno = 1;
		for (QuestionPoolDataIfc pool: pools) {
		    List<ItemDataIfc> itemList = questionPoolFacadeQueries.getAllItems(pool.getQuestionPoolId());
		    if (itemList != null && itemList.size() > 0)
			outputQuestions(itemList, ("pool" + (poolno++)), pool.getTitle(), out, errStream, ccExport, resource, version);
		}
	    }
	}

	out.println("  </objectbank>");
	out.println("</questestinterop>");

	return true;
   }


    class Feedback {
	String id;
	String text;
    }

    void outputQuestions(List<ItemDataIfc> itemList, String assessmentSeq, String assessmentTitle, ZipPrintStream out, PrintStream errStream, CCExport ccExport, CCExport.Resource resource, int version) {

       int seq = 1;

       // feedback:
       // item: Map<String, String> where keys are org.sakaiproject.tool.assessment.data.ifc.assessment.ItemFeedbackIfc.
       //  CORRECT_FEEDBACK = "Correct Feedback";
       //  INCORRECT_FEEDBACK = "InCorrect Feedback";
       //  GENERAL_FEEDBACK = "General Feedback";
       // but may be easiest to use item.getItemFeedback(type)
       //   or getCorrectItemFeedback, getInCorrectItemFeedback, getGeneralItemFeedback
       //  for individual answers,
       //  answer: Set, 
       //  org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerFeedbackIfc.
       //  CORRECT_FEEDBACK = "Correct Feedback";
       //  INCORRECT_FEEDBACK = "InCorrect Feedback";
       //   GENERAL_FEEDBACK = "General Feedback";
       //  ANSWER_FEEDBACK = "answerfeedback";
       // matching has correct and/or incorrect for each answer
       // multiple choice has general feedback for each answer
       // answer_feedback doesn't seem to be used
       // probably easier to use answer.getAnswerFeedback(type)
       // or answer.getCorrectAnswerFeedback, getInCorrectAnswerFeedback, getGeneralAnswerFeedback, getTheAnswerFeedback

	for (ItemDataIfc item: itemList) {

	    SectionDataIfc section = item.getSection();
	    String itemId = null;
	    String title = null;

	    List<Feedback> feedbacks = new ArrayList<Feedback>();

	    if (section != null) {
		itemId = item.getSection().getSequence() + "_" + item.getSequence();
		title = item.getSection().getSequence() + "." + item.getSequence();
	    } else {
		itemId = assessmentSeq + "_" + seq;
		title = assessmentTitle + " " + (seq++);
	    }

	    Set<ItemTextIfc> texts = item.getItemTextSet();
	    List<ItemTextIfc> textlist = new ArrayList<ItemTextIfc>();
	    textlist.addAll(texts);

	    // for FIB we have a textlist rather than just one text
	    if (textlist.size() > 1) {
		Collections.sort(textlist, new Comparator() {
			public int compare (Object o1, Object o2) {
			    Long v1 = ((ItemTextIfc)o1).getSequence();
			    Long v2 = ((ItemTextIfc)o2).getSequence();
			    return v1.compareTo(v2);
			}
		    });
	    }

	    Set<AnswerIfc> answers = null;
	    if (textlist.size() > 0)
		answers = textlist.get(0).getAnswerSet();
	    else
		answers = new HashSet<AnswerIfc>();
	    List<AnswerIfc> answerlist = new ArrayList<AnswerIfc>();
	    answerlist.addAll(answers);

	    if (answerlist.size() > 1) {
		Collections.sort(answerlist, new Comparator() {
			public int compare (Object o1, Object o2) {
			    Long v1 = ((AnswerIfc)o1).getSequence();
			    Long v2 = ((AnswerIfc)o2).getSequence();
			    return v1.compareTo(v2);
			}
		    });
	    }

	    String profile = "cc.multiple_choice.v0p1";
	    Long type = item.getTypeId();
	    boolean survey = false;

	    if (type.equals(TypeIfc.MULTIPLE_CHOICE) || type.equals(TypeIfc.MULTIPLE_CHOICE_SURVEY) || type.equals(TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION)) {
		if (type.equals(TypeIfc.MULTIPLE_CHOICE_SURVEY))
		    survey = true;
		type = TypeIfc.MULTIPLE_CHOICE; // normalize it
		profile = "cc.multiple_choice.v0p1";
	    } else if (type.equals(TypeIfc.MULTIPLE_CORRECT)) {
		profile = "cc.multiple_response.v0p1";
	    } else if (type.equals(TypeIfc.TRUE_FALSE)) {
		profile = "cc.true_false.v0p1"; 
	    } else if (type.equals(TypeIfc.ESSAY_QUESTION)) {
		profile = "cc.essay.v0p1"; 
	    } else if (type.equals(TypeIfc.FILL_IN_BLANK) || type.equals(TypeIfc.FILL_IN_NUMERIC) ) {
		String answerString = "";
		if (answerlist.size() > 0)
		    answerString = answerlist.get(0).getText();
		// only limited pattern match is supported. It has to be just one alternative, and
		// it can only be a substring. I classify anything starting or ending in *, and with one
		// alternative as pattern match, otherwise FIB, and give error except for the one proper case
		if (answerString.indexOf("*") >= 0 && answerString.indexOf("|") < 0)
		    profile = "cc.pattern_match.v0p1";
		else
		    profile = "cc.fib.v0p1"; 
		type = TypeIfc.FILL_IN_BLANK; // normalize

	    } else {
		errStream.println(messageLocator.getMessage("simplepage.exportcc-sam-undefinedtype").replace("{1}", title).replace("{2}",assessmentTitle)); 
		continue;
	    }		
	    
	    //ignore
	    // MATCHING
	    // FILE_UPLOAD:
	    // AUDIO_RECORDING:
	    // MATRIX_CHOICES_SURVEY:

	    String cardinality = "Single";
	    if (type.equals(TypeIfc.MULTIPLE_CORRECT) || type.equals(TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION))
		cardinality = "Multiple";
	    Set<Long> correctSet = new HashSet<Long>();
	    String correctItem = "";
	    
	    // CC doesn't have survey questoins. We treat them as multiple correct single selection
	    if (answerlist.size() > 0) {
		for (AnswerIfc answer: answerlist) {
		    if (survey || answer.getIsCorrect() != null && answer.getIsCorrect()) {
			if (type.equals(TypeIfc.TRUE_FALSE))
			    correctItem = answer.getText().toLowerCase();
			else
			    correctItem = "QUE_" + itemId + "_" + answer.getSequence();
			correctSet.add(answer.getSequence());
		    }
		}
	    }

	    out.println("      <item ident=\"QUE_" + itemId + "\" title=\"" + StringEscapeUtils.escapeXml(title) + "\">");
	    out.println("        <itemmetadata>");
	    out.println("          <qtimetadata>");
	    out.println("            <qtimetadatafield>");
	    out.println("              <fieldlabel>cc_profile</fieldlabel>");
	    out.println("              <fieldentry>" + profile + "</fieldentry>");
	    out.println("            </qtimetadatafield>");
	    if (type.equals(TypeIfc.ESSAY_QUESTION)) {
	    out.println("            <qtimetadatafield>");
	    out.println("              <fieldlabel>qmd_scoringpermitted</fieldlabel>");
	    out.println("              <fieldentry>Yes</fieldentry>");
	    out.println("            </qtimetadatafield>");
	    out.println("            <qtimetadatafield>");
	    out.println("              <fieldlabel>qmd_computerscored</fieldlabel>");
	    out.println("              <fieldentry>No</fieldentry>");
	    out.println("            </qtimetadatafield>");
	    }
	    out.println("          </qtimetadata>");
	    out.println("        </itemmetadata>");

	    out.println("        <presentation>");
	    out.println("          <material>");
	    String text = "";
	    if (type.equals(TypeIfc.FILL_IN_BLANK) || type.equals(TypeIfc.FILL_IN_NUMERIC) ) {
		// gettext replaces {} with ____. The problem is that some of the CC samples tests have
		// an actual ____ in the text. Thus it's best to work with the original {}.
		for (ItemTextIfc it: textlist) {
		    text += "" + it.getText();
		}
		text = text.trim();
		// If there's more than one {} we'll get a weird result, but there's not a lot we can do about that.
		int index = 0;
		int blanks = 0;
		while (true) {
		    index = text.indexOf("{}", index+2);
		    if (index >= 0)
			blanks ++;
		    else break;
		}

		if (blanks > 1) {
		    errStream.println(messageLocator.getMessage("simplepage.exportcc-sam-too-many-blanks").replace("{1}", title).replace("{2}",assessmentTitle).replace("{3}", ""+blanks));
		}

		// now we have the whole string with {}. If the {} isn't at the end, replace
		// it with [____] so the student can see where the replacement actually is. The
		// CC subset won't allow the actual blank to be there.
		if (text.endsWith("{}"))
		    text = text.substring(0, text.length()-2);
		text = text.replaceAll("\\{\\}", "[____]");
		    

	    } else
		text = item.getText();

	    out.println("            <mattext texttype=\"text/html\">" + ccExport.fixup(text, resource) + "</mattext>");
	    out.println("          </material>");

	    if (type.equals(TypeIfc.MULTIPLE_CHOICE) ||type.equals(TypeIfc.MULTIPLE_CORRECT) || type.equals(TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION) || type.equals(TypeIfc.TRUE_FALSE)) {
		// mc has general for each item, correct and incorrect, survey has general

		out.println("          <response_lid ident=\"QUE_" + itemId + "_RL\" rcardinality=\"" + cardinality + "\">");
		out.println("            <render_choice>");
	
		for (AnswerIfc answer: answerlist) {
		    String answerId = "QUE_" + itemId + "_" + answer.getSequence();
		    if (type.equals(TypeIfc.TRUE_FALSE))
			answerId = answer.getText().toLowerCase();
		    String atext = answer.getText();
		    if (atext == null || atext.trim().equals(""))
			continue;
		    out.println("              <response_label ident=\"" + answerId + "\">");
		    out.println("                <material>");
		    out.println("                  <mattext texttype=\"text/html\">" + ccExport.fixup(atext, resource) + "</mattext>");
		    out.println("                </material>");
		    out.println("              </response_label>");
		}

		out.println("            </render_choice>");
		out.println("          </response_lid>");
		out.println("        </presentation>");
		out.println("        <resprocessing>");
		out.println("          <outcomes>");
		out.println("            <decvar maxvalue=\"100\" minvalue=\"0\" varname=\"SCORE\" vartype=\"Decimal\"/>");
		out.println("          </outcomes>");
		
		if (item.getGeneralItemFeedback() != null) {
		    out.println("          <respcondition continue=\"Yes\">");
		    out.println("            <conditionvar><other/></conditionvar>");
                    out.println("            <displayfeedback feedbacktype=\"Response\" linkrefid=\"general_fb\" />");
		    out.println("          </respcondition>");
		    Feedback feedback = new Feedback();
		    feedback.id = "general_fb";
		    feedback.text = item.getGeneralItemFeedback();
		    feedbacks.add(feedback);
		}

		for (AnswerIfc answer: answerlist) {
		    if (answer.getGeneralAnswerFeedback() != null) {
			String atext = answer.getText();
			if (atext == null || atext.trim().equals(""))
			    continue;

			String answerId = "QUE_" + itemId + "_" + answer.getSequence();
			if (type.equals(TypeIfc.TRUE_FALSE))
			    answerId = answer.getText().toLowerCase();
			out.println("          <respcondition continue=\"Yes\">");
			out.println("              <conditionvar>");
			out.println("                <varequal respident=\"QUE_" + itemId + "_RL\">" + answerId + "</varequal>");
			out.println("              </conditionvar>");
			out.println("              <displayfeedback feedbacktype=\"Response\" linkrefid=\"" + answerId + "_fb\" />");
			out.println("          </respcondition>");
			Feedback feedback = new Feedback();
			feedback.id = answerId + "_fb";
			feedback.text = answer.getGeneralAnswerFeedback();
			feedbacks.add(feedback);
		    }
		} 			

		out.println("          <respcondition continue=\"No\">");
		out.println("            <conditionvar>");
		if (type.equals(TypeIfc.MULTIPLE_CHOICE) || type.equals(TypeIfc.TRUE_FALSE)) {
		    int remaining = -1; // default to allow all correct answers
		    if (correctSet.size() > 1) {
			if (version < CCExport.V12) {
			    errStream.println(messageLocator.getMessage("simplepage.exportcc-sam-mcss").replace("{1}", title).replace("{2}",assessmentTitle)); 
			    remaining = 1;
			} else
			    out.println("              <or>");
		    }
		    for (AnswerIfc answer: answerlist) {
			String answerId = "QUE_" + itemId + "_" + answer.getSequence();
			if (type.equals(TypeIfc.TRUE_FALSE))
			    answerId = answer.getText().toLowerCase();
			if (correctSet.contains(answer.getSequence()) && remaining != 0) {
			    out.println("              <varequal case=\"Yes\" respident=\"QUE_" + itemId + "_RL\">" + answerId + "</varequal>");
			    remaining--;
			}
		    }
		    if (correctSet.size() > 1 && remaining < 0)
			out.println("              </or>");
		} else if (type.equals(TypeIfc.MULTIPLE_CORRECT) || type.equals(TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION)) {
		    if (type.equals(TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION))
			errStream.println(messageLocator.getMessage("simplepage.exportcc-sam-mcss").replace("{1}", title).replace("{2}",assessmentTitle));
		    out.println("              <and>");
		    for (AnswerIfc answer: answerlist) {
			String answerId = itemId + "_" + answer.getSequence();
			String atext = answer.getText();
			if (atext == null || atext.trim().equals(""))
			    continue;
			if (correctSet.contains(answer.getSequence())) {
			    out.println("              <varequal case=\"Yes\" respident=\"QUE_" + itemId + "_RL\">QUE_" + itemId + "_" + answer.getSequence() + "</varequal>");
			} else {
			    out.println("              <not>");
			    out.println("                <varequal case=\"Yes\" respident=\"QUE_" + itemId + "_RL\">QUE_" + itemId + "_" + answer.getSequence() + "</varequal>");
			    out.println("              </not>");
			}
		    }
		    out.println("              </and>");
		}
		out.println("            </conditionvar>");
		out.println("            <setvar action=\"Set\" varname=\"SCORE\">100</setvar>");
		if (item.getCorrectItemFeedback() != null) {
		    out.println("            <displayfeedback feedbacktype=\"Response\" linkrefid=\"correct_fb\"/>");
		    Feedback feedback = new Feedback();
		    feedback.id = "correct_fb";
		    feedback.text = item.getCorrectItemFeedback();
		    feedbacks.add(feedback);
		}
		out.println("          </respcondition>");
		if (item.getInCorrectItemFeedback() != null) {
		    out.println("         <respcondition>");
		    out.println("           <conditionvar><other/></conditionvar>");
		    out.println("           <displayfeedback feedbacktype=\"Response\" linkrefid=\"general_incorrect_fb\" />");
		    out.println("         </respcondition>");
		    Feedback feedback = new Feedback();
		    feedback.id = "general_incorrect_fb";
		    feedback.text = item.getInCorrectItemFeedback();
		    feedbacks.add(feedback);
		}
		out.println("        </resprocessing>");
	    } 

	    if (type.equals(TypeIfc.FILL_IN_BLANK) || type.equals(TypeIfc.ESSAY_QUESTION)) {
		// FIB has correct or incorrect, essay has general

		out.println("          <response_str ident=\"QUE_" + itemId + "_RL\">");
		out.println("            <render_fib columns=\"30\" rows=\"1\"/>");
		out.println("          </response_str>");
		out.println("        </presentation>");

		if (type.equals(TypeIfc.FILL_IN_BLANK) && answerlist.size() > 0) {
		    out.println("        <resprocessing>");
		    out.println("          <outcomes>");
		    out.println("            <decvar maxvalue=\"100\" minvalue=\"0\" varname=\"SCORE\" vartype=\"Decimal\"/>");
		    out.println("          </outcomes>");

		    if (item.getGeneralItemFeedback() != null) {
			out.println("          <respcondition continue=\"Yes\">");
			out.println("            <conditionvar><other/></conditionvar>");
			out.println("            <displayfeedback feedbacktype=\"Response\" linkrefid=\"general_fb\" />");
			out.println("          </respcondition>");
			Feedback feedback = new Feedback();
			feedback.id = "general_fb";
			feedback.text = item.getGeneralItemFeedback();
			feedbacks.add(feedback);
		    }

		    out.println("          <respcondition continue=\"No\">");
		    out.println("            <conditionvar>");
		    
		    String answerId = "QUE_" + itemId + "_RL";
		    String answerString = answerlist.get(0).getText();
		    String[] answerArray = answerString.split("\\|");
		    boolean toomanystars = false;
		    if (answerString.indexOf("*") >= 0 && answerString.indexOf("|") >= 0) {
			errStream.println(messageLocator.getMessage("simplepage.exportcc-sam-fib-too-many-star").replace("{1}", title).replace("{2}",assessmentTitle).replace("{3}", answerString));
			toomanystars = true;
		    }

		    for (String answer: answerArray) {
			boolean substr = false;
			boolean hasStar = answer.indexOf("*") >= 0;
			String orig = answer;

			// this isn't a perfect test. Not much we can do with * in the middle of a string
			// and just at the end or just at the beginning isn't a perfect match to this.
			// if more than one alternative, don't treat as matching, since that format isn't legal

			if (!toomanystars) {
			    if (answer.startsWith("*")) {
				answer = answer.substring(1);
				substr = true;
			    }
			    if (answer.endsWith("*")) {
				answer = answer.substring(0, answer.length()-1);
				substr = true;
			    }
			}
				
			if (hasStar) {
			    if (substr)
				errStream.println(messageLocator.getMessage("simplepage.exportcc-sam-fib-star").replace("{1}", title).replace("{2}",assessmentTitle).replace("{3}", orig).replace("{4}", answer));
			    else 
				errStream.println(messageLocator.getMessage("simplepage.exportcc-sam-fib-bad-star").replace("{1}", title).replace("{2}",assessmentTitle).replace("{3}", orig));
			}

			if (substr)
			    out.println("              <varsubstring case=\"No\" respident=\"" + answerId + "\">" + StringEscapeUtils.escapeXml(answer) + "</varsubstring>");
			else
			    out.println("              <varequal case=\"No\" respident=\"" + answerId + "\">" + StringEscapeUtils.escapeXml(answer) + "</varequal>");
		    }

		    out.println("            </conditionvar>");
		    out.println("            <setvar action=\"Set\" varname=\"SCORE\">100</setvar>");
		    if (item.getCorrectItemFeedback() != null) {
			out.println("            <displayfeedback feedbacktype=\"Response\" linkrefid=\"correct_fb\"/>");
			Feedback feedback = new Feedback();
			feedback.id = "correct_fb";
			feedback.text = item.getCorrectItemFeedback();
			feedbacks.add(feedback);
		    }
		    out.println("          </respcondition>");
		    if (item.getInCorrectItemFeedback() != null) {
			out.println("         <respcondition>");
			out.println("           <conditionvar><other/></conditionvar>");
			out.println("           <displayfeedback feedbacktype=\"Response\" linkrefid=\"general_incorrect_fb\" />");
			out.println("         </respcondition>");
			Feedback feedback = new Feedback();
			feedback.id = "general_incorrect_fb";
			feedback.text = item.getInCorrectItemFeedback();
			feedbacks.add(feedback);
		    }
		    out.println("        </resprocessing>");
		}
	    } 

	    if (type.equals(TypeIfc.ESSAY_QUESTION)) {
		// essay has no resprocessing except if there is general feedback
		if (item.getGeneralItemFeedback() != null) {
		    out.println("        <resprocessing>");
		    out.println("          <outcomes>");
		    out.println("            <decvar maxvalue=\"100\" minvalue=\"0\" varname=\"SCORE\" vartype=\"Decimal\"/>");
		    out.println("          </outcomes>");
		    out.println("          <respcondition continue=\"No\">");
		    out.println("            <conditionvar><other/></conditionvar>");
		    out.println("            <displayfeedback feedbacktype=\"Response\" linkrefid=\"general_fb\" />");
		    out.println("          </respcondition>");
		    out.println("        </resprocessing>");
		    Feedback feedback = new Feedback();
		    feedback.id = "general_fb";
		    feedback.text = item.getGeneralItemFeedback();
		    feedbacks.add(feedback);
		}
	    }

	    if (feedbacks.size() > 0) {
		for (Feedback feedback: feedbacks) {
		    out.println("        <itemfeedback ident=\"" + feedback.id + "\">");
		    out.println("          <material>");
		    out.println("            <mattext texttype=\"text/html\">" + ccExport.fixup(feedback.text, resource) + "</mattext>");
		    out.println("          </material>");
		    out.println("        </itemfeedback>");
		}
	    }
	    out.println("      </item>");
	}
    }

    public List<ItemDataIfc> preparePublishedItemList(PublishedAssessmentIfc publishedAssessment){
	List<ItemDataIfc> ret = new ArrayList();

	ArrayList sectionArray = publishedAssessment.getSectionArray();
	Collections.sort(sectionArray, new Comparator() {
		public int compare(Object o1, Object o2) {
		    return ((SectionDataIfc)o1).getSequence() -
			((SectionDataIfc)o2).getSequence();
		}});
	
	for (int i=0;i<sectionArray.size(); i++){
	    SectionDataIfc section = (SectionDataIfc)sectionArray.get(i);
	    ArrayList itemArray = section.getItemArray();
	    Collections.sort(itemArray, new Comparator() {
		    public int compare(Object o1, Object o2) {
			return ((ItemDataIfc)o1).getSequence() -
			    ((ItemDataIfc)o2).getSequence();
		    }});
	    for (int j=0;j<itemArray.size(); j++){
		ItemDataIfc item = (ItemDataIfc)itemArray.get(j);
		ret.add(item);
	    }
	}

	return ret;
    }
}
