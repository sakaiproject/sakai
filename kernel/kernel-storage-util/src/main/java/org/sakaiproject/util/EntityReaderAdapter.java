/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/kernel-util/src/main/java/org/sakaiproject/util/EntityReaderAdapter.java $
 * $Id: EntityReaderAdapter.java 51317 2008-08-24 04:38:02Z csev@umich.edu $
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

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.xml.sax.SAXException;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.serialize.EntityParseException;
import org.sakaiproject.entity.api.serialize.EntityReaderHandler;

/**
 * This class provides a bridge between the XML serializers and the non XML serializers, so that whatever
 * the deployed serializer, exisitng Blobs can be read. If this class is configured, it will also 
 * write the BLOBS out in the new format.
 * This class should be used as a Proxy in anything implementing an EntityReader to ensure that other
 * formats can still be read.
 * @author ieb
 */
@Slf4j
public class EntityReaderAdapter implements EntityReaderHandler
{
	private SingleStorageUser storageUser = null;

	private String containerEntryTagName;

	private String resourceEntryTagName;

	private SAXEntityReader saxEntityReader;

	private EntityReaderHandler target;

	public void init() {
		
	}
	public void destroy() {
		
	}
	


	/**
	 * @throws IOException
	 * @throws SAXException
	 * @see org.sakaiproject.entity.api.serialize.EntityReader#parseResource(java.lang.String)
	 */
	public Entity parse(String xml, byte[] blob) throws EntityParseException
	{
		if ( target.accept(blob) ) {
			return target.parse(xml,blob);			
		} else {
			if (saxEntityReader != null)
			{
				try
				{
					DefaultEntityHandler deh = saxEntityReader.getDefaultHandler(saxEntityReader.getServices());
					StorageUtils.processString(xml, deh);
					return deh.getEntity();
				}
				catch (Exception ex)
				{
					throw new EntityParseException("Failed to parse Entity using SAX ",
							ex);
				}
			}
			else
			{
				try
				{
					// read the xml using DOM.
					Document doc = StorageUtils.readDocumentFromString(xml);

					// verify the root element
					Element root = doc.getDocumentElement();
					if (!root.getTagName().equals(containerEntryTagName))
					{
						log.warn("readContainer(): not = " + containerEntryTagName
								+ " : " + root.getTagName());
						return null;
					}

					// re-create a resource
					// Originally this code just assumed that it was dealing with a DoubleStorage.
					// When the only place it was used was with SingleStorage in which containers don't make
					// sense.
					Entity entry = null;
					if (storageUser instanceof DoubleStorageUser) {
						entry = ((DoubleStorageUser)storageUser).newContainer(root);
					} else {
						entry = storageUser.newResource(null, root);
					}
					return entry;
				}
				catch (Exception ex)
				{
					throw new EntityParseException("Failed to parse Entity using DOM ",
							ex);
				}
			}
		}
	}


	/**
	 * @throws EntityParseException 
	 * @see org.sakaiproject.entity.api.serialize.EntityReader#parseResource(org.sakaiproject.entity.api.Entity,
	 *      java.lang.String)
	 */
	public Entity parse(Entity container, String xml, byte[] blob) throws EntityParseException
	{
		if ( target.accept(blob)) {
			log.debug("Parsing Blob "+target);
			return target.parse(container,xml,blob);			
		} 
		else
		{
			if (saxEntityReader != null)
			{
				try
				{
					log.debug("Parsing With SAX using "+saxEntityReader);
					DefaultEntityHandler deh = saxEntityReader.getDefaultHandler(saxEntityReader.getServices());
					deh.setContainer(container);
					StorageUtils.processString(xml, deh);
					return deh.getEntity();
				}
				catch (Exception ex)
				{
					throw new EntityParseException("Failed to parse Entity using SAX ",
							ex);
				}
			}
			else
			{
				try
				{
					// read the xml
					Document doc = StorageUtils.readDocumentFromString(xml);

					// verify the root element
					Element root = doc.getDocumentElement();
					if (!root.getTagName().equals(resourceEntryTagName))
					{
						log.warn("readResource(): not = " + resourceEntryTagName + " : "
								+ root.getTagName());
						return null;
					}

					// re-create a resource
					Entity entry = storageUser.newResource(container, root);

					return entry;
				}
				catch (Exception ex)
				{
					throw new EntityParseException("Failed to parse Entity using DOM ",
							ex);
				}
			}
		}
	}
	/**
	 * @return the containerEntryTagName
	 */
	public String getContainerEntryTagName()
	{
		return containerEntryTagName;
	}
	/**
	 * @param containerEntryTagName the containerEntryTagName to set
	 */
	public void setContainerEntryTagName(String containerEntryTagName)
	{
		this.containerEntryTagName = containerEntryTagName;
	}
	/**
	 * @return the resourceEntryTagName
	 */
	public String getResourceEntryTagName()
	{
		return resourceEntryTagName;
	}
	/**
	 * @param resourceEntryTagName the resourceEntryTagName to set
	 */
	public void setResourceEntryTagName(String resourceEntryTagName)
	{
		this.resourceEntryTagName = resourceEntryTagName;
	}
	/**
	 * @return the storageUser
	 */
	public SingleStorageUser getStorageUser()
	{
		return storageUser;
	}
	/**
	 * @param storageUser the storageUser to set
	 */
	public void setStorageUser(SingleStorageUser storageUser)
	{
		this.storageUser = storageUser;
	}
	/**
	 * We want to always serialise as the target
	 * @throws EntityParseException 
	 * @see org.sakaiproject.entity.api.serialize.EntityReader#toString(org.sakaiproject.entity.api.Entity)
	 */
	public byte[] serialize(Entity entry) throws EntityParseException
	{
		return target.serialize(entry);
	}
	/** 
	 * Either this or the target will parse whatever is sent to it
	 * @see org.sakaiproject.entity.api.serialize.EntityReader#accept(java.lang.String)
	 */
	public boolean accept(byte[] blob)
	{
		return true;
	}
	/**
	 * @return the saxEntityReader
	 */
	public SAXEntityReader getSaxEntityReader()
	{
		return saxEntityReader;
	}
	/**
	 * @param saxEntityReader the saxEntityReader to set
	 */
	public void setSaxEntityReader(SAXEntityReader saxEntityReader)
	{
		this.saxEntityReader = saxEntityReader;
	}
	/**
	 * @return the target
	 */
	public EntityReaderHandler getTarget()
	{
		return target;
	}
	/**
	 * @param target the target to set
	 */
	public void setTarget(EntityReaderHandler target)
	{
		this.target = target;
	}
}
