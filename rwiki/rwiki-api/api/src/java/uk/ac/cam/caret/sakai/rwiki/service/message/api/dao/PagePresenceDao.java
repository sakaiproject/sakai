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

package uk.ac.cam.caret.sakai.rwiki.service.message.api.dao;

import java.util.List;

import uk.ac.cam.caret.sakai.rwiki.service.message.api.model.PagePresence;

/**
 * @author ieb
 */
public interface PagePresenceDao
{
	PagePresence createPagePresence(String pageName, String pageSpace,
			String sessionid, String user);

	List findBySpace(String pageSpace);

	List findByPage(String pageSpace, String pageName);

	List findByUser(String user);

	PagePresence findBySession(String sessionid);

	/**
	 * @param pp
	 */
	void update(Object o);

	/**
	 * @param pageSpace
	 * @param pageName
	 * @return
	 */
	List findBySpaceOnly(String pageSpace, String pageName);
}
