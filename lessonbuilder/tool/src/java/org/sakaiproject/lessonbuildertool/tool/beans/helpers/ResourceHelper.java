/**
 * Copyright (c) 2003-2014 The Apereo Foundation
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
package org.sakaiproject.lessonbuildertool.tool.beans.helpers;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.util.BaseResourceProperties;

/**
+ * Helper methods for working with resources
+ * @author plukasew
+ */
public class ResourceHelper
{
    public static final String URL_RESOURCE_TYPE = "org.sakaiproject.content.types.urlResource";
    public static final String URL_HAS_PROTOCOL_REGEX = "\\w+://.*";
    
    private ContentResource r;
    
    public ResourceHelper(ContentResource resource)
    {
        r = resource;
    }
    
    public ResourceProperties getResourceProperties()
    {
        if (r == null || r.getProperties() == null)
        {
            return new BaseResourceProperties();
        }

        return r.getProperties();
    }
    
    public String getResourceType()
    {
        if (r != null)
        {
            return StringUtils.trimToEmpty(r.getResourceType());
        }
            
        return "";
    }
    
    public boolean isNameCustom(String itemName)
    {
        // this handles both normal file resources and url resources that were added as "existing resources"
        boolean custom = itemName != null && !itemName.equalsIgnoreCase(getResourceProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME));
        
        // if custom by logic above, check for url resources that were added as new resources by Lessons
        // the item name for urls added this way will have a protocol prefix (ie. https://)
        // if the protocol prefix does not appear, consider this to be custom
        if (custom && URL_RESOURCE_TYPE.equals(getResourceType()))
        {
            custom = itemName != null && !itemName.matches(URL_HAS_PROTOCOL_REGEX);
        }
        
        return custom;
    }
    
    public boolean isDescCustom(String itemDescription)
    {
        String resDesc = StringUtils.trimToEmpty(getResourceProperties().getProperty(ResourceProperties.PROP_DESCRIPTION));
        String itemDesc = StringUtils.trimToEmpty(itemDescription);
        
        // if the descriptions are not equal, the item description is assumed to be custom
        return !resDesc.equalsIgnoreCase(itemDesc);
    }
}
