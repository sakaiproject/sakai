/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
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

package org.sakaiproject.tool.gradebook.facades;

import org.sakaiproject.service.gradebook.shared.UnknownUserException;

/**
 * Facade to a user directory service provided by the framework.
 */
public interface UserDirectoryService {

    /**
     * Looks up a user's display name based on their uid.
     *
     * TODO See if there's some way to narrow this broad method. It's used only in two places:
     *
     * 1) When displaying a grade history log, it's used to obtain a grader's name based on their user UID.
     *    In this case, it couldn't be replaced by checking just people who play an explict part in the
     *    gradebook, since Authz may have let administrators change scores, or the grader may no longer play
     *    an active part. The only workaround is to pick up the currently authorized user's name and then
     *    store it as an additional field in the grade history log.
     * 2) In the student view, it's currently used to display the student's name. This case could be
     *    handled by supporting either a CourseManagement getEnrollmentForUserUid() method or an Authn
     *    getCurrentUserDisplayName() method.
     */
    public String getUserDisplayName(String userUid) throws UnknownUserException;
}

