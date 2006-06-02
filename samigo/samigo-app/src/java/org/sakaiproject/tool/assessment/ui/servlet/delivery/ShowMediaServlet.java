/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the"License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/



package org.sakaiproject.tool.assessment.ui.servlet.delivery;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.data.dao.grading.MediaData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
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
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class ShowMediaServlet extends HttpServlet
{
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
    log.info("**mediaId = "+mediaId);
    GradingService gradingService = new GradingService();
    MediaData mediaData = gradingService.getMedia(mediaId);
    String mediaLocation = mediaData.getLocation();
    int fileSize = mediaData.getFileSize().intValue();
    byte[] media = mediaData.getMedia();
    log.debug("****1. media file size="+mediaData.getFileSize());
    log.debug("****2. media length="+media.length);

    // get assessment's ownerId
    String assessmentCreatedBy = req.getParameter("createdBy");

    // who can access the media? You can,
    // a. if you are the creator.
    // b. if you have a assessment.grade.any or assessment.grade.own permission
    boolean accessDenied = true;
    String agentIdString = getAgentString(req, res);
    String currentSiteId="";
    if (mediaData != null){
      Long assessmentGradingId = mediaData.getItemGradingData().getAssessmentGradingId();
      PublishedAssessmentIfc pub = gradingService.getPublishedAssessmentByAssessmentGradingId(assessmentGradingId.toString()); 
      if (pub!=null){
        PublishedAssessmentService service = new PublishedAssessmentService();
        currentSiteId = service.getPublishedAssessmentOwner(pub.getPublishedAssessmentId());
      }
    }

    // some log checking
    log.debug("agentIdString ="+agentIdString);
    log.debug("****current site Id ="+currentSiteId);

    if (agentIdString !=null && mediaData != null &&
         (agentIdString.equals(mediaData.getCreatedBy()) // user is creator
	  || canGrade(req, res, agentIdString, currentSiteId, assessmentCreatedBy)))  
      accessDenied = false;
    if (accessDenied){
      String path = "/jsf/delivery/mediaAccessDenied.faces";
      RequestDispatcher dispatcher = req.getRequestDispatcher(path);
      dispatcher.forward(req, res);
    }
    else {
      String displayType="inline";
      if (mediaData.getMimeType()!=null){
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
      BufferedOutputStream buf_outputStream = new BufferedOutputStream(outputStream);
      if (mediaLocation == null || (mediaLocation.trim()).equals("")){
        buf_inputStream = new BufferedInputStream(new ByteArrayInputStream(media));
        log.debug("**** media.length="+media.length);
      }
      else{
        inputStream = getFileStream(mediaLocation);
        buf_inputStream = new BufferedInputStream(inputStream);
      }

      int count=0;
      try{
      int i=0;
      if (buf_inputStream !=null){
        while ((i=buf_inputStream.read()) != -1){
          //System.out.print(i);
          buf_outputStream.write(i);
          count++;
        }
      }
      }
      catch(Exception e){
        System.out.println(e.getMessage());
        System.out.println("***** catch count"+count);
      }

      log.debug("**** mediaLocation="+mediaLocation);
      log.debug("**** count="+count);
      res.setContentLength(count);
      res.flushBuffer();
      buf_outputStream.close();
      buf_inputStream.close();
      if (inputStream != null)
        inputStream.close();
      outputStream.close();
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
    String agentIdString = req.getRemoteUser();
    System.out.println("**** Show Media: agentId"+agentIdString);
    if (agentIdString == null){ // try this
      agentIdString = AgentFacade.getAgentString();
    }
    if (agentIdString == null || agentIdString.equals("")){ // try this
      PersonBean person = (PersonBean) ContextUtil.lookupBeanFromExternalServlet(
			   "person", req, res);
      agentIdString = person.getAnonymousId();
    }
    return agentIdString;
  }

  public boolean canGrade(HttpServletRequest req,  HttpServletResponse res,
                          String agentId, String currentSiteId, String assessmentCreatedBy){
    AuthorizationBean authzBean = (AuthorizationBean) ContextUtil.lookupBeanFromExternalServlet(
			   "authorization", req, res);
    boolean hasPrivilege_any = authzBean.getGradeAnyAssessment(req, currentSiteId);
    boolean hasPrivilege_own0 = authzBean.getGradeOwnAssessment(req, currentSiteId);
    boolean hasPrivilege_own = (hasPrivilege_own0 && isOwner(agentId, assessmentCreatedBy));
    boolean hasPrivilege = (hasPrivilege_any || hasPrivilege_own);
    return hasPrivilege;    
  }


  public boolean isOwner(String agentId, String ownerId){
    boolean isOwner = false;
    isOwner = agentId.equals(ownerId);
    return isOwner;
  }
}
