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
package uk.ac.cam.caret.sakai.rwiki.service.api.dao;

import java.util.List;

import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiCurrentObject;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiHistoryObject;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;

// FIXME: Service

public interface RWikiHistoryObjectDao extends RWikiObjectDao
{

	/**
	 * Update a History Object
	 * 
	 * @param rwo
	 */
	void update(RWikiHistoryObject rwo);

	/**
	 * Create a new History object by cloning a Current Object
	 * 
	 * @param rwo
	 * @return
	 */
	RWikiHistoryObject createRWikiHistoryObject(RWikiCurrentObject rwo);

	/**
	 * Find a revision based ont the rwikiobjectid of the reference
	 * 
	 * @param rwo
	 * @param revision
	 * @return
	 */
	RWikiHistoryObject getRWikiHistoryObject(RWikiObject rwo, int revision);

	/**
	 * Find all revisions based on the rwikiobjectid of the reference
	 * 
	 * @param reference
	 * @return
	 */
	List findRWikiHistoryObjects(RWikiObject reference);

	/**
	 * Find all revisions based on the rwikiobjectid of the reference in reverse
	 * 
	 * @param reference
	 * @return
	 */
	List findRWikiHistoryObjectsInReverse(RWikiObject reference);

}
