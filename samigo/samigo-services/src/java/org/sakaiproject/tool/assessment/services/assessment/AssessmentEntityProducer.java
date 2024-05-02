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
package org.sakaiproject.tool.assessment.services.assessment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import org.sakaiproject.authz.api.Member;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.samigo.api.SamigoReferenceReckoner;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.assessment.data.dao.assessment.Answer;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemText;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.questionpool.QuestionPoolDataIfc;
import org.sakaiproject.tool.assessment.data.dao.assessment.*;
import org.sakaiproject.tool.assessment.data.dao.questionpool.QuestionPoolItemData;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacadeQueries;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.facade.SectionFacade;
import org.sakaiproject.tool.assessment.shared.api.questionpool.QuestionPoolServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.qti.QTIServiceAPI;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AssessmentEntityProducer implements EntityTransferrer, EntityProducer {

    private static final int QTI_VERSION = 1;
    private static final String ARCHIVED_ELEMENT = "assessment";
    public static final String REFERENCE_ROOT = Entity.SEPARATOR + "samigo";
    private QTIServiceAPI qtiService;
    
    @Getter @Setter protected EntityManager entityManager;
    @Getter @Setter protected ServerConfigurationService serverConfigService;
    @Getter @Setter protected ContentHostingService contentHostingService;
    @Getter @Setter protected QuestionPoolServiceAPI questionPoolService;
    @Getter @Setter protected SiteService siteService;
    @Getter @Setter protected UserDirectoryService userDirectoryService;
    @Getter @Setter protected PublishedAssessmentFacadeQueriesAPI publishedAssessmentFacadeQueries;

	public void init() {
		log.info("init()");
		try {
			entityManager.registerEntityProducer(this, REFERENCE_ROOT);
		} catch (Exception e) {
			log.warn("Error registering Samigo Entity Producer", e);
		}
	}

	public void destroy() {
	}

    public void setQtiService(QTIServiceAPI qtiService)  {
        this.qtiService = qtiService;
    }

	public String[] myToolIds() {
		String[] toolIds = { "sakai.samigo" };
		return toolIds;
	}

	@Override
	public List<Map<String, String>> getEntityMap(String fromContext) {

        AssessmentService assessmentService = new AssessmentService();
        return assessmentService.getAllActiveAssessmentsbyAgent(fromContext).stream()
			.map(ass -> Map.of("id", ass.getAssessmentId().toString(), "title", ass.getTitle())).collect(Collectors.toList());
	}

	public Map<String, String> transferCopyEntities(String fromContext, String toContext, List<String> ids, List<String> transferOptions) {

		AssessmentService service = new AssessmentService();
		Map<String, String> transversalMap = new HashMap<String, String>();
		service.copyAllAssessments(fromContext, toContext, ids, transversalMap);
		
		// At a minimum, we need to remap all the attachment URLs to point to the new site

		transversalMap.put("/content/attachment/" + fromContext + "/", "/content/attachment/" + toContext + "/");
		return transversalMap;
	}

    /*
     * Archive draft and published assessments and question pools referenced by assessments
     * @return Details of what was archived
     */
    public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments) {

        StringBuilder results = new StringBuilder();
        results.append("archiving ").append(getLabel()).append("\n");

        String qtiPath = archivePath + File.separator + "qti";
        File qtiDir = new File(qtiPath);
        if (!qtiDir.isDirectory() && !qtiDir.mkdir()) {
            log.error("Could not create directory " + qtiPath);
            results.append("Could not create " + qtiPath + "\n");
            return  results.toString();
        }

        Element element = doc.createElement(this.getClass().getName());
        ((Element) stack.peek()).appendChild(element);
        stack.push(element);
        AssessmentService assessmentService = new AssessmentService();

        // Question pools referenced in draft and published assessments
        Set<String> poolIds = new TreeSet<String>();

	// Attachments and inline resources referenced in draft and published assssments
	Set<String> resourceIds = new TreeSet<String>();

        // Draft assessments
        List<AssessmentData> assessmentList 
                = (List<AssessmentData>) assessmentService.getAllActiveAssessmentsbyAgent(siteId);
        for (AssessmentData data : assessmentList) {

            Element assessmentXml = doc.createElement(ARCHIVED_ELEMENT);
            String id = data.getAssessmentId().toString();
            assessmentXml.setAttribute("id", id);

	    // QTI representation
	    String assessmentQti = qtiService.getExportedAssessmentAsString(id, QTI_VERSION);
	    Document assessment = qtiService.getExportedAssessment(id, QTI_VERSION);

	    // Write it out
            FileWriter writer = null;
            try {
                File assessmentFile = new File(qtiPath + File.separator + ARCHIVED_ELEMENT + id + ".xml");
                writer = new FileWriter(assessmentFile);
                writer.write(assessmentQti);
            } catch (IOException e) {
                results.append(e.getMessage() + "\n");
                log.error(e.getMessage(), e);
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (Throwable t) {
                        log.error(t.getMessage(), t);
                    }
                } 
            }
            element.appendChild(assessmentXml);

	    // Question pool IDs for random draw (if used)
	    poolIds.addAll(assessmentService.getQuestionPoolIdsForAssessment(data.getAssessmentId(), false));

	    // Attachments and inline references
	    resourceIds.addAll(getAttachmentResourceIds(assessment.getElementsByTagName("qtimetadatafield")));
	    resourceIds.addAll(getInlineResourceIds(siteId, assessment.getElementsByTagName("mattext")));

        } // draft

	// Published assessments
	PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
	List<PublishedAssessmentData> publishedAssessmentList = publishedAssessmentService.getAllPublishedAssessmentsForSite(siteId);
	for (PublishedAssessmentData data : publishedAssessmentList) {

		Element assessmentXml = doc.createElement(ARCHIVED_ELEMENT);
		String publishedAssessmentId = data.getPublishedAssessmentId().toString();

		String assessmentQti = qtiService.getExportedPublishedAssessmentAsString(publishedAssessmentId, QTI_VERSION);
		Document assessment = qtiService.getExportedPublishedAssessment(publishedAssessmentId, QTI_VERSION);

		assessmentXml.setAttribute("id", String.format("pub%s", publishedAssessmentId));
		assessmentXml.setAttribute("published", "true");
		assessmentXml.setAttribute("baseId", data.getAssessmentBaseId().toString());

		FileWriter writer = null;
		try {
			File assessmentFile = new File(qtiPath + File.separator + ARCHIVED_ELEMENT + String.format("pub%s", publishedAssessmentId) + ".xml");
			writer = new FileWriter(assessmentFile);
			writer.write(assessmentQti);
		} catch (IOException e) {
			results.append(e.getMessage() + "\n");
			log.error(e.getMessage(), e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (Throwable t) {
					log.error(t.getMessage(), t);
				}
			}
		}
		element.appendChild(assessmentXml);

		// Question pool IDs for random draw (if used)
		poolIds.addAll(assessmentService.getQuestionPoolIdsForAssessment(data.getPublishedAssessmentId(), true));

		// Attachments and inline references
		resourceIds.addAll(getAttachmentResourceIds(assessment.getElementsByTagName("qtimetadatafield")));
		resourceIds.addAll(getInlineResourceIds(siteId, assessment.getElementsByTagName("mattext")));

	} // published

	// Question Pools
        results.append(exportQuestionPools(siteId, archivePath, poolIds, resourceIds));

	// Add the attachment references
	for (String resourceId : resourceIds) {
		ContentResource resource = null;
		try {
			resource = contentHostingService.getResource(resourceId);
		} catch (PermissionException e) {
			log.warn("Permission error fetching attachment: {}", resourceId);
			continue;
		} catch (TypeException e) {
			log.warn("TypeException error fetching attachment: {}", resourceId);
			continue;
		} catch (IdUnusedException e) {
			log.warn("IdUnusedException error fetching attachment: {}", resourceId);
			continue;
		}
		attachments.add(entityManager.newReference(resource.getReference()));
	}

	// Done
        stack.pop();

	return results.toString();
	}

	public String getEntityUrl(Reference ref) {
		if (StringUtils.isNotBlank(ref.getId())) {
			Long id = Long.parseLong(ref.getId());
			PublishedAssessmentFacade paf = publishedAssessmentFacadeQueries.getPublishedAssessment(id);
			if (paf != null) {
				String alias = paf.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.ALIAS);
				return serverConfigService.getServerUrl() + "/samigo-app/servlet/Login?id=" + alias;
			}
		} 
		return null;
	}

	public Optional<String> getEntityUrl(Reference ref, Entity.UrlType urlType) {

        SamigoReferenceReckoner.SamigoReference samigoRef
            = SamigoReferenceReckoner.reckoner().reference(ref.getReference()).reckon();
        try {
            Long id = Long.parseLong(samigoRef.getId());
            Site site = siteService.getSite(samigoRef.getSite());
            ToolConfiguration tc = site.getToolForCommonId(SamigoConstants.TOOL_ID);
            if (tc != null) {
                return Optional.of("/portal/site/" + samigoRef.getSite() + "/tool/" + tc.getId() + "/jsf/evaluation/totalScores?publishedId=" + id);
            }
        } catch (NumberFormatException nfe) {
            log.error("{} is not a valid Samigo id", samigoRef.getId());
        } catch (IdUnusedException idue) {
            log.error("No site for id {}", samigoRef.getSite());
        }
		return Optional.empty();
	}

	public String getLabel() {
		return "samigo";
	}

	public String merge(String siteId, Element root, String archivePath,
			String fromSiteId, Map attachmentNames, Map userIdTrans,
			Set userListAllowImport) {
	if (log.isDebugEnabled()) log.debug("merging " + getLabel());
        StringBuilder results = new StringBuilder();
        String qtiPath = (new File(archivePath)).getParent() 
                         + File.separator + "qti" + File.separator;
        //TODO: replaced by getChildren when we make sure we have the
        NodeList assessments = root.getElementsByTagName(ARCHIVED_ELEMENT);

        DocumentBuilder dbuilder = null;
        try {
            dbuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            return getLabel() + " Error: " + t.getMessage();
        }

        for (int i=0; i<assessments.getLength(); ++i) {
            Element element = (Element) assessments.item(i);
            String id = element.getAttribute("id");
            String path = qtiPath + ARCHIVED_ELEMENT + id + ".xml";
            try {
                AssessmentIfc assessment = qtiService.createImportedAssessment(path, QTI_VERSION,  siteId);
                results.append(getLabel() + " imported assessment '" + assessment.getTitle() + "'\n");            
            } catch (Throwable t) {
                log.error(t.getMessage(), t);
                results.append(getLabel() + " error with assessment "  
                               + id + ": " + t.getMessage() + "\n");
            }
        }
        return results.toString();
	}

	public boolean parseEntityReference(String reference, Reference ref) {
		if (StringUtils.startsWith(reference, REFERENCE_ROOT)) {
			String[] parts = StringUtils.splitPreserveAllTokens(reference, Entity.SEPARATOR);
			if (parts.length >= 3) {
				ref.set("sakai:samigo", ARCHIVED_ELEMENT, parts[3], parts[2], parts[2]);	
			}
			return true;
		}
		return false;
	}

	public boolean willArchiveMerge() {
		return true;
	}

	public Map<String, String> transferCopyEntities(String fromContext, String toContext, List<String> ids, List<String> options, boolean cleanup) {

		try {
			if (cleanup) {
				if (log.isDebugEnabled()) log.debug("deleting assessments from " + toContext);
				AssessmentService service = new AssessmentService();
				List assessmentList = service.getAllActiveAssessmentsbyAgent(toContext);
				log.debug("found " + assessmentList.size() + " assessments in site: " + toContext);
				for (Iterator iter = assessmentList.iterator(); iter.hasNext();) {
					AssessmentData oneassessment = (AssessmentData) iter.next();
					log.debug("removing assessemnt id = " +oneassessment.getAssessmentId() );
					service.removeAssessment(oneassessment.getAssessmentId().toString());
				}
			}
		} catch (Exception e) {
			log.error("transferCopyEntities: End removing Assessment data", e);
		}
		
		return transferCopyEntities(fromContext, toContext, ids, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateEntityReferences(String toContext, Map<String, String> transversalMap){
		if(transversalMap != null && transversalMap.size() > 0){

			Set<Entry<String, String>> entrySet = (Set<Entry<String, String>>) transversalMap.entrySet();

			AssessmentService service = new AssessmentService();
		
			List assessmentList = service.getAllActiveAssessmentsbyAgent(toContext);			
			Iterator assessmentIter =assessmentList.iterator();
			while (assessmentIter.hasNext()) {
				AssessmentData assessment = (AssessmentData) assessmentIter.next();		
				//get initialized assessment
				AssessmentFacade assessmentFacade = (AssessmentFacade) service.getAssessment(assessment.getAssessmentId());		
				boolean needToUpdate = false;
				
				String assessmentDesc = assessmentFacade.getDescription();
				if(assessmentDesc != null){
					assessmentDesc = org.sakaiproject.util.cover.LinkMigrationHelper.migrateAllLinks(entrySet, assessmentDesc);
					if(!assessmentDesc.equals(assessmentFacade.getDescription())){
						//need to save since a ref has been updated:
						needToUpdate = true;
						assessmentFacade.setDescription(assessmentDesc);
					}
				}
				
				List sectionList = assessmentFacade.getSectionArray();
				for(int i = 0; i < sectionList.size(); i++){
					SectionFacade section = (SectionFacade) sectionList.get(i);
					String sectionDesc = section.getDescription();
					if(sectionDesc != null){
						sectionDesc = org.sakaiproject.util.cover.LinkMigrationHelper.migrateAllLinks(entrySet, sectionDesc);
						if(!sectionDesc.equals(section.getDescription())){
							//need to save since a ref has been updated:
							needToUpdate = true;
							section.setDescription(sectionDesc);
						}
					}
					
					List itemList = section.getItemArray();
					for(int j = 0; j < itemList.size(); j++){
						ItemData item = (ItemData) itemList.get(j);
						
						
						String itemIntr = item.getInstruction();
						if(itemIntr != null){
							itemIntr = org.sakaiproject.util.cover.LinkMigrationHelper.migrateAllLinks(entrySet, itemIntr);
							if(!itemIntr.equals(item.getInstruction())){
								//need to save since a ref has been updated:
								needToUpdate = true;
								item.setInstruction(itemIntr);
							}
						}
						
						String itemDesc = item.getDescription();
						if(itemDesc != null){
							itemDesc = org.sakaiproject.util.cover.LinkMigrationHelper.migrateAllLinks(entrySet, itemDesc);
							if(!itemDesc.equals(item.getDescription())){
								//need to save since a ref has been updated:
								needToUpdate = true;
								item.setDescription(itemDesc);
							}
						}
						
						List itemTextList = item.getItemTextArray();
						if(itemTextList != null){
							for(int k = 0; k < itemTextList.size(); k++){
								ItemText itemText = (ItemText) itemTextList.get(k);
								String text = itemText.getText();
								if(text != null){
									// Transfer all of the attachments to the new site
									text = service.copyContentHostingAttachments(text, toContext);
									
									text = org.sakaiproject.util.cover.LinkMigrationHelper.migrateAllLinks(entrySet, text);
									if(!text.equals(itemText.getText())){
										//need to save since a ref has been updated:
										needToUpdate = true;
										itemText.setText(text);
									}
								}
								
								List answerSetList = itemText.getAnswerArray();
								if (answerSetList != null) {
									for (int l = 0; l < answerSetList.size(); l++) {
										Answer answer = (Answer) answerSetList.get(l);
										String answerText = answer.getText();
										
										if (answerText != null) {
											// Transfer all of the attachments embedded in the answer text
											answerText = service.copyContentHostingAttachments(answerText, toContext);
											
											// Now rewrite the answerText with links to the new site
											answerText = org.sakaiproject.util.cover.LinkMigrationHelper.migrateAllLinks(entrySet, answerText);
											
											if (!answerText.equals(answer.getText())) {
												needToUpdate = true;
												answer.setText(answerText);
											}
										}
									}
								}
								
								
							}
						}	
						
					}					
				}
				
				if(needToUpdate){
					//since the text changes were direct manipulations (no iterators),
					//hibernate will take care of saving everything that changed:
					service.saveAssessment(assessmentFacade);
				}
			}
		}
	}

	private String exportQuestionPools(String siteId, String archivePath, Set<String> questionPoolIds, Set<String> resourceIds) {

		String xmlPath = archivePath + File.separator + "samigo_question_pools.xml";

		int pools_exported = 0;
		int archive_warnings = 0;

		StringBuilder warnings = new StringBuilder();

		try {
			Site site = siteService.getSite(siteId);

			if (site.getToolForCommonId("sakai.samigo") == null) {
				return "T&Q not used in this site: skipping Question Pool archive\n";
			}

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			Element questionPools = doc.createElement("QuestionPools");

			List targetPools = new ArrayList<>();

			// Pools used in assessments in the site
			log.info("Exporting question pools used in site {}", siteId);
			for (String poolId : questionPoolIds) {
				targetPools.add(questionPoolService.getPool(new Long(poolId), null));
			}

			// Export each pool
			for (Object poolObj : targetPools) {

				// Attachments and inline references
				Set<String> poolResourceIds = new TreeSet<String>();

				Element questionPool = doc.createElement("QuestionPool");
				QuestionPoolDataIfc pool = (QuestionPoolDataIfc)poolObj;

				if (pool == null) {
					log.warn("Question pool not found");
					continue;
				}

				questionPool.setAttribute("id", String.valueOf(pool.getQuestionPoolId()));
				questionPool.setAttribute("title", pool.getTitle());
				questionPool.setAttribute("ownerId", pool.getOwnerId());

				log.info("Exporting question pool id {} title '{}' owner {}",
					pool.getQuestionPoolId(), pool.getTitle(), pool.getOwnerId());

				if (pool.getParentPoolId() != null && pool.getParentPoolId() != 0L) {
					questionPool.setAttribute("parentId", String.valueOf(pool.getParentPoolId()));
				}

				questionPool.setAttribute("sourcebank_ref", String.format("%d::%s", pool.getQuestionPoolId(), pool.getTitle()));

				for (Object itemObj : pool.getQuestionPoolItems()) {
				    try {

					QuestionPoolItemData item = (QuestionPoolItemData)itemObj;
					Document qpItem = qtiService.getExportedItem(String.valueOf(item.getItemId()), QTI_VERSION);
					NodeList nodes = qpItem.getChildNodes();

					for (int i=0; i<nodes.getLength(); ++i) {
						Element node = (Element) nodes.item(i);
						questionPool.appendChild(doc.adoptNode(node));
					}

				    } catch (Exception e) {
					String poolError = String.format("Caught an exception while exporting question pool (id=%s; title=%s) for owner %s: %s",
						pool.getQuestionPoolId(), pool.getTitle(), pool.getOwnerId(), e.getMessage());
					log.error(poolError, e);
					throw new RuntimeException(poolError);
				    }
				}

				// Attachments and inline references
				poolResourceIds.addAll(getInlineResourceIds(siteId, questionPool.getElementsByTagName("mattext")));
				poolResourceIds.addAll(getAttachmentResourceIds(questionPool.getElementsByTagName("qtimetadatafield")));

				for (String resourceId : poolResourceIds) {
					ContentResource resource = null;
					try {
						resource = contentHostingService.getResource(resourceId);
					} catch (PermissionException e) {
						log.warn("Permission error fetching attachment: {}", resourceId);
					} catch (TypeException e) {
						log.warn("TypeException error fetching attachment: {}", resourceId);
					} catch (IdUnusedException e) {
						log.warn("IdUnusedException error fetching attachment: {}", resourceId);
					}
					if (resource != null) {
						resourceIds.add(resourceId);
					} else {
						log.warn("Unable to archive attachment for resource {} in question pool (id={}; title={}) for owner {}",
							resourceId, pool.getQuestionPoolId(), pool.getTitle(), pool.getOwnerId());
					}
				}

				questionPools.appendChild(questionPool);
				pools_exported++;

			} // for

			doc.appendChild(questionPools);

			FileWriter writer = null;
			try {
				File file = new File(xmlPath);
				writer = new FileWriter(file);

				TransformerFactory tFactory = TransformerFactory.newInstance();
				Transformer transformer = tFactory.newTransformer();

				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(writer);
				transformer.transform(source, result);
			} catch (IOException | TransformerException e) {
				log.error("Unable to write XML to {}: {}", xmlPath, e.getMessage());
			} finally {
				if (writer != null) {
					try {
						writer.close();
					} catch (Throwable t) {
					}
				}
			}
		} catch (ParserConfigurationException | IdUnusedException e) {
			log.error("Unable to export question pools for site {}: {}", siteId, e.getMessage());
		}

		return String.format("archived %d question pool(s) with %d warning(s)\n%s", pools_exported, archive_warnings, warnings.toString());
	}

    /*
     * Parse a qtimetadatafield/fieldentry plain text list of attachment references, formatted like:
     *   /attachment/SITEID/Tests_Quizzes/UID/filenamewithoutspaces.ext|filename with spaces.ext|content/type
     */
    private List<String> parseAttachmentResourceIds(Element e) {

	if (!"ATTACHMENT".equals(getChildElementValue(e, "fieldlabel"))) {
		return Collections.emptyList();
	}

	String qText = getChildElementValue(e, "fieldentry");

	if (StringUtils.isEmpty(qText) || !qText.contains("/attachment")) {
		return Collections.emptyList();
	}

        List<String> result = new ArrayList<>();

	String[] attachmentRefs = qText.split("\n");
	for (String attachmentRef : attachmentRefs) {

		// Restore the spaces in the attachment path that the QTI code removes
		String[] attachmentParts = attachmentRef.split("\\|");
		String realName = attachmentParts[1];
		String attachmentId = attachmentParts[0].replace("/Tests_Quizzes/","/Tests _ Quizzes/").replace(realName.replace(" ",""), realName);

		result.add(attachmentId);
		log.info("Found attachment: {}", attachmentId);
	}

	return result;
    }

    /*
     * Parse an HTML text blob to extract inline URLs and turn them back into
     * Sakai references. Similar to
     * https://github.com/cilt-uct/sakai/blob/21.x/lessonbuilder/tool/src/java/org/sakaiproject/lessonbuildertool/service/LessonBuilderEntityProducer.java#L470
     */
    private List<String> parseInlineResourceRefs(String siteId, String qText) {

	if (StringUtils.isEmpty(qText) || !qText.contains("<") || qText.equals("<![CDATA[]]>")) {
		return Collections.emptyList();
	}

	if (qText.contains("CDATA")) {
		qText = qText.replace("<![CDATA[", "").replace("]]>", "");
	}

        List<String> result = new ArrayList<>();
	org.jsoup.nodes.Document htmlDoc = Jsoup.parse(qText);

        // Typically audio or video <source> or <img>
	Elements media = htmlDoc.select("[src]");

	for (org.jsoup.nodes.Element src : media) {
		String link = src.attr("abs:src");

		// embedded fckeditor attachments
		if (link.contains("/access/content/attachment/")) {
			String linkRef = link.replace(link.substring(0, link.indexOf("/attachment/")), "");
			result.add(linkRef);
			log.info("Found inline attachment asset: {} adding to attachment list as: {}", link, linkRef);
			continue;
		}

		// URLs below may include spaces, so URLDecode to turn back into a Sakai content ID
		try {
			// cross-site references
			if (link.contains("/access/content/group/") && !link.contains(siteId)) {
				String linkRef = URLDecoder.decode(link.replace(link.substring(0, link.indexOf("/group/")), ""), "UTF-8");
				result.add(linkRef);
				log.info("Found inline cross-site asset: {} adding to attachment list as: {}", link, linkRef);
			}

			// references to user workspace files: map the user eid in the path to id
			if (link.contains("/access/content/user/")) {
				String linkRef = URLDecoder.decode(link.replace(link.substring(0, link.indexOf("/user/")), ""), "UTF-8");
				String linkParts[] = linkRef.split("/", 4);
				String userId = userDirectoryService.getUserId(linkParts[2]);
				linkRef = "/user/" + userId + "/" + linkParts[3];
				result.add(linkRef);
				log.info("Found inline user workspace asset: {} adding to attachment list as: {}", link, linkRef);
			}

			// references to public files
			if (link.contains("/access/content/public/")) {
				String linkRef = URLDecoder.decode(link.replace(link.substring(0, link.indexOf("/public/")), ""), "UTF-8");
				result.add(linkRef);
				log.info("Found inline public asset: {} adding to attachment list as: {}", link, linkRef);
			}

		} catch (UnsupportedEncodingException|UserNotDefinedException e) {
			log.warn("Unable to add link {} to attachment list: {}", link, e.getMessage());
		}

	}

	return result;
    }

    /*
     * Fetch references from inline URLs contained in mattext elements
     * @return List of resource references
     */
    private List<String> getInlineResourceIds(String siteId, NodeList list) {
        List<String> result = new ArrayList<>();
	for (int i = 0; i < list.getLength(); i++) {
		Element e = (Element) list.item(i);
		result.addAll(parseInlineResourceRefs(siteId, e.getTextContent()));
	}
        return result;
    }

    /*
     * Fetch text value of a child element
     * @return Text content of the first child element matching the tag name,
     * 	otherwise null if there is no matching element
     */
    private String getChildElementValue(Element e, String childName) {
        NodeList list = e.getElementsByTagName(childName);
	if (list.getLength() > 0) {
                Element c = (Element) list.item(0);
		return c.getTextContent();
	}

	return null;
    }

    /*
     * Fetch references from attachment URLs contained in qtimetadatafield elements
     * @return List of resource references
     */
    private List<String> getAttachmentResourceIds(NodeList list) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < list.getLength(); i++) {
                Element e = (Element) list.item(i);
		result.addAll(parseAttachmentResourceIds(e));
        }
        return result;
    }
}
