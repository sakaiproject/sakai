/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation
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

package org.sakaiproject.content.metadata.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sakaiproject.content.metadata.logic.MetadataService;
import org.sakaiproject.content.metadata.model.GroupMetadataType;
import org.sakaiproject.content.metadata.model.MetadataType;
import org.sakaiproject.content.metadata.model.StringMetadataType;

/**
 * Hardcoded DublinCore metadataModel to facilitate transition to the new metadata system.
 * <p/>
 * This system should NOT be used except for DublinCore
 *
 * @author Colin Hebert
 */
public final class MetadataServiceDublinCore implements MetadataService
{
	private final List<MetadataType> dublinCoreMeta;
	private static final String NAMESPACE_DC = "http://purl.org/dc/elements/1.1/";
	private static final String NAMESPACE_DCTERMS = "http://purl.org/dc/terms/";

	public MetadataServiceDublinCore()
	{
		GroupMetadataType dcMetadata = new GroupMetadataType();
		dcMetadata.setName("label.dc");
		dcMetadata.setUniqueName("dublin_core");
		dcMetadata.setTranslated(true);
		List<MetadataType<?>> subTags = new ArrayList<MetadataType<?>>();
		dcMetadata.setMetadataTypes(subTags);

		// Title
		{
			StringMetadataType title = new StringMetadataType();
			title.setName("label.dc_title");
			title.setDescription("descr.dc_title");
			title.setTranslated(true);
			title.setUniqueName(NAMESPACE_DC + "title");
			//subTags.add(title);
		}

		//Description
		{
			StringMetadataType description = new StringMetadataType();
			description.setName("label.dc_descr");
			description.setDescription("descr.dc_descr");
			description.setTranslated(true);
			description.setUniqueName(NAMESPACE_DC + "description");
			description.setLongText(true);
			//subTags.add(description);
		}

		// Alternate Title
		{
			StringMetadataType alternateTitle = new StringMetadataType();
			alternateTitle.setName("label.dc_alt");
			alternateTitle.setDescription("descr.dc_alt");
			alternateTitle.setTranslated(true);
			alternateTitle.setUniqueName(NAMESPACE_DC + "alternative");
			subTags.add(alternateTitle);
		}

		//Creator
		{
			StringMetadataType creator = new StringMetadataType();
			creator.setName("label.dc_creator");
			creator.setDescription("descr.dc_creator");
			creator.setTranslated(true);
			creator.setUniqueName(NAMESPACE_DC + "creator");
			subTags.add(creator);
		}

		//Publisher
		{
			StringMetadataType publisher = new StringMetadataType();
			publisher.setName("label.dc_publisher");
			publisher.setDescription("descr.dc_publisher");
			publisher.setTranslated(true);
			publisher.setUniqueName(NAMESPACE_DC + "publisher");
			subTags.add(publisher);
		}

		//Subject (and keywords)
		{
			StringMetadataType subjectKeywords = new StringMetadataType();
			subjectKeywords.setName("label.dc_subject");
			subjectKeywords.setDescription("descr.dc_subject");
			subjectKeywords.setTranslated(true);
			subjectKeywords.setUniqueName(NAMESPACE_DC + "subject");
			subjectKeywords.setLongText(true);
			subTags.add(subjectKeywords);
		}

		//Creation date
		{
			//TODO Should be a date
			StringMetadataType dateCreated = new StringMetadataType();
			dateCreated.setName("label.dc_created");
			dateCreated.setDescription("descr.dc_created");
			dateCreated.setTranslated(true);
			dateCreated.setUniqueName(NAMESPACE_DCTERMS + "created");
			subTags.add(dateCreated);
		}

		//Issue date
		{
			//TODO Should be a date
			StringMetadataType dateIssued = new StringMetadataType();
			dateIssued.setName("label.dc_issued");
			dateIssued.setDescription("descr.dc_issued");
			dateIssued.setTranslated(true);
			dateIssued.setUniqueName(NAMESPACE_DCTERMS + "issued");
			subTags.add(dateIssued);
		}

		//Modification date
		{
			//TODO Should be a date
			StringMetadataType dateModified = new StringMetadataType();
			dateModified.setName("label.dc_modified");
			dateModified.setDescription("descr.dc_modified");
			dateModified.setTranslated(true);
			dateModified.setUniqueName(NAMESPACE_DCTERMS + "modified");
			//subTags.add(dateModified);
		}

		//Table of contents
		{
			StringMetadataType tableOfContents = new StringMetadataType();
			tableOfContents.setName("label.dc_toc");
			tableOfContents.setDescription("descr.dc_toc");
			tableOfContents.setTranslated(true);
			tableOfContents.setUniqueName(NAMESPACE_DCTERMS + "tableOfContents");
			tableOfContents.setLongText(true);
			//subTags.add(tableOfContents);
		}

		//Abstract
		{
			//Please don't even try rename this variable "abstract"...
			StringMetadataType abstractText = new StringMetadataType();
			abstractText.setName("label.dc_abstract");
			abstractText.setDescription("descr.dc_abstract");
			abstractText.setTranslated(true);
			abstractText.setUniqueName(NAMESPACE_DCTERMS + "abstract");
			abstractText.setLongText(true);
			subTags.add(abstractText);
		}

		//Contributor
		{
			StringMetadataType contributor = new StringMetadataType();
			contributor.setName("label.dc_contributor");
			contributor.setDescription("descr.dc_contributor");
			contributor.setTranslated(true);
			contributor.setUniqueName(NAMESPACE_DC + "contributor");
			contributor.setLongText(true);
			subTags.add(contributor);
		}

		//Type
		{
			StringMetadataType type = new StringMetadataType();
			type.setName("label.dc_type");
			type.setDescription("descr.dc_type");
			type.setTranslated(true);
			type.setUniqueName(NAMESPACE_DC + "type");
			//subTags.add(type);
		}

		//Format
		{
			StringMetadataType format = new StringMetadataType();
			format.setName("label.dc_format");
			format.setDescription("descr.dc_format");
			format.setTranslated(true);
			format.setUniqueName(NAMESPACE_DC + "format");
			//subTags.add(format);
		}

		//Identifier
		{
			//TODO Should be an URI (add constraints)
			StringMetadataType identifier = new StringMetadataType();
			identifier.setName("label.dc_id");
			identifier.setDescription("descr.dc_id");
			identifier.setTranslated(true);
			identifier.setUniqueName(NAMESPACE_DC + "identifier");
			//subTags.add(identifier);
		}

		//Source
		{
			StringMetadataType source = new StringMetadataType();
			source.setName("label.dc_source");
			source.setDescription("descr.dc_source");
			source.setTranslated(true);
			source.setUniqueName(NAMESPACE_DC + "source");
			//subTags.add(source);
		}

		//Language
		{
			StringMetadataType language = new StringMetadataType();
			language.setName("label.dc_lang");
			language.setDescription("descr.dc_lang");
			language.setTranslated(true);
			language.setUniqueName(NAMESPACE_DC + "language");
			//subTags.add(language);
		}

		//Coverage
		{
			StringMetadataType coverage = new StringMetadataType();
			coverage.setName("label.dc_coverage");
			coverage.setDescription("descr.dc_coverage");
			coverage.setTranslated(true);
			coverage.setUniqueName(NAMESPACE_DC + "coverage");
			//subTags.add(coverage);
		}

		//Rights
		{
			StringMetadataType rights = new StringMetadataType();
			rights.setName("label.dc_rights");
			rights.setDescription("descr.dc_rights");
			rights.setTranslated(true);
			rights.setUniqueName(NAMESPACE_DC + "rights");
			//subTags.add(rights);
		}

		//Audience
		{
			StringMetadataType audience = new StringMetadataType();
			audience.setName("label.dc_audience");
			audience.setDescription("descr.dc_audience");
			audience.setTranslated(true);
			audience.setUniqueName(NAMESPACE_DCTERMS + "audience");
			subTags.add(audience);
		}

		//Education level
		{
			StringMetadataType educationLevel = new StringMetadataType();
			educationLevel.setName("label.dc_edlevel");
			educationLevel.setDescription("descr.dc_edlevel");
			educationLevel.setTranslated(true);
			educationLevel.setUniqueName(NAMESPACE_DCTERMS + "educationLevel");
			educationLevel.setLongText(true);
			subTags.add(educationLevel);
		}

		dublinCoreMeta = Collections.<MetadataType>singletonList(dcMetadata);
	}

	public List<MetadataType> getMetadataAvailable(String resourceType)
	{
		return dublinCoreMeta;
	}

	public List<MetadataType> getMetadataAvailable(String siteId, String resourceType)
	{
		return dublinCoreMeta;
	}
}
