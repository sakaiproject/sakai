package org.sakaiproject.api.app.postem.data;

public interface Template {
	public String getTemplateCode();

	public void setTemplateCode(String templateCode);

	public String fillGrades(StudentGrades student);
}
