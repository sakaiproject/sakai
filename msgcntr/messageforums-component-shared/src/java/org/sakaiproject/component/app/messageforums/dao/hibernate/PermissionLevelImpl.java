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

package org.sakaiproject.component.app.messageforums.dao.hibernate;

import org.sakaiproject.api.app.messageforums.PermissionLevel;


public class PermissionLevelImpl extends MutableEntityImpl implements PermissionLevel{
					
	private String typeUuid;

	private Boolean newForum;
	private Boolean newTopic;
	private Boolean newResponse;
	private Boolean responseToResponse;
	private Boolean movePosting;
	private Boolean changeSettings;
	private Boolean postGrades;
	private Boolean read;
	private Boolean markAsRead;
	private Boolean moderatePostings;
	private Boolean deleteOwn;
	private Boolean deleteAny;
	private Boolean reviseOwn;
	private Boolean reviseAny;
	
	public String getTypeUuid() {
		return typeUuid;
	}
	
	public void setTypeUuid(String typeUuid) {
		this.typeUuid = typeUuid;
	}

	public Boolean getChangeSettings() {
		return changeSettings;
	}

	public void setChangeSettings(Boolean changeSettings) {
		this.changeSettings = changeSettings;
	}

	public Boolean getDeleteAny() {
		return deleteAny;
	}

	public void setDeleteAny(Boolean deleteAny) {
		this.deleteAny = deleteAny;
	}

	public Boolean getDeleteOwn() {
		return deleteOwn;
	}

	public void setDeleteOwn(Boolean deleteOwn) {
		this.deleteOwn = deleteOwn;
	}

	public Boolean getMarkAsRead() {
		return markAsRead;
	}

	public void setMarkAsRead(Boolean markAsRead) {
		this.markAsRead = markAsRead;
	}

	public Boolean getModeratePostings() {
		return moderatePostings;
	}

	public void setModeratePostings(Boolean moderatePostings) {
		this.moderatePostings = moderatePostings;
	}

	public Boolean getMovePosting() {
		return movePosting;
	}

	public void setMovePosting(Boolean movePosting) {
		this.movePosting = movePosting;
	}

	public Boolean getNewForum() {
		return newForum;
	}

	public void setNewForum(Boolean newForum) {
		this.newForum = newForum;
	}

	public Boolean getNewResponse() {
		return newResponse;
	}

	public void setNewResponse(Boolean newResponse) {
		this.newResponse = newResponse;
	}

	public Boolean getNewTopic() {
		return newTopic;
	}

	public void setNewTopic(Boolean newTopic) {
		this.newTopic = newTopic;
	}

	public Boolean getPostGrades() {
		return postGrades;
	}

	public void setPostGrades(Boolean postGrades) {
		this.postGrades = postGrades;
	}

	public Boolean getRead() {
		return read;
	}

	public void setRead(Boolean read) {
		this.read = read;
	}

	public Boolean getResponseToResponse() {
		return responseToResponse;
	}

	public void setResponseToResponse(Boolean responseToResponse) {
		this.responseToResponse = responseToResponse;
	}

	public Boolean getReviseAny() {
		return reviseAny;
	}

	public void setReviseAny(Boolean reviseAny) {
		this.reviseAny = reviseAny;
	}

	public Boolean getReviseOwn() {
		return reviseOwn;
	}

	public void setReviseOwn(Boolean reviseOwn) {
		this.reviseOwn = reviseOwn;
	}
	
}
