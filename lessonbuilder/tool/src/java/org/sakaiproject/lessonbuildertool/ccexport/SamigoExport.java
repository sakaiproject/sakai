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

import static org.sakaiproject.lessonbuildertool.ccexport.CCVersion.V12;
import static org.sakaiproject.lessonbuildertool.ccexport.CCVersion.V13;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.sakaiproject.lessonbuildertool.service.LessonEntity;
import org.sakaiproject.lessonbuildertool.util.ResourceLoaderMessageSource;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.questionpool.QuestionPoolDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.facade.QuestionPoolFacade;
import org.sakaiproject.tool.assessment.facade.QuestionPoolFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.api.FormattedText;
import org.springframework.context.MessageSource;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/*
 * set up as a singleton. But CCexport is not.
 */
@Slf4j
public class SamigoExport {

    @Setter private CCUtils ccUtils;
    @Setter private FormattedText formattedText;
    @Setter private PublishedAssessmentService pubService = new PublishedAssessmentService();
    @Setter private QuestionPoolFacadeQueriesAPI questionPoolFacadeQueries;
    @Setter private UserDirectoryService userDirectoryService;

    private ResourceLoaderMessageSource messageSource;

    public SamigoExport() {
        messageSource = new ResourceLoaderMessageSource();
        messageSource.setBasename("messages");
    }

    public List<String> getEntitiesInSite(String siteId) {
        // find topics in site, but organized by forum
        return Optional.ofNullable(pubService.getBasicInfoOfAllPublishedAssessments2("title", true, siteId).stream())
                .orElseGet(Stream::empty)
                .filter(a -> AssessmentIfc.ACTIVE_STATUS.equals(a.getStatus()))
                .map(a -> LessonEntity.SAM_PUB + "/" + a.getPublishedAssessmentId().toString())
                .collect(Collectors.toList());
    }

    public List<Long> getAllPools() {
        // are there any items in a pool?
        return Optional.ofNullable(questionPoolFacadeQueries.getBasicInfoOfAllPools(userDirectoryService.getCurrentUser().getId()).stream())
                .orElseGet(Stream::empty)
                .filter(p -> !questionPoolFacadeQueries.getAllItems(p.getQuestionPoolId()).isEmpty())
                .map(QuestionPoolFacade::getQuestionPoolId)
                .collect(Collectors.toList());
    }

    public boolean outputEntity(CCConfig ccConfig, String samigoId, ZipPrintStream out, PrintWriter resultsWriter, CCResourceItem CCResourceItem, CCVersion ccVersion) {
        String publishedAssessmentString = samigoId.substring(samigoId.indexOf("/") + 1);
        PublishedAssessmentFacade assessment = pubService.getPublishedAssessment(publishedAssessmentString, true);
        List<ItemDataIfc> publishedItemList = preparePublishedItemList(assessment);
        String assessmentTitle = formattedText.convertFormattedTextToPlaintext(assessment.getTitle());

        out.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");

        switch (ccVersion) {
            case V11:
                out.println("<questestinterop xmlns=\"http://www.imsglobal.org/xsd/ims_qtiasiv1p2\"");
                out.println("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.imsglobal.org/xsd/ims_qtiasiv1p2 http://www.imsglobal.org/profile/cc/ccv1p1/ccv1p1_qtiasiv1p2p1_v1p0.xsd\">");
                break;
            case V13:
                out.println("<questestinterop xmlns=\"http://www.imsglobal.org/xsd/ims_qtiasiv1p2\"");
                out.println("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.imsglobal.org/xsd/ims_qtiasiv1p2 http://www.imsglobal.org/profile/cc/ccv1p3/ccv1p3_qtiasiv1p2p1_v1p0.xsd\">");
                break;
            default:
                out.println("<questestinterop xmlns=\"http://www.imsglobal.org/xsd/ims_qtiasiv1p2\"");
                out.println("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.imsglobal.org/xsd/ims_qtiasiv1p2 http://www.imsglobal.org/profile/cc/ccv1p2/ccv1p2_qtiasiv1p2p1_v1p0.xsd\">");
                break;
        }

        out.println("  <assessment ident=\"QDB_1\" title=\"" + StringEscapeUtils.escapeXml11(assessmentTitle) + "\">");
        out.println("    <section ident=\"S_1\">");

        outputQuestions(ccConfig, publishedItemList, null, assessmentTitle, out, resultsWriter, CCResourceItem, ccVersion);

        out.println("    </section>");
        out.println("  </assessment>");
        out.println("</questestinterop>");

        return true;
    }

    public boolean outputBank(CCConfig ccConfig, Long poolId, ZipPrintStream out, PrintWriter resultsWriter, CCResourceItem ccResourceItem, CCVersion ccVersion) {

        out.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");

        switch (ccVersion) {
            case V11:
                out.println("<questestinterop xmlns=\"http://www.imsglobal.org/xsd/ims_qtiasiv1p2\"");
                out.println("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.imsglobal.org/xsd/ims_qtiasiv1p2 http://www.imsglobal.org/profile/cc/ccv1p1/ccv1p1_qtiasiv1p2p1_v1p0.xsd\">");
                break;
            case V13:
                out.println("<questestinterop xmlns=\"http://www.imsglobal.org/xsd/ims_qtiasiv1p2\"");
                out.println("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.imsglobal.org/xsd/ims_qtiasiv1p2 http://www.imsglobal.org/profile/cc/ccv1p3/ccv1p3_qtiasiv1p2p1_v1p0.xsd\">");
                break;
            default:
                out.println("<questestinterop xmlns=\"http://www.imsglobal.org/xsd/ims_qtiasiv1p2\"");
                out.println("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.imsglobal.org/xsd/ims_qtiasiv1p2 http://www.imsglobal.org/profile/cc/ccv1p2/ccv1p2_qtiasiv1p2p1_v1p0.xsd\">");
                break;
        }

        out.println("  <objectbank ident=\"QDB_1\">");

        if (ccVersion.greaterThanOrEqualTo(V13)) {
            // 1.3 or later, specific pool
            QuestionPoolFacade pool = questionPoolFacadeQueries.getPoolById(poolId);
            if (pool != null) {
                List<ItemDataIfc> itemList = questionPoolFacadeQueries.getAllItems(poolId);
                if (itemList != null && itemList.size() > 0)
                    outputQuestions(ccConfig, itemList, "pool" + poolId, pool.getTitle(), out, resultsWriter, ccResourceItem, ccVersion);
            }
        } else {
            // older. all pools at once
            List<QuestionPoolFacade> pools = questionPoolFacadeQueries.getBasicInfoOfAllPools(userDirectoryService.getCurrentUser().getId());

            log.info("pools " + pools.size());

            if (pools != null && pools.size() > 0) {
                int poolno = 1;
                for (QuestionPoolDataIfc pool : pools) {
                    List<ItemDataIfc> itemList = questionPoolFacadeQueries.getAllItems(pool.getQuestionPoolId());
                    if (itemList != null && itemList.size() > 0)
                        outputQuestions(ccConfig, itemList, ("pool" + (poolno++)), pool.getTitle(), out, resultsWriter, ccResourceItem, ccVersion);
                }
            }
        }

        out.println("  </objectbank>");
        out.println("</questestinterop>");

        return true;
    }

    public void outputQuestions(CCConfig ccConfig, List<ItemDataIfc> itemList, String assessmentSeq, String assessmentTitle, ZipPrintStream out, PrintWriter resultsWriter, CCResourceItem CCResourceItem, CCVersion ccVersion) {

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

        for (ItemDataIfc item : itemList) {

            SectionDataIfc section = item.getSection();
            String itemId;
            String title;

            List<CCFeedbackItem> ccFeedbackItems = new ArrayList<>();

            if (section != null) {
                itemId = item.getSection().getSequence() + "_" + item.getSequence();
                title = item.getSection().getSequence() + "." + item.getSequence();
            } else {
                itemId = assessmentSeq + "_" + seq;
                title = assessmentTitle + " " + (seq++);
            }

            List<ItemTextIfc> texts = Optional.ofNullable(item.getItemTextSet().stream()).orElseGet(Stream::empty)
                    .sorted(Comparator.comparing(ItemTextIfc::getSequence))
                    .collect(Collectors.toList());

            List<AnswerIfc> answers = texts.stream().findAny()
                    .map(ItemTextIfc::getAnswerSet).orElseGet(HashSet::new)
                    .stream()
                    .sorted(Comparator.comparing(AnswerIfc::getSequence))
                    .collect(Collectors.toList());

            Long type = item.getTypeId();
            boolean survey = false;

            String profile;
            if (type.equals(TypeIfc.MULTIPLE_CHOICE) || type.equals(TypeIfc.MULTIPLE_CHOICE_SURVEY) || type.equals(TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION)) {
                if (type.equals(TypeIfc.MULTIPLE_CHOICE_SURVEY)) survey = true;
                type = TypeIfc.MULTIPLE_CHOICE; // normalize it
                profile = "cc.multiple_choice.v0p1";
            } else if (type.equals(TypeIfc.MULTIPLE_CORRECT)) {
                profile = "cc.multiple_response.v0p1";
            } else if (type.equals(TypeIfc.TRUE_FALSE)) {
                profile = "cc.true_false.v0p1";
            } else if (type.equals(TypeIfc.ESSAY_QUESTION)) {
                profile = "cc.essay.v0p1";
            } else if (type.equals(TypeIfc.FILL_IN_BLANK) || type.equals(TypeIfc.FILL_IN_NUMERIC)) {
                String answerString = "";
                if (!answers.isEmpty()) answerString = answers.get(0).getText();
                // only limited pattern match is supported. It has to be just one alternative, and
                // it can only be a substring. I classify anything starting or ending in *, and with one
                // alternative as pattern match, otherwise FIB, and give error except for the one proper case
                if (answerString.contains("*") && !answerString.contains("|")) {
                    profile = "cc.pattern_match.v0p1";
                } else {
                    profile = "cc.fib.v0p1";
                }
                type = TypeIfc.FILL_IN_BLANK; // normalize

            } else {
                resultsWriter.println(messageSource.getMessage("simplepage.exportcc-sam-undefinedtype", null, ccConfig.getLocale()).replace("{1}", title).replace("{2}", assessmentTitle));
                continue;
            }

            //ignore
            // MATCHING
            // FILE_UPLOAD:
            // AUDIO_RECORDING:
            // MATRIX_CHOICES_SURVEY:

            out.println("      <item ident=\"QUE_" + itemId + "\" title=\"" + StringEscapeUtils.escapeXml11(title) + "\">");
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

            String text;

            if (type.equals(TypeIfc.FILL_IN_BLANK) || type.equals(TypeIfc.FILL_IN_NUMERIC)) {
                // gettext replaces {} with ____. The problem is that some of the CC samples tests have
                // an actual ____ in the text. Thus it's best to work with the original {}.
                text = texts.stream().map(ItemTextIfc::getText).collect(Collectors.joining());
                text = text.trim();
                // If there's more than one {} we'll get a weird result, but there's not a lot we can do about that.
                int blanks = StringUtils.countMatches(text, "{}");
                if (blanks > 1) {
                    resultsWriter.println(messageSource.getMessage("simplepage.exportcc-sam-too-many-blanks", null, ccConfig.getLocale()).replace("{1}", title).replace("{2}", assessmentTitle).replace("{3}", "" + blanks));
                }

                // now we have the whole string with {}. If the {} isn't at the end, replace
                // it with [____] so the student can see where the replacement actually is. The
                // CC subset won't allow the actual blank to be there.
                if (text.endsWith("{}")) {
                    text = text.substring(0, text.length() - 2);
                }
                text = text.replaceAll("\\{}", "[____]");
            } else {
                text = item.getText();
            }

            out.println("            <mattext texttype=\"text/html\">" + ccUtils.fixup(ccConfig, text, CCResourceItem) + "</mattext>");
            out.println("          </material>");

            String cardinality = "Single";
            if (type.equals(TypeIfc.MULTIPLE_CORRECT) || type.equals(TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION)) {
                cardinality = "Multiple";
            }

            final boolean tmpSurvey = survey;
            Set<Long> correctSet = answers.stream()
                    .filter(a -> tmpSurvey || BooleanUtils.isTrue(a.getIsCorrect())).map(AnswerIfc::getSequence)
                    .collect(Collectors.toSet());

            if (type.equals(TypeIfc.MULTIPLE_CHOICE) || type.equals(TypeIfc.MULTIPLE_CORRECT) || type.equals(TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION) || type.equals(TypeIfc.TRUE_FALSE)) {
                // mc has general for each item, correct and incorrect, survey has general

                out.println("          <response_lid ident=\"QUE_" + itemId + "_RL\" rcardinality=\"" + cardinality + "\">");
                out.println("            <render_choice>");

                for (AnswerIfc answer : answers) {
                    String answerId;
                    if (type.equals(TypeIfc.TRUE_FALSE)) {
                        answerId = answer.getText().toLowerCase();
                    } else {
                        answerId = "QUE_" + itemId + "_" + answer.getSequence();
                    }

                    String answerText = answer.getText();
                    if (StringUtils.isNotBlank(answerText)) {
                        out.println("              <response_label ident=\"" + answerId + "\">");
                        out.println("                <material>");
                        out.println("                  <mattext texttype=\"text/html\">" + ccUtils.fixup(ccConfig, answerText, CCResourceItem) + "</mattext>");
                        out.println("                </material>");
                        out.println("              </response_label>");
                    }
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
                    CCFeedbackItem ccFeedbackItem = new CCFeedbackItem("general_fb", item.getGeneralItemFeedback());
                    ccFeedbackItems.add(ccFeedbackItem);
                }

                for (AnswerIfc answer : answers) {
                    if (answer.getGeneralAnswerFeedback() != null) {
                        String answerText = answer.getText();
                        if (StringUtils.isNotBlank(answerText)) {
                            String answerId;
                            if (type.equals(TypeIfc.TRUE_FALSE)) {
                                answerId = answer.getText().toLowerCase();
                            } else {
                                answerId = "QUE_" + itemId + "_" + answer.getSequence();
                            }
                            out.println("          <respcondition continue=\"Yes\">");
                            out.println("              <conditionvar>");
                            out.println("                <varequal respident=\"QUE_" + itemId + "_RL\">" + answerId + "</varequal>");
                            out.println("              </conditionvar>");
                            out.println("              <displayfeedback feedbacktype=\"Response\" linkrefid=\"" + answerId + "_fb\" />");
                            out.println("          </respcondition>");
                            CCFeedbackItem ccFeedbackItem = new CCFeedbackItem(answerId + "_fb", answer.getGeneralAnswerFeedback());
                            ccFeedbackItems.add(ccFeedbackItem);
                        }
                    }
                }

                out.println("          <respcondition continue=\"No\">");
                out.println("            <conditionvar>");
                if (type.equals(TypeIfc.MULTIPLE_CHOICE) || type.equals(TypeIfc.TRUE_FALSE)) {
                    int remaining = -1; // default to allow all correct answers
                    if (correctSet.size() > 1) {
                        if (ccVersion.greaterThan(V12)) {
                            resultsWriter.println(messageSource.getMessage("simplepage.exportcc-sam-mcss", null, ccConfig.getLocale()).replace("{1}", title).replace("{2}", assessmentTitle));
                            remaining = 1;
                        } else
                            out.println("              <or>");
                    }
                    for (AnswerIfc answer : answers) {
                        String answerId;
                        if (type.equals(TypeIfc.TRUE_FALSE)) {
                            answerId = answer.getText().toLowerCase();
                        } else {
                            answerId = "QUE_" + itemId + "_" + answer.getSequence();
                        }
                        if (correctSet.contains(answer.getSequence()) && remaining != 0) {
                            out.println("              <varequal case=\"Yes\" respident=\"QUE_" + itemId + "_RL\">" + answerId + "</varequal>");
                            remaining--;
                        }
                    }
                    if (correctSet.size() > 1 && remaining < 0) {
                        out.println("              </or>");
                    }
                } else {
                    // type.equals TypeIfc.MULTIPLE_CORRECT || TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION
                    if (type.equals(TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION)) {
                        resultsWriter.println(messageSource.getMessage("simplepage.exportcc-sam-mcss", null, ccConfig.getLocale()).replace("{1}", title).replace("{2}", assessmentTitle));
                    }
                    out.println("              <and>");
                    for (AnswerIfc answer : answers) {
                        String answerText = answer.getText();
                        if (StringUtils.isNotBlank(answerText)) {
                            if (correctSet.contains(answer.getSequence())) {
                                out.println("              <varequal case=\"Yes\" respident=\"QUE_" + itemId + "_RL\">QUE_" + itemId + "_" + answer.getSequence() + "</varequal>");
                            } else {
                                out.println("              <not>");
                                out.println("                <varequal case=\"Yes\" respident=\"QUE_" + itemId + "_RL\">QUE_" + itemId + "_" + answer.getSequence() + "</varequal>");
                                out.println("              </not>");
                            }
                        }
                    }
                    out.println("              </and>");
                }
                out.println("            </conditionvar>");
                out.println("            <setvar action=\"Set\" varname=\"SCORE\">100</setvar>");
                if (item.getCorrectItemFeedback() != null) {
                    out.println("            <displayfeedback feedbacktype=\"Response\" linkrefid=\"correct_fb\"/>");
                    CCFeedbackItem ccFeedbackItem = new CCFeedbackItem("correct_fb", item.getCorrectItemFeedback());
                    ccFeedbackItems.add(ccFeedbackItem);
                }
                out.println("          </respcondition>");
                if (item.getInCorrectItemFeedback() != null) {
                    out.println("         <respcondition>");
                    out.println("           <conditionvar><other/></conditionvar>");
                    out.println("           <displayfeedback feedbacktype=\"Response\" linkrefid=\"general_incorrect_fb\" />");
                    out.println("         </respcondition>");
                    CCFeedbackItem ccFeedbackItem = new CCFeedbackItem("general_incorrect_fb", item.getInCorrectItemFeedback());
                    ccFeedbackItems.add(ccFeedbackItem);
                }
                out.println("        </resprocessing>");
            }

            if (type.equals(TypeIfc.FILL_IN_BLANK) || type.equals(TypeIfc.ESSAY_QUESTION)) {
                // FIB has correct or incorrect, essay has general

                out.println("          <response_str ident=\"QUE_" + itemId + "_RL\">");
                out.println("            <render_fib columns=\"30\" rows=\"1\"/>");
                out.println("          </response_str>");
                out.println("        </presentation>");

                if (type.equals(TypeIfc.FILL_IN_BLANK) && answers.size() > 0) {
                    out.println("        <resprocessing>");
                    out.println("          <outcomes>");
                    out.println("            <decvar maxvalue=\"100\" minvalue=\"0\" varname=\"SCORE\" vartype=\"Decimal\"/>");
                    out.println("          </outcomes>");

                    if (item.getGeneralItemFeedback() != null) {
                        out.println("          <respcondition continue=\"Yes\">");
                        out.println("            <conditionvar><other/></conditionvar>");
                        out.println("            <displayfeedback feedbacktype=\"Response\" linkrefid=\"general_fb\" />");
                        out.println("          </respcondition>");
                        CCFeedbackItem ccFeedbackItem = new CCFeedbackItem("general_fb", item.getGeneralItemFeedback());
                        ccFeedbackItems.add(ccFeedbackItem);
                    }

                    out.println("          <respcondition continue=\"No\">");
                    out.println("            <conditionvar>");

                    String answerId = "QUE_" + itemId + "_RL";
                    String answerString = answers.get(0).getText();
                    String[] answerArray = answerString.split("\\|");
                    boolean tooManyStars = false;
                    if (answerString.contains("*") && answerString.contains("|")) {
                        resultsWriter.println(messageSource.getMessage("simplepage.exportcc-sam-fib-too-many-star", null, ccConfig.getLocale()).replace("{1}", title).replace("{2}", assessmentTitle).replace("{3}", answerString));
                        tooManyStars = true;
                    }

                    for (String answer : answerArray) {
                        boolean substr = false;
                        boolean hasStar = answer.contains("*");
                        String orig = answer;

                        // this isn't a perfect test. Not much we can do with * in the middle of a string
                        // and just at the end or just at the beginning isn't a perfect match to this.
                        // if more than one alternative, don't treat as matching, since that format isn't legal

                        if (!tooManyStars) {
                            if (answer.startsWith("*")) {
                                answer = answer.substring(1);
                                substr = true;
                            }
                            if (answer.endsWith("*")) {
                                answer = answer.substring(0, answer.length() - 1);
                                substr = true;
                            }
                        }

                        if (hasStar) {
                            if (substr) {
                                resultsWriter.println(messageSource.getMessage("simplepage.exportcc-sam-fib-star", null, ccConfig.getLocale()).replace("{1}", title).replace("{2}", assessmentTitle).replace("{3}", orig).replace("{4}", answer));
                            } else {
                                resultsWriter.println(messageSource.getMessage("simplepage.exportcc-sam-fib-bad-star", null, ccConfig.getLocale()).replace("{1}", title).replace("{2}", assessmentTitle).replace("{3}", orig));
                            }
                        }

                        if (substr) {
                            out.println("              <varsubstring case=\"No\" respident=\"" + answerId + "\">" + StringEscapeUtils.escapeXml11(answer) + "</varsubstring>");
                        } else {
                            out.println("              <varequal case=\"No\" respident=\"" + answerId + "\">" + StringEscapeUtils.escapeXml11(answer) + "</varequal>");
                        }
                    }

                    out.println("            </conditionvar>");
                    out.println("            <setvar action=\"Set\" varname=\"SCORE\">100</setvar>");
                    if (item.getCorrectItemFeedback() != null) {
                        out.println("            <displayfeedback feedbacktype=\"Response\" linkrefid=\"correct_fb\"/>");
                        CCFeedbackItem ccFeedbackItem = new CCFeedbackItem("correct_fb", item.getCorrectItemFeedback());
                        ccFeedbackItems.add(ccFeedbackItem);
                    }
                    out.println("          </respcondition>");
                    if (item.getInCorrectItemFeedback() != null) {
                        out.println("         <respcondition>");
                        out.println("           <conditionvar><other/></conditionvar>");
                        out.println("           <displayfeedback feedbacktype=\"Response\" linkrefid=\"general_incorrect_fb\" />");
                        out.println("         </respcondition>");
                        CCFeedbackItem ccFeedbackItem = new CCFeedbackItem("general_incorrect_fb", item.getInCorrectItemFeedback());
                        ccFeedbackItems.add(ccFeedbackItem);
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
                    CCFeedbackItem ccFeedbackItem = new CCFeedbackItem("general_fb", item.getGeneralItemFeedback());
                    ccFeedbackItems.add(ccFeedbackItem);
                }
            }

            if (!ccFeedbackItems.isEmpty()) {
                for (CCFeedbackItem CCFeedbackItem : ccFeedbackItems) {
                    out.println("        <itemfeedback ident=\"" + CCFeedbackItem.getId() + "\">");
                    out.println("          <material>");
                    out.println("            <mattext texttype=\"text/html\">" + ccUtils.fixup(ccConfig, CCFeedbackItem.getText(), CCResourceItem) + "</mattext>");
                    out.println("          </material>");
                    out.println("        </itemfeedback>");
                }
            }
            out.println("      </item>");
        }
    }

    public List<ItemDataIfc> preparePublishedItemList(PublishedAssessmentIfc publishedAssessment) {

        List<ItemDataIfc> items = new ArrayList<>();
        List<SectionDataIfc> sortedSections = (List<SectionDataIfc>) publishedAssessment.getSectionSet().stream()
                .sorted(Comparator.comparing(SectionDataIfc::getSequence))
                .collect(Collectors.toList());

        for (SectionDataIfc section : sortedSections) {
            section.getItemSet().stream()
                    .sorted(Comparator.comparing(ItemDataIfc::getSequence))
                    .forEach(i -> items.add((ItemDataIfc) i));
        }
        return items;
    }
}

