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
