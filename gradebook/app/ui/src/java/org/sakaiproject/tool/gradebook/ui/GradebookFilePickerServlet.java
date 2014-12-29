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
