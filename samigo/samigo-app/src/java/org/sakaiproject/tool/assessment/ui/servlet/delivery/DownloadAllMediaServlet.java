/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/samigo-app/src/java/org/sakaiproject/tool/assessment/ui/servlet/delivery/DownloadAllMediaServlet.java $
 * $Id: DownloadAllMediaServlet.java 11494 2006-07-05 21:15:29Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007 Sakai Foundation
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
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.shared.impl.assessment.PublishedAssessmentServiceImpl;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.MediaIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.integration.helper.integrated.AgentHelperImpl;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.QuestionScoresBean;
import org.sakaiproject.tool.assessment.ui.bean.shared.PersonBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
 * @version $Id: DownloadAllMediaServlet 11494 2006-07-05 21:15:29Z daisyf@stanford.edu $
 */

public class DownloadAllMediaServlet extends HttpServlet
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 1465451058167004991L;
private static Log log = LogFactory.getLog(DownloadAllMediaServlet.class);
  private GradingService gradingService = new GradingService();
  
  public DownloadAllMediaServlet()
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
    String publishedItemId = req.getParameter("publishedItemId");
    log.debug("publishedItemId = " + publishedItemId);
    
    // who can access the zip file? You can,
    // if you have a assessment.grade.any or assessment.grade.own permission
    boolean accessDenied = true;
    String agentIdString = getAgentString(req, res);
    String currentSiteId="";
    String assessmentName = "";
    
    PublishedAssessmentIfc pub = gradingService.getPublishedAssessmentByPublishedItemId(publishedItemId.toString()); 
    if (pub != null){
    	assessmentName = pub.getTitle();
    	PublishedAssessmentService service = new PublishedAssessmentService();
    	log.debug("pub.getPublishedAssessmentId() = " + pub.getPublishedAssessmentId());
        currentSiteId = service.getPublishedAssessmentOwner(pub.getPublishedAssessmentId());
    }
    // get assessment's ownerId
    String assessmentCreatedBy = req.getParameter("createdBy");
    if (canGrade(req, res, agentIdString, currentSiteId, assessmentCreatedBy)) { 
    	accessDenied = false;
    }
    
    if (accessDenied){
        String path = "/jsf/delivery/mediaAccessDenied.faces";
        RequestDispatcher dispatcher = req.getRequestDispatcher(path);
        dispatcher.forward(req, res);
    }
    else {
    	res.setContentType("application/x-zip-compressed");
  	    StringBuilder zipFilename = new StringBuilder();
  	    zipFilename.append(assessmentName);
  	    String partAndQues = getPartNumAndQuestionNum(publishedItemId);
	    log.debug("partAndQues = " + partAndQues);
	    zipFilename.append(partAndQues);
  	    zipFilename.append((".zip"));
    	log.debug("zipFilename = " + zipFilename);
        res.setHeader("Content-Disposition", "attachment;filename=\"" + zipFilename + "\";");
        
        String anonymous = req.getParameter("anonymous");
  	  	if ("true".equals(anonymous)) {
  	  		processAnonymous(req, res);
  	  	}
  	  	else {
  	  		processNonAnonymous(req, res);
  	  	}
    }
  }
   
  private void processAnonymous(HttpServletRequest req, HttpServletResponse res){
	  String publishedId = req.getParameter("publishedId");
	  String publishedItemId = req.getParameter("publishedItemId");
	  String scoringType = req.getParameter("scoringType");
	  log.debug("publishedId = " + publishedId);
	  log.debug("publishedItemId = " + publishedItemId);
	  log.debug("scoringType = " + scoringType);
	  
	  GradingService gradingService = new GradingService();
	  List mediaList = gradingService.getMediaArray(publishedId,publishedItemId, scoringType);

	  MediaIfc mediaData;
	  log.debug("mediaList.size() = " + mediaList.size());

	  ZipOutputStream zos = null;
      try{
    	  ServletOutputStream outputStream = res.getOutputStream();
    	  zos = new ZipOutputStream(outputStream);
    	  for (int i = 0; i < mediaList.size(); i++){
    		  mediaData = (MediaIfc) mediaList.get(i);
    		  processOneMediaData(zos, mediaData, true, -1); 
    	  }  
      }
	  catch(IOException e){
		  log.error(e.getMessage());
		  e.printStackTrace();
	  }
	  finally {
		  if (zos != null) {
			  try {
				  zos.close();
			  }
			  catch(IOException e) {
				  log.error(e.getMessage());
			  }
		  }
	  }	  
  }
  
  private void processNonAnonymous(HttpServletRequest req, HttpServletResponse res){
	  String publishedId = req.getParameter("publishedId");
	  String publishedItemId = req.getParameter("publishedItemId");
	  String scoringType = req.getParameter("scoringType");
	  log.debug("publishedId = " + publishedId);
	  log.debug("publishedItemId = " + publishedItemId);
	  log.debug("scoringType = " + scoringType);
	  
      HashMap hashByAgentId = new HashMap();
      HashMap subHashByAssessmentGradingId;
      MediaIfc mediaData;
      ArrayList list;
      ItemGradingData itemGradingData;

	  List mediaList;
	  mediaList = gradingService.getMediaArray(publishedId, publishedItemId, scoringType);
	  log.debug("mediaList.size() = " + mediaList.size());
		  
	  QuestionScoresBean questionScoresBean = (QuestionScoresBean) ContextUtil.lookupBeanFromExternalServlet(
			   "questionScores", req, res);
	  Map userIdMap = questionScoresBean.getUserIdMap();
	  
	  String agentId;
	  Long assessmentGradingId;
	  for (int i = 0; i < mediaList.size(); i++) {
		  mediaData = (MediaIfc) mediaList.get(i);
		  itemGradingData = (ItemGradingData) mediaData.getItemGradingData();
		  agentId = itemGradingData.getAgentId();
		  assessmentGradingId = itemGradingData.getAssessmentGradingId();
		  log.debug("agentId = " + agentId);
		  log.debug("assessmentGradingId = " + assessmentGradingId);
		  if (!userIdMap.containsKey(agentId)) {
			  log.debug("Do not download files from this user - agentId = " + agentId);
			  continue;
		  }
		  if (hashByAgentId.containsKey(agentId)) {
			  log.debug("same agentId");
			  subHashByAssessmentGradingId = (HashMap) hashByAgentId.get(agentId);
			  if (subHashByAssessmentGradingId.containsKey(assessmentGradingId)) {
				  log.debug("same assessmentGradingId");
				  list = (ArrayList) subHashByAssessmentGradingId.get(assessmentGradingId);
				  list.add(mediaData);
			  }
			  else {
				  log.debug("different assessmentGradingId");
				  list = new ArrayList();
				  list.add(mediaData);
				  subHashByAssessmentGradingId.put(assessmentGradingId, list);
			  }
		  }
		  else {
			  log.debug("different agentId");
			  list = new ArrayList();
			  list.add(mediaData);
			  subHashByAssessmentGradingId = new HashMap();
			  subHashByAssessmentGradingId.put(assessmentGradingId, list);
			  hashByAgentId.put(agentId, subHashByAssessmentGradingId);
		  }
	  }
	  log.debug("HashMap built successfully");
	  ZipOutputStream zos = null;
	  try {
		  ServletOutputStream outputStream = res.getOutputStream();
		  zos = new ZipOutputStream(outputStream);			  
          
		  HashMap hashMap;
		  Iterator iter = hashByAgentId.values().iterator();
		  int numberSubmission;
		  while (iter.hasNext()) {
  			  hashMap = (HashMap) iter.next();
   			  numberSubmission = hashMap.size();
   			  log.debug("numberSubmission = " + numberSubmission);
   			  Iterator subIter = hashMap.keySet().iterator();
   			  // this student has submitted more than once
   			  if (numberSubmission > 1) {
   				  // Because Hashmap makes no guarantees as to the order of the map; 
   				  // and it does not guarantee that the order will remain constant over time,
   				  // following implementation is to make sure we get the correct order
   				  // that is, if there are two submissions from John Doe:
   				  // submission id 24 submitted on Jun 28, 2006 (file A.txt)
   				  // submission id 33 submitted on Jul 03, 2006 (file B.txt)
   				  // We want to make sure the filename of these two are:
   				  // Doe_John_sub1_A.txt and Doe_John_sub2_B.txt
   				  // If we don't sort it, the outcome might be:
   				  // Doe_John_sub2_A.txt and Doe_John_sub1_B.txt which are not what we want
   				  ArrayList keyList = new ArrayList();
   				  Long key;
   				  while(subIter.hasNext()) {
   					  key = (Long) subIter.next();
   					  log.debug("key = " + key);
   					  keyList.add(key);
   					  Collections.sort(keyList);
   				  }
   			  
   				  ArrayList valueList;
   				  Long sortedKey;
   				  for (int i = 0; i < keyList.size(); i++) {
   					  sortedKey = (Long) keyList.get(i);
   					  valueList = (ArrayList) hashMap.get(sortedKey);
   					  for (int j = 0; j < valueList.size(); j++) {
   						  log.debug("j = " + j);
   						  mediaData = (MediaIfc) valueList.get(j);
   						  processOneMediaData(zos, mediaData, false, i+1);
   					  }
   				  }
   			  }
   			  // this student has only one submission
   			  else if (numberSubmission == 1){
   				  ArrayList valueList;
   				  while(subIter.hasNext()) {
   					valueList = (ArrayList) hashMap.get(subIter.next());
   					log.debug("valueList.size() = " + valueList.size());
   					for (int i = 0; i < valueList.size(); i++) {
   						log.debug("i = " + i);
   						mediaData = (MediaIfc) valueList.get(i);
   						// we use "-1" to indicate one submission
   						// "sub" will not be instered into filename
   						processOneMediaData(zos, mediaData, false, -1); 
   					}
   				  }
   			  }
   		  }
	  }
	  catch (IOException e) {
		  log.error(e.getMessage());
		e.printStackTrace();
	  }
	  finally {
		  if (zos != null) {
			  try {
				  zos.close();
			  }
			  catch(IOException e) {
				  log.error(e.getMessage());
			  }
		  }
	  }	  
  }
  
  private void processOneMediaData(ZipOutputStream zos, MediaIfc mediaData, boolean anonymous, int numberSubmission) 
  throws IOException {
	  int BUFFER_SIZE = 2048;
	  byte data[] = new byte[ BUFFER_SIZE ];
	  int count = 0;
	  BufferedInputStream bufInputStream = null;
	  ZipEntry ze = null;
	  String mediaLocation = mediaData.getLocation();
	  log.debug("mediaLocation = " + mediaLocation);
	  String filename = getFilename(mediaData, anonymous, numberSubmission);
	  if (mediaLocation == null || (mediaLocation.trim()).equals("")){          		  
		  byte[] media = mediaData.getMedia();
		  log.debug("media.length = " + media.length);
		  bufInputStream = new BufferedInputStream(new ByteArrayInputStream(media));
	  }
	  else {
		  bufInputStream = new BufferedInputStream(getFileStream(mediaLocation));
	  }
	  ze = new ZipEntry(filename);
	  try {
		  zos.putNextEntry(ze);
		  while( (count = bufInputStream.read(data, 0, BUFFER_SIZE)) != -1 ) {
			  zos.write(data, 0, count);
		  }
	  }
	  catch(IOException e){
		  log.error(e.getMessage());
		  throw e;
	  }
	  finally {
		  if (bufInputStream != null) {
			  try {
				  bufInputStream.close();
			  }
			  catch(IOException e) {
				  log.error(e.getMessage());
			  }
		  }
		  if (zos != null) {
			  try {
				  zos.closeEntry();
			  }
			  catch(IOException e) {
				  log.error(e.getMessage());
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
    String agentIdString = AgentFacade.getAgentString();
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
    
    log.debug("agentId=" + agentId);
    log.debug("currentSiteId=" + currentSiteId);
    log.debug("assessmentCreatedBy=" + assessmentCreatedBy);
    boolean hasPrivilege_any = authzBean.getGradeAnyAssessment(req, currentSiteId);
    boolean hasPrivilege_own0 = authzBean.getGradeOwnAssessment(req, currentSiteId);
    boolean hasPrivilege_own = (hasPrivilege_own0 && isOwner(agentId, assessmentCreatedBy));
    boolean hasPrivilege = (hasPrivilege_any || hasPrivilege_own);
    log.debug("hasPrivilege_any=" + hasPrivilege_any);
    log.debug("hasPrivilege_own0=" + hasPrivilege_own0);
    log.debug("hasPrivilege_own=" + hasPrivilege_own);
    log.debug("hasPrivilege=" + hasPrivilege);
    return hasPrivilege;    
  }


  public boolean isOwner(String agentId, String ownerId){
    boolean isOwner = false;
    isOwner = agentId.equals(ownerId);
    log.debug("isOwner=" + isOwner);
    return isOwner;
  }
  
  private String getPartNumAndQuestionNum(String itemId){
	  log.debug("itemId = " + itemId);
	  PublishedAssessmentServiceImpl pubAssessmentServiceImpl = new PublishedAssessmentServiceImpl();
	  ItemDataIfc item = pubAssessmentServiceImpl.loadPublishedItem(itemId);
	  Integer partNum = item.getSection().getSequence();
	  log.debug("partNum = " + partNum);
	  Integer questionNum = item.getSequence();
	  log.debug("questionNum=" + questionNum);
	  StringBuilder partAndQues = new StringBuilder("_Part");
	  partAndQues.append(partNum);
	  partAndQues.append("_Ques");
	  partAndQues.append(questionNum);
	  log.debug("partAndQues = " + partAndQues);
	  return partAndQues.toString();
  }

  private String getFilename(MediaIfc mediaData, boolean anonymous, int numberSubmission) {
	  log.debug("numberSubmission = " + numberSubmission);
	  StringBuilder filename = new StringBuilder();
	  ItemGradingData itemGradingData = (ItemGradingData) mediaData.getItemGradingData();
	  if (anonymous) {
		  Long assessmentGradingId = itemGradingData.getAssessmentGradingId();
		  log.debug("submissionId(assessmentGradingId) = " + assessmentGradingId);
		  filename.append(assessmentGradingId);
		  filename.append("_");
		  filename.append(mediaData.getFilename());
		  log.debug("filename = " + filename);
	  }
	  else {
		  AgentHelperImpl helper = new AgentHelperImpl();
		  String agentId = itemGradingData.getAgentId();
		  String lastName = helper.getLastName(agentId);
		  String firstName = helper.getFirstName(agentId);
		  String eid = helper.getEidById(agentId);
		  filename.append(lastName);
		  filename.append("_");
		  filename.append(firstName);
		  filename.append("_");
		  filename.append(eid);
		  filename.append("_");
		  log.debug("filename = " + filename);
		  if (numberSubmission == -1) {
			  filename.append(mediaData.getFilename());
			  log.debug("filename = " + filename);
		  }
		  else {
			  filename.append("sub");
			  filename.append(numberSubmission);
			  filename.append("_");
			  filename.append(mediaData.getFilename());
			  log.debug("filename = " + filename);
		  }
	  }
		
  	  return filename.toString();
  }
}
