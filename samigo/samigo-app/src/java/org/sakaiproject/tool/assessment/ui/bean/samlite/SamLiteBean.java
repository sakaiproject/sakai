/**
 * Copyright (c) 2005-2017 The Apereo Foundation
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
package org.sakaiproject.tool.assessment.ui.bean.samlite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacadeQueries;
import org.sakaiproject.tool.assessment.facade.AssessmentTemplateFacade;
import org.sakaiproject.tool.assessment.samlite.api.Question;
import org.sakaiproject.tool.assessment.samlite.api.QuestionGroup;
import org.sakaiproject.tool.assessment.samlite.api.SamLiteService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.TextFormat;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;
import org.w3c.dom.Document;

@Slf4j
public class SamLiteBean implements Serializable {
	private static final long serialVersionUID = -3122436861866172596L;
	public static final String DEFAULT_CHARSET = "ascii-us";

	private String name;
	private String description;
	private String data;
	private String assessmentTemplateId;
	private String outcome;
	
	private boolean isVisible = true;
	
	private AuthorBean authorBean;

	private ResourceLoader rb = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.SamLite");
	
	public void setAuthorBean(AuthorBean authorBean) {
		this.authorBean = authorBean;
	}
	
	private QuestionGroup questionGroup;
	private SamLiteService samLiteService;

	public void setSamLiteService(SamLiteService samLiteService) {
		this.samLiteService = samLiteService;
	}
	
	public SamLiteBean() {
		String samliteProperty = ServerConfigurationService.getString("samigo.samliteEnabled");
		if (null != samliteProperty && "false".equalsIgnoreCase(samliteProperty)) 
			isVisible = false;
	}
	
	public void parse() {
		questionGroup = samLiteService.parse(TextFormat.convertPlaintextToFormattedTextNoHighUnicode(name), 
				TextFormat.convertPlaintextToFormattedTextNoHighUnicode(description), 
				TextFormat.convertPlaintextToFormattedTextNoHighUnicode(data));
	}
	
	public Document createDocument() {
		return samLiteService.createDocument(questionGroup);
	}
	
	public void createAssessment(AssessmentFacade assessment) {		
	    authorBean.setAssessTitle("");
	    authorBean.setAssessmentDescription("");
	    authorBean.setAssessmentTypeId("");
	    authorBean.setAssessmentTemplateId(AssessmentTemplateFacade.DEFAULTTEMPLATE.toString());

	    AssessmentService assessmentService = new AssessmentService();
	    List list = assessmentService.getBasicInfoOfAllActiveAssessments(
	    		authorBean.getCoreAssessmentOrderBy(), authorBean.isCoreAscending());
	    Iterator iter = list.iterator();
		while (iter.hasNext()) {
			AssessmentFacade assessmentFacade= (AssessmentFacade) iter.next();
			assessmentFacade.setTitle(FormattedText.convertFormattedTextToPlaintext(assessmentFacade.getTitle()));
		}
	    authorBean.setAssessments(list);
	}
	
	public List getQuestions() {
		List<Question> list = questionGroup.getQuestions();
		for (Question question: list) {
			question.setQuestionTypeAsString(getQuestionTypeAsString(question));
		}
		
		return list;

	}
	
	public QuestionGroup getQuestionGroup() {
		return questionGroup;
	}
	
	public void setQuestionGroup(QuestionGroup questionGroup) {
		this.questionGroup = questionGroup;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setAssessmentTemplateId(String assessmentTemplateId) {
	    this.assessmentTemplateId = assessmentTemplateId;
	}

	public String getAssessmentTemplateId() {
	    return assessmentTemplateId;
	}
	
	public boolean isVisible() {
		return isVisible;
	}
	
	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}

	private String getQuestionTypeAsString(Question question) {
		switch (question.getQuestionType()) {
		case Question.MULTIPLE_CHOICE_QUESTION:
			return rb.getString("sam_lite_multiple_choice");
		case Question.MULTIPLE_CHOICE_MULTIPLE_ANSWER_QUESTION:
			return rb.getString("sam_lite_multiple_choice_multiple");
		case Question.FILL_IN_THE_BLANK_QUESTION:
			return rb.getString("sam_lite_fill_in_the_blank");
		case Question.TRUE_FALSE_QUESTION:
			return rb.getString("sam_lite_true_false");
		case Question.SHORT_ESSAY_QUESTION:
			return rb.getString("sam_lite_short_essay");
		case Question.FILL_IN_NUMERIC_QUESTION:
			return rb.getString("sam_lite_fill_in_numeric");
		};
		return "Unrecognized Type";
	}

	public void setOutcome(String outcome) {
		this.outcome = outcome;
	}

	public String getOutcome() {
		return outcome;
	}
}
