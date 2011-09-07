package org.sakaiproject.tool.assessment.services.assessment;

import org.sakaiproject.entity.cover.EntityManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.Iterator;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.EntityTransferrerRefMigrator;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemText;
import org.sakaiproject.tool.assessment.data.dao.assessment.SectionData;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.SectionFacade;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class AssessmentEntityProducer implements EntityTransferrer,
		EntityProducer, EntityTransferrerRefMigrator {

	private static Log log = LogFactory.getLog(AssessmentEntityProducer.class);

	public void init() {
		log.info("init()");
		try {
			EntityManager.registerEntityProducer(this, Entity.SEPARATOR
					+ "samigo");
		} catch (Exception e) {
			log.warn("Error registering Samigo Entity Producer", e);
		}
	}

	public void destroy() {
	}

	public String[] myToolIds() {
		String[] toolIds = { "sakai.samigo" };
		return toolIds;
	}

        public void transferCopyEntities(String fromContext, String toContext, List resourceIds)
        {
                transferCopyEntitiesRefMigrator(fromContext, toContext, resourceIds); 
        }

        public Map<String, String> transferCopyEntitiesRefMigrator(String fromContext, String toContext, List resourceIds)
	{
		AssessmentService service = new AssessmentService();
		service.copyAllAssessments(fromContext, toContext);
		return null;
	}

	public String archive(String siteId, Document doc, Stack stack,
			String archivePath, List attachments) {
		return null;
	}

	public Entity getEntity(Reference ref) {
		return null;
	}

	public Collection getEntityAuthzGroups(Reference ref, String userId) {
		return null;
	}

	public String getEntityDescription(Reference ref) {
		return null;
	}

	public ResourceProperties getEntityResourceProperties(Reference ref) {
		return null;
	}

	public String getEntityUrl(Reference ref) {
		return null;
	}

	public HttpAccess getHttpAccess() {
		return null;
	}

	public String getLabel() {
		return "samigo";
	}

	public String merge(String siteId, Element root, String archivePath,
			String fromSiteId, Map attachmentNames, Map userIdTrans,
			Set userListAllowImport) {
		return null;
	}

	public boolean parseEntityReference(String reference, Reference ref) {
		return false;
	}

	public boolean willArchiveMerge() {
		return false;
	}

	 
        public void transferCopyEntities(String fromContext, String toContext, List ids, boolean cleanup)
        {
                transferCopyEntitiesRefMigrator(fromContext, toContext, ids, cleanup);
        }

        public Map<String, String> transferCopyEntitiesRefMigrator(String fromContext, String toContext, List ids, boolean cleanup)
	{	
		try
		{
			if(cleanup == true)
			{
				log.debug("deleting assessments from " + toContext);
				AssessmentService service = new AssessmentService();
				List assessmentList = service.getAllActiveAssessmentsbyAgent(toContext);
				log.debug("found " + assessmentList.size() + " assessments in site: " + toContext);
				Iterator iter =assessmentList.iterator();
				while (iter.hasNext()) {
					AssessmentData oneassessment = (AssessmentData) iter.next();
					log.debug("removing assessemnt id = " +oneassessment.getAssessmentId() );
					service.removeAssessment(oneassessment.getAssessmentId().toString());
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			log.debug("transferCopyEntities: End removing Assessment data");
		}
		transferCopyEntitiesRefMigrator(fromContext, toContext, ids);

		return null;
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
					assessmentDesc = replaceAllRefs(assessmentDesc, entrySet);
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
						sectionDesc = replaceAllRefs(sectionDesc, entrySet);
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
							itemIntr = replaceAllRefs(itemIntr, entrySet);
							if(!itemIntr.equals(item.getInstruction())){
								//need to save since a ref has been updated:
								needToUpdate = true;
								item.setInstruction(itemIntr);
							}
						}
						
						String itemDesc = item.getDescription();
						if(itemDesc != null){
							itemDesc = replaceAllRefs(itemDesc, entrySet);
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
									text = replaceAllRefs(text, entrySet);
									if(!text.equals(itemText.getText())){
										//need to save since a ref has been updated:
										needToUpdate = true;
										itemText.setText(text);
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
	
	private String replaceAllRefs(String msgBody, Set<Entry<String, String>> entrySet){
		if(msgBody != null){
			Iterator<Entry<String, String>> entryItr = entrySet.iterator();
			while(entryItr.hasNext()) {
				Entry<String, String> entry = (Entry<String, String>) entryItr.next();
				String fromContextRef = entry.getKey();
				if(msgBody.contains(fromContextRef)){					
					msgBody = msgBody.replace(fromContextRef, entry.getValue());
				}								
			}
		}	
		return msgBody;		
	}
	
}
