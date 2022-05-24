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

import lombok.Data;

@Data
public class SimplePageQuestionAnswerImpl implements SimplePageQuestionAnswer {
	private long id;
	private long questionId;
	private String text;
	private boolean correct;
	private String prompt;
	private String response;

	public SimplePageQuestionAnswerImpl(long id, String answerText, boolean correct) {
		this.id = id;
		this.text = answerText;
		this.correct = correct;
	}
	public SimplePageQuestionAnswerImpl(long id, String prompt, String response) {
		this.id = id;
		this.prompt = prompt;
		this.response = response;
	}
}
