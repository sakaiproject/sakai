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

package org.sakaiproject.search.api;

import java.util.List;

import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.Notification;

/**
 * A SearchIndexBuilder builds a search index, it must manage its own list of
 * pending documents and should probably do this in a seperate thread
 * 
 * @author ieb
 */
public interface SearchIndexBuilder
{

	/**
	 * Adds a resource to the index builder
	 * 
	 * @param notification
	 * @param event
	 */
	void addResource(Notification notification, Event event);

	/**
	 * EntityProducers that want their content indexed on full text must
	 * register an EntityContentProducer with the SearchIndexBuilder
	 * 
	 * @param ecp
	 */
	void registerEntityContentProducer(EntityContentProducer ecp);

	/**
	 * Refresh the index based on the registered entities
	 */
	void refreshIndex();

	/**
	 * rebuild the index completely from scratch
	 */
	void rebuildIndex();

	/**
	 * Does the Queue contain work to do.
	 * 
	 * @return
	 */
	boolean isBuildQueueEmpty();

	/**
	 * get all the producers registerd, as a clone to avoid concurrent
	 * modification exceptions
	 * 
	 * @return
	 */
	List getContentProducers();

	/**
	 * Close down the entire search infrastructure
	 */
	void destroy();

	/**
	 * get the number of pending documents
	 * 
	 * @return
	 */
	int getPendingDocuments();

}
