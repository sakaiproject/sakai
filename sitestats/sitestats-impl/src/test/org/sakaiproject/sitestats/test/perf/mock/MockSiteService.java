package org.sakaiproject.sitestats.test.perf.mock;

import java.util.*;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.site.api.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class MockSiteService implements SiteService {

	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Site> getUserSites( boolean requireDescription, boolean includeUnpublishedSites )
	{
		return null;
	}

	@Override
	public List<Site> getUserSites( boolean requireDescription, String userID, boolean includeUnpublishedSites )
	{
		return null;
	}

	@Override
	public List<Site> getUserSites( boolean requireDescription, boolean includeUnpublishedSites, List excludedSites )
	{
		return null;
	}

	@Override
	public List<Site> getUserSites( boolean requireDescription, String userID, boolean includeUnpublishedSites, List excludedSites )
	{
		return null;
	}

	@Override
	public boolean willArchiveMerge() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String archive(String siteId, Document doc, Stack<Element> stack,
			String archivePath, List<Reference> attachments) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String merge(String siteId, Element root, String archivePath,
			String fromSiteId, Map<String, String> attachmentNames,
			Map<String, String> userIdTrans, Set<String> userListAllowImport) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean parseEntityReference(String reference, Reference ref) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getEntityDescription(Reference ref) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResourceProperties getEntityResourceProperties(Reference ref) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Entity getEntity(Reference ref) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getEntityUrl(Reference ref) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> getEntityAuthzGroups(Reference ref, String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpAccess getHttpAccess() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getLayoutNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean allowAccessSite(String id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean siteExists(String id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Site getSite(String id) throws IdUnusedException {
		return new MockSite(id);
	}

	@Override
	public Site getSiteVisit(String id) throws IdUnusedException,
			PermissionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean allowUpdateSite(String id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean allowUpdateSiteMembership(String id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean allowUpdateGroupMembership(String id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void save(Site site) throws IdUnusedException, PermissionException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveSiteMembership(Site site) throws IdUnusedException,
			PermissionException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveGroupMembership(Site site) throws IdUnusedException,
			PermissionException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveSiteInfo(String id, String description, String infoUrl)
			throws IdUnusedException, PermissionException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean allowAddSite(String id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean allowAddCourseSite() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean allowAddPortfolioSite() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean allowAddProjectSite() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Site addSite(String id, String type) throws IdInvalidException,
			IdUsedException, PermissionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Site addSite(String id, Site other) throws IdInvalidException,
			IdUsedException, PermissionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean allowRemoveSite(String id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeSite(Site site) throws PermissionException,
			IdUnusedException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String siteReference(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String sitePageReference(String siteId, String pageId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String siteToolReference(String siteId, String toolId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String siteGroupReference(String siteId, String groupId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isUserSite(String site) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getSiteUserId(String site) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUserSiteId(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSpecialSite(String site) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getSiteSpecialId(String site) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSpecialSiteId(String special) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSiteDisplay(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ToolConfiguration findTool(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SitePage findPage(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean allowViewRoster(String id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void join(String id) throws IdUnusedException, PermissionException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isAllowedToJoin(String id) {
		return false;
	}

	@Override
	public String getJoinGroupId(String id) {
		return null;
	}

	@Override
	public boolean isCurrentUserMemberOfSite(String id) {
		return false;
	}

	@Override
	public boolean isLimitByAccountTypeEnabled(String id) {
		return false;
	}

	@Override
	public LinkedHashSet<String> getAllowedJoinableAccountTypeCategories() {
		return null;
	}

	@Override
	public List<String> getAllowedJoinableAccountTypes() {
		return null;
	}

	@Override
	public List<AllowedJoinableAccount> getAllowedJoinableAccounts() {
		return null;
	}

	@Override
	public boolean isGlobalJoinGroupEnabled() {
		return false;
	}

	@Override
	public boolean isGlobalJoinExcludedFromPublicListEnabled() {
		return false;
	}

	@Override
	public boolean isGlobalJoinLimitByAccountTypeEnabled() {
		return false;
	}

	@Override
	public boolean isGlobalJoinFromSiteBrowserEnabled() {
		return false;
	}

	@Override
	public boolean allowUnjoinSite(String id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void unjoin(String id) throws IdUnusedException, PermissionException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getSiteSkin(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getSiteTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Site> getUserSites() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Site> getUserSites(boolean requireDescription) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Site> getUserSites(boolean requireDescription, String userId)
	{
		return null;
	}

	public List<Site> getSites(SelectionType type, Object ofType, String criteria, Map<String, String> propertyCriteria, SortType sort, PagingPosition page, boolean requireDescription, String userId)
	{
		return null;
	}

	@Override
	public List<Site> getSites(SelectionType type, Object ofType,
			String criteria, Map<String, String> propertyCriteria,
			SortType sort, PagingPosition page) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Site> getSites(SelectionType type, Object ofType,
			String criteria, Map<String, String> propertyCriteria,
			SortType sort, PagingPosition page, boolean requireDescription) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> getSiteIds(SelectionType type, Object ofType, String criteria, Map<String, String> propertyCriteria, SortType sort, PagingPosition page, boolean requireDescription, String userId)
	{
		return null;
	}

	@Override
	public List<String> getSiteIds(SelectionType type, Object ofType, String criteria, Map<String, String> propertyCriteria, SortType sort, PagingPosition page) {
		return null;
	}

	@Override
	public List<Site> getSoftlyDeletedSites() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int countSites(SelectionType type, Object ofType, String criteria,
			Map<String, String> propertyCriteria) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setSiteSecurity(String siteId, Set<String> updateUsers,
			Set<String> visitUnpUsers, Set<String> visitUsers) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setUserSecurity(String userId, Set<String> updateSites,
			Set<String> visitUnpSites, Set<String> visitSites) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String merge(String toSiteId, Element e, String creatorId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Group findGroup(String refOrId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addSiteAdvisor(SiteAdvisor advisor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean removeSiteAdvisor(SiteAdvisor advisor) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<SiteAdvisor> getSiteAdvisors() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean allowRoleSwap(String id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean allowImportArchiveSite()
	{
		return true;
	}

	@Override
	public List<String> getSiteTypeStrings(String type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addSiteRemovalAdvisor(SiteRemovalAdvisor siteRemovalAdvisor) {
	}

	@Override
	public boolean removeSiteRemovalAdvisor(SiteRemovalAdvisor siteRemovalAdvisor) {
		return false;
	}

	@Override
	public List<Site> getSubSites(String siteId) {
		return null;
	}

	@Override
	public String getParentSite(String siteId) {
		return null;
	}

	@Override
	public String getUserSpecificSiteTitle( Site site, String userID )
	{
		return null;
	}
}
