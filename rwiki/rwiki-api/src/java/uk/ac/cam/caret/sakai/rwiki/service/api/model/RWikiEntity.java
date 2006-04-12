/**********************************************************************************
*
* $Header$
*
***********************************************************************************
*
* Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
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

import org.sakaiproject.service.legacy.entity.Entity;
import org.w3c.dom.Element;

public interface RWikiEntity extends Entity {
	
	/**
	 * Get the Rwiki Object that the entiy represent
	 * @return
	 */
	
	RWikiObject getRWikiObject();
	
	/**
	 * Populates the Object using an XML block
	 * @param el
	 * @param defaultRealm
	 * @throws Exception
	 */
	void fromXml(Element el, String defaultRealm) throws Exception;
	
	
	public static final String RP_ID = "id";
	public static final String RP_OWNER = "owner";
	public static final String RP_REALM = "realm";
	public static final String RP_REFERENCED = "referenced";
	public static final String RP_RWID = "rwid";
	public static final String RP_SHA1 = "sha1";
	public static final String RP_USER = "user";
	public static final String RP_GROUP_ADMIN = "group-admin";
	public static final String RP_GROUP_READ = "group-read";
	public static final String RP_GROUP_WRITE = "group-write";
	public static final String RP_OWNER_ADMIN = "owner-admin";
	public static final String RP_OWNER_READ = "owner-read";
	public static final String RP_OWNER_WRITE = "owner-write";
	public static final String RP_PUBLIC_READ = "public-read";
	public static final String RP_PUBLIC_WRITE = "public-write";
	public static final String RP_REVISION = "revision";
	public static final String RP_VERSION = "version";
	public static final String RP_NAME = "name";
	public static final String RP_CONTAINER = "container";
	/**
	 * Is the entity a container ?
	 * @return
	 */
	boolean isContainer();


}
