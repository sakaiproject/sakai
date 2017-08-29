/**
 * Copyright (c) 2003-2011 The Apereo Foundation
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
package org.sakaiproject.tool.gradebook.business;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.sakaiproject.tool.gradebook.CommonGradeRecord;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.GradebookAssignment;

public interface GbSynchronizer 
{
  public boolean isProjectSite();
  
  public void deleteLegacyAssignment(String assignmentName);
  
  public Map convertEidUid(Collection gradeRecords);
  
  public Map getLegacyAssignmentWithStats(String assignmentName);
  
  public AssignmentGradeRecord convertIquizRecordToUid(AssignmentGradeRecord iquizRecord, Map persistentRecordMap, boolean isUpdateAll, String graderId);
  
  public CommonGradeRecord getNeededUpdateIquizRecord(GradebookAssignment assignment, AssignmentGradeRecord record);

  public void updateLegacyGradeRecords(String assignmentName, List legacyUpdates);

  public Map reconcileAllAssignments(List assignments);
  
  public void addLegacyAssignment(String name);
  
  public Map getPersistentRecords(final Long gradableObjId);
  
  public Map getPersistentRecordsForStudent(final String studentId);

  public void synchrornizeAssignments(List assignments);
  
  public void updateAssignment(String title, String newTitle, int grade_type);
}
