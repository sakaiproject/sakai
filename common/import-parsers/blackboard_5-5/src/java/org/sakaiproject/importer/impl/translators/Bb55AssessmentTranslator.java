/**********************************************************************************
 * $URL: https://newtools.oirt.rutgers.edu:8443/repos/sakai2.x/sakai/trunk/archive/import-parsers/blackboard_5-5/src/java/org/sakaiproject/importer/impl/translators/Bb55AssessmentTranslator.java $
 * $Id: Bb55AssessmentTranslator.java 1314 2009-04-08 19:09:09Z weresow $
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.importer.api.IMSResourceTranslator;
import org.sakaiproject.importer.api.Importable;
import org.sakaiproject.importer.impl.Blackboard55FileParser;
import org.sakaiproject.importer.impl.XPathHelper;
import org.sakaiproject.importer.impl.importables.Assessment;
import org.sakaiproject.importer.impl.importables.AssessmentAnswer;
import org.sakaiproject.importer.impl.importables.FileResource;
import org.sakaiproject.importer.impl.importables.AssessmentQuestion;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class Bb55AssessmentTranslator implements IMSResourceTranslator{
	
	private final String rootElement = "ASSESSMENT";

	public String getTypeName() {
		return "assessment/x-bb-quiz";
	}

	public Importable translate(Node resourceNode, Document descriptor, String contextPath, String archiveBasePath) {
		Assessment rv = newImportable();
		rv.setDescription(cleanUpCharacters(XPathHelper.getNodeValue("/" + rootElement() + "/DESCRIPTION/TEXT", descriptor)));
		rv.setTitle(cleanUpCharacters(XPathHelper.getNodeValue("/" + rootElement() + "/TITLE/@value", descriptor)));
		// from the descriptor, add all the questions to this QuestionPool object
		List multiChoiceNodes = XPathHelper.selectNodes("//QUESTION_MULTIPLECHOICE", descriptor);
		List essayNodes = XPathHelper.selectNodes("//QUESTION_ESSAY", descriptor);
		List fillTheBlankNodes = XPathHelper.selectNodes("//QUESTION_FILLINBLANK", descriptor);
		List matchingNodes = XPathHelper.selectNodes("//QUESTION_MATCH", descriptor);
		List multiAnswerNodes = XPathHelper.selectNodes("//QUESTION_MULTIPLEANSWER", descriptor);
		List orderingNodes = XPathHelper.selectNodes("//QUESTION_ORDER", descriptor);
		List trueFalseNodes = XPathHelper.selectNodes("//QUESTION_TRUEFALSE", descriptor);
		
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
		rv.setLegacyGroup(Blackboard55FileParser.ASSESSMENT_GROUP);
		//		try {
		//			rv.setAttachments(getAttachments(resourceNode, descriptor, archiveBasePath));
		//		} catch (IOException e) {
		//			throw new RuntimeException("Bb55AssessmentTranslator could not get the attachment files that are supposed to be there: '" + archiveBasePath +"'", e);
		//		}
		return rv;
	}

	private List getAttachments(Node resourceNode, Document descriptor, String archiveBasePath) throws IOException {
		List rv = new ArrayList();
		List fileNodes = XPathHelper.selectNodes("//FILEREF", descriptor);
		for (Iterator i = fileNodes.iterator(); i.hasNext();) {
			Node fileNode = (Node)i.next();
			String relFile = XPathHelper.getNodeValue("./RELFILE/@value", fileNode);
			String filePath = archiveBasePath + "/" + XPathHelper.getNodeValue("./CONTENTID/@value", fileNode) +
			    "/" + relFile;
			FileResource attachment = new FileResource();
			attachment.setInputStream(new FileInputStream(filePath));
			attachment.setFileName(relFile.substring(relFile.lastIndexOf("/") + 1));
			rv.add(attachment);
		}
		return rv;
	}

	public boolean processResourceChildren() {
		return false;
	}
	
	private List getQuestionsFromNodes(List questionNodes, int questionType) {
		List rv = new ArrayList();
		AssessmentQuestion q;
		Map answers;
		Map choices;
		Node questionNode;
		Node answerNode;
		AssessmentAnswer a;
		for(Iterator i = questionNodes.iterator();i.hasNext();) {
			q = new AssessmentQuestion();
			answers = new HashMap();
			questionNode = (Node)i.next();
			String questionId = XPathHelper.getNodeValue("./@id", questionNode);
			int questionPosition = XPathHelper.selectNodes("../QUESTIONLIST/QUESTION[@id='" + questionId + "']/preceding-sibling::QUESTION", questionNode).size() + 1;
			q.setPosition(new Integer(questionPosition));
			List answerNodes = XPathHelper.selectNodes("./ANSWER", questionNode);
			for (Iterator j = answerNodes.iterator();j.hasNext();) {
				a = new AssessmentAnswer();
				answerNode = (Node)j.next();
				a.setAnswerId(XPathHelper.getNodeValue("./@id", answerNode));
				try {
					a.setPosition(Integer.parseInt(XPathHelper.getNodeValue("./@position", answerNode)));
				} catch (NumberFormatException nfe) {
					// this just means there was no position information in this answer, which is ok.
				}
				String answerText = XPathHelper.getNodeValue("./TEXT", answerNode);
				if (XPathHelper.selectNode("./IMAGE", answerNode) != null) {
					answerText += "\n<p>\n  " + createHtmlImageReference(XPathHelper.selectNode("./IMAGE", answerNode), XPathHelper.getNodeValue("./IMAGE/@style", answerNode)) + "\n</p>";
				}
				answerText = cleanUpCharacters(answerText);
				a.setAnswerText(answerText);
				//TODO parse the date strings in the XML into Date objects
				answers.put(a.getAnswerId(), a);
			}
			if (questionType == AssessmentQuestion.MATCHING) {
				choices = new HashMap();
				AssessmentAnswer c;
				Node choiceNode;
				List choiceNodes = XPathHelper.selectNodes("./CHOICE", questionNode);
				for (Iterator j = choiceNodes.iterator();j.hasNext();) {
					c = new AssessmentAnswer();
					choiceNode = (Node)j.next();
					c.setAnswerId(XPathHelper.getNodeValue("./@id", choiceNode));
					try {
						c.setPosition(Integer.parseInt(XPathHelper.getNodeValue("./@position", choiceNode)));
					} catch (NumberFormatException nfe) {
						// no position information for this choice. That's ok.
					}
					String matchingAnswerText = XPathHelper.getNodeValue("./TEXT", choiceNode);
					c.setAnswerText(cleanUpCharacters(matchingAnswerText));
					// TODO parse the date strings into Date objects
					choices.put(c.getAnswerId(), c);
				}
				q.setChoices(choices);
			}
			//TODO parse the date strings for the question into Date objects
			q.setQuestionType(questionType);
			q.setAnswers(answers);
			String questionTextString = XPathHelper.getNodeValue("./BODY/TEXT", questionNode);
			//TODO replace some crazy characters here with something a browser can actually display
			questionTextString = cleanUpCharacters(questionTextString);
			if (XPathHelper.selectNode("./IMAGE", questionNode) != null) {
				questionTextString += "\n<p>\n  " + createHtmlImageReference(XPathHelper.selectNode("./IMAGE", questionNode), XPathHelper.getNodeValue("./IMAGE/@style", questionNode)) + "\n</p>";
			}
			q.setQuestionText(questionTextString);
			try {
				Double pointValue = Double.parseDouble(XPathHelper.getNodeValue("../QUESTIONLIST/QUESTION[@id='" + questionId + "']/@points", questionNode));
				q.setPointValue(pointValue);
			} catch (NumberFormatException e) {
				// this just means we didn't get a number out when we tried to get a point value
				// Jimmy suggests that questions should have a default point value of 2 points.
				// Update: Whitten requested this be changed to one point
				q.setPointValue(new Double(1.0));
			}
			List correctAnswerNodes = XPathHelper.selectNodes("./GRADABLE/CORRECTANSWER", questionNode);
			Set correctAnswerIDs = new HashSet();
			for (Iterator j = correctAnswerNodes.iterator();j.hasNext();) {
				Node correctAnswerNode = (Node)j.next();
				String correctAnswerId = XPathHelper.getNodeValue("./@answer_id", correctAnswerNode);
				correctAnswerIDs.add(correctAnswerId);
				((AssessmentAnswer)answers.get(correctAnswerId)).setChoiceId(XPathHelper.getNodeValue("./@choice_id", correctAnswerNode));
			}
			q.setCorrectAnswerIDs(correctAnswerIDs);
			q.setFeedbackWhenCorrect(cleanUpCharacters(XPathHelper.getNodeValue("./GRADABLE/FEEDBACK_WHEN_CORRECT", questionNode)));
			q.setFeedbackWhenIncorrect(cleanUpCharacters(XPathHelper.getNodeValue("./GRADABLE/FEEDBACK_WHEN_INCORRECT", questionNode)));
			rv.add(q);	
		}
		return rv;
	}
	
	private String cleanUpCharacters(String questionTextString) {
		String rv = new String(questionTextString);
		char leftSingleQuote = 0x91;
		char rightSingleQuote = 0x92;
		char leftDoubleQuote = 0x93;
		char rightDoubleQuote = 0x94;
		char enDash = 0x96;
		char dash = '-';
		char apos = 0x92;
		char apostrophe = '\'';
		char doubleQuote = '"';
		rv = rv.replace(enDash, dash);
		rv = rv.replace(apos, apostrophe);
		rv = rv.replace(leftSingleQuote, apostrophe);
		rv = rv.replace(rightSingleQuote, apostrophe);
		rv = rv.replace(leftDoubleQuote, doubleQuote);
		rv = rv.replace(rightDoubleQuote, doubleQuote);
		return rv;
	}

	private String createHtmlImageReference(Node imageNode, String referenceType) {
		String imageFileName = XPathHelper.getNodeValue("./@source", imageNode);
		if ("link".equals(referenceType)) {
			return "<a href=\"" + imageFileName + "\" target=\"_blank\">Link to file</a>";
		} else if ("embed".equals(referenceType)) {
			return "<img src=\"" + imageFileName +"\" alt=\"" + imageFileName + "\" />"; 
		} else return "";
	}

	protected Assessment newImportable() {
		return new Assessment();
	}

	protected String rootElement() {
		return rootElement;
	}
	
	protected byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
    
        // Get the size of the file
        long length = file.length();
    
        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }
    
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];
    
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
    
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }
    
        // Close the input stream and return bytes
        is.close();
        return bytes;
    }
}
