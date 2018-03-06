/**
 * Copyright (c) 2005-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.tool.assessment.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.dao.grading.MediaData;
import org.sakaiproject.tool.assessment.integration.helper.integrated.AgentHelperImpl;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.DownloadFileSubmissionsBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.TotalScoresBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.util.ResourceLoader;

@Slf4j
public class DownloadFileUtil {

	public void processWholeSiteOrOneSection(HttpServletRequest req, HttpServletResponse res, ArrayList<ItemDataIfc> idataList, ArrayList<String> userUidList) {
		processWholeSiteOrOneSection(req, res, idataList, userUidList, null);
	}

	public void processWholeSiteOrOneSection(HttpServletRequest req, HttpServletResponse res, ArrayList<ItemDataIfc> idataList, ArrayList<String> userUidList, String sectionName){
		ResourceLoader rb = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.EvaluationMessages");
		res.setContentType("application/x-zip-compressed");
		TotalScoresBean totalScores = (TotalScoresBean) ContextUtil.lookupBean("totalScores");

		StringBuilder zipFilename = new StringBuilder();
		zipFilename.append(totalScores.getAssessmentName());
		if (idataList.size() == 1) {
			ItemDataIfc idata = (ItemDataIfc) idataList.get(0);
			zipFilename.append("_");
			zipFilename.append(getPartNumAndQuestionNum(idata));
		}

		if (sectionName != null) {
			zipFilename.append("_");
			zipFilename.append(sectionName);
		}
		zipFilename.append((".zip"));
		res.setHeader("Content-Disposition", "attachment;filename=\"" + zipFilename + "\";");

		String anonymous = totalScores.getAnonymous();
		String publishedAssessmentId = totalScores.getPublishedId();
		String scoringType = totalScores.getSortType();
		ServletOutputStream outputStream = null;
		ZipOutputStream zos = null;
		try {
			outputStream = res.getOutputStream();
			zos = new ZipOutputStream(outputStream);

			Iterator iter = idataList.iterator();
			while (iter.hasNext()) {
				ItemDataIfc idata = (ItemDataIfc) iter.next();
				String itemId = idata.getItemId().toString();
				StringBuffer questionFolderPath = new StringBuffer();
				try {
					if (idataList.size() != 1) {
						questionFolderPath.append(getPartNumAndQuestionNum(idata));
						questionFolderPath.append("/");
						ZipEntry questionFolderEntry = new ZipEntry(questionFolderPath.toString());
						zos.putNextEntry(questionFolderEntry);
					}
					if ("true".equals(anonymous)) {
						processAnonymous(zos, publishedAssessmentId, itemId, scoringType, userUidList, questionFolderPath.toString());
					}
					else {
						processNonAnonymous(zos, publishedAssessmentId, itemId, scoringType, userUidList, questionFolderPath.toString());
					}
				} catch (IOException e) {
					log.error(e.getMessage());
				} finally {
					if (idataList.size() != 1 && zos != null) {
						try {
							zos.closeEntry();
						}
						catch(IOException e) {
							log.error(e.getMessage());
						}
					}
				}
			}
		} catch (IOException e) {
			log.error(e.getMessage());
		} finally {

			if (zos != null) {
				try {
					zos.close();
				}
				catch(IOException e) {
					log.error(e.getMessage());
				}
			}
			if (outputStream != null) {
				try {
					outputStream.flush();
					outputStream.flush();
				} catch (IOException e) {
					log.error(e.getMessage());
				}
			}
		}
	}

	public void processMultipleSection(HttpServletRequest req, HttpServletResponse res, 
			ArrayList<ItemDataIfc> idataList, HashMap sectionUsersMap){
		TotalScoresBean totalScores = (TotalScoresBean) ContextUtil.lookupBean("totalScores");
		DownloadFileSubmissionsBean downloadFileSubmissionsBean = (DownloadFileSubmissionsBean) ContextUtil
		.lookupBean("downloadFileSubmissions");

		StringBuilder zipFilename = new StringBuilder();
		zipFilename.append(totalScores.getAssessmentName());
		if (idataList.size() == 1) {
			ItemDataIfc idata = (ItemDataIfc) idataList.get(0);
			zipFilename.append("_");
			zipFilename.append(getPartNumAndQuestionNum(idata));
			zipFilename.append(idata.getSequence());
		}
		zipFilename.append((".zip"));

		String anonymous = totalScores.getAnonymous();
		res.setHeader("Content-Disposition", "attachment;filename=\"" + zipFilename + "\";");

		ServletOutputStream outputStream = null;
		ZipOutputStream zos = null;
		try {
			outputStream = res.getOutputStream();
			zos = new ZipOutputStream(outputStream);	

			Iterator iter = idataList.iterator();
			while (iter.hasNext()) {
				ItemDataIfc idata = (ItemDataIfc) iter.next();
				String itemId = idata.getItemId().toString();
				StringBuffer questionFolderPath = new StringBuffer();
				if (idataList.size() != 1) {
					questionFolderPath.append(getPartNumAndQuestionNum(idata));
					questionFolderPath.append("_");
				}

				Set<String> sectoinSet = sectionUsersMap.keySet();
				Iterator iter2 = sectoinSet.iterator();
				String sectionId;
				String sectionName;
				HashMap<String, String> sectionUuidNameMap = downloadFileSubmissionsBean.getSectionUuidNameMap();
				String publishedAssessmentId = totalScores.getPublishedId();
				String scoringType = totalScores.getSortType();
				while (iter2.hasNext()) {
					sectionId = (String) iter2.next();
					sectionName = (String) sectionUuidNameMap.get(sectionId);
					ArrayList<String> userUidList = (ArrayList<String>) sectionUsersMap.get(sectionId);
					String sectionFolderPath = questionFolderPath.toString() + sectionName + "/";
					ZipEntry sectionFolderEntry = new ZipEntry(sectionFolderPath);
					try {
						zos.putNextEntry(sectionFolderEntry);
						if ("true".equals(anonymous)) {
							processAnonymous(zos, publishedAssessmentId, itemId, scoringType, userUidList, sectionFolderPath);
						}
						else {
							processNonAnonymous(zos, publishedAssessmentId, itemId, scoringType, userUidList, sectionFolderPath);
						}
					} catch(IOException e) {
						log.error(e.getMessage());
					}
					finally {
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
			}
		} catch (IOException e) {
			log.error(e.getMessage());
		} finally {
			if (zos != null) {
				try {
					zos.closeEntry();
				}
				catch(IOException e) {
					log.error(e.getMessage());
				}
			}

			if (zos != null) {
				try {
					zos.close();
				}
				catch(IOException e) {
					log.error(e.getMessage());
				}
			}
			if (outputStream != null) {
				try {
					outputStream.flush();
					outputStream.flush();
				} catch (IOException e) {
					log.error(e.getMessage());
				}
			}
		}
	}

	public void processAnonymous(ZipOutputStream zos, String publishedAssessmentId, String publishedItemId, String scoringType, ArrayList<String> userUidList){
		processAnonymous(zos, publishedAssessmentId, publishedItemId, scoringType, userUidList, null);
	}

	public void processAnonymous(ZipOutputStream zos, String publishedAssessmentId, String publishedItemId, String scoringType, ArrayList<String> userUidList, String sectionFolderPath){

		GradingService gradingService = new GradingService();
		List mediaList = gradingService.getMediaArray(publishedAssessmentId,publishedItemId, scoringType);

		MediaData mediaData;
		log.debug("mediaList.size() = " + mediaList.size());

		ItemGradingData itemGradingData;
		String agentId;
		for (int i = 0; i < mediaList.size(); i++){
			mediaData = (MediaData) mediaList.get(i);
			itemGradingData = (ItemGradingData) mediaData.getItemGradingData();
			agentId = itemGradingData.getAgentId();
			if (!userUidList.contains(agentId)) {
				log.debug("Do not download files from this user - agentId = " + agentId);
				continue;
			}
			processOneMediaData(zos, mediaData, true, -1, sectionFolderPath); 
		}  
	}

	public void processNonAnonymous(ZipOutputStream zos, String publishedAssessmentId, String publishedItemId, String scoringType, ArrayList<String> userUidList){
		processNonAnonymous(zos, publishedAssessmentId, publishedItemId, scoringType, userUidList, null);
	}

	public void processNonAnonymous(ZipOutputStream zos, String publishedAssessmentId, String publishedItemId, String scoringType, ArrayList<String> userUidList, String sectionFolderPath){
		HashMap hashByAgentId = new HashMap();
		HashMap subHashByAssessmentGradingId;
		MediaData mediaData;
		ArrayList list;
		ItemGradingData itemGradingData;

		List mediaList;
		GradingService gradingService = new GradingService();
		mediaList = gradingService.getMediaArray(publishedAssessmentId, publishedItemId, scoringType);
		log.debug("mediaList.size() = " + mediaList.size());

		String agentId;
		Long assessmentGradingId;
		for (int i = 0; i < mediaList.size(); i++) {
			mediaData = (MediaData) mediaList.get(i);
			itemGradingData = (ItemGradingData) mediaData.getItemGradingData();
			agentId = itemGradingData.getAgentId();
			assessmentGradingId = itemGradingData.getAssessmentGradingId();
			log.debug("agentId = " + agentId);
			log.debug("assessmentGradingId = " + assessmentGradingId);
			if (!userUidList.contains(agentId)) {
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
						mediaData = (MediaData) valueList.get(j);
						processOneMediaData(zos, mediaData, false, i+1, sectionFolderPath);
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
						mediaData = (MediaData) valueList.get(i);
						// we use "-1" to indicate one submission
						// "sub" will not be instered into filename
						processOneMediaData(zos, mediaData, false, -1, sectionFolderPath); 
					}
				}
			}
		}  
	}

	private void processOneMediaData(ZipOutputStream zos, MediaData mediaData, boolean anonymous, int numberSubmission, String sectionFolderPath) {
		int BUFFER_SIZE = 2048;
		byte data[] = new byte[ BUFFER_SIZE ];
		int count = 0;
		BufferedInputStream bufInputStream = null;
		ZipEntry ze = null;
		String mediaLocation = mediaData.getLocation();
		log.debug("mediaLocation = " + mediaLocation);
		String filename = getFilename(mediaData, anonymous, numberSubmission);
		if (sectionFolderPath != null) {
			filename = sectionFolderPath + filename;
		}
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

	private String getPartNumAndQuestionNum(ItemDataIfc item) {
		ResourceLoader rb = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.EvaluationMessages");
		Integer partNum = item.getSection().getSequence();
		Integer questionNum = item.getSequence();
		StringBuilder partAndQues = new StringBuilder(rb.getString("part"));
		partAndQues.append(partNum);
		partAndQues.append("_");
		partAndQues.append(rb.getString("q"));
		partAndQues.append(questionNum);
		log.debug("partAndQues = " + partAndQues);
		return partAndQues.toString();
	}

	private String getFilename(MediaData mediaData, boolean anonymous, int numberSubmission) {
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
