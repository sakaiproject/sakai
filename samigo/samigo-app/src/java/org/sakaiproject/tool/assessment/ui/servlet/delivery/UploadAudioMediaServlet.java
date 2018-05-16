/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008, 2009 The Sakai Foundation
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

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.MediaData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemText;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.util.DateFormatterUtil;
import org.sakaiproject.util.ResourceLoader;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager.
 * Upload audio media to delivery bean.
 * This gets a posted input stream (from AudioRecorder.java in the client JVM)
 * and writes out to a file.</p>
 * @author Ed Smiley
 * @version $Id$
 */
@Slf4j
public class UploadAudioMediaServlet extends HttpServlet
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 8389831837152012411L;

	ResourceLoader rb = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.DeliveryMessages");

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
    JsonObject json = null;

    // test for nonemptiness first
    if (mediaLocation != null && !(mediaLocation.trim()).equals(""))
    {
      File repositoryPathDir = new File(repositoryPath);
      // Fix Windows paths
      if("\\".equals(File.separator)){
          mediaLocation = mediaLocation.replace("/","\\");
      }
      mediaLocation = repositoryPathDir.getCanonicalPath() + File.separator + mediaLocation;
      File mediaFile = new File(mediaLocation);
      
      if (mediaFile.getCanonicalPath().equals (mediaLocation)){
    	  File mediaDir = mediaFile.getParentFile(); 
          if (!mediaDir.exists())
            mediaDir.mkdirs();

          mediaIsValid=writeToFile(req, mediaLocation);  
      }else{
    	  log.error ("****Error in file paths " + mediaFile.getCanonicalPath() + " is not equal to " + mediaLocation);
    	  mediaIsValid=false;
      }

      //this is progess for SAK-5792, comment is out for now
      //zip_mediaLocation = createZipFile(mediaDir.getPath(), mediaLocation);
    }

    //#2 - record media as question submission
    if (mediaIsValid){
      // note that this delivery bean is empty. this is not the same one created for the
      // user during take assessment.
      try{
        json = submitMediaAsAnswer(req, mediaLocation, saveToDb);
        log.info("Audio has been saved and submitted as answer to the question. Any old recordings have been removed from the system.");
      }
      catch (Exception ex){
        log.info(ex.getMessage());
      }
    }
    String response = new Gson().toJson(json);
    res.setContentType("application/json");
    res.setCharacterEncoding("UTF-8");
    try (PrintWriter out = res.getWriter()) {
      out.println(response);
      out.close();
    }
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

  private JsonObject submitMediaAsAnswer(HttpServletRequest req,
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
    int agentIndex = mediaLocation.indexOf(File.separator, questionIndex + 8);
    //int myfileIndex = mediaLocation.lastIndexOf("/");
    String pubAssessmentId = mediaLocation.substring(assessmentIndex + 10,
						     questionIndex - 1);
    String questionId = mediaLocation.substring(questionIndex + 8, agentIndex);
    //String agentEid = mediaLocation.substring(agentIndex+1, myfileIndex);

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
    List<MediaData> mediaList = new ArrayList<>();
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
      itemGrading.setOverrideScore(Double.valueOf(0));
      itemGrading.setSubmittedDate(new Date());
      itemGrading.setAttemptsRemaining(Integer.valueOf(attemptsRemaining));
      itemGrading.setLastDuration(duration);
      gradingService.saveItemGrading(itemGrading);
    }
    log.debug("****1. assessmentGradingId="+adata.getAssessmentGradingId());
    log.debug("****2. attemptsRemaining="+attemptsRemaining);
    log.debug("****3. itemGradingDataId="+itemGrading.getItemGradingId());
    // 3. save Media and fix up itemGrading
    return saveMedia(attemptsRemaining, mimeType, agentId, mediaLocation, itemGrading, saveToDb, duration);
  }

  private JsonObject saveMedia(int attemptsRemaining, String mimeType, String agent,
                         String mediaLocation, ItemGradingData itemGrading,
                        String saveToDb, String duration){
    boolean SAVETODB = false;
    if ("true".equals(saveToDb))
      SAVETODB = true;

    log.debug("****4. saveMedia, saveToDB"+SAVETODB);
    log.debug("****5. saveMedia, mediaLocation"+mediaLocation);

    GradingService gradingService = new GradingService();
    // 1. create a media record
    File media = new File(mediaLocation);
    log.debug("**** SAVETODB=" + SAVETODB);
    MediaData mediaData = null;

    if (SAVETODB)
    { // put the byte[] in
      byte[] mediaByte = getMediaStream(mediaLocation);
      mediaData = new MediaData(itemGrading, mediaByte,
                                Long.valueOf(mediaByte.length + ""),
                                mimeType, "description", null,
                                media.getName(), false, false, Integer.valueOf(1),
                                agent, new Date(),
                                agent, new Date(), duration);
    }
    else
    { // put the location in
      mediaData = new MediaData(itemGrading, null,
                                Long.valueOf(media.length() + ""),
                                mimeType, "description", mediaLocation,
                                media.getName(), false, false, Integer.valueOf(1),
                                agent, new Date(),
                                agent, new Date(), duration);

    }
    Long mediaId = gradingService.saveMedia(mediaData);
    mediaData.setMediaId(mediaId);
    log.debug("mediaId=" + mediaId);

    // 2. store mediaId in itemGradingRecord.answerText
    itemGrading.setAttemptsRemaining(Integer.valueOf(attemptsRemaining));
    itemGrading.setSubmittedDate(new Date());
    itemGrading.setAnswerText(mediaId + "");
    itemGrading.setAutoScore(Double.valueOf(0));
    gradingService.saveItemGrading(itemGrading);

    // 3. if saveToDB, remove file from file system
    try{
      if (SAVETODB) {
	boolean success = media.delete();
	if (!success)
		log.error ("Delete Failed for media. mediaid = " + mediaId);
      }
    }
    catch(Exception e){
      log.warn(e.getMessage());
    }
    JsonObject json = new JsonObject();
    json.addProperty("mediaId", mediaId);
    json.addProperty("duration", mediaData.getDuration());
    json.addProperty("createdDate", DateFormatterUtil.format(mediaData.getCreatedDate(), rb.getString("delivery_date_format"), rb.getLocale()));
    return json;
  }

  private byte[] getMediaStream(String mediaLocation)
  {
    byte[] mediaByte = new byte[0];
    
    try
    {
      mediaByte = Files.readAllBytes(new File(mediaLocation).toPath());
    }
    catch (FileNotFoundException ex)
    {
      log.error("File not found in UploadAudioMediaServlet.getMediaStream(): " + ex.getMessage());
    }
    catch (IOException ex)
    {
      log.error("IO Exception in UploadAudioMediaServlet.getMediaStream(): " + ex.getMessage());
    }
    
    return mediaByte;
  }

}
