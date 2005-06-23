/**********************************************************************************
* $HeadURL$
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
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
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
    String mediaId = req.getParameter("mediaId");
    log.debug("**mediaId = "+mediaId);
    GradingService gradingService = new GradingService();
    MediaData mediaData = gradingService.getMedia(mediaId);
    String mediaLocation = mediaData.getLocation();
    byte[] media = mediaData.getMedia();

    // who can access the media? You can,
    // a. if you are the creator.
    // b. if you have a "maintain" role in the site where the media has been created.
    boolean accessDenied = true;
    String agentIdString = req.getRemoteUser();
    String currentSiteId = AgentFacade.getCurrentSiteIdFromExternalServlet(req,res);
    String mediaSiteId = mediaData.getItemGradingData().getAssessmentGrading().getPublishedAssessment().getOwnerSiteId();
    log.debug("****current site Id ="+currentSiteId);
    log.debug("****media site Id ="+mediaSiteId);
    String role = AgentFacade.getRole(agentIdString);
    if (mediaData == null || (mediaData!=null && !agentIdString.equals(mediaData.getCreatedBy())) ||
	!(("maintain").equals(role) && currentSiteId.equals(mediaSiteId)))
      accessDenied = false;

    if (accessDenied){
      String path = "/jsf/delivery/mediaAccessDenied.faces";
      RequestDispatcher dispatcher = req.getRequestDispatcher(path);
      dispatcher.forward(req, res);
    }
    else {
    FileInputStream inputStream = null;
    BufferedInputStream buf_inputStream = null;
    ServletOutputStream outputStream = res.getOutputStream();
    BufferedOutputStream buf_outputStream = new BufferedOutputStream(outputStream);

    if (mediaLocation == null){
      buf_inputStream = new BufferedInputStream(new ByteArrayInputStream(media));
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
        log.debug(i+"");
        count++;
      }
    }

    String displayType="inline";
    res.setHeader("Content-Disposition", displayType+";filename=\""+mediaData.getFilename()+"\";");
    res.setContentLength(count);
    if (mediaData.getMimeType()!=null)
      res.setContentType(mediaData.getMimeType());
    else
      res.setContentType("application/octet-stream");
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
      log.debug("file not found="+ex.getMessage());
    }
    return inputStream;
  }

}
