package org.sakaiproject.api.app.postem.data;

import java.sql.Timestamp;
import java.util.List;

public interface StudentGrades {
	public Gradebook getGradebook();

	public void setGradebook(Gradebook gradebook);

	public String getUsername();

	public void setUsername(String username);

	public List getGrades();

	public void setGrades(List grades);
	
	public String getCheckDateTime();

	public Timestamp getLastChecked();

	public void setLastChecked(Timestamp lastChecked);

	public Long getId();

	public void setId(Long id);

	public boolean getReadAfterUpdate();

	public String formatGrades();

	public String getGradesRow();

}
