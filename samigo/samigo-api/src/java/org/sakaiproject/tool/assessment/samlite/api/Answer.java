/**
 * Copyright (c) 2005-2014 The Apereo Foundation
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

public class Answer {
	private String id;
	private String text;
	private boolean isCorrect;
	
	public Answer(String id, String text, boolean isCorrect) {
		this.id = id;
		this.text = text;
		this.isCorrect = isCorrect;
	}
	
	public String getId() {
		return id;
	}
	public boolean isCorrect() {
		return isCorrect;
	}
	public String getText() {
		return text;
	}
	
	public void postProcessing(int questionType) {
		if (questionType == Question.EXTENDED_MATCHING_ITEMS_QUESTION) {
			text = text.substring(text.lastIndexOf("[")+1, text.lastIndexOf("]")).trim();
		}
	}
}
