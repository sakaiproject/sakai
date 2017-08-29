/**
 * Copyright (c) 2005-2007 The Apereo Foundation
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

import java.util.LinkedList;
import java.util.List;

public class QuestionGroup {
	private String name;
	private String description;
	private List questions;
	
	public QuestionGroup(String name, String description) {
		super();
		this.name = name;
		this.description = description;
		this.questions = new LinkedList();
	}

	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void addQuestion(Question question) {
		this.questions.add(question);
	}
	
	public List getQuestions() {
		return questions;
	}

}
