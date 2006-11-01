/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/access/trunk/access-impl/impl/src/java/org/sakaiproject/access/tool/AccessServlet.java $
 * $Id: AccessServlet.java 17063 2006-10-11 19:48:42Z jimeng@umich.edu $
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

package org.sakaiproject.importer.impl.importables;

import java.util.List;

public class QuestionPool extends AbstractImportable {
	
	private String title;
	private String description;
	private List essayQuestions;
	private List multiChoiceQuestions;
	private List fillBlankQuestions;
	private List matchQuestions;
	private List multiAnswerQuestions;
	private List trueFalseQuestions;
	private List orderingQuestions;
	

	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}


	public List getEssayQuestions() {
		return essayQuestions;
	}


	public void setEssayQuestions(List essayQuestions) {
		this.essayQuestions = essayQuestions;
	}


	public List getFillBlankQuestions() {
		return fillBlankQuestions;
	}


	public void setFillBlankQuestions(List fillBlankQuestions) {
		this.fillBlankQuestions = fillBlankQuestions;
	}


	public List getMatchQuestions() {
		return matchQuestions;
	}


	public void setMatchQuestions(List matchQuestions) {
		this.matchQuestions = matchQuestions;
	}


	public List getMultiAnswerQuestions() {
		return multiAnswerQuestions;
	}


	public void setMultiAnswerQuestions(List multiAnswerQuestions) {
		this.multiAnswerQuestions = multiAnswerQuestions;
	}


	public List getMultiChoiceQuestions() {
		return multiChoiceQuestions;
	}


	public void setMultiChoiceQuestions(List multiChoiceQuestions) {
		this.multiChoiceQuestions = multiChoiceQuestions;
	}


	public List getOrderingQuestions() {
		return orderingQuestions;
	}


	public void setOrderingQuestions(List orderingQuestions) {
		this.orderingQuestions = orderingQuestions;
	}


	public String getTitle() {
		return title;
	}


	public void setTitle(String title) {
		this.title = title;
	}


	public List getTrueFalseQuestions() {
		return trueFalseQuestions;
	}


	public void setTrueFalseQuestions(List trueFalseQuestions) {
		this.trueFalseQuestions = trueFalseQuestions;
	}


	public String getTypeName() {
		return "sakai-question-pool";
	}

}
