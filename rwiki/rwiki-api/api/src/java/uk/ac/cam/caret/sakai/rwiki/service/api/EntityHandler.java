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

package uk.ac.cam.caret.sakai.rwiki.service.api;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;

/**
 * An entity handler matches resource references and handles how those resource
 * references interact witht he Entity provider. One Entity provider may have
 * one or more Entity handlers. Entity Handlers are in general singletons, that
 * are thread safe and operate on oobjects passed in through the stack or thread
 * local.
 * 
 * @author ieb
 */
public interface EntityHandler
{

	/**
	 * This sets the reference using this entity handler
	 * 
	 * @param majorType
	 *        The mojor type to bind this reference to
	 * @param ref
	 *        the reference object
	 * @param reference
	 *        the reference string
	 */
	void setReference(String majorType, Reference ref, String reference);

	/**
	 * Get the description of the Rwiki Object based on the inbound Entity
	 * object
	 * 
	 * @param entity
	 * @return the description of this entity
	 */
	String getDescription(Entity entity);

	/**
	 * Retruns true if the entity matches the reference
	 * 
	 * @param reference
	 * @return true if this entity handler matches this
	 */
	boolean matches(String reference);

	/**
	 * Get the revision based on the referenece string, if there is no version,
	 * then it is implementation specific what is returned (eg -1)
	 * 
	 * @param reference
	 * @return the revision number
	 */
	int getRevision(Reference reference);

	/**
	 * get a url for the entity
	 * 
	 * @param entity
	 *        the entity object
	 * @return a url that represents the entity
	 */
	String getUrl(Entity entity);

	/**
	 * output the entity to the output stream
	 * 
	 * @param entity
	 *        the Entity to output
	 * @param rrequest
	 *        the servlet request object
	 * @param res
	 *        the servlet response object
	 */
	void outputContent(Entity entity, Entity sidebar, HttpServletRequest request,
			HttpServletResponse res);

	/**
	 * Gets the resource properties for the id
	 * 
	 * @param e
	 *        The entity
	 * @return
	 */
	ResourceProperties getProperties(Entity e);

	/**
	 * Gets the AuthZGroups associated with this entity
	 * 
	 * @param entity
	 * @param userId
	 * @return
	 */
	Collection getAuthzGroups(Reference entity, String userId);

	/**
	 * Generate a HTML link for the entity handler
	 * 
	 * @param e
	 * @return
	 */
	String getHTML(Entity e);

}
