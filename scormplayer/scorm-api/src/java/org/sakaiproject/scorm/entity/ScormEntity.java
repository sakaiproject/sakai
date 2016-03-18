package org.sakaiproject.scorm.entity;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Wrapper to hold only the fields that we want to return to the EntityBroker.
 * 
 * @author bjones86
 */
public class ScormEntity implements Comparable<ScormEntity>
{
    private String siteID;
    private String toolID;
    private String contentPackageID;
    private String resourceID;
    private String title;

    // Constructors
    public ScormEntity() {}
    public ScormEntity( String siteID, String toolID, String contentPackageID, String resourceID, String title )
    {
        this.siteID             = siteID;
        this.toolID             = toolID;
        this.contentPackageID   = contentPackageID;
        this.resourceID         = resourceID;
        this.title              = title;
    }

    @Override
    public int compareTo( ScormEntity entity )
    {
        return ObjectUtils.compare( entity.getID(), this.getID() );
    }

    // Getters
    public String getSiteID()           { return this.siteID; }
    public String getToolID()           { return this.toolID; }
    public String getContentPackageID() { return this.contentPackageID; }
    public String getResourceID()       { return this.resourceID; }
    public String getTitle()            { return this.title; }
    public String getID()
    {
        if( StringUtils.isNotBlank( toolID ) && StringUtils.isNotBlank( contentPackageID ) && StringUtils.isNotBlank( resourceID ) )
        {
            return siteID           + ScormEntityProvider.ENTITY_PARAM_DELIMITER + 
                   toolID           + ScormEntityProvider.ENTITY_PARAM_DELIMITER + 
                   contentPackageID + ScormEntityProvider.ENTITY_PARAM_DELIMITER + 
                   resourceID       + ScormEntityProvider.ENTITY_PARAM_DELIMITER + 
                   title;
        }
        else
        {
            return null;
        }
    }

    // Setters
    public void setSiteID           ( String siteID )           { this.siteID = siteID; }
    public void setToolID           ( String toolID )           { this.toolID = toolID; }
    public void setContentPackageID ( String contentPackageID ) { this.contentPackageID = contentPackageID; }
    public void setResourceID       ( String resourceID )       { this.resourceID = resourceID; }
    public void setTitle            ( String title )            { this.title = title; }
}
