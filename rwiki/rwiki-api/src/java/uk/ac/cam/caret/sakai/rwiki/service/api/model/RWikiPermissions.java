/**********************************************************************************
 *
 * $Header$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2005 University of Cambridge
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
package uk.ac.cam.caret.sakai.rwiki.service.api.model;

/**
 * A holder class for page permissions. It <b>DOES NOT</b> link back to the
 * RWikiObject
 * @author andrew
 * 
 */
//FIXME: Service

public interface RWikiPermissions {
	/**
	 * does the page have Group admin ticked
	 * @return
	 */
	public boolean isGroupAdmin();

	/**
	 * Set group admin on the page
	 * @param groupAdmin
	 */
	public void setGroupAdmin(boolean groupAdmin);

	/**
	 * Is the group red page permission set
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
