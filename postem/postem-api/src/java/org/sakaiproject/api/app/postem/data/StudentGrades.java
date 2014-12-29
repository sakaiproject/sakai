/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

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
