/*
 * Copyright (c) 2016, The Apereo Foundation
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
 *
 */
package org.sakaiproject.tool.assessment.facade;

import java.io.File;
import java.text.Collator;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.comparators.NullComparator;
import org.apache.commons.lang3.StringUtils;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.sakaiproject.antivirus.api.VirusFoundException;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.event.cover.NotificationService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedSectionData;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingAttachment;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.GradingAttachmentData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingAttachment;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.MediaData;
import org.sakaiproject.tool.assessment.data.dao.grading.SectionGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.StudentGradingSummaryData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.StudentGradingSummaryIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.PersistenceHelper;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.api.LocaleService;
import org.sakaiproject.util.comparator.SakaiCollators;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Slf4j
@Transactional
public class AssessmentGradingFacadeQueries implements AssessmentGradingFacadeQueriesAPI {

	private SessionFactory sessionFactory;

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	protected Session getCurrentSession() {
		if (sessionFactory == null) {
			throw new DataAccessResourceFailureException("SessionFactory is null");
		}
		return sessionFactory.getCurrentSession();
	}

    /**
     * Default empty Constructor
     */
    public AssessmentGradingFacadeQueries() {
    }

    /**
     * Injected Services
     */
    private ContentHostingService contentHostingService;

    public void setContentHostingService(ContentHostingService contentHostingService) {
        this.contentHostingService = contentHostingService;
    }

    private SecurityService securityService;

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    private UserDirectoryService userDirectoryService;


    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }


    private PersistenceHelper persistenceHelper;

    public void setPersistenceHelper(PersistenceHelper persistenceHelper) {
        this.persistenceHelper = persistenceHelper;
    }

    public List<AssessmentGradingData> getTotalScores(final Long publishedId, final String which, final boolean getSubmittedOnly) {
        if (publishedId == null) return Collections.emptyList();
        try {
            final String forGradeClause = getSubmittedOnly
                    ? " and a.forGrade = true"
                    : " and (a.forGrade = true or (a.forGrade = false and a.status = :noSubmission))";

            Session session = getCurrentSession();

            Query<AssessmentGradingData> query = session.createQuery(
                    "from AssessmentGradingData a where a.publishedAssessmentId = :id" +
                    " and a.status > :removed" +
                    forGradeClause +
                    " order by a.agentId asc, a.finalScore desc, a.submittedDate desc",
                    AssessmentGradingData.class);
            query.setParameter("id", publishedId);
            query.setParameter("removed", AssessmentGradingData.REMOVED);
            if (!getSubmittedOnly) {
                query.setParameter("noSubmission", AssessmentGradingData.NO_SUBMISSION);
            }
            List<AssessmentGradingData> list = query.list();

            Map<Long, List<AssessmentGradingAttachment>> attachmentMap = getAssessmentGradingAttachmentMap(publishedId);
            for (AssessmentGradingData data : list) {
                if (attachmentMap.get(data.getAssessmentGradingId()) != null) {
                    data.setAssessmentGradingAttachmentList(attachmentMap.get(data.getAssessmentGradingId()));
                } else {
                    data.setAssessmentGradingAttachmentList(new ArrayList<>());
                }
            }

            // last submission
            if (which.equals(EvaluationModelIfc.LAST_SCORE.toString())) {
                Query<AssessmentGradingData> q2 = session.createQuery(
                        "from AssessmentGradingData a where a.publishedAssessmentId = :id" +
                        " and a.status > :removed" +
                        forGradeClause +
                        " order by a.agentId asc, a.submittedDate desc",
                        AssessmentGradingData.class);
                q2.setParameter("id", publishedId);
                q2.setParameter("removed", AssessmentGradingData.REMOVED);
                if (!getSubmittedOnly) {
                    q2.setParameter("noSubmission", AssessmentGradingData.NO_SUBMISSION);
                }
                list = q2.list();
            }

            if (which.equals(EvaluationModelIfc.ALL_SCORE.toString()) || which.equals(EvaluationModelIfc.AVERAGE_SCORE.toString())) {
                return list;
            } else {
                if (list.isEmpty()) {
                    return Collections.emptyList();
                }

                AssessmentGradingData first = list.get(0);
                if (first != null) {
                    // TODO while refactoring noticed this only sets published assessment id on the first one and not the rest?
                    first.setPublishedAssessmentId(Long.valueOf(publishedId));
                }
                return new ArrayList<>(list.stream()
                        .collect(Collectors.toMap(AssessmentGradingData::getAgentId, p -> p, (p, q) -> p))
                        .values());
            }
        } catch (Exception e) {
            log.warn(e.toString(), e);
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    public List<AssessmentGradingData> getAllSubmissions(final String publishedId) {
        try {
            Session session = getCurrentSession();
            Query<AssessmentGradingData> q = session.createQuery(
                    "from AssessmentGradingData a where a.publishedAssessmentId = :id and a.forGrade = :forgrade and a.status > :status",
                    AssessmentGradingData.class);
            q.setParameter("id", Long.parseLong(publishedId));
            q.setParameter("forgrade", true);
            q.setParameter("status", AssessmentGradingData.REMOVED);
            return q.list();
        } catch (Exception e) {
            log.warn("Error getting all submissions for publishedId {}: {}", publishedId, e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<AssessmentGradingData> getAllAssessmentGradingData(final Long publishedId) {
        try {
            Session session = getCurrentSession();
            Query<AssessmentGradingData> q = session.createQuery(
                    "from AssessmentGradingData a where a.publishedAssessmentId = :id and a.status <> :status and a.status <> :removed order by a.agentId asc, a.submittedDate desc",
                    AssessmentGradingData.class);
            q.setParameter("id", publishedId);
            q.setParameter("status", AssessmentGradingData.NO_SUBMISSION);
            q.setParameter("removed", AssessmentGradingData.REMOVED);
            List<AssessmentGradingData> list = q.list();

            list.forEach(agd -> agd.setItemGradingSet(getItemGradingSet(agd.getAssessmentGradingId())));

            return list;
        } catch (Exception e) {
            log.warn("Error getting all assessment grading data for assessment {}: {}", publishedId, e.toString());
            return new ArrayList<>();
        }
    }

    public Map<Long, List<ItemGradingData>> getItemScores(Long publishedId, final Long itemId, String which) {
        List scores = getTotalScores(publishedId, which, true);
        return getItemScores(itemId, scores, false);
    }

    public Map<Long, List<ItemGradingData>> getItemScores(Long publishedId, final Long itemId, String which, boolean loadItemGradingAttachment) {
        List scores = getTotalScores(publishedId, which, true);
        return getItemScores(itemId, scores, loadItemGradingAttachment);
    }

    public Map<Long, List<ItemGradingData>> getItemScores(final Long itemId, List<AssessmentGradingData> scores, boolean loadItemGradingAttachment) {
        try {
            if (scores == null || scores.isEmpty()) {
                return new HashMap<>();
            }

            HashMap<Long, List<ItemGradingData>> map = new HashMap<>();

            Session session = getCurrentSession();
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<ItemGradingData> cq = cb.createQuery(ItemGradingData.class);
            Root<ItemGradingData> root = cq.from(ItemGradingData.class);

            /** make list from AssessmentGradingData ids */
            List<Long> gradingIdList = scores.stream()
                    .map(AssessmentGradingData::getAssessmentGradingId)
                    .collect(Collectors.toList());

            /** create or disjunctive expression for (in clauses) */
            List<Predicate> inPredicates = new ArrayList<>();
            for (int i = 0; i < gradingIdList.size(); i += 50) {
                List<Long> tempList = gradingIdList.subList(i, Math.min(i + 50, gradingIdList.size()));
                inPredicates.add(root.get("assessmentGradingId").in(tempList));
            }
            Predicate disjunction = cb.or(inPredicates.toArray(new Predicate[0]));

            if (itemId.equals(Long.valueOf(0))) {
                cq.where(disjunction);
                //criteria.add(Expression.isNotNull("submittedDate"));
            } else {
                /** create logical and between the pubCriterion and the disjunction criterion */
                //Criterion pubCriterion = Expression.eq("publishedItem.itemId", itemId);
                Predicate pubCriterion = cb.equal(root.get("publishedItemId"), itemId);
                cq.where(cb.and(pubCriterion, disjunction));
                //criteria.add(Expression.isNotNull("submittedDate"));
            }
            cq.orderBy(
                cb.asc(root.get("agentId")),
                cb.desc(root.get("submittedDate"))
            );

            List<ItemGradingData> temp = session.createQuery(cq).getResultList();

            Map<Long, Set<ItemGradingAttachment>> attachmentMap = new HashMap<>();
            if (loadItemGradingAttachment) {
                attachmentMap = getItemGradingAttachmentMap(itemId);
            }
            for (ItemGradingData data : temp) {
                if (loadItemGradingAttachment) {
                    if (attachmentMap.get(data.getItemGradingId()) != null) {
                        data.setItemGradingAttachmentSet(attachmentMap.get(data.getItemGradingId()));
                    } else {
                        data.setItemGradingAttachmentSet(new HashSet<>());
                    }
                }
                List<ItemGradingData> thisone = map.get(data.getPublishedItemId());
                if (thisone == null) {
                    thisone = new ArrayList<>();
                }
                thisone.add(data);
                map.put(data.getPublishedItemId(), thisone);
            }
            map.forEach((k, v) -> {
                Collections.sort(v, new Comparator<ItemGradingData>() {
                    public int compare(ItemGradingData itg1, ItemGradingData itg2) {
                        return new NullComparator().compare(itg1.getPublishedAnswerId(), itg2.getPublishedAnswerId());
                    }
                });
            });
            return map;
        } catch (Exception e) {
            log.warn(e.toString(), e);
            return new HashMap<>();
        }
    }

    /**
     * This returns a hashmap of all the latest item entries, keyed by
     * item id for easy retrieval.
     *
     * @param publishedId
     * @param agentId
     * @return
     */
    public Map<Long, List<ItemGradingData>> getLastItemGradingData(final Long publishedId, final String agentId) {
        try {
            Session session = getCurrentSession();
            // I am debating should I use (a.forGrade=false and a.status=NO_SUBMISSION) or attemptDate is not null
            Query<AssessmentGradingData> q = session.createQuery(
                    "from AssessmentGradingData a where a.publishedAssessmentId = :id " +
                            "and a.agentId = :agent and a.forGrade = :forgrade and a.status <> :status and a.status <> :removed " +
                            "order by a.submittedDate DESC",
                    AssessmentGradingData.class);
            q.setParameter("id", publishedId);
            q.setParameter("agent", agentId);
            q.setParameter("forgrade", false);
            q.setParameter("status", AssessmentGradingData.NO_SUBMISSION);
            q.setParameter("removed", AssessmentGradingData.REMOVED);
            List<AssessmentGradingData> scores = q.list();

            if (scores.isEmpty()) {
                return new HashMap<>();
            }
            HashMap<Long, List<ItemGradingData>> map = new HashMap<>();
            AssessmentGradingData gdata = scores.get(0);
            // initialize itemGradingSet
            gdata.setItemGradingSet(getItemGradingSet(gdata.getAssessmentGradingId()));
            if (gdata.getForGrade()) {
                return new HashMap<>();
            }
            for (ItemGradingData data : gdata.getItemGradingSet()) {
                List<ItemGradingData> thisone = map.get(data.getPublishedItemId());
                if (thisone == null) {
                    thisone = new ArrayList<>();
                }
                thisone.add(data);
                map.put(data.getPublishedItemId(), thisone);
            }
            return map;
        } catch (Exception e) {
            log.warn(e.toString(), e);
            return new HashMap<>();
        }
    }

    public Map<Long, List<ItemGradingData>> getStudentGradingData(String assessmentGradingId) {
        return getStudentGradingData(assessmentGradingId, true);
    }

    private Map<Long, List<ItemGradingData>> getStudentGradingData(String assessmentGradingId, boolean loadGradingAttachment) {
        try {
            Map<Long, List<ItemGradingData>> map = new HashMap<>();
            Long id = Long.parseLong(assessmentGradingId);
            AssessmentGradingData gdata = load(id, loadGradingAttachment);
            log.debug("****#6, gdata={}", gdata);
            gdata.getItemGradingSet().forEach(data ->
                    map.computeIfAbsent(data.getPublishedItemId(), k -> new ArrayList<>()).add(data)
            );
            return map;
        } catch (Exception e) {
            log.warn("Failed to retrieve student grading data for assessment grading ID: {}, {}", assessmentGradingId, e.toString());
            return new HashMap<>();
        }
    }


    public Map<Long, List<ItemGradingData>> getSubmitData(final Long publishedId, final String agentId, final Integer scoringoption, final Long assessmentGradingId) {
        try {
            if (publishedId == null || agentId == null || scoringoption == null) {
                log.warn("getSubmitData called with null parameters");
                return new HashMap<>();
            }

            Session session = getCurrentSession();
            log.debug("scoringoption = " + scoringoption);

            List<AssessmentGradingData> scores;
            if (EvaluationModelIfc.LAST_SCORE.equals(scoringoption)) {
                // last submission
                if (assessmentGradingId == null) {
                    Query<AssessmentGradingData> q = session.createQuery(
                            "from AssessmentGradingData a where a.publishedAssessmentId = :id and a.agentId = :agent and a.forGrade = :forgrade and a.status > :status order by a.submittedDate DESC",
                            AssessmentGradingData.class);
                    q.setParameter("id", publishedId);
                    q.setParameter("agent", agentId);
                    q.setParameter("forgrade", true);
                    q.setParameter("status", AssessmentGradingData.REMOVED);
                    scores = q.list();
                } else {
                    Query<AssessmentGradingData> q = session.createQuery(
                            "from AssessmentGradingData a where a.assessmentGradingId = :id",
                            AssessmentGradingData.class);
                    q.setParameter("id", assessmentGradingId);
                    scores = q.list();
                }
            } else {
                // highest submission
                if (assessmentGradingId == null) {
                    Query<AssessmentGradingData> q = session.createQuery(
                            "from AssessmentGradingData a where a.publishedAssessmentId = :id and a.agentId = :agent and a.forGrade = :forgrade and a.status > :status order by a.finalScore DESC, a.submittedDate DESC",
                            AssessmentGradingData.class);
                    q.setParameter("id", publishedId);
                    q.setParameter("agent", agentId);
                    q.setParameter("forgrade", true);
                    q.setParameter("status", AssessmentGradingData.REMOVED);
                    scores = q.list();
                } else {
                    Query<AssessmentGradingData> q = session.createQuery(
                            "from AssessmentGradingData a where a.assessmentGradingId = :id",
                            AssessmentGradingData.class);
                    q.setParameter("id", assessmentGradingId);
                    scores = q.list();
                }
            }

            HashMap<Long, List<ItemGradingData>> map = new HashMap<>();
            if (scores.isEmpty()) {
                return new HashMap<>();
            }
            AssessmentGradingData gdata = scores.get(0);
            Map<Long, Set<ItemGradingAttachment>> attachmentMap = getItemGradingAttachmentMapByAssessmentGradingId(
                    gdata.getAssessmentGradingId());
            gdata.setItemGradingSet(getItemGradingSet(gdata.getAssessmentGradingId()));
            for (ItemGradingData data : gdata.getItemGradingSet()) {
                if (attachmentMap.get(data.getItemGradingId()) != null) {
                    data.setItemGradingAttachmentSet(attachmentMap.get(data.getItemGradingId()));
                } else {
                    data.setItemGradingAttachmentSet(new HashSet<>());
                }

                List<ItemGradingData> thisone = map.get(data.getPublishedItemId());
                if (thisone == null) {
                    thisone = new ArrayList<>();
                }
                thisone.add(data);
                map.put(data.getPublishedItemId(), thisone);
            }
            return map;
        } catch (Exception e) {
            log.warn(e.toString(), e);
            return new HashMap<>();
        }
    }

    public Long add(AssessmentGradingData a) {
        int retryCount = persistenceHelper.getRetryCount();
        while (retryCount > 0) {
            try {
                Session session = getCurrentSession();
                session.persist(a);
                retryCount = 0;
            } catch (Exception e) {
                log.warn("problem adding assessmentGrading: " + e.toString());
                retryCount = persistenceHelper.retryDeadlock(e, retryCount);
            }
        }
        return a.getAssessmentGradingId();
    }

    public int getSubmissionSizeOfPublishedAssessment(Long publishedAssessmentId) {
        try {
            Session session = getCurrentSession();
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Long> cq = cb.createQuery(Long.class);
            Root<AssessmentGradingData> root = cq.from(AssessmentGradingData.class);

            cq.select(cb.count(root));
            cq.where(
                cb.equal(root.get("publishedAssessmentId"), publishedAssessmentId),
                cb.equal(root.get("forGrade"), true)
            );

            Long size = session.createQuery(cq).uniqueResult();
            return size != null ? size.intValue() : 0;
        } catch (Exception e) {
            log.warn("Error getting submission size for published assessment {}: {}", publishedAssessmentId, e.toString());
            return 0;
        }
    }

    public Long saveMedia(byte[] media, String mimeType) {
        MediaData mediaData = new MediaData(media, mimeType);
        mediaData.setFileSize((long) media.length);
        return saveMedia(mediaData);
    }

    protected SecurityAdvisor pushAdvisor() {
        SecurityAdvisor samigoAdvisor = new SecurityAdvisor() {
            public SecurityAdvice isAllowed(String userId, String function, String reference) {
                return SecurityAdvice.ALLOWED;
            }
        };
        securityService.pushAdvisor(samigoAdvisor);
        return samigoAdvisor;
    }

    protected void popAdvisor(SecurityAdvisor sa) {
        if (sa != null) {
            securityService.popAdvisor(sa);
        }
        else {
            throw new IllegalArgumentException("popAdvisor was called with a null SecurityAdvisor");
        }
    }

    protected boolean checkMediaCollection(String id) {
        SecurityAdvisor resourceAdvisor = pushAdvisor();
        try {
            contentHostingService.checkCollection(id);
        } catch (IdUnusedException | TypeException | PermissionException e) {
            return false;
        } finally {
            popAdvisor(resourceAdvisor);
        }
        return true;
    }

    protected boolean ensureMediaCollection(String id) {
        SecurityAdvisor resourceAdvisor = pushAdvisor();
        try {
            ContentCollection coll = contentHostingService.getCollection(id);
        } catch (IdUnusedException ie) {
            log.debug("Creating collection: " + id);
            String name = id;
            if (name.endsWith("/")) {
                name = id.substring(0, id.length() - 1);
            }
            name = name.substring(name.lastIndexOf('/') + 1);

            try {
                ContentCollectionEdit edit = contentHostingService.addCollection(id);
                ResourcePropertiesEdit props = edit.getPropertiesEdit();
                props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
                contentHostingService.commitCollection(edit);
            } catch (IdUsedException | IdInvalidException | PermissionException | InconsistentException collex) {
                log.warn("[Samigo Media Attachments] Exception while creating collection (" + id + "): " + collex.toString());
                return false;
            }
        } catch (TypeException | PermissionException e) {
            log.warn("[Samigo Media Attachments] General exception while ensuring collection: " + e.toString());
        } finally {
            popAdvisor(resourceAdvisor);
        }
        return true;
    }

    protected boolean ensureMediaPath(String path) {
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("[Samigo Media Attachments] Relative media paths are not acceptable. (" + path + ")");
        }

        int lastSlash = path.lastIndexOf("/");

        // Fast track already existing collections
        if (lastSlash != 0 && checkMediaCollection(path.substring(0, lastSlash + 1))) {
            return true;
        }

        // Ensure everything exists from the root
        int slash = 1;
        while (slash != lastSlash) {
            slash = path.indexOf("/", slash + 1);
            if (!ensureMediaCollection(path.substring(0, slash + 1))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Create or update a ContentResource for the media payload of this MediaData.
     *
     * @param mediaData the complete MediaData item to save if the media byte array is not null
     * @return the ID in Content Hosting of the stored item; null on failure
     */
    protected String saveMediaToContent(MediaData mediaData) {
        String mediaPath = getMediaPath(mediaData);
        if (mediaData.getMedia() != null && ensureMediaPath(mediaPath)) {
            log.debug("=====> Saving media: " + mediaPath);
            SecurityAdvisor resourceAdvisor = pushAdvisor();
            boolean newResource = true;

            try {
                contentHostingService.checkResource(mediaPath);
                newResource = false;
            } catch (PermissionException | IdUnusedException | TypeException e) {
                // Just a check, no handling
            }

            try {
                ContentResource chsMedia;
                if (newResource) {
                    ContentResourceEdit edit = contentHostingService.addResource(mediaPath);
                    edit.setContentType(mediaData.getMimeType());
                    edit.setContent(mediaData.getMedia());
                    ResourcePropertiesEdit props = edit.getPropertiesEdit();
                    props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, mediaData.getFilename());
                    contentHostingService.commitResource(edit);
                    chsMedia = contentHostingService.getResource(mediaPath);
                } else {
                    chsMedia = contentHostingService.updateResource(mediaPath,
                            mediaData.getMimeType(),
                            mediaData.getMedia());
                }
                if (chsMedia != null && StringUtils.isNotBlank(chsMedia.getContentType())) {
                    mediaData.setMimeType(chsMedia.getContentType());
                }
                mediaData.setContentResource(chsMedia);
                return mediaPath;
            } catch (PermissionException | IdUsedException | IdInvalidException | InconsistentException | ServerOverloadException | OverQuotaException | VirusFoundException | IdUnusedException | TypeException | InUseException e) {
                log.warn("Exception while saving media to content: " + e.toString());
            } finally {
                popAdvisor(resourceAdvisor);
            }
        }
        return null;
    }

    protected ContentResource getMediaContentResource(MediaData mediaData) {
        if (mediaData.getContentResource() != null) {
            return mediaData.getContentResource();
        }

        String id = getMediaPath(mediaData);
        log.debug("=====> Reading media: " + id);
        if (id != null) {
            SecurityAdvisor resourceAdvisor = pushAdvisor();
            try {
                ContentResource res = contentHostingService.getResource(id);
                return res;
            } catch (IdUnusedException ie) {
                log.info("Nonexistent resource when trying to load media (id: " + mediaData.getMediaId() + "): " + id);
            } catch (PermissionException | TypeException e) {
                log.debug("Exception while reading media from content (" + mediaData.getMediaId() + "):" + e.toString());
            } finally {
                popAdvisor(resourceAdvisor);
            }
        }
        return null;
    }

    protected String getMediaPath(MediaData mediaData) {
        String mediaBase = "/private/samigo/";
        String mediaPath = null;

        ItemGradingData itemGrading = mediaData.getItemGradingData();

        if (itemGrading != null) {
            PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
            PublishedAssessmentIfc assessment = getPublishedAssessmentByAssessmentGradingId(
                    itemGrading.getAssessmentGradingId());
            String assessmentId = assessment.getPublishedAssessmentId().toString();
            String siteId = publishedAssessmentService.getPublishedAssessmentSiteId(assessmentId);
            String userId = itemGrading.getAgentId();
            String questionId = itemGrading.getPublishedItemId().toString();

            if (questionId != null && assessmentId != null) {
                mediaPath = mediaBase + siteId + "/" + assessmentId + "/" + userId + "/" + questionId + "_"
                        + mediaData.getFilename();
            }
        }

        return mediaPath;
    }

    public Long saveMedia(MediaData mediaData) {
        log.debug("****" + mediaData.getFilename() + " saving media...size=" + mediaData.getFileSize() + " " + (new Date()));
        int retryCount = persistenceHelper.getRetryCount();

        getMediaPath(mediaData);

        while (retryCount > 0) {
            try {
                saveMediaToContent(mediaData);
                Session session = getCurrentSession();
                session.merge(mediaData);
                retryCount = 0;
            } catch (Exception e) {
                log.warn("problem saving media: " + e.toString());
                retryCount = persistenceHelper.retryDeadlock(e, retryCount);
            }
        }
        log.debug("****" + mediaData.getFilename() + " saved media." + (new Date()));
        return mediaData.getMediaId();
    }

    public void removeMediaById(Long mediaId) {
        removeMediaById(mediaId, null);
    }

    public void removeMediaById(Long mediaId, Long itemGradingId) {
        String mediaLocation = null;
        String mediaFilename = null;
        int retryCount = persistenceHelper.getRetryCount();
        while (retryCount > 0) {
            try {
                MediaData mediaData = this.getMedia(mediaId);
                if (mediaData == null) {
                    log.warn("MediaData with id {} not found", mediaId);
                    retryCount = 0;
                    return;
                }
                mediaLocation = mediaData.getLocation();
                mediaFilename = mediaData.getFilename();
                Session session = getCurrentSession();
                session.remove(mediaData);
                retryCount = 0;
            } catch (Exception e) {
                log.warn("Problem deleting media with Id {}", mediaId);
                retryCount = persistenceHelper.retryDeadlock(e, retryCount);
            }
        }

        if (mediaLocation != null) {
            File mediaFile = new File(mediaLocation);
            if (mediaFile.delete()) {
                log.warn("problem removing file. mediaLocation = {}", mediaLocation);
            }
        }

        if (itemGradingId != null) {
            ItemGradingData itemGradingData = getItemGrading(itemGradingId);
            if (itemGradingData != null) {
                itemGradingData.setAutoScore(Double.valueOf(0));
                saveItemGrading(itemGradingData);
                EventTrackingService.post(EventTrackingService.newEvent(
                        SamigoConstants.EVENT_ASSESSMENT_ATTACHMENT_DELETE, 
                        "itemGradingId=" + itemGradingData.getItemGradingId() + ", " + mediaFilename, 
                        null, true, NotificationService.NOTI_REQUIRED));
            }
        }
    }

    public MediaData getMedia(Long mediaId) {

        try {
            Session session = getCurrentSession();
            MediaData mediaData = session.get(MediaData.class, mediaId);

            if (mediaData == null) {
                log.warn("MediaData with id {} not found", mediaId);
                return null;
            }

            // Only try to read from Content Hosting if this isn't a link and
            // there is no media content in the database
            if (mediaData.getLocation() == null) {
                mediaData.setContentResource(getMediaContentResource(mediaData));
            }
            return mediaData;
        } catch (Exception e) {
            log.warn("Error getting media with id {}: {}", mediaId, e.toString());
            return null;
        }
    }

    public List<MediaData> getMediaArray(final Long itemGradingId) {
        log.debug("*** itemGradingId =" + itemGradingId);
        List<MediaData> a = new ArrayList<>();

        try {
            Session session = getCurrentSession();
            Query<MediaData> q = session.createQuery(
                    "from MediaData m where m.itemGradingData.itemGradingId = :id",
                    MediaData.class);
            q.setParameter("id", itemGradingId);
            List<MediaData> list = q.list();

            for (MediaData mediaData : list) {
                mediaData.setContentResource(getMediaContentResource(mediaData));
                a.add(mediaData);
            }
            log.debug("*** no. of media = {}", a.size());
            return a;
        } catch (Exception e) {
            log.warn("Error getting media array for item {}: {}", itemGradingId, e.toString());
            return new ArrayList<>();
        }
    }

    public List<MediaData> getMediaArray2(final Long itemGradingId) {
        log.debug("*** itemGradingId =" + itemGradingId);
        List<MediaData> a = new ArrayList<>();
        try {
            Session session = getCurrentSession();
            Query<MediaData> q = session.createQuery(
                    "select new MediaData(m.mediaId, m.filename, m.fileSize, m.duration, m.createdDate) " +
                            " from MediaData m where m.itemGradingData.itemGradingId = :id",
                    MediaData.class);
            q.setParameter("id", itemGradingId);
            List<MediaData> list = q.list();

            for (MediaData mediaData : list) {
                mediaData.setContentResource(getMediaContentResource(mediaData));
                a.add(mediaData);
            }
            log.debug("*** no. of media = {}", a.size());
            return a;
        } catch (Exception e) {
            log.warn("Error getting media array2 for item {}: {}", itemGradingId, e.toString());
            return new ArrayList<>();
        }
    }

    public Map<Long, List<ItemGradingData>> getMediaItemGradingHash(final Long assessmentGradingId) {
        log.debug("*** assessmentGradingId = {}", assessmentGradingId);
        Map<Long, List<ItemGradingData>> map = new HashMap<>();

        try {
            Session session = getCurrentSession();
            Query<ItemGradingData> q = session.createQuery(
                    "select i from MediaData m, ItemGradingData i " +
                            "where m.itemGradingData.itemGradingId = i.itemGradingId " +
                            "and i.assessmentGradingId = :id",
                    ItemGradingData.class);
            q.setParameter("id", assessmentGradingId);
            List<ItemGradingData> list = q.list();

            for (ItemGradingData itemGradingData : list) {
                List<ItemGradingData> al = new ArrayList<>();
                al.add(itemGradingData);
                // There might be duplicate. But we just overwrite it with the same itemGradingData
                map.put(itemGradingData.getPublishedItemId(), al);
            }
            log.debug("*** no. of media = {}", map.size());
            return map;
        } catch (Exception e) {
            log.warn("Error getting media item grading hash for assessment {}: {}", assessmentGradingId, e.toString());
            return new HashMap<>();
        }
    }

    public ArrayList getMediaArray(ItemGradingData item) {
        ArrayList<MediaData> a = new ArrayList<>();
        try {
            Session session = getCurrentSession();
            Query<MediaData> q = session.createQuery(
                    "from MediaData m where m.itemGradingData = :id",
                    MediaData.class);
            q.setParameter("id", item);
            List<MediaData> list = q.list();

            for (MediaData mediaData : list) {
                mediaData.setContentResource(getMediaContentResource(mediaData));
                a.add(mediaData);
            }
            log.debug("*** no. of media = {}", a.size());
            return a;
        } catch (Exception e) {
            log.warn("Error getting media array for item: {}", e.toString());
            return new ArrayList<>();
        }
    }

    public List<MediaData> getMediaArray(Long publishedId, final Long publishedItemId, String which) {
        try {
            Map<Long, List<ItemGradingData>> itemScores = getItemScores(publishedId, publishedItemId, which);
            final List<ItemGradingData> list = itemScores.get(publishedItemId);
            log.debug("list size list.size() = " + list.size());

            Session session = getCurrentSession();
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<MediaData> cq = cb.createQuery(MediaData.class);
            Root<MediaData> root = cq.from(MediaData.class);

            /** make list from AssessmentGradingData ids */
            List<Long> itemGradingIdList = list.stream()
                    .map(ItemGradingData::getItemGradingId)
                    .collect(Collectors.toList());

            /** create or disjunctive expression for (in clauses) */
            List<Predicate> inPredicates = new ArrayList<>();
            for (int i = 0; i < itemGradingIdList.size(); i += 50) {
                List<Long> tempList = itemGradingIdList.subList(i, Math.min(i + 50, itemGradingIdList.size()));
                inPredicates.add(root.get("itemGradingData").get("itemGradingId").in(tempList));
            }
            cq.where(cb.or(inPredicates.toArray(new Predicate[0])));

            List<MediaData> hbmList = session.createQuery(cq).getResultList();

            List<MediaData> a = new ArrayList<>();
            for (MediaData mediaData : hbmList) {
                mediaData.setContentResource(getMediaContentResource(mediaData));
                a.add(mediaData);
            }
            return a;

        } catch (Exception e) {
            log.warn(e.toString(), e);
            return new ArrayList<>();
        }
    }

    public List<Long> getMediaConversionBatch() {
        try {
            Session session = getCurrentSession();
            Query<Long> q = session.createQuery(
                    "SELECT id FROM MediaData WHERE dbMedia IS NOT NULL AND location IS NULL",
                    Long.class);
            q.setMaxResults(10);
            return q.list();
        } catch (Exception e) {
            log.warn("Error getting media conversion batch: {}", e.toString()	);
            return new ArrayList<>();
        }
    }

    public boolean markMediaForConversion(final List<Long> mediaIds) {
        try {
            Session session = getCurrentSession();
            Query<?> q = session.createQuery(
                    "UPDATE MediaData SET location = 'CONVERTING' WHERE id in (:ids)");
            q.setParameterList("ids", mediaIds);
            int updatedCount = q.executeUpdate();
            return updatedCount == mediaIds.size();
        } catch (Exception e) {
            log.warn("Error marking media for conversion: {}", e.toString());
            return false;
        }
    }

    public List<Long> getMediaWithDataAndLocation() {
        try {
            Session session = getCurrentSession();
            Query<Long> q = session.createQuery(
                    "SELECT id FROM MediaData WHERE dbMedia IS NOT NULL AND location IS NOT NULL",
                    Long.class);
            return q.list();
        } catch (Exception e) {
            log.warn("Error getting media with data and location: {}", e.toString());
            return new ArrayList<>();
        }
    }

    public List<Long> getMediaInConversion() {
        try {
            Session session = getCurrentSession();
            Query<Long> q = session.createQuery(
                    "SELECT id FROM MediaData WHERE location = 'CONVERTING'",
                    Long.class);
            return q.list();
        } catch (Exception e) {
            log.warn("Error getting media in conversion: {}", e.toString());
            return new ArrayList<>();
        }
    }

    public ItemGradingData getLastItemGradingDataByAgent(final Long publishedItemId, final String agentId) {
        try {
            Session session = getCurrentSession();
            Query<ItemGradingData> q = session.createQuery(
                    "from ItemGradingData i where i.publishedItemId = :id and i.agentId = :agent",
                    ItemGradingData.class);
            q.setParameter("id", publishedItemId);
            q.setParameter("agent", agentId);
            q.setMaxResults(1);
            List<ItemGradingData> itemGradings = q.list();
            
            if (itemGradings.isEmpty()) {
                return null;
            }
            return itemGradings.get(0);
        } catch (Exception e) {
            log.warn("Error getting last item grading data for item {} and agent {}: {}", publishedItemId, agentId, e.toString());
            return null;
        }
    }

    public ItemGradingData getItemGradingData(final Long itemGradingId) {
        try {
            Session session = getCurrentSession();
            Query<ItemGradingData> q = session.createQuery(
                    "from ItemGradingData i where i.itemGradingId = :id",
                    ItemGradingData.class);
            q.setParameter("id", itemGradingId);
            List<ItemGradingData> itemGradings = q.list();
            
            if (itemGradings.isEmpty()) {
                return null;
            }
            return itemGradings.get(0);
        } catch (Exception e) {
            log.warn("Error getting item grading data for item {}: {}", itemGradingId, e.toString());
            return null;
        }
    }

    public ItemGradingData getItemGradingData(final Long assessmentGradingId, final Long publishedItemId) {
        log.debug("****assessmentGradingId={}", assessmentGradingId);
        log.debug("****publishedItemId={}", publishedItemId);

        try {
            Session session = getCurrentSession();
            Query<ItemGradingData> q = session.createQuery(
                    "from ItemGradingData i where i.assessmentGradingId = :gradingid and i.publishedItemId = :itemid",
                    ItemGradingData.class);
            q.setParameter("gradingid", assessmentGradingId);
            q.setParameter("itemid", publishedItemId);
            q.setMaxResults(1);
            List<ItemGradingData> itemGradings = q.list();

            if (itemGradings.isEmpty()) {
                return null;
            }
            return itemGradings.get(0);
        } catch (Exception e) {
            log.warn("Error getting item grading data for assessment {} and item {}: {}", 
                    assessmentGradingId, publishedItemId, e.toString());
            return null;
        }
    }

    public AssessmentGradingData load(Long id) {
        return load(id, true);
    }

public AssessmentGradingData load(Long id, boolean loadGradingAttachment) {
        try {
            Session session = getCurrentSession();
            AssessmentGradingData gdata = session.get(AssessmentGradingData.class, id);

            if (gdata == null) {
                log.warn("AssessmentGradingData with id {} not found", id);
                return null;
            }

            Set<ItemGradingData> itemGradingSet = new HashSet<>();

            // Get (ItemGradingId, ItemGradingData) pair
            Map<Long, ItemGradingData> itemGradingMap = getItemGradingMap(gdata.getAssessmentGradingId());
            if (itemGradingMap.keySet().size() > 0) {
                Collection<ItemGradingData> itemGradingCollection = itemGradingMap.values();

                if (loadGradingAttachment) {
                    // Get (ItemGradingId, ItemGradingAttachment) pair
                    Map<Long, Set<ItemGradingAttachment>> attachmentMap = getItemGradingAttachmentMap(itemGradingMap.keySet());

                    Iterator<ItemGradingData> iter = itemGradingCollection.iterator();
                    while (iter.hasNext()) {
                        ItemGradingData itemGradingData = iter.next();
                        if (attachmentMap.get(itemGradingData.getItemGradingId()) != null) {
                            itemGradingData.setItemGradingAttachmentSet(attachmentMap.get(itemGradingData.getItemGradingId()));
                        } else {
                            itemGradingData.setItemGradingAttachmentSet(new HashSet<>());
                        }
                        itemGradingSet.add(itemGradingData);
                    }
                } else {
                    itemGradingSet.addAll(itemGradingCollection);
                }
            }

            gdata.setItemGradingSet(itemGradingSet);
            return gdata;
        } catch (Exception e) {
            log.warn("Error loading AssessmentGradingData with id {}: {}", id, e.toString());
            return null;
        }
    }

    public ItemGradingData getItemGrading(Long id) {
        try {
            Session session = getCurrentSession();
            return session.load(ItemGradingData.class, id);
        } catch (Exception e) {
            log.warn("Error getting item grading with id {}: {}", id, e.toString());
            return null;
        }
    }

    public AssessmentGradingData getLastSavedAssessmentGradingByAgentId(final Long publishedAssessmentId, final String agentIdString) {
        AssessmentGradingData ag = null;
        // don't pick the assessmentGradingData that is created by instructor entering comments/scores
        try {
            Session session = getCurrentSession();
            Query<AssessmentGradingData> q = session.createQuery(
                    "from AssessmentGradingData a where a.publishedAssessmentId = :id and a.agentId = :agent and a.forGrade = :forgrade and a.status not in (:status1, :status2) order by a.submittedDate desc",
                    AssessmentGradingData.class);
            q.setParameter("id", publishedAssessmentId);
            q.setParameter("agent", agentIdString);
            q.setParameter("forgrade", false);
            q.setParameter("status1", AssessmentGradingData.NO_SUBMISSION);
            q.setParameter("status2", AssessmentGradingData.REMOVED);
            q.setMaxResults(1);
            ag = q.uniqueResult();

            if (ag != null) {
                ag.setItemGradingSet(getItemGradingSet(ag.getAssessmentGradingId()));
            }
            return ag;
        } catch (Exception e) {
            log.warn("Error getting last saved assessment grading by agent for assessment {} and agent {}: {}", 
                    publishedAssessmentId, agentIdString, e.toString());
            return null;
        }
    }

    public AssessmentGradingData getLastSubmittedAssessmentGradingByAgentId(final Long publishedAssessmentId, final String agentIdString, Long assessmentGradingId) {
        AssessmentGradingData ag = null;

        try {
            Session session = getCurrentSession();
            Query<AssessmentGradingData> q = session.createQuery(
                    "from AssessmentGradingData a where a.publishedAssessmentId = :id and a.agentId = :agent and a.forGrade = :forgrade and a.status > :status order by a.submittedDate desc",
                    AssessmentGradingData.class);
            q.setParameter("id", publishedAssessmentId);
            q.setParameter("agent", agentIdString);
            q.setParameter("forgrade", true);
            q.setParameter("status", AssessmentGradingData.REMOVED);
            q.setMaxResults(1);
            
            if (assessmentGradingId == null) {
                ag = q.uniqueResult();
            } else {
                List<AssessmentGradingData> assessmentGradings = q.list();
                for (AssessmentGradingData agd : assessmentGradings) {
                    if (agd.getAssessmentGradingId().compareTo(assessmentGradingId) == 0) {
                        ag = agd;
                        ag.setItemGradingSet(getItemGradingSet(agd.getAssessmentGradingId()));
                        break;
                    }
                }
            }

            if (ag != null) {
                // get AssessmentGradingAttachments
                Map<Long, List<AssessmentGradingAttachment>> map = getAssessmentGradingAttachmentMap(publishedAssessmentId);
                List<AssessmentGradingAttachment> attachments = map.get(ag.getAssessmentGradingId());
                if (attachments != null) {
                    ag.setAssessmentGradingAttachmentList(attachments);
                } else {
                    ag.setAssessmentGradingAttachmentList(new ArrayList<>());
                }
            }

            return ag;
        } catch (Exception e) {
            log.warn("Error getting last submitted assessment grading by agent for assessment {} and agent {}: {}", 
                    publishedAssessmentId, agentIdString, e.toString());
            return null;
        }
    }

    public AssessmentGradingData getLastAssessmentGradingByAgentId(final Long publishedAssessmentId, final String agentIdString) {
        AssessmentGradingData ag = null;

        try {
            Session session = getCurrentSession();
            Query<AssessmentGradingData> q = session.createQuery(
                    "from AssessmentGradingData a where a.publishedAssessmentId = :id and a.agentId = :agent and a.status > :status order by a.submittedDate desc",
                    AssessmentGradingData.class);
            q.setParameter("id", publishedAssessmentId);
            q.setParameter("agent", agentIdString);
            q.setParameter("status", AssessmentGradingData.REMOVED);
            q.setMaxResults(1);
            ag = q.uniqueResult();

            if (ag != null) {
                ag.setItemGradingSet(getItemGradingSet(ag.getAssessmentGradingId()));
            }
            return ag;
        } catch (Exception e) {
            log.warn("Error getting last assessment grading by agent for assessment {} and agent {}: {}", 
                    publishedAssessmentId, agentIdString, e.toString());
            return null;
        }
    }

    public void saveItemGrading(ItemGradingData item) {
        int retryCount = persistenceHelper.getRetryCount();
        while (retryCount > 0) {
            try {
                Session session = getCurrentSession();
                session.merge(item);
                retryCount = 0;
            } catch (Exception e) {
                log.warn("problem saving itemGrading: " + e.toString());
                retryCount = persistenceHelper.retryDeadlock(e, retryCount);
            }
        }
    }

    public boolean saveOrUpdateAssessmentGrading(AssessmentGradingData assessment) {
        int retryCount = persistenceHelper.getRetryCount();
        boolean success = false;
        while (retryCount > 0) {
            try {
                Session session = getCurrentSession();
                session.merge(assessment);
                retryCount = 0;
                success = true;
            } catch (Exception e) {
                log.warn("problem inserting/updating assessmentGrading: {}", e.toString());
                retryCount = persistenceHelper.retryDeadlock(e, retryCount);
            }
        }
        return success;
    }

    public List<Long> getAssessmentGradingIds(final Long publishedItemId) {
    	try {
            Session session = getCurrentSession();
            Query<Long> q = session.createQuery(
                    "select g.assessmentGradingId from ItemGradingData g where g.publishedItemId = :id",
                    Long.class);
            q.setParameter("id", publishedItemId);
            return q.list();
        } catch (Exception e) {
            log.warn("Error getting assessment grading ids for published item {}: {}", publishedItemId, e.toString());
            return new ArrayList<>();
        }
    }

    public AssessmentGradingData getHighestAssessmentGrading(final Long publishedAssessmentId, final String agentId) {
        AssessmentGradingData ag = null;

        try {
            Session session = getCurrentSession();
            Query<AssessmentGradingData> q = session.createQuery(
                    "from AssessmentGradingData a where a.publishedAssessmentId = :id and " +
                            " a.agentId = :agent and a.status > :status order by a.finalScore desc, a.submittedDate desc",
                    AssessmentGradingData.class);
            q.setParameter("id", publishedAssessmentId);
            q.setParameter("agent", agentId);
            q.setParameter("status", AssessmentGradingData.REMOVED);
            q.setMaxResults(1);
            ag = q.uniqueResult();

            if (ag != null) {
                ag.setItemGradingSet(getItemGradingSet(ag.getAssessmentGradingId()));
            }
            return ag;
        } catch (Exception e) {
            log.warn("Error getting highest assessment grading for assessment {} and agent {}: {}", 
                    publishedAssessmentId, agentId, e.toString());
            return null;
        }
    }

    public AssessmentGradingData getHighestSubmittedAssessmentGrading(final Long publishedAssessmentId, final String agentId, Long assessmentGradingId) {
        AssessmentGradingData ag = null;

        try {
            Session session = getCurrentSession();
            Query<AssessmentGradingData> q = session.createQuery(
                    "from AssessmentGradingData a where a.publishedAssessmentId = :id and a.agentId = :agent and " +
                            " a.forGrade = :forgrade and a.status > :status order by a.finalScore desc, a.submittedDate desc",
                    AssessmentGradingData.class);
            q.setParameter("id", publishedAssessmentId);
            q.setParameter("agent", agentId);
            q.setParameter("forgrade", true);
            q.setParameter("status", AssessmentGradingData.REMOVED);
            List<AssessmentGradingData> assessmentGradings = q.list();

            if (assessmentGradingId == null) {
                if (!assessmentGradings.isEmpty()) {
                    ag = assessmentGradings.get(0);
                }
            } else {
                for (AssessmentGradingData agd : assessmentGradings) {
                    if (agd.getAssessmentGradingId().compareTo(assessmentGradingId) == 0) {
                        ag = agd;
                        ag.setItemGradingSet(getItemGradingSet(agd.getAssessmentGradingId()));
                        break;
                    }
                }
            }

            // get AssessmentGradingAttachments
            List<AssessmentGradingAttachment> attachments = new ArrayList<>();
            if (ag != null) {
                Map<Long, List<AssessmentGradingAttachment>> map = getAssessmentGradingAttachmentMap(publishedAssessmentId);
                if (map != null && map.containsKey(ag.getAssessmentGradingId())) {
                    attachments = map.get(ag.getAssessmentGradingId());
                }
                ag.setAssessmentGradingAttachmentList(attachments);
            }
            return ag;
        } catch (Exception e) {
            log.warn("Error getting highest submitted assessment grading for assessment {} and agent {}: {}", 
                    publishedAssessmentId, agentId, e.toString());
            return null;
        }
    }

    public List getLastAssessmentGradingList(final Long publishedAssessmentId) {

        try {
            Session session = getCurrentSession();
            Query<AssessmentGradingData> query = session.createQuery(
                    "from AssessmentGradingData a where a.publishedAssessmentId = :id and a.status > :status order by a.agentId asc, a.submittedDate desc",
                    AssessmentGradingData.class);
            query.setParameter("id", publishedAssessmentId);
            query.setParameter("status", AssessmentGradingData.REMOVED);
            List<AssessmentGradingData> assessmentGradings = query.list();

            return new ArrayList<>(assessmentGradings.stream()
                    .collect(Collectors.toMap(
                            AssessmentGradingData::getAgentId, 
                            p -> p, 
                            (p, q) -> p,
                            LinkedHashMap::new))
                    .values());
        } catch (Exception e) {
            log.warn("Error getting last assessment grading list for assessment {}: {}", publishedAssessmentId, e.toString());
            return new ArrayList<>();
        }
    }

    public List getLastSubmittedAssessmentGradingList(final Long publishedAssessmentId) {

    	try {
            Session session = getCurrentSession();
            Query<AssessmentGradingData> query = session.createQuery(
                    "select a from AssessmentGradingData a left join fetch a.assessmentGradingAttachmentSet " +
                            "where a.publishedAssessmentId = :id and a.forGrade = :forgrade and a.status > :status order by a.agentId asc, a.submittedDate desc",
                    AssessmentGradingData.class);
            query.setParameter("id", publishedAssessmentId);
            query.setParameter("forgrade", true);
            query.setParameter("status", AssessmentGradingData.REMOVED);
            List<AssessmentGradingData> assessmentGradings = query.list();

            return new ArrayList<>(assessmentGradings.stream()
                    .collect(Collectors.toMap(
                            AssessmentGradingData::getAgentId, 
                            p -> p, 
                            (p, q) -> p,
                            LinkedHashMap::new))
                    .values());
        } catch (Exception e) {
            log.warn("Error getting last submitted assessment grading list for assessment {}: {}", 
                    publishedAssessmentId, e.toString());
            return new ArrayList<>();
        }
    }

    public List getLastSubmittedOrGradedAssessmentGradingList(final Long publishedAssessmentId) {
        try {
            Session session = getCurrentSession();
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<AssessmentGradingData> cq = cb.createQuery(AssessmentGradingData.class);
            Root<AssessmentGradingData> root = cq.from(AssessmentGradingData.class);

            Predicate forGradeTrue = cb.equal(root.get("forGrade"), true);
            Predicate forGradeFalse = cb.equal(root.get("forGrade"), false);
            Predicate noSubmission = cb.equal(root.get("status"), AssessmentGradingData.NO_SUBMISSION);

            cq.where(
                cb.equal(root.get("publishedAssessmentId"), publishedAssessmentId),
                cb.or(
                    forGradeTrue,
                    cb.and(forGradeFalse, noSubmission)
                )
            );

            cq.orderBy(
                cb.asc(root.get("agentId")),
                cb.desc(root.get("submittedDate"))
            );

            List<AssessmentGradingData> assessmentGradings = session.createQuery(cq).getResultList();

            return new ArrayList<>(assessmentGradings.stream()
                    .collect(Collectors.toMap(
                            AssessmentGradingData::getAgentId, 
                            p -> p, 
                            (p, q) -> p,
                            LinkedHashMap::new))
                    .values());
        } catch (Exception e) {
            log.warn("Error getting last submitted or graded assessment grading list for assessment {}: {}", 
                    publishedAssessmentId, e.toString());
            return new ArrayList<>();
        }
    }

    public List<AssessmentGradingData> getHighestAssessmentGradingList(final Long publishedAssessmentId) {

        try {
            Session session = getCurrentSession();
            Query<AssessmentGradingData> query = session.createQuery(
                    "from AssessmentGradingData a where a.publishedAssessmentId = :id and a.status > :status order by a.agentId asc, a.finalScore desc",
                    AssessmentGradingData.class);
            query.setParameter("id", publishedAssessmentId);
            query.setParameter("status", AssessmentGradingData.REMOVED);
            List<AssessmentGradingData> assessmentGradings = query.list();

            return new ArrayList<>(assessmentGradings.stream()
                    .collect(Collectors.toMap(
                            AssessmentGradingData::getAgentId, 
                            p -> p, 
                            (p, q) -> p,
                            LinkedHashMap::new))
                    .values());
        } catch (Exception e) {
            log.warn("Error getting highest assessment grading list for assessment {}: {}", publishedAssessmentId, e.toString());
            return new ArrayList<>();
        }
    }


    public List<AssessmentGradingData> getHighestSubmittedOrGradedAssessmentGradingList(final Long publishedAssessmentId) {
        try {
            Session session = getCurrentSession();
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<AssessmentGradingData> cq = cb.createQuery(AssessmentGradingData.class);
            Root<AssessmentGradingData> root = cq.from(AssessmentGradingData.class);

            Predicate forGradeTrue = cb.equal(root.get("forGrade"), true);
            Predicate forGradeFalse = cb.equal(root.get("forGrade"), false);
            Predicate noSubmission = cb.equal(root.get("status"), AssessmentGradingData.NO_SUBMISSION);

            cq.where(
                cb.equal(root.get("publishedAssessmentId"), publishedAssessmentId),
                cb.gt(root.get("status"), AssessmentGradingData.REMOVED),
                cb.or(
                    forGradeTrue,
                    cb.and(forGradeFalse, noSubmission)
                )
            );

            cq.orderBy(
                cb.asc(root.get("agentId")),
                cb.desc(root.get("finalScore"))
            );

            List<AssessmentGradingData> assessmentGradings = session.createQuery(cq).getResultList();

            return new ArrayList<>(assessmentGradings.stream()
                    .collect(Collectors.toMap(
                            AssessmentGradingData::getAgentId, 
                            p -> p, 
                            (p, q) -> p,
                            LinkedHashMap::new))
                    .values());
        } catch (Exception e) {
            log.warn("Error getting highest submitted or graded assessment grading list for assessment {}: {}", 
                    publishedAssessmentId, e.toString());
            return new ArrayList<>();
        }
    }

    // build a Hashmap (Long publishedItemId, ArrayList assessmentGradingIds)
    // containing the item submission of the last AssessmentGrading
    // (regardless of users who submitted it) of a given published assessment
    public Map<Long, List<Long>> getLastAssessmentGradingByPublishedItem(final Long publishedAssessmentId) {
        Map<Long, List<Long>> h = new HashMap<>();

        try {
            Session session = getCurrentSession();
            Query<AssessmentGradingData> q = session.createQuery(
                    "select new AssessmentGradingData(" +
                            " a.assessmentGradingId, p.itemId, " +
                            " a.agentId, a.finalScore, a.submittedDate) " +
                            " from ItemGradingData i, AssessmentGradingData a," +
                            " PublishedItemData p where " +
                            " i.assessmentGradingId = a.assessmentGradingId and i.publishedItemId = p.itemId and " +
                            " a.publishedAssessmentId = :id and a.status > :status " +
                            " order by a.agentId asc, a.submittedDate desc",
                    AssessmentGradingData.class);
            q.setParameter("id", publishedAssessmentId);
            q.setParameter("status", AssessmentGradingData.REMOVED);
            List<AssessmentGradingData> assessmentGradings = q.list();

            String currentAgent = "";
            Date submittedDate = null;
            for (int i = 0; i < assessmentGradings.size(); i++) {
                AssessmentGradingData g = assessmentGradings.get(i);
                Long itemId = g.getPublishedItemId();
                Long gradingId = g.getAssessmentGradingId();
                log.debug("**** itemId=" + itemId + ", gradingId=" + gradingId + ", agentId=" + g.getAgentId() + ", score=" + g.getFinalScore());
                if (i == 0) {
                    currentAgent = g.getAgentId();
                    submittedDate = g.getSubmittedDate();
                }
                if (currentAgent.equals(g.getAgentId())
                        && ((submittedDate == null && g.getSubmittedDate() == null)
                        || (submittedDate != null && submittedDate.equals(g.getSubmittedDate())))) {
                    List<Long> o = h.get(itemId);
                    if (o != null) {
                        o.add(gradingId);
                    } else {
                        List<Long> gradingIds = new ArrayList<>();
                        gradingIds.add(gradingId);
                        h.put(itemId, gradingIds);
                    }
                }
                if (!currentAgent.equals(g.getAgentId())) {
                    currentAgent = g.getAgentId();
                    submittedDate = g.getSubmittedDate();
                }
            }
            return h;
        } catch (Exception e) {
            log.warn("Error getting last assessment grading by published item for assessment {}: {}", 
                    publishedAssessmentId, e.toString());
            return new HashMap<>();
        }
    }

    // build a Hashmap (Long publishedItemId, ArrayList assessmentGradingIds)
    // containing the item submission of the highest AssessmentGrading
    // (regardless of users who submitted it) of a given published assessment
    public Map<Long, List<Long>> getHighestAssessmentGradingByPublishedItem(final Long publishedAssessmentId) {
        Map<Long, List<Long>> h = new HashMap<>();

        try {
            Session session = getCurrentSession();
            Query<AssessmentGradingData> q = session.createQuery(
                    "select new AssessmentGradingData(" +
                            " a.assessmentGradingId, p.itemId, " +
                            " a.agentId, a.finalScore, a.submittedDate) " +
                            " from ItemGradingData i, AssessmentGradingData a, " +
                            " PublishedItemData p where " +
                            " i.assessmentGradingId = a.assessmentGradingId and i.publishedItemId = p.itemId and " +
                            " a.publishedAssessmentId = :id and a.status > :status " +
                            " order by a.agentId asc, a.finalScore desc",
                    AssessmentGradingData.class);
            q.setParameter("id", publishedAssessmentId);
            q.setParameter("status", AssessmentGradingData.REMOVED);
            List<AssessmentGradingData> assessmentGradings = q.list();

            String currentAgent = "";
            Double finalScore = null;
            for (int i = 0; i < assessmentGradings.size(); i++) {
                AssessmentGradingData g = assessmentGradings.get(i);
                Long itemId = g.getPublishedItemId();
                Long gradingId = g.getAssessmentGradingId();
                log.debug("**** itemId=" + itemId + ", gradingId=" + gradingId + ", agentId=" + g.getAgentId() + ", score=" + g.getFinalScore());
                if (i == 0) {
                    currentAgent = g.getAgentId();
                    finalScore = g.getFinalScore();
                }
                if (currentAgent.equals(g.getAgentId())
                        && ((finalScore == null && g.getFinalScore() == null)
                        || (finalScore != null && finalScore.equals(g.getFinalScore())))) {
                    List<Long> o = h.get(itemId);
                    if (o != null) {
                        o.add(gradingId);
                    } else {
                        List<Long> gradingIds = new ArrayList<>();
                        gradingIds.add(gradingId);
                        h.put(itemId, gradingIds);
                    }
                }
                if (!currentAgent.equals(g.getAgentId())) {
                    currentAgent = g.getAgentId();
                    finalScore = g.getFinalScore();
                }
            }
            return h;
        } catch (Exception e) {
            log.warn("Error getting highest assessment grading by published item for assessment {}: {}", 
                    publishedAssessmentId, e.toString());
            return new HashMap<>();
        }
    }

    public Set<ItemGradingData> getItemGradingSet(final Long assessmentGradingId) {

        try {
            Session session = getCurrentSession();
            Query<ItemGradingData> q = session.createQuery(
                    "from ItemGradingData i where i.assessmentGradingId = :id",
                    ItemGradingData.class);
            q.setParameter("id", assessmentGradingId);
            List<ItemGradingData> itemGradings = q.list();

            return new HashSet<>(itemGradings);
        } catch (Exception e) {
            log.warn("Error getting item grading set for assessment {}: {}", assessmentGradingId, e.toString());
            return new HashSet<>();
        }
    }

    public Map<Long, ItemGradingData> getItemGradingMap(final Long assessmentGradingId) {

        try {
            Session session = getCurrentSession();
            Query<ItemGradingData> q = session.createQuery(
                    "from ItemGradingData i where i.assessmentGradingId = :id",
                    ItemGradingData.class);
            q.setParameter("id", assessmentGradingId);
            List<ItemGradingData> itemGradingList = q.list();

            return itemGradingList.stream()
                    .collect(Collectors.toMap(
                            ItemGradingData::getItemGradingId, 
                            p -> p,
                            (existing, replacement) -> existing));
        } catch (Exception e) {
            log.warn("Error getting item grading map for assessment {}: {}", assessmentGradingId, e.toString());
            return new HashMap<>();
        }
    }

    public Map<Long, AssessmentGradingData> getAssessmentGradingByItemGradingId(final Long publishedAssessmentId) {
        try {
            Map<Long, AssessmentGradingData> submissionDataMap = getAllSubmissions(publishedAssessmentId.toString()).stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(AssessmentGradingData::getAssessmentGradingId, a -> a));

            Session session = getCurrentSession();
            Query<ItemGradingData> q = session.createQuery(
                    "select new ItemGradingData(i.itemGradingId, a.assessmentGradingId) " +
                            " from ItemGradingData i, AssessmentGradingData a " +
                            " where i.assessmentGradingId = a.assessmentGradingId " +
                            " and a.publishedAssessmentId = :id " +
                            " and a.forGrade = :forgrade and a.status > :status",
                    ItemGradingData.class);
            q.setParameter("id", publishedAssessmentId);
            q.setParameter("forgrade", true);
            q.setParameter("status", AssessmentGradingData.REMOVED);
            List<ItemGradingData> l = q.list();

            return l.stream()
                    .filter(i -> Objects.nonNull(submissionDataMap.get(i.getAssessmentGradingId())))
                    .collect(Collectors.toMap(
                            ItemGradingData::getItemGradingId, 
                            g -> submissionDataMap.get(g.getAssessmentGradingId())));
        } catch (Exception e) {
            log.warn("Error getting assessment grading by item grading id for assessment {}: {}", 
                    publishedAssessmentId, e.toString());
            return new HashMap<>();
        }
    }

    public void deleteAll(Collection c) {
        int retryCount = persistenceHelper.getRetryCount();
        while (retryCount > 0) {
            try {
                Session session = getCurrentSession();
                c.stream()
                    .filter(Objects::nonNull)
                    .forEach(entity -> {
                        Object attached = session.merge(entity);
                        session.remove(attached);
                    });
                retryCount = 0;
            } catch (Exception e) {
                log.warn("problem deleting entities: {}", e.toString());
                retryCount = persistenceHelper.retryDeadlock(e, retryCount);
            }
        }
    }

    public void saveOrUpdateAll(Collection<ItemGradingData> c) {
        c.removeAll(Collections.singleton(null));
        if (c.isEmpty()) {
            return;
        }

        int retryCount = persistenceHelper.getRetryCount();
        while (retryCount > 0) {
            try {
                Session session = getCurrentSession();
                for (ItemGradingData itemGradingData : c) {
                    if (itemGradingData != null) {
                        session.merge(itemGradingData);
                    }
                }
                retryCount = 0;
            } catch (Exception e) {
                log.warn("problem inserting assessmentGrading: " + e.toString());
                retryCount = persistenceHelper.retryDeadlock(e, retryCount);
            }
        }
    }

    public PublishedAssessmentIfc getPublishedAssessmentByAssessmentGradingId(final Long assessmentGradingId) {

        try {
            Session session = getCurrentSession();
            Query<PublishedAssessmentData> q = session.createQuery(
                    "select p from PublishedAssessmentData p, AssessmentGradingData a " +
                            "where a.publishedAssessmentId = p.publishedAssessmentId and a.assessmentGradingId = :id",
                    PublishedAssessmentData.class);
            q.setParameter("id", assessmentGradingId);
            q.setMaxResults(1);
            List<PublishedAssessmentData> pubList = q.list();

            if (pubList != null && !pubList.isEmpty()) {
                return pubList.get(0);
            }
            return null;
        } catch (Exception e) {
            log.warn("Error getting published assessment by assessment grading id {}: {}", assessmentGradingId, e.toString());
            return null;
        }
    }

    public PublishedAssessmentIfc getPublishedAssessmentByPublishedItemId(final Long publishedItemId) {

        try {
            Session session = getCurrentSession();
            Query<PublishedAssessmentData> q = session.createQuery(
                    "select p from PublishedAssessmentData p, PublishedItemData i " +
                            "where p.publishedAssessmentId = i.section.assessment.publishedAssessmentId and i.itemId = :id",
                    PublishedAssessmentData.class);
            q.setParameter("id", publishedItemId);
            q.setMaxResults(1);
            List<PublishedAssessmentData> pubList = q.list();

            if (pubList != null && !pubList.isEmpty()) {
                return pubList.get(0);
            }
            return null;
        } catch (Exception e) {
            log.warn("Error getting published assessment by published item id {}: {}", publishedItemId, e.toString());
            return null;
        }
    }

    public List<Integer> getLastItemGradingDataPosition(final Long assessmentGradingId, final String agentId) {
        List<Integer> position = new ArrayList<>();
        try {
            Session session = getCurrentSession();
            Query<Integer> q = session.createQuery(
                    "select s.sequence " +
                            " from ItemGradingData i, PublishedItemData pi, PublishedSectionData s " +
                            " where i.agentId = :agent and i.assessmentGradingId = :id " +
                            " and pi.itemId = i.publishedItemId " +
                            " and pi.section.id = s.id " +
                            " group by i.publishedItemId, s.sequence, pi.sequence " +
                            " order by s.sequence desc , pi.sequence desc",
                    Integer.class);
            q.setParameter("agent", agentId);
            q.setParameter("id", assessmentGradingId);
            List<Integer> list = q.list();

            if (list.isEmpty()) {
                position.add(0);
                position.add(0);
            } else {
                Integer sequence = list.get(0);
                Integer nextSequence;
                int count = 1;
                for (int i = 1; i < list.size(); i++) {
                    log.debug("i = {}", i);
                    nextSequence = list.get(i);
                    if (sequence.equals(nextSequence)) {
                        log.debug("equal");
                        count++;
                    } else {
                        break;
                    }
                }
                log.debug("sequence = " + sequence);
                log.debug("count = " + count);
                position.add(sequence);
                position.add(count);
            }
            return position;
        } catch (Exception e) {
            log.warn(e.getMessage(), e.toString());
            position.add(0);
            position.add(0);
            return position;
        }
    }

    public List<Long> getPublishedItemIds(final Long assessmentGradingId) {

        try {
            Session session = getCurrentSession();
            Query<Long> q = session.createQuery(
                    "select i.publishedItemId from ItemGradingData i where i.assessmentGradingId = :id",
                    Long.class);
            q.setParameter("id", assessmentGradingId);
            return q.list();
        } catch (Exception e) {
            log.warn("Error getting published item ids for assessment {}: {}", assessmentGradingId, e.toString());
            return new ArrayList<>();
        }
    }

    public List<Long> getItemGradingIds(final Long assessmentGradingId) {
        try {
            Session session = getCurrentSession();
            Query<Long> q = session.createQuery(
                    "select i.itemGradingId from ItemGradingData i where i.assessmentGradingId = :id",
                    Long.class);
            q.setParameter("id", assessmentGradingId);
            return q.list();
        } catch (Exception e) {
            log.warn("Error getting item grading ids for assessment {}: {}", assessmentGradingId, e.toString());
            return new ArrayList<>();
        }
    }

    public Set<PublishedItemData> getItemSet(final Long publishedAssessmentId, final Long sectionId) {

        try {
            Session session = getCurrentSession();

            Query<Long> q1 = session.createQuery(
                    "select distinct p.itemId " +
                            "from PublishedItemData p, AssessmentGradingData a, ItemGradingData i " +
                            "where a.publishedAssessmentId = :id and a.forGrade = :forgrade and p.section.id = :sectionid " +
                            "and i.assessmentGradingId = a.assessmentGradingId " +
                            "and p.itemId = i.publishedItemId and a.status > :status",
                    Long.class);
            q1.setParameter("id", publishedAssessmentId);
            q1.setParameter("forgrade", true);
            q1.setParameter("sectionid", sectionId);
            q1.setParameter("status", AssessmentGradingData.REMOVED);
            List<Long> itemIds = q1.list();

            if (itemIds.isEmpty()) {
                return new HashSet<>();
            }

            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<PublishedItemData> cq = cb.createQuery(PublishedItemData.class);
            Root<PublishedItemData> root = cq.from(PublishedItemData.class);
            
            if (itemIds.size() > 1000) {
                List<Predicate> inPredicates = new ArrayList<>();
                for (int i = 0; i < itemIds.size(); i += 1000) {
                    List<Long> chunk = itemIds.subList(i, Math.min(i + 1000, itemIds.size()));
                    inPredicates.add(root.get("itemId").in(chunk));
                }
                cq.where(cb.or(inPredicates.toArray(new Predicate[0])));
            } else {
                cq.where(root.get("itemId").in(itemIds));
            }

            List<PublishedItemData> publishedItems = session.createQuery(cq).getResultList();
            return new HashSet<>(publishedItems);
            
        } catch (Exception e) {
            log.warn("Error getting item set for assessment {} and section {}: {}", 
                    publishedAssessmentId, sectionId, e.toString());
            return new HashSet<>();
        }
    }

    public Long getTypeId(final Long itemGradingId) {
        Long typeId = Long.valueOf(-1);

        try {
            Session session = getCurrentSession();
            Query<Long> q = session.createQuery(
                    "select p.typeId " +
                            "from PublishedItemData p, ItemGradingData i " +
                            "where i.itemGradingId = :id " +
                            "and p.itemId = i.publishedItemId",
                    Long.class);
            q.setParameter("id", itemGradingId);
            q.setMaxResults(1);
            List<Long> typeIds = q.list();
            
            if (typeIds != null && !typeIds.isEmpty()) {
                typeId = typeIds.get(0);
                log.debug("typeId = {}", typeId);
                return typeId;
            }
            return typeId;
        } catch (Exception e) {
            log.warn("Error getting typeId for itemGradingId {}: {}", itemGradingId, e.toString());
            return typeId;
        }
    }

    public List<AssessmentGradingData> getAllAssessmentGradingByAgentId(final Long publishedAssessmentId, final String agentIdString) {

       try {
            Session session = getCurrentSession();
            Query<AssessmentGradingData> q = session.createQuery(
                    "from AssessmentGradingData a where a.publishedAssessmentId = :id and a.agentId = :agent and a.forGrade = :forgrade and a.status > :status order by a.submittedDate desc",
                    AssessmentGradingData.class);
            q.setParameter("id", publishedAssessmentId);
            q.setParameter("agent", agentIdString);
            q.setParameter("forgrade", true);
            q.setParameter("status", AssessmentGradingData.REMOVED);
            return q.list();
        } catch (Exception e) {
            log.warn("Error getting assessment grading by agent for assessment {} and agent {}: {}", 
                    publishedAssessmentId, agentIdString, e.toString());
            return new ArrayList<>();
        }
    }

    public List<ItemGradingData> getAllItemGradingDataForItemInGrading(final Long assesmentGradingId, final Long publishedItemId) {
        if (assesmentGradingId == null) {
            throw new IllegalArgumentException("assesmentGradingId cant' be null");
        }

        if (publishedItemId == null) {
            throw new IllegalArgumentException("publishedItemId cant' be null");
        }

        try {
            Session session = getCurrentSession();
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<ItemGradingData> cq = cb.createQuery(ItemGradingData.class);
            Root<ItemGradingData> root = cq.from(ItemGradingData.class);
            cq.where(
                cb.equal(root.get("assessmentGradingId"), assesmentGradingId),
                cb.equal(root.get("publishedItemId"), publishedItemId)
            );
            return session.createQuery(cq).getResultList();
        } catch (Exception e) {
            log.warn("Error getting item grading data for assessment {} and item {}: {}", 
                    assesmentGradingId, publishedItemId, e.toString());
            return new ArrayList<>();
        }
    }

    public Map<Long, Map<String, Integer>> getSiteSubmissionCountHash(final String siteId) {
        Map<Long, Map<String, Integer>> siteSubmissionCountHash = new HashMap<>();
        try {
            Session session = getCurrentSession();
            Query<Object[]> q = session.createQuery(
                    "select a.publishedAssessmentId, a.agentId, count(*) " +
                            "from AssessmentGradingData a, AuthorizationData au  " +
                            "where a.forGrade = :forgrade and au.functionId = :fid and au.agentIdString = :agent and a.publishedAssessmentId = au.qualifierId and a.status > :status " +
                            "group by a.publishedAssessmentId, a.agentId " +
                            "order by a.publishedAssessmentId, a.agentId",
                    Object[].class);
            q.setParameter("forgrade", true);
            q.setParameter("fid", "OWN_PUBLISHED_ASSESSMENT");
            q.setParameter("agent", siteId);
            q.setParameter("status", AssessmentGradingData.REMOVED);
            q.setCacheable(true);
            List<Object[]> countList = q.list();
            
            Map<String, Integer> numberSubmissionPerStudentHash = new HashMap<>();
            Long lastPublishedAssessmentId = -1L;

            for (Object[] o : countList) {
                Long publishedAssessmentId = (Long) o[0];

                if (lastPublishedAssessmentId.equals(publishedAssessmentId)) {
                    numberSubmissionPerStudentHash.put((String) o[1], ((Long) o[2]).intValue());
                } else {
                    numberSubmissionPerStudentHash = new HashMap<>();
                    numberSubmissionPerStudentHash.put((String) o[1], ((Long) o[2]).intValue());
                    siteSubmissionCountHash.put(publishedAssessmentId, numberSubmissionPerStudentHash);
                    lastPublishedAssessmentId = publishedAssessmentId;
                }
            }

            return siteSubmissionCountHash;
        } catch (Exception e) {
            log.warn("Error getting site submission count hash for site {}: {}", siteId, e.toString());
            return new HashMap<>();
        }
    }

    public Map<Long, Map<String, Long>> getSiteInProgressCountHash(final String siteId) {
        Map<Long, Map<String, Long>> siteInProgressCountHash = new HashMap<>();
        try {
            Session session = getCurrentSession();
            Query<Object[]> q = session.createQuery(
                    "select a.publishedAssessmentId, a.agentId, count(*) " +
                            "from AssessmentGradingData a, AuthorizationData au  " +
                            "where a.forGrade = :forgrade and au.functionId = :fid and au.agentIdString = :agent " +
                            "and a.publishedAssessmentId = au.qualifierId and (a.status = :status1 or a.status = :status2) " +
                            "group by a.publishedAssessmentId, a.agentId " +
                            "order by a.publishedAssessmentId, a.agentId",
                    Object[].class);
            q.setParameter("forgrade", false);
            q.setParameter("fid", "OWN_PUBLISHED_ASSESSMENT");
            q.setParameter("agent", siteId);
            q.setParameter("status1", AssessmentGradingData.IN_PROGRESS);
            q.setParameter("status2", AssessmentGradingData.ASSESSMENT_UPDATED);
            q.setCacheable(true);
            List<Object[]> countList = q.list();
            
            Map<String, Long> numberInProgressPerStudentHash = new HashMap<>();
            Long lastPublishedAssessmentId = -1L;
            for (Object[] o : countList) {
                Long publishedAssessmentId = (Long) o[0];

                if (lastPublishedAssessmentId.equals(publishedAssessmentId)) {
                    numberInProgressPerStudentHash.put((String) o[1], (Long) o[2]);
                } else {
                    numberInProgressPerStudentHash = new HashMap<>();
                    numberInProgressPerStudentHash.put((String) o[1], (Long) o[2]);
                    siteInProgressCountHash.put(publishedAssessmentId, numberInProgressPerStudentHash);
                    lastPublishedAssessmentId = publishedAssessmentId;
                }
            }

            return siteInProgressCountHash;
        } catch (Exception e) {
            log.warn("Error getting site in-progress count hash for site {}: {}", siteId, e.toString());
            return new HashMap<>();
        }
    }

    public int getActualNumberRetake(final Long publishedAssessmentId, final String agentIdString) {

        try {
            Session session = getCurrentSession();
            Query<Long> q = session.createQuery(
                    "select count(*) from AssessmentGradingData a, StudentGradingSummaryData s " +
                            " where a.publishedAssessmentId = :id and a.agentId = :agent and a.forGrade = :forgrade " +
                            " and a.publishedAssessmentId = s.publishedAssessmentId and a.agentId = s.agentId " +
                            " and a.submittedDate > s.createdDate and a.status > :status",
                    Long.class);
            q.setParameter("id", publishedAssessmentId);
            q.setParameter("agent", agentIdString);
            q.setParameter("forgrade", true);
            q.setParameter("status", AssessmentGradingData.REMOVED);
            Long count = q.uniqueResult();
            
            return count != null ? Math.toIntExact(count) : 0;
        } catch (Exception e) {
            log.warn("Error getting actual number retake for assessment {} and agent {}: {}", 
                    publishedAssessmentId, agentIdString, e.toString());
            return 0;
        }
    }

    public Map<Long, Map<String, Long>> getSiteActualNumberRetakeHash(final String siteId) {
        Map<Long, Map<String, Long>> actualNumberRetakeHash = new HashMap<>();
        try {
            Session session = getCurrentSession();
            Query<Object[]> q = session.createQuery(
                    "select a.publishedAssessmentId, a.agentId, count(*) " +
                            " from AssessmentGradingData a, StudentGradingSummaryData s, AuthorizationData au, PublishedAssessmentData p " +
                            " where a.forGrade = :forgrade and au.functionId = :fid and au.agentIdString = :agent and a.publishedAssessmentId = au.qualifierId" +
                            " and a.publishedAssessmentId = s.publishedAssessmentId and a.agentId = s.agentId " +
                            " and a.submittedDate > s.createdDate" +
                            " and a.publishedAssessmentId = p.publishedAssessmentId" +
                            " and p.status != 2 and a.status > :astatus" +
                            " group by a.publishedAssessmentId, a.agentId" +
                            " order by a.publishedAssessmentId",
                    Object[].class);
            q.setParameter("forgrade", true);
            q.setParameter("fid", "OWN_PUBLISHED_ASSESSMENT");
            q.setParameter("astatus", AssessmentGradingData.REMOVED);
            q.setParameter("agent", siteId);
            List<Object[]> countList = q.list();
            
            Map<String, Long> actualNumberRetakePerStudentHash = new HashMap<>();
            Long lastPublishedAssessmentId = -1L;
            for (Object[] o : countList) {
                Long publishedAssessmentId = (Long) o[0];

                if (lastPublishedAssessmentId.equals(publishedAssessmentId)) {
                    actualNumberRetakePerStudentHash.put((String) o[1], (Long) o[2]);
                } else {
                    actualNumberRetakePerStudentHash = new HashMap<>();
                    actualNumberRetakePerStudentHash.put((String) o[1], (Long) o[2]);
                    actualNumberRetakeHash.put(publishedAssessmentId, actualNumberRetakePerStudentHash);
                    lastPublishedAssessmentId = publishedAssessmentId;
                }
            }

            return actualNumberRetakeHash;
        } catch (Exception e) {
            log.warn("Error getting site actual number retake hash for site {}: {}", siteId, e.toString());
            return new HashMap<>();
        }
    }

    public Map<Long, Integer> getActualNumberRetakeHash(final String agentIdString) {
        Map<Long, Integer> actualNumberRetakeHash = new HashMap<>();
        try {
            Session session = getCurrentSession();
            Query<Object[]> q = session.createQuery(
                    "select a.publishedAssessmentId, count(*) from AssessmentGradingData a, StudentGradingSummaryData s " +
                            " where a.agentId = :agent and a.forGrade = :forgrade " +
                            " and a.publishedAssessmentId = s.publishedAssessmentId and a.agentId = s.agentId " +
                            " and a.submittedDate > s.createdDate and a.status > :status" +
                            " group by a.publishedAssessmentId",
                    Object[].class);
            q.setParameter("agent", agentIdString);
            q.setParameter("forgrade", true);
            q.setParameter("status", AssessmentGradingData.REMOVED);
            List<Object[]> countList = q.list();
            
            for (Object[] o : countList) {
                actualNumberRetakeHash.put((Long) o[0], ((Number) o[1]).intValue());
            }
            return actualNumberRetakeHash;
        } catch (Exception e) {
            log.warn("Error getting actual number retake hash for agent {}: {}", agentIdString, e.toString());
            return new HashMap<>();
        }
    }

    public List<StudentGradingSummaryData> getStudentGradingSummaryData(final Long publishedAssessmentId, final String agentIdString) {
        try {
            Session session = getCurrentSession();
            Query<StudentGradingSummaryData> q = session.createQuery(
                    "select s " +
                            "from StudentGradingSummaryData s " +
                            "where s.publishedAssessmentId = :id and s.agentId = :agent",
                    StudentGradingSummaryData.class);
            q.setParameter("id", publishedAssessmentId);
            q.setParameter("agent", agentIdString);
            return q.list();
        } catch (Exception e) {
            log.warn("Error getting student grading summary data for assessment {} and agent {}: {}", 
                    publishedAssessmentId, agentIdString, e.toString());
            return new ArrayList<>();
        }
    }

    public int getNumberRetake(final Long publishedAssessmentId, final String agentIdString) {
        try {
            Session session = getCurrentSession();
            Query<Integer> q = session.createQuery(
                    "select s.numberRetake " +
                            "from StudentGradingSummaryData s " +
                            "where s.publishedAssessmentId = :id and s.agentId = :agent",
                    Integer.class);
            q.setParameter("id", publishedAssessmentId);
            q.setParameter("agent", agentIdString);
            q.setMaxResults(1);
            Integer result = q.uniqueResult();
            
            return result != null ? result : 0;
        } catch (Exception e) {
            log.warn("Error getting number retake for assessment {} and agent {}: {}", 
                    publishedAssessmentId, agentIdString, e.toString());
            return 0;
        }
    }

    public Map<Long, StudentGradingSummaryData> getNumberRetakeHash(final String agentIdString) {
        try {
            Session session = getCurrentSession();
            Query<StudentGradingSummaryData> q = session.createQuery(
                    "select s " +
                            "from StudentGradingSummaryData s " +
                            "where s.agentId = :agent",
                    StudentGradingSummaryData.class);
            q.setParameter("agent", agentIdString);
            List<StudentGradingSummaryData> numberRetakeList = q.list();
            
            return numberRetakeList.stream()
                    .collect(Collectors.toMap(
                            StudentGradingSummaryData::getPublishedAssessmentId,
                            Function.identity(),
                            (oldValue, newValue) -> newValue,
                            HashMap::new
                    ));
        } catch (Exception e) {
            log.warn("Error getting number retake hash for agent {}: {}", agentIdString, e.toString());
            return new HashMap<>();
        }
    }

    public Map<Long, Map<String, Integer>> getSiteNumberRetakeHash(final String siteId) {
        Map<Long, Map<String, Integer>> siteNumberRetakeHash = new HashMap<>();
        try {
            Session session = getCurrentSession();
            Query<StudentGradingSummaryData> q = session.createQuery(
                    "select s " +
                            "from StudentGradingSummaryData s, AuthorizationData au " +
                            "where au.functionId = :fid and au.agentIdString = :agent " +
                            "and s.publishedAssessmentId = au.qualifierId " +
                            "order by s.publishedAssessmentId, s.agentId",
                    StudentGradingSummaryData.class);
            q.setParameter("fid", "OWN_PUBLISHED_ASSESSMENT");
            q.setParameter("agent", siteId);
            List<StudentGradingSummaryData> countList = q.list();

            Long lastPublishedAssessmentId = -1L;
            Map<String, Integer> numberRetakePerStudentHash = null;
            for (StudentGradingSummaryData s : countList) {
                Long publishedAssessmentId = s.getPublishedAssessmentId();

                if (lastPublishedAssessmentId.equals(publishedAssessmentId)) {
                    numberRetakePerStudentHash.put(s.getAgentId(), s.getNumberRetake());
                } else {
                    numberRetakePerStudentHash = new HashMap<>();
                    numberRetakePerStudentHash.put(s.getAgentId(), s.getNumberRetake());
                    siteNumberRetakeHash.put(publishedAssessmentId, numberRetakePerStudentHash);
                    lastPublishedAssessmentId = publishedAssessmentId;
                }
            }

            return siteNumberRetakeHash;
        } catch (Exception e) {
            log.warn("Error getting site number retake hash for site {}: {}", siteId, e.toString());
            return new HashMap<>();
        }
    }

    public void saveStudentGradingSummaryData(StudentGradingSummaryIfc studentGradingSummaryData) {
        int retryCount = persistenceHelper.getRetryCount();
        while (retryCount > 0) {
            try {
                Session session = getCurrentSession();
                session.merge(studentGradingSummaryData);
                retryCount = 0;
            } catch (Exception e) {
                log.warn("problem saving studentGradingSummaryData: " + e.toString());
                retryCount = persistenceHelper.retryDeadlock(e, retryCount);
            }
        }
    }

    public int getLateSubmissionsNumberByAgentId(final Long publishedAssessmentId, final String agentIdString, final Date dueDate) {
        try {
            Session session = getCurrentSession();
            Query<AssessmentGradingData> q = session.createQuery(
                    "from AssessmentGradingData a where a.publishedAssessmentId = :id and a.agentId = :agent and a.forGrade = :forgrade and a.submittedDate > :submitted and a.status > :status",
                    AssessmentGradingData.class);
            q.setParameter("id", publishedAssessmentId);
            q.setParameter("agent", agentIdString);
            q.setParameter("forgrade", true);
            q.setParameter("submitted", dueDate);
            q.setParameter("status", AssessmentGradingData.REMOVED);
            List<AssessmentGradingData> assessmentGradings = q.list();

            return assessmentGradings.size();
        } catch (Exception e) {
            log.warn("Error getting late submissions count for assessment {} and agent {}: {}", 
                    publishedAssessmentId, agentIdString, e.toString());
            return 0;
        }
    }

    public List<AssessmentGradingData> getAllOrderedSubmissions(final String publishedId) {

       try {
            Long id;
            try {
                id = Long.parseLong(publishedId);
            } catch (NumberFormatException e) {
                log.warn("Invalid publishedId format: {}", publishedId);
                return new ArrayList<>();
            }
            
            Session session = getCurrentSession();
            Query<AssessmentGradingData> q = session.createQuery(
                    "from AssessmentGradingData a " +
                            "where a.publishedAssessmentId = :id and (a.forGrade = :forgrade1 or (a.forGrade = :forgrade2 and a.status = :status and a.finalScore <> 0)) " +
                            "order by a.agentId ASC, a.submittedDate",
                    AssessmentGradingData.class);
            q.setParameter("id", id);
            q.setParameter("forgrade1", true);
            q.setParameter("forgrade2", false);
            q.setParameter("status", AssessmentGradingData.NO_SUBMISSION);
            return q.list();
        } catch (Exception e) {
            log.warn("Error getting all ordered submissions for publishedId {}: {}", publishedId, e.toString());
            return new ArrayList<>();
        }
    }

    public Map<ExportSection, List<List<CellValue<?>>>> getExportResponsesData(String publishedAssessmentId, boolean anonymous, String audioMessage, String fileUploadMessage, String noSubmissionMessage,
                                       boolean showPartAndTotalScoreSpreadsheetColumns, String poolString, String partString, String questionString, String textString,
                                       String responseString, String pointsString, String rationaleString, String itemGradingCommentsString, Map<String, EnrollmentRecord> useridMap,
                                       String responseCommentString) {
        return this.getExportResponsesData(publishedAssessmentId, anonymous, audioMessage, fileUploadMessage, noSubmissionMessage, showPartAndTotalScoreSpreadsheetColumns,
                                    poolString, partString, questionString, textString, responseString, pointsString, rationaleString, itemGradingCommentsString, useridMap,
                                    responseCommentString, false);
    }

    public Map<ExportSection, List<List<CellValue<?>>>> getExportResponsesData(String publishedAssessmentId,
                                       boolean anonymous,
                                       String audioMessage,
                                       String fileUploadMessage,
                                       String noSubmissionMessage,
                                       boolean showPartAndTotalScoreSpreadsheetColumns,
                                       String poolString,
                                       String partString,
                                       String questionString,
                                       String textString,
                                       String responseString,
                                       String pointsString,
                                       String rationaleString,
                                       String itemGradingCommentsString,
                                       Map<String, EnrollmentRecord> useridMap,
                                       String responseCommentString,
                                       boolean isOneSelectionType) 
    {
        List<CellValue<?>> headerList = new ArrayList<>();
        List<List<CellValue<?>>> dataList = new ArrayList<>();
        PublishedAssessmentService pubService = new PublishedAssessmentService();

        Set<PublishedSectionData> publishedAssessmentSections = pubService.getSectionSetForAssessment(Long.valueOf(publishedAssessmentId));
        Double zeroDouble = 0.0;
        Map<Long, AnswerIfc> publishedAnswerHash = pubService.preparePublishedAnswerHash(pubService.getPublishedAssessment(publishedAssessmentId));
        Map<Long, ItemTextIfc> publishedItemTextHash = pubService.preparePublishedItemTextHash(pubService.getPublishedAssessment(publishedAssessmentId));
        Map<Long, ItemDataIfc> publishedItemHash = pubService.preparePublishedItemHash(pubService.getPublishedAssessment(publishedAssessmentId));

        //Get this sorted to add the blank gradings for the questions not answered later.
        Set<ItemDataIfc> publishItemSet = new TreeSet<>(new ItemComparator());
        publishItemSet.addAll(publishedItemHash.values());

        int numSubmission = 1;
        String numSubmissionText;
        String lastAgentId = "";
        String agentEid = "";
        String firstName = "";
        String lastName = "";
        Set<String> useridSet = new HashSet<>(useridMap.keySet());

        boolean canBeExported;
        boolean fistItemGradingData = true;
        List<AssessmentGradingData> list = getAllOrderedSubmissions(publishedAssessmentId);
        for (AssessmentGradingData gradingData : list) {
            List<CellValue<?>> responseList = new ArrayList<>();
            // create new section-item-scores structure for this assessmentGrading
            Iterator<PublishedSectionData> sectionsIter = publishedAssessmentSections.iterator();
            Map<Integer, Map<Long, Long>> sectionItems = new HashMap<>();
            Map<Integer, Double> sectionScores = new TreeMap<>();
            while (sectionsIter.hasNext()) {
                PublishedSectionData publishedSection = sectionsIter.next();
                List<ItemDataIfc> itemsArray = publishedSection.getItemArraySortedForGrading();

                // adding fixed questions (could be empty if not fixed and draw part)
                List<ItemDataIfc> sortedList = itemsArray.stream()
                        .filter(ItemDataIfc::getIsFixed)
                        .toList();

                // getting all hashes from the sortedList
                List<String> distinctHashValues = sortedList.stream()
                        .filter(item -> item instanceof PublishedItemData)
                        .map(ItemDataIfc::getHash)
                        .distinct()
                        .toList();

                // removing from itemSet if there are hashes repeated and getFixed false -> itemArray with only fixed and not repeated fixed on the randow draw
                itemsArray.removeIf(item -> item instanceof PublishedItemData &&
                        !item.getIsFixed() &&
                        distinctHashValues.stream().anyMatch(hash -> hash.equals(item.getHash())));

                Iterator<ItemDataIfc> itemsIter = itemsArray.iterator();
                // Iterate through the assessment questions (items)
                Map<Long, Long> itemsForSection = new HashMap<>();
                while (itemsIter.hasNext()) {
                    ItemDataIfc item = itemsIter.next();
                    itemsForSection.put(item.getItemId(), item.getItemId());
                }
                sectionItems.put(publishedSection.getSequence(), itemsForSection);
                sectionScores.put(publishedSection.getSequence(), zeroDouble);
            }

            String agentId = gradingData.getAgentId();
            canBeExported = false;
            if (anonymous) {
                canBeExported = true;
                responseList.add(CellValue.LONG(gradingData.getAssessmentGradingId()));
            } else {
                if (useridMap.containsKey(agentId)) {
                    useridSet.remove(agentId);
                    canBeExported = true;
                    // these are declared outside the loop, so reset them first: a failed lookup would
                    // otherwise leave the previous submitter's identity on this row
                    agentEid = agentId;
                    firstName = "";
                    lastName = "";
                    try {
                        User user = userDirectoryService.getUser(agentId);
                        agentEid = user.getEid();
                        firstName = user.getFirstName();
                        lastName = user.getLastName();
                    } catch (UserNotDefinedException e) {
                        log.warn("Cannot get user [{}], exporting their submission without a name, {}", agentId, e.toString());
                    }
                    responseList.add(CellValue.STRING(lastName));
                    responseList.add(CellValue.STRING(firstName));
                    responseList.add(CellValue.STRING(agentEid));
                    if (gradingData.getForGrade()) {
                        if (lastAgentId.equals(agentId)) {
                            numSubmission++;
                        } else {
                            numSubmission = 1;
                            lastAgentId = agentId;
                        }
                    } else {
                        numSubmission = 0;
                        lastAgentId = agentId;
                    }
                    if (numSubmission == 0) {
                        numSubmissionText = noSubmissionMessage;
                    } else {
                        numSubmissionText = String.valueOf(numSubmission);
                    }
                    responseList.add(CellValue.STRING(numSubmissionText));
                }
            }

            if (canBeExported) {

                Date attempt = gradingData.getAttemptDate();
                Date submitted = gradingData.getSubmittedDate();
                responseList.add(attempt == null ? CellValue.EMPTY() : CellValue.DATE(attempt));
                responseList.add(submitted == null ? CellValue.EMPTY() : CellValue.DATE(submitted));

                int sectionScoreColumnStart = responseList.size();
                if (showPartAndTotalScoreSpreadsheetColumns) {
                    Double finalScore = gradingData.getFinalScore();
                    log.debug("finalScore is {}", finalScore);
                    responseList.add(finalScore == null ? CellValue.EMPTY() : CellValue.DOUBLE(finalScore));
                }
                int emptyIndex = 0;
                if (isOneSelectionType) {
                    responseList.add(CellValue.INTEGER(0));
                    responseList.add(CellValue.INTEGER(0));
                    responseList.add(CellValue.INTEGER(0));
                    emptyIndex = responseList.size() - 1;
                }

                String assessmentGradingComments = "";
                if (gradingData.getComments() != null) {
                    assessmentGradingComments = gradingData.getComments().replaceAll("<br\\s*/>", "");
                }
                responseList.add(CellValue.STRING(assessmentGradingComments));

                Long assessmentGradingId = gradingData.getAssessmentGradingId();

                Map<Long, List<ItemGradingData>> studentGradingMap = getStudentGradingData(assessmentGradingId.toString(), false);
                List<List<ItemGradingData>> grades = new ArrayList<>(studentGradingMap.values());
                grades.sort(new QuestionComparator(publishedItemHash));

                //Add the blank gradings for the questions not answered in random pools.
                if (grades.size() < publishItemSet.size()) {
                    int index = -1;
                    for (Object pido : publishItemSet) {
                        index++;
                        PublishedItemData pid = (PublishedItemData) pido;
                        if (index == grades.size() ||
                                ((ItemGradingData) ((List) grades.get(index)).get(0)).getPublishedItemId()
                                        .longValue() != pid.getItemId().longValue()) {
                            //have to add the placeholder
                            List<ItemGradingData> newList = new ArrayList<>();
                            newList.add(new EmptyItemGrading(pid.getSection().getSequence(),
                                    pid.getItemId(),
                                    pid.getSequence()));
                            grades.add(index, newList);
                        }
                    }
                }

                int questionNumber = 0;
                for (Object oo : grades) {
                    // There can be more than one answer to a question, e.g. for
                    // FIB with more than one blank or matching questions. So sort
                    // by sequence number of answer. (don't bother to sort if just 1)

                    List l = (List) oo;
                    if (l.size() > 1)
                        Collections.sort(l, new AnswerComparator(publishedAnswerHash));

                    String maintext = "";
                    String rationale = "";
                    String responseComment = "";

                    boolean addRationale = false;
                    boolean addResponseComment = false;

                    boolean matrixChoices = false;
                    Map<Long, String> responsesMap = new TreeMap<>();
                    // loop over answers per question
                    int count = 0;
                    ItemGradingData grade = null;
                    //boolean isAudioFileUpload = false;
                    boolean isFinFib = false;

                    double itemScore = 0.0d;

                    //Add the missing sequences!
                    //To manage emi answers, could help with others too
                    Map<Long, String> emiAnswerText = new TreeMap<>();

                    boolean textOfQuestionIncluded = false;

                    for (Object ooo : l) {
                        grade = (ItemGradingData) ooo;
                        if (grade == null) {
                            continue;
                        }
                        if (grade instanceof EmptyItemGrading) {
                            responseList.add(CellValue.STRING("-"));
                            continue;
                        }
                        if (grade.getAutoScore() != null) {
                            itemScore += grade.getAutoScore();
                        }

                        // now print answer data
                        log.debug("<br> " + grade.getPublishedItemId() + " " + grade.getRationale() + " " + grade.getAnswerText() + " " + grade
                                .getComments() + " " + grade.getReview());
                        Long publishedItemId = grade.getPublishedItemId();
                        ItemDataIfc publishedItemData = publishedItemHash.get(publishedItemId);
                        Long typeId = publishedItemData.getTypeId();
                        if (count == 0) {
                            if (!TypeIfc.MATRIX_CHOICES_SURVEY.equals(typeId)) {
                                responseList.add(CellValue.STRING(publishedItemData.getText())); // The Text of the question
                            } else if (!textOfQuestionIncluded) {
                                // type MATRIX_CHOICES_SURVEY
                                responseList.add(CellValue.STRING(publishedItemData.getText())); // The Text of the question
                                textOfQuestionIncluded = true;
                            }
                        }
                        questionNumber = publishedItemData.getSequence();
                        if (typeId.equals(TypeIfc.FILL_IN_BLANK) || typeId.equals(TypeIfc.FILL_IN_NUMERIC) || typeId.equals(
                                TypeIfc.CALCULATED_QUESTION)) {
                            log.debug("FILL_IN_BLANK, FILL_IN_NUMERIC");
                            isFinFib = true;
                            String thistext;

                            Long answerid = grade.getPublishedAnswerId();
                            Long sequence = null;
                            if (answerid != null) {
                                AnswerIfc answer = publishedAnswerHash.get(answerid);
                                if (answer != null) {
                                    sequence = answer.getSequence();
                                }
                            }

                            String temptext = grade.getAnswerText();
                            if (temptext == null) {
                                temptext = "No Answer";
                            }
                            thistext = sequence + ": " + temptext;

                            if (count == 0)
                                maintext = thistext;
                            else
                                maintext = maintext + "|" + thistext;

                            count++;
                        } else if (typeId.equals(TypeIfc.MATCHING)) {
                            log.debug("MATCHING");
                            String thistext;

                            // for some question types we have another text field
                            Long answerid = grade.getPublishedAnswerId();
                            String temptext = "No Answer";
                            Long sequence = null;
                            if (answerid != null) {
                                AnswerIfc answer = publishedAnswerHash.get(answerid);
                                if (answer != null) {
                                    temptext = answer.getText();
                                    if (temptext == null) {
                                        temptext = "No Answer";
                                    }
                                    sequence = answer.getItemText().getSequence();
                                } else if (answerid == -1) {
                                    temptext = "None of the Above";
                                    ItemTextIfc itemTextIfc = publishedItemTextHash.get(grade.getPublishedItemTextId());
                                    sequence = itemTextIfc.getSequence();
                                }
                            } else {
                                ItemTextIfc itemTextIfc = publishedItemTextHash.get(grade.getPublishedItemTextId());
                                sequence = itemTextIfc.getSequence();
                            }
                            thistext = sequence + ": " + temptext;

                            if (count == 0)
                                maintext = thistext;
                            else
                                maintext = maintext + "|" + thistext;

                            count++;
                        } else if (typeId.equals(TypeIfc.IMAGEMAP_QUESTION)) {
                            log.debug("IMAGEMAP_QUESTION");

                            ItemTextIfc itemTextIfc = publishedItemTextHash.get(grade.getPublishedItemTextId());
                            Long sequence = itemTextIfc.getSequence();
                            String temptext = "No OK";
                            if (grade.getIsCorrect() != null) {
                                temptext = (grade.getIsCorrect()) ? "OK" : "No OK";
                            }

                            String thistext = sequence + ": " + temptext;

                            if (count == 0)
                                maintext = thistext;
                            else
                                maintext = maintext + "|" + thistext;

                            count++;
                        } else if (typeId.equals(TypeIfc.EXTENDED_MATCHING_ITEMS)) {
                            log.debug("EXTENDED_MATCHING_ITEMS");
                            String thistext;

                            // for some question types we have another text field
                            Long answerid = grade.getPublishedAnswerId();
                            String temptext = "No Answer";
                            Long sequence = null;

                            if (answerid != null) {
                                AnswerIfc answer = publishedAnswerHash.get(answerid);
                                if (answer != null) {
                                    temptext = answer.getLabel();
                                    if (temptext == null) {
                                        temptext = "No Answer";
                                    }
                                    sequence = answer.getItemText().getSequence();
                                }
                            }

                            if (sequence == null) {
                                ItemTextIfc itemTextIfc = publishedItemTextHash.get(grade.getPublishedItemTextId());
                                if (itemTextIfc != null) {
                                    sequence = itemTextIfc.getSequence();
                                }
                            }

                            if (sequence != null) {
                                thistext = emiAnswerText.get(sequence);
                                if (thistext == null) {
                                    thistext = temptext;
                                } else {
                                    thistext = thistext + temptext;
                                }
                                emiAnswerText.put(sequence, thistext);
                            } else {
                                // Orphaned answer: the answer item to which it refers was removed after the assessment was taken,
                                // as a result of editing the published assessment. This behaviour should be fixed, i.e. it should
                                // not be possible to get orphaned answer item references in the database.
                                sequence = 99L;
                                emiAnswerText.put(sequence, "Item Removed");
                            }
                        } else if (typeId.equals(TypeIfc.MATRIX_CHOICES_SURVEY)) {
                            log.debug("MATRIX_CHOICES_SURVEY");
                            // for this kind of question a responsesMap is generated
                            matrixChoices = true;
                            Long answerid = grade.getPublishedAnswerId();
                            String temptext;
                            Long sequence;
                            if (answerid != null) {
                                AnswerIfc answer = publishedAnswerHash.get(answerid);
                                temptext = answer.getText();
                                if (temptext == null) {
                                    temptext = "No Answer";
                                }
                                sequence = answer.getItemText().getSequence();
                            } else {
                                ItemTextIfc itemTextIfc = publishedItemTextHash.get(grade.getPublishedItemTextId());
                                sequence = itemTextIfc.getSequence();
                                log.debug("Answerid null for " + grade.getPublishedItemId() + ". Adding " + sequence);
                                temptext = "No Answer";
                            }
                            responsesMap.put(sequence, temptext);
                        } else if (typeId.equals(TypeIfc.AUDIO_RECORDING)) {
                            log.debug("AUDIO_RECORDING");
                            maintext = audioMessage;
                            //isAudioFileUpload = true;
                        } else if (typeId.equals(TypeIfc.FILE_UPLOAD)) {
                            log.debug("FILE_UPLOAD");
                            maintext = fileUploadMessage;
                            //isAudioFileUpload = true;
                        } else if (typeId.equals(TypeIfc.ESSAY_QUESTION)) {
                            log.debug("ESSAY_QUESTION");
                            if (grade.getAnswerText() != null) {
                                maintext = grade.getAnswerText();
                            }
                        } else {
                            log.debug("other type");
                            String thistext = "";

                            // for some question types we have another text field
                            Long answerid = grade.getPublishedAnswerId();
                            if (answerid != null) {
                                AnswerIfc answer = publishedAnswerHash.get(answerid);
                                if (answer != null) {
                                    if (isOneSelectionType) {
                                        Boolean answerCorrectness = resolveOneSelectionCorrectness(answer, grade);
                                        if (Boolean.TRUE.equals(answerCorrectness)) {
                                            // For correct answers cases
                                            incrementIntegerCounter(responseList, emptyIndex - 2);
                                        } else if (Boolean.FALSE.equals(answerCorrectness)) {
                                            // For incorrect answers cases
                                            incrementIntegerCounter(responseList, emptyIndex - 1);
                                        } else {
                                            log.debug("Skipping one-selection tally for answer {} due to unknown correctness", answerid);
                                        }
                                    }
                                    String temptext = answer.getText();
                                    if (temptext != null)
                                        thistext = temptext;
                                } else {
                                    log.warn("Published answer for " + answerid + " is null");
                                }
                            } else if (isOneSelectionType) {
                                // For empty answers cases
                                incrementIntegerCounter(responseList, emptyIndex);
                            }

                            if (count == 0)
                                maintext = thistext;
                            else
                                maintext = maintext + "|" + thistext;

                            count++;
                        }

                        // taking care of rationale
                        if (!addRationale && (typeId.equals(TypeIfc.MULTIPLE_CHOICE) || typeId.equals(TypeIfc.MULTIPLE_CORRECT) || typeId
                                .equals(TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION) || typeId.equals(TypeIfc.TRUE_FALSE))) {
                            log.debug(
                                    "MULTIPLE_CHOICE or MULTIPLE_CORRECT or MULTIPLE_CORRECT_SINGLE_SELECTION or TRUE_FALSE");
                            if (publishedItemData.getHasRationale() != null && publishedItemData.getHasRationale()) {
                                addRationale = true;
                                rationale = grade.getRationale();
                                if (rationale == null) {
                                    rationale = "";
                                }
                            }
                        }

                        //Survey - Matrix of Choices - Add Comment Field
                        if (matrixChoices) {
                            PublishedItemData pid = (PublishedItemData) publishedItemData;
                            if (pid.getAddCommentFlag()) {
                                addResponseComment = true;
                                if (responseComment.equals("") && grade.getAnswerText() != null) {
                                    responseComment = grade.getAnswerText();
                                }
                            }
                        }
                    } // inner for - answers


                    if (!emiAnswerText.isEmpty()) {
                        if (maintext == null) {
                            maintext = "";
                        }
                        for (Entry<Long, String> entry : emiAnswerText.entrySet()) {
                            maintext = maintext + "|" + entry.getKey().toString() + ":" + entry.getValue();
                        }
                        if (maintext.startsWith("|")) {
                            maintext = maintext.substring(1);
                        }
                    }
                    Integer sectionSequenceNumber;
                    if (grade == null || EmptyItemGrading.class.isInstance(grade)) {
                        sectionSequenceNumber = EmptyItemGrading.class.cast(grade).getSectionSequence();
                        questionNumber = EmptyItemGrading.class.cast(grade).getItemSequence();
                        // indicate that the student was not presented with this question
                        maintext = "-";
                    } else {
                        sectionSequenceNumber = updateSectionScore(sectionItems,
                                sectionScores,
                                grade.getPublishedItemId(),
                                itemScore);
                    }

                    if (isFinFib && maintext.contains("No Answer") && count == 1) {
                        maintext = "No Answer";
                    } else if ("".equals(maintext)) {
                        maintext = "No Answer";
                    }
                    // if question type is not matrix choices apply the original code
                    if (!matrixChoices) {
                        responseList.add(CellValue.STRING(maintext));
                        responseList.add(CellValue.DOUBLE(itemScore));
                    } else {
                        // if there are questions not answered, a no answer response is added to the map
                        ItemDataIfc correspondingPublishedItemData = publishedItemHash.get(grade.getPublishedItemId());
                        List correspondingItemTextArray = correspondingPublishedItemData.getItemTextArray();
                        log.debug("publishedItem is " + correspondingPublishedItemData.getText() + " and number of rows " + correspondingItemTextArray
                                .size());
                        if (responsesMap.size() < correspondingItemTextArray.size()) {
                            Iterator itItemTextHash = correspondingItemTextArray.iterator();
                            while (itItemTextHash.hasNext()) {
                                ItemTextIfc itemTextIfc = (ItemTextIfc) itItemTextHash.next();
                                if (!responsesMap.containsKey(itemTextIfc.getSequence())) {
                                    log.debug("responsesMap does not contain answer to " + itemTextIfc.getText());
                                    responsesMap.put(itemTextIfc.getSequence(), "No Answer");
                                }
                            }
                        }
                        responsesMap.entrySet().stream()
                                .peek(e -> log.debug("Adding to response list {} and {}", e.getKey(), e.getValue()))
                                .map(e -> CellValue.STRING(e.getValue()))
                                .forEach(responseList::add);
                    }

                    if (addResponseComment) {
                        responseList.add(CellValue.STRING(responseComment));
                    }

                    if (addRationale) {
                        responseList.add(CellValue.STRING(rationale));
                    }

                    String itemGradingComments = "";
                    if (grade.getComments() != null) {
                        itemGradingComments = grade.getComments().replaceAll("<br\\s*/>", "");
                    }
                    responseList.add(CellValue.STRING(itemGradingComments));

                    // Only set header based on the first item grading data
                    if (fistItemGradingData) {
                        //get the pool name
                        String poolName = null;
                        for (PublishedSectionData psd : publishedAssessmentSections) {
                            if (psd.getSequence().intValue() == sectionSequenceNumber) {
                                poolName = psd.getSectionMetaDataByLabel(SectionDataIfc.POOLNAME_FOR_RANDOM_DRAW);
                                if (SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOLS.equals(Integer.valueOf(psd.getSectionMetaDataByLabel(SectionDataIfc.AUTHOR_TYPE)))
                                        && psd.getSectionMetaDataByLabel(SectionDataIfc.RANDOM_POOL_COUNT) != null) {
                                    for (int j = 1; j < Integer.valueOf(psd.getSectionMetaDataByLabel(SectionDataIfc.RANDOM_POOL_COUNT)); j++) {
                                        poolName += SectionDataIfc.SEPARATOR_COMMA + psd.getSectionMetaDataByLabel(SectionDataIfc.POOLNAME_FOR_RANDOM_DRAW + SectionDataIfc.SEPARATOR_MULTI + j);
                                    }
                                }
                            }
                        }
                        if (!matrixChoices) {
                            headerList.add(CellValue.STRING(makeHeader(partString,
                                    sectionSequenceNumber,
                                    questionString,
                                    textString,
                                    questionNumber,
                                    poolString,
                                    poolName)));
                            headerList.add(CellValue.STRING(makeHeader(partString,
                                    sectionSequenceNumber,
                                    questionString,
                                    responseString,
                                    questionNumber,
                                    poolString,
                                    poolName)));
                            headerList.add(CellValue.STRING(makeHeader(partString,
                                    sectionSequenceNumber,
                                    questionString,
                                    pointsString,
                                    questionNumber,
                                    poolString,
                                    poolName)));
                            if (addRationale) {
                                headerList.add(
                                        CellValue.STRING(makeHeader(partString,
                                        sectionSequenceNumber,
                                        questionString,
                                        rationaleString,
                                        questionNumber,
                                        poolString,
                                        poolName)));
                            }
                            headerList.add(CellValue.STRING(makeHeader(partString,
                                    sectionSequenceNumber,
                                    questionString,
                                    itemGradingCommentsString,
                                    questionNumber,
                                    poolString,
                                    poolName)));
                        } else {
                            int numberRows = responsesMap.size();
                            headerList.add(CellValue.STRING(makeHeader(partString,
                                    sectionSequenceNumber,
                                    questionString,
                                    textString,
                                    questionNumber,
                                    poolString,
                                    poolName)));
                            for (int i = 0; i < numberRows; i = i + 1) {
                                headerList.add(CellValue.STRING(makeHeaderMatrix(partString,
                                        sectionSequenceNumber,
                                        questionString,
                                        responseString,
                                        questionNumber,
                                        i + 1,
                                        poolString,
                                        poolName)));
                            }
                            if (addRationale) {
                                headerList.add(CellValue.STRING(makeHeader(partString,
                                        sectionSequenceNumber,
                                        questionString,
                                        rationaleString,
                                        questionNumber,
                                        poolString,
                                        poolName)));
                            }
                            if (addResponseComment) {
                                headerList.add(CellValue.STRING(makeHeader(partString,
                                        sectionSequenceNumber,
                                        questionString,
                                        responseCommentString,
                                        questionNumber,
                                        poolString,
                                        poolName)));
                            }
                            headerList.add(CellValue.STRING(makeHeader(partString,
                                    sectionSequenceNumber,
                                    questionString,
                                    itemGradingCommentsString,
                                    questionNumber,
                                    poolString,
                                    poolName)));
                        }
                    }
                } // outer for - questions

                if (showPartAndTotalScoreSpreadsheetColumns) {
                    if (sectionScores.size() > 1) {
                        for (Integer integer : sectionScores.keySet()) {
                            Double partScore = sectionScores.get(integer);
                            responseList.add(sectionScoreColumnStart++, CellValue.DOUBLE(partScore));
                        }
                    }
                }

                dataList.add(responseList);

                if (fistItemGradingData) {
                    fistItemGradingData = false;
                }
            }
        }

        if (!anonymous && !useridSet.isEmpty()) {
            for (String id : useridSet) {
                agentEid = id;
                firstName = "";
                lastName = "";
                try {
                    User user = userDirectoryService.getUser(id);
                    agentEid = user.getEid();
                    firstName = user.getFirstName();
                    lastName = user.getLastName();
                } catch (UserNotDefinedException e) {
                    log.error("Cannot get user [{}], {}", id, e.toString());
                }
                dataList.add(new ArrayList<>(
                            List.of(CellValue.STRING(lastName),
                                CellValue.STRING(firstName),
                                CellValue.STRING(agentEid),
                                CellValue.STRING(noSubmissionMessage))));
            }
        }
        Collator collator = SakaiCollators.getCollatorWithUnderscoreAfterSpace(
                ComponentManager.get(LocaleService.class).getLocaleForCurrentSiteAndUser(), Collator.TERTIARY);
        Collections.sort(dataList, new CellComparator(anonymous, collator));

        Map<ExportSection, List<List<CellValue<?>>>> result = new EnumMap<>(ExportSection.class);
        result.put(ExportSection.HEADER, List.of(headerList));
        result.put(ExportSection.ROWS, dataList);
        return result;
    }

    /**
     * Resolve correctness for one-selection export counters without null unboxing.
     * Order of precedence:
     * 1) Published answer correctness flag
     * 2) Item grading correctness flag
     * 3) Item grading auto score sign
     */
    Boolean resolveOneSelectionCorrectness(AnswerIfc answer, ItemGradingData grade) {
        if (answer != null && answer.getIsCorrect() != null) {
            return answer.getIsCorrect();
        }

        if (grade != null && grade.getIsCorrect() != null) {
            return grade.getIsCorrect();
        }

        if (grade != null && grade.getAutoScore() != null) {
            return grade.getAutoScore() > 0;
        }

        return null;
    }

    private static void incrementIntegerCounter(List<CellValue<?>> responseList, int index) {
        if (responseList.get(index) instanceof CellValue.IntegerValue current) {
            responseList.set(index, CellValue.INTEGER(current.value() + 1));
        }
    }


    /**
     * Updates section scores and items map based on the provided published item and its score.
     *
     * <p>This method scans through all sections to find which section contains the given published item.
     * When found, it updates the section's cumulative score by adding the item's score and returns
     * the section's sequence number.</p>
     *
     * @param sectionItems a map of section sequences to their contained item IDs
     *                     (Map&lt;Integer, Map&lt;Long, Long&gt;&gt;)
     * @param sectionScores a map of section sequences to their cumulative scores
     *                      (Map&lt;Integer, Double&gt;)
     * @param publishedItemId the ID of the published item being scored
     * @param itemScore the score to add to the section's cumulative score
     * @return the section sequence number (Integer) where the item was found,
     *         or zero if the section is not found (unlikely)
     */
    private int updateSectionScore(Map<Integer, Map<Long, Long>> sectionItems, Map<Integer, Double> sectionScores, Long publishedItemId, double itemScore) {

        for (Map.Entry<Integer, Map<Long, Long>> entry : sectionItems.entrySet()) {
            Integer sectionSequence = entry.getKey();
            Map<Long, Long> itemsForSection = entry.getValue();

            if (itemsForSection.get(publishedItemId) != null) {
                Double score = sectionScores.get(sectionSequence) + itemScore;
                sectionScores.put(sectionSequence, score);
                return sectionSequence;
            }
        }
        return 0;
    }


    /*
     sort answers by sequence number within question if one is defined
	 normally it will be, but use id number if not
	 hint: "item" things are specific to the user's answer
	 sequence numbers are stored with the published assessment, not
	 separate with each user, so we need to use the hash to find the
	 published answer
	 */
    private static class AnswerComparator implements Comparator {

        Map publishedAnswerHash;

        public AnswerComparator(Map m) {
            publishedAnswerHash = m;
        }

        public int compare(Object a, Object b) {
            ItemGradingData agrade = (ItemGradingData) a;
            ItemGradingData bgrade = (ItemGradingData) b;

            Long aindex = agrade.getItemGradingId();
            Long bindex = bgrade.getItemGradingId();

            Long aanswerid = agrade.getPublishedAnswerId();
            Long banswerid = bgrade.getPublishedAnswerId();

            AnswerIfc aanswer;
            AnswerIfc banswer;

            if (aanswerid != null && banswerid != null) {
                aanswer = (AnswerIfc) publishedAnswerHash
                        .get(aanswerid);
                banswer = (AnswerIfc) publishedAnswerHash
                        .get(banswerid);

                if (aanswer == null || banswer == null) {
                    return (aanswer == null ? -1 : 1);
                } else {
                    //For EMI, use this test
                    if (aanswer.getItem() != null &&
                            TypeIfc.EXTENDED_MATCHING_ITEMS.equals(aanswer.getItem().getTypeId()) &&
                            banswer.getItem() != null &&
                            TypeIfc.EXTENDED_MATCHING_ITEMS.equals(banswer.getItem().getTypeId())) {
                        Long aTextSeq = aanswer.getItemText().getSequence();
                        Long bTextSeq = banswer.getItemText().getSequence();
                        if (!aTextSeq.equals(bTextSeq)) {
                            return aTextSeq.compareTo(bTextSeq);
                        } else {
                            return aanswer.getLabel().compareToIgnoreCase(banswer.getLabel());
                        }
                    }

                    aindex = aanswer.getSequence();
                    bindex = banswer.getSequence();
                }
            }

            if (aindex < bindex) {
                return -1;
            } else if (aindex > bindex) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    /**
     * Comparator for sorting lists of ItemGradingData by their associated questions' display order.
     *
     * <p>Questions are sorted first by section sequence number, then by item sequence number within
     * each section. This ensures answers are displayed in the same order as questions were presented
     * to users.</p>
     *
     * <p><strong>Note:</strong> ItemGradingData objects contain user-specific answer data, but their
     * sequence numbers reference the published assessment structure. This comparator uses the
     * publishedItemHash to look up the published question metadata for ordering.</p>
     *
     * @see ItemGradingData user's answer data for a specific question
     * @see ItemDataIfc published question structure with sequence information
     */
    private static class QuestionComparator implements Comparator<List<ItemGradingData>> {

        Map<Long, ItemDataIfc> publishedItemHash;

        public QuestionComparator(Map<Long, ItemDataIfc> map) {
            publishedItemHash = map;
        }

        public int compare(List<ItemGradingData> a, List<ItemGradingData> b) {
            ItemGradingData agrade = a.get(0);
            ItemGradingData bgrade = b.get(0);

            ItemDataIfc aitem = publishedItemHash.get(agrade.getPublishedItemId());
            ItemDataIfc bitem = publishedItemHash.get(bgrade.getPublishedItemId());

            Integer asectionseq = aitem.getSection().getSequence();
            Integer bsectionseq = bitem.getSection().getSequence();

            if (asectionseq < bsectionseq) return -1;
            else if (asectionseq > bsectionseq) return 1;

            Integer aitemseq = aitem.getSequence();
            Integer bitemseq = bitem.getSequence();

            return aitemseq.compareTo(bitemseq);
        }
    }

    /*
	 sort questions in same order presented to users
	 first by section then by question within section
	 hint: "item" things are specific to the user's answer
	 sequence numbers are stored with the published assessment, not
	 separate with each user, so we need to use the hash to find the
	 published question
	 */
    private static class CellComparator implements Comparator<List<CellValue<?>>> {
        private final Collator collator;
        boolean anonymous;

        public CellComparator(boolean anony, Collator collator) {
            anonymous = anony;
            this.collator = collator;
        }

		public int compare(List<CellValue<?>> a, List<CellValue<?>> b) {
			// For anonymous, it should return after the first element comparison
			if (anonymous) {
				return compareCells(a.get(0), b.get(0));
			}
			// For non-anonymous, it compares last names first, if it is the same,
			// compares first name, and then Eid
			else {
				int result = compareCells(a.get(0), b.get(0));
				if (result != 0) {
					return result;
				}
				result = compareCells(a.get(1), b.get(1));
				if (result != 0) {
					return result;
				}
				return compareCells(a.get(2), b.get(2));
			}
		}

		// Each cell's own record variant carries its value's real type, so pattern-matching
		// to that variant recovers a properly typed value with no cast. Mismatched types
		// (shouldn't happen in practice) fall back to a fixed type ordering.
		private int compareCells(CellValue<?> a, CellValue<?> b) {
			if (a instanceof CellValue.StringValue av && b instanceof CellValue.StringValue bv) {
				return collator.compare(av.value(), bv.value());
			}
			if (a instanceof CellValue.LongValue av && b instanceof CellValue.LongValue bv) {
				return av.value().compareTo(bv.value());
			}
			if (a instanceof CellValue.IntegerValue av && b instanceof CellValue.IntegerValue bv) {
				return av.value().compareTo(bv.value());
			}
			if (a instanceof CellValue.DoubleValue av && b instanceof CellValue.DoubleValue bv) {
				return av.value().compareTo(bv.value());
			}
			if (a instanceof CellValue.DateValue av && b instanceof CellValue.DateValue bv) {
				return av.value().compareTo(bv.value());
			}
			if (a instanceof CellValue.BooleanValue av && b instanceof CellValue.BooleanValue bv) {
				return av.value().compareTo(bv.value());
			}
			return typeOrder(a) - typeOrder(b);
		}

		private static int typeOrder(CellValue<?> value) {
			if (value instanceof CellValue.BooleanValue) return 0;
			if (value instanceof CellValue.IntegerValue) return 1;
			if (value instanceof CellValue.LongValue) return 2;
			if (value instanceof CellValue.DoubleValue) return 3;
			if (value instanceof CellValue.DateValue) return 4;
			return 5; // StringValue
		}
	}

    /**
     * Comparator for sorting assessment items by their display order in sections.
     *
     * <p>Items are sorted first by their section's sequence number, then by the item's
     * sequence number within that section. This ensures items are ordered as they appear
     * in the published assessment structure.</p>
     *
     * <p>Example ordering:</p>
     * <ul>
     *   <li>Section 1, Item 1</li>
     *   <li>Section 1, Item 2</li>
     *   <li>Section 2, Item 1</li>
     * </ul>
     *
     * @see ItemDataIfc for item sequence information
     * @see SectionDataIfc for section sequence information
     */
    private static class ItemComparator implements Comparator<ItemDataIfc> {

        public int compare(ItemDataIfc o1, ItemDataIfc o2) {
            int sectionComparison = Integer.compare(o1.getSection().getSequence(), o2.getSection().getSequence());
            return sectionComparison != 0 ? sectionComparison : o1.getSequence() - o2.getSequence();
        }
    }

    public void removeUnsubmittedAssessmentGradingData(final AssessmentGradingData data) {
        try {
            Session session = getCurrentSession();
            Query<AssessmentGradingData> q = session.createQuery(
                    "from AssessmentGradingData a where a.publishedAssessmentId = :id and a.agentId = :agent " +
                            "and a.forGrade = :forgrade and a.status = :status " +
                            "order by a.submittedDate desc",
                    AssessmentGradingData.class);
            q.setParameter("id", data.getPublishedAssessmentId());
            q.setParameter("agent", data.getAgentId());
            q.setParameter("forgrade", false);
            q.setParameter("status", AssessmentGradingData.NO_SUBMISSION);
            List<AssessmentGradingData> assessmentGradings = q.list();
            
            if (!assessmentGradings.isEmpty()) {
                deleteAll(assessmentGradings);
            }
        } catch (Exception e) {
            log.warn("Error removing unsubmitted assessment grading data for agent {}: {}", data.getAgentId(), e.toString());
        }
    }

    public void removeAssessmentGradingData(final AssessmentGradingData data) {
    	data.setStatus(AssessmentGradingData.REMOVED);
    	data.setForGrade(false);
    	saveOrUpdateAssessmentGrading(data);
    }

    public boolean getHasGradingData(final Long publishedAssessmentId) {
         try {
            Session session = getCurrentSession();
            Query<AssessmentGradingData> q = session.createQuery(
                    "from AssessmentGradingData a where a.publishedAssessmentId = :id and a.status > :status",
                    AssessmentGradingData.class);
            q.setParameter("id", publishedAssessmentId);
            q.setParameter("status", AssessmentGradingData.REMOVED);
            q.setMaxResults(1);
            return !q.list().isEmpty();
         } catch (Exception e) {
            log.warn("Error checking if assessment {} has grading data: {}", publishedAssessmentId, e.toString());
            return false;
        }
    }

    public List<Boolean> getHasGradingDataAndHasSubmission(final Long publishedAssessmentId) {
    	try {
            Session session = getCurrentSession();
            Query<AssessmentGradingData> q = session.createQuery(
                    "from AssessmentGradingData a where a.publishedAssessmentId = :id and a.status > :status order by a.agentId asc, a.submittedDate desc",
                    AssessmentGradingData.class);
            q.setParameter("id", publishedAssessmentId);
            q.setParameter("status", AssessmentGradingData.REMOVED);
            List<AssessmentGradingData> assessmentGradings = q.list();
            
            // first element represents hasGradingData
            // second element represents hasSubmission
            List<Boolean> al = new ArrayList<>();
            if (assessmentGradings.isEmpty()) {
                al.add(Boolean.FALSE); // no gradingData
                al.add(Boolean.FALSE); // no submission
            } else {
                al.add(Boolean.TRUE); // yes gradingData
                String currentAgent = "";
                boolean hasSubmission = false;
                for (AssessmentGradingData adata : assessmentGradings) {
                    if (!currentAgent.equals(adata.getAgentId())) {
                        if (adata.getForGrade()) {
                            al.add(Boolean.TRUE); // has submission
                            hasSubmission = true;
                            break;
                        }
                        currentAgent = adata.getAgentId();
                    }
                }
                if (!hasSubmission) {
                    al.add(Boolean.FALSE); // no submission
                }
            }
            return al;
        } catch (Exception e) {
            log.warn("Error getting has grading data and has submission for assessment {}: {}", publishedAssessmentId, e.toString());
            List<Boolean> errorResult = new ArrayList<>();
            errorResult.add(Boolean.FALSE);
            errorResult.add(Boolean.FALSE);
            return errorResult;
        }
    }


    public String getFilename(Long itemGradingId, String agentId, String filename) {
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex < 0) {
            return getFilenameWOExtesion(itemGradingId, agentId, filename);
        } else {
            return getFilenameWExtesion(itemGradingId, agentId, filename, dotIndex);
        }
    }

    private String getFilenameWOExtesion(Long itemGradingId, String agentId, String filename) {
        StringBuilder bindVar = new StringBuilder(filename);
        bindVar.append("%");

        try {
            Session session = getCurrentSession();
            Query<String> q = session.createQuery(
                    "select filename from MediaData m where m.itemGradingData.itemGradingId = :id and m.createdBy = :agent and m.filename like :file",
                    String.class);
            q.setParameter("id", itemGradingId);
            q.setParameter("agent", agentId);
            q.setParameter("file", bindVar.toString());
            List<String> list = q.list();
            
            if (list.isEmpty()) {
                return filename;
            }

            HashSet<String> hs = new HashSet<>();
            // Only add the filename which
            // 1. with no extension because the newly updated one has no extention
            // 2. name is same to filename or name like filename(...
            // For example, if the filename is ab. We only want ab, ab(1), ab(2)... and don't want abc to be in
            for (String name : list) {
                name = name.trim();
                if (!name.contains(".") && (name.equals(filename) || name.startsWith(filename + "("))) {
                    hs.add(name);
                }
            }

            if (hs.isEmpty()) {
                return filename;
            }

            StringBuilder testName = new StringBuilder(filename);
            int i = 1;
            while (true) {
                if (!hs.contains(testName.toString())) {
                    return testName.toString();
                } else {
                    i++;
                    testName = new StringBuilder(filename);
                    testName.append("(");
                    testName.append(i);
                    testName.append(")");
                }
            }
        } catch (Exception e) {
            log.warn("Error getting filename without extension for item {} and agent {}: {}", itemGradingId, agentId, e.toString());
            return filename;
        }
    }

    private String getFilenameWExtesion(Long itemGradingId, String agentId, String filename, int dotIndex) {
        String filenameWithoutExtension = filename.substring(0, dotIndex);
        StringBuilder bindVar = new StringBuilder(filenameWithoutExtension);
        bindVar.append("%");
        bindVar.append(filename.substring(dotIndex));

        try {
            Session session = getCurrentSession();
            Query<String> q = session.createQuery(
                    "select filename from MediaData m where m.itemGradingData.itemGradingId = :id and m.createdBy = :agent and m.filename like :file",
                    String.class);
            q.setParameter("id", itemGradingId);
            q.setParameter("agent", agentId);
            q.setParameter("file", bindVar.toString());
            List<String> list = q.list();
            
            if (list.isEmpty()) {
                return filename;
            }

            HashSet<String> hs = new HashSet<>();
            String extension = filename.substring(dotIndex);
            int extensionLength = extension.length();
            
            for (String name : list) {
                name = name.trim();
                if ((name.equals(filename) || name.startsWith(filenameWithoutExtension + "("))) {
                    hs.add(name.substring(0, name.length() - extensionLength));
                }
            }

            if (hs.isEmpty()) {
                return filename;
            }

            StringBuilder testName = new StringBuilder(filenameWithoutExtension);
            int i = 1;
            while (true) {
                if (!hs.contains(testName.toString())) {
                    testName.append(extension);
                    return testName.toString();
                } else {
                    i++;
                    testName = new StringBuilder(filenameWithoutExtension);
                    testName.append("(");
                    testName.append(i);
                    testName.append(")");
                }
            }
        } catch (Exception e) {
            log.warn("Error getting filename with extension for item {} and agent {}: {}", itemGradingId, agentId, e.toString());
            return filename;
        }
    }

    // This method returns a list of two lists.
    // The first list contains the assessment ids with status ASSESSMENT_UPDATED_NEED_RESUBMIT
    // The second list contains the assessment ids with status ASSESSMENT_UPDATED.
    public List<Set<Long>> getUpdatedAssessmentList(String agentId, String siteId) {
        Set<Long> updatedAssessmentIds = new LinkedHashSet<>();
        Set<Long> needResubmitAssessmentIds = new LinkedHashSet<>();

        try {
            Session session = getCurrentSession();
            Query<Object[]> q = session.createQuery(
                    "select distinct a.publishedAssessmentId, a.status from AssessmentGradingData a, AuthorizationData az " +
                            " where a.agentId = :agent and az.agentIdString = :site and az.functionId = :fid " +
                            " and az.qualifierId = a.publishedAssessmentId and a.forGrade = :forgrade and (a.status = :status1 or a.status = :status2) " +
                            " order by a.status",
                    Object[].class);
            q.setParameter("agent", agentId);
            q.setParameter("site", siteId);
            q.setParameter("fid", "OWN_PUBLISHED_ASSESSMENT");
            q.setParameter("forgrade", false);
            q.setParameter("status1", AssessmentGradingData.ASSESSMENT_UPDATED);
            q.setParameter("status2", AssessmentGradingData.ASSESSMENT_UPDATED_NEED_RESUBMIT);
            List<Object[]> results = q.list();

            if (results != null) {
                for (Object[] row : results) {
                    Long assessmentId = (Long) row[0];
                    Integer status = (Integer) row[1];

                    if (AssessmentGradingData.ASSESSMENT_UPDATED_NEED_RESUBMIT.equals(status)) {
                        updatedAssessmentIds.remove(assessmentId);
                        needResubmitAssessmentIds.add(assessmentId);
                    } else if (AssessmentGradingData.ASSESSMENT_UPDATED.equals(status)
                            && !needResubmitAssessmentIds.contains(assessmentId)) {
                        updatedAssessmentIds.add(assessmentId);
                    }
                }
            }

            List<Set<Long>> finalList = new ArrayList<>(2);
            finalList.add(needResubmitAssessmentIds);
            finalList.add(updatedAssessmentIds);
            return finalList;
        } catch (Exception e) {
            log.warn("Error getting updated assessment list for agent {} and site {}: {}", agentId, siteId, e.toString());
            List<Set<Long>> emptyList = new ArrayList<>(2);
            emptyList.add(needResubmitAssessmentIds);
            emptyList.add(updatedAssessmentIds);
            return emptyList;
        }
    }

    public List getSiteNeedResubmitList(String siteId) {
    	try {
            Session session = getCurrentSession();
            Query<Long> q = session.createQuery(
                    "select distinct a.publishedAssessmentId from AssessmentGradingData a, AuthorizationData au " +
                            "where au.functionId = :fid and au.agentIdString = :site and a.publishedAssessmentId = au.qualifierId " +
                            "and a.forGrade = :forgrade and a.status = :status",
                    Long.class);
            q.setParameter("fid", "OWN_PUBLISHED_ASSESSMENT");
            q.setParameter("site", siteId);
            q.setParameter("forgrade", false);
            q.setParameter("status", AssessmentGradingData.ASSESSMENT_UPDATED_NEED_RESUBMIT);
            return q.list();
        } catch (Exception e) {
            log.warn("Error getting site need resubmit list for site {}: {}", siteId, e.toString());
            return new ArrayList<>();
        }
    }

    @Override
    public int autoSubmitAssessments() {
        java.util.Date currentTime = new java.util.Date();
        int failures = 0;

        try {
            Session session = getCurrentSession();

            Query<AssessmentGradingData> query = session.createQuery(
                    "select new AssessmentGradingData(a.assessmentGradingId, a.publishedAssessmentId, " +
                            " a.agentId, a.submittedDate, a.isLate, a.forGrade, a.totalAutoScore, a.totalOverrideScore, " +
                            " a.finalScore, a.comments, a.status, a.gradedBy, a.gradedDate, a.attemptDate, a.timeElapsed) " +
                            " from AssessmentGradingData a, PublishedAccessControl c " +
                            " where a.publishedAssessmentId = c.assessment.publishedAssessmentId " +
                            " and ((c.lateHandling = 1 and c.retractDate <= :currentTime) or (c.lateHandling = 2 and c.dueDate <= :currentTime))" +
                            " and a.status not in (:status) and (a.hasAutoSubmissionRun = 0 or a.hasAutoSubmissionRun is null) and c.autoSubmit = 1 " +
                            " and a.attemptDate is not null " +
                            " order by a.publishedAssessmentId, a.agentId, a.forGrade desc, a.assessmentGradingId",
                    AssessmentGradingData.class);

            query.setParameter("currentTime", currentTime);
            query.setParameterList("status", Arrays.asList(AssessmentGradingData.REMOVED, AssessmentGradingData.NO_SUBMISSION));
            query.setTimeout(300);

            List<AssessmentGradingData> list = query.list();

            Iterator<AssessmentGradingData> iter = list.iterator();
            String lastAgentId = "";
            Long lastPublishedAssessmentId = 0L;
            PublishedAssessmentFacade assessment = null;
            AssessmentGradingData adata = null;
            Map<Long, Set<PublishedSectionData>> sectionSetMap = new HashMap<>();

            PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
            boolean updateGrades = IntegrationContextFactory.getInstance() != null;
            AutoSubmitFacadeQueriesAPI autoSubmitFacade = PersistenceService.getInstance().getAutoSubmitFacadeQueries();

            while (iter.hasNext()) {
                try {
                    adata = iter.next();

                    if (!lastPublishedAssessmentId.equals(adata.getPublishedAssessmentId())) {
                        assessment = publishedAssessmentService.getPublishedAssessmentQuick(adata.getPublishedAssessmentId().toString());
                    }

                    // this call happens in a separate transaction, so a rollback only affects this iteration
                    boolean success = autoSubmitFacade.processAttempt(adata, updateGrades, this, assessment, currentTime, 
                            lastAgentId, lastPublishedAssessmentId, sectionSetMap);
                    if (!success) {
                        ++failures;
                    }

                    lastPublishedAssessmentId = adata.getPublishedAssessmentId();
                    lastAgentId = adata.getAgentId();
                } catch (Exception e) {
                    ++failures;
                    if (adata != null) {
                        log.error("Error while auto submitting assessment grade data id: " + adata.getAssessmentGradingId(), e);
                    } else {
                        log.error(e.getMessage(), e);
                    }
                } finally {
                    adata = null;
                }
            }

            return failures;
        } catch (Exception e) {
            log.error("Error in autoSubmitAssessments: {}", e.toString(), e);
            return failures;
        }
    }

    private String makeHeader(String section, int sectionNumber, String question, String headerType, int questionNumber, String pool, String poolName) {
        StringBuilder sb = new StringBuilder(section);
        sb.append(" ");
        sb.append(sectionNumber);
        sb.append(", ");
        sb.append(question);
        sb.append(" ");
        sb.append(questionNumber);
        sb.append(", ");
        if (poolName != null) {
            sb.append(pool);
            sb.append(" ");
            sb.append(poolName);
            sb.append(", ");
        }
        sb.append(headerType);
        return sb.toString();
    }

    private String makeHeaderMatrix(String section, int sectionNumber, String question, String headerType, int questionNumber, int questionRow, String pool, String poolName) {
        StringBuilder sb = new StringBuilder(section);
        sb.append(" ");
        sb.append(sectionNumber);
        sb.append(", ");
        sb.append(question);
        sb.append(" ");
        sb.append(questionNumber);
        sb.append(": ");
        sb.append(questionRow);
        sb.append(", ");
        if (poolName != null) {
            sb.append(pool);
            sb.append(" ");
            sb.append(poolName);
            sb.append(", ");
        }
        sb.append(headerType);
        return sb.toString();
    }

    public ItemGradingAttachment createItemGradingtAttachment(ItemGradingData itemGrading, String resourceId, String filename, String protocol) {
        GradingAttachmentData attach = createGradingtAttachment(resourceId, filename, protocol);
        ItemGradingAttachment itemAttach = new ItemGradingAttachment(attach, itemGrading);
        itemAttach.setItemGrading(itemGrading);
        return itemAttach;
    }

    public AssessmentGradingAttachment createAssessmentGradingtAttachment(AssessmentGradingData assessmentGrading, String resourceId, String filename, String protocol) {
        GradingAttachmentData attach = createGradingtAttachment(resourceId, filename, protocol);
        AssessmentGradingAttachment assessAttach = new AssessmentGradingAttachment(attach, assessmentGrading);
        assessAttach.setAssessmentGrading(assessmentGrading);
        return assessAttach;
    }

    private GradingAttachmentData createGradingtAttachment(String resourceId, String filename, String protocol) {
        GradingAttachmentData attach = null;
        Boolean isLink = Boolean.FALSE;
        try {
            ContentResource cr = contentHostingService.getResource(resourceId);
            if (cr != null) {
                AssessmentFacadeQueries assessmentFacadeQueries = new AssessmentFacadeQueries();
                ResourceProperties p = cr.getProperties();
                attach = new GradingAttachmentData();
                attach.setResourceId(resourceId);
                attach.setFilename(filename);
                attach.setMimeType(cr.getContentType());
                // we want to display kb, so divide by 1000 and round the result
                attach.setFileSize(assessmentFacadeQueries.fileSizeInKB(cr.getContentLength()));
                if (cr.getContentType().lastIndexOf("url") > -1) {
                    isLink = Boolean.TRUE;
                    if (!filename.toLowerCase().startsWith("http")) {
                        String adjustedFilename = "http://" + filename;
                        attach.setFilename(adjustedFilename);
                    } else {
                        attach.setFilename(filename);
                    }
                } else {
                    attach.setFilename(filename);
                }
                attach.setIsLink(isLink);
                attach.setStatus(AssessmentAttachmentIfc.ACTIVE_STATUS);
                attach.setCreatedBy(p.getProperty(p.getNamePropCreator()));
                attach.setCreatedDate(new Date());
                attach.setLastModifiedBy(p.getProperty(p.getNamePropModifiedBy()));
                attach.setLastModifiedDate(new Date());
                attach.setLocation(assessmentFacadeQueries.getRelativePath(cr.getUrl(), protocol));
            }
        } catch (PermissionException | IdUnusedException | TypeException pe) {
            log.warn(pe.getMessage(), pe);
        }
        return attach;
    }

    public void removeItemGradingAttachment(Long attachmentId) {
        int retryCount = persistenceHelper.getRetryCount();
        while (retryCount > 0) {
            try {
                Session session = getCurrentSession();
                ItemGradingAttachment itemGradingAttachment = session.get(ItemGradingAttachment.class, attachmentId);
                
                if (itemGradingAttachment == null) {
                    log.warn("ItemGradingAttachment with ID {} not found", attachmentId);
                    retryCount = 0;
                    return;
                }

                ItemGradingData itemGrading = itemGradingAttachment.getItemGrading();
                // String resourceId = assessmentAttachment.getResourceId();

                if (itemGrading != null) {
                    Set<ItemGradingAttachment> set = itemGrading.getItemGradingAttachmentSet();
                    if (set != null && set.contains(itemGradingAttachment)) {
                        set.remove(itemGradingAttachment);
                        session.merge(itemGrading);
                    }
                }
                
                session.remove(itemGradingAttachment);
                retryCount = 0;
            } catch (Exception e) {
                log.warn("problem delete assessmentAttachment: " + e.toString());
                retryCount = persistenceHelper.retryDeadlock(e, retryCount);
            }
        }
    }

    public void removeAssessmentGradingAttachment(Long attachmentId) {
        int retryCount = persistenceHelper.getRetryCount();
        while (retryCount > 0) {
            try {
                Session session = getCurrentSession();
                AssessmentGradingAttachment assessmentGradingAttachment = session.get(AssessmentGradingAttachment.class, attachmentId);
                
                if (assessmentGradingAttachment == null) {
                    log.warn("AssessmentGradingAttachment with ID {} not found", attachmentId);
                    retryCount = 0;
                    return;
                }

                AssessmentGradingData assessmentGrading = assessmentGradingAttachment.getAssessmentGrading();
                // String resourceId = assessmentAttachment.getResourceId();

                if (assessmentGrading != null) {
                    Set<AssessmentGradingAttachment> set = assessmentGrading.getAssessmentGradingAttachmentSet();
                    if (set != null && set.contains(assessmentGradingAttachment)) {
                        set.remove(assessmentGradingAttachment);
                        session.merge(assessmentGrading);
                    }
                }
                
                session.remove(assessmentGradingAttachment);
                retryCount = 0;
            } catch (Exception e) {
                log.warn("problem delete assessmentAttachment: " + e.toString());
                retryCount = persistenceHelper.retryDeadlock(e, retryCount);
            }
        }
    }

    public void saveOrUpdateAttachments(List<AttachmentIfc> list) {
        try {
            Session session = getCurrentSession();
            for (AttachmentIfc attachment : list) {
                if (attachment != null) {
                    session.merge(attachment);
                }
            }
        } catch (Exception e) {
            log.warn("Error saving or updating attachments: {}", e.toString());
            throw new DataAccessResourceFailureException("Failed to save or update attachments", e);
        }
    }

    public HashMap getInProgressCounts(String siteId) {
        try {
            Session session = getCurrentSession();
            Query<Object[]> q = session.createQuery(
                    "select a.publishedAssessmentId, count(*) from AssessmentGradingData a, AuthorizationData au " +
                            "where au.functionId = :fid and au.agentIdString = :site and a.publishedAssessmentId = au.qualifierId " +
                            "and a.forGrade = :forgrade and (a.status = :status1 or a.status = :status2) group by a.publishedAssessmentId",
                    Object[].class);
            q.setParameter("fid", "OWN_PUBLISHED_ASSESSMENT");
            q.setParameter("site", siteId);
            q.setParameter("forgrade", false);
            q.setParameter("status1", AssessmentGradingData.IN_PROGRESS);
            q.setParameter("status2", AssessmentGradingData.ASSESSMENT_UPDATED);
            List<Object[]> list = q.list();
            
            HashMap<Long, Long> inProgressCountsMap = new HashMap<>();
            for (Object[] o : list) {
                inProgressCountsMap.put((Long) o[0], (Long) o[1]);
            }
            return inProgressCountsMap;
        } catch (Exception e) {
            log.warn("Error getting in-progress counts for site {}: {}", siteId, e.toString());
            return new HashMap<>();
        }
    }

    public HashMap getSubmittedCounts(String siteId) {
        try {
            Session session = getCurrentSession();
            Query<Object[]> q = session.createQuery(
                    "select a.publishedAssessmentId, count(distinct a.agentId) " +
                            "from AssessmentGradingData a, AuthorizationData au, PublishedAssessmentData p " +
                            "where au.functionId = :fid and au.agentIdString = :site and a.publishedAssessmentId = au.qualifierId " +
                            "and a.forGrade = :forgrade and a.status > :status and a.publishedAssessmentId = p.publishedAssessmentId and " +
                            "(p.lastNeedResubmitDate is null or a.submittedDate >= p.lastNeedResubmitDate) group by a.publishedAssessmentId",
                    Object[].class);
            q.setParameter("fid", "OWN_PUBLISHED_ASSESSMENT");
            q.setParameter("site", siteId);
            q.setParameter("forgrade", true);
            q.setParameter("status", AssessmentGradingData.REMOVED);
            List<Object[]> list = q.list();
            
            HashMap<Long, Long> startedCountsMap = new HashMap<>();
            for (Object[] o : list) {
                startedCountsMap.put((Long) o[0], (Long) o[1]);
            }
            return startedCountsMap;
        } catch (Exception e) {
            log.warn("Error getting submitted counts for site {}: {}", siteId, e.toString());
            return new HashMap<>();
        }
    }

    public void completeItemGradingData(AssessmentGradingData assessmentGradingData) {
        completeItemGradingData(assessmentGradingData, null);
    }

    @Override
    public void completeItemGradingData(AssessmentGradingData assessmentGradingData, Map<Long, Set<PublishedSectionData>> sectionSetMap) {
        List<Long> publishedItemIds = getPublishedItemIds(assessmentGradingData.getAssessmentGradingId());
        List<Long> answeredPublishedItemIdList = publishedItemIds;

        PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
        Long publishedAssessmentId = assessmentGradingData.getPublishedAssessmentId();
        Set<PublishedSectionData> sectionSet;
        if (sectionSetMap == null || !sectionSetMap.containsKey(publishedAssessmentId)) {
            sectionSet = publishedAssessmentService.getSectionSetForAssessment(publishedAssessmentId);
            if (sectionSetMap != null) {
                sectionSetMap.put(publishedAssessmentId, sectionSet);
            }
        } else {
            sectionSet = (Set) sectionSetMap.get(publishedAssessmentId);
        }

        if (sectionSet == null) {
            return;
        }

        List<ItemDataIfc> itemArrayList;
        Long publishedItemId;
        PublishedItemData publishedItemData;
        for (PublishedSectionData publishedSectionData : sectionSet) {
            log.debug("sectionId = {}", publishedSectionData.getSectionId());
            itemArrayList = publishedSectionData.getItemArray();
            String authorType = publishedSectionData.getSectionMetaDataByLabel(SectionDataIfc.AUTHOR_TYPE);
            if (authorType != null && (authorType.equals(SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL.toString()) ||
                    authorType.equals(SectionDataIfc.FIXED_AND_RANDOM_DRAW_FROM_QUESTIONPOOL.toString()) || authorType.equals(SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOLS.toString()))) {
                log.debug("Fixed or Random draw from questionpool");
                long seed = AgentFacade.getAgentString().hashCode();

                // If the section has a previous seed we must use it to use the same order.
                String sectionRandomizationSeed = publishedSectionData.getSectionMetaDataByLabel(SectionDataIfc.RANDOMIZATION_SEED);
                if (StringUtils.isNotBlank(sectionRandomizationSeed)) {
                    seed += Long.parseLong(sectionRandomizationSeed);
                }

                if (publishedSectionData.getSectionMetaDataByLabel(SectionDataIfc.RANDOMIZATION_TYPE) != null && publishedSectionData
                        .getSectionMetaDataByLabel(SectionDataIfc.RANDOMIZATION_TYPE)
                        .equals(SectionDataIfc.PER_SUBMISSION)) {
                    seed = (assessmentGradingData.getAssessmentGradingId()
                            .toString() + "_" + publishedSectionData.getSectionId().toString()).hashCode();
                }

                if (authorType.equals(SectionDataIfc.FIXED_AND_RANDOM_DRAW_FROM_QUESTIONPOOL.toString())) {
                    // adding fixed questions
                    List<ItemDataIfc> sortedlist = itemArrayList.stream()
                        .filter(ItemDataIfc::getIsFixed)
                        .toList();

                    // removing isFixed questions from itemlist
                    itemArrayList.removeIf(ItemDataIfc::getIsFixed);

                    // getting all hashes from the sortedlist
                    List<String> distinctHashValues = sortedlist.stream()
                        .filter(item -> item instanceof PublishedItemData)
                        .map(ItemDataIfc::getHash)
                        .distinct()
                        .toList();

                    // removing from itemlist if there are hashes repeated -> avoid fixed questions on the random draw
                    itemArrayList.removeIf(item -> item instanceof PublishedItemData &&
                                                   distinctHashValues.stream().anyMatch(hash -> hash.equals(item.getHash())));
                }

                Collections.shuffle(itemArrayList, new Random(seed));

                int numberToBeDrawn = 0;
                if (publishedSectionData.getSectionMetaDataByLabel(SectionDataIfc.NUM_QUESTIONS_DRAWN) != null) {
                    numberToBeDrawn = Integer.parseInt(publishedSectionData.getSectionMetaDataByLabel(SectionDataIfc.NUM_QUESTIONS_DRAWN));
                }

                int samplesize = Math.min(numberToBeDrawn, itemArrayList.size());
                for (int i = 0; i < samplesize; i++) {
                    publishedItemData = (PublishedItemData) itemArrayList.get(i);
                    publishedItemId = publishedItemData.getItemId();
                    log.debug("publishedItemId = {}", publishedItemId);
                    if (!answeredPublishedItemIdList.contains(publishedItemId)) {
                        saveItemGradingData(assessmentGradingData, publishedItemId);
                    }
                }
            } else {
                log.debug("Not random draw from questionpool");
                for (ItemDataIfc pid : itemArrayList) {
                    publishedItemId = pid.getItemId();
                    log.debug("publishedItemId = {}", publishedItemId);
                    if (!answeredPublishedItemIdList.contains(publishedItemId)) {
                        saveItemGradingData(assessmentGradingData, publishedItemId);
                    }
                }
            }
        }
    }

    private void saveItemGradingData(AssessmentGradingData assessmentGradingData, Long publishedItemId) {
        log.debug("Adding one ItemGradingData...");
        ItemGradingData itemGradingData = new ItemGradingData();
        itemGradingData.setAssessmentGradingId(assessmentGradingData.getAssessmentGradingId());
        itemGradingData.setAgentId(assessmentGradingData.getAgentId());
        itemGradingData.setPublishedItemId(publishedItemId);
        ItemService itemService = new ItemService();
        Long itemTextId = itemService.getItemTextId(publishedItemId);
        log.debug("itemTextId = {}", itemTextId);
        if (itemTextId != -1) {
            itemGradingData.setPublishedItemTextId(itemTextId);
            //we're in the DAO su we can use the DAO method directly
            saveItemGrading(itemGradingData);
        }
    }

    public Double getAverageSubmittedAssessmentGrading(final Long publishedAssessmentId, final String agentId) {
        double averageScore = 0.0;
        try {
            Session session = getCurrentSession();
            Query<AssessmentGradingData> q = session.createQuery(
                    "from AssessmentGradingData a where a.publishedAssessmentId = :id and a.agentId = :agent and a.forGrade = :forgrade and a.status > :status order by a.submittedDate desc",
                    AssessmentGradingData.class);
            q.setParameter("id", publishedAssessmentId);
            q.setParameter("agent", agentId);
            q.setParameter("forgrade", true);
            q.setParameter("status", AssessmentGradingData.REMOVED);
            List<AssessmentGradingData> assessmentGradings = q.list();

            if (!assessmentGradings.isEmpty()) {
                Double cumulativeScore = 0D;
                for (AssessmentGradingData agd : assessmentGradings) {
                    if (agd.getFinalScore() != null) {
                        cumulativeScore += agd.getFinalScore();
                    }
                }
                averageScore = cumulativeScore / assessmentGradings.size();

                DecimalFormat df = new DecimalFormat("0.##");
                DecimalFormatSymbols dfs = new DecimalFormatSymbols();
                dfs.setDecimalSeparator('.');
                df.setDecimalFormatSymbols(dfs);

                averageScore = Double.valueOf(df.format(averageScore));
            }
            return averageScore;
        } catch (Exception e) {
            log.warn("Error getting average submitted assessment grading for assessment {} and agent {}: {}", 
                    publishedAssessmentId, agentId, e.toString());
            return 0.0;
        }
    }

    public List<AssessmentGradingData> getHighestSubmittedAssessmentGradingList(final Long publishedAssessmentId) {

        try {
            Session session = getCurrentSession();
            Query<AssessmentGradingData> query = session.createQuery(
                    "from AssessmentGradingData a where a.publishedAssessmentId = :id and a.forGrade = :forgrade and a.status > :status order by a.agentId asc, a.finalScore desc",
                    AssessmentGradingData.class);
            query.setParameter("id", publishedAssessmentId);
            query.setParameter("forgrade", true);
            query.setParameter("status", AssessmentGradingData.REMOVED);
            List<AssessmentGradingData> assessmentGradings = query.list();

            return new ArrayList<>(assessmentGradings.stream()
                    .collect(Collectors.toMap(AssessmentGradingData::getAgentId, p -> p, (p, q) -> p))
                    .values());
        } catch (Exception e) {
            log.warn("Error getting highest submitted assessment grading list for assessment {}: {}", publishedAssessmentId, e.toString());
            return new ArrayList<>();
        }
    }

    public Map<Long, List<Long>> getAverageAssessmentGradingByPublishedItem(final Long publishedAssessmentId) {
        Map<Long, List<Long>> h = new HashMap<>();

        try {
            Session session = getCurrentSession();
            Query<AssessmentGradingData> q = session.createQuery(
                    "select new AssessmentGradingData(" +
                            " a.assessmentGradingId, p.itemId, " +
                            " a.agentId, a.finalScore, a.submittedDate) " +
                            " from ItemGradingData i, AssessmentGradingData a," +
                            " PublishedItemData p where " +
                            " i.assessmentGradingId = a.assessmentGradingId and i.publishedItemId = p.itemId and " +
                            " a.publishedAssessmentId = :id and a.status > :status" +
                            " order by a.agentId asc, a.submittedDate desc",
                    AssessmentGradingData.class);
            q.setParameter("id", publishedAssessmentId);
            q.setParameter("status", AssessmentGradingData.REMOVED);
            List<AssessmentGradingData> assessmentGradings = q.list();

            String currentAgent = "";
            Date submittedDate = null;
            for (int i = 0; i < assessmentGradings.size(); i++) {
                AssessmentGradingData g = assessmentGradings.get(i);
                Long itemId = g.getPublishedItemId();
                Long gradingId = g.getAssessmentGradingId();
                if (i == 0) {
                    currentAgent = g.getAgentId();
                    submittedDate = g.getSubmittedDate();
                }
                if (currentAgent.equals(g.getAgentId())
                        && ((submittedDate == null && g.getSubmittedDate() == null)
                        || (submittedDate != null && submittedDate.equals(g.getSubmittedDate())))) {
                    List<Long> o = h.get(itemId);
                    if (o != null) {
                        o.add(gradingId);
                    } else {
                        List<Long> gradingIds = new ArrayList<>();
                        gradingIds.add(gradingId);
                        h.put(itemId, gradingIds);
                    }
                }
                if (!currentAgent.equals(g.getAgentId())) {
                    currentAgent = g.getAgentId();
                    submittedDate = g.getSubmittedDate();
                }
            }
            return h;
        } catch (Exception e) {
            log.warn("Error getting average assessment grading by published item for assessment {}: {}", publishedAssessmentId, e.toString());
            return new HashMap<>();
        }
    }

    private Map<Long, Set<ItemGradingAttachment>> getItemGradingAttachmentMap(final Set itemGradingIds) {

        try {
            Session session = getCurrentSession();
            Query<ItemGradingAttachment> q = session.createQuery(
                    "from ItemGradingAttachment a where a.itemGrading.itemGradingId in (:itemGradingIds)",
                    ItemGradingAttachment.class);
            q.setParameterList("itemGradingIds", itemGradingIds);
            List<ItemGradingAttachment> list = q.list();
            Set<ItemGradingAttachment> itemGradingAttachmentList = new HashSet<>(list);
            return processItemGradingAttachment(itemGradingAttachmentList);
        } catch (Exception e) {
            log.warn("Error getting item grading attachment map for itemGradingIds: {}", e.toString());
            return new HashMap<>();
        }
    }

    private Map<Long, Set<ItemGradingAttachment>> getItemGradingAttachmentMap(final Long publishedItemId) {

        try {
            Session session = getCurrentSession();
            Query<ItemGradingAttachment> q = session.createQuery(
                    "select a from ItemGradingAttachment a where a.itemGrading.publishedItemId = :publishedItemId",
                    ItemGradingAttachment.class);
            q.setParameter("publishedItemId", publishedItemId);
            List<ItemGradingAttachment> list = q.list();
            Set<ItemGradingAttachment> itemGradingAttachmentSet = new HashSet<>(list);
            return processItemGradingAttachment(itemGradingAttachmentSet);
        } catch (Exception e) {
            log.warn("Error getting item grading attachment map for publishedItemId {}: {}", publishedItemId, e.toString());
            return new HashMap<>();
        }
    }

    public Map<Long, List<AssessmentGradingAttachment>> getAssessmentGradingAttachmentMap(final Long pubAssessmentId) {

        try {
            Session session = getCurrentSession();
            Query<AssessmentGradingAttachment> q = session.createQuery(
                    "select a from AssessmentGradingAttachment a where a.assessmentGrading.publishedAssessmentId = :pubAssessmentId",
                    AssessmentGradingAttachment.class);
            q.setParameter("pubAssessmentId", pubAssessmentId);
            List<AssessmentGradingAttachment> assessmentGradingAttachmentList = q.list();
            return processAssessmentGradingAttachment(assessmentGradingAttachmentList);
        } catch (Exception e) {
            log.warn("Error getting assessment grading attachment map for assessment {}: {}", pubAssessmentId, e.toString());
            return new HashMap<>();
        }
    }

    public Map<Long, Set<ItemGradingAttachment>> getItemGradingAttachmentMapByAssessmentGradingId(final Long assessmentGradingId) {

        try {
            Session session = getCurrentSession();
            Query<ItemGradingAttachment> q = session.createQuery(
                    "select a from ItemGradingAttachment a, ItemGradingData i " +
                            "where a.itemGrading.itemGradingId = i.itemGradingId " +
                            "and i.assessmentGradingId = :assessmentGradingId",
                    ItemGradingAttachment.class);
            q.setParameter("assessmentGradingId", assessmentGradingId);
            List<ItemGradingAttachment> list = q.list();
            Set<ItemGradingAttachment> itemGradingAttachmentList = new HashSet<>(list);
            return processItemGradingAttachment(itemGradingAttachmentList);
        } catch (Exception e) {
            log.warn("Error getting item grading attachment map for assessment grading {}: {}", assessmentGradingId, e.toString());
            return new HashMap<>();
        }
    }

    private Map<Long, Set<ItemGradingAttachment>> processItemGradingAttachment(Set<ItemGradingAttachment> itemGradingAttachmentSet) {

        Map<Long, Set<ItemGradingAttachment>> itemGradingAttachmentMap = new HashMap<>();
        for (ItemGradingAttachment attachment : itemGradingAttachmentSet) {
            Long itemGrdingId = attachment.getItemGrading().getItemGradingId();
            Set<ItemGradingAttachment> attachmentSet;
            if (itemGradingAttachmentMap.containsKey(itemGrdingId)) {
                attachmentSet = itemGradingAttachmentMap.get(itemGrdingId);
            } else {
                attachmentSet = new HashSet<>();
            }
            attachmentSet.add(attachment);
            itemGradingAttachmentMap.put(itemGrdingId, attachmentSet);
        }

        return itemGradingAttachmentMap;
    }

    private Map<Long, List<AssessmentGradingAttachment>> processAssessmentGradingAttachment(
            List<AssessmentGradingAttachment> assessmentGradingAttachmentList) {

        Map<Long, List<AssessmentGradingAttachment>> assessmentGradingAttachmentMap = new HashMap<>();
        for (int i = 0; i < assessmentGradingAttachmentList.size(); i++) {
            AssessmentGradingAttachment attachment = assessmentGradingAttachmentList.get(i);
            Long assessGradingId = attachment.getAssessmentGrading().getAssessmentGradingId();
            List<AssessmentGradingAttachment> attachmentList = new ArrayList<>();
            if (assessmentGradingAttachmentMap.containsKey(assessGradingId)) {
                attachmentList = assessmentGradingAttachmentMap.get(assessGradingId);
            }

            attachmentList.add(attachment);
            assessmentGradingAttachmentMap.put(assessGradingId, attachmentList);
        }

        return assessmentGradingAttachmentMap;
    }

    /**
     * This is a dummy class for sections that are made up of random questions
     * from a pool
     */
    private static class EmptyItemGrading extends ItemGradingData {
        /**
         *
         */
        private static final long serialVersionUID = 1444166131103415747L;
        private Integer sectionSequence;
        private Long publishedItemId;
        private Integer itemSequence;

        EmptyItemGrading(Integer sectionSequence, Long publishedItemId, Integer itemSequence) {
            this.sectionSequence = sectionSequence;
            this.publishedItemId = publishedItemId;
            this.itemSequence = itemSequence;
        }

        /**
         * @return the itemSequence
         */

        public Integer getItemSequence() {
            return itemSequence;
        }

        public Integer getSectionSequence() {
            return sectionSequence;
        }

    }

    public List<AssessmentGradingData> getUnSubmittedAssessmentGradingDataList(final Long publishedAssessmentId, final String agentIdString) {
        try {
            Session session = getCurrentSession();
            Query<AssessmentGradingData> q = session.createQuery(
                    "from AssessmentGradingData a where a.publishedAssessmentId = :id and a.agentId = :agent and a.forGrade = :forgrade and a.status > :status order by a.attemptDate desc",
                    AssessmentGradingData.class);
            q.setParameter("id", publishedAssessmentId);
            q.setParameter("agent", agentIdString);
            q.setParameter("forgrade", false);
            q.setParameter("status", AssessmentGradingData.REMOVED);
            return q.list();
        } catch (Exception e) {
            log.warn("Error getting unsubmitted assessment grading data list for assessment {} and agent {}: {}", 
                    publishedAssessmentId, agentIdString, e.toString());
            return new ArrayList<>();
        }
    }

    public SectionGradingData getSectionGradingData(Long assessmentGradingId, Long sectionId, String agentId) {
        try {
            Session session = getCurrentSession();
            Query<SectionGradingData> q = session.createQuery(
                    "from SectionGradingData s where " +
                        "s.assessmentGradingId = :assessmentGradingId " +
                        "and s.publishedSectionId = :sectionId " +
                        "and s.agentId = :agent",
                    SectionGradingData.class);
            q.setParameter("assessmentGradingId", assessmentGradingId);
            q.setParameter("sectionId", sectionId);
            q.setParameter("agent", agentId);
            List<SectionGradingData> sectionGradings = q.list();
            
            if (sectionGradings.isEmpty()) {
                return null;
            }
            return sectionGradings.get(0);
        } catch (Exception e) {
            log.warn("Error getting section grading data for assessment {}, section {} and agent {}: {}", 
                    assessmentGradingId, sectionId, agentId, e.toString());
            return null;
        }
    }

    public void saveSectionGrading(SectionGradingData item) {
        int retryCount = persistenceHelper.getRetryCount();
        while (retryCount > 0) {
            try {
                Session session = getCurrentSession();
                session.merge(item);
                retryCount = 0;
            } catch (Exception e) {
                log.warn("problem saving sectionGrading: " + e.toString());
                retryCount = persistenceHelper.retryDeadlock(e, retryCount);
            }
        }
    }
}
