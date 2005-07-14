/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/
package org.sakaiproject.component.app.syllabus;

// imports
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import org.sakaiproject.exception.EmptyException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.service.legacy.content.cover.ContentTypeImageService;
import org.sakaiproject.service.framework.log.cover.Logger;
import org.sakaiproject.service.legacy.resource.ResourceProperties;
import org.sakaiproject.service.legacy.time.Time;
import org.sakaiproject.service.legacy.time.cover.TimeService;
import org.sakaiproject.service.legacy.user.User;
import org.sakaiproject.service.legacy.user.cover.UserDirectoryService;
import org.sakaiproject.util.java.EmptyIterator;
import org.sakaiproject.util.java.EnumerationIterator;
import org.sakaiproject.util.java.StringUtil;
import org.sakaiproject.util.xml.Xml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>
 * BaseResourceProperties is the base class for ResourceProperties implementations.
 * </p>
 * 
 * @author University of Michigan, Sakai Software Development Team
 * @version $Revision: 20 $
 */
public class BaseResourceProperties implements ResourceProperties
{
	/** The hashtable of properties. */
	protected Hashtable m_props = null;

	/** If the full properties have not yet been read. */
	protected transient boolean m_lazy = false;

	/**
	 * Construct.
	 */
	public BaseResourceProperties()
	{
		m_props = new Hashtable();

	} // BaseResourceProperties

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
				String enc = StringUtil.trimToNull(element.getAttribute("enc"));
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
						List values = new Vector();
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
						Logger.warn(this + "construct(el): value set not a list: " + name);
					}
				}
				else
				{
					m_props.put(name, value);
				}
			}
		}

	} // BaseResourceProperties

	/**
	 * Serialize the resource into XML, adding an element to the doc under the top of the stack element.
	 * 
	 * @param doc
	 *        The DOM doc to contain the XML (or null for a string return).
	 * @param stack
	 *        The DOM elements, the top of which is the containing element of the new "resource" element.
	 * @return The newly added element.
	 */
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
						Logger.warn(this + ".toXml: in list not string: " + name);
					}
				}
			}
			else
			{
				Logger.warn(this + ".toXml: not a string, not a value: " + name);
			}
		}

		return properties;

	} // toXml

	public boolean isLazy()
	{
		return m_lazy;
	}

	/**
	 * Access an iterator on the names of the defined properties (Strings).
	 * 
	 * @return An iterator on the names of the defined properties (Strings) (may be empty).
	 */
	public Iterator getPropertyNames()
	{
		if (m_props.size() == 0)
		{
			return new EmptyIterator();
		}

		return new EnumerationIterator(m_props.keys());

	} // getPropertyNames

	/**
	 * Access a named property as a string (won't find multi-valued ones.)
	 * 
	 * @param name
	 *        The property name.
	 * @return the property value, or null if not found.
	 */
	public String getProperty(String name)
	{
		Object value = m_props.get(name);
		if (value instanceof String) return (String) value;

		return null;

	} // getProperty

	/**
	 * Access a named property as a List of (String), good for single or multi-valued properties.
	 * 
	 * @param name
	 *        The property name.
	 * @return the property value, or null if not found.
	 */
	public List getPropertyList(String name)
	{
		Object value = m_props.get(name);
		if (value == null) return null;

		if (value instanceof String)
		{
			List rv = new Vector();
			rv.add(value);
			return rv;
		}

		else if (value instanceof List)
		{
			List rv = new Vector();
			rv.addAll((List) value);
			return rv;
		}

		return null;

	} // getPropertyList

	/**
	 * Check if a named property is a live one (auto updated).
	 * 
	 * @param name
	 *        The property name.
	 * @return True if the property is a live one, false if not.
	 */
	public boolean isLiveProperty(String name)
	{
		if ((name.equals(PROP_CREATOR)) || (name.equals(PROP_MODIFIED_BY)) || (name.equals(PROP_CREATION_DATE))
				|| (name.equals(PROP_CONTENT_LENGTH)) || (name.equals(PROP_CONTENT_TYPE)) || (name.equals(PROP_MODIFIED_DATE))
				|| (name.equals(PROP_IS_COLLECTION)))
		{
			return true;
		}

		return false;

	} // isLiveProperty

	/**
	 * Access a named property as a properly formatted string.
	 * 
	 * @param name
	 *        The property name.
	 * @return the property value, or an empty string if not found.
	 */
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
					return ContentTypeImageService.getContentTypeDisplayName((String) value);
				}
			}
			catch (EmptyException e)
			{
				return "";
			}
			catch (TypeException e)
			{
			}

			// all else failed, so just return the value
			return (String) value;
		}

		else if (value instanceof List)
		{
			StringBuffer buf = new StringBuffer();
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
			Logger.warn(this + "getPropertyFormatted: value not string, not list: " + name);
			return "";
		}

	} // getPropertyFormatted

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
	public boolean getBooleanProperty(String name) throws EmptyException, TypeException
	{
		String p = getProperty(name);
		if (p == null) throw new EmptyException();
		try
		{
			return Boolean.valueOf(p).booleanValue();
		}
		catch (Exception any)
		{
			throw new TypeException(name);
		}

	} // getBooleanProperty

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
	public long getLongProperty(String name) throws EmptyException, TypeException
	{
		String p = getProperty(name);
		if (p == null) throw new EmptyException();
		try
		{
			return Long.parseLong(p);
		}
		catch (Exception any)
		{
			throw new TypeException(name);
		}

	} // getLongProperty

	/**
	 * Access a named property as a Time.
	 * 
	 * @param name
	 *        The property name.
	 * @return the property value
	 * @exception EmptyException
	 *            if not found.
	 * @exception TypeException
	 *            if the property is found but not a Time.
	 */
	public Time getTimeProperty(String name) throws EmptyException, TypeException
	{
		String p = getProperty(name);
		if (p == null) throw new EmptyException();
		try
		{
			return TimeService.newTimeGmt(p);
		}
		catch (Exception any)
		{
			throw new TypeException(name);
		}

	} // getTimeProperty

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
	public User getUserProperty(String name) throws EmptyException, TypeException
	{
		String p = getProperty(name);
		if (p == null) throw new EmptyException();
		try
		{
			return UserDirectoryService.getUser(p);
		}
		catch (Exception any)
		{
			throw new TypeException(name);
		}

	} // getUserProperty

	/**
	 * Get the static String of PROP_CREATOR
	 * 
	 * @return The static String of PROP_CREATOR
	 */
	public String getNamePropCreator()
	{
		return PROP_CREATOR;

	} //	getNamePropCreator

	/**
	 * Get the static String of PROP_MODIFIED_BY
	 * 
	 * @return The static String of PROP_MODIFIED_BY
	 */
	public String getNamePropModifiedBy()
	{
		return PROP_MODIFIED_BY;

	} //	getNamePropModifiedBy

	/**
	 * Get the static String of PROP_CREATION_DATE
	 * 
	 * @return The static String of PROP_CREATION_DATE
	 */
	public String getNamePropCreationDate()
	{
		return PROP_CREATION_DATE;

	} //	getNamePropCreationDate

	/**
	 * Get the static String of PROP_DISPLAY_NAME
	 * 
	 * @return The static String of PROP_DISPLAY_NAME
	 */
	public String getNamePropDisplayName()
	{
		return PROP_DISPLAY_NAME;

	} //	getNamePropDisplayName

	/**
	 * Get the static String of PROP_COPYRIGHT_CHOICE
	 * 
	 * @return The static String of PROP_COPYRIGHT_CHOICE
	 */
	public String getNamePropCopyrightChoice()
	{
		return PROP_COPYRIGHT_CHOICE;

	} // getNamePropCopyrightChoice

	/**
	 * Get the static String of PROP_COPYRIGHT_ALERT
	 * 
	 * @return The static String of PROP_COPYRIGHT_ALERT
	 */
	public String getNamePropCopyrightAlert()
	{
		return PROP_COPYRIGHT_ALERT;

	} // getNamePropCopyrightAlert

	/**
	 * Get the static String of PROP_COPYRIGHT
	 * 
	 * @return The static String of PROP_COPYRIGHT
	 */
	public String getNamePropCopyright()
	{
		return PROP_COPYRIGHT;

	} // getNamePropCopyright

	/**
	 * Get the static String of PROP_CONTENT_LENGTH
	 * 
	 * @return The static String of PROP_CONTENT_LENGTH
	 */
	public String getNamePropContentLength()
	{
		return PROP_CONTENT_LENGTH;

	} //	getNamePropContentLength

	/**
	 * Get the static String of PROP_CONTENT_TYPE
	 * 
	 * @return The static String of PROP_CONTENT_TYPE
	 */
	public String getNamePropContentType()
	{
		return PROP_CONTENT_TYPE;

	} //	getNamePropContentType

	/**
	 * Get the static String of PROP_MODIFIED_DATE
	 * 
	 * @return The static String of PROP_MODIFIED_DATE
	 */
	public String getNamePropModifiedDate()
	{
		return PROP_MODIFIED_DATE;

	} // getNamePropModifiedDate

	/**
	 * Get the static String of PROP_IS_COLLECTION
	 * 
	 * @return The static String of PROP_IS_COLLECTION
	 */
	public String getNamePropIsCollection()
	{
		return PROP_IS_COLLECTION;

	} //	getNamePropIsCollection

	/**
	 * Get the static String of PROP_COLLECTION_BODY_QUOTA
	 * 
	 * @return The static String of PROP_COLLECTION_BODY_QUOTA
	 */
	public String getNamePropCollectionBodyQuota()
	{
		return PROP_COLLECTION_BODY_QUOTA;

	} //	getNamePropCollectionBodyQuota

	/**
	 * Get the static String of PROP_CHAT_ROOM
	 * 
	 * @return The static String of PROP_CHAT_ROOM
	 */
	public String getNamePropChatRoom()
	{
		return PROP_CHAT_ROOM;

	} //	getNamePropChatRoom

	/**
	 * Get the static String of PROP_TO
	 * 
	 * @return The static String of PROP_TO
	 */
	public String getNamePropTo()
	{
		return PROP_TO;

	} // getNamePropTo

	/**
	 * Get the static String of PROP_DESCRIPTION
	 * 
	 * @return The static String of PROP_DESCRIPTION
	 */
	public String getNamePropDescription()
	{
		return PROP_DESCRIPTION;

	} //	getNamePropDescription

	/**
	 * Get the static String of PROP_CALENDAR_TYPE
	 * 
	 * @return The static String of PROP_CALENDAR_TYPE
	 */
	public String getNamePropCalendarType()
	{
		return PROP_CALENDAR_TYPE;

	} //	getNamePropCalendarType

	/**
	 * Get the static String of PROP_CALENDAR_LOCATION
	 * 
	 * @return The static String of PROP_CALENDAR_LOCATION
	 */
	public String getNamePropCalendarLocation()
	{
		return PROP_CALENDAR_LOCATION;

	} //	getNamePropCalendarLocation

	/**
	 * Get the static String of PROP_REPLY_STYLE
	 * 
	 * @return The static String of PROP_REPLY_STYLE
	 */
	public String getNamePropReplyStyle()
	{
		return PROP_REPLY_STYLE;

	} //	getNamePropReplyStyle

	/**
	 * Get the static String of NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE
	 * 
	 * @return The static String of NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE
	 */
	public String getNamePropNewAssignmentCheckAddDueDate()
	{
		return NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE;

	} // getNamePropNewAssignmentCheckAddDueDate

	/**
	 * Get the static String of NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE
	 * 
	 * @return The static String of NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE
	 */
	public String getNamePropNewAssignmentCheckAutoAnnounce()
	{
		return NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE;

	} // getNamePropNewAssignmentCheckAutoAnnounce

	/**
	 * Get the static String of PROP_SUBMISSION_PREVIOUS_GRADES
	 * 
	 * @return The static String of PROP_SUBMISSION_PREVIOUS_GRADES
	 */
	public String getNamePropSubmissionPreviousGrades()
	{
		return PROP_SUBMISSION_PREVIOUS_GRADES;

	} //	getNamePropSubmissionPreviousGrades

	/**
	 * Get the static String of PROP_SUBMISSION_SCALED_PREVIOUS_GRADES
	 * 
	 * @return The static String of PROP_SUBMISSION_SCALED_PREVIOUS_GRADES
	 */
	public String getNamePropSubmissionScaledPreviousGrades()
	{
		return PROP_SUBMISSION_SCALED_PREVIOUS_GRADES;

	} // getNamePropSubmissionScaledPreviousGrades

	/**
	 * Get the static String of PROP_SUBMISSION_PREVIOUS_FEEDBACK_TEXT
	 * 
	 * @return The static String of PROP_SUBMISSION_PREVIOUS_FEEDBACK_TEXT
	 */
	public String getNamePropSubmissionPreviousFeedbackText()
	{
		return PROP_SUBMISSION_PREVIOUS_FEEDBACK_TEXT;

	} //	getNamePropSubmissionPreviousFeedbackText

	/**
	 * Get the static String of PROP_SUBMISSION_PREVIOUS_FEEDBACK_COMMENT
	 * 
	 * @return The static String of PROP_SUBMISSION_PREVIOUS_FEEDBACK_COMMENT
	 */
	public String getNamePropSubmissionPreviousFeedbackComment()
	{
		return PROP_SUBMISSION_PREVIOUS_FEEDBACK_COMMENT;

	} //	getNamePropSubmissionPreviousFeedbackComment

	/**
	 * Get the static String of PROP_ASSIGNMENT_DELETED
	 * 
	 * @return The static String of PROP_ASSIGNMENT_DELETED
	 */
	public String getNamePropAssignmentDeleted()
	{
		return PROP_ASSIGNMENT_DELETED;

	} //	getNamePropAssignmentDeleted

	/**
	 * Get the static String of TYPE_URL
	 * 
	 * @return The static String of TYPE_URL
	 */
	public String getTypeUrl()
	{
		return TYPE_URL;
	}

} // BaseResourceProperties



