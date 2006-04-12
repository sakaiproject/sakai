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

package uk.ac.cam.caret.sakai.rwiki.service.message.api.model;

import java.util.Date;

/**
 * @author ieb
 *
 */
public interface PagePresence {

    /**
     * @return Returns the id.
     */
    String getId();

    /**
     * @param id The id to set.
     */
    void setId(String id);

    /**
     * @return Returns the lastseen.
     */
    Date getLastseen();

    /**
     * @param lastseen The lastseen to set.
     */
    void setLastseen(Date lastseen);

    /**
     * @return Returns the pagename.
     */
    String getPagename();

    /**
     * @param pagename The pagename to set.
     */
    void setPagename(String pagename);

    /**
     * @return Returns the pagespace.
     */
    String getPagespace();

    /**
     * @param pagespace The pagespace to set.
     */
    void setPagespace(String pagespace);

    /**
     * @return Returns the sessionid.
     */
    String getSessionid();

    /**
     * @param sessionid The sessionid to set.
     */
    void setSessionid(String sessionid);

    /**
     * @return Returns the user.
     */
    String getUser();

    /**
     * @param user The user to set.
     */
    void setUser(String user);

}