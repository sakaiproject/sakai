/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2009 Sakai Foundation
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

package org.sakaiproject.rights.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.rights.api.CreativeCommonsLicense;
import org.sakaiproject.rights.api.CreativeCommonsLicenseManager;
import org.sakaiproject.util.Xml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * 
 * CCLicenseManagerImpl
 *
 */
public class CreativeCommonsLicenseManagerImpl implements CreativeCommonsLicenseManager, Observer 
{
	protected static final String ATTR_REQUIRES = "requires";

	protected static final String ATTR_PROHIBITS = "prohibits";

	protected static final String ATTR_PERMITS = "permits";

	protected static final String ATTR_JURISDICTION = "jurisdiction";

	protected static final String ATTR_VERSION = "version";

	private Log logger = LogFactory.getLog(CreativeCommonsLicenseManagerImpl.class);
	
	protected Map<String,CreativeCommonsLicense> licenses = new HashMap<String,CreativeCommonsLicense>();

	protected Map<String,Map<String,Set<String>>> indexes = new HashMap<String,Map<String,Set<String>>>();
	
	protected List<String> versionList = new ArrayList<String>();
	public void setVersionList(List versions)
	{
		this.versionList = versions;
	}

	public List<String> getVersionList() 
	{
		return new ArrayList<String>(this.versionList);
	}

	protected String rootFolderReference = "/content/user/admin/";
	public void setRootFolderReference(String rootFolderReference) {
		this.rootFolderReference = rootFolderReference;
	}
	
	protected String relativeFolderPath = "creativecommons/";
	public void setRelativeFolderPath(String relativeFolderPath) {
		this.relativeFolderPath = relativeFolderPath;
	}
	
	protected ContentHostingService contentService;
	public void setContentService(ContentHostingService contentService)
	{
		this.contentService = contentService;
	}
	
	protected EntityManager entityManager;
	public void setEntityManager(EntityManager entityManager)
	{
		this.entityManager = entityManager;
	}
	
	protected EventTrackingService eventTrackingService;
	public void setEventTrackingService(EventTrackingService eventTrackingService)
	{
		this.eventTrackingService = eventTrackingService;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.rights.api.CCLicenseManager#getLicense(java.lang.String, java.lang.String, java.lang.String)
	 */
	public Collection<CreativeCommonsLicense> getLicenses(String version, String jurisdiction, Set<String> permits, Set<String> prohibits, Set<String> requires) 
	{
		Set<String> included = new HashSet<String>(this.licenses.keySet());
		
		included.retainAll(getLicenseIdentifiers(ATTR_JURISDICTION, jurisdiction));
		included.retainAll(getLicenseIdentifiers(ATTR_PERMITS, permits));
		included.retainAll(getLicenseIdentifiers(ATTR_PROHIBITS, prohibits));
		included.retainAll(getLicenseIdentifiers(ATTR_REQUIRES, requires));

		if(included.size() > 0)
		{
			if(version == null)
			{
				// do nothing
			}
			else
			{
				if(version == LATEST_VERSION)
				{
					// filter to find latest version
					Map<String, Set<String>> versionMap = this.indexes.get(ATTR_VERSION);
					for(String v : this.getVersionList())
					{
						Set<String> vSet = new HashSet<String>(versionMap.get(v));
						vSet.retainAll(included);
						if(vSet.size() > 0)
						{
							included.retainAll(vSet);
							break;
						}
					}
				}
				else
				{
					included.retainAll( getLicenseIdentifiers(ATTR_VERSION, version) );
				}
			}
		}
		
		Collection<CreativeCommonsLicense> rv = new ArrayList<CreativeCommonsLicense>();
		for(String key : included)
		{
			rv.add(this.licenses.get(key));
		}

		return rv;
	}

	/**
	 * @param index_name
	 * @param values
	 * @return
	 */
	protected Set<String> getLicenseIdentifiers(String index_name, Set<String> values) 
	{
		Set<String> license_ids = this.licenses.keySet();
		if(values == null || values.isEmpty())
		{
			// do nothing
		}
		else
		{
			Map<String,Set<String>> map = this.indexes.get(index_name);
			for(String value : values)
			{
				Set<String> set = map.get(value);
				license_ids.retainAll(set);
			}
		}
		return license_ids;
	}

	/**
	 * Get all identifiers for licenses which have the specified value for the named attribute.
	 * If value is null, this method returns all values from all indexes for this attribute. 
	 * @param name
	 * @param value
	 * @return
	 */
	protected Set<String> getLicenseIdentifiers(String name, String value) 
	{
		Set<String> licenses;
		
		Map<String,Set<String>> index = this.indexes.get(name);
		if(index == null)
		{
			index = new HashMap<String,Set<String>>();
			this.indexes.put(name, index);
		}
		
		if(value == null || value.trim().equals(""))
		{
			licenses = new HashSet<String>();
			// get all values
			for(String key : index.keySet())
			{
				licenses.addAll(index.get(key));
			}
		}
		else
		{
			Set<String> set = index.get(value);
			if(set == null)
			{
				set = new HashSet<String>();
				index.put(value, set);
			}
			licenses = new HashSet<String>(set);
		}
		return licenses;
	}
	
	
	public void init()
	{
		logger.info(this + ".init()");
		this.eventTrackingService.addObserver(this);
		
		String folderRef = rootFolderReference + relativeFolderPath;
		
		Reference reference = this.entityManager.newReference(folderRef);
		String folderId = reference.getId();
		logger.info(this + ".init() id == " + reference.getId());
				
		// check whether folder exists. If not, add it.
		try 
		{
			this.contentService.checkCollection(folderId);
		} 
		catch (IdUnusedException e) 
		{
			try 
			{
				this.contentService.addCollection(folderId);
			} 
			catch (IdUsedException e1) 
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 
			catch (IdInvalidException e1) 
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 
			catch (PermissionException e1) 
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 
			catch (InconsistentException e1) 
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} 
		catch (TypeException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (PermissionException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// check whether there are files in the folder.  If so, read them and add licenses 
		readAllLicenses(folderId);
		
		
		// watch the folder for newly added items 
		//    when new items are added or existing items are changed, read them and add licenses
		//    when new items, start watching them
		// check whether there are license files in the pack. If so add them to the root folder.
		
		// need to get a reference obj using the folderRef and get the id from the reference obj

		for(String uri : this.licenses.keySet())
		{
			CreativeCommonsLicense license = this.licenses.get(uri);
			
			logger.info(license.toJSON());
		}
	}

	/**
	 * @param folderId
	 */
	protected void readAllLicenses(String folderId) {
		try
		{
			
			ContentCollection collection = this.contentService.getCollection(folderId);
			
			for(ContentEntity entity : (List<ContentEntity>) collection.getMemberResources())
			{
				if(entity.isResource())
				{
					ContentResource resource = (ContentResource) entity;
					Collection<CreativeCommonsLicense> licenses = readLicenses(resource);
					
					if(licenses != null)
					{
						for(CreativeCommonsLicense license : licenses)
						{
							this.licenses.put(license.getUri(), license);
						}
					}
				}
			}
		}
		catch(IdUnusedException e)
		{
			logger.debug("IdUnusedException " + e, e);
		}
		catch(TypeException e)
		{
			logger.debug("TypeException " + e, e);
		} catch (PermissionException e) 
		{
			logger.debug("PermissionException " + e, e);
		}
	}

	/**
	 * @param resource
	 * @return
	 */
	protected Collection<CreativeCommonsLicense> readLicenses(ContentResource resource) 
	{
		List<CreativeCommonsLicense> list = new ArrayList<CreativeCommonsLicense>();
		try
		{
			InputStream in = resource.streamContent();
			Document doc = Xml.readDocumentFromStream(in);
			NodeList licenses = doc.getElementsByTagName("cc:License");
			for(int i = 0; i < licenses.getLength(); i++)
			{
				Node license_node = licenses.item(i);
				if(license_node instanceof Element)
				{
					Element license_element = (Element) license_node;
					CreativeCommonsLicense license = new CreativeCommonsLicenseImpl();
					
					license.setUri(license_element.getAttribute(RDF_ABOUT));
					license.setJurisdiction(getNodeResourceValue(license_element, CC_JURISDICTION));
					license.setSource(getNodeResourceValue(license_element, DC_SOURCE));
					license.setReplacedBy(this.getNodeResourceValue(license_element, DCQ_IS_REPLACED_BY));
					license.setVersion(this.getNodeResourceValue(license_element, DCQ_IS_REPLACED_BY));
					license.setLegalcode(this.getNodeResourceValue(license_element, CC_LEGALCODE));
					license.setCreator(this.getNodeResourceValue(license_element, DC_CREATOR));
					
					license.addPermissions(this.getNodeResourceValues(license_element, CC_PERMITS));
					license.addRequirements(this.getNodeResourceValues(license_element, CC_REQUIRES));
					license.addProhibitions(this.getNodeResourceValues(license_element, CC_PROHIBITS));
					
					license.addDescriptions(getLocalizationMap(license_element, DC_DESCRIPTION));
					license.addTitles(this.getLocalizationMap(license_element, DC_TITLE));
					
					list.add(license);
				}
				
			}
		}
		catch(Exception e)
		{
			logger.warn("Exception " + e, e);
		}
		return list;
	}

	/**
	 * @param parent
	 * @param node_name
	 * @return
	 */
	protected Map<String, String> getLocalizationMap(Element parent, String node_name) 
	{
		Map<String,String> values = new HashMap<String,String>();
		NodeList nodes = parent.getElementsByTagName(node_name);
		
		for(int n = 0; n < nodes.getLength(); n++)
		{
			Node node = nodes.item(n);
			if(node instanceof Element)
			{
				Element element = (Element) node;
				String lang = element.getAttribute(XML_LANG);
				String value = element.getTextContent();
				if(value != null && ! value.trim().equals(""))
				{
					values.put(lang, value);
				}
			}
		}
		return values;
	}

	/**
	 * @param parent
	 * @param node_name
	 * @return
	 */
	protected String getNodeResourceValue(Element parent, String node_name) 
	{
		NodeList nodes = parent.getElementsByTagName(node_name);
		String value = "";
		for( int n = 0; n < nodes.getLength(); n++)
		{
			Node node = nodes.item(n);
			if(node instanceof Element)
			{
				Element element = (Element) node;
				String text = element.getAttribute(RDF_RESOURCE);
				if(text != null && ! text.trim().equals(""))
				{
					value += text;
				}
			}
		}
		return value;
	}

	/**
	 * @param parent
	 * @param node_name
	 * @return
	 */
	protected Set<String> getNodeResourceValues(Element parent, String node_name) 
	{
		NodeList nodes = parent.getElementsByTagName(node_name);
		Set<String> values = new TreeSet<String>();
		for( int n = 0; n < nodes.getLength(); n++)
		{
			Node node = nodes.item(n);
			if(node instanceof Element)
			{
				Element element = (Element) node;
				String text = element.getAttribute(RDF_RESOURCE);
				if(text != null && ! text.trim().equals(""))
				{
					values.add(text);
				}
			}
		}
		return values;
	}

	public void update(Observable arg0, Object arg1) {
		if (arg1 instanceof Event) {
			Event event = (Event) arg1;
			/*
			 * Modified? If so (and this is our file) update the the
			 * configuration
			 */
			if (event.getModify()) {
				String refstr = event.getResource();

				if (refstr != null
						&& refstr.startsWith(this.rootFolderReference
								+ this.relativeFolderPath)) {
					logger.debug("Updating configuration from " + refstr);
					updateConfig(refstr);
				}
			}
		}
	}

	protected void updateConfig(String refstr) 
	{
		Reference reference = this.entityManager.newReference(refstr);
		ContentEntity entity = (ContentEntity) reference.getEntity();
		if(entity == null)
		{
			logger.info("Null entity: refstr == " + refstr);
		}
		else if(entity.isResource())
		{
			Collection<CreativeCommonsLicense> licenses = this.readLicenses((ContentResource) entity);
			if(licenses != null && ! licenses.isEmpty())
			{
				for(CreativeCommonsLicense license : licenses)
				{
					this.addLicense(license);
					logger.info(license.toJSON());
				}
			}
		}
		else
		{
			logger.info("Entity is collection: refstr == " + refstr);
			// read files from folder and update
		}

		
	}

	protected void addLicense(CreativeCommonsLicense license) 
	{
		String uri = license.getUri();
		indexLicense(uri, ATTR_VERSION, license.getVersion());
		String jurisdiction = license.getJurisdiction();
		if(jurisdiction == null || jurisdiction.trim().equals(""))
		{
			jurisdiction = DEFAULT_JURISDICTION;
		}
		indexLicense(uri, ATTR_JURISDICTION, jurisdiction);
		Collection<String> permissions = license.getPermissions();
		if(permissions != null)
		{
			for(String permission : permissions)
			{
				indexLicense(uri, ATTR_PERMITS, permission);
			}
		}
		Collection<String> prohibitions = license.getProhibitions();
		if(prohibitions != null)
		{
			for(String prohibition : prohibitions)
			{
				indexLicense(uri, ATTR_PROHIBITS, prohibition);
			}
		}
		Collection<String> requirements = license.getRequirements();
		if(requirements != null)
		{
			for(String requirement : requirements)
			{
				indexLicense(uri, ATTR_REQUIRES, requirement);
			}
		}
	}

	/**
	 * @param uri
	 * @param index_name
	 * @param key
	 */
	protected void indexLicense(String uri, String index_name, String key) 
	{
		if(key != null && ! key.trim().equals(""))
		{
			Map<String,Set<String>> index = this.indexes.get(index_name);
			if(index == null)
			{
				index = new HashMap<String,Set<String>>();
				this.indexes.put(index_name, index);
			}
			Set<String> set = index.get(key);
			if(set == null)
			{
				set = new HashSet<String>();
				index.put(key, set);
			}
			set.add(uri);
		}
	}

}
