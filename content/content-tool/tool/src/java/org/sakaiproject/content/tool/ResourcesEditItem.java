/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.content.tool;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import org.sakaiproject.content.api.GroupAwareEntity.AccessMode;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.cover.NotificationService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Validator;

/**
 * Inner class encapsulates information about resources (folders and items) for editing
 * This is being phased out as we switch to the resources type registry.
 */
public class ResourcesEditItem
	extends ResourcesBrowseItem
{
	/** Resource bundle using current language locale */
	private static ResourceLoader rb = new ResourceLoader("content");

	protected String m_copyrightStatus;
	protected String m_copyrightInfo;
	// protected boolean m_copyrightAlert;

	protected String m_filename;
	protected byte[] m_content;
	protected String m_encoding = ResourcesAction.UTF_8_ENCODING;

	protected String m_mimetype;
	protected String m_description;
	protected Map m_metadata;
	protected boolean m_hasQuota;
	protected boolean m_canSetQuota;
	protected String m_quota;
	protected boolean m_isUrl;
	protected boolean m_contentHasChanged;
	protected boolean m_contentTypeHasChanged;
	protected int m_notification = NotificationService.NOTI_NONE;

	protected String m_formtype;
	protected String m_rootname;
	protected Map m_structuredArtifact;
	protected List m_properties;

	protected Set m_metadataGroupsShowing;

	protected Set m_missingInformation;
	protected boolean m_hasBeenAdded;
	protected ResourcesMetadata m_form;
	protected boolean m_isBlank;
	protected String m_instruction;
	protected String m_ccRightsownership;
	protected String m_ccLicense;
	protected String m_ccCommercial;
	protected String m_ccModification;
	protected String m_ccRightsOwner;
	protected String m_ccRightsYear;

	protected boolean m_hidden;
	protected Time m_releaseDate;
	protected Time m_retractDate;
	protected boolean m_useReleaseDate;
	protected boolean m_useRetractDate;
	private boolean m_isInUserSite;
	protected boolean m_isAdmin;

	/**
	 * @param id
	 * @param name
	 * @param type
	 */
	public ResourcesEditItem(String id, String name, String type)
	{
		super(id, name, type);

		m_filename = "";
		m_contentHasChanged = false;
		m_contentTypeHasChanged = false;
		m_metadata = new Hashtable();
		m_structuredArtifact = new Hashtable();
		m_metadataGroupsShowing = new HashSet();
		m_mimetype = type;
		m_content = null;
		m_encoding = ResourcesAction.UTF_8_ENCODING;
		m_notification = NotificationService.NOTI_NONE;
		m_hasQuota = false;
		m_canSetQuota = false;
		m_canSelect = true;
		m_formtype = "";
		m_rootname = "";
		m_missingInformation = new HashSet();
		m_hasBeenAdded = false;
		m_properties = new Vector();
		m_isBlank = true;
		m_instruction = "";
		m_ccRightsownership = "";
		m_ccLicense = "";
		// m_copyrightStatus = ServerConfigurationService.getString("default.copyright");

		m_hidden = false;
		m_releaseDate = TimeService.newTime();
		m_retractDate = TimeService.newTime();
		m_useReleaseDate = false;
		m_useRetractDate = false;


	}

	public void setInWorkspace(boolean isInUserSite) 
	{
		m_isInUserSite = isInUserSite;
	}

	public boolean isInWorkspace()
	{
		return m_isInUserSite;
	}

	public void setHidden(boolean hidden) 
	{
		this.m_hidden = hidden;
	}

	public boolean isHidden()
	{
		return this.m_hidden;
	}

	public SortedSet<String> convertToRefs(Collection<String> groupIds) 
	{
		SortedSet<String> groupRefs = new TreeSet<String>();
		Iterator<String> it = groupIds.iterator();
		while(it.hasNext())
		{
			String groupId = (String) it.next();
			Group group = (Group) this.m_allSiteGroupsMap.get(groupId);
			if(group != null)
			{
				groupRefs.add(group.getReference());
			}
		}
		return groupRefs;

	}

	public void setRightsowner(String ccRightsOwner)
	{
		m_ccRightsOwner = ccRightsOwner;
	}

	public String getRightsowner()
	{
		return m_ccRightsOwner;
	}

	public void setRightstyear(String ccRightsYear)
	{
		m_ccRightsYear = ccRightsYear;
	}

	public String getRightsyear()
	{
		return m_ccRightsYear;
	}

	public void setAllowModifications(String ccModification)
	{
		m_ccModification = ccModification;
	}

	public String getAllowModifications()
	{
		return m_ccModification;
	}

	public void setAllowCommercial(String ccCommercial)
	{
		m_ccCommercial = ccCommercial;
	}

	public String getAllowCommercial()
	{
		return m_ccCommercial;
	}

	/**
	 * 
	 * @param license
	 */
	 public void setLicense(String license)
	 {
		 m_ccLicense = license;
	 }

	 /**
	  * 
	  * @return
	  */
	 public String getLicense()
	 {
		 return m_ccLicense;
	 }

	 /**
	  * Record a value for instructions to be displayed to the user in the editor (for Form Items).
	  * @param instruction The value of the instructions.
	  */
	 public void setInstruction(String instruction)
	 {
		 if(instruction == null)
		 {
			 instruction = "";
		 }

		 m_instruction = instruction.trim();
	 }

	 /**
	  * Access instructions to be displayed to the user in the editor (for Form Items).
	  * @return The instructions.
	  */
	 public String getInstruction()
	 {
		 return m_instruction;
	 }

	 /**
	  * Set the character encoding type that will be used when converting content body between strings and byte arrays.
	  * Default is UTF_8_ENCODING.
	  * @param encoding A valid name for a character set encoding scheme (@see java.lang.Charset)
	  */
	 public void setEncoding(String encoding)
	 {
		 m_encoding = encoding;
	 }

	 /**
	  * Get the character encoding type that is used when converting content body between strings and byte arrays.
	  * Default is "UTF-8".
	  * @return The name of the character set encoding scheme (@see java.lang.Charset)
	  */
	 public String getEncoding()
	 {
		 return m_encoding;
	 }

	 /**
	  * Set marker indicating whether current item is a blank entry
	  * @param isBlank
	  */
	 public void markAsBlank(boolean isBlank)
	 {
		 m_isBlank = isBlank;
	 }

	 /**
	  * Access marker indicating whether current item is a blank entry
	  * @return true if current entry is blank, false otherwise
	  */
	 public boolean isBlank()
	 {
		 return m_isBlank;
	 }

	 /**
	  * Change the root ResourcesMetadata object that defines the form for a Structured Artifact.
	  * @param form
	  */
	 public void setForm(ResourcesMetadata form)
	 {
		 m_form = form;
	 }

	 /**
	  * Access the root ResourcesMetadata object that defines the form for a Structured Artifact.
	  * @return the form.
	  */
	 public ResourcesMetadata getForm()
	 {
		 return m_form;
	 }

	 /**
	  * @param properties
	  */
	 public void setProperties(List properties)
	 {
		 m_properties = properties;

	 }

	 public List getProperties()
	 {
		 return m_properties;
	 }



	 /**
	  * Replace current values of Structured Artifact with new values.
	  * @param map The new values.
	  */
	 public void setValues(Map map)
	 {
		 m_structuredArtifact = map;

	 }

	 /**
	  * Access the entire set of values stored in the Structured Artifact
	  * @return The set of values.
	  */
	 public Map getValues()
	 {
		 return m_structuredArtifact;

	 }

	 /**
	  * @param id
	  * @param name
	  * @param type
	  */
	 public ResourcesEditItem(String type)
	 {
		 this(null, "", type);
	 }

	 /**
	  * @param id
	  */
	 public void setId(String id)
	 {
		 m_id = id;
	 }

	 /**
	  * Show the indicated metadata group for the item
	  * @param group
	  */
	 public void showMetadataGroup(String group)
	 {
		 m_metadataGroupsShowing.add(group);
	 }

	 /**
	  * Hide the indicated metadata group for the item
	  * @param group
	  */
	 public void hideMetadataGroup(String group)
	 {
		 m_metadataGroupsShowing.remove(group);
		 m_metadataGroupsShowing.remove(Validator.escapeUrl(group));
	 }

	 /**
	  * Query whether the indicated metadata group is showing for the item
	  * @param group
	  * @return true if the metadata group is showing, false otherwise
	  */
	 public boolean isGroupShowing(String group)
	 {
		 return m_metadataGroupsShowing.contains(group) || m_metadataGroupsShowing.contains(Validator.escapeUrl(group));
	 }

	 /**
	  * @return
	  */
	 public boolean isFileUpload()
	 {
		 return !isFolder() && !isUrl() && !isHtml() && !isPlaintext() ;
	 }

	 /**
	  * @param type
	  */
	 public void setType(String type)
	 {
		 m_type = type;
	 }

	 /**
	  * @param mimetype
	  */
	 public void setMimeType(String mimetype)
	 {
		 m_mimetype = mimetype;
	 }

	 public String getRightsownership()
	 {
		 return m_ccRightsownership;
	 }

	 public void setRightsownership(String owner)
	 {
		 m_ccRightsownership = owner;
	 }

	 /**
	  * @return
	  */
	 public String getMimeType()
	 {
		 return m_mimetype;
	 }

	 public String getMimeCategory()
	 {
		 if(this.m_mimetype == null || "".equals(this.m_mimetype))
		 {
			 return "";
		 }
		 int index = this.m_mimetype.indexOf("/");
		 if(index < 0)
		 {
			 return this.m_mimetype;
		 }
		 return this.m_mimetype.substring(0, index);
	 }

	 public String getMimeSubtype()
	 {
		 if(this.m_mimetype == null || "".equals(this.m_mimetype))
		 {
			 return "";
		 }
		 int index = this.m_mimetype.indexOf("/");
		 if(index < 0 || index + 1 == this.m_mimetype.length())
		 {
			 return "";
		 }
		 return this.m_mimetype.substring(index + 1);
	 }

	 /**
	  * @param formtype
	  */
	 public void setFormtype(String formtype)
	 {
		 m_formtype = formtype;
	 }

	 /**
	  * @return
	  */
	 public String getFormtype()
	 {
		 return m_formtype;
	 }

	 /**
	  * @return Returns the copyrightInfo.
	  */
	 public String getCopyrightInfo() 
	 {
		 return m_copyrightInfo;
	 }
	 /**
	  * @param copyrightInfo The copyrightInfo to set.
	  */
	 public void setCopyrightInfo(String copyrightInfo) 
	 {
		 m_copyrightInfo = copyrightInfo;
	 }
	 /**
	  * @return Returns the copyrightStatus.
	  */
	 public String getCopyrightStatus() 
	 {
		 return m_copyrightStatus;
	 }
	 /**
	  * @param copyrightStatus The copyrightStatus to set.
	  */
	 public void setCopyrightStatus(String copyrightStatus) 
	 {
		 m_copyrightStatus = copyrightStatus;
	 }
	 /**
	  * @return Returns the description.
	  */
	 public String getDescription() 
	 {
		 return m_description;
	 }
	 /**
	  * @param description The description to set.
	  */
	 public void setDescription(String description) 
	 {
		 m_description = description;
	 }
	 /**
	  * @return Returns the filename.
	  */
	 public String getFilename() 
	 {
		 return m_filename;
	 }
	 /**
	  * @param filename The filename to set.
	  */
	 public void setFilename(String filename) 
	 {
		 m_filename = filename;
	 }
	 /**
	  * @return Returns the metadata.
	  */
	 public Map getMetadata() 
	 {
		 return m_metadata;
	 }
	 /**
	  * @param metadata The metadata to set.
	  */
	 public void setMetadata(Map metadata) 
	 {
		 m_metadata = metadata;
	 }
	 /**
	  * @param name
	  * @param value
	  */
	 public void setMetadataItem(String name, Object value)
	 {
		 m_metadata.put(name, value);
	 }

	 /**
	  * @return
	  */
	 public boolean isSitePossible()
	 {
		 return !m_pubview_inherited && !isGroupInherited() && !isSingleGroupInherited();
	 }

	 /**
	  * @return
	  */
	 public boolean isGroupPossible()
	 {
		 // Collection groups = getPossibleGroups();
		 // return ! groups.isEmpty();
		 return this.m_allowedAddGroupRefs != null && ! this.m_allowedAddGroupRefs.isEmpty();

	 }

	 /**
	  * @return
	  */
	 public boolean isGroupInherited()
	 {
		 return AccessMode.INHERITED.toString().equals(this.m_access) && AccessMode.GROUPED.toString().equals(m_inheritedAccess);
	 }

	 /**
	  * Does this entity inherit grouped access mode with a single group that has access?
	  * @return true if this entity inherits grouped access mode with a single group that has access, and false otherwise.
	  */
	 public boolean isSingleGroupInherited()
	 {
		 //Collection groups = getInheritedGroups();
		 return // AccessMode.INHERITED.toString().equals(this.m_access) && 
		 AccessMode.GROUPED.toString().equals(this.m_inheritedAccess) && 
		 this.m_inheritedGroupRefs != null && 
		 this.m_inheritedGroupRefs.size() == 1; 
		 // && this.m_oldInheritedGroups != null 
		 // && this.m_oldInheritedGroups.size() == 1;
	 }

	 /**
	  * @return
	  */
	 public String getSingleGroupTitle()
	 {
		 return (String) rb.getFormattedMessage("access.title4", new Object[]{getGroupNames()});
	 }

	 /**
	  * Is this entity's access restricted to the site (not pubview) and are there no groups defined for the site?
	  * @return
	  */
	 public boolean isSiteOnly()
	 {
		 boolean isSiteOnly = false;
		 isSiteOnly = !isGroupPossible() && !isPubviewPossible();
		 return isSiteOnly;
	 }


	 /**
	  * @return Returns the content.
	  */
	 public byte[] getContent() 
	 {
		 return m_content;
	 }

	 /**
	  * @return Returns the content as a String.
	  */
	 public String getContentstring()
	 {
		 String rv = "";
		 if(m_content != null && m_content.length > 0)
		 {
			 try
			 {
				 rv = new String( m_content, m_encoding );
			 }
			 catch(UnsupportedEncodingException e)
			 {
				 rv = new String( m_content );
			 }
		 }
		 return rv;
	 }

	 /**
	  * @param content The content to set.
	  */
	 public void setContent(byte[] content) {
		 m_content = content;
	 }

	 /**
	  * @param content The content to set.
	  */
	 public void setContent(String content) {
		 try
		 {
			 m_content = content.getBytes(m_encoding);
		 }
		 catch(UnsupportedEncodingException e)
		 {
			 m_content = content.getBytes();
		 }
	 }

	 /**
	  * @return Returns the canSetQuota.
	  */
	 public boolean canSetQuota() {
		 return m_canSetQuota;
	 }

	 /**
	  * @param canSetQuota The canSetQuota to set.
	  */
	 public void setCanSetQuota(boolean canSetQuota) 
	 {
		 m_canSetQuota = canSetQuota;
	 }

	 
	  
	 /**
	  * Can the user set admin properties?
	  * @return
	  */
	 public boolean isAdmin() {
		return m_isAdmin;
	}

	 /**
	  * 
	  * @param canSetAdminProps
	  */
	public void setIsAdmin(boolean canSetAdminProps) {
		m_isAdmin = canSetAdminProps;
	}

	/**
	  * @return Returns the hasQuota.
	  */
	 public boolean hasQuota() 
	 {
		 return m_hasQuota;
	 }

	 /**
	  * @param hasQuota The hasQuota to set.
	  */
	 public void setHasQuota(boolean hasQuota) 
	 {
		 m_hasQuota = hasQuota;
	 }

	 /**
	  * @return Returns the quota.
	  */
	 public String getQuota() 
	 {
		 return m_quota;
	 }

	 /**
	  * @param quota The quota to set.
	  */
	 public void setQuota(String quota) 
	 {
		 m_quota = quota;
	 }

	 /**
	  * @return true if content-type of item indicates it represents a URL, false otherwise
	  */
	 public boolean isUrl()
	 {
		 return ResourcesAction.TYPE_URL.equals(m_type) || ResourceProperties.TYPE_URL.equals(m_mimetype);
	 }

	 /**
	  * @return true if content-type of item is "text/text" (plain text), false otherwise
	  */
	 public boolean isPlaintext()
	 {
		 return ResourcesAction.MIME_TYPE_DOCUMENT_PLAINTEXT.equals(m_mimetype) || ResourcesAction.MIME_TYPE_DOCUMENT_PLAINTEXT.equals(m_type);
	 }

	 /**
	  * @return true if content-type of item is "text/html" (an html document), false otherwise
	  */
	 public boolean isHtml()
	 {
		 return ResourcesAction.MIME_TYPE_DOCUMENT_HTML.equals(m_mimetype) || ResourcesAction.MIME_TYPE_DOCUMENT_HTML.equals(m_type);
	 }

	 /**
	  * @return
	  */
	 public boolean contentHasChanged()
	 {
		 return m_contentHasChanged;
	 }

	 /**
	  * @param changed
	  */
	 public void setContentHasChanged(boolean changed)
	 {
		 m_contentHasChanged = changed;
	 }

	 /**
	  * @return
	  */
	 public boolean contentTypeHasChanged()
	 {
		 return m_contentTypeHasChanged;
	 }

	 /**
	  * @param changed
	  */
	 public void setContentTypeHasChanged(boolean changed)
	 {
		 m_contentTypeHasChanged = changed;
	 }

	 /**
	  * @param notification
	  */
	 public void setNotification(int notification)
	 {
		 m_notification = notification;
	 }

	 /**
	  * @return
	  */
	 public int getNotification()
	 {
		 return m_notification;
	 }

	 /**
	  * @return Returns the artifact.
	  */
	 public Map getStructuredArtifact()
	 {
		 return m_structuredArtifact;
	 }

	 /**
	  * @param artifact The artifact to set.
	  */
	 public void setStructuredArtifact(Map artifact)
	 {
		 this.m_structuredArtifact = artifact;
	 }

	 /**
	  * @param name
	  * @param value
	  */
	 public void setValue(String name, Object value)
	 {
		 setValue(name, 0, value);
	 }

	 /**
	  * @param name
	  * @param index
	  * @param value
	  */
	 public void setValue(String name, int index, Object value)
	 {
		 List list = getList(name);
		 try
		 {
			 list.set(index, value);
		 }
		 catch(ArrayIndexOutOfBoundsException e)
		 {
			 list.add(value);
		 }
		 m_structuredArtifact.put(name, list);
	 }

	 /**
	  * Access a value of a structured artifact field of type String.
	  * @param name	The name of the field to access.
	  * @return the value, or null if the named field is null or not a String.
	  */
	 public String getString(String name)
	 {
		 if(m_structuredArtifact == null)
		 {
			 m_structuredArtifact = new Hashtable();
		 }
		 Object value = m_structuredArtifact.get(name);
		 String rv = "";
		 if(value == null)
		 {
			 // do nothing
		 }
		 else if(value instanceof String)
		 {
			 rv = (String) value;
		 }
		 else
		 {
			 rv = value.toString();
		 }
		 return rv;
	 }

	 /**
	  * @param name
	  * @param index
	  * @return
	  */
	 public Object getValue(String name, int index)
	 {
		 List list = getList(name);
		 Object rv = null;
		 try
		 {
			 rv = list.get(index);
		 }
		 catch(ArrayIndexOutOfBoundsException e)
		 {
			 // return null
		 }
		 return rv;

	 }

	 /**
	  * @param name
	  * @return
	  */
	 public Object getPropertyValue(String name)
	 {
		 return getPropertyValue(name, 0);
	 }

	 /**
	  * Access a particular value in a Structured Artifact, as identified by the parameter "name".  This
	  * implementation of the method assumes that the name is a series of String identifiers delimited
	  * by the ResourcesAction.ResourcesMetadata.DOT String.
	  * @param name The delimited identifier for the item.
	  * @return The value identified by the name, or null if the name does not identify a valid item.
	  */
	 public Object getPropertyValue(String name, int index)
	 {
		 Object rv = null;
		 if(m_properties == null)
		 {
			 m_properties = new Vector();
		 }
		 Iterator it = m_properties.iterator();
		 while(rv == null && it.hasNext())
		 {
			 ResourcesMetadata prop = (ResourcesMetadata) it.next();
			 if(name.equals(prop.getDottedname()))
			 {
				 rv = prop.getValue(index);
			 }
		 }
		 return rv;

	 }

	 /**
	  * @param name
	  * @param value
	  */
	 public void setPropertyValue(String name, Object value)
	 {
		 setPropertyValue(name, 0, value);
	 }

	 /**
	  * Access a particular value in a Structured Artifact, as identified by the parameter "name".  This
	  * implementation of the method assumes that the name is a series of String identifiers delimited
	  * by the ResourcesAction.ResourcesMetadata.DOT String.
	  * @param name The delimited identifier for the item.
	  * @return The value identified by the name, or null if the name does not identify a valid item.
	  */
	 public void setPropertyValue(String name, int index, Object value)
	 {
		 if(m_properties == null)
		 {
			 m_properties = new Vector();
		 }
		 boolean found = false;
		 Iterator it = m_properties.iterator();
		 while(!found && it.hasNext())
		 {
			 ResourcesMetadata prop = (ResourcesMetadata) it.next();
			 if(name.equals(prop.getDottedname()))
			 {
				 found = true;
				 prop.setValue(index, value);
			 }
		 }

	 }

	 /**
	  * Access a particular value in a Structured Artifact, as identified by the parameter "name".  This
	  * implementation of the method assumes that the name is a series of String identifiers delimited
	  * by the ResourcesAction.ResourcesMetadata.DOT String.
	  * @param name The delimited identifier for the item.
	  * @return The value identified by the name, or null if the name does not identify a valid item.
	  */
	 public Object getValue(String name)
	 {
		 //. has a special meaning in regex so needs to be escaped
		 String[] names = name.split("\\" + ResourcesMetadata.DOT);
		 Object rv = m_structuredArtifact;
		 if(rv != null && ((Map) rv).isEmpty())
		 {
			 rv = null;
		 }
		 for(int i = 1; rv != null && i < names.length; i++)
		 {
			 if(rv instanceof Map)
			 {
				 rv = ((Map) rv).get(names[i]);
			 }
			 else
			 {
				 rv = null;
			 }
		 }
		 return rv;

	 }

	 /**
	  * Access a list of values associated with a named property of a structured artifact.
	  * @param name The name of the property.
	  * @return The list of values associated with that name, or an empty list if the property is not defined.
	  */
	 public List getList(String name)
	 {
		 if(m_structuredArtifact == null)
		 {
			 m_structuredArtifact = new Hashtable();
		 }
		 Object value = m_structuredArtifact.get(name);
		 List rv = new Vector();
		 if(value == null)
		 {
			 m_structuredArtifact.put(name, rv);
		 }
		 else if(value instanceof Collection)
		 {
			 rv.addAll((Collection)value);
		 }
		 else
		 {
			 rv.add(value);
		 }
		 return rv;

	 }

	 /**
	  * @return
	  */
	 /*
		public Element exportStructuredArtifact(List properties)
		{
	        return null;
		}
	  */

	 /**
	  * @return Returns the name of the root of a structured artifact definition.
	  */
	 public String getRootname()
	 {
		 return m_rootname;
	 }

	 /**
	  * @param rootname The name to be assigned for the root of a structured artifact.
	  */
	 public void setRootname(String rootname)
	 {
		 m_rootname = rootname;
	 }

	 /**
	  * Add a property name to the list of properties missing from the input.
	  * @param propname The name of the property.
	  */
	 public void setMissing(String propname)
	 {
		 m_missingInformation.add(propname);
	 }

	 /**
	  * Query whether a particular property is missing
	  * @param propname The name of the property
	  * @return The value "true" if the property is missing, "false" otherwise.
	  */
	 public boolean isMissing(String propname)
	 {
		 return m_missingInformation.contains(propname) || m_missingInformation.contains(Validator.escapeUrl(propname));
	 }

	 /**
	  * Empty the list of missing properties.
	  */
	 public void clearMissing()
	 {
		 m_missingInformation.clear();
	 }

	 public void setAdded(boolean added)
	 {
		 m_hasBeenAdded = added;
	 }

	 public boolean hasBeenAdded()
	 {
		 return m_hasBeenAdded;
	 }

	 /**
	  * @return the releaseDate
	  */
	 public Time getReleaseDate() 
	 {
		 return m_releaseDate;
	 }

	 /**
	  * @param releaseDate the releaseDate to set
	  */
	 public void setReleaseDate(Time releaseDate) 
	 {
		 this.m_releaseDate = releaseDate;
	 }

	 /**
	  * @return the retractDate
	  */
	 public Time getRetractDate() 
	 {
		 return m_retractDate;
	 }

	 /**
	  * @param retractDate the retractDate to set
	  */
	 public void setRetractDate(Time retractDate) 
	 {
		 this.m_retractDate = retractDate;
	 }

	 /**
	  * @return the useReleaseDate
	  */
	 public boolean useReleaseDate() 
	 {
		 return m_useReleaseDate;
	 }

	 /**
	  * @param useReleaseDate the useReleaseDate to set
	  */
	 public void setUseReleaseDate(boolean useReleaseDate) 
	 {
		 this.m_useReleaseDate = useReleaseDate;
	 }

	 /**
	  * @return the useRetractDate
	  */
	 public boolean useRetractDate() 
	 {
		 return m_useRetractDate;
	 }

	 /**
	  * @param useRetractDate the useRetractDate to set
	  */
	 public void setUseRetractDate(boolean useRetractDate) 
	 {
		 this.m_useRetractDate = useRetractDate;
	 }

}	// inner class ResourcesEditItem