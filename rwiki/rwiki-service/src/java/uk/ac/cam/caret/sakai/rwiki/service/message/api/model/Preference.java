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
public interface Preference {

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
     * @return Returns the preference.
     */
    String getPreference();

    /**
     * @param preference The preference to set.
     */
    void setPreference(String preference);

    /**
     * @return Returns the user.
     */
    String getUserid();

    /**
     * @param user The user to set.
     */
    void setUserid(String user);
    
    /**
     * The context of the preference
     * @return
     */
	String getPrefcontext();
	/**
	 * The context of the perfernece
	 * @param prefcontext
	 */
	void setPrefcontext(String prefcontext);
	/**
	 * The type of the preference
	 * @return
	 */
	String getPreftype();
	/**
	 * The type of the preference
	 * @param preftype
	 */
	void setPreftype(String preftype);
    
   
}