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

import java.beans.PropertyDescriptor;

import org.apache.commons.beanutils.PropertyUtils;
import org.sakaiproject.api.app.messageforums.PermissionLevel;

public class PermissionLevelImpl extends MutableEntityImpl 
                                 implements PermissionLevel, Comparable{
					
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
		StringBuffer buffer = new StringBuffer("[");
		buffer.append(changeSettings);		
		//buffer.append("," + deleteAny);
		//buffer.append("," + deleteOwn);
		buffer.append("," + markAsRead);
		//buffer.append("," + movePosting);
		buffer.append("," + newForum);
		buffer.append("," + newResponse);
		buffer.append("," + newResponseToResponse);
		buffer.append("," + newTopic);
		buffer.append("," + postToGradebook);
		buffer.append("," + read);
		buffer.append("," + reviseAny);
		buffer.append("," + reviseOwn);
		//buffer.append("," + moderatePostings);
		buffer.append("]");
		
				
//		try{
//      PropertyDescriptor[] propDescriptors = PropertyUtils.getPropertyDescriptors(this);
//      for (int i = 0; i < propDescriptors.length; i++){
//    	  if (propDescriptors[i].getPropertyType().equals(Boolean.class)){
//          Boolean bThis = (Boolean) PropertyUtils.getProperty(this, propDescriptors[i].getName());          
//          buffer.append((bThis.booleanValue()) ? "true" : "false");
//          buffer.append(",");
//    	  }
//      }
//    }
//    catch(Exception e){
//    	throw new Error(e);
//    }
//    // replace comma with right brace
//    buffer.replace(buffer.length() - 1, buffer.length(), "]");   
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
    
    try{
      PropertyDescriptor[] propDescriptors = PropertyUtils.getPropertyDescriptors(this);
      for (int i = 0; i < propDescriptors.length; i++){
    	  if (propDescriptors[i].getPropertyType().equals(Boolean.class)){
          Boolean bThis = (Boolean) PropertyUtils.getProperty(this, propDescriptors[i].getName());
          Boolean bObj = (Boolean) PropertyUtils.getProperty(obj, propDescriptors[i].getName());
          boolean temp = (bThis == null) ? bObj == null : bThis.equals(bObj);
          if (!temp){  
          	returnValue = false;
          	break;
          }
    	  }
      }
    }
    catch(Exception e){
    	throw new Error(e);
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
      PropertyDescriptor[] propDescriptors = PropertyUtils.getPropertyDescriptors(this);
      for (int i = 0; i < propDescriptors.length; i++){
    	  if (propDescriptors[i].getPropertyType().equals(Boolean.class)){
          Boolean bThis = (Boolean) PropertyUtils.getProperty(this, propDescriptors[i].getName());          
          int temp = (bThis == null) ? 0 : bThis.hashCode();
          result = result + temp;
    	  }
      }
    }
    catch(Exception e){
    	throw new Error(e);
    }
        
    return result;    
  }
	
}
