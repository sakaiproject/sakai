package org.sakaiproject.component.gradebook;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.service.gradebook.shared.GradebookPermissionService;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.Permission;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;

public class GradebookPermissionServiceImpl extends BaseHibernateManager implements GradebookPermissionService
{
	public List getCategoriesForUser(Long gradebookId, String userId, List categoryList, int cateType)
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
	
	public Map getStudentsForItem(Long gradebookId, String userId, List studentIds, int cateType, Long categoryId, List groups)
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
				
				perms = this.getPermissionsForUser(gradebookId, userId);

				if(perms != null)
				{
					Map studentMapForGroups = filterPermissionForGrader(perms, studentIds, groups);
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
				List perms = this.getPermissionsForUserAnyGroupForCategory(gradebookId, userId, cateList);

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
				for(Iterator iter = groups.iterator(); iter.hasNext();)
				{
					Group grp = (Group) iter.next();
					if(grp != null)
						groupIds.add(grp.getId());
				}
				perms = getPermissionsForUserForGoupsAnyCategory(gradebookId, userId, groupIds);
				if(perms != null)
				{
					Map studentMapForGroups = filterPermissionForGrader(perms, studentIds, groups);
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
					Map studentMapForGroups = filterPermissionForGrader(perms, studentIds, groups);
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
	
	private Map filterPermissionForGrader(List perms, List studentIds, Collection groups)
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
			try
			{
				//Site currentSite = SiteService.getSite(ToolManager.getCurrentPlacement().getContext()); 
				//Collection groups = currentSite.getGroups();
				Map studentMap = new HashMap();

				if(perms != null)
				{
					for(Iterator iter = studentIds.iterator(); iter.hasNext();)
					{
						String studentId = (String) iter.next();
						for(Iterator groupIter = groups.iterator(); groupIter.hasNext();)
						{
							Group grp = (Group) groupIter.next();
							if(grp.getMember(studentId) != null && permMap.containsKey(grp.getId()))
							{
								if(studentMap.containsKey(studentId) && ((String)studentMap.get(studentId)).equalsIgnoreCase(GradebookService.viewPermission))
								{
									if(((String)permMap.get(grp.getId())).equalsIgnoreCase(GradebookService.gradePermission))
										studentMap.put(studentId, GradebookService.gradePermission);
								}
								else if(!studentMap.containsKey(studentId))
									studentMap.put(studentId, permMap.get(grp.getId()));
							}
						}
					}
				}
				return studentMap;
			}
			catch(Exception e)
			{
				return null;
			}
		}
		else
			return null;
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
			return null;
	}
}
