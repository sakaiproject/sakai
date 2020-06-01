/**
 * $Id: ReportHandlerHook.java 52273 2008-08-21 14:28:00Z art27@cantab.net $
 * $URL: https://source.sakaiproject.org/contrib/evaluation/tags/1.3.0/tool/src/java/org/sakaiproject/evaluation/tool/reporting/ReportHandlerHook.java $
 * ReportHandlerHook.java - evaluation - 23 Jan 2007 11:35:56 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.lessonbuildertool.tool.beans;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.tool.view.ExportCCViewParameters;
import org.sakaiproject.tool.cover.ToolManager;

import lombok.extern.slf4j.Slf4j;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * Handles the generation of files for exporting results
 * 
 * @author Steven Githens
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
@Slf4j
public class ReportHandlerHook {
   private ViewParameters viewparams;
   public void setViewparams(ViewParameters viewparams) {
      this.viewparams = viewparams;
   }
   
   private HttpServletResponse response;
   public void setResponse(HttpServletResponse response) {
      this.response = response;
   }
   
   /* (non-Javadoc)
    * @see uk.org.ponder.rsf.processor.HandlerHook#handle()
    */
   public boolean handle() {
      if (viewparams instanceof ExportCCViewParameters) {
      ExportCCViewParameters parameters = ((ExportCCViewParameters) viewparams);
	  String siteId = ToolManager.getCurrentPlacement().getContext();
	  String ref = "/site/" + siteId;
	  boolean ok = SecurityService.unlock(SimplePage.PERMISSION_LESSONBUILDER_UPDATE, ref);
	  // In this context it's hard to report an error. However since the UI will
	  // never present this option unless the user has permission, anyone for whom
	  // this fails is deep in hack mode.
	  if (!ok) return false;

          try {
              response.sendRedirect("/lessonbuilder-tool/ccexport?" +
                      "version=" + parameters.getVersion() + "&" +
                      "bank=" + parameters.getBank() + "&" +
                      "siteid=" + siteId);
          } catch (IOException ioe) {
              log.warn("Could not send redirect, {}", ioe.toString());
              return false;
          }
          return true;
      }
      return false;
   }

}
