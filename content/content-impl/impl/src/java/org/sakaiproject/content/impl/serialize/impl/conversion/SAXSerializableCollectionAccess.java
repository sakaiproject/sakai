/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.GroupAwareEntity.AccessMode;
import org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess;
import org.sakaiproject.content.impl.serialize.impl.Type1BaseContentCollectionSerializer;
import org.sakaiproject.entity.api.serialize.SerializableEntity;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Xml;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author ieb
 */
public class SAXSerializableCollectionAccess implements SerializableCollectionAccess,
		SerializableEntity
{

	protected static final long END_OF_TIME = 8000L * 365L * 24L * 60L * 60L * 1000L;

	protected static final long START_OF_TIME = 365L * 24L * 60L * 60L * 1000L;

	protected static final Log log = LogFactory
			.getLog(SAXSerializableResourceAccess.class);

	private Type1BaseContentCollectionSerializer type1CollectionSerializer;

	private ThreadLocal<SAXParser> parserHolder = new ThreadLocal<SAXParser>();

	private SAXParserFactory parserFactory;

	private AccessMode accessMode;

	private Collection<String> group;

	private boolean hidden;

	private String id;

	private SAXSerializablePropertiesAccess saxSerializableProperties;

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
		if (xml.startsWith(Type1BaseContentCollectionSerializer.BLOB_ID))
		{
			type1CollectionSerializer.parse(this, xml);
		}
		else
		{
			Reader r = new StringReader(xml);
			InputSource ss = new InputSource(r);

			SAXParser p = parserHolder.get();
			if (p == null)
			{
				if (parserFactory == null)
				{
					parserFactory = SAXParserFactory.newInstance();
					parserFactory.setNamespaceAware(false);
					parserFactory.setValidating(false);
				}
				try
				{
					p = parserFactory.newSAXParser();
					parserHolder.set(p);
				}
				catch (ParserConfigurationException e)
				{
					throw new SAXException("Failed to get a parser ", e);
				}
			}
			else
			{
				p.reset();
			}
			final Map<String, Object> props = new HashMap<String, Object>();
			p.parse(ss, new DefaultHandler()
			{

				/*
				 * (non-Javadoc)
				 * 
				 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
				 *      java.lang.String, java.lang.String,
				 *      org.xml.sax.Attributes)
				 */
				@Override
				public void startElement(String uri, String localName, String qName,
						Attributes attributes) throws SAXException
				{

					if ("property".equals(qName))
					{

						String name = attributes.getValue("name");
						String enc = StringUtil.trimToNull(attributes.getValue("enc"));
						String value = null;
						if ("BASE64".equalsIgnoreCase(enc))
						{
							String charset = StringUtil.trimToNull(attributes
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
					if ("collection".equals(qName))
					{
						id = attributes.getValue("id");
						resourceType = ResourceType.TYPE_FOLDER;

						// extract access
						AccessMode access = AccessMode.INHERITED;
						String access_mode = attributes.getValue("sakai:access_mode");
						if (access_mode != null && !access_mode.trim().equals(""))
						{
							access = AccessMode.fromString(access_mode);
						}

						if (access == null || AccessMode.SITE == access)
						{
							access = AccessMode.INHERITED;
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
					else
					{
						log.warn("Unexpected Element " + qName);
					}

				}
			});
		}
	}

}
