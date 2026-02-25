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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.text.Normalizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.tool.assessment.data.dao.grading.MediaData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
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

	private static final long serialVersionUID = 2203681863823855810L;
    private static final Pattern HTTP_RANGE_PATTERN = Pattern.compile("bytes=(?<start>\\d*)-(?<end>\\d*)");
    private static final ServerConfigurationService serverConfigurationService = ComponentManager.get(ServerConfigurationService.class);

  public ShowMediaServlet()
  {
  }

  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException
  {
    handleMediaRequest(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException
  {
    handleMediaRequest(req, res);
  }

  private void handleMediaRequest(HttpServletRequest req, HttpServletResponse res)
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

    String fileName = escapeInvalidCharsEntry(mediaData.getFilename());

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

    boolean hasPrivilege = StringUtils.equals(agentIdString, mediaData.getCreatedBy()) // user is creator
    	 || canGrade(req, res, agentIdString, currentSiteId);
    if (hasPrivilege) {
      accessDenied = false;
    }
    if (accessDenied){
      String path = "/jsf/delivery/mediaAccessDenied.faces";
      RequestDispatcher dispatcher = req.getRequestDispatcher(path);
      dispatcher.forward(req, res);
    }
    else {
      ContentResource cr = mediaData.getContentResource();
      String responseContentType = StringUtils.trimToNull(cr != null ? cr.getContentType() : null);
      if (StringUtils.isBlank(responseContentType)) {
          responseContentType = StringUtils.trimToNull(mediaData.getMimeType());
      }
      boolean unknownOrGenericContentType = StringUtils.isBlank(responseContentType)
              || StringUtils.equals(responseContentType, "application/octet-stream");
      boolean forceDownload = unknownOrGenericContentType || StringUtils.equals(setMimeType, "false");
      String displayType="inline";
      if (forceDownload) {
        displayType="attachment";
      }
      if (!unknownOrGenericContentType) {
        res.setContentType(responseContentType);
        res.setHeader("Content-Type", responseContentType);
        res.setHeader("X-Content-Type-Options", "nosniff");
      }
      log.debug("****"+displayType+";filename=\""+fileName+"\";");
      res.setHeader("Content-Disposition", displayType+";filename=\""+fileName+"\";");

      // See if we can bypass handling a large byte array
        try {
            URI directLink = AssessmentService.getContentHostingService().getDirectLinkToAsset(cr);
            if (directLink != null) {
                res.addHeader("Accept-Ranges", "none");
                if (serverConfigurationService.getBoolean("cloud.content.sendfile", false)) {
                    int hostLength = new String(directLink.getScheme() + "://" + directLink.getHost()).length();
                    String linkPath = "/sendfile" + directLink.toString().substring(hostLength);
                    log.debug("cloud.content.sendfile; path={}", linkPath);

                    // Nginx uses X-Accel-Redirect and Apache and others use X-Sendfile
                    res.addHeader("X-Accel-Redirect", linkPath);
                    res.addHeader("X-Sendfile", linkPath);
                    return;
                }
                else if (!forceDownload && serverConfigurationService.getBoolean("cloud.content.directurl", true)) {
                    log.debug("cloud.content.directurl; path={}", directLink.toString());
                    res.setContentLength(0);
                    res.sendRedirect(directLink.toString());
                    return;
                }
            }
        } catch (Exception e) {
            log.warn("Could not fetch direct link to asset", e);
        }

      
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
      try (BufferedInputStream buf_inputStream = (StringUtils.isBlank(mediaLocation)) ?
    		  new BufferedInputStream(new ByteArrayInputStream(mediaData.getMedia()))
    		  :
    		  new BufferedInputStream(getFileStream(mediaLocation));
    		  BufferedOutputStream buf_outputStream = new BufferedOutputStream(res.getOutputStream());){

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
      catch(IOException ioe) {
    	  log.warn("Error handling with IO in doPost", ioe);
      }
      catch(Exception e){
        log.warn(e.getMessage());
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


  private String escapeInvalidCharsEntry(String accentedString) {
    String decomposed = Normalizer.normalize(accentedString, Normalizer.Form.NFD);
    decomposed = decomposed.replaceAll("\\p{InCombiningDiacriticalMarks}+", StringUtils.EMPTY);
    decomposed = decomposed.replaceAll("\\?", StringUtils.EMPTY);
    // To avoid issues, dash variations will be replaced by a regular dash.
    decomposed = decomposed.replaceAll("\\p{Pd}", "-");
    // Remove any non-ascii characters to avoid errors like 'cannot be encoded as it is outside the permitted range of 0 to 255'
    decomposed = decomposed.replaceAll("[^\\p{ASCII}]", StringUtils.EMPTY);
    return decomposed;
  }

}
