/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pluto.descriptors.servlet;

import org.apache.pluto.descriptors.common.IconDD;

/**
 * Models a &lt;jsp-property-group&gt; of a servlet
 * 2.4 deployment descriptor.
 */
public class JspPropertyGroupDD
{
    private String description = null;
    private String displayName = null;
    private IconDD icon = null;
    
    private String urlPattern = null;   
    private Boolean elIgnored = null;
    private String pageEncoding = null;
    private Boolean scriptingInvalid = null;
    private Boolean isXml = null;
    private String includePrelude = null;
    private String includeCoda = null;
    

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public void setDisplayName( String displayName )
    {
        this.displayName = displayName;
    }

    public IconDD getIcon()
    {
        return icon;
    }

    public void setIcon( IconDD icon )
    {
        this.icon = icon;
    }
    
    public Boolean getElIgnored()
    {
        return elIgnored;
    }
    
    public void setElIgnored( Boolean elIgnored )
    {
        this.elIgnored = elIgnored;
    }
    
    public String getIncludeCoda()
    {
        return includeCoda;
    }
    
    public void setIncludeCoda( String includeCoda )
    {
        this.includeCoda = includeCoda;
    }
    
    public String getIncludePrelude()
    {
        return includePrelude;
    }
    
    public void setIncludePrelude( String includePrelude )
    {
        this.includePrelude = includePrelude;
    }
    
    public Boolean getIsXml()
    {
        return isXml;
    }
    
    public void setIsXml( Boolean isXml )
    {
        this.isXml = isXml;
    }
    
    public String getPageEncoding()
    {
        return pageEncoding;
    }
    
    public void setPageEncoding( String pageEncoding )
    {
        this.pageEncoding = pageEncoding;
    }
    
    public Boolean getScriptingInvalid()
    {
        return scriptingInvalid;
    }
    
    public void setScriptingInvalid( Boolean scriptingInvalid )
    {
        this.scriptingInvalid = scriptingInvalid;
    }
    
    public String getUrlPattern()
    {
        return urlPattern;
    }
    
    public void setUrlPattern( String urlPattern )
    {
        this.urlPattern = urlPattern;
    }
}
