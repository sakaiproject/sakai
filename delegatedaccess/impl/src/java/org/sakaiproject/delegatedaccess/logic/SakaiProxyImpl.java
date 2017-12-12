/*
* The Trustees of Columbia University in the City of New York
* licenses this file to you under the Educational Community License,
* Version 2.0 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of the
* License at:
*
* http://opensource.org/licenses/ecl2.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.delegatedaccess.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.RoleAlreadyDefinedException;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.coursemanagement.api.AcademicSession;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.delegatedaccess.dao.DelegatedAccessDao;
import org.sakaiproject.delegatedaccess.util.DelegatedAccessConstants;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SortType;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * Implementation of our SakaiProxy API
 * 
 * @author Bryan Holladay (holladay@longsight.com)
 *
 */
@Slf4j
public class SakaiProxyImpl implements SakaiProxy {
	@Getter @Setter
	private AuthzGroupService authzGroupService;

	@Getter @Setter
	private SessionManager sessionManager;

	@Getter @Setter
	private UserDirectoryService userDirectoryService;

	@Getter @Setter
	private SecurityService securityService;

	@Getter @Setter
	private EventTrackingService eventTrackingService;

	@Getter @Setter
	private ServerConfigurationService serverConfigurationService;

	@Getter @Setter
	private SiteService siteService;

	@Getter @Setter
	private EntityManager entityManager;

	@Getter @Setter
	private ToolManager toolManager;
	
	@Getter @Setter
	private DelegatedAccessDao dao;
	
	@Getter @Setter
	private CourseManagementService cms;

	@Getter @Setter
	private EmailService emailService;

	//cached variables:
	private List<String[]> terms;
	
	/**
	 * init - perform any actions required here for when this bean starts up
	 */
	public void init() {
	}


	/**
	 * {@inheritDoc}
	 */
	public String getCurrentUserId() {
		return sessionManager.getCurrentSessionUserId();
	}


	/**
	 * {@inheritDoc}
	 */
	public boolean isSuperUser() {
		return securityService.isSuperUser();
	}

	/**
	 * {@inheritDoc}
	 */
	public Session getCurrentSession(){
		return sessionManager.getCurrentSession();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Site> getAllSites(){
		return siteService.getSites(SelectionType.NON_USER, null, null, null, null, null);
	}
	
	/**
    * {@inheritDoc}
    */
   public List<Site> getAllSitesByPages(Map<String, String> propsMap, int page, int pageMax, boolean orderByModifiedDate){
      PagingPosition pp = new PagingPosition(page, pageMax);
      return siteService.getSites(SelectionType.NON_USER, null, null, propsMap, orderByModifiedDate ? SortType.MODIFIED_ON_DESC : null, pp);
   }


	/**
	 * {@inheritDoc}
	 */
	public void postEvent(String event,String reference,boolean modify) {
		eventTrackingService.post(eventTrackingService.newEvent(event,reference,modify));
	}

	public Set<Tool> getAllTools(){
		Set<Tool> toolSet = new HashSet<Tool>();
		String[] toolsList = null;
		String siteType = serverConfigurationService.getString(DelegatedAccessConstants.PROP_TOOL_LIST_TEMPLATE);
		if(siteType != null && !"".equals(siteType)){
			toolSet = toolManager.findTools(new HashSet<String>(Arrays.asList(siteType)), null);
		}
		if(toolSet.size() == 0){
			toolsList = serverConfigurationService.getStrings(DelegatedAccessConstants.PROP_TOOL_LIST);
			if(toolsList != null && toolsList.length > 0){
				for(String toolId : toolsList){
					Tool tool = toolManager.getTool(toolId);
					if(tool != null){
						toolSet.add(tool);
					}
				}
			}
		}
		if(toolSet.size() == 0){
			toolSet = toolManager.findTools(new HashSet<String>(), null);
		}
		//exclude tools
		String[] excludedTools = serverConfigurationService.getStrings(DelegatedAccessConstants.PROP_TOOL_LIST_EXCLUDE);		
		if(excludedTools != null && excludedTools.length > 0){
			for(String excludedTool : excludedTools){
				for (Iterator iterator = toolSet.iterator(); iterator.hasNext();) {
					Tool tool = (Tool) iterator.next();
					if(excludedTool.equals(tool.getId())){
						iterator.remove();
						break;
					}
				}
			}
		}
		
		return toolSet;
	}
	
	public Tool getTool(String toolId){
		return toolManager.getTool(toolId);
	}
	
	public String[] getHomeTools(){
		return serverConfigurationService.getStrings(DelegatedAccessConstants.PROPERTIES_HOME_TOOLS);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getSkinRepoProperty(){
		return serverConfigurationService.getString("skin.repo");
	}

	/**
	 * {@inheritDoc}
	 */
	public String getToolSkinCSS(String skinRepo){

		String skin = siteService.findTool(sessionManager.getCurrentToolSession().getPlacementId()).getSkin();			

		if(skin == null) {
			skin = serverConfigurationService.getString("skin.default");
		}

		return skinRepo + "/" + skin + "/tool.css";
	}

	/**
	 * {@inheritDoc}
	 */
	public List<User> searchUsers(String search) {
		List<User> returnList = new ArrayList<User>();
		returnList.addAll(userDirectoryService.searchExternalUsers(search, -1, -1));
		returnList.addAll(userDirectoryService.searchUsers(search, 1, Integer.MAX_VALUE));
		return returnList;
	}

	/**
	 * {@inheritDoc}
	 */
	public User getUser(String id){
		try {
			return userDirectoryService.getUser(id);
		} catch (UserNotDefinedException e) {
			return null;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public User getUserByEid(String eid){
		try {
			return userDirectoryService.getUserByEid(eid);
		} catch (UserNotDefinedException e) {
			return null;
		}
	}
	/**
	 * {@inheritDoc}
	 */
	public Site getSiteByRef(String siteRef){
		Site site = null;
		Reference r = entityManager.newReference(siteRef);
		if(r.getType().equals(SiteService.APPLICATION_ID)){
			try {
				site = siteService.getSite(r.getId());
			} catch (IdUnusedException e) {
				log.error(e.getMessage(), e);
			}
		}
		return site;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Site getSiteById(String siteId){
		Site site = null;
		try {
			site = siteService.getSite(siteId);
		} catch (IdUnusedException e) {
		}
		return site;
	}

	/**
	 * {@inheritDoc}
	 */
	public void saveSite(Site site){
		SecurityAdvisor yesMan = new SecurityAdvisor() {
			public SecurityAdvice isAllowed(String userId, String function, String reference) {
				return SecurityAdvice.ALLOWED;
			}
		};
		try {
			securityService.pushAdvisor(yesMan);
			siteService.save(site);
		} catch (IdUnusedException e) {
			log.error(e.getMessage(), e);
		} catch (PermissionException e) {
			log.error(e.getMessage(), e);
		}finally{
			securityService.popAdvisor(yesMan);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getRootName(){
		return serverConfigurationService.getString(
				DelegatedAccessConstants.HIERARCHY_ROOT_TITLE_PROPERTY,
				serverConfigurationService.getString("ui.service",
						DelegatedAccessConstants.HIERARCHY_ROOT_TITLE_DEFAULT));
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getServerConfigurationStrings(String property){
		return serverConfigurationService.getStrings(property);
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<String, List<String>> getSiteTemplates(){
		Map<String, List<String>> returnList = new HashMap<String, List<String>>();
		for(AuthzGroup group : authzGroupService.getAuthzGroups("!site.", new PagingPosition(1, DelegatedAccessConstants.SEARCH_RESULTS_MAX))){
			returnList.put(group.getId(), getFilteredRoles(group, null));
		}
		return returnList;
	}

	public Map<String, List<String>> getShoppingRealmOptions(){
		String[] authzGroups = serverConfigurationService.getStrings(DelegatedAccessConstants.PROPERTIES_REALM_OPTIONS_SHOPPING);
		if(authzGroups != null && authzGroups.length != 0){
			String[] filterRoles = serverConfigurationService.getStrings(DelegatedAccessConstants.PROPERTIES_ROLE_OPTIONS_SHOPPING);
			return getGroupsById(authzGroups, filterRoles);
		}else{
			return getSiteTemplates();
		}
	}
	
	public Map<String, List<String>> getDelegatedAccessRealmOptions(){
		String[] authzGroups = serverConfigurationService.getStrings(DelegatedAccessConstants.PROPERTIES_REALM_OPTIONS_ACCESS);
		if(authzGroups != null && authzGroups.length != 0){
			String[] filterRoles = serverConfigurationService.getStrings(DelegatedAccessConstants.PROPERTIES_ROLE_OPTIONS_ACCESS);
			return getGroupsById(authzGroups, filterRoles);
		}else{
			return getSiteTemplates();
		}
	}
	
	private Map<String, List<String>> getGroupsById(String[] groups, String[] filterRoles){
		Map<String, List<String>> returnList = new HashMap<String, List<String>>();
		for(int i = 0; i < groups.length; i++){
			try {
				AuthzGroup group = authzGroupService.getAuthzGroup(groups[i]);
				if(group != null){
					returnList.put(group.getId(), getFilteredRoles(group, filterRoles));
				}
			} catch (GroupNotDefinedException e) {
				log.error(e.getMessage(), e);
			}
		}
		return returnList;
	}
	/**
	 * this will remove any role that isn't in the list of roles passed in.
	 * If roles is null or empty, it will not filter any roles
	 * 
	 * @param groups
	 * @param roles
	 * @return
	 */
	private List<String> getFilteredRoles(AuthzGroup group, String[] filterRoles){
		List<String> returnRoles = new ArrayList<String>();
		if(group != null){
			for(Role role : group.getRoles()){
				if(filterRoles != null){
					for(int i = 0; i < filterRoles.length; i++){
						if(role.getId().equals(filterRoles[i])){
							returnRoles.add(role.getId());
							break;
						}
					}
				}else{
					returnRoles.add(role.getId());
				}
			}
		}
		return returnRoles;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void refreshCurrentUserAuthz(){
		authzGroupService.refreshUser(getCurrentUserId());
	}

	public Set<String> getUserMembershipForCurrentUser(){
		Set<String> returnSet = new HashSet<String>();
		for(Site site: siteService.getSites(SelectionType.ACCESS, null, null, null, SortType.NONE, null)){
			returnSet.add(site.getReference());
		}
		return returnSet;
	}
	
	public List<Site> getSites(SelectionType type, String search, Map<String, String> propsMap){
		return siteService.getSites(type, null, search, propsMap, SortType.NONE, null);
	}
	
	public boolean isShoppingTool(){
		return "sakai.delegatedaccess.shopping".equals(toolManager.getCurrentPlacement().getToolId());
	}
	
	/**
	 * {@inheritDoc}
	 */
	public AuthzGroup getAuthzGroup(String siteId){
		AuthzGroup group = null;
		try {
			group = authzGroupService.getAuthzGroup(siteId);
		} catch (GroupNotDefinedException e) {
			log.error(e.getMessage(), e);
		}
		return group;
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeRoleFromAuthzGroup(AuthzGroup group, Role role) {
		// Cheating to become admin in order to modify authz groups
		SecurityAdvisor yesMan = new SecurityAdvisor() {
			public SecurityAdvice isAllowed(String userId, String function, String reference) {
				return SecurityAdvice.ALLOWED;
			}
		};
		try {
			group.removeRole(role.getId());
			securityService.pushAdvisor(yesMan);
			authzGroupService.save(group);
		} catch (AuthzPermissionException e) {
			log.error(e.getMessage(), e);
		} catch (GroupNotDefinedException e) {
			log.error(e.getMessage(), e);
		} finally {
			securityService.popAdvisor(yesMan);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void copyNewRole(String siteRef, String copyRealm, String copyRole, String newRole){
		// Cheating to become admin in order to modify authz groups
		SecurityAdvisor yesMan = new SecurityAdvisor() {
			public SecurityAdvice isAllowed(String userId, String function, String reference) {
				return SecurityAdvice.ALLOWED;
			}
		};

		try {
			// become admin
			securityService.pushAdvisor(yesMan);

			// Get the source realm and role
			AuthzGroup sourceGroup = authzGroupService.getAuthzGroup(copyRealm);
			Role copyFromRole = sourceGroup.getRole(copyRole);

			// Copy the role to the dest role 
			AuthzGroup destGroup = authzGroupService.getAuthzGroup(siteRef);
			destGroup.removeRole(newRole);
			destGroup.addRole(newRole, copyFromRole);
			authzGroupService.save(destGroup);

		} catch (RoleAlreadyDefinedException e) {
			log.error(e.getMessage(), e); // wtf?
		} catch (GroupNotDefinedException e) {
			log.error(e.getMessage(), e);
		} catch (AuthzPermissionException e) {
			log.error(e.getMessage(), e);
		} finally {
			securityService.popAdvisor(yesMan);
		}
	}

	public SecurityAdvisor addSiteUpdateSecurityAdvisor(){
		// Cheating to become admin in order to modify authz groups
		SecurityAdvisor yesMan = new SecurityAdvisor() {
			public SecurityAdvice isAllowed(String userId, String function, String reference) {
				if("site.upd".equals(function))
					return SecurityAdvice.ALLOWED;

				return SecurityAdvice.PASS;
			}
		};
		securityService.pushAdvisor(yesMan);
		return yesMan;
	}

	public void popSecurityAdvisor(SecurityAdvisor advisor){
		securityService.popAdvisor(advisor);
	}
	
	public String getTermField(){
		return serverConfigurationService.getString(DelegatedAccessConstants.PROPERTIES_TERMFIELD, "term_eid");
	}
	
	public boolean useCourseManagementApiForTerms(){
		return serverConfigurationService.getBoolean(DelegatedAccessConstants.PROPERTIES_TERM_USE_CM_API, true);
	}
	
	public int showLatestXTerms(){
		return serverConfigurationService.getInt(DelegatedAccessConstants.PROPERTIES_TERM_SHOW_LATEST_X_TERMS, -1);
	}
	
	
	public List<String[]> getTerms(){
		if(terms == null){
			terms = new ArrayList<String[]>();
			if(!useCourseManagementApiForTerms()){
				//user has set sakai.properties to override the coursemanagement API
				List<String> termsList = dao.getDistinctSiteTerms(getTermField());
				//check for term order if exists:
				String[] termOrder = serverConfigurationService.getStrings("portal.term.order");
				List<String> termOrderList = new ArrayList<String>();
				if(termOrder != null && termOrder.length > 0){

					Collections.addAll(termOrderList, termOrder);
					for(String term : termOrderList){
						if(termsList.contains(term)){
							terms.add(new String[]{term, term});
						}
					}
				}
				//add the remaining (non ordered) termsho
				for(String term : termsList){
					if(termOrderList == null || termOrderList.size() == 0 || !termOrderList.contains(term))
						terms.add(new String[]{term, term});
				}
			}else{
				//use sakai's coursemanagement API to get the term options
				List<AcademicSession> academicSessions = cms.getAcademicSessions();
				if(academicSessions != null){
					Collections.sort(academicSessions, new Comparator<AcademicSession>() {
						@Override
						public int compare(AcademicSession o1, AcademicSession o2) {
							if(o1.getStartDate() == null){
								return -1;
							}else if(o2.getStartDate() == null){
								return 1;
							}else{
								return o2.getStartDate().compareTo(o1.getStartDate());
							}
						}
						
					});
					for(AcademicSession session : academicSessions){
						String termId = session.getEid();
						if(!"term_eid".equals(getTermField())){
							termId = session.getTitle();
						}
						terms.add(new String[]{termId, session.getTitle()});
					}
				}
			}
			
			//if there is a setting to only show the last X terms, 
			int showLatestXTerms = showLatestXTerms();
			if(showLatestXTerms != -1){
				//make sure the showLastXTerms doesn't do an IndexOutOfBounds
				showLatestXTerms = showLatestXTerms < terms.size() ? showLatestXTerms : terms.size();
				terms = terms.subList(0, showLatestXTerms);
			}
		}
		return terms;
	}
	
	public void sendEmail(String subject, String body){
		String toAddress = serverConfigurationService.getString(DelegatedAccessConstants.PROPERTIES_EMAIL_ERRORS);
		String fromAddress = toAddress;
		if(toAddress != null && !"".equals(toAddress)){
			emailService.send(fromAddress, toAddress, subject, body, null, null, null);
		}
	}
	
	public boolean getDisableUserTreeView(){
		return serverConfigurationService.getBoolean(DelegatedAccessConstants.PROP_DISABLE_USER_TREE_VIEW, false);
	}
	
	public boolean getDisableShoppingTreeView(){
		return serverConfigurationService.getBoolean(DelegatedAccessConstants.PROP_DISABLE_SHOPPING_TREE_VIEW, false);
	}
	
	public boolean isUserInstructor(String userId, String siteId){
		Site site = getSiteById(siteId);
		return isUserInstructor(userId, site);
	}
	
	private boolean isUserInstructor(String userId, Site site){
		if(site != null){
			if(site.getMember(userId) != null){
				if(securityService.unlock(userId, "site.upd", siteService.siteReference(site.getId()))){
					return true;
				}
			}
		}
		return false;
	}
	
	public List<User> getInstructorsForSite(String siteId){
		List<User> instructors = new ArrayList<User>();
		Site site = getSiteById(siteId);
		if(site != null){
			for(Member member : site.getMembers()){
				if(isUserInstructor(member.getUserId(), site)){
					try {
						instructors.add(userDirectoryService.getUser(member.getUserId()));
					} catch (UserNotDefinedException e) {
						log.error(e.getMessage(), e);
					}
				}
			}
		}
		return instructors;
	}
	
	public boolean isUserMember(String userId, String siteRef){
		return authzGroupService.getUserRole(userId, siteRef) != null;
	}
	
	public Map<String,String> isUserMember(String userId, Collection<String> siteRefs){
		return authzGroupService.getUserRoles(userId, siteRefs);
	}
	
	public boolean isShoppingPeriodInstructorEditable(){
		return serverConfigurationService.getBoolean(DelegatedAccessConstants.PROPERTIES_SHOPPING_INSTRUCTOR_EDITABLE, false);
	}
	
	public boolean getSyncMyworkspaceTool(){
		return serverConfigurationService.getBoolean(DelegatedAccessConstants.PROPERTIES_SYNC_MYWORKSPACE_TOOL, true);
	}
	
	public String[] getHideRolesForInstructorViewAccess(){
		return serverConfigurationService.getStrings(DelegatedAccessConstants.PROPERTIES_HIDE_ROLES_FOR_VIEW_ACCESS);
	}
	
	public String[] getSubAdminOrderedRealmRoles(){
		return serverConfigurationService.getStrings(DelegatedAccessConstants.PROPERTIES_SUBADMIN_REALM_ROLE_ORDER);
	}
	

	
	/**
	 * DAC-40 Highlight Inactive Courses in site search
	 * requires the job "InactiveCoursesJob" attached in the jira
	 */
	public boolean isActiveSiteFlagEnabled(){
		return serverConfigurationService.getBoolean(DelegatedAccessConstants.PROPERTIES_ENABLE_ACTIVE_SITE_FLAG, false);
	}
	
	public void setSessionUserId(String userId){
		sessionManager.getCurrentSession().setUserId(userId);
	}
	
	public boolean allowAccessAdminsSetBecomeUserPerm(){
		return serverConfigurationService.getBoolean(DelegatedAccessConstants.PROPERTIES_ACCESS_ADMIN_ALLOW_SET_ALLOW_BECOME_USER, true);
	}
	
	public String siteReference(String context){
		return siteService.siteReference(context);
	}
	
	public Placement getCurrentPlacement(){
		return toolManager.getCurrentPlacement();
	}

	@Override
	public boolean isProviderIdLookupEnabled() {
		return serverConfigurationService.getBoolean(DelegatedAccessConstants.PROPERTIES_ACCESS_ENABLE_PROVIDER_ID_LOOKUP, false);
	}
	
	public String getProviderId(String siteRef){
		try {
			return authzGroupService.getAuthzGroup(siteRef).getProviderGroupId();
		} catch (GroupNotDefinedException e) {
			return "";
		}		
	}
	
	public String getHierarchySearchLabel(String hierarchyLevel){
		return serverConfigurationService.getString(DelegatedAccessConstants.PROPERTIES_SEARCH_HIERARCH_LABEL + hierarchyLevel, hierarchyLevel);
	}
	
	public boolean isSearchHideTerm(){
		return serverConfigurationService.getBoolean(DelegatedAccessConstants.PROPERTIES_SEARCH_HIDE_TERM, false);
	}
}
