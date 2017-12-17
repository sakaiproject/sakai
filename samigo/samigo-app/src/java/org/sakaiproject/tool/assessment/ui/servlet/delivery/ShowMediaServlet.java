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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tool.assessment.ui.servlet.delivery;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;

import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.tool.assessment.data.dao.grading.MediaData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.ui.bean.shared.PersonBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id$
 */
@Slf4j
public class ShowMediaServlet extends HttpServlet
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 2203681863823855810L;
    private static final Pattern HTTP_RANGE_PATTERN = Pattern.compile("bytes=(?<start>\\d*)-(?<end>\\d*)");

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
	  String agentIdString = getAgentString(req, res);
	  if (agentIdString == null) {
		  String path = "/jsf/delivery/mediaAccessDenied.faces";
		  RequestDispatcher dispatcher = req.getRequestDispatcher(path);
		  dispatcher.forward(req, res);
		  return;
	  } 
	  String mediaId = req.getParameter("mediaId");
	  if (mediaId == null || mediaId.trim().equals("")) {
			  return;
	  }
	  
	// get media
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
    String currentSiteId="";
    boolean isAudio = false;
    Long assessmentGradingId = mediaData.getItemGradingData().getAssessmentGradingId();
    PublishedAssessmentIfc pub = gradingService.getPublishedAssessmentByAssessmentGradingId(assessmentGradingId.toString()); 
    if (pub!=null){
      PublishedAssessmentService service = new PublishedAssessmentService();
      currentSiteId = service.getPublishedAssessmentOwner(pub.getPublishedAssessmentId());
    }

    
    boolean hasPrivilege = agentIdString !=null &&
    	(agentIdString.equals(mediaData.getCreatedBy()) // user is creator
    	 || canGrade(req, res, agentIdString, currentSiteId));
    if (hasPrivilege) {
      accessDenied = false;
    }
    if (accessDenied){
      String path = "/jsf/delivery/mediaAccessDenied.faces";
      RequestDispatcher dispatcher = req.getRequestDispatcher(path);
      dispatcher.forward(req, res);
    }
    else {
      String displayType="inline";
      if (mediaData.getMimeType()!=null && !mediaData.getMimeType().equals("application/octet-stream") && !(setMimeType!=null && ("false").equals(setMimeType))){
          res.setContentType(mediaData.getMimeType());
      }
      else {
        displayType="attachment";
        res.setContentType("application/octet-stream");
      }
      log.debug("****"+displayType+";filename=\""+mediaData.getFilename()+"\";");

      res.setHeader("Content-Disposition", displayType+";filename=\""+mediaData.getFilename()+"\";");
      
      int start = 0;
      int end = fileSize - 1;
      int rangeContentLength = end - start + 1;
      
      String range = req.getHeader("Range");
      
      if (StringUtils.isNotBlank(range)) {
	      Matcher matcher = HTTP_RANGE_PATTERN.matcher(range);
	       
	      if (matcher.matches()) {
	        String startMatch = matcher.group(1);
	        start = startMatch.isEmpty() ? start : Integer.valueOf(startMatch);
	        start = start < 0 ? 0 : start;
	   
	        String endMatch = matcher.group(2);
	        end = endMatch.isEmpty() ? end : Integer.valueOf(endMatch);
	        end = end > fileSize - 1 ? fileSize - 1 : end;
	        
	        rangeContentLength = end - start + 1;
	      }
	   
	      res.setHeader("Content-Range", String.format("bytes %s-%s/%s", start, end, fileSize));
	      res.setHeader("Content-Length", String.format("%s", rangeContentLength));
	      res.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
      }
      else {
    	  res.setContentLength(fileSize);
      }
      
      

      //** note that res.setContentType() must be called before res.getOutputStream(). see javadoc on this
      FileInputStream inputStream = null;
      BufferedInputStream buf_inputStream = null;
      ServletOutputStream outputStream = res.getOutputStream();
      BufferedOutputStream buf_outputStream = null;
      ByteArrayInputStream byteArrayInputStream = null;
      if (mediaLocation == null || (mediaLocation.trim()).equals("")){
        try{
          byteArrayInputStream = new ByteArrayInputStream(mediaData.getMedia());
          buf_inputStream = new BufferedInputStream(byteArrayInputStream);
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

      try{
    	  
    	  buf_outputStream = new BufferedOutputStream(outputStream);
        int i=0;
        if (buf_inputStream != null)  {
        	// skip to the start of the possible range request
        	buf_inputStream.skip(start); 
        	
        	int bytesLeft = rangeContentLength;
        	byte[] buffer = new byte[1024];

        	while ( (i = buf_inputStream.read(buffer)) != -1 && bytesLeft > 0){
        		buf_outputStream.write(buffer);
        		bytesLeft -= i;
        	}
        }
        log.debug("**** mediaLocation="+mediaLocation);
        
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
    boolean hasPrivilege_any = hasPrivilege(req, "assessment.gradeAssessment.any", currentSiteId);
    boolean hasPrivilege_own = hasPrivilege(req, "assessment.gradeAssessment.own", currentSiteId);
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
  
  public boolean hasPrivilege(HttpServletRequest req, String functionName, String context){
	  boolean privilege = SecurityService.unlock(functionName, "/site/"+context);
	  return privilege;
  }
}
