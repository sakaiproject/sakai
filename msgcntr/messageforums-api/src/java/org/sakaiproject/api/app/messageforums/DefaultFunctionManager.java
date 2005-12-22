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

/**
 * @author <a href="mailto:rshastri@iupui.edu">Rashmi Shastri</a>
 */
public interface DefaultFunctionManager // TODO: rename me : more like default setting manager or default permission manager
{
  public static final String FUNCTION_NEW_FORUM="messagecenter.newForum";
  public static final String FUNCTION_NEW_TOPIC="messagecenter.newTopic";
  public static final String FUNCTION_NEW_RESPONSE="messagecenter.newResponse";
  public static final String FUNCTION_NEW_RESPONSE_TO_RESPONSE="messagecenter.newResponseToResponse";
  public static final String FUNCTION_MOVE_POSTINGS="messagecenter.movePostings";
  public static final String FUNCTION_CHANGE_SETTINGS="messagecenter.changeSettings";
  public static final String FUNCTION_POST_TO_GRADEBOOK="messagecenter.postToGradebook";
 
  public static final String FUNCTION_READ="messagecenter.read";
  public static final String FUNCTION_REVISE_ANY="messagecenter.reviseAny";
  public static final String FUNCTION_REVISE_OWN="messagecenter.reviseOwn";
  public static final String FUNCTION_DELETE_ANY="messagecenter.deleteAny";
  public static final String FUNCTION_DELETE_OWN="messagecenter.deleteOwn";
  public static final String FUNCTION_MARK_AS_READ="messagecenter.markAsRead";
  
  
  // control permissions
  public boolean isNewForum(String role);

  public boolean isNewTopic(String role);

  public boolean isNewResponse(String role);

  public boolean isResponseToResponse(String role);

  public boolean isMovePostings(String role);

  public boolean isChangeSettings(String role);

  public boolean isPostToGradebook(String role);

  // message permissions
  public boolean isRead(String role);

  public boolean isReviseAny(String role);

  public boolean isReviseOwn(String role);

  public boolean isDeleteAny(String role);

  public boolean isDeleteOwn(String role);

  public boolean isMarkAsRead(String role);

}
