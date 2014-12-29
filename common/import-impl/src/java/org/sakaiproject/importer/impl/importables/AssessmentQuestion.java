/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.importer.impl.importables;

import java.util.Map;
import java.util.Set;

public class AssessmentQuestion extends AbstractImportable {
	
	public static int MULTIPLE_CHOICE = 1;
	public static int MULTIPLE_ANSWER = 2;
	public static int SURVEY = 3;
	public static int TRUE_FALSE = 4;
	public static int ESSAY = 5;
	public static int FILE_UPLOAD = 6;
	public static int FILL_BLANK = 8;
	public static int MATCHING = 9;
	public static int ORDERING = 10;
	
	private int questionType;
	private Map answers;
	private Map choices;
	private String questionText;
	private Set correctAnswerIDs;
	private String feedbackWhenCorrect;
	private String feedbackWhenIncorrect;
	private Double pointValue;
	private Integer position;

	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}

	public String getTypeName() {
		return "sakai-question-multiplechoice";
	}

	public Map getAnswers() {
		return answers;
	}

	public void setAnswers(Map answers) {
		this.answers = answers;
	}

	public Set getCorrectAnswerIDs() {
		return correctAnswerIDs;
	}

	public void setCorrectAnswerIDs(Set correctAnswerIDs) {
		this.correctAnswerIDs = correctAnswerIDs;
	}

	public String getQuestionText() {
		return questionText;
	}

	public void setQuestionText(String questionText) {
		this.questionText = questionText;
	}

	public int getQuestionType() {
		return questionType;
	}

	public void setQuestionType(int questionType) {
		this.questionType = questionType;
	}

	public String getFeedbackWhenCorrect() {
		return feedbackWhenCorrect;
	}

	public void setFeedbackWhenCorrect(String feedbackWhenCorrect) {
		this.feedbackWhenCorrect = feedbackWhenCorrect;
	}

	public String getFeedbackWhenIncorrect() {
		return feedbackWhenIncorrect;
	}

	public void setFeedbackWhenIncorrect(String feedbackWhenIncorrect) {
		this.feedbackWhenIncorrect = feedbackWhenIncorrect;
	}

	public Map getChoices() {
		return choices;
	}

	public void setChoices(Map choices) {
		this.choices = choices;
	}

	public Double getPointValue() {
		return pointValue;
	}

	public void setPointValue(Double pointValue) {
		this.pointValue = pointValue;
	}

}
