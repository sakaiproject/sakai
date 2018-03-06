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
import java.util.Objects;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stax.StAXSource;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.email.api.EmailService;

/**
 * A quartz job to synchronize the TAGS with an
 * xml file available in sakai home.
 *
 *
 */
@Slf4j
public class TagsSyncJob extends TagSynchronizer implements Job {

	private static final String TAGSERVICE_IMPORT_JOB_EMAIL_PROPERTY = "tags.import_job_email";
	private static final String SAKAI_PORTAL_ERROR_EMAIL_PROPERTY = "portal.error.email";
	private String tagsPathToXml;
	private String tagCollectionsPathToXml;

	private ServerConfigurationService serverConfigurationService;
	private EmailService emailService;

	/**
	 * {@inheritDoc}
	 */
	public InputStream getTagsXmlInputStream() {
		tagsPathToXml = serverConfigurationService.getSakaiHomePath() + serverConfigurationService.getString("tags.tagsfile","tags/tags.xml");
		log.debug(tagsPathToXml);
		File xmlFile = new File(tagsPathToXml);
		InputStream targetStream = null;
				try {
					targetStream=new FileInputStream(xmlFile);
				} catch (Exception e){
					log.warn("The Tags file can't be found in the specified route: " + tagsPathToXml);
				}
		return targetStream;
	}

	public InputStream getTagCollectionssXmlInputStream() {
		tagCollectionsPathToXml = serverConfigurationService.getSakaiHomePath() + serverConfigurationService.getString("tags.tagcollectionsfile","tags/tagcollections.xml");
		log.debug(tagCollectionsPathToXml);
		File xmlFile = new File(tagCollectionsPathToXml);
		InputStream targetStream = null;
		try {
			targetStream=new FileInputStream(xmlFile);
		} catch (Exception e){
			log.warn("The Tags file can't be found in the specified route: " + tagCollectionsPathToXml);
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
			log.info("Starting Tag Collection synchronization");
		}

		try{
			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLStreamReader xsr = factory.createXMLStreamReader(getTagCollectionssXmlInputStream());
			xsr.next();
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer t = tf.newTransformer();

			while (xsr.nextTag() == XMLStreamConstants.START_ELEMENT) {
				DOMResult result = new DOMResult();
				t.transform(new StAXSource(xsr), result);

				Node nNode = result.getNode();
				Element element = ((Document)nNode).getDocumentElement();

				String name = getString("Name",element);
				log.debug("Found name: " + name);
				String description = getString("Description",element);
				log.debug("Found description : " + description);
				String externalSourceName =  getString("ExternalSourceName",element);
				log.debug("externalSourceName: " + externalSourceName);
				String externalSourceDescription = getString("ExternalSourceDescription",element);
				log.debug("externalSourceDescription: " + externalSourceDescription);
				long lastUpdateDateInExternalSystem = xmlDateToMs(element.getElementsByTagName("DateRevised").item(0),name);
				log.debug("lastUpdateDateInExternalSystem: " + lastUpdateDateInExternalSystem);

				updateOrCreateTagCollection(name, description,
						externalSourceName, externalSourceDescription, lastUpdateDateInExternalSystem);
			}

			sendStatusMail(1,"");
		}catch (Exception e){
			log.warn("Error Synchronizing the Tags from an xml file:",e);
			sendStatusMail(2,e.getMessage());
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
				String action = element.getAttribute("Action");
				String tagLabel =	getString("TagLabel",element);
				log.debug("Found tagLabel: " + tagLabel);
				String externalId = getString("ExternalId",element);
				log.debug("Found externalId: " + externalId);
				String description = getString("Description",element);
				log.debug("Found description : " + description);
				long externalCreationDate = xmlDateToMs(element.getElementsByTagName("DateCreated").item(0),tagLabel);
				log.debug("externalCreationDate: " + externalCreationDate);
				long lastUpdateDateInExternalSystem = xmlDateToMs(element.getElementsByTagName("DateRevised").item(0),tagLabel);
				log.debug("lastUpdateDateInExternalSystem: " + lastUpdateDateInExternalSystem);
				String externalHierarchyCode =getString("HierarchyCode",element);
				log.debug("externalHierarchyCode: " + externalHierarchyCode);
				String externalType = getString("Type",element);
				log.debug("externalType: " + externalType);
				String alternativeLabels = getString("AlternativeLabels",element);
				log.debug("alternativeLabels: " + alternativeLabels);
				String externalSourceName =  getString("ExternalSourceName",element);
				log.debug("externalSourceName: " + externalSourceName);
				String data =  getString("Data",element);
				log.debug("data: " + data);
				String parentId = getString("ParentId",element);
				log.debug("parentId: " + parentId);

				if (Objects.equals(action,"delete")){
					deleteTagFromExternalCollection(externalId,externalSourceName);
				}else{
					updateOrCreateTagWithExternalSourceName(externalId, externalSourceName,  tagLabel,  description,
							alternativeLabels,  externalCreationDate,  lastUpdateDateInExternalSystem,  parentId,
							externalHierarchyCode,  externalType,  data);
				}

				updateTagCollectionSynchronization(externalSourceName,0L);
			}
			sendStatusMail(1,"");
		}catch (Exception e){
			log.warn("Error Synchronizing the Tags from an xml file:",e);
			sendStatusMail(2,e.getMessage());
		}
		if(log.isInfoEnabled()) {
			log.info("Finished Tags synchronization in " + (System.currentTimeMillis()-start) + " ms");
		}

	}

	protected void sendStatusMail(int status,String extraInfo){

		String body;
		String subject;
		String from;
		String emailAddr= serverConfigurationService.getString(TAGSERVICE_IMPORT_JOB_EMAIL_PROPERTY,serverConfigurationService.getString(SAKAI_PORTAL_ERROR_EMAIL_PROPERTY));

		if (StringUtils.isNotBlank(emailAddr)) {

			if (status == 1){
				body="The Tag XML import job has been successful";
				subject = "Tag XML import job success";
			}else{
				body="The Tag XML import jab had and error. look at the Sakai log for more information";
				subject = "Tag XML import job error";
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
