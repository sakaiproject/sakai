/**********************************************************************************
 *
 * $Header$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2006 University of Cambridge
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

package org.sakaiproject.search;

import java.io.Reader;
import java.util.List;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.api.Event;

/**
 * This is a special class than handles ContentResources for the purposes of
 * search. This must be impelented in a thread safe way. The aim is to map the
 * content handler to the mime type
 * 
 * @author ieb
 */
public interface EntityContentProducer
{

	/**
	 * Should the consumer use the reader or is it Ok to use a memory copy of
	 * the content
	 * 
	 * @param cr
	 * @return
	 */
	boolean isContentFromReader(Entity cr);

	/**
	 * Get a reader for the supplied content resource
	 * 
	 * @param cr
	 * @return
	 */
	Reader getContentReader(Entity cr);

	/**
	 * Get the content as a string
	 * 
	 * @param cr
	 * @return
	 */
	String getContent(Entity cr);

	/**
	 * get the title for the content
	 * 
	 * @param cr
	 * @return
	 */
	String getTitle(Entity cr);

	/**
	 * Gets the url that displays the entity
	 * 
	 * @param entity
	 * @return
	 */
	String getUrl(Entity entity);

	/**
	 * If the reference matches this EntityContentProducer return true
	 * 
	 * @param ref
	 * @return
	 */
	boolean matches(Reference ref);

	/**
	 * Gets a list of Entity resource as a String to represent all indexable
	 * content
	 * 
	 * @return
	 */
	List getAllContent();

	Integer getAction(Event event);

	boolean matches(Event event);

	String getTool();

	String getSiteId(Reference ref);

}
