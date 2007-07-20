package org.sakaiproject.service.gradebook.shared;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface GradebookPermissionService
{
  /**
   * Get all available categories for a user that the user can either view or grade.
   * 
   * @param gradebookId Gradebook ID
   * @param userId grader ID
   * @param categoryList List of Category. (should be all categories for this gradebook)
   * @param cateType gradebook category type
   * @return List of categories
   */
	public List getCategoriesForUser(Long gradebookId, String userId, List categoryList, int cateType);
	
  /**
   * Get students IDs that the current grader can either view or grade.
   * When categoryId is null and cateType is with category - return students' map that the grader
   * can grade/view any category for groups. (this is mostly for items that have no category 
   * associated with them in a gradebook with category turned on)
   * 
   * @param gradebookId Gradebook ID
   * @param userId grader ID
   * @param studentIds List of student IDs
   * @param cateType gradebook category type
   * @param categoryId current category ID that the permission check is based on. it can be null.
   * @param groups List of groups for current site
   * @return Map of student IDs with grade/view as function value
   */
	public Map getStudentsForItem(Long gradebookId, String userId, List studentIds, int cateType, Long categoryId, List groups);
}