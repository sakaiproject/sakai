/**********************************************************************************
 * $URL: https://newtools.oirt.rutgers.edu:8443/repos/sakai2.x/sakai/trunk/archive/import-parsers/blackboard_6/impl/src/java/org/sakaiproject/importer/impl/translators/Bb6AssessmentTranslator.java $
 * $Id: Bb6AssessmentTranslator.java 1314 2009-04-08 19:09:09Z weresow $
 ***********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.importer.impl.translators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.importer.api.IMSResourceTranslator;
import org.sakaiproject.importer.api.Importable;
import org.sakaiproject.importer.impl.Blackboard6FileParser;
import org.sakaiproject.importer.impl.XPathHelper;
import org.sakaiproject.importer.impl.importables.Assessment;
import org.sakaiproject.importer.impl.importables.AssessmentAnswer;
import org.sakaiproject.importer.impl.importables.AssessmentQuestion;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class Bb6AssessmentTranslator implements IMSResourceTranslator{

	public String getTypeName() {
		return "assessment/x-bb-qti-test";
	}

	public Importable translate(Node resourceNode, Document descriptor, String contextPath, String archiveBasePath) {
		Assessment rv = newImportable();
		rv.setDescription((XPathHelper.getNodeValue(
				"/questestinterop/assessment/presentation_material/flow_mat/material/mat_extension/mat_formattedtext", descriptor)));
		rv.setTitle(XPathHelper.getNodeValue("/questestinterop/assessment/@title", descriptor));
		// from the descriptor, add all the questions to this QuestionPool object
		List multiChoiceNodes = XPathHelper.selectNodes("//item/itemmetadata/bbmd_questiontype[. = 'Multiple Choice']/ancestor::item", descriptor);
		List essayNodes = XPathHelper.selectNodes("//item/itemmetadata/bbmd_questiontype[. = 'Essay']/ancestor::item", descriptor);
		List fillTheBlankNodes = XPathHelper.selectNodes("//item/itemmetadata/bbmd_questiontype[. = 'Fill in the Blank']/ancestor::item", descriptor);
		List matchingNodes = XPathHelper.selectNodes("//item/itemmetadata/bbmd_questiontype[. = 'Matching']/ancestor::item", descriptor);
		List multiAnswerNodes = XPathHelper.selectNodes("//item/itemmetadata/bbmd_questiontype[. = 'Multiple Answer']/ancestor::item", descriptor);
		List orderingNodes = XPathHelper.selectNodes("//item/itemmetadata/bbmd_questiontype[. = 'Ordering']/ancestor::item", descriptor);
		List trueFalseNodes = XPathHelper.selectNodes("//item/itemmetadata/bbmd_questiontype[. = 'True/False']/ancestor::item", descriptor);
		
		int totalNumberOfQuestions = multiChoiceNodes.size() + essayNodes.size() + 
		fillTheBlankNodes.size() + matchingNodes.size() + multiAnswerNodes.size() + orderingNodes.size() + trueFalseNodes.size();
		
		if (totalNumberOfQuestions < 1) return null;
		
		rv.setMultiChoiceQuestions(
				getQuestionsFromNodes(multiChoiceNodes, AssessmentQuestion.MULTIPLE_CHOICE));
		rv.setEssayQuestions(
				getQuestionsFromNodes(essayNodes, AssessmentQuestion.ESSAY));
		rv.setFillBlankQuestions(
				getQuestionsFromNodes(fillTheBlankNodes, AssessmentQuestion.FILL_BLANK));
		rv.setMatchQuestions(
				getQuestionsFromNodes(matchingNodes, AssessmentQuestion.MATCHING));
		rv.setMultiAnswerQuestions(
				getQuestionsFromNodes(multiAnswerNodes, AssessmentQuestion.MULTIPLE_ANSWER));
		rv.setOrderingQuestions(
				getQuestionsFromNodes(orderingNodes, AssessmentQuestion.ORDERING));
		rv.setTrueFalseQuestions(
				getQuestionsFromNodes(trueFalseNodes, AssessmentQuestion.TRUE_FALSE));
		rv.setLegacyGroup(Blackboard6FileParser.ASSESSMENT_GROUP);
		return rv;
	}

	/**
	 * This is a factory method that allows sublasses to use a different type inside this class's methods
	 * @return an empty Assessment object ready to fill up.
	 */
	protected Assessment newImportable() {
		return new Assessment();
	}

	public boolean processResourceChildren() {
		return true;
	}
	
	private List getQuestionsFromNodes(List questionNodes, int questionType) {
		List rv = new ArrayList();
		AssessmentQuestion q;
		Map answers;
		Map choices;
		Node questionNode;
		Node answerNode;
		AssessmentAnswer a;
		Set correctAnswerIDs;
		for(Iterator i = questionNodes.iterator();i.hasNext();) {
			q = new AssessmentQuestion();
			answers = new HashMap();
			correctAnswerIDs = new HashSet();
			questionNode = (Node)i.next();
			int questionPosition = XPathHelper.selectNodes("./preceding-sibling::item", questionNode).size() + 1;
			q.setPosition(new Integer(questionPosition));
			List answerNodes = null;
			if (questionType == AssessmentQuestion.MATCHING) {
				answerNodes = XPathHelper.selectNodes("./presentation//flow[@class='RESPONSE_BLOCK']/flow[@class='Block']", questionNode);
				choices = new HashMap();
				AssessmentAnswer c;
				int choicesSize = XPathHelper.selectNodes("./presentation//flow[@class='RIGHT_MATCH_BLOCK']/flow[@class='Block']", questionNode).size();
				// iterate over the answers here
				int answerPosition = 0;
				for (Iterator j = answerNodes.iterator();j.hasNext();) {
					a = new AssessmentAnswer();
					answerPosition++;
					answerNode = (Node)j.next();
					a.setAnswerId(XPathHelper.getNodeValue("./response_lid/@ident", answerNode));
					a.setPosition(answerPosition);
					String answerText = XPathHelper.getNodeValue(".//mat_formattedtext[1]", answerNode);
					String answerImageText = getAnswerImageText(answerNode);
					if (!("".equals(answerImageText))) answerText = new StringBuffer(answerText + "\n").append("<img src=\"" + answerImageText + "\"/>").toString();
					a.setAnswerText(answerText);
					c = new AssessmentAnswer();
					c.setAnswerId(XPathHelper.getNodeValue("./resprocessing//varequal[@respident='" + a.getAnswerId() + "']", questionNode));
					for (int k = 1;k <= choicesSize; k++) {
						if (c.getAnswerId().equals(XPathHelper.getNodeValue("./response_lid/render_choice/flow_label/response_label[" + k + "]/@ident", answerNode))) {
							String choiceText = XPathHelper.getNodeValue("./presentation//flow[@class='RIGHT_MATCH_BLOCK']/flow[" + k + "]//mat_formattedtext", questionNode);
							String choiceImageText = getChoiceImageText(questionNode, k);
							if (!("".equals(choiceImageText))) choiceText = new StringBuffer(choiceText + "\n").append("<img src=\"" + choiceImageText + "\"/>").toString();
							c.setAnswerText(choiceText);
							// XPath uses 1-based indexes, but we use zero-based positioning in an AssessmentAnswer
							c.setPosition(k-1);
							choices.put(c.getAnswerId(), c);
							a.setChoiceId(c.getAnswerId());
							break;
						}
					}
					correctAnswerIDs.add(a.getAnswerId());
					answers.put(a.getAnswerId(), a);
				}
				q.setChoices(choices);
			} else if (questionType == AssessmentQuestion.ESSAY) {
				a = new AssessmentAnswer();
				a.setAnswerId(XPathHelper.getNodeValue("./itemmetadata/bbmd_asi_object_id", questionNode));
				a.setAnswerText(XPathHelper.getNodeValue("./itemfeedback[@ident='solution']//mat_formattedtext[1]", questionNode));
				correctAnswerIDs.add(a.getAnswerId());
				answers.put(a.getAnswerId(), a);
			} else if (questionType == AssessmentQuestion.FILL_BLANK) {
				answerNodes = XPathHelper.selectNodes("./resprocessing/respcondition/conditionvar/varequal[@respident='response']/ancestor::respcondition", questionNode);
				for (Iterator j = answerNodes.iterator();j.hasNext();) {
					a = new AssessmentAnswer();
					answerNode = (Node)j.next();
					a.setAnswerId(XPathHelper.getNodeValue("./@title", answerNode));
					a.setAnswerText(XPathHelper.getNodeValue("./conditionvar/varequal", answerNode));
					answers.put(a.getAnswerId(), a);
				}
				
				List correctAnswerNodes = null;
				correctAnswerNodes = XPathHelper.selectNodes("./resprocessing/respcondition/conditionvar/varequal", questionNode);
				for (Iterator j = correctAnswerNodes.iterator();j.hasNext();) {
					Node correctAnswerNode = (Node)j.next();
					String correctAnswerId = XPathHelper.getNodeValue(".", correctAnswerNode);
					// sometimes the correctAnswerNode yields an empty string for correctAnswerId
					if ("".equals(correctAnswerId)) continue;
					correctAnswerIDs.add(correctAnswerId);
				}
			} else {
				answerNodes = XPathHelper.selectNodes("./presentation//flow[@class='RESPONSE_BLOCK']//response_label", questionNode);
				int position = 0;
				for (Iterator j = answerNodes.iterator();j.hasNext();) {
					a = new AssessmentAnswer();
					answerNode = (Node)j.next();
					position++;
					a.setAnswerId(XPathHelper.getNodeValue("./@ident", answerNode));
					a.setPosition(position);
					try {
						// can't tell yet if qti has any position information for its answers.
						// a.setPosition(Integer.parseInt(XPathHelper.getNodeValue("./@position", answerNode)));
					} catch (NumberFormatException nfe) {
						// this just means there was no position information in this answer, which is ok.
					}
					String answerText = XPathHelper.getNodeValue(".//mat_formattedtext[1]", answerNode);
					String answerImageText = getAnswerImageText(answerNode);
					if (!("".equals(answerImageText))) answerText = new StringBuffer(answerText + "\n").append("<img src=\"" + answerImageText + "\"/>").toString();
					if (answerText == null || "".equals(answerText)) {
						answerText = XPathHelper.getNodeValue(".//mattext[1]", answerNode);
					}
					a.setAnswerText(answerText);
					//TODO parse the date strings in the XML into Date objects
					answers.put(a.getAnswerId(), a);
				}
				List correctAnswerNodes = null;
				if (questionType == AssessmentQuestion.MULTIPLE_ANSWER) {
					correctAnswerNodes = XPathHelper.selectNodes("./resprocessing/respcondition[@title='correct']/conditionvar/and/varequal", questionNode);
				} else {
					correctAnswerNodes = XPathHelper.selectNodes("./resprocessing/respcondition[@title='correct']/conditionvar/varequal", questionNode);
					// dammit, sometimes there is no title='correct' attribute.
					if ((correctAnswerNodes == null) || (correctAnswerNodes.size() < 1)) {
						correctAnswerNodes = XPathHelper.selectNodes("./resprocessing/respcondition/displayfeedback[@linkrefid='correct']/../conditionvar/varequal", questionNode);
					}
					
				}
				
				for (Iterator j = correctAnswerNodes.iterator();j.hasNext();) {
					Node correctAnswerNode = (Node)j.next();
					String correctAnswerId = XPathHelper.getNodeValue(".", correctAnswerNode);
					// sometimes the correctAnswerNode yields an empty string for correctAnswerId
					if ("".equals(correctAnswerId)) continue;
					// BB 6 uses true and false rather than the ID for true/false questions
					if (correctAnswerId.equalsIgnoreCase("true") ||
					    correctAnswerId.equalsIgnoreCase("false")) {
					    for (Object o:answers.values()) {
						a = (AssessmentAnswer)o;
						if (a.getAnswerText().equalsIgnoreCase(correctAnswerId))
						    correctAnswerId = a.getAnswerId();
					    }
					}
					correctAnswerIDs.add(correctAnswerId);
				}
			}
				
			//TODO parse the date strings for the question into Date objects
			q.setQuestionType(questionType);
			q.setAnswers(answers);
			String questionTextString = XPathHelper.getNodeValue("./presentation//flow[@class='FORMATTED_TEXT_BLOCK']//mat_formattedtext[1]", questionNode);
			String questionImageText = getQuestionImageText(questionNode);
			if (!("".equals(questionImageText))) questionTextString = new StringBuffer(questionTextString + "\n").append("<img src=\"" + questionImageText + "\"/>").toString();
			q.setQuestionText(questionTextString);
			try {
				double pointValue = Double.parseDouble(XPathHelper.getNodeValue("./itemmetadata/qmd_absolutescore_max", questionNode));
				if (pointValue < 0) {
					pointValue = Double.parseDouble(XPathHelper.getNodeValue("./resprocessing/outcomes/decvar/@maxvalue", questionNode));
				}
				q.setPointValue(pointValue);
			} catch (NumberFormatException e) {
				// this just means we didn't get a number out when we tried to get a point value
				// we can live with that.
			}
			q.setCorrectAnswerIDs(correctAnswerIDs);
			q.setFeedbackWhenCorrect(XPathHelper.getNodeValue("./itemfeedback[@ident = 'correct']//mat_formattedtext[1]", questionNode));
			q.setFeedbackWhenIncorrect(XPathHelper.getNodeValue("./itemfeedback[@ident = 'incorrect']//mat_formattedtext[1]", questionNode));
			rv.add(q);	
		}
		return rv;
	}

	protected String getChoiceImageText(Node questionNode, int choiceNum) {
		String imageUri = XPathHelper.getNodeValue("./presentation//flow[@class='RIGHT_MATCH_BLOCK']/flow[" + choiceNum + "]//matapplication[1]/@uri", questionNode);
		return imageUri.replaceAll("\\\\", "/");
	}

	protected String getAnswerImageText(Node answerNode) {
		String imageUri = XPathHelper.getNodeValue(".//matapplication[1]/@uri", answerNode);
		// no more back slashes!
		return imageUri.replaceAll("\\\\", "/");
	}

	protected String getQuestionImageText(Node questionNode) {
		String imageUri = XPathHelper.getNodeValue("./presentation/flow/flow[@class='QUESTION_BLOCK']/flow[@class='FILE_BLOCK']/material/matapplication[1]/@uri", questionNode);
		// get rid of those pesky back slashes
		return imageUri.replaceAll("\\\\", "/");
	}
}
