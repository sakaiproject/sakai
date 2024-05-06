/*
 * Copyright (c) 2003-2023 The Apereo Foundation
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
package org.sakaiproject.scorm.service.sakai.impl.mocks;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.*;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.site.api.AllowedJoinableAccount;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteAdvisor;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteRemovalAdvisor;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author bjones86
 */
public class MockSiteService implements SiteService
{
    @Override
    public Site addSite( String id, String type ) throws IdInvalidException, IdUsedException, PermissionException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public Site addSite( String id, Site other ) throws IdInvalidException, IdUsedException, PermissionException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public void activateRoleViewOnSite(String siteReference, String role) throws SakaiException {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public Site addSite( String id, Site other, String realmTemplate ) throws IdInvalidException, IdUsedException, PermissionException
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addSiteAdvisor( SiteAdvisor advisor )
    {
    }

    @Override
    public void addSiteRemovalAdvisor( SiteRemovalAdvisor siteRemovalAdvisor )
    {
    }

    @Override
    public boolean allowAccessSite( String id )
    {
        return false;
    }

    @Override
    public boolean allowAddCourseSite()
    {
        return false;
    }

    @Override
    public boolean allowAddProjectSite()
    {
        return false;
    }

    @Override
    public boolean allowAddSite( String id )
    {
        return false;
    }

    @Override
    public boolean allowImportArchiveSite()
    {
        return false;
    }

    @Override
    public boolean allowRemoveSite( String id )
    {
        return false;
    }

    @Override
    public boolean allowRoleSwap( String id )
    {
        return false;
    }

    @Override
    public boolean allowUnjoinSite( String id )
    {
        return false;
    }

    @Override
    public boolean allowUpdateGroupMembership( String id )
    {
        return false;
    }

    @Override
    public boolean allowUpdateGroupMembership( String siteId, String groupId )
    {
        return false;
    }

    @Override
    public boolean allowUpdateSite( String id )
    {
        return false;
    }

    @Override
    public boolean allowUpdateSiteMembership( String id )
    {
        return false;
    }

    @Override
    public boolean allowViewRoster( String id )
    {
        return false;
    }

    @Override
    public String archive( String siteId, Document doc, Stack<Element> stack, String archivePath, List<Reference> attachments )
    {
        return SiteService.super.archive( siteId, doc, stack, archivePath, attachments );
    }

    @Override
    public int countSites( SelectionType type, Object ofType, String criteria, Map<String, String> propertyCriteria )
    {
        return 0;
    }

    @Override
    public Group findGroup( String refOrId )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public SitePage findPage( String id )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public ToolConfiguration findTool( String id )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public LinkedHashSet<String> getAllowedJoinableAccountTypeCategories()
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public List<String> getAllowedJoinableAccountTypes()
    {
        return Collections.emptyList();
    }

    @Override
    public List<AllowedJoinableAccount> getAllowedJoinableAccounts()
    {
        return Collections.emptyList();
    }

    @Override
    public Entity getEntity( Reference ref )
    {
        return SiteService.super.getEntity( ref );
    }

    @Override
    public Collection<String> getEntityAuthzGroups( Reference ref, String userId )
    {
        return SiteService.super.getEntityAuthzGroups( ref, userId );
    }

    @Override
    public String getEntityDescription( Reference ref )
    {
        return SiteService.super.getEntityDescription( ref );
    }

    @Override
    public ResourceProperties getEntityResourceProperties( Reference ref )
    {
        return SiteService.super.getEntityResourceProperties( ref );
    }

    @Override
    public String getEntityUrl( Reference ref )
    {
        return SiteService.super.getEntityUrl( ref );
    }

    @Override
    public Optional<String> getEntityUrl( Reference ref, Entity.UrlType urlType )
    {
        return SiteService.super.getEntityUrl( ref, urlType );
    }

    @Override
    public HttpAccess getHttpAccess()
    {
        return SiteService.super.getHttpAccess();
    }

    @Override
    public String getJoinGroupId( String id )
    {
        return "";
    }

    @Override
    public String getLabel()
    {
        return SiteService.super.getLabel();
    }

    @Override
    public String[] getLayoutNames()
    {
        return new String[]{};
    }

    @Override
    public String getParentSite( String siteId )
    {
        return "";
    }

    @Override
    public Site getSite( String id ) throws IdUnusedException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public List<SiteAdvisor> getSiteAdvisors()
    {
        return Collections.emptyList();
    }

    @Override
    public String getSiteDisplay( String id )
    {
        return "";
    }

    @Override
    public List<String> getSiteIds( SelectionType type, Object ofType, String criteria, Map<String, String> propertyCriteria, SortType sort, PagingPosition page, boolean requireDescription, String userId )
    {
        return Collections.emptyList();
    }

    @Override
    public List<String> getSiteIds( SelectionType type, Object ofType, String criteria, Map<String, String> propertyCriteria, SortType sort, PagingPosition page )
    {
        return Collections.emptyList();
    }

    @Override
    public List<String> getSiteIds( SelectionType type, Object ofType, String criteria, Map<String, String> propertyCriteria, Map<String, String> propertyRestrictions, SortType sort, PagingPosition page, String userId )
    {
        return Collections.emptyList();
    }

    @Override
    public Optional<Locale> getSiteLocale( String siteId )
    {
        return Optional.empty();
    }

    @Override
    public Optional<Locale> getSiteLocale( Site site )
    {
        return Optional.empty();
    }

    @Override
    public String getSiteSkin( String id )
    {
        return "";
    }

    @Override
    public String getSiteSpecialId( String site )
    {
        return "";
    }

    @Override
    public List<String> getSiteTypeStrings( String type )
    {
        return Collections.emptyList();
    }

    @Override
    public List<String> getSiteTypes()
    {
        return Collections.emptyList();
    }

    @Override
    public String getSiteUserId( String site )
    {
        return "";
    }

    @Override
    public Site getSiteVisit( String id ) throws IdUnusedException, PermissionException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public List<Site> getSites( SelectionType type, Object ofType, String criteria, Map<String, String> propertyCriteria, SortType sort, PagingPosition page, boolean requireDescription, String userId )
    {
        return Collections.emptyList();
    }

    @Override
    public List<Site> getSites( SelectionType type, Object ofType, String criteria, Map<String, String> propertyCriteria, SortType sort, PagingPosition page )
    {
        return Collections.emptyList();
    }

    @Override
    public List<Site> getSites( SelectionType type, Object ofType, String criteria, Map<String, String> propertyCriteria, SortType sort, PagingPosition page, boolean requireDescription )
    {
        return Collections.emptyList();
    }

    @Override
    public List<Site> getSoftlyDeletedSites()
    {
        return Collections.emptyList();
    }

    @Override
    public String getSpecialSiteId( String special )
    {
        return "";
    }

    @Override
    public List<Site> getSubSites( String siteId )
    {
        return Collections.emptyList();
    }

    @Override
    public String getUserSiteId( String userId )
    {
        return "";
    }

    @Override
    public List<Site> getUserSites()
    {
        return Collections.emptyList();
    }

    @Override
    public List<Site> getUserSites( boolean requireDescription )
    {
        return Collections.emptyList();
    }

    @Override
    public List<Site> getUserSites( boolean requireDescription, boolean includeUnpublishedSites )
    {
        return Collections.emptyList();
    }

    @Override
    public List<Site> getUserSites( boolean requireDescription, boolean includeUnpublishedSites, List excludedSites )
    {
        return Collections.emptyList();
    }

    @Override
    public List<Site> getUserSites( boolean requireDescription, String userId )
    {
        return Collections.emptyList();
    }

    @Override
    public List<Site> getUserSites( boolean requireDescription, String userID, boolean includeUnpublishedSites )
    {
        return Collections.emptyList();
    }

    @Override
    public List<Site> getUserSites( boolean requireDescription, String userID, boolean includeUnpublishedSites, List excludedSites )
    {
        return Collections.emptyList();
    }

    @Override
    public String getUserSpecificSiteTitle( Site site, String userID )
    {
        return "";
    }

    @Override
    public String getUserSpecificSiteTitle( Site site, String userID, List<String> siteProviders )
    {
        return "";
    }

    @Override
    public boolean isAllowedToJoin( String id )
    {
        return false;
    }

    @Override
    public boolean isCurrentUserMemberOfSite( String id )
    {
        return false;
    }

    @Override
    public boolean isGlobalJoinExcludedFromPublicListEnabled()
    {
        return false;
    }

    @Override
    public boolean isGlobalJoinFromSiteBrowserEnabled()
    {
        return false;
    }

    @Override
    public boolean isGlobalJoinGroupEnabled()
    {
        return false;
    }

    @Override
    public boolean isGlobalJoinLimitByAccountTypeEnabled()
    {
        return false;
    }

    @Override
    public boolean isLimitByAccountTypeEnabled( String id )
    {
        return false;
    }

    @Override
    public boolean isSpecialSite( String site )
    {
        return false;
    }

    @Override
    public boolean isStealthedToolPresent( Site site, String toolID )
    {
        return false;
    }

    @Override
    public boolean isUserSite( String site )
    {
        return false;
    }

    @Override
    public void join( String id ) throws IdUnusedException, PermissionException
    {
    }

    @Override
    public String merge( String toSiteId, Element e, String creatorId )
    {
        return "";
    }

    @Override
    public String merge( String siteId, Element root, String archivePath, String fromSiteId, Map<String, String> attachmentNames, Map<String, String> userIdTrans, Set<String> userListAllowImport )
    {
        return SiteService.super.merge( siteId, root, archivePath, fromSiteId, attachmentNames, userIdTrans, userListAllowImport );
    }

    @Override
    public boolean parseEntityReference( String reference, Reference ref )
    {
        return SiteService.super.parseEntityReference( reference, ref );
    }

    @Override
    public void removeSite( Site site ) throws PermissionException, IdUnusedException
    {
    }

    @Override
    public void removeSite( Site site, boolean hardDelete ) throws PermissionException, IdUnusedException
    {
    }

    @Override
    public boolean removeSiteAdvisor( SiteAdvisor advisor )
    {
        return false;
    }

    @Override
    public boolean removeSiteRemovalAdvisor( SiteRemovalAdvisor siteRemovalAdvisor )
    {
        return false;
    }

    @Override
    public void save( Site site ) throws IdUnusedException, PermissionException
    {
    }

    @Override
    public void saveGroupMembership( Site site ) throws IdUnusedException, PermissionException
    {
    }

    @Override
    public void saveSiteInfo( String id, String description, String infoUrl ) throws IdUnusedException, PermissionException
    {
    }

    @Override
    public void saveSiteMembership( Site site ) throws IdUnusedException, PermissionException
    {
    }

    @Override
    public void saveSitePropertyOnSites( String propertyName, String propertyValue, String... siteIds )
    {
    }

    @Override
    public void setSiteSecurity( String siteId, Set<String> updateUsers, Set<String> visitUnpUsers, Set<String> visitUsers )
    {
    }

    @Override
    public void setUserSecurity( String userId, Set<String> updateSites, Set<String> visitUnpSites, Set<String> visitSites )
    {
    }

    @Override
    public void silentlyUnpublish( List<String> siteIds )
    {
    }

    @Override
    public boolean siteExists( String id )
    {
        return false;
    }

    @Override
    public Optional<Site> getOptionalSite(String id) {
        return Optional.empty();
    }

    @Override
    public String siteGroupReference( String siteId, String groupId )
    {
        return "";
    }

    @Override
    public String sitePageReference( String siteId, String pageId )
    {
        return "";
    }

    @Override
    public String siteReference( String id )
    {
        return "";
    }

    @Override
    public String siteToolReference( String siteId, String toolId )
    {
        return "";
    }

    @Override
    public void unjoin( String id ) throws IdUnusedException, PermissionException
    {
    }

    @Override
    public SiteTitleValidationStatus validateSiteTitle( String orig, String stripped )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public boolean willArchiveMerge()
    {
        return SiteService.super.willArchiveMerge();
    }

    @Override
    public String idFromSiteReference(String ref) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }
}
