package org.sakaiproject.elfinder.sakai.scorm;

import cn.bluejoe.elfinder.service.FsItem;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.elfinder.sakai.ReadOnlyFsVolume;
import org.sakaiproject.elfinder.sakai.SakaiFsService;
import org.sakaiproject.elfinder.sakai.SiteVolume;
import org.sakaiproject.elfinder.sakai.SiteVolumeFactory;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.scorm.api.ScormConstants;
import org.sakaiproject.scorm.entity.ScormEntityProvider;
import org.sakaiproject.scorm.exceptions.ResourceStorageException;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.site.api.SiteService;

/**
 *
 * @author bjones86
 */
@Slf4j
public class ScormSiteVolumeFactory implements SiteVolumeFactory
{
    private static final String DIRECTORY               = "directory";
    private static final String SCORM_TYPE              = "sakai/scorm";
    private static final String SCORM_DIRECT_URL_PREFIX = "/direct/scorm/";

    // Sakai APIs
    @Setter private ServerConfigurationService serverConfigurationService;
    @Setter private ContentHostingService contentHostingService;
    @Setter private SecurityService securityService;
    @Setter private SiteService siteService;

    // SCORM APIs
    @Setter private ScormContentService scormContentService;

    @Override
    public String getPrefix()
    {
        return ScormEntityProvider.ENTITY_PREFIX;
    }

    @Override
    public String getToolId()
    {
        return ScormConstants.SCORM_TOOL_ID;
    }

    @Override
    public SiteVolume getVolume( SakaiFsService service, String siteID )
    {
        return new ScormSiteVolume( service, siteID );
    }

    @AllArgsConstructor
    public class ScormSiteVolume extends ReadOnlyFsVolume implements SiteVolume
    {
        private SakaiFsService service;
        @Getter private String siteId;

        @Override
        public SiteVolumeFactory getSiteVolumeFactory()
        {
            return ScormSiteVolumeFactory.this;
        }

        @Override
        public long getLastModified( FsItem item )
        {
            if( !getRoot().equals( item ) && item instanceof ScormFsItem )
            {
                ContentPackage cp = ((ScormFsItem) item).getContentPackage();
                return cp != null && cp.getModifiedOn() != null ? cp.getModifiedOn().getTime() / 1000 : 0L;
            }

            return 0L;
        }

        @Override
        public String getMimeType( FsItem item )
        {
            return isFolder( item ) ? DIRECTORY : SCORM_TYPE;
        }

        @Override
        public boolean isFolder( FsItem item )
        {
            return item instanceof ScormFsItem && ((ScormFsItem) item).getId().equals( "" );
        }

        @Override
        public String getName()
        {
            return ScormConstants.SCORM_DFLT_TOOL_NAME;
        }

        @Override
        public String getName( FsItem item )
        {
            if( getRoot().equals( item ) )
            {
                return getName();
            }
            if( item instanceof ScormFsItem )
            {
                ContentPackage cp = ((ScormFsItem) item).getContentPackage();
                return cp.getTitle();
            }
            else
            {
                throw new IllegalArgumentException( "Could not get title for: " + item.toString() );
            }
        }

        @Override
        public FsItem getRoot()
        {
            return new ScormFsItem( "", this );
        }

        @Override
        public FsItem getParent( FsItem item )
        {
            if( getRoot().equals( item ) )
            {
                return service.getSiteVolume( siteId ).getRoot();
            }
            else if( item instanceof ScormFsItem )
            {
                return getRoot();
            }

            return null;
        }

        @Override
        public boolean hasChildFolder( FsItem item )
        {
            return item instanceof ScormFsItem;
        }

        @Override
        public String getPath( FsItem item ) throws IOException
        {
            if( getRoot().equals( item ) )
            {
                return "";
            }
            else if( item instanceof ScormFsItem )
            {
                ScormFsItem scormItem = (ScormFsItem) item;
                return "/" + getPrefix() + "/" + scormItem.getContentPackage().getContentPackageId();
            }

            throw new IllegalArgumentException( "Wrong Type: " + item.toString() );
        }

        @Override
        public FsItem fromPath( String path )
        {
            if( StringUtils.isNotEmpty( path ) )
            {
                String[] parts = path.split( "/" );
                if( parts.length > 2 && getPrefix().equals( parts[1] ) )
                {
                    try
                    {
                        ContentPackage cp = scormContentService.getContentPackage( Long.valueOf( parts[2] ) );
                        return new ScormFsItem( buildEntityID( cp ), this, cp );
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
        public FsItem[] listChildren( FsItem item )
        {
            List<FsItem> items = new ArrayList<>();

            // Top level for a site, get all SCORM modules...
            if( getRoot().equals( item ) )
            {
                for( ContentPackage cp : scormContentService.getContentPackages( siteId ) )
                {
                    items.add( new ScormFsItem( buildEntityID( cp ), this, cp ) );
                }
            }

            return items.toArray( new FsItem[items.size()] );
        }

        @Override
        public String getURL( FsItem item )
        {
            if( item instanceof ScormFsItem && StringUtils.isNotBlank( ((ScormFsItem) item).getId() ) )
            {
                ScormFsItem scormItem = ((ScormFsItem) item);
                String serverURL = serverConfigurationService.getServerUrl();
                return serverURL + SCORM_DIRECT_URL_PREFIX + scormItem.getId();
            }

            return null;
        }

        @Override
        public long getSize( FsItem item ) throws IOException
        {
            if( !getRoot().equals( item ) && item instanceof ScormFsItem && securityService.unlock( ScormConstants.PERM_CONFIG, siteService.siteReference( siteId ) ) )
            {
                ContentPackage cp = ((ScormFsItem) item).getContentPackage();
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
                log.warn( "Unexpected exception for SCORM linking", ex );
            }

            return null;
        }

        // Unimplemented methods below
        @Override public boolean        exists              ( FsItem item )                     { return false; }
        @Override public boolean        isRoot              ( FsItem item )                     { return false; }
        @Override public boolean        isWriteable         ( FsItem fsi  )                     { return false; }
        @Override public String         getDimensions       ( FsItem item )                     { return null;  }
        @Override public String         getThumbnailFileName( FsItem fsi  )                     { return null;  }
        @Override public InputStream    openInputStream     ( FsItem item ) throws IOException  { return null;  }
    }
}
