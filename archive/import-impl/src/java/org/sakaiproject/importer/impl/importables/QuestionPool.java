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
