/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/



package org.sakaiproject.tool.assessment.ui.servlet.delivery;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.data.dao.grading.MediaData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.ui.bean.shared.PersonBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import java.io.*;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class ShowMediaServlet extends HttpServlet
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 2203681863823855810L;
private static Log log = LogFactory.getLog(ShowMediaServlet.class);

  public ShowMediaServlet()
  {
  }

  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException
  {
    doPost(req,res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException
  {
    // get media
    String mediaId = req.getParameter("mediaId");
    if (mediaId == null || mediaId.equals("")) {
    	return;
    }
    GradingService gradingService = new GradingService();
    MediaData mediaData = gradingService.getMedia(mediaId);
    String mediaLocation = mediaData.getLocation();
    int fileSize = mediaData.getFileSize().intValue();
    log.info("****1. media file size="+fileSize);

    //if setMimeType="false" in query string, implies, we want to do a forced download
    //in this case, we set contentType='application/octet-stream'
    String setMimeType = req.getParameter("setMimeType");
    log.info("****2. setMimeType="+setMimeType);

    // get assessment's ownerId
    // String assessmentCreatedBy = req.getParameter("createdBy");

    // who can access the media? You can,
    // a. if you are the creator.
    // b. if you have a assessment.grade.any or assessment.grade.own permission
    boolean accessDenied = true;
    String agentIdString = getAgentString(req, res);
    String currentSiteId="";
    boolean isAudio = false;
    Long assessmentGradingId = mediaData.getItemGradingData().getAssessmentGradingId();
    PublishedAssessmentIfc pub = gradingService.getPublishedAssessmentByAssessmentGradingId(assessmentGradingId.toString()); 
    if (pub!=null){
      PublishedAssessmentService service = new PublishedAssessmentService();
      currentSiteId = service.getPublishedAssessmentOwner(pub.getPublishedAssessmentId());
    }
    Long typeId = gradingService.getTypeId(mediaData.getItemGradingData().getItemGradingId());
    if (typeId.equals(TypeIfc.AUDIO_RECORDING)) {
    	isAudio = true;
    }

    // some log checking
    //log.debug("agentIdString ="+agentIdString);
    //log.debug("****current site Id ="+currentSiteId);
    
    // We only need to verify the Previleage if we display the media as a hyperlink
    // If we display them in line, the previleage has been checked during rendering
    // For SAK-6294, we want to display audio player in line. So we set isAudio to true above
    // and skip the privilege checking
    boolean hasPrivilege = agentIdString !=null &&
    	(agentIdString.equals(mediaData.getCreatedBy()) // user is creator
    	 || canGrade(req, res, agentIdString, currentSiteId));
    if (hasPrivilege || isAudio) {
      accessDenied = false;
    }
    if (accessDenied){
      String path = "/jsf/delivery/mediaAccessDenied.faces";
      RequestDispatcher dispatcher = req.getRequestDispatcher(path);
      dispatcher.forward(req, res);
    }
    else {
      String displayType="inline";
      if (mediaData.getMimeType()!=null && !(setMimeType!=null && ("false").equals(setMimeType))){
        res.setContentType(mediaData.getMimeType());
      }
      else {
        displayType="attachment";
        res.setContentType("application/octet-stream");
      }
      log.debug("****"+displayType+";filename=\""+mediaData.getFilename()+"\";");
      res.setHeader("Content-Disposition", displayType+";filename=\""+mediaData.getFilename()+"\";");

      //** note that res.setContentType() must be called before res.getOutputStream(). see javadoc on this
      FileInputStream inputStream = null;
      BufferedInputStream buf_inputStream = null;
      ServletOutputStream outputStream = res.getOutputStream();
      BufferedOutputStream buf_outputStream = null;
      ByteArrayInputStream byteArrayInputStream = null;
      if (mediaLocation == null || (mediaLocation.trim()).equals("")){
        try{
          byte[] media = mediaData.getMedia();
          byteArrayInputStream = new ByteArrayInputStream(media);
          buf_inputStream = new BufferedInputStream(byteArrayInputStream);
          log.debug("**** media.length="+media.length);
        }
        catch(Exception e){
          log.error("****empty media save to DB="+e.getMessage());
        }
      }
      else{
    	  try{
    		  inputStream = getFileStream(mediaLocation);
    		  buf_inputStream = new BufferedInputStream(inputStream);
    	  }
    	  catch(Exception e){
    		  log.error("****empty media save to file ="+e.getMessage());
    	  }

      }

      int count=0;
      try{
    	  
    	  buf_outputStream = new BufferedOutputStream(outputStream);
        int i=0;
        if (buf_inputStream != null)  {
        while ( (i=buf_inputStream.read()) != -1){
            //System.out.print(i);
            buf_outputStream.write(i);
            count++;
          }
        }
        log.debug("**** mediaLocation="+mediaLocation);
        res.setContentLength(count);
        res.flushBuffer();
      }
      catch(Exception e){
        log.warn(e.getMessage());
      }
      finally {
    	  if (buf_outputStream != null) {
			  try {
				  buf_outputStream.close();
			  }
			  catch(IOException e) {
				  log.error(e.getMessage());
			  }
    	  }
    	  if (buf_inputStream != null) {
			  try {
				  buf_inputStream.close();
			  }
			  catch(IOException e) {
				  log.error(e.getMessage());
			  }
    	  }
          if (inputStream != null) {
			  try {
				  inputStream.close();
			  }
			  catch(IOException e) {
				  log.error(e.getMessage());
			  }
          }
          if (outputStream != null) {
			  try {
				  outputStream.close();
			  }
			  catch(IOException e) {
				  log.error(e.getMessage());
			  }
          }
          if (byteArrayInputStream != null) {
			  try {
				  byteArrayInputStream.close();
			  }
			  catch(IOException e) {
				  log.error(e.getMessage());
			  }
          }
      }
    }
  }

  private FileInputStream getFileStream(String mediaLocation){
    FileInputStream inputStream=null;
    try{
      File media=new File(mediaLocation);
      inputStream = new FileInputStream(media);
    }
    catch (FileNotFoundException ex) {
      log.warn("file not found="+ex.getMessage());
    }
    return inputStream;
  }

  public String getAgentString(HttpServletRequest req,  HttpServletResponse res){ 
      //String agentIdString = req.getRemoteUser();
    String agentIdString = AgentFacade.getAgentString();
    if (agentIdString == null || agentIdString.equals("")){ // try this
      PersonBean person = (PersonBean) ContextUtil.lookupBeanFromExternalServlet(
			   "person", req, res);
      agentIdString = person.getAnonymousId();
    }
    return agentIdString;
  }

  public boolean canGrade(HttpServletRequest req,  HttpServletResponse res,
                          String agentId, String currentSiteId){
    boolean hasPrivilege_any = hasPrivilege(req, "grade_any_assessment", currentSiteId);
    boolean hasPrivilege_own = hasPrivilege(req, "grade_own_assessment", currentSiteId);
    log.debug("hasPrivilege_any="+hasPrivilege_any);
    log.debug("hasPrivilege_own="+hasPrivilege_own);
    boolean hasPrivilege = (hasPrivilege_any || hasPrivilege_own);
    return hasPrivilege;    

  }


  public boolean isOwner(String agentId, String ownerId){
    boolean isOwner = false;
    isOwner = agentId.equals(ownerId);
    return isOwner;
  }
  
  public boolean hasPrivilege(HttpServletRequest req, String functionKey, String context){
	  String functionName=(String)ContextUtil.getLocalizedString(req,"org.sakaiproject.tool.assessment.bundle.AuthzPermissions", functionKey);
	  boolean privilege = SecurityService.unlock(functionName, "/site/"+context);
	  return privilege;
  }
}
