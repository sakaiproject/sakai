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

import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;

import org.sakaiproject.antivirus.api.VirusFoundException;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdLengthException;
import org.sakaiproject.exception.IdUniquenessException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.time.api.Time;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author bjones86
 */
public class MockContentHostingService implements ContentHostingService
{
    @Override
    public ContentResource addAttachmentResource( String name, String type, byte[] content, ResourceProperties properties ) throws IdInvalidException, InconsistentException, IdUsedException, PermissionException, OverQuotaException, ServerOverloadException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public ContentResource addAttachmentResource( String name, String type, InputStream content, ResourceProperties properties ) throws IdInvalidException, InconsistentException, IdUsedException, PermissionException, OverQuotaException, ServerOverloadException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public ContentResource addAttachmentResource( String name, String site, String tool, String type, byte[] content, ResourceProperties properties ) throws IdInvalidException, InconsistentException, IdUsedException, PermissionException, OverQuotaException, ServerOverloadException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public ContentResource addAttachmentResource( String name, String site, String tool, String type, InputStream content, ResourceProperties properties ) throws IdInvalidException, InconsistentException, IdUsedException, PermissionException, OverQuotaException, ServerOverloadException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public ContentResourceEdit addAttachmentResource( String name ) throws IdInvalidException, InconsistentException, IdUsedException, PermissionException, ServerOverloadException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
	public ContentResource copyAttachment(String oAttachmentId, String toContext, String toolTitle, Map<String, String> attachmentImportMap) throws IdUnusedException, TypeException, PermissionException {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public ContentCollection addCollection( String id, ResourceProperties properties ) throws IdUsedException, IdInvalidException, PermissionException, InconsistentException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public ContentCollection addCollection( String id, ResourceProperties properties, Collection<String> groups ) throws IdUsedException, IdInvalidException, PermissionException, InconsistentException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public ContentCollection addCollection( String id, ResourceProperties properties, Collection<String> groups, boolean hidden, Time releaseDate, Time retractDate ) throws IdUsedException, IdInvalidException, PermissionException, InconsistentException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public ContentCollectionEdit addCollection( String id ) throws IdUsedException, IdInvalidException, PermissionException, InconsistentException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public ContentCollectionEdit addCollection( String collectionId, String name ) throws PermissionException, IdUnusedException, IdUsedException, IdLengthException, IdInvalidException, TypeException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public ContentCollectionEdit addCollection( String collectionId, String name, int limit ) throws PermissionException, IdUnusedException, IdUsedException, IdLengthException, IdInvalidException, TypeException, IdUniquenessException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public ResourceProperties addProperty( String id, String name, String value ) throws PermissionException, IdUnusedException, TypeException, InUseException, ServerOverloadException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public ContentResource addResource( String id, String type, byte[] content, ResourceProperties properties, int priority ) throws PermissionException, IdUsedException, IdInvalidException, InconsistentException, OverQuotaException, ServerOverloadException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public ContentResource addResource( String id, String type, InputStream content, ResourceProperties properties, int priority ) throws PermissionException, IdUsedException, IdInvalidException, InconsistentException, OverQuotaException, ServerOverloadException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public ContentResource addResource( String name, String collectionId, int limit, String type, byte[] content, ResourceProperties properties, int priority ) throws PermissionException, IdUniquenessException, IdLengthException, IdInvalidException, InconsistentException, IdLengthException, OverQuotaException, ServerOverloadException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public ContentResource addResource( String id, String type, byte[] content, ResourceProperties properties, Collection<String> groups, int priority ) throws PermissionException, IdUsedException, IdInvalidException, InconsistentException, OverQuotaException, ServerOverloadException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public ContentResource addResource( String id, String type, InputStream content, ResourceProperties properties, Collection groups, int priority ) throws PermissionException, IdUsedException, IdInvalidException, InconsistentException, OverQuotaException, ServerOverloadException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public ContentResource addResource( String name, String collectionId, int limit, String type, byte[] content, ResourceProperties properties, Collection<String> groups, int priority ) throws PermissionException, IdUniquenessException, IdLengthException, IdInvalidException, InconsistentException, IdLengthException, OverQuotaException, ServerOverloadException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public ContentResource addResource( String name, String collectionId, int limit, String type, byte[] content, ResourceProperties properties, Collection<String> groups, boolean hidden, Time releaseDate, Time retractDate, int priority ) throws PermissionException, IdUniquenessException, IdLengthException, IdInvalidException, InconsistentException, IdLengthException, OverQuotaException, ServerOverloadException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public ContentResource addResource( String name, String collectionId, int limit, String type, InputStream content, ResourceProperties properties, Collection groups, boolean hidden, Time releaseDate, Time retractDate, int priority ) throws PermissionException, IdUniquenessException, IdLengthException, IdInvalidException, InconsistentException, IdLengthException, OverQuotaException, ServerOverloadException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public ContentResourceEdit addResource( String id ) throws PermissionException, IdUsedException, IdInvalidException, InconsistentException, ServerOverloadException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public ContentResourceEdit addResource( String collectionId, String basename, String extension, int maximum_tries ) throws PermissionException, IdUniquenessException, IdLengthException, IdInvalidException, IdUnusedException, OverQuotaException, ServerOverloadException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public boolean allowAddAttachmentResource()
    {
        return false;
    }

    @Override
    public boolean allowAddCollection( String id )
    {
        return false;
    }

    @Override
    public boolean allowAddProperty( String id )
    {
        return false;
    }

    @Override
    public boolean allowAddResource( String id )
    {
        return false;
    }

    @Override
    public boolean allowCopy( String id, String new_id )
    {
        return false;
    }

    @Override
    public boolean allowGetCollection( String id )
    {
        return false;
    }

    @Override
    public boolean allowGetProperties( String id )
    {
        return false;
    }

    @Override
    public boolean allowGetResource( String id )
    {
        return false;
    }

    @Override
    public boolean allowRemoveCollection( String id )
    {
        return false;
    }

    @Override
    public boolean allowRemoveProperty( String id )
    {
        return false;
    }

    @Override
    public boolean allowRemoveResource( String id )
    {
        return false;
    }

    @Override
    public boolean allowRename( String id, String new_id )
    {
        return false;
    }

    @Override
    public boolean allowUpdateCollection( String id )
    {
        return false;
    }

    @Override
    public boolean allowUpdateResource( String id )
    {
        return false;
    }

    @Override
    public String archive( String siteId, Document doc, Stack<Element> stack, String archivePath, List<Reference> attachments )
    {
        return ContentHostingService.super.archive( siteId, doc, stack, archivePath, attachments );
    }

    @Override
    public String archiveResources( List resources, Document doc, Stack stack, String archivePath )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public void cancelCollection( ContentCollectionEdit edit )
    {
    }

    @Override
    public void cancelResource( ContentResourceEdit edit )
    {
    }

    @Override
    public void checkCollection( String id ) throws IdUnusedException, TypeException, PermissionException
    {
    }

    @Override
    public void checkResource( String id ) throws PermissionException, IdUnusedException, TypeException
    {
    }

    @Override
    public void commitCollection( ContentCollectionEdit edit )
    {
    }

    @Override
    public void commitResource( ContentResourceEdit edit ) throws OverQuotaException, ServerOverloadException, VirusFoundException
    {
    }

    @Override
    public void commitResource( ContentResourceEdit edit, int priority ) throws OverQuotaException, ServerOverloadException, VirusFoundException
    {
    }

    @Override
    public boolean containsLockedNode( String id )
    {
        return false;
    }

    @Override
    public String copy( String id, String new_id ) throws PermissionException, IdUnusedException, TypeException, InUseException, OverQuotaException, IdUsedException, ServerOverloadException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public String copyIntoFolder( String id, String folder_id ) throws PermissionException, IdUnusedException, TypeException, InUseException, OverQuotaException, IdUsedException, ServerOverloadException, InconsistentException, IdLengthException, IdUniquenessException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public void createDropboxCollection()
    {
    }

    @Override
    public void createDropboxCollection( String siteId )
    {
    }

    @Override
    public void createIndividualDropbox( String siteId )
    {
    }

    @Override
    public ContentCollectionEdit editCollection( String id ) throws IdUnusedException, TypeException, PermissionException, InUseException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public ContentResourceEdit editResource( String id ) throws PermissionException, IdUnusedException, TypeException, InUseException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public void eliminateDuplicates( Collection<String> resourceIds )
    {
    }

    @Override
    public String expandMacros( String url )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public void expandZippedResource( String resourceId ) throws Exception
    {
    }

    @Override
    public List<ContentResource> findResources( String type, String primaryMimeType, String subMimeType, Set<String> contextIds )
    {
        return Collections.emptyList();
    }

    @Override
    public List<ContentResource> findResources( String type, String primaryMimeType, String subMimeType )
    {
        return Collections.emptyList();
    }

    @Override
    public List getAllDeletedResources( String id )
    {
        return Collections.emptyList();
    }

    @Override
    public List getAllEntities( String id )
    {
        return Collections.emptyList();
    }

    @Override
    public List<ContentResource> getAllResources( String id )
    {
        return Collections.emptyList();
    }

    @Override
    public ContentCollection getCollection( String id ) throws IdUnusedException, TypeException, PermissionException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public Map<String, String> getCollectionMap()
    {
        return Collections.emptyMap();
    }

    @Override
    public int getCollectionSize( String id ) throws IdUnusedException, TypeException, PermissionException
    {
        return 0;
    }

    @Override
    public String getContainingCollectionId( String id )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public Collection<ContentResource> getContextResourcesOfType( String resourceType, Set<String> contextIds )
    {
        return Collections.emptyList();
    }

    @Override
    public int getDepth( String resourceId, String baseCollectionId )
    {
        return 0;
    }

    @Override
    public URI getDirectLinkToAsset( ContentResource resource ) throws Exception
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public String getDropboxCollection()
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public String getDropboxCollection( String siteId )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public String getDropboxDisplayName()
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public String getDropboxDisplayName( String siteId )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public Entity getEntity( Reference ref )
    {
        return ContentHostingService.super.getEntity( ref );
    }

    @Override
    public Collection<String> getEntityAuthzGroups( Reference ref, String userId )
    {
        return Collections.emptyList();
    }

    @Override
    public String getEntityDescription( Reference ref )
    {
        return ContentHostingService.super.getEntityDescription( ref );
    }

    @Override
    public ResourceProperties getEntityResourceProperties( Reference ref )
    {
        return ContentHostingService.super.getEntityResourceProperties( ref );
    }

    @Override
    public String getEntityUrl( Reference ref )
    {
        return ContentHostingService.super.getEntityUrl( ref );
    }

    @Override
    public Optional<String> getEntityUrl( Reference ref, Entity.UrlType urlType )
    {
        return Optional.empty();
    }

    @Override
    public Collection getGroupsWithAddPermission( String collectionId )
    {
        return Collections.emptyList();
    }

    @Override
    public Collection getGroupsWithReadAccess( String collectionId )
    {
        return Collections.emptyList();
    }

    @Override
    public Collection getGroupsWithRemovePermission( String collectionId )
    {
        return Collections.emptyList();
    }

    @Override
    public Map<String, String> getHtmlForRef( String ref )
    {
        return Collections.emptyMap();
    }

    @Override
    public List<String> getHtmlForRefMimetypes()
    {
        return Collections.emptyList();
    }

    @Override
    public HttpAccess getHttpAccess()
    {
        return ContentHostingService.super.getHttpAccess();
    }

    @Override
    public String getIndividualDropboxId( String entityId )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public String getInstructorUploadFolderName()
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public String getLabel()
    {
        return ContentHostingService.super.getLabel();
    }

    @Override
    public Collection getLocks( String id )
    {
        return Collections.emptyList();
    }

    @Override
    public ResourceProperties getProperties( String id ) throws PermissionException, IdUnusedException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public long getQuota( ContentCollection collection )
    {
        return 0l;
    }

    @Override
    public String getReference( String id )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public ContentResource getResource( String id ) throws PermissionException, IdUnusedException, TypeException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public Collection<ContentResource> getResourcesOfType( String resourceType, int pageSize, int page )
    {
        return Collections.emptyList();
    }

    @Override
    public Set<String> getRoleViews( String id )
    {
        return Collections.emptySet();
    }

    @Override
    public String getSiteCollection( String siteId )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public String getStudentUploadFolderName()
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public String getUrl( String id )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public String getUrl( String id, String rootProperty )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public String getUuid( String id )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public boolean isAttachmentResource( String id )
    {
        return false;
    }

    @Override
    public boolean isAvailabilityEnabled()
    {
        return false;
    }

    @Override
    public boolean isAvailable( String entityId )
    {
        return false;
    }

    @Override
    public boolean isCollection( String entityId )
    {
        return false;
    }

    @Override
    public boolean isContentHostingHandlersEnabled()
    {
        return false;
    }

    @Override
    public boolean isDropboxGroups( String siteId )
    {
        return false;
    }

    @Override
    public boolean isDropboxMaintainer()
    {
        return false;
    }

    @Override
    public boolean isDropboxMaintainer( String siteId )
    {
        return false;
    }

    @Override
    public boolean isInDropbox( String entityId )
    {
        return false;
    }

    @Override
    public boolean isIndividualDropbox( String entityId )
    {
        return false;
    }

    @Override
    public boolean isInheritingPubView( String id )
    {
        return false;
    }

    @Override
    public boolean isInheritingRoleView( String id, String roleId )
    {
        return false;
    }

    @Override
    public boolean isInsideIndividualDropbox( String entityId )
    {
        return false;
    }

    @Override
    public boolean isLocked( String id )
    {
        return false;
    }

    @Override
    public boolean isPubView( String id )
    {
        return false;
    }

    @Override
    public boolean isRoleView( String id, String roleId )
    {
        return false;
    }

    @Override
    public boolean isRootCollection( String id )
    {
        return false;
    }

    @Override
    public boolean isShortRefs()
    {
        return false;
    }

    @Override
    public boolean isSiteLevelDropbox( String entityId )
    {
        return false;
    }

    @Override
    public boolean isSortByPriorityEnabled()
    {
        return false;
    }

    @Override
    public void lockObject( String id, String lockId, String subject, boolean system )
    {
    }

    @Override
    public String merge( String siteId, Element root, String archivePath, String fromSiteId, Map<String, String> attachmentNames, Map<String, String> userIdTrans, Set<String> userListAllowImport )
    {
        return ContentHostingService.super.merge( siteId, root, archivePath, fromSiteId, attachmentNames, userIdTrans, userListAllowImport );
    }

    @Override
    public String moveIntoFolder( String id, String folder_id ) throws PermissionException, IdUnusedException, TypeException, InUseException, OverQuotaException, IdUsedException, InconsistentException, ServerOverloadException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public Comparator newContentHostingComparator( String property, boolean ascending )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public ResourcePropertiesEdit newResourceProperties()
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public boolean parseEntityReference( String reference, Reference ref )
    {
        return false;
    }

    @Override
    public void removeAllLocks( String id )
    {
    }

    @Override
    public void removeCollection( String id ) throws IdUnusedException, TypeException, PermissionException, InUseException, ServerOverloadException
    {
    }

    @Override
    public void removeCollection( ContentCollectionEdit edit ) throws TypeException, PermissionException, InconsistentException, ServerOverloadException
    {
    }

    @Override
    public void removeDeletedResource( String resourceId ) throws PermissionException, IdUnusedException, TypeException, InUseException
    {
    }

    @Override
    public void removeLock( String id, String lockId )
    {
    }

    @Override
    public ResourceProperties removeProperty( String id, String name ) throws PermissionException, IdUnusedException, TypeException, InUseException, ServerOverloadException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public void removeResource( String id ) throws PermissionException, IdUnusedException, TypeException, InUseException
    {
    }

    @Override
    public void removeResource( ContentResourceEdit edit ) throws PermissionException
    {
    }

    @Override
    public String resolveUuid( String uuid )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public void restoreResource( String id ) throws PermissionException, IdUsedException, IdUnusedException, IdInvalidException, InconsistentException, OverQuotaException, ServerOverloadException, TypeException, InUseException
    {
    }

    @Override
    public void setPubView( String id, boolean pubview )
    {
    }

    @Override
    public void setRoleView( String id, String roleId, boolean grantAccess ) throws AuthzPermissionException
    {
    }

    @Override
    public void setUuid( String id, String uuid ) throws IdInvalidException
    {
    }

    @Override
    public ContentResource updateResource( String id, String type, byte[] content ) throws PermissionException, IdUnusedException, TypeException, InUseException, OverQuotaException, ServerOverloadException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public boolean willArchiveMerge()
    {
        return false;
    }
}
