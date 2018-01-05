/**
 * Copyright (c) 2007-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.tool.assessment.samlite.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import lombok.extern.slf4j.Slf4j;
import org.apache.xmlbeans.XmlOptions;
import org.imsglobal.xsd.imsQtiasiv1P2.AssessfeedbackType;
import org.imsglobal.xsd.imsQtiasiv1P2.AssessmentType;
import org.imsglobal.xsd.imsQtiasiv1P2.AssessmentcontrolType;
import org.imsglobal.xsd.imsQtiasiv1P2.ConditionvarType;
import org.imsglobal.xsd.imsQtiasiv1P2.DecvarType;
import org.imsglobal.xsd.imsQtiasiv1P2.DisplayfeedbackType;
import org.imsglobal.xsd.imsQtiasiv1P2.FlowMatType;
import org.imsglobal.xsd.imsQtiasiv1P2.FlowType;
import org.imsglobal.xsd.imsQtiasiv1P2.ItemType;
import org.imsglobal.xsd.imsQtiasiv1P2.ItemfeedbackType;
import org.imsglobal.xsd.imsQtiasiv1P2.ItemmetadataType;
import org.imsglobal.xsd.imsQtiasiv1P2.ItemrubricType;
import org.imsglobal.xsd.imsQtiasiv1P2.MaterialType;
import org.imsglobal.xsd.imsQtiasiv1P2.MatimageType;
import org.imsglobal.xsd.imsQtiasiv1P2.MattextType;
import org.imsglobal.xsd.imsQtiasiv1P2.OrType;
import org.imsglobal.xsd.imsQtiasiv1P2.OrderType;
import org.imsglobal.xsd.imsQtiasiv1P2.OutcomesType;
import org.imsglobal.xsd.imsQtiasiv1P2.PresentationMaterialType;
import org.imsglobal.xsd.imsQtiasiv1P2.PresentationType;
import org.imsglobal.xsd.imsQtiasiv1P2.QtimetadataType;
import org.imsglobal.xsd.imsQtiasiv1P2.QtimetadatafieldType;
import org.imsglobal.xsd.imsQtiasiv1P2.QuestestinteropDocument;
import org.imsglobal.xsd.imsQtiasiv1P2.RenderChoiceType;
import org.imsglobal.xsd.imsQtiasiv1P2.RenderFibType;
import org.imsglobal.xsd.imsQtiasiv1P2.RespconditionType;
import org.imsglobal.xsd.imsQtiasiv1P2.ResponseLabelType;
import org.imsglobal.xsd.imsQtiasiv1P2.ResponseLidType;
import org.imsglobal.xsd.imsQtiasiv1P2.ResponseStrType;
import org.imsglobal.xsd.imsQtiasiv1P2.ResprocessingType;
import org.imsglobal.xsd.imsQtiasiv1P2.RubricType;
import org.imsglobal.xsd.imsQtiasiv1P2.SectionType;
import org.imsglobal.xsd.imsQtiasiv1P2.SelectionOrderingType;
import org.imsglobal.xsd.imsQtiasiv1P2.SetvarType;
import org.imsglobal.xsd.imsQtiasiv1P2.VarequalType;
import org.sakaiproject.tool.assessment.samlite.api.Answer;
import org.sakaiproject.tool.assessment.samlite.api.Question;
import org.sakaiproject.tool.assessment.samlite.api.QuestionGroup;
import org.sakaiproject.tool.assessment.samlite.api.SamLiteService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentMetaData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemMetaDataIfc;
import org.w3c.dom.Document;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.util.ResourceLoader;

@Slf4j
public class SamLiteServiceImpl implements SamLiteService {
	public static final String DEFAULT_CHARSET = "UTF-8";
	private static ResourceLoader rb = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.SamLitePatternMessages");
	
	private Pattern justQuestionPattern, startOfQuestionPattern, correctAnswerPattern;
	private Pattern correctFillInPattern, answerPattern, endQuestionPattern, correctMultipleChoicePattern;
	private Pattern shortEssayPattern, correctTruePattern, correctFalsePattern, unnecessaryTruePattern, unnecessaryFalsePattern;
	private Pattern feedbackOKPattern,feedbackNOKPattern;
	private Pattern correctfillInNumericPattern,complexNumberPattern,intervalPattern,realPattern;
	
	private Pattern startOfQuestionNumericPattern;
	private Pattern pointsPattern;
	private Pattern discountPattern;
	String realPatternString = "((\\+||\\-)?(\\d+((\\.|\\,)\\d+)?)((E|e)(\\-|\\+)?\\d+)?)";
	private Pattern randomizePattern;
	private Pattern rationalePattern;
	
	private Pattern extendedMatchingCorrectAnswersPattern;
	
	public void init() {	
		// Initialization		
		startOfQuestionNumericPattern = Pattern.compile("^(\\d+\\.|\\)|\\]\\s*)", Pattern.CASE_INSENSITIVE);
		correctAnswerPattern = Pattern.compile("^\\*");
		correctMultipleChoicePattern = Pattern.compile("^\\*\\s*([a-z])\\.\\s*(.*)", Pattern.CASE_INSENSITIVE);
		correctFillInPattern = Pattern.compile("^\\*\\s*(?!\\{)(.*)");
		answerPattern = Pattern.compile("^([a-z])\\.\\s*(.*)", Pattern.CASE_INSENSITIVE);
		feedbackOKPattern=Pattern.compile("^#FBOK:\\s*(.*)$");
		feedbackNOKPattern=Pattern.compile("^#FBNOK:\\s*(.*)$");
		
		// REGEX: ^(\d+\.+ ).*\[[a-z[ ,]]*\].* - start with digits point space then string containing brackets with [a-z] commas or spaces 
		extendedMatchingCorrectAnswersPattern = Pattern.compile("^(\\d+\\.+ ).*\\[[a-z[ ,]]*\\].*", Pattern.CASE_INSENSITIVE);
		
		correctfillInNumericPattern = Pattern.compile("^\\*\\{(.*)\\}");
		complexNumberPattern = Pattern.compile("^\\*\\{"+realPatternString+"(\\s*)(\\+||\\-)(\\s*)"+realPatternString+"(i|I)}");
		intervalPattern = Pattern.compile("^\\*\\{(\\s*)"+realPatternString+"(\\s*)(\\|)(\\s*)"+realPatternString+"(\\s*)}");
		realPattern = Pattern.compile("^\\*\\{"+realPatternString+"}");
	} 
	
	public Question saveLast(QuestionGroup questionGroup, Question question) {
		
		if (null != question) {
			// Short essay questions don't have any answer, so it's the default type
			if (Question.UNDEFINED_QUESTION == question.getQuestionType())
				question.setQuestionType(Question.SHORT_ESSAY_QUESTION);
			// If it doesn't have points yet, it's not going to get any
			if (!question.hasPoints())
				question.setQuestionPoints("0");
			question.postProcessing();
			questionGroup.addQuestion(question);
		}
		
		return new Question();
	}
	
	
	private String getPoints(String line) {
		Matcher pointsMatcher = pointsPattern.matcher(line);
		
		String points = "";
		
		if (pointsMatcher.find()) 
			points = pointsMatcher.group(1);
		
		return points;
	}
	
	
	private String stripPoints(String line) {
		Matcher pointsMatcher = pointsPattern.matcher(line);
		
		StringBuffer sb = new StringBuffer();
		while (pointsMatcher.find()) {
			pointsMatcher.appendReplacement(sb, "");
		}
		pointsMatcher.appendTail(sb);
		
		return sb.toString();
	}
	
	private String getDiscount(String line) {
		Matcher discountMatcher = discountPattern.matcher(line);
				
		String discount = "";
				
		if (discountMatcher.find()) 
			discount = discountMatcher.group(1);
				
		return discount;
	}
	
	private String stripDiscount(String line) {
		Matcher discountMatcher = discountPattern.matcher(line);
				
		StringBuffer sb = new StringBuffer();
		while (discountMatcher.find()) {
			discountMatcher.appendReplacement(sb, "");
		}
		discountMatcher.appendTail(sb);
				
		return sb.toString();
	}
			
	private String removeMatchedPattern(Matcher m) {
		StringBuffer buffer = new StringBuffer();
		
		m.appendReplacement(buffer, "");
		m.appendTail(buffer);
		
		return buffer.toString();
	}
	
	
	public QuestionGroup parse(String name, String description, String data) {
		String stQuestion = rb.getString("question");
		startOfQuestionPattern = Pattern.compile("^(" + stQuestion + "\\s*\\d*\\s*)", Pattern.CASE_INSENSITIVE);
		String stPoints = rb.getString("points");
		pointsPattern = Pattern.compile("\\((\\d*\\.?\\d*)\\s+" + stPoints + "\\)", Pattern.CASE_INSENSITIVE);
		String stDiscount = rb.getString("discount");
		discountPattern = Pattern.compile("\\((\\d*\\.?\\d*)\\s+" + stDiscount + "\\)", Pattern.CASE_INSENSITIVE);
		String stSaveAnswer = rb.getString("save_answer");
		endQuestionPattern = Pattern.compile("^" + stSaveAnswer, Pattern.CASE_INSENSITIVE);
		String stTrue = rb.getString("true");
		String stFalse = rb.getString("false");
		correctTruePattern = Pattern.compile("^\\*\\s*" + stTrue + "$");
		correctFalsePattern = Pattern.compile("^\\*\\s*" + stFalse + "$");
		unnecessaryTruePattern = Pattern.compile("^" + stTrue + "$");
		unnecessaryFalsePattern = Pattern.compile("^" + stFalse + "$");
		String txtRandomize = rb.getString("randomize", "#randomize");
		randomizePattern = Pattern.compile("^" + txtRandomize + "$", Pattern.CASE_INSENSITIVE);
		String txtRationale = rb.getString("rationale", "#rationale");
		rationalePattern = Pattern.compile("^" + txtRationale + "$", Pattern.CASE_INSENSITIVE);




		QuestionGroup questionGroup = new QuestionGroup(name, description);
		
		String cleanData = data; 
		
		String[] lines = cleanData.split("\\n");
		Question question = null;
		
		int questionNumber = 1;
	
		for (int i=0;i<lines.length;i++) {
			if (lines[i].endsWith("<br />"))
				lines[i] = lines[i].replaceAll("<br />", "").replace('\r', ' ');
			String line = lines[i].trim();
			
			if (null != line && !"".equals(line)) {	
				Matcher startOfQuestionMatcher = startOfQuestionPattern.matcher(line);
				Matcher startOfQuestionNumericMatcher = startOfQuestionNumericPattern.matcher(line);
				Matcher pointsMatcher = pointsPattern.matcher(line);
				Matcher discountMatcher = discountPattern.matcher(line);
				Matcher extendedMatchingCorrectAnswersMatcher = extendedMatchingCorrectAnswersPattern.matcher(line);
				
				// The question can begin with the word 'Question'
				boolean isQuestionStart = startOfQuestionMatcher.find();
				// Or it can begin with a number followed by a delimitor
				boolean isQuestionNumericStart = startOfQuestionNumericMatcher.find(); 
				// Some users may prefer to delineate questions with just the points line
				boolean isJustPoints = pointsMatcher.find();
				// Some users may want to specify the discount line
				boolean isJustDiscount = discountMatcher.find();
				
				boolean isEMICorrectAnswerLine = extendedMatchingCorrectAnswersMatcher.find(); 
				
				if (!isEMICorrectAnswerLine && (isQuestionStart || isQuestionNumericStart || isJustPoints || isJustDiscount)) {
					question = saveLast(questionGroup, question);
					
					if (isQuestionStart)
						line = removeMatchedPattern(startOfQuestionMatcher);
					else if (isQuestionNumericStart)
						line = removeMatchedPattern(startOfQuestionNumericMatcher);
									
					String points = getPoints(line);
					
					question.setQuestionPoints(points);
					
					String discount = getDiscount(line);
 					
					question.setQuestionDiscount(discount);
										
					String questionText = stripDiscount(stripPoints(line));
					
					question.append(questionText.trim());
					
					// Don't bother looking at what the user enters for question numbers, since they 
					// could introduce errors -- we're going to number by order of questions
					question.setQuestionNumber(questionNumber);
					
					questionNumber++;
				} else if (null != question) 
					parseLine(question, line);
			}
		}
		// Make sure we get that last question
		if (null != question) 
			saveLast(questionGroup, question);
			
		return questionGroup;
	}
	
	
	private void parseLine(Question question, String line) {				
		boolean isEndOfQuestion = endQuestionPattern.matcher(line).find();
		boolean isCorrectAnswer = correctAnswerPattern.matcher(line).lookingAt();
		Matcher answerMatcher = answerPattern.matcher(line);
		boolean isAnswer = answerMatcher.find();
		boolean isEmptyTrue = unnecessaryTruePattern.matcher(line).find();
		boolean isEmptyFalse = unnecessaryFalsePattern.matcher(line).find();		
		Matcher feedbackOKMatcher = feedbackOKPattern.matcher(line);
		boolean hasfeedbackOK = feedbackOKMatcher.find();
		Matcher feedbackNOKMatcher = feedbackNOKPattern.matcher(line);
		boolean hasfeedbackNOK = feedbackNOKMatcher.find();
		boolean randomize = randomizePattern.matcher(line).find();
		boolean rationale = rationalePattern.matcher(line).find();
		
		boolean isEMICorrectAnswer = extendedMatchingCorrectAnswersPattern.matcher(line).find();
		
		if (isEndOfQuestion) {
			// Do nothing, we just want to ignore this line
		} else if (isAnswer) {
			question.addAnswer(answerMatcher.group(1), answerMatcher.group(2), false);
		} else if (isEMICorrectAnswer) {
	  		question.setQuestionType(Question.EXTENDED_MATCHING_ITEMS_QUESTION);
	  		String answerId = line.substring(0, line.indexOf("."));
	  		String questionAnswers = (line.substring(line.indexOf(".")+1)).trim();
			question.addAnswer(answerId, questionAnswers, true);
		} else if (isCorrectAnswer) {
			Matcher multipleChoiceMatcher = correctMultipleChoicePattern.matcher(line);
			boolean isMC = multipleChoiceMatcher.find();
			Matcher fillInMatcher = correctFillInPattern.matcher(line);
			boolean isFI = fillInMatcher.find();
			boolean isTrue = correctTruePattern.matcher(line).find();
			boolean isFalse = correctFalsePattern.matcher(line).find();
			Matcher fillInNumericMatcher = correctfillInNumericPattern.matcher(line);
			boolean isFillInNumeric = fillInNumericMatcher.find();
			
			if (isMC) {
				String earlierCorrectAnswer = question.getCorrectAnswer();
				boolean hasOneCorrectAnswerAlready = (null != earlierCorrectAnswer && !"".equals(earlierCorrectAnswer.trim()));
				
				question.setCorrectAnswer(multipleChoiceMatcher.group(1));
			  	question.addAnswer(multipleChoiceMatcher.group(1), multipleChoiceMatcher.group(2), true);
			  	
			  	if (hasOneCorrectAnswerAlready)
			  		question.setQuestionType(Question.MULTIPLE_CHOICE_MULTIPLE_ANSWER_QUESTION);
			  	else
			  		question.setQuestionType(Question.MULTIPLE_CHOICE_QUESTION);
			} else if (isTrue) {
				question.addAnswer("A", "True", true);
				question.addAnswer("B", "False", false);
				question.setQuestionType(Question.TRUE_FALSE_QUESTION);
				question.setCorrectAnswer("True");
			} else if (isFalse) {
				question.addAnswer("A", "True", false);
				question.addAnswer("B", "False", true);
				question.setQuestionType(Question.TRUE_FALSE_QUESTION);
				question.setCorrectAnswer("False");
			} else if (isFI) {
	  			question.setCorrectAnswer(fillInMatcher.group(1));
		  		question.setQuestionType(Question.FILL_IN_THE_BLANK_QUESTION);
	  		} else if (isFillInNumeric) {
				String correctAnswer = fillInNumericMatcher.group(1);
				if(isValidNumericResponse(line))
					question.setCorrectAnswer(correctAnswer);
		  		question.setQuestionType(Question.FILL_IN_NUMERIC_QUESTION);
	  		}
		} else if (isEmptyTrue || isEmptyFalse) {
			// Do nothing, since the 'correct' true or false answer is all we need.
		} else if (hasfeedbackOK) {
			question.setFeedbackOK(feedbackOKMatcher.group(1));
		} else if (hasfeedbackNOK) {
			question.setFeedbackNOK(feedbackNOKMatcher.group(1));
		} else if (randomize) {
			if (question.getQuestionType() == Question.MULTIPLE_CHOICE_QUESTION || 
				question.getQuestionType() == Question.MULTIPLE_CHOICE_MULTIPLE_ANSWER_QUESTION) {
				question.setRandomize(randomize);
			}


		} else if (rationale) {
			if (question.getQuestionType() == Question.MULTIPLE_CHOICE_QUESTION || 
					question.getQuestionType() == Question.MULTIPLE_CHOICE_MULTIPLE_ANSWER_QUESTION || 
					question.getQuestionType() == Question.TRUE_FALSE_QUESTION) {
				question.setRationale(rationale);
			}

			
		}



		else {
			// If we didn't match anything, then assume it's just part of the question text
			question.append(line);
		}
	}
	
	public Document createDocument(QuestionGroup questionGroup) {
		log.debug("Creating a new qti document with the following name: " + questionGroup.getName());
		QuestestinteropDocument document = createQTIDocument(questionGroup);
		XmlOptions options = new XmlOptions();
		
		options.setSavePrettyPrintIndent(4);
		options.setSavePrettyPrint();
		options.setUseDefaultNamespace();
		Map prefixes = new HashMap();
		prefixes.put("", "http://www.imsglobal.org/xsd/ims_qtiasiv1p2");
		options.setSaveImplicitNamespaces(prefixes);

		InputStream inputStream = null;
		try {
			inputStream = document.newInputStream(options);	

	      	DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
	      	builderFactory.setNamespaceAware(true);
	      	DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
	      	Document doc = documentBuilder.parse(inputStream);
					
		    return doc;
		} catch (ParserConfigurationException pce) { 
			log.error("Unable to parse quiz document ", pce);
		} catch (IOException ioe) { 
			log.error("Low-level IOException caught reading quiz document ", ioe);
		} catch (Exception qse) {
			log.error("Unable to save this quiz as an imported assessment ", qse);
		} finally {
			if (null != inputStream) {
				try { inputStream.close(); } catch (IOException ioe) { log.warn("Unimportant ioe"); }
			}
		}
		
		return null;
	}
	
	
	public QuestestinteropDocument createQTIDocument(QuestionGroup questionGroup) {
		// Create a new instance of the document object
		QuestestinteropDocument doc = QuestestinteropDocument.Factory.newInstance();
		// Add a Questestinterop object
		doc.addNewQuestestinterop();
		// Add an assessment object
		doc.getQuestestinterop().addNewAssessment();
		// Grab the assessment in a local var
		AssessmentType assessment = doc.getQuestestinterop().getAssessment();
		assessment.setTitle(questionGroup.getName());
		// There's a bunch of metadata that we pre-populate here
		addAssessmentMetadata(assessment);
		// Now we're getting into the necessary xml fragments that make up the QTI schema
		AssessmentcontrolType assessmentControl = assessment.addNewAssessmentcontrol();
		assessmentControl.setFeedbackswitch(AssessmentcontrolType.Feedbackswitch.YES);
		assessmentControl.setHintswitch(AssessmentcontrolType.Hintswitch.YES);
		assessmentControl.setSolutionswitch(AssessmentcontrolType.Solutionswitch.YES);
		assessmentControl.setView(AssessmentcontrolType.View.ALL);
		
		RubricType rubric = assessment.addNewRubric();
		buildMattext(rubric.addNewMaterial().addNewMattext());
		
		PresentationMaterialType presentationMaterial = assessment.addNewPresentationMaterial();
		FlowMatType pFlowMat = presentationMaterial.addNewFlowMat();
		pFlowMat.setClass1("Block");
		buildMattext(pFlowMat.addNewMaterial().addNewMattext(), questionGroup.getDescription());
		
		AssessfeedbackType assessFeedback = assessment.addNewAssessfeedback();
		assessFeedback.setIdent("Feedback");
		assessFeedback.setTitle("Feedback");
		assessFeedback.setView(AssessfeedbackType.View.ALL);
		
		FlowMatType afFlowMat = assessFeedback.addNewFlowMat();
		buildMattext(afFlowMat.addNewMaterial().addNewMattext());

		// Add the section
		SectionType section = assessment.addNewSection();
		section.setIdent("Multiple Choice Questions");
		
		QtimetadataType sectionMetaData = section.addNewQtimetadata();
		buildMetaDataField(sectionMetaData, "SECTION_OBJECTIVE", "");
		buildMetaDataField(sectionMetaData, "SECTION_KEYWORD", "");
		buildMetaDataField(sectionMetaData, "SECTION_RUBRIC", "");
		
		PresentationMaterialType sectionPresentationMaterial = section.addNewPresentationMaterial();
		FlowMatType spFlowMat = sectionPresentationMaterial.addNewFlowMat();
		spFlowMat.setClass1("Block");
		buildMattext(spFlowMat.addNewMaterial().addNewMattext());
		buildEmptyMaterialImage(spFlowMat.addNewMaterial().addNewMatimage());
		
		SelectionOrderingType selectionOrdering = section.addNewSelectionOrdering();
		selectionOrdering.setSequenceType("Normal");
		
		OrderType order = selectionOrdering.addNewOrder();
		order.setOrderType("Sequential");
		
		// Now, loop through the questions and add to the qti section object
		if (null != questionGroup.getQuestions()) {
			for (Iterator it = questionGroup.getQuestions().iterator();it.hasNext();) {
				Question question = (Question)it.next();
				processQuestion(section, question);
			}
		}
		
		return doc;
	}
		
	
	private void buildMetaDataField(QtimetadataType metadata, String label, String entry) {
		QtimetadatafieldType field = metadata.addNewQtimetadatafield();
		field.setFieldlabel(label);
		field.setFieldentry(entry);
	}
	
	private void addAssessmentMetadata(AssessmentType assessment) {
		QtimetadataType metadata = assessment.addNewQtimetadata();
		
		AssessmentService service = new AssessmentService();
		List metadataList = service.getDefaultMetaDataSet();
		Iterator iter = metadataList.iterator();
		String automaticSubmissionEditable = "true";
		while (iter.hasNext()) {
			AssessmentMetaData mData = (AssessmentMetaData) iter.next();
			if ("automaticSubmission_isInstructorEditable".equals((String) mData.getLabel())) {
				automaticSubmissionEditable = (String) mData.getEntry();
			}
			else {
				String label = (String) mData.getLabel();
				String entry = (String) mData.getEntry();
				buildMetaDataField(metadata, label, entry);
			}
			
		}
		
		boolean autoSubmitEnabled = ServerConfigurationService.getBoolean("samigo.autoSubmit.enabled", false);
	    if (!autoSubmitEnabled) {
	    	buildMetaDataField(metadata, "automaticSubmission_isInstructorEditable", "false");
	    }
	    else {
	    	buildMetaDataField(metadata, "automaticSubmission_isInstructorEditable", automaticSubmissionEditable);
	    }
	}
	
	private void processQuestion(SectionType section, Question question) {
		log.debug("Processing a question");
		switch (question.getQuestionType()) {
		case Question.MULTIPLE_CHOICE_QUESTION:
			processMultipleChoiceQuestion(section, question);
			break;
		case Question.MULTIPLE_CHOICE_MULTIPLE_ANSWER_QUESTION:
			processMultipleChoiceMultipleAnswerQuestion(section, question);
			break;
		case Question.FILL_IN_THE_BLANK_QUESTION:
			processFillInQuestion(section, question);
			break;
		case Question.FILL_IN_NUMERIC_QUESTION:
			processFillInNumericQuestion(section, question);
			break;	
		case Question.SHORT_ESSAY_QUESTION:
			processShortEssayQuestion(section, question);
			break;
		case Question.TRUE_FALSE_QUESTION:
			processTrueFalseQuestion(section, question);
			break;
		case Question.EXTENDED_MATCHING_ITEMS_QUESTION:
			processExtendedMatchingItemsQuestion(section, question);
			break;
		default:
			// TODO: Notify the user that this question didn't work...	
		};
	}
	
	private void buildMattext(MattextType mattext) {
		mattext.setCharset(DEFAULT_CHARSET);
		mattext.setTexttype("text/plain");
	}
	
	private void buildMattext(MattextType mattext, String value) {
		buildMattext(mattext);
		mattext.setStringValue(cdata(value));
	}
	
	private String cdata(String value) {
		return value;
		//new StringBuffer().append("<![CDATA[").append(value).append("]]>").toString();
	}
	
	private void buildEmptyMaterialImage(MatimageType matImage) {
		matImage.setEmbedded("base64");
		matImage.setImagtype("text/html");
	}
		
	private void buildItemFeedback(ItemType item, String identity) {
		ItemfeedbackType ifeedback = item.addNewItemfeedback();
		ifeedback.setIdent(identity);
		ifeedback.setView(ItemfeedbackType.View.ALL);
		FlowMatType ifflow = ifeedback.addNewFlowMat();
		ifflow.setClass1("Block");
		buildMattext(ifflow.addNewMaterial().addNewMattext());
		buildEmptyMaterialImage(ifflow.addNewMaterial().addNewMatimage());
	}

	private void buildItemFeedback(ItemType item, String identity, String feedback) {
		ItemfeedbackType ifeedback = item.addNewItemfeedback();
		ifeedback.setIdent(identity);
		ifeedback.setView(ItemfeedbackType.View.ALL);
		FlowMatType ifflow = ifeedback.addNewFlowMat();
		ifflow.setClass1("Block");
		buildMattext(ifflow.addNewMaterial().addNewMattext(),feedback);
		buildEmptyMaterialImage(ifflow.addNewMaterial().addNewMatimage());
	}
	
	private void buildPresentationAndResponseLid(ItemType item, Question question, String label, 
			String respLidId, ResponseLidType.Rcardinality.Enum rCardinality) {	
		
		PresentationType presentation = item.addNewPresentation();
		presentation.setLabel(label);
		FlowType flow = presentation.addNewFlow();
		flow.setClass1("Block");
		MaterialType material = flow.addNewMaterial();
		buildMattext(material.addNewMattext(), cdata(question.getQuestion()));
		
		MaterialType material2 = flow.addNewMaterial();
		buildMattext(material2.addNewMattext());
		
		// Only for multiple choice
		if (Question.MULTIPLE_CHOICE_QUESTION == question.getQuestionType() ||
				Question.MULTIPLE_CHOICE_MULTIPLE_ANSWER_QUESTION == question.getQuestionType()) {
			MatimageType matImage = material2.addNewMatimage();
			matImage.setEmbedded("base64");
			matImage.setImagtype("text/html");
			matImage.setUri("");
		}
		
		ResponseLidType responseLid = flow.addNewResponseLid();
		responseLid.setIdent(respLidId);
		responseLid.setRcardinality(rCardinality);
		responseLid.setRtiming(ResponseLidType.Rtiming.NO);
		RenderChoiceType renderChoice = responseLid.addNewRenderChoice();
		renderChoice.setShuffle(RenderChoiceType.Shuffle.NO);
		
		char c = 'A';
		for (Iterator it = question.getAnswers().iterator();it.hasNext();) {		
			Answer answer = (Answer)it.next();
			ResponseLabelType responseLabel = renderChoice.addNewResponseLabel();
			responseLabel.setIdent(String.valueOf(c));
			responseLabel.setRarea(ResponseLabelType.Rarea.ELLIPSE);
			responseLabel.setRrange(ResponseLabelType.Rrange.EXACT);
			responseLabel.setRshuffle(ResponseLabelType.Rshuffle.YES);
			MaterialType mat = responseLabel.addNewMaterial();
			MattextType text = mat.addNewMattext();
			text.setCharset(DEFAULT_CHARSET);
			text.setTexttype("text/plain");
			text.setStringValue(cdata(answer.getText()));
			MaterialType m2 = responseLabel.addNewMaterial();
			MatimageType mi = m2.addNewMatimage();
			mi.setEmbedded("base64");
			mi.setImagtype("text/html");
			mi.setUri("");
			
			c++;
		}
	}
	
	private void addResponseLid(FlowType flow, Question question, boolean isMultipleAnswer) {
		ResponseLidType responseLid = flow.addNewResponseLid();
		responseLid.setIdent("MCSC");
		if (isMultipleAnswer)
			responseLid.setRcardinality(ResponseLidType.Rcardinality.MULTIPLE);
		else
			responseLid.setRcardinality(ResponseLidType.Rcardinality.SINGLE);
		responseLid.setRtiming(ResponseLidType.Rtiming.NO);
		RenderChoiceType renderChoice = responseLid.addNewRenderChoice();
		renderChoice.setShuffle(RenderChoiceType.Shuffle.NO);
		
		char c = 'A';
		for (Iterator it = question.getAnswers().iterator();it.hasNext();) {		
			Answer answer = (Answer)it.next();
			ResponseLabelType responseLabel = renderChoice.addNewResponseLabel();
			responseLabel.setIdent(String.valueOf(c));
			responseLabel.setRarea(ResponseLabelType.Rarea.ELLIPSE);
			responseLabel.setRrange(ResponseLabelType.Rrange.EXACT);
			responseLabel.setRshuffle(ResponseLabelType.Rshuffle.YES);
			MaterialType mat = responseLabel.addNewMaterial();
			MattextType text = mat.addNewMattext();
			text.setCharset(DEFAULT_CHARSET);
			text.setTexttype("text/plain");
			text.setStringValue(cdata(answer.getText()));
			MaterialType m2 = responseLabel.addNewMaterial();
			MatimageType mi = m2.addNewMatimage();
			mi.setEmbedded("base64");
			mi.setImagtype("text/html");
			mi.setUri("");
			
			c++;
		}
	}
	
	private void addRespProcessing(ItemType item, Question question, String respident) {
		ResprocessingType resProcessing = item.addNewResprocessing();
		DecvarType decvar = resProcessing.addNewOutcomes().addNewDecvar();
		decvar.setDefaultval("0");
		decvar.setMaxvalue(question.getQuestionPoints());
		if (Question.MULTIPLE_CHOICE_MULTIPLE_ANSWER_QUESTION != question.getQuestionType())
			decvar.setMinvalue(question.getQuestionDiscount());
		else
			decvar.setMinvalue("0");
		decvar.setVarname("SCORE");
		decvar.setVartype(DecvarType.Vartype.INTEGER);
		
		char alpha = 'A';
		
		if (null != question.getAnswers()) {
			for (Iterator it = question.getAnswers().iterator();it.hasNext();) {	
				Answer answer = (Answer)it.next();
				String response = answer.isCorrect() ? "Correct" : "InCorrect";
				
				RespconditionType respCondition = resProcessing.addNewRespcondition();
				respCondition.setContinue(RespconditionType.Continue.NO);
				ConditionvarType conditionVar = respCondition.addNewConditionvar();
				VarequalType varEqual = conditionVar.addNewVarequal();
				varEqual.setCase(VarequalType.Case.YES);
				varEqual.setRespident(respident);
				varEqual.setStringValue(String.valueOf(alpha));
				SetvarType setVar = respCondition.addNewSetvar();
				setVar.setAction(SetvarType.Action.ADD);
				setVar.setVarname("SCORE");
				setVar.setStringValue("0.0");
				DisplayfeedbackType feedback1 = respCondition.addNewDisplayfeedback();
				feedback1.setFeedbacktype(DisplayfeedbackType.Feedbacktype.RESPONSE);
				feedback1.setLinkrefid(response);
				DisplayfeedbackType feedback2 = respCondition.addNewDisplayfeedback();
				feedback2.setFeedbacktype(DisplayfeedbackType.Feedbacktype.RESPONSE);
				feedback2.setLinkrefid("AnswerFeedback");
				feedback2.setStringValue(cdata(""));
				
				alpha++;
			}
		}
	}
	
	private void processExtendedMatchingItemsQuestion(SectionType section, Question question) {
		ItemType item = section.addNewItem();
		item.setTitle(question.getQuestionTypeAsString());
		
		ItemmetadataType itemMetaData = item.addNewItemmetadata();
		QtimetadataType qtiMetaData = itemMetaData.addNewQtimetadata();
		
		buildMetaDataField(qtiMetaData, "qmd_itemtype", question.getQuestionTypeAsString());
		buildMetaDataField(qtiMetaData, "TEXT_FORMAT", "HTML");
		buildMetaDataField(qtiMetaData, "hasRationale", "False");
		
		ItemrubricType itemRubric = item.addNewItemrubric();
		MattextType mattext = itemRubric.addNewMaterial().addNewMattext();
		mattext.setCharset("UTF-8");
		mattext.setTexttype("text/plain");

		buildPresentationAndResponseLid(item, question, "Resp001", "EMIQ", ResponseLidType.Rcardinality.MULTIPLE);
		
		addRespProcessing(item, question, "EMIQ");
		
		int numberOfAnswers = question.getAnswers().size();
		char c = 'A';
		for (int i=0;i<numberOfAnswers;i++) {	
			buildItemFeedback(item, String.valueOf(c) + "1");
			c++;
		}
		
		buildItemFeedback(item, "Correct");
		buildItemFeedback(item, "InCorrect");
	}
        
	private void processTrueFalseQuestion(SectionType section, Question question) {
		ItemType item = section.addNewItem();
		item.setTitle("True-False");
		
		ItemmetadataType itemMetaData = item.addNewItemmetadata();
		QtimetadataType qtiMetaData = itemMetaData.addNewQtimetadata();
		
		buildMetaDataField(qtiMetaData, "qmd_itemtype", "True False");
		buildMetaDataField(qtiMetaData, "TEXT_FORMAT", "HTML");
		buildMetaDataField(qtiMetaData, "hasRationale", Boolean.valueOf(question.isRationale()).toString());
		
		ItemrubricType itemRubric = item.addNewItemrubric();
		itemRubric.setView(ItemrubricType.View.ALL);
		
		MattextType mattext = itemRubric.addNewMaterial().addNewMattext();
		mattext.setCharset(DEFAULT_CHARSET);
		mattext.setTexttype("text/plain");

		buildPresentationAndResponseLid(item, question, "Resp001", "TF02", ResponseLidType.Rcardinality.SINGLE);
		
		addRespProcessing(item, question, "TF02");
		
		buildItemFeedback(item, "Correct",question.getFeedbackOK() );
		buildItemFeedback(item, "InCorrect", question.getFeedbackNOK());
	}
	
	private void processMultipleChoiceQuestion(SectionType section, Question question) {
		
		ItemType item = section.addNewItem();
		item.setTitle("Multiple Choice");
		
		ItemmetadataType itemMetaData = item.addNewItemmetadata();
		QtimetadataType qtiMetaData = itemMetaData.addNewQtimetadata();
		
		buildMetaDataField(qtiMetaData, "qmd_itemtype", "Multiple Choice");
		buildMetaDataField(qtiMetaData, "TEXT_FORMAT", "HTML");
		buildMetaDataField(qtiMetaData, "hasRationale", Boolean.valueOf(question.isRationale()).toString());
		buildMetaDataField(qtiMetaData, ItemMetaDataIfc.RANDOMIZE, Boolean.valueOf(question.isRandomize()).toString());
		
		ItemrubricType itemRubric = item.addNewItemrubric();
		MattextType mattext = itemRubric.addNewMaterial().addNewMattext();
		mattext.setCharset("UTF-8");
		mattext.setTexttype("text/plain");

		buildPresentationAndResponseLid(item, question, "Resp001", "MCSC", ResponseLidType.Rcardinality.SINGLE);
		
		addRespProcessing(item, question, "MCSC");
		
		int numberOfAnswers = question.getAnswers().size();
		char c = 'A';
		for (int i=0;i<numberOfAnswers;i++) {	
			buildItemFeedback(item, String.valueOf(c) + "1");
			c++;
		}
		buildItemFeedback(item, "Correct",question.getFeedbackOK() );
		buildItemFeedback(item, "InCorrect", question.getFeedbackNOK());
	}
	
	private void processMultipleChoiceMultipleAnswerQuestion(SectionType section, Question question) {
		
		ItemType item = section.addNewItem();
		item.setTitle("Multiple Correct");
		
		ItemmetadataType itemMetaData = item.addNewItemmetadata();
		QtimetadataType qtiMetaData = itemMetaData.addNewQtimetadata();
		
		buildMetaDataField(qtiMetaData, "qmd_itemtype", "Multiple Correct Answer");
		buildMetaDataField(qtiMetaData, "TEXT_FORMAT", "HTML");
		buildMetaDataField(qtiMetaData, "hasRationale", Boolean.valueOf(question.isRationale()).toString());
		buildMetaDataField(qtiMetaData, ItemMetaDataIfc.RANDOMIZE, Boolean.valueOf(question.isRandomize()).toString());
		
		ItemrubricType itemRubric = item.addNewItemrubric();
		MattextType mattext = itemRubric.addNewMaterial().addNewMattext();
		mattext.setCharset("UTF-8");
		mattext.setTexttype("text/plain");
		
		buildPresentationAndResponseLid(item, question, "Resp001", "MCMC", ResponseLidType.Rcardinality.MULTIPLE);
		
		addRespProcessing(item, question, "MCMC");
		
		int numberOfAnswers = question.getAnswers().size();
		char c = 'A';
		for (int i=0;i<numberOfAnswers;i++) {	
			buildItemFeedback(item, String.valueOf(c) + "1");
			c++;
		}
		buildItemFeedback(item, "Correct",question.getFeedbackOK() );
		buildItemFeedback(item, "InCorrect", question.getFeedbackNOK());
	}
	
	private void processFillInQuestion(SectionType section, Question question) {
		ItemType item = section.addNewItem();
		item.setTitle("Fill in the Blank");
		
		ItemmetadataType itemMetaData = item.addNewItemmetadata();
		QtimetadataType qtiMetaData = itemMetaData.addNewQtimetadata();
		
		buildMetaDataField(qtiMetaData, "qmd_itemtype", "Fill In the Blank");
		buildMetaDataField(qtiMetaData, "TEXT_FORMAT", "HTML");
		buildMetaDataField(qtiMetaData, "MUTUALLY_EXCLUSIVE", "True");
		
		ItemrubricType itemRubric = item.addNewItemrubric();
		MattextType mattext = itemRubric.addNewMaterial().addNewMattext();
		mattext.setCharset(DEFAULT_CHARSET);
		mattext.setTexttype("text/plain");

		PresentationType presentation = item.addNewPresentation();
		presentation.setLabel("FIB");
		FlowType flow = presentation.addNewFlow();
		flow.setClass1("Block");
		MaterialType material = flow.addNewMaterial();
		buildMattext(material.addNewMattext(), cdata(question.getQuestion()));
		
		MaterialType material2 = flow.addNewMaterial();
		buildMattext(material2.addNewMattext());
		
		ResponseStrType responseStr = flow.addNewResponseStr();
		responseStr.setRcardinality(ResponseStrType.Rcardinality.ORDERED);
		responseStr.setRtiming(ResponseStrType.Rtiming.NO);
		
		RenderFibType renderFib = responseStr.addNewRenderFib();
		renderFib.setCharset(DEFAULT_CHARSET);
		renderFib.setColumns("5");
		renderFib.setEncoding("UTF_8");
		renderFib.setFibtype(RenderFibType.Fibtype.STRING);
		renderFib.setPrompt(RenderFibType.Prompt.BOX);
		renderFib.setRows("1");
		
		MaterialType material3 = flow.addNewMaterial();
		buildMattext(material3.addNewMattext());
		
		ResprocessingType resProcessing = item.addNewResprocessing();
		OutcomesType outcomes = resProcessing.addNewOutcomes();
		DecvarType decvar = outcomes.addNewDecvar();
		decvar.setDefaultval("0");
		decvar.setMaxvalue(question.getQuestionPoints());
		decvar.setMinvalue("0");
		decvar.setVarname("SCORE");
		decvar.setVartype(DecvarType.Vartype.INTEGER);
		
		RespconditionType respCondition = resProcessing.addNewRespcondition();
		respCondition.setContinue(RespconditionType.Continue.YES);
		ConditionvarType condition = respCondition.addNewConditionvar();
		OrType or = condition.addNewOr();
		VarequalType varequal = or.addNewVarequal();
		varequal.setCase(VarequalType.Case.NO);
		varequal.setStringValue(cdata(question.getCorrectAnswer()));
		SetvarType setvar = respCondition.addNewSetvar();
		setvar.setAction(SetvarType.Action.ADD);
		setvar.setVarname("SCORE");
		setvar.setStringValue("0");
		
		buildItemFeedback(item, "Correct",question.getFeedbackOK() );
		buildItemFeedback(item, "InCorrect", question.getFeedbackNOK());
	}
	
	private void processFillInNumericQuestion(SectionType section, Question question) {
		ItemType item = section.addNewItem();
		item.setTitle("Numeric Response");
		
		ItemmetadataType itemMetaData = item.addNewItemmetadata();
		QtimetadataType qtiMetaData = itemMetaData.addNewQtimetadata();
		
		buildMetaDataField(qtiMetaData, "qmd_itemtype", "Numeric Response");
		buildMetaDataField(qtiMetaData, "TEXT_FORMAT", "HTML");
		
		ItemrubricType itemRubric = item.addNewItemrubric();
		MattextType mattext = itemRubric.addNewMaterial().addNewMattext();
		mattext.setCharset(DEFAULT_CHARSET);
		mattext.setTexttype("text/plain");

		PresentationType presentation = item.addNewPresentation();
		presentation.setLabel("FIN");
		FlowType flow = presentation.addNewFlow();
		flow.setClass1("Block");
		MaterialType material = flow.addNewMaterial();
		buildMattext(material.addNewMattext(), cdata(question.getQuestion()));
		
		MaterialType material2 = flow.addNewMaterial();
		buildMattext(material2.addNewMattext());
		
		ResponseStrType responseStr = flow.addNewResponseStr();
		responseStr.setRcardinality(ResponseStrType.Rcardinality.ORDERED);
		responseStr.setRtiming(ResponseStrType.Rtiming.NO);
		
		RenderFibType renderFib = responseStr.addNewRenderFib();
		renderFib.setCharset(DEFAULT_CHARSET);
		renderFib.setColumns("5");
		renderFib.setEncoding("UTF_8");
		renderFib.setFibtype(RenderFibType.Fibtype.STRING);
		renderFib.setPrompt(RenderFibType.Prompt.BOX);
		renderFib.setRows("1");
		
		MaterialType material3 = flow.addNewMaterial();
		buildMattext(material3.addNewMattext());
		
		ResprocessingType resProcessing = item.addNewResprocessing();
		OutcomesType outcomes = resProcessing.addNewOutcomes();
		DecvarType decvar = outcomes.addNewDecvar();
		decvar.setDefaultval("0");
		decvar.setMaxvalue(question.getQuestionPoints());
		decvar.setMinvalue("0");
		decvar.setVarname("SCORE");
		decvar.setVartype(DecvarType.Vartype.INTEGER);
		
		RespconditionType respCondition = resProcessing.addNewRespcondition();
		respCondition.setContinue(RespconditionType.Continue.YES);
		ConditionvarType condition = respCondition.addNewConditionvar();
		OrType or = condition.addNewOr();
		VarequalType varequal = or.addNewVarequal();
		varequal.setCase(VarequalType.Case.NO);
		varequal.setStringValue(cdata(question.getCorrectAnswer()));
		SetvarType setvar = respCondition.addNewSetvar();
		setvar.setAction(SetvarType.Action.ADD);
		setvar.setVarname("SCORE");
		setvar.setStringValue("0");
		
		buildItemFeedback(item, "Correct",question.getFeedbackOK() );
		buildItemFeedback(item, "InCorrect", question.getFeedbackNOK());
	}
	
	private void processShortEssayQuestion(SectionType section, Question question) {
		ItemType item = section.addNewItem();
		item.setTitle("Essay Question");
		
		ItemmetadataType itemMetaData = item.addNewItemmetadata();
		QtimetadataType qtiMetaData = itemMetaData.addNewQtimetadata();
		
		buildMetaDataField(qtiMetaData, "qmd_itemtype", "Essay");
		buildMetaDataField(qtiMetaData, "TEXT_FORMAT", "HTML");
		
		ItemrubricType itemRubric = item.addNewItemrubric();
		itemRubric.setView(ItemrubricType.View.ALL);
		
		buildMattext(itemRubric.addNewMaterial().addNewMattext());

		PresentationType presentation = item.addNewPresentation();
		presentation.setLabel("Model Short Answer");
		FlowType flow = presentation.addNewFlow();
		flow.setClass1("Block");
		
		MaterialType material = flow.addNewMaterial();
		buildMattext(material.addNewMattext(), cdata(question.getQuestion()));
		
		MaterialType material2 = flow.addNewMaterial();
		buildMattext(material2.addNewMattext());
		
		addResponseLid(flow, question, false);
		
		ResprocessingType resProcessing = item.addNewResprocessing();
		OutcomesType outcomes = resProcessing.addNewOutcomes();
		DecvarType decvar = outcomes.addNewDecvar();
		decvar.setDefaultval("0");
		decvar.setMaxvalue(question.getQuestionPoints());
		decvar.setMinvalue("0");
		decvar.setVarname("SCORE");
		decvar.setVartype(DecvarType.Vartype.INTEGER);
		
		buildItemFeedback(item, "Correct",question.getFeedbackOK() );
		buildItemFeedback(item, "InCorrect", question.getFeedbackNOK());
	}
	
	private boolean isValidNumericResponse(String numericResponse){
		Matcher complexNumberMatcher = complexNumberPattern.matcher(numericResponse);
		boolean isComplexNumber = complexNumberMatcher.find();
		Matcher isIntervalMatcher = intervalPattern.matcher(numericResponse);
		boolean isInterval = isIntervalMatcher.find();
		Matcher isRealMatcher = realPattern.matcher(numericResponse);
		boolean isReal = isRealMatcher.find();
		return (isComplexNumber || isInterval || isReal);
	}
}
