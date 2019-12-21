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

package org.sakaiproject.util;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentTypeImageService;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.serialize.SerializableEntity;
import org.sakaiproject.entity.api.serialize.SerializablePropertiesAccess;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * BaseResourceProperties is the base class for ResourceProperties implementations.
 * </p>
 */
@Slf4j
public class BaseResourceProperties implements ResourceProperties, SerializablePropertiesAccess, SerializableEntity
{
	/** A fixed class serian number. */
	private static final long serialVersionUID = 1L;

	/** The hashtable of properties. */
	protected Hashtable<String, Object> m_props = null;

	/** If the full properties have not yet been read. */
	protected transient boolean m_lazy = false;

	/**
	 * Construct.
	 */
	public BaseResourceProperties()
	{
		m_props = new Hashtable<>();
	}

	public BaseResourceProperties(Map<String, String> map) {
		this();
		for (Map.Entry<String, String> entry : map.entrySet()) {
		    if (entry.getKey() != null) {
		    	addProperty(entry.getKey(), entry.getValue());
			}
		}
	}

	/**
	 * Construct from XML.
	 * 
	 * @param el
	 *        The XML DOM element.
	 */
	public BaseResourceProperties(Element el)
	{
		this();

		// the children (property)
		NodeList children = el.getChildNodes();
		final int length = children.getLength();
		for (int i = 0; i < length; i++)
		{
			Node child = children.item(i);
			if (child.getNodeType() != Node.ELEMENT_NODE) continue;
			Element element = (Element) child;

			// look for property
			if (element.getTagName().equals("property"))
			{
				String name = element.getAttribute("name");
				String enc = StringUtils.trimToNull(element.getAttribute("enc"));
				String value = null;
				if ("BASE64".equalsIgnoreCase(enc))
				{
					value = Xml.decodeAttribute(element, "value");
				}
				else
				{
					value = element.getAttribute("value");
				}

				// deal with multiple valued lists
				if ("list".equals(element.getAttribute("list")))
				{
					// accumulate multiple values in a list
					Object current = m_props.get(name);

					// if we don't have a value yet, make a list to hold this one
					if (current == null)
					{
						List<String> values = new ArrayList<>();
						m_props.put(name, values);
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
					m_props.put(name, value);
				}
			}
		}
	}

	/**
	 * Serialize the resource into XML, adding an element to the doc under the top of the stack element.
	 * 
	 * @param doc
	 *        The DOM doc to contain the XML (or null for a string return).
	 * @param stack
	 *        The DOM elements, the top of which is the containing element of the new "resource" element.
	 * @return The newly added element.
	 */
	@Override
	public Element toXml(Document doc, Stack stack)
	{
		Element properties = doc.createElement("properties");
		((Element) stack.peek()).appendChild(properties);
		Enumeration props = m_props.keys();
		while (props.hasMoreElements())
		{
			String name = (String) props.nextElement();
			Object value = m_props.get(name);
			if (value instanceof String)
			{
				Element propElement = doc.createElement("property");
				properties.appendChild(propElement);
				propElement.setAttribute("name", name);

				// encode to allow special characters in the value
				Xml.encodeAttribute(propElement, "value", (String) value);
				propElement.setAttribute("enc", "BASE64");
			}
			else if (value instanceof List)
			{
				for (Iterator iValues = ((List) value).iterator(); iValues.hasNext();)
				{
					Object val = iValues.next();
					if (val instanceof String)
					{
						Element propElement = doc.createElement("property");
						properties.appendChild(propElement);
						propElement.setAttribute("name", name);
						Xml.encodeAttribute(propElement, "value", (String) val);
						propElement.setAttribute("enc", "BASE64");
						propElement.setAttribute("list", "list");
					}
					else
					{
						log.warn(".toXml: in list not string: " + name);
					}
				}
			}
			else
			{
				log.warn(".toXml: not a string, not a value: " + name);
			}
		}

		return properties;
	}

	public boolean isLazy()
	{
		return m_lazy;
	}

	/**
	 * Access an iterator on the names of the defined properties (Strings).
	 * 
	 * @return An iterator on the names of the defined properties (Strings) (may be empty).
	 */
	@Override
	public Iterator<String> getPropertyNames()
	{
		if (m_props.size() == 0)
		{
			return new EmptyIterator();
		}

		return new EnumerationIterator(m_props.keys());
	}

	/**
	 * Access a named property as a string (won't find multi-valued ones.)
	 * 
	 * @param name
	 *        The property name.
	 * @return the property value, or null if not found.
	 */
	@Override
	public String getProperty(String name)
	{
		Object value = m_props.get(name);
		if (value instanceof String) return (String) value;

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object get(String name)
	{
		return m_props.get(name);
	}

	/**
	 * Access a named property as a List of (String), good for single or multi-valued properties.
	 * 
	 * @param name
	 *        The property name.
	 * @return the property value, or null if not found.
	 */
	@Override
	public List<String> getPropertyList(String name)
	{
		Object value = m_props.get(name);
		if (value == null) return null;

		if (value instanceof String)
		{
			List rv = new ArrayList<>();
			rv.add(value);
			return rv;
		}

		else if (value instanceof List)
		{
			List rv = new ArrayList<>();
			rv.addAll((List) value);
			return rv;
		}

		return null;
	}

	/**
	 * Check if a named property is a live one (auto updated).
	 * 
	 * @param name
	 *        The property name.
	 * @return True if the property is a live one, false if not.
	 */
	@Override
	public boolean isLiveProperty(String name)
	{
		if ((name.equals(PROP_CREATOR)) || (name.equals(PROP_MODIFIED_BY)) || (name.equals(PROP_CREATION_DATE))
				|| (name.equals(PROP_CONTENT_LENGTH)) || (name.equals(PROP_CONTENT_TYPE)) || (name.equals(PROP_MODIFIED_DATE))
				|| (name.equals(PROP_IS_COLLECTION)))
		{
			return true;
		}

		return false;
	}

	/**
	 * Access a named property as a properly formatted string.
	 * 
	 * @param name
	 *        The property name.
	 * @return the property value, or an empty string if not found.
	 */
	@Override
	public String getPropertyFormatted(String name)
	{
		Object value = m_props.get(name);

		// if missing, return blank
		if (value == null) return "";

		if (value instanceof String)
		{
			try
			{
				// check all known properties...

				// User
				if ((name.equals(PROP_CREATOR)) || (name.equals(PROP_MODIFIED_BY)) || name.equals(PROP_TO))
				{
					return getUserProperty(name).getDisplayName(); // %%% no user?
				}

				// Time
				else if ((name.equals(PROP_CREATION_DATE)) || (name.equals(PROP_MODIFIED_DATE)))
				{
					return getTimeProperty(name).toStringLocalFull();
				}

				// content length- in kb
				else if (name.equals(PROP_CONTENT_LENGTH))
				{
					long len = getLongProperty(name);
					String[] byteString = { "KB", "KB", "MB", "GB" };
					int count = 0;
					long newLen = 0;
					long lenBytesExtra = len;

					while (len > 1024)
					{
						newLen = len / 1024;
						lenBytesExtra = len - (newLen * 1024);
						len = newLen;
						count++;
					}

					if ((lenBytesExtra >= 512) || ((lenBytesExtra > 0) && (newLen == 0)))
					{
						newLen++;
					}

					return Long.toString(newLen) + " " + byteString[count];
				}

				// content type
				else if (name.equals(PROP_CONTENT_TYPE))
				{
					ContentTypeImageService contentTypeImageService = ComponentManager.get(ContentTypeImageService.class);
					return contentTypeImageService.getContentTypeDisplayName((String) value);
				}
			}
			catch (EntityPropertyNotDefinedException e)
			{
				return "";
			}
			catch (EntityPropertyTypeException e)
			{
			}

			// all else failed, so just return the value
			return (String) value;
		}

		else if (value instanceof List)
		{
			StringBuilder buf = new StringBuilder();
			for (Iterator i = ((List) value).iterator(); i.hasNext();)
			{
				String val = (String) i.next();
				buf.append(val);
				if (i.hasNext())
				{
					buf.append(", ");
				}
			}
			return buf.toString();
		}

		else
		{
			log.warn("getPropertyFormatted: value not string, not list: " + name);
			return "";
		}
	}

	/**
	 * Access a named property as a boolean.
	 * 
	 * @param name
	 *        The property name.
	 * @return the property value.
	 * @exception EmptyException
	 *            if not found.
	 * @exception TypeException
	 *            if the property is found but not a boolean.
	 */
	@Override
	public boolean getBooleanProperty(String name) throws EntityPropertyNotDefinedException, EntityPropertyTypeException
	{
		String p = getProperty(name);
		if (p == null) throw new EntityPropertyNotDefinedException();
		try
		{
			return Boolean.valueOf(p).booleanValue();
		}
		catch (Exception any)
		{
			throw new EntityPropertyTypeException(name);
		}

	}

	/**
	 * Access a named property as a long.
	 * 
	 * @param name
	 *        The property name.
	 * @return the property value.
	 * @exception EmptyException
	 *            if not found.
	 * @exception TypeException
	 *            if the property is found but not a long.
	 */
	@Override
	public long getLongProperty(String name) throws EntityPropertyNotDefinedException, EntityPropertyTypeException
	{
		String p = getProperty(name);
		if (p == null) throw new EntityPropertyNotDefinedException();
		try
		{
			return Long.parseLong(p);
		}
		catch (Exception any)
		{
			throw new EntityPropertyTypeException(name);
		}
	}

	@Override
	public Time getTimeProperty(String name) throws EntityPropertyNotDefinedException, EntityPropertyTypeException
	{
		String p = getProperty(name);
		if (p == null) throw new EntityPropertyNotDefinedException();
		try
		{
			TimeService timeService = ComponentManager.get(TimeService.class);
			return timeService.newTimeGmt(p);
		}
		catch (Exception any)
		{
			throw new EntityPropertyTypeException(name);
		}

	} // getTimeProperty

	@Override
	public Date getDateProperty(String name) throws EntityPropertyNotDefinedException, EntityPropertyTypeException
	{
		Time time = getTimeProperty(name);
		return new Date(time.getTime());
	}
	
	@Override
	public Instant getInstantProperty(String name) throws EntityPropertyNotDefinedException, EntityPropertyTypeException
	{
		Time time = getTimeProperty(name);
		return Instant.ofEpochMilli(time.getTime());
	}
	
	/**
	 * Access a named property as a User.
	 * 
	 * @param name
	 *        The property name.
	 * @return the property value
	 * @exception EmptyException
	 *            if not found.
	 * @exception TypeException
	 *            if the property is found but not a User.
	 */
	private User getUserProperty(String name) throws EntityPropertyNotDefinedException, EntityPropertyTypeException
	{
		String p = getProperty(name);
		if (p == null) throw new EntityPropertyNotDefinedException();
		try
		{
			UserDirectoryService userDirectoryService = ComponentManager.get(UserDirectoryService.class);
			return userDirectoryService.getUser(p);
		}
		catch (Exception any)
		{
			throw new EntityPropertyTypeException(name);
		}
	}

	/**
	 * Get the static String of PROP_CREATOR
	 * 
	 * @return The static String of PROP_CREATOR
	 */
	@Override
	public String getNamePropCreator()
	{
		return PROP_CREATOR;
	}

	/**
	 * Get the static String of PROP_MODIFIED_BY
	 * 
	 * @return The static String of PROP_MODIFIED_BY
	 */
	@Override
	public String getNamePropModifiedBy()
	{
		return PROP_MODIFIED_BY;
	}

	/**
	 * Get the static String of PROP_CREATION_DATE
	 * 
	 * @return The static String of PROP_CREATION_DATE
	 */
	@Override
	public String getNamePropCreationDate()
	{
		return PROP_CREATION_DATE;
	}

	/**
	 * Get the static String of PROP_DISPLAY_NAME
	 * 
	 * @return The static String of PROP_DISPLAY_NAME
	 */
	@Override
	public String getNamePropDisplayName()
	{
		return PROP_DISPLAY_NAME;
	}

	/**
	 * Get the static String of PROP_COPYRIGHT_CHOICE
	 * 
	 * @return The static String of PROP_COPYRIGHT_CHOICE
	 */
	@Override
	public String getNamePropCopyrightChoice()
	{
		return PROP_COPYRIGHT_CHOICE;
	}

	/**
	 * Get the static String of PROP_COPYRIGHT_ALERT
	 * 
	 * @return The static String of PROP_COPYRIGHT_ALERT
	 */
	@Override
	public String getNamePropCopyrightAlert()
	{
		return PROP_COPYRIGHT_ALERT;
	}

	/**
	 * Get the static String of PROP_COPYRIGHT
	 * 
	 * @return The static String of PROP_COPYRIGHT
	 */
	@Override
	public String getNamePropCopyright()
	{
		return PROP_COPYRIGHT;
	}

	/**
	 * Get the static String of PROP_CONTENT_LENGTH
	 * 
	 * @return The static String of PROP_CONTENT_LENGTH
	 */
	@Override
	public String getNamePropContentLength()
	{
		return PROP_CONTENT_LENGTH;
	}

	/**
	 * Get the static String of PROP_CONTENT_TYPE
	 * 
	 * @return The static String of PROP_CONTENT_TYPE
	 */
	@Override
	public String getNamePropContentType()
	{
		return PROP_CONTENT_TYPE;
	}

	/**
	 * Get the static String of PROP_MODIFIED_DATE
	 * 
	 * @return The static String of PROP_MODIFIED_DATE
	 */
	@Override
	public String getNamePropModifiedDate()
	{
		return PROP_MODIFIED_DATE;
	}

	/**
	 * Get the static String of PROP_IS_COLLECTION
	 * 
	 * @return The static String of PROP_IS_COLLECTION
	 */
	@Override
	public String getNamePropIsCollection()
	{
		return PROP_IS_COLLECTION;
	}

	/**
	 * Get the static String of PROP_COLLECTION_BODY_QUOTA
	 * 
	 * @return The static String of PROP_COLLECTION_BODY_QUOTA
	 */
	@Override
	public String getNamePropCollectionBodyQuota()
	{
		return PROP_COLLECTION_BODY_QUOTA;
	}

	/**
	 * Get the static String of PROP_CHAT_ROOM
	 * 
	 * @return The static String of PROP_CHAT_ROOM
	 */
	@Override
	public String getNamePropChatRoom()
	{
		return PROP_CHAT_ROOM;
	}

	/**
	 * Get the static String of PROP_TO
	 * 
	 * @return The static String of PROP_TO
	 */
	@Override
	public String getNamePropTo()
	{
		return PROP_TO;
	}

	/**
	 * Get the static String of PROP_DESCRIPTION
	 * 
	 * @return The static String of PROP_DESCRIPTION
	 */
	@Override
	public String getNamePropDescription()
	{
		return PROP_DESCRIPTION;
	}

	/**
	 * Get the static String of PROP_CALENDAR_TYPE
	 * 
	 * @return The static String of PROP_CALENDAR_TYPE
	 */
	@Override
	public String getNamePropCalendarType()
	{
		return PROP_CALENDAR_TYPE;
	}

	/**
	 * Get the static String of PROP_CALENDAR_LOCATION
	 * 
	 * @return The static String of PROP_CALENDAR_LOCATION
	 */
	@Override
	public String getNamePropCalendarLocation()
	{
		return PROP_CALENDAR_LOCATION;
	}

	/**
	 * Get the static String of PROP_REPLY_STYLE
	 * 
	 * @return The static String of PROP_REPLY_STYLE
	 */
	@Override
	public String getNamePropReplyStyle()
	{
		return PROP_REPLY_STYLE;
	}

	/**
	 * Get the static String of NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE
	 * 
	 * @return The static String of NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE
	 */
	@Override
	public String getNamePropNewAssignmentCheckAddDueDate()
	{
		return NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE;
	}

	/**
	 * Get the static String of NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE
	 * 
	 * @return The static String of NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE
	 */
	@Override
	public String getNamePropNewAssignmentCheckAutoAnnounce()
	{
		return NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE;
	}

	/**
	 * Get the static String of PROP_SUBMISSION_PREVIOUS_GRADES
	 * 
	 * @return The static String of PROP_SUBMISSION_PREVIOUS_GRADES
	 */
	@Override
	public String getNamePropSubmissionPreviousGrades()
	{
		return PROP_SUBMISSION_PREVIOUS_GRADES;
	}

	/**
	 * Get the static String of PROP_SUBMISSION_SCALED_PREVIOUS_GRADES
	 * 
	 * @return The static String of PROP_SUBMISSION_SCALED_PREVIOUS_GRADES
	 */
	@Override
	public String getNamePropSubmissionScaledPreviousGrades()
	{
		return PROP_SUBMISSION_SCALED_PREVIOUS_GRADES;
	}

	/**
	 * Get the static String of PROP_SUBMISSION_PREVIOUS_FEEDBACK_TEXT
	 * 
	 * @return The static String of PROP_SUBMISSION_PREVIOUS_FEEDBACK_TEXT
	 */
	@Override
	public String getNamePropSubmissionPreviousFeedbackText()
	{
		return PROP_SUBMISSION_PREVIOUS_FEEDBACK_TEXT;
	}

	/**
	 * Get the static String of PROP_SUBMISSION_PREVIOUS_FEEDBACK_COMMENT
	 * 
	 * @return The static String of PROP_SUBMISSION_PREVIOUS_FEEDBACK_COMMENT
	 */
	@Override
	public String getNamePropSubmissionPreviousFeedbackComment()
	{
		return PROP_SUBMISSION_PREVIOUS_FEEDBACK_COMMENT;
	}

	/**
	 * Get the static String of PROP_ASSIGNMENT_DELETED
	 * 
	 * @return The static String of PROP_ASSIGNMENT_DELETED
	 */
	@Override
	public String getNamePropAssignmentDeleted()
	{
		return PROP_ASSIGNMENT_DELETED;
	}

	/**
	 * Get the static String of TYPE_URL
	 * 
	 * @return The static String of TYPE_URL
	 */
	@Override
	public String getTypeUrl()
	{
		return TYPE_URL;
	}

	/**
	 * Get the static String of PROP_STRUCTOBJ_TYPE
	 * 
	 * @return The static String of PROP_STRUCTOBJ_TYPE
	 */
	@Override
	public String getNamePropStructObjType()
	{
		return PROP_STRUCTOBJ_TYPE;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setLazy(boolean lazy)
	{
		m_lazy = lazy;
	}

	/**
	 * Add a single valued property.
	 * 
	 * @param name
	 *        The property name.
	 * @param value
	 *        The property value.
	 */
	@Override
	public void addProperty(String name, String value)
	{
		// protect against a null put
		if (value == null) value = "";

		m_props.put(name, value);
	}

	/**
	 * Add a value to a multi-valued property.
	 * 
	 * @param name
	 *        The property name.
	 * @param value
	 *        The property value.
	 */
	@Override
	public void addPropertyToList(String name, String value)
	{
		// protect against a null put
		if (value == null) value = "";

		// accumulate multiple values in a list
		Object current = m_props.get(name);

		// if we don't have a value yet, make a list to hold this one
		if (current == null)
		{
			List values = new ArrayList<>();
			m_props.put(name, values);
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
			log.warn("addPropertyToList() value set not a list: " + name);
		}
	}

	/**
	 * Add all the properties from the other ResourceProperties object.
	 * 
	 * @param other
	 *        The ResourceProperties to add.
	 */
	@Override
	public void addAll(ResourceProperties other)
	{
		for (Iterator iNames = other.getPropertyNames(); iNames.hasNext();)
		{
			String name = (String) iNames.next();
			
			// use the general accessor for String or List return
			Object value = other.get(name);

			if (value != null)
			{
				// Strings are immutable so can be placed directly in
				if (value instanceof String)
				{
					m_props.put(name, value);
				}
				
				// deep copy the list
				else if (value instanceof List)
				{
					List list = new ArrayList<>();
					list.addAll((List) value);
					m_props.put(name, list);					
				}
			}
		}
	}

	/**
	 * Add all the properties from the Properties object.
	 * 
	 * @param props
	 *        The Properties to add.
	 */
	@Override
	public void addAll(Properties props)
	{
		// if there's a list, it must be deep copied
		for (Enumeration e = props.propertyNames(); e.hasMoreElements();)
		{
			String name = (String) e.nextElement();
			Object value = props.get(name);
			if (value instanceof List)
			{
				List list = new ArrayList<>();
				list.addAll((List) value);
				m_props.put(name, list);
			}
			else
			{
				m_props.put(name, value);
			}
		}
	}

	/**
	 * Remove all properties.
	 */
	@Override
	public void clear()
	{
		m_props.clear();
	}

	/**
	 * Remove a property.
	 * 
	 * @param name
	 *        The property name.
	 */
	@Override
	public void removeProperty(String name)
	{
		m_props.remove(name);
	}

	/**
	 * Take all values from this object.
	 * 
	 * @param props the ResourceProperties object to take values from.
	 */
	@Override
	public void set(ResourceProperties props)
	{
		clear();
		addAll(props);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.entity.api.ResourceProperties#getContentHander()
	 */
	public ContentHandler getContentHander()
	{
		return new DefaultHandler()
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
						String charset = StringUtils.trimToNull(attributes.getValue("charset"));
						if (charset == null) charset = "UTF-8";

						value = Xml.decode(charset,attributes.getValue("value"));
					}
					else
					{
						value = attributes.getValue("value");
					}
						
					// deal with multiple valued lists
					if ("list".equals(attributes.getValue("list")))
					{
						// accumulate multiple values in a list
						Object current = m_props.get(name);

						// if we don't have a value yet, make a list to hold
						// this one
						if (current == null)
						{
							List values = new ArrayList<>();
							m_props.put(name, values);
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
						m_props.put(name, value);
					}
				}
			}

		};
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.SerializableProperties#getSerializableProperties()
	 */
	@Override
	public Map<String, Object> getSerializableProperties()
	{
		Map<String, Object>  m = new HashMap<String, Object>();
		m.putAll(m_props);
		return m;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.SerializableProperties#setSerializableProperties(java.util.Map)
	 */
	@Override
	public void setSerializableProperties(Map<String, Object> properties)
	{
		m_props.clear();
		m_props.putAll(properties);
		
	}
}
