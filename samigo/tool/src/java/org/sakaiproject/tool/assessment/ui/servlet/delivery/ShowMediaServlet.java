/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2003-2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.assessment.ui.servlet.delivery;
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
    log.info("****1. media file size="+mediaData.getFileSize());
    log.info("****2. media length="+media.length);

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
      if (pub!=null)
        currentSiteId = pub.getOwnerSiteId();
    }

    // some log checking
    log.info("agentIdString ="+agentIdString);
    log.info("****current site Id ="+currentSiteId);

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
      log.info("****"+displayType+";filename=\""+mediaData.getFilename()+"\";");
      res.setHeader("Content-Disposition", displayType+";filename=\""+mediaData.getFilename()+"\";");

      //** note that res.setContentType() must be called before res.getOutputStream(). see javadoc on this
      FileInputStream inputStream = null;
      BufferedInputStream buf_inputStream = null;
      ServletOutputStream outputStream = res.getOutputStream();
      BufferedOutputStream buf_outputStream = new BufferedOutputStream(outputStream);
      if (mediaLocation == null || (mediaLocation.trim()).equals("")){
        buf_inputStream = new BufferedInputStream(new ByteArrayInputStream(media));
        log.info("**** media.length="+media.length);
      }
      else{
        inputStream = getFileStream(mediaLocation);
        buf_inputStream = new BufferedInputStream(inputStream);
      }
      int i=0;
      int count=0;
      if (buf_inputStream !=null){
        while ((i=buf_inputStream.read()) != -1){
          buf_outputStream.write(i);
          //System.out.print(i);
          count++;
        }
      }

      log.info("**** mediaLocation="+mediaLocation);
      log.info("**** count="+count);
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
