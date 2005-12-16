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

public interface UserPermissionManager {

    public boolean canRead(Topic topic, String typeId);
    public boolean canReviseAny(Topic topic, String typeId);
    public boolean canReviseOwn(Topic topic, String typeId);
    public boolean canDeleteAny(Topic topic, String typeId);
    public boolean canDeleteOwn(Topic topic, String typeId);
    public boolean canMarkAsRead(Topic topic, String typeId);

    public boolean canRead(BaseForum forum, String typeId);
    public boolean canReviseAny(BaseForum forum, String typeId);
    public boolean canReviseOwn(BaseForum forum, String typeId);
    public boolean canDeleteAny(BaseForum forum, String typeId);
    public boolean canDeleteOwn(BaseForum forum, String typeId);
    public boolean canMarkAsRead(BaseForum forum, String typeId);

    public boolean canRead(Area area, String typeId);
    public boolean canReviseAny(Area area, String typeId);
    public boolean canReviseOwn(Area area, String typeId);
    public boolean canDeleteAny(Area area, String typeId);
    public boolean canDeleteOwn(Area area, String typeId);
    public boolean canMarkAsRead(Area area, String typeId);

    
    public boolean canNewResponse(Topic topic, String typeId);
    public boolean canResponseToResponse(Topic topic, String typeId);
    public boolean canMovePostings(Topic topic, String typeId);
    public boolean canChangeSettings(Topic topic, String typeId);
    public boolean canPostToGradebook(Topic topic, String typeId);

    public boolean canNewTopic(BaseForum forum, String typeId);
    public boolean canNewResponse(BaseForum forum, String typeId);
    public boolean canResponseToResponse(BaseForum forum, String typeId);
    public boolean canMovePostings(BaseForum forum, String typeId);
    public boolean canChangeSettings(BaseForum forum, String typeId);
    public boolean canPostToGradebook(BaseForum forum, String typeId);

    public boolean canNewForum(Area area, String typeId);
    public boolean canNewTopic(Area area, String typeId);
    public boolean canNewResponse(Area area, String typeId);
    public boolean canResponseToResponse(Area area, String typeId);
    public boolean canMovePostings(Area area, String typeId);
    public boolean canChangeSettings(Area area, String typeId);
    public boolean canPostToGradebook(Area area, String typeId);

}
