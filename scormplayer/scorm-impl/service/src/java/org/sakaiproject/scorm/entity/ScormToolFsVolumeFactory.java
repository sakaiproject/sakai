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
package org.sakaiproject.scorm.entity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.elfinder.FsType;
import org.sakaiproject.elfinder.ReadOnlyFsVolume;
import org.sakaiproject.elfinder.SakaiFsItem;
import org.sakaiproject.elfinder.SakaiFsService;
import org.sakaiproject.elfinder.ToolFsVolume;
import org.sakaiproject.elfinder.ToolFsVolumeFactory;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.scorm.api.ScormConstants;
import org.sakaiproject.scorm.exceptions.ResourceStorageException;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.site.api.SiteService;

/**
 * This class (and subclass) provide all functionality required to link to SCORM modules from within the CKEditor.
 * @author bjones86
 */
@Slf4j
public class ScormToolFsVolumeFactory implements ToolFsVolumeFactory
{
    private static final String DIRECTORY               = "directory";
    private static final String SCORM_TYPE              = "sakai/scorm";
    private static final String SCORM_DIRECT_URL_PREFIX = "/direct/scorm/";

    // Sakai APIs
    @Setter private ServerConfigurationService serverConfigurationService;
    @Setter private ContentHostingService contentHostingService;
    @Setter private SecurityService securityService;
    @Setter private SakaiFsService sakaiFsService;
    @Setter private SiteService siteService;

    // SCORM APIs
    @Setter private ScormContentService scormContentService;

    public void init()
    {
        sakaiFsService.registerToolVolume(this);
    }

    @Override
    public String getPrefix()
    {
        return FsType.SCORM.toString();
    }

    @Override
    public String getToolId()
    {
        return ScormConstants.SCORM_TOOL_ID;
    }

    @Override
    public ToolFsVolume getVolume( String siteId )
    {
        return new ScormToolFsVolume( sakaiFsService, siteId );
    }

    public class ScormToolFsVolume extends ReadOnlyFsVolume implements ToolFsVolume
    {
        private SakaiFsService service;
        private String siteId;
        private ContentPackage contentPackage;

        public ScormToolFsVolume( SakaiFsService sakaiFsService, String siteId )
        {
            this( sakaiFsService, siteId, null );
        }

        public ScormToolFsVolume( SakaiFsService sakaiFsService, String siteId, ContentPackage contentPackage )
        {
            this.service = sakaiFsService;
            this.siteId = siteId;
            this.contentPackage = contentPackage;
        }

        @Override
        public String getSiteId()
        {
            return siteId;
        }

        @Override
        public ToolFsVolumeFactory getToolVolumeFactory()
        {
            return ScormToolFsVolumeFactory.this;
        }

        @Override
        public SakaiFsItem fromPath( String path )
        {
            if( StringUtils.isNotEmpty( path ) )
            {
                String[] parts = path.split( "/" );
                if( parts.length > 2 && getPrefix().equals( parts[1] ) )
                {
                    try
                    {
                        ContentPackage cp = scormContentService.getContentPackage( Long.valueOf( parts[2] ) );
                        this.contentPackage = cp;
                        return new SakaiFsItem( buildEntityID( cp ), cp.getTitle(), this, FsType.SCORM );
                    }
                    catch( NumberFormatException ex )
                    {
                        log.warn( "Could not parse SCORM Content Package ID = {}", parts[2], ex );
                    }
                    catch( ResourceStorageException ex )
                    {
                        log.warn( "Unexpected exception for SCORM linking", ex );
                    }
                }
            }

            return getRoot();
        }

        @Override
        public long getLastModified( SakaiFsItem fsItem )
        {
            if( !getRoot().equals( fsItem ) && FsType.SCORM.equals( fsItem.getType() ) )
            {
                ContentPackage cp = ((ScormToolFsVolume) fsItem.getVolume()).contentPackage;
                return cp != null && cp.getModifiedOn() != null ? cp.getModifiedOn().getTime() / 1000 : 0L;
            }

            return 0L;
        }

        @Override
        public String getMimeType( SakaiFsItem fsItem )
        {
            return isFolder( fsItem ) ? DIRECTORY : SCORM_TYPE;
        }

        @Override
        public String getName()
        {
            return ScormConstants.SCORM_DFLT_TOOL_NAME;
        }

        @Override
        public String getName( SakaiFsItem fsItem )
        {
            if( getRoot().equals( fsItem ) )
            {
                return getName();
            }
            if( FsType.SCORM.equals( fsItem.getType() ) )
            {
                ContentPackage cp = ((ScormToolFsVolume) fsItem.getVolume()).contentPackage;
                return cp.getTitle();
            }
            else
            {
                throw new IllegalArgumentException( "Could not get title for: " + fsItem.toString() );
            }
        }

        @Override
        public SakaiFsItem getParent( SakaiFsItem fsItem )
        {
            if( getRoot().equals( fsItem ) )
            {
                return service.getSiteVolume( siteId ).getRoot();
            }
            else if( FsType.SCORM.equals( fsItem.getType() ) )
            {
                return getRoot();
            }

            return null;
        }

        @Override
        public String getPath( SakaiFsItem fsItem ) throws IOException
        {
            if( getRoot().equals( fsItem ) )
            {
                return "";
            }
            else if( FsType.SCORM.equals( fsItem.getType() ) )
            {
                return "/" + getPrefix() + "/" + ((ScormToolFsVolume) fsItem.getVolume()).contentPackage.getContentPackageId();
            }

            throw new IllegalArgumentException( "Wrong Type: " + fsItem.toString() );
        }

        @Override
        public SakaiFsItem getRoot()
        {
            return new SakaiFsItem( "", "", this, FsType.SCORM );
        }

        @Override
        public long getSize( SakaiFsItem fsItem ) throws IOException
        {
            if( !getRoot().equals( fsItem ) && FsType.SCORM.equals( fsItem.getType() ) && securityService.unlock( ScormConstants.PERM_CONFIG, siteService.siteReference( siteId ) ) )
            {
                ContentPackage cp = ((ScormToolFsVolume) fsItem.getVolume()).contentPackage;
                if( cp != null )
                {
                    String resourceID = ScormConstants.ROOT_DIRECTORY + cp.getResourceId() + "/";
                    try
                    {
                        if( contentHostingService.isCollection( resourceID ) )
                        {
                            return contentHostingService.getCollectionSize( resourceID );
                        }
                        else
                        {
                            return contentHostingService.getResource( resourceID ).getContentLength();
                        }
                    }
                    catch( IdUnusedException uie )
                    {
                        log.debug( "Failed to file size as item can't be found: {}", resourceID, uie );
                    }
                    catch( Exception ex )
                    {
                        log.warn( "Failed to get size for: {}", resourceID, ex );
                    }
                }
            }

            return 0L;
        }

        @Override
        public boolean isFolder( SakaiFsItem fsItem )
        {
            return FsType.SCORM.equals( fsItem.getType() ) && fsItem.getId().equals( "" );
        }

        @Override
        public SakaiFsItem[] listChildren( SakaiFsItem fsItem )
        {
            List<SakaiFsItem> items = new ArrayList<>();

            // Top level for a site, get all SCORM modules...
            if( getRoot().equals( fsItem ) )
            {
                for( ContentPackage cp : scormContentService.getContentPackages( siteId ) )
                {
                    ScormToolFsVolume volume = new ScormToolFsVolume( sakaiFsService, siteId, cp );
                    items.add( new SakaiFsItem( buildEntityID( cp ), cp.getTitle(), volume, FsType.SCORM ) );
                }
            }

            return items.toArray( new SakaiFsItem[items.size()] );
        }

        @Override
        public String getURL( SakaiFsItem fsItem )
        {
            if( FsType.SCORM.equals( fsItem.getType() ) && StringUtils.isNotBlank( fsItem.getId() ) )
            {
                return serverConfigurationService.getServerUrl() + SCORM_DIRECT_URL_PREFIX + fsItem.getId();
            }

            return null;
        }

        /**
         * Utility method to build an {@link org.sakaiproject.scorm.entity.ScormEntity} ID for the given {@link org.sakaiproject.scorm.model.api.ContentPackage}
         * @param cp the {@link org.sakaiproject.scorm.model.api.ContentPackage} to build an entity ID for
         * @return a {@link org.sakaiproject.scorm.entity.ScormEntity} ID in the format of "siteID:toolID:contentPackageID:resourceID:title"
         */
        private String buildEntityID( ContentPackage cp )
        {
            try
            {
                return siteId + ScormEntityProvider.ENTITY_PARAM_DELIMITER
                       + siteService.getSite( siteId ).getToolForCommonId( ScormConstants.SCORM_TOOL_ID ).getId() + ScormEntityProvider.ENTITY_PARAM_DELIMITER
                       + cp.getContentPackageId() + ScormEntityProvider.ENTITY_PARAM_DELIMITER
                       + cp.getResourceId() + ScormEntityProvider.ENTITY_PARAM_DELIMITER
                       + cp.getTitle();
            }
            catch( Exception ex )
            {
                log.warn( "Unexpected exception while building entity ID for SCORM linking", ex );
            }

            return null;
        }

        // Unimplemented methods
        @Override public boolean        exists              ( SakaiFsItem fsItem )                      { return false; }
        @Override public boolean        isRoot              ( SakaiFsItem fsItem )                      { return false; }
        @Override public boolean        isWriteable         ( SakaiFsItem fsItem )                      { return false; }
        @Override public boolean        hasChildFolder      ( SakaiFsItem fsItem )                      { return false; }
        @Override public String         getDimensions       ( SakaiFsItem fsItem )                      { return null;  }
        @Override public String         getThumbnailFileName( SakaiFsItem fsItem )                      { return null;  }
        @Override public InputStream    openInputStream     ( SakaiFsItem fsItem ) throws IOException   { return null;  }
    }
}
