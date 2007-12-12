package org.sakaiproject.service.gradebook.shared;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface GradebookPermissionService
{
  /**
   * Get all available categories for a user that the user can either view or grade.
   * (For overview page)
   * 
   * @param gradebookId Gradebook ID
   * @param userId grader ID
   * @param categoryList List of Category. (should be all categories for this gradebook)
   * @param cateType gradebook category type
   * @throws IllegalArgumentException
   * @return List of categories
   */
	public List getCategoriesForUser(Long gradebookId, String userId, List categoryList, int cateType) throws IllegalArgumentException;
	
	/**
	 * Returns viewable categories for a user for a specific student
	 * @param gradebookId
	 * @param userId
	 * @param studentId
	 * @param categories
	 * @param cateType
	 * @param sectionIds
	 * @return
	 * @throws IllegalArgumentException
	 */
	public List getCategoriesForUserForStudentView(Long gradebookId, String userId, String studentId, List categories, int cateType, List sectionIds) throws IllegalArgumentException;
	
  /**
   * Get true/false value for current user which indicats if he has permission for all
   * assignments in a gradebook with category turned off or he has permission for
   * assignments without category associated with in a gradebook with category
   * turned on.
   * (For overview page)
   * 
   * @param gradebookId Gradebook ID
   * @param userId grader ID
   * @throws IllegalArgumentException
   * @return boolean of true/false
   */
	public boolean getPermissionForUserForAllAssignment(Long gradebookId, String userId) throws IllegalArgumentException;

	/**
	 * Get true/false value for current user which indicates if he has permission for
	 * all gb items for a given student
	 * @param gradebookId
	 * @param userId
	 * @param studentId
	 * @param sectionIds
	 * @return
	 * @throws IllegalArgumentException
	 */
	public boolean getPermissionForUserForAllAssignmentForStudent(Long gradebookId, String userId, String studentId, List sectionIds) throws IllegalArgumentException;
	
	/**
   * Get students IDs that the current grader can either view or grade.
   * When categoryId is null and cateType is with category - return students' map that the grader
   * can grade/view any category for course sections. (this is mostly for items that have no category 
   * associated with them in a gradebook with category turned on)
   * (For item detail page)
   * 
   * @param gradebookId Gradebook ID
   * @param userId grader ID
   * @param studentIds List of student IDs
   * @param cateType gradebook category type
   * @param categoryId current category ID that the permission check is based on. it can be null.
   * @param courseSections List of course sections for current site
   * @throws IllegalArgumentException
   * @return Map of student IDs with grade/view as function value
   */
	public Map getStudentsForItem(Long gradebookId, String userId, List studentIds, int cateType, Long categoryId, List courseSections) throws IllegalArgumentException;
	
/**
   * Get students IDs that the current grader can either view or grade.
   * When categoryId is null and cateType is with category - return students' map that the grader
   * can grade/view any category for course sections. (this is mostly for items that have no category 
   * associated with them in a gradebook with category turned on)
   * (For item detail page)
   * 
   * @param gradebookUid gradebook uid
   * @param userId grader ID
   * @param studentIds List of student IDs
   * @param cateType gradebook category type
   * @param categoryId current category ID that the permission check is based on. it can be null.
   * @param courseSections List of course sections for current site
   * @throws IllegalArgumentException
   * @return Map of student IDs with grade/view as function value
   */
	public Map getStudentsForItem(String gradebookUid, String userId, List studentIds, int cateType, Long categoryId, List courseSections) throws IllegalArgumentException;
	

  /**
   * Get a map of itemId/permission(grade/view) of a student for a grader that he can grade
   * or view for gradebook.
   * (For a student's roster page) 
   * 
   * @param gradebookUid Gradebook UID
   * @param userId grader ID
   * @param studentId student ID
   * @param courseSections List of course sections for current site
   * @throws IllegalArgumentException
   * @return Map of item IDs with grade/view as function value
   */
	 public Map getAvailableItemsForStudent(String gradebookUid, String userId, String studentId, Collection courseSections) throws IllegalArgumentException;
	 
	 /**
	   * Get a map of itemId/permission(grade/view) of a student for a grader that he can grade
	   * or view for gradebook.
	   * (For a student's roster page) 
	   * 
	   * @param gradebookId Gradebook ID
	   * @param userId grader ID
	   * @param studentId student ID
	   * @param courseSections List of course sections for current site
	   * @throws IllegalArgumentException
	   * @return Map of item IDs with grade/view as function value
	   */
		 public Map getAvailableItemsForStudent(Long gradebookId, String userId, String studentId, Collection courseSections) throws IllegalArgumentException;
	 
	  /**
	   * Get a map of map for students whose IDs are in studentIds with id as key and another map
	   * as value: itemId/permission(grade/view) for a grader that he can grade
	   * or view for gradebook.
	   * (For a student's roster page) 
	   * 
	   * @param gradebookId Gradebook ID
	   * @param userId grader ID
	   * @param studentIds List of student IDs
	   * @param courseSections List of course sections for current site
	   * @throws IllegalArgumentException
	   * @return Map 
	   */
	 public Map getAvailableItemsForStudents(Long gradebookId, String userId, List studentIds, Collection courseSections) throws IllegalArgumentException;

	 /**
	   * Get a map of map for students whose IDs are in studentIds with id as key and another map
	   * as value: itemId/permission(grade/view) for a grader that he can grade
	   * or view for gradebook.
	   * (For a student's roster page) 
	   * 
	   * @param gradebookUid Gradebook UID
	   * @param userId grader ID
	   * @param studentIds List of student IDs
	   * @param courseSections List of course sections for current site
	   * @throws IllegalArgumentException
	   * @return Map 
	   */
	 public Map getAvailableItemsForStudents(String gradebookUid, String userId, List studentIds, Collection courseSections) throws IllegalArgumentException;

	 
	 /**
	  * Get a map with student IDs as key and view/grade as value for their course grade.
	  * (For course grade page) 
	  * 
	  * @param gradebookId Gradebook ID
	  * @param userId grader ID
	  * @param studentIds List of student IDs
	  * @param courseSections List of course sections for current site (Should be all course sections the current site has.)
	  * @throws IllegalArgumentException
	  * @return Map of student IDs with view/grade as function value 
	  */
	 public Map getCourseGradePermission(Long gradebookId, String userId, List studentIds, List courseSections) throws IllegalArgumentException;
	 
	 /**
	  * Get a map with student IDs as key and view/grade as value for their course grade.
	  * (For course grade page) 
	  * 
	  * @param gradebookUid Gradebook Uid
	  * @param userId grader ID
	  * @param studentIds List of student IDs
	  * @param courseSections List of course sections for current site (Should be all course sections the current site has.)
	  * @throws IllegalArgumentException
	  * @return Map of student IDs with view/grade as function value 
	  */
	 public Map getCourseGradePermission(String gradebookUid, String userId, List studentIds, List courseSections) throws IllegalArgumentException;

	 
	 /**
	  * Get all Permission records associated with this user and gradebook
	  * @param gradebookId
	  * @param userId
	  * @return List of Permission records for user and gradebook
	  */
	 public List getGraderPermissionsForUser(Long gradebookId, String userId); 
	 
	 /**
	  * Get all Permission records associated with this user and gradebook
	  * @param gradebookUid
	  * @param userId
	  * @return List of Permission records for user and gradebook
	  */
	 public List getGraderPermissionsForUser(String gradebookUid, String userId);

	 /**
	  * Get all group ids associated with groups that contain at least one member
	  * that the user is authorized to view/grade
	  * @param gradebookId
	  * @param userId
	  * @param groupIds
	  * @return List of group ids that user has some authorization to view
	  */
	 public List getViewableGroupsForUser(Long gradebookId, String userId, List groupIds);
	 
	 /**
	  * Get all group ids associated with groups that contain at least one member
	  * that the user is authorized to view/grade
	  * @param gradebookUid
	  * @param userId
	  * @param groupIds
	  * @return List of group ids that user has some authorization to view
	  */
	 public List getViewableGroupsForUser(String gradebookUid, String userId, List groupIds);
	 
	 /**
	  * Get a unique list of students that the current user has grader permissions to view in some capacity
	  * @param gradebookId
	  * @param userId
	  * @param studentIds
	  * @param sections
	  * @return List of student ids
	  */
	 public List getViewableStudentsForUser(Long gradebookId, String userId, List studentIds, List sections);
	 
	 /**
	  * Get a unique list of students that the current user has grader permissions to view in some capacity
	  * @param gradebookUid
	  * @param userId
	  * @param studentIds
	  * @param sections
	  * @return List of student ids
	  */
	 public List getViewableStudentsForUser(String gradebookUid, String userId, List studentIds, List sections);
	 
}