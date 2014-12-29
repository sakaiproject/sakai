package org.sakaiproject.tool.assessment.samlite.api;

import org.w3c.dom.Document;

public interface SamLiteService {

	public QuestionGroup parse(String name, String description, String data);
	
	public Document createDocument(QuestionGroup questionGroup);

}
