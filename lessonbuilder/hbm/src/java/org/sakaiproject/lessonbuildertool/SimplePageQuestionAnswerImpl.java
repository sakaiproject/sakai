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

public class SimplePageQuestionAnswerImpl implements SimplePageQuestionAnswer {
	private long id;
	private long questionId;
	private String answerText;
	private boolean correct;
	
	public SimplePageQuestionAnswerImpl() {}
	
	public SimplePageQuestionAnswerImpl(long id, String answerText, boolean correct) {
		this.id = id;
		this.answerText = answerText;
		this.correct = correct;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public long getId() {
		return id;
	}
	
	public void setQuestionId(long questionId) {
		this.questionId = questionId;
	}
	
	public long getQuestionId() {
		return questionId;
	}
	
	public void setText(String answerText) {
		this.answerText = answerText;
	}
	
	public String getText() {
		return answerText;
	}
	
	public void setCorrect(boolean correct) {
		this.correct = correct;
	}
	
	public boolean isCorrect() {
		return correct;
	}
}
