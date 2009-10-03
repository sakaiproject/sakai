package org.sakaiproject.tool.gradebook;

import java.util.Date;

/**
 * <p>
 * <code>CommonGradebookRecordImpl</code> uniquely identifies student grade records
 * on both CL and Legacy data sources
 * 
 * @author  Jarrod Lannan
 * @version $Revision:$
 */

public interface CommonGradeRecord
{
	
	/**
	 * @return Returns the assignmentName.
	 */
	public String getAssignmentName();		

	/**
	 * @param assignmentName The assignmentName to set.
	 */
	public void setAssignmentName(String assignmentName);

	/**
	 * @return Returns the dateGraded.
	 */
	public Date getDateGraded();

	/**
	 * @param dateGraded The dateGraded to set.
	 */
	public void setDateGraded(Date dateGraded);

	/**
	 * @return Returns the graderComments.
	 */
	public String getGraderComments();

	/**
	 * @param graderComments The graderComments to set.
	 */
	public void setGraderComments(String graderComments);
		
	/**
	 * @return Returns the graderId.
	 */
	public String getGraderId();
		
	/**
	 * @param graderId The graderId to set.
	 */
	public void setGraderId(String graderId);
		
	/**
	 * @return Returns the pointsEarned.
	 */
	public Double getPointsEarned();		

	/**
	 * @param pointsEarned The pointsEarned to set.
	 */
	public void setPointsEarned(Double pointsEarned);
		
	/**
	 * @return Returns the studentUserId.
	 */
	public String getStudentUserId();
		
	/**
	 * @param studentUserId The studentUserId to set.
	 */
	public void setStudentUserId(String studentUserId);
		
}
/**********************************************************************************
 *
 * $Id:$
 *
 **********************************************************************************/
