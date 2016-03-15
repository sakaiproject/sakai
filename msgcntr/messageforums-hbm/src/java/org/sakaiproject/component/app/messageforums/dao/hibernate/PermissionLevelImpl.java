/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-hbm/src/java/org/sakaiproject/component/app/messageforums/dao/hibernate/PermissionLevelImpl.java $
 * $Id: PermissionLevelImpl.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.component.app.messageforums.dao.hibernate;

import org.sakaiproject.api.app.messageforums.PermissionLevel;

public class PermissionLevelImpl extends MutableEntityImpl 
                                 implements PermissionLevel, Comparable, Cloneable{
					
	private String typeUuid;
	private String name;
	
	private Boolean changeSettings;
	private Boolean deleteAny;
	private Boolean deleteOwn;
	private Boolean markAsRead;
	private Boolean movePosting;	
	private Boolean newForum;
	private Boolean newResponse;
	private Boolean newResponseToResponse;
	private Boolean newTopic;				
	private Boolean postToGradebook;
	private Boolean read;
	private Boolean reviseAny;
	private Boolean reviseOwn;
	private Boolean moderatePostings;
	private Boolean identifyAnonAuthors;
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
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

	public Boolean getIdentifyAnonAuthors()
	{
		return identifyAnonAuthors;
	}

	public void setIdentifyAnonAuthors(Boolean identifyAnonAuthors)
	{
		this.identifyAnonAuthors = identifyAnonAuthors;
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

	public Boolean getPostToGradebook() {
		return postToGradebook;
	}

	public void setPostToGradebook(Boolean postToGradebook) {
		this.postToGradebook = postToGradebook;
	}

	public Boolean getRead() {
		return read;
	}

	public void setRead(Boolean read) {
		this.read = read;
	}

	public Boolean getNewResponseToResponse() {
		return newResponseToResponse;
	}

	public void setNewResponseToResponse(Boolean newResponseToResponse) {
		this.newResponseToResponse = newResponseToResponse;
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
 
	public int compareTo(Object obj) {
		
		PermissionLevelImpl pli = (PermissionLevelImpl) obj;				
		return (name == null) ? 0 : name.compareTo(pli.getName());		
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {		
		StringBuilder buffer = new StringBuilder("[");
		buffer.append(changeSettings);		
		buffer.append(",").append(markAsRead);
		//buffer.append(",").append(movePosting);
		buffer.append(",").append(newForum);
		buffer.append(",").append(newResponse);
		buffer.append(",").append(newResponseToResponse);
		buffer.append(",").append(newTopic);
		buffer.append(",").append(postToGradebook);
		buffer.append(",").append(read);
		buffer.append(",").append(reviseAny);
		buffer.append(",").append(reviseOwn);
		buffer.append(",").append(moderatePostings);
		buffer.append(",").append(identifyAnonAuthors);
		buffer.append(",").append(deleteAny);
		buffer.append(",").append(deleteOwn);
		buffer.append("]");
		
		return buffer.toString();
	}

	/**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object o)
  {        
    if (o == this){
      return true;
    }
        
    if (!(o instanceof PermissionLevelImpl))
      return false;
    
    PermissionLevelImpl obj = (PermissionLevelImpl) o;
        
    boolean returnValue = true;

    // Check the equality of each permission; if any permission is not equal, return false immediately; otherwise continue to the next permission
    Boolean bThis = this.getChangeSettings();         
    returnValue = (bThis == null) ? obj.getChangeSettings() == null : bThis.equals(obj.getChangeSettings()); 
    if(!returnValue)
    	return returnValue;
	bThis = this.getDeleteAny();
    returnValue = (bThis == null) ? obj.getDeleteAny() == null : bThis.equals(obj.getDeleteAny());
    if(!returnValue)
    	return returnValue;
	bThis = this.getDeleteOwn();         
    returnValue = (bThis == null) ? obj.getDeleteOwn() == null : bThis.equals(obj.getDeleteOwn());
    if(!returnValue)
    	return returnValue;
	bThis = this.getMarkAsRead();         
    returnValue = (bThis == null) ? obj.getMarkAsRead() == null : bThis.equals(obj.getMarkAsRead());
    if(!returnValue)
    	return returnValue;
	bThis = this.getMovePosting();         
    returnValue = (bThis == null) ? obj.getMovePosting() == null : bThis.equals(obj.getMovePosting());
    if(!returnValue)
    	return returnValue;
	bThis = this.getNewForum();         
    returnValue = (bThis == null) ? obj.getNewForum() == null : bThis.equals(obj.getNewForum());
    if(!returnValue)
    	return returnValue;
	bThis = this.getNewResponse();         
    returnValue = (bThis == null) ? obj.getNewResponse() == null : bThis.equals(obj.getNewResponse());
    if(!returnValue)
    	return returnValue;
	bThis = this.getNewResponseToResponse();         
    returnValue = (bThis == null) ? obj.getNewResponseToResponse() == null : bThis.equals(obj.getNewResponseToResponse());
    if(!returnValue)
    	return returnValue;
	bThis = this.getNewTopic();         
    returnValue = (bThis == null) ? obj.getNewTopic() == null : bThis.equals(obj.getNewTopic());
    if(!returnValue)
    	return returnValue;
	bThis = this.getPostToGradebook();         
    returnValue = (bThis == null) ? obj.getPostToGradebook() == null : bThis.equals(obj.getPostToGradebook());
    if(!returnValue)
    	return returnValue;
	bThis = this.getRead();         
    returnValue = (bThis == null) ? obj.getRead() == null : bThis.equals(obj.getRead());
    if(!returnValue)
    	return returnValue;
	bThis = this.getReviseAny();         
    returnValue = (bThis == null) ? obj.getReviseAny() == null : bThis.equals(obj.getReviseAny());
    if(!returnValue)
    	return returnValue;
	bThis = this.getReviseOwn();
    returnValue = (bThis == null) ? obj.getReviseOwn() == null : bThis.equals(obj.getReviseOwn());
    if(!returnValue)
    	return returnValue;
	bThis = this.getModeratePostings();         
    returnValue = (bThis == null) ? obj.getModeratePostings() == null : bThis.equals(obj.getModeratePostings());
    if(!returnValue)
    	return returnValue;
	bThis = this.getIdentifyAnonAuthors();         
    returnValue = (bThis == null) ? obj.getIdentifyAnonAuthors() == null : bThis.equals(obj.getIdentifyAnonAuthors());
    if(!returnValue)
    {
        return returnValue;
    }
	
    return returnValue;   
  }
  
  
  /**
   * @see java.lang.Object#hashCode()
   */
  public int hashCode()
  {
    int result = 17;
    
    try{
    		Boolean bThis = this.getChangeSettings();         
    		int temp = (bThis == null) ? 0 : bThis.hashCode();
    		result = result + temp;
    		bThis = this.getDeleteAny();         
    		temp = (bThis == null) ? 0 : bThis.hashCode();
    		result = result + temp;
    		bThis = this.getDeleteOwn();         
    		temp = (bThis == null) ? 0 : bThis.hashCode();
    		result = result + temp;
    		bThis = this.getMarkAsRead();         
    		temp = (bThis == null) ? 0 : bThis.hashCode();
    		result = result + temp;
    		bThis = this.getMovePosting();         
    		temp = (bThis == null) ? 0 : bThis.hashCode();
    		result = result + temp;
    		bThis = this.getNewForum();         
    		temp = (bThis == null) ? 0 : bThis.hashCode();
    		result = result + temp;
    		bThis = this.getNewResponse();         
    		temp = (bThis == null) ? 0 : bThis.hashCode();
    		result = result + temp;
    		bThis = this.getNewResponseToResponse();         
    		temp = (bThis == null) ? 0 : bThis.hashCode();
    		result = result + temp;
    		bThis = this.getNewTopic();         
    		temp = (bThis == null) ? 0 : bThis.hashCode();
    		result = result + temp;
    		bThis = this.getPostToGradebook();         
    		temp = (bThis == null) ? 0 : bThis.hashCode();
    		result = result + temp;
    		bThis = this.getRead();         
    		temp = (bThis == null) ? 0 : bThis.hashCode();
    		result = result + temp;
    		bThis = this.getReviseAny();         
    		temp = (bThis == null) ? 0 : bThis.hashCode();
    		result = result + temp;
    		bThis = this.getReviseOwn();         
    		temp = (bThis == null) ? 0 : bThis.hashCode();
    		result = result + temp;
    		bThis = this.getModeratePostings();         
    		temp = (bThis == null) ? 0 : bThis.hashCode();
    		result = result + temp;
    		bThis = this.getIdentifyAnonAuthors();         
    		temp = (bThis == null) ? 0 : bThis.hashCode();
    		result = result + temp;
    }
    catch(Exception e){
    	throw new RuntimeException(e);
    }
        
    return result;    
  }
  
	public PermissionLevel clone()
	{
		PermissionLevelImpl pli = new PermissionLevelImpl();
		pli.setChangeSettings(this.getChangeSettings());
		pli.setCreated(this.getCreated());
		pli.setCreatedBy(this.getCreatedBy());
		pli.setDeleteAny(this.getDeleteAny());
		pli.setDeleteOwn(this.getDeleteOwn());
		pli.setId(this.getId());
		pli.setMarkAsRead(this.getMarkAsRead());
		pli.setModeratePostings(this.getModeratePostings());
		pli.setIdentifyAnonAuthors(this.getIdentifyAnonAuthors());
		pli.setModified(this.getModified());
		pli.setModifiedBy(this.getModifiedBy());
		pli.setMovePosting(this.getMovePosting());
		pli.setName(this.getName());
		pli.setNewForum(this.getNewForum());
		pli.setNewResponse(this.getNewResponse());
		pli.setNewResponseToResponse(this.getNewResponseToResponse());
		pli.setNewTopic(this.getNewTopic());
		pli.setPostToGradebook(this.getPostToGradebook());
		pli.setRead(this.getRead());
		pli.setReviseAny(this.getReviseAny());
		pli.setReviseOwn(this.getReviseOwn());
		pli.setTypeUuid(this.getTypeUuid());
		pli.setUuid(this.getUuid());
		pli.setVersion(this.getVersion());
		
		return pli;
	}
	
}
