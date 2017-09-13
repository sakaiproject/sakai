/**
 * Copyright (c) 2005-2015 The Apereo Foundation
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
package org.sakaiproject.tool.assessment.samlite.api;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;

public class Question {
	public static final int UNDEFINED_QUESTION = 0;
	public static final int MULTIPLE_CHOICE_QUESTION = 10;
	public static final int MULTIPLE_CHOICE_MULTIPLE_ANSWER_QUESTION = 15;
	public static final int FILL_IN_THE_BLANK_QUESTION = 20;
	public static final int TRUE_FALSE_QUESTION = 30;
	public static final int SHORT_ESSAY_QUESTION = 40;
    public static final int EXTENDED_MATCHING_ITEMS_QUESTION = 50;
	public static final int FILL_IN_NUMERIC_QUESTION = 60;
	
    // for EMI question
    private String themeText;
    private String leadInText;
    private ArrayList emiAnswerOptions;  // store List of possible options for an EMI question's anwers
    private ArrayList emiQuestionAnswerCombinations;  // store List of possible options for an EMI question's anwers
	
	private int questionNumber;
	private String questionPoints;
	private String questionDiscount;
	private List questionLines;
	private int questionType;
	private String correctAnswer;
	private List answers;
	private boolean hasPoints;
	private boolean hasDiscount;
	private String questionTypeAsString;
	private String feedbackOK;
	private String feedbackNOK;
	private boolean randomize;
	private boolean rationale;

	public Question() {
		this.questionNumber = 0;
		this.questionPoints = "";
		this.questionDiscount = "";
		this.questionLines = new LinkedList();
		this.questionType = UNDEFINED_QUESTION;
		this.correctAnswer = "";
		this.answers = new LinkedList();
		this.hasPoints = false;
		this.hasDiscount = false;
		this.questionTypeAsString = "";
		this.randomize = false;
	}

	public void addAnswer(String id, String text, boolean isCorrect) {
		this.answers.add(new Answer(id, text, isCorrect));
	}

	public List getAnswers() {
		return answers;
	}


	public void setAnswers(List answers) {
		this.answers = answers;
	}


	public String getCorrectAnswer() {
		return correctAnswer;
	}


	public void setCorrectAnswer(String correctAnswer) {
		this.correctAnswer = correctAnswer;
	}


	public String getQuestion() {
		StringBuilder buffer = new StringBuilder();
		
		for (Iterator it = questionLines.iterator();it.hasNext();) {
			String line = (String)it.next();
			if (null != line && !"".equals(line)) 	
				buffer.append(line.trim()).append(" ");
		}
		
		return buffer.toString();
	}

	public void append(String questionSegment) {
		this.questionLines.add(questionSegment);
	}
	
	public int getQuestionNumber() {
		return questionNumber;
	}


	public void setQuestionNumber(int questionNumber) {
		this.questionNumber = questionNumber;
	}


	public String getQuestionPoints() {
		return questionPoints;
	}


	public void setQuestionPoints(String questionPoints) {
		if (null != questionPoints && !"".equals(questionPoints))
			this.hasPoints = true;
		this.questionPoints = questionPoints;
	}
	
	public String getQuestionDiscount() {
		return questionDiscount;
	}
	
	public void setQuestionDiscount(String questionDiscount) {
		if (null != questionDiscount && !"".equals(questionDiscount))
			this.hasDiscount = true;
		this.questionDiscount = questionDiscount;
	}
	
	public boolean hasPoints() {
		return hasPoints;
	}
	
	public boolean hasDiscount() {
		return hasDiscount;
	}
	
	public int getQuestionType() {
		return questionType;
	}

	public void setQuestionType(int questionType) {
		this.questionType = questionType;
	}

	public boolean isRationale() {
		return rationale;
	}

	public void setRationale(boolean rationale) {
		this.rationale = rationale;
	}
	
	public void postProcessing() {
		if (getQuestionType() == EXTENDED_MATCHING_ITEMS_QUESTION) {
			int themeLineIndex = 1;
			int optionLine = 2;
			questionLines.set(themeLineIndex, questionLines.get(themeLineIndex).toString() + "<br /><br />");
			Iterator answerLines = answers.iterator();
			String textToAdd = "Options: ";
			questionLines.add(optionLine++, textToAdd + "<br />");
			while (answerLines.hasNext()) {
				Answer answer = (Answer) answerLines.next();
				textToAdd = answer.getId() + ". " + answer.getText();
				if (!answer.isCorrect()) {
					// add at next options position
					questionLines.add(optionLine++, textToAdd + "<br />");
				}
				else {
					// add at end
					//textToAdd = textToAdd.substring(0, textToAdd.indexOf("[")).trim() + "<br />";
					questionLines.add(textToAdd);
				}
			}
			answerLines = answers.iterator();
			while (answerLines.hasNext()) {
				Answer answer = (Answer)answerLines.next();
				if (!answer.isCorrect()) {
					answerLines.remove();
				}
				else {
					answer.postProcessing(questionType);
				}
			}
		}
	}

	public String getQuestionTypeAsString() {
		return questionTypeAsString;
	}

	public void setQuestionTypeAsString(String questionTypeAsString) {
		this.questionTypeAsString = questionTypeAsString;
	}

	public boolean isRandomize() {
		return randomize;
	}

	public void setRandomize(boolean randomize) {
		this.randomize = randomize;
	}
	
	public String getFeedbackOK() {
		return feedbackOK;
	}

	public void setFeedbackOK(String feedbackOK) {
		this.feedbackOK = feedbackOK;
	}

	public String getFeedbackNOK() {
		return feedbackNOK;
	}

	public void setFeedbackNOK(String feedbackNOK) {
		this.feedbackNOK = feedbackNOK;
	}

//************ Theme and Lead-In Text ******************

  public String getLeadInText() {
	if (leadInText == null) {
		setThemeAndLeadInText();
	}
	return leadInText;
  }

  public String getThemeText() {
	if (themeText == null) {
		setThemeAndLeadInText();
	}
	return themeText;
  }

  public void setThemeAndLeadInText() {
	themeText = (String)questionLines.get(1);
	leadInText = (String)questionLines.get(2);
  }
	  
	  	
  //************ EMI Answer Options and Q-A combinations******************
  
  public ArrayList getEmiAnswerOptions() {
  	if (emiAnswerOptions==null) {
  		setEmiOptionsAndQACombinations();
    }
  	return emiAnswerOptions;
  }
  
  public ArrayList getEmiQuestionAnswerCombinations() {
  	if (emiQuestionAnswerCombinations==null) {
  		setEmiOptionsAndQACombinations();
    }
  	return emiQuestionAnswerCombinations;
  }
 
  private void setEmiOptionsAndQACombinations() {
	emiAnswerOptions = new ArrayList();
	emiQuestionAnswerCombinations = new ArrayList();
	Iterator iter = answers.iterator();
	while (iter.hasNext()) {
		Answer answer = (Answer)iter.next();
		if (answer.getId().matches("[0-9]+")) {
			emiQuestionAnswerCombinations.add(answer);
		}
		else {
			emiAnswerOptions.add(answer);
		}
	}
  }
}
