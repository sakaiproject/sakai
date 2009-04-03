/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/services/assessment/AssessmentService.java $
 * $Id: AssessmentService.java 9273 2006-05-10 22:34:28Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.services.assessment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentTemplateData;
import org.sakaiproject.tool.assessment.data.dao.assessment.AttachmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentBaseIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.facade.AssessmentTemplateFacade;
import org.sakaiproject.tool.assessment.facade.SectionFacade;
import org.sakaiproject.tool.assessment.facade.TypeFacade;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.cover.ToolManager;

/**
 * The AssessmentService calls the service locator to reach the manager on the
 * back end.
 * 
 * @author Rachel Gollub <rgollub@stanford.edu>
 */
public class AssessmentService {
	private static Log log = LogFactory.getLog(AssessmentService.class);

	/**
	 * Creates a new QuestionPoolService object.
	 */
	public AssessmentService() {
	}

	public AssessmentTemplateFacade getAssessmentTemplate(
			String assessmentTemplateId) {
		try {
			return PersistenceService.getInstance()
					.getAssessmentFacadeQueries().getAssessmentTemplate(
							new Long(assessmentTemplateId));
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}

	public AssessmentFacade getAssessment(String assessmentId) {
		try {
			return PersistenceService.getInstance()
					.getAssessmentFacadeQueries().getAssessment(
							Long.valueOf(assessmentId));
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}
	
	public AssessmentIfc getAssessment(Long assessmentId) {
		try {
			return PersistenceService.getInstance()
					.getAssessmentFacadeQueries().getAssessment(assessmentId);
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}

	public AssessmentFacade getBasicInfoOfAnAssessment(String assessmentId) {
		try {
			return PersistenceService.getInstance()
					.getAssessmentFacadeQueries().getBasicInfoOfAnAssessment(
							new Long(assessmentId));
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}

	public ArrayList getAllAssessmentTemplates() {
		try {
			return PersistenceService.getInstance()
					.getAssessmentFacadeQueries().getAllAssessmentTemplates();
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}

	public ArrayList getAllActiveAssessmentTemplates() {
		try {
			return PersistenceService.getInstance()
					.getAssessmentFacadeQueries()
					.getAllActiveAssessmentTemplates();
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}

	public ArrayList getTitleOfAllActiveAssessmentTemplates() {
		try {
			return PersistenceService.getInstance()
					.getAssessmentFacadeQueries()
					.getTitleOfAllActiveAssessmentTemplates();
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}

	public ArrayList getAllAssessments(String orderBy) {
		return PersistenceService.getInstance().getAssessmentFacadeQueries()
				.getAllAssessments(orderBy); // signalling all & no paging
	}

	public ArrayList getAllActiveAssessments(String orderBy) {
		return PersistenceService.getInstance().getAssessmentFacadeQueries()
				.getAllActiveAssessments(orderBy); // signalling all & no
													// paging
	}

	/**
	 * @param orderBy
	 * @return an ArrayList of AssessmentFacade. It is IMPORTANT to note that
	 *         the object is a partial object which contains no SectionFacade
	 */
	public ArrayList getSettingsOfAllActiveAssessments(String orderBy) {
		return PersistenceService.getInstance().getAssessmentFacadeQueries()
				.getSettingsOfAllActiveAssessments(orderBy); // signalling
																// all & no
																// paging
	}

	/**
	 * @param orderBy
	 * @return an ArrayList of AssessmentFacade. It is IMPORTANT to note that
	 *         the object is a partial object which contains only Assessment
	 *         basic info such as title, lastModifiedDate. This method is used
	 *         by Authoring Front Door
	 */
	public ArrayList getBasicInfoOfAllActiveAssessments(String orderBy,
			boolean ascending) {
		String siteAgentId = AgentFacade.getCurrentSiteId();
		return PersistenceService.getInstance().getAssessmentFacadeQueries()
				.getBasicInfoOfAllActiveAssessmentsByAgent(orderBy,
						siteAgentId, ascending); // signalling all & no
													// paging
	}

	public ArrayList getBasicInfoOfAllActiveAssessments(String orderBy) {
		String siteAgentId = AgentFacade.getCurrentSiteId();
		return PersistenceService
				.getInstance()
				.getAssessmentFacadeQueries()
				.getBasicInfoOfAllActiveAssessmentsByAgent(orderBy, siteAgentId); // signalling
																					// all
																					// & no
																					// paging
	}

	public ArrayList getAllAssessments(int pageSize, int pageNumber,
			String orderBy) {
		try {
			if (pageSize > 0 && pageNumber > 0) {
				return PersistenceService.getInstance()
						.getAssessmentFacadeQueries().getAllAssessments(
								pageSize, pageNumber, orderBy);
			} else {
				return PersistenceService.getInstance()
						.getAssessmentFacadeQueries()
						.getAllAssessments(orderBy);
			}
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}

	public AssessmentFacade createAssessment(String title, String description,
			String typeId, String templateId) throws Exception {
		AssessmentFacade assessment = null;
		try {
			AssessmentTemplateFacade assessmentTemplate = null;
			// #1 - check templateId and prepared it in Long
			Long templateIdLong = AssessmentTemplateFacade.DEFAULTTEMPLATE;
			if (templateId != null && !templateId.equals(""))
				templateIdLong = new Long(templateId);

			// #2 - check typeId and prepared it in Long
			Long typeIdLong = TypeFacade.HOMEWORK;
			if (typeId != null && !typeId.equals(""))
				typeIdLong = new Long(typeId);

			AssessmentFacadeQueriesAPI queries = PersistenceService
					.getInstance().getAssessmentFacadeQueries();
			log.debug("**** AssessmentFacadeQueries=" + queries);
			assessment = queries.createAssessment(title, description,
					typeIdLong, templateIdLong);
		} catch (Exception e) {
			log.error(e);
			throw new Exception(e);
		}
		return assessment;
	}

	public int getQuestionSize(String assessmentId) {
		return PersistenceService.getInstance().getAssessmentFacadeQueries()
				.getQuestionSize(new Long(assessmentId));
	}

	public void update(AssessmentFacade assessment) {
		PersistenceService.getInstance().getAssessmentFacadeQueries()
				.saveOrUpdate(assessment);
	}

	public void save(AssessmentTemplateData template) {
		PersistenceService.getInstance().getAssessmentFacadeQueries()
				.saveOrUpdate(template);
	}

	public void deleteAllSecuredIP(AssessmentIfc assessment) {
		PersistenceService.getInstance().getAssessmentFacadeQueries()
				.deleteAllSecuredIP(assessment);
	}

	public void saveAssessment(AssessmentFacade assessment) {
		PersistenceService.getInstance().getAssessmentFacadeQueries()
				.saveOrUpdate(assessment);
	}

	public void deleteAssessmentTemplate(Long assessmentId) {
		PersistenceService.getInstance().getAssessmentFacadeQueries()
				.deleteTemplate(assessmentId);
	}

	public void removeAssessment(String assessmentId) {
		PersistenceService.getInstance().getAssessmentFacadeQueries()
				.removeAssessment(new Long(assessmentId));
	}

	/**
	 * public int checkDelete(long assessmentId){ return
	 * assessmentService.checkDelete(assessmentId); }
	 * 
	 * public void deleteAssessment(Id assessmentId) throws
	 * osid.assessment.AssessmentException {
	 * assessmentService.deleteAssessment(assessmentId); }
	 * 
	 * public AssessmentIterator getAssessments() throws
	 * osid.assessment.AssessmentException { return
	 * assessmentService.getAssessments(); }
	 * 
	 */

	public SectionFacade addSection(String assessmentId) {
		SectionFacade section = null;
		try {
			Long assessmentIdLong = new Long(assessmentId);
			AssessmentFacadeQueriesAPI queries = PersistenceService
					.getInstance().getAssessmentFacadeQueries();
			section = queries.addSection(assessmentIdLong);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return section;
	}

	public void removeSection(String sectionId) {
		try {
			Long sectionIdLong = new Long(sectionId);
			AssessmentFacadeQueriesAPI queries = PersistenceService
					.getInstance().getAssessmentFacadeQueries();
			queries.removeSection(sectionIdLong);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public SectionFacade getSection(String sectionId) {
		try {
			return PersistenceService.getInstance()
					.getAssessmentFacadeQueries().getSection(
							new Long(sectionId));
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}

	public void saveOrUpdateSection(SectionFacade section) {
		try {
			PersistenceService.getInstance().getAssessmentFacadeQueries()
					.saveOrUpdateSection(section);
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}

	public void moveAllItems(String sourceSectionId, String destSectionId) {
		PersistenceService.getInstance().getAssessmentFacadeQueries()
				.moveAllItems(new Long(sourceSectionId),
						new Long(destSectionId)); // signalling all & no
													// paging
	}

	public void removeAllItems(String sourceSectionId) {
		PersistenceService.getInstance().getAssessmentFacadeQueries()
				.removeAllItems(new Long(sourceSectionId));
	}

	public ArrayList getBasicInfoOfAllActiveAssessmentTemplates(String orderBy) {
		return PersistenceService.getInstance().getAssessmentFacadeQueries()
				.getBasicInfoOfAllActiveAssessmentTemplates(orderBy); // signalling
																		// all &
																		// no
																		// paging
	}

	public AssessmentFacade createAssessmentWithoutDefaultSection(String title,
			String description, String typeId, String templateId)
			throws Exception {
		AssessmentFacade assessment = null;
		try {
			AssessmentTemplateFacade assessmentTemplate = null;
			// #1 - check templateId and prepared it in Long
			Long templateIdLong = AssessmentTemplateFacade.DEFAULTTEMPLATE;
			if (templateId != null && !templateId.equals(""))
				templateIdLong = new Long(templateId);

			// #2 - check typeId and prepared it in Long
			Long typeIdLong = TypeFacade.HOMEWORK;
			if (typeId != null && !typeId.equals(""))
				typeIdLong = new Long(typeId);

			AssessmentFacadeQueriesAPI queries = PersistenceService
					.getInstance().getAssessmentFacadeQueries();
			assessment = queries.createAssessmentWithoutDefaultSection(title,
					description, typeIdLong, templateIdLong);
		} catch (Exception e) {
			log.error(e);
			throw new Exception(e);
		}
		return assessment;
	}

	public boolean assessmentTitleIsUnique(String assessmentBaseId,
			String title, boolean isTemplate) {
		return PersistenceService.getInstance().getAssessmentFacadeQueries()
				.assessmentTitleIsUnique(new Long(assessmentBaseId), title,
						Boolean.valueOf(isTemplate));
	}

	public List getAssessmentByTemplate(String templateId) {
		return PersistenceService.getInstance().getAssessmentFacadeQueries()
				.getAssessmentByTemplate(new Long(templateId));
	}

	public List getDefaultMetaDataSet() {
		return PersistenceService.getInstance().getAssessmentFacadeQueries()
				.getDefaultMetaDataSet();
	}

	public void deleteAllMetaData(AssessmentBaseIfc assessment) {
		PersistenceService.getInstance().getAssessmentFacadeQueries()
				.deleteAllMetaData(assessment);
	}

	public ItemAttachmentIfc createItemAttachment(ItemDataIfc item,
			String resourceId, String filename, String protocol) {
		return createItemAttachment(item, resourceId,
					filename, protocol, true);
	}

	public ItemAttachmentIfc createItemAttachment(ItemDataIfc item,
			String resourceId, String filename, String protocol, boolean isEditPendingAssessmentFlow) {
		ItemAttachmentIfc attachment = null;
		try {
			AssessmentFacadeQueriesAPI queries = PersistenceService
					.getInstance().getAssessmentFacadeQueries();
			attachment = queries.createItemAttachment(item, resourceId,
					filename, protocol, isEditPendingAssessmentFlow);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return attachment;
	}
	
	public void removeItemAttachment(String attachmentId) {
		PersistenceService.getInstance().getAssessmentFacadeQueries()
				.removeItemAttachment(new Long(attachmentId));
	}

	public void updateAssessmentLastModifiedInfo(
			AssessmentFacade assessmentFacade) {
		try {
			PersistenceService.getInstance().getAssessmentFacadeQueries()
					.updateAssessmentLastModifiedInfo(assessmentFacade);
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}

	public SectionAttachmentIfc createSectionAttachment(SectionDataIfc section,
			String resourceId, String filename, String protocol) {
		SectionAttachmentIfc attachment = null;
		try {
			AssessmentFacadeQueriesAPI queries = PersistenceService
					.getInstance().getAssessmentFacadeQueries();
			attachment = queries.createSectionAttachment(section, resourceId,
					filename, protocol);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return attachment;
	}

	public void removeSectionAttachment(String attachmentId) {
		PersistenceService.getInstance().getAssessmentFacadeQueries()
				.removeSectionAttachment(new Long(attachmentId));
	}

	public AssessmentAttachmentIfc createAssessmentAttachment(
			AssessmentIfc assessment, String resourceId, String filename,
			String protocol) {
		AssessmentAttachmentIfc attachment = null;
		try {
			AssessmentFacadeQueriesAPI queries = PersistenceService
					.getInstance().getAssessmentFacadeQueries();
			attachment = queries.createAssessmentAttachment(assessment,
					resourceId, filename, protocol);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return attachment;
	}

	public void removeAssessmentAttachment(String attachmentId) {
		PersistenceService.getInstance().getAssessmentFacadeQueries()
				.removeAssessmentAttachment(new Long(attachmentId));
	}

	public AttachmentData createEmailAttachment(String resourceId,
			String filename, String protocol) {
		AttachmentData attachment = null;
		try {
			AssessmentFacadeQueriesAPI queries = PersistenceService
					.getInstance().getAssessmentFacadeQueries();
			attachment = queries.createEmailAttachment(resourceId, filename,
					protocol);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return attachment;
	}

	public List getAssessmentResourceIdList(AssessmentIfc pub) {
		List resourceIdList = new ArrayList();
		List list = pub.getAssessmentAttachmentList();
		if (list != null) {
			resourceIdList = getResourceIdList(list);
		}
		Set sectionSet = pub.getSectionSet();
		Iterator iter = sectionSet.iterator();
		while (iter.hasNext()) {
			SectionDataIfc section = (SectionDataIfc) iter.next();
			List sectionAttachments = getSectionResourceIdList(section);
			if (sectionAttachments != null) {
				resourceIdList.addAll(sectionAttachments);
			}
		}
		log.debug("*** resource size=" + resourceIdList.size());
		return resourceIdList;
	}

	public List getSectionResourceIdList(SectionDataIfc section) {
		List resourceIdList = new ArrayList();
		List list = section.getSectionAttachmentList();
		if (list != null) {
			resourceIdList = getResourceIdList(list);
		}
		Set itemSet = section.getItemSet();
		Iterator iter1 = itemSet.iterator();
		while (iter1.hasNext()) {
			ItemDataIfc item = (ItemDataIfc) iter1.next();
			List itemAttachments = getItemResourceIdList(item);
			if (itemAttachments != null) {
				resourceIdList.addAll(itemAttachments);
			}
		}
		return resourceIdList;
	}

	public List getItemResourceIdList(ItemDataIfc item) {
		List resourceIdList = new ArrayList();
		List list = item.getItemAttachmentList();
		if (list != null) {
			resourceIdList = getResourceIdList(list);
		}
		return resourceIdList;
	}

	private List getResourceIdList(List list) {
		List resourceIdList = new ArrayList();
		for (int i = 0; i < list.size(); i++) {
			AttachmentIfc attach = (AttachmentIfc) list.get(i);
			resourceIdList.add(attach.getResourceId());
		}
		return resourceIdList;
	}

	public void deleteResources(List resourceIdList) {
		if (resourceIdList == null)
			return;
		for (int i = 0; i < resourceIdList.size(); i++) {
			String resourceId = (String) resourceIdList.get(i);
			resourceId = resourceId.trim();
			if (resourceId.toLowerCase().startsWith("/attachment")) {
				try {
					log.debug("removing=" + resourceId);
					AssessmentService.getContentHostingService().removeResource(resourceId);
				} catch (PermissionException e) {
					log.warn("cannot remove resourceId=" + resourceId + ":"
							+ e.getMessage());
					log.warn("PermissionException from ContentHostingService:"
							+ e.getMessage());
				} catch (IdUnusedException e) {
					log.warn("cannot remove resourceId=" + resourceId + ":"
							+ e.getMessage());
					log.warn("IdUnusedException from ContentHostingService:"
							+ e.getMessage());
				} catch (TypeException e) {
					log.warn("cannot remove resourceId=" + resourceId + ":"
							+ e.getMessage());
					log.warn("TypeException from ContentHostingService:"
							+ e.getMessage());
				} catch (InUseException e) {
					log.warn("cannot remove resourceId=" + resourceId + ":"
							+ e.getMessage());
					log.warn("InUseException from ContentHostingService:"
							+ e.getMessage());
				}
			}
		}
	}

	public void saveOrUpdateAttachments(List list) {
		PersistenceService.getInstance().getAssessmentFacadeQueries()
				.saveOrUpdateAttachments(list);
	}

	public ContentResource createCopyOfContentResource(String resourceId,
			String filename) {
		// trouble using Validator, so use string replacement instead
		// java.lang.NoClassDefFoundError: org/sakaiproject/util/Validator
		filename = filename.replaceAll("http://","http:__");
		ContentResource cr_copy = null;
		try {
			// create a copy of the resource
			ContentResource cr = AssessmentService.getContentHostingService().getResource(resourceId);
			String escapedName = escapeResourceName(filename);
			cr_copy = AssessmentService.getContentHostingService().addAttachmentResource(escapedName, 
					ToolManager.getCurrentPlacement().getContext(), 
					ToolManager.getTool("sakai.samigo").getTitle(), cr
					.getContentType(), cr.getContent(), cr.getProperties());
		} catch (IdInvalidException e) {
			log.warn(e.getMessage());
		} catch (PermissionException e) {
			log.warn(e.getMessage());
		} catch (IdUnusedException e) {
			log.warn(e.getMessage());
		} catch (TypeException e) {
			log.warn(e.getMessage());
		} catch (InconsistentException e) {
			log.warn(e.getMessage());
		} catch (IdUsedException e) {
			log.warn(e.getMessage());
		} catch (OverQuotaException e) {
			log.warn(e.getMessage());
		} catch (ServerOverloadException e) {
			log.warn(e.getMessage());
		}
		return cr_copy;
	}

	/** These characters are not allowed in a resource id */
	public static final String INVALID_CHARS_IN_RESOURCE_ID = "^/\\{}[]()%*?#&=\n\r\t\b\f";

	protected static final String MAP_TO_A = "‰ŠˆŒ€?‡‡";

	protected static final String MAP_TO_B = "§§";

	protected static final String MAP_TO_C = "‚?¢¢";

	protected static final String MAP_TO_E = "Ž?‘?ƒ¾®®";

	protected static final String MAP_TO_I = "•”“’’";

	protected static final String MAP_TO_L = "££";

	protected static final String MAP_TO_N = "–„„";

	protected static final String MAP_TO_O = "™š˜…——";

	protected static final String MAP_TO_U = "Ÿž?†œœ";

	protected static final String MAP_TO_Y = "Ø´??";

	protected static final String MAP_TO_X = "???¤©»¨±?«µ¦À?";

	/**
	 * These characters are allowed; but if escapeResourceName() is called, they are escaped (actually, removed) Certain characters cause problems with filenames in certain OSes - so get rid of these characters in filenames
	 */
	protected static final String ESCAPE_CHARS_IN_RESOURCE_ID = ";'\"";
	
	/**
	 * Return a string based on id that is valid according to Resource name validity rules.
	 * 
	 * @param id
	 *        The string to escape.
	 * @return id fully escaped using Resource name validity rules.
	 */
	public static String escapeResourceName(String id)
	{
		if (id == null) return "";
		id = id.trim();
		try
		{
			StringBuilder buf = new StringBuilder();
			for (int i = 0; i < id.length(); i++)
			{
				char c = id.charAt(i);
				if (MAP_TO_A.indexOf(c) >= 0)
				{
					buf.append('a');
				}
				else if (MAP_TO_E.indexOf(c) >= 0)
				{
					buf.append('e');
				}
				else if (MAP_TO_I.indexOf(c) >= 0)
				{
					buf.append('i');
				}
				else if (MAP_TO_O.indexOf(c) >= 0)
				{
					buf.append('o');
				}
				else if (MAP_TO_U.indexOf(c) >= 0)
				{
					buf.append('u');
				}
				else if (MAP_TO_Y.indexOf(c) >= 0)
				{
					buf.append('y');
				}
				else if (MAP_TO_N.indexOf(c) >= 0)
				{
					buf.append('n');
				}
				else if (MAP_TO_B.indexOf(c) >= 0)
				{
					buf.append('b');
				}
				else if (MAP_TO_C.indexOf(c) >= 0)
				{
					buf.append('c');
				}
				else if (MAP_TO_L.indexOf(c) >= 0)
				{
					buf.append('l');
				}
				else if (MAP_TO_X.indexOf(c) >= 0)
				{
					buf.append('x');
				}
				else if (c < '\040')	// Remove any ascii control characters
				{
					buf.append('_');
				}
				else if (INVALID_CHARS_IN_RESOURCE_ID.indexOf(c) >= 0 || ESCAPE_CHARS_IN_RESOURCE_ID.indexOf(c) >= 0)
				{
					buf.append('_');
				}
				else
				{
					buf.append(c);
				}
			}

			String rv = buf.toString();
			return rv;
		}
		catch (Exception e)
		{
			log.warn("escapeResourceName: ", e);
			return id;
		}

	} // escapeResourceName
	
	public void copyAllAssessments(String fromContext, String toContext) {
		try {
			PersistenceService.getInstance().getAssessmentFacadeQueries()
					.copyAllAssessments(fromContext, toContext);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new RuntimeException(e);
		}
	}
	
	public void copyAssessment(String assessmentId, String apepndCopyTitle) {
		try {
			PersistenceService.getInstance().getAssessmentFacadeQueries()
					.copyAssessment(assessmentId, apepndCopyTitle);
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}

	public List getAllActiveAssessmentsbyAgent(String fromContext) {
		try {
			return PersistenceService.getInstance().getAssessmentFacadeQueries()
					.getAllActiveAssessmentsByAgent(fromContext);
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}

	}
	
	  public String getAssessmentSiteId(String assessmentId){
		    return PersistenceService.getInstance().getAssessmentFacadeQueries().getAssessmentSiteId(assessmentId);
	  }

	  public String getAssessmentCreatedBy(String assessmentId){
		    return PersistenceService.getInstance().getAssessmentFacadeQueries().getAssessmentCreatedBy(assessmentId);
	  }
	  
	  public Set copyItemAttachmentSet(ItemData newItem, Set itemAttachmentSet){
		    return PersistenceService.getInstance().getAssessmentFacadeQueries().copyItemAttachmentSet(newItem, itemAttachmentSet);
	  }

	  public static ContentHostingService getContentHostingService(){
		  return (ContentHostingService) ComponentManager.get(ContentHostingService.class.getName());
	  }
}
