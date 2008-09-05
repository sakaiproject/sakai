/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006 Sakai Foundation
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
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.MediaData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemText;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.services.GradingService;

import java.util.Date;
import java.util.ArrayList;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager.
 * Upload audio media to delivery bean.
 * This gets a posted input stream (from AudioRecorder.java in the client JVM)
 * and writes out to a file.</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class UploadAudioMediaServlet extends HttpServlet
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 8389831837152012411L;
private static Log log = LogFactory.getLog(UploadAudioMediaServlet.class);

  public UploadAudioMediaServlet()
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
    boolean mediaIsValid = true;
    ServletContext context = super.getServletContext();
    String repositoryPath = (String)context.getAttribute("FILEUPLOAD_REPOSITORY_PATH");
    String saveToDb = (String)context.getAttribute("FILEUPLOAD_SAVE_MEDIA_TO_DB");

    log.debug("req content length ="+req.getContentLength());
    log.debug("req content type ="+req.getContentType());

    // we get media location in assessmentXXX/questionXXX/agentId/audio_assessmentGradingIdXXX.au form
    String suffix = req.getParameter("suffix");
    if (suffix == null || ("").equals(suffix))
      suffix = "au";
    String mediaLocation = req.getParameter("media")+"."+suffix;
    log.debug("****media location="+mediaLocation);
    String zip_mediaLocation=null;
    String response = "empty";

    // test for nonemptiness first
    if (mediaLocation != null && !(mediaLocation.trim()).equals(""))
    {
      mediaLocation = repositoryPath+"/"+mediaLocation;
      File mediaFile = new File(mediaLocation);
      File mediaDir = mediaFile.getParentFile(); 
      if (!mediaDir.exists())
        mediaDir.mkdirs();
      //log.debug("*** directory exist="+mediaDir.exists());
      mediaIsValid=writeToFile(req, mediaLocation);

      //this is progess for SAK-5792, comment is out for now
      //zip_mediaLocation = createZipFile(mediaDir.getPath(), mediaLocation);
    }

    //#2 - record media as question submission
    if (mediaIsValid){
      // note that this delivery bean is empty. this is not the same one created for the
      // user during take assessment.
      try{
        if (zip_mediaLocation != null)
        response = submitMediaAsAnswer(req, zip_mediaLocation, saveToDb);
        else
        response = submitMediaAsAnswer(req, mediaLocation, saveToDb);
        log.info("Audio has been saved and submitted as answer to the question. Any old recordings have been removed from the system.");
      }
      catch (Exception ex){
        log.info(ex.getMessage());
      }
    }
  	res.setContentType("text/plain");
	res.setContentLength(response.length());
	PrintWriter out = res.getWriter();
	out.println(response);
	out.close();
	out.flush();
  }

  private boolean writeToFile(HttpServletRequest req, String mediaLocation){
    // default status message, if things go wrong
    boolean mediaIsValid = false;
    String status = "Upload failure: empty media location.";
    ServletInputStream inputStream = null;
    FileOutputStream fileOutputStream = null;
    BufferedInputStream bufInputStream = null;
    BufferedOutputStream bufOutputStream = null; 
    int count = 0;

    try{
      inputStream = req.getInputStream();
      fileOutputStream = getFileOutputStream(mediaLocation);

      // buffered input for servlet
      bufInputStream = new BufferedInputStream(inputStream);
      // buffered output to file
      bufOutputStream = new BufferedOutputStream(fileOutputStream);

      // write the binary data
      int i = 0;
      count = 0;
      if (bufInputStream != null){
        while ( (i = bufInputStream.read()) != -1){
          bufOutputStream.write(i);
          count++;
        }
      }
      bufOutputStream.flush();

      /* Move following clean up code to finally block
      // clean up
      bufOutputStream.close();
      bufInputStream.close();
      if (inputStream != null){
        inputStream.close();
      }
      fileOutputStream.close();
      */
      status = "Acknowleged: " +mediaLocation+"-> "+count+" bytes.";
      if (count > 0) 
        mediaIsValid = true;
    }
    catch (Exception ex){
      log.info(ex.getMessage());
      status = "Upload failure: "+ mediaLocation;
    }
    finally {
    	if (bufOutputStream != null) {
    		try {
    			bufOutputStream.close();
    		}
    		catch(IOException e) {
    			log.error(e.getMessage());
    		}
    	}
    	if (bufInputStream != null) {
    		try {
    			bufInputStream.close();
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
    	if (fileOutputStream != null) {
    		try {
    			fileOutputStream.close();
    		}
    		catch(IOException e) {
    			log.error(e.getMessage());
    		}
    	}
    }
    log.info(status);
    return mediaIsValid;
  }

  /*
  private String createZipFile(String mediaDirString, String mediaLocation){
    // Create a buffer for reading the files
    File file = new File(mediaLocation);
    String fileName=file.getName();
    byte[] buf = new byte[1024];
    String zip_mediaLocation = mediaDirString+"/"+fileName+".zip";
    ZipOutputStream zip = null;
    FileInputStream in = null;
    FileOutputStream out = null;
    try {
      // Create the ZIP file
      log.debug("*** zip file="+zip_mediaLocation);
      out = new FileOutputStream(zip_mediaLocation);
      zip = new ZipOutputStream(out);
    
      // Add ZIP entry to output stream.
      zip.putNextEntry(new ZipEntry(fileName));
    
      // Transfer bytes from the file to the ZIP file
      in = new FileInputStream(mediaLocation);
      int len;
      while ((len = in.read(buf)) > 0) {
        zip.write(buf, 0, len);
      }
    } 
    catch (IOException e) {
      zip_mediaLocation=null;
      log.error("problem zipping file at "+mediaLocation);
    }
    finally {
    	if (zip != null) {
    		try {
    			zip.closeEntry();
    			zip.close();
    		}
    		catch (IOException e) {
    			log.error(e.getMessage());
    		}
    	}
    	if (in != null) {
    		try {
    			in.close();
    		}
    		catch (IOException e) {
    			log.error(e.getMessage());
    		}
    	}
    	if (out != null) {
    		try {
    			out.close();
    		}
    		catch (IOException e) {
    			log.error(e.getMessage());
    		}
    	}
    }
    return zip_mediaLocation;
  }
  */
  
  private FileOutputStream getFileOutputStream(String mediaLocation){
    FileOutputStream outputStream=null;
    try{
      File media = new File(mediaLocation);
      outputStream = new FileOutputStream(media);
    }
    catch (FileNotFoundException ex) {
      log.warn("file not found="+ex.getMessage());
    }
    return outputStream;
  }

  private String submitMediaAsAnswer(HttpServletRequest req,
                                   String mediaLocation, String saveToDb)
    throws Exception{
    // read parameters passed in
    String mimeType = req.getContentType();
    String duration  = req.getParameter("lastDuration");
    String agentId  = req.getParameter("agent");
    
    int attemptsRemaining = Integer.parseInt(req.getParameter("attempts"));
    GradingService gradingService = new GradingService();
    PublishedAssessmentService pubService = new PublishedAssessmentService();
    int assessmentIndex = mediaLocation.indexOf("assessment");
    int questionIndex = mediaLocation.indexOf("question");
    int agentIndex = mediaLocation.indexOf("/", questionIndex + 8);
    //int myfileIndex = mediaLocation.lastIndexOf("/");
    String pubAssessmentId = mediaLocation.substring(assessmentIndex + 10,
						     questionIndex - 1);
    String questionId = mediaLocation.substring(questionIndex + 8, agentIndex);
    //String agentEid = mediaLocation.substring(agentIndex+1, myfileIndex);
    //log.debug("****pubAss="+pubAssessmentId);
    //log.debug("****questionId="+questionId);
    //log.debug("****agent="+agentId);

    PublishedItemData item = pubService.loadPublishedItem(questionId);
    PublishedItemText itemText = (PublishedItemText)(item.getItemTextSet()).iterator().next();

    log.debug("****agentId="+agentId);
    log.debug("****pubAssId="+pubAssessmentId);
    // 1. get assessmentGrading form DB
    AssessmentGradingData adata = gradingService.getLastSavedAssessmentGradingByAgentId(
                                  pubAssessmentId, agentId);

    if (adata == null){ 
      // adata should already be created by BeginAssessment
      // lets throws exception
      throw new Exception("This page must have been reached by mistake.");
    }

    // 2. get itemGrading from DB, remove any attached media
    //    also work out no. of attempts remaining 
    ItemGradingData itemGrading = gradingService.getItemGradingData(
                                  adata.getAssessmentGradingId().toString(), questionId);
    ArrayList mediaList = new ArrayList();
    if (itemGrading != null){
      // just need update itemGrading, and media.media 
      GradingService service = new GradingService();
      if (itemGrading.getItemGradingId() != null)
	mediaList = service.getMediaArray(itemGrading.getItemGradingId().toString());

      if (mediaList.size()>0){
        log.debug("*** delete old audio");
        gradingService.deleteAll(mediaList);
      }
      if (itemGrading.getAttemptsRemaining() == null){
        // this is not possible unless the question is submitted without the
        // attempt set correctly
        if ((item.getTriesAllowed()).intValue() >= 9999)
          attemptsRemaining = 9999;  
      }
      else{
        if ((item.getTriesAllowed()).intValue() >= 9999 )
          attemptsRemaining = 9999;
        else if (itemGrading.getAttemptsRemaining().intValue() > 0);
        // We're now getting the applet to tell us how many attempts remain
//          attemptsRemaining = itemGrading.getAttemptsRemaining().intValue() - 1;
        else
          throw new Exception("This page must have been reached by mistake. Our record shows that no more attempt for this question is allowed.");
      }
    }
    else{
      // create an itemGrading
      if ((item.getTriesAllowed()).intValue() >= 9999 )
        attemptsRemaining = 9999;
      else; 
//        attemptsRemaining = (item.getTriesAllowed()).intValue() -1;
      itemGrading = new ItemGradingData();
      itemGrading.setAssessmentGradingId(adata.getAssessmentGradingId());
      itemGrading.setPublishedItemId(item.getItemId());
      itemGrading.setPublishedItemTextId(itemText.getId());
      itemGrading.setAgentId(agentId);
      itemGrading.setOverrideScore(new Float(0));
      itemGrading.setSubmittedDate(new Date());
      itemGrading.setAttemptsRemaining(new Integer(attemptsRemaining));
      itemGrading.setLastDuration(duration);
      gradingService.saveItemGrading(itemGrading);
    }
    log.debug("****1. assessmentGradingId="+adata.getAssessmentGradingId());
    log.debug("****2. attemptsRemaining="+attemptsRemaining);
    log.debug("****3. itemGradingDataId="+itemGrading.getItemGradingId());
    // 3. save Media and fix up itemGrading
    return saveMedia(attemptsRemaining, mimeType, agentId, mediaLocation, itemGrading, saveToDb, duration);
  }

  private String saveMedia(int attemptsRemaining, String mimeType, String agent,
                         String mediaLocation, ItemGradingData itemGrading,
                        String saveToDb, String duration){
    boolean SAVETODB = false;
    if (saveToDb.equals("true"))
      SAVETODB = true;

    log.debug("****4. saveMedia, saveToDB"+SAVETODB);
    log.debug("****5. saveMedia, mediaLocation"+mediaLocation);

    GradingService gradingService = new GradingService();
    // 1. create a media record
    File media = new File(mediaLocation);
    byte[] mediaByte = getMediaStream(mediaLocation);
    log.debug("**** SAVETODB=" + SAVETODB);
    MediaData mediaData = null;

    if (SAVETODB)
    { // put the byte[] in
      mediaData = new MediaData(itemGrading, mediaByte,
                                new Long(mediaByte.length + ""),
                                mimeType, "description", null,
                                media.getName(), false, false, new Integer(1),
                                agent, new Date(),
                                agent, new Date(), duration);
    }
    else
    { // put the location in
      mediaData = new MediaData(itemGrading, null,
                                new Long(mediaByte.length + ""),
                                mimeType, "description", mediaLocation,
                                media.getName(), false, false, new Integer(1),
                                agent, new Date(),
                                agent, new Date(), duration);

    }
    Long mediaId = gradingService.saveMedia(mediaData);
    log.debug("mediaId=" + mediaId);

    // 2. store mediaId in itemGradingRecord.answerText
    itemGrading.setAttemptsRemaining(new Integer(attemptsRemaining));
    itemGrading.setSubmittedDate(new Date());
    itemGrading.setAnswerText(mediaId + "");
    gradingService.saveItemGrading(itemGrading);

    // 3. if saveToDB, remove file from file system
    try{
      if (SAVETODB) media.delete();
    }
    catch(Exception e){
      log.warn(e.getMessage());
    }
    return mediaId.toString();
  }

  private byte[] getMediaStream(String mediaLocation)
  {
    FileInputStream mediaStream = null;
    FileInputStream mediaStream2 = null;
    byte[] mediaByte = new byte[0];
    try
    {
      //int i = 0;
      int size = 0;
      mediaStream = new FileInputStream(mediaLocation);
      if (mediaStream != null)
      {
        //while ( (i = mediaStream.read()) != -1)
        while (mediaStream.read() != -1)
        {
          size++;
        }
      }
      mediaStream2 = new FileInputStream(mediaLocation);
      mediaByte = new byte[size];
      if (mediaStream2 != null) {
    	  mediaStream2.read(mediaByte, 0, size);
      }
    }
    catch (FileNotFoundException ex)
    {
      log.debug("file not found=" + ex.getMessage());
    }
    catch (IOException ex)
    {
      log.debug("io exception=" + ex.getMessage());
    }
    finally
    {
      if (mediaStream != null) {
    	  try
    	  {
    		  mediaStream.close();
    	  }
    	  catch (IOException ex1)
    	  {
    		  log.warn(ex1.getMessage());
    	  }
      }
      if (mediaStream2 != null) {
    	  try
    	  {
    		  mediaStream2.close();
    	  }
    	  catch (IOException ex1)
    	  {
    		  log.warn(ex1.getMessage());
    	  }
      }
    }
    return mediaByte;
  }

}
