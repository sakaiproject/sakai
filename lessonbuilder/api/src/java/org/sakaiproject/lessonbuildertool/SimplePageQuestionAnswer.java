package org.sakaiproject.lessonbuildertool;

public interface SimplePageQuestionAnswer {
	public void setId(long id);
	
	public long getId();
	
	public void setText(String text);
	
	public String getText();
	
	public void setCorrect(boolean correct);
	
	public boolean isCorrect();
}
