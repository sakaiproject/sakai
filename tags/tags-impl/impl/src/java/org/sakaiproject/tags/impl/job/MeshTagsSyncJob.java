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
import java.io.InputStream;
import java.io.FileInputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.dom.DOMResult;
import javax.xml.stream.XMLStreamException;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.email.api.EmailService;

/**
 * A quartz job to synchronize the TAGS with an
 * xml file available in sakai home.
 *
 *
 */
@Slf4j
public class MeshTagsSyncJob extends TagSynchronizer implements Job {

	private String pathToXml;
	private ServerConfigurationService serverConfigurationService;
	private EmailService emailService;
	private int counterSuccess=0;
	private int counterTotal=0;
	private String lastSuccessfulLabel = "";

	/**
	 * {@inheritDoc}
	 */
	public InputStream getTagsXmlInputStream() {
		pathToXml = serverConfigurationService.getSakaiHomePath() + serverConfigurationService.getString("tags.meshcollectionfile","tags/mesh.xml");
		log.debug(pathToXml);
		File xmlFile = new File(pathToXml);
		InputStream targetStream = null;
				try {
					targetStream=new FileInputStream(xmlFile);
				} catch (Exception e){
					log.warn("The Mesh file can't be found in the specified route: " + pathToXml);
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
			log.info("Starting MESH Tag Collection synchronization");
		}
		try{
			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLStreamReader xsr = factory.createXMLStreamReader(getTagsXmlInputStream());
			xsr.next();
			xsr.nextTag();
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer t = tf.newTransformer();

			while (xsr.nextTag() == XMLStreamConstants.START_ELEMENT) {
				DOMResult result = new DOMResult();
				t.transform(new StAXSource(xsr), result);

				Node nNode = result.getNode();
				Element element = ((Document)nNode).getDocumentElement();
				String tagLabel="undefined";
				counterTotal++;

				try {
					Element descriptorName = (Element) element.getElementsByTagName("DescriptorName").item(0);
					tagLabel =	getString("String",descriptorName);
					String externalId = getString("DescriptorUI",element);
					String description = getString("Annotation",element);
					long externalCreationDate = xmlDateToMs(element.getElementsByTagName("DateCreated").item(0),externalId);
					long lastUpdateDateInExternalSystem = xmlDateToMs(element.getElementsByTagName("DateRevised").item(0),externalId);
					String externalHierarchyCode = xmlTreeToString(element.getElementsByTagName("TreeNumberList").item(0),externalId);
					String externalType = element.getAttribute("DescriptorClass");
					String alternativeLabels = xmlToAlternativeLabels(element.getElementsByTagName("ConceptList").item(0), externalId);
					updateOrCreateTagWithExternalSourceName(externalId, "MESH",  tagLabel,  description,
							alternativeLabels,  externalCreationDate, lastUpdateDateInExternalSystem,  null,
							externalHierarchyCode,  externalType,  null);
					counterSuccess++;
					lastSuccessfulLabel=tagLabel;
				} catch (Exception e) {
					log.warn("Mesh XML can't be processed for this Label: " + tagLabel + ". If the value is undefined, then, the previous successful label was: " + lastSuccessfulLabel,e);
					sendStatusMail(2,e.getMessage());
				}
				if(counterTotal%1000==0){
					log.info(counterSuccess + "/" + counterTotal + " labels processed correctly... and still processing. " + (counterTotal - counterSuccess) + " errors by the moment");
				}


			} // end while
			xsr.close();
			updateTagCollectionSynchronization("MESH",0L);
			deleteTagsOlderThanDateFromCollection("MESH",start);
			sendStatusMail(1,"Imported from MESH finished. Num of labels processed successfully " + counterSuccess + "of" + counterTotal);
		} catch (XMLStreamException ex) {
			log.warn("Mesh XML can't be processed",ex);
		} catch (Exception e){
			log.warn("Mesh XML can't be processed",e);
			sendStatusMail(2,e.getMessage());
		}

		if(log.isInfoEnabled()) {
			log.info("Finished Mesh Tags synchronization in " + (System.currentTimeMillis()-start) + " ms");
		}
		counterTotal=0;
		counterSuccess=0;
		lastSuccessfulLabel="";
	}

	//These are particulars for mesh format
	private String xmlTreeToString(Node nNode, String externalId) {
		Element element = (Element) nNode;
		String treeNumberList = "";
		boolean first = true;
		try {
			NodeList treeNumber = element.getElementsByTagName("TreeNumber");
			for (int i = 0; i < treeNumber.getLength(); i++) {
				Node childElement = (Node) treeNumber.item(i);
				if (!first) {
					treeNumberList += ", ";
				}
				treeNumberList += childElement.getTextContent();
				first = false;
			}
			return treeNumberList;
		}catch (Exception e){
			log.debug("Treenumber Mesh XML can't be processed in:" + externalId,e);
			return null;
		}
	}

	private String xmlToAlternativeLabels(Node nNode, String externalId) {
        //Procesing the dates
		Element element = (Element) nNode;
		String alternativeLabelsList = "";

		try {
			NodeList concept = element.getElementsByTagName("Concept");
			for (int i = 0; i < concept.getLength(); i++) {
				Element conceptNode = (Element) concept.item(i);
				Element conceptName = (Element) conceptNode.getElementsByTagName("ConceptName").item(0);
				alternativeLabelsList += "Concept: " + getString("String",conceptName)+"\r\n";
				String scope = getString("ScopeNote",conceptNode);
				if (scope != null) {
					scope = "Scope Note: " + scope + "\r\n";
					alternativeLabelsList += scope;
				}
				boolean first = true;
				//Element termList = (Element) conceptNode.getElementsByTagName("TermList").item(0);
				NodeList term = element.getElementsByTagName("Term");
				for (int j = 0; j < term.getLength(); j++) {
					Element termNode = (Element) term.item(j);
					if (!first) {
						alternativeLabelsList += ", ";
					}else{
						alternativeLabelsList += "Terms: ";
					}
					alternativeLabelsList += getString("String",termNode);
					first = false;
				}
				alternativeLabelsList +="\r\n\r\n";
			}
			return alternativeLabelsList;
		}catch (Exception e){
			log.warn("AlternativeLabels in Mesh XML can't be processed at" +  externalId,e);
			return null;
		}
	}

	protected void sendStatusMail(int status,String extraInfo){

		String body;
		String subject;
		String from;
		String emailAddr= serverConfigurationService.getString("tags.import_job_email",serverConfigurationService.getString("portal.error.email"));

		if (StringUtils.isNotBlank(emailAddr)) {

			if (status == 1){
				body="The Mesh Tag XML import job has been successful";
				subject = "Mesh Tag XML import job success";
			}else{
				body="The Mesh Tag XML import jab had and error. look at the Sakai log for more information";
				subject = "Mesh Tag XML import job error";
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
		if(log.isInfoEnabled()) log.info("init()");
	}

	public void destroy() {
		if(log.isInfoEnabled()) log.info("destroy()");
	}

}
