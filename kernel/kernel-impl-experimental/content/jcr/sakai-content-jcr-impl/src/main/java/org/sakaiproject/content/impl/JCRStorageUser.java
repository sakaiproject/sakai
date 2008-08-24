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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.impl;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.GroupAwareEntity.AccessMode;
import org.sakaiproject.content.impl.BaseContentService.BaseCollectionEdit;
import org.sakaiproject.content.impl.BaseContentService.BaseResourceEdit;
import org.sakaiproject.content.impl.BaseContentService.BasicGroupAwareEdit;
import org.sakaiproject.content.impl.jcr.SakaiConstants;
import org.sakaiproject.content.impl.util.GMTDateformatter;
import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.jcr.api.DAVConstants;
import org.sakaiproject.jcr.api.JCRConstants;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;

/**
 * @author ieb
 */
public class JCRStorageUser implements LiteStorageUser
{
	/**
	 * Performs custom converion on a specific area of the metadata
	 * 
	 * @author ieb
	 */
	public interface CustomConverter
	{
		/**
		 * Convert from the Entity to the JCR space
		 * 
		 * @param edit
		 *        the entity to convert
		 * @param rp
		 *        name resource properties on the entity
		 * @param n
		 *        the destination node
		 * @throws RepositoryException
		 */
		void convert(Edit edit, ResourceProperties rp, Node n) throws RepositoryException;

		/**
		 * Convert from the JCR space to the entity space
		 * 
		 * @param n
		 *        the source node
		 * @param edit
		 *        the destination edit
		 * @param rp
		 *        the destincation resource properties
		 * @throws RepositoryException
		 */
		void convert(Node n, Edit edit, ResourceProperties rp) throws RepositoryException;

	}

	/**
	 * A generic type converter that converts named properties
	 * 
	 * @author ieb
	 */
	public interface GenericConverter
	{

		/**
		 * Convert from entity to JCR
		 * 
		 * @param edit
		 *        the source entity
		 * @param rp
		 *        the source properties
		 * @param name
		 *        the source name
		 * @param n
		 *        the destination node
		 * @param jname
		 *        the destincation name
		 */
		void  copy(Edit edit, ResourceProperties rp, String name, Node n, String jname);

		/**
		 * Convert from JCR property to entity
		 * 
		 * @param p
		 *        source JCR property
		 * @param jname
		 *        source JCR name
		 * @param rp
		 *        destincation resource property
		 * @param ename
		 *        dstination name of hte entity property
		 * @throws RepositoryException
		 */
		void copy(Property p, String jname, ResourceProperties rp, String ename)
				throws RepositoryException;

	}

	/**
	 * Does not perform any convertion
	 * 
	 * @author ieb
	 */
	public class NullConverter implements GenericConverter
	{

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.content.impl.JCRStorageUser.GenericConverter#copy(org.sakaiproject.entity.api.Edit,
		 *      org.sakaiproject.entity.api.ResourceProperties,
		 *      java.lang.String, javax.jcr.Node, java.lang.String)
		 */
		public void copy(Edit edit, ResourceProperties rp, String name, Node n,
				String jname)
		{
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.content.impl.JCRStorageUser.GenericConverter#copy(javax.jcr.Property,
		 *      java.lang.String,
		 *      org.sakaiproject.entity.api.ResourceProperties,
		 *      java.lang.String)
		 */
		public void copy(Property p, String jname, ResourceProperties rp, String ename)
				throws RepositoryException
		{
		}

	}

	/**
	 * Converts all Display name oriented attributes
	 * 
	 * @author ieb
	 */
	public class DisplayNameConverter implements CustomConverter
	{

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.content.impl.JCRStorageUser.Converter#convert(org.sakaiproject.entity.api.Edit,
		 *      org.sakaiproject.entity.api.ResourceProperties, javax.jcr.Node)
		 */
		public void convert(Edit edit, ResourceProperties rp, Node n)
		{
			String displayName = rp.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
			if (displayName != null && displayName.trim().length() > 0)
			{
				setJCRProperty(
						convertEntityName2JCRName(ResourceProperties.PROP_DISPLAY_NAME),
						displayName, n);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.content.impl.JCRStorageUser.Converter#convert(javax.jcr.Node,
		 *      org.sakaiproject.entity.api.Edit,
		 *      org.sakaiproject.entity.api.ResourceProperties)
		 */
		public void convert(Node n, Edit edit, ResourceProperties rp)
				throws RepositoryException
		{
			BasicGroupAwareEdit bedit = (BasicGroupAwareEdit) edit;

			String jname = convertEntityName2JCRName(ResourceProperties.PROP_DISPLAY_NAME);
			if (n.hasProperty(jname))
			{
				Property p = n.getProperty(jname);
				if (p != null)
				{
					bedit.m_properties.addProperty(ResourceProperties.PROP_DISPLAY_NAME,
							p.getString());
				}
				else
				{
					bedit.m_properties.addProperty(ResourceProperties.PROP_DISPLAY_NAME,
							n.getName());
				}
			}
			else
			{
				bedit.m_properties.addProperty(ResourceProperties.PROP_DISPLAY_NAME, n
						.getName());
			}
		}

	}

	/**
	 * Converts all Content Attributes, must be the first in the list
	 * 
	 * @author ieb
	 */
	public class ContentConverter implements CustomConverter
	{

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.content.impl.JCRStorageUser.Converter#convert(org.sakaiproject.entity.api.Edit,
		 *      org.sakaiproject.entity.api.ResourceProperties, javax.jcr.Node)
		 */
		public void convert(Edit edit, ResourceProperties rp, Node n)
				throws RepositoryException
		{

			if (edit instanceof BaseResourceEdit)
			{

				BaseResourceEdit bedit = (BaseResourceEdit) edit;

				setJCRProperty(DAVConstants.DAV_GETCONTENTLENGTH, bedit
						.getContentLength(), n);
				setJCRProperty(DAVConstants.DAV_GETCONTENTTYPE, bedit.getContentType(), n);
								
				setJCRProperty(
						convertEntityName2JCRName(SakaiConstants.SAKAI_RESOURCE_TYPE),
						bedit.getResourceType(), n);;
				setJCRProperty(convertEntityName2JCRName(SakaiConstants.SAKAI_FILE_PATH),
						bedit.m_filePath, n);
				
				Node content = null;
				if (n.hasNode(JCRConstants.JCR_CONTENT))
				{						
					content = n.getNode(JCRConstants.JCR_CONTENT);
				}
				else
				{
					content = n.addNode(JCRConstants.JCR_CONTENT,
								JCRConstants.NT_RESOURCE);
				}
				if (bedit.m_body != null)
				{
					log.warn("Setting Content from Byte Array in Memory, size: "
							+ bedit.m_body.length);
					content.setProperty(JCRConstants.JCR_DATA, new ByteArrayInputStream(
							bedit.m_body));
				}
				content.setProperty(JCRConstants.JCR_MIMETYPE, bedit.getContentType());	
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.content.impl.JCRStorageUser.Converter#convert(javax.jcr.Node,
		 *      org.sakaiproject.entity.api.Edit,
		 *      org.sakaiproject.entity.api.ResourceProperties)
		 */
		public void convert(Node n, Edit edit, ResourceProperties rp)
				throws RepositoryException
		{

			if (edit instanceof BaseJCRCollectionEdit)
			{
				BaseJCRCollectionEdit bce = (BaseJCRCollectionEdit) edit;
				bce.setNode(n);
				rp.addProperty(ResourceProperties.PROP_IS_COLLECTION,
						"true");
				bce.setResourceType(ResourceType.TYPE_FOLDER);
			}
			else if (edit instanceof BaseJCRResourceEdit)
			{
				BaseJCRResourceEdit bre = (BaseJCRResourceEdit) edit;
				bre.setNode(n);
				rp.addProperty(ResourceProperties.PROP_IS_COLLECTION,
						"false");
				bre.setResourceType(ResourceType.TYPE_UPLOAD);
			}

			log.debug("Checking for BaseGroupAwareEdit ");
			if (edit instanceof BasicGroupAwareEdit)
			{
				if (log.isDebugEnabled())
				{
					log.debug("IS Checking for BaseGroupAwareEdit ");
				}
				BasicGroupAwareEdit bedit = (BasicGroupAwareEdit) edit;

				try
				{
					Property pcd = n.getProperty(JCRConstants.JCR_CREATED);
					String creationDate = GMTDateformatter.format(pcd.getDate().getTime());
					if (log.isDebugEnabled())
					{
						log.debug("Setting Creation date on " + bedit.m_id + " to "
								+ creationDate);
					}
					rp.addProperty(ResourceProperties.PROP_CREATION_DATE,
							creationDate);
					rp.addProperty(ResourceProperties.PROP_MODIFIED_DATE,
							creationDate);
				}
				catch (Exception ex)
				{
					log.error("Failed to set Creation date " + ex.getMessage());
				}

				try
				{
					if (n.hasNode(JCRConstants.JCR_CONTENT))
					{
						Node content = n.getNode(JCRConstants.JCR_CONTENT);
						Property pmd = content.getProperty(JCRConstants.JCR_LASTMODIFIED);
						String modifiedDate = GMTDateformatter.format(pmd.getDate().getTime());
						if (log.isDebugEnabled())
						{
							log.debug("Setting Modification date on " + bedit.m_id
									+ " to " + modifiedDate);
						}
						rp.addProperty(
								ResourceProperties.PROP_MODIFIED_DATE, modifiedDate);
					}

				}
				catch (Exception ex)
				{
					try
					{
						log.warn("Failed to set Modified date ", ex);
						log.info("Primary Node Type is " + n.getPrimaryNodeType());
						for (NodeType nt : n.getMixinNodeTypes())
						{
							log.info(" Mixing  " + nt);
						}
						for (PropertyIterator pi = n.getProperties(); pi.hasNext();)
						{
							Property p = pi.nextProperty();
							log.info(" Property [" + p.getName() + "][" + p.getString()
									+ "]");
						}
					}
					catch (Exception ex2)
					{
						log.info("Failed to debug ", ex2);
					}
				}
			}

			if (edit instanceof BaseResourceEdit)
			{

				BaseResourceEdit bedit = (BaseResourceEdit) edit;
				if (n.hasProperty(DAVConstants.DAV_GETCONTENTTYPE))
				{
					Property p = n.getProperty(DAVConstants.DAV_GETCONTENTTYPE);
					if (p != null)
					{
						bedit.m_contentType = p.getString();
					}
				}

				if (n.hasNode(JCRConstants.JCR_CONTENT))
				{
					Node content = n.getNode(JCRConstants.JCR_CONTENT);
					Property p = content.getProperty(JCRConstants.JCR_DATA);
					bedit.m_contentLength = (int) p.getLength();
					rp.addProperty(ResourceProperties.PROP_CONTENT_LENGTH, String.valueOf(bedit.m_contentLength));
					p = content.getProperty(JCRConstants.JCR_MIMETYPE);
					if ( p != null ) {
						bedit.m_contentType = p.getString();
						if ( log.isDebugEnabled() ) {
							log.debug("Content type set to "+bedit.m_contentType);
						}
					} else {
						bedit.m_contentType = "application/octet-stream";
						log.debug("Content default to "+bedit.m_contentType);
					}
				}
				else if (n.hasProperty(DAVConstants.DAV_GETCONTENTLENGTH))
				{
					Property p = n.getProperty(DAVConstants.DAV_GETCONTENTLENGTH);
					if (p != null)
					{
						bedit.m_contentLength = (int) p.getLong();
					}
					else
					{
						bedit.m_contentLength = 0;
					}
					rp.addProperty(ResourceProperties.PROP_CONTENT_LENGTH, String.valueOf(bedit.m_contentLength));
				}

				// could optimize this property away
				if (n
						.hasProperty(convertEntityName2JCRName(SakaiConstants.SAKAI_RESOURCE_TYPE)))
				{
					Property p = n
							.getProperty(convertEntityName2JCRName(SakaiConstants.SAKAI_RESOURCE_TYPE));
					if (p != null)
					{
						bedit.setResourceType(p.getString());
					}
				}

				if (n
						.hasProperty(convertEntityName2JCRName(SakaiConstants.SAKAI_FILE_PATH)))
				{
					Property p = n
							.getProperty(convertEntityName2JCRName(SakaiConstants.SAKAI_FILE_PATH));
					if (p != null)
					{
						bedit.m_filePath = StringUtil.trimToNull(p.getString());
					}

				}
			}
		}
	}

	/**
	 * Converts the hidden attribute
	 * 
	 * @author ieb
	 */
	public class HiddenConverter implements CustomConverter
	{

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.content.impl.JCRStorageUser.Converter#convert(org.sakaiproject.entity.api.Edit,
		 *      org.sakaiproject.entity.api.ResourceProperties, javax.jcr.Node)
		 */
		public void convert(Edit edit, ResourceProperties rp, Node n)
		{
			BasicGroupAwareEdit bedit = (BasicGroupAwareEdit) edit;
			setJCRProperty(convertEntityName2JCRName(SakaiConstants.SAKAI_HIDDEN),
					bedit.m_hidden, n);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.content.impl.JCRStorageUser.Converter#convert(javax.jcr.Node,
		 *      org.sakaiproject.entity.api.Edit,
		 *      org.sakaiproject.entity.api.ResourceProperties)
		 */
		public void convert(Node n, Edit edit, ResourceProperties rp)
				throws RepositoryException
		{
			BasicGroupAwareEdit bedit = (BasicGroupAwareEdit) edit;
			bedit.m_hidden = false;
			if (n.hasProperty(convertEntityName2JCRName(SakaiConstants.SAKAI_HIDDEN)))
			{
				Property p = n
						.getProperty(convertEntityName2JCRName(SakaiConstants.SAKAI_HIDDEN));
				if (p != null)
				{
					bedit.m_hidden = p.getBoolean();
				}
			}
		}

	}

	/**
	 * Converts release and restract dates
	 * 
	 * @author ieb
	 */
	public class ReleaseRetractDateConverter implements CustomConverter
	{

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.content.impl.JCRStorageUser.Converter#convert(org.sakaiproject.entity.api.Edit,
		 *      org.sakaiproject.entity.api.ResourceProperties, javax.jcr.Node)
		 */
		public void convert(Edit edit, ResourceProperties rp, Node n)
		{
			BasicGroupAwareEdit bedit = (BasicGroupAwareEdit) edit;
			if (bedit.m_hidden)
			{
				clearJCRProperty(
						convertEntityName2JCRName(SakaiConstants.SAKAI_RELEASE_DATE), n);
				clearJCRProperty(
						convertEntityName2JCRName(SakaiConstants.SAKAI_RETRACT_DATE), n);
			}
			else
			{

				if (bedit.m_releaseDate != null)
				{
					setJCRProperty(
							convertEntityName2JCRName(SakaiConstants.SAKAI_RELEASE_DATE),
							new Date(bedit.m_releaseDate.getTime()), n);
				}
				if (bedit.m_retractDate != null)
				{
					setJCRProperty(
							convertEntityName2JCRName(SakaiConstants.SAKAI_RETRACT_DATE),
							new Date(bedit.m_retractDate.getTime()), n);
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.content.impl.JCRStorageUser.Converter#convert(javax.jcr.Node,
		 *      org.sakaiproject.entity.api.Edit,
		 *      org.sakaiproject.entity.api.ResourceProperties)
		 */
		public void convert(Node n, Edit edit, ResourceProperties rp)
				throws RepositoryException
		{
			BasicGroupAwareEdit bedit = (BasicGroupAwareEdit) edit;
			if (bedit.m_hidden)
			{
				bedit.m_releaseDate = null;
				bedit.m_retractDate = null;
			}
			else
			{
				if (n
						.hasProperty(convertEntityName2JCRName(SakaiConstants.SAKAI_RELEASE_DATE)))
				{
					Property p = n
							.getProperty(convertEntityName2JCRName(SakaiConstants.SAKAI_RELEASE_DATE));
					if (p != null)
					{
						bedit.m_releaseDate = TimeService.newTime(p.getDate()
								.getTimeInMillis());
					}
				}
				if (n
						.hasProperty(convertEntityName2JCRName(SakaiConstants.SAKAI_RETRACT_DATE)))
				{
					Property p = n
							.getProperty(convertEntityName2JCRName(SakaiConstants.SAKAI_RETRACT_DATE));
					if (p != null)
					{
						bedit.m_retractDate = TimeService.newTime(p.getDate()
								.getTimeInMillis());
					}
				}
			}
		}

	}

	/**
	 * Converts access mode
	 * 
	 * @author ieb
	 */
	public class AccessModeConverter implements CustomConverter
	{

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.content.impl.JCRStorageUser.Converter#convert(org.sakaiproject.entity.api.Edit,
		 *      org.sakaiproject.entity.api.ResourceProperties, javax.jcr.Node)
		 */
		public void convert(Edit edit, ResourceProperties rp, Node n)
		{
			BasicGroupAwareEdit bedit = (BasicGroupAwareEdit) edit;
			if (bedit.m_access == null)
			{
				setJCRProperty(
						convertEntityName2JCRName(SakaiConstants.SAKAI_ACCESS_MODE),
						AccessMode.INHERITED.toString(), n);
			}
			else
			{
				setJCRProperty(
						convertEntityName2JCRName(SakaiConstants.SAKAI_ACCESS_MODE),
						bedit.m_access.toString(), n);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.content.impl.JCRStorageUser.Converter#convert(javax.jcr.Node,
		 *      org.sakaiproject.entity.api.Edit,
		 *      org.sakaiproject.entity.api.ResourceProperties)
		 */
		public void convert(Node n, Edit edit, ResourceProperties rp)
				throws RepositoryException
		{
			BasicGroupAwareEdit bedit = (BasicGroupAwareEdit) edit;
			bedit.m_access = AccessMode.INHERITED;
			if (n
					.hasProperty(convertEntityName2JCRName(SakaiConstants.SAKAI_ACCESS_MODE)))
			{
				Property p = n
						.getProperty(convertEntityName2JCRName(SakaiConstants.SAKAI_ACCESS_MODE));
				if (p != null)
				{
					bedit.m_access = AccessMode.fromString(p.getString());
				}
				else
				{
					log.error("Access Mode Property Null ");
				}
			}
		}

	}

	/**
	 * Converts group lists
	 * 
	 * @author ieb
	 */
	public class GroupListConverter implements CustomConverter
	{

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.content.impl.JCRStorageUser.Converter#convert(org.sakaiproject.entity.api.Edit,
		 *      org.sakaiproject.entity.api.ResourceProperties, javax.jcr.Node)
		 */
		public void convert(Edit edit, ResourceProperties rp, Node n)
		{
			BasicGroupAwareEdit bedit = (BasicGroupAwareEdit) edit;
			setJCRProperty(convertEntityName2JCRName(SakaiConstants.SAKAI_GROUP_LIST),
					new ArrayList(bedit.m_groups), n);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.content.impl.JCRStorageUser.Converter#convert(javax.jcr.Node,
		 *      org.sakaiproject.entity.api.Edit,
		 *      org.sakaiproject.entity.api.ResourceProperties)
		 */
		public void convert(Node n, Edit edit, ResourceProperties rp)
				throws RepositoryException
		{
			BasicGroupAwareEdit bedit = (BasicGroupAwareEdit) edit;
			bedit.m_groups = new ArrayList<String>();
			if (n.hasProperty(convertEntityName2JCRName(SakaiConstants.SAKAI_GROUP_LIST)))
			{
				Property p = n
						.getProperty(convertEntityName2JCRName(SakaiConstants.SAKAI_GROUP_LIST));
				if (p != null)
				{
					Value[] v = p.getValues();
					if (v != null)
					{
						for (int i = 0; i < v.length; i++)
						{
							bedit.m_groups.add(v[i].getString());
						}
					}
				}
			}
		}

	}

	/**
	 * A generic named property converter that uses simple property types and
	 * name mappings to perform the conversion
	 * 
	 * @author ieb
	 */
	public class DefaultConverter implements GenericConverter
	{

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.content.impl.JCRStorageUser.Converter#copy(org.sakaiproject.entity.api.Edit,
		 *      org.sakaiproject.entity.api.ResourceProperties,
		 *      java.lang.String, javax.jcr.Node, java.lang.String)
		 */
		public void copy(Edit edit, ResourceProperties rp, String name, Node n,
				String jname)
		{
			Object v = rp.get(name);
			if (v instanceof String)
			{
				setJCRProperty(jname, (String) v, n);
			}
			else if (v instanceof List)
			{
				setJCRProperty(jname, (List) v, n);
			}
			else
			{
				setJCRProperty(jname, String.valueOf(v), n);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.content.impl.JCRStorageUser.Converter#copy(javax.jcr.Property,
		 *      java.lang.String,
		 *      org.sakaiproject.entity.api.ResourceProperties,
		 *      java.lang.String)
		 */
		public void copy(Property p, String jname, ResourceProperties rp, String ename)
				throws RepositoryException
		{
			setEntityProperty(p, rp);
		}

	}

	private static final Log log = LogFactory.getLog(JCRStorageUser.class);

	private static final String IGNORE_PROPERTY = "ignore";

	private static final String REPOSITORY_PREFIX = "/content";

	private BaseContentService baseContentService;

	private Map<String, String> jcrTypes = new HashMap<String, String>();

	private Map<String, String> jcrToEntity = new HashMap<String, String>();

	private Map<String, String> entityToJcr = new HashMap<String, String>();

	private String repoPrefix;

	private String jcrWorkspace = "/sakai";

	private List<String> createNodes = new ArrayList<String>();

	private ConcurrentHashMap<String, PropertyDefinition> ntCache = new ConcurrentHashMap<String, PropertyDefinition>();

	private GenericConverter defaultConverter = new DefaultConverter();

	private Map<String, GenericConverter> resourceConverterMap = new HashMap<String, GenericConverter>();;

	private Map<String, GenericConverter> collectionConverterMap = new HashMap<String, GenericConverter>();

	private List<CustomConverter> resourceConverterList = new ArrayList<CustomConverter>();

	private List<CustomConverter> collectionConverterList = new ArrayList<CustomConverter>();

	private Map<String, String> namespaces;

	public JCRStorageUser()
	{
	}

	public void init()
	{
		repoPrefix = jcrWorkspace + REPOSITORY_PREFIX;

		CustomConverter glconverter = new GroupListConverter();
		CustomConverter amconverter = new AccessModeConverter();
		CustomConverter releaseRetractDateConverter = new ReleaseRetractDateConverter();
		CustomConverter hiddenConverter = new HiddenConverter();
		CustomConverter contentConverter = new ContentConverter();
		CustomConverter displayNameConverter = new DisplayNameConverter();
		GenericConverter nullConverter = new NullConverter();
		collectionConverterMap.put(SakaiConstants.SAKAI_GROUP_LIST, nullConverter);
		collectionConverterMap.put(SakaiConstants.SAKAI_ACCESS_MODE, nullConverter);
		collectionConverterMap.put(SakaiConstants.SAKAI_RELEASE_DATE, nullConverter);
		collectionConverterMap.put(SakaiConstants.SAKAI_RETRACT_DATE, nullConverter);
		collectionConverterMap.put(SakaiConstants.SAKAI_HIDDEN, nullConverter);
		collectionConverterMap.put(ResourceProperties.PROP_DISPLAY_NAME, nullConverter);
		collectionConverterMap.put(ResourceProperties.PROP_CREATION_DATE, nullConverter);
		collectionConverterMap.put(ResourceProperties.PROP_MODIFIED_DATE, nullConverter);

		collectionConverterList.add(contentConverter);
		collectionConverterList.add(glconverter);
		collectionConverterList.add(amconverter);
		collectionConverterList.add(releaseRetractDateConverter);
		collectionConverterList.add(hiddenConverter);
		collectionConverterList.add(displayNameConverter);

		resourceConverterMap.put(SakaiConstants.SAKAI_GROUP_LIST, nullConverter);
		resourceConverterMap.put(SakaiConstants.SAKAI_ACCESS_MODE, nullConverter);
		resourceConverterMap.put(SakaiConstants.SAKAI_RELEASE_DATE, nullConverter);
		resourceConverterMap.put(SakaiConstants.SAKAI_RETRACT_DATE, nullConverter);
		resourceConverterMap.put(SakaiConstants.SAKAI_HIDDEN, nullConverter);
		resourceConverterMap.put(SakaiConstants.DAV_CONTENT_LENGTH, nullConverter);
		resourceConverterMap.put(SakaiConstants.DAV_CONTENT_TYPE, nullConverter);
		resourceConverterMap.put(ResourceProperties.PROP_CONTENT_TYPE,nullConverter);
		resourceConverterMap.put(SakaiConstants.SAKAI_RESOURCE_TYPE, nullConverter);
		resourceConverterMap.put(SakaiConstants.SAKAI_FILE_PATH, nullConverter);
		resourceConverterMap.put(ResourceProperties.PROP_DISPLAY_NAME, nullConverter);
		resourceConverterMap.put(ResourceProperties.PROP_CREATION_DATE, nullConverter);
		resourceConverterMap.put(ResourceProperties.PROP_MODIFIED_DATE, nullConverter);
		resourceConverterMap.put(ResourceProperties.PROP_CONTENT_LENGTH, nullConverter);

		resourceConverterList.add(contentConverter);
		resourceConverterList.add(glconverter);
		resourceConverterList.add(amconverter);
		resourceConverterList.add(releaseRetractDateConverter);
		resourceConverterList.add(hiddenConverter);
		resourceConverterList.add(displayNameConverter);

		// ignore these properties, they are protected
		jcrTypes.put(JCRConstants.JCR_MIXINTYPES, IGNORE_PROPERTY);
		jcrTypes.put(JCRConstants.JCR_LOCKISDEEP, IGNORE_PROPERTY);
		jcrTypes.put(JCRConstants.JCR_PRIMARYTYPE, IGNORE_PROPERTY);
		jcrTypes.put(JCRConstants.JCR_UUID, IGNORE_PROPERTY);
		jcrTypes.put(JCRConstants.JCR_UUID, IGNORE_PROPERTY);
		jcrTypes.put(JCRConstants.JCR_PREDECESSORS, IGNORE_PROPERTY);

		// these properties are dates
		jcrTypes.put(SakaiConstants.SAKAI_RELEASE_DATE, PropertyType.TYPENAME_DATE);
		jcrTypes.put(SakaiConstants.SAKAI_RETRACT_DATE, PropertyType.TYPENAME_DATE);

	}

	public void destroy()
	{

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.LiteStorageUser#commit(org.sakaiproject.entity.api.Edit,
	 *      javax.jcr.Node)
	 */
	public void commit(Edit edit, Object o)
	{
		if (o instanceof Node)
		{
			Node n = (Node) o;
			try
			{
				copy(edit, n);
				n.save();
			}
			catch (RepositoryException e)
			{
				log.error("Failed to save node to JCR ", e);
			}
		}
	}

	/**
	 * @param edit
	 * @param n
	 * @throws RepositoryException
	 */
	public void copy(Edit edit, Object o) throws RepositoryException
	{
		if ((o instanceof Node)
				&& ((edit instanceof BaseCollectionEdit) || (edit instanceof BaseResourceEdit)))
		{

			Map<String, GenericConverter> cmap = resourceConverterMap;
			List<CustomConverter> clist = resourceConverterList;
			if (edit instanceof BaseCollectionEdit)
			{
				cmap = collectionConverterMap;
				clist = collectionConverterList;
			}
			Node n = (Node) o;
			ResourceProperties rp = edit.getProperties();
			for (CustomConverter c : clist)
			{
				c.convert(edit, rp, n);
			}
			for (Iterator i = rp.getPropertyNames(); i.hasNext();)
			{
				String name = (String) i.next();
				GenericConverter c = cmap.get(name);
				String jname = convertEntityName2JCRName(name);
				if (c != null)
				{
					c.copy(edit, rp, name, n, jname);
				}
				else
				{
					defaultConverter.copy(edit, rp, name, n, jname);
				}
			}

		}
	}

	/**
	 * @param n
	 * @param e
	 * @throws RepositoryException
	 */
	public void copy(Node n, Entity e) throws RepositoryException
	{

		// copy from the node to the entity,
		// there may be some items in the Node properties that we do not want to
		// copy
		if (e instanceof Edit)
		{
			if (log.isDebugEnabled())
			{
				log.debug(" Instance of Edit " + e);
			}
			Edit edit = (Edit) e;
			Map<String, GenericConverter> cmap = resourceConverterMap;
			List<CustomConverter> clist = resourceConverterList;
			if (e instanceof BaseJCRCollectionEdit)
			{
				cmap = collectionConverterMap;
				clist = collectionConverterList;
			}

			ResourceProperties rp = e.getProperties();
			for (CustomConverter c : clist)
			{
				if (log.isDebugEnabled())
				{
					log.debug("Converter Calling " + c);
				}
				c.convert(n, edit, rp);
			}
			for (PropertyIterator pi = n.getProperties(); pi.hasNext();)
			{
				Property p = pi.nextProperty();
				String jname = p.getName();
				String ename = convertJCRName2EntityName(jname);
				GenericConverter converter = cmap.get(ename);
				if (converter != null)
				{
					if (log.isDebugEnabled())
					{
						log.debug("Converter Calling " + converter + " for " + jname);
					}
					converter.copy(p, jname, rp, ename);
				}
				else
				{
					if (log.isDebugEnabled())
					{
						log.debug("Converter Calling Default " + jname);
					}
					defaultConverter.copy(p, jname, rp, ename);
				}
			}
		}
		else
		{
			if (log.isDebugEnabled())
			{
				log.debug("Not an Instance of Edit " + e);
			}
		}
	}

	/**
	 * @param string
	 * @param n
	 * @throws RepositoryException
	 * @throws
	 */
	private void clearJCRProperty(String jname, Node n)
	{

		try
		{
			if (n.hasProperty(jname))
			{
				Property p = n.getProperty(jname);
				if (p != null)
				{
					p.setValue((Value) null);
				}
			}
		}
		catch (RepositoryException re)
		{
			log.error("Failed to clear property ");
		}
	}

	/**
	 * @param jname
	 * @param list
	 * @param n
	 */
	private void setJCRProperty(String jname, List list, Node n)
	{
		try
		{
			String stype = jcrTypes.get(jname);
			if (IGNORE_PROPERTY.equals(stype))
			{
				return;
			}
			int type = PropertyType.STRING;
			if (stype != null)
			{
				type = PropertyType.valueFromName(stype);
			}
			Session s = n.getSession();
			ValueFactory vf = s.getValueFactory();
			switch (type)
			{
				case PropertyType.BINARY:
					throw new UnsupportedOperationException(
							"Cant set a binary list at the moment");
				case PropertyType.BOOLEAN:
				{
					Value[] sv = new Value[list.size()];
					for (int i = 0; i < sv.length; i++)
					{
						Object ov = list.get(i);
						if (ov instanceof Boolean)
						{
							sv[i] = vf.createValue(((Boolean) ov).booleanValue());
						}
						else
						{
							sv[i] = vf.createValue(new Boolean(String
									.valueOf(list.get(i))));
						}
					}

					try
					{
						n.setProperty(jname, sv);
					}
					catch (RepositoryException e)
					{
						log.error("Failed to set " + jname + " to " + list + " cause:"
								+ e.getMessage());
					}
					break;
				}
				case PropertyType.DATE:
				{
					Value[] sv = new Value[list.size()];
					for (int i = 0; i < sv.length; i++)
					{
						Object ov = list.get(i);
						if (ov instanceof Calendar)
						{
							sv[i] = vf.createValue((Calendar) ov);
						}
						else if (ov instanceof Date)
						{
							GregorianCalendar gc = new GregorianCalendar();
							gc.setTime((Date) ov);
							sv[i] = vf.createValue(gc);
						}
						else
						{
							GregorianCalendar gc = GMTDateformatter.parseGregorian(String.valueOf(ov));
							sv[i] = vf.createValue(gc);
						}
					}
					try
					{
						n.setProperty(jname, sv);
					}
					catch (RepositoryException e)
					{
						log.error("Failed to set " + jname + " to " + list + " cause:"
								+ e.getMessage());
					}
					break;
				}
				case PropertyType.DOUBLE:
				{
					Value[] sv = new Value[list.size()];
					for (int i = 0; i < sv.length; i++)
					{
						Object ov = list.get(i);
						if (ov instanceof Double)
						{
							sv[i] = vf.createValue(((Double) ov).doubleValue());
						}
						else
						{
							sv[i] = vf.createValue(Double.parseDouble(String.valueOf(list
									.get(i))));
						}
					}
					try
					{
						n.setProperty(jname, sv);
					}
					catch (RepositoryException e)
					{
						log.error("Failed to set " + jname + " to " + list + " cause:"
								+ e.getMessage());
					}
					break;
				}
				case PropertyType.LONG:
				{
					Value[] sv = new Value[list.size()];
					for (int i = 0; i < sv.length; i++)
					{
						sv[i] = vf.createValue(Long
								.parseLong(String.valueOf(list.get(i))));
					}
					try
					{
						n.setProperty(jname, sv);
					}
					catch (RepositoryException e)
					{
						log.error("Failed to set " + jname + " to " + list + " cause:"
								+ e.getMessage());
					}
					break;
				}
				case PropertyType.UNDEFINED:
				case PropertyType.STRING:
				case PropertyType.REFERENCE:
				case PropertyType.NAME:
				case PropertyType.PATH:
				{
					Value[] sv = new Value[list.size()];
					for (int i = 0; i < sv.length; i++)
					{
						sv[i] = vf.createValue(String.valueOf(list.get(i)));
					}
					try
					{
						n.setProperty(jname, sv);
					}
					catch (RepositoryException e)
					{
						log.error("Failed to set " + jname + " to " + list + " cause:"
								+ e.getMessage());
					}
					break;
				}
			}
		}
		catch (RepositoryException e)
		{
			log.error("Failed to set propert " + jname + " to " + list, e);
		}
	}

	/**
	 * @param jname
	 * @param string
	 * @param n
	 */
	private void setJCRProperty(String jname, Object ov, Node n)
	{
		try
		{
			if (isProtected(n, jname))
			{
				if (log.isDebugEnabled())
				{
					log.debug(jname + " is protected ignoring ");
				}
				return;
			}
			String stype = jcrTypes.get(jname);
			if (IGNORE_PROPERTY.equals(stype))
			{
				return;
			}
			int type = PropertyType.STRING;
			if (stype != null)
			{
				type = PropertyType.valueFromName(stype);
			}
			Session s = n.getSession();
			ValueFactory vf = s.getValueFactory();
			switch (type)
			{
				case PropertyType.BINARY:
					throw new UnsupportedOperationException(
							"Cant set a binary list at the moment");
				case PropertyType.BOOLEAN:
				{
					try
					{
						if (ov instanceof Boolean)
						{
							n.setProperty(jname, ((Boolean) ov).booleanValue());
						}
						else
						{
							n.setProperty(jname, new Boolean(String.valueOf(ov)));
						}
					}
					catch (RepositoryException e)
					{
						log.error("Failed to set " + jname + " to " + ov + " cause:"
								+ e.getMessage());
					}
					break;
				}
				case PropertyType.DATE:
				{
					try
					{
						if (ov instanceof Calendar)
						{
							n.setProperty(jname, (Calendar) ov);
						}
						else if (ov instanceof Date)
						{
							GregorianCalendar gc = new GregorianCalendar();
							gc.setTime((Date) ov);
							n.setProperty(jname, gc);
						}
						else
						{
							GregorianCalendar gc = GMTDateformatter.parseGregorian(String.valueOf(ov));
							n.setProperty(jname, gc);
						}
					}
					catch (RepositoryException e)
					{
						log.error("Failed to set " + jname + " to " + ov + " cause:"
								+ e.getMessage());
					}
					break;
				}
				case PropertyType.DOUBLE:
				{
					try
					{
						if (ov instanceof Double)
						{
							n.setProperty(jname, ((Double) ov).doubleValue());
						}
						else
						{
							n.setProperty(jname, Double.parseDouble(String.valueOf(ov)));
						}
					}
					catch (RepositoryException e)
					{
						log.error("Failed to set " + jname + " to " + ov + " cause:"
								+ e.getMessage());
					}
					break;
				}
				case PropertyType.LONG:
				{
					try
					{
						if (ov instanceof Long)
						{
							n.setProperty(jname, ((Long) ov).longValue());
						}
						else if (ov instanceof Integer)
						{
							n.setProperty(jname, ((Integer) ov).longValue());
						}
						else
						{
							n.setProperty(jname, Long.parseLong(String.valueOf(ov)));
						}
					}
					catch (RepositoryException e)
					{
						log.error("Failed to set " + jname + " to " + ov + " cause:"
								+ e.getMessage());
					}
					break;
				}
				case PropertyType.UNDEFINED:
				case PropertyType.STRING:
				case PropertyType.REFERENCE:
				case PropertyType.NAME:
				case PropertyType.PATH:
				{
					try
					{
						n.setProperty(jname, String.valueOf(ov));
					}
					catch (RepositoryException e)
					{
						log.error("Failed to set " + jname + " to " + ov + " cause: "
								+ e.getMessage());
					}
					break;
				}
			}
		}
		catch (RepositoryException e)
		{
			log.error("Failed to set propert " + jname + " to " + ov, e);
		}

	}

	/**
	 * @param n
	 * @param jname
	 * @return
	 * @throws RepositoryException
	 */
	private boolean isProtected(Node n, String jname) throws RepositoryException
	{
		PropertyDefinition pd = getDefinition(n, jname);
		if (pd == null)
		{
			return false;
		}
		return pd.isProtected();
	}

	/**
	 * @param n
	 * @param jname
	 * @throws RepositoryException
	 */
	private PropertyDefinition getDefinition(Node n, String jname)
			throws RepositoryException
	{
		NodeType pnt = n.getPrimaryNodeType();
		String name = pnt.getName() + ":" + jname;
		PropertyDefinition opd = ntCache.get(name);
		if (opd == null)
		{
			{

				for (PropertyDefinition pd : pnt.getPropertyDefinitions())
				{
					ntCache.put(pnt.getName() + ":" + pd.getName(), pd);
				}
			}
			for (NodeType nt : n.getMixinNodeTypes())
			{
				for (PropertyDefinition pd : nt.getPropertyDefinitions())
				{
					ntCache.put(pnt.getName() + ":" + pd.getName(), pd);
				}
			}
			opd = ntCache.get(name);
		}
		return opd;
	}

	/**
	 * @param p
	 * @param rp
	 * @throws RepositoryException
	 * @throws
	 */
	private void setEntityProperty(Property p, ResourceProperties rp)
			throws RepositoryException
	{
		if (IGNORE_PROPERTY.equals(jcrTypes.get(p.getName())))
		{
			return;
		}
		// log.info("Converting " + p.getName());

		PropertyDefinition pd = p.getDefinition();
		if (pd.isMultiple())
		{
			String ename = convertJCRName2EntityName(p.getName());
			for (Value v : p.getValues())
			{
				rp.addPropertyToList(ename, v.getString());
			}
		}
		else
		{
			rp.addProperty(convertJCRName2EntityName(p.getName()), p.getString());
		}
	}

	/**
	 * @param name
	 * @return
	 */
	private String convertJCRName2EntityName(String name)
	{
		String entityName = jcrToEntity.get(name);
		if (entityName == null)
		{
			String[] parts = name.split(":", 2);
			if (parts.length == 2)
			{
				String uri = namespaces.get(parts[0]);
				if (uri != null)
				{
					return uri + parts[1];
				}
			}
			return name;
		}
		return entityName;
	}

	/**
	 * @param name
	 * @return
	 * @throws RepositoryException
	 */
	private String convertEntityName2JCRName(String name)
	{
		String jcrName = entityToJcr.get(name);
		if (jcrName == null)
		{
			for (String prefix : namespaces.keySet())
			{
				String uri = namespaces.get(prefix);
				if (name.startsWith(uri))
				{
					return prefix + ":" + name.substring(uri.length());
				}
			}

			return name;
		}
		return jcrName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.LiteStorageUser#convertId2Storage(java.lang.String)
	 */
	public String convertId2Storage(String id)
	{
		String jcrPath = repoPrefix + id;
		if (jcrPath.endsWith("/"))
		{
			jcrPath = jcrPath.substring(0, jcrPath.length() - 1);
		}
		// log.info(" Id2JCR [" + id + "] >> [" + jcrPath + "]");
		return jcrPath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.LiteStorageUser#convertStorage2Id(java.lang.String)
	 */
	public String convertStorage2Id(String path)
	{
		String id = path;
		if (path.startsWith(repoPrefix))
		{
			id = path.substring(repoPrefix.length());
		}
		else
		{
			log
					.error("Trying to convert a path to Id that is not a storage path "
							+ path);
		}
		if (id == null || id.length() == 0)
		{
			id = "/";
		}
		// log.info(" JCR2Id [" + path + "] >> [" + id + "]");
		return id;
	}

	/**
	 * @param path
	 * @return
	 */
	private String xconvertStorage2Ref(String path)
	{
		String id = convertStorage2Id(path);
		return baseContentService.getReference(id);
	}

	/**
	 * @param ref
	 * @return
	 */
	private String convertRef2Id(String ref)
	{
		String baseRef = baseContentService.getReference("/");
		if (baseRef.endsWith("/"))
		{
			baseRef = baseRef.substring(0, baseRef.length() - 1);
		}
		if (log.isDebugEnabled())
		{
			log.debug("Base Reference is " + baseRef);
		}
		String id = ref;
		if (ref.startsWith(baseRef))
		{
			id = ref.substring(baseRef.length());
			log.error("Ref2Id ref[" + ref + "] >> id[" + id + "]");
		}
		else
		{
			log.error("Reference does not appear to be a CHS reference [" + ref
					+ "] should start with [" + baseRef + "]");
		}

		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.LiteStorageUser#newResource(java.lang.Object)
	 */
	public Entity newResource(Object source)
	{
		// create a new resource of the required type and
		// copy the source (which is a JCR Node) into the resource
		if (source instanceof Node)
		{
			Node n = (Node) source;
			try
			{
				NodeType nt = n.getPrimaryNodeType();
				if (JCRConstants.NT_FILE.equals(nt.getName()))
				{
					Entity e = newResource(null, convertStorage2Id(n.getPath()), null);
					if (log.isDebugEnabled())
					{
						log.debug("Loading File from " + n);
					}
					copy(n, e);
					return e;
				}
				else if (JCRConstants.NT_FOLDER.equals(nt.getName()))
				{
					Entity e = newContainerById(convertStorage2Id(n.getPath()));
					if (log.isDebugEnabled())
					{
						log.debug("Loading Colletion from " + n);
					}
					copy(n, e);
					return e;
				}
				else
				{
					log.error("Unable to determine node type " + nt.getName());
					return null;
				}
			}
			catch (RepositoryException e1)
			{
				log.error("Failed to create new resource", e1);
			}
		}
		log.error("Cant Create Resource from source " + source);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.LiteStorageUser#newResourceEdit(org.sakaiproject.entity.api.Entity)
	 */
	public Edit newResourceEdit(Object source)
	{
		// create a new resource of the required type and
		// copy the source (which is a JCR Node) into the resource
		if (source instanceof Node)
		{
			Node n = (Node) source;
			try
			{
				NodeType nt = n.getPrimaryNodeType();
				if (log.isDebugEnabled())
				{
					log.debug("Building resource from " + nt.getName());
				}
				if (JCRConstants.NT_FILE.equals(nt.getName()))
				{
					Edit e = newResourceEdit(null, convertStorage2Id(n.getPath()), null);
					copy(n, e);
					return e;
				}
				else if (JCRConstants.NT_FOLDER.equals(nt.getName()))
				{
					Edit e = newContainerEditById(convertStorage2Id(n.getPath()));
					copy(n, e);
					return e;
				}
				else
				{
					log.error("Cant create Resource Edit from a " + nt.getName());
					return null;
				}
			}
			catch (RepositoryException e1)
			{
				log.error("Failed to create new resource", e1);
			}
		}
		log.error("Unable to create JCR based resource from a source object " + source);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.util.StorageUser#getDate(org.sakaiproject.entity.api.Entity)
	 */
	public Time getDate(Entity r)
	{
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.util.StorageUser#getOwnerId(org.sakaiproject.entity.api.Entity)
	 */
	public String getOwnerId(Entity r)
	{
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.util.StorageUser#isDraft(org.sakaiproject.entity.api.Entity)
	 */
	public boolean isDraft(Entity r)
	{
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.util.StorageUser#newContainer(java.lang.String)
	 */
	public Entity newContainer(String ref)
	{
		String id = convertRef2Id(ref);
		if (!id.endsWith("/"))
		{
			id = id + "/";
		}
		return new BaseJCRCollectionEdit(baseContentService, id);
	}

	public Entity newContainerById(String id)
	{
		if (!id.endsWith("/"))
		{
			id = id + "/";
		}
		return new BaseJCRCollectionEdit(baseContentService, id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.util.StorageUser#newContainer(org.w3c.dom.Element)
	 */
	public Entity newContainer(Element element)
	{
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.util.StorageUser#newContainer(org.sakaiproject.entity.api.Entity)
	 */
	public Entity newContainer(Entity other)
	{
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.util.StorageUser#newContainerEdit(java.lang.String)
	 */
	public Edit newContainerEdit(String ref)
	{
		String id = convertRef2Id(ref);
		if (!id.endsWith("/"))
		{
			id = id + "/";
		}

		return new BaseJCRCollectionEdit(baseContentService, id);
	}

	public Edit newContainerEditById(String id)
	{
		if (!id.endsWith("/"))
		{
			id = id + "/";
		}

		return new BaseJCRCollectionEdit(baseContentService, id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.util.StorageUser#newContainerEdit(org.w3c.dom.Element)
	 */
	public Edit newContainerEdit(Element element)
	{
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.util.StorageUser#newContainerEdit(org.sakaiproject.entity.api.Entity)
	 */
	public Edit newContainerEdit(Entity other)
	{
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.util.StorageUser#newResource(org.sakaiproject.entity.api.Entity,
	 *      java.lang.String, java.lang.Object[])
	 */
	public Entity newResource(Entity container, String id, Object[] others)
	{
		return new BaseJCRResourceEdit(baseContentService, id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.util.StorageUser#newResource(org.sakaiproject.entity.api.Entity,
	 *      org.w3c.dom.Element)
	 */
	public Entity newResource(Entity container, Element element)
	{
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.util.StorageUser#newResource(org.sakaiproject.entity.api.Entity,
	 *      org.sakaiproject.entity.api.Entity)
	 */
	public Entity newResource(Entity container, Entity other)
	{
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.util.StorageUser#newResourceEdit(org.sakaiproject.entity.api.Entity,
	 *      java.lang.String, java.lang.Object[])
	 */
	public Edit newResourceEdit(Entity container, String id, Object[] others)
	{
		return new BaseJCRResourceEdit(baseContentService, id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.util.StorageUser#newResourceEdit(org.sakaiproject.entity.api.Entity,
	 *      org.w3c.dom.Element)
	 */
	public Edit newResourceEdit(Entity container, Element element)
	{
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.util.StorageUser#newResourceEdit(org.sakaiproject.entity.api.Entity,
	 *      org.sakaiproject.entity.api.Entity)
	 */
	public Edit newResourceEdit(Entity container, Entity other)
	{
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.util.StorageUser#storageFields(org.sakaiproject.entity.api.Entity)
	 */
	public Object[] storageFields(Entity r)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @return the baseContentService
	 */
	public BaseContentService getBaseContentService()
	{
		return baseContentService;
	}

	/**
	 * @param baseContentService
	 *        the baseContentService to set
	 */
	public void setBaseContentService(BaseContentService baseContentService)
	{
		this.baseContentService = baseContentService;
	}

	/**
	 * @return the entityToJcr
	 */
	public Map<String, String> getEntityToJcr()
	{
		return entityToJcr;
	}

	/**
	 * @param entityToJcr
	 *        the entityToJcr to set
	 */
	public void setEntityToJcr(Map<String, String> entityToJcr)
	{
		this.entityToJcr = entityToJcr;
	}

	/**
	 * @return the jcrToEntity
	 */
	public Map<String, String> getJcrToEntity()
	{
		return jcrToEntity;
	}

	/**
	 * @param jcrToEntity
	 *        the jcrToEntity to set
	 */
	public void setJcrToEntity(Map<String, String> jcrToEntity)
	{
		this.jcrToEntity = jcrToEntity;
	}

	/**
	 * @return the jcrTypes
	 */
	public Map<String, String> getJcrTypes()
	{
		return jcrTypes;
	}

	/**
	 * @param jcrTypes
	 *        the jcrTypes to set
	 */
	public void setJcrTypes(Map<String, String> jcrTypes)
	{
		this.jcrTypes = jcrTypes;
	}

	/**
	 * @return the jcrWorkspace
	 */
	public String getJcrWorkspace()
	{
		return jcrWorkspace;
	}

	/**
	 * @param jcrWorkspace
	 *        the jcrWorkspace to set
	 */
	public void setJcrWorkspace(String jcrWorkspace)
	{
		this.jcrWorkspace = jcrWorkspace;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.LiteStorageUser#startupNodes()
	 */
	public Iterator<String> startupNodes()
	{
		return createNodes.iterator();
	}

	/**
	 * @return the createNodes
	 */
	public List<String> getCreateNodes()
	{
		return createNodes;
	}

	/**
	 * @param createNodes
	 *        the createNodes to set
	 */
	public void setCreateNodes(List<String> createNodes)
	{
		this.createNodes = createNodes;
	}

	/**
	 * @return the namespaces
	 */
	public Map<String, String> getNamespaces()
	{
		return namespaces;
	}

	/**
	 * @param namespaces
	 *        the namespaces to set
	 */
	public void setNamespaces(Map<String, String> namespaces)
	{
		this.namespaces = namespaces;
	}

}
