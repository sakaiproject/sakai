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
package uk.ac.cam.caret.sakai.rwiki.service.exception;

import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;

//FIXME: Service

public class ReadPermissionException extends PermissionException {

    /**
     * @see java.io.ObjectInputStream.resolveClass()
     */
    private static final long serialVersionUID = -3744459824034953929L;
    private String user;
    private RWikiObject rwikiObject;
    private String realm;

    public ReadPermissionException(String user, RWikiObject rwikiObject) {
        super("User: " + user + " cannot read RWikiObject " + rwikiObject);
        this.user = user;
        this.rwikiObject = rwikiObject;
        this.realm = rwikiObject.getRealm();
    }
    
    public ReadPermissionException(String user, RWikiObject rwikiObject, Throwable cause) {
        super("User: " + user + " cannot read RWikiObject " + rwikiObject ,cause);
        this.user = user;
        this.rwikiObject = rwikiObject;
        this.realm = rwikiObject.getRealm();
    }
    
    public ReadPermissionException(String user, String realm) {
        super("User: " + user + " is not permitted to read in realm " + realm);
        this.user = user;
        this.realm = realm;
        this.rwikiObject = null;
    }

    public RWikiObject getRWikiObject() {
        return rwikiObject;
    }

    public void setRWikiObject(RWikiObject rwikiObject) {
        this.rwikiObject = rwikiObject;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }
    
    
}
