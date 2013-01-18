/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Author: Eric Jeney, jeney@rutgers.edu
 *
 * Copyright (c) 2010 Rutgers, the State University of New Jersey
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


package org.sakaiproject.lessonbuildertool;

import java.util.Date;

public class SimplePageQuestionResponseImpl implements SimplePageQuestionResponse {
	private long id;
	private Date timeAnswered;
	private String userId;
	private long questionId; // ID of the question in the items table
	private boolean correct;
	private boolean overridden; // Has the instructor set the points manually?
	private Double points;
	
	private String shortanswer;
	private long multipleChoiceId;
	
	// The text of the multiple choice question at the time it was answered
	private String originalText;

	public SimplePageQuestionResponseImpl() {}

	public SimplePageQuestionResponseImpl(String userId, long questionId) {
		timeAnswered = new Date();
		this.userId = userId;
		this.questionId = questionId;
		this.overridden = false;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Date getTimeAnswered() {
		return timeAnswered;
	}

	public void setTimeAnswered(Date timeAnswered) {
		this.timeAnswered = timeAnswered;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public long getQuestionId() {
		return questionId;
	}

	public void setQuestionId(long questionId) {
		this.questionId = questionId;
	}

	public boolean isCorrect() {
		return correct;
	}

	public void setCorrect(boolean c) {
		correct = c;
	}

	public String getShortanswer() {
		return shortanswer;
	}
	
	public void setShortanswer(String sa) {
		shortanswer = sa;
	}
	
	public long getMultipleChoiceId() {
		return multipleChoiceId;
	}
	
	public void setMultipleChoiceId(long id) {
		this.multipleChoiceId = id;
	}
	
	public Double getPoints() {
		return points;
	}
	
	public void setPoints(Double p) {
		if(p != null && p > 0) {
			points = p;
		}else {
			points = 0.0;
		}
	}
	
	public boolean isOverridden() {
		return overridden;
	}
	
	public void setOverridden(boolean overridden) {
		this.overridden = overridden;
	}
	
	public String getOriginalText() {
		return originalText;
	}
	
	public void setOriginalText(String originalText) {
		this.originalText = originalText;
	}
}
