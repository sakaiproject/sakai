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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.ResourceTypeRegistry;
import org.sakaiproject.content.api.GroupAwareEntity.AccessMode;
import org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess;
import org.sakaiproject.content.impl.serialize.impl.Type1BaseContentResourceSerializer;
import org.sakaiproject.entity.api.serialize.EntityParseException;
import org.sakaiproject.entity.api.serialize.SerializableEntity;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.util.Xml;

/**
 * @author ieb
 */
@Slf4j
public class SAXSerializableResourceAccess implements SerializableResourceAccess,
		SerializableEntity
{
	protected static final long END_OF_TIME = 8000L * 365L * 24L * 60L * 60L * 1000L;

	protected static final long START_OF_TIME = 365L * 24L * 60L * 60L * 1000L;

	private Type1BaseContentResourceSerializer type1ResourceSerializer;

	private SAXParserFactory parserFactory;

	private AccessMode accessMode = AccessMode.INHERITED;

	private long contentLength;

	private String contentType;

	private String filePath;

	private Collection<String> group = new ArrayList<String>();

	private boolean hidden;

	private String id;

	private SAXSerializablePropertiesAccess saxSerializableProperties = new SAXSerializablePropertiesAccess();

	private Time releaseDate;

	private String resourceType;

	private Time retractDate;

	private byte[] body;

	private ConversionTimeService conversionTimeService;

	public SAXSerializableResourceAccess()
	{
		type1ResourceSerializer = new Type1BaseContentResourceSerializer();
		conversionTimeService = new ConversionTimeService();
		type1ResourceSerializer.setTimeService(conversionTimeService);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#getResourceTypeRegistry()
	 */
	public ResourceTypeRegistry getResourceTypeRegistry()
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#getSerializableAccess()
	 */
	public AccessMode getSerializableAccess()
	{
		return accessMode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#getSerializableBody()
	 */
	public byte[] getSerializableBody()
	{
		return body;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#getSerializableContentLength()
	 */
	public long getSerializableContentLength()
	{
		return contentLength;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#getSerializableContentType()
	 */
	public String getSerializableContentType()
	{
		return contentType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#getSerializableFilePath()
	 */
	public String getSerializableFilePath()
	{
		return filePath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#getSerializableGroup()
	 */
	public Collection<String> getSerializableGroup()
	{
		return group;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#getSerializableHidden()
	 */
	public boolean getSerializableHidden()
	{
		return hidden;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#getSerializableId()
	 */
	public String getSerializableId()
	{
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#getSerializableProperties()
	 */
	public SerializableEntity getSerializableProperties()
	{
		return saxSerializableProperties;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#getSerializableReleaseDate()
	 */
	public Time getSerializableReleaseDate()
	{
		return releaseDate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#getSerializableResourceType()
	 */
	public String getSerializableResourceType()
	{
		return resourceType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#getSerializableRetractDate()
	 */
	public Time getSerializableRetractDate()
	{
		return retractDate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#setSerializableAccess(org.sakaiproject.content.api.GroupAwareEntity.AccessMode)
	 */
	public void setSerializableAccess(AccessMode access)
	{
		accessMode = access;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#setSerializableBody(byte[])
	 */
	public void setSerializableBody(byte[] body)
	{

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#setSerializableContentLength(long)
	 */
	public void setSerializableContentLength(long contentLength)
	{
		this.contentLength = contentLength;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#setSerializableContentType(java.lang.String)
	 */
	public void setSerializableContentType(String contentType)
	{
		this.contentType = contentType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#setSerializableFilePath(java.lang.String)
	 */
	public void setSerializableFilePath(String filePath)
	{
		this.filePath = filePath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#setSerializableGroups(java.util.Collection)
	 */
	public void setSerializableGroups(Collection<String> groups)
	{
		this.group = groups;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#setSerializableHidden(boolean)
	 */
	public void setSerializableHidden(boolean hidden)
	{
		this.hidden = hidden;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#setSerializableId(java.lang.String)
	 */
	public void setSerializableId(String id)
	{
		this.id = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#setSerializableReleaseDate(org.sakaiproject.time.api.Time)
	 */
	public void setSerializableReleaseDate(Time releaseDate)
	{
		this.releaseDate = releaseDate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#setSerializableResourceType(java.lang.String)
	 */
	public void setSerializableResourceType(String resourceType)
	{
		this.resourceType = resourceType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#setSerializableRetractDate(org.sakaiproject.time.api.Time)
	 */
	public void setSerializableRetractDate(Time retractDate)
	{
		this.retractDate = retractDate;
	}
	


	/**
	 * @param xml
	 * @throws EntityParseException
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
				if(qName == null)
				{
					// will be ignored
				}
				else
				{
					qName = qName.trim();
				}
				if ("property".equalsIgnoreCase(qName))
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
				else if ("resource".equalsIgnoreCase(qName))
				{
					id = attributes.getValue("id");
					contentType = StringUtils.trimToNull(attributes
							.getValue("content-type"));
					contentLength = 0;
					try
					{
						contentLength = Integer.parseInt(attributes
								.getValue("content-length"));
					}
					catch (Exception ignore)
					{
					}
					resourceType = StringUtils.trimToNull(attributes
							.getValue("resource-type"));
					
					if(resourceType == null)
					{
						resourceType = ResourceType.TYPE_UPLOAD;
					}

					String enc = StringUtils.trimToNull(attributes.getValue("body"));
					if (enc != null)
					{
						byte[] decoded = null;
						try
						{
							decoded = Base64.decodeBase64(enc.getBytes("UTF-8"));
						}
						catch (UnsupportedEncodingException e)
						{
							log.error(e.getMessage(), e);
						}
						body = new byte[(int) contentLength];
						System.arraycopy(decoded, 0, body, 0, (int) contentLength);
					}

					filePath = StringUtils.trimToNull(attributes.getValue("filePath"));
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

					String shidden = attributes.getValue("sakai:hidden");
					hidden = shidden != null && !shidden.trim().equals("")
							&& !Boolean.FALSE.toString().equalsIgnoreCase(shidden);

					if (hidden)
					{
						releaseDate = null;
						retractDate = null;
					}
					else
					{
						// extract release date
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
						String date1 = attributes.getValue("sakai:retract_date");
						if (date1 != null && !date1.trim().equals(""))
						{
							retractDate = conversionTimeService.newTimeGmt(date1);
							if (retractDate.getTime() >= END_OF_TIME)
							{
								retractDate = null;
							}
						}
					}
				}
				else if ("sakai:authzGroup".equalsIgnoreCase(qName))
				{
					if (group == null)
					{
						group = new ArrayList<String>();
					}
					group.add(attributes.getValue("sakai:group_name"));
				}
				else if ("properties".equalsIgnoreCase(qName))
				{

				}
				else if ("members".equalsIgnoreCase(qName))
				{
					// ignore
				}
				else if ("member".equalsIgnoreCase(qName))
				{
					// ignore
				}
				else
				{
					log.warn("Unexpected Element \"" + qName + "\"");
				}

			}
		});
	}

	/**
	 * @param sax2
	 * @throws Exception
	 */
	public void check(SAXSerializableResourceAccess sax2) throws Exception
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
					"resourceType not equal [" + resourceType + "]!=[" + sax2.resourceType + "]")
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
		if ((body == null && sax2.body != null) || (body != null && sax2.body == null))
		{
			sb.append("     ").append(
					"group not equal [" + Arrays.toString(body) + "]!=[" + Arrays.toString(sax2.body) + "]").append("\n");
		}
		if (body != null && sax2.body != null)
		{
			if (this.body.length != sax2.body.length)
			{
				sb.append("     ").append(
						"group not equal [" + Arrays.toString(body) + "]!=[" + Arrays.toString(sax2.body) + "]").append(
						"\n");
			}
			else
			{
				for (int i = 0; i < body.length; i++)
				{
					if (body[i] != sax2.body[i])
					{
						sb.append("     ").append(
								"group not equal [" + body[i] + "]!=[" + sax2.body[i]
										+ "]").append("\n");
					}
				}
			}
		}
		if (this.contentLength != sax2.contentLength)
		{
			sb.append("     ").append(
					"ContentLength not equal [" + contentLength + "]!=["
							+ sax2.contentLength + "]").append("\n");
		}
		if(contentType != null && contentType.trim().equals(""))
		{
			contentType = null;
		}
		if(sax2.contentType != null && sax2.contentType.trim().equals(""))
		{
			sax2.contentType = null;
		}
		if ((contentType != null && !contentType.equals(sax2.contentType))
				|| (contentType == null && sax2.contentType != null)
				|| (contentType != null && sax2.contentType == null))
		{
			sb.append("     ").append(
					"Content Type not equal [" + contentType + "]!=[" + sax2.contentType
							+ "]").append("\n");
		}
		if(filePath != null && filePath.trim().equals(""))
		{
			filePath = null;
		}
		if(sax2.filePath != null && sax2.filePath.trim().equals(""))
		{
			sax2.filePath = null;
		}
		if ((filePath != null && !filePath.equals(sax2.filePath))
				|| (filePath == null && sax2.filePath != null)
				|| (filePath != null && sax2.filePath == null))
		{
			sb.append("     ").append(
					"FilePath not equal [" + filePath + "]!=[" + sax2.filePath + "]")
					.append("\n");
		}
		if (sb.length() != 0)
		{
			log.error(sb.toString());
			throw new Exception("Serialization Items do not match ");
		}
		saxSerializableProperties.check((SAXSerializablePropertiesAccess) sax2
				.getSerializableProperties());
	}
}
