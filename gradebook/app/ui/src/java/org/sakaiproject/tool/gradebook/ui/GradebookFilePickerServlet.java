/**
 * Copyright (c) 2003-2009 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * 
 */
package org.sakaiproject.tool.gradebook.ui;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.content.api.FilePickerHelper;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.jsf.util.HelperAwareJsfTool;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;

/**
 * @author bsawert
 *
 */
public class GradebookFilePickerServlet extends HelperAwareJsfTool {
	
  protected boolean sendToHelper(HttpServletRequest req, HttpServletResponse res, String target) {
	  ToolSession toolSession = SessionManager.getCurrentToolSession();
	  
      ToolSession session = SessionManager.getCurrentToolSession();
      if (session.getAttribute(FilePickerHelper.FILE_PICKER_CANCEL) == null &&
          session.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS) != null) {
    	  
    	  // attachments are References, which cannot be Serialized
    	  // get reference strings and store those instead
    	  
    	  List refs = (List) session.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
        
    	  if (refs != null && refs.size() > 0) {
	    	  // we are only processing a single reference - the first one
	    	  Reference ref = (Reference) refs.get(0);
	    	  
	    	  // save reference string
	    	  String refString = ref.getReference();
	    	  session.setAttribute(SpreadsheetUploadBean.PICKED_FILE_REFERENCE, refString);
	    	  
    		  // save display name
    		  ResourceProperties rp = ref.getProperties();
    		  String pickedFileDesc = (String) rp.get(rp.getNamePropDisplayName());
    		  session.setAttribute(SpreadsheetUploadBean.PICKED_FILE_DESC, pickedFileDesc);
    	  }
    	  
      	session.removeAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
    	session.removeAttribute(FilePickerHelper.FILE_PICKER_CANCEL);
      }
	  
	  return super.sendToHelper(req, res, target);
  }


}
