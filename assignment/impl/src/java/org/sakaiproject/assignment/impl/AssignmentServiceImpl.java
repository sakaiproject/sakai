/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.assignment.impl;

import static org.sakaiproject.assignment.api.AssignmentConstants.*;
import static org.sakaiproject.assignment.api.AssignmentServiceConstants.*;
import static org.sakaiproject.assignment.api.model.Assignment.Access.*;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.sakaiproject.announcement.api.AnnouncementChannel;
import org.sakaiproject.announcement.api.AnnouncementMessageEdit;
import org.sakaiproject.announcement.api.AnnouncementMessageHeaderEdit;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentConstants.SubmissionStatus;
import org.sakaiproject.assignment.api.AssignmentEntity;
import org.sakaiproject.assignment.api.AssignmentReferenceReckoner;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.AssignmentServiceConstants;
import org.sakaiproject.assignment.api.ContentReviewResult;
import org.sakaiproject.assignment.api.MultiGroupRecord;
import org.sakaiproject.assignment.api.MultiGroupRecord.AsnGroup;
import org.sakaiproject.assignment.api.MultiGroupRecord.AsnUser;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentAllPurposeItem;
import org.sakaiproject.assignment.api.model.AssignmentAllPurposeItemAccess;
import org.sakaiproject.assignment.api.model.AssignmentModelAnswerItem;
import org.sakaiproject.assignment.api.model.AssignmentNoteItem;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.assignment.api.model.AssignmentSubmissionSubmitter;
import org.sakaiproject.assignment.api.model.AssignmentSupplementItemAttachment;
import org.sakaiproject.assignment.api.model.AssignmentSupplementItemService;
import org.sakaiproject.assignment.api.model.PeerAssessmentItem;
import org.sakaiproject.assignment.api.persistence.AssignmentRepository;
import org.sakaiproject.assignment.api.reminder.AssignmentDueReminderService;
import org.sakaiproject.assignment.api.taggable.AssignmentActivityProducer;
import org.sakaiproject.assignment.api.sort.AnonymousSubmissionComparator;
import org.sakaiproject.assignment.api.sort.AssignmentSubmissionComparator;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.AuthzRealmLockException;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.contentreview.dao.ContentReviewConstants;
import org.sakaiproject.contentreview.dao.ContentReviewItem;
import org.sakaiproject.contentreview.exception.QueueException;
import org.sakaiproject.contentreview.service.ContentReviewService;
import org.sakaiproject.grading.api.GradingConstants;
import org.sakaiproject.entity.api.ContentExistsAware;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.LearningResourceStoreService;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Actor;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Object;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Result;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Statement;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Verb;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Verb.SAKAI_VERB;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.grading.api.AssessmentNotFoundException;
import org.sakaiproject.grading.api.CategoryDefinition;
import org.sakaiproject.grading.api.GradebookInformation;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.messaging.api.Message;
import org.sakaiproject.messaging.api.MessageMedium;
import org.sakaiproject.messaging.api.UserMessagingService;
import org.sakaiproject.rubrics.api.RubricsService;
import org.sakaiproject.rubrics.api.beans.AssociationTransferBean;
import org.sakaiproject.rubrics.api.model.ToolItemRubricAssociation;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.taggable.api.TaggingManager;
import org.sakaiproject.taggable.api.TaggingProvider;
import org.sakaiproject.tags.api.TagService;
import org.sakaiproject.tasks.api.Priorities;
import org.sakaiproject.tasks.api.Task;
import org.sakaiproject.tasks.api.TaskService;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.timesheet.api.TimeSheetEntry;
import org.sakaiproject.timesheet.api.TimeSheetService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.CandidateDetailProvider;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.BaseResourceProperties;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.SortedIterator;
import org.sakaiproject.util.Validator;
import org.sakaiproject.util.api.FormattedText;
import org.sakaiproject.util.api.LinkMigrationHelper;
import org.sakaiproject.util.comparator.UserSortNameComparator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.sakaiproject.util.MergeConfig;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by enietzel on 3/3/17.
 */
@Slf4j
@Transactional(readOnly = true)
public class AssignmentServiceImpl implements AssignmentService, EntityTransferrer, ContentExistsAware, ApplicationContextAware {

	@Setter private AnnouncementService announcementService;
    @Setter private ApplicationContext applicationContext;
    @Setter private AssignmentActivityProducer assignmentActivityProducer;
    @Setter private AssignmentDueReminderService assignmentDueReminderService;
    @Setter private ObjectFactory<AssignmentEntity> assignmentEntityFactory;
    @Setter private AssignmentRepository assignmentRepository;
    @Setter private AssignmentSupplementItemService assignmentSupplementItemService;
    @Setter private AuthzGroupService authzGroupService;
    @Setter private CalendarService calendarService;
    @Setter private CandidateDetailProvider candidateDetailProvider;
    @Setter private ContentHostingService contentHostingService;
    @Setter private ContentReviewService contentReviewService;
    @Setter private EmailUtil emailUtil;
    @Setter private EntityManager entityManager;
    @Setter private EventTrackingService eventTrackingService;
    @Setter private FormattedText formattedText;
    @Setter private FunctionManager functionManager;
    @Setter private GradeSheetExporter gradeSheetExporter;
    @Setter private GradingService gradingService;
    @Setter private LearningResourceStoreService learningResourceStoreService;
    @Setter private LinkMigrationHelper linkMigrationHelper;
    @Setter private TransactionTemplate transactionTemplate;
    @Setter private ResourceLoader resourceLoader;
    @Setter private RubricsService rubricsService;
    @Setter private SecurityService securityService;
    @Setter private SessionManager sessionManager;
    @Setter private SearchService searchService;
    @Setter private ServerConfigurationService serverConfigurationService;
    @Setter private SiteService siteService;
    @Setter private TaggingManager taggingManager;
    @Setter private TaskService taskService;
    @Setter private TimeService timeService;
    @Setter private ToolManager toolManager;
    @Setter private UserDirectoryService userDirectoryService;
    @Resource private UserMessagingService userMessagingService;
    @Setter private UserTimeService userTimeService;
    @Setter private TimeSheetService timeSheetService;
    @Setter private TagService tagService;
    @Setter private LTIService ltiService;

    private boolean allowSubmitByInstructor;
    private boolean exposeContentReviewErrorsToUI;
    private boolean createGroupsOnImport;

    private static final String ASSIGNMENT_REFERENCE_PROPERTY = "assignmentReference";
    private static final String ANNOUNCEMENT_NOTIFICATION_INVOKEE = "org.sakaiproject.announcement.impl.SiteEmailNotificationAnnc";

    private static ResourceLoader rb = new ResourceLoader("assignment");

    public void init() {
        allowSubmitByInstructor = serverConfigurationService.getBoolean("assignments.instructor.submit.for.student", true);
        if (!allowSubmitByInstructor) {
            log.info("Instructor submission of assignments is disabled - add assignments.instructor.submit.for.student=true to sakai config to enable");
        } else {
            log.info("Instructor submission of assignments is enabled");
        }

        exposeContentReviewErrorsToUI = serverConfigurationService.getBoolean("contentreview.expose.errors.to.ui", true);
        createGroupsOnImport = serverConfigurationService.getBoolean("assignment.create.groups.on.import", true);

        // register as an entity producer
        entityManager.registerEntityProducer(this, REFERENCE_ROOT);

        // register functions
        functionManager.registerFunction(SECURE_ALL_GROUPS, true);
        functionManager.registerFunction(SECURE_ADD_ASSIGNMENT, true);
        functionManager.registerFunction(SECURE_ADD_ASSIGNMENT_SUBMISSION, true);
        functionManager.registerFunction(SECURE_REMOVE_ASSIGNMENT, true);
        functionManager.registerFunction(SECURE_ACCESS_ASSIGNMENT, true);
        functionManager.registerFunction(SECURE_UPDATE_ASSIGNMENT, true);
        functionManager.registerFunction(SECURE_GRADE_ASSIGNMENT_SUBMISSION, true);
        functionManager.registerFunction(SECURE_ASSIGNMENT_RECEIVE_NOTIFICATIONS, true);
        functionManager.registerFunction(SECURE_SHARE_DRAFTS, true);

        // this is needed to avoid a circular dependency, notice we set the AssignmentService proxy and not this
        assignmentSupplementItemService.setAssignmentService(applicationContext.getBean(AssignmentService.class));

        userMessagingService.importTemplateFromResourceXmlFile("templates/releaseGrade.xml", AssignmentConstants.TOOL_ID + ".releasegrade");
        userMessagingService.importTemplateFromResourceXmlFile("templates/releaseResubmission.xml", AssignmentConstants.TOOL_ID + ".releaseresubmission");
        userMessagingService.importTemplateFromResourceXmlFile("templates/submission.xml", AssignmentConstants.TOOL_ID + ".submission");
        userMessagingService.importTemplateFromResourceXmlFile("templates/dueReminder.xml", AssignmentConstants.TOOL_ID + ".duereminder");
    }

    @Override
    public boolean isTimeSheetEnabled(String siteId) {
       return timeSheetService.isTimeSheetEnabled(siteId);
    }

    @Override
    public String getLabel() {
        return "assignment";
    }

    @Override
    public String getToolId() {
        return AssignmentServiceConstants.ASSIGNMENT_TOOL_ID;
    }

    @Override
    public boolean willArchiveMerge() {
        return true;
    }

    @Override
    public String archive(String siteId, Document doc, Stack<Element> stack, String archivePath, List<Reference> attachments) {
        final StringBuilder results = new StringBuilder();
        results.append("begin archiving ").append(getLabel()).append(" context ").append(siteId).append(LINE_SEPARATOR);

        // start with an element with our very own (service) name
        Element element = doc.createElement(AssignmentService.class.getName());
        stack.peek().appendChild(element);
        stack.push(element);

        int assignmentsArchived = 0;
        for (Assignment assignment : getAssignmentsForContext(siteId)) {

            String xml = assignmentRepository.toXML(assignment);
            log.debug(xml);

            try {
                InputSource in = new InputSource(new StringReader(xml));
                Document assignmentDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
                Element assignmentElement = assignmentDocument.getDocumentElement();

                if ( assignment.getContentId() != null ) {
                    Long contentKey = assignment.getContentId().longValue();
                    Element contentElement = ltiService.archiveContentByKey(assignmentDocument, contentKey, siteId);
                    if ( contentElement != null ) assignmentElement.appendChild(contentElement);
                }

                Node assignmentNode = doc.importNode(assignmentElement, true);
                element.appendChild(assignmentNode);


                // Model answer with optional attachments
                AssignmentModelAnswerItem modelAnswer = assignmentSupplementItemService.getModelAnswer(assignment.getId());
                if (modelAnswer != null) {
                    Element modelAnswerElement = doc.createElement("ModelAnswer");

                    if (modelAnswer.getShowTo() != null) {
                        modelAnswerElement.setAttribute("showTo", modelAnswer.getShowTo().toString());
                    }

                    // Write text as CDATA
                    CDATASection cdata = doc.createCDATASection(modelAnswer.getText());
                    modelAnswerElement.appendChild(cdata);

                    // Add attachments from the supplementary item
                    addSupplementaryItemAttachments(doc, modelAnswerElement, assignmentSupplementItemService.getAttachmentListForSupplementItem(modelAnswer), attachments);

                    assignmentNode.appendChild(modelAnswerElement);
                }

                // Note (text only)
                AssignmentNoteItem noteItem = assignmentSupplementItemService.getNoteItem(assignment.getId());
                if (noteItem != null) {
                    Element noteElement = doc.createElement("PrivateNote");

                    if (noteItem.getShareWith() != null) {
                        noteElement.setAttribute("shareWith", noteItem.getShareWith().toString());
                    }

                    // Write text as CDATA
                    CDATASection cdata = doc.createCDATASection(noteItem.getNote());
                    noteElement.appendChild(cdata);

                    assignmentNode.appendChild(noteElement);
                }

                // All Purpose Item (with optional attachments)
                // Not archived: getHide(), getReleaseDate(), getRetractDate(), getAccessSet()
                AssignmentAllPurposeItem allPurposeItem = assignmentSupplementItemService.getAllPurposeItem(assignment.getId());
                if (allPurposeItem != null) {
                    Element itemElement = doc.createElement("AllPurposeItem");

                    if (allPurposeItem.getTitle() != null) {
                        itemElement.setAttribute("title", allPurposeItem.getTitle());
                    }

                    // Write text as CDATA
                    CDATASection cdata = doc.createCDATASection(allPurposeItem.getText());
                    itemElement.appendChild(cdata);

                    assignmentNode.appendChild(itemElement);

                    // Add attachments from the supplementary item
                    addSupplementaryItemAttachments(doc, itemElement, assignmentSupplementItemService.getAttachmentListForSupplementItem(allPurposeItem), attachments);
                }

                // Add attachmments from the assignment
                for (String resourceId : assignment.getAttachments()) {
                    attachments.add(entityManager.newReference(resourceId));
                }

                // Add the assignment
                element.appendChild(assignmentNode);
                assignmentsArchived++;

            } catch (Exception e) {
                String error = String.format("could not append assignment %s to archive: %s", assignment.getId(), e.getMessage());
                log.error(error, e);
                throw new RuntimeException(error);
            }
        }

        stack.pop();

        results.append("completed archiving ").append(getLabel()).append(" context ").append(siteId).append(" count (").append(assignmentsArchived).append(")").append(LINE_SEPARATOR);
        return results.toString();
    }

    private void addSupplementaryItemAttachments(Document doc, Element item, List<String> itemAttachments, List archiveAttachments) {

        if (itemAttachments.isEmpty()) {
            return;
        }

        Element attachmentsElement = doc.createElement("attachments");

        // Add attachments from the supplementary item
        for (String resourceId : itemAttachments) {
            archiveAttachments.add(entityManager.newReference(resourceId));
            Element attachmentElement = doc.createElement("attachment");
            attachmentElement.appendChild(doc.createTextNode(resourceId));
            attachmentsElement.appendChild(attachmentElement);
        }

        item.appendChild(attachmentsElement);

        return;
    }

    @Override
    @Transactional
    public String merge(String siteId, Element root, String archivePath, String fromSiteId, MergeConfig mcx) {


        final StringBuilder results = new StringBuilder();
        results.append("begin merging ").append(getLabel()).append(" context ").append(siteId).append(LINE_SEPARATOR);
        final NodeList allChildrenNodeList = root.getChildNodes();
        final Stream<Node> allChildrenNodes = IntStream.range(0, allChildrenNodeList.getLength()).mapToObj(allChildrenNodeList::item);
        final List<Element> assignmentElements = allChildrenNodes.filter(node -> node.getNodeType() == Node.ELEMENT_NODE).map(element -> (Element) element).collect(Collectors.toList());

        Set<String> assignmentTitles = getAssignmentsForContext(siteId).stream()
            .map(Assignment::getTitle) // Extract the title from each Assignment
            .collect(Collectors.toCollection(LinkedHashSet::new)); // Collect into a LinkedHashSet

        int assignmentsMerged = 0;

        for (Element assignmentElement : assignmentElements) {

            try {
                mergeAssignment(siteId, assignmentElement, results, assignmentTitles, mcx);
                assignmentsMerged++;
            } catch (Exception e) {
                final String error = "could not merge assignment with id: " + assignmentElement.getFirstChild().getFirstChild().getNodeValue();
                log.warn(error, e);
                results.append(error).append(LINE_SEPARATOR);
            }
        }
        results.append("completed merging ").append(getLabel()).append(" context ").append(siteId).append(" count (").append(assignmentsMerged).append(")").append(LINE_SEPARATOR);
        return results.toString();
    }

    @Override
    public boolean parseEntityReference(String stringReference, Reference reference) {
        if (StringUtils.startsWith(stringReference, REFERENCE_ROOT)) {
            AssignmentReferenceReckoner.AssignmentReference reckoner = AssignmentReferenceReckoner.reckoner().reference(stringReference).reckon();
            reference.set(SAKAI_ASSIGNMENT, reckoner.getSubtype(), reckoner.getId(), reckoner.getContainer(), reckoner.getContext());
            return true;
        }
        return false;
    }

    @Override
    public String getEntityDescription(Reference reference) {
        String description = "Assignment: " + reference.getReference();

        try {
            switch (reference.getSubType()) {
                case REF_TYPE_CONTENT:
                case REF_TYPE_ASSIGNMENT:
                    Assignment a = getAssignment(reference.getReference());
                    description = "Assignment: " + a.getId() + " (" + a.getContext() + ")";
                    break;
                case REF_TYPE_SUBMISSION:
                    AssignmentSubmission s = getSubmission(reference.getReference());
                    description = "AssignmentSubmission: " + s.getId() + " (" + s.getAssignment().getContext() + ")";
                    break;
                default:
                    log.warn("Unknown Entity subtype: {} in ref: {}", reference.getSubType(), reference.getReference());
            }
        } catch (Exception e) {
            log.warn("Could not get the entity description for ref = {}", reference.getReference(), e);
        }

        return description;
    }

    @Override
    public ResourceProperties getEntityResourceProperties(Reference reference) {
        ResourceProperties properties = null;

        try {
            switch (reference.getSubType()) {
                case REF_TYPE_CONTENT:
                case REF_TYPE_ASSIGNMENT:
                    Assignment a = getAssignment(reference.getReference());
                    properties = new BaseResourceProperties(a.getProperties());
                    break;
                case REF_TYPE_SUBMISSION:
                    AssignmentSubmission s = getSubmission(reference.getReference());
                    properties = new BaseResourceProperties(s.getProperties());
                    break;
                default:
                    log.warn("Unknown Entity subtype: {} in ref: {}", reference.getSubType(), reference.getReference());
            }
        } catch (Exception e) {
            log.warn("Could not get the entity properties for ref = {}", reference.getReference(), e);
        }

        return properties;
    }

    @Override
    public Entity getEntity(Reference reference) {
        Objects.requireNonNull(reference);
        Entity entity = null;
        switch (reference.getSubType()) {
            case REF_TYPE_CONTENT:
            case REF_TYPE_ASSIGNMENT:
                entity = createAssignmentEntity(reference.getId());
                break;
            case REF_TYPE_SUBMISSION:
                // TODO assignment submission entities
                log.warn("Submission Entity not implemented open a JIRA, reference: {}", reference.getReference());
                break;
            default:
                log.warn("Unknown Entity subtype: {} in ref: {}", reference.getSubType(), reference.getReference());
        }

        return entity;
    }

    @Override
    public Entity createAssignmentEntity(String assignmentId) {
        AssignmentEntity entity = assignmentEntityFactory.getObject();
        entity.initEntity(assignmentId);
        return entity;
    }

    @Override
    public String getEntityUrl(Reference reference) {
        return getEntity(reference).getUrl();
    }

    @Override
    public Optional<String> getEntityUrl(Reference ref, Entity.UrlType urlType) {

        try {
            Assignment a = getAssignment(ref);
            return Optional.of(getDeepLink(a.getContext(), a.getId(), userDirectoryService.getCurrentUser().getId()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Collection<String> getEntityAuthzGroups(Reference reference, String userId) {
        Collection<String> references = new ArrayList<>();

        // for AssignmentService assignments:
        // if access set to SITE, use the assignment and site authzGroups.
        // if access set to GROUP, use the assignment, and the groups, but not the site authzGroups.
        // if the user has SECURE_ALL_GROUPS in the context, ignore GROUP access and treat as if SITE

        try {
            switch (reference.getSubType()) {
                case REF_TYPE_CONTENT:
                case REF_TYPE_ASSIGNMENT:
                    references.add(reference.getReference());

                    boolean grouped = false;
                    Collection groups = null;

                    // check SECURE_ALL_GROUPS - if not, check if the assignment has groups or not
                    // TODO: the last param needs to be a ContextService.getRef(ref.getContext())... or a ref.getContextAuthzGroup() -ggolden
                    if ((userId == null) || ((!securityService.isSuperUser(userId)) && (!securityService.unlock(userId, SECURE_ALL_GROUPS, siteService.siteReference(reference.getContext()))))) {
                        // get the channel to get the message to get group information
                        // TODO: check for efficiency, cache and thread local caching usage -ggolden
                        if (reference.getId() != null) {
                            Assignment a = getAssignment(reference);
                            if (a != null) {
                                grouped = GROUP == a.getTypeOfAccess();
                                groups = a.getGroups();
                            }
                        }
                    }

                    if (grouped) {
                        // groups
                        references.addAll(groups);
                    } else {
                        // not grouped
                        reference.addSiteContextAuthzGroup(references);
                    }
                    break;
                case REF_TYPE_SUBMISSION:
                    // for submission, use site security setting
                    references.add(reference.getReference());
                    reference.addSiteContextAuthzGroup(references);
                    break;
                default:
                    log.warn("Unknown Entity subtype: {} in ref: {}", reference.getSubType(), reference.getReference());
            }
        } catch (Exception e) {
            log.warn("Could not get the Entity's authz groups with ref = {}", reference.getReference(), e);
        }

        return references;
    }

    @Override
    public HttpAccess getHttpAccess() {
        return (req, res, ref, copyrightAcceptedRefs) -> {
            if (sessionManager.getCurrentSessionUserId() == null) {
                log.warn("Only logged in users can access assignment downloads");
            } else {
                // determine the type of download to create using the reference that was requested
                AssignmentReferenceReckoner.AssignmentReference refReckoner = AssignmentReferenceReckoner.reckoner().reference(ref.getReference()).reckon();

                if (REFERENCE_ROOT.equals("/" + refReckoner.getType())) {
                    // don't process any references that are not of type assignment
                    switch (refReckoner.getSubtype()) {
                        case REF_TYPE_CONTENT:
                        case REF_TYPE_ASSIGNMENT:
                            String date = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(userTimeService.getLocalTimeZone().toZoneId()).format(ZonedDateTime.now());
                            String queryString = req.getQueryString();
                            if (StringUtils.isNotBlank(refReckoner.getId())) {
                                // if subtype is assignment then were downloading all submissions for an assignment
                                try {
                                    Assignment a = getAssignment(refReckoner.getId());
                                    String filename = escapeInvalidCharsEntry(a.getTitle()) + "_" + date;
                                    res.setContentType("application/zip");
                                    res.setHeader("Content-Disposition", "attachment; filename = \"" + filename + ".zip\"");

                                    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                                        @Override
                                        protected void doInTransactionWithoutResult(TransactionStatus status) {
                                            try (OutputStream out = res.getOutputStream()) {
                                                getSubmissionsZip(out, ref.getReference(), queryString);
                                            } catch (Exception e) {
                                                log.warn("Could not stream the submissions for reference: {}", ref.getReference(), e);
                                            }
                                        }
                                    });
                                } catch (Exception e) {
                                    log.warn("Could not find assignment for ref = {}", ref.getReference(), e);
                                }
                            } else {
                                String filename = "bulk_download_" + date;
                                // if subtype is assignment and there is no assignmentId then were downloading grades
                                res.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

                                if (queryString.contains("estimate")) {
                                    res.setHeader("Content-Disposition", "attachment; filename = \"export_worklog_" + filename + ".xlsx\"");
                                } else {
                                    res.setHeader("Content-Disposition", "attachment; filename = \"export_grades_" + filename + ".xlsx\"");
                                }
                                try (OutputStream out = res.getOutputStream()) {
                                    gradeSheetExporter.writeGradesSpreadsheet(out, queryString);
                                } catch (Exception e) {
                                    log.warn("Could not stream the grades for reference: {}", ref.getReference(), e);
                                }
                            }
                            break;
                        case REF_TYPE_SUBMISSION:
                        default:
                            log.warn("Assignments download unhandled download type for reference: {}", ref.getReference());
                    }
                }
            }
        };
    }

    @Override
    public boolean allowReceiveSubmissionNotification(String context) {
        String resourceString = AssignmentReferenceReckoner.reckoner().context(context).reckon().getReference();
        if (permissionCheck(SECURE_ASSIGNMENT_RECEIVE_NOTIFICATIONS, resourceString, null)) return true;
        return false;
    }

    @Override
    public List<User> allowReceiveSubmissionNotificationUsers(String context) {
        String resourceString = AssignmentReferenceReckoner.reckoner().context(context).reckon().getReference();
        return securityService.unlockUsers(SECURE_ASSIGNMENT_RECEIVE_NOTIFICATIONS, resourceString);
    }

    @Override
    public boolean allowAddSiteAssignment(String context) {
        String resourceString = AssignmentReferenceReckoner.reckoner().context(context).reckon().getReference();
        return permissionCheck(SECURE_ADD_ASSIGNMENT, resourceString, null);
    }

    @Override
    public boolean allowAllGroups(String context) {
        String resourceString = siteService.siteReference(context);
        return permissionCheck(SECURE_ALL_GROUPS, resourceString, null);
    }

    @Override
    public boolean allowGetAssignment(String context) {
        String resourceString = AssignmentReferenceReckoner.reckoner().context(context).reckon().getReference();
        return permissionCheck(SECURE_ACCESS_ASSIGNMENT, resourceString, null);
    }

    @Override
    public Collection<Group> getGroupsAllowAddAssignment(String context) {
        return getGroupsAllowAddAssignment(context, null);
    }

    @Override
    public Collection<Group> getGroupsAllowAddAssignment(String context, String userId) {
        return getGroupsAllowFunction(SECURE_ADD_ASSIGNMENT, context, userId);
    }

    @Override
    public Collection<Group> getGroupsAllowUpdateAssignment(String context) {
        return getGroupsAllowFunction(SECURE_UPDATE_ASSIGNMENT, context, null);
    }

    @Override
    public Collection<Group> getGroupsAllowGradeAssignment(String assignmentReference) {
        AssignmentReferenceReckoner.AssignmentReference referenceReckoner = AssignmentReferenceReckoner.reckoner().reference(assignmentReference).reckon();
        if (allowGradeSubmission(assignmentReference)) {
            Collection<Group> groupsAllowed = getGroupsAllowFunction(SECURE_GRADE_ASSIGNMENT_SUBMISSION, referenceReckoner.getContext(), null);
            Assignment assignment = assignmentRepository.findAssignment(referenceReckoner.getId());
            if (assignment != null) {
                switch (assignment.getTypeOfAccess()) {
                    case SITE: // return all groups for site access
                        return groupsAllowed;
                    case GROUP: // return only matching groups for group access
                        Set<String> assignmentGroups = assignment.getGroups();
                        return groupsAllowed.stream().filter(g -> assignmentGroups.contains(g.getReference())).collect(Collectors.toSet());
                }
            }
        }
        return Collections.emptySet();
    }

    @Override
    public boolean allowUpdateAssignmentInContext(String context) {
        String resourceString = AssignmentReferenceReckoner.reckoner().context(context).reckon().getReference();
        if (permissionCheck(SECURE_UPDATE_ASSIGNMENT, resourceString, null)) return true;
        // if not, see if the user has any groups to which updates are allowed
        return (!getGroupsAllowUpdateAssignment(context).isEmpty());
    }

    @Override
    public boolean allowUpdateAssignment(String assignmentReference) {
        AssignmentReferenceReckoner.AssignmentReference referenceReckoner = AssignmentReferenceReckoner.reckoner().reference(assignmentReference).reckon();
        return allowUpdateAssignmentInContext(referenceReckoner.getContext());
    }

    @Override
    public boolean allowRemoveAssignment(String assignmentReference) {
        return permissionCheck(SECURE_REMOVE_ASSIGNMENT, assignmentReference, null);
    }

    @Override
    public Collection<Group> getGroupsAllowRemoveAssignment(String context) {
        return getGroupsAllowFunction(SECURE_REMOVE_ASSIGNMENT, context, null);
    }

    @Override
    public boolean allowAddAssignment(String context) {
        return allowAddAssignment(context, null);
    }

    @Override
    public boolean allowAddAssignment(String context, String userId) {
        String resourceString = AssignmentReferenceReckoner.reckoner().context(context).reckon().getReference();
        if (permissionCheck(SECURE_ADD_ASSIGNMENT, resourceString, userId)) return true;
        // if not, see if the user has any groups to which adds are allowed
        return (!getGroupsAllowAddAssignment(context, userId).isEmpty());
    }

    @Override
    public boolean allowRemoveAssignmentInContext(String context) {
        String resourceString = AssignmentReferenceReckoner.reckoner().context(context).reckon().getReference();
        if (permissionCheck(SECURE_REMOVE_ASSIGNMENT, resourceString, null)) return true;
        // if not, see if the user has any groups to which remove is allowed
        return (!getGroupsAllowRemoveAssignment(context).isEmpty());
    }

    @Override
    public boolean allowAddSubmission(String context) {
        String resourceString = AssignmentReferenceReckoner.reckoner().context(context).subtype("s").reckon().getReference();
        return permissionCheck(SECURE_ADD_ASSIGNMENT_SUBMISSION, resourceString, null);
    }

    @Override
    public boolean allowAddSubmissionCheckGroups(Assignment assignment) {
        return permissionCheckWithGroups(SECURE_ADD_ASSIGNMENT_SUBMISSION, assignment, null);
    }

    @Override
    public List<User> allowAddSubmissionUsers(String assignmentReference) {
        return securityService.unlockUsers(SECURE_ADD_ASSIGNMENT_SUBMISSION, assignmentReference);
    }

    @Override
    public List<User> allowGradeAssignmentUsers(String assignmentReference) {
        List<User> users = securityService.unlockUsers(SECURE_GRADE_ASSIGNMENT_SUBMISSION, assignmentReference);
        String assignmentId = AssignmentReferenceReckoner.reckoner().reference(assignmentReference).reckon().getId();
        try {
            Assignment a = getAssignment(assignmentId);
            if (a.getTypeOfAccess() == GROUP) {
                // for grouped assignment, need to include those users that with "all.groups" and "grade assignment" permissions on the site level
                try {
                    AuthzGroup group = authzGroupService.getAuthzGroup(siteService.siteReference(a.getContext()));
                    // get the roles which are allowed for submission but not for all_site control
                    Set<String> rolesAllowAllSite = group.getRolesIsAllowed(SECURE_ALL_GROUPS);
                    Set<String> rolesAllowGradeAssignment = group.getRolesIsAllowed(SECURE_GRADE_ASSIGNMENT_SUBMISSION);
                    // save all the roles with both "all.groups" and "grade assignment" permissions
                    if (rolesAllowAllSite != null)
                        rolesAllowAllSite.retainAll(rolesAllowGradeAssignment);
                    if (rolesAllowAllSite != null && rolesAllowAllSite.size() > 0) {
                        for (String aRolesAllowAllSite : rolesAllowAllSite) {
                            Set<String> userIds = group.getUsersHasRole(aRolesAllowAllSite);
                            if (userIds != null) {
                                for (String userId : userIds) {
                                    try {
                                        User u = userDirectoryService.getUser(userId);
                                        if (!users.contains(u)) {
                                            users.add(u);
                                        }
                                    } catch (Exception ee) {
                                        log.warn("problem with getting user = {}, {}", userId, ee.getMessage());
                                    }
                                }
                            }
                        }
                    }
                } catch (GroupNotDefinedException gnde) {
                    log.warn("Cannot get authz group for site = {}, {}", a.getContext(), gnde.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("Could not fetch assignment with assignmentId = {}", assignmentId, e);
        }

        return users;
    }

    @Override
    public List<String> allowAddAnySubmissionUsers(String context) {
        List<String> rv = new ArrayList<>();

        try {
            AuthzGroup group = authzGroupService.getAuthzGroup(context);

            // get the roles which are allowed for submission but not for all_site control
            Set<String> rolesAllowSubmission = group.getRolesIsAllowed(SECURE_ADD_ASSIGNMENT_SUBMISSION);
            Set<String> rolesAllowAllSite = group.getRolesIsAllowed(SECURE_ALL_GROUPS);
            rolesAllowSubmission.removeAll(rolesAllowAllSite);

            for (String role : rolesAllowSubmission) {
                rv.addAll(group.getUsersHasRole(role));
            }
        } catch (Exception e) {
            log.warn("Could not get authz group where context = {}", context);
        }

        return rv;
    }

    @Override
    public List<User> allowAddAssignmentUsers(String context) {
        String resourceString = AssignmentReferenceReckoner.reckoner().context(context).reckon().getReference();
        return securityService.unlockUsers(SECURE_ADD_ASSIGNMENT, resourceString);
    }

    @Override
    public boolean allowGetSubmission(String submissionReference) {
        if (permissionCheck(SECURE_ACCESS_ASSIGNMENT_SUBMISSION, submissionReference, null)) return true;
        return permissionCheck(SECURE_ACCESS_ASSIGNMENT, submissionReference, null);
    }

    @Override
    public boolean allowUpdateSubmission(String submissionReference) {
        if (permissionCheck(SECURE_UPDATE_ASSIGNMENT_SUBMISSION, submissionReference, null)) return true;
        return permissionCheck(SECURE_UPDATE_ASSIGNMENT, submissionReference, null);
    }

    @Override
    public boolean allowRemoveSubmission(String submissionReference) {
        return permissionCheck(SECURE_REMOVE_ASSIGNMENT_SUBMISSION, submissionReference, null);
    }

    @Override
    public boolean allowReviewService(Site site) {
        return serverConfigurationService.getBoolean("assignment.useContentReview", false) && contentReviewService != null && contentReviewService.isSiteAcceptable(site);
    }

    @Override
    public boolean allowGradeSubmission(String assignmentReference) {
        return permissionCheck(SECURE_GRADE_ASSIGNMENT_SUBMISSION, assignmentReference, null);
    }

    @Override
    @Transactional
    public Assignment addAssignment(String context) throws PermissionException {
        // security check
        if (!allowAddAssignment(context)) {
            throw new PermissionException(sessionManager.getCurrentSessionUserId(), SECURE_ADD_ASSIGNMENT, null);
        }

        Assignment assignment = new Assignment();
        assignment.setContext(context);
        assignment.setAuthor(sessionManager.getCurrentSessionUserId());
        assignment.setPosition(0);
        assignmentRepository.newAssignment(assignment);

        log.debug("Created new assignment {}", assignment.getId());

        // String reference = AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference();
        // AssignmentAction#post_save_assignment contains the logic for adding a new assignment
        // the event should come at the end of that logic, eventually that logic should be moved here
        // eventTrackingService.post(eventTrackingService.newEvent(AssignmentConstants.EVENT_ADD_ASSIGNMENT, reference, true));

        return assignment;
    }

    @Transactional
    @Override
    public void saveTimeSheetEntry(AssignmentSubmissionSubmitter submissionSubmitter, TimeSheetEntry timeSheetEntry) throws PermissionException {
        if (submissionSubmitter != null && timeSheetEntry != null) {
            AssignmentSubmission submission = submissionSubmitter.getSubmission();
            String siteId = AssignmentReferenceReckoner.reckoner().submission(submission).reckon().getContext();
            if (!allowAddSubmission(siteId)) {
                throw new PermissionException(sessionManager.getCurrentSessionUserId(), SECURE_ADD_ASSIGNMENT_SUBMISSION, null);
            }
            timeSheetEntry.setReference(AssignmentReferenceReckoner.reckoner().submission(submission).reckon().getReference());
            timeSheetService.create(timeSheetEntry);
            log.debug("Add time sheet entry for submitter: {}", submissionSubmitter);
        }
    }

    @Transactional
    @Override
    public void deleteTimeSheetEntry(Long timeSheetEntryId) throws PermissionException {
      String timeSheetEntryReference = getTimeSheetEntryReference(timeSheetEntryId);
      if (StringUtils.isNotBlank(timeSheetEntryReference)) {
          String siteId = AssignmentReferenceReckoner.reckoner().reference(timeSheetEntryReference).reckon().getContext();
          if (!allowAddSubmission(siteId)) {
              throw new PermissionException(sessionManager.getCurrentSessionUserId(), SECURE_UPDATE_ASSIGNMENT_SUBMISSION, null);
          }
          timeSheetService.delete(timeSheetEntryId);
          log.debug("Deleting time sheet entry: {}", timeSheetEntryId);
      } else {
          log.warn("Attempted to delete time sheet entry: {} however it does not exist.", timeSheetEntryId);
      }
    }

    @Override
    public List<TimeSheetEntry> getTimeSheetEntries(AssignmentSubmissionSubmitter submissionSubmitter) {
        if (submissionSubmitter != null) {
            String reference = AssignmentReferenceReckoner.reckoner().submission(submissionSubmitter.getSubmission()).reckon().getReference();
            return timeSheetService.getByReference(reference);
        } else {
            // submission is null
            log.debug("SubmissionSubmitter is null");
            return null;
        }
      }

    public String getTimeSpent(AssignmentSubmission submission) {
        return submission.getSubmitters().stream().findAny().get().getTimeSpent();
    }

    private Assignment mergeAssignment(final String siteId, final Element element, final StringBuilder results, Set<String> assignmentTitles, MergeConfig mcx) throws PermissionException {

        if (!allowAddAssignment(siteId)) {
            throw new PermissionException(sessionManager.getCurrentSessionUserId(), SECURE_ADD_ASSIGNMENT, AssignmentReferenceReckoner.reckoner().context(siteId).reckon().getReference());
        }

        // Serialize the element to a String
        DOMImplementationLS dom = (DOMImplementationLS) element.getOwnerDocument().getImplementation();
        LSSerializer domSerializer = dom.createLSSerializer();
        domSerializer.getDomConfig().setParameter("xml-declaration", false);
        final String xml = domSerializer.writeToString(element);

        // Get an assignment object from the xml
        final Assignment assignmentFromXml = assignmentRepository.fromXML(xml);

        // Remove duplicates from the import
        String assignmentTitle = assignmentFromXml.getTitle();
        if ( assignmentTitles.contains(assignmentTitle) ) return null;

        if (assignmentFromXml != null) {
            assignmentFromXml.setId(null);
            assignmentFromXml.setContext(siteId);
            assignmentFromXml.setContentId(null);
            if ( StringUtils.isNotEmpty(mcx.creatorId) ) assignmentFromXml.setAuthor(mcx.creatorId);

            String newInstructions = ltiService.fixLtiLaunchUrls(assignmentFromXml.getInstructions(), siteId, mcx);
            newInstructions = linkMigrationHelper.migrateLinksInMergedRTE(siteId, mcx, newInstructions);
            assignmentFromXml.setInstructions(newInstructions);

            Long contentKey = ltiService.mergeContentFromImport(element, siteId);
            if ( contentKey != null ) assignmentFromXml.setContentId(contentKey.intValue());

            // Import attachments
            Set<String> oAttachments = assignmentFromXml.getAttachments();
            assignmentFromXml.setAttachments(new HashSet<>());
            for (String oAttachment : oAttachments) {
                String fromResourcePath = mcx.attachmentNames.get(oAttachment);
                String fromContext = null;  // No-Op, there is no fromContext when importing from a ZIP
                String nAttachId = transferAttachment(fromContext, siteId, fromResourcePath, mcx);
                assignmentFromXml.getAttachments().add(nAttachId);
            }

            if (serverConfigurationService.getBoolean(SAK_PROP_ASSIGNMENT_IMPORT_SUBMISSIONS, false)) {
                Set<AssignmentSubmission> submissions = assignmentFromXml.getSubmissions();
                if (submissions != null) {
	                List<String> submitters = submissions.stream().flatMap(s -> s.getSubmitters().stream()).map(AssignmentSubmissionSubmitter::getSubmitter).collect(Collectors.toList());
	                // only if all submitters can be found do we import submissions
	                if (submitters.containsAll(userDirectoryService.getUsers(submitters).stream().map(user -> user.getId()).collect(Collectors.toList()))) {
                        submissions.forEach(s -> s.setId(null));
                        submissions.forEach(s -> s.getSubmitters().forEach(u -> u.setId(null)));
	                }
                }
                else {
	                assignmentFromXml.setSubmissions(new HashSet<>());
                }
            } else {
                // here it is importing the assignment only
                assignmentFromXml.setDraft(true);
                assignmentFromXml.setGroups(new HashSet<>());
                assignmentFromXml.setTypeOfAccess(SITE);
                Map<String, String> properties = assignmentFromXml.getProperties().entrySet().stream()
                        .filter(e -> !PROPERTIES_EXCLUDED_FROM_DUPLICATE_ASSIGNMENTS.contains(e.getKey()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                assignmentFromXml.setProperties(properties);
                assignmentFromXml.setSubmissions(new HashSet<>());
            }
            assignmentRepository.newAssignment(assignmentFromXml);
            String result = "merging assignment " + assignmentFromXml.getId() + " with " + assignmentFromXml.getSubmissions().size() + " submissions.";
            results.append(result).append(LINE_SEPARATOR);
            log.debug(result);
        }

        // After we save the assignment, we can save the private note, model answer, and attachments
        NodeList nodes = element.getElementsByTagName("PrivateNote");
        for (int j=0; j<nodes.getLength(); ++j) {
            Element nodeElement = (Element) nodes.item(j);
            String text = nodeElement.getTextContent();
            String shareWith = nodeElement.getAttribute("shareWith");
            AssignmentNoteItem copy = assignmentSupplementItemService.newNoteItem();
            copy.setAssignmentId(assignmentFromXml.getId());
            copy.setNote(text);
            copy.setShareWith(Integer.parseInt(shareWith));
            copy.setCreatorId(mcx.creatorId);
            assignmentSupplementItemService.saveNoteItem(copy);
        }

        nodes = element.getElementsByTagName("ModelAnswer");
        for (int j=0; j<nodes.getLength(); ++j) {
            Element nodeElement = (Element) nodes.item(j);
            String text = nodeElement.getTextContent();
            String showTo = nodeElement.getAttribute("showTo");
            AssignmentModelAnswerItem copy = assignmentSupplementItemService.newModelAnswer();
            copy.setAssignmentId(assignmentFromXml.getId());
            copy.setText(text);
            copy.setShowTo(Integer.parseInt(showTo));

            // We have to save the model answer so it exists before it can have attachments; otherwise we get a Hibernate exception
            assignmentSupplementItemService.saveModelAnswer(copy);

            NodeList attachments = nodeElement.getElementsByTagName("attachment");
            for (int k=0; k<attachments.getLength(); ++k) {
                Element attachmentElement = (Element) attachments.item(k);
                String attachmentId = attachmentElement.getTextContent();
                AssignmentSupplementItemAttachment attachment = assignmentSupplementItemService.newAttachment();
                String fromContext = null;  // No-Op, there is no fromContext when importing from a ZIP
                String nAttachId = transferAttachment(fromContext, siteId, attachmentId, mcx);
                attachment.setAssignmentSupplementItemWithAttachment(copy);
                attachment.setAttachmentId(nAttachId);
                assignmentSupplementItemService.saveAttachment(attachment);
            }
        }

        return assignmentFromXml;
    }

    @Override
    @Transactional
    public Assignment addDuplicateAssignment(String context, String assignmentId) throws IdInvalidException, PermissionException, IdUsedException, IdUnusedException {
        Assignment assignment = null;

        if (StringUtils.isNoneBlank(context, assignmentId)) {
            if (!assignmentRepository.existsAssignment(assignmentId)) {
                throw new IdUnusedException(assignmentId);
            } else {
                if (!allowAddAssignment(context)) {
                    throw new PermissionException(sessionManager.getCurrentSessionUserId(), SECURE_ADD_ASSIGNMENT, null);
                }

                log.debug("duplicating assignment with ref = {}", assignmentId);

                Assignment existingAssignment = getAssignment(assignmentId);

                assignment = new Assignment();
                assignment.setContext(context);
                assignment.setAuthor(sessionManager.getCurrentSessionUserId());
                assignment.setTitle(existingAssignment.getTitle() + " - " + resourceLoader.getString("assignment.copy"));
                assignment.setInstructions(existingAssignment.getInstructions());
                assignment.setHonorPledge(existingAssignment.getHonorPledge());
                assignment.setSection(existingAssignment.getSection());
                assignment.setOpenDate(existingAssignment.getOpenDate());
                assignment.setDueDate(existingAssignment.getDueDate());
                assignment.setDropDeadDate(existingAssignment.getDropDeadDate());
                assignment.setCloseDate(existingAssignment.getCloseDate());
                assignment.setHideDueDate(existingAssignment.getHideDueDate());
                assignment.setDraft(true);
                assignment.setPosition(existingAssignment.getPosition());
                assignment.setIsGroup(existingAssignment.getIsGroup());
                assignment.setAllowPeerAssessment(existingAssignment.getAllowPeerAssessment());
                assignment.setModifier(userDirectoryService.getCurrentUser().getId());
                assignment.setDateModified(Instant.now());
                if (!existingAssignment.getGroups().isEmpty()) {
                	assignment.setGroups(new HashSet<>(existingAssignment.getGroups()));
                	assignment.setTypeOfAccess(GROUP);
                }

                // peer properties
                assignment.setPeerAssessmentInstructions(existingAssignment.getPeerAssessmentInstructions());
                assignment.setPeerAssessmentAnonEval(existingAssignment.getPeerAssessmentAnonEval());
                assignment.setPeerAssessmentNumberReviews(existingAssignment.getPeerAssessmentNumberReviews());
                assignment.setPeerAssessmentPeriodDate(existingAssignment.getPeerAssessmentPeriodDate());
                assignment.setPeerAssessmentStudentReview(existingAssignment.getPeerAssessmentStudentReview());

                assignment.setTypeOfSubmission(existingAssignment.getTypeOfSubmission());
                assignment.setTypeOfGrade(existingAssignment.getTypeOfGrade());
                assignment.setMaxGradePoint(existingAssignment.getMaxGradePoint());
                assignment.setScaleFactor(existingAssignment.getScaleFactor());
                assignment.setIndividuallyGraded(existingAssignment.getIndividuallyGraded());
                assignment.setReleaseGrades(existingAssignment.getReleaseGrades());
                assignment.setAllowAttachments(existingAssignment.getAllowAttachments());
                // for ContentReview service
                assignment.setContentReview(existingAssignment.getContentReview());

                assignment.setEstimateRequired(existingAssignment.getEstimateRequired());
                assignment.setEstimate(existingAssignment.getEstimate());

                //duplicating attachments
                Set<String> tempAttach = existingAssignment.getAttachments();
                if (tempAttach != null && !tempAttach.isEmpty()){
                    for (String attachId : tempAttach){
                        Reference tempRef = entityManager.newReference(attachId);
                        if (tempRef != null){
                            String tempRefId = tempRef.getId();
                            String tempRefCollectionId = contentHostingService.getContainingCollectionId(tempRefId);
                            try {
                                // get the original attachment display name
                                ResourceProperties p = contentHostingService.getProperties(tempRefId);
                                String displayName = p.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
                                // add another attachment instance
                                String newItemId = contentHostingService.copyIntoFolder(tempRefId, tempRefCollectionId);
                                ContentResourceEdit copy = contentHostingService.editResource(newItemId);
                                // with the same display name
                                ResourcePropertiesEdit pedit = copy.getPropertiesEdit();
                                pedit.addProperty(ResourceProperties.PROP_DISPLAY_NAME, displayName);
                                contentHostingService.commitResource(copy, NotificationService.NOTI_NONE);
                                Reference newRef = entityManager.newReference(copy.getReference());
                                assignment.getAttachments().add(newRef.getReference());
                            } catch (Exception e){
                                log.warn("ERROR DUPLICATING ATTACHMENTS : " + e.toString());
                            }
                        }
                    }
                }

                Map<String, String> properties = assignment.getProperties();
                existingAssignment.getProperties().entrySet().stream()
                        .filter(e -> !PROPERTIES_EXCLUDED_FROM_DUPLICATE_ASSIGNMENTS.contains(e.getKey()))
                        .forEach(e -> properties.put(e.getKey(), e.getValue()));
                
                // Always set duplicated assignments to use "Create new Gradebook item" option
                properties.put(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK, GRADEBOOK_INTEGRATION_ADD);

                assignmentRepository.newAssignment(assignment);
                log.debug("Created duplicate assignment {} from {}", assignment.getId(), assignmentId);

                // Copy model answer
                AssignmentModelAnswerItem existingModelAnswer = assignmentSupplementItemService.getModelAnswer(assignmentId);
                if (existingModelAnswer != null) {
                    AssignmentModelAnswerItem copy = assignmentSupplementItemService.newModelAnswer();
                    copy.setAssignmentId(assignment.getId());
                    copy.setText(existingModelAnswer.getText());
                    copy.setShowTo(existingModelAnswer.getShowTo());

                    // We have to save the model answer so it exists before it can have attachments; otherwise we get a Hibernate exception
                    assignmentSupplementItemService.saveModelAnswer(copy);

                    Set<AssignmentSupplementItemAttachment> attachments = new HashSet<>();
                    List<String> attachmentIDs = assignmentSupplementItemService.getAttachmentListForSupplementItem(existingModelAnswer);
                    for (String attachmentID : attachmentIDs) {
                        AssignmentSupplementItemAttachment attachment = assignmentSupplementItemService.newAttachment();
                        attachment.setAssignmentSupplementItemWithAttachment(copy);
                        attachment.setAttachmentId(attachmentID);
                        assignmentSupplementItemService.saveAttachment(attachment);
                        attachments.add(attachment);
                    }

                    copy.setAttachmentSet(attachments);
                    assignmentSupplementItemService.saveModelAnswer(copy); // save again to persist attachments
                }

                // Copy Private Note
                AssignmentNoteItem oNoteItem = assignmentSupplementItemService.getNoteItem(assignmentId);
                if (oNoteItem != null) {
                    AssignmentNoteItem nNoteItem = assignmentSupplementItemService.newNoteItem();
                    nNoteItem.setAssignmentId(assignment.getId());
                    nNoteItem.setNote(oNoteItem.getNote());
                    nNoteItem.setShareWith(oNoteItem.getShareWith());
                    nNoteItem.setCreatorId(userDirectoryService.getCurrentUser().getId());
                    assignmentSupplementItemService.saveNoteItem(nNoteItem);
                }

                // Copy All Purpose
                AssignmentAllPurposeItem existingAllPurposeItem = assignmentSupplementItemService.getAllPurposeItem(assignmentId);
                if (existingAllPurposeItem != null) {
                    AssignmentAllPurposeItem nAllPurposeItem = assignmentSupplementItemService.newAllPurposeItem();
                    nAllPurposeItem.setAssignmentId(assignment.getId());
                    nAllPurposeItem.setTitle(existingAllPurposeItem.getTitle());
                    nAllPurposeItem.setText(existingAllPurposeItem.getText());
                    nAllPurposeItem.setHide(existingAllPurposeItem.getHide());
                    nAllPurposeItem.setReleaseDate(existingAllPurposeItem.getReleaseDate());
                    nAllPurposeItem.setRetractDate(existingAllPurposeItem.getRetractDate());
                    assignmentSupplementItemService.saveAllPurposeItem(nAllPurposeItem);
                    Set<AssignmentSupplementItemAttachment> attachments = new HashSet<>();
                    List<String> attachmentIDs = assignmentSupplementItemService.getAttachmentListForSupplementItem(existingAllPurposeItem);
                    for (String attachmentID : attachmentIDs) {
                        AssignmentSupplementItemAttachment attachment = assignmentSupplementItemService.newAttachment();
                        attachment.setAssignmentSupplementItemWithAttachment(nAllPurposeItem);
                        attachment.setAttachmentId(attachmentID);
                        assignmentSupplementItemService.saveAttachment(attachment);
                        attachments.add(attachment);
                    }
                    nAllPurposeItem.setAttachmentSet(attachments);
                    assignmentSupplementItemService.cleanAllPurposeItemAccess(nAllPurposeItem);
                    Set<AssignmentAllPurposeItemAccess> accessSet = new HashSet<>();
                    Set<AssignmentAllPurposeItemAccess> existingAccessSet = existingAllPurposeItem.getAccessSet();
                    for (AssignmentAllPurposeItemAccess assignmentAllPurposeItemAccess : existingAccessSet) {
                        AssignmentAllPurposeItemAccess access = assignmentSupplementItemService.newAllPurposeItemAccess();
                        access.setAccess(assignmentAllPurposeItemAccess.getAccess());
                        access.setAssignmentAllPurposeItem(nAllPurposeItem);
                        assignmentSupplementItemService.saveAllPurposeItemAccess(access);
                        accessSet.add(access);
                    }
                    nAllPurposeItem.setAccessSet(accessSet);
                    assignmentSupplementItemService.saveAllPurposeItem(nAllPurposeItem);
                }

                //copy rubric
                try {
                    Optional<ToolItemRubricAssociation> rubricAssociation = rubricsService.getRubricAssociation(AssignmentConstants.TOOL_ID, assignmentId);
                    if (rubricAssociation.isPresent()) {
                        rubricsService.saveRubricAssociation(AssignmentConstants.TOOL_ID, assignment.getId(), rubricAssociation.get().getFormattedAssociation());
                    }
                } catch(Exception e){
                    log.error("Error while trying to duplicate Rubrics: {} ", e.getMessage());
                }

                // copy tags
                if (serverConfigurationService.getBoolean("tagservice.enable.integrations", true)) {
                    List<String> tagIds = tagService.getTagAssociationIds(assignment.getContext(), assignmentId);
                    tagService.updateTagAssociations(assignment.getContext(), assignment.getId(), tagIds, true);
                }

                String reference = AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference();
                // event for tracking
                eventTrackingService.post(eventTrackingService.newEvent(AssignmentConstants.EVENT_ADD_ASSIGNMENT, reference, true));
            }
        }
        return assignment;
    }

    @Override
    @Transactional
    public void deleteAssignment(Assignment assignment) throws PermissionException {
        Objects.requireNonNull(assignment, "Assignment cannot be null");
        log.debug("Attempting to delete assignment with id = {}", assignment.getId());
        String reference = AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference();

        if (!allowRemoveAssignment(reference)) {
            throw new PermissionException(sessionManager.getCurrentSessionUserId(), SECURE_REMOVE_ASSIGNMENT, null);
        }

        String context = assignment.getContext();

        assignmentDueReminderService.removeScheduledReminder(assignment.getId());
        assignmentRepository.deleteAssignment(assignment.getId());

        for (String groupReference : assignment.getGroups()) {
            try {
                AuthzGroup group = authzGroupService.getAuthzGroup(groupReference);
                group.setLockForReference(reference, AuthzGroup.RealmLockMode.NONE);
                authzGroupService.save(group);
            } catch (GroupNotDefinedException | AuthzPermissionException e) {
                log.warn("Exception while removing lock for assignment {}, {}", assignment.getId(), e.toString());
            }
        }

        eventTrackingService.post(eventTrackingService.newEvent(AssignmentConstants.EVENT_REMOVE_ASSIGNMENT, reference, true));

        // remove any realm defined for this resource
        try {
            authzGroupService.removeAuthzGroup(reference);
            log.debug("successful delete for assignment with id = {}", assignment.getId());
        } catch (AuthzPermissionException ape) {
            log.warn("deleting realm for assignment reference = {}, {}", reference, ape.toString());
        } catch (AuthzRealmLockException arle) {
            log.warn("GROUP LOCK REGRESSION: {}", arle.toString());
        }

        // clean up content-review items
        contentReviewService.deleteAssignment(context, reference);
    }

    @Override
    @Transactional
    public void softDeleteAssignment(Assignment assignment) throws PermissionException {
        Objects.requireNonNull(assignment, "Assignment cannot be null");
        // we don't actually want to delete assignments just mark them as deleted "soft delete feature"
        log.debug("Attempting to soft delete assignment with id = {}", assignment.getId());
        String reference = AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference();

        if (!allowRemoveAssignment(reference)) {
            throw new PermissionException(sessionManager.getCurrentSessionUserId(), SECURE_REMOVE_ASSIGNMENT, null);
        }

        taskService.removeTaskByReference(reference);

        assignmentDueReminderService.removeScheduledReminder(assignment.getId());
        assignmentRepository.softDeleteAssignment(assignment.getId());

        // we post the same event as remove assignment
        eventTrackingService.post(eventTrackingService.newEvent(AssignmentConstants.EVENT_REMOVE_ASSIGNMENT, reference, true));
    }

    // TODO removing related content from other tools shouldn't be the concern for assignments service
    // it should post an event and let those tools take action.
    // * Unless a transaction is required
    @Override
    @Transactional
    public void deleteAssignmentAndAllReferences(Assignment assignment) throws PermissionException {
        Objects.requireNonNull(assignment, "Assignment cannot be null");
        log.debug("Removing all associated reference to assignment with id = {}", assignment.getId());

        Entity entity = createAssignmentEntity(assignment.getId());

        // CHECK PERMISSION
        permissionCheck(SECURE_REMOVE_ASSIGNMENT, entity.getReference(), null);

        // we may need to remove associated calendar events and annc, so get the basic info here
//            ResourcePropertiesEdit pEdit = assignment.getPropertiesEdit();
//            String context = assignment.getContext();

        // 1. remove associated calendar events, if exists
        removeAssociatedCalendarItem(getCalendar(assignment.getContext()), assignment);

        // 2. remove associated announcement, if exists
        removeAssociatedAnnouncementItem(getAnnouncementChannel(assignment.getContext()), assignment);

        // 3. remove Gradebook items, if linked
        removeAssociatedGradebookItem(assignment);

        // 4. remove tags as necessary
        removeAssociatedTaggingItem(assignment);

        // 5. remove assignment submissions
//            List submissions = getSubmissions(assignment);
//            if (submissions != null) {
//                for (Iterator sIterator = submissions.iterator(); sIterator.hasNext(); ) {
//                    AssignmentSubmission s = (AssignmentSubmission) sIterator.next();
//                    String sReference = s.getReference();
//                    try {
//                        removeSubmission(editSubmission(sReference));
//                    } catch (PermissionException e) {
//                        M_log.warn("removeAssignmentAndAllReference: User does not have permission to remove submission " + sReference + " for assignment: " + assignment.getId() + e.getMessage());
//                    } catch (InUseException e) {
//                        M_log.warn("removeAssignmentAndAllReference: submission " + sReference + " for assignment: " + assignment.getId() + " is in use. " + e.getMessage());
//                    } catch (IdUnusedException e) {
//                        M_log.warn("removeAssignmentAndAllReference: submission " + sReference + " for assignment: " + assignment.getId() + " does not exist. " + e.getMessage());
//                    }
//                }
//            }

        // 6. remove associated content object
//            try {
//                removeAssignmentContent(editAssignmentContent(assignment.getContent().getReference()));
//            } catch (AssignmentContentNotEmptyException e) {
//                M_log.warn(" deleteAssignmentAndAllReferences(): cannot remove non-empty AssignmentContent object for assignment = " + assignment.getId() + ". " + e.getMessage());
//            } catch (PermissionException e) {
//                M_log.warn(" deleteAssignmentAndAllReferences(): not allowed to remove AssignmentContent object for assignment = " + assignment.getId() + ". " + e.getMessage());
//            } catch (InUseException e) {
//                M_log.warn(" deleteAssignmentAndAllReferences(): AssignmentContent object for assignment = " + assignment.getId() + " is in used. " + e.getMessage());
//            } catch (IdUnusedException e) {
//                M_log.warn(" deleteAssignmentAndAllReferences(): cannot find AssignmentContent object for assignment = " + assignment.getId() + ". " + e.getMessage());
//            }

        // 7. remove assignment
        softDeleteAssignment(assignment);

        // close the edit object
//            ((BaseAssignmentEdit) assignment).closeEdit();

        // 8. remove any realm defined for this resource
//            try {
//                authzGroupService.removeAuthzGroup(assignment.getReference());
//            } catch (AuthzPermissionException e) {
//                M_log.warn(" deleteAssignment: removing realm for assignment reference=" + assignment.getReference() + " : " + e.getMessage());
//            }
//
//            // track event
//            eventTrackingService.post(eventTrackingService.newEvent(AssignmentConstants.EVENT_REMOVE_ASSIGNMENT, assignment.getReference(), true));
    }

    @Override
    @Transactional
    public AssignmentSubmission addSubmission(String assignmentId, String submitter) throws PermissionException {

        if (StringUtils.isBlank(submitter)) return null;
        Optional<Assignment> assignment = Optional.empty();

        try {
            assignment = Optional.ofNullable(getAssignment(assignmentId));
        } catch (IdUnusedException iue) {
            log.warn("A submission cannot be added to an unknown assignment: {}", assignmentId);
        }

        if (assignment.isPresent()) {
            Assignment a = assignment.get();
            String assignmentReference = AssignmentReferenceReckoner.reckoner().assignment(a).reckon().getReference();
            // check permissions - allow the user to add a submission to the assignment
            // if they have the permission asn.submit or asn.grade
            if (a.getTypeOfAccess() == GROUP) {
                if (!(allowAddSubmissionCheckGroups(a) || allowGradeSubmission(assignmentReference))) {
                    throw new PermissionException(sessionManager.getCurrentSessionUserId(), SECURE_ADD_ASSIGNMENT_SUBMISSION, assignmentReference);
                }
            } else {
                if (!(allowAddSubmission(a.getContext()) || allowGradeSubmission(assignmentReference))) {
                    throw new PermissionException(sessionManager.getCurrentSessionUserId(), SECURE_ADD_ASSIGNMENT_SUBMISSION, assignmentReference);
                }
            }

            // Prevent users from having more than one submission, currently assignments expects groups or users to
            // only have a single submission. When assignments decides to support multiple submissions this should be removed.
            if (a.getIsGroup()) {
                AssignmentSubmission existingSubmission = assignmentRepository.findSubmissionForGroup(a.getId(), submitter);
                if (existingSubmission != null) {
                    return existingSubmission;
                }
            } else {
                AssignmentSubmission existingSubmission = assignmentRepository.findSubmissionForUser(a.getId(), submitter);
                if (existingSubmission != null) {
                    return existingSubmission;
                }
            }

            Site site;
            try {
                site = siteService.getSite(a.getContext());
            } catch (IdUnusedException iue) {
                log.warn("Site not found while attempting to add a submission to assignment: {}, site: {}", assignmentId, a.getContext());
                return null;
            }

            Set<AssignmentSubmissionSubmitter> submissionSubmitters = new HashSet<>();
            Optional<String> groupId = Optional.empty();
            if (site != null) {
                if (a.getIsGroup()) {
                    Group group = site.getGroup(submitter);
                    if (group != null && a.getGroups().contains(group.getReference())) {
                        group.getMembers().stream()
                                .filter(m -> (m.getRole().isAllowed(SECURE_ADD_ASSIGNMENT_SUBMISSION) || group.isAllowed(m.getUserId(), SECURE_ADD_ASSIGNMENT_SUBMISSION))
                                        && !m.getRole().isAllowed(SECURE_GRADE_ASSIGNMENT_SUBMISSION)
                                        && !group.isAllowed(m.getUserId(), SECURE_GRADE_ASSIGNMENT_SUBMISSION))
                                .forEach(member -> {
                                    AssignmentSubmissionSubmitter ass = new AssignmentSubmissionSubmitter();
                                    ass.setSubmitter(member.getUserId());
                                    submissionSubmitters.add(ass);
                                });
                        groupId = Optional.of(submitter);
                    } else {
                        log.warn("A submission cannot be added for group {} to assignment {}", submitter, assignmentId);
                    }
                } else {
                    if (site.getMember(submitter) != null) {
                        AssignmentSubmissionSubmitter submissionSubmitter = new AssignmentSubmissionSubmitter();
                        submissionSubmitter.setSubmitter(submitter);
                        submissionSubmitters.add(submissionSubmitter);
                    } else {
                        log.warn("Cannot add a submission for submitter {} to assignment {} as they are not a member of the site", submitter, assignmentId);
                    }
                }
            }

            if (submissionSubmitters.isEmpty()) {
                log.warn("A submission can't be added to assignment {} with no submitters", a.getId());
                return null;
            }

            // identify who the submittee is using the session
            String currentUser = sessionManager.getCurrentSessionUserId();
            submissionSubmitters.stream().filter(s -> s.getSubmitter().equals(currentUser)).findFirst().ifPresent(s -> s.setSubmittee(true));

            AssignmentSubmission submission = assignmentRepository.newSubmission(a.getId(), groupId, Optional.of(submissionSubmitters), Optional.empty(), Optional.empty(), Optional.empty());

            String submissionReference = AssignmentReferenceReckoner.reckoner().submission(submission).reckon().getReference();
            eventTrackingService.post(eventTrackingService.newEvent(AssignmentConstants.EVENT_ADD_ASSIGNMENT_SUBMISSION, submissionReference, true));

            log.debug("New submission: {} added to assignment: {}", submission.getId(), assignmentId);
            return submission;
        }
        return null;
    }

    @Override
    @Transactional
    public void removeSubmission(AssignmentSubmission submission) throws PermissionException {
        if (submission != null) {
            String reference = AssignmentReferenceReckoner.reckoner().submission(submission).reckon().getReference();
            // check security
            if (!permissionCheck(SECURE_REMOVE_ASSIGNMENT_SUBMISSION, reference, null)) {
                throw new PermissionException(sessionManager.getCurrentSessionUserId(), SECURE_REMOVE_ASSIGNMENT_SUBMISSION, reference);
            }

            // remove submission
            assignmentRepository.deleteSubmission(submission.getId());

            // track event
            eventTrackingService.post(eventTrackingService.newEvent(AssignmentConstants.EVENT_REMOVE_ASSIGNMENT_SUBMISSION, reference, true));

            try {
                authzGroupService.removeAuthzGroup(authzGroupService.getAuthzGroup(reference));
            } catch (AuthzPermissionException e) {
                log.warn("removing realm for : {} : {}", reference, e.getMessage());
            } catch (GroupNotDefinedException e) {
                log.warn("cannot find group for submission : {} : {}", reference, e.getMessage());
            } catch (AuthzRealmLockException arle) {
                log.warn("GROUP LOCK REGRESSION: {}", arle.getMessage(), arle);
            }
        }
    }

    @Override
    @Transactional
    public void updateAssignment(Assignment assignment) throws PermissionException {
        Assert.notNull(assignment, "Assignment cannot be null");
        Assert.notNull(assignment.getId(), "Assignment doesn't appear to have been persisted yet");

        String reference = AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference();
        // security check
        if (!allowUpdateAssignment(reference)) {
            throw new PermissionException(sessionManager.getCurrentSessionUserId(), SECURE_UPDATE_ASSIGNMENT, null);
        }

        Collection<String> prevAssignedGroupRefs = assignmentRepository.findGroupsForAssignmentById(assignment.getId());
        switch (assignment.getTypeOfAccess()) {
            case GROUP:
                Collection<String> currentAssignedGroupRefs = assignment.getGroups();
                // get the unassigned Group References by removing the current ones !
                prevAssignedGroupRefs.removeAll(currentAssignedGroupRefs);

                Site site = null;
                try {
                    site = siteService.getSite(assignment.getContext());
                } catch (IdUnusedException e) {
                    log.warn("Could not get the site {} for assignment {} while updating it", assignment.getContext(), assignment.getId());
                }
                // on Lessons, a group assignment or one with prereq. will be managed through a single (access) group
                Group anAssignedGroup = site.getGroup(currentAssignedGroupRefs.stream().findFirst().orElse(null));
                // we can infer it is an ACCESS group if it has the following property
                boolean accessGroupAssigned = StringUtils.isNotBlank(anAssignedGroup.getProperties().getProperty("lessonbuilder_ref"));

                // If the assigned group is not an ACCESS group...
                if (accessGroupAssigned == false) {
                    for (String groupRef : prevAssignedGroupRefs) { // remove locks for groups that were removed or unassigned
                        try {
                            AuthzGroup group = authzGroupService.getAuthzGroup(groupRef);
                            group.setLockForReference(reference, AuthzGroup.RealmLockMode.NONE);
                            authzGroupService.save(group);
                        } catch (GroupNotDefinedException | AuthzPermissionException e) {
                            log.warn("Exception while removing lock for assignment {}, {}", assignment.getId(), e.toString());
                        }
                    }
                }
                if (!assignment.getDraft()) { // don't add locks for draft assignments
                    if (assignment.getIsGroup()) { // lock mode ALL for group assignments
                        for (String groupRef : currentAssignedGroupRefs) {
                            try {
                                Group assignedGroup = site.getGroup(groupRef);
                                boolean isAccessGroup = StringUtils.isNotBlank(assignedGroup.getProperties().getProperty("lessonbuilder_ref"));

                                AuthzGroup group = authzGroupService.getAuthzGroup(groupRef);
                                if (isAccessGroup) {
                                    // exception: it's an ACCESS group, membership won't be locked
                                    // as users are dynamically added to be able to see the related assignment on Lessons
                                    group.setLockForReference(reference, AuthzGroup.RealmLockMode.DELETE);
                                } else {
                                    group.setLockForReference(reference, AuthzGroup.RealmLockMode.ALL);
                                }
                                authzGroupService.save(group);
                            } catch (GroupNotDefinedException | AuthzPermissionException e) {
                                log.warn("Exception while adding lock ALL for assignment {}, {}", assignment.getId(), e.toString());
                            }
                        }
                    } else { // lock mode DELETE for assignments released to groups through Lessons
                        for (String groupRef : currentAssignedGroupRefs) {
                            try {
                                AuthzGroup group = authzGroupService.getAuthzGroup(groupRef);
                                group.setLockForReference(reference, AuthzGroup.RealmLockMode.DELETE);
                                authzGroupService.save(group);
                            } catch (GroupNotDefinedException | AuthzPermissionException e) {
                                log.warn("Exception while adding lock DELETE for assignment {}, {}", assignment.getId(), e.toString());
                            }
                        }
                    }
                }
                break;
            case SITE:
                for (String groupRef : prevAssignedGroupRefs) { // remove all locks if they exist
                    try {
                        AuthzGroup group = authzGroupService.getAuthzGroup(groupRef);
                        group.setLockForReference(reference, AuthzGroup.RealmLockMode.NONE);
                        authzGroupService.save(group);
                    } catch (GroupNotDefinedException | AuthzPermissionException e) {
                        log.warn("Exception while clearing lock for assignment {}, {}", assignment.getId(), e.toString());
                    }
                }
                break;
            default:
                log.warn("Unknown Access for assignment {}, access={}", assignment.getId(), assignment.getTypeOfAccess().name());
        }

        assignment.setDateModified(Instant.now());
        assignment.setModifier(sessionManager.getCurrentSessionUserId());
        Assignment updatedAssignment = assignmentRepository.merge(assignment);

        Task task = new Task();
        task.setSiteId(updatedAssignment.getContext());
        task.setReference(reference);
        task.setSystem(true);
        task.setDescription(updatedAssignment.getTitle());
        task.getGroups().addAll(updatedAssignment.getGroups());
        task.setStarts(updatedAssignment.getOpenDate());

        if (!updatedAssignment.getHideDueDate()) {
            task.setDue(updatedAssignment.getDueDate());
        }

        if (!updatedAssignment.getDraft()) {
            taskService.createTask(task, allowAddSubmissionUsers(reference)
                    .stream().map(User::getId).collect(Collectors.toSet()),
                    Priorities.HIGH);
        }

    }

    @Override
    @Transactional
    public void updateSubmission(AssignmentSubmission submission) throws PermissionException {
        Assert.notNull(submission, "Submission cannot be null");
        Assert.notNull(submission.getId(), "Submission doesn't appear to have been persisted yet");

        String reference = AssignmentReferenceReckoner.reckoner().submission(submission).reckon().getReference();
        String assignmentReference = AssignmentReferenceReckoner.reckoner().assignment(submission.getAssignment()).reckon().getReference();

        // TODO these permissions checks should coincide with the changes that are being made for the submission
        if (!(allowUpdateSubmission(reference) || allowGradeSubmission(assignmentReference))) {
            throw new PermissionException(sessionManager.getCurrentSessionUserId(), SECURE_UPDATE_ASSIGNMENT_SUBMISSION, reference);
        }
        eventTrackingService.post(eventTrackingService.newEvent(AssignmentConstants.EVENT_UPDATE_ASSIGNMENT_SUBMISSION, reference, true));

        assignmentRepository.updateSubmission(submission);

        // Assignment Submission Notifications
        Instant dateReturned = submission.getDateReturned();
        Instant dateSubmitted = submission.getDateSubmitted();
        if (!submission.getSubmitted()) {
            // if the submission is not submitted then saving a submission event
            eventTrackingService.post(eventTrackingService.newEvent(AssignmentConstants.EVENT_SAVE_ASSIGNMENT_SUBMISSION, reference, true));
        } else if (dateReturned == null && !submission.getReturned() && (dateSubmitted == null || submission.getDateModified().toEpochMilli() - dateSubmitted.toEpochMilli() > 1000 * 60)) {
            // make sure the last modified time is at least one minute after the submit time
            if (!(StringUtils.trimToNull(submission.getSubmittedText()) == null && submission.getAttachments().isEmpty() && StringUtils.trimToNull(submission.getGrade()) == null && StringUtils.trimToNull(submission.getFeedbackText()) == null && StringUtils.trimToNull(submission.getFeedbackComment()) == null && submission.getFeedbackAttachments().isEmpty())) {
                if (submission.getGraded()) {
                	//TODO: This should use an LRS_Group when that exists rather than firing off individual events for each LRS_Actor KNL-1560
                    for (AssignmentSubmissionSubmitter submitter : submission.getSubmitters()) {
                        try {
                            User user = userDirectoryService.getUser(submitter.getSubmitter());
                            LRS_Statement statement = getStatementForAssignmentGraded(reference, submission.getAssignment(), submission, user);
                            // graded and saved before releasing it
                            Event event = eventTrackingService.newEvent(AssignmentConstants.EVENT_GRADE_ASSIGNMENT_SUBMISSION, reference, null, true, NotificationService.NOTI_OPTIONAL, statement);
                            eventTrackingService.post(event);
                        } catch (UserNotDefinedException e) {
                            log.warn("Assignments could not find user ({}) while registering Event for LRSS", submitter.getSubmitter());
                        }
                    }
                }
            }
        } else if (dateReturned != null && submission.getGraded() && (dateSubmitted == null || dateReturned.isAfter(dateSubmitted) || dateSubmitted.isAfter(dateReturned) && submission.getDateModified().isAfter(dateSubmitted))) {
            if (submission.getGraded()) {
                // Send a non LRS event for other listeners to handle, user notifications for instance.
                Event event = eventTrackingService.newEvent(AssignmentConstants.EVENT_GRADE_ASSIGNMENT_SUBMISSION, reference, null, true, NotificationService.NOTI_NONE, true);
                eventTrackingService.post(event);

            	//TODO: This should use an LRS_Group when that exists rather than firing off individual events for each LRS_Actor KNL-1560
                for (AssignmentSubmissionSubmitter submitter : submission.getSubmitters()) {
                    try {
                        User user = userDirectoryService.getUser(submitter.getSubmitter());
                        LRS_Statement statement = getStatementForAssignmentGraded(reference, submission.getAssignment(), submission, user);
                        // releasing a submitted assignment or releasing grade to an unsubmitted assignment
                        Event lrsEvent = eventTrackingService.newEvent(AssignmentConstants.EVENT_GRADE_ASSIGNMENT_SUBMISSION, reference, null, true, NotificationService.NOTI_OPTIONAL, statement);
                        eventTrackingService.post(lrsEvent);
                    } catch (UserNotDefinedException e) {
                        log.warn("Assignments could not find user ({}) while registering Event for LRSS", submitter.getSubmitter());
                    }
                }
            }

            // if this is releasing grade, depending on the release grade notification setting, send email notification to student
            sendGradeReleaseNotification(submission);
        } else if (dateSubmitted == null) {
           	//TODO: This should use an LRS_Group when that exists rather than firing off individual events for each LRS_Actor KNL-1560
            for (AssignmentSubmissionSubmitter submitter : submission.getSubmitters()) {
                try {
                    User user = userDirectoryService.getUser(submitter.getSubmitter());
                    LRS_Statement statement = getStatementForUnsubmittedAssignmentGraded(reference, submission.getAssignment(), submission, user);
                    // releasing a submitted assignment or releasing grade to an unsubmitted assignment
                    Event event = eventTrackingService.newEvent(AssignmentConstants.EVENT_GRADE_ASSIGNMENT_SUBMISSION, reference, null, true, NotificationService.NOTI_OPTIONAL, statement);
                    eventTrackingService.post(event);
                } catch (UserNotDefinedException e) {
                    log.warn("Assignments could not find user ({}) while registering Event for LRSS", submitter.getSubmitter());
                }
            }
        } else {
            // submitting a submission
            Assignment a = submission.getAssignment();
            LRS_Statement statement = getStatementForSubmitAssignment(a.getId(), serverConfigurationService.getAccessUrl(), a.getTitle());
            eventTrackingService.post(eventTrackingService.newEvent(AssignmentConstants.EVENT_SUBMIT_ASSIGNMENT_SUBMISSION, reference, null, true, NotificationService.NOTI_OPTIONAL, statement));

            // only doing the notification for real online submissions
            if (a.getTypeOfSubmission() != Assignment.SubmissionType.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION) {
                // instructor notification
                notificationToInstructors(submission, a);

                // student notification, whether the student gets email notification once he submits an assignment
                notificationToStudent(submission, a);

                List<String> submitterIds
                    = submission.getSubmitters().stream()
                        .map(ass -> ass.getSubmitter()).collect(Collectors.toList());
                taskService.completeUserTaskByReference(assignmentReference, submitterIds);
            }
        }
    }

    @Override
    public Assignment getAssignment(Reference reference) throws IdUnusedException, PermissionException {
        Assert.notNull(reference, "Reference cannot be null");
        if (StringUtils.equals(SAKAI_ASSIGNMENT, reference.getType())) {
            return getAssignment(reference.getId());
        }
        return null;
    }

    @Override
    public Assignment getAssignment(String assignmentId) throws IdUnusedException, PermissionException {
        log.debug("GET ASSIGNMENT : ID : {}", assignmentId);

        Assignment assignment = assignmentRepository.findAssignment(assignmentId);
        if (assignment == null) throw new IdUnusedException(assignmentId);

        String currentUserId = sessionManager.getCurrentSessionUserId();
        // check security on the assignment

        return checkAssignmentAccessibleForUser(assignment, currentUserId);
    }

    @Override
    public AssignmentConstants.Status getAssignmentCannonicalStatus(String assignmentId) throws IdUnusedException, PermissionException {
        Assignment assignment = getAssignment(assignmentId);
        ZonedDateTime currentTime = ZonedDateTime.now();

        // TODO these status's should be an enum and translation should occur in tool
        if (assignment.getDraft()) {
            return AssignmentConstants.Status.DRAFT;
        } else if (assignment.getOpenDate().isAfter(currentTime.toInstant())) {
            return AssignmentConstants.Status.NOT_OPEN;
        } else if (assignment.getDueDate().isAfter(currentTime.toInstant())) {
            return AssignmentConstants.Status.OPEN;
        } else if ((assignment.getCloseDate() != null) && (assignment.getCloseDate().isBefore(currentTime.toInstant()))) {
            return AssignmentConstants.Status.CLOSED;
        } else {
            return AssignmentConstants.Status.DUE;
        }
    }

    @Override
    public Collection<Assignment> getAssignmentsForContext(String context) {
        log.debug("GET ASSIGNMENTS : CONTEXT : {}", context);
        List<Assignment> assignments = new ArrayList<>();
        if (StringUtils.isBlank(context)) return assignments;

        // access is checked for each assignment:
        //   - drafts accessible by owner or if user has share drafts permission
        //   - if assignment is restricted to groups only those in the group
        //   - minimally user needs read permission
        for (Assignment assignment : assignmentRepository.findAssignmentsBySite(context)) {
        	String currentUserId = sessionManager.getCurrentSessionUserId();
        	boolean canViewAssigment = false;
        	try {
                checkAssignmentAccessibleForUser(assignment, currentUserId);
                canViewAssigment = true;
            } catch (PermissionException e) {
            	canViewAssigment = false;
            }
            if(canViewAssigment) {
                if (assignment.getDraft()) {
                    if (isDraftAssignmentVisible(assignment)) {
                        // only those who can see a draft assignment
                        assignments.add(assignment);
                    }
                } else if (assignment.getTypeOfAccess() == GROUP) {
                    if (permissionCheckWithGroups(SECURE_ACCESS_ASSIGNMENT, assignment, null)) {
                        assignments.add(assignment);
                    }
                } else if (allowGetAssignment(context)) {
                    assignments.add(assignment);
                }
            }
        }

        return assignments;
    }

    @Override
    public Collection<Assignment> getDeletedAssignmentsForContext(String context) {
        log.debug("GET DELETED ASSIGNMENTS : CONTEXT : {}", context);
        List<Assignment> assignments = new ArrayList<>();
        if (StringUtils.isBlank(context)) return assignments;

        return assignmentRepository.findDeletedAssignmentsBySite(context);
    }

    @Override
    public Map<Assignment, List<String>> getSubmittableAssignmentsForContext(String context) {
        Map<Assignment, List<String>> submittable = new HashMap<>();
        if (!allowGetAssignment(context)) {
            // no permission to read assignment in context
            return submittable;
        }

        try {
            Site site = siteService.getSite(context);
            Set<String> siteSubmitterIds = authzGroupService.getUsersIsAllowed(SECURE_ADD_ASSIGNMENT_SUBMISSION, Arrays.asList(site.getReference()));
            Map<String, Set<String>> groupIdUserIds = new HashMap<>();
            for (Group group : site.getGroups()) {
                String groupRef = group.getReference();
                for (Member member : group.getMembers()) {
                    if (member.getRole().isAllowed(SECURE_ADD_ASSIGNMENT_SUBMISSION)) {
                        if (!groupIdUserIds.containsKey(groupRef)) {
                            groupIdUserIds.put(groupRef, new HashSet<>());
                        }
                        groupIdUserIds.get(groupRef).add(member.getUserId());
                    }
                }
            }

            // TODO this called getAccessibleAssignments need to implement
            for (Assignment assignment : getAssignmentsForContext(context)) {
                Set<String> userIds = new HashSet<>();
                if (assignment.getTypeOfAccess() == GROUP) {
                    for (String groupRef : assignment.getGroups()) {
                        if (groupIdUserIds.containsKey(groupRef)) {
                            userIds.addAll(groupIdUserIds.get(groupRef));
                        }
                    }
                } else {
                    userIds.addAll(siteSubmitterIds);
                }
                submittable.put(assignment, new ArrayList<>(userIds));
            }
        } catch (IdUnusedException e) {
            log.debug("Could not retrieve submittable assignments for nonexistent site: {}", context);
        }

        return submittable;
    }

    @Override
    public AssignmentSubmission getSubmission(String submissionId) throws PermissionException {
        if (StringUtils.isNotBlank(submissionId)) {
            AssignmentSubmission submission = assignmentRepository.findSubmission(submissionId);
            if (submission != null) {
                String reference = AssignmentReferenceReckoner.reckoner().submission(submission).reckon().getReference();
                if (allowGetSubmission(reference)) {
                    return submission;
                } else {
                    throw new PermissionException(sessionManager.getCurrentSessionUserId(), SECURE_ACCESS_ASSIGNMENT_SUBMISSION, reference);
                }
            }
        }
        log.debug("Submission ID does not exist {}", submissionId);
        return null;
    }

    private String getTimeSheetEntryReference(Long timeSheetId) {
      return timeSheetService.getTimeSheetEntryReference(timeSheetId);

    }

    @Override
    @Transactional
    public AssignmentSubmission getSubmission(String assignmentId, User person) throws PermissionException {
        return getSubmission(assignmentId, person.getId());
    }

    @Override
    @Transactional
    public AssignmentSubmission getSubmission(String assignmentId, String submitterId) throws PermissionException {

        if (!StringUtils.isAnyBlank(assignmentId, submitterId)) {
            // normal submission lookup where submitterId is for a user
            AssignmentSubmission submission = assignmentRepository.findSubmissionForUser(assignmentId, submitterId);
            if (submission == null) {
                // if not found submitterId could be a group id
                submission = assignmentRepository.findSubmissionForGroup(assignmentId, submitterId);
            }

            if (submission != null) {
                String reference = AssignmentReferenceReckoner.reckoner().submission(submission).reckon().getReference();
                if (allowGetSubmission(reference)) {
                    return submission;
                } else {
                    throw new PermissionException(sessionManager.getCurrentSessionUserId(), SECURE_ACCESS_ASSIGNMENT_SUBMISSION, reference);
                }
            } else {
                // submission not found looked for a user submission and group submission
                log.debug("No submission found for user {} in assignment {}", submitterId, assignmentId);
            }
        }
        return null;
    }

    @Override
    public AssignmentSubmission getSubmission(List<AssignmentSubmission> submissions, User person) {
        throw new UnsupportedOperationException("Method is deprecated, remove");
    }

    @Override
    public Set<AssignmentSubmission> getSubmissions(Assignment assignment) {
        return assignment.getSubmissions();
    }

    @Override
    public String getAssignmentStatus(String assignmentId) {
        Assignment assignment = null;
        try {
            assignment = getAssignment(assignmentId);

            Instant now = Instant.now();
            if (assignment.getDraft()) {
                return resourceLoader.getString("gen.dra1");
            } else if (assignment.getOpenDate().isAfter(now)) {
                return resourceLoader.getString("gen.notope");
            } else if (assignment.getDueDate().isAfter(now)) {
                return resourceLoader.getString("gen.open");
            } else if ((assignment.getCloseDate() != null) && (assignment.getCloseDate().isBefore(now))) {
                return resourceLoader.getString("gen.closed");
            } else {
                return resourceLoader.getString("gen.due");
            }
        } catch (NullPointerException | IdUnusedException | PermissionException e) {
            log.warn("Could not determine the status for assignment: {}, {}", assignmentId, e.getMessage());
        }
        return null;
    }

    @Override
    public String getSubmissionStatus(String submissionId, boolean returnFormattedDate) {
        AssignmentSubmission submission = null;
        SubmissionStatus submissionStatus;
        String submitTime = "";
        boolean canGrade = false;

        try {
            submission = getSubmission(submissionId);

            if (submission != null) {
                Assignment assignment = submission.getAssignment();
                String assignmentReference = AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference();
                canGrade = allowGradeSubmission(assignmentReference);

                if (submission.getDateSubmitted() != null) {
                    submitTime = returnFormattedDate ?
                            userTimeService.dateTimeFormat(submission.getDateSubmitted(), null, null) :
                            submission.getDateSubmitted().toString();
                }
            }
        } catch (PermissionException e) {
            log.debug("Could not get submission with id {}, {}", submissionId, e.toString());
        }

        submissionStatus = getSubmissionCanonicalStatus(submission, canGrade);
        String i18nStatus = getFormattedStatus(submissionStatus, submitTime);

        // if this is a grader and there is no NO_SUBMISSION add on the submitters status for added clarity
        if (canGrade && (SubmissionStatus.NO_SUBMISSION.equals(submissionStatus) || SubmissionStatus.UNGRADED.equals(submissionStatus))) {
            SubmissionStatus submitterStatus = getSubmittersCanonicalSubmissionStatus(submission);
            return i18nStatus + " - " +  getFormattedStatus(submitterStatus, submitTime);
        }

        return i18nStatus;
    }

    private String getFormattedStatus(SubmissionStatus status, String submittedTime) {
        switch (status) {
            case RESUBMITTED:
                return resourceLoader.getString("gen.resub");
            case LATE:
                return resourceLoader.getString("gen.resub") + " " + submittedTime + resourceLoader.getString("gen.late2");
            case SUBMITTED:
                return resourceLoader.getString("gen.subm4") + " " + submittedTime;
            case RETURNED:
                return resourceLoader.getString("gen.returned");
            case UNGRADED:
                return resourceLoader.getString("ungra");
            case NO_SUBMISSION:
                return resourceLoader.getString("listsub.nosub");
            case NOT_STARTED:
                return resourceLoader.getString("gen.notsta");
            case IN_PROGRESS:
                return resourceLoader.getString("gen.inpro");
            case COMMENTED:
                return resourceLoader.getString("gen.commented");
            case GRADED:
                return resourceLoader.getString("grad3");
            case HONOR_ACCEPTED:
                return resourceLoader.getString("gen.hpsta");
            case RESUBMIT_ALLOWED:
                return resourceLoader.getString("gen.pending_resubmit");
            default:
                return "Undefined Status";
        }
    }

    @Override
    public SubmissionStatus getSubmissionCanonicalStatus(AssignmentSubmission submission, boolean canGrade) {
        SubmissionStatus status;
        if (canGrade) {
            status = getGradersCanonicalSubmissionStatus(submission);
        } else {
            status = getSubmittersCanonicalSubmissionStatus(submission);
        }

        log.debug("getSubmissionCanonicalStatus for submission {} : {}", submission, status);
        return status;
    }

    private AssignmentConstants.SubmissionStatus getGradersCanonicalSubmissionStatus(AssignmentSubmission submission) {
        if (submission == null) return SubmissionStatus.NO_SUBMISSION;

        Instant submitTime = submission.getDateSubmitted();
        Instant returnTime = submission.getDateReturned();

        // States matching a person who can grade a submission
        if (submission.getSubmitted()) {
            if (submitTime != null) {
                if (submission.getReturned()) {
                    if (returnTime != null && returnTime.isBefore(submitTime)) {
                        if (!submission.getGraded()) {
                            if (submitTime.isAfter(submission.getAssignment().getDueDate())) {
                                return SubmissionStatus.LATE;
                            } else {
                                return SubmissionStatus.RESUBMITTED;
                            }
                        } else {
                            return canSubmitResubmission(submission, Instant.now()) ? SubmissionStatus.RESUBMIT_ALLOWED : SubmissionStatus.RETURNED;
                        }
                    } else {
                        if (returnTime != null && returnTime.isAfter(submitTime)) {
                            return canSubmitResubmission(submission, Instant.now()) ? SubmissionStatus.RESUBMIT_ALLOWED : SubmissionStatus.RETURNED;
                        } else {
                            return SubmissionStatus.RETURNED;
                        }
                    }
                } else if (submission.getGraded()) {
                    if (StringUtils.isNotBlank(submission.getGrade())) {
                        return SubmissionStatus.GRADED;
                    } else if (doesSubmissionHaveInstructorFeedback(submission)) {
                        return SubmissionStatus.COMMENTED;
                    }
                } else if (doesSubmissionHaveInstructorFeedback(submission)) {
                    return SubmissionStatus.COMMENTED;
                }
            } else {
                if (submission.getReturned()) {
                    return SubmissionStatus.RETURNED;
                } else if (submission.getGraded()) {
                    if (StringUtils.isNotBlank(submission.getGrade())) {
                        return SubmissionStatus.GRADED;
                    } else if (doesSubmissionHaveInstructorFeedback(submission)) {
                        return SubmissionStatus.COMMENTED;
                    }
                } else if (doesSubmissionHaveInstructorFeedback(submission)) {
                    return SubmissionStatus.COMMENTED;
                } else {
                    return SubmissionStatus.NO_SUBMISSION;
                }
            }
        } else {
            if (submission.getGraded()) {
                if (submission.getReturned()) {
                    // not submitted submmission has been graded and returned
                    return SubmissionStatus.RETURNED;
                } else {
                    // grade saved but not release yet, show this to graders
                    if (StringUtils.isNotBlank(submission.getGrade())) {
                        return SubmissionStatus.GRADED;
                    } else if (doesSubmissionHaveInstructorFeedback(submission)) {
                        return SubmissionStatus.COMMENTED;
                    }
                }
            } else if (doesSubmissionHaveInstructorFeedback(submission)) {
                return SubmissionStatus.COMMENTED;
            }
        }
        return SubmissionStatus.UNGRADED;
    }

    public boolean doesSubmissionHaveInstructorFeedback(AssignmentSubmission submission) {
        return StringUtils.isNotBlank(submission.getFeedbackComment()) || CollectionUtils.isNotEmpty(submission.getFeedbackAttachments());
    }

    private AssignmentConstants.SubmissionStatus getSubmittersCanonicalSubmissionStatus(AssignmentSubmission submission) {
        if (submission == null) return SubmissionStatus.NOT_STARTED;

        Instant submitTime = submission.getDateSubmitted();
        Instant returnTime = submission.getDateReturned();
        Instant lastModTime = submission.getDateModified();

        // States matching a person that submits a submission
        if (submission.getSubmitted()) {
            if (submitTime != null) {
                if (submission.getReturned()) {
                    if (returnTime != null && returnTime.isBefore(submitTime)) {
                        if (!submission.getGraded()) {
                            if (submitTime.isAfter(submission.getAssignment().getDueDate())) {
                                return SubmissionStatus.LATE;
                            } else {
                                return SubmissionStatus.RESUBMITTED;
                            }
                        } else {
                            return canSubmitResubmission(submission, Instant.now()) ? SubmissionStatus.RESUBMIT_ALLOWED : SubmissionStatus.RETURNED;
                        }
                    } else {
                        if (returnTime != null && returnTime.isAfter(submitTime)) {
                            return canSubmitResubmission(submission, Instant.now()) ? SubmissionStatus.RESUBMIT_ALLOWED : SubmissionStatus.RETURNED;
                        } else {
                            return SubmissionStatus.RETURNED;
                        }
                    }
                } else {
                    return SubmissionStatus.SUBMITTED;
                }
            } else {
                if (submission.getReturned()) {
                    return SubmissionStatus.RETURNED;
                } else {
                    if (submission.getAssignment().getHonorPledge() && submission.getHonorPledge()) {
                        return SubmissionStatus.HONOR_ACCEPTED;
                    } else {
                        return SubmissionStatus.NOT_STARTED;
                    }
                }
            }
        } else {
            if (submission.getGraded()) {
                if (submission.getReturned()) {
                    // modified time is after returned time + 10 seconds
                    if (lastModTime != null && returnTime != null && lastModTime.isAfter(returnTime.plusSeconds(10))) {
                        // working on a returned submission now
                        return SubmissionStatus.IN_PROGRESS;
                    } else {
                        // not submitted submmission has been graded and returned
                        return SubmissionStatus.RETURNED;
                    }
                } else {
                    // submission saved, not submitted.
                    return SubmissionStatus.IN_PROGRESS;
                }
            } else {
                if (submission.getAssignment().getHonorPledge() && submission.getHonorPledge() && submission.getDateCreated().equals(submission.getDateModified())) {
                    return SubmissionStatus.HONOR_ACCEPTED;
                } else {
                    // submission saved, not submitted,
                    return SubmissionStatus.IN_PROGRESS;
                }
            }
        }
    }

    // TODO this could probably be removed
    @Override
    public List<User> getSortedGroupUsers(Group g) {
        List<User> users = new ArrayList<>();
		g.getMembers().stream()
		.filter(m -> (m.getRole().isAllowed(SECURE_ADD_ASSIGNMENT_SUBMISSION)
				|| g.isAllowed(m.getUserId(), SECURE_ADD_ASSIGNMENT_SUBMISSION))
				&& !m.getRole().isAllowed(SECURE_GRADE_ASSIGNMENT_SUBMISSION)
				&& !g.isAllowed(m.getUserId(), SECURE_GRADE_ASSIGNMENT_SUBMISSION))
		.forEach( member -> {
			try {
				users.add(userDirectoryService.getUser(member.getUserId()));
			} catch (Exception e) {
				log.warn("Creating a list of users, user = {}, {}", member.getUserId(), e.getMessage());
			}
		});
        users.sort(new UserSortNameComparator());
        return users;
    }

    @Override
    public int countSubmissions(String assignmentReference, Boolean graded) {
        String assignmentId = AssignmentReferenceReckoner.reckoner().reference(assignmentReference).reckon().getId();
        try {
            Assignment assignment = getAssignment(assignmentId);

            boolean isNonElectronic = false;
            if (assignment.getTypeOfSubmission() == Assignment.SubmissionType.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION) {
                isNonElectronic = true;
            }
            List<User> allowAddSubmissionUsers = allowAddSubmissionUsers(assignmentReference);
            // SAK-28055 need to take away those users who have the permissions defined in sakai.properties
            String resourceString = AssignmentReferenceReckoner.reckoner().context(assignment.getContext()).reckon().getReference();
            String[] permissions = serverConfigurationService.getStrings("assignment.submitter.remove.permission");
            if (permissions != null) {
                for (String permission : permissions) {
                    allowAddSubmissionUsers.removeAll(securityService.unlockUsers(permission, resourceString));
                }
            } else {
                allowAddSubmissionUsers.removeAll(securityService.unlockUsers(SECURE_ADD_ASSIGNMENT, resourceString));
            }
            List<String> userIds = allowAddSubmissionUsers.stream().map(User::getId).collect(Collectors.toList());
            // if the assignment is non-electronic don't include submission date or is user submission
            return (int) assignmentRepository.countAssignmentSubmissions(assignmentId, graded, !isNonElectronic, !isNonElectronic, userIds);
        } catch (Exception e) {
            log.warn("Couldn't count submissions for assignment reference {}, {}", assignmentReference, e.getMessage());
        }
        return 0;
    }

    @Override
    public byte[] getGradesSpreadsheet(String ref) throws IdUnusedException, PermissionException {
        return new byte[0];
    }

    @Override
    public void getSubmissionsZip(OutputStream out, String reference, String query) throws IdUnusedException, PermissionException {
        boolean withStudentSubmissionText = false;
        boolean withStudentSubmissionAttachment = false;
        boolean withGradeFile = false;
        boolean withFeedbackText = false;
        boolean withFeedbackComment = false;
        boolean withFeedbackAttachment = false;
        boolean withRubrics = false;
        boolean withoutFolders = false;
        boolean includeNotSubmitted = false;
        String gradeFileFormat = "csv";
        String viewString = "";
        String contextString = "";
        String searchString = "";
        String searchFilterOnly = "";

        if (query != null) {
            StringTokenizer queryTokens = new StringTokenizer(query, "&");

            // Parsing the range list
            while (queryTokens.hasMoreTokens()) {
                String token = queryTokens.nextToken().trim();

                // check against the content elements selection
                if (token.contains("studentSubmissionText")) {
                    // should contain student submission text information
                    withStudentSubmissionText = true;
                } else if (token.contains("studentSubmissionAttachment")) {
                    // should contain student submission attachment information
                    withStudentSubmissionAttachment = true;
                } else if (token.contains("gradeFile")) {
                    // should contain grade file
                    withGradeFile = true;
                    if (token.contains("gradeFileFormat=csv")) {
                        gradeFileFormat = "csv";
                    } else if (token.contains("gradeFileFormat=excel")) {
                        gradeFileFormat = "excel";
                    }
                } else if (token.contains("feedbackTexts")) {
                    // inline text
                    withFeedbackText = true;
                } else if (token.contains("feedbackComments")) {
                    // comments  should be available
                    withFeedbackComment = true;
                } else if (token.contains("feedbackAttachments")) {
                    // feedback attachment
                    withFeedbackAttachment = true;
                } else if (token.contains("rubrics")) {
                    withRubrics = true;
                } else if (token.contains("withoutFolders")) {
                    // feedback attachment
                    withoutFolders = true;
                } else if (token.contains("includeNotSubmitted")) {
                    // include empty submissions
                    includeNotSubmitted = true;
                } else if (token.contains("contextString")) {
                    // context
                    contextString = token.contains("=") ? token.substring(token.indexOf("=") + 1) : "";
                    try {
                        // The siteId comes encoded in a URL.
                        // If the siteId contains encoded symbols (like spaces) the siteService may not get the site properly resulting in an invalid ZIP file, it needs to be decoded.
                        contextString = URLDecoder.decode(contextString, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        log.warn("The site {} cannot be decoded {}.", e);
                    }
                } else if (token.contains("viewString")) {
                    // view
                    viewString = token.contains("=") ? token.substring(token.indexOf("=") + 1) : "";
                } else if (token.contains("searchString")) {
                    // search
                    searchString = token.contains("=") ? token.substring(token.indexOf("=") + 1) : "";
                } else if (token.contains("searchFilterOnly")) {
                    // search and group filter only
                    searchFilterOnly = token.contains("=") ? token.substring(token.indexOf("=") + 1) : "";
                }
            }
        }

        byte[] rv = null;

        try {
            String id = AssignmentReferenceReckoner.reckoner().reference(reference).reckon().getId();
            Assignment assignment = getAssignment(id);

            if (assignment.getIsGroup()) {
                Collection<Group> submitterGroups = getSubmitterGroupList(searchFilterOnly,
                        viewString.length() == 0 ? AssignmentConstants.ALL : viewString,
                        searchString,
                        id,
                        contextString == null ? assignment.getContext() : contextString);
                if (submitterGroups != null && !submitterGroups.isEmpty()) {
                    List<AssignmentSubmission> submissions = new ArrayList<>();
                    for (Group g : submitterGroups) {
                        log.debug("ZIP GROUP " + g.getTitle());
                        AssignmentSubmission sub = getSubmission(id, g.getId());
                        log.debug("ZIP GROUP " + g.getTitle() + " SUB " + (sub == null ? "null" : sub.getId()));
                        if (sub != null) {
                            submissions.add(sub);
                        }
                    }
                    StringBuilder exceptionMessage = new StringBuilder();

                    if (allowGradeSubmission(reference)) {
                        zipGroupSubmissions(reference,
                                assignment.getTitle(),
                                assignment.getTypeOfGrade().toString(),
                                assignment.getTypeOfSubmission(),
                                new SortedIterator(submissions.iterator(), new AssignmentSubmissionComparator(applicationContext.getBean(AssignmentService.class), siteService, userDirectoryService)),
                                out,
                                exceptionMessage,
                                withStudentSubmissionText,
                                withStudentSubmissionAttachment,
                                withGradeFile,
                                withFeedbackText,
                                withFeedbackComment,
                                withFeedbackAttachment,
                                withRubrics,
                                gradeFileFormat,
                                includeNotSubmitted);

                        if (exceptionMessage.length() > 0) {
                            // log any error messages
                            log.warn("Encountered an issue while zipping submissions for ref = {}, exception message {}", reference, exceptionMessage);
                        }
                    }
                }
            } else {

                Map<User, AssignmentSubmission> submitters = getSubmitterMap(searchFilterOnly,
                        viewString.length() == 0 ? AssignmentConstants.ALL : viewString,
                        searchString,
                        reference,
                        contextString == null ? assignment.getContext() : contextString);

                if (!submitters.isEmpty()) {
                    List<AssignmentSubmission> submissions = new ArrayList<AssignmentSubmission>(submitters.values());

                    StringBuilder exceptionMessage = new StringBuilder();
                    SortedIterator sortedIterator;
                    if (assignmentUsesAnonymousGrading(assignment)){
                        sortedIterator = new SortedIterator(submissions.iterator(), new AnonymousSubmissionComparator());
                    } else {
                        sortedIterator = new SortedIterator(submissions.iterator(), new AssignmentSubmissionComparator(applicationContext.getBean(AssignmentService.class), siteService, userDirectoryService));
                    }
                    if (allowGradeSubmission(reference)) {
                        zipSubmissions(reference,
                                assignment.getTitle(),
                                assignment.getTypeOfGrade(),
                                assignment.getTypeOfSubmission(),
                                sortedIterator,
                                out,
                                exceptionMessage,
                                withStudentSubmissionText,
                                withStudentSubmissionAttachment,
                                withGradeFile,
                                withFeedbackText,
                                withFeedbackComment,
                                withFeedbackAttachment,
                                withRubrics,
                                withoutFolders,
                                gradeFileFormat,
                                includeNotSubmitted,
                                assignment.getContext());
                        if (exceptionMessage.length() > 0) {
                            log.warn("Encountered and issue while zipping submissions for ref = {}, exception message {}", reference, exceptionMessage);
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.warn("Cannot create submissions zip file for reference = {}", reference, e);
        }
    }

    @Override
    public String assignmentReference(String context, String id) {
        return AssignmentReferenceReckoner.reckoner().context(context).id(id).reckon().getReference();
    }

    @Override
    public String assignmentReference(String id) {
        Assignment assignment = assignmentRepository.findAssignment(id);
        if (assignment != null) {
            return AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference();
        }
        return null;
    }

    @Override
    public String submissionReference(String context, String id, String assignmentId) {
        return AssignmentReferenceReckoner.reckoner().context(context).id(id).container(assignmentId).subtype("s").reckon().getReference();
    }

    /**
     * Perform a check to see of a submission can be resubmitted
     * @param submission the AssignmentSubmission to check
     * @param time the time to use when calculating
     * @return true if submission is not null and the user has resubmissions
     *              and if time bounded the time is before the close date
     */
    private boolean canSubmitResubmission(AssignmentSubmission submission, Instant time) {
        if (submission == null) return false; // false if submission is null

        // check that a submission has been submitted
        if (submission.getDateSubmitted() != null) {
            // get the resubmit settings from submission object first
            String allowResubmitNumString = submission.getProperties().get(AssignmentConstants.ALLOW_RESUBMIT_NUMBER);
            String allowResubmitCloseTimeString = submission.getProperties().get(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME);

            // -1 = unlimited resubmissions
            //  0 = no resubmissions left
            // >0 = number of resubmissions allowed
            int resubmitNumber = NumberUtils.toInt(allowResubmitNumString, 0);
            long resubmitCloseTimeMillis = NumberUtils.toLong(allowResubmitCloseTimeString, 0);

            log.debug("Submission {} has: number of resubmits [{}], resubmit close time [{}]", submission.getId(), resubmitNumber, resubmitCloseTimeMillis);

            if (resubmitCloseTimeMillis > 0) { // if a resubmission close time has been set then it is part of the check
                Instant resubmitCloseTime = Instant.ofEpochMilli(resubmitCloseTimeMillis);
                Assignment assignment = submission.getAssignment();
                Instant assignmentCloseDate = assignment.getCloseDate() != null ? assignment.getCloseDate() : assignment.getDueDate();
                // use whichever time is later the assignment close time or the resubmission close time
                if (assignmentCloseDate != null && resubmitCloseTime.isBefore(assignmentCloseDate)) {
                    // otherwise, use assignment close time as the resubmission close time
                    resubmitCloseTime = assignmentCloseDate;
                }

                if (time == null) time = Instant.now(); // if a time was not supplied use now
                // true if the user has more resubmission attempts and current time is before resubmission close time
                return (resubmitNumber > 0 || resubmitNumber == -1) && time.isBefore(resubmitCloseTime);
            } else { // otherwise it is an open ended assignment and we just check number of attempts left
                // true if the user has more resubmission attempts
                return (resubmitNumber > 0 || resubmitNumber == -1);
            }
        }
        return false;
    }

    @Override
    public boolean canSubmit(Assignment assignment, String userId) {
        if (assignment == null || BooleanUtils.isTrue(assignment.getDeleted())) return false;

        // submissions are never allowed to non-electronic assignments
        if (assignment.getTypeOfSubmission() == Assignment.SubmissionType.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION) {
            return false;
        }

        if (StringUtils.isBlank(userId)) {
            userId = sessionManager.getCurrentSessionUserId();
        }

        try {
            // return false only if the user is not allowed to submit and not allowed to add to the assignment
            if (!permissionCheckWithGroups(SECURE_ADD_ASSIGNMENT_SUBMISSION, assignment, userId) // check asn.submit for user on assignment consulting groups
                    && !permissionCheck(SECURE_ADD_ASSIGNMENT, siteService.siteReference(assignment.getContext()), userId)) return false; // check asn.new for user in site not consulting groups

            // if user the user can access this assignment
            checkAssignmentAccessibleForUser(assignment, userId);

            Instant currentTime = Instant.now();

            // return false if the assignment is draft or is not open yet
            Instant openTime = assignment.getOpenDate();
            if (assignment.getDraft() || openTime.isAfter(currentTime)) {
                return false;
            }

            // whether the current time is after the assignment close date inclusive
            boolean isBeforeAssignmentCloseDate = currentTime.isBefore(assignment.getCloseDate());

            AssignmentSubmission submission = getSubmission(AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getId(), userId);

            if (submission != null) {

                // If an Extension exists for the user, we switch out the assignment's overall
                // close date for the extension deadline
                if (!isBeforeAssignmentCloseDate && StringUtils.isNotBlank(submission.getProperties().get(AssignmentConstants.ALLOW_EXTENSION_CLOSETIME))) {
                    Instant extensionCloseTime = Instant.ofEpochMilli(Long.parseLong(submission.getProperties().get(AssignmentConstants.ALLOW_EXTENSION_CLOSETIME)));
                    isBeforeAssignmentCloseDate = currentTime.isBefore(extensionCloseTime);
                }

                // before the assignment close date
                // and if no date then a submission was never truly submitted by the student
                if (isBeforeAssignmentCloseDate && submission.getDateSubmitted() == null) return true;

                // returns true if resubmission is allowed
                if (canSubmitResubmission(submission, currentTime)) return true;
            } else {
                // there is no submission yet so only check if before assignment close date
                return isBeforeAssignmentCloseDate;
            }
        } catch (PermissionException e) {
            log.warn("The user {} cannot submit to assignment {}, {}", userId, assignment.getId(), e.getMessage());
        }
        return false;
    }

    @Override
    public boolean canSubmit(Assignment assignment) {
        return canSubmit(assignment, sessionManager.getCurrentSessionUserId());
    }

    @Override
    @Transactional
    public Collection<Group> getSubmitterGroupList(String searchFilterOnly, String allOrOneGroup, String searchString, String assignmentId, String contextString) {
        Collection<Group> rv = new ArrayList<Group>();
        allOrOneGroup = StringUtils.trimToNull(allOrOneGroup);
        try {
            Assignment a = getAssignment(assignmentId);
            String assignmentReference = AssignmentReferenceReckoner.reckoner().assignment(a).reckon().getReference();

            if (a != null) {
                Site st = siteService.getSite(contextString);
                if (StringUtils.equals(allOrOneGroup, AssignmentConstants.ALL) || StringUtils.isEmpty(allOrOneGroup)) {
                    if (a.getTypeOfAccess().equals(SITE)) {
                        rv.addAll(st.getGroups());
                    } else {
                        rv.addAll(getGroupsAllowGradeAssignment(assignmentReference));
                    }
                } else {
                    Group group = st.getGroup(allOrOneGroup);
                    if (group != null) {// && _gg.getProperties().get(GROUP_SECTION_PROPERTY) == null) {
                        rv.add(group);
                    }
                }

                for (Group g : rv) {
                    AssignmentSubmission uSubmission = getSubmission(assignmentId, g.getId());
                    if (uSubmission == null) {
                        if (allowGradeSubmission(assignmentReference)) {
                            if (a.getIsGroup()) {
                                // temporarily allow the user to read and write from assignments (asn.revise permission)
                                SecurityAdvisor securityAdvisor = new MySecurityAdvisor(
                                        sessionManager.getCurrentSessionUserId(),
                                        new ArrayList<>(Arrays.asList(SECURE_ADD_ASSIGNMENT_SUBMISSION, SECURE_UPDATE_ASSIGNMENT_SUBMISSION)),
                                        ""/* no submission id yet, pass the empty string to advisor*/);
                                try {
                                    securityService.pushAdvisor(securityAdvisor);
                                    log.debug("context {} for assignment {} for group {}", contextString, a.getId(), g.getId());
                                    AssignmentSubmission s = addSubmission(a.getId(), g.getId());
                                    s.setSubmitted(true);
                                    s.setUserSubmission(false);

                                    // set the resubmission properties
                                    // get the assignment setting for resubmitting
                                    Map<String, String> assignmentProperties = a.getProperties();
                                    String assignmentAllowResubmitNumber = assignmentProperties.get(AssignmentConstants.ALLOW_RESUBMIT_NUMBER);
                                    if (assignmentAllowResubmitNumber != null) {
                                        s.getProperties().put(AssignmentConstants.ALLOW_RESUBMIT_NUMBER, assignmentAllowResubmitNumber);

                                        String assignmentAllowResubmitCloseDate = assignmentProperties.get(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME);
                                        // if assignment's setting of resubmit close time is null, use assignment close time as the close time for resubmit
                                        s.getProperties().put(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME,
                                                assignmentAllowResubmitCloseDate != null ? assignmentAllowResubmitCloseDate : String.valueOf(a.getCloseDate().toEpochMilli()));
                                    }

                                    assignmentRepository.updateSubmission(s);
                                    // clear the permission
                                } catch (Exception e) {
                                    log.warn("exception thrown while creating empty submission for group who has not submitted, {}", e.getMessage());
                                } finally {
                                    securityService.popAdvisor(securityAdvisor);
                                }
                            }
                        }
                    }
                }
            }
        } catch (IdUnusedException aIdException) {
            log.warn("Assignment id not used: {}, {}", assignmentId, aIdException.getMessage());
        } catch (PermissionException aPerException) {
            log.warn("Not allowed to get assignment {}, {}", assignmentId, aPerException.getMessage());
        }

        return rv;
    }

    @Override
    public boolean getAllowSubmitByInstructor() {
        return allowSubmitByInstructor;
    }

    @Override
    @Transactional
    public List<String> getSubmitterIdList(String searchFilterOnly, String allOrOneGroup, String searchString, String aRef, String contextString) {
        List<String> rv = new ArrayList<>();
        Map<User, AssignmentSubmission> submitterMap = getSubmitterMap(searchFilterOnly, allOrOneGroup, searchString, aRef, contextString);
        for (User u : submitterMap.keySet()) {
            rv.add(u.getId());
        }

        return rv;
    }

    @Override
    @Transactional
    public Map<User, AssignmentSubmission> getSubmitterMap(String searchFilterOnly, String allOrOneGroup, String searchString, String aRef, String contextString) {
        Map<User, AssignmentSubmission> submitterMap = new HashMap<>();

        Optional<Assignment> assignment = Optional.empty();
        if (StringUtils.isNotBlank(aRef)) {
            String id = AssignmentReferenceReckoner.reckoner().reference(aRef).reckon().getId();
            try {
                assignment = Optional.ofNullable(getAssignment(id));
            } catch (Exception e) {
                log.warn("Assignment could not be found with id: {}, {}", id, e.getMessage());
            }
        }

        if (assignment.isPresent() ) {
            final Assignment a = assignment.get();
            final String assignmentId = a.getId();
            final List<User> users;
            allOrOneGroup = StringUtils.trimToNull(allOrOneGroup);
            searchString = StringUtils.trimToNull(searchString);
            boolean bSearchFilterOnly = "true".equalsIgnoreCase(searchFilterOnly);

            if (assignmentUsesAnonymousGrading(a)) {
                bSearchFilterOnly = false;
                searchString = "";
            }

            if (bSearchFilterOnly) {
                if (allOrOneGroup == null && searchString == null) {
                    // if the option is set to "Only show user submissions according to Group Filter and Search result"
                    // if no group filter and no search string is specified, no user will be shown first by default;
                    return submitterMap;
                } else {
                    List<User> allowAddSubmissionUsers = allowAddSubmissionUsers(aRef);
                    if (allOrOneGroup == null) {
                        // search is done for all submitters
                        users = getSearchedUsers(searchString, allowAddSubmissionUsers, false);
                    } else if (searchString != null) {
                        // group filter first
                        List<User> selectedGroupUsers = getSelectedGroupUsers(allOrOneGroup, contextString, a, allowAddSubmissionUsers);
                        users = getSearchedUsers(searchString, selectedGroupUsers, true);
                    } else {
                        users = getSelectedGroupUsers(allOrOneGroup, contextString, a, allowAddSubmissionUsers);
                    }
                }
            } else {
                List<User> allowAddSubmissionUsers = allowAddSubmissionUsers(aRef);

                // SAK-28055 need to take away those users who have the permissions defined in sakai.properties
                String resourceString = AssignmentReferenceReckoner.reckoner().context(a.getContext()).reckon().getReference();
                String[] permissions = serverConfigurationService.getStrings("assignment.submitter.remove.permission");
                if (permissions != null) {
                    for (String permission : permissions) {
                        allowAddSubmissionUsers.removeAll(securityService.unlockUsers(permission, resourceString));
                    }
                } else {
                    allowAddSubmissionUsers.removeAll(securityService.unlockUsers(SECURE_ADD_ASSIGNMENT, resourceString));
                }

                // Step 1: get group if any that is selected
                List<User> selectedGroupUsers = getSelectedGroupUsers(allOrOneGroup, contextString, a, allowAddSubmissionUsers);

                // Step 2: get all student that meets the search criteria based on previous group users. If search is null or empty string, return all users.
                users = getSearchedUsers(searchString, selectedGroupUsers, true);
            }

            if (!users.isEmpty()) {
                List<String> userids = users.stream().filter(Objects::nonNull).map(User::getId).collect(Collectors.toList());

                List<AssignmentSubmission> submissions = assignmentRepository.findSubmissionForUsers(assignmentId, userids);

                for (final AssignmentSubmission submission : submissions) {
                    submission.getSubmitters().forEach(submitter -> {
                        users.stream()
                                .filter(Objects::nonNull)
                                .filter(u -> u.getId().equals(submitter.getSubmitter()))
                                .findAny()
                                .ifPresent(u -> submitterMap.put(u, submission));
                    });
                }

                List<User> usersWithNoSubmission = new ArrayList<>(users);
                usersWithNoSubmission.removeAll(submitterMap.keySet());

                for (final User user : usersWithNoSubmission) {
                    String submitterId = getSubmitterIdForAssignment(a, user.getId());
                    if (StringUtils.isNotBlank(submitterId)) {
                        try {
                            AssignmentSubmission submission = addSubmission(assignmentId, submitterId);
                            if (submission != null) {
                                // Note: If we had s.setSubmitted(false);, this would put it in 'draft mode'
                                submission.setSubmitted(true);
                                /*
                                 * Since setSubmitted represents whether the submission is in draft mode state, we need another property. So we created isUserSubmission.
                                 * This represents whether the submission was generated by a user.
                                 * We set it to false because these submissions are generated so that the instructor has something to grade;
                                 * the user did not in fact submit anything.
                                 */
                                submission.setUserSubmission(false);

                                // set the resubmission properties
                                // get the assignment setting for resubmitting
                                Map<String, String> assignmentProperties = a.getProperties();
                                String assignmentAllowResubmitNumber = assignmentProperties.get(AssignmentConstants.ALLOW_RESUBMIT_NUMBER);
                                if (StringUtils.isNotBlank(assignmentAllowResubmitNumber)) {
                                    Map<String, String> submissionProperties = submission.getProperties();
                                    submissionProperties.put(AssignmentConstants.ALLOW_RESUBMIT_NUMBER, assignmentAllowResubmitNumber);

                                    String assignmentAllowResubmitCloseDate = assignmentProperties.get(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME);
                                    // if assignment's setting of resubmit close time is null, use assignment close time as the close time for resubmit
                                    submissionProperties.put(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME,
                                            StringUtils.isNotBlank(assignmentAllowResubmitCloseDate)
                                                    ? assignmentAllowResubmitCloseDate
                                                    : String.valueOf(a.getCloseDate().toEpochMilli()));
                                }
                                assignmentRepository.updateSubmission(submission);
                                submitterMap.put(user, submission);
                            } else {
                                log.warn("No submission was found/created for user {} in assignment {}, this should never happen", user.getId(), a.getId());
                            }
                        } catch (PermissionException pe) {
                            log.debug("A new submission could not be added because the user lacks a permission, {}", pe.getMessage());
                        } catch (Exception e) {
                            log.warn("Exception thrown while creating empty submission for student who has not submitted, {}", e.getMessage(), e);
                        }
                    }
                }
            }
        }
        return submitterMap;
    }

    @Override
    public String getSubmitterIdForAssignment(Assignment assignment, String userId) {
        String submitter = null;
        if (userId != null) {
            switch (assignment.getTypeOfAccess()) {
                case SITE:
                    // access is for the entire site and submitter is a user
                    submitter = userId;
                    break;
                case GROUP:
                    // access is restricted to groups
                    Site site;
                    try {
                        site = siteService.getSite(assignment.getContext());
                        //if it is an actual group id we just return it
                        if (site.getGroup(userId) != null) {
                            return userId;
                        }
                        Set<String> assignmentGroups = assignment.getGroups();
                        Collection<Group> userGroups = site.getGroupsWithMember(userId);
                        Set<String> groupIdsMatchingAssignmentForUser = userGroups.stream().filter(g -> assignmentGroups.contains(g.getReference())).map(Group::getId).collect(Collectors.toSet());

                        if (groupIdsMatchingAssignmentForUser.size() < 1) {
                            log.debug("User {} is not a member of any groups for this assignment {}", userId, assignment.getId());
                        } else if (groupIdsMatchingAssignmentForUser.size() == 1) {
                            if (assignment.getIsGroup()) {
                                submitter = groupIdsMatchingAssignmentForUser.toArray(new String[] {})[0];
                            } else {
                                submitter = userId;
                            }
                        } else if (groupIdsMatchingAssignmentForUser.size() > 1 && !assignment.getIsGroup()) {
                            submitter = userId;
                        } else {
                            log.warn("User {} is on more than one group for this assignment {}, please remove the user from a group so that they are only a member of a single group", userId, assignment.getId());
                        }
                    } catch (IdUnusedException iue) {
                        log.warn("Could not get the site {} for assignment {} while determining the submitter of the submission", assignment.getContext(), assignment.getId());
                    }
                    break;
                default:
                    log.warn("Can't determine the type of submission to create for user {} in assignment {}", userId, assignment.getId());
                    break;
            }
        }
        return submitter;
    }

    public List<User> getSelectedGroupUsers(String allOrOneGroup, String contextString, Assignment assignment, List<User> allowAddSubmissionUsers) {
        Collection<String> authzRefs = new ArrayList<>();

        List<User> selectedGroupUsers = new ArrayList<>();
        if (StringUtils.isNotBlank(allOrOneGroup)) {
            // now are we view all sections/groups or just specific one?
            if (allOrOneGroup.equals(AssignmentConstants.ALL)) {
                if (allowAllGroups(contextString)) {
                    // site range
                    try {
                        Site site = siteService.getSite(contextString);
                        authzRefs.add(site.getReference());
                    } catch (IdUnusedException e) {
                        log.warn("Cannot find site {} {}", contextString, e.getMessage());
                    }
                } else {
                    // get all those groups that user is allowed to grade
                    Collection<Group> groups = getGroupsAllowGradeAssignment(AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference());
                    groups.forEach(g -> authzRefs.add(g.getReference()));
                }
            } else {
                // filter out only those submissions from the selected-group members
                try {
                    Group group = siteService.getSite(contextString).getGroup(allOrOneGroup);
                    authzRefs.add(group.getReference());
                } catch (IdUnusedException e) {
                    log.warn("Cannot add groupId = {}, {}", allOrOneGroup, e.getMessage());
                }
            }

            for (String ref : authzRefs) {
                try {
                    AuthzGroup group = authzGroupService.getAuthzGroup(ref);
                    for (String userId : group.getUsers()) {
                        // don't show user multiple times
                        try {
                            User u = userDirectoryService.getUser(userId);
                            if (u != null && allowAddSubmissionUsers.contains(u)) {
                                if (!selectedGroupUsers.contains(u)) {
                                    selectedGroupUsers.add(u);
                                }
                            }
                        } catch (UserNotDefinedException uException) {
                            log.warn("User not found with id = {}, {}", userId, uException.getMessage());
                        }
                    }
                } catch (GroupNotDefinedException gException) {
                    log.warn("Group not found with reference = {}, {}", ref, gException.getMessage());
                }
            }
        }
        return selectedGroupUsers;
    }

    private List<User> getSearchedUsers(String searchString, List<User> userList, boolean retain) {
        List<User> rv = new ArrayList<>();
        if (StringUtils.isNotBlank(searchString)) {
            String normalizedSearchString = normalizeStringForSearch(searchString);
            for (User u : userList) {
                // search on user sortname, eid, email
                String[] fields = {u.getSortName(), u.getEid(), u.getEmail()};
                List<String> l = Arrays.asList(fields);
                for (String s : l) {
                    if (s != null && normalizeStringForSearch(s).contains(normalizedSearchString)) {
                        rv.add(u);
                        break;
                    }
                }
            }
        } else if (retain) {
            // retain the original list
            rv = userList;
        }
        return rv;
    }

    /**
     * Normalizes a string for case and accent-insensitive search
     * @param input the string to normalize
     * @return the normalized string without accents and in lowercase
     */
    private String normalizeStringForSearch(String input) {
        if (input == null) return null;
        String decomposed = Normalizer.normalize(input, Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase(Locale.ROOT);
    }

    @Override
    public String escapeInvalidCharsEntry(String accentedString) {
        String decomposed = Normalizer.normalize(accentedString, Normalizer.Form.NFD);
        decomposed = decomposed.replaceAll("\\p{InCombiningDiacriticalMarks}+", StringUtils.EMPTY);
        decomposed = decomposed.replaceAll("\\?", StringUtils.EMPTY);
        // To avoid issues, dash variations will be replaced by a regular dash.
        decomposed = decomposed.replaceAll("\\p{Pd}", "-");
        // Remove any non-ascii characters to avoid errors like 'cannot be encoded as it is outside the permitted range of 0 to 255'
        decomposed = decomposed.replaceAll("[^\\p{ASCII}]", StringUtils.EMPTY);
        return decomposed;
    }

    @Override
    public boolean assignmentUsesAnonymousGrading(Assignment assignment) {
        if (assignment != null) {
            return Boolean.parseBoolean(assignment.getProperties().get(AssignmentServiceConstants.NEW_ASSIGNMENT_CHECK_ANONYMOUS_GRADING));
        }
        return false;
    }

    @Override
    public Integer getScaleFactor() {
        Integer decimals = serverConfigurationService.getInt("assignment.grading.decimals", AssignmentConstants.DEFAULT_DECIMAL_POINT);
        return Double.valueOf(Math.pow(10.0, decimals)).intValue();
    }

    @Override
    public String getDeepLinkWithPermissions(String context, String assignmentId, boolean allowReadAssignment, boolean allowAddAssignment, boolean allowSubmitAssignment, boolean allowGradeAssignment) throws Exception {
        Assignment a = getAssignment(assignmentId);

        String assignmentContext = a.getContext(); // assignment context
        if (allowReadAssignment && a.getOpenDate().isBefore(ZonedDateTime.now().toInstant())) {
            // this checks if we want to display an assignment link
            try {
                Site site = siteService.getSite(assignmentContext);
                // site id
                ToolConfiguration fromTool = site.getToolForCommonId("sakai.assignment.grades");
                // Three different urls to be rendered depending on the
                // user's permission
                if (allowGradeAssignment) {
                    return serverConfigurationService.getPortalUrl()
                            + "/directtool/"
                            + fromTool.getId()
                            + "?assignmentId="
                            + AssignmentReferenceReckoner.reckoner().context(context).id(assignmentId).reckon().getReference()
                            + "&panel=Main&sakai_action=doGrade_assignment";
                } else if (allowAddAssignment) {
                    return serverConfigurationService.getPortalUrl()
                            + "/directtool/"
                            + fromTool.getId()
                            + "?assignmentId="
                            + AssignmentReferenceReckoner.reckoner().context(context).id(assignmentId).reckon().getReference()
                            + "&panel=Main&sakai_action=doView_assignment";
                } else if (allowSubmitAssignment) {
                    String sakaiAction = "doView_submission";
                    if(a.getHonorPledge()) {
                        sakaiAction = "doView_assignment_honorPledge";
                    }
                    return serverConfigurationService.getPortalUrl()
                            + "/directtool/"
                            + fromTool.getId()
                            + "?assignmentId=" + assignmentId + "&assignmentReference="
                            + AssignmentReferenceReckoner.reckoner().context(context).id(assignmentId).reckon().getReference()
                            + "&panel=Main&sakai_action=" + sakaiAction;
                } else {
                    // user can read the assignment, but not submit, so
                    // render the appropriate url
                    return serverConfigurationService.getPortalUrl()
                            + "/directtool/"
                            + fromTool.getId()
                            + "?assignmentId=" + assignmentId + "&assignmentReference="
                            + AssignmentReferenceReckoner.reckoner().context(context).id(assignmentId).reckon().getReference()
                            + "&panel=Main&sakai_action=doView_assignment_as_student";
                }
            } catch (IdUnusedException e) {
                // No site found
                throw new IdUnusedException("No site found while creating assignment url");
            }
        }
        return "";
    }

    @Override
    public String getDeepLink(String context, String assignmentId, String userId) throws Exception {
        String resourceString = AssignmentReferenceReckoner.reckoner().context(context).reckon().getReference();
        boolean allowReadAssignment = permissionCheck(SECURE_ACCESS_ASSIGNMENT, resourceString, userId);
        boolean allowAddAssignment = permissionCheck(SECURE_ADD_ASSIGNMENT, resourceString, userId) || (!getGroupsAllowFunction(SECURE_ADD_ASSIGNMENT, context, userId).isEmpty());
        boolean allowSubmitAssignment = permissionCheck(SECURE_ADD_ASSIGNMENT_SUBMISSION, resourceString, userId);
        boolean allowGradeAssignment = permissionCheck(SECURE_GRADE_ASSIGNMENT_SUBMISSION, resourceString, userId);

        return getDeepLinkWithPermissions(context, assignmentId, allowReadAssignment, allowAddAssignment, allowSubmitAssignment, allowGradeAssignment);
    }

    @Override
    public String getCsvSeparator() {
        String defaultSeparator = ",";
        //If the decimal separator is a comma
        if (",".equals(formattedText.getDecimalSeparator())) {
            defaultSeparator = ";";
        }

        return serverConfigurationService.getString("csv.separator", defaultSeparator);
    }

    @Override
    public String getXmlAssignment(Assignment assignment) {
        return assignmentRepository.toXML(assignment);
    }

    @Override
    public String getGradeForSubmitter(String submissionId, String submitter) {
        if (StringUtils.isAnyBlank(submissionId, submitter)) return null;
        return getGradeForSubmitter(assignmentRepository.findSubmission(submissionId), submitter);
    }

    @Override
    public String getGradeForSubmitter(AssignmentSubmission submission, String submitter) {
        if (submission == null || StringUtils.isBlank(submitter)) return null;

        String grade = submission.getGrade(); // start with submission grade
        Assignment assignment = submission.getAssignment();

        if (assignment.getIsGroup()) {
            Optional<AssignmentSubmissionSubmitter> submissionSubmitter = submission.getSubmitters().stream().filter(s -> s.getSubmitter().equals(submitter)).findAny();
            if (submissionSubmitter.isPresent()) {
                grade = StringUtils.defaultIfBlank(submissionSubmitter.get().getGrade(), grade); // if there is a grade override use that
            }
        }

        Integer scale = assignment.getScaleFactor() != null ? assignment.getScaleFactor() : getScaleFactor();
        return getGradeDisplay(grade, assignment.getTypeOfGrade(), scale);
    }

    public boolean isGradeOverridden(AssignmentSubmission submission, String submitter) {

        Assignment assignment = submission.getAssignment();

        if (!assignment.getIsGroup()) {
            return false;
        }

        Optional<AssignmentSubmissionSubmitter> submissionSubmitter
            = submission.getSubmitters().stream()
                .filter(s -> s.getSubmitter().equals(submitter)).findAny();

        return submissionSubmitter.isPresent() && StringUtils.isNotBlank(submissionSubmitter.get().getGrade());
    }

    /**
     * Contains logic to consistently output a String based version of a grade
     * Interprets the grade using the scale for display
     *
     * @param grade
     * @param typeOfGrade
     * @param scaleFactor
     * @return
     */
    @Override
    public String getGradeDisplay(String grade, Assignment.GradeType typeOfGrade, Integer scaleFactor) {
        String returnGrade = StringUtils.trimToEmpty(grade);
        if (scaleFactor == null) scaleFactor = getScaleFactor();

        switch (typeOfGrade) {
            case SCORE_GRADE_TYPE:
                if (!returnGrade.isEmpty() && !"0".equals(returnGrade)) {
                    int dec = new Double(Math.log10(scaleFactor)).intValue();
                    String decSeparator = formattedText.getDecimalSeparator();
                    String decimalGradePoint = returnGrade;
                    try {
                        Integer.parseInt(returnGrade);
                        // if point grade, display the grade with factor decimal place
                        if (returnGrade.length() > dec) {
                            decimalGradePoint = returnGrade.substring(0, returnGrade.length() - dec) + decSeparator + returnGrade.substring(returnGrade.length() - dec);
                        } else {
                            String newGrade = "0".concat(decSeparator);
                            for (int i = returnGrade.length(); i < dec; i++) {
                                newGrade = newGrade.concat("0");
                            }
                            decimalGradePoint = newGrade.concat(returnGrade);
                        }
                    } catch (NumberFormatException nfe1) {
                        log.debug("Could not parse grade [{}] as an Integer trying as a Float, {}", returnGrade, nfe1.getMessage());
                        try {
                            Float.parseFloat(returnGrade);
                            decimalGradePoint = returnGrade;
                        } catch (NumberFormatException nfe2) {
                            log.debug("Could not parse grade [{}] as a Float, {}", returnGrade, nfe2.getMessage());
                        }
                    }
                    // get localized number format
                    NumberFormat numberFormat = formattedText.getNumberFormat(dec, dec, false);
                    DecimalFormat decimalFormat = (DecimalFormat) numberFormat;
                    // show grade in localized number format
                    Double aDouble = 0D;
                    try {
                        aDouble = decimalFormat.parse(decimalGradePoint).doubleValue();
                    } catch (Exception e) {
                        log.warn("Parsing the grade [{}] as a SCORE_TYPE failed, {}, returning grade as a 0", returnGrade, e.toString());
                    }
                    decimalGradePoint = numberFormat.format(aDouble);
                    returnGrade = decimalGradePoint;
                }
                break;
            case UNGRADED_GRADE_TYPE:
                if (returnGrade.equalsIgnoreCase("gen.nograd")) {
                    returnGrade = resourceLoader.getString("gen.nograd");
                }
                break;
            case PASS_FAIL_GRADE_TYPE:
                if (returnGrade.equalsIgnoreCase("Pass")) {
                    returnGrade = resourceLoader.getString("pass");
                } else if (returnGrade.equalsIgnoreCase("Fail")) {
                    returnGrade = resourceLoader.getString("fail");
                } else {
                    returnGrade = "";
                }
                break;
            case CHECK_GRADE_TYPE:
                if (returnGrade.equalsIgnoreCase("Checked")) {
                    returnGrade = resourceLoader.getString("gen.checked");
                } else {
                    returnGrade = "";
                }
                break;
            default:
                if (returnGrade.isEmpty()) {
                    returnGrade = "";
                }
        }
        return returnGrade;
    }

    @Override
    public String getMaxPointGradeDisplay(int factor, int maxGradePoint) {
        // formated to show factor decimal places, for example, 1000 to 100.0
        // get localized number format
        //        NumberFormat nbFormat = FormattedText.getNumberFormat((int)Math.log10(factor),(int)Math.log10(factor),false);
        // show grade in localized number format
        //        Double dblGrade = new Double(maxGradePoint/(double)factor);
        //        return nbFormat.format(dblGrade);
        return getGradeDisplay(Integer.toString(maxGradePoint), Assignment.GradeType.SCORE_GRADE_TYPE, factor);
    }

    @Override
    public Optional<AssignmentSubmissionSubmitter> getSubmissionSubmittee(AssignmentSubmission submission) {
        return Optional.ofNullable(submission)
                .map(AssignmentSubmission::getSubmitters)
                .flatMap(submitters -> submitters.stream()
                        .filter(AssignmentSubmissionSubmitter::getSubmittee)
                        .findFirst()
                        .or(() -> submitters.stream().findFirst())
                );
    }

    @Override
    public Collection<User> getSubmissionSubmittersAsUsers(AssignmentSubmission submission) {
        if (submission == null) return Collections.emptyList();
        return submission.getSubmitters().stream()
                .map(AssignmentSubmissionSubmitter::getSubmitter)
                .filter(StringUtils::isNotBlank)
                .map(u -> {
                    User user = null;
                    try {
                        user = userDirectoryService.getUser(u);
                    } catch (UserNotDefinedException e) {
                        log.warn("Could not find user with id: {}", u);
                    }
                    return user;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isPeerAssessmentOpen(Assignment assignment) {
        if (assignment.getAllowPeerAssessment()) {
            Instant now = Instant.now();
            return now.isBefore(assignment.getPeerAssessmentPeriodDate()) && now.isAfter(assignment.getCloseDate());
        }
        return false;
    }

    @Override
    public boolean isPeerAssessmentPending(Assignment assignment) {
        if (assignment.getAllowPeerAssessment()) {
            return Instant.now().isBefore(assignment.getCloseDate());
        }
        return false;
    }

    @Override
    public boolean isPeerAssessmentClosed(Assignment assignment) {
        if (assignment.getAllowPeerAssessment()) {
            return Instant.now().isAfter(assignment.getPeerAssessmentPeriodDate());
        }
        return false;
    }

    private Collection<Group> getGroupsAllowFunction(String function, String context, String userId) {
        Collection<Group> rv = new HashSet<>();

        try {
            Site site = siteService.getSite(context);

            if (StringUtils.isBlank(userId)) {
                userId = sessionManager.getCurrentSessionUserId();
            }

            Collection<Group> groups = site.getGroups();
            // if the user has SECURE_ALL_GROUPS in the context (site), select all site groups
            if (securityService.unlock(userId, SECURE_ALL_GROUPS, siteService.siteReference(context))
                    && permissionCheck(function, siteService.siteReference(context), userId)) {
                rv.addAll(groups);
            } else {
                // get a list of the group refs, which are authzGroup ids
                Set<String> groupRefs = groups.stream().map(Group::getReference).collect(Collectors.toSet());

                // ask the authzGroup service to filter them down based on function
                Set<String> allowedGroupRefs = authzGroupService.getAuthzGroupsIsAllowed(userId, function, groupRefs);

                // pick the Group objects from the site's groups to return, those that are in the allowedGroupRefs list
                rv = groups.stream().filter(g -> allowedGroupRefs.contains(g.getReference())).collect(Collectors.toSet());
            }
        } catch (IdUnusedException e) {
            log.debug("site {} not found, {}", context, e.getMessage());
        }

        return rv;
    }

    private Assignment checkAssignmentAccessibleForUser(Assignment assignment, String currentUserId) throws PermissionException {

        if (assignment.getTypeOfAccess() == GROUP) {
            String context = assignment.getContext();
            Collection<String> asgGroups = assignment.getGroups();
            Collection<Group> allowedGroups = getGroupsAllowFunction(SECURE_ACCESS_ASSIGNMENT, context, currentUserId);
            // reject and throw PermissionException if there is no intersection
            if (!allowAllGroups(context)
                    && !StringUtils.equals(assignment.getAuthor(), currentUserId)
                    && !CollectionUtils.containsAny(asgGroups, allowedGroups.stream().map(Group::getReference).collect(Collectors.toSet()))) {
                throw new PermissionException(currentUserId, SECURE_ACCESS_ASSIGNMENT, assignment.getId());
            }
        }

        if (allowAddAssignment(assignment.getContext())) {
            // always return for users that can add assignment in the context
            return assignment;
        } else if (isAvailableOrSubmitted(assignment, currentUserId)) {
            return assignment;
        }
        throw new PermissionException(currentUserId, SECURE_ACCESS_ASSIGNMENT, assignment.getId());
    }

    private boolean isAvailableOrSubmitted(Assignment assignment, String userId) {
        if (!assignment.getDeleted()) {
            // show not deleted, not draft, opened assignments
            // TODO do we need to use time zone
            Instant openTime = assignment.getOpenDate();
            Instant visibleTime = assignment.getVisibleDate();
            if ((openTime != null && Instant.now().isAfter(openTime))
                    || (visibleTime != null && Instant.now().isAfter(visibleTime))
                    && !assignment.getDraft()) {
                return true;
            }
        } else {
            try {
                if (assignment.getDeleted() &&
                        assignment.getTypeOfSubmission() != Assignment.SubmissionType.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION &&
                        getSubmission(assignment.getId(), userId) != null) {
                    // and those deleted but not non-electronic assignments but the user has made submissions to them
                    return true;
                }
            } catch (PermissionException e) {
                log.warn("User doesn't have permission to access submission, {}", e.getMessage());
            }
        }
        return false;
    }

    private boolean isDraftAssignmentVisible(Assignment assignment) {
        return StringUtils.equals(assignment.getAuthor(), userDirectoryService.getCurrentUser().getId()) // the author can see it
                || permissionCheck(SECURE_SHARE_DRAFTS, siteService.siteReference(assignment.getContext()), null); // any role user with share draft permission
    }

    public boolean permissionCheck(String permission, String resource, String user) {
        boolean access = false;
        if (!StringUtils.isAnyBlank(resource, permission)) {
            if (StringUtils.isBlank(user)) {
                access = securityService.unlock(permission, resource);
            } else {
                access = securityService.unlock(user, permission, resource);
            }
        }
        log.debug("checking permission [{}] in context [{}] for user [{}]: {}", permission, resource, user, access);
        return access;
    }

    public boolean permissionCheckInGroups(String permission, Assignment assignment, String user) {
        return this.permissionCheckWithGroups(permission, assignment, user);
    }

    private boolean permissionCheckWithGroups(final String permission, final Assignment assignment, final String user) {
        if (StringUtils.isBlank(permission) || assignment == null) return false;
        String siteReference = siteService.siteReference(assignment.getContext());

        boolean access = false;
        if (GROUP == assignment.getTypeOfAccess()) {
            // assignment access is group
            for (String groupId : assignment.getGroups()) {
                if (StringUtils.isBlank(user)) { // check permission for current user
                    if (securityService.unlock(permission, groupId)) { // check permission for group
                        access = true;
                        break;
                    }
                } else { // check permission for the specified user
                    if (securityService.unlock(user, permission, groupId)) { // check permission for group
                        access = true;
                        break;
                    }
                }
            }
            // lastly if the user has permission asn.all.groups and has permission for the site
            if (!access) {
                if (StringUtils.isBlank(user)) { // check permission for current user
                    if (securityService.unlock(permission, siteReference) // check permission for user in site
                            && securityService.unlock(SECURE_ALL_GROUPS, siteReference)) { // check asn.all.groups for user in site
                        access = true;
                    }
                } else { // check permission for the specified user
                    if (securityService.unlock(user, permission, siteReference) // check permission for user in site
                            && securityService.unlock(user, SECURE_ALL_GROUPS, siteReference)) { // check asn.all.groups for user in site
                        access = true;
                    }
                }
            }
        } else {
            // assignment access is non group or site
            access = permissionCheck(permission, siteReference, user);
        }
        log.debug("checking permission with groups [{}] in context [{}] for user [{}]: {}", permission, siteReference, user, access);
        return access;
    }

    // /////////////////////////////////////////////////////////////
    // TODO
    // cleaning up the following entries in other tools should
    // probably happen as the result of posting an event in the
    // respective tools service and not be chained here
    // only if a rollback were needed would we want to include here
    // /////////////////////////////////////////////////////////////
    private void removeAssociatedTaggingItem(Assignment assignment) {
        try {
            if (taggingManager.isTaggable()) {
                for (TaggingProvider provider : taggingManager.getProviders()) {
                    provider.removeTags(assignmentActivityProducer.getActivity(assignment));
                }
            }
        } catch (PermissionException pe) {
            log.warn("removeAssociatedTaggingItem: User does not have permission to remove tags for assignment: " + assignment.getId() + " via transferCopyEntities");
        }
    }

    // for regular task deletion this has no effect as it has already been done on doDelete_assignment, it only works when it comes from import content + replace
    private void removeAssociatedGradebookItem(Assignment assignment) {
        String associatedGradebookAssignment = assignment.getProperties().get(AssignmentConstants.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);
        if (StringUtils.isNotBlank(associatedGradebookAssignment)) {
            gradingService.removeExternalAssignment(null, associatedGradebookAssignment, getToolId());
        }
    }

    private Calendar getCalendar(String context) {
        Calendar calendar = null;

        String calendarId = serverConfigurationService.getString("calendar", null);
        if (calendarId == null) {
            calendarId = calendarService.calendarReference(context, siteService.MAIN_CONTAINER);
            try {
                calendar = calendarService.getCalendar(calendarId);
            } catch (IdUnusedException e) {
                log.warn("No calendar found for site: " + context);
                calendar = null;
            } catch (PermissionException e) {
                log.error("The current user does not have permission to access the calendar for context: {}", context, e);
            } catch (Exception ex) {
                log.error("Unknown exception occurred retrieving calendar for site: {}", context, ex);
                calendar = null;
            }
        }

        return calendar;
    }

    private void removeAssociatedCalendarItem(Calendar calendar, Assignment assignment) {
        Map<String, String> properties = assignment.getProperties();
        String isThereEvent = properties.get(AssignmentConstants.NEW_ASSIGNMENT_DUE_DATE_SCHEDULED);
        if (isThereEvent != null && isThereEvent.equals(Boolean.TRUE.toString())) {
            // remove the associated calendar event
            if (calendar != null) {
                // already has calendar object
                // get the old event
                CalendarEvent event = null;
                String oldEventId = properties.get(ResourceProperties.PROP_ASSIGNMENT_DUEDATE_CALENDAR_EVENT_ID);
                if (oldEventId != null) {
                    try {
                        event = calendar.getEvent(oldEventId);
                    } catch (Exception e) {
                        log.warn("Could not get the calendar event with id = {}", oldEventId, e);
                    }
                }

                // remove the event if it exists
                if (event != null) {
                    try {
                        calendar.removeEvent(calendar.getEditEvent(event.getId(), CalendarService.EVENT_REMOVE_CALENDAR));
                        properties.remove(AssignmentConstants.NEW_ASSIGNMENT_DUE_DATE_SCHEDULED);
                        properties.remove(ResourceProperties.PROP_ASSIGNMENT_DUEDATE_CALENDAR_EVENT_ID);
                    } catch (PermissionException ee) {
                        log.warn("Not allowed to remove calendar event for assignment = {}", assignment.getId());
                    } catch (InUseException ee) {
                        log.warn("Someone else is editing calendar event for assignment = {}", assignment.getId());
                    } catch (IdUnusedException ee) {
                        log.warn("Calendar event are in use for assignment = {} and event = {}", assignment.getId(), event.getId());
                    }
                }
            }
        }
    }


    private AnnouncementChannel getAnnouncementChannel(String contextId) {
        AnnouncementChannel channel = null;
        String channelId = serverConfigurationService.getString(announcementService.ANNOUNCEMENT_CHANNEL_PROPERTY, null);
        if (channelId == null) {
            channelId = announcementService.channelReference(contextId, siteService.MAIN_CONTAINER);
            try {
                channel = announcementService.getAnnouncementChannel(channelId);
            } catch (IdUnusedException e) {
                log.warn("No announcement channel found with id = {}", channelId);
                channel = null;
            } catch (PermissionException e) {
                log.warn("Current user not authorized to delete announcement with id = {}", channelId, e);
                channel = null;
            }
        }
        return channel;
    }

    private void removeAssociatedAnnouncementItem(AnnouncementChannel channel, Assignment assignment) {
        Map<String, String> properties = assignment.getProperties();
        if (channel != null) {
            String openDateAnnounced = StringUtils.trimToNull(properties.get(AssignmentConstants.NEW_ASSIGNMENT_OPEN_DATE_ANNOUNCED));
            String openDateAnnouncementId = StringUtils.trimToNull(properties.get(ResourceProperties.PROP_ASSIGNMENT_OPENDATE_ANNOUNCEMENT_MESSAGE_ID));
            if (openDateAnnounced != null && openDateAnnouncementId != null) {
                try {
                    channel.removeMessage(openDateAnnouncementId);
                } catch (PermissionException e) {
                    log.warn("User does not have permission", e);
                }
            }
        }
    }

    // TODO zipSubmissions and zipGroupSubmissions should be combined
    private void zipSubmissions(String assignmentReference, String assignmentTitle, Assignment.GradeType gradeType, Assignment.SubmissionType typeOfSubmission, Iterator submissions, OutputStream outputStream, StringBuilder exceptionMessage, boolean withStudentSubmissionText, boolean withStudentSubmissionAttachment, boolean withGradeFile, boolean withFeedbackText, boolean withFeedbackComment, boolean withFeedbackAttachment, boolean withRubrics, boolean withoutFolders, String gradeFileFormat, boolean includeNotSubmitted, String siteId) {
        ZipOutputStream out = null;

        boolean isAdditionalNotesEnabled = false;
        Site st = null;
        try {
            st = siteService.getSite(siteId);
            isAdditionalNotesEnabled = candidateDetailProvider != null && candidateDetailProvider.isAdditionalNotesEnabled(st);
        } catch (IdUnusedException e) {
            log.warn("Could not find site {} - isAdditionalNotesEnabled set to false", siteId);
        }

        try {
            out = new ZipOutputStream(outputStream);
            out.setLevel(serverConfigurationService.getInt("zip.compression.level", 1));

            // create the folder structure - named after the assignment's title
            final String root = escapeInvalidCharsEntry(Validator.escapeZipEntry(assignmentTitle)) + Entity.SEPARATOR;

            final SpreadsheetExporter.Type type = SpreadsheetExporter.Type.valueOf(gradeFileFormat.toUpperCase());
            final SpreadsheetExporter sheet = SpreadsheetExporter.getInstance(type, assignmentTitle, gradeType.toString(), getCsvSeparator());

            String submittedText = "";
            if (!submissions.hasNext()) {
                exceptionMessage.append("There is no submission yet. ");
            }

            if (isAdditionalNotesEnabled) {
                sheet.addHeader(resourceLoader.getString("grades.id"), resourceLoader.getString("grades.eid"), resourceLoader.getString("grades.lastname"),
                        resourceLoader.getString("grades.firstname"), resourceLoader.getString("grades.grade"),
                        resourceLoader.getString("grades.submissionTime"), resourceLoader.getString("grades.late"), resourceLoader.getString("gen.notes"));
            } else {
                sheet.addHeader(resourceLoader.getString("grades.id"), resourceLoader.getString("grades.eid"), resourceLoader.getString("grades.lastname"),
                        resourceLoader.getString("grades.firstname"), resourceLoader.getString("grades.grade"),
                        resourceLoader.getString("grades.submissionTime"), resourceLoader.getString("grades.late"));
            }

            // allow add assignment members
            final List<User> allowAddSubmissionUsers = allowAddSubmissionUsers(assignmentReference);

            // Create the ZIP file
            String caughtException = null;
            String caughtStackTrace = null;
            final StringBuilder submittersAdditionalNotesHtml = new StringBuilder();

            while (submissions.hasNext()) {
            	final AssignmentSubmission s = (AssignmentSubmission) submissions.next();
                boolean isAnon = assignmentUsesAnonymousGrading(s.getAssignment());
                //SAK-29314 added a new value where it's by default submitted but is marked when the user submits
                if ((s.getSubmitted() && s.getUserSubmission()) || includeNotSubmitted) {
                    // get the submitter who submitted the submission see if the user is still in site
                    final Optional<AssignmentSubmissionSubmitter> assignmentSubmitter = s.getSubmitters().stream().findAny();
                    try {
                        User u = null;
                        if (assignmentSubmitter.isPresent()) {
                            u = userDirectoryService.getUser(assignmentSubmitter.get().getSubmitter());
                        }
                        if (allowAddSubmissionUsers.contains(u)) {
                        	String submittersName = root;

                            final User[] submitters = s.getSubmitters().stream().map(p -> {
                                try {
                                    return userDirectoryService.getUser(p.getSubmitter());
                                } catch (UserNotDefinedException e) {
                                    log.warn("User not found {}, {}", p.getSubmitter(), e.getMessage());
                                }
                                return null;
                            }).filter(Objects::nonNull).toArray(User[]::new);

                            String submittersString = "";
                            for (int i = 0; i < submitters.length; i++) {
                                if (i > 0) {
                                    submittersString = submittersString.concat("; ");
                                }
                                String fullName = submitters[i].getSortName();
                                // in case the user doesn't have first name or last name
                                if (!fullName.contains(",")) {
                                    fullName = fullName.concat(",");
                                }
                                submittersString = submittersString.concat(fullName);
                                // add the eid to the end of it to guarantee folder name uniqness
                                // if user Eid contains non ascii characters, the user internal id will be used
                                final String userEid = submitters[i].getEid();
                                final String candidateEid = escapeInvalidCharsEntry(userEid);
                                if (candidateEid.equals(userEid)) {
                                    submittersString = submittersString + "(" + candidateEid + ")";
                                } else {
                                    submittersString = submittersString + "(" + submitters[i].getId() + ")";
                                }
                                submittersString = escapeInvalidCharsEntry(submittersString);
                                // Work out if submission is late.
                                final String latenessStatus = whenSubmissionMade(s);
                                log.debug("latenessStatus: " + latenessStatus);
                                
                                final String anonTitle = resourceLoader.getString("grading.anonymous.title");
                                final String fullAnonId = s.getId() + " " + anonTitle;

                                String[] params = new String[7];
                                if (isAdditionalNotesEnabled && candidateDetailProvider != null) {
                                    final List<String> notes = candidateDetailProvider.getAdditionalNotes(submitters[i], st).orElse(new ArrayList<String>());

                                    if (!notes.isEmpty()) {
                                        params = new String[notes.size() + 7];
                                        System.arraycopy(notes.toArray(new String[notes.size()]), 0, params, 7, notes.size());
                                    }
                                }

                                // SAK-17606
                                if (!isAnon) {
                                	log.debug("Zip user: " + submitters[i].toString());
                                    params[0] = submitters[i].getDisplayId();
                                    params[1] = submitters[i].getEid();
                                    params[2] = submitters[i].getLastName();
                                    params[3] = submitters[i].getFirstName();
                                    params[4] = this.getGradeForSubmitter(s, submitters[i].getId());
                                    if (s.getDateSubmitted() != null) {
                                    	params[5] = s.getDateSubmitted().toString(); // TODO may need to be formatted
                                    } else {
                                    	params[5] = "";	
                                    }
                                    params[6] = latenessStatus;
                                } else {
                                    params[0] = fullAnonId;
                                    params[1] = fullAnonId;
                                    params[2] = anonTitle;
                                    params[3] = anonTitle;
                                    params[4] = this.getGradeForSubmitter(s, submitters[i].getId());
                                    if (s.getDateSubmitted() != null) {
                                    	params[5] = s.getDateSubmitted().toString(); // TODO may need to be formatted
                                    } else {
                                    	params[5] = "";	
                                    }
                                    params[6] = latenessStatus;
                                }
                                sheet.addRow(params);
                            }

                            if (StringUtils.trimToNull(submittersString) != null) {
                                submittersName = submittersName.concat(StringUtils.trimToNull(submittersString));
                                submittedText = s.getSubmittedText();

                                // SAK-17606
                                if (isAnon) {
                                    submittersString = s.getId() + " " + resourceLoader.getString("grading.anonymous.title");
                                    submittersName = root + submittersString;
                                }

                                if (!withoutFolders) {
                                    submittersName = submittersName.concat("/");
                                } else {
                                    submittersName = submittersName.concat("_");
                                }

                                // record submission timestamp
                                if (!withoutFolders && s.getSubmitted() && s.getDateSubmitted() != null) {
                                    final String zipEntryName = submittersName + "timestamp" + AssignmentConstants.ZIP_COMMENT_FILE_TYPE;
                                    final String textEntryString = s.getDateSubmitted().toString();
                                    createTextZipEntry(out, zipEntryName, textEntryString);
                                }

                                // create the folder structure - named after the submitter's name
                                if (typeOfSubmission != Assignment.SubmissionType.ATTACHMENT_ONLY_ASSIGNMENT_SUBMISSION && typeOfSubmission != Assignment.SubmissionType.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION) {
                                    // include student submission text
                                    if (withStudentSubmissionText) {
                                        // create the text file only when a text submission is allowed
                                    	final StringBuilder submittersNameString = new StringBuilder(submittersName);
                                        //remove folder name if Download All is without user folders
                                        if (!withoutFolders) {
                                            submittersNameString.append(submittersString);
                                        }
                                    	
                                    	final String zipEntryName = submittersNameString.append("_submissionText" + AssignmentConstants.ZIP_SUBMITTED_TEXT_FILE_TYPE).toString();
                                        createTextZipEntry(out, zipEntryName, submittedText);
                                    }

                                    // include student submission feedback text
                                    if (withFeedbackText) {
                                        // create a feedbackText file into zip
                                    	final String zipEntryName = submittersName + "feedbackText" + AssignmentConstants.ZIP_SUBMITTED_TEXT_FILE_TYPE;
                                        final String textEntryString = s.getFeedbackText();
                                        createTextZipEntry(out, zipEntryName, textEntryString);
                                    }
                                }

                                if (typeOfSubmission != Assignment.SubmissionType.TEXT_ONLY_ASSIGNMENT_SUBMISSION && typeOfSubmission != Assignment.SubmissionType.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION && withStudentSubmissionAttachment) {
                                    // include student submission attachment
                                    //remove "/" that creates a folder if Download All is without user folders
                                    String sSubAttachmentFolder = submittersName + resourceLoader.getString("stuviewsubm.submissatt");//jh + "/";
                                    if (!withoutFolders) {
                                    	// create a attachment folder for the submission attachments
                                        sSubAttachmentFolder = submittersName + resourceLoader.getString("stuviewsubm.submissatt") + "/";
                                        sSubAttachmentFolder = escapeInvalidCharsEntry(sSubAttachmentFolder);
                                        final ZipEntry sSubAttachmentFolderEntry = new ZipEntry(sSubAttachmentFolder);
                                        out.putNextEntry(sSubAttachmentFolderEntry);
                                    } else {
                                    	sSubAttachmentFolder += "_";
                                        //submittersName = submittersName.concat("_");
                                    }

                                    // add all submission attachment into the submission attachment folder
                                    zipAttachments(out, submittersName, sSubAttachmentFolder, s.getAttachments());
                                    out.closeEntry();
                                }

                                if (withFeedbackComment) {
                                    // the comments.txt file to show instructor's comments
                                	final String zipEntryName = submittersName + "comments" + AssignmentConstants.ZIP_COMMENT_FILE_TYPE;
                                    final String textEntryString = formattedText.encodeUnicode(s.getFeedbackComment());
                                    createTextZipEntry(out, zipEntryName, textEntryString);
                                }

                                if (withFeedbackAttachment) {
                                    // create an attachment folder for the feedback attachments
                                    String feedbackSubAttachmentFolder = submittersName + resourceLoader.getString("download.feedback.attachment");
                                    if (!withoutFolders) {
                                        feedbackSubAttachmentFolder += "/";
                                        final ZipEntry feedbackSubAttachmentFolderEntry = new ZipEntry(feedbackSubAttachmentFolder);
                                        out.putNextEntry(feedbackSubAttachmentFolderEntry);
                                    } else {
                                        submittersName = submittersName.concat("_");
                                    }

                                    // add all feedback attachment folder
                                    zipAttachments(out, submittersName, feedbackSubAttachmentFolder, s.getFeedbackAttachments());
                                }

                                String assignmentId = s.getAssignment().getId();

                                Optional<AssociationTransferBean> optAssociation = rubricsService.getAssociationForToolAndItem(AssignmentConstants.TOOL_ID, assignmentId, siteId);

                                if (withRubrics && optAssociation.isPresent()) {
                                    byte[] pdf = rubricsService.createPdf(siteId, optAssociation.get().getRubricId(), AssignmentServiceConstants.ASSIGNMENT_TOOL_ID, assignmentId, s.getId());

                                    final ZipEntry zipEntryPdf = new ZipEntry(submittersName + resourceLoader.getString("zip.rubrics") + AssignmentConstants.ZIP_PDF_FILE_TYPE);

                                    out.putNextEntry(zipEntryPdf);
                                    out.write(pdf);
                                    out.closeEntry();
                                }
                            } // if

                            if (isAdditionalNotesEnabled && candidateDetailProvider != null) {
                                final List<String> notes = candidateDetailProvider.getAdditionalNotes(u, st).orElse(new ArrayList<String>());
                                if (!notes.isEmpty()) {
                                    final StringBuilder noteList = new StringBuilder("<ul>");
                                    for (String note : notes) {
                                        noteList.append("<li>" + StringEscapeUtils.escapeHtml4(note) + "</li>");
                                    }
                                    noteList.append("</ul>");
                                    submittersAdditionalNotesHtml.append("<tr><td style='padding-right:10px;padding-left:10px'>" + submittersString + "</td><td style='padding-right:10px'>" + noteList + "</td></tr>");
                                }
                            }
                        } else {
                            log.warn("Can't add submission: {} to zip, missing the submittee or they are no longer allowed to submit in the site", s.getId());
                        }
                    } catch (Exception e) {
                        caughtException = e.toString();
                        if (log.isDebugEnabled()) {
                            caughtStackTrace = ExceptionUtils.getStackTrace(e);
                        }
                        break;
                    }
                } // if the user is still in site

            } // while -- there is submission

            if (caughtException == null) {
                // continue
                if (withGradeFile) {
                    final ZipEntry gradesCSVEntry = new ZipEntry(root + "grades." + sheet.getFileExtension());
                    out.putNextEntry(gradesCSVEntry);
                    sheet.write(out);
                    out.closeEntry();
                }

                if (isAdditionalNotesEnabled) {
                	final ZipEntry additionalEntry = new ZipEntry(root + resourceLoader.getString("assignment.additional.notes.file.title") + ".html");
                    out.putNextEntry(additionalEntry);

                    StringBuilder htmlString = new StringBuilder();
                    htmlString.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\n")
                    .append("    \"http://www.w3.org/TR/html4/loose.dtd\">\n")
                    .append("<html>\n")
                    .append("  <head><title>additionalnotes</title></head>\n")
                    .append("  <body>\n")
                    .append("<h1>").append(resourceLoader.getString("assignment.additional.notes.export.title")).append("</h1>")
                    .append("<div>").append(resourceLoader.getString("assignment.additional.notes.export.header")).append("</div><br/>")
                    .append("<table border=\"1\"  style=\"border-collapse:collapse;\"><tr><th>")
                    .append(resourceLoader.getString("gen.student")).append("</th><th>")
                    .append(resourceLoader.getString("gen.notes")).append("</th>").append(submittersAdditionalNotesHtml)
                    .append("</table>")
                    .append("<br/><div>").append(resourceLoader.getString("assignment.additional.notes.export.footer")).append("</div>")
                    .append("\n  </body>\n</html>\n");

                    log.debug("Additional information html: {}", htmlString.toString());

                    final byte[] wes = htmlString.toString().getBytes();
                    out.write(wes);
                    additionalEntry.setSize(wes.length);
                    out.closeEntry();
                }
            } else {
                // log the error
                exceptionMessage.append(" Exception " + caughtException + " for creating submission zip file for assignment " + "\"" + assignmentTitle + "\"\n");
                if (log.isDebugEnabled()) {
                    exceptionMessage.append(caughtStackTrace);
                }
            }
        } catch (IOException e) {
            exceptionMessage.append("IOException for creating submission zip file for assignment " + "\"" + assignmentTitle + "\" exception: " + e + "\n");
        } finally {
            // Complete the ZIP file
            if (out != null) {
                try {
                    out.finish();
                    out.flush();
                } catch (IOException e) {
                    // tried
                }
                try {
                    out.close();
                } catch (IOException e) {
                    // tried
                }
            }
        }
    }

    // TODO zipSubmissions and zipGroupSubmissions should be combined
    protected void zipGroupSubmissions(String assignmentReference, String assignmentTitle, String gradeTypeString, Assignment.SubmissionType typeOfSubmission, Iterator submissions, OutputStream outputStream, StringBuilder exceptionMessage, boolean withStudentSubmissionText, boolean withStudentSubmissionAttachment, boolean withGradeFile, boolean withFeedbackText, boolean withFeedbackComment, boolean withFeedbackAttachment, boolean withRubrics, String gradeFileFormat, boolean includeNotSubmitted) {
        ZipOutputStream out = null;
        try {
            out = new ZipOutputStream(outputStream);
            out.setLevel(serverConfigurationService.getInt("zip.compression.level", 1));

            // create the folder structure - named after the assignment's title
            final String root = escapeInvalidCharsEntry(Validator.escapeZipEntry(assignmentTitle)) + Entity.SEPARATOR;

            final SpreadsheetExporter.Type type = SpreadsheetExporter.Type.valueOf(gradeFileFormat.toUpperCase());
            final SpreadsheetExporter sheet = SpreadsheetExporter.getInstance(type, assignmentTitle, gradeTypeString, getCsvSeparator());

            if (!submissions.hasNext()) {
                exceptionMessage.append("There is no submission yet. ");
            }

            // Write the header
            sheet.addHeader(resourceLoader.getString("group"), resourceLoader.getString("grades.eid"), resourceLoader.getString("grades.members"),
                    resourceLoader.getString("grades.grade"), resourceLoader.getString("grades.submissionTime"), resourceLoader.getString("grades.late"));

            // allow add assignment members
            allowAddSubmissionUsers(assignmentReference);

            // Create the ZIP file
            String caughtException = null;
            String caughtStackTrace = null;
            while (submissions.hasNext()) {
                final AssignmentSubmission s = (AssignmentSubmission) submissions.next();

                log.debug(this + " ZIPGROUP " + (s == null ? "null" : s.getId()));

                //SAK-29314 added a new value where it's by default submitted but is marked when the user submits
                if ((s.getSubmitted() && s.getUserSubmission()) || includeNotSubmitted) {
                    try {
                    	final StringBuilder submittersName = new StringBuilder(root);

                        final User[] submitters = s.getSubmitters().stream().map(p -> {
                            try {
                                return userDirectoryService.getUser(p.getSubmitter());
                            } catch (UserNotDefinedException e) {
                                log.warn("User not found {}", p.getSubmitter());
                                return null;
                            }
                        }).filter(Objects::nonNull).toArray(User[]::new);

                        final String groupTitle = siteService.getSite(s.getAssignment().getContext()).getGroup(s.getGroupId()).getTitle();
                        final StringBuilder submittersString = new StringBuilder();
                        final StringBuilder submitters2String = new StringBuilder();

                        for (int i = 0; i < submitters.length; i++) {
                            if (i > 0) {
                                submittersString.append("; ");
                                submitters2String.append("; ");
                            }
                            String fullName = submitters[i].getSortName();
                            // in case the user doesn't have first name or last name
                            if (fullName.indexOf(",") == -1) {
                                fullName = fullName.concat(",");
                            }
                            submittersString.append(fullName);
                            submitters2String.append(submitters[i].getDisplayName());
                            // add the eid to the end of it to guarantee folder name uniqness
                            submittersString.append("(" + submitters[i].getEid() + ")");
                        }
                        final String latenessStatus = whenSubmissionMade(s);

                        final String gradeDisplay = getGradeDisplay(s.getGrade(), s.getAssignment().getTypeOfGrade(), s.getAssignment().getScaleFactor());
                        
                        //Adding the row
                        sheet.addRow(groupTitle, s.getGroupId(), submitters2String.toString(),
                        		gradeDisplay, s.getDateSubmitted() != null ? s.getDateSubmitted().toString(): StringUtils.EMPTY, latenessStatus);


                        if (StringUtils.trimToNull(groupTitle) != null) {
                            submittersName.append(StringUtils.trimToNull(groupTitle)).append(" (").append(s.getGroupId()).append(")");
                            final String submittedText = s.getSubmittedText();

                            submittersName.append("/");

                            // record submission timestamp
                            if (s.getSubmitted() && s.getDateSubmitted() != null) {
                            	createTextZipEntry(out, submittersName + "timestamp" + AssignmentConstants.ZIP_COMMENT_FILE_TYPE, s.getDateSubmitted().toString());
                            }

                            // create the folder structure - named after the submitter's name
                            if (typeOfSubmission != Assignment.SubmissionType.ATTACHMENT_ONLY_ASSIGNMENT_SUBMISSION && typeOfSubmission != Assignment.SubmissionType.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION) {
                                // include student submission text
                                if (withStudentSubmissionText) {
                                    // create the text file only when a text submission is allowed
                                	final String zipEntryName = submittersName + groupTitle + "_submissionText" + AssignmentConstants.ZIP_SUBMITTED_TEXT_FILE_TYPE;
                                	createTextZipEntry(out, zipEntryName, submittedText);
                                }

                                // include student submission feedback text
                                if (withFeedbackText) {
                                    // create a feedbackText file into zip
                                	createTextZipEntry(out, submittersName + "feedbackText" + AssignmentConstants.ZIP_SUBMITTED_TEXT_FILE_TYPE, s.getFeedbackText());
                                }
                            }

                            if (typeOfSubmission != Assignment.SubmissionType.TEXT_ONLY_ASSIGNMENT_SUBMISSION && typeOfSubmission != Assignment.SubmissionType.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION && withStudentSubmissionAttachment) {
                                // include student submission attachment
                                // create a attachment folder for the submission attachments
                                final String sSubAttachmentFolder = submittersName + resourceLoader.getString("stuviewsubm.submissatt") + "/";
                                final ZipEntry sSubAttachmentFolderEntry = new ZipEntry(sSubAttachmentFolder);
                                out.putNextEntry(sSubAttachmentFolderEntry);
                                // add all submission attachment into the submission attachment folder
                                zipAttachments(out, submittersName.toString(), sSubAttachmentFolder, s.getAttachments());
                                out.closeEntry();
                            }

                            if (withFeedbackComment) {
                                // the comments.txt file to show instructor's comments
                                final String zipEntryName = submittersName + "comments" + AssignmentConstants.ZIP_COMMENT_FILE_TYPE;
                            	final String textEntryString = formattedText.encodeUnicode(s.getFeedbackComment());
                            	createTextZipEntry(out, zipEntryName, textEntryString);
                            }

                            if (withFeedbackAttachment) {
                                // create an attachment folder for the feedback attachments
                            	final String feedbackSubAttachmentFolder = submittersName + resourceLoader.getString("download.feedback.attachment") + "/";
                            	final ZipEntry feedbackSubAttachmentFolderEntry = new ZipEntry(feedbackSubAttachmentFolder);
                                out.putNextEntry(feedbackSubAttachmentFolderEntry);
                                // add all feedback attachment folder
                                zipAttachments(out, submittersName.toString(), feedbackSubAttachmentFolder, s.getFeedbackAttachments());
                                out.closeEntry();
                            }

                            Assignment assignment = s.getAssignment();
                            String assignmentId = assignment.getId();
                            String siteId = assignment.getContext();

                            Optional<AssociationTransferBean> optAssociation = rubricsService.getAssociationForToolAndItem(AssignmentConstants.TOOL_ID, assignmentId, siteId);

                            if (withRubrics && optAssociation.isPresent()) {
                                byte[] pdf = rubricsService.createPdf(siteId, optAssociation.get().getRubricId(), AssignmentServiceConstants.ASSIGNMENT_TOOL_ID, assignmentId, s.getId());

                                final ZipEntry zipEntryPdf = new ZipEntry(submittersName + resourceLoader.getString("zip.rubrics") + AssignmentConstants.ZIP_PDF_FILE_TYPE);

                                out.putNextEntry(zipEntryPdf);
                                out.write(pdf);
                                out.closeEntry();
                            }
                        } // if
                    } catch (Exception e) {
                        caughtException = e.toString();
                        if (log.isDebugEnabled()) {
                            caughtStackTrace = ExceptionUtils.getStackTrace(e);
                        }
                        break;
                    }
                } // if the user is still in site

            } // while -- there is submission

            if (caughtException == null) {
                // continue
                if (withGradeFile) {
                    final ZipEntry gradesCSVEntry = new ZipEntry(root + "grades." + sheet.getFileExtension());
                    out.putNextEntry(gradesCSVEntry);
                    sheet.write(out);
                    out.closeEntry();
                }
            } else {
                // log the error
                exceptionMessage.append(" Exception " + caughtException + " for creating submission zip file for assignment " + "\"" + assignmentTitle + "\"\n");
                if (log.isDebugEnabled()) {
                    exceptionMessage.append(caughtStackTrace);
                }
            }
        } catch (IOException e) {
            exceptionMessage.append("IOException for creating submission zip file for assignment " + "\"" + assignmentTitle + "\" exception: " + e + "\n");
        } finally {
            // Complete the ZIP file
            if (out != null) {
                try {
                    out.finish();
                    out.flush();
                } catch (IOException e) {
                    // tried
                }
                try {
                    out.close();
                } catch (IOException e) {
                    // tried
                }
            }
        }
    }

	private void createTextZipEntry(ZipOutputStream out, final String zipEntryName, final String textEntryString)
			throws IOException {
		final ZipEntry textEntry = new ZipEntry(zipEntryName);
		out.putNextEntry(textEntry);
		if(textEntryString != null) {
			final byte[] text = textEntryString.getBytes();
			out.write(text);
			textEntry.setSize(text.length);
		}
		out.closeEntry();
	}

    // TODO refactor this
    private String whenSubmissionMade(AssignmentSubmission s) {
        Instant dueTime = s.getAssignment().getDueDate();
        Instant submittedTime = s.getDateSubmitted();
        String latenessStatus;
        if (submittedTime == null) {
            latenessStatus = resourceLoader.getString("grades.lateness.unknown");
        } else if (dueTime != null && submittedTime.isAfter(dueTime)) {
            latenessStatus = resourceLoader.getString("grades.lateness.late");
        } else {
            latenessStatus = resourceLoader.getString("grades.lateness.ontime");
        }
        return latenessStatus;
    }

    // TODO refactor this
    private void zipAttachments(ZipOutputStream out, String submittersName, String sSubAttachmentFolder, Collection<String> attachments) {
        int attachedUrlCount = 0;
        InputStream content = null;
        Map<String, Integer> done = new HashMap<>();
        for (String r : attachments) {
            try {
                String attachId = removeReferencePrefix(r);
                ContentResource resource = contentHostingService.getResource(attachId);

                String contentType = resource.getContentType();

                ResourceProperties props = resource.getProperties();
                if ("true".equals(props.getProperty(AssignmentConstants.PROP_INLINE_SUBMISSION)))
                {
                    // File for the inline submission - the inline text has a separate file designated in the archive, so skip
                    continue;
                }
                String displayName = props.getPropertyFormatted(props.getNamePropDisplayName());
                displayName = escapeInvalidCharsEntry(displayName);

                // for URL content type, encode a redirect to the body URL
                if (contentType.equalsIgnoreCase(ResourceProperties.TYPE_URL)) {
                    displayName = "attached_URL_" + attachedUrlCount;
                    attachedUrlCount++;
                }

                // buffered stream input
                content = resource.streamContent();
                byte data[] = new byte[1024 * 10];
                BufferedInputStream bContent = null;
                try {
                    bContent = new BufferedInputStream(content, data.length);

                    String candidateName = sSubAttachmentFolder + displayName;
                    String realName = null;
                    Integer already = done.get(candidateName);
                    if (already == null) {
                        realName = candidateName;
                        done.put(candidateName, 1);
                    } else {
                        String fileName = org.springframework.util.StringUtils.stripFilenameExtension(candidateName);
                        String fileExt = org.springframework.util.StringUtils.getFilenameExtension(candidateName);
                        realName = org.springframework.util.StringUtils.hasText(fileExt)
                                ?  fileName + "+" + already + "." + fileExt
                                :  fileName + "+" + already;
                        done.put(candidateName, already + 1);
                    }

                    ZipEntry attachmentEntry = new ZipEntry(realName);
                    out.putNextEntry(attachmentEntry);
                    int bCount = -1;
                    while ((bCount = bContent.read(data, 0, data.length)) != -1) {
                        out.write(data, 0, bCount);
                    }

                    try {
                        out.closeEntry(); // The zip entry need to be closed
                    } catch (IOException ioException) {
                        log.warn(":zipAttachments: problem closing zip entry " + ioException);
                    }
                } catch (IllegalArgumentException iException) {
                    log.warn(":zipAttachments: problem creating BufferedInputStream with content and length " + data.length + iException);
                } finally {
                    if (bContent != null) {
                        try {
                            bContent.close(); // The BufferedInputStream needs to be closed
                        } catch (IOException ioException) {
                            log.warn(":zipAttachments: problem closing FileChannel " + ioException);
                        }
                    }
                }
            } catch (PermissionException e) {
                log.warn(" zipAttachments--PermissionException submittersName="
                        + submittersName + " attachment reference=" + r);
            } catch (IdUnusedException e) {
                log.warn(" zipAttachments--IdUnusedException submittersName="
                        + submittersName + " attachment reference=" + r);
            } catch (TypeException e) {
                log.warn(" zipAttachments--TypeException: submittersName="
                        + submittersName + " attachment reference=" + r);
            } catch (IOException e) {
                log.warn(" zipAttachments--IOException: Problem in creating the attachment file: submittersName="
                        + submittersName + " attachment reference=" + r + " error " + e);
            } catch (ServerOverloadException e) {
                log.warn(" zipAttachments--ServerOverloadException: submittersName="
                        + submittersName + " attachment reference=" + r);
            } finally {
                if (content != null) {
                    try {
                        content.close(); // The input stream needs to be closed
                    } catch (IOException ioException) {
                        log.warn(":zipAttachments: problem closing Inputstream content " + ioException);
                    }
                }
            }
        } // for
    }

    @Transactional
    public void resetAssignment(Assignment assignment) {
        assignmentRepository.resetAssignment(assignment);
    }

    @Override
    @Transactional
    public void postReviewableSubmissionAttachments(AssignmentSubmission submission) {
        try {
            Optional<AssignmentSubmissionSubmitter> submitter = submission.getSubmitters().stream().filter(AssignmentSubmissionSubmitter::getSubmittee).findFirst();
            if (!submitter.isPresent() && contentReviewService.allowSubmissionsOnBehalf()) {
            	//no submittee was found but the CRS allows submissions on behalf, grab the first submitter:
            	submitter = submission.getSubmitters().stream().findAny();
            }
            if (submitter.isPresent()) {
                Assignment assignment = submission.getAssignment();
                String assignmentRef = AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference();
                List<ContentResource> resources = new ArrayList<>();
                for (String attachment : submission.getAttachments()) {
                    Reference attachmentRef = entityManager.newReference(attachment);
                    try {
                        ContentResource resource = contentHostingService.getResource(attachmentRef.getId());
                        if (contentReviewService.isAcceptableContent(resource)) {
                            resources.add(resource);
                        }
                    } catch (TypeException e) {
                        log.warn("Could not retrieve content resource: {}, {}", assignmentRef, e.getMessage());
                    }
                }
                try {
                    contentReviewService.queueContent(submitter.get().getSubmitter(), assignment.getContext(), assignmentRef, resources);
                } catch (QueueException e) {
                    log.warn("Could not queue submission: {} for review, {}", submission.getId(), e.getMessage());
                }
            }
        } catch (IdUnusedException | PermissionException e) {
            log.warn("Could not locate submission: {}, {}", submission.getId(), e.getMessage());
        }
    }

    @Transactional
    public String createContentReviewAssignment(Assignment assignment, String assignmentRef, Instant openTime, Instant dueTime, Instant closeTime) {
        Map<String, Object> opts = new HashMap<>();
        Map<String, String> p = assignment.getProperties();

        opts.put("submit_papers_to", p.get(NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_RADIO));
        opts.put("report_gen_speed", p.get(NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_RADIO));
        opts.put("institution_check", Boolean.valueOf(p.get(NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INSTITUTION)) ? "1" : "0");
        opts.put("internet_check", Boolean.valueOf(p.get(NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INTERNET)) ? "1" : "0");
        opts.put("journal_check", Boolean.valueOf(p.get(NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_PUB)) ? "1" : "0");
        opts.put("s_paper_check", Boolean.valueOf(p.get(NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_TURNITIN)) ? "1" : "0");
        opts.put("s_view_report", Boolean.valueOf(p.get("s_view_report")) ? "1" : "0");

        if (serverConfigurationService.getBoolean("turnitin.option.exclude_bibliographic", true)) {
            //we don't want to pass parameters if the user didn't get an option to set it
            opts.put("exclude_biblio", Boolean.valueOf(p.get(NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_BIBLIOGRAPHIC)) ? "1" : "0");
        }
        //Rely on the deprecated "turnitin.option.exclude_quoted" setting if set, otherwise use "contentreview.option.exclude_quoted"
        boolean showExcludeQuoted = serverConfigurationService.getBoolean("turnitin.option.exclude_quoted", serverConfigurationService.getBoolean("contentreview.option.exclude_quoted", Boolean.TRUE));
        if (showExcludeQuoted) {
            opts.put("exclude_quoted", Boolean.valueOf(p.get(NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_QUOTED)) ? "1" : "0");
        } else {
            Boolean defaultExcludeQuoted = serverConfigurationService.getBoolean("contentreview.option.exclude_quoted.default", true);
            opts.put("exclude_quoted", defaultExcludeQuoted ? "1" : "0");
        }

        //exclude self plag
        if (serverConfigurationService.getBoolean("contentreview.option.exclude_self_plag", true)) {
            opts.put("exclude_self_plag", Boolean.valueOf(p.get(NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_SELF_PLAG)) ? "1" : "0");
        } else {
            Boolean defaultExcludeSelfPlag = serverConfigurationService.getBoolean("contentreview.option.exclude_self_plag.default", true);
            opts.put("exclude_self_plag", defaultExcludeSelfPlag ? "1" : "0");
        }

        //Store institutional Index
        if (serverConfigurationService.getBoolean("contentreview.option.store_inst_index", true)) {
            opts.put("store_inst_index", Boolean.valueOf(p.get(NEW_ASSIGNMENT_REVIEW_SERVICE_STORE_INST_INDEX)) ? "1" : "0");
        } else {
            Boolean defaultStoreInstIndex = serverConfigurationService.getBoolean("contentreview.option.store_inst_index.default", true);
            opts.put("store_inst_index", defaultStoreInstIndex ? "1" : "0");
        }

        //Student preview
        if (serverConfigurationService.getBoolean("contentreview.option.student_preview", false)) {
            opts.put("student_preview", Boolean.valueOf(p.get(NEW_ASSIGNMENT_REVIEW_SERVICE_STUDENT_PREVIEW)) ? "1" : "0");
        } else {
            Boolean defaultStudentPreview = serverConfigurationService.getBoolean("contentreview.option.student_preview.default", false);
            opts.put("student_preview", defaultStudentPreview ? "1" : "0");
        }

        int excludeType = Integer.parseInt(p.get(NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_TYPE));
        int excludeValue = Integer.parseInt(p.get(NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_VALUE));
        if ((excludeType == 1 || excludeType == 2)
                && excludeValue >= 0 && excludeValue <= 100) {
            opts.put("exclude_type", Integer.toString(excludeType));
            opts.put("exclude_value", Integer.toString(excludeValue));
        }
        opts.put("late_accept_flag", "1");

        SimpleDateFormat dform = ((SimpleDateFormat) DateFormat.getDateInstance());
        dform.applyPattern("yyyy-MM-dd HH:mm:ss");
        opts.put("dtstart", dform.format(openTime.toEpochMilli()));
        opts.put("dtdue", dform.format(dueTime.toEpochMilli()));
        //opts.put("dtpost", dform.format(closeTime.getTime()));
        opts.put("points", assignment.getMaxGradePoint());
        opts.put("title", assignment.getTitle());
        opts.put("instructions", assignment.getInstructions());
        if (!assignment.getAttachments().isEmpty()) {
            opts.put("attachments", new ArrayList<>(assignment.getAttachments()));
        }
        try {
            contentReviewService.createAssignment(assignment.getContext(), assignmentRef, opts);
            return "";
        } catch (Exception e) {
            log.error(e.toString());
            return e.getMessage();
        }
    }

    @Override
    public String[] myToolIds() {
        return new String[] { "sakai.assignment", "sakai.assignment.grades" };
    }

    @Override
    public Optional<List<String>> getTransferOptions() {
        return Optional.of(Arrays.asList(new String[] { EntityTransferrer.COPY_PERMISSIONS_OPTION, EntityTransferrer.PUBLISH_OPTION }));
    }

    @Override
    @Transactional
    public void updateEntityReferences(String toContext, Map<String, String> transversalMap) {
        if (transversalMap != null && !transversalMap.isEmpty()) {
            for (Assignment assignment : getAssignmentsForContext(toContext)) {
                try {
                    String msgBody = assignment.getInstructions();
                    String msgBodyMigrated = linkMigrationHelper.migrateAllLinks(transversalMap.entrySet(), msgBody);

                    String peerBody = assignment.getPeerAssessmentInstructions();
                    String peerBodyMigrated = linkMigrationHelper.migrateAllLinks(transversalMap.entrySet(), peerBody);

                    try {
                        if (!Objects.equals(msgBody, msgBodyMigrated)) {
                            assignment.setInstructions(msgBodyMigrated);
                            updateAssignment(assignment);
                        }
                        if (!Objects.equals(peerBody, peerBodyMigrated)) {
                            assignment.setPeerAssessmentInstructions(peerBodyMigrated);
                            updateAssignment(assignment);
                        }
                    } catch (Exception e) {
                        // exception
                        log.warn("UpdateEntityReference: cannot get assignment content for {}, {}", assignment.getId(), e.getMessage());
                    } finally {
                        // remove advisor
//                        securityService.popAdvisor(securityAdvisor);
                    }
                } catch (Exception ee) {
                    log.warn("UpdateEntityReference: remove Assignment and all references for {}, {}", assignment.getId(), ee.getMessage());
                }
            }
        }
    }

    @Override
    public boolean hasContent(String siteId) {

        return assignmentRepository.countAssignmentsBySite(siteId) > 0L;
    }

    @Override
    @Transactional
    public Map<String, String> transferCopyEntities(String fromContext, String toContext, List<String> ids, List<String> transferOptions) {

        Map<String, String> transversalMap = new HashMap<>();

        for (Assignment oAssignment : getAssignmentsForContext(fromContext)) {
            String oAssignmentId = oAssignment.getId();
            String nAssignmentId = null;

            if (ids == null || ids.isEmpty() || ids.contains(oAssignmentId)) {
                try {
                    Assignment nAssignment = addAssignment(toContext);
                    nAssignmentId = nAssignment.getId();

                    nAssignment.setTitle(oAssignment.getTitle());
                    // replace all occurrence of old context with new context inside instruction text
                    if(StringUtils.isNotBlank(oAssignment.getInstructions())){
                        String newInstructions = ltiService.fixLtiLaunchUrls(oAssignment.getInstructions(), fromContext, toContext, transversalMap);
                    	nAssignment.setInstructions(newInstructions.replaceAll(fromContext, toContext));
                    }
                    nAssignment.setTypeOfGrade(oAssignment.getTypeOfGrade());
                    nAssignment.setTypeOfSubmission(oAssignment.getTypeOfSubmission());

                    // User supplied publish option takes precedence, then property, then source.
                    if (oAssignment.getAllowPeerAssessment()) {
                        // Always set peer assessment assignments to draft on import as they need setup
                        nAssignment.setDraft(true);
                    } else if (transferOptions != null && transferOptions.contains(EntityTransferrer.PUBLISH_OPTION)) {
                        nAssignment.setDraft(false);
                    } else if (serverConfigurationService.getBoolean("import.importAsDraft", true)) {
                        nAssignment.setDraft(true);
                    } else {
                        nAssignment.setDraft(oAssignment.getDraft());
                    }

                    nAssignment.setCloseDate(oAssignment.getCloseDate());
                    nAssignment.setDropDeadDate(oAssignment.getDropDeadDate());
                    nAssignment.setDueDate(oAssignment.getDueDate());
                    nAssignment.setOpenDate(oAssignment.getOpenDate());
                    nAssignment.setHideDueDate(oAssignment.getHideDueDate());

                    nAssignment.setPosition(oAssignment.getPosition());
                    nAssignment.setAllowAttachments(oAssignment.getAllowAttachments());
                    nAssignment.setHonorPledge(oAssignment.getHonorPledge());
                    nAssignment.setIndividuallyGraded(oAssignment.getIndividuallyGraded());
                    nAssignment.setMaxGradePoint(oAssignment.getMaxGradePoint());
                    nAssignment.setScaleFactor(oAssignment.getScaleFactor());
                    nAssignment.setReleaseGrades(oAssignment.getReleaseGrades());
                    nAssignment.setEstimateRequired(oAssignment.getEstimateRequired());
                    nAssignment.setEstimate(oAssignment.getEstimate());

                    // If there is a LTI launch associated with this copy it over
                    if ( oAssignment.getContentId() != null ) {
                        Long contentKey = oAssignment.getContentId().longValue();
                        Object retval = ltiService.copyLTIContent(contentKey, toContext, fromContext);
                        if ( retval instanceof Long ) {
                            nAssignment.setContentId(((Long) retval).intValue());
                        // If something went wrong, we can't be an LTI submission in the new site
                        } else if ( retval == null || retval instanceof String ) {
                            nAssignment.setTypeOfSubmission(Assignment.SubmissionType.ASSIGNMENT_SUBMISSION_TYPE_NONE);
                            log.error("Could not copy LTI Content Item oldSite={} contentKey={} retval={}",fromContext, contentKey, retval);
                        }
                    }

                    if (!createGroupsOnImport) {
                        nAssignment.setTypeOfAccess(SITE);
                    } else {
                        // group assignment
                        if (oAssignment.getTypeOfAccess() == GROUP) {
                            nAssignment.setTypeOfAccess(GROUP);
                            nAssignment.setDraft(true); // for group assignments always set to draft
                            Site oSite = siteService.getSite(oAssignment.getContext());
                            Site nSite = siteService.getSite(nAssignment.getContext());

                            boolean siteChanged = false;
                            Collection<Group> nGroups = nSite.getGroups();
                            for (String groupId : oAssignment.getGroups()) {
                                Group oGroup = oSite.getGroup(groupId);
                                Optional<Group> existingGroup = nGroups.stream().filter(g -> StringUtils.equals(g.getTitle(), oGroup.getTitle())).findAny();
                                Group nGroup;
                                if (existingGroup.isPresent()) {
                                    // found a matching group
                                    nGroup = existingGroup.get();
                                } else {
                                    // create group
                                    nGroup = nSite.addGroup();
                                    nGroup.setTitle(oGroup.getTitle());
                                    nGroup.setDescription(oGroup.getDescription());
                                    nGroup.getProperties().addProperty("group_prop_wsetup_created", Boolean.TRUE.toString());
                                    siteChanged = true;
                                }
                                nAssignment.getGroups().add(nGroup.getReference());
                            }
                            if (siteChanged) siteService.save(nSite);
                            nAssignment.setIsGroup(oAssignment.getIsGroup());
                        }
                    }

                    // attachments
                    Set<String> oAttachments = oAssignment.getAttachments();
                    for (String oAttachment : oAttachments) {
                        Reference oReference = entityManager.newReference(oAttachment);
                        String oAttachmentId = oReference.getId();
                        // transfer attachment, replace the context string if necessary and add new attachment
                        String nReference = transferAttachment(fromContext, toContext, oAttachmentId, null);
                        nAssignment.getAttachments().add(nReference);
                    }

                    // peer review
                    nAssignment.setAllowPeerAssessment(oAssignment.getAllowPeerAssessment());
                    nAssignment.setPeerAssessmentAnonEval(oAssignment.getPeerAssessmentAnonEval());
                    nAssignment.setPeerAssessmentInstructions(oAssignment.getPeerAssessmentInstructions());
                    nAssignment.setPeerAssessmentNumberReviews(oAssignment.getPeerAssessmentNumberReviews());
                    nAssignment.setPeerAssessmentStudentReview(oAssignment.getPeerAssessmentStudentReview());
                    nAssignment.setPeerAssessmentPeriodDate(oAssignment.getPeerAssessmentPeriodDate());
                    if (nAssignment.getPeerAssessmentPeriodDate() == null && nAssignment.getCloseDate() != null) {
                        // set the peer period time to be 10 mins after accept until date
                        Instant tenMinutesAfterCloseDate = Instant.from(nAssignment.getCloseDate().plus(Duration.ofMinutes(10)));
                        nAssignment.setPeerAssessmentPeriodDate(tenMinutesAfterCloseDate);
                    }

                    // properties
                    Map<String, String> nProperties = nAssignment.getProperties();
                    nProperties.putAll(oAssignment.getProperties());
                    // remove the link btw assignment and announcement item. One can announce the open date afterwards
                    nProperties.remove(AssignmentConstants.NEW_ASSIGNMENT_OPEN_DATE_ANNOUNCED);
                    nProperties.remove(ResourceProperties.PROP_ASSIGNMENT_OPENDATE_ANNOUNCEMENT_MESSAGE_ID);

                    // remove the link btw assignment and calendar item. One can add the due date to calendar afterwards
                    nProperties.remove(AssignmentConstants.NEW_ASSIGNMENT_DUE_DATE_SCHEDULED);
                    nProperties.remove(ResourceProperties.PROP_ASSIGNMENT_DUEDATE_CALENDAR_EVENT_ID);

                    if (!nAssignment.getDraft()) {
                        addImportedDueDateCalendarEvent(oAssignment, nAssignment, nProperties);
                        addImportedOpenDateAnnouncement(oAssignment, nAssignment, nProperties);
                    }

                    // gradebook-integration link
                    final String associatedGradebookAssignment = nProperties.get(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);
                    // always clear the old assignments gradebook link
                    nProperties.remove(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);
                    final String assignmentAddToGradebookChoice = nProperties.get(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK);
                    final String nAssignmentRef = AssignmentReferenceReckoner.reckoner().assignment(nAssignment).reckon().getReference();

                    switch (assignmentAddToGradebookChoice) {
                        case GRADEBOOK_INTEGRATION_ADD, GRADEBOOK_INTEGRATION_ASSOCIATE -> {
                            org.sakaiproject.grading.api.Assignment originalGBAssignment = null;
                            org.sakaiproject.grading.api.Assignment newGbAssignment = null;

                            boolean isOriginalAssignmentExternal = gradingService.isExternalAssignmentDefined(oAssignment.getContext(), associatedGradebookAssignment);
                            if (!isOriginalAssignmentExternal) {
                                // load the assignment for internal gb link
                                try {
                                    originalGBAssignment = gradingService.getAssignmentByNameOrId(
                                            oAssignment.getContext(),
                                            oAssignment.getContext(),
                                            associatedGradebookAssignment);
                                } catch (AssessmentNotFoundException anfe) {
                                    // strange if couldn't load the original gb assignment
                                    log.debug("Original assignment {} not found in gradebook for site {}", associatedGradebookAssignment, nAssignment.getContext(), anfe);
                                }

                                if (originalGBAssignment != null) {
                                    try {
                                        // because the the association points to the original gb (ID) we can only look up by it's Name
                                        newGbAssignment = gradingService.getAssignmentByNameOrId(
                                                nAssignment.getContext(),
                                                nAssignment.getContext(),
                                                originalGBAssignment.getName());
                                    } catch (AssessmentNotFoundException anfe) {
                                        // couldn't load the new gb item so the user didn't import gradebook items
                                        log.debug("Assignment {} not found in gradebook for site {}", originalGBAssignment.getName(), nAssignment.getContext(), anfe);
                                    }
                                }
                            } else {
                                originalGBAssignment = gradingService.getExternalAssignment(oAssignment.getContext(), associatedGradebookAssignment);
                            }

                            if (nAssignment.getDraft()) {
                                // assignment is in the draft state
                                if (isOriginalAssignmentExternal) {
                                    // if this is an external defined assignments will create when publishing
                                    nProperties.remove(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);
                                    nProperties.put(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK, GRADEBOOK_INTEGRATION_ADD);
                                } else {
                                    if (newGbAssignment != null) {
                                        if (StringUtils.isNotBlank(newGbAssignment.getId().toString())) {
                                            nProperties.put(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT, newGbAssignment.getId().toString());
                                        }
                                        nProperties.put(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK, GRADEBOOK_INTEGRATION_ASSOCIATE);
                                    } else {
                                        nProperties.remove(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);
                                        nProperties.put(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK, GRADEBOOK_INTEGRATION_NO);
                                    }
                                }
                            } else {
                                // not in draft state
                                if (isOriginalAssignmentExternal) {
                                    // external gradebook items are created by assignments and linked using the assignments reference
                                    Long categoryId = null;
                                    if (originalGBAssignment != null) {
                                        categoryId = createCategoryForGbAssignmentIfNecessary(originalGBAssignment, oAssignment.getContext(), nAssignment.getContext())
                                                .orElse(null);
                                    }

                                    try {
                                        gradingService.addExternalAssessment(
                                                nAssignment.getContext(),
                                                nAssignment.getContext(),
                                                nAssignmentRef,
                                                null,
                                                nAssignment.getTitle(),
                                                nAssignment.getMaxGradePoint() / (double) nAssignment.getScaleFactor(),
                                                Date.from(nAssignment.getDueDate()),
                                                this.getToolId(),
                                                null,
                                                false,
                                                categoryId,
                                                null);
                                        if (StringUtils.isNotBlank(nAssignmentRef)) {
                                            nProperties.put(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT, nAssignmentRef);
                                        }
                                        nProperties.put(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK, GRADEBOOK_INTEGRATION_ASSOCIATE);
                                    } catch (org.sakaiproject.grading.api.ConflictingAssignmentNameException e) {
                                        // Assignment name conflicts with existing gradebook item
                                        // Generate a unique title and retry
                                        log.warn("Assignment '{}' conflicts with existing gradebook item in site {}. Attempting to rename. oAssignmentId={} nAssignmentId={}",
                                                nAssignment.getTitle(), nAssignment.getContext(), oAssignmentId, nAssignmentId);

                                        String uniqueTitle = generateUniqueAssignmentTitle(nAssignment.getTitle(), nAssignment.getContext());
                                        nAssignment.setTitle(uniqueTitle);
                                        nAssignment.setDraft(true); // Set to draft so user can review the renamed assignment
                                        // Use direct repository merge to avoid duplicate task/notification creation
                                        // The final updateAssignment() call at the end of this method will handle side effects
                                        nAssignment.setModifier(sessionManager.getCurrentSessionUserId());
                                        assignmentRepository.merge(nAssignment);

                                        try {
                                            // Retry with unique title
                                            gradingService.addExternalAssessment(
                                                    nAssignment.getContext(),
                                                    nAssignment.getContext(),
                                                    nAssignmentRef,
                                                    null,
                                                    uniqueTitle,
                                                    nAssignment.getMaxGradePoint() / (double) nAssignment.getScaleFactor(),
                                                    Date.from(nAssignment.getDueDate()),
                                                    this.getToolId(),
                                                    null,
                                                    false,
                                                    categoryId,
                                                    null);
                                            if (StringUtils.isNotBlank(nAssignmentRef)) {
                                                nProperties.put(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT, nAssignmentRef);
                                            }
                                            nProperties.put(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK, GRADEBOOK_INTEGRATION_ASSOCIATE);
                                            log.info("Successfully imported assignment with renamed title '{}' in site {}", uniqueTitle, nAssignment.getContext());
                                        } catch (Exception retryException) {
                                            // If retry also fails, set to draft and disable gradebook integration
                                            log.error("Failed to import assignment even after renaming. Setting to draft. Title='{}' oAssignmentId={} nAssignmentId={} Error: {}",
                                                    uniqueTitle, oAssignmentId, nAssignmentId, retryException.getMessage());
                                            nAssignment.setDraft(true);
                                            nProperties.remove(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);
                                            nProperties.put(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK, GRADEBOOK_INTEGRATION_NO);
                                            // Use direct repository merge to avoid duplicate task/notification creation
                                            // The final updateAssignment() call at the end of this method will handle side effects
                                            nAssignment.setModifier(sessionManager.getCurrentSessionUserId());
                                            assignmentRepository.merge(nAssignment);
                                        }
                                    }
                                } else {
                                    // internal gradebook items should have already been created and are linked using a Long or a title
                                    if (newGbAssignment == null) {
                                        // no existing gb item, only thing to do set assignment as draft and clear the link
                                        nProperties.remove(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);
                                        // change the integration to no so that they my choose
                                        nProperties.put(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK, GRADEBOOK_INTEGRATION_NO);
                                    } else {
                                        // gb item found just link it
                                        if (StringUtils.isNotBlank(newGbAssignment.getId().toString())) {
                                            nProperties.put(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT, newGbAssignment.getId().toString());
                                        }
                                        nProperties.put(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK, GRADEBOOK_INTEGRATION_ASSOCIATE);
                                    }
                                }
                            }
                        }
                        case GRADEBOOK_INTEGRATION_NO -> {
                            // disables gradebook integration
                            nProperties.put(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK, GRADEBOOK_INTEGRATION_NO);
                            nProperties.remove(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);
                        }
                        default -> {
                            // in a future jdk this can be combined with previous
                            nProperties.put(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK, GRADEBOOK_INTEGRATION_NO);
                            nProperties.remove(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);
                        }
                    }

                    updateAssignment(nAssignment);

                    // review service
                    if (oAssignment.getContentReview()) {
                        nAssignment.setContentReview(true);
                        String errorMsg = createContentReviewAssignment(nAssignment, nAssignmentRef, nAssignment.getOpenDate(), nAssignment.getDueDate(), nAssignment.getCloseDate());
                        if (StringUtils.isNotBlank(errorMsg)) {
                            log.warn("Error while copying old assignments and creating content review link: {}", errorMsg);
                            nAssignment.setDraft(true);
                            updateAssignment(nAssignment);
                        }
                    }

                    transversalMap.put("assignment/" + oAssignmentId, "assignment/" + nAssignmentId);
                    log.info("Old assignment id: {} - new assignment id: {}", oAssignmentId, nAssignmentId);

                    try {
                        if (taggingManager.isTaggable()) {
                            for (TaggingProvider provider : taggingManager.getProviders()) {
                                provider.transferCopyTags(assignmentActivityProducer.getActivity(oAssignment), assignmentActivityProducer.getActivity(nAssignment));
                            }
                        }
                    } catch (PermissionException pe) {
                        log.error("{} oAssignmentId={} nAssignmentId={}", pe.toString(), oAssignmentId, nAssignmentId);
                    }

                    // Import supplementary items if they are present in the assignment to be imported
                    // Model Answer
                    AssignmentModelAnswerItem oModelAnswerItem = assignmentSupplementItemService.getModelAnswer(oAssignmentId);
                    if (oModelAnswerItem != null) {
                        AssignmentModelAnswerItem nModelAnswerItem = assignmentSupplementItemService.newModelAnswer();
                        nModelAnswerItem.setAssignmentId(nAssignmentId);
                        assignmentSupplementItemService.saveModelAnswer(nModelAnswerItem);
                        nModelAnswerItem.setText(oModelAnswerItem.getText());
                        nModelAnswerItem.setShowTo(oModelAnswerItem.getShowTo());
                        Set<AssignmentSupplementItemAttachment> oModelAnswerItemAttachments = oModelAnswerItem.getAttachmentSet();
                        Set<AssignmentSupplementItemAttachment> nModelAnswerItemAttachments = new HashSet<>();
                        for (AssignmentSupplementItemAttachment oAttachment : oModelAnswerItemAttachments) {
                            AssignmentSupplementItemAttachment nAttachment = assignmentSupplementItemService.newAttachment();
                            // New attachment creation
                            String nAttachmentId = transferAttachment(fromContext, toContext, oAttachment.getAttachmentId(), null);
                            if (StringUtils.isNotEmpty(nAttachmentId)) {
                                nAttachment.setAssignmentSupplementItemWithAttachment(nModelAnswerItem);
                                nAttachment.setAttachmentId(nAttachmentId);
                                assignmentSupplementItemService.saveAttachment(nAttachment);
                                nModelAnswerItemAttachments.add(nAttachment);
                            }
                        }
                        nModelAnswerItem.setAttachmentSet(nModelAnswerItemAttachments);
                        assignmentSupplementItemService.saveModelAnswer(nModelAnswerItem);
                    }

                    // Private Note
                    AssignmentNoteItem oNoteItem = assignmentSupplementItemService.getNoteItem(oAssignmentId);
                    if (oNoteItem != null) {
                        AssignmentNoteItem nNoteItem = assignmentSupplementItemService.newNoteItem();
                        nNoteItem.setAssignmentId(nAssignment.getId());
                        nNoteItem.setNote(oNoteItem.getNote());
                        nNoteItem.setShareWith(oNoteItem.getShareWith());
                        nNoteItem.setCreatorId(userDirectoryService.getCurrentUser().getId());
                        assignmentSupplementItemService.saveNoteItem(nNoteItem);
                    }

                    // All Purpose
                    AssignmentAllPurposeItem oAllPurposeItem = assignmentSupplementItemService.getAllPurposeItem(oAssignmentId);
                    if (oAllPurposeItem != null) {
                        AssignmentAllPurposeItem nAllPurposeItem = assignmentSupplementItemService.newAllPurposeItem();
                        nAllPurposeItem.setAssignmentId(nAssignment.getId());
                        nAllPurposeItem.setTitle(oAllPurposeItem.getTitle());
                        nAllPurposeItem.setText(oAllPurposeItem.getText());
                        nAllPurposeItem.setHide(oAllPurposeItem.getHide());
                        nAllPurposeItem.setReleaseDate(null);
                        nAllPurposeItem.setRetractDate(null);

                        Set<AssignmentSupplementItemAttachment> oAllPurposeItemAttachments = oAllPurposeItem.getAttachmentSet();
                        Set<AssignmentSupplementItemAttachment> nAllPurposeItemAttachments = new HashSet<>();
                        for (AssignmentSupplementItemAttachment oAttachment : oAllPurposeItemAttachments) {
                            AssignmentSupplementItemAttachment nAttachment = assignmentSupplementItemService.newAttachment();
                            // New attachment creation
                            String nAttachId = transferAttachment(fromContext, toContext, oAttachment.getAttachmentId(), null);
                            if (StringUtils.isNotEmpty(nAttachId)) {
                                nAttachment.setAssignmentSupplementItemWithAttachment(nAllPurposeItem);
                                nAttachment.setAttachmentId(nAttachId);
                                assignmentSupplementItemService.saveAttachment(nAttachment);
                                nAllPurposeItemAttachments.add(nAttachment);
                            }
                        }
                        nAllPurposeItem.setAttachmentSet(nAllPurposeItemAttachments);
                        
                        // First save the AllPurposeItem to persist it before creating access entries
                        assignmentSupplementItemService.saveAllPurposeItem(nAllPurposeItem);
                        
                        // Now clean up existing access entries
                        assignmentSupplementItemService.cleanAllPurposeItemAccess(nAllPurposeItem);
                        
                        // Create and save new access entries
                        Set<AssignmentAllPurposeItemAccess> accessSet = new HashSet<>();
                        AssignmentAllPurposeItemAccess access = assignmentSupplementItemService.newAllPurposeItemAccess();
                        access.setAccess(userDirectoryService.getCurrentUser().getId());
                        access.setAssignmentAllPurposeItem(nAllPurposeItem);
                        accessSet.add(access);
                        nAllPurposeItem.setAccessSet(accessSet);
                        
                        // Save both the access entry and the updated AllPurposeItem
                        assignmentSupplementItemService.saveAllPurposeItemAccess(access);
                        assignmentSupplementItemService.saveAllPurposeItem(nAllPurposeItem);
                    }
                } catch (Exception e) {
                    log.error("{} oAssignmentId={} nAssignmentId={}", e.toString(), oAssignmentId, nAssignmentId);
                }
            }
        }
        return transversalMap;
    }

    private void addImportedDueDateCalendarEvent(Assignment sourceAssignment, Assignment importedAssignment,
            Map<String, String> importedProperties) {

        Map<String, String> sourceProperties = sourceAssignment.getProperties();
        if (!BooleanUtils.toBoolean(sourceProperties.get(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE))) {
            return;
        }

        // Scope imported due-date events to the imported assignment, not the source site's event metadata.
        CalendarEvent.EventAccess importedEventAccess = importedAssignment.getTypeOfAccess() == GROUP
            ? CalendarEvent.EventAccess.GROUPED
            : CalendarEvent.EventAccess.SITE;
        Collection<Group> importedEventGroups = getImportedAssignmentGroups(importedAssignment);
        if (importedAssignment.getTypeOfAccess() == GROUP && CollectionUtils.isEmpty(importedEventGroups)) {
            log.warn("No groups resolved for grouped imported assignment {} in site {}. "
                    + "Skipping due date calendar event import to avoid creating a no-audience event.",
                importedAssignment.getId(), importedAssignment.getContext());
            return;
        }

        Calendar toCalendar;
        String toCalendarId = calendarService.calendarReference(importedAssignment.getContext(), SiteService.MAIN_CONTAINER);
        try {
            toCalendar = calendarService.getCalendar(toCalendarId);
        } catch (IdUnusedException iue) {
            try {
                calendarService.commitCalendar(calendarService.addCalendar(toCalendarId));
                toCalendar = calendarService.getCalendar(toCalendarId);
            } catch (IdUsedException | IdInvalidException | IdUnusedException | PermissionException e) {
                log.warn("Failed getting/creating calendar {} while importing assignment {} into {}",
                    toCalendarId, sourceAssignment.getId(), importedAssignment.getId(), e);
                return;
            }
        } catch (PermissionException e) {
            log.warn("No permission to access calendar {} while importing assignment {} into {}",
                toCalendarId, sourceAssignment.getId(), importedAssignment.getId(), e);
            return;
        }

        CalendarEvent importedEvent = null;
        String sourceCalendarEventId = StringUtils.trimToNull(
            sourceProperties.get(ResourceProperties.PROP_ASSIGNMENT_DUEDATE_CALENDAR_EVENT_ID));
        if (sourceCalendarEventId != null) {
            try {
                String fromCalendarId = calendarService.calendarReference(sourceAssignment.getContext(), SiteService.MAIN_CONTAINER);
                Calendar fromCalendar = calendarService.getCalendar(fromCalendarId);
                CalendarEvent fromEvent = fromCalendar.getEvent(sourceCalendarEventId);
                // Reuse timing/title/content from the source event while applying destination access/groups.
                importedEvent = toCalendar.addEvent(fromEvent.getRange(), fromEvent.getDisplayName(),
                    fromEvent.getDescription(), fromEvent.getType(), fromEvent.getLocation(),
                    importedEventAccess, importedEventGroups, fromEvent.getAttachments());
            } catch (IdUnusedException | PermissionException e) {
                log.warn("Failed copying calendar event {} while importing assignment {} into {}",
                    sourceCalendarEventId, sourceAssignment.getId(), importedAssignment.getId(), e);
            }
        }

        if (importedEvent == null && importedAssignment.getDueDate() != null) {
            try {
                String dueTitle = resourceLoader.getString("gen.due");
                String dueDescription = resourceLoader.getFormattedMessage("assign_due_event_desc", importedAssignment.getTitle(),
                    getUsersLocalDateTimeString(importedAssignment.getDueDate()));
                if (dueDescription == null) {
                    dueDescription = importedAssignment.getTitle();
                }
                importedEvent = toCalendar.addEvent(
                    timeService.newTimeRange(importedAssignment.getDueDate().toEpochMilli(), 0),
                    dueTitle + " " + importedAssignment.getTitle(),
                    dueDescription,
                    "Deadline", "", importedEventAccess, importedEventGroups, null);
            } catch (PermissionException e) {
                log.warn("Failed recreating due date calendar event while importing assignment {} into {}",
                    sourceAssignment.getId(), importedAssignment.getId(), e);
            }
        }

        if (importedEvent != null) {
            importedProperties.put(ResourceProperties.PROP_ASSIGNMENT_DUEDATE_CALENDAR_EVENT_ID, importedEvent.getId());
            importedProperties.put(AssignmentConstants.NEW_ASSIGNMENT_DUE_DATE_SCHEDULED, Boolean.TRUE.toString());
            importedProperties.put(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE, Boolean.TRUE.toString());
        }
    }

    private Collection<Group> getImportedAssignmentGroups(Assignment importedAssignment) {

        if (importedAssignment.getTypeOfAccess() != GROUP || CollectionUtils.isEmpty(importedAssignment.getGroups())) {
            return Collections.emptyList();
        }

        try {
            Site site = siteService.getSite(importedAssignment.getContext());
            Collection<Group> groups = new ArrayList<>();
            for (String groupRef : importedAssignment.getGroups()) {
                // Imported assignment groups are already destination refs after transferCopyEntities group remap.
                Group group = site.getGroup(groupRef);
                if (group != null) {
                    groups.add(group);
                }
            }
            return groups;
        } catch (IdUnusedException e) {
            log.warn("Failed resolving groups for imported assignment {} in site {}",
                importedAssignment.getId(), importedAssignment.getContext(), e);
            return Collections.emptyList();
        }
    }

    private void addImportedOpenDateAnnouncement(Assignment sourceAssignment, Assignment importedAssignment,
            Map<String, String> importedProperties) {

        if (!BooleanUtils.toBoolean(sourceAssignment.getProperties().get(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE))
                || importedAssignment.getOpenDate() == null) {
            return;
        }

        try {
            AnnouncementChannel channel = getAnnouncementChannel(importedAssignment.getContext());
            if (channel == null) {
                return;
            }

            AnnouncementMessageEdit message = null;
            boolean committed = false;
            try {
                message = channel.addAnnouncementMessage();
                AnnouncementMessageHeaderEdit header = message.getAnnouncementHeaderEdit();
                header.setDraft(false);
                header.replaceAttachments(entityManager.newReferenceList());
                message.getPropertiesEdit().addProperty(ASSIGNMENT_REFERENCE_PROPERTY,
                    AssignmentReferenceReckoner.reckoner().assignment(importedAssignment).reckon().getReference());
                String subject = resourceLoader.getFormattedMessage("assig6", importedAssignment.getTitle());
                if (subject == null) {
                    subject = importedAssignment.getTitle();
                }
                header.setSubject(subject);

                if (importedAssignment.getTypeOfAccess() == GROUP) {
                    Collection<Group> groups = getImportedAssignmentGroups(importedAssignment);
                    if (CollectionUtils.isEmpty(groups)) {
                        log.warn("No groups resolved for grouped imported assignment {} in site {}. "
                                + "Canceling pending announcement edit to avoid creating a no-audience announcement.",
                            importedAssignment.getId(), importedAssignment.getContext());
                        channel.cancelMessage(message);
                        message = null;
                        return;
                    }
                    header.setGroupAccess(groups);
                } else {
                    header.clearGroupAccess();
                }

                String body = resourceLoader.getFormattedMessage("opedat",
                    formattedText.convertPlaintextToFormattedText(importedAssignment.getTitle()),
                    getUsersLocalDateTimeString(importedAssignment.getOpenDate()));
                if (body == null) {
                    body = importedAssignment.getTitle();
                }
                message.setBody("<p>" + body + "</p>");

                channel.commitMessage(message, NotificationService.NOTI_NONE, ANNOUNCEMENT_NOTIFICATION_INVOKEE);
                committed = true;

                importedProperties.put(AssignmentConstants.NEW_ASSIGNMENT_OPEN_DATE_ANNOUNCED, Boolean.TRUE.toString());
                importedProperties.put(ResourceProperties.PROP_ASSIGNMENT_OPENDATE_ANNOUNCEMENT_MESSAGE_ID, message.getId());
            } finally {
                if (!committed && message != null) {
                    channel.cancelMessage(message);
                }
            }
        } catch (PermissionException e) {
            log.warn("Failed recreating open date announcement while importing assignment {} into {}",
                sourceAssignment.getId(), importedAssignment.getId(), e);
        }
    }

    @Override
    @Transactional
    public Map<String, String> transferCopyEntities(String fromContext, String toContext, List<String> ids, List<String> transferOptions, boolean cleanup) {

        Map<String, String> transversalMap = new HashMap<>();

        try {
            if (cleanup) {
                for (Assignment assignment : getAssignmentsForContext(toContext)) {
                    String assignmentId = assignment.getId();

                    try {
                        // remove this assignment with all its associated items
                        deleteAssignmentAndAllReferences(assignment);
                    } catch (Exception e) {
                        log.warn("Remove assignment and all references for {}, {}", assignmentId, e.getMessage());
                    }
                }
            }
            transversalMap.putAll(transferCopyEntities(fromContext, toContext, ids, transferOptions));
        } catch (Exception e) {
            log.info("End removing Assignment data {}", e.getMessage());
        }

        return transversalMap;
    }

    @Override
    public List<Map<String, String>> getEntityMap(String fromContext) {

        return getAssignmentsForContext(fromContext).stream()
            .map(ass -> Map.of("id", ass.getId(), "title", ass.getTitle())).collect(Collectors.toList());
    }

    @Override
    public String getToolPermissionsPrefix() {
        return AssignmentServiceConstants.SECURE_PERMISSION_PREFIX;
    }

    private String transferAttachment(String fromContext, String toContext, String oAttachmentId, MergeConfig mcx) {
        String toolTitle = toolManager.getTool("sakai.assignment.grades").getTitle();
        try {
            ContentResource attachment = contentHostingService.copyAttachment(oAttachmentId, toContext, toolTitle, mcx);
            if ( attachment != null ) {
                return attachment.getReference();
            }
        } catch (IdUnusedException | TypeException | PermissionException e) {
            log.error("Error copying attachment: {}", e.getMessage());
        }
        return null;
    }

    private LRS_Statement getStatementForAssignmentGraded(String reference, Assignment a, AssignmentSubmission s, User studentUser) {
    	LRS_Actor instructor = learningResourceStoreService.getActor(sessionManager.getCurrentSessionUserId());
        LRS_Verb verb = new LRS_Verb(SAKAI_VERB.scored);
        LRS_Object lrsObject = new LRS_Object(serverConfigurationService.getPortalUrl() + reference, "received-grade-assignment");
        Map<String, String> nameMap = new HashMap<>();
        nameMap.put("en-US", "User received a grade");
        lrsObject.setActivityName(nameMap);
        Map<String, String> descMap = new HashMap<>();
        String resubmissionNumber = StringUtils.defaultString(s.getProperties().get(AssignmentConstants.ALLOW_RESUBMIT_NUMBER), "0");
        descMap.put("en-US", "User received a grade for their assginment: " + a.getTitle() + "; Submission #: " + resubmissionNumber);
        lrsObject.setDescription(descMap);
        LRS_Actor student = learningResourceStoreService.getActor(studentUser.getId());
        student.setName(studentUser.getDisplayName());
        return new LRS_Statement(student, verb, lrsObject, getLRS_Result(a, s, true), null);
    }

    private LRS_Result getLRS_Result(Assignment a, AssignmentSubmission s, boolean completed) {
        LRS_Result result = null;
        String decSeparator = formattedText.getDecimalSeparator();
        // gradeDisplay ready to conversion to Float
        String gradeDisplay = StringUtils.replace(getGradeDisplay(s.getGrade(), a.getTypeOfGrade(), a.getScaleFactor()), decSeparator, ".");
        if (Assignment.GradeType.SCORE_GRADE_TYPE == a.getTypeOfGrade() && NumberUtils.isCreatable(gradeDisplay)) { // Points
            String maxGradePointDisplay = StringUtils.replace(getMaxPointGradeDisplay(a.getScaleFactor(), a.getMaxGradePoint()), decSeparator, ".");
            result = new LRS_Result(new Float(gradeDisplay), 0.0f, new Float(maxGradePointDisplay), null);
            result.setCompletion(completed);
        } else {
            result = new LRS_Result(completed);
            result.setGrade(getGradeDisplay(s.getGrade(), a.getTypeOfGrade(), a.getScaleFactor()));
        }
        return result;
    }

    private LRS_Statement getStatementForUnsubmittedAssignmentGraded(String reference, Assignment a, AssignmentSubmission s, User studentUser) {
    	LRS_Actor instructor = learningResourceStoreService.getActor(sessionManager.getCurrentSessionUserId());
        LRS_Verb verb = new LRS_Verb(SAKAI_VERB.scored);
        LRS_Object lrsObject = new LRS_Object(serverConfigurationService.getAccessUrl() + reference, "received-grade-unsubmitted-assignment");
        Map<String, String> nameMap = new HashMap<>();
        nameMap.put("en-US", "User received a grade");
        lrsObject.setActivityName(nameMap);
        Map<String, String> descMap = new HashMap<>();
        descMap.put("en-US", "User received a grade for an unsubmitted assginment: " + a.getTitle());
        lrsObject.setDescription(descMap);
        LRS_Actor student = learningResourceStoreService.getActor(studentUser.getId());
        student.setName(studentUser.getDisplayName());
        return new LRS_Statement(student, verb, lrsObject, getLRS_Result(a, s, false), null);
    }

    private LRS_Statement getStatementForSubmitAssignment(String reference, String accessUrl, String assignmentName) {
    	LRS_Actor actor = learningResourceStoreService.getActor(sessionManager.getCurrentSessionUserId());
        LRS_Verb verb = new LRS_Verb(SAKAI_VERB.attempted);
        LRS_Object lrsObject = new LRS_Object(accessUrl + reference, "submit-assignment");
        HashMap<String, String> nameMap = new HashMap<String, String>();
        nameMap.put("en-US", "User submitted an assignment");
        lrsObject.setActivityName(nameMap);
        // Add description
        HashMap<String, String> descMap = new HashMap<String, String>();
        descMap.put("en-US", "User submitted an assignment: " + assignmentName);
        lrsObject.setDescription(descMap);
        return new LRS_Statement(actor, verb, lrsObject);
    }

    private void sendGradeReleaseNotification(AssignmentSubmission submission) {
        Set<User> filteredUsers;
        Assignment assignment = submission.getAssignment();
        Map<String, String> assignmentProperties = assignment.getProperties();
        String siteId = assignment.getContext();
        String resubmitNumber = submission.getProperties().get(AssignmentConstants.ALLOW_RESUBMIT_NUMBER);

        boolean released = BooleanUtils.toBoolean(submission.getGradeReleased());
        Set<String> submitterIds = new HashSet<>();
        try {
            Set<String> siteUsers = siteService.getSite(siteId).getUsers();
            submitterIds = submission.getSubmitters().stream().map(AssignmentSubmissionSubmitter::getSubmitter).filter(siteUsers::contains).collect(Collectors.toSet());
            filteredUsers = submitterIds.stream().map(id -> {
                try {
                    return userDirectoryService.getUser(id);
                } catch (UserNotDefinedException e) {
                    log.warn("Could not find user with id = {}, {}", id, e.getMessage());
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toSet());
        } catch (IdUnusedException e) {
            log.warn("Site ({}) not found.", siteId);
            return;
        }

        if (released && StringUtils.equals(AssignmentConstants.ASSIGNMENT_RELEASEGRADE_NOTIFICATION_EACH, assignmentProperties.get(AssignmentConstants.ASSIGNMENT_RELEASEGRADE_NOTIFICATION_VALUE))) {
            // send email to every submitters
            if (!filteredUsers.isEmpty()) {
                // send the message immediately
                userMessagingService.message(filteredUsers,
                        Message.builder().tool(AssignmentConstants.TOOL_ID).type("releasegrade").build(),
                        Arrays.asList(new MessageMedium[] { MessageMedium.EMAIL }), emailUtil.getReleaseGradeReplacements(assignment, siteId), NotificationService.NOTI_REQUIRED);
            }
        }
        if (StringUtils.isNotBlank(resubmitNumber) && StringUtils.equals(AssignmentConstants.ASSIGNMENT_RELEASERESUBMISSION_NOTIFICATION_EACH, assignmentProperties.get(AssignmentConstants.ASSIGNMENT_RELEASERESUBMISSION_NOTIFICATION_VALUE))) {
            // send email to every submitters
            if (!filteredUsers.isEmpty()) {
                // send the message immidiately
                userMessagingService.message(filteredUsers,
                        Message.builder().tool(AssignmentConstants.TOOL_ID).type("releaseresubmission").build(),
                        Arrays.asList(new MessageMedium[] { MessageMedium.EMAIL }), emailUtil.getReleaseResubmissionReplacements(submission), NotificationService.NOTI_REQUIRED);
            }
        }
    }

    private void notificationToInstructors(AssignmentSubmission submission, Assignment assignment) {
        String notiOption = assignment.getProperties().get(AssignmentConstants.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_VALUE);
        if (notiOption != null && !notiOption.equals(AssignmentConstants.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_NONE)) {
            // need to send notification email
            String context = assignment.getContext();
            String assignmentReference = AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference();

            // compare the list of users with the receive.notifications and list of users who can actually grade this assignment
            List<User> receivers = allowReceiveSubmissionNotificationUsers(context);
            List allowGradeAssignmentUsers = allowGradeAssignmentUsers(assignmentReference);
            receivers.retainAll(allowGradeAssignmentUsers);

            Map<String, Object> replacements = emailUtil.getSubmissionReplacements(submission);
            if (notiOption.equals(AssignmentConstants.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_EACH)) {
                // send the message immediately
                userMessagingService.message(new HashSet<>(receivers),
                    Message.builder().tool(AssignmentConstants.TOOL_ID).type("submission").build(),
                    Arrays.asList(new MessageMedium[] { MessageMedium.EMAIL }), replacements, NotificationService.NOTI_REQUIRED);
            } else if (notiOption.equals(AssignmentConstants.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_DIGEST)) {
                // digest the message to each user
                userMessagingService.message(new HashSet<>(receivers),
                    Message.builder().tool(AssignmentConstants.TOOL_ID).type("submission").build(),
                    Arrays.asList(new MessageMedium[] {MessageMedium.DIGEST}), replacements, NotificationService.NOTI_REQUIRED);
            }
        }
    }

    private void notificationToStudent(AssignmentSubmission submission, Assignment assignment) {
        if (serverConfigurationService.getBoolean("assignment.submission.confirmation.email", true)) {
            String siteId = assignment.getContext();
            Set<String> siteUsers = Collections.emptySet();
            try {
                siteUsers = siteService.getSite(siteId).getUsers();
            } catch (IdUnusedException e) {
                log.warn("Could not find site: {}", siteId, e);
            }
            Set<User> users = submission.getSubmitters()
                    .stream()
                    .map(AssignmentSubmissionSubmitter::getSubmitter)
                    .filter(siteUsers::contains) // this filters active users
                    .map(id -> {
                        try {
                            return userDirectoryService.getUser(id);
                        } catch (UserNotDefinedException e) {
                            log.warn("Could not find user with id = {}, {}", id, e.getMessage());
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            userMessagingService.message(users,
                Message.builder().tool(AssignmentConstants.TOOL_ID).type("submission").build(),
                Arrays.asList(new MessageMedium[] { MessageMedium.EMAIL }), emailUtil.getSubmissionReplacements(submission), NotificationService.NOTI_REQUIRED);
        }
    }

    @Override
    public String getUsersLocalDateTimeString(Instant date) {
        return userTimeService.dateTimeFormat(date, null, null);
    }

    @Override
    public String getUsersLocalDateTimeStringFromProperties(String date){
        if (date == null){
            return null;
        }
        Long dateLong = Long.parseLong(date);
        return getUsersLocalDateTimeString(Instant.ofEpochMilli(dateLong));
    }

    @Override
    public String removeReferencePrefix(String referenceId) {
        if (referenceId.startsWith(REF_PREFIX)) {
            referenceId = referenceId.replaceFirst(REF_PREFIX, "");
        }
        return referenceId;
    }

    @Override
    @Transactional
    public List<ContentReviewResult> getContentReviewResults(AssignmentSubmission s){
        ArrayList<ContentReviewResult> reviewResults = new ArrayList<ContentReviewResult>();
        //get all the attachments for this submission and populate the reviewResults
        List<ContentResource> contentResources = getAllAcceptableAttachments(s);
        for(ContentResource cr : contentResources){
            ContentReviewResult reviewResult = new ContentReviewResult();
            reviewResult.setContentResource(cr);
            ContentReviewItem cri = contentReviewService.getContentReviewItemByContentId(cr.getId());
            if(cri == null){
                log.warn("Retrieved null ContentReviewItem for content " + cr.getId());
                continue;
            }
            reviewResult.setContentReviewItem(cri);

            AssignmentReferenceReckoner.AssignmentReference referenceReckoner = AssignmentReferenceReckoner.reckoner().assignment(s.getAssignment()).reckon();
            reviewResult.setReviewReport(getReviewReport(cr, referenceReckoner.getReference()));
            String iconUrl = getReviewIconCssClass(reviewResult);
            reviewResult.setReviewIconCssClass(iconUrl);
            reviewResult.setReviewError(getReviewError(reviewResult));

            if ("true".equals(reviewResult.isInline())) {
                reviewResults.add(0, reviewResult);
            } else {
                reviewResults.add(reviewResult);
            }
        }
        return reviewResults;
    }

    public List<ContentReviewResult> getSortedContentReviewResults(AssignmentSubmission s){
        List<ContentReviewResult> reviewResults = getContentReviewResults(s);

        Comparator<ContentReviewResult> byReviewScore = Comparator.comparing(r ->
        {
            if (r.isPending()) {
                return -2;
            }
            else if (StringUtils.equals(r.getReviewReport(), "Error")) {
                return -1;
            }
            return r.getReviewScore();
        });

        reviewResults.sort(byReviewScore.reversed());
        return reviewResults;
    }

    @Override
    public boolean isContentReviewVisibleForSubmission(AssignmentSubmission submission)
    {
        if (submission == null)
        {
            throw new IllegalArgumentException("isContentReviewVisibleForSubmission invoked with submission = null");
        }

        Assignment assignment = submission.getAssignment();
        String assignmentReference = AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference();

        boolean hasInstructorPermission = allowGradeSubmission(assignmentReference);
        boolean hasStudentPermission = false;
        // If we have instructor permission, we can short circuit past student checks
        // Student checks: ensure the assignment is configured to allow students to view reports, and that the user is permitted to get the specified submission
        if (!hasInstructorPermission && Boolean.valueOf(assignment.getProperties().get("s_view_report")))
        {
            String submissionReference = AssignmentReferenceReckoner.reckoner().submission(submission).reckon().getReference();
            hasStudentPermission = allowGetSubmission(submissionReference);
        }

        // Content Review results should be visible iff the user has permission and the submission is not a draft
        return (hasInstructorPermission || hasStudentPermission) && submission.getSubmitted() && submission.getDateSubmitted() != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ContentResource> getAllAcceptableAttachments(AssignmentSubmission s) {
        List<ContentResource> attachments = new ArrayList<>();
        for (String attachment : s.getAttachments()) {
            Reference attachmentRef = entityManager.newReference(attachment);
            try {
                ContentResource resource = contentHostingService.getResource(attachmentRef.getId());
                if (contentReviewService.isAcceptableContent(resource)) {
                    attachments.add(resource);
                }
            } catch (Exception e) {
                log.warn(":getAllAcceptableAttachments() {} ", e.getMessage());
            }
        }
        return attachments;
    }

    private String getReviewIconCssClass(ContentReviewResult reviewResult) {
        if (reviewResult == null) {
            log.debug("{} getReviewIconCssClass(ContentResource, int) called with reviewResult == null", reviewResult.getContentResource().getId());
            return null;
        }

        Long status = reviewResult.getStatus();
        String reviewReport = reviewResult.getReviewReport();
        String iconCssClass = null;

        if (!"Error".equals(reviewReport)) {
            iconCssClass = contentReviewService.getIconCssClassforScore(reviewResult.getReviewScore(), reviewResult.getContentResource().getId());
        } else if (ContentReviewConstants.CONTENT_REVIEW_SUBMITTED_AWAITING_REPORT_CODE.equals(status) || ContentReviewConstants.CONTENT_REVIEW_NOT_SUBMITTED_CODE.equals(status)) {
            iconCssClass = "contentReviewIconPending";
        } else {
            iconCssClass = "contentReviewIconWarn";
        }

        return iconCssClass;
    }

    private String getReviewReport(ContentResource cr, String assignmentReference) {
        if (cr == null) {
            log.debug("getReviewReport(ContentResource) called with cr == null"/*, this.getId()*/);
            return "Error";
        }
        try {
            String contentId = cr.getId();
            if (allowGradeSubmission(assignmentReference)) {
                return contentReviewService.getReviewReportInstructor(contentId, assignmentReference, userDirectoryService.getCurrentUser().getId());
            } else {
                return contentReviewService.getReviewReportStudent(contentId, assignmentReference, userDirectoryService.getCurrentUser().getId());
            }
        } catch (Exception e) {
            log.debug(":getReviewReport(ContentResource) {}", e.getMessage());
            return "Error";
        }
    }

    private String getReviewError(ContentReviewResult reviewResult){
        if (reviewResult == null) {
            log.debug("getReviewReport(ContentReviewResult) called with reviewResult == null");
            return null;
        }
        if(reviewResult.getStatus() == null) {
            log.debug("getReviewReport(ContentReviewResult) called with reviewResult.getStatus() == null");
            return null;
        }
        Long status = reviewResult.getStatus();
        //This should use getLocalizedReviewErrorMesage(contentId) to get a i18n message of the error
        String errorMessage = null;
        boolean exposeError = false;
        if (status.equals(ContentReviewConstants.CONTENT_REVIEW_REPORT_ERROR_NO_RETRY_CODE)){
            errorMessage = resourceLoader.getString("content_review.error.REPORT_ERROR_NO_RETRY_CODE");
        } else if (status.equals(ContentReviewConstants.CONTENT_REVIEW_REPORT_ERROR_RETRY_CODE)) {
            errorMessage = resourceLoader.getString("content_review.error.REPORT_ERROR_RETRY_CODE");
        } else if (status.equals(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_NO_RETRY_CODE)) {
            errorMessage = resourceLoader.getString("content_review.error.SUBMISSION_ERROR_NO_RETRY_CODE");
            exposeError = true;
        } else if (status.equals(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_CODE)) {
            errorMessage = resourceLoader.getString("content_review.error.SUBMISSION_ERROR_RETRY_CODE");
            exposeError = true;
        } else if (status.equals(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_EXCEEDED_CODE)) {
            errorMessage = resourceLoader.getString("content_review.error.SUBMISSION_ERROR_RETRY_EXCEEDED_CODE");
        } else if (status.equals(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_USER_DETAILS_CODE)) {
            errorMessage = resourceLoader.getString("content_review.error.SUBMISSION_ERROR_USER_DETAILS_CODE");
        } else if (ContentReviewConstants.CONTENT_REVIEW_SUBMITTED_AWAITING_REPORT_CODE.equals(status)) {
            errorMessage = resourceLoader.getString("content_review.pending.info");
        } else if (ContentReviewConstants.CONTENT_REVIEW_NOT_SUBMITTED_CODE.equals(status)) {
            errorMessage = resourceLoader.getFormattedMessage("content_review.notYetSubmitted", new Object[] { contentReviewService.getServiceName() });
        }

        if (errorMessage == null) {
            errorMessage = resourceLoader.getString("content_review.error");
        }

        // Expose the underlying CRS error to the UI
        if (exposeError && exposeContentReviewErrorsToUI) {
            errorMessage += " " + resourceLoader.getFormattedMessage("content_review.errorFromSource", contentReviewService.getLocalizedLastError(reviewResult.getContentReviewItem()));
        }

        return errorMessage;
    }

    /**
     * Generates a unique assignment title by appending a suffix (-1, -2, etc.) if a conflict exists.
     * This is used during assignment import to avoid duplicate assignment names.
     *
     * @param originalTitle the original assignment title
     * @param context the site context
     * @return a unique title that doesn't conflict with existing assignments
     */
    private String generateUniqueAssignmentTitle(String originalTitle, String context) {
        Collection<Assignment> existingAssignments = getAssignmentsForContext(context);
        Set<String> existingTitles = existingAssignments.stream()
                .map(Assignment::getTitle)
                .collect(java.util.stream.Collectors.toSet());

        String uniqueTitle = originalTitle;
        int suffix = 1;

        // Keep incrementing suffix until we find a unique title
        while (existingTitles.contains(uniqueTitle)) {
            uniqueTitle = originalTitle + "-" + suffix;
            suffix++;
        }

        return uniqueTitle;
    }

    private Optional<Long> createCategoryForGbAssignmentIfNecessary(
            org.sakaiproject.grading.api.Assignment gbAssignment, String fromGradebookId,
            String toGradebookId) {

        String categoryName = gbAssignment.getCategoryName();
        if (!StringUtils.isBlank(categoryName)) {
            GradebookInformation toGbInformation = gradingService.getGradebookInformation(toGradebookId, toGradebookId);
            Integer destCategoryType = toGbInformation.getCategoryType();

            // Respect destination Gradebook configuration during assignment-only imports.
            // - If destination has no categories, do not create categories implicitly (leave uncategorized).
            if (Objects.equals(destCategoryType, GradingConstants.CATEGORY_TYPE_NO_CATEGORY)) {
                log.info("Assignment import: destination gradebook has no categories; skipping creation for category '{}'.", categoryName);
                return Optional.empty();
            }

            List<CategoryDefinition> toCategoryDefinitions
                = gradingService.getCategoryDefinitions(toGradebookId, toGradebookId);
            if (toCategoryDefinitions == null) {
                toCategoryDefinitions = new ArrayList<>();
            }

            if (toCategoryDefinitions.stream().noneMatch(cd -> Objects.equals(cd.getName(), categoryName))) {
                // The category doesn't exist yet
                Optional<CategoryDefinition> maybeFrom = gradingService.getCategoryDefinitions(fromGradebookId, fromGradebookId)
                    .stream()
                    .filter(cd -> Objects.equals(cd.getName(), categoryName))
                    .findAny();
                if (!maybeFrom.isPresent()) {
                    return Optional.empty();
                }
                CategoryDefinition fromCategoryDefinition = maybeFrom.get();
                CategoryDefinition toCategoryDefinition = new CategoryDefinition();
                toCategoryDefinition.setName(fromCategoryDefinition.getName());
                // Do not seed destination category with source-site assignments. Leave empty; the
                // external gradebook link will be established later via addExternalAssessment(categoryId).
                toCategoryDefinition.setAssignmentList(Collections.emptyList());
                toCategoryDefinition.setExtraCredit(fromCategoryDefinition.getExtraCredit());
                // For categories-only destinations, ignore source weight; set safe default 0.0.
                // For weighted destinations, create with 0.0 so total weight remains 100% until instructor adjusts.
                Double safeWeight = 0.0d;
                toCategoryDefinition.setWeight(safeWeight);
                toCategoryDefinition.setDropHighest(fromCategoryDefinition.getDropHighest());
                toCategoryDefinition.setDropLowest(fromCategoryDefinition.getDropLowest());
                toCategoryDefinition.setKeepHighest(fromCategoryDefinition.getKeepHighest());
                // Add new category and persist without changing destination category type
                List<CategoryDefinition> categories = toGbInformation.getCategories();
                if (categories == null) {
                    categories = new ArrayList<>();
                    toGbInformation.setCategories(categories);
                }
                categories.add(toCategoryDefinition);
                gradingService.updateGradebookSettings(toGradebookId, toGradebookId, toGbInformation);
                if (Objects.equals(destCategoryType, GradingConstants.CATEGORY_TYPE_WEIGHTED_CATEGORY)) {
                    log.info("Assignment import: created new weighted category '{}' in site {} with weight 0.0; instructor should adjust weights to include it.", categoryName, toGradebookId);
                } else {
                    log.info("Assignment import: created new category '{}' in site {}.", categoryName, toGradebookId);
                }
            }

            // A new category may have been added in the previous block. Pull them again, just to be sure. This will
            // ensure that any upstream caching is refreshed, too.
            Optional<CategoryDefinition> optional
                = gradingService.getCategoryDefinitions(toGradebookId, toGradebookId)
                    .stream()
                    .filter(cd -> cd.getName().equals(categoryName)).findAny();
            if (optional.isPresent()) {
                return Optional.of(optional.get().getId());
            } else {
                log.warn("Created new gb category, but couldn't find it after creation. Returning empty ...");
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<Assignment> getAssignmentsForGradebookLink(String context, String linkId) throws IdUnusedException, PermissionException {
        if (StringUtils.isNoneBlank(context, linkId)) {
            return assignmentRepository.findAssignmentsForGradebookLink(context, linkId);
        }
        return null;
    }

    @Override
    public List<MultiGroupRecord> checkAssignmentForUsersInMultipleGroups(String siteId, Collection<Group> asnGroups) {
        Collection<String> checkUsers = asnGroups.stream().flatMap(g -> g.getUsers().stream()).distinct().collect(Collectors.toList());
        return usersInMultipleGroups(siteId, checkUsers, asnGroups);
    }

    @Override
    public List<MultiGroupRecord> checkSubmissionForUsersInMultipleGroups(String siteId, Group submissionGroup, Collection<Group> asnGroups) {
        return usersInMultipleGroups(siteId, submissionGroup.getUsers(), asnGroups);
    }

     /**
      * A utility method to determine users listed in multiple groups
      * eligible to submit an assignment.  This is a bad situation.
      * Current mechanism is to error out assignments with this situation
      * to prevent affected groups from submitting and viewing feedback
      * and prevent instructors from grading or sending feedback to
      * affected groups until the conflict is resolved (by altering
      * membership or perhaps by designating a resolution).
      * @param siteId the site id
      * @param checkUsers the users to check for multiple group memberships
      * @param groups the groups to check
      * @return a list of records (each user that appears in multiple groups and the groups they are in)
      */
    private List<MultiGroupRecord> usersInMultipleGroups(String siteId, Collection<String> checkUsers, Collection<Group> groups) {
        List<MultiGroupRecord> dupes = new ArrayList<>();
        Site site;
        try {
            site = siteService.getSite(siteId);
        } catch (IdUnusedException e) {
            log.warn("Could not find site with id: " + siteId);
            return dupes;  // technically this should be some kind of error, but the chances of this actually happening are minuscule
        }

        for (String userid : checkUsers) {
            Collection<Group> userGroups = site.getGroupsWithMember(userid);
            List<Group> filteredUserGroups = userGroups.stream().filter(ug -> groups.contains(ug)).collect(Collectors.toList());

            if (filteredUserGroups.size() < 2) {
                continue; // not in multiple groups, skip this user
            }

            AsnUser user;
            try {
                User u = userDirectoryService.getUser(userid);
                /*
                * SAK-23697 Allow user to be in multiple groups if
                * no SECURE_ADD_ASSIGNMENT_SUBMISSION permission or
                * if user has both SECURE_ADD_ASSIGNMENT_SUBMISSION
                * and SECURE_GRADE_ASSIGNMENT_SUBMISSION permission (TAs and Instructors)
                */
                if (!securityService.unlock(u, SECURE_ADD_ASSIGNMENT_SUBMISSION, site.getReference())
                        || securityService.unlock(u, SECURE_GRADE_ASSIGNMENT_SUBMISSION, site.getReference())) {
                    continue; // INS/TA, skip
                }
                user = new AsnUser(userid, u.getDisplayId(siteId), u.getDisplayName(siteId));
            } catch (UserNotDefinedException e) {
                user = new AsnUser(userid, "", ""); // assume to be a student and report it
            }

            List<AsnGroup> groupList = filteredUserGroups.stream().map(g -> new AsnGroup(g.getId(), g.getTitle())).collect(Collectors.toList());
            groupList.sort(Comparator.comparing(g -> g.getTitle()));
            dupes.add(new MultiGroupRecord(user, groupList));
        }

        dupes.sort(Comparator.comparing(r -> r.user.getDisplayName()));
        return dupes;
    }

    @Override
    public boolean isValidTimeSheetTime(String time) {
        return timeSheetService.isValidTimeSheetTime(time);
    }

    @Override
    public String getTotalTimeSheet(AssignmentSubmissionSubmitter asnSubmissionSubmitter) {
        AssignmentSubmission submission = asnSubmissionSubmitter.getSubmission();
        String reference = AssignmentReferenceReckoner.reckoner().submission(submission).reckon().getReference();
        return timeSheetService.getTotalTimeSheet(reference);
    }

    @Override
    public Integer timeToInt(String time) {
        return timeSheetService.timeToInt(time);
    }

    @Override
    public String intToTime(int time) {
        return timeSheetService.intToTime(time);
    }

    @Override
    public String getContentReviewServiceName() {
        return this.contentReviewService.getServiceName();
    }
    
    @Override
    public String getAssignmentModifier(String modifier) {
        try {
            return userDirectoryService.getUser(modifier).getDisplayName();
        } catch (UserNotDefinedException e) {
    		return resourceLoader.getString("user.modify.unknown", "");
    	}
    }

    /**
     * Implementation of HardDeleteAware to allow content to be fully purged
     */
    public void hardDelete(String siteId) {
        log.info("Hard Delete  of Tool Assignments for context: {}", siteId);

        Collection<Assignment> assignments = getDeletedAssignmentsForContext(siteId);
        assignments.addAll(getAssignmentsForContext(siteId));

        //remove associated tags and delete assignment
        Iterator<Assignment> it =  assignments.iterator();
        while (it.hasNext()) {
            Assignment a = (org.sakaiproject.assignment.api.model.Assignment) it.next();
            removeAssociatedTaggingItem(a);

            try {
                deleteAssignment(a);
            } catch (PermissionException e) {
                log.error("insufficient permissions to delete assignment ", e);
            }
        }

        //remove attachements
        List<ContentResource> resources = contentHostingService.getAllResources("/attachment/" + siteId + "/Assignments/");
        for (ContentResource resource : resources) {
            log.debug("Removing resource: {}", resource.getId());
            try {
                contentHostingService.removeResource(resource.getId());
            } catch (Exception e) {
                log.warn("Failed to remove content.", e);
            }
        }

        // Cleanup the collections
        ContentCollection contentCollection = null;
        try {
            contentCollection = contentHostingService.getCollection("/attachment/" + siteId + "/Assignments/");
        } catch (IdUnusedException e) {
            log.warn("id for collection does not exist " + e);
        } catch (TypeException e1) {
            log.warn("not a collection " + e1);
        } catch (PermissionException e2) {
            log.warn("insufficient permissions " + e2);
        }

        try{
            if(contentCollection !=  null){
                List<String> members = contentCollection.getMembers();
                for(String member : members){
                    log.debug("remove contenCollection: " + member);
                    contentHostingService.removeCollection(member);
                }
                contentHostingService.removeCollection(contentCollection.getId());
            }
        }catch (IdUnusedException e) {
            log.warn("id for collection does not exist " + e);
        } catch (TypeException  e1) {
            log.warn("not a collection " + e1);
        } catch (PermissionException e2) {
            log.warn("insufficient permissions " + e2);
        }catch (InUseException e3){
            log.warn("InUseException " + e3);
        }catch (ServerOverloadException e4){
            log.warn("ServerOverloadException " + e4);
        }
    }

    @Override
    public boolean allowAddTags(String context) {
        String resourceString = AssignmentReferenceReckoner.reckoner().context(context).reckon().getReference();
        return permissionCheck(TagService.TAGSERVICE_MANAGE_PERMISSION, resourceString, null);
    }

    public FormattedText getFormattedText() {
        return formattedText;
    }
}
