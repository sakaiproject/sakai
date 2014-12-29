package org.sakaiproject.lessonbuildertool;

import java.util.Date;

public interface SimplePagePeerEval {
	
	public long getPeerEvalId();
	public void setPeerEvalId(long id);
	
	
	public String getRowText();
	public void setRowText(String text);
	
	public int getRowSequence();
	public void setRowSequence(int sequence);


}
