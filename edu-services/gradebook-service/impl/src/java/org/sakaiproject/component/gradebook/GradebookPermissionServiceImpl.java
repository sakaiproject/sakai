package org.sakaiproject.component.gradebook;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.sakaiproject.section.api.SectionAwareness;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.service.gradebook.shared.GradebookPermissionService;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.Permission;
import org.springframework.orm.hibernate3.HibernateCallback;

public class GradebookPermissionServiceImpl extends BaseHibernateManager implements GradebookPermissionService
{
	private SectionAwareness sectionAwareness;
	
	public List<Long> getCategoriesForUser(Long gradebookId, String userId, List<Long> categoryIdList, int cateType) throws IllegalArgumentException
	{
		if(gradebookId == null || userId == null)
			throw new IllegalArgumentException("Null parameter(s) in GradebookPermissionServiceImpl.getCategoriesForUser");
		if(cateType != GradebookService.CATEGORY_TYPE_ONLY_CATEGORY && cateType != GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY)
			throw new IllegalArgumentException("CategoryType must be CATEGORY_TYPE_ONLY_CATEGORY or CATEGORY_TYPE_WEIGHTED_CATEGORY in GradebookPermissionServiceImpl.getCategoriesForUser");

		List anyCategoryPermission = getPermissionsForUserAnyCategory(gradebookId, userId);
		if(anyCategoryPermission != null && anyCategoryPermission.size() > 0 )
		{
			return categoryIdList;
		}
		else
		{
//			List ids = new ArrayList();
//			for(Iterator iter = categoryList.iterator(); iter.hasNext(); )
//			{
//				Category cate = (Category) iter.next();
//				if(cate != null)
//					ids.add(cate.getId());
//			}

			List<Long> returnCatIds = new ArrayList<Long>();
			List<Permission> permList = getPermissionsForUserForCategory(gradebookId, userId, categoryIdList);
			for(Iterator<Permission> iter = permList.iterator(); iter.hasNext();)
			{
				Permission perm = (Permission) iter.next();
				if(perm != null && !returnCatIds.contains(perm.getCategoryId()))
				{
					returnCatIds.add(perm.getCategoryId());
				}
			}
			
			return returnCatIds;
			

//			List filteredCates = new ArrayList();
//			for(Iterator cateIter = categoryList.iterator(); cateIter.hasNext();)
//			{
//				Category cate = (Category) cateIter.next();
//				if(cate != null)
//				{
//					for(Iterator iter = permList.iterator(); iter.hasNext();)
//					{
//						Permission perm = (Permission) iter.next();
//						if(perm != null && perm.getCategoryId().equals(cate.getId()))
//						{
//							filteredCates.add(cate);
//							break;
//						}
//					}
//				}
//			}
//			return filteredCates;
		}
	}
	
	public List<Long> getCategoriesForUserForStudentView(Long gradebookId, String userId, String studentId, List<Long> categoriesIds, int cateType, List<String> sectionIds) throws IllegalArgumentException
	{
		if(gradebookId == null || userId == null || studentId == null)
			throw new IllegalArgumentException("Null parameter(s) in GradebookPermissionServiceImpl.getCategoriesForUser");
		if(cateType != GradebookService.CATEGORY_TYPE_ONLY_CATEGORY && cateType != GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY)
			throw new IllegalArgumentException("CategoryType must be CATEGORY_TYPE_ONLY_CATEGORY or CATEGORY_TYPE_WEIGHTED_CATEGORY in GradebookPermissionServiceImpl.getCategoriesForUser");
		
		List<Long> returnCategoryList = new ArrayList<Long>();
		//Map categoryMap = new HashMap();  // to keep the elements unique
		if (categoriesIds == null || categoriesIds.isEmpty())
			return returnCategoryList;
		
		List graderPermissions = getPermissionsForUser(gradebookId, userId);
		if(graderPermissions == null || graderPermissions.isEmpty())
		{
			return returnCategoryList;
		}
		
//		Map categoryIdCategoryMap = new HashMap();
//		for (Iterator catIter = categories.iterator(); catIter.hasNext();) {
//			Category cat = (Category) catIter.next();
//			if (cat != null) {
//				categoryIdCategoryMap.put(cat.getId(), cat);
//			}
//		}
		
		List<String> studentSections = new ArrayList<String>();
		
		if (sectionIds != null) {
			for (Iterator<String> sectionIter = sectionIds.iterator(); sectionIter.hasNext();) {
				String sectionId = (String) sectionIter.next();
				if (sectionId != null && sectionAwareness.isSectionMemberInRole(sectionId, studentId, Role.STUDENT)) {
					studentSections.add(sectionId);
				}
			}
		}

		for (Iterator permIter = graderPermissions.iterator(); permIter.hasNext();) {
			Permission perm = (Permission) permIter.next();
			String sectionId = perm.getGroupId();
			if (studentSections.contains(sectionId) || sectionId == null) {
				Long catId = perm.getCategoryId();
				if (catId == null) {
					return returnCategoryList;
				}else{
					returnCategoryList.add(catId);
				}
		//		Category cat = (Category)categoryIdCategoryMap.get(catId);
		//		if (cat != null)
//					categoryMap.put(cat, null);
			}
		}
		
		return returnCategoryList;
	}
	
	public boolean getPermissionForUserForAllAssignment(Long gradebookId, String userId) throws IllegalArgumentException
	{
		if(gradebookId == null || userId == null)
			throw new IllegalArgumentException("Null parameter(s) in GradebookPermissionServiceImpl.getPermissionForUserForAllAssignment");
		
		List anyCategoryPermission = getPermissionsForUserAnyCategory(gradebookId, userId);

		if(anyCategoryPermission != null && anyCategoryPermission.size() > 0 )
		{
			return true;
		}

		return false;
	}
	
	public boolean getPermissionForUserForAllAssignmentForStudent(Long gradebookId, String userId, String studentId, List sectionIds) throws IllegalArgumentException
	{
		if(gradebookId == null || userId == null)
			throw new IllegalArgumentException("Null parameter(s) in GradebookPermissionServiceImpl.getPermissionForUserForAllAssignment");
		
		List<Permission> graderPermissions = this.getPermissionsForUser(gradebookId, userId);
		if(graderPermissions == null || graderPermissions.isEmpty())
		{
			return false;
		}
		
		for (Iterator<Permission> permIter = graderPermissions.iterator(); permIter.hasNext();) {
			Permission perm = (Permission) permIter.next();
			String sectionId = perm.getGroupId();
			if (sectionId == null || (sectionIds.contains(sectionId) && sectionAwareness.isSectionMemberInRole(sectionId, studentId, Role.STUDENT))) {
				if (perm.getCategoryId() == null) {
					return true;
				}
			}
		}

		return false;
	}

	public Map<String, String> getStudentsForItem(Long gradebookId, String userId, List<String> studentIds, int cateType, Long categoryId, List courseSections)
	throws IllegalArgumentException
	{
		if(gradebookId == null || userId == null)
			throw new IllegalArgumentException("Null parameter(s) in GradebookPermissionServiceImpl.getStudentsForItem");
		if(cateType != GradebookService.CATEGORY_TYPE_ONLY_CATEGORY && cateType != GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY && cateType != GradebookService.CATEGORY_TYPE_NO_CATEGORY)
			throw new IllegalArgumentException("Invalid category type in GradebookPermissionServiceImpl.getStudentsForItem");

		if(studentIds != null)
		{
			Map<String, List<String>> sectionIdStudentIdsMap = getSectionIdStudentIdsMap(courseSections, studentIds);
			if(cateType == GradebookService.CATEGORY_TYPE_NO_CATEGORY)
			{
				List<Permission> perms = getPermissionsForUserAnyGroup(gradebookId, userId);

				Map<String, String> studentMap = new HashMap<String, String>();
				if(perms != null && perms.size() > 0)
				{
					boolean view = false;
					boolean grade = false;
					for(Iterator<Permission> iter = perms.iterator(); iter.hasNext();)
					{
						Permission perm = (Permission) iter.next();
						if(perm != null && perm.getFunction().equalsIgnoreCase(GradebookService.gradePermission))
						{
							grade = true;
							break;
						}
						if(perm != null && perm.getFunction().equalsIgnoreCase(GradebookService.viewPermission))
						{
							view = true;
						}
					}
					for(Iterator<String> studentIter = studentIds.iterator(); studentIter.hasNext();)
					{
						if(grade == true)
							studentMap.put((String)studentIter.next(), GradebookService.gradePermission);
						else if(view == true)
							studentMap.put((String)studentIter.next(), GradebookService.viewPermission);
					}
				}

				perms = getPermissionsForUser(gradebookId, userId);

				if(perms != null)
				{
					Map<String, String> studentMapForGroups = filterPermissionForGrader(perms, studentIds, sectionIdStudentIdsMap);
                    for(Iterator<Map.Entry<String, String>> iter = studentMapForGroups.entrySet().iterator(); iter.hasNext();)
					{
                        Map.Entry<String, String> entry = iter.next();
                        String key = entry.getKey();
						if((studentMap.containsKey(key) && ((String)studentMap.get(key)).equalsIgnoreCase(GradebookService.viewPermission))
								|| !studentMap.containsKey(key))
							studentMap.put(key, studentMapForGroups.get(key));
					}
				}

				return studentMap;
			}
			else
			{
				List<Long> cateList = new ArrayList<Long>();
				cateList.add(categoryId);
				List<Permission> perms = getPermissionsForUserAnyGroupForCategory(gradebookId, userId, cateList);

				Map<String, String> studentMap = new HashMap<String, String>();
				if(perms != null && perms.size() > 0)
				{
					boolean view = false;
					boolean grade = false;
					for(Iterator<Permission> iter = perms.iterator(); iter.hasNext();)
					{
						Permission perm = (Permission) iter.next();
						if(perm != null && perm.getFunction().equalsIgnoreCase(GradebookService.gradePermission))
						{
							grade = true;
							break;
						}
						if(perm != null && perm.getFunction().equalsIgnoreCase(GradebookService.viewPermission))
						{
							view = true;
						}
					}
					for(Iterator<String> studentIter = studentIds.iterator(); studentIter.hasNext();)
					{
						if(grade == true)
							studentMap.put((String)studentIter.next(), GradebookService.gradePermission);
						else if(view == true)
							studentMap.put((String)studentIter.next(), GradebookService.viewPermission);
					}
				}
				perms = getPermissionsForUserAnyGroupAnyCategory(gradebookId, userId);

				if(perms != null)
				{
					Map<String, String> studentMapForGroups = filterPermissionForGraderForAllStudent(perms, studentIds);
					for(Iterator<Entry<String, String>> iter = studentMapForGroups.entrySet().iterator(); iter.hasNext();)
					{
						Entry<String, String> entry = iter.next();
						String key = entry.getKey();
						if((studentMap.containsKey(key) && ((String)studentMap.get(key)).equalsIgnoreCase(GradebookService.viewPermission))
								|| !studentMap.containsKey(key))
							studentMap.put(key, entry.getValue());
					}
				}
				
				if (courseSections != null && !courseSections.isEmpty()) {
					List<String> groupIds = new ArrayList<String>();
					for(Iterator<CourseSection> iter = courseSections.iterator(); iter.hasNext();)
					{
						CourseSection grp = (CourseSection) iter.next();
						if(grp != null)
							groupIds.add(grp.getUuid());
					}

					perms = getPermissionsForUserForGoupsAnyCategory(gradebookId, userId, groupIds);
					if(perms != null)
					{
						Map<String, String> studentMapForGroups = filterPermissionForGrader(perms, studentIds, sectionIdStudentIdsMap);
						for(Iterator<Entry<String, String>> iter = studentMapForGroups.entrySet().iterator(); iter.hasNext();)
						{
							Entry<String, String> entry = iter.next();
							String key = entry.getKey();
							if((studentMap.containsKey(key) && ((String)studentMap.get(key)).equalsIgnoreCase(GradebookService.viewPermission))
									|| !studentMap.containsKey(key))
								studentMap.put(key, entry.getValue());
						}
					}
				}

				perms = getPermissionsForUserForCategory(gradebookId, userId, cateList);
				if(perms != null)
				{
					Map<String, String> studentMapForGroups = filterPermissionForGrader(perms, studentIds, sectionIdStudentIdsMap);
					for(Iterator<Entry<String, String>> iter = studentMapForGroups.entrySet().iterator(); iter.hasNext();)
					{
						Entry<String, String> entry = iter.next();
						String key = entry.getKey();
						if((studentMap.containsKey(key) && ((String)studentMap.get(key)).equalsIgnoreCase(GradebookService.viewPermission))
								|| !studentMap.containsKey(key))
							studentMap.put(key, entry.getValue());
					}
				}

				return studentMap;
			}
		}
		return null;
	}
	
	public Map<String, String> getStudentsForItem(String gradebookUid, String userId, List<String> studentIds, int cateType, Long categoryId, List courseSections)
	throws IllegalArgumentException
	{
		if(gradebookUid == null || userId == null)
			throw new IllegalArgumentException("Null parameter(s) in GradebookPermissionServiceImpl.getStudentsForItem");
	
		Long gradebookId = getGradebook(gradebookUid).getId();
		return getStudentsForItem(gradebookId, userId, studentIds, cateType, categoryId, courseSections);
	}

	public List<String> getViewableGroupsForUser(Long gradebookId, String userId, List<String> groupIds) {
		if(gradebookId == null || userId == null)
			throw new IllegalArgumentException("Null parameter(s) in GradebookPermissionServiceImpl.getViewableSectionsForUser");
		
		if (groupIds == null || groupIds.size() == 0)
			return null;
		
		List anyGroupPermission = getPermissionsForUserAnyGroup(gradebookId, userId);
		if(anyGroupPermission != null && anyGroupPermission.size() > 0 )
		{
			return groupIds;
		}
		else
		{
			List<Permission> permList = getPermissionsForUserForGroup(gradebookId, userId, groupIds);
			
			List<String> filteredGroups = new ArrayList<String>();
			for(Iterator<String> groupIter = groupIds.iterator(); groupIter.hasNext();)
			{
				String groupId = (String)groupIter.next();
				if(groupId != null)
				{
					for(Iterator<Permission> iter = permList.iterator(); iter.hasNext();)
					{
						Permission perm = (Permission) iter.next();
						if(perm != null && perm.getGroupId().equals(groupId))
						{
							filteredGroups.add(groupId);
							break;
						}
					}
				}
			}
			return filteredGroups;
		}
		
	}
	
	public List getViewableGroupsForUser(String gradebookUid, String userId, List groupIds) {
		if(gradebookUid == null || userId == null)
			throw new IllegalArgumentException("Null parameter(s) in GradebookPermissionServiceImpl.getViewableSectionsForUser");
	
		Long gradebookId = getGradebook(gradebookUid).getId();
		
		return getViewableGroupsForUser(gradebookId, userId, groupIds);
	}
	
	public List getGraderPermissionsForUser(Long gradebookId, String userId) {
		if(gradebookId == null || userId == null)
			throw new IllegalArgumentException("Null parameter(s) in GradebookPermissionServiceImpl.getPermissionsForUser");
		
		return getPermissionsForUser(gradebookId, userId);
	}
	
	public List getGraderPermissionsForUser(String gradebookUid, String userId) {
		if (gradebookUid == null || userId == null) {
			throw new IllegalArgumentException("Null gradebookUid or userId passed to getGraderPermissionsForUser");
		}
		
		Long gradebookId = getGradebook(gradebookUid).getId();
		
		return getPermissionsForUser(gradebookId, userId);
	}
	
	private Map<String, String> filterPermissionForGrader(List<Permission> perms, List<String> studentIds, Map<String, List<String>> sectionIdStudentIdsMap)
	{
		if(perms != null)
		{
			Map<String, String> permMap = new HashMap<String, String>();
			for(Iterator<Permission> iter = perms.iterator(); iter.hasNext();)
			{
				Permission perm = (Permission)iter.next();
				if(perm != null)
				{
					if(permMap.containsKey(perm.getGroupId()) && ((String)permMap.get(perm.getGroupId())).equalsIgnoreCase(GradebookService.viewPermission))
					{
						if(perm.getFunction().equalsIgnoreCase(GradebookService.gradePermission))
							permMap.put(perm.getGroupId(), GradebookService.gradePermission);
					}
					else if(!permMap.containsKey(perm.getGroupId()))
					{
						permMap.put(perm.getGroupId(), perm.getFunction());
					}
				}
			}
			Map<String, String> studentMap = new HashMap<String, String>();

			if(perms != null)
			{
				for(Iterator<String> iter = studentIds.iterator(); iter.hasNext();)
				{
					String studentId = (String) iter.next();
					if (sectionIdStudentIdsMap != null) {
						for(Iterator<Map.Entry<String, List<String>>> groupIter = sectionIdStudentIdsMap.entrySet().iterator(); groupIter.hasNext();)
						{
						    Map.Entry<String, List<String>> entry = groupIter.next();
							String grpId = entry.getKey();
							List<String> sectionMembers = entry.getValue();

							if(sectionMembers != null && sectionMembers.contains(studentId) && permMap.containsKey(grpId))
							{
								if(studentMap.containsKey(studentId) && ((String)studentMap.get(studentId)).equalsIgnoreCase(GradebookService.viewPermission))
								{
									if(((String)permMap.get(grpId)).equalsIgnoreCase(GradebookService.gradePermission))
										studentMap.put(studentId, GradebookService.gradePermission);
								}
								else if(!studentMap.containsKey(studentId))
									studentMap.put(studentId, permMap.get(grpId));
							}
						}
					}
				}
			}
			return studentMap;
		}
		else
			return new HashMap<String, String>();
	}

	private Map<String, String> filterPermissionForGraderForAllStudent(List<Permission> perms, List<String> studentIds)
	{
		if(perms != null)
		{
			Boolean grade = false;
			Boolean view = false;
			for(Iterator<Permission> iter = perms.iterator(); iter.hasNext();)
			{
				Permission perm = (Permission)iter.next();
				if(perm.getFunction().equalsIgnoreCase(GradebookService.gradePermission))
				{
					grade = true;
					break;
				}
				else if(perm.getFunction().equalsIgnoreCase(GradebookService.viewPermission))
					view = true;
			}

			Map<String, String> studentMap = new HashMap<String, String>();

			if(grade || view)
			{
				for(Iterator<String> iter = studentIds.iterator(); iter.hasNext();)
				{
					String studentId = (String) iter.next();
					if(grade)
						studentMap.put(studentId, GradebookService.gradePermission);
					else if(view)
						studentMap.put(studentId, GradebookService.viewPermission);
				}
			}
			return studentMap;
		}
		else
			return new HashMap<String, String>();
	}

	private Map filterPermissionForGraderForAllAssignments(List perms, List assignmentList)
	{
		if(perms != null)
		{
			Boolean grade = false;
			Boolean view = false;
			for(Iterator iter = perms.iterator(); iter.hasNext();)
			{
				Permission perm = (Permission)iter.next();
				if(perm.getFunction().equalsIgnoreCase(GradebookService.gradePermission))
				{
					grade = true;
					break;
				}
				else if(perm.getFunction().equalsIgnoreCase(GradebookService.viewPermission))
					view = true;
			}

			Map assignMap = new HashMap();

			if(grade || view)
			{
				for(Iterator iter = assignmentList.iterator(); iter.hasNext();)
				{
					Assignment assign = (Assignment) iter.next();
					if(grade && assign != null)
						assignMap.put(assign.getId(), GradebookService.gradePermission);
					else if(view && assign != null)
						assignMap.put(assign.getId(), GradebookService.viewPermission);
				}
			}
			return assignMap;
		}
		else
			return new HashMap();
	}

	private Map getAvailableItemsForStudent(Gradebook gradebook, String userId, String studentId, Map sectionIdCourseSectionMap, Map catIdCategoryMap, List assignments, List permsForUserAnyGroup, List allPermsForUser, List permsForAnyGroupForCategories, List permsForUserAnyGroupAnyCategory, List permsForGroupsAnyCategory, List permsForUserForCategories, Map sectionIdStudentIdsMap) throws IllegalArgumentException
	{
		if(gradebook == null || userId == null || studentId == null)
			throw new IllegalArgumentException("Null parameter(s) in GradebookPermissionServiceImpl.getAvailableItemsForStudent");
		
		List cateList = new ArrayList(catIdCategoryMap.values());
		
		if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_NO_CATEGORY)
		{
			Map assignMap = new HashMap();
			if(permsForUserAnyGroup != null && permsForUserAnyGroup.size() > 0)
			{
				boolean view = false;
				boolean grade = false;
				for(Iterator iter = permsForUserAnyGroup.iterator(); iter.hasNext();)
				{
					Permission perm = (Permission) iter.next();
					if(perm != null && perm.getFunction().equalsIgnoreCase(GradebookService.gradePermission))
					{
						grade = true;
						break;
					}
					if(perm != null && perm.getFunction().equalsIgnoreCase(GradebookService.viewPermission))
					{
						view = true;
					}
				}
				for(Iterator iter = assignments.iterator(); iter.hasNext();)
				{
					Assignment as = (Assignment) iter.next();
					if(grade == true && as != null)
						assignMap.put(as.getId(), GradebookService.gradePermission);
					else if(view == true && as != null)
						assignMap.put(as.getId(), GradebookService.viewPermission);
				}
			}

			if(allPermsForUser != null)
			{
				Map assignsMapForGroups = filterPermissionForGrader(allPermsForUser, studentId, assignments, sectionIdStudentIdsMap);
                for(Iterator<Map.Entry<Long, String>> iter = assignsMapForGroups.entrySet().iterator(); iter.hasNext();)
                {
                    Map.Entry<Long, String> entry = iter.next();
                    Long key = entry.getKey();
					if((assignMap.containsKey(key) && ((String)assignMap.get(key)).equalsIgnoreCase(GradebookService.viewPermission))
							|| !assignMap.containsKey(key))
						assignMap.put(key, entry.getValue());
				}
			}
			return assignMap;
		}
		else
		{

			Map assignMap = new HashMap();
			if(permsForAnyGroupForCategories != null && permsForAnyGroupForCategories.size() > 0)
			{
				for(Iterator iter = permsForAnyGroupForCategories.iterator(); iter.hasNext();)
				{
					Permission perm = (Permission)iter.next();
					if(perm != null)
					{
						if(perm.getCategoryId() != null)
						{
							for(Iterator cateIter = cateList.iterator(); cateIter.hasNext();)
							{
								Category cate = (Category) cateIter.next();
								if(cate != null && cate.getId().equals(perm.getCategoryId()))
								{
									List assignmentList = cate.getAssignmentList();
									if (assignmentList != null) {
										for(Iterator assignIter = assignmentList.iterator(); assignIter.hasNext();)
										{
											Assignment as = (Assignment)assignIter.next();
											if(as != null)
											{
												Long assignId = as.getId();
												if(as.getCategory() != null)
												{
													if(assignMap.containsKey(assignId) && ((String)assignMap.get(assignId)).equalsIgnoreCase(GradebookService.viewPermission))
													{
														if(perm.getFunction().equalsIgnoreCase(GradebookService.gradePermission))
														{
															assignMap.put(assignId, GradebookService.gradePermission);
														}
													}
													else if(!assignMap.containsKey(assignId))
													{
														assignMap.put(assignId, perm.getFunction());
													}
												}
											}
										}
									}
									break;
								}
							}
						}
					}
				}				
			}

			if(permsForUserAnyGroupAnyCategory != null)
			{
				Map<Long, String> assignMapForGroups = filterPermissionForGraderForAllAssignments(permsForUserAnyGroupAnyCategory, assignments);
				for(Iterator<Entry<Long, String>> iter = assignMapForGroups.entrySet().iterator(); iter.hasNext();)
				{
					Entry<Long, String> entry = iter.next();
					Long key = entry.getKey();
					if((assignMap.containsKey(key) && ((String)assignMap.get(key)).equalsIgnoreCase(GradebookService.viewPermission))
							|| !assignMap.containsKey(key))
						assignMap.put(key, entry.getValue());
				}
			}
			
			if(permsForGroupsAnyCategory != null)
			{
				Map<Long, String> assignMapForGroups = filterPermissionForGrader(permsForGroupsAnyCategory, studentId, assignments, sectionIdStudentIdsMap);
				for(Iterator<Entry<Long, String>> iter = assignMapForGroups.entrySet().iterator(); iter.hasNext();)
				{
					Entry<Long, String> entry = iter.next();
					Long key = entry.getKey();
					if((assignMap.containsKey(key) && ((String)assignMap.get(key)).equalsIgnoreCase(GradebookService.viewPermission))
							|| !assignMap.containsKey(key))
						assignMap.put(key, entry.getValue());
				}
			}

			if(permsForUserForCategories != null)
			{
				Map assignMapForGroups = filterPermissionForGraderForCategory(permsForUserForCategories, studentId, cateList, sectionIdStudentIdsMap);
				if(assignMapForGroups != null)
				{
					for(Iterator<Entry<Long, String>> iter = assignMapForGroups.entrySet().iterator(); iter.hasNext();)
					{
						Entry<Long, String> entry = iter.next();
						Long key = entry.getKey();
						if((assignMap.containsKey(key) && ((String)assignMap.get(key)).equalsIgnoreCase(GradebookService.viewPermission))
								|| !assignMap.containsKey(key))
						{
							assignMap.put(key, entry.getValue());
						}
					}
				}
			}

			return assignMap;
		}
	}
	
	public Map getAvailableItemsForStudent(Long gradebookId, String userId, String studentId, Collection courseSections) throws IllegalArgumentException
	{
		if(gradebookId == null || userId == null || studentId == null)
			throw new IllegalArgumentException("Null parameter(s) in GradebookPermissionServiceImpl.getAvailableItemsForStudent");

		List categories = getCategoriesWithAssignments(gradebookId);
		Map catIdCategoryMap = new HashMap();
		if (!categories.isEmpty()) {
			for (Iterator catIter = categories.iterator(); catIter.hasNext();) {
				Category cat = (Category)catIter.next();
				if (cat != null)
					catIdCategoryMap.put(cat.getId(), cat);
			}
		}
		Map sectionIdCourseSectionMap = new HashMap();
		if (!courseSections.isEmpty()) {
			for (Iterator sectionIter = courseSections.iterator(); sectionIter.hasNext();) {
				CourseSection section = (CourseSection) sectionIter.next();
				if (section != null) {
					sectionIdCourseSectionMap.put(section.getUuid(), section);
				}
			}
		}
		List studentIds = new ArrayList();
		studentIds.add(studentId);
		Map sectionIdStudentIdsMap = getSectionIdStudentIdsMap(courseSections, studentIds);
		
		Gradebook gradebook = getGradebook(getGradebookUid(gradebookId));
		List assignments = getAssignments(gradebookId);
		List categoryIds = new ArrayList(catIdCategoryMap.keySet());
		List groupIds = new ArrayList(sectionIdCourseSectionMap.keySet());
		
		// Retrieve all the different permission info needed here so not called repeatedly for each student
		List permsForUserAnyGroup = getPermissionsForUserAnyGroup(gradebookId, userId);
		List allPermsForUser = getPermissionsForUser(gradebookId, userId);
		List permsForAnyGroupForCategories = getPermissionsForUserAnyGroupForCategory(gradebookId, userId, categoryIds);
		List permsForUserAnyGroupAnyCategory = getPermissionsForUserAnyGroupAnyCategory(gradebookId, userId);
		List permsForGroupsAnyCategory = getPermissionsForUserForGoupsAnyCategory(gradebookId, userId, groupIds);
		List permsForUserForCategories = getPermissionsForUserForCategory(gradebookId, userId, categoryIds);
		
		return getAvailableItemsForStudent(gradebook, userId, studentId, sectionIdCourseSectionMap, catIdCategoryMap, assignments, permsForUserAnyGroup, allPermsForUser, permsForAnyGroupForCategories, permsForUserAnyGroupAnyCategory, permsForGroupsAnyCategory, permsForUserForCategories, sectionIdStudentIdsMap);
	}
	
	public Map getAvailableItemsForStudent(String gradebookUid, String userId, String studentId, Collection courseSections) throws IllegalArgumentException {
		if(gradebookUid == null || userId == null || studentId == null)
			throw new IllegalArgumentException("Null parameter(s) in GradebookPermissionServiceImpl.getAvailableItemsForStudent");
		
		Long gradebookId = getGradebook(gradebookUid).getId();
		
		return getAvailableItemsForStudent(gradebookId, userId, studentId, courseSections);

	}

	private List getAssignments(final Long gradebookId) throws HibernateException 
	{
		return (List)getHibernateTemplate().execute(new HibernateCallback() 
		{
			public Object doInHibernate(Session session) throws HibernateException 
			{
				List assignments = getAssignments(gradebookId, session);
				return assignments;
			}
		});
	}

	private Map filterPermissionForGrader(List perms, String studentId, List assignmentList, Map sectionIdStudentIdsMap)
	{
		if(perms != null)
		{
			Map permMap = new HashMap();
			for(Iterator iter = perms.iterator(); iter.hasNext();)
			{
				Permission perm = (Permission)iter.next();
				if(perm != null)
				{
					if(permMap.containsKey(perm.getGroupId()) && ((String)permMap.get(perm.getGroupId())).equalsIgnoreCase(GradebookService.viewPermission))
					{
						if(perm.getFunction().equalsIgnoreCase(GradebookService.gradePermission))
							permMap.put(perm.getGroupId(), GradebookService.gradePermission);
					}
					else if(!permMap.containsKey(perm.getGroupId()))
					{
						permMap.put(perm.getGroupId(), perm.getFunction());
					}
				}
			}
			Map assignmentMap = new HashMap();

			if(perms != null && sectionIdStudentIdsMap != null)
			{
				for(Iterator iter = assignmentList.iterator(); iter.hasNext();)
				{
					Long assignId = ((Assignment)iter.next()).getId();
	                for(Iterator<Map.Entry<String, List>> groupIter = sectionIdStudentIdsMap.entrySet().iterator(); groupIter.hasNext();)
	                {
	                    Map.Entry<String, List> entry = groupIter.next();
	                    String grpId = entry.getKey();
						List sectionMembers = (List) sectionIdStudentIdsMap.get(grpId);
						
						if(sectionMembers != null && sectionMembers.contains(studentId) && permMap.containsKey(grpId))
						{
							if(assignmentMap.containsKey(assignId) && ((String)assignmentMap.get(assignId)).equalsIgnoreCase(GradebookService.viewPermission))
							{
								if(((String)permMap.get(grpId)).equalsIgnoreCase(GradebookService.gradePermission))
									assignmentMap.put(assignId, GradebookService.gradePermission);
							}
							else if(!assignmentMap.containsKey(assignId))
								assignmentMap.put(assignId, permMap.get(grpId));
						}
					}
				}
			}
			return assignmentMap;
		}
		else
			return new HashMap();
	}

	private	 Map<Long, String> filterPermissionForGraderForCategory(List perms, String studentId, List categoryList, Map sectionIdStudentIdsMap)
	{
		if(perms != null)
		{
			Map<Long, String> assignmentMap = new HashMap<Long, String>();
			
			for(Iterator iter = perms.iterator(); iter.hasNext();)
			{
				Permission perm = (Permission)iter.next();
				if(perm != null && perm.getCategoryId() != null)
				{
					for(Iterator cateIter = categoryList.iterator(); cateIter.hasNext();)
					{
						Category cate = (Category) cateIter.next();
						if(cate != null && cate.getId().equals(perm.getCategoryId()))
						{
							List assignmentList = cate.getAssignmentList();
							if (assignmentList != null) {
								for(Iterator assignIter = assignmentList.iterator(); assignIter.hasNext();)
								{
									Assignment as = (Assignment)assignIter.next();
									if(as != null && sectionIdStudentIdsMap != null)
									{
										Long assignId = as.getId();
                                        for(Iterator<Map.Entry<String, List>> groupIter = sectionIdStudentIdsMap.entrySet().iterator(); groupIter.hasNext();)
					                    {
					                        Map.Entry<String, List> entry = groupIter.next();
					                        String grpId = entry.getKey();
											List sectionMembers = (List) sectionIdStudentIdsMap.get(grpId);

											if(sectionMembers != null && sectionMembers.contains(studentId) && as.getCategory() != null)
											{
												if(assignmentMap.containsKey(assignId) && grpId.equals(perm.getGroupId()) && ((String)assignmentMap.get(assignId)).equalsIgnoreCase(GradebookService.viewPermission))
												{
													if(perm.getFunction().equalsIgnoreCase(GradebookService.gradePermission))
													{
														assignmentMap.put(assignId, GradebookService.gradePermission);
													}
												}
												else if(!assignmentMap.containsKey(assignId) && grpId.equals(perm.getGroupId()))
												{
													assignmentMap.put(assignId, perm.getFunction());
												}
											}
										}
									}
								}
							}
							break;
						}
					}
				}
			}
			return assignmentMap;
		}
		else
			return new HashMap();
	}

	public Map getAvailableItemsForStudents(Long gradebookId, String userId, List studentIds, Collection courseSections) throws IllegalArgumentException
	{
		if(gradebookId == null || userId == null)
			throw new IllegalArgumentException("Null parameter(s) in GradebookPermissionServiceImpl.getAvailableItemsForStudents");
		
		Map catIdCategoryMap = new HashMap();
		List categories = getCategoriesWithAssignments(gradebookId);
		if (categories != null && !categories.isEmpty()) {
			for (Iterator catIter = categories.iterator(); catIter.hasNext();) {
				Category cat = (Category)catIter.next();
				if (cat != null) {
					catIdCategoryMap.put(cat.getId(), cat);
				}
			}
		}
		Map sectionIdCourseSectionMap = new HashMap();
		if (!courseSections.isEmpty()) {
			for (Iterator sectionIter = courseSections.iterator(); sectionIter.hasNext();) {
				CourseSection section = (CourseSection) sectionIter.next();
				if (section != null) {
					sectionIdCourseSectionMap.put(section.getUuid(), section);
				}
			}
		}
		
		Map sectionIdStudentIdsMap = getSectionIdStudentIdsMap(courseSections, studentIds);
		
		Gradebook gradebook = getGradebook(getGradebookUid(gradebookId));
		List assignments = getAssignments(gradebookId);
		List categoryIds = new ArrayList(catIdCategoryMap.keySet());
		List groupIds = new ArrayList(sectionIdCourseSectionMap.keySet());
		
		// Retrieve all the different permission info needed here so not called repeatedly for each student
		List permsForUserAnyGroup = getPermissionsForUserAnyGroup(gradebookId, userId);
		List allPermsForUser = getPermissionsForUser(gradebookId, userId);
		List permsForAnyGroupForCategories = getPermissionsForUserAnyGroupForCategory(gradebookId, userId, categoryIds);
		List permsForUserAnyGroupAnyCategory = getPermissionsForUserAnyGroupAnyCategory(gradebookId, userId);
		List permsForGroupsAnyCategory = getPermissionsForUserForGoupsAnyCategory(gradebookId, userId, groupIds);
		List permsForUserForCategories = getPermissionsForUserForCategory(gradebookId, userId, categoryIds);
		
		if(studentIds != null)
		{
			Map studentsMap = new HashMap();
			for(Iterator iter = studentIds.iterator(); iter.hasNext();)
			{
				String studentId = (String) iter.next();
				if(studentId != null)
				{
				    Map assignMap = getAvailableItemsForStudent(gradebook, userId, studentId, sectionIdCourseSectionMap, catIdCategoryMap, assignments, permsForUserAnyGroup, allPermsForUser, permsForAnyGroupForCategories, permsForUserAnyGroupAnyCategory, permsForGroupsAnyCategory, permsForUserForCategories, sectionIdStudentIdsMap);
					studentsMap.put(studentId, assignMap);
				}
			}
			return studentsMap;
		}

		return new HashMap();
	}
	
	public Map getAvailableItemsForStudents(String gradebookUid, String userId, List studentIds, Collection courseSections) throws IllegalArgumentException
	{
		if(gradebookUid == null || userId == null)
			throw new IllegalArgumentException("Null parameter(s) in GradebookPermissionServiceImpl.getAvailableItemsForStudents");
		
		Long gradebookId = getGradebook(gradebookUid).getId();
		return getAvailableItemsForStudents(gradebookId, userId, studentIds, courseSections);
	}

	public Map getCourseGradePermission(Long gradebookId, String userId, List studentIds, List courseSections) throws IllegalArgumentException
	{
		if(gradebookId == null || userId == null)
			throw new IllegalArgumentException("Null parameter(s) in GradebookPermissionServiceImpl.getCourseGradePermission");

		if(studentIds != null)
		{
			Map studentsMap = new HashMap();
			Map sectionIdStudentIdsMap = getSectionIdStudentIdsMap(courseSections, studentIds);

			List perms = getPermissionsForUserAnyGroupAnyCategory(gradebookId, userId);
			if(perms != null)
			{
				Map studentMapForGroups = filterPermissionForGraderForAllStudent(perms, studentIds);
				for(Iterator<Map.Entry<String, String>> iter = studentMapForGroups.entrySet().iterator(); iter.hasNext();)
				{
                    Map.Entry<String, String> entry = iter.next();
                    String key = entry.getKey();
					if((studentsMap.containsKey(key) && ((String)studentsMap.get(key)).equalsIgnoreCase(GradebookService.viewPermission))
							|| !studentsMap.containsKey(key))
						studentsMap.put(key, studentMapForGroups.get(key));
				}
			}

			List groupIds = new ArrayList();
			if(courseSections != null)
			{
				for(Iterator iter = courseSections.iterator(); iter.hasNext();)
				{
					CourseSection grp = (CourseSection) iter.next();
					if(grp != null)
						groupIds.add(grp.getUuid());
				}
				
				perms = getPermissionsForUserForGoupsAnyCategory(gradebookId, userId, groupIds);
				if(perms != null)
				{
					Map<String, String> studentMapForGroups = filterPermissionForGrader(perms, studentIds, sectionIdStudentIdsMap);
					for(Iterator<Entry<String, String>> iter = studentMapForGroups.entrySet().iterator(); iter.hasNext();)
					{
						Entry<String, String> entry = iter.next();
						String key = entry.getKey();
						if((studentsMap.containsKey(key) && ((String)studentsMap.get(key)).equalsIgnoreCase(GradebookService.viewPermission))
								|| !studentsMap.containsKey(key))
							studentsMap.put(key, entry.getValue());
					}
				}
				
				Gradebook gradebook = getGradebook(getGradebookUid(gradebookId));
				if(gradebook != null && (gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_ONLY_CATEGORY || 
						gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY))
				{
					List cateList = getCategories(gradebookId);
					
					perms = getPermissionsForUserForGroup(gradebookId, userId, groupIds);
					if(perms != null)
					{
						Map<String, String> studentMapForGroups = filterForAllCategoryStudents(perms, studentIds, cateList, sectionIdStudentIdsMap);
						for(Iterator<Entry<String, String>> iter = studentMapForGroups.entrySet().iterator(); iter.hasNext();)
						{
							Entry<String, String> entry = iter.next();
							String key = entry.getKey();
							if((studentsMap.containsKey(key) && ((String)studentsMap.get(key)).equalsIgnoreCase(GradebookService.viewPermission))
									|| !studentsMap.containsKey(key))
								studentsMap.put(key, entry.getValue());
						}
					}
					
					List cateIdList = new ArrayList();
					for(Iterator iter = cateList.iterator(); iter.hasNext();)
					{
						Category cate = (Category) iter.next();
						if(cate != null)
							cateIdList.add(cate.getId());
					}
					perms = getPermissionsForUserAnyGroupForCategory(gradebookId, userId, cateIdList);
					if(perms != null && perms.size() > 0)
					{
						Map<String, String> studentMapForGroups = filterForAllCategoryStudentsAnyGroup(perms, courseSections, studentIds, cateList);
						for(Iterator<Entry<String, String>> iter = studentMapForGroups.entrySet().iterator(); iter.hasNext();)
						{
							Entry<String, String> entry = iter.next();
							String key = entry.getKey();
							if((studentsMap.containsKey(key) && ((String)studentsMap.get(key)).equalsIgnoreCase(GradebookService.viewPermission))
									|| !studentsMap.containsKey(key))
								studentsMap.put(key, entry.getValue());
						}
					}
				}
			}

			return studentsMap;
		}
		return new HashMap();
	}
	
	public Map getCourseGradePermission(String gradebookUid, String userId, List studentIds, List courseSections) throws IllegalArgumentException
	{
		if(gradebookUid == null || userId == null)
			throw new IllegalArgumentException("Null parameter(s) in GradebookPermissionServiceImpl.getCourseGradePermission");
	
		Long gradebookId = getGradebook(gradebookUid).getId();
		return getCourseGradePermission(gradebookId, userId, studentIds, courseSections);
	}
	
	private Map<String, String> filterForAllCategoryStudents(List perms, List studentIds, List cateList, Map sectionIdStudentIdsMap)
	{
		if(perms != null && sectionIdStudentIdsMap != null && studentIds != null && cateList != null)
		{
			List cateIdList = new ArrayList();
			for(Iterator iter = cateList.iterator(); iter.hasNext();)
			{
				Category cate = (Category) iter.next();
				if(cate != null)
					cateIdList.add(cate.getId());
			}

			Map studentCateMap = new HashMap();
			for(Iterator studentIter = studentIds.iterator(); studentIter.hasNext();)
			{
				String studentId = (String) studentIter.next();
				studentCateMap.put(studentId, new HashMap());
				if(studentId != null)
				{
					for(Iterator<Map.Entry<String, List>> grpIter = sectionIdStudentIdsMap.entrySet().iterator(); grpIter.hasNext();)
					{
                        Map.Entry<String, List> entry = grpIter.next();
                        String grpId = entry.getKey();
						
						if(grpId != null)
						{				
							List grpMembers = (List)sectionIdStudentIdsMap.get(grpId);
							if (grpMembers != null && !grpMembers.isEmpty() && grpMembers.contains(studentId)) {
								for(Iterator permIter = perms.iterator(); permIter.hasNext();)
								{
									Permission perm = (Permission) permIter.next();
									if(perm != null && perm.getGroupId().equals(grpId) && perm.getCategoryId() != null && cateIdList.contains(perm.getCategoryId()))
									{
										Map cateMap = (Map) studentCateMap.get(studentId);
										if(cateMap.get(perm.getCategoryId()) == null || ((String)cateMap.get(perm.getCategoryId())).equals(GradebookService.viewPermission))
											cateMap.put(perm.getCategoryId(), perm.getFunction());
										studentCateMap.put(studentId, cateMap);
									}
								}
							}
						}
					}
				}
			}
			
			Map<String, String> studentPermissionMap = new HashMap<String, String>();
			for(Iterator<Entry<String, Map>> iter = studentCateMap.entrySet().iterator(); iter.hasNext();)
			{
				Entry<String, Map> perEntry = iter.next();
				String studentId = perEntry.getKey();
				Map cateMap = perEntry.getValue();
				if(cateMap != null)
				{
					for(Iterator allCatesIter = cateIdList.iterator(); allCatesIter.hasNext();)
					{
						Long existCateId = (Long) allCatesIter.next();
						if(existCateId != null)
						{
							boolean hasPermissionForCate = false;
							String permission = null;
							for(Iterator<Entry<Long, String>> cateIter = cateMap.entrySet().iterator(); cateIter.hasNext();)
							{
								Entry<Long, String> entry = cateIter.next();
								Long cateId = entry.getKey();
								if(cateId.equals(existCateId))
								{
									hasPermissionForCate = true;
									permission = entry.getValue();
									break;
								}
							}
							if(hasPermissionForCate && permission != null)
							{
								if(studentPermissionMap.get(studentId) == null || ((String)studentPermissionMap.get(studentId)).equals(GradebookService.gradePermission))
									studentPermissionMap.put(studentId, permission);
							}
							else if(!hasPermissionForCate)
							{
								if(studentPermissionMap.get(studentId) != null)
									studentPermissionMap.remove(studentId);
							}
						}
					}
				}
			}
			return studentPermissionMap;
		}
		return new HashMap<String, String>();
	}

	private Map<String, String> filterForAllCategoryStudentsAnyGroup(List perms, List courseSections, List studentIds, List cateList)
	{
		if(perms != null && courseSections != null && studentIds != null && cateList != null)
		{	
			Map<Long, String> cateMap = new HashMap<Long, String>();
			for(Iterator cateIter = cateList.iterator(); cateIter.hasNext();)
			{
				Category cate = (Category) cateIter.next();
				if(cate != null)
				{
					boolean permissionExistForCate = false;
					for(Iterator permIter = perms.iterator(); permIter.hasNext();)
					{
						Permission perm = (Permission) permIter.next();
						if(perm != null && perm.getCategoryId().equals(cate.getId()))
						{
							if((cateMap.get(cate.getId()) == null || ((String)cateMap.get(cate.getId())).equals(GradebookService.viewPermission)))
								cateMap.put(cate.getId(), perm.getFunction());
							permissionExistForCate = true;
						}
					}
					if(!permissionExistForCate)
						return new HashMap<String, String>();
				}
			}
			
			boolean view = false;
			for(Iterator iter = cateMap.keySet().iterator(); iter.hasNext();)
			{
				String permission = (String) cateMap.get((Long)iter.next());
				if(permission != null && permission.equals(GradebookService.viewPermission))
				{
					view = true;
				}
			}
			Map<String, String> studentMap = new HashMap<String, String>();
			for(Iterator studentIter = studentIds.iterator(); studentIter.hasNext();)
			{
				String studentId = (String) studentIter.next();
				if(view)
					studentMap.put(studentId, GradebookService.viewPermission);
				else
					studentMap.put(studentId, GradebookService.gradePermission);
			}
			
			return studentMap;
		}
		return new HashMap<String, String>();
	}
	
	public List getViewableStudentsForUser(Long gradebookId, String userId, List studentIds, List sections) {
		if(gradebookId == null || userId == null)
			throw new IllegalArgumentException("Null parameter(s) in GradebookPermissionServiceImpl.getAvailableItemsForStudent");
		
		List viewableStudents = new ArrayList();
		
		if (studentIds == null || studentIds.isEmpty())
			return viewableStudents;
		
		
		List permsForAnyGroup = getPermissionsForUserAnyGroup(gradebookId, userId);
		if (!permsForAnyGroup.isEmpty()) {
			return studentIds;
		}
		
		Map sectionIdStudentIdsMap = getSectionIdStudentIdsMap(sections, studentIds);
		
		if (sectionIdStudentIdsMap.isEmpty()) {
			return null;
		}
		
		// use a map to make sure the student ids are unique
		Map studentMap = new HashMap();
		
		// Next, check for permissions for specific sections
		List groupIds = new ArrayList(sectionIdStudentIdsMap.keySet());
		List permsForGroupsAnyCategory = getPermissionsForUserForGroup(gradebookId, userId, groupIds);
		
		if (permsForGroupsAnyCategory.isEmpty()) {
			return viewableStudents;
		}
		
		for (Iterator permsIter = permsForGroupsAnyCategory.iterator(); permsIter.hasNext();) {
			Permission perm = (Permission) permsIter.next();
			String groupId = perm.getGroupId();
			if (groupId != null) {
				List sectionStudentIds = (ArrayList)sectionIdStudentIdsMap.get(groupId);
				if (sectionStudentIds != null && !sectionStudentIds.isEmpty()) {
					for (Iterator studentIter = sectionStudentIds.iterator(); studentIter.hasNext();) {
						String studentId = (String) studentIter.next();
						studentMap.put(studentId, null);
					}
				}
			}
		}
		
		return new ArrayList(studentMap.keySet());
	}
	
	public List getViewableStudentsForUser(String gradebookUid, String userId, List studentIds, List sections) {
		if(gradebookUid == null || userId == null)
			throw new IllegalArgumentException("Null parameter(s) in GradebookPermissionServiceImpl.getViewableStudentsForUser");
		
		Long gradebookId = getGradebook(gradebookUid).getId();
		
		return getViewableStudentsForUser(gradebookId, userId, studentIds, sections);
		
	}

	public SectionAwareness getSectionAwareness()
	{
		return sectionAwareness;
	}

	public void setSectionAwareness(SectionAwareness sectionAwareness)
	{
		this.sectionAwareness = sectionAwareness;
	}
	
	private Map<String, List<String>> getSectionIdStudentIdsMap(Collection courseSections, Collection studentIds) {
		Map<String, List<String>> sectionIdStudentIdsMap = new HashMap<String, List<String>>();
		if (courseSections != null) {
			for (Iterator sectionIter = courseSections.iterator(); sectionIter.hasNext();) {
				CourseSection section = (CourseSection)sectionIter.next();
				if (section != null) {
					String sectionId = section.getUuid();
					List members = getSectionAwareness().getSectionMembersInRole(sectionId, Role.STUDENT);
					List<String> sectionMembersFiltered = new ArrayList<String>();
					if (!members.isEmpty()) {
						for (Iterator<EnrollmentRecord> memberIter = members.iterator(); memberIter.hasNext();) {
							EnrollmentRecord enr = (EnrollmentRecord) memberIter.next();
							String studentId = enr.getUser().getUserUid();
							if (studentIds.contains(studentId))
								sectionMembersFiltered.add(studentId);
						}
					}
					sectionIdStudentIdsMap.put(sectionId, sectionMembersFiltered);
				}
			}
		}
		return sectionIdStudentIdsMap;
	}

}
