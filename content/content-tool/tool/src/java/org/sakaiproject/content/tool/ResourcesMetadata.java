/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

// package
package org.sakaiproject.content.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.content.api.ContentHostingHandlerResolver;
import org.sakaiproject.util.ResourceLoader;

/**
 * The class represents metadata properties.
 */
@Slf4j
public class ResourcesMetadata
{
	/** Resource bundle using current language locale */
	private ResourceLoader rb = new ResourceLoader("content");
    
	public static final String WIDGET_STRING = "string";
	public static final String WIDGET_TEXTAREA = "textarea";
	public static final String WIDGET_BOOLEAN = "boolean";
	public static final String WIDGET_INTEGER = "integer";
	public static final String WIDGET_DOUBLE = "double";
	public static final String WIDGET_DATE = "date";
	public static final String WIDGET_TIME = "time";
	public static final String WIDGET_DATETIME = "datetime";
	public static final String WIDGET_ANYURI = "anyURI";
	public static final String WIDGET_ENUM = "enumeration";
	public static final String WIDGET_NESTED = "nested";
	public static final String WIDGET_WYSIWYG = "wysiwig-editor";
	
	public static final String WIDGET_DURATION = "duration";

	public static final String WIDGET_DROPDOWN = "dropdown";

	
	public static final String XSD_STRING = "string";
	public static final String XSD_BOOLEAN = "boolean";	
	public static final String XSD_INTEGER = "integer";	
	public static final String XSD_FLOAT = "float";	
	public static final String XSD_DOUBLE = "double";	
	public static final String XSD_DATE = "date";	
	public static final String XSD_TIME = "time";	
	public static final String XSD_DATETIME = "dateTime";
	public static final String XSD_DURATION = "duration";	
	public static final String XSD_ANYURI = "anyURI";	
	public static final String XSD_NORMALIZED_STRING = "normalizedString";
	
	
	public static final String CLASS_SAKAI_RESOURCE_NAMESPACE = "http://sakaiproject.org/metadata#";
	public static final String CLASS_SAKAI_RESOURCE_LOCALNAME = "Resource";
	public static final String CLASS_SAKAI_RESOURCE_LABEL = "Resource";
	
	public static final String NAMESPACE_DC = "http://purl.org/dc/elements/1.1/";
	public static final String NAMESPACE_DC_ABBREV = "dc:";
	public static final String NAMESPACE_DCTERMS = "http://purl.org/dc/terms/";
	public static final String NAMESPACE_DCTERMS_ABBREV = "dcterms:";
	public static final String NAMESPACE_XSD = "http://www.w3.org/2001/XMLSchema#";
	public static final String NAMESPACE_XSD_ABBREV = "xs:";
	public static final String NAMESPACE_LOM = "http://ltsc.ieee.org/xsd/lomv1.0/";
	public static final String NAMESPACE_LOM_ABBREV = "lom:";
	

	protected static AtomicInteger namespaceNumber = new AtomicInteger(0);
	
	public static final String PROPERTY_NAME_DC_TITLE = "title";
	public static final String PROPERTY_LABEL_DC_TITLE = "label.dc_title";
	public static final String PROPERTY_DESCRIPTION_DC_TITLE = "descr.dc_title";
	public static final String PROPERTY_TYPE_DC_TITLE = NAMESPACE_XSD + XSD_STRING;
	public static final String PROPERTY_WIDGET_DC_TITLE = WIDGET_STRING;
	
	public static final ResourcesMetadata PROPERTY_DC_TITLE
		= new ResourcesMetadata(
								NAMESPACE_DC,
								PROPERTY_NAME_DC_TITLE, 
								PROPERTY_LABEL_DC_TITLE,
								PROPERTY_DESCRIPTION_DC_TITLE,
								PROPERTY_TYPE_DC_TITLE,
								PROPERTY_WIDGET_DC_TITLE
							);

	public static final String PROPERTY_NAME_DC_ALTERNATIVE = "alternative";
	public static final String PROPERTY_LABEL_DC_ALTERNATIVE = "label.dc_alt";
	public static final String PROPERTY_DESCRIPTION_DC_ALTERNATIVE = "descr.dc_alt";
	public static final String PROPERTY_TYPE_DC_ALTERNATIVE = NAMESPACE_XSD + XSD_STRING;
	public static final String PROPERTY_WIDGET_DC_ALTERNATIVE = WIDGET_STRING;
		
	public static final ResourcesMetadata PROPERTY_DC_ALTERNATIVE
		= new ResourcesMetadata(
								NAMESPACE_DC,
								PROPERTY_NAME_DC_ALTERNATIVE, 
								PROPERTY_LABEL_DC_ALTERNATIVE,
								PROPERTY_DESCRIPTION_DC_ALTERNATIVE,
								PROPERTY_TYPE_DC_ALTERNATIVE,
								PROPERTY_WIDGET_DC_ALTERNATIVE
							);
	
	public static final String PROPERTY_NAME_DC_CREATOR = "creator";
	public static final String PROPERTY_LABEL_DC_CREATOR = "label.dc_creator";
	public static final String PROPERTY_DESCRIPTION_DC_CREATOR = "descr.dc_creator";
	public static final String PROPERTY_TYPE_DC_CREATOR = NAMESPACE_XSD + XSD_STRING;
	public static final String PROPERTY_WIDGET_DC_CREATOR = WIDGET_STRING;
	
	public static final ResourcesMetadata PROPERTY_DC_CREATOR
		= new ResourcesMetadata(
								NAMESPACE_DC,
								PROPERTY_NAME_DC_CREATOR, 
								PROPERTY_LABEL_DC_CREATOR,
								PROPERTY_DESCRIPTION_DC_CREATOR,
								PROPERTY_TYPE_DC_CREATOR,
								PROPERTY_WIDGET_DC_CREATOR
							);
	
	public static final String PROPERTY_NAME_DC_SUBJECT = "subject";
	public static final String PROPERTY_LABEL_DC_SUBJECT = "label.dc_subject";
	public static final String PROPERTY_DESCRIPTION_DC_SUBJECT = "descr.dc_subject";
	public static final String PROPERTY_TYPE_DC_SUBJECT = NAMESPACE_XSD + XSD_STRING;
	public static final String PROPERTY_WIDGET_DC_SUBJECT = WIDGET_TEXTAREA;
	
	public static final ResourcesMetadata PROPERTY_DC_SUBJECT
		= new ResourcesMetadata(
								NAMESPACE_DC,
								PROPERTY_NAME_DC_SUBJECT, 
								PROPERTY_LABEL_DC_SUBJECT,
								PROPERTY_DESCRIPTION_DC_SUBJECT,
								PROPERTY_TYPE_DC_SUBJECT,
								PROPERTY_WIDGET_DC_SUBJECT
							);

	public static final String PROPERTY_NAME_DC_DESCRIPTION = "description";
	public static final String PROPERTY_LABEL_DC_DESCRIPTION = "label.dc_descr";
	public static final String PROPERTY_DESCRIPTION_DC_DESCRIPTION = "descr.dc_descr";
	public static final String PROPERTY_TYPE_DC_DESCRIPTION = NAMESPACE_XSD + XSD_STRING;
	public static final String PROPERTY_WIDGET_DC_DESCRIPTION = WIDGET_TEXTAREA;
	
	public static final ResourcesMetadata PROPERTY_DC_DESCRIPTION
		= new ResourcesMetadata(
								NAMESPACE_DC,
								PROPERTY_NAME_DC_DESCRIPTION, 
								PROPERTY_LABEL_DC_DESCRIPTION,
								PROPERTY_DESCRIPTION_DC_DESCRIPTION,
								PROPERTY_TYPE_DC_DESCRIPTION,
								PROPERTY_WIDGET_DC_DESCRIPTION
							);

	public static final String PROPERTY_NAME_DC_PUBLISHER = "publisher";
	public static final String PROPERTY_LABEL_DC_PUBLISHER = "label.dc_publisher";
	public static final String PROPERTY_DESCRIPTION_DC_PUBLISHER = "descr.dc_publisher";
	public static final String PROPERTY_TYPE_DC_PUBLISHER = NAMESPACE_XSD + XSD_STRING;
	public static final String PROPERTY_WIDGET_DC_PUBLISHER = WIDGET_STRING;
	
	public static final ResourcesMetadata PROPERTY_DC_PUBLISHER
		= new ResourcesMetadata(
								NAMESPACE_DC,
								PROPERTY_NAME_DC_PUBLISHER, 
								PROPERTY_LABEL_DC_PUBLISHER,
								PROPERTY_DESCRIPTION_DC_PUBLISHER,
								PROPERTY_TYPE_DC_PUBLISHER,
								PROPERTY_WIDGET_DC_PUBLISHER
							);

	public static final String PROPERTY_NAME_DC_CONTRIBUTOR = "contributor";
	public static final String PROPERTY_LABEL_DC_CONTRIBUTOR = "label.dc_contributor";
	public static final String PROPERTY_DESCRIPTION_DC_CONTRIBUTOR = "descr.dc_contributor";
	public static final String PROPERTY_TYPE_DC_CONTRIBUTOR = NAMESPACE_XSD + XSD_STRING;
	public static final String PROPERTY_WIDGET_DC_CONTRIBUTOR = WIDGET_TEXTAREA;
	
	public static final ResourcesMetadata PROPERTY_DC_CONTRIBUTOR
		= new ResourcesMetadata(
								NAMESPACE_DC,
								PROPERTY_NAME_DC_CONTRIBUTOR, 
								PROPERTY_LABEL_DC_CONTRIBUTOR,
								PROPERTY_DESCRIPTION_DC_CONTRIBUTOR,
								PROPERTY_TYPE_DC_CONTRIBUTOR,
								PROPERTY_WIDGET_DC_CONTRIBUTOR
							);

	public static final String PROPERTY_NAME_DC_TYPE = "type";
	public static final String PROPERTY_LABEL_DC_TYPE = "label.dc_type";
	public static final String PROPERTY_DESCRIPTION_DC_TYPE = "descr.dc_type";
	public static final String PROPERTY_TYPE_DC_TYPE = NAMESPACE_XSD + XSD_STRING;
	public static final String PROPERTY_WIDGET_DC_TYPE = WIDGET_STRING;

	public static final ResourcesMetadata PROPERTY_DC_TYPE
		= new ResourcesMetadata(
								NAMESPACE_DC,
								PROPERTY_NAME_DC_TYPE, 
								PROPERTY_LABEL_DC_TYPE,
								PROPERTY_DESCRIPTION_DC_TYPE,
								PROPERTY_TYPE_DC_TYPE,
								PROPERTY_WIDGET_DC_TYPE
							);

	public static final String PROPERTY_NAME_DC_FORMAT = "format";
	public static final String PROPERTY_LABEL_DC_FORMAT = "label.dc_format";
	public static final String PROPERTY_DESCRIPTION_DC_FORMAT = "descr.dc_format";
	public static final String PROPERTY_TYPE_DC_FORMAT = NAMESPACE_XSD + XSD_STRING;
	public static final String PROPERTY_WIDGET_DC_FORMAT = WIDGET_STRING;
	
	public static final ResourcesMetadata PROPERTY_DC_FORMAT
		= new ResourcesMetadata(
								NAMESPACE_DC,
								PROPERTY_NAME_DC_FORMAT, 
								PROPERTY_LABEL_DC_FORMAT,
								PROPERTY_DESCRIPTION_DC_FORMAT,
								PROPERTY_TYPE_DC_FORMAT,
								PROPERTY_WIDGET_DC_FORMAT
							);

	public static final String PROPERTY_NAME_DC_IDENTIFIER = "identifier";
	public static final String PROPERTY_LABEL_DC_IDENTIFIER = "label.dc_id";
	public static final String PROPERTY_DESCRIPTION_DC_IDENTIFIER = "descr.dc_id";
	public static final String PROPERTY_TYPE_DC_IDENTIFIER = NAMESPACE_XSD + XSD_STRING;
	public static final String PROPERTY_WIDGET_DC_IDENTIFIER = WIDGET_STRING; // WIDGET_ANYURI;
	
	public static final ResourcesMetadata PROPERTY_DC_IDENTIFIER
		= new ResourcesMetadata(
								NAMESPACE_DC,
								PROPERTY_NAME_DC_IDENTIFIER, 
								PROPERTY_LABEL_DC_IDENTIFIER,
								PROPERTY_DESCRIPTION_DC_IDENTIFIER,
								PROPERTY_TYPE_DC_IDENTIFIER,
								PROPERTY_WIDGET_DC_IDENTIFIER
							);

	public static final String PROPERTY_NAME_DC_SOURCE = "source";
	public static final String PROPERTY_LABEL_DC_SOURCE = "label.dc_source";
	public static final String PROPERTY_DESCRIPTION_DC_SOURCE = "descr.dc_source";
	public static final String PROPERTY_TYPE_DC_SOURCE = NAMESPACE_XSD + XSD_STRING;
	public static final String PROPERTY_WIDGET_DC_SOURCE = WIDGET_STRING;
	
	public static final ResourcesMetadata PROPERTY_DC_SOURCE
		= new ResourcesMetadata(
								NAMESPACE_DC,
								PROPERTY_NAME_DC_SOURCE, 
								PROPERTY_LABEL_DC_SOURCE,
								PROPERTY_DESCRIPTION_DC_SOURCE,
								PROPERTY_TYPE_DC_SOURCE,
								PROPERTY_WIDGET_DC_SOURCE
							);

	public static final String PROPERTY_NAME_DC_LANGUAGE = "language";
	public static final String PROPERTY_LABEL_DC_LANGUAGE = "label.dc_lang";
	public static final String PROPERTY_DESCRIPTION_DC_LANGUAGE = "descr.dc_lang";
	public static final String PROPERTY_TYPE_DC_LANGUAGE = NAMESPACE_XSD + XSD_STRING;
	public static final String PROPERTY_WIDGET_DC_LANGUAGE = WIDGET_STRING;

	public static final ResourcesMetadata PROPERTY_DC_LANGUAGE
		= new ResourcesMetadata(
								NAMESPACE_DC,
								PROPERTY_NAME_DC_LANGUAGE, 
								PROPERTY_LABEL_DC_LANGUAGE,
								PROPERTY_DESCRIPTION_DC_LANGUAGE,
								PROPERTY_TYPE_DC_LANGUAGE,
								PROPERTY_WIDGET_DC_LANGUAGE
							);

	public static final String PROPERTY_NAME_DC_COVERAGE = "coverage";
	public static final String PROPERTY_LABEL_DC_COVERAGE = "label.dc_coverage";
	public static final String PROPERTY_DESCRIPTION_DC_COVERAGE = "descr.dc_coverage";
	public static final String PROPERTY_TYPE_DC_COVERAGE = NAMESPACE_XSD + XSD_STRING;
	public static final String PROPERTY_WIDGET_DC_COVERAGE = WIDGET_STRING;

	public static final ResourcesMetadata PROPERTY_DC_COVERAGE
		= new ResourcesMetadata(
								NAMESPACE_DC,
								PROPERTY_NAME_DC_COVERAGE, 
								PROPERTY_LABEL_DC_COVERAGE,
								PROPERTY_DESCRIPTION_DC_COVERAGE,
								PROPERTY_TYPE_DC_COVERAGE,
								PROPERTY_WIDGET_DC_COVERAGE
							);

	public static final String PROPERTY_NAME_DC_RIGHTS = "rights";
	public static final String PROPERTY_LABEL_DC_RIGHTS = "label.dc_rights";
	public static final String PROPERTY_DESCRIPTION_DC_RIGHTS = "descr.dc_rights";
	public static final String PROPERTY_TYPE_DC_RIGHTS = NAMESPACE_XSD + XSD_STRING;
	public static final String PROPERTY_WIDGET_DC_RIGHTS = WIDGET_STRING;

	public static final ResourcesMetadata PROPERTY_DC_RIGHTS
		= new ResourcesMetadata(
								NAMESPACE_DC,
								PROPERTY_NAME_DC_RIGHTS, 
								PROPERTY_LABEL_DC_RIGHTS,
								PROPERTY_DESCRIPTION_DC_RIGHTS,
								PROPERTY_TYPE_DC_RIGHTS,
								PROPERTY_WIDGET_DC_RIGHTS
							);

	public static final String PROPERTY_NAME_DC_AUDIENCE = "audience";
	public static final String PROPERTY_LABEL_DC_AUDIENCE = "label.dc_audience";
	public static final String PROPERTY_DESCRIPTION_DC_AUDIENCE = "descr.dc_audience";
	public static final String PROPERTY_TYPE_DC_AUDIENCE = NAMESPACE_XSD + XSD_STRING;
	public static final String PROPERTY_WIDGET_DC_AUDIENCE = WIDGET_STRING;

	public static final ResourcesMetadata PROPERTY_DC_AUDIENCE
		= new ResourcesMetadata(
								NAMESPACE_DCTERMS,
								PROPERTY_NAME_DC_AUDIENCE, 
								PROPERTY_LABEL_DC_AUDIENCE,
								PROPERTY_DESCRIPTION_DC_AUDIENCE,
								PROPERTY_TYPE_DC_AUDIENCE,
								PROPERTY_WIDGET_DC_AUDIENCE
							);

	public static final String PROPERTY_NAME_DC_TABLEOFCONTENTS = "tableOfContents";
	public static final String PROPERTY_LABEL_DC_TABLEOFCONTENTS = "label.dc_toc";
	public static final String PROPERTY_DESCRIPTION_DC_TABLEOFCONTENTS = "descr.dc_toc";
	public static final String PROPERTY_TYPE_DC_TABLEOFCONTENTS = NAMESPACE_XSD + XSD_STRING;
	public static final String PROPERTY_WIDGET_DC_TABLEOFCONTENTS = WIDGET_TEXTAREA;
	
	public static final ResourcesMetadata PROPERTY_DC_TABLEOFCONTENTS
		= new ResourcesMetadata(
								NAMESPACE_DCTERMS,
								PROPERTY_NAME_DC_TABLEOFCONTENTS, 
								PROPERTY_LABEL_DC_TABLEOFCONTENTS,
								PROPERTY_DESCRIPTION_DC_TABLEOFCONTENTS,
								PROPERTY_TYPE_DC_TABLEOFCONTENTS,
								PROPERTY_WIDGET_DC_TABLEOFCONTENTS
							);

	public static final String PROPERTY_NAME_DC_ABSTRACT = "abstract";
	public static final String PROPERTY_LABEL_DC_ABSTRACT = "label.dc_abstract";
	public static final String PROPERTY_DESCRIPTION_DC_ABSTRACT = "descr.dc_abstract";
	public static final String PROPERTY_TYPE_DC_ABSTRACT = NAMESPACE_XSD + XSD_STRING;
	public static final String PROPERTY_WIDGET_DC_ABSTRACT = WIDGET_TEXTAREA;
	
	public static final ResourcesMetadata PROPERTY_DC_ABSTRACT
		= new ResourcesMetadata(
								NAMESPACE_DCTERMS,
								PROPERTY_NAME_DC_ABSTRACT, 
								PROPERTY_LABEL_DC_ABSTRACT,
								PROPERTY_DESCRIPTION_DC_ABSTRACT,
								PROPERTY_TYPE_DC_ABSTRACT,
								PROPERTY_WIDGET_DC_ABSTRACT
							);

	public static final String PROPERTY_NAME_DC_CREATED = "created";
	public static final String PROPERTY_LABEL_DC_CREATED = "label.dc_created";
	public static final String PROPERTY_DESCRIPTION_DC_CREATED = "descr.dc_created";
	public static final String PROPERTY_TYPE_DC_CREATED = NAMESPACE_XSD + XSD_NORMALIZED_STRING;  //XSD_DATE;
	public static final String PROPERTY_WIDGET_DC_CREATED = WIDGET_STRING; // WIDGET_DATE;
	
	public static final ResourcesMetadata PROPERTY_DC_CREATED
		= new ResourcesMetadata(
								NAMESPACE_DCTERMS,
								PROPERTY_NAME_DC_CREATED, 
								PROPERTY_LABEL_DC_CREATED,
								PROPERTY_DESCRIPTION_DC_CREATED,
								PROPERTY_TYPE_DC_CREATED,
								PROPERTY_WIDGET_DC_CREATED
							);

	public static final String PROPERTY_NAME_DC_ISSUED = "issued";
	public static final String PROPERTY_LABEL_DC_ISSUED = "label.dc_issued";
	public static final String PROPERTY_DESCRIPTION_DC_ISSUED = "descr.dc_issued";
	public static final String PROPERTY_TYPE_DC_ISSUED = NAMESPACE_XSD + XSD_NORMALIZED_STRING;  //XSD_DATE;
	public static final String PROPERTY_WIDGET_DC_ISSUED = WIDGET_STRING; // WIDGET_DATE;
	
	public static final ResourcesMetadata PROPERTY_DC_ISSUED
		= new ResourcesMetadata(
								NAMESPACE_DCTERMS,
								PROPERTY_NAME_DC_ISSUED, 
								PROPERTY_LABEL_DC_ISSUED,
								PROPERTY_DESCRIPTION_DC_ISSUED,
								PROPERTY_TYPE_DC_ISSUED,
								PROPERTY_WIDGET_DC_ISSUED
							);

	public static final String PROPERTY_NAME_DC_MODIFIED = "modified";
	public static final String PROPERTY_LABEL_DC_MODIFIED = "label.dc_modified";
	public static final String PROPERTY_DESCRIPTION_DC_MODIFIED = "descr.dc_modified";
	public static final String PROPERTY_TYPE_DC_MODIFIED = NAMESPACE_XSD + XSD_NORMALIZED_STRING;  //XSD_DATE;
	public static final String PROPERTY_WIDGET_DC_MODIFIED = WIDGET_STRING; // WIDGET_DATE;
	
	public static final ResourcesMetadata PROPERTY_DC_MODIFIED
		= new ResourcesMetadata(
								NAMESPACE_DCTERMS,
								PROPERTY_NAME_DC_MODIFIED, 
								PROPERTY_LABEL_DC_MODIFIED,
								PROPERTY_DESCRIPTION_DC_MODIFIED,
								PROPERTY_TYPE_DC_MODIFIED,
								PROPERTY_WIDGET_DC_MODIFIED
							);

	public static final String PROPERTY_NAME_DC_EDULEVEL = "educationLevel";
	public static final String PROPERTY_LABEL_DC_EDULEVEL = "label.dc_edlevel";
	public static final String PROPERTY_DESCRIPTION_DC_EDULEVEL = "descr.dc_edlevel";
	public static final String PROPERTY_TYPE_DC_EDULEVEL = NAMESPACE_XSD + XSD_STRING;
	public static final String PROPERTY_WIDGET_DC_EDULEVEL = WIDGET_TEXTAREA;
	
	public static final ResourcesMetadata PROPERTY_DC_EDULEVEL
		= new ResourcesMetadata(
								NAMESPACE_DCTERMS,
								PROPERTY_NAME_DC_EDULEVEL, 
								PROPERTY_LABEL_DC_EDULEVEL,
								PROPERTY_DESCRIPTION_DC_EDULEVEL,
								PROPERTY_TYPE_DC_EDULEVEL,
								PROPERTY_WIDGET_DC_EDULEVEL
							);

	/* File System mount points */
	public static final String PROPERTY_NAME_FSMOUNT_NAMESPACE = ContentHostingHandlerResolver.CHH_BEAN_NAME.split(":")[0];
	public static final String PROPERTY_NAME_FSMOUNT_ACTIVE = ContentHostingHandlerResolver.CHH_BEAN_NAME.split(":")[1];
	public static final String PROPERTY_LABEL_FSMOUNT_ACTIVE = "label.fsmount_active";
	public static final String PROPERTY_DESCRIPTION_FSMOUNT_ACTIVE = "descr.fsmount_active";
	// TYPE should be a BOOLEAN but that does not appear to be implemented -- miserable.
	// Instead, we store a string and ask users to type "YES" or "NO".  Yes, this sucks.
	public static final String PROPERTY_TYPE_FSMOUNT_ACTIVE = NAMESPACE_XSD + XSD_STRING;
	public static final String PROPERTY_WIDGET_FSMOUNT_ACTIVE = WIDGET_STRING;
	
	public static final ResourcesMetadata PROPERTY_FSMOUNT_ACTIVE
		= new ResourcesMetadata(
								PROPERTY_NAME_FSMOUNT_NAMESPACE+":",
								PROPERTY_NAME_FSMOUNT_ACTIVE, 
								PROPERTY_LABEL_FSMOUNT_ACTIVE,
								PROPERTY_DESCRIPTION_FSMOUNT_ACTIVE,
								PROPERTY_TYPE_FSMOUNT_ACTIVE,
								PROPERTY_WIDGET_FSMOUNT_ACTIVE
							);
	
	
	
	
	public static final ResourcesMetadata PROPERTY_DC_BOOLEAN
		= new ResourcesMetadata(
								NAMESPACE_XSD,
								XSD_BOOLEAN, 
								WIDGET_BOOLEAN,
								"Test " + WIDGET_BOOLEAN,
								NAMESPACE_XSD + XSD_BOOLEAN,
								WIDGET_BOOLEAN
							);

	public static final ResourcesMetadata PROPERTY_DC_DATE
		= new ResourcesMetadata(
								NAMESPACE_XSD,
								XSD_DATE, 
								WIDGET_DATE,
								"Test " + WIDGET_DATE,
								NAMESPACE_XSD + XSD_DATE,
								WIDGET_DATE
							);

	public static final ResourcesMetadata PROPERTY_DC_TIME
		= new ResourcesMetadata(
								NAMESPACE_XSD,
								XSD_TIME, 
								WIDGET_TIME,
								"Test " + WIDGET_TIME,
								NAMESPACE_XSD + XSD_TIME,
								WIDGET_TIME
							);

	public static final ResourcesMetadata PROPERTY_DC_DATETIME
		= new ResourcesMetadata(
								NAMESPACE_XSD,
								XSD_DATETIME, 
								WIDGET_DATETIME,
								"Test " + WIDGET_DATETIME,
								NAMESPACE_XSD + XSD_DATETIME,
								WIDGET_DATETIME
							);

	public static final ResourcesMetadata PROPERTY_DC_INTEGER
		= new ResourcesMetadata(
								NAMESPACE_XSD,
								XSD_INTEGER, 
								WIDGET_INTEGER,
								"Test " + WIDGET_INTEGER,
								NAMESPACE_XSD + XSD_INTEGER,
								WIDGET_INTEGER
							);

	public static final ResourcesMetadata PROPERTY_DC_DOUBLE
		= new ResourcesMetadata(
								NAMESPACE_XSD,
								XSD_DOUBLE, 
								WIDGET_DOUBLE,
								"Test " + WIDGET_DOUBLE,
								NAMESPACE_XSD + XSD_DOUBLE,
								WIDGET_DOUBLE
							);

	public static final ResourcesMetadata PROPERTY_DC_ANYURI
		= new ResourcesMetadata(
								NAMESPACE_XSD,
								XSD_ANYURI, 
								WIDGET_ANYURI,
								"Test " + WIDGET_ANYURI,
								NAMESPACE_XSD + XSD_ANYURI,
								WIDGET_ANYURI
							);

	/* LOM role */
	public static final ResourcesMetadata PROPERTY_LOM_ROLE
		= new ResourcesMetadata(
								NAMESPACE_LOM,
								"role", 
								"label.lom_role",
								"descr.lom_role",
								NAMESPACE_XSD + XSD_STRING,
								WIDGET_STRING
							);
	
	/* LOM coverage */
	public static final ResourcesMetadata PROPERTY_LOM_COVERAGE
		= new ResourcesMetadata(
								NAMESPACE_LOM,
								"coverage", 
								"label.lom_coverage",
								"descr.lom_coverage",
								NAMESPACE_XSD + XSD_STRING,
								WIDGET_STRING
							);
	
	/* LOM status */
	public static final ResourcesMetadata PROPERTY_LOM_STATUS
		= new ResourcesMetadata(
								NAMESPACE_LOM,
								"status", 
								"label.lom_status",
								"descr.lom_status",
								NAMESPACE_XSD + XSD_STRING,
								WIDGET_DROPDOWN
							);
	
	/* LOM duration */
	public static final ResourcesMetadata PROPERTY_LOM_DURATION
		= new ResourcesMetadata(
								NAMESPACE_LOM,
								"duration", 
								"label.lom_duration",
								"descr.lom_duration",
								NAMESPACE_XSD + XSD_TIME,
								WIDGET_DURATION
							);
	
	/* LOM engagement type */
	public static final ResourcesMetadata PROPERTY_LOM_ENGAGEMENT_TYPE
		= new ResourcesMetadata(
								NAMESPACE_LOM,
								"engagement", 
								"label.lom_engagement",
								"descr.lom_engagement",
								NAMESPACE_XSD + XSD_STRING,
								WIDGET_DROPDOWN
							);
	
	/* LOM learning resource type */
	public static final ResourcesMetadata PROPERTY_LOM_LEARNING_RESOURCE_TYPE
		= new ResourcesMetadata(
								NAMESPACE_LOM,
								"learning_resource_type", 
								"label.lom_learning_resource_type",
								"descr.lom_learning_resource_type",
								NAMESPACE_XSD + XSD_STRING,
								WIDGET_DROPDOWN
							);
	
	/* LOM interactivity level */
	public static final ResourcesMetadata PROPERTY_LOM_INTERACTIVITY_LEVEL
		= new ResourcesMetadata(
								NAMESPACE_LOM,
								"interactivity_level", 
								"label.lom_interactivity_level",
								"descr.lom_interactivity_level",
								NAMESPACE_XSD + XSD_STRING,
								WIDGET_DROPDOWN
							);
	
	/* LOM context level */
	public static final ResourcesMetadata PROPERTY_LOM_CONTEXT_LEVEL
		= new ResourcesMetadata(
								NAMESPACE_LOM,
								"context_level", 
								"label.lom_context_level",
								"descr.lom_context_level",
								NAMESPACE_XSD + XSD_STRING,
								WIDGET_DROPDOWN
							);

	/* LOM difficulty */
	public static final ResourcesMetadata PROPERTY_LOM_DIFFICULTY
		= new ResourcesMetadata(
								NAMESPACE_LOM,
								"difficulty", 
								"label.lom_difficulty",
								"descr.lom_difficulty",
								NAMESPACE_XSD + XSD_STRING,
								WIDGET_DROPDOWN
							);
	
	/* LOM learning time */
	public static final ResourcesMetadata PROPERTY_LOM_LEARNING_TIME
		= new ResourcesMetadata(
								NAMESPACE_LOM,
								"learning_time", 
								"label.lom_learning_time",
								"descr.lom_learning_time",
								NAMESPACE_XSD + XSD_STRING,
								WIDGET_DURATION
							);
	
	/* LOM assumed knowledge */
	public static final ResourcesMetadata PROPERTY_LOM_ASSUMED_KNOWLEDGE
		= new ResourcesMetadata(
								NAMESPACE_LOM,
								"assumed_knowledge", 
								"label.lom_assumed_knowledge",
								"descr.lom_assumed_knowledge",
								NAMESPACE_XSD + XSD_STRING,
								WIDGET_TEXTAREA
							);
	
	/* LOM tech req */
	public static final ResourcesMetadata PROPERTY_LOM_TECHNICAL_REQUIREMENTS
		= new ResourcesMetadata(
								NAMESPACE_LOM,
								"technical_requirements", 
								"label.lom_technical_requirements",
								"descr.lom_technical_requirements",
								NAMESPACE_XSD + XSD_STRING,
								WIDGET_TEXTAREA
							);
	
	/* LOM install remarks */
	public static final ResourcesMetadata PROPERTY_LOM_INSTALL_REMARKS
		= new ResourcesMetadata(
								NAMESPACE_LOM,
								"install_remarks", 
								"label.lom_install_remarks",
								"descr.lom_install_remarks",
								NAMESPACE_XSD + XSD_STRING,
								WIDGET_TEXTAREA
							);
	
	/* LOM other requirements */
	public static final ResourcesMetadata PROPERTY_LOM_OTHER_REQUIREMENTS
		= new ResourcesMetadata(
								NAMESPACE_LOM,
								"other_requirements", 
								"label.lom_other_requirements",
								"descr.lom_other_requirements",
								NAMESPACE_XSD + XSD_STRING,
								WIDGET_TEXTAREA
							);
	
	/* LOM granularity level */
	public static final ResourcesMetadata PROPERTY_LOM_GRANULARITY_LEVEL
		= new ResourcesMetadata(
								NAMESPACE_LOM,
								"granularity_level", 
								"label.lom_granularity_level",
								"descr.lom_granularity_level",
								NAMESPACE_XSD + XSD_STRING,
								WIDGET_DROPDOWN
							);
	
	
	/* LOM structure */
	public static final ResourcesMetadata PROPERTY_LOM_STRUCTURE
		= new ResourcesMetadata(
								NAMESPACE_LOM,
								"structure", 
								"label.lom_structure",
								"descr.lom_structure",
								NAMESPACE_XSD + XSD_STRING,
								WIDGET_DROPDOWN
							);
	
	/* LOM relation */
	public static final ResourcesMetadata PROPERTY_LOM_RELATION
		= new ResourcesMetadata(
								NAMESPACE_LOM,
								"relation", 
								"label.lom_relation",
								"descr.lom_relation",
								NAMESPACE_XSD + XSD_STRING,
								WIDGET_TEXTAREA
							);
	
	/* LOM reviewer */
	public static final ResourcesMetadata PROPERTY_LOM_REVIEWER
		= new ResourcesMetadata(
								NAMESPACE_LOM,
								"reviewer", 
								"label.lom_reviewer",
								"descr.lom_reviewer",
								NAMESPACE_XSD + XSD_STRING,
								WIDGET_STRING
							);
	
	/* LOM review date */
	public static final ResourcesMetadata PROPERTY_LOM_REVIEW_DATE
		= new ResourcesMetadata(
								NAMESPACE_LOM,
								"review_date", 
								"label.lom_review_date",
								"descr.lom_review_date",
								NAMESPACE_XSD + XSD_STRING,
								WIDGET_STRING
							);
	
	/* LOM review comments */
	public static final ResourcesMetadata PROPERTY_LOM_REVIEW_COMMENTS
		= new ResourcesMetadata(
								NAMESPACE_LOM,
								"review_comments", 
								"label.lom_review_comments",
								"descr.lom_review_comments",
								NAMESPACE_XSD + XSD_STRING,
								WIDGET_TEXTAREA
							);	
	

	/*
	public static final String PROPERTY_NAME_DC_ = "";
	public static final String PROPERTY_LABEL_DC_ = "";
	public static final String PROPERTY_DESCRIPTION_DC_ = 
			"";
	public static final String PROPERTY_TYPE_DC_ = XSD_STRING;
	public static final String PROPERTY_WIDGET_DC_ = WIDGET_STRING;
	
	public static final ResourcesMetadata PROPERTY_DC_
		= new ResourcesMetadata(
								PROPERTY_NAME_DC_, 
								PROPERTY_LABEL_DC_,
								PROPERTY_DESCRIPTION_DC_,
								PROPERTY_TYPE_DC_,
								PROPERTY_WIDGET_DC_
							);

*/

	public static final String[] DublinCore =	
		{
			PROPERTY_NAME_DC_TITLE,
			PROPERTY_NAME_DC_ALTERNATIVE,
			PROPERTY_NAME_DC_CREATOR,
			PROPERTY_NAME_DC_SUBJECT,
			PROPERTY_NAME_DC_DESCRIPTION,
			PROPERTY_NAME_DC_TABLEOFCONTENTS,
			PROPERTY_NAME_DC_ABSTRACT,
			PROPERTY_NAME_DC_PUBLISHER,
			PROPERTY_NAME_DC_CONTRIBUTOR,
			PROPERTY_NAME_DC_TYPE,
			PROPERTY_NAME_DC_FORMAT,
			PROPERTY_NAME_DC_CREATED,
			PROPERTY_NAME_DC_ISSUED,
			PROPERTY_NAME_DC_MODIFIED,
			PROPERTY_NAME_DC_IDENTIFIER,
			PROPERTY_NAME_DC_SOURCE,
			PROPERTY_NAME_DC_LANGUAGE,
			PROPERTY_NAME_DC_COVERAGE,
			PROPERTY_NAME_DC_RIGHTS,
			PROPERTY_NAME_DC_AUDIENCE,
			PROPERTY_NAME_DC_EDULEVEL
		};
		

	public static final ResourcesMetadata[] DEFINED_PROPERTIES =	
		{
			PROPERTY_DC_TITLE,
			PROPERTY_DC_ALTERNATIVE,
			PROPERTY_DC_CREATOR,
			PROPERTY_DC_SUBJECT,
			PROPERTY_DC_DESCRIPTION,
			PROPERTY_DC_TABLEOFCONTENTS,
			PROPERTY_DC_ABSTRACT,
			PROPERTY_DC_PUBLISHER,
			PROPERTY_DC_CONTRIBUTOR,
			PROPERTY_DC_TYPE,
			PROPERTY_DC_FORMAT,
			PROPERTY_DC_CREATED,
			PROPERTY_DC_ISSUED,
			PROPERTY_DC_MODIFIED,
			PROPERTY_DC_IDENTIFIER,
			PROPERTY_DC_SOURCE,
			PROPERTY_DC_LANGUAGE,
			PROPERTY_DC_COVERAGE,
			PROPERTY_DC_RIGHTS,
			PROPERTY_DC_AUDIENCE,
			PROPERTY_DC_EDULEVEL,
			PROPERTY_DC_ANYURI,
			PROPERTY_DC_DOUBLE,
			PROPERTY_DC_DATETIME,
			PROPERTY_DC_TIME,
			PROPERTY_DC_DATE,
			PROPERTY_DC_BOOLEAN,
			PROPERTY_DC_INTEGER,
			PROPERTY_LOM_ROLE,
			PROPERTY_LOM_COVERAGE,
			PROPERTY_LOM_STATUS,
			PROPERTY_LOM_DURATION,
			PROPERTY_LOM_ENGAGEMENT_TYPE,
			PROPERTY_LOM_LEARNING_RESOURCE_TYPE,
			PROPERTY_LOM_INTERACTIVITY_LEVEL,
			PROPERTY_LOM_CONTEXT_LEVEL,
			PROPERTY_LOM_DIFFICULTY,
			PROPERTY_LOM_LEARNING_TIME,
			PROPERTY_LOM_ASSUMED_KNOWLEDGE,
			PROPERTY_LOM_TECHNICAL_REQUIREMENTS,
			PROPERTY_LOM_INSTALL_REMARKS,
			PROPERTY_LOM_OTHER_REQUIREMENTS,
			PROPERTY_LOM_GRANULARITY_LEVEL,
			PROPERTY_LOM_STRUCTURE,
			PROPERTY_LOM_RELATION,
			PROPERTY_LOM_REVIEWER,
			PROPERTY_LOM_REVIEW_DATE,
			PROPERTY_LOM_REVIEW_COMMENTS
		};

	/**
	 * The character(s) used to delimite parts of the Dotted Name of a StructuredArtifact property.
	 */
	public static final String DOT = ".";

	/**
	 * A regular expression that will match DOT in an expression.
	 */
	private static final String DOT_REGEX = "\\.";

	/** The default size of the text-input widget for strings */
	public static final int DEFAULT_LENGTH = 50;

	public static ResourcesMetadata getProperty(String name)
	{
		ResourcesMetadata rv = null;
		if(name != null)
		{
			boolean found = false;
			for(int k = 0; !found && k < DEFINED_PROPERTIES.length; k++)
			{
				if(DEFINED_PROPERTIES[k].getFullname().equalsIgnoreCase(name) || DEFINED_PROPERTIES[k].getShortname().equalsIgnoreCase(name))
				{
					rv = DEFINED_PROPERTIES[k];
					found = true;
				}
			}
		}
		return rv;
	}
	

	public static ResourcesMetadata[] getProperties(String[] names)
	{
		List results = new ArrayList();
		for(int i = 0; i < names.length; i++)
		{
			if(names[i] == null)
			{
				continue;
			}
			boolean found = false;
			for(int k = 0; !found && k < DEFINED_PROPERTIES.length; k++)
			{
				if(DEFINED_PROPERTIES[k].getFullname().equalsIgnoreCase(names[i]))
				{
					results.add(DEFINED_PROPERTIES[k]);
					found = true;
				}
			}
		}
		
		ResourcesMetadata[] rv = new ResourcesMetadata[results.size()];
		
		for(int j = 0; j < results.size(); j++)
		{
			rv[j] = (ResourcesMetadata) results.get(j);
		}
		
		return rv;
	}
	
	/**
	 * The string representation of the localname for the metadata property
	 */
	protected String m_localname;
	
	/**
	 * The string representation of the namespace for the metadata property
	 */
	protected String m_namespace;
	
	/**
	 * The parts of the name of a nested structured object.
	 */
	protected List m_dottedparts;
	
	/**
	 * A string that can be used to refer to the metadata property
	 */
	protected String m_label;
	
	/**
	 * An explanation of the metadata property, including the nature of the legal values
	 */
	protected String m_description;
	
	/**
	 * The datatype of legal values for the metadata property 
	 * (usually a URI ref for an XML Schema Datatype)
	 */
	protected String m_datatype;
	
	/**
	 * The default editor widget for the metadata property
	 */
	protected String m_widget;
	
	protected int m_minCardinality;
	protected int m_maxCardinality;
	protected int m_currentCount;
	protected List m_currentValues;
	protected int m_length;
	protected List m_enumeration;
	protected List m_nested;
	protected List m_nestedinstances;
	protected List m_instances;
	protected ResourcesMetadata m_container;
	protected ResourcesMetadata m_parent;
	protected Object m_minValue;
	protected Object m_maxValue;
	protected boolean m_minInclusive;
	protected boolean m_maxInclusive;
	protected Pattern m_pattern;
	protected int m_depth;
	
	/**
	 * Constructor.
	 * @param 	name		The string representation of the URI for the metadata property
	 * @param 	label		A string that can be used to refer to the metadata property
	 * @param 	description	An explanation of the metadata property, describing the valid values
	 * @param 	datatype	The datatype of legal values for the metadata property 
	 * 						(usually a URI ref for an XML Schema Datatype)
	 * @param 	widget		The default editor widget for the metadata property 
	 */
	public ResourcesMetadata(String namespace, String localname, String label, String description, String datatype, String widget)
	{
		m_datatype = datatype;
		m_description = description;
		m_label = label;
		m_namespace = namespace;
		m_localname = localname;
		m_widget = widget;
		m_minCardinality = 1;
		m_maxCardinality = 1;
		m_currentCount = 1;
		m_enumeration = null;
		m_currentValues = new ArrayList();
		m_nested = new ArrayList();
		m_pattern = Pattern.compile(".*");
		m_minInclusive = true;
		m_maxInclusive = true;
		m_depth = 0;
		m_dottedparts = new ArrayList();
		m_nestedinstances = new ArrayList();
		m_instances = new ArrayList();
		m_parent = null;
		m_container = null;
		m_length = DEFAULT_LENGTH;

	}
	
	/**
	 * @param prop
	 */
	public ResourcesMetadata(ResourcesMetadata other) 
	{
		m_datatype = other.m_datatype;
		m_description = other.m_description;
		m_label = other.m_label;
		m_namespace = other.m_namespace;
		m_localname = other.m_localname;
		m_widget = other.m_widget;
		m_minCardinality = other.m_minCardinality;
		m_maxCardinality = other.m_maxCardinality;
		m_currentCount = other.m_currentCount;
		if(other.m_enumeration == null)
		{
			m_enumeration = null;
		}
		else
		{
			m_enumeration = new ArrayList(other.m_enumeration);
		}
		m_currentValues = new ArrayList();
		m_pattern = other.m_pattern;
		m_minInclusive = other.m_minInclusive;
		m_maxInclusive = other.m_maxInclusive;
		m_depth = other.m_depth;
		m_dottedparts = new ArrayList(other.m_dottedparts);
		m_nestedinstances = new ArrayList();
		m_instances = new ArrayList();
		m_nested = new ArrayList();
		Iterator it = other.m_nested.iterator();
		while(it.hasNext())
		{
			ResourcesMetadata child = (ResourcesMetadata) it.next();
			this.m_nested.add(new ResourcesMetadata(child));
		}
		m_container = other.m_container;
		m_length = other.m_length;
		
	}
	
	/**
	 * @return The datatype of legal values for the metadata property (usually a URI ref for an XML Schema Datatype)
	 */
	public String getDatatype()
	{
		return m_datatype;
	}

	/**
	 * @return An explanation of the metadata property describing the valid values
	 */
	public String getDescription()
	{
		return m_description;
	}

	/**
	 * @return
	 */
	public String getLabel()
	{
		String name = rb.getString(m_label);
		if (name.indexOf("missing_key")!=-1)
			return m_label;
		else
			return name;
	}

	/**
	 * @return The string representation of the URI for the metadata property
	 */
	public String getLocalname()
	{
		return m_localname;
	}

	/**
	 * @return The string representation of the full namespace for the metadata property
	 */
	public String getNamespace()
	{
		return m_namespace;
	}
	
	/**
	 * @return The abbreviated version of the namespace (including delimiter)
	 */
	public String getNamespaceAbbrev()
	{
		return getNamespaceAbbrev(m_namespace);
	}

	/**
	 * @return The string representation of the URI for the metadata property
	 */
	public String getFullname()
	{
		return m_namespace + m_localname;
	}

	/**
	 * @return The string representation of the URI for the metadata property
	 */
	public String getShortname()
	{
		String abbrev = getNamespaceAbbrev(m_namespace);
		if(abbrev == null)
		{
			abbrev = m_namespace;
		}
		return abbrev + m_localname;
	}

	/**
	 * @return The default editor widget for the metadata property 
	 */
	public String getWidget()
	{
		return m_widget;
	}

	/**
	 * @param 	datatype	The datatype of legal values for the metadata property (usually a URI ref for an XML Schema Datatype)
	 */
	public void setDatatype(String datatype)
	{
		m_datatype = datatype;
	}

	/**
	 * @param 	description	An explanation of the metadata property describing the valid values
	 */
	public void setDescription(String description)
	{
		m_description = description;
	}

	/**
	 * @param 	label	A string that can be used to refer to the metadata property
	 */
	public void setLabel(String label)
	{
		m_label = label;
	}

	/**
	 * @param 	name	The string representation of the namespace for the metadata property
	 */
	public void setNamespace(String namespace)
	{
		m_namespace = namespace;
	}

	/**
	 * @param 	name	The string representation of the URI for the metadata property
	 */
	public void setLocalname(String localname)
	{
		m_localname = localname;
	}

	/**
	 * @param 	widget	The default editor widget for the metadata property 
	 */
	public void setWidget(String widget)
	{
		m_widget = widget;
	}
	
	public void setContainer(ResourcesMetadata container)
	{
		this.m_container = container;
	}
	
	public ResourcesMetadata getContainer()
	{
		return this.m_container;
	}
	
	protected static Map m_ns2abbrev;
	protected static Map m_abbrev2ns;

	protected String m_id;
	 
	/**
	 * @param namespace The string representation of the namespace for the metadata property
	 * @param abbrev The abbreviated version of the namespace (including delimiter)
	 */
	public static void setNamespaceAbbrev(String namespace, String abbrev)
	{
		if(m_ns2abbrev == null || m_abbrev2ns == null)
		{
			initNamespaceMaps();
		}

		// what if namespace already defined mapping to a different abbrev?
		// new abbrev will be used instead but old abbrev will still map to namespace
		
		m_abbrev2ns.put(abbrev, namespace);
		m_ns2abbrev.put(namespace,abbrev);
	}
	
	/**
	 * @param namespace	The string representation of the namespace for a metadata property
	 * @return The abbreviated version of the namespace identifier (including delimiter)
	 */
	public static String getNamespaceAbbrev(String namespace)
	{
		String abbrev = null;
		if(m_ns2abbrev == null)
		{
			initNamespaceMaps();
		}
		abbrev = (String) m_ns2abbrev.get(namespace);
		if(abbrev == null)
		{
			abbrev = assignAbbrev(namespace);
		}
		return abbrev;
	}
	
	/**
	 * @param abbrev The abbreviated version of the namespace identifier (including delimiter)
	 * @return The string representation of the full name of the namespace 
	 */
	public static String getNamespace(String abbrev)
	{
		String namespace = null;
		if(m_abbrev2ns == null)
		{
			initNamespaceMaps();
		}
		namespace = (String) m_abbrev2ns.get(abbrev);
		return namespace;
	}
	
	/**
	 * Make sure that maps are defined and default values for Dublin Core 
	 * and XMLSchema Datatypes are included
	 */
	protected static void initNamespaceMaps()
	{
		if(m_ns2abbrev == null)
		{
			m_ns2abbrev = new HashMap();
		}
		if(m_abbrev2ns == null)
		{
			m_abbrev2ns = new HashMap();
		}
		setNamespaceAbbrev(NAMESPACE_DC, NAMESPACE_DC_ABBREV);
		setNamespaceAbbrev(NAMESPACE_DCTERMS, NAMESPACE_DCTERMS_ABBREV);
		setNamespaceAbbrev(NAMESPACE_XSD, NAMESPACE_XSD_ABBREV);
		setNamespaceAbbrev(NAMESPACE_LOM, NAMESPACE_LOM_ABBREV);
	}
	
	protected static String assignAbbrev(String namespace)
	{
		String abbrev = "error";
		// removed the sync block from here
        abbrev = "s" + namespaceNumber;
        namespaceNumber = new AtomicInteger(namespaceNumber.byteValue() + 1);
		setNamespaceAbbrev(namespace, abbrev);
		return abbrev;
	}

	/**
	 * @return Returns the currentCount.
	 */
	public int getCurrentCount() 
	{
		return this.m_currentCount;

	}
	
	/**
	 * @return Returns the currentCount.
	 */
	public Integer getCount() 
	{
		return Integer.valueOf(this.getCurrentCount());
	}
	
	public void setValue(String name, Object value)
	{
		if(name.startsWith(this.getDottedname()))
		{
			String localname = name.substring(name.length());
			if(localname == null || "".equals(localname))
			{
				if(this.m_currentValues == null)
				{
					this.m_currentValues = new ArrayList();
				}
				
				if(this.m_currentValues.size() > 0)
				{
					this.m_currentValues.set(0, value);	
				}
				else
				{
					this.m_currentValues.add(0, value);
				}
			}
			else
			{
				String[] parts = localname.split(DOT_REGEX);
				String target = parts[0];
				boolean found = false;
				Iterator it = getNested().iterator();
				ResourcesMetadata prop = null;
				while(!found && it.hasNext())
				{
					prop = (ResourcesMetadata) it.next();
					if(prop.getLocalname().equals(target))
					{
						found = true;
					}
				}
				
				if(found)
				{
					prop.setValue(name, value);
				}
			}
		}
	}
	
	/**
	 * @return
	 */
	public List getValues()
	{
		return m_currentValues;
	}
	
	/**
	 * @param index
	 * @return
	 */
	public Object getValue(int index)
	{
		Object rv = null;
		if(m_currentValues != null && ! m_currentValues.isEmpty())
		{
			try
			{
				rv = m_currentValues.get(index);

			}
			catch(ArrayIndexOutOfBoundsException e)
			{
				// return null
			}
		}
		return rv;
	}
	
	/**
	 * @return
	 */
	public Object getValue()
	{
		return getValue(0);
	}
	
	public List getInstanceValues()
	{
		List values = new ArrayList();
		values.addAll(this.m_currentValues);
		if(this.m_instances == null)
		{
			this.m_instances = new ArrayList();
		}
		Iterator it = this.m_instances.iterator();
		while(it.hasNext())
		{
			ResourcesMetadata instance = (ResourcesMetadata) it.next();
			values.addAll(instance.getValues());
		}
		return values;
	}
	
	/**
	 * @param index
	 * @param value
	 */
	public void setValue(int index, Object value)
	{
		if(m_currentValues == null)
		{
			m_currentValues = new ArrayList();
		}
		try
		{
			m_currentValues.set(index, value);
		}
		catch(IndexOutOfBoundsException e)
		{
			m_currentValues.add(value);
		}
		catch(Exception e)
		{
			log.warn("ResourcesMetadata[" + this.m_id + "].setValue(" + index + "," + value + ") " + e);
		}
	}
	
	/**
	 * @param currentCount The currentCount to set.
	 */
	public void setCurrentCount(int currentCount) 
	{
		m_currentCount = currentCount;
		if(m_parent != null)
		{
			m_parent.setCurrentCount(currentCount);
		}
		if(this.m_instances != null)
		{
			Iterator it = this.m_instances.iterator();
			while(it.hasNext())
			{
				ResourcesMetadata instance = (ResourcesMetadata) it.next();
				instance.m_currentCount = currentCount;
			}
		}
	}
	
	/**
	 * @return Returns the maxCardinality.
	 */
	public int getMaxCardinality() 
	{
		return m_maxCardinality;
	}
	
	/**
	 * @param maxCardinality The maxCardinality to set.
	 */
	public void setMaxCardinality(int maxCardinality) 
	{
		m_maxCardinality = maxCardinality;
	}
	
	/**
	 * @return Returns the minCardinality.
	 */
	public int getMinCardinality() 
	{
		return m_minCardinality;
	}
	
	/**
	 * @param minCardinality The minCardinality to set.
	 */
	public void setMinCardinality(int minCardinality) 
	{
		m_minCardinality = minCardinality;
	}
	
	/**
	 * increments the currentCount if it is less than maxCardinality.
	 */
	public void incrementCount() 
	{
		if(this.getCurrentCount() < m_maxCardinality)
		{
			this.setCurrentCount(this.m_currentCount + 1);
		}
		
	}	// incrementCount
	
	/**
	 * @return true if additional instances of the field can be added, false otherwise.
	 */
	public boolean canAddInstances()
	{
		return this.getCurrentCount() < m_maxCardinality;
		
	}
	
	/**
	 * Access the desired size of a text input field.
	 * @return Returns the length, which represents the size of a text input field.
	 */
	public int getLength() 
	{
		return m_length;
		
	}
	
	/**
	 * Set the the size of a text input field.
	 * @param length The length to set. 
	 */
	public void setLength(int length) 
	{
		m_length = length;
	}
	
	/**
	 * @return Returns the enumeration.
	 */
	public List getEnumeration() 
	{
		List rv;
		if(m_enumeration == null)
		{
			rv = new ArrayList();
		}
		else
		{
			rv = new ArrayList(m_enumeration);
		}
		return rv;
	}
	
	/**
	 * @param enumeration The enumeration to set.
	 */
	public void setEnumeration(List enumeration) 
	{
		m_enumeration = new ArrayList(enumeration);
	}
	
	public boolean isNested()
	{
		return (m_nested != null) && ! m_nested.isEmpty();
	}
	
	/**
	 * @return Returns the nested.
	 */
	public List getNested() 
	{
		if(m_nested == null)
		{
			m_nested = new ArrayList();
		}
		return m_nested;
	}
	
	/**
	 * @return Returns the nested.
	 */
	public List getNestedInstances() 
	{
		List instances = new ArrayList();
		if(m_nested == null)
		{
			m_nested = new ArrayList();
		}
		Iterator it = this.m_nested.iterator();
		while(it.hasNext())
		{
			ResourcesMetadata nested = (ResourcesMetadata) it.next();
			instances.addAll(nested.m_instances);
		}
		return instances;
	}
	
	/**
	 * @param nested The nested to set.
	 */
	public void setNested(List nested) 
	{
		m_nested = nested;
	}
	
	/**
	 * @return Returns the maxValue.
	 */
	public Object getMaxValue() 
	{
		return m_maxValue;
	}
	
	/**
	 * @param maxValue The maxValue to set.
	 */
	public void setMaxValue(Object maxValue) 
	{
		m_maxValue = maxValue;
	}
	
	/**
	 * @return Returns the minValue.
	 */
	public Object getMinValue() 
	{
		return m_minValue;
	}
	
	/**
	 * @param minValue The minValue to set.
	 */
	public void setMinValue(Object minValue) 
	{
		m_minValue = minValue;
	}
	
	/**
	 * @return Returns the pattern.
	 */
	public Pattern getPattern() 
	{
		return m_pattern;
	}
	
	/**
	 * @param pattern The pattern to set.
	 */
	public void setPattern(Pattern pattern) 
	{
		m_pattern = pattern;
	}
	
	/**
	 * @return Returns the maxInclusive.
	 */
	public boolean isMaxInclusive() 
	{
		return m_maxInclusive;
	}
	
	/**
	 * @param maxInclusive The maxInclusive to set.
	 */
	public void setMaxInclusive(boolean maxInclusive) 
	{
		m_maxInclusive = maxInclusive;
	}
	
	/**
	 * @return Returns the minInclusive.
	 */
	public boolean isMinInclusive() 
	{
		return m_minInclusive;
	}
	
	/**
	 * @param minInclusive The minInclusive to set.
	 */
	public void setMinInclusive(boolean minInclusive) 
	{
		m_minInclusive = minInclusive;
	}
	
	/**
	 * @return Returns the depth.
	 */
	public int getDepth() 
	{
		return m_depth;
	}
	
	/**
	 * @param depth The depth to set.
	 */
	public void setDepth(int depth) 
	{
		m_depth = depth;
	}


	/**
	 * @param string
	 */
	public void setId(String id) 
	{
		m_id = id;
		
	}
	
	public String getId()
	{
		return m_id;
	}
	
	public void setDottedparts(List parts)
	{
		m_dottedparts = parts;
	}
	
	public List getDottedparts()
	{
		return m_dottedparts;
	}
	
	public void setDottedpart(int index, String part)
	{
		if(m_dottedparts == null)
		{
			m_dottedparts = new ArrayList();
		}
		if(index >= 0 && m_dottedparts.size() < index)
		{
			m_dottedparts.set(index, part);
		}
		else
		{
			m_dottedparts.add(part);
		}
	}
	
	public void insertDottedpart(int index, String part)
	{
		if(m_dottedparts == null)
		{
			m_dottedparts = new ArrayList();
		}
		if(index >= 0 && index < m_dottedparts.size())
		{
			m_dottedparts.add(index, part);
		}
		else
		{
			m_dottedparts.add(part);
		}
	}
	
	public String getDottedname()
	{
	    StringBuilder name = new StringBuilder();
        Iterator it = m_dottedparts.iterator();
        while (it.hasNext()) {
            String part = (String) it.next();
            name.append(part);
            if (it.hasNext()) {
                name.append(DOT);
            }
        }
		return name.toString();
	}
	
	public String getParentname()
	{
        String name = "";
        if (m_parent != null) {
            name = m_parent.getDottedname();
        } else {
            boolean first = true;
            StringBuilder sb = new StringBuilder();
            Iterator it = m_dottedparts.iterator();
            while (it.hasNext()) {
                String part = (String) it.next();
                if (it.hasNext()) {
                    if (!first) {
                        sb.append(DOT);
                    }
                    sb.append(part);
                    first = false;
                }
            }
            name = sb.toString();
        }
        return name;
	}
	
	public void setDottedparts(String path)
	{
		String[] names = path.split(DOT_REGEX);
		m_dottedparts = new ArrayList();
		for(int i = 0; i < names.length; i++)
		{
			m_dottedparts.add(names[i]);
		}
		
	}
	
	/**
	 * Recursively traverses a hierarchy of ResourcesMetadata objects rooted at this
	 * node in the hierarchy and returns a flat list of ResourcesMetadata objects.  
	 * The hierarchy is expressed as references in the list of nested objects. The return
	 * value is a list of objects ordered for rendering as an HTML form, with at least 
	 * one entry for each HTML tag required to render the form.
	 * 
	 * @return An ordered list of ResourcesMetadata objects.
	 */
	public List getFlatList()
	{
		List rv = new ArrayList();
		rv.add(this);

		Iterator it = this.getNested().iterator();
		while(it.hasNext())
		{
			ResourcesMetadata prop = (ResourcesMetadata) it.next();
			if(prop.getMaxCardinality() > 1)
			{
				for(int i = 0; i < prop.getCurrentCount(); i++)
				{
					ResourcesMetadata copy = null;
					if(i < prop.m_instances.size())
					{
						copy = (ResourcesMetadata) prop.m_instances.get(i);
					}
					else
					{
						copy = new ResourcesMetadata(prop);
						List parts = new ArrayList(this.getDottedparts());
						parts.add(copy.getLocalname());
						parts.add(Integer.toString(i));
						copy.setDottedparts(parts);
						copy.setContainer(this);
						if(prop.m_parent == null)
						{
							// in that case, prop is the parent
							prop.m_instances.add(copy);
							copy.m_parent = prop;
						}
						else
						{
							// and otherwise, prop's parent is parent
							prop.m_parent.m_instances.add(copy);
							//copy.m_parent = prop.m_parent;
						}
						//copy.m_parent.m_currentCount++;
					}

					if(copy.getNested().isEmpty())
					{
						rv.add(copy);
					}
					else
					{
						rv.addAll(copy.getFlatList());
					}
				}
			}
			else
			{
				ResourcesMetadata copy = null;
				if(prop.m_instances.size() > 0)
				{
					copy = (ResourcesMetadata) prop.m_instances.get(0);
				}
				else
				{
					copy = new ResourcesMetadata(prop);
					List parts = new ArrayList(this.getDottedparts());
					parts.add(copy.getLocalname());
					copy.setDottedparts(parts);
					copy.setContainer(this);
					if(prop.m_parent == null)
					{
						// prop is parent
						prop.m_instances.add(copy);
						copy.m_parent = prop;
					}
					else
					{
						// prop's parent is parent
						prop.m_parent.m_instances.add(copy);
						//copy.m_parent = prop.m_parent;
					}
					// copy.m_parent.m_currentCount++;
				}
				
				if(copy.getNested().isEmpty())
				{
					rv.add(copy);
				}
				else
				{
					rv.addAll(copy.getFlatList());
				}
			}
		}
		return rv;
		
	}	// getFlatList
	
	/**
	 * Add an instance of "this" property.  The dotted-name of the new instance will be the 
	 * same as the dotted-name of "this" property, except that it will have a decimal number 
	 * appended if the property's maximum cardinality allows multiple instances of the property.
	 */
	public ResourcesMetadata addInstance()
	{
		if(this.m_currentCount > this.m_maxCardinality)
		{
			return null;
		}
		ResourcesMetadata copy = new ResourcesMetadata(this);
		copy.setContainer(this.m_container);
		List parts = new ArrayList(this.getDottedparts());
		if(this.getMaxCardinality() > 1 && this.m_parent == null)
		{
			parts.add(Integer.toString(this.m_instances.size()));
		}
		else if(this.getMaxCardinality() > 1)
		{
			parts.add(Integer.toString(this.m_parent.m_instances.size()));
		}
		copy.setDottedparts(parts);
		if(this.m_parent == null)
		{
			this.m_instances.add(copy);
			copy.m_parent = this;
			this.setCurrentCount(this.m_instances.size());
		}
		else
		{
			this.m_parent.m_instances.add(copy);
			//copy.m_parent = this.m_parent;
			this.setCurrentCount(this.m_parent.m_instances.size());
		}
		
		return copy;
		
	}	// addNestedInstance

}	// ResourcesMetadata



