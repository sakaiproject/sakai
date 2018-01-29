/**
 * Copyright (c) 2003-2015 The Apereo Foundation
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

package org.sakaiproject.tool.gradebook.ui;

import java.text.DateFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;


import org.apache.commons.lang.StringUtils;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.jsf.util.LocaleUtil;
import org.sakaiproject.section.api.SectionAwareness;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.service.gradebook.shared.GradebookPermissionService;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.GradeMapping;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.business.GradebookManager;
import org.sakaiproject.tool.gradebook.business.GradebookScoringAgentManager;
import org.sakaiproject.tool.gradebook.facades.Authn;
import org.sakaiproject.tool.gradebook.facades.UserDirectoryService;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;
import org.sakaiproject.util.ResourceLoader;


public abstract class GradebookDependentBean extends InitializableBean {
	private String pageName;
    
    /** Used by breadcrumb display */
    private String breadcrumbPage;
    private Boolean editing;
    private Boolean adding;
    private Boolean middle;
    private boolean isExistingConflictScale = false;
    
    protected final String BREADCRUMBPAGE = "breadcrumbPage";

	/**
	 * Marked transient to allow serializable subclasses.
	 */
	private transient GradebookBean gradebookBean;
    private transient PreferencesBean preferencesBean;

	/**
	 * Convenience method, for use in calling locally implemented services
	 * that assume the gradebook ID is an integer.
	 */
	Long getGradebookId() {
		return getGradebookBean().getGradebookId();
	}

	/**
	 * Convenience method, for use in calling external facades
	 * that assume the gradebook ID is an string.
	 */
	private transient String gradebookUid;
	String getGradebookUid() {
		if (gradebookUid == null) {
			gradebookUid = getGradebookManager().getGradebookUid(getGradebookId());
		}
		return gradebookUid;
	}

	/**
	 * Convenience method to hide the Authn context object.
	 */
	public String getUserUid() {
		return getAuthnService().getUserUid();
	}

	/**
	 * Convenience method to load the current gradebook object.
	 */
	private transient Gradebook gradebook;
	Gradebook getGradebook() {
		if (gradebook == null) {
			gradebook = getGradebookManager().getGradebook(getGradebookId());
		}
		
		return gradebook;
	}

    /**
     * Gets a localized message string based on the locale determined by the
     * FacesContext.  Useful for adding localized FacesMessages from a backing bean.
     *
     * TODO Replace with direct calls to FacesUtil.
     *
     * @param key The key to look up the localized string
     */
    public String getLocalizedString(String key) {
    	return FacesUtil.getLocalizedString(key);
    }

    /**
     * Gets a localized message string based on the locale determined by the
     * FacesContext.  Useful for adding localized FacesMessages from a backing bean.
     *
     * TODO Replace with direct calls to FacesUtil.
     *
     * @param key The key to look up the localized string
     * @param params The array of strings to use in replacing the placeholders
     * in the localized string
     */
    public String getLocalizedString(String key, String[] params) {
    	return FacesUtil.getLocalizedString(key, params);
    }
    
    /**
     * Gets a localized percent input symbol based on the locale determined by FacesContext.
     */
    public String getLocalizedPercentInput() {
    	Locale locale = LocaleUtil.getLocale(FacesContext.getCurrentInstance());
    	DecimalFormatSymbols dfs = new DecimalFormatSymbols(locale);
    	return String.valueOf(dfs.getPercent());
    }

    // Still more convenience methods, hiding the bean configuration details.

	public GradebookManager getGradebookManager() {
		return getGradebookBean().getGradebookManager();
	}

	public SectionAwareness getSectionAwareness() {
		return getGradebookBean().getSectionAwareness();
	}

	public UserDirectoryService getUserDirectoryService() {
		return getGradebookBean().getUserDirectoryService();
	}

	public Authn getAuthnService() {
		return getGradebookBean().getAuthnService();
	}
	
	public GradebookPermissionService getGradebookPermissionService() {
		return getGradebookBean().getGradebookPermissionService();
	}

	public GradebookExternalAssessmentService getGradebookExternalAssessmentService() {
		return getGradebookBean().getGradebookExternalAssessmentService();
	}
	
	public GradebookScoringAgentManager getScoringAgentManager() {
		return getGradebookBean().getScoringAgentManager();
	}

	// Because these methods are referred to inside "rendered" tag attributes,
	// JSF will call them multiple times in every request. To cut back on
	// business logic traffic, cache them in request scope. They need to be
	// declared transient, however, so that they aren't copied between
	// requests (which would prevent changes in a user's authz status).
	private transient Boolean userAbleToEditAssessments;
	public boolean isUserAbleToEditAssessments() {
		if (userAbleToEditAssessments == null) {
			userAbleToEditAssessments = new Boolean(getGradebookBean().getAuthzService().isUserAbleToEditAssessments(getGradebookUid()));
		}
		return userAbleToEditAssessments.booleanValue();
	}
	private transient Boolean userAbleToGradeAll;
	public boolean isUserAbleToGradeAll() {
		if (userAbleToGradeAll == null) {
			userAbleToGradeAll = new Boolean(getGradebookBean().getAuthzService().isUserAbleToGradeAll(getGradebookUid()));
		}
		return userAbleToGradeAll.booleanValue();
	}

	private transient List viewableSections;
	public List getViewableSections() {
		if (viewableSections == null) {
			viewableSections = getGradebookBean().getAuthzService().getViewableSections(getGradebookUid());	
		}
		
		return viewableSections;
	}
	
	private transient List viewableSectionIds;
	public List getViewableSectionIds() {
		if (viewableSectionIds == null) {
			viewableSectionIds = new ArrayList();
			
			List sectionList = getViewableSections();
			if (sectionList == null || sectionList.isEmpty()) {
				return viewableCategoryIds;
			}
			
			if (!sectionList.isEmpty()) {
				for (Iterator sectionIter = sectionList.iterator(); sectionIter.hasNext();) {
					CourseSection section = (CourseSection) sectionIter.next();
					if (section != null) {
						viewableSectionIds.add(section.getUuid());
					}
				}
			}
		}
		return viewableSectionIds;
	}
	
	private transient Boolean userHasGraderPermissions;
	public boolean isUserHasGraderPermissions() {
		if (userHasGraderPermissions == null) {
			userHasGraderPermissions = new Boolean(getGradebookBean().getAuthzService().isUserHasGraderPermissions(getGradebookId(), getUserUid()));
		}
		
		return userHasGraderPermissions.booleanValue();
	}
	
	private transient Boolean userWithTaFlagExistsInSite;
	public boolean isUserWithTaFlagExistsInSite() {
		if (userWithTaFlagExistsInSite == null) {
			List tas = getSectionAwareness().getSiteMembersInRole(getGradebookUid(), Role.TA);
			userWithTaFlagExistsInSite =  new Boolean(tas != null && tas.size() > 0);
		}
		
		return userWithTaFlagExistsInSite.booleanValue();
	}
	
	private transient Boolean userHasPermissionsForAllItems;
	public boolean isUserHasPermissionsForAllItems() {
		if (userHasPermissionsForAllItems == null) {
			userHasPermissionsForAllItems = new Boolean(getGradebookBean().getGradebookPermissionService().getPermissionForUserForAllAssignment(getGradebookId(), getUserUid()));
		}
		
		return userHasPermissionsForAllItems.booleanValue();
	}
	
	private transient List viewableCategories;
	public List getViewableCategories() {
		if (viewableCategories == null) {
			viewableCategories = new ArrayList();
			
			List categoryList = getGradebookManager().getCategories(getGradebookId());
			if (categoryList == null || categoryList.isEmpty()) {
				return viewableCategories;
			}
			
			if (isUserAbleToGradeAll()) {
				viewableCategories = categoryList;
			} else {
				if (getGradebookBean().getAuthzService().isUserHasGraderPermissions(getGradebookId(), getUserUid())) {
					//SAK-19896, eduservice's can't share the same "Category" class, so just pass the ID's
    					List<Long> catIds = new ArrayList<Long>();
    					for (Category category : (List<Category>) categoryList) {
    						catIds.add(category.getId());
    					}
    					List<Long> viewableCats = getGradebookPermissionService().getCategoriesForUser(getGradebookId(), getUserUid(), catIds);
    					List<Category> viewableCategories = new ArrayList<Category>();
    					for (Category category : (List<Category>) categoryList) {
    						if(viewableCats.contains(category.getId())){
    							viewableCategories.add(category);
    						}
    					}
				} else {
					viewableCategories = categoryList;
				}
			}
		}
		return viewableCategories;
	}
	
	private transient List viewableCategoryIds;
	public List getViewableCategoryIds() {
		if (viewableCategoryIds == null) {
			viewableCategoryIds = new ArrayList();
			
			List categoryList = getViewableCategories();
			if (categoryList == null || categoryList.isEmpty()) {
				return viewableCategoryIds;
			}
			
			if (!categoryList.isEmpty()) {
				for (Iterator catIter = categoryList.iterator(); catIter.hasNext();) {
					Category category = (Category) catIter.next();
					if (category != null) {
						viewableCategoryIds.add(category.getId());
					}
				}
			}
		}
		return viewableCategories;
	}
	
	
	public Map findMatchingEnrollmentsForItem(Long categoryId, String optionalSearchString, String optionalSectionUid) {
		return getGradebookBean().getAuthzService().findMatchingEnrollmentsForItem(getGradebookUid(), categoryId, getGradebook().getCategory_type(), optionalSearchString, optionalSectionUid);
	}
	
	public Map findMatchingEnrollmentsForAllItems(String optionalSearchString, String optionalSectionUid) {
		return getGradebookBean().getAuthzService().findMatchingEnrollmentsForViewableItems(getGradebookUid(), 
				getGradebookManager().getAssignments(getGradebookId()), optionalSearchString, optionalSectionUid);
	}
	
	public Map findMatchingEnrollmentsForViewableCourseGrade(String optionalSearchString, String optionalSectionUid) {
		return getGradebookBean().getAuthzService().findMatchingEnrollmentsForViewableCourseGrade(getGradebookUid(), getGradebook().getCategory_type(), optionalSearchString, optionalSectionUid);
	}
	
	public List getAllSections() {
		return getGradebookBean().getAuthzService().getAllSections(getGradebookUid());
	}

	/**
	 * Get the gradebook context.
	 */
	public GradebookBean getGradebookBean() {
		if (gradebookBean == null) {
			// This probably happened because gradebookBean is transient.
			// Just restore it from the session context.
			setGradebookBean((GradebookBean)FacesUtil.resolveVariable("gradebookBean"));
		}
		return gradebookBean;
	}

	/**
	 * Set the gradebook context.
	 */
	public void setGradebookBean(GradebookBean gradebookBean) {
		this.gradebookBean = gradebookBean;
	}

    /**
     * @return Returns the preferencesBean.
     */
    public PreferencesBean getPreferencesBean() {
        if (preferencesBean == null) {
            setPreferencesBean((PreferencesBean)FacesUtil.resolveVariable("preferencesBean"));
        }
        return preferencesBean;
    }
    /**
     * @param preferencesBean The preferencesBean to set.
     */
    public void setPreferencesBean(PreferencesBean preferencesBean) {
        this.preferencesBean = preferencesBean;
    }

    /**
     * Set up close relations with page and action names for easier control
     * of menus.
     */
    public String getPageName() {
    	return pageName;
    }
    public void setPageName(String pageName) {
    	this.pageName = pageName;
    }

    /**
     * Saves state for menu and breadcrumb trail. Pass in NULL to keep
     * current value.
     *  
     * @param breadcrumbPage
     * 			Top level page to return to
     * @param editing
     * 			If navigating TO edit page.
     * @param adding
     * 			If navigating TO add page.
     * @param middle
     * 			If navigating TO 2nd level down.
     * @param fromPage
     * 			currently, when navigating from details page down.
     */
    public void setNav(String breadcrumbPage, String editing, String adding, String middle,
    		 				String fromPage) {
 		final ToolSession session = SessionManager.getCurrentToolSession();
		
 		if (breadcrumbPage != null) session.setAttribute(BREADCRUMBPAGE, breadcrumbPage);
 		
 		if (editing != null) session.setAttribute("editing", editing);
 		
 		if (adding != null) session.setAttribute("adding", adding);
 		
 		if (middle != null)	session.setAttribute("middle", middle);
		
		if (fromPage != null) session.setAttribute("fromPage", fromPage);
    }

    /**
	 * Used to determine where details page called from
	 */
	public String getBreadcrumbPage() {
		if (breadcrumbPage == null) {
			breadcrumbPage = (String) SessionManager.getCurrentToolSession().getAttribute(BREADCRUMBPAGE);
			return breadcrumbPage;
		}
		else {
			return breadcrumbPage;
		}
	}

	/**
	 * Used to set where details page called from
	 */
	public void setBreadcrumbPage(String breadcrumbPage) {
		this.breadcrumbPage = breadcrumbPage;
	}

	/**
	 * In IE (but not Mozilla/Firefox) empty request parameters may be returned
	 * to JSF as the string "null". JSF always "restores" some idea of the
	 * last view, even if that idea is always going to be null because a redirect
	 * has occurred. Put these two things together, and you end up with
	 * a class cast exception when redirecting from this request-scoped
	 * bean to a static page.
	 */
	public void setBreadcrumbPageParam(String breadcrumbPageParam) {
		if (SessionManager.getCurrentToolSession().getAttribute(BREADCRUMBPAGE) != null) {
			if ((breadcrumbPageParam != null) && !breadcrumbPageParam.equals("null")) {
				setBreadcrumbPage(breadcrumbPageParam);
				if (!"".equals(breadcrumbPageParam)) SessionManager.getCurrentToolSession().setAttribute(BREADCRUMBPAGE, breadcrumbPageParam);
			}
			else {
				ToolSession session = SessionManager.getCurrentToolSession();
				final String fromPage = (String) session.getAttribute(BREADCRUMBPAGE);
				
				if (fromPage != null) {
					setBreadcrumbPage(fromPage);
				}
			}
		}
	}

	/**
     * Return if breadcrumb will display 'Edit' piece
     */
    public Boolean getEditing() {
   		return new Boolean((String) SessionManager.getCurrentToolSession().getAttribute("editing"));
    }
    
    /**
     * Return if breadcrumb will display 'Add' piece
     */
    public Boolean getAdding() {
    	if (adding == null) {
    		final ToolSession session = SessionManager.getCurrentToolSession();
    		adding = new Boolean((String) session.getAttribute("adding"));
    	}
    	
    	return adding;
    }
    
	/**
     * Return if breadcrumb trail needs to display the middle section
     */
    public Boolean getMiddle() {
		if (middle == null) {
			final ToolSession session = SessionManager.getCurrentToolSession();
			middle = new Boolean((String) session.getAttribute("middle"));
		}
		
    	return middle;
    }
    
  	/**
     * Generates a default filename (minus the extension) for a download from this Gradebook. 
     *
	 * @param   prefix for filename
	 * @return The appropriate filename for the export
	 */
    public String getDownloadFileName(String prefix) {
		Date now = new Date();
		DateFormat df = DateFormat.getDateInstance( DateFormat.SHORT, (new ResourceLoader()).getLocale() ); 
		StringBuilder fileName = new StringBuilder(prefix);
        String gbName = getGradebook().getName();
        if(StringUtils.trimToNull(gbName) != null) {
            gbName = gbName.replaceAll("\\s", "_"); // replace whitespace with '_'
            fileName.append("-");
            fileName.append(gbName);
        }
		fileName.append("-");
		fileName.append(df.format(now));
		return fileName.toString();
	}


    /**
     * 
     * @return
     */
    public String getAuthzLevel(){
         return (getGradebookBean().getAuthzService().isUserAbleToGradeAll(getGradebookUid())) ?"instructor" : "TA";
    }
    
    /**
     * Returns whether the gb has enabled categories (with or without weighting)
     */
    private transient Boolean categoriesEnabled;
    public boolean getCategoriesEnabled() {
    	if (categoriesEnabled == null)
    		categoriesEnabled = new Boolean(getGradebook().getCategory_type() != GradebookService.CATEGORY_TYPE_NO_CATEGORY);
    	
    	return categoriesEnabled.booleanValue();
    }
    
    /**
     * Returns whether the gb has enabled weighting
     */
    private transient Boolean weightingEnabled;
    public boolean getWeightingEnabled() {
    	if (weightingEnabled == null)
    		weightingEnabled = new Boolean(getGradebook().getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY);
    	
    	return weightingEnabled.booleanValue();
    }
    
    /**
     * Returns whether the gb grade entry is by points
     */
    private transient Boolean gradeEntryByPoints;
    public boolean getGradeEntryByPoints() {
    	if (gradeEntryByPoints == null)
    		gradeEntryByPoints = new Boolean(getGradebook().getGrade_type() == GradebookService.GRADE_TYPE_POINTS);
    	
    	return gradeEntryByPoints.booleanValue();
    }
    
    /**
     * Returns whether the gb grade entry is by percentage
     */
    private transient Boolean gradeEntryByPercent;
    public boolean getGradeEntryByPercent() {
    	if (gradeEntryByPercent == null)
    		gradeEntryByPercent = new Boolean(getGradebook().getGrade_type() == GradebookService.GRADE_TYPE_PERCENTAGE);
    	
    	return gradeEntryByPercent.booleanValue();
    }
    
    /**
     * Returns whether the gb grade entry is by letter
     */
    private transient Boolean gradeEntryByLetter;
    public boolean getGradeEntryByLetter() {
    	if (gradeEntryByLetter == null)
    		gradeEntryByLetter = new Boolean(getGradebook().getGrade_type() == GradebookService.GRADE_TYPE_LETTER);
    	
    	return gradeEntryByLetter.booleanValue();
    }
    
    private transient Boolean scoringAgentEnabled;
    /**
     * 
     * @return true if a ScoringAgent has been enabled
     * for this gradebook to allow scoring via an external
     * scoring service
     */
    public boolean isScoringAgentEnabled() {
    	if (scoringAgentEnabled == null) {
    		scoringAgentEnabled = getGradebookBean().getScoringAgentManager()
    				.isScoringAgentEnabledForGradebook(getGradebookUid());
    	}
    	
    	return scoringAgentEnabled;
    }

	/**
	 * Set proper text for navigation button on assignment detials and
	 * instructor view pages.
	 */
	public String getReturnString() {
		final String breadcrumbPage = getBreadcrumbPage();
		if (breadcrumbPage != null && !"".equals(breadcrumbPage)) {
			return ("overview".equals(breadcrumbPage)) ? getLocalizedString("assignment_details_return_to_overview")
													   : getLocalizedString("assignment_details_return_to_roster");
		}
		else {
			final String where = (String) SessionManager.getCurrentToolSession().getAttribute("fromPage");
			return ("overview".equals(where)) ? getLocalizedString("assignment_details_return_to_overview")
					  						  : getLocalizedString("assignment_details_return_to_roster");
		}
	}

	/**
	 * Return fromPage property within tool session - used for breadcrumb trail.
	 */
	public String getFromPage() {
		final String fp = (String) SessionManager.getCurrentToolSession().getAttribute("fromPage");
		
		return fp;
	}
	
    /**
     * Return back to overview page. State is kept in
     * tool session, hence attribute setting.
     */
    public String navigateToOverview() {
    	setNav("overview", "false", "false", "false", "");

		return "overview";
    }
    
    /**
     * Go to roster page. State is kept in
     * tool session, hence attribute setting.
     */
    public String navigateToRoster() {
    	setNav("roster", "false", "false", "false", "");

		return "roster";
   }    

    /**
     * Go to edit assignment page. State is kept in
     * tool session, hence attribute setting.
     */
	public String navigateToEdit() {
		setNav(null, "true", "false", "true", null);
		
		return "editAssignment";
	}

	/**
	 * Go to gradebook setup. State is kept in
	 * tool session, hence attribute setting.
	 */
	public String navigateToGradebookSetup() {
		setNav("other","false","false","false","");
		
		return "gradebookSetup";
	}
	
	/**
	 * Go to permissions page. State is kept in
	 * tool session, hence attribute setting.
	 */
	public String navigateToPermissionSettings() {
		setNav("other","false","false","false","");
		
		return "graderRules";
	}
	
	/**
	 * Go to gradebook course grade setup. State is kept in
	 * tool session, hence attribute setting.
	 */
	public String navigateToFeedbackOptions() {
		setNav("other","false","false","false","");
		
		return "feedbackOptions";
	}
	
	/**
	 * Go to spreadsheet (csv) export/bulk import. State is kept in
	 * tool session, hence attribute setting.
	 */
	public String navigateToImportGrades() {
		setNav("other","false","false","false","");
		
		return "spreadsheetAll";
	}
	
	/**
	 * Go to course grade details pg. State is kept in
	 * tool session, hence attribute setting.
	 */
	public String navigateToCourseGrades() {
		setNav("other","false","false","false","");
		
		return "courseGradeDetails";
	}

	/** 
	 * Determine where to return to. Used by both Assignmenet Details and
	 * Instructor View pages, so put here in super class. 
	 */
	public String processCancel() {
		final String breadcrumbPage = getBreadcrumbPage();
		if (breadcrumbPage != null && !"".equals(breadcrumbPage)) {
			return breadcrumbPage;
		}
		else {
			String where = (String) SessionManager.getCurrentToolSession().getAttribute(BREADCRUMBPAGE);
			
			if ("assignmentDetails".equals(where)) {
				where = (String) SessionManager.getCurrentToolSession().getAttribute("fromPage");
				SessionManager.getCurrentToolSession().removeAttribute("fromPage");
			}

			return where;
		}
	}
	
	/**
	 * We can't rely on the converters to properly display 2 decimals for us,
	 * b/c setMaxFractionDigits rounds 
	 * @param score
	 * @return
	 */
	public Double truncateScore(Double score) {
		if (score == null)
			return null;
		
		return new Double(FacesUtil.getRoundDown(score.doubleValue(), 2));	
	}

	public boolean getIsExistingConflictScale()
	{
		isExistingConflictScale = true;
		Gradebook gb = getGradebookManager().getGradebookWithGradeMappings(getGradebookId());
		if(gb != null && gb.getGrade_type() == GradebookService.GRADE_TYPE_LETTER)
		{
			if((gb.getSelectedGradeMapping().getGradingScale() != null && gb.getSelectedGradeMapping().getGradingScale().getUid().equals("LetterGradeMapping"))
					|| (gb.getSelectedGradeMapping().getGradingScale() == null && gb.getSelectedGradeMapping().getName().equals("Letter Grades")))
			{
				isExistingConflictScale = false;
				return isExistingConflictScale;
			}
			Set mappings = gb.getGradeMappings();
			for(Iterator iter = mappings.iterator(); iter.hasNext();)
			{
				GradeMapping gm = (GradeMapping) iter.next();
				if(gm != null)
				{
					if((gm.getGradingScale() != null && gm.getGradingScale().getUid().equals("LetterGradePlusMinusMapping"))
							|| (gm.getGradingScale() == null && gm.getName().equals("Letter Grades with +/-")))
					{
						Map defaultMapping = gm.getDefaultBottomPercents();
						for (Iterator gradeIter = gm.getGrades().iterator(); gradeIter.hasNext(); ) 
						{
							String grade = (String)gradeIter.next();
							Double percentage = (Double)gm.getValue(grade);
							Double defautPercentage = (Double)defaultMapping.get(grade);
							if (percentage != null && !percentage.equals(defautPercentage)) 
							{
								isExistingConflictScale = false;
								break;
							}
						}
					}
				}
			}
		}
		return isExistingConflictScale;
	}

	public void setIsExistingConflictScale(boolean isExistingConflictScale)
	{
		this.isExistingConflictScale = isExistingConflictScale;
	}
	
	/**
	 * 
	 * @param gradebookUid
	 * @param gradebookItemId Optional - if null, will only populate the data related to
	 * the ScoringAgent itself, ie name and image ref
	 * @param studentUid Optional - if not null, will populate fields related to this student's data
	 * @return a {@link ScoringAgentData} object representing the scoring data
	 * associated with the given gradebookUid and optionally gradebookItemId or studentUid
	 */
	public ScoringAgentData initializeScoringAgentData(String gradebookUid, Long gradebookItemId, String studentUid) {
		ScoringAgentData data = new ScoringAgentData();
		if (isScoringAgentEnabled()) {
			data.setScoringAgentName(getScoringAgentManager().getScoringAgentName());
			data.setScoringAgentImageRef(getScoringAgentManager().getScoringAgentImageRef());
			data.setScoringComponentUrl(getScoringAgentManager().getScoringComponentUrl(gradebookUid, gradebookItemId));
			
			if (gradebookItemId != null) {
				boolean scoringComponentEnabled = getScoringAgentManager().isScoringComponentEnabledForGbItem(gradebookUid, gradebookItemId);
				data.setScoringComponentEnabled(scoringComponentEnabled);
				
				if (scoringComponentEnabled) {
					data.setScoringComponentName(getScoringAgentManager().getScoringComponentName(gradebookUid, gradebookItemId));
					data.setScoringComponentUrl(getScoringAgentManager().getScoringComponentUrl(gradebookUid, gradebookItemId));
					data.setScoreAllUrl(getScoringAgentManager().getScoreAllUrl(gradebookUid, gradebookItemId));
					data.setRetrieveScoresUrl(getScoringAgentManager().getScoresUrl(gradebookUid, gradebookItemId));
				}
			}
			
			if (studentUid != null) {
				data.setRetrieveStudentScoresUrl(getScoringAgentManager().getStudentScoresUrl(gradebookUid, studentUid));
			}
			
			// There are several places in the UI where the text requires parameterized bundle
			// references, but the JSF component does not allow parameters. We will build
			// them here instead. For example, alt tags on the image
			data.setSelectScoringComponentText(getLocalizedString("selectScoringComponent", new String[]{data.getScoringAgentName()}));
			data.setViewWithScoringAgentText(getLocalizedString("viewWithScoringAgent", new String[]{data.getScoringAgentName()}));
			data.setGradeWithScoringAgentText(getLocalizedString("gradeWithScoringAgent", new String[]{data.getScoringAgentName()}));
			data.setGradeAllWithScoringAgentText(getLocalizedString("gradeAllWithScoringAgent", new String[]{data.getScoringAgentName()}));
			data.setRefreshAllGradesText(getLocalizedString("refreshAllGrades", new String[]{data.getScoringAgentName()}));
			data.setRefreshGradeText(getLocalizedString("refreshGrade", new String[]{data.getScoringAgentName()}));
		}
		
		return data;
	}
	
	/* Get a property that many beans need relating to whether or not to showCoursePoints feature */ 
    public boolean getShowCoursePoints() {
        String showCoursePoints = ServerConfigurationService.getString("gradebook.showCoursePoints", "false");
        return Boolean.parseBoolean(showCoursePoints);
    }
    
}
