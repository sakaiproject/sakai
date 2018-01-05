/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2016 The Sakai Foundation
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
package org.sakaiproject.tags.impl.job;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.dom.DOMResult;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.email.api.EmailService;

/**
 * A quartz job to synchronize the TAGS with an
 * xml file available in sakai home.
 *
 *
 */
@Slf4j
public class TagsExportedXMLSyncJob extends TagSynchronizer implements Job {

	private static final int VALID_ID_LENGTH = 36;
	private String tagsPathToXml;
	private String tagCollectionsPathToXml;

	private ServerConfigurationService serverConfigurationService;
	private EmailService emailService;

	private String collectionToUpdate=null;
	private Boolean collectionToUpdateMoreThanOne=false;


	/**
	 * {@inheritDoc}
	 */
	public InputStream getTagsXmlInputStream() {
		tagsPathToXml = serverConfigurationService.getSakaiHomePath() + serverConfigurationService.getString("tags.fullxmltagsfile","tags/fullxmltags.xml");
		log.debug(tagsPathToXml);
		File xmlFile = new File(tagsPathToXml);
		InputStream targetStream = null;
			try {
				targetStream=new FileInputStream(xmlFile);
			} catch (Exception e){
				log.warn("The Full Tags XML file can't be found in the specified route: " + tagsPathToXml);
			}

		return targetStream;


	}


	/**
	 * {@inheritDoc}
	 */
	public void execute(JobExecutionContext arg0) throws JobExecutionException {

		syncAllTags();
	}


	public synchronized void syncAllTags() {

		long start = System.currentTimeMillis();

		if(log.isInfoEnabled()) {
			log.info("Starting Full XML Tag Collection synchronization");
		}
		try{
			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLStreamReader xsr = factory.createXMLStreamReader(getTagsXmlInputStream());
			xsr.next();
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer t = tf.newTransformer();

			while (xsr.nextTag() == XMLStreamConstants.START_ELEMENT) {
				DOMResult result = new DOMResult();
				t.transform(new StAXSource(xsr), result);

				Node nNode = result.getNode();
				Element element = ((Document)nNode).getDocumentElement();


				String tagLabel =	getString("tagLabel",element);
				String tagId = getString("tagId",element);
				String externalId = getString("externalId",element);
				String description = getString("description",element);
				long externalCreationDate = stringToLong(getString("externalCreationDate",element),0L);
				long lastUpdateDateInExternalSystem = stringToLong(getString("lastUpdateDateInExternalSystem",element),0L);
				String externalHierarchyCode =getString("externalHierarchyCode",element);
				String externalType = getString("externalType",element);
				String alternativeLabels = getString("alternativeLabels",element);
				String tagCollectionId =  getString("tagCollectionId",element);

				if (collectionToUpdate!=null && !collectionToUpdateMoreThanOne){
					if (!(collectionToUpdate.equals(tagCollectionId))){
						collectionToUpdateMoreThanOne=true;
					}
				}else{
					collectionToUpdate=tagCollectionId;
				}
				String data =  getString("data",element);
				String parentId = getString("parentId",element);

				if (tagId !=null && tagId.length()==VALID_ID_LENGTH){
					if (tagWithIdIsPresent(tagId)){
					updateLabelWithId(tagId, externalId, tagCollectionId, tagLabel, description,
							alternativeLabels, externalCreationDate, lastUpdateDateInExternalSystem, parentId,
							externalHierarchyCode, externalType, data);
					}else {
						updateOrCreateTagWithCollectionId(externalId, tagCollectionId, tagLabel, description,
								alternativeLabels, externalCreationDate, lastUpdateDateInExternalSystem, parentId,
								externalHierarchyCode, externalType, data);
					}
				}else {
					updateOrCreateTagWithCollectionId(externalId, tagCollectionId, tagLabel, description,
							alternativeLabels, externalCreationDate, lastUpdateDateInExternalSystem, parentId,
							externalHierarchyCode, externalType, data);
				}
			}

			updateTagCollectionSynchronizationWithCollectionId(collectionToUpdate,0L);
			//We will delete the old ones when there is only one collectionID in the file.
			if (collectionToUpdate!=null && !collectionToUpdateMoreThanOne) {
				deleteTagsOlderThanDateFromCollectionWithCollectionId(collectionToUpdate, start);
			}
			sendStatusMail(1,"");
		}catch (Exception e){
			log.warn("Full Tags XML can't be processed",e);
			sendStatusMail(2,e.getMessage());
		}

		String collectionToUpdate=null;
		Boolean collectionToUpdateMoreThanOne=false;
		if(log.isInfoEnabled()) {
			log.info("Finished Full XML Tags synchronization in " + (System.currentTimeMillis() - start) + " ms");
		}
	}

	protected void sendStatusMail(int status,String extraInfo){

		String body;
		String subject;
		String from;
		String emailAddr= serverConfigurationService.getString("tags.import_job_email",serverConfigurationService.getString("portal.error.email"));

		if (StringUtils.isNotBlank(emailAddr)) {

			if (status == 1){
				body="The Full Tag XML import job has been successful";
				subject = "Full Tag XML import job success";
			}else{
				body="The Full Tag XML import jab had and error. look at the Sakai log for more information";
				subject = "Full Tag XML import job error";
			}

			from= "no-reply@" + serverConfigurationService.getServerName();


			if (emailService != null) {
				emailService.send(from, emailAddr, subject, body + "\n" + extraInfo, emailAddr, null, null);
			} else {
				log.error("Could not send email, no emailService");
			}
		}
	}


	/**
	 * @param serverConfigurationService The ServerConfigurationService.
	 */
	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService)
	{
		this.serverConfigurationService = serverConfigurationService;
	}


	public void setEmailService(EmailService emailService) {

		this.emailService = emailService;

	}

	public void init() {
		if(log.isInfoEnabled()) {
			log.info("init()");
		}
	}

	public void destroy() {
		if(log.isInfoEnabled()) {
			log.info("destroy()");
		}
	}

}
