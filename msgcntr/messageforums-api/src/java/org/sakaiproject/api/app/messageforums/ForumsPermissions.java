/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
 *                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
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

package org.sakaiproject.api.app.messageforums;

public interface ForumsPermissions {

    // Actor Permissions
    public boolean isUserAccessor();

    public boolean isUserContributor();

    public boolean isUserModerator();

    // Control Permissions
    public boolean isUserAbleToPostNewTopic(String uuid);

    public boolean isUserAbleToPostNewResponse(String uuid);

    public boolean isUserAbleToPostResponseToResponse(String uuid);

    public boolean isUserAbleToMovePostings(String uuid);

    public boolean isUserAbleToChangeControlSettings(String uuid);

    // Message Permissions
    public boolean isUserAbleToRead(String uuid);

    public boolean isUserAbleToReviseAny(String uuid);

    public boolean isUserAbleToReviseOwn(String uuid);

    public boolean isUserAbleToDeleteAny(String uuid);

    public boolean isUserAbleToDeleteOwn(String uuid);

    public boolean isUserAbleToReadDrafts(String uuid);

}