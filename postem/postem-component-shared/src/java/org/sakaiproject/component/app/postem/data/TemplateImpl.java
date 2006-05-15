package org.sakaiproject.component.app.postem.data;

import java.io.Serializable;
import java.util.ListIterator;

import org.sakaiproject.api.app.postem.data.StudentGrades;
import org.sakaiproject.api.app.postem.data.Template;

public class TemplateImpl implements Template, Serializable {
	protected String templateCode;

	public String getTemplateCode() {
		return templateCode;
	}

	public void setTemplateCode(String templateCode) {
		this.templateCode = templateCode;
	}

	public String fillGrades(StudentGrades student) {
		String output = new String(templateCode);
		output = output.replaceAll("\\$0", student.getUsername());
		ListIterator grades = student.getGrades().listIterator();
		while (grades.hasNext()) {
			int index = grades.nextIndex();
			String grade = (String) grades.next();
			output = output.replaceAll("\\$" + (index + 1), grade);
		}
		return output;
	}
}
