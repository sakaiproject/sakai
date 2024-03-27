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
package org.sakaiproject.scorm.content.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.LinkedList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import static org.sakaiproject.scorm.api.ScormConstants.*;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingHandlerResolver;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.scorm.service.api.ScormContentService;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

@Slf4j
public class ScormCHH extends FastZipCHH
{
	private static final long serialVersionUID = -5151758531490144616L;

	private static final String CONTENT_HOSTING_HANDLER_NAME = "org.sakaiproject.scorm.content.api.ScormCHH";

	protected VirtualFileSystem buildVirtualFileSystem(ContentEntity realParent)
	{
		String realParentId = realParent.getId();
		VirtualFileSystem fs = new VirtualFileSystem(realParentId);

		try
		{
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance(System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);

			factory.setNamespaceAware(true);
			XmlPullParser xpp = factory.newPullParser();

			InputStream inStream = getManifestAsStream(realParentId);
			xpp.setInput(inStream, null);

			int eventType = xpp.getEventType();
			String tagName = null;
			String xmlBase = null;
			while (eventType != XmlPullParser.END_DOCUMENT)
			{
				if (eventType == XmlPullParser.START_TAG)
				{
					tagName = xpp.getName();
					if (tagName != null)
					{
						if (tagName.equals("resource"))
						{
							xmlBase = getAttributeValue(xpp, "xml", "base");
						}
						else if (tagName.equals("file")) {
							String href = getAttributeValue(xpp, null, "href");

							StringBuilder pathBuffer = new StringBuilder();
							if (xmlBase != null)
							{
								pathBuffer.append(xmlBase);
							}

							if (href != null)
							{
								pathBuffer.append(href);
								fs.addPath(pathBuffer.toString());
							}
						}
					}
				}
				else if (eventType == XmlPullParser.END_TAG)
				{
					tagName = xpp.getName();
					if (tagName != null && tagName.equals("resource"))
					{
						xmlBase = null;
					}
				}

				eventType = xpp.next();
			}

			if (inStream != null)
			{
				inStream.close();
			}
		}
		catch (Exception xppe)
		{
			log.error("Caught an exception parsing the xml of the manifest document", xppe);
		}

		return fs;
	}

	@Override
	protected int countChildren(ContentEntity parent, int depth)
	{
		ContentResource realParent = (ContentResource) getRealParent(parent.getId());
		String relativePath = getRelativePath(realParent.getId(), parent.getId());
		VirtualFileSystem fs = getVirtualFileSystem(realParent);
		return fs.getCount(relativePath);
	}

	@Override
	protected List<ContentEntity> extractChildren(ContentEntity parent, int depth)
	{
		ContentResource realParent = (ContentResource) getRealParent(parent.getId());
		String relativePath = getRelativePath(realParent.getId(), parent.getId());

		VirtualFileSystem fs = getVirtualFileSystem(realParent);
		List<ContentEntity> list = new LinkedList<>();
		List<String> entityNames = fs.getChildren(relativePath);

		for (String name : entityNames)
		{
			if (name.endsWith(Entity.SEPARATOR))
			{
				list.add(makeCollection(realParent, newId(relativePath, name), name));
			}
			else
			{
				list.add(makeResource(realParent, newId(relativePath, name), name, null));
			}
		}

		return list;
	}

	private String getAttributeValue(XmlPullParser xpp, String prefix, String name)
	{
		int count = xpp.getAttributeCount();
		String value = null;
		for (int i = 0; i < count; i++)
		{
			String attrPrefix = xpp.getAttributePrefix(i);
			String attrName = xpp.getAttributeName(i);
			if (attrName != null && (prefix == null || (attrPrefix != null && attrPrefix.equals(prefix))))
			{
				if (attrName.equals(name))
				{
					value = URLDecoder.decode(xpp.getAttributeValue(i));
					return value;
				}
			}
		}

		return value;
	}

	@Override
	public String getContentHostingHandlerName()
	{
		return CONTENT_HOSTING_HANDLER_NAME;
	}

	private InputStream getManifestAsStream(String contentPackageId)
	{
		InputStream stream = null;
		String path = new StringBuffer().append(contentPackageId).append("/imsmanifest.xml").toString();
		try
		{
			ContentResource resource = super.getContentResourceEdit(path);
			if (resource != null)
			{
				byte[] content = resource.getContent();
				if (content != null)
				{
					stream = new ByteArrayInputStream(content);
				} 
				else
				{
					stream = resource.streamContent();
				}
			}
		}
		catch (Exception iue)
		{
			log.error("Caught an exception grabbing the manifest file as a stream", iue);
		}

		return stream;
	}

	@Override
	public ContentEntity getVirtualContentEntity(ContentEntity ce, String finalId)
	{
		ContentEntity virtualEntity = null;
		if (null == ce)
		{
			return null;
		}

		ResourceProperties realProperties = ce.getProperties();
		if (null == realProperties)
		{
			return null;
		}

		String chhbeanname = realProperties.getProperty(ContentHostingHandlerResolver.CHH_BEAN_NAME);
		if (chhbeanname == null || !chhbeanname.equals(getContentHostingHandlerName()))
		{
			return null;
		}

		String parentId = ce.getId();
		String path = getRelativePath(parentId, finalId);

		if (path.length() == 0)
		{
			// Grab some data from the real content entity
			String name = (String) realProperties.get(ResourceProperties.PROP_DISPLAY_NAME);

			ContentCollectionEdit virtualCollection = makeCollection(ce, path, name);

			virtualCollection.setContentHandler(this);
			ce.setVirtualContentEntity(virtualCollection);

			if (realProperties.getProperty(CONTENT_PACKAGE_TITLE_PROPERTY) != null)
			{
				ResourcePropertiesEdit virtualProps = virtualCollection.getPropertiesEdit();
				virtualProps.addProperty(CONTENT_PACKAGE_TITLE_PROPERTY, realProperties.getProperty(CONTENT_PACKAGE_TITLE_PROPERTY));
				virtualProps.addProperty(IS_CONTENT_PACKAGE_PROPERTY, "true");
			}

			virtualEntity = virtualCollection;
		}
		else
		{
			virtualEntity = uncacheEntity(newId(parentId, path));
			if (null == virtualEntity)
			{
				try
				{
					virtualEntity = extractEntity(ce, path);
					cacheEntity(virtualEntity);
				}
				catch (Exception e)
				{
					log.error("Caught an exception extracting resource ", e);
				}
			}
		}

		return virtualEntity;
	}

	@Override
	protected VirtualFileSystem getVirtualFileSystem(ContentEntity parent)
	{
		ContentResource realParent = (ContentResource) getRealParent(parent.getId());
		String realParentId = realParent.getId();

		VirtualFileSystem fs = uncacheVirtualFileSystem(realParentId);
		if (fs == null)
		{
			fs = buildVirtualFileSystem(realParent);
			if (fs != null)
			{
				cacheVirtualFileSystem(realParentId, fs);
			}
		}

		return fs;
	}

	@Override
	public void init()
	{
		resourceTypeRegistry.register(new ScormCollectionType());
	}

	@Override
	protected ContentCollectionEdit makeCollection(ContentEntity ce, String path, String name)
	{
		ContentCollectionEdit collection = super.makeCollection(ce, path, name);
		collection.setResourceType(ScormCollectionType.SCORM_CONTENT_TYPE_ID);
		return collection;
	}

	protected ScormContentService scormContentService()
	{
		return null;
	}
}
