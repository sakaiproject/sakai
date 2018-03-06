/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.content.impl.serialize.impl.conversion;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.GroupAwareEntity.AccessMode;
import org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess;
import org.sakaiproject.content.impl.serialize.impl.Type1BaseContentCollectionSerializer;
import org.sakaiproject.entity.api.serialize.SerializableEntity;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.util.Xml;

/**
 * @author ieb
 */
@Slf4j
public class SAXSerializableCollectionAccess implements SerializableCollectionAccess,
		SerializableEntity
{

	protected static final long END_OF_TIME = 8000L * 365L * 24L * 60L * 60L * 1000L;

	protected static final long START_OF_TIME = 365L * 24L * 60L * 60L * 1000L;

	private Type1BaseContentCollectionSerializer type1CollectionSerializer;

	private SAXParserFactory parserFactory;

	private AccessMode accessMode = AccessMode.INHERITED;

	private Collection<String> group = new ArrayList<String>();

	private boolean hidden;

	private String id;

	private SAXSerializablePropertiesAccess saxSerializableProperties = new SAXSerializablePropertiesAccess();

	private Time releaseDate;

	private Time retractDate;

	private String resourceType;

	private ConversionTimeService conversionTimeService;

	public SAXSerializableCollectionAccess()
	{
		type1CollectionSerializer = new Type1BaseContentCollectionSerializer();
		conversionTimeService = new ConversionTimeService();
		type1CollectionSerializer.setTimeService(conversionTimeService);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess#getSerializableAccess()
	 */
	public AccessMode getSerializableAccess()
	{
		return accessMode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess#getSerializableGroup()
	 */
	public Collection<String> getSerializableGroup()
	{
		return group;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess#getSerializableHidden()
	 */
	public boolean getSerializableHidden()
	{
		return hidden;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess#getSerializableId()
	 */
	public String getSerializableId()
	{
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess#getSerializableProperties()
	 */
	public SerializableEntity getSerializableProperties()
	{
		return saxSerializableProperties;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess#getSerializableReleaseDate()
	 */
	public Time getSerializableReleaseDate()
	{
		return releaseDate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess#getSerializableRetractDate()
	 */
	public Time getSerializableRetractDate()
	{
		return retractDate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess#setSerializableAccess(org.sakaiproject.content.api.GroupAwareEntity.AccessMode)
	 */
	public void setSerializableAccess(AccessMode access)
	{
		this.accessMode = access;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess#setSerializableGroups(java.util.Collection)
	 */
	public void setSerializableGroups(Collection<String> groups)
	{
		this.group = groups;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess#setSerializableHidden(boolean)
	 */
	public void setSerializableHidden(boolean hidden)
	{
		this.hidden = hidden;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess#setSerializableId(java.lang.String)
	 */
	public void setSerializableId(String id)
	{
		this.id = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess#setSerializableReleaseDate(org.sakaiproject.time.api.Time)
	 */
	public void setSerializableReleaseDate(Time releaseDate)
	{
		this.releaseDate = releaseDate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess#setSerializableResourceType(java.lang.String)
	 */
	public void setSerializableResourceType(String resourceType)
	{
		this.resourceType = resourceType;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess#setSerializableRetractDate(org.sakaiproject.time.api.Time)
	 */
	public void setSerializableRetractDate(Time retractDate)
	{
		this.retractDate = retractDate;

	}

	/**
	 * @param xml
	 */
	public void parse(String xml) throws Exception
	{
		Reader r = new StringReader(xml);
		InputSource ss = new InputSource(r);

		SAXParser p = null;
		if (parserFactory == null)
		{
			parserFactory = SAXParserFactory.newInstance();
			parserFactory.setNamespaceAware(false);
			parserFactory.setValidating(false);
		}
		try
		{
			p = parserFactory.newSAXParser();
		}
		catch (ParserConfigurationException e)
		{
			throw new SAXException("Failed to get a parser ", e);
		}
		final Map<String, Object> props = new HashMap<String, Object>();
		saxSerializableProperties.setSerializableProperties(props);
		p.parse(ss, new DefaultHandler()
		{

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
			 *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
			 */
			@Override
			public void startElement(String uri, String localName, String qName,
					Attributes attributes) throws SAXException
			{

				if ("property".equals(qName))
				{

					String name = attributes.getValue("name");
					String enc = StringUtils.trimToNull(attributes.getValue("enc"));
					String value = null;
					if ("BASE64".equalsIgnoreCase(enc))
					{
						String charset = StringUtils.trimToNull(attributes
								.getValue("charset"));
						if (charset == null) charset = "UTF-8";

						value = Xml.decode(charset, attributes.getValue("value"));
					}
					else
					{
						value = attributes.getValue("value");
					}

					// deal with multiple valued lists
					if ("list".equals(attributes.getValue("list")))
					{
						// accumulate multiple values in a list
						Object current = props.get(name);

						// if we don't have a value yet, make a list to
						// hold
						// this one
						if (current == null)
						{
							List values = new Vector();
							props.put(name, values);
							values.add(value);
						}

						// if we do and it's a list, add this one
						else if (current instanceof List)
						{
							((List) current).add(value);
						}

						// if it's not a list, it's wrong!
						else
						{
							log.warn("construct(el): value set not a list: " + name);
						}
					}
					else
					{
						props.put(name, value);
					}
				}
				else if ("collection".equals(qName))
				{
					id = attributes.getValue("id");
					resourceType = ResourceType.TYPE_FOLDER;

					// extract access
					accessMode = AccessMode.INHERITED;
					String access_mode = attributes.getValue("sakai:access_mode");
					if (access_mode != null && !access_mode.trim().equals(""))
					{
						accessMode = AccessMode.fromString(access_mode);
					}

					if (accessMode == null || AccessMode.SITE == accessMode)
					{
						accessMode = AccessMode.INHERITED;
					}

					// extract release date
					// m_releaseDate = TimeService.newTime(0);
					String date0 = attributes.getValue("sakai:release_date");
					if (date0 != null && !date0.trim().equals(""))
					{
						releaseDate = conversionTimeService.newTimeGmt(date0);
						if (releaseDate.getTime() <= START_OF_TIME)
						{
							releaseDate = null;
						}
					}

					// extract retract date
					// m_retractDate = TimeService.newTimeGmt(9999,12,
					// 31, 23, 59, 59, 999);
					String date1 = attributes.getValue("sakai:retract_date");
					if (date1 != null && !date1.trim().equals(""))
					{
						retractDate = conversionTimeService.newTimeGmt(date1);
						if (retractDate.getTime() >= END_OF_TIME)
						{
							retractDate = null;
						}
					}

					String shidden = attributes.getValue("sakai:hidden");
					hidden = shidden != null && !shidden.trim().equals("")
							&& !Boolean.FALSE.toString().equalsIgnoreCase(shidden);
				}
				else if ("sakai:authzGroup".equals(qName))
				{
					String groupRef = attributes.getValue("sakai:group_name");
					if (groupRef != null)
					{
						group.add(groupRef);
					}
				}
				else if ("rightsAssignment".equals(qName))
				{

				}
				else if ("properties".equals(qName))
				{

				}

				else
				{
					log.warn("Unexpected Element " + qName);
				}

			}
		});
	}

	/**
	 * @param sax2
	 * @throws Exception
	 */
	public void check(SAXSerializableCollectionAccess sax2) throws Exception
	{
		StringBuilder sb = new StringBuilder();
		if ((accessMode != null && !accessMode.equals(sax2.accessMode))
				|| (accessMode == null && sax2.accessMode != null)
				|| (accessMode != null && sax2.accessMode == null))
		{
			sb.append("     ").append(
					"Access Mode not equal [" + accessMode + "]!=[" + sax2.accessMode
							+ "]").append("\n");
		}
		if (this.hidden != sax2.hidden)
		{
			sb.append("     ").append(
					"Hidden not equal [" + hidden + "]!=[" + sax2.hidden + "]").append(
					"\n");
		}
		if ((id != null && !id.equals(sax2.id)) || (id == null && sax2.id != null)
				|| (id != null && sax2.id == null))
		{
			sb.append("     ").append("ID not equal [" + id + "]!=[" + sax2.id + "]")
					.append("\n");
		}
		if ((releaseDate != null && sax2.releaseDate != null && (this.releaseDate
				.getTime() != sax2.releaseDate.getTime()))
				|| (releaseDate == null && sax2.releaseDate != null)
				|| (releaseDate != null && sax2.releaseDate == null))
		{
			sb.append("     ")
					.append(
							"Release not equal [" + releaseDate + "]!=["
									+ sax2.releaseDate + "]").append("\n");
		}
		if ((retractDate != null && sax2.retractDate != null && (this.retractDate
				.getTime() != sax2.retractDate.getTime()))
				|| (retractDate == null && sax2.retractDate != null)
				|| (retractDate != null && sax2.retractDate == null))
		{
			sb.append("     ")
					.append(
							"Release not equal [" + retractDate + "]!=["
									+ sax2.retractDate + "]").append("\n");
		}
		if ((resourceType != null && !resourceType.equals(sax2.resourceType))
				|| (resourceType == null && sax2.resourceType != null)
				|| (resourceType != null && sax2.resourceType == null))
		{
			sb.append("     ").append(
					"ID not equal [" + resourceType + "]!=[" + sax2.resourceType + "]")
					.append("\n");
		}
		if ((group == null && sax2.group != null)
				|| (group != null && sax2.group == null))
		{
			sb.append("     ").append(
					"group not equal [" + group + "]!=[" + sax2.group + "]").append("\n");
		}
		if (group != null && sax2.group != null)
		{
			if (this.group.size() != sax2.group.size())
			{
				sb.append("     ").append(
						"group not equal [" + group + "]!=[" + sax2.group + "]").append(
						"\n");
			}
			else
			{
				for (String g : group)
				{
					if (!sax2.group.contains(g))
					{
						sb.append("     ").append(
								"group not present in other object [" + g + "]").append(
								"\n");
					}
				}
				for (String g : sax2.group)
				{
					if (!group.contains(g))
					{
						sb.append("     ").append(
								"group not present in this object [" + g + "]").append(
								"\n");
					}
				}
			}
		}
		if (sb.length() != 0)
		{
			log.error(sb.toString());
			throw new Exception("Serialization Items do not match ");
		}

	}

}
