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


package uk.ac.cam.caret.sakai.rwiki.service.api.model;

import java.util.Date;

/**
 * The base object for a Wiki page, id, name, realm, permissions, revision,
 * version, owner
 */
// FIXME: Service
public interface RWikiObject extends Comparable
{
	/**
	 * Access the unique id.
	 * 
	 * @return The unique id.
	 */
	String getId();

	/**
	 * Set the ID, ONLY do this if you really know you must.
	 * 
	 * @return
	 */
	void setId(String id);

	/**
	 * Access the object's version time stamp.
	 * 
	 * @return The object's version time stamp.
	 */
	Date getVersion();

	/**
	 * Set the "version"
	 * 
	 * @param version
	 *        The new "version".
	 */
	void setVersion(Date version);

	/**
	 * Access "name".
	 * 
	 * @return "name".
	 */
	String getName();

	/**
	 * Set the "name".
	 * 
	 * @param name
	 *        The new "name".
	 */
	void setName(String name);

	/**
	 * Access "rank".
	 * 
	 * @return "rank".
	 */
	String getRealm();

	/**
	 * Set the "rank".
	 * 
	 * @param rank
	 *        The new "rank".
	 */
	void setRealm(String rank);

	/**
	 * Access "serial number".
	 * 
	 * @return "serial number".
	 */
	String getContent();

	/**
	 * a list of referenced pages seperated by ::
	 * 
	 * @return
	 */
	String getReferenced();

	/**
	 * a list of referenced pages seperated by ::
	 */
	void setReferenced(String referenced);

	/**
	 * Set the "serial number".
	 * 
	 * @param sn
	 *        The new "serial number".
	 */
	void setContent(String sn);

	/**
	 * The user that last edited this record
	 * 
	 * @param user
	 */
	void setUser(String user);

	/**
	 * @return The user that last edited this record
	 * @return
	 */
	String getUser();

	/**
	 * The user that owns this record
	 */
	void setOwner(String owner);

	/**
	 * @return The user that owns this record
	 */
	String getOwner();

	/**
	 * Can the user read the page
	 * 
	 * @return
	 */
	boolean getOwnerRead();

	/**
	 * set what the owner can do to the page, requires admin for the user
	 * 
	 * @param userRead
	 */
	void setOwnerRead(boolean ownerRead);

	/**
	 * can the user edit the page
	 * 
	 * @return
	 */
	boolean getOwnerWrite();

	/**
	 * set the user edit permission
	 * 
	 * @param userWrite
	 */
	void setOwnerWrite(boolean ownerWrite);

	/**
	 * can the user admin the page (set other permissions)
	 * 
	 * @return
	 */
	boolean getOwnerAdmin();

	/**
	 * Can the user set admin permissions, (set other permissions, including
	 * this one, potential lock out so we need a higher level admin )
	 * 
	 * @param userAdmin
	 */
	void setOwnerAdmin(boolean ownerAdmin);

	/**
	 * can the group read the page
	 * 
	 * @return
	 */
	boolean getGroupRead();

	/**
	 * set the group permission
	 * 
	 * @param groupRead
	 */
	void setGroupRead(boolean groupRead);

	/**
	 * can the group edit the page
	 * 
	 * @return
	 */
	boolean getGroupWrite();

	/**
	 * set the group edit
	 * 
	 * @param groupWrite
	 */
	void setGroupWrite(boolean groupWrite);

	/**
	 * can the group admin the page (set other permissions)
	 * 
	 * @return
	 */
	boolean getGroupAdmin();

	/**
	 * set the group admin permissoins
	 * 
	 * @param groupAdmin
	 */
	void setGroupAdmin(boolean groupAdmin);

	/**
	 * Can the public read the page
	 * 
	 * @return
	 */
	boolean getPublicRead();

	/**
	 * set the public read permissions
	 * 
	 * @param publicRead
	 */
	void setPublicRead(boolean publicRead);

	/**
	 * can the public edit the page
	 * 
	 * @return
	 */
	boolean getPublicWrite();

	/**
	 * set the public edit permissions
	 * 
	 * @param publicWrite
	 */
	void setPublicWrite(boolean publicWrite);

	/**
	 * @return Returns the revision number
	 */
	Integer getRevision();

	/**
	 * 
	 */
	void setRevision(Integer revision);

	/**
	 * Copy a RWikiObject into an existing RWiki Object This will copy the
	 * contents and settings of the RWiki Object into the supplied RWikiObject.
	 * The ID and the User will not be changed, everything else will be coppied.
	 * NB. THE GLOBAL NAME, AND REALM WILL NOT BE COPIED, and should be changed
	 * by the calling method.
	 */
	void copyTo(RWikiObject rwo);

	/**
	 * Copies everything
	 * 
	 * @param rwo
	 */
	void copyAllTo(RWikiObject rwo);

	// /**
	// * Update content of the RWikiObject, taking care of history
	// * @return false if newContent is the same as the current content
	// */
	// iebdelete boolean updateContent(String newContent);

	// iebdelete RWikiHistoryObject getRevision(int revisionNumber);

	// iebdelete int getNumberOfRevisions();

	/**
	 * Update permissions, the permission array must be of length 8
	 */
	void setPermissions(RWikiPermissions permissions);

	/**
	 * get an object containing the current state of the permissions, this MUST
	 * be pushed back into the DAO for the changes to take effect. Changing the
	 * contents of RWikiPermissions is not sufficient.
	 * 
	 * @return
	 */
	RWikiPermissions getPermissions();

	/**
	 * The id of the rwiki object which this is a version of
	 * 
	 * @param rwikiobjectid
	 */
	void setRwikiobjectid(String rwikiobjectid);

	/**
	 * The parent rwiki object
	 * 
	 * @return
	 */
	String getRwikiobjectid();

	/**
	 * get the hash
	 * 
	 * @return
	 */
	String getSha1();

	/**
	 * set the hash.
	 * 
	 * @param sha1
	 */
	void setSha1(String sha1);

	/**
	 * @return
	 */
	String getSource();

}

/*******************************************************************************
 * $Header$
 ******************************************************************************/
