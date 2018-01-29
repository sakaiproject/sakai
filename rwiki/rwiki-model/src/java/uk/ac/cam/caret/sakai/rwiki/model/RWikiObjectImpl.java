/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

package uk.ac.cam.caret.sakai.rwiki.model;

import java.security.MessageDigest;
import java.util.Date;

import lombok.extern.slf4j.Slf4j;

import uk.ac.cam.caret.sakai.rwiki.service.api.dao.RWikiObjectContentDao;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObjectContent;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiPermissions;

/**
 * <p>
 * CrudObjectImpl implements the CrudObject for the CrudServiceImpl.
 * </p>
 * 
 * @author University of Michigan, CHEF Software Development Team
 * @version $Revision$
 */
@Slf4j
public abstract class RWikiObjectImpl implements RWikiObject
{

	public RWikiObjectImpl()
	{
		// EMPTY
	}

	/**
	 * DAO Object for the content
	 */
	protected RWikiObjectContentDao codao = null;

	/**
	 * The lazy loaded content object
	 */
	protected RWikiObjectContent co = null;

	/** The id. */
	protected String m_id = null;

	/** The version. */
	protected Date m_version = null;

	/** The "name". */
	protected String m_name = "";

	/** The "rank". */
	protected String m_realm = "";

	/** The "serial number". */
	// Now lazy loaded protected String m_content = "";
	protected String m_referenced = "";

	protected String m_user = "";

	protected String m_owner = null;

	protected boolean m_ownerread = true;

	protected boolean m_ownerwrite = true;

	protected boolean m_owneradmin = true;

	protected boolean m_groupread = true;

	protected boolean m_groupwrite = true;

	protected boolean m_groupadmin = true;

	protected boolean m_publicread = false;

	protected boolean m_publicwrite = false;

	protected Integer m_revision = Integer.valueOf(0);

	// iebdelete protected List m_history = null;

	/**
	 * @return Returns the history.
	 */
	/*
	 * iebdelete public List getXHistory() { return m_history; }
	 */
	/**
	 * @param history
	 *        The history to set.
	 */
	/*
	 * iebdelete public void setXHistory(List history) { this.m_history =
	 * history; }
	 */
	/**
	 * {@inheritDoc}
	 */
	public String getId()
	{
		return m_id;
	}

	/**
	 * Set the id - should be used only by the storage layer, not by end users!
	 * 
	 * @param id
	 *        The object id.
	 */
	public void setId(String id)
	{
		m_id = id;
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getVersion()
	{
		return m_version;
	}

	/**
	 * Set the version - should be used only by the storage layer, not by end
	 * users!
	 * 
	 * @param version
	 *        The object version.
	 */
	public void setVersion(Date version)
	{
		m_version = version;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getName()
	{
		return m_name;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setName(String name)
	{
		m_name = name;
		if (m_name == null) m_name = "";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getRealm()
	{
		return m_realm;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setRealm(String realm)
	{
		m_realm = realm;
		if (m_realm == null) m_realm = "";
	}

	/**
	 * {@inheritDoc}
	 */
	// public String getContent()
	// {
	// return m_content;
	// }
	/**
	 * {@inheritDoc}
	 */
	// public void setContent(String content)
	// {
	// m_content = content;
	// if (m_content == null)
	// m_content = "";
	// }
	/**
	 * {@inheritDoc}
	 */
	public int compareTo(Object o)
	{
		if (!(o instanceof RWikiObject)) throw new ClassCastException();

		// if the object are the same, say so
		if (o == this) return 0;

		// start the compare by comparing their names
		int compare = getName().compareTo(((RWikiObject) o).getName());

		// if these are the same
		if (compare == 0)
		{
			// compare rank
			compare = getRealm().compareTo(((RWikiObject) o).getRealm());

			if (compare == 0)
			{
				compare = (getRevision().compareTo(((RWikiObject) o)
						.getRevision()));
				if (compare == 0)
				{
					// compare serial number
					compare = getContent().compareTo(
							((RWikiObject) o).getContent());
				}
			}
			// if these are the same
		}

		return compare;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object obj)
	{
		if (!(obj instanceof RWikiObject)) return false;

		return ((RWikiObject) obj).getId().equals(getId());
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode()
	{
		return getId().hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.tool.service.RWikiObject#getReferenced()
	 */
	public String getReferenced()
	{
		return m_referenced;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.tool.service.RWikiObject#setReferenced()
	 */
	public void setReferenced(String referenced)
	{
		m_referenced = referenced;
		// SAK-2470
		if (m_referenced == null) m_referenced = "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.tool.service.RWikiObject#getUser()
	 */
	public String getUser()
	{
		return m_user;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.tool.service.RWikiObject#getUser()
	 */
	public String getOwner()
	{
		return m_owner;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.tool.service.RWikiObject#getGroupadmin()
	 */
	public boolean getGroupAdmin()
	{
		return m_groupadmin;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.tool.service.RWikiObject#getGroupread()
	 */
	public boolean getGroupRead()
	{
		return m_groupread;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.tool.service.RWikiObject#getGroupwrite()
	 */
	public boolean getGroupWrite()
	{
		return m_groupwrite;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.tool.service.RWikiObject#getPublicread()
	 */
	public boolean getPublicRead()
	{
		return m_publicread;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.tool.service.RWikiObject#getPublicwrite()
	 */
	public boolean getPublicWrite()
	{
		return m_publicwrite;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.tool.service.RWikiObject#getUseradmin()
	 */
	public boolean getOwnerAdmin()
	{
		return m_owneradmin;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.tool.service.RWikiObject#getUserread()
	 */
	public boolean getOwnerRead()
	{
		return m_ownerread;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.tool.service.RWikiObject#getUserwrite()
	 */
	public boolean getOwnerWrite()
	{
		return m_ownerwrite;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.tool.service.RWikiObject#setGroupadmin(boolean)
	 */
	public void setGroupAdmin(boolean groupadmin)
	{
		m_groupadmin = groupadmin;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.tool.service.RWikiObject#setGroupread(boolean)
	 */
	public void setGroupRead(boolean groupread)
	{
		m_groupread = groupread;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.tool.service.RWikiObject#setGroupwrite(boolean)
	 */
	public void setGroupWrite(boolean groupwrite)
	{
		m_groupwrite = groupwrite;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.tool.service.RWikiObject#setPublicread(boolean)
	 */
	public void setPublicRead(boolean publicread)
	{
		m_publicread = publicread;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.tool.service.RWikiObject#setPublicwrite(boolean)
	 */
	public void setPublicWrite(boolean publicwrite)
	{
		m_publicwrite = publicwrite;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.tool.service.RWikiObject#setUser(java.lang.String)
	 */
	public void setUser(String user)
	{
		m_user = user;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.tool.service.RWikiObject#setUser(java.lang.String)
	 */
	public void setOwner(String owner)
	{
		m_owner = owner;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.tool.service.RWikiObject#setUseradmin(boolean)
	 */
	public void setOwnerAdmin(boolean useradmin)
	{
		m_owneradmin = useradmin;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.tool.service.RWikiObject#setUserread(boolean)
	 */
	public void setOwnerRead(boolean userread)
	{
		m_ownerread = userread;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.tool.service.RWikiObject#setUserwrite(boolean)
	 */
	public void setOwnerWrite(boolean userwrite)
	{
		m_ownerwrite = userwrite;

	}

	public void copyAllTo(RWikiObject rwo)
	{
		rwo.setName(this.getName());
		rwo.setOwner(this.getOwner());
		rwo.setRealm(this.getRealm());
		rwo.setRevision(this.getRevision());
		rwo.setUser(this.getUser());
		rwo.setVersion(this.getVersion());
		rwo.setSha1(this.getSha1());

		rwo.setContent(this.getContent());
		rwo.setGroupAdmin(this.getGroupAdmin());
		rwo.setGroupRead(this.getGroupRead());
		rwo.setGroupWrite(this.getGroupWrite());
		rwo.setPublicRead(this.getPublicRead());
		rwo.setPublicWrite(this.getPublicWrite());
		rwo.setReferenced(this.getReferenced());
		rwo.setOwnerAdmin(this.getOwnerAdmin());
		rwo.setOwnerRead(this.getOwnerRead());
		rwo.setOwnerWrite(this.getOwnerWrite());
	}

	public void copyTo(RWikiObject rwo)
	{
		rwo.setContent(this.getContent());
		rwo.setGroupAdmin(this.getGroupAdmin());
		rwo.setGroupRead(this.getGroupRead());
		rwo.setGroupWrite(this.getGroupWrite());
		rwo.setPublicRead(this.getPublicRead());
		rwo.setPublicWrite(this.getPublicWrite());
		rwo.setReferenced(this.getReferenced());
		rwo.setOwnerAdmin(this.getOwnerAdmin());
		rwo.setOwnerRead(this.getOwnerRead());
		rwo.setOwnerWrite(this.getOwnerWrite());
		rwo.setSha1(this.getSha1());
	}

	protected String m_source = null;

	private String testContent;

	/**
	 * The name of the source used for loading the object content when injected
	 * 
	 * @param source
	 */
	public void setSource(String source)
	{
		m_source = source;
	}

	public String getSource()
	{
		return m_source;
	}

	public String toString()
	{
		return this.getClass().toString() + " ID:" + this.getId() + " Name: "
				+ this.getName();
	}

	/**
	 * @param content
	 * @returns true if the contents was updated
	 */
	/*
	 * iebdelete public boolean updateContent(String content) { if
	 * (content.equals(this.m_content)) { return false; } // Copy current object
	 * to History. RWikiHistoryObject newHistoryObject = new
	 * RWikiHistoryObjectImpl(); newHistoryObject.setContent(this.getContent());
	 * newHistoryObject.setVersion(this.getVersion());
	 * newHistoryObject.setUser(this.getUser()); List list = this.getHistory();
	 * if (list == null) { list = new ArrayList(); this.setHistory(list); }
	 * list.add(newHistoryObject); // finally set the new content
	 * this.setContent(content); return true; }
	 */

	/*
	 * public RWikiHistoryObject getRevision(int revision) { int
	 * numberOfRevisions = this.getNumberOfRevisions(); if (revision >= 0 &&
	 * revision < numberOfRevisions) { // This needs to be a finder return
	 * ((RWikiHistoryObject) this.getHistory().get(revision)); } else if
	 * (revision == numberOfRevisions) { RWikiHistoryObjectImpl mock = new
	 * RWikiHistoryObjectImpl(); mock.setContent(this.getContent());
	 * mock.setRevision(numberOfRevisions); mock.setUser(this.getUser());
	 * mock.setVersion(this.getVersion()); return mock; } else { throw new
	 * IllegalArgumentException( "Invalid version number: " + revision); } }
	 */
	/*
	 * public int getNumberOfRevisions() { // this needs to be a finder if
	 * (this.getHistory() != null) return this.getHistory().size(); return 0; }
	 */

	public void setPermissions(boolean[] permissions)
	{
		if (permissions.length != 8)
		{
			// yuck
			throw new IllegalArgumentException(
					"Must be given an array of length 8");
		}
		this.setOwnerRead(permissions[0]);
		this.setOwnerWrite(permissions[1]);
		this.setOwnerAdmin(permissions[2]);
		this.setGroupRead(permissions[3]);
		this.setGroupWrite(permissions[4]);
		this.setGroupAdmin(permissions[5]);
		this.setPublicRead(permissions[6]);
		this.setPublicWrite(permissions[7]);

	}

	public void setPermissions(RWikiPermissions permissions)
	{
		setOwnerRead(permissions.isOwnerRead());
		setOwnerWrite(permissions.isOwnerWrite());
		setOwnerAdmin(permissions.isOwnerAdmin());
		setGroupRead(permissions.isGroupRead());
		setGroupWrite(permissions.isGroupWrite());
		setGroupAdmin(permissions.isGroupAdmin());
		setPublicRead(permissions.isPublicRead());
		setPublicWrite(permissions.isPublicWrite());
	}

	public RWikiPermissions getPermissions()
	{
		RWikiPermissions permissions = new RWikiPermissionsImpl();
		permissions.setOwnerRead(getOwnerRead());
		permissions.setOwnerWrite(getOwnerWrite());
		permissions.setOwnerAdmin(getOwnerAdmin());
		permissions.setGroupRead(getGroupRead());
		permissions.setGroupWrite(getGroupWrite());
		permissions.setGroupAdmin(getGroupAdmin());
		permissions.setPublicRead(getPublicRead());
		permissions.setPublicWrite(getPublicWrite());
		return permissions;
	}

	public Integer getRevision()
	{
		return m_revision;
	}

	public void setRevision(Integer revision)
	{
		this.m_revision = revision;
		// SAK-2470
		if (m_revision == null) m_revision = Integer.valueOf(0);
	}

	/*
	 * Lazy loading of content.
	 */
	private RWikiObjectContentDao getRwikiObjectContentDao()
	{
		return codao;
	}

	public void setRwikiObjectContentDao(RWikiObjectContentDao codao)
	{
		this.codao = codao;
	}

	public RWikiObjectContent getRWikiObjectContent()
	{
		lazyLoadContentObject();
		return co;
	}

	public void setRWikiObjectContent(RWikiObjectContent co)
	{
		this.co = co;
	}

	private void lazyLoadContentObject()
	{
		if (codao == null)
		{
			// Exception ex = new RuntimeException("TRACE: Content Object DAO is
			// null");
			// log.info("Problem with loading Lazy Content, this is
			// Ok, just means lazyLoadContent was called by Hibernate");
			// log.error(ex.getMessage(), ex);
			return;
		}
		if (co == null)
		{
			co = codao.getContentObject(this);
			if (co == null)
			{
				co = codao.createContentObject(this);
			}
			// this will cause the Sha1 to be recomputed if its is not present
			// It MUST be done here, outside this if will generate recursion
		}
	}

	public void setContent(String content)
	{
		lazyLoadContentObject();
		if (content == null) content = "";
		if (co != null)
		{ // could be null if triggered during a hibernate
			// template load
			co.setContent(content);
			// recompute the Sha1
		}
		else
		{
			// only for testing content
			testContent = content;
		}
		sha1 = computeSha1(content);
	}

	public String getContent()
	{
		lazyLoadContentObject();

		String content = null;
		if (co != null)
		{
			content = co.getContent(); // could be null if triggerd during a
		}
		else
		{
			// only for testing
			content = testContent;
		}
		// template load
		if (content == null) content = "";
		return content;
	}

	public void setSha1(String sha1)
	{
		this.sha1 = sha1;
	}

	public String getSha1()
	{
		return sha1;
	}

	private String sha1;

	private static MessageDigest shatemplate = null;

	public static String computeSha1(String content)
	{
		String digest = "";
		try
		{
			if (shatemplate == null)
			{
				shatemplate = MessageDigest.getInstance("SHA");
			}

			MessageDigest shadigest = (MessageDigest) shatemplate.clone();
			byte[] bytedigest = shadigest.digest(content.getBytes("UTF8"));
			digest = byteArrayToHexStr(bytedigest);
		}
		catch (Exception ex)
		{
			log.error("Unable to create SHA hash of content", ex);
		}
		return digest;
	}

	private static String byteArrayToHexStr(byte[] data)
	{
		char[] chars = new char[data.length * 2];
		for (int i = 0; i < data.length; i++) 
		{
			byte current = data[i];
			int hi = (current & 0xF0) >> 4;
			int lo = current & 0x0F; 
			chars[2*i] =  (char) (hi < 10 ? ('0' + hi) : ('A' + hi - 10));
			chars[2*i+1] =  (char) (lo < 10 ? ('0' + lo) : ('A' + lo - 10));
		}
		return new String(chars);
	}

}

/*******************************************************************************
 * $Header$
 ******************************************************************************/
