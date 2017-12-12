/**********************************************************************************
 *
 * $Id: GraderRulesBean.java 20001 2007-04-01 19:41:33Z wagnermr@iupui.edu $
 *
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation, The MIT Corporation
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
package org.sakaiproject.tool.gradebook.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;

import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;

import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.ParticipationRecord;
import org.sakaiproject.section.api.coursemanagement.User;
import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.GraderPermission;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;
import org.sakaiproject.tool.gradebook.Permission;

@Slf4j
public class GraderRulesBean extends GradebookDependentBean implements Serializable
{
	private List graderList;
	private String selectedGraderId;
	private Grader selectedGrader;
	private List rulesToRemove;
	
	private boolean assistantsDefined;
	private boolean categoriesDefined;
	private boolean sectionsDefined;
	
	private List sectionSelectMenu;
	private Map sectionUuidNameMap;

	private Map categoryIdNameMap;
	private List categorySelectMenu;
	
	private Map graderDisplayIdUidMap;
	private Map graderIdSortNameMap;
	private Map graderIdDisplayNameMap;
	private List graderNameSelectMenu;
	
	private static final String NONE = "";
	private static final String ALL = "";
	
	private static final String GRADER_RULES_PG = "graderRules";
	private static final String ROW_INDEX_PARAM = "rowIndex";
	
	private List gradeOrViewMenu;
	
	private boolean refreshView = true;


	protected void init() {
		// first, load data that is non-grader specific. only load first time through
		if (refreshView) {
			rulesToRemove = new ArrayList();
			List taList = getSectionAwareness().getSiteMembersInRole(getGradebookUid(), Role.TA);
			
			if (taList == null || taList.size() <= 0) {
				assistantsDefined = false;
				return;
			}
			
			assistantsDefined = true;
			
			// set up the select menu of grader names and maps for single lookup
			graderDisplayIdUidMap = new HashMap();  // so we don't display Uid in html
			graderIdSortNameMap = new HashMap();
			graderIdDisplayNameMap = new HashMap();
			graderNameSelectMenu = new ArrayList();
			
			graderNameSelectMenu.add(new SelectItem(NONE, FacesUtil.getLocalizedString("grader_rules_select_menu_none")));
			for (Iterator taIter = taList.iterator(); taIter.hasNext();) {
				ParticipationRecord participationRecord = (ParticipationRecord)taIter.next();
				User user = participationRecord.getUser();
				graderDisplayIdUidMap.put(user.getDisplayId(), user.getUserUid());
				graderIdSortNameMap.put(user.getDisplayId(), user.getSortName());
				graderIdDisplayNameMap.put(user.getDisplayId(), user.getDisplayName());
				
				graderNameSelectMenu.add(new SelectItem(user.getDisplayId(), user.getSortName()));
			}
			
			// set up the section information
			sectionUuidNameMap = new HashMap();
			sectionSelectMenu = new ArrayList();
			
			List sectionList = getAllSections();
			sectionsDefined = sectionList != null && sectionList.size() > 0;
			if (sectionsDefined) {
				sectionSelectMenu.add(new SelectItem(ALL, FacesUtil.getLocalizedString("grader_rules_all_section")));
				for (Iterator sectionIter = sectionList.iterator(); sectionIter.hasNext();) {
					CourseSection section = (CourseSection)sectionIter.next();
					sectionUuidNameMap.put(section.getUuid(), section.getTitle());
					sectionSelectMenu.add(new SelectItem(section.getUuid(), section.getTitle()));
				}
			} 
			
			// set up the category information
			categoryIdNameMap = new HashMap();
			categorySelectMenu = new ArrayList();
			if (getCategoriesEnabled()) {
				List categoryList = getGradebookManager().getCategories(getGradebookId());
				categoriesDefined = categoryList != null && categoryList.size() > 0;
				if (categoriesDefined) {
					categorySelectMenu.add(new SelectItem(ALL, FacesUtil.getLocalizedString("grader_rules_all_category")));
					for (Iterator catIter = categoryList.iterator(); catIter.hasNext();) {
						Category category = (Category)catIter.next();
						categoryIdNameMap.put(category.getId(), category.getName());
						categorySelectMenu.add(new SelectItem(category.getId().toString(), category.getName()));
					}
				}
			} else {
				categoriesDefined = false;
			}
			
			// set up grade or view menu
			gradeOrViewMenu = new ArrayList();
			gradeOrViewMenu.add(new SelectItem(GradebookService.viewPermission, FacesUtil.getLocalizedString("grader_rules_view")));
			gradeOrViewMenu.add(new SelectItem(GradebookService.gradePermission, FacesUtil.getLocalizedString("grader_rules_grade")));
			
			refreshView = false;
		}
		
		if (selectedGrader == null) {
			if (selectedGraderId == null || selectedGraderId.equals(NONE) || !graderDisplayIdUidMap.containsKey(selectedGraderId)) {
				selectedGrader = null;
			
			} else {
				selectedGrader = new Grader();
				selectedGrader.setGraderName((String)graderIdDisplayNameMap.get(selectedGraderId));
				selectedGrader.setGraderId(selectedGraderId);
				selectedGrader.setGraderUid((String)graderDisplayIdUidMap.get(selectedGraderId));
				selectedGrader.setGraderRules(getGraderRulesForUser((String)graderDisplayIdUidMap.get(selectedGraderId)));
			}
		} 
	}
		
	private void refreshSelectedGraderRules() {
		selectedGrader.setGraderRules(getGraderRulesForUser(selectedGrader.getGraderUid()));
		rulesToRemove = new ArrayList();
	}
	
	public void setGraderList(List graderList) {
		this.graderList = graderList;
	}
	public List getGraderList() {
		return graderList;
	}
	
	public void setSelectedGraderId(String selectedGraderId) {
		this.selectedGraderId = selectedGraderId;
	}
	public String getSelectedGraderId() {
		return selectedGraderId;
	}
	
	public void setSelectedGrader(Grader selectedGrader) {
		this.selectedGrader = selectedGrader;
	}
	public Grader getSelectedGrader() {
		return selectedGrader;
	}
	
	public void setSectionSelectMenu(List sectionSelectMenu) {
		this.sectionSelectMenu = sectionSelectMenu;
	}
	public List getSectionSelectMenu() {
		return sectionSelectMenu;
	}
	
	public void setCategorySelectMenu(List categorySelectMenu) {
		this.categorySelectMenu = categorySelectMenu;
	}
	public List getCategorySelectMenu() {
		return categorySelectMenu;
	}
	
	public void setGradeOrViewMenu(List gradeOrViewMenu) {
		this.gradeOrViewMenu = gradeOrViewMenu;
	}
	public List getGradeOrViewMenu() {
		return gradeOrViewMenu;
	}
	
	public void setGraderNameSelectMenu(List graderNameSelectMenu) {
		this.graderNameSelectMenu = graderNameSelectMenu;
	}
	public List getGraderNameSelectMenu() {
		return graderNameSelectMenu;
	}
	
	public boolean isAssistantsDefined() {
		return assistantsDefined;
	}
	public boolean isCategoriesDefined() {
		return categoriesDefined;
	}
	public boolean isSectionsDefined() {
		return sectionsDefined;
	}
	public boolean isUserHasRules() {
		if (selectedGrader == null || selectedGrader.getGraderRules() == null || selectedGrader.getGraderRules().size() <= 0)
			return false;
		return true;
	}
	
	public String processSelectGrader(ValueChangeEvent event) {
		try
		{
			String newUserId = (String)event.getNewValue();		
			selectedGraderId = newUserId;
			selectedGrader = null;
		}
		catch(Exception e)
		{
			// do nothing
		}
		
		return GRADER_RULES_PG;
	}
	
	public String processRemoveRule(ActionEvent event) {
		if (selectedGrader == null || selectedGrader.getGraderRules() == null)
			return GRADER_RULES_PG;
	
		try
		{
			Map params = FacesUtil.getEventParameterMap(event);
			Integer index = (Integer) params.get(ROW_INDEX_PARAM);
			if (index == null) {
				return GRADER_RULES_PG;
			}
			int indexToRemove = index.intValue();
			List graderRulesList = selectedGrader.getGraderRules();
			GraderRule ruleToRemove = (GraderRule)graderRulesList.get(indexToRemove);
			if (ruleToRemove != null) {
				Permission permToRemove = ruleToRemove.getPermission();
				if (permToRemove != null) {
					rulesToRemove.add(permToRemove);
				}
				graderRulesList.remove(ruleToRemove);
			}
			
			selectedGrader.setGraderRules(graderRulesList);
		}
		catch(Exception e)
		{
			// do nothing
		}
		
		return GRADER_RULES_PG;
	}
	
	public String processAddRule() {
		if (selectedGrader == null)
			return GRADER_RULES_PG;
		
		List rules = selectedGrader.getGraderRules();
		if (rules == null)
			rules = new ArrayList();
		
		GraderRule newRule = new GraderRule();
		newRule.setGradeOrViewValue(GradebookService.viewPermission);
		
		rules.add(newRule);
		selectedGrader.setGraderRules(rules);
		
		return GRADER_RULES_PG;
	}
	
	/**
	 * 
	 * @param userUid
	 * @return list of GraderRules for this user id
	 */
	private List getGraderRulesForUser(String userUid) {
		List graderRules = new ArrayList();
		if (userUid == null)
			return null;
		
		List currPerms = getGradebookManager().getPermissionsForUser(getGradebookId(), userUid);
		
		if (currPerms == null || currPerms.size() <= 0)  // there are no grader perms for this user
			return null;
		
		for (Iterator permIter = currPerms.iterator(); permIter.hasNext();) {
			/* iterate through each perm to make sure category and section still exist.
			 * if not, delete it from the table to clean things up.
			 */
			Permission permission = (Permission) permIter.next();
			String sectionUuid = permission.getGroupId();
			Long categoryId = permission.getCategoryId();
			
			if ((sectionUuid != null && !sectionUuidNameMap.containsKey(sectionUuid)) || 
					(categoryId != null && !categoryIdNameMap.containsKey(categoryId))) {
				log.info("unknown category or section. Permission " + permission.getId() + " was deleted");
				getGradebookManager().deletePermission(permission);
			} else {
				
				//make compatible with GradebookNG, skip the view course grade permission
				if(StringUtils.equalsIgnoreCase(permission.getFunction(), GraderPermission.VIEW_COURSE_GRADE.toString())){
					continue;
				}
				
				GraderRule graderRule = new GraderRule();
				graderRule.setGradeOrViewValue(permission.getFunction());
				
				if (categoryId == null) {
					graderRule.setSelectedCategoryId(ALL);
				} else {
					graderRule.setSelectedCategoryId(categoryId.toString());
				}
				
				if (sectionUuid == null) {
					graderRule.setSelectedSectionUuid(ALL);
				} else {
					graderRule.setSelectedSectionUuid(sectionUuid);
				} 
				
				graderRule.setPermission(permission);
				
				graderRules.add(graderRule);
			}
		}
		
		return graderRules;
	}
	
	public String processSaveGraderRules() {
		
		if (selectedGrader == null) {
			FacesUtil.addErrorMessage(getLocalizedString("grader_rules_no_grader_selected"));
			return "failure";
		}
			
		// first, remove all marked rules
		boolean updated = false;
		if (rulesToRemove != null) {
			for (Iterator removeIter = rulesToRemove.iterator(); removeIter.hasNext();) {
				Permission permToRemove = (Permission) removeIter.next();
				getGradebookManager().deletePermission(permToRemove);
				updated = true;
			}
		}

		List graderRules = selectedGrader.getGraderRules();
		if (graderRules != null && graderRules.size() > 0) {

			for (Iterator rulesIter = graderRules.iterator(); rulesIter.hasNext();) {
				GraderRule rule = (GraderRule) rulesIter.next();
				
				String selGradeOrViewVal = rule.getGradeOrViewValue();
				String selCategoryId = rule.getSelectedCategoryId();
				String selSectionId = rule.getSelectedSectionUuid();
				
				// check for valid Grade/View selection
				if (selGradeOrViewVal == null || 
					(!selGradeOrViewVal.equals(GradebookService.viewPermission) &&
					 !selGradeOrViewVal.equals(GradebookService.gradePermission))) {
					FacesUtil.addErrorMessage(getLocalizedString("grader_rules_invalid_function"));
					return GRADER_RULES_PG;
				}
				
				// convert category id to a Long
				Long selCatIdAsLong = null;
				if (selCategoryId != null && selCategoryId.length() > 0 && !selCategoryId.equals(ALL)) {
					try {
						selCatIdAsLong = new Long(selCategoryId);
					} catch (NumberFormatException nfe) {
						FacesUtil.addErrorMessage(getLocalizedString("grader_rules_invalid_category"));
						return GRADER_RULES_PG;
					}
				}
				
				// empty string signals "All sections", which is defined by null in db
				if (selSectionId == null || selSectionId.equals(ALL)) {
					selSectionId = null;
				}

				Permission rulePerm = rule.getPermission();
				
				if (rulePerm != null) {
					boolean hasChanged = false;
					Long ruleCategoryId = rulePerm.getCategoryId();
					String ruleSectionId = rulePerm.getGroupId();
					
					if (!rulePerm.getFunction().equals(selGradeOrViewVal))
						hasChanged = true;
					else if (ruleCategoryId != null && selCatIdAsLong != null && !ruleCategoryId.equals(selCatIdAsLong))
						hasChanged = true;
					else if ((ruleCategoryId == null && selCatIdAsLong != null) || (ruleCategoryId != null && selCatIdAsLong == null))
						hasChanged = true;
					else if (ruleSectionId != null && selSectionId != null && !ruleSectionId.equals(selSectionId))
						hasChanged = true;
					else if ((ruleSectionId == null && selSectionId != null) || (ruleSectionId != null && selSectionId == null))
						hasChanged = true;

					if (hasChanged) {
						// there has been a change, so update rec
						rulePerm.setCategoryId(selCatIdAsLong);
						rulePerm.setFunction(rule.getGradeOrViewValue());
						rulePerm.setGroupId(selSectionId);
						getGradebookManager().updatePermission(rulePerm);
						updated = true;
					}
				} else {
					// this is a new rule
					getGradebookManager().addPermission(getGradebookId(), selectedGrader.getGraderUid(), rule.getGradeOrViewValue(), selCatIdAsLong, selSectionId);
					updated = true;
				}
			}
		}

		if (updated) {
			FacesUtil.addRedirectSafeMessage(getLocalizedString("grader_rules_saved"));
			refreshSelectedGraderRules();
			return GRADER_RULES_PG;
		} else {
			FacesUtil.addErrorMessage(getLocalizedString("grader_rules_no_changes"));
			return GRADER_RULES_PG;
		}
	}
	
	public String processCancelChanges() {
		refreshSelectedGraderRules();
		return GRADER_RULES_PG;
	}

	public class Grader implements Serializable {
		private String graderId;
		private String graderUid;
		private String graderName;
		private List graderRules;

		public Grader() {
		}

		public void setGraderId(String graderId) {
			this.graderId = graderId;
		}
		public String getGraderId() {
			return graderId;
		}
		
		public void setGraderUid(String graderUid) {
			this.graderUid = graderUid;
		}
		public String getGraderUid() {
			return graderUid;
		}

		public void setGraderName(String graderName) {
			this.graderName = graderName;
		}
		public String getGraderName() {
			return graderName;
		}
		
		public void setGraderRules(List graderRules) {
			this.graderRules = graderRules;
		}
		public List getGraderRules() {
			return graderRules;
		}	
	}
}
