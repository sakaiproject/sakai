package org.sakaiproject.scorm.ui.player.pages;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.sakaiproject.content.api.FilePickerHelper;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.ActiveToolManager;
import org.sakaiproject.tool.cover.SessionManager;

public class FilePickerPage extends BaseToolPage {
	private static final long serialVersionUID = 1L;
	
	public FilePickerPage() {
		add(new AjaxFallbackLink("sendToHelper") {

			@Override

			     public void onClick(AjaxRequestTarget target) {

			         HttpServletRequest req = getWebRequestCycle().getWebRequest().getHttpServletRequest();

			         HttpServletResponse res = getWebRequestCycle().getWebResponse().getHttpServletResponse();

			 

			         try {

			              sendToHelper(req, res);

			          } catch (Exception e) {

			              throw new RuntimeException(e);

			          }

			     }

			});
	}
	
	
	protected List sendToHelper(HttpServletRequest req, HttpServletResponse res) throws ToolException {

		ToolSession toolSession = SessionManager.getCurrentToolSession();
		
		List filePickerList = EntityManager.newReferenceList();
		toolSession.setAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS,
		    filePickerList);
		
		ActiveTool helperTool = ActiveToolManager.getActiveTool("sakai.filepicker");
		
		toolSession.setAttribute(helperTool.getId() + Tool.HELPER_DONE_URL,
		    req.getContextPath() + req.getServletPath());
		
		String toolContext = req.getContextPath() + req.getServletPath();
		String toolPath = "sakai.filepicker.helper/tool";
		
		helperTool.help(req, res, toolContext, toolPath);
		return filePickerList;
	}
	
	
	

}
