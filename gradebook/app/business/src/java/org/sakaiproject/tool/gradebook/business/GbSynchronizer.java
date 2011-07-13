package org.sakaiproject.tool.gradebook.business;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.sakaiproject.tool.gradebook.CommonGradeRecord;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.Assignment;

public interface GbSynchronizer 
{
  public boolean isProjectSite();
  
  public void deleteLegacyAssignment(String assignmentName);
  
  public Map convertEidUid(Collection gradeRecords);
  
  public Map getLegacyAssignmentWithStats(String assignmentName);
  
  public AssignmentGradeRecord convertIquizRecordToUid(AssignmentGradeRecord iquizRecord, Map persistentRecordMap, boolean isUpdateAll, String graderId);
  
  public CommonGradeRecord getNeededUpdateIquizRecord(Assignment assignment, AssignmentGradeRecord record);

  public void updateLegacyGradeRecords(String assignmentName, List legacyUpdates);

  public Map reconcileAllAssignments(List assignments);
  
  public void addLegacyAssignment(String name);
  
  public Map getPersistentRecords(final Long gradableObjId);
  
  public Map getPersistentRecordsForStudent(final String studentId);

  public void synchrornizeAssignments(List assignments);
  
  public void updateAssignment(String title, String newTitle, int grade_type);
}
