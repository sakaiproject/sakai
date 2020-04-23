/**
 * Copyright (c) 2003-2019 The Apereo Foundation
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
