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
package uk.ac.cam.caret.sakai.rwiki.service.api;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.service.legacy.entity.Entity;
import org.sakaiproject.service.legacy.entity.Reference;
import org.sakaiproject.service.legacy.entity.ResourceProperties;

/**
 * An entity handler matches resource references and handles how those resource
 * references interact witht he Entity provider. One Entity provider may have
 * one or more Entity handlers. Entity Handlers are in general singletons, that
 * are thread safe and operate on oobjects passed in through the stack or thread
 * local.
 * 
 * @author ieb
 * 
 */
public interface EntityHandler {

	/**
	 * This sets the reference using this entity handler
	 * 
	 * @param majorType
	 *            The mojor type to bind this reference to
	 * @param ref
	 *            the reference object
	 * @param reference
	 *            the reference string
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
	 *            the entity object
	 * @return a url that represents the entity
	 */
	String getUrl(Entity entity);

	/**
	 * output the entity to the output stream
	 * 
	 * @param entity
	 *            the Entity to output
	 * @param rrequest
	 *            the servlet request object
	 * @param res
	 *            the servlet response object
	 */
	void outputContent(Entity entity, HttpServletRequest request,
			HttpServletResponse res);

	/**
	 * Gets the resource properties for the id
	 * 
	 * @param e
	 *            The entity
	 * @return
	 */
	ResourceProperties getProperties(Entity e);

	/**
	 * Gets the AuthZGroups associated with this entity
	 * 
	 * @param entity
	 * @return
	 */
	Collection getAuthzGroups(Reference entity);

	/**
	 * Generate a HTML link for the entity handler
	 * @param e
	 * @return
	 */
	String getHTML(Entity e);

}
