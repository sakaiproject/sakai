package org.sakaiproject.acadtermmanage.logic;

import java.util.List;

import org.sakaiproject.acadtermmanage.exceptions.DuplicateKeyException;
import org.sakaiproject.acadtermmanage.exceptions.NoSuchKeyException;
import org.sakaiproject.acadtermmanage.model.Semester;


public interface AcademicSessionLogic {

	// Also update the tool-xml and the Readme.md if you change this
	public final static String FUNCTION_IS_AS_MANAGER = "sakai.acadtermmanage.is_manager";

	
	
	public Semester getSemester(String eid);	
	
	public List<Semester> getSemesters();
		
	public boolean addSemester(Semester s) throws DuplicateKeyException;	
	
	public void removeSemester(String eid);
	
	public void updateSemester(String oldEID, Semester newValues) throws NoSuchKeyException;
	
	/**
	 * Checks if the current user is allowed to use the tool  
	 * 
	 * @return true if the current user has been assigned the required permission
	 */
	public boolean isAcademicSessionManager();
	
}
