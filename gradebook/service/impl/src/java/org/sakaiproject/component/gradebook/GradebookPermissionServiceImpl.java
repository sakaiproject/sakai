package org.sakaiproject.component.gradebook;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.sakaiproject.service.gradebook.shared.GradebookPermissionService;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.Permission;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.ParticipationRecord;
import org.sakaiproject.component.section.cover.SectionAwareness;
import org.springframework.orm.hibernate3.HibernateCallback;

public class GradebookPermissionServiceImpl extends BaseHibernateManager implements GradebookPermissionService
{
	public List getCategoriesForUser(Long gradebookId, String userId, List categoryList, int cateType) throws IllegalArgumentException
	{
		if(gradebookId == null || userId == null)
			throw new IllegalArgumentException("Null parameter(s) in GradebookPermissionServiceImpl.getCategoriesForUser");
		if(cateType != GradebookService.CATEGORY_TYPE_ONLY_CATEGORY && cateType != GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY)
			throw new IllegalArgumentException("CategoryType must be CATEGORY_TYPE_ONLY_CATEGORY or CATEGORY_TYPE_WEIGHTED_CATEGORY in GradebookPermissionServiceImpl.getCategoriesForUser");

		List anyCategoryPermission = getPermissionsForUserAnyCategory(gradebookId, userId);
		if(anyCategoryPermission != null && anyCategoryPermission.size() > 0 )
		{
			return categoryList;
		}
		else
		{
			List ids = new ArrayList();
			for(Iterator iter = categoryList.iterator(); iter.hasNext(); )
			{
				Category cate = (Category) iter.next();
				if(cate != null)
					ids.add(cate.getId());
			}

			List permList = getPermissionsForUserForCategory(gradebookId, userId, ids);

			List filteredCates = new ArrayList();
			for(Iterator cateIter = categoryList.iterator(); cateIter.hasNext();)
			{
				Category cate = (Category) cateIter.next();
				if(cate != null)
				{
					for(Iterator iter = permList.iterator(); iter.hasNext();)
					{
						Permission perm = (Permission) iter.next();
						if(perm != null && perm.getCategoryId().equals(cate.getId()))
						{
							filteredCates.add(cate);
							break;
						}
					}
				}
			}
			return filteredCates;
		}
	}

	public Map getStudentsForItem(Long gradebookId, String userId, List studentIds, int cateType, Long categoryId, List courseSections)
	throws IllegalArgumentException
	{
		if(gradebookId == null || userId == null)
			throw new IllegalArgumentException("Null parameter(s) in GradebookPermissionServiceImpl.getStudentsForItem");
		if(cateType != GradebookService.CATEGORY_TYPE_ONLY_CATEGORY && cateType != GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY && cateType != GradebookService.CATEGORY_TYPE_NO_CATEGORY)
			throw new IllegalArgumentException("Invalid category type in GradebookPermissionServiceImpl.getStudentsForItem");

		if(studentIds != null)
		{
			if(cateType == GradebookService.CATEGORY_TYPE_NO_CATEGORY)
			{
				List perms = getPermissionsForUserAnyGroup(gradebookId, userId);

				Map studentMap = new HashMap();
				if(perms != null && perms.size() > 0)
				{
					boolean view = false;
					boolean grade = false;
					for(Iterator iter = perms.iterator(); iter.hasNext();)
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
					for(Iterator studentIter = studentIds.iterator(); studentIter.hasNext();)
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
					Map studentMapForGroups = filterPermissionForGrader(perms, studentIds, courseSections);
					for(Iterator iter = studentMapForGroups.keySet().iterator(); iter.hasNext();)
					{
						String key = (String)iter.next();
						if((studentMap.containsKey(key) && ((String)studentMap.get(key)).equalsIgnoreCase(GradebookService.viewPermission))
								|| !studentMap.containsKey(key))
							studentMap.put(key, studentMapForGroups.get(key));
					}
				}

				return studentMap;
			}
			else
			{
				List cateList = new ArrayList();
				cateList.add(categoryId);
				List perms = getPermissionsForUserAnyGroupForCategory(gradebookId, userId, cateList);

				Map studentMap = new HashMap();
				if(perms != null && perms.size() > 0)
				{
					boolean view = false;
					boolean grade = false;
					for(Iterator iter = perms.iterator(); iter.hasNext();)
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
					for(Iterator studentIter = studentIds.iterator(); studentIter.hasNext();)
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
					Map studentMapForGroups = filterPermissionForGraderForAllStudent(perms, studentIds);
					for(Iterator iter = studentMapForGroups.keySet().iterator(); iter.hasNext();)
					{
						String key = (String)iter.next();
						if((studentMap.containsKey(key) && ((String)studentMap.get(key)).equalsIgnoreCase(GradebookService.viewPermission))
								|| !studentMap.containsKey(key))
							studentMap.put(key, studentMapForGroups.get(key));
					}
				}

				List groupIds = new ArrayList();
				for(Iterator iter = courseSections.iterator(); iter.hasNext();)
				{
					CourseSection grp = (CourseSection) iter.next();
					if(grp != null)
						groupIds.add(grp.getUuid());
				}
				perms = getPermissionsForUserForGoupsAnyCategory(gradebookId, userId, groupIds);
				if(perms != null)
				{
					Map studentMapForGroups = filterPermissionForGrader(perms, studentIds, courseSections);
					for(Iterator iter = studentMapForGroups.keySet().iterator(); iter.hasNext();)
					{
						String key = (String)iter.next();
						if((studentMap.containsKey(key) && ((String)studentMap.get(key)).equalsIgnoreCase(GradebookService.viewPermission))
								|| !studentMap.containsKey(key))
							studentMap.put(key, studentMapForGroups.get(key));
					}
				}

				perms = getPermissionsForUserForCategory(gradebookId, userId, cateList);
				if(perms != null)
				{
					Map studentMapForGroups = filterPermissionForGrader(perms, studentIds, courseSections);
					for(Iterator iter = studentMapForGroups.keySet().iterator(); iter.hasNext();)
					{
						String key = (String)iter.next();
						if((studentMap.containsKey(key) && ((String)studentMap.get(key)).equalsIgnoreCase(GradebookService.viewPermission))
								|| !studentMap.containsKey(key))
							studentMap.put(key, studentMapForGroups.get(key));
					}
				}

				return studentMap;
			}
		}
		return null;
	}

	private Map filterPermissionForGrader(List perms, List studentIds, Collection courseSections)
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
			Map studentMap = new HashMap();

			if(perms != null)
			{
				for(Iterator iter = studentIds.iterator(); iter.hasNext();)
				{
					String studentId = (String) iter.next();
					for(Iterator groupIter = courseSections.iterator(); groupIter.hasNext();)
					{
						CourseSection grp = (CourseSection) groupIter.next();
						List members = SectionAwareness.getSectionMembers(grp.getUuid());
						List memberIdList = new ArrayList();
						for(Iterator<ParticipationRecord> memberIter = members.iterator(); memberIter.hasNext();)
						{
							ParticipationRecord member = memberIter.next();
							if(member != null)
							{
								String userId = member.getUser().getUserUid();
								memberIdList.add(userId);
							}
						}
						if(memberIdList.contains(studentId) && permMap.containsKey(grp.getUuid()))
						{
							if(studentMap.containsKey(studentId) && ((String)studentMap.get(studentId)).equalsIgnoreCase(GradebookService.viewPermission))
							{
								if(((String)permMap.get(grp.getUuid())).equalsIgnoreCase(GradebookService.gradePermission))
									studentMap.put(studentId, GradebookService.gradePermission);
							}
							else if(!studentMap.containsKey(studentId))
								studentMap.put(studentId, permMap.get(grp.getUuid()));
						}
					}
				}
			}
			return studentMap;
		}
		else
			return new HashMap();
	}

	private Map filterPermissionForGraderForAllStudent(List perms, List studentIds)
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

			Map studentMap = new HashMap();

			if(grade || view)
			{
				for(Iterator iter = studentIds.iterator(); iter.hasNext();)
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
			return new HashMap();
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

	public Map getAvailableItemsForStudent(Long gradebookId, String userId, String studentId, Collection courseSections) throws IllegalArgumentException
	{
		if(gradebookId == null || userId == null || studentId == null)
			throw new IllegalArgumentException("Null parameter(s) in GradebookPermissionServiceImpl.getAvailableItemsForStudent");

		Gradebook gradebook = getGradebook(getGradebookUid(gradebookId));
		List assignments = getAssignments(gradebookId);

		if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_NO_CATEGORY)
		{
			List perms = getPermissionsForUserAnyGroup(gradebookId, userId);

			Map assignMap = new HashMap();
			if(perms != null && perms.size() > 0)
			{
				boolean view = false;
				boolean grade = false;
				for(Iterator iter = perms.iterator(); iter.hasNext();)
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

			perms = getPermissionsForUser(gradebookId, userId);

			if(perms != null)
			{
				Map assignsMapForGroups = filterPermissionForGrader(perms, studentId, assignments, courseSections);
				for(Iterator iter = assignsMapForGroups.keySet().iterator(); iter.hasNext();)
				{
					Long key = (Long)iter.next();
					if((assignMap.containsKey(key) && ((String)assignMap.get(key)).equalsIgnoreCase(GradebookService.viewPermission))
							|| !assignMap.containsKey(key))
						assignMap.put(key, assignsMapForGroups.get(key));
				}
			}
			return assignMap;
		}
		else
		{
			List cateList = getCategories(gradebookId);
			List cateIdList = new ArrayList();
			for(Iterator iter = cateList.iterator(); iter.hasNext();)
			{
				Category cate = (Category) iter.next();
				if(cate != null)
					cateIdList.add(cate.getId());
			}

			List perms = getPermissionsForUserAnyGroupForCategory(gradebookId, userId, cateIdList);

			Map assignMap = new HashMap();
			if(perms != null && perms.size() > 0)
			{
				for(Iterator iter = perms.iterator(); iter.hasNext();)
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
									List assignmentList = getAssignmentsForCategory(cate.getId());
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
									break;
								}
							}
						}
					}
				}				
			}

			perms = getPermissionsForUserAnyGroupAnyCategory(gradebookId, userId);

			if(perms != null)
			{
				Map assignMapForGroups = filterPermissionForGraderForAllAssignments(perms, assignments);
				for(Iterator iter = assignMapForGroups.keySet().iterator(); iter.hasNext();)
				{
					Long key = (Long)iter.next();
					if((assignMap.containsKey(key) && ((String)assignMap.get(key)).equalsIgnoreCase(GradebookService.viewPermission))
							|| !assignMap.containsKey(key))
						assignMap.put(key, assignMapForGroups.get(key));
				}
			}

			List groupIds = new ArrayList();
			for(Iterator iter = courseSections.iterator(); iter.hasNext();)
			{
				CourseSection grp = (CourseSection) iter.next();
				if(grp != null)
					groupIds.add(grp.getUuid());
			}
			perms = getPermissionsForUserForGoupsAnyCategory(gradebookId, userId, groupIds);
			if(perms != null)
			{
				Map assignMapForGroups = filterPermissionForGrader(perms, studentId, assignments, courseSections);
				for(Iterator iter = assignMapForGroups.keySet().iterator(); iter.hasNext();)
				{
					Long key = (Long)iter.next();
					if((assignMap.containsKey(key) && ((String)assignMap.get(key)).equalsIgnoreCase(GradebookService.viewPermission))
							|| !assignMap.containsKey(key))
						assignMap.put(key, assignMapForGroups.get(key));
				}
			}

			perms = getPermissionsForUserForCategory(gradebookId, userId, cateIdList);
			if(perms != null)
			{
				Map assignMapForGroups = filterPermissionForGraderForCategory(perms, studentId, courseSections, cateList);
				if(assignMapForGroups != null)
				{
					for(Iterator iter = assignMapForGroups.keySet().iterator(); iter.hasNext();)
					{
						Long key = (Long)iter.next();
						if((assignMap.containsKey(key) && ((String)assignMap.get(key)).equalsIgnoreCase(GradebookService.viewPermission))
								|| !assignMap.containsKey(key))
						{
							assignMap.put(key, assignMapForGroups.get(key));
						}
					}
				}
			}

			return assignMap;
		}
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

	private Map filterPermissionForGrader(List perms, String studentId, List assignmentList, Collection courseSections)
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

			if(perms != null)
			{
				for(Iterator iter = assignmentList.iterator(); iter.hasNext();)
				{
					Long assignId = ((Assignment)iter.next()).getId();
					for(Iterator groupIter = courseSections.iterator(); groupIter.hasNext();)
					{
						CourseSection grp = (CourseSection) groupIter.next();
						
						List members = SectionAwareness.getSectionMembers(grp.getUuid());
						List memberIdList = new ArrayList();
						for(Iterator<ParticipationRecord> memberIter = members.iterator(); memberIter.hasNext();)
						{
							ParticipationRecord member = memberIter.next();
							if(member != null)
							{
								String userId = member.getUser().getUserUid();
								memberIdList.add(userId);
							}
						}
						if(memberIdList.contains(studentId) && permMap.containsKey(grp.getUuid()))
						{
							if(assignmentMap.containsKey(assignId) && ((String)assignmentMap.get(assignId)).equalsIgnoreCase(GradebookService.viewPermission))
							{
								if(((String)permMap.get(grp.getUuid())).equalsIgnoreCase(GradebookService.gradePermission))
									assignmentMap.put(assignId, GradebookService.gradePermission);
							}
							else if(!assignmentMap.containsKey(assignId))
								assignmentMap.put(assignId, permMap.get(grp.getUuid()));
						}
					}
				}
			}
			return assignmentMap;
		}
		else
			return new HashMap();
	}

	private	Map filterPermissionForGraderForCategory(List perms, String studentId, Collection courseSections, List categoryList)
	{
		if(perms != null)
		{
			Map assignmentMap = new HashMap();
			
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
							List assignmentList = getAssignmentsForCategory(cate.getId());
							for(Iterator assignIter = assignmentList.iterator(); assignIter.hasNext();)
							{
								Assignment as = (Assignment)assignIter.next();
								if(as != null)
								{
									Long assignId = as.getId();
									for(Iterator groupIter = courseSections.iterator(); groupIter.hasNext();)
									{
										CourseSection grp = (CourseSection) groupIter.next();
										List members = SectionAwareness.getSectionMembers(grp.getUuid());
										List memberIdList = new ArrayList();
										for(Iterator<ParticipationRecord> memberIter = members.iterator(); memberIter.hasNext();)
										{
											ParticipationRecord member = memberIter.next();
											if(member != null)
											{
												String userId = member.getUser().getUserUid();
												memberIdList.add(userId);
											}
										}
										
										if(memberIdList.contains(studentId) && as.getCategory() != null)
										{
											if(assignmentMap.containsKey(assignId) && grp.getUuid().equals(perm.getGroupId()) && ((String)assignmentMap.get(assignId)).equalsIgnoreCase(GradebookService.viewPermission))
											{
												if(perm.getFunction().equalsIgnoreCase(GradebookService.gradePermission))
												{
													assignmentMap.put(assignId, GradebookService.gradePermission);
												}
											}
											else if(!assignmentMap.containsKey(assignId) && grp.getUuid().equals(perm.getGroupId()))
											{
												assignmentMap.put(assignId, perm.getFunction());
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
		
		if(studentIds != null)
		{
			Map studentsMap = new HashMap();
			for(Iterator iter = studentIds.iterator(); iter.hasNext();)
			{
				Map assignMap = new HashMap();
				String studentId = (String) iter.next();
				if(studentId != null)
				{
					assignMap = getAvailableItemsForStudent(gradebookId, userId, studentId, courseSections);
					studentsMap.put(studentId, assignMap);
				}
			}
			return studentsMap;
		}

		return new HashMap();
	}

	public Map getCourseGradePermission(Long gradebookId, String userId, List studentIds, List courseSections) throws IllegalArgumentException
	{
		if(gradebookId == null || userId == null)
			throw new IllegalArgumentException("Null parameter(s) in GradebookPermissionServiceImpl.getCourseGradePermission");

		if(studentIds != null)
		{
			Map studentsMap = new HashMap();

			List perms = getPermissionsForUserAnyGroupAnyCategory(gradebookId, userId);
			if(perms != null)
			{
				Map studentMapForGroups = filterPermissionForGraderForAllStudent(perms, studentIds);
				for(Iterator iter = studentMapForGroups.keySet().iterator(); iter.hasNext();)
				{
					String key = (String)iter.next();
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
					Map studentMapForGroups = filterPermissionForGrader(perms, studentIds, courseSections);
					for(Iterator iter = studentMapForGroups.keySet().iterator(); iter.hasNext();)
					{
						String key = (String)iter.next();
						if((studentsMap.containsKey(key) && ((String)studentsMap.get(key)).equalsIgnoreCase(GradebookService.viewPermission))
								|| !studentsMap.containsKey(key))
							studentsMap.put(key, studentMapForGroups.get(key));
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
						Map studentMapForGroups = filterForAllCategoryStudents(perms, courseSections, studentIds, cateList);
						for(Iterator iter = studentMapForGroups.keySet().iterator(); iter.hasNext();)
						{
							String key = (String)iter.next();
							if((studentsMap.containsKey(key) && ((String)studentsMap.get(key)).equalsIgnoreCase(GradebookService.viewPermission))
									|| !studentsMap.containsKey(key))
								studentsMap.put(key, studentMapForGroups.get(key));
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
					Map studentMap = new HashMap();
					if(perms != null && perms.size() > 0)
					{
						Map studentMapForGroups = filterForAllCategoryStudentsAnyGroup(perms, courseSections, studentIds, cateList);
						for(Iterator iter = studentMapForGroups.keySet().iterator(); iter.hasNext();)
						{
							String key = (String)iter.next();
							if((studentsMap.containsKey(key) && ((String)studentsMap.get(key)).equalsIgnoreCase(GradebookService.viewPermission))
									|| !studentsMap.containsKey(key))
								studentsMap.put(key, studentMapForGroups.get(key));
						}
					}
				}
			}

			return studentsMap;
		}
		return new HashMap();
	}
	
	private Map filterForAllCategoryStudents(List perms, List courseSections, List studentIds, List cateList)
	{
		if(perms != null && courseSections != null && studentIds != null && cateList != null)
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
					for(Iterator grpIter = courseSections.iterator(); grpIter.hasNext();)
					{
						CourseSection grp = (CourseSection) grpIter.next();
						List members = SectionAwareness.getSectionMembers(grp.getUuid());
						List memberIdList = new ArrayList();
						for(Iterator<ParticipationRecord> memberIter = members.iterator(); memberIter.hasNext();)
						{
							ParticipationRecord member = memberIter.next();
							if(member != null)
							{
								String userId = member.getUser().getUserUid();
								memberIdList.add(userId);
							}
						}
						
						if(grp != null && memberIdList.contains(studentId))
						{								
							for(Iterator permIter = perms.iterator(); permIter.hasNext();)
							{
								Permission perm = (Permission) permIter.next();
								if(perm != null && perm.getGroupId().equals(grp.getUuid()) && perm.getCategoryId() != null && cateIdList.contains(perm.getCategoryId()))
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
			
			Map studentPermissionMap = new HashMap();
			for(Iterator iter = studentCateMap.keySet().iterator(); iter.hasNext();)
			{
				String studentId = (String) iter.next();
				Map cateMap = (Map) studentCateMap.get(studentId);
				if(cateMap != null)
				{
					for(Iterator allCatesIter = cateIdList.iterator(); allCatesIter.hasNext();)
					{
						Long existCateId = (Long) allCatesIter.next();
						if(existCateId != null)
						{
							boolean hasPermissionForCate = false;
							String permission = null;
							for(Iterator cateIter = cateMap.keySet().iterator(); cateIter.hasNext();)
							{
								Long cateId = (Long) cateIter.next();
								if(cateId.equals(existCateId))
								{
									hasPermissionForCate = true;
									permission = (String) cateMap.get(cateId);
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
		return new HashMap();
	}

	private Map filterForAllCategoryStudentsAnyGroup(List perms, List courseSections, List studentIds, List cateList)
	{
		if(perms != null && courseSections != null && studentIds != null && cateList != null)
		{	
			Map cateMap = new HashMap();
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
						return new HashMap();
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
			Map studentMap = new HashMap();
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
		return new HashMap();
	}
}
