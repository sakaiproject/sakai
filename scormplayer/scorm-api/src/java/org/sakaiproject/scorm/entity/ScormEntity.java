/*
 * #%L
 * SCORM API
 * %%
 * Copyright (C) 2007 - 2016 Sakai Project
 * %%
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
 * #L%
 */
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
    private String titleEncoded;

    // Constructors
    public ScormEntity() {}
    public ScormEntity( String siteID, String toolID, String contentPackageID, String resourceID, String title, String titleEncoded )
    {
        this.siteID             = siteID;
        this.toolID             = toolID;
        this.contentPackageID   = contentPackageID;
        this.resourceID         = resourceID;
        this.title              = title;
        this.titleEncoded       = titleEncoded;
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
    public String getTitleEncoded()     { return this.titleEncoded; }
    public String getID()
    {
        if( StringUtils.isNotBlank( toolID ) && StringUtils.isNotBlank( contentPackageID ) && StringUtils.isNotBlank( resourceID ) )
        {
            return siteID           + ScormEntityProvider.ENTITY_PARAM_DELIMITER + 
                   toolID           + ScormEntityProvider.ENTITY_PARAM_DELIMITER + 
                   contentPackageID + ScormEntityProvider.ENTITY_PARAM_DELIMITER + 
                   resourceID       + ScormEntityProvider.ENTITY_PARAM_DELIMITER + 
                   titleEncoded;
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
    public void setTitleEncoded     ( String titleEncoded )     { this.titleEncoded = titleEncoded; }
}
