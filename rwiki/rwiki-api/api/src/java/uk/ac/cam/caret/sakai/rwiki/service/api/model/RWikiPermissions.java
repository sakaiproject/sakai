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

/**
 * A holder class for page permissions. It <b>DOES NOT</b> link back to the
 * RWikiObject
 * 
 * @author andrew
 */
// FIXME: Service
public interface RWikiPermissions
{
	/**
	 * does the page have Group admin ticked
	 * 
	 * @return
	 */
	public boolean isGroupAdmin();

	/**
	 * Set group admin on the page
	 * 
	 * @param groupAdmin
	 */
	public void setGroupAdmin(boolean groupAdmin);

	/**
	 * Is the group red page permission set
	 * 
	 * @return
	 */
	public boolean isGroupRead();

	public void setGroupRead(boolean groupRead);

	public boolean isGroupWrite();

	public void setGroupWrite(boolean groupWrite);

	public boolean isOwnerAdmin();

	public void setOwnerAdmin(boolean ownerAdmin);

	public boolean isOwnerRead();

	public void setOwnerRead(boolean ownerRead);

	public boolean isOwnerWrite();

	public void setOwnerWrite(boolean ownerWrite);

	public boolean isPublicRead();

	public void setPublicRead(boolean publicRead);

	public boolean isPublicWrite();

	public void setPublicWrite(boolean publicWrite);

}
