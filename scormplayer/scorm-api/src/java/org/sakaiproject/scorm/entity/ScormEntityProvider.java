/**
 * Copyright (c) 2007 The Apereo Foundation
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

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;

/**
 * Beans may implement this interface directly to provide "extension" capabilities to an existing
 * {@link CoreEntityProvider}. If you are the provider for a set of entities then you will want to
 * implement {@link CoreEntityProvider}, this interface is primarily for extending an existing
 * entity provider (adding extra functionality to one that is already registered
 * 
 * Usage:
 * 1) Implement this interface
 * 2) Implement any additional capabilities interfaces (optional, but it would be crazy not to do at least one)
 * 3) Create a spring bean definition in the Sakai application context (components.xml)
 * 4) Implement {@link AutoRegisterEntityProvider} or register this implementation some other way
 * 
 * @author Brian Jones (bjones86@uwo.ca)
 */
public interface ScormEntityProvider extends EntityProvider
{
    // Publicly available static string defining the entity prefix for SCORM entities
    public final static String ENTITY_PREFIX = "scorm";

    // Publicly available static string array defining the handled output formats
    public final static String[] HANDLED_OUTPUT_FORMATS = new String[] { Formats.HTML };

    // Publicly available static string defining the character used to seperate the parameters of the SCORM entity (siteID, toolID, contentPackageID, resourceID)
    public final static String ENTITY_PARAM_DELIMITER = ":";

    // Entity property keys
    public final static String SCORM_ENTITY_PROP_SITE_ID            = "siteID";
    public final static String SCORM_ENTITY_PROP_TOOL_ID            = "toolID";
    public final static String SCORM_ENTITY_PROP_CONTENT_PACKAGE_ID = "contentPackageID";
    public final static String SCORM_ENTITY_PROP_RESOURCE_ID        = "resourceID";
    public final static String SCORM_ENTITY_PROP_TITLE              = "title";
    public final static String SCORM_ENTITY_PROP_TITLE_ENCODED      = "titleEncoded";
}
