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
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.NullPrecedence;
import org.hibernate.query.Query;
import org.sakaiproject.antivirus.api.VirusFoundException;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
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
import org.sakaiproject.user.api.UserDirectoryService;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.tool.assessment.services.PersistenceService;

@Slf4j
public class AssessmentGradingFacadeQueries extends HibernateDaoSupport implements AssessmentGradingFacadeQueriesAPI {

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
            final HibernateCallback<List<AssessmentGradingData>> hcb = session -> {
                Criteria q = session.createCriteria(AssessmentGradingData.class)
                        .add(Restrictions.eq("publishedAssessmentId", publishedId))
                        .add(Restrictions.gt("status", AssessmentGradingData.REMOVED))
                        .addOrder(Order.asc("agentId").nulls(NullPrecedence.LAST))
                        .addOrder(Order.desc("finalScore").nulls(NullPrecedence.LAST))
                        .addOrder(Order.desc("submittedDate").nulls(NullPrecedence.LAST));

                if (getSubmittedOnly) {
                    q.add(Restrictions.eq("forGrade", true));
                } else {
                    q.add(Restrictions.or(
                            Restrictions.eq("forGrade", true),
                            Restrictions.and(
                                    Restrictions.eq("forGrade", false),
                                    Restrictions.eq("status", AssessmentGradingData.NO_SUBMISSION))));
                }

                return q.list();
            };
            List<AssessmentGradingData> list = getHibernateTemplate().execute(hcb);
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
                final HibernateCallback<List<AssessmentGradingData>> hcb2 = session -> {
                    Criteria q = session.createCriteria(AssessmentGradingData.class)
                            .add(Restrictions.eq("publishedAssessmentId", publishedId))
                            .add(Restrictions.gt("status", AssessmentGradingData.REMOVED))
                            .addOrder(Order.asc("agentId").nulls(NullPrecedence.LAST))
                            .addOrder(Order.desc("submittedDate").nulls(NullPrecedence.LAST));

                    if (getSubmittedOnly) {
                        q.add(Restrictions.eq("forGrade", true));
                    } else {
                        q.add(Restrictions.or(
                                Restrictions.eq("forGrade", true),
                                Restrictions.and(
                                        Restrictions.eq("forGrade", false),
                                        Restrictions.eq("status", AssessmentGradingData.NO_SUBMISSION))));
                    }
                    return q.list();
                };
                list = getHibernateTemplate().execute(hcb2);
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
            log.warn(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    public List<AssessmentGradingData> getAllSubmissions(final String publishedId) {
        final HibernateCallback<List<AssessmentGradingData>> hcb = session -> {
            Query q = session.createQuery(
                    "from AssessmentGradingData a where a.publishedAssessmentId = :id and a.forGrade = :forgrade and a.status > :status");
            q.setParameter("id", Long.parseLong(publishedId));
            q.setParameter("forgrade", true);
            q.setParameter("status", AssessmentGradingData.REMOVED);
            return q.list();
        };
        return getHibernateTemplate().execute(hcb);
    }

    public List<AssessmentGradingData> getAllAssessmentGradingData(final Long publishedId) {
        final HibernateCallback<List<AssessmentGradingData>> hcb = session -> {
            Query q = session.createQuery(
                    "from AssessmentGradingData a where a.publishedAssessmentId = :id and a.status <> :status and a.status <> :removed order by a.agentId asc, a.submittedDate desc");
            q.setParameter("id", publishedId);
            q.setParameter("status", AssessmentGradingData.NO_SUBMISSION);
            q.setParameter("removed", AssessmentGradingData.REMOVED);
            return q.list();
        };
        List<AssessmentGradingData> list = getHibernateTemplate().execute(hcb);

        list.forEach(agd -> agd.setItemGradingSet(getItemGradingSet(agd.getAssessmentGradingId())));

        return list;
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
            HashMap<Long, List<ItemGradingData>> map = new HashMap<>();

            HibernateCallback<List<ItemGradingData>> hcb = session -> {
                Criteria criteria = session.createCriteria(ItemGradingData.class);
                Disjunction disjunction = Expression.disjunction();

                /** make list from AssessmentGradingData ids */
                List<Long> gradingIdList = scores.stream()
                        .map(AssessmentGradingData::getAssessmentGradingId)
                        .collect(Collectors.toList());

                /** create or disjunctive expression for (in clauses) */
                List tempList;
                for (int i = 0; i < gradingIdList.size(); i += 50) {
                    if (i + 50 > gradingIdList.size()) {
                        tempList = gradingIdList.subList(i, gradingIdList.size());
                        disjunction.add(Expression.in("assessmentGradingId", tempList));
                    } else {
                        tempList = gradingIdList.subList(i, i + 50);
                        disjunction.add(Expression.in("assessmentGradingId", tempList));
                    }
                }

                if (itemId.equals(Long.valueOf(0))) {
                    criteria.add(disjunction);
                    //criteria.add(Expression.isNotNull("submittedDate"));
                } else {

                    /** create logical and between the pubCriterion and the disjunction criterion */
                    //Criterion pubCriterion = Expression.eq("publishedItem.itemId", itemId);
                    Criterion pubCriterion = Expression.eq("publishedItemId", itemId);
                    criteria.add(Expression.and(pubCriterion, disjunction));
                    //criteria.add(Expression.isNotNull("submittedDate"));
                }
                criteria.addOrder(Order.asc("agentId").nulls(NullPrecedence.LAST));
                criteria.addOrder(Order.desc("submittedDate").nulls(NullPrecedence.LAST));
                return criteria.list();
                //large list cause out of memory error (java heap space)
                //return criteria.setMaxResults(10000).list();
            };
            List<ItemGradingData> temp = getHibernateTemplate().execute(hcb);

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
            log.warn(e.getMessage(), e);
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
            final HibernateCallback<List<AssessmentGradingData>> hcb = session -> {
                // I am debating should I use (a.forGrade=false and a.status=NO_SUBMISSION) or attemptDate is not null
                Query q = session.createQuery(
                        "from AssessmentGradingData a where a.publishedAssessmentId = :id " +
                                "and a.agentId = :agent and a.forGrade = :forgrade and a.status <> :status and a.status <> :removed " +
                                "order by a.submittedDate DESC");
                q.setParameter("id", publishedId);
                q.setParameter("agent", agentId);
                q.setParameter("forgrade", false);
                q.setParameter("status", AssessmentGradingData.NO_SUBMISSION);
                q.setParameter("removed", AssessmentGradingData.REMOVED);
                return q.list();
            };
            List<AssessmentGradingData> scores = getHibernateTemplate().execute(hcb);

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
            log.warn(e.getMessage(), e);
            return new HashMap<>();
        }
    }


    /**
     * This returns a hashmap of all the submitted items, keyed by
     * item id for easy retrieval.
     *
     * @param assessmentGradingId
     * @return
     */
    public HashMap getStudentGradingData(String assessmentGradingId) {
        return getStudentGradingData(assessmentGradingId, true);
    }

    public HashMap getStudentGradingData(String assessmentGradingId, boolean loadGradingAttachment) {
        try {
            HashMap map = new HashMap();
            AssessmentGradingData gdata = load(new Long(assessmentGradingId), loadGradingAttachment);
            log.debug("****#6, gdata=" + gdata);
            for (ItemGradingData data : gdata.getItemGradingSet()) {
                ArrayList thisone = (ArrayList)
                        map.get(data.getPublishedItemId());
                if (thisone == null)
                    thisone = new ArrayList();
                thisone.add(data);
                map.put(data.getPublishedItemId(), thisone);
            }
            return map;
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return new HashMap();
        }
    }


    public Map<Long, List<ItemGradingData>> getSubmitData(final Long publishedId, final String agentId, final Integer scoringoption, final Long assessmentGradingId) {
        try {
            final HibernateCallback<List<AssessmentGradingData>> hcb = session -> {
                log.debug("scoringoption = " + scoringoption);
                if (EvaluationModelIfc.LAST_SCORE.equals(scoringoption)) {
                    // last submission
                    Query q;
                    if (assessmentGradingId == null) {
                        q = session.createQuery(
                                "from AssessmentGradingData a where a.publishedAssessmentId = :id and a.agentId = :agent and a.forGrade = :forgrade and a.status > :status order by a.submittedDate DESC");
                        q.setParameter("id", publishedId);
                        q.setParameter("agent", agentId);
                        q.setParameter("forgrade", true);
                        q.setParameter("status", AssessmentGradingData.REMOVED);
                    } else {
                        q = session.createQuery("from AssessmentGradingData a where a.assessmentGradingId = :id");
                        q.setParameter("id", assessmentGradingId);
                    }
                    return q.list();
                } else {
                    //highest submission
                    Query q1 = null;
                    if (assessmentGradingId == null) {
                        q1 = session.createQuery(
                                "from AssessmentGradingData a where a.publishedAssessmentId = :id and a.agentId = :agent and a.forGrade = :forgrade and a.status > :status order by a.finalScore DESC, a.submittedDate DESC");
                        q1.setParameter("id", publishedId);
                        q1.setParameter("agent", agentId);
                        q1.setParameter("forgrade", true);
                        q1.setParameter("status", AssessmentGradingData.REMOVED);
                    } else {
                        q1 = session.createQuery("from AssessmentGradingData a where a.assessmentGradingId = :id");
                        q1.setParameter("id", assessmentGradingId);
                    }
                    return q1.list();
                }
            };
            List<AssessmentGradingData> scores = getHibernateTemplate().execute(hcb);

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
            log.warn(e.getMessage(), e);
            return new HashMap<>();
        }
    }

    public Long add(AssessmentGradingData a) {
        int retryCount = persistenceHelper.getRetryCount();
        while (retryCount > 0) {
            try {
                getHibernateTemplate().save(a);
                retryCount = 0;
            } catch (Exception e) {
                log.warn("problem adding assessmentGrading: " + e.getMessage());
                retryCount = persistenceHelper.retryDeadlock(e, retryCount);
            }
        }
        return a.getAssessmentGradingId();
    }

    public int getSubmissionSizeOfPublishedAssessment(Long publishedAssessmentId) {
        Number size = (Number) getHibernateTemplate().execute(session -> session.createCriteria(AssessmentGradingData.class)
                .add(Restrictions.eq("publishedAssessmentId", publishedAssessmentId))
                .add(Restrictions.eq("forGrade", true))
                .setProjection(Projections.rowCount())
                .uniqueResult());
        return size.intValue();
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
                getHibernateTemplate().saveOrUpdate(mediaData);
                retryCount = 0;
            } catch (Exception e) {
                log.warn("problem saving media: " + e.getMessage());
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
                mediaLocation = mediaData.getLocation();
                mediaFilename = mediaData.getFilename();
                getHibernateTemplate().delete(mediaData);
                retryCount = 0;
            } catch (Exception e) {
                log.warn("Problem deleting media with Id {}",  mediaId);
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
            itemGradingData.setAutoScore(Double.valueOf(0));
            saveItemGrading(itemGradingData);
            EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_ATTACHMENT_DELETE, "itemGradingId=" + itemGradingData.getItemGradingId() + ", " + mediaFilename, null, true, NotificationService.NOTI_REQUIRED));
        }
    }

    public MediaData getMedia(Long mediaId) {

        MediaData mediaData = (MediaData) getHibernateTemplate().load(MediaData.class, mediaId);

        // Only try to read from Content Hosting if this isn't a link and
        // there is no media content in the database
        if (mediaData.getLocation() == null) {
            mediaData.setContentResource(getMediaContentResource(mediaData));
        }
        return mediaData;
    }

    public List<MediaData> getMediaArray(final Long itemGradingId) {
        log.debug("*** itemGradingId =" + itemGradingId);
        List<MediaData> a = new ArrayList<>();

        final HibernateCallback<List<MediaData>> hcb = session -> {
            Query q = session.createQuery("from MediaData m where m.itemGradingData.itemGradingId = :id");
            q.setParameter("id", itemGradingId);
            return q.list();
        };
        List<MediaData> list = getHibernateTemplate().execute(hcb);

        for (MediaData mediaData : list) {
            mediaData.setContentResource(getMediaContentResource(mediaData));
            a.add(mediaData);
        }
        log.debug("*** no. of media = {}", a.size());
        return a;
    }

    public List<MediaData> getMediaArray2(final Long itemGradingId) {
        log.debug("*** itemGradingId =" + itemGradingId);
        List<MediaData> a = new ArrayList<>();
        final HibernateCallback<List<MediaData>> hcb = session -> {
            Query q = session.createQuery(
                    "select new MediaData(m.mediaId, m.filename, m.fileSize, m.duration, m.createdDate) " +
                            " from MediaData m where m.itemGradingData.itemGradingId = :id");
            q.setParameter("id", itemGradingId);
            return q.list();
        };
        List<MediaData> list = getHibernateTemplate().execute(hcb);

        for (MediaData mediaData : list) {
            mediaData.setContentResource(getMediaContentResource(mediaData));
            a.add(mediaData);
        }
        log.debug("*** no. of media = {}", a.size());
        return a;
    }

    public Map<Long, List<ItemGradingData>> getMediaItemGradingHash(final Long assessmentGradingId) {
        log.debug("*** assessmentGradingId = {}", assessmentGradingId);
        Map<Long, List<ItemGradingData>> map = new HashMap<>();

        final HibernateCallback<List<ItemGradingData>> hcb = session -> {
            Query q = session.createQuery(
                    "select i from MediaData m, ItemGradingData i " +
                            "where m.itemGradingData.itemGradingId = i.itemGradingId " +
                            "and i.assessmentGradingId = :id");
            q.setParameter("id", assessmentGradingId);
            return q.list();
        };
        List<ItemGradingData> list = getHibernateTemplate().execute(hcb);

        for (ItemGradingData itemGradingData : list) {
            List<ItemGradingData> al = new ArrayList<>();
            al.add(itemGradingData);
            // There might be duplicate. But we just overwrite it with the same itemGradingData
            map.put(itemGradingData.getPublishedItemId(), al);
        }
        log.debug("*** no. of media = {}", map.size());
        return map;
    }

    public ArrayList getMediaArray(ItemGradingData item) {
        ArrayList a = new ArrayList();
        List list = getHibernateTemplate().findByNamedParam(
                "from MediaData m where m.itemGradingData = :id", "id", item);
        for (int i = 0; i < list.size(); i++) {
            MediaData mediaData = (MediaData) list.get(i);
            mediaData.setContentResource(getMediaContentResource(mediaData));
            a.add(mediaData);
        }
        log.debug("*** no. of media = {}", a.size());
        return a;
    }

    public List<MediaData> getMediaArray(Long publishedId, final Long publishedItemId, String which) {
        try {
            Map<Long, List<ItemGradingData>> itemScores = getItemScores(publishedId, publishedItemId, which);
            final List<ItemGradingData> list = itemScores.get(publishedItemId);
            log.debug("list size list.size() = " + list.size());

            HibernateCallback<List<MediaData>> hcb = session -> {
                Criteria criteria = session.createCriteria(MediaData.class);
                Disjunction disjunction = Expression.disjunction();

                /** make list from AssessmentGradingData ids */
                List<Long> itemGradingIdList = list.stream()
                        .map(ItemGradingData::getItemGradingId)
                        .collect(Collectors.toList());

                /** create or disjunctive expression for (in clauses) */
                List<Long> tempList;
                for (int i = 0; i < itemGradingIdList.size(); i += 50) {
                    if (i + 50 > itemGradingIdList.size()) {
                        tempList = itemGradingIdList.subList(i, itemGradingIdList.size());
                        disjunction.add(Expression.in("itemGradingData.itemGradingId", tempList));
                    } else {
                        tempList = itemGradingIdList.subList(i, i + 50);
                        disjunction.add(Expression.in("itemGradingData.itemGradingId", tempList));
                    }
                }
                criteria.add(disjunction);
                return criteria.list();
                //large list cause out of memory error (java heap space)
                //return criteria.setMaxResults(10000).list();
            };

            List<MediaData> a = new ArrayList<>();
            List<MediaData> hbmList = getHibernateTemplate().execute(hcb);
            for (MediaData mediaData : hbmList) {
                mediaData.setContentResource(getMediaContentResource(mediaData));
                a.add(mediaData);
            }
            return a;

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public List<Long> getMediaConversionBatch() {
        final HibernateCallback<List<Long>> hcb = session -> {
            Query q = session.createQuery("SELECT id FROM MediaData WHERE dbMedia IS NOT NULL AND location IS NULL");
            q.setMaxResults(10);
            return q.list();
        };
        return getHibernateTemplate().execute(hcb);
    }

    public boolean markMediaForConversion(final List<Long> mediaIds) {
        final HibernateCallback<Integer> hcb = session -> {
            Query q = session.createQuery("UPDATE MediaData SET location = 'CONVERTING' WHERE id in (:ids)");
            q.setParameterList("ids", mediaIds);
            return q.executeUpdate();
        };
        return getHibernateTemplate().execute(hcb).equals(mediaIds.size());
    }

    public List<Long> getMediaWithDataAndLocation() {
        final HibernateCallback<List<Long>> hcb = session -> {
            Query q = session.createQuery("SELECT id FROM MediaData WHERE dbMedia IS NOT NULL AND location IS NOT NULL");
            return q.list();
        };
        return getHibernateTemplate().execute(hcb);
    }

    public List<Long> getMediaInConversion() {
        final HibernateCallback<List<Long>> hcb = session -> {
            Query q = session.createQuery("SELECT id FROM MediaData WHERE location = 'CONVERTING'");
            return q.list();
        };
        return getHibernateTemplate().execute(hcb);
    }

    public ItemGradingData getLastItemGradingDataByAgent(final Long publishedItemId, final String agentId) {
        final HibernateCallback<List<ItemGradingData>> hcb = session -> {
            Query q = session.createQuery("from ItemGradingData i where i.publishedItemId = :id and i.agentId = :agent");
            q.setParameter("id", publishedItemId);
            q.setParameter("agent", agentId);
            return q.list();
        };
        List<ItemGradingData> itemGradings = getHibernateTemplate().execute(hcb);
        if (itemGradings.isEmpty()) {
            return null;
        }
        return itemGradings.get(0);
    }

    public ItemGradingData getItemGradingData(final Long itemGradingId) {
        final HibernateCallback<List<ItemGradingData>> hcb = session -> {
            Query q = session.createQuery("from ItemGradingData i where i.itemGradingId = :id");
            q.setParameter("id", itemGradingId);
            return q.list();
        };
        List<ItemGradingData> itemGradings = getHibernateTemplate().execute(hcb);
        if (itemGradings.isEmpty()) {
            return null;
        }
        ;
        return itemGradings.get(0);
    }

    public ItemGradingData getItemGradingData(final Long assessmentGradingId, final Long publishedItemId) {
        log.debug("****assessmentGradingId={}", assessmentGradingId);
        log.debug("****publishedItemId={}", publishedItemId);

        final HibernateCallback<List<ItemGradingData>> hcb = session -> {
            Query q = session.createQuery(
                    "from ItemGradingData i where i.assessmentGradingId = :gradingid and i.publishedItemId = :itemid");
            q.setParameter("gradingid", assessmentGradingId);
            q.setParameter("itemid", publishedItemId);
            return q.list();
        };
        List<ItemGradingData> itemGradings = getHibernateTemplate().execute(hcb);

        if (itemGradings.isEmpty()) {
            return null;
        }
        return itemGradings.get(0);
    }

    public AssessmentGradingData load(Long id) {
        return load(id, true);
    }

    public AssessmentGradingData load(Long id, boolean loadGradingAttachment) {
        AssessmentGradingData gdata = (AssessmentGradingData) getHibernateTemplate().load(AssessmentGradingData.class,
                id);
        Set<ItemGradingData> itemGradingSet = new HashSet();

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
    }

    public ItemGradingData getItemGrading(Long id) {
        return (ItemGradingData) getHibernateTemplate().load(ItemGradingData.class, id);
    }

    public AssessmentGradingData getLastSavedAssessmentGradingByAgentId(final Long publishedAssessmentId, final String agentIdString) {
        AssessmentGradingData ag = null;
        // don't pick the assessmentGradingData that is created by instructor entering comments/scores
        final HibernateCallback<List<AssessmentGradingData>> hcb = session -> {
            Query q = session.createQuery(
                    "from AssessmentGradingData a where a.publishedAssessmentId = :id and a.agentId = :agent and a.forGrade = :forgrade and a.status not in (:status1, :status2) order by a.submittedDate desc");
            q.setParameter("id", publishedAssessmentId);
            q.setParameter("agent", agentIdString);
            q.setParameter("forgrade", false);
            q.setParameter("status1", AssessmentGradingData.NO_SUBMISSION);
            q.setParameter("status2", AssessmentGradingData.REMOVED);
            return q.list();
        };
        List<AssessmentGradingData> assessmentGradings = getHibernateTemplate().execute(hcb);

        if (!assessmentGradings.isEmpty()) {
            ag = assessmentGradings.get(0);
            ag.setItemGradingSet(getItemGradingSet(ag.getAssessmentGradingId()));
        }
        return ag;
    }

    public AssessmentGradingData getLastSubmittedAssessmentGradingByAgentId(final Long publishedAssessmentId, final String agentIdString, Long assessmentGradingId) {
        AssessmentGradingData ag = null;

        final HibernateCallback<List<AssessmentGradingData>> hcb = session -> {
            Query q = session.createQuery(
                    "from AssessmentGradingData a where a.publishedAssessmentId = :id and a.agentId = :agent and a.forGrade = :forgrade and a.status > :status order by a.submittedDate desc");
            q.setParameter("id", publishedAssessmentId);
            q.setParameter("agent", agentIdString);
            q.setParameter("forgrade", true);
            q.setParameter("status", AssessmentGradingData.REMOVED);
            return q.list();
        };
        List<AssessmentGradingData> assessmentGradings = getHibernateTemplate().execute(hcb);

        if (assessmentGradingId == null) {
            if (assessmentGradings.size() > 0) {
                ag = assessmentGradings.get(0);
            }
        } else {
            for (int i = 0; i < assessmentGradings.size(); i++) {
                AssessmentGradingData agd = assessmentGradings.get(i);
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
	            ag.setAssessmentGradingAttachmentList(new ArrayList<AssessmentGradingAttachment>());
	        }
        }

        return ag;
    }

    public AssessmentGradingData getLastAssessmentGradingByAgentId(final Long publishedAssessmentId, final String agentIdString) {
        AssessmentGradingData ag = null;

        final HibernateCallback<List<AssessmentGradingData>> hcb = session -> {
            Query q = session.createQuery(
                    "from AssessmentGradingData a where a.publishedAssessmentId = :id and a.agentId = :agent and a.status > :status order by a.submittedDate desc");
            q.setParameter("id", publishedAssessmentId);
            q.setParameter("agent", agentIdString);
            q.setParameter("status", AssessmentGradingData.REMOVED);
            return q.list();
        };
        List<AssessmentGradingData> assessmentGradings = getHibernateTemplate().execute(hcb);

        if (!assessmentGradings.isEmpty()) {
            ag = assessmentGradings.get(0);
            ag.setItemGradingSet(getItemGradingSet(ag.getAssessmentGradingId()));
        }
        return ag;
    }

    public void saveItemGrading(ItemGradingData item) {
        int retryCount = persistenceHelper.getRetryCount();
        while (retryCount > 0) {
            try {
                getHibernateTemplate().saveOrUpdate(item);
                retryCount = 0;
            } catch (Exception e) {
                log.warn("problem saving itemGrading: " + e.getMessage());
                retryCount = persistenceHelper.retryDeadlock(e, retryCount);
            }
        }
    }

    public boolean saveOrUpdateAssessmentGrading(AssessmentGradingData assessment) {
        int retryCount = persistenceHelper.getRetryCount();
        boolean success = false;
        while (retryCount > 0) {
            try {
                if (assessment.getAssessmentGradingId() != null) {
                    getHibernateTemplate().merge((AssessmentGradingData) assessment);
                }
                else {
                    getHibernateTemplate().save((AssessmentGradingData) assessment);
                }
                retryCount = 0;
				success = true;
            } catch (Exception e) {
                log.warn("problem inserting/updating assessmentGrading: {}", e.getMessage());
                retryCount = persistenceHelper.retryDeadlock(e, retryCount);
            }
        }
        return success;
    }

    public List<Long> getAssessmentGradingIds(final Long publishedItemId) {
        final HibernateCallback<List<Long>> hcb = session -> {
            Query q = session.createQuery(
                    "select g.assessmentGradingId from ItemGradingData g where g.publishedItemId = :id");
            q.setParameter("id", publishedItemId);
            return q.list();
        };
        return getHibernateTemplate().execute(hcb);
    }

    public AssessmentGradingData getHighestAssessmentGrading(final Long publishedAssessmentId, final String agentId) {
        AssessmentGradingData ag = null;

        final HibernateCallback<List<AssessmentGradingData>> hcb = session -> {
            Query q = session.createQuery(
                    "from AssessmentGradingData a where a.publishedAssessmentId = :id and " +
                            " a.agentId = :agent and a.status > :status order by a.finalScore desc, a.submittedDate desc");
            q.setParameter("id", publishedAssessmentId);
            q.setParameter("agent", agentId);
            q.setParameter("status", AssessmentGradingData.REMOVED);
            return q.list();
        };
        List<AssessmentGradingData> assessmentGradings = getHibernateTemplate().execute(hcb);

        if (!assessmentGradings.isEmpty()) {
            ag = assessmentGradings.get(0);
            ag.setItemGradingSet(getItemGradingSet(ag.getAssessmentGradingId()));
        }
        return ag;
    }

    public AssessmentGradingData getHighestSubmittedAssessmentGrading(final Long publishedAssessmentId, final String agentId, Long assessmentGradingId) {
        AssessmentGradingData ag = null;

        final HibernateCallback<List<AssessmentGradingData>> hcb = session -> {
            Query q = session.createQuery(
                    "from AssessmentGradingData a where a.publishedAssessmentId = :id and a.agentId = :agent and " +
                            " a.forGrade = :forgrade and a.status > :status order by a.finalScore desc, a.submittedDate desc");
            q.setParameter("id", publishedAssessmentId);
            q.setParameter("agent", agentId);
            q.setParameter("forgrade", true);
            q.setParameter("status", AssessmentGradingData.REMOVED);
            return q.list();
        };
        List<AssessmentGradingData> assessmentGradings = getHibernateTemplate().execute(hcb);

        if (assessmentGradingId == null) {
            if (assessmentGradings.size() > 0) {
                ag = assessmentGradings.get(0);
            }
        } else {
            for (int i = 0; i < assessmentGradings.size(); i++) {
                AssessmentGradingData agd = assessmentGradings.get(i);
                if (agd.getAssessmentGradingId().compareTo(assessmentGradingId) == 0) {
                    ag = agd;
                    ag.setItemGradingSet(getItemGradingSet(agd.getAssessmentGradingId()));
                    break;
                }
            }
        }

        // get AssessmentGradingAttachments
        List<AssessmentGradingAttachment> attachments = new ArrayList<AssessmentGradingAttachment>();
        if (ag != null) {
            Map<Long, List<AssessmentGradingAttachment>> map = getAssessmentGradingAttachmentMap(publishedAssessmentId);
            if (map != null && map.containsKey(ag.getAssessmentGradingId())) {
                attachments = map.get(ag.getAssessmentGradingId());
            }
            ag.setAssessmentGradingAttachmentList(attachments);
        }
        return ag;
    }

    public List getLastAssessmentGradingList(final Long publishedAssessmentId) {

        final HibernateCallback<List<AssessmentGradingData>> hcb = session -> {
            Query q = session.createQuery(
                    "from AssessmentGradingData a where a.publishedAssessmentId = :id and a.status > :status order by a.agentId asc, a.submittedDate desc");
            q.setParameter("id", publishedAssessmentId);
            q.setParameter("status", AssessmentGradingData.REMOVED);
            return q.list();
        };
        List<AssessmentGradingData> assessmentGradings = getHibernateTemplate().execute(hcb);

        return new ArrayList<>(assessmentGradings.stream()
                .collect(Collectors.toMap(AssessmentGradingData::getAgentId, p -> p, (p, q) -> p))
                .values());
    }

    public List getLastSubmittedAssessmentGradingList(final Long publishedAssessmentId) {

        final HibernateCallback<List<AssessmentGradingData>> hcb = session -> {
            Query q = session.createQuery(
                    "select a from AssessmentGradingData a left join fetch a.assessmentGradingAttachmentSet " +
                            "where a.publishedAssessmentId = :id and a.forGrade = :forgrade and a.status > :status order by a.agentId asc, a.submittedDate desc");
            q.setParameter("id", publishedAssessmentId);
            q.setParameter("forgrade", true);
            q.setParameter("status", AssessmentGradingData.REMOVED);
            return q.list();
        };
        List<AssessmentGradingData> assessmentGradings = getHibernateTemplate().execute(hcb);

        return new ArrayList<>(assessmentGradings.stream()
                .collect(Collectors.toMap(AssessmentGradingData::getAgentId, p -> p, (p, q) -> p))
                .values());
    }

    public List getLastSubmittedOrGradedAssessmentGradingList(final Long publishedAssessmentId) {
        final HibernateCallback<List<AssessmentGradingData>> hcb = session -> session.createCriteria(
                AssessmentGradingData.class)
                .add(Restrictions.eq("publishedAssessmentId", publishedAssessmentId))
                .add(Restrictions.or(
                        Restrictions.eq("forGrade", true),
                        Restrictions.and(
                                Restrictions.eq("forGrade", false),
                                Restrictions.eq("status", AssessmentGradingData.NO_SUBMISSION))))
                .addOrder(Order.asc("agentId").nulls(NullPrecedence.LAST))
                .addOrder(Order.desc("submittedDate").nulls(NullPrecedence.LAST))
                .list();
        List<AssessmentGradingData> assessmentGradings = getHibernateTemplate().execute(hcb);

        return new ArrayList<>(assessmentGradings.stream()
                .collect(Collectors.toMap(AssessmentGradingData::getAgentId, p -> p, (p, q) -> p))
                .values());
    }

    public List<AssessmentGradingData> getHighestAssessmentGradingList(final Long publishedAssessmentId) {

        final HibernateCallback<List<AssessmentGradingData>> hcb = session -> {
            Query q = session.createQuery(
                    "from AssessmentGradingData a where a.publishedAssessmentId = :id and a.status > :status order by a.agentId asc, a.finalScore desc");
            q.setParameter("id", publishedAssessmentId);
            q.setParameter("status", AssessmentGradingData.REMOVED);
            return q.list();
        };
        List<AssessmentGradingData> assessmentGradings = getHibernateTemplate().execute(hcb);

        return new ArrayList<>(assessmentGradings.stream()
                .collect(Collectors.toMap(AssessmentGradingData::getAgentId, p -> p, (p, q) -> p))
                .values());
    }


    public List<AssessmentGradingData> getHighestSubmittedOrGradedAssessmentGradingList(final Long publishedAssessmentId) {
        final HibernateCallback<List<AssessmentGradingData>> hcb = session -> session.createCriteria(
                AssessmentGradingData.class)
                .add(Restrictions.eq("publishedAssessmentId", publishedAssessmentId))
                .add(Restrictions.gt("status", AssessmentGradingData.REMOVED))
                .add(Restrictions.or(
                        Restrictions.eq("forGrade", true),
                        Restrictions.and(
                                Restrictions.eq("forGrade", false),
                                Restrictions.eq("status", AssessmentGradingData.NO_SUBMISSION))))
                .addOrder(Order.asc("agentId").nulls(NullPrecedence.LAST))
                .addOrder(Order.desc("finalScore").nulls(NullPrecedence.LAST))
                .list();

        List<AssessmentGradingData> assessmentGradings = getHibernateTemplate().execute(hcb);

        return new ArrayList<>(assessmentGradings.stream()
                .collect(Collectors.toMap(AssessmentGradingData::getAgentId, p -> p, (p, q) -> p))
                .values());
    }

    // build a Hashmap (Long publishedItemId, ArrayList assessmentGradingIds)
    // containing the item submission of the last AssessmentGrading
    // (regardless of users who submitted it) of a given published assessment
    public Map<Long, List<Long>> getLastAssessmentGradingByPublishedItem(final Long publishedAssessmentId) {
        Map<Long, List<Long>> h = new HashMap<>();

        final HibernateCallback<List<AssessmentGradingData>> hcb = session -> {
            Query q = session.createQuery(
                    "select new AssessmentGradingData(" +
                            " a.assessmentGradingId, p.itemId, " +
                            " a.agentId, a.finalScore, a.submittedDate) " +
                            " from ItemGradingData i, AssessmentGradingData a," +
                            " PublishedItemData p where " +
                            " i.assessmentGradingId = a.assessmentGradingId and i.publishedItemId = p.itemId and " +
                            " a.publishedAssessmentId = :id and a.status > :status " +
                            " order by a.agentId asc, a.submittedDate desc");
            q.setParameter("id", publishedAssessmentId);
            q.setParameter("status", AssessmentGradingData.REMOVED);
            return q.list();
        };
        List<AssessmentGradingData> assessmentGradings = getHibernateTemplate().execute(hcb);

        String currentAgent = "";
        Date submittedDate = null;
        for (int i = 0; i < assessmentGradings.size(); i++) {
            AssessmentGradingData g = assessmentGradings.get(i);
            Long itemId = g.getPublishedItemId();
            Long gradingId = g.getAssessmentGradingId();
            log.debug("**** itemId=" + itemId + ", gradingId=" + gradingId + ", agentId=" + g.getAgentId() + ", score=" + g
                    .getFinalScore());
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
    }

    // build a Hashmap (Long publishedItemId, ArrayList assessmentGradingIds)
    // containing the item submission of the highest AssessmentGrading
    // (regardless of users who submitted it) of a given published assessment
    public Map<Long, List<Long>> getHighestAssessmentGradingByPublishedItem(final Long publishedAssessmentId) {
        Map<Long, List<Long>> h = new HashMap<>();

        final HibernateCallback<List<AssessmentGradingData>> hcb = session -> {
            Query q = session.createQuery(
                    "select new AssessmentGradingData(" +
                            " a.assessmentGradingId, p.itemId, " +
                            " a.agentId, a.finalScore, a.submittedDate) " +
                            " from ItemGradingData i, AssessmentGradingData a, " +
                            " PublishedItemData p where " +
                            " i.assessmentGradingId = a.assessmentGradingId and i.publishedItemId = p.itemId and " +
                            " a.publishedAssessmentId = :id and a.status > :status " +
                            " order by a.agentId asc, a.finalScore desc");
            q.setParameter("id", publishedAssessmentId);
            q.setParameter("status", AssessmentGradingData.REMOVED);
            return q.list();
        };
        List<AssessmentGradingData> assessmentGradings = getHibernateTemplate().execute(hcb);

        String currentAgent = "";
        Double finalScore = null;
        for (int i = 0; i < assessmentGradings.size(); i++) {
            AssessmentGradingData g = (AssessmentGradingData) assessmentGradings.get(i);
            Long itemId = g.getPublishedItemId();
            Long gradingId = g.getAssessmentGradingId();
            log.debug("**** itemId=" + itemId + ", gradingId=" + gradingId + ", agentId=" + g.getAgentId() + ", score=" + g
                    .getFinalScore());
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
    }

    public Set<ItemGradingData> getItemGradingSet(final Long assessmentGradingId) {

        final HibernateCallback<List<ItemGradingData>> hcb = session -> {
            Query q = session.createQuery("from ItemGradingData i where i.assessmentGradingId = :id");
            q.setParameter("id", assessmentGradingId);
            return q.list();
        };
        List<ItemGradingData> itemGradings = getHibernateTemplate().execute(hcb);

        return new HashSet<>(itemGradings);
    }

    public Map<Long, ItemGradingData> getItemGradingMap(final Long assessmentGradingId) {

        final HibernateCallback<List<ItemGradingData>> hcb = session -> {
            Query q = session.createQuery("from ItemGradingData i where i.assessmentGradingId = :id");
            q.setParameter("id", assessmentGradingId);
            return q.list();
        };

        List<ItemGradingData> itemGradingList = getHibernateTemplate().execute(hcb);

        return itemGradingList.stream().collect(Collectors.toMap(ItemGradingData::getItemGradingId, p -> p));
    }

    public Map<Long, AssessmentGradingData> getAssessmentGradingByItemGradingId(final Long publishedAssessmentId) {
        Map<Long, AssessmentGradingData> submissionDataMap = getAllSubmissions(publishedAssessmentId.toString()).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(AssessmentGradingData::getAssessmentGradingId, a -> a));

        final HibernateCallback<List<ItemGradingData>> hcb = session -> {
            Query q = session.createQuery(
                    "select new ItemGradingData(i.itemGradingId, a.assessmentGradingId) " +
                            " from ItemGradingData i, AssessmentGradingData a " +
                            " where i.assessmentGradingId = a.assessmentGradingId " +
                            " and a.publishedAssessmentId = :id " +
                            " and a.forGrade = :forgrade and a.status > :status ");
            q.setParameter("id", publishedAssessmentId);
            q.setParameter("forgrade", true);
            q.setParameter("status", AssessmentGradingData.REMOVED);
            return q.list();
        };
        List<ItemGradingData> l = getHibernateTemplate().execute(hcb);

        return l.stream().filter(i -> Objects.nonNull(submissionDataMap.get(i.getAssessmentGradingId())))
                .collect(Collectors.toMap(ItemGradingData::getItemGradingId, g -> submissionDataMap.get(g.getAssessmentGradingId())));
    }

    public void deleteAll(Collection c) {
        int retryCount = persistenceHelper.getRetryCount();
        while (retryCount > 0) {
            try {
                c.stream().filter(Objects::nonNull).map(getHibernateTemplate()::merge).forEach(getHibernateTemplate()::delete);
                retryCount = 0;
            } catch (Exception e) {
                log.warn("problem inserting assessmentGrading: {}", e.toString());
                retryCount = persistenceHelper.retryDeadlock(e, retryCount);
            }
        }
    }

    public void saveOrUpdateAll(Collection<ItemGradingData> c) {
        int retryCount = persistenceHelper.getRetryCount();

        c.removeAll(Collections.singleton(null));
        while (retryCount > 0) {
            try {
                for (ItemGradingData itemGradingData : c) {
                    getHibernateTemplate().merge(itemGradingData);
                }
                retryCount = 0;
            } catch (Exception e) {
                log.warn("problem inserting assessmentGrading: " + e.getMessage());
                retryCount = persistenceHelper.retryDeadlock(e, retryCount);
            }
        }
    }

    public PublishedAssessmentIfc getPublishedAssessmentByAssessmentGradingId(final Long assessmentGradingId) {

        final HibernateCallback<List<PublishedAssessmentData>> hcb = session -> {
            Query q = session.createQuery(
                    "select p from PublishedAssessmentData p, AssessmentGradingData a where a.publishedAssessmentId = p.publishedAssessmentId and a.assessmentGradingId = :id");
            q.setParameter("id", assessmentGradingId);
            return q.list();
        };
        List<PublishedAssessmentData> pubList = getHibernateTemplate().execute(hcb);

        if (pubList != null && !pubList.isEmpty()) {
            return pubList.get(0);
        }
        return null;
    }

    public PublishedAssessmentIfc getPublishedAssessmentByPublishedItemId(final Long publishedItemId) {

        final HibernateCallback<List<PublishedAssessmentData>> hcb = session -> {
            Query q = session.createQuery(
                    "select p from PublishedAssessmentData p, PublishedItemData i where p.publishedAssessmentId = i.section.assessment.publishedAssessmentId and i.itemId = :id");
            q.setParameter("id", publishedItemId);
            return q.list();
        };
        List<PublishedAssessmentData> pubList = getHibernateTemplate().execute(hcb);

        if (pubList != null && !pubList.isEmpty()) {
            return pubList.get(0);
        }

        return null;
    }

    public List<Integer> getLastItemGradingDataPosition(final Long assessmentGradingId, final String agentId) {
        List<Integer> position = new ArrayList<>();
        try {
            final HibernateCallback<List<Integer>> hcb = session -> {
                Query q = session.createQuery("select s.sequence " +
                        " from ItemGradingData i, PublishedItemData pi, PublishedSectionData s " +
                        " where i.agentId = :agent and i.assessmentGradingId = :id " +
                        " and pi.itemId = i.publishedItemId " +
                        " and pi.section.id = s.id " +
                        " group by i.publishedItemId, s.sequence, pi.sequence " +
                        " order by s.sequence desc , pi.sequence desc");
                q.setParameter("agent", agentId);
                q.setParameter("id", assessmentGradingId);
                return q.list();
            };
            List<Integer> list = getHibernateTemplate().execute(hcb);

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
            log.warn(e.getMessage(), e);
            position.add(0);
            position.add(0);
            return position;
        }
    }

    public List<Long> getPublishedItemIds(final Long assessmentGradingId) {

        final HibernateCallback<List<Long>> hcb = session -> {
            Query q = session.createQuery(
                    "select i.publishedItemId from ItemGradingData i where i.assessmentGradingId = :id");
            q.setParameter("id", assessmentGradingId);
            return q.list();
        };
        return getHibernateTemplate().execute(hcb);
    }

    public List<Long> getItemGradingIds(final Long assessmentGradingId) {
        final HibernateCallback<List<Long>> hcb = session -> {
            Query q = session.createQuery(
                    "select i.itemGradingId from ItemGradingData i where i.assessmentGradingId = :id");
            q.setParameter("id", assessmentGradingId);
            return q.list();
        };
        return getHibernateTemplate().execute(hcb);
    }

    public Set<PublishedItemData> getItemSet(final Long publishedAssessmentId, final Long sectionId) {

        final HibernateCallback<List<Long>> hcb = session -> {
            Query q = session.createQuery(
                    "select distinct p.itemId " +
                            "from PublishedItemData p, AssessmentGradingData a, ItemGradingData i " +
                            "where a.publishedAssessmentId = :id and a.forGrade = :forgrade and p.section.id = :sectionid " +
                            "and i.assessmentGradingId = a.assessmentGradingId " +
                            "and p.itemId = i.publishedItemId and a.status > :status ");
            q.setParameter("id", publishedAssessmentId);
            q.setParameter("forgrade", true);
            q.setParameter("sectionid", sectionId);
            q.setParameter("status", AssessmentGradingData.REMOVED);
            return q.list();
        };
        List<Long> itemIds = getHibernateTemplate().execute(hcb);

        if (itemIds.isEmpty()) {
            return new HashSet<>();
        }

        final HibernateCallback<List<PublishedItemData>> hcb2 = session -> {

            final Criteria criteria = session.createCriteria(PublishedItemData.class);
            if (itemIds.size() > 1000) {
                final Set<Long> ids = new HashSet<>();
                Disjunction disjunction = Restrictions.disjunction();

                for (Long id : itemIds) {
                    if (ids.size() < 1000) {
                        ids.add(id);
                    } else {
                        criteria.add(disjunction.add(Restrictions.in("itemId", ids)));
                        ids.clear();
                    }
                }
            } else {
                criteria.add(Restrictions.in("itemId", itemIds));
            }
            return criteria.list();
        };

        List<PublishedItemData> publishedItems = getHibernateTemplate().execute(hcb2);

        return new HashSet<>(publishedItems);
    }

    public Long getTypeId(final Long itemGradingId) {
        Long typeId = Long.valueOf(-1);

        final HibernateCallback<List<Long>> hcb = session -> {
            Query q = session.createQuery(
                    "select p.typeId " +
                            "from PublishedItemData p, ItemGradingData i " +
                            "where i.itemGradingId = :id " +
                            "and p.itemId = i.publishedItemId ");
            q.setParameter("id", itemGradingId);
            return q.list();
        };

        List<Long> typeIds = getHibernateTemplate().execute(hcb);
        if (typeIds != null) {
            for (Long id : typeIds) {
                typeId = id;
                log.debug("typeId = {}", typeId);
            }
        }
        return typeId;
    }

    public List<AssessmentGradingData> getAllAssessmentGradingByAgentId(final Long publishedAssessmentId, final String agentIdString) {

        final HibernateCallback<List<AssessmentGradingData>> hcb = session -> {
            Query q = session.createQuery(
                    "from AssessmentGradingData a where a.publishedAssessmentId = :id and a.agentId = :agent and a.forGrade = :forgrade and a.status > :status order by a.submittedDate desc");
            q.setParameter("id", publishedAssessmentId);
            q.setParameter("agent", agentIdString);
            q.setParameter("forgrade", true);
            q.setParameter("status", AssessmentGradingData.REMOVED);
            return q.list();
        };
        return getHibernateTemplate().execute(hcb);
    }

    public List<ItemGradingData> getAllItemGradingDataForItemInGrading(final Long assesmentGradingId, final Long publishedItemId) {
        if (assesmentGradingId == null) {
            throw new IllegalArgumentException("assesmentGradingId cant' be null");
        }

        if (publishedItemId == null) {
            throw new IllegalArgumentException("publishedItemId cant' be null");
        }

        return getHibernateTemplate().execute(session -> session.createCriteria(ItemGradingData.class)
                .add(Restrictions.eq("assessmentGradingId", assesmentGradingId))
                .add(Restrictions.eq("publishedItemId", publishedItemId))
                .list());
    }

    public Map<Long, Map<String, Integer>> getSiteSubmissionCountHash(final String siteId) {
        Map<Long, Map<String, Integer>> siteSubmissionCountHash = new HashMap<>();
        final HibernateCallback<List<Object[]>> hcb = session -> session.createQuery(
                "select a.publishedAssessmentId, a.agentId, count(*) " +
                        "from AssessmentGradingData a, AuthorizationData au  " +
                        "where a.forGrade = :forgrade and au.functionId = :fid and au.agentIdString = :agent and a.publishedAssessmentId = au.qualifierId and a.status > :status " +
                        "group by a.publishedAssessmentId, a.agentId " +
                        "order by a.publishedAssessmentId, a.agentId ")
                .setParameter("forgrade", true)
                .setParameter("fid", "OWN_PUBLISHED_ASSESSMENT")
                .setParameter("agent", siteId)
                .setParameter("status", AssessmentGradingData.REMOVED)
                .setCacheable(true)
                .list();

        List<Object[]> countList = getHibernateTemplate().execute(hcb);
        Map<String, Integer> numberSubmissionPerStudentHash = new HashMap<>();
        Long lastPublishedAssessmentId = -1L;

        for (Object[] o : countList) {
            Long publishedAssessmentid = (Long) o[0];

            if (lastPublishedAssessmentId.equals(publishedAssessmentid)) {
                numberSubmissionPerStudentHash.put((String) o[1], ((Long) o[2]).intValue());
            } else {
                numberSubmissionPerStudentHash = new HashMap<>();
                numberSubmissionPerStudentHash.put((String) o[1], ((Long) o[2]).intValue());
                siteSubmissionCountHash.put(publishedAssessmentid, numberSubmissionPerStudentHash);
                lastPublishedAssessmentId = publishedAssessmentid;
            }
        }

        return siteSubmissionCountHash;
    }

    public Map<Long, Map<String, Long>> getSiteInProgressCountHash(final String siteId) {
        Map<Long, Map<String, Long>> siteInProgressCountHash = new HashMap<>();
        final HibernateCallback<List<Object[]>> hcb = session -> session.createQuery(
                "select a.publishedAssessmentId, a.agentId, count(*) " +
                        "from AssessmentGradingData a, AuthorizationData au  " +
                        "where a.forGrade = :forgrade and au.functionId = :fid and au.agentIdString = :agent " +
                        "and a.publishedAssessmentId = au.qualifierId and (a.status = :status1 or a.status = :status2) " +
                        "group by a.publishedAssessmentId, a.agentId " +
                        "order by a.publishedAssessmentId, a.agentId ")
                .setParameter("forgrade", false)
                .setParameter("fid", "OWN_PUBLISHED_ASSESSMENT")
                .setParameter("agent", siteId)
                .setParameter("status1", AssessmentGradingData.IN_PROGRESS)
                .setParameter("status2", AssessmentGradingData.ASSESSMENT_UPDATED)
                .setCacheable(true)
                .list();

        List<Object[]> countList = getHibernateTemplate().execute(hcb);
        Map<String, Long> numberInProgressPerStudentHash = new HashMap<>();
        Long lastPublishedAssessmentId = -1l;
        for (Object[] o : countList) {
            Long publishedAssessmentid = (Long) o[0];

            if (lastPublishedAssessmentId.equals(publishedAssessmentid)) {
                numberInProgressPerStudentHash.put((String) o[1], (Long) o[2]);
            } else {
                numberInProgressPerStudentHash = new HashMap<>();
                numberInProgressPerStudentHash.put((String) o[1], (Long) o[2]);
                siteInProgressCountHash.put(publishedAssessmentid, numberInProgressPerStudentHash);
                lastPublishedAssessmentId = publishedAssessmentid;
            }
        }

        return siteInProgressCountHash;
    }

    public int getActualNumberRetake(final Long publishedAssessmentId, final String agentIdString) {

        final HibernateCallback<List<Long>> hcb = session -> {
            Query q = session.createQuery(
                    "select count(*) from AssessmentGradingData a, StudentGradingSummaryData s " +
                            " where a.publishedAssessmentId = :id and a.agentId = :agent and a.forGrade = :forgrade " +
                            " and a.publishedAssessmentId = s.publishedAssessmentId and a.agentId = s.agentId " +
                            " and a.submittedDate > s.createdDate and a.status > :status");
            q.setParameter("id", publishedAssessmentId);
            q.setParameter("agent", agentIdString);
            q.setParameter("forgrade", true);
            q.setParameter("status", AssessmentGradingData.REMOVED);
            return q.list();
        };
        List<Long> countList = getHibernateTemplate().execute(hcb);
        if (countList != null && !countList.isEmpty()) {
            return Math.toIntExact(countList.get(0));
        }
        return 0;
    }

    public Map<Long, Map<String, Long>> getSiteActualNumberRetakeHash(final String siteId) {
        Map<Long, Map<String, Long>> actualNumberRetakeHash = new HashMap<>();
        final HibernateCallback<List<Object[]>> hcb = session -> {
            Query q = session.createQuery(
                    "select a.publishedAssessmentId, a.agentId, count(*) " +
                            " from AssessmentGradingData a, StudentGradingSummaryData s, AuthorizationData au, PublishedAssessmentData p " +
                            " where a.forGrade = :forgrade and au.functionId = :fid and au.agentIdString = :agent and a.publishedAssessmentId = au.qualifierId" +
                            " and a.publishedAssessmentId = s.publishedAssessmentId and a.agentId = s.agentId " +
                            " and a.submittedDate > s.createdDate" +
                            " and a.publishedAssessmentId = p.publishedAssessmentId" +
                            " and p.status != 2 and a.status > :astatus" +
                            " group by a.publishedAssessmentId, a.agentId" +
                            " order by a.publishedAssessmentId");
            q.setParameter("forgrade", true);
            q.setParameter("fid", "OWN_PUBLISHED_ASSESSMENT");
            q.setParameter("astatus", AssessmentGradingData.REMOVED);
            q.setParameter("agent", siteId);
            return q.list();
        };
        List<Object[]> countList = getHibernateTemplate().execute(hcb);
        Map<String, Long> actualNumberRetakePerStudentHash = new HashMap<>();
        Long lastPublishedAssessmentId = -1l;
        for (Object[] o : countList) {
            Long publishedAssessmentid = (Long) o[0];

            if (lastPublishedAssessmentId.equals(publishedAssessmentid)) {
                actualNumberRetakePerStudentHash.put((String) o[1], (Long) o[2]);
            } else {
                actualNumberRetakePerStudentHash = new HashMap();
                actualNumberRetakePerStudentHash.put((String) o[1], (Long) o[2]);
                actualNumberRetakeHash.put(publishedAssessmentid, actualNumberRetakePerStudentHash);
                lastPublishedAssessmentId = publishedAssessmentid;
            }
        }

        return actualNumberRetakeHash;
    }

    public Map<Long, Integer> getActualNumberRetakeHash(final String agentIdString) {
        Map<Long, Integer> actualNumberRetakeHash = new HashMap<>();
        final HibernateCallback<List<Object[]>> hcb = session -> {
            Query q = session.createQuery(
                    "select a.publishedAssessmentId, count(*) from AssessmentGradingData a, StudentGradingSummaryData s " +
                            " where a.agentId = :agent and a.forGrade = :forgrade " +
                            " and a.publishedAssessmentId = s.publishedAssessmentId and a.agentId = s.agentId " +
                            " and a.submittedDate > s.createdDate and a.status > :status" +
                            " group by a.publishedAssessmentId");
            q.setParameter("agent", agentIdString);
            q.setParameter("forgrade", true);
            q.setParameter("status", AssessmentGradingData.REMOVED);
            return q.list();
        };
        List<Object[]> countList = getHibernateTemplate().execute(hcb);
        for (Object[] o : countList) {
            Long l = (Long) o[1];
            actualNumberRetakeHash.put((Long) o[0], l.intValue());
        }
        return actualNumberRetakeHash;
    }

    public List<StudentGradingSummaryData> getStudentGradingSummaryData(final Long publishedAssessmentId, final String agentIdString) {
        final HibernateCallback<List<StudentGradingSummaryData>> hcb = session -> {
            Query q = session.createQuery(
                    "select s " +
                            "from StudentGradingSummaryData s " +
                            "where s.publishedAssessmentId = :id and s.agentId = :agent");
            q.setParameter("id", publishedAssessmentId);
            q.setParameter("agent", agentIdString);
            return q.list();
        };
        return getHibernateTemplate().execute(hcb);
    }

    public int getNumberRetake(final Long publishedAssessmentId, final String agentIdString) {
        final HibernateCallback<List<Integer>> hcb = session -> {
            Query q = session.createQuery(
                    "select s.numberRetake " +
                            "from StudentGradingSummaryData s " +
                            "where s.publishedAssessmentId = :id and s.agentId = :agent");
            q.setParameter("id", publishedAssessmentId);
            q.setParameter("agent", agentIdString);
            return q.list();
        };
        List<Integer> numberRetakeList = getHibernateTemplate().execute(hcb);

        if (!numberRetakeList.isEmpty()) {
            return numberRetakeList.get(0);
        }
        return 0;
    }

    public Map<Long, StudentGradingSummaryData> getNumberRetakeHash(final String agentIdString) {
        Map<Long, StudentGradingSummaryData> h = new HashMap<>();
        final HibernateCallback<List<StudentGradingSummaryData>> hcb = session -> {
            Query q = session.createQuery(
                    "select s " +
                            "from StudentGradingSummaryData s " +
                            "where s.agentId = :agent");
            q.setParameter("agent", agentIdString);
            return q.list();
        };
        List<StudentGradingSummaryData> numberRetakeList = getHibernateTemplate().execute(hcb);
        return numberRetakeList.stream()
                .collect(Collectors.toMap(StudentGradingSummaryData::getPublishedAssessmentId, Function.identity(), (oldValue, newValue) -> newValue));
    }

    public Map<Long, Map<String, Integer>> getSiteNumberRetakeHash(final String siteId) {
        Map<Long, Map<String, Integer>> siteNumberRetakeHash = new HashMap<>();
        final HibernateCallback<List<StudentGradingSummaryData>> hcb = session -> {
            Query q = session.createQuery(
                    "select s " +
                            "from StudentGradingSummaryData s, AuthorizationData au " +
                            "where au.functionId = :fid and au.agentIdString = :agent " +
                            "and s.publishedAssessmentId = au.qualifierId " +
                            "order by s.publishedAssessmentId, s.agentId");
            q.setParameter("fid", "OWN_PUBLISHED_ASSESSMENT");
            q.setParameter("agent", siteId);
            return q.list();
        };
        List<StudentGradingSummaryData> countList = getHibernateTemplate().execute(hcb);
        Long lastPublishedAssessmentId = -1l;
        Map<String, Integer> numberRetakePerStudentHash = null;
        for (StudentGradingSummaryData s : countList) {
            Long publishedAssessmentid = s.getPublishedAssessmentId();

            if (lastPublishedAssessmentId.equals(publishedAssessmentid)) {
                numberRetakePerStudentHash.put(s.getAgentId(), s.getNumberRetake());
            } else {
                numberRetakePerStudentHash = new HashMap<>();
                numberRetakePerStudentHash.put(s.getAgentId(), s.getNumberRetake());
                siteNumberRetakeHash.put(publishedAssessmentid, numberRetakePerStudentHash);
                lastPublishedAssessmentId = publishedAssessmentid;
            }
        }

        return siteNumberRetakeHash;
    }

    public void saveStudentGradingSummaryData(StudentGradingSummaryIfc studentGradingSummaryData) {
        int retryCount = persistenceHelper.getRetryCount();
        while (retryCount > 0) {
            try {
                getHibernateTemplate().saveOrUpdate(studentGradingSummaryData);
                retryCount = 0;
            } catch (Exception e) {
                log.warn("problem saving studentGradingSummaryData: " + e.getMessage());
                retryCount = persistenceHelper.retryDeadlock(e, retryCount);
            }
        }
    }

    public int getLateSubmissionsNumberByAgentId(final Long publishedAssessmentId, final String agentIdString, final Date dueDate) {
        final HibernateCallback<List<AssessmentGradingData>> hcb = session -> {
            Query q = session.createQuery(
                    "from AssessmentGradingData a where a.publishedAssessmentId = :id and a.agentId = :agent and a.forGrade = :forgrade and a.submittedDate > :submitted and a.status > :status");
            q.setParameter("id", publishedAssessmentId);
            q.setParameter("agent", agentIdString);
            q.setParameter("forgrade", true);
            q.setParameter("submitted", dueDate);
            q.setParameter("status", AssessmentGradingData.REMOVED);
            return q.list();
        };
        List<AssessmentGradingData> assessmentGradings = getHibernateTemplate().execute(hcb);

        return assessmentGradings.size();
    }

    public List<AssessmentGradingData> getAllOrderedSubmissions(final String publishedId) {

        final HibernateCallback<List<AssessmentGradingData>> hcb = session -> {
            Query q = session.createQuery(
                    "from AssessmentGradingData a " +
                            "where a.publishedAssessmentId = :id and (a.forGrade = :forgrade1 or (a.forGrade = :forgrade2 and a.status = :status and a.finalScore <> 0)) " +
                            "order by a.agentId ASC, a.submittedDate");
            q.setParameter("id", Long.parseLong(publishedId));
            q.setParameter("forgrade1", true);
            q.setParameter("forgrade2", false);
            q.setParameter("status", AssessmentGradingData.NO_SUBMISSION);
            return q.list();
        };
        return getHibernateTemplate().execute(hcb);
    }

    public List getExportResponsesData(String publishedAssessmentId, boolean anonymous, String audioMessage, String fileUploadMessage, String noSubmissionMessage, 
                                       boolean showPartAndTotalScoreSpreadsheetColumns, String poolString, String partString, String questionString, String textString, 
                                       String responseString, String pointsString, String rationaleString, String itemGradingCommentsString, Map useridMap, 
                                       String responseCommentString) {
        return this.getExportResponsesData(publishedAssessmentId, anonymous, audioMessage, fileUploadMessage, noSubmissionMessage, showPartAndTotalScoreSpreadsheetColumns, 
                                    poolString, partString, questionString, textString, responseString, pointsString, rationaleString, itemGradingCommentsString, useridMap, 
                                    responseCommentString, false);
    }

    public List getExportResponsesData(String publishedAssessmentId, boolean anonymous, String audioMessage, String fileUploadMessage, String noSubmissionMessage, boolean showPartAndTotalScoreSpreadsheetColumns, String poolString, String partString, String questionString, String textString, String responseString, String pointsString, String rationaleString, String itemGradingCommentsString, Map useridMap, String responseCommentString, boolean isOneSelectionType) 
    {
        List dataList = new ArrayList();
        List headerList = new ArrayList();
        List finalList = new ArrayList(2);
        PublishedAssessmentService pubService = new PublishedAssessmentService();

        Set publishedAssessmentSections = pubService.getSectionSetForAssessment(Long.valueOf(publishedAssessmentId));
        Double zeroDouble = 0.0;
        Map publishedAnswerHash = pubService.preparePublishedAnswerHash(pubService.getPublishedAssessment(
                publishedAssessmentId));
        Map publishedItemTextHash = pubService.preparePublishedItemTextHash(pubService.getPublishedAssessment(
                publishedAssessmentId));
        Map publishedItemHash = pubService.preparePublishedItemHash(pubService.getPublishedAssessment(
                publishedAssessmentId));

        //Get this sorted to add the blank gradings for the questions not answered later.
        Set publishItemSet = new TreeSet(new ItemComparator());
        publishItemSet.addAll(publishedItemHash.values());

        int numSubmission = 1;
        String numSubmissionText;
        String lastAgentId = "";
        String agentEid = "";
        String firstName = "";
        String lastName = "";
        Set useridSet = new HashSet(useridMap.keySet());
        List responseList;
        boolean canBeExported;
        boolean fistItemGradingData = true;
        List list = getAllOrderedSubmissions(publishedAssessmentId);
        Iterator assessmentGradingIter = list.iterator();
        while (assessmentGradingIter.hasNext()) {

            // create new section-item-scores structure for this assessmentGrading
            Iterator sectionsIter = publishedAssessmentSections.iterator();
            Map sectionItems = new HashMap();
            Map sectionScores = new TreeMap();
            while (sectionsIter.hasNext()) {
                PublishedSectionData publishedSection = (PublishedSectionData) sectionsIter.next();
                List<ItemDataIfc> itemsArray = publishedSection.getItemArraySortedForGrading();

                // adding fixed questions (could be empty if not fixed and draw part)
                List<ItemDataIfc> sortedList = itemsArray.stream()
                    .filter(item -> ((PublishedItemData) item).getIsFixed())
                    .collect(Collectors.toList());

                // getting all hashes from the sortedListt
                List<String> distinctHashValues = sortedList.stream()
                    .filter(item -> item instanceof PublishedItemData)
                    .map(item -> ((PublishedItemData) item).getHash())
                    .distinct()
                    .collect(Collectors.toList());

                // removing from itemSet if there are hashes repeated and getFixed false -> itemArray with only fixed and not repeated fixed on the randow draw
                itemsArray.removeIf(item -> item instanceof PublishedItemData &&
                                            !item.getIsFixed() &&
                                            distinctHashValues.stream().anyMatch(hash -> hash.equals(item.getHash())));

                Iterator itemsIter = itemsArray.iterator();
                // Iterate through the assessment questions (items)
                Map itemsForSection = new HashMap();
                while (itemsIter.hasNext()) {
                    ItemDataIfc item = (ItemDataIfc) itemsIter.next();
                    itemsForSection.put(item.getItemId(), item.getItemId());
                }
                sectionItems.put(publishedSection.getSequence(), itemsForSection);
                sectionScores.put(publishedSection.getSequence(), zeroDouble);
            }

            AssessmentGradingData assessmentGradingData = (AssessmentGradingData) assessmentGradingIter.next();
            String agentId = assessmentGradingData.getAgentId();
            responseList = new ArrayList();
            canBeExported = false;
            if (anonymous) {
                canBeExported = true;
                responseList.add(assessmentGradingData.getAssessmentGradingId());
            } else {
                if (useridMap.containsKey(assessmentGradingData.getAgentId())) {
                    useridSet.remove(assessmentGradingData.getAgentId());
                    canBeExported = true;
                    try {
                        agentEid = userDirectoryService.getUser(assessmentGradingData.getAgentId()).getEid();
                        firstName = userDirectoryService.getUser(assessmentGradingData.getAgentId()).getFirstName();
                        lastName = userDirectoryService.getUser(assessmentGradingData.getAgentId()).getLastName();
                    } catch (Exception e) {
                        log.error("Cannot get user");
                    }
                    responseList.add(lastName);
                    responseList.add(firstName);
                    responseList.add(agentEid);
                    if (assessmentGradingData.getForGrade()) {
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
                    responseList.add(numSubmissionText);
                }
            }

            if (canBeExported) {

                Date attempt = assessmentGradingData.getAttemptDate();
                Date submitted = assessmentGradingData.getSubmittedDate();
                responseList.add(attempt == null ? "" : attempt);
                responseList.add(submitted == null ? "" : submitted);

                int sectionScoreColumnStart = responseList.size();
                if (showPartAndTotalScoreSpreadsheetColumns) {
                    Double finalScore = assessmentGradingData.getFinalScore();
                    if (finalScore != null) {
                        responseList.add(finalScore); // gopal - cast for spreadsheet numerics
                    } else {
                        log.debug("finalScore is NULL");
                        responseList.add(0d);
                    }
                }
                int emptyIndex = 0;
                if (isOneSelectionType) {
                    responseList.add(0);
                    responseList.add(0);
                    responseList.add(0);
                    emptyIndex = responseList.size() - 1;
                }

                String assessmentGradingComments = "";
                if (assessmentGradingData.getComments() != null) {
                    assessmentGradingComments = assessmentGradingData.getComments().replaceAll("<br\\s*/>", "");
                }
                responseList.add(assessmentGradingComments);

                Long assessmentGradingId = assessmentGradingData.getAssessmentGradingId();

                Map studentGradingMap = getStudentGradingData(assessmentGradingData.getAssessmentGradingId().toString(),
                        false);
                List grades = new ArrayList();
                grades.addAll(studentGradingMap.values());

                Collections.sort(grades, new QuestionComparator(publishedItemHash));

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
                            List newList = new ArrayList();
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
                    TreeMap responsesMap = new TreeMap();
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
                        	responseList.add("-");
                        	continue;
                        }
                        if (grade.getAutoScore() != null) {
                            itemScore += grade.getAutoScore();
                        }

                        // now print answer data
                        log.debug("<br> " + grade.getPublishedItemId() + " " + grade.getRationale() + " " + grade.getAnswerText() + " " + grade
                                .getComments() + " " + grade.getReview());
                        Long publishedItemId = grade.getPublishedItemId();
                        ItemDataIfc publishedItemData = (ItemDataIfc) publishedItemHash.get(publishedItemId);
                        Long typeId = publishedItemData.getTypeId();
                        if (count == 0) {
                            if (!TypeIfc.MATRIX_CHOICES_SURVEY.equals(typeId)) {
                                responseList.add(publishedItemData.getText()); // The Text of the question
                            } else if(!textOfQuestionIncluded) {
                                // type MATRIX_CHOICES_SURVEY
                                responseList.add(publishedItemData.getText()); // The Text of the question
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
                                AnswerIfc answer = (AnswerIfc) publishedAnswerHash.get(answerid);
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
                                AnswerIfc answer = (AnswerIfc) publishedAnswerHash.get(answerid);
                                if (answer != null) {
                                    temptext = answer.getText();
                                    if (temptext == null) {
                                        temptext = "No Answer";
                                    }
                                    sequence = answer.getItemText().getSequence();
                                } else if (answerid == -1) {
                                    temptext = "None of the Above";
                                    ItemTextIfc itemTextIfc = (ItemTextIfc) publishedItemTextHash.get(grade.getPublishedItemTextId());
                                    sequence = itemTextIfc.getSequence();
                                }
                            } else {
                                ItemTextIfc itemTextIfc = (ItemTextIfc) publishedItemTextHash.get(grade.getPublishedItemTextId());
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

                            ItemTextIfc itemTextIfc = (ItemTextIfc) publishedItemTextHash.get(grade.getPublishedItemTextId());
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
                                AnswerIfc answer = (AnswerIfc) publishedAnswerHash.get(answerid);
                                if (answer != null) {
                                    temptext = answer.getLabel();
                                    if (temptext == null) {
                                        temptext = "No Answer";
                                    }
                                    sequence = answer.getItemText().getSequence();
                                }
                            }

                            if (sequence == null) {
                                ItemTextIfc itemTextIfc = (ItemTextIfc) publishedItemTextHash.get(grade.getPublishedItemTextId());
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
                                sequence = new Long(99);
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
                                AnswerIfc answer = (AnswerIfc) publishedAnswerHash.get(answerid);
                                temptext = answer.getText();
                                if (temptext == null) {
                                    temptext = "No Answer";
                                }
                                sequence = answer.getItemText().getSequence();
                            } else {
                                ItemTextIfc itemTextIfc = (ItemTextIfc) publishedItemTextHash.get(grade.getPublishedItemTextId());
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
                                AnswerIfc answer = (AnswerIfc) publishedAnswerHash.get(answerid);
                                if (answer != null) {
                                    if (isOneSelectionType) {
                                        Boolean answerCorrectness = resolveOneSelectionCorrectness(answer, grade);
                                        if (Boolean.TRUE.equals(answerCorrectness)) {
                                            // For correct answers cases
                                            responseList.set(emptyIndex - 2, ((int) responseList.get(emptyIndex - 2)) + 1);
                                        } else if (Boolean.FALSE.equals(answerCorrectness)) {
                                            // For incorrect answers cases
                                            responseList.set(emptyIndex - 1, ((int) responseList.get(emptyIndex - 1)) + 1);
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
                                responseList.set(emptyIndex, ((int) responseList.get(emptyIndex)) + 1);
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
                        responseList.add(maintext);
                        responseList.add(itemScore);
                    } else {
                        // if there are questions not answered, a no answer response is added to the map
                        ItemDataIfc correspondingPublishedItemData = (ItemDataIfc) publishedItemHash.get(grade.getPublishedItemId());
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
                        Iterator it = responsesMap.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry e = (Map.Entry) it.next();
                            log.debug("Adding to response list " + e.getKey() + " and " + e.getValue());
                            responseList.add(e.getValue());
                        }
                    }

                    if (addResponseComment) {
                        responseList.add(responseComment);
                    }

                    if (addRationale) {
                        responseList.add(rationale);
                    }

                    String itemGradingComments = "";
                    if (grade.getComments() != null) {
                        itemGradingComments = grade.getComments().replaceAll("<br\\s*/>", "");
                    }
                    responseList.add(itemGradingComments);

                    // Only set header based on the first item grading data
                    if (fistItemGradingData) {
                        //get the pool name
                        String poolName = null;
                        for (Iterator i = publishedAssessmentSections.iterator(); i.hasNext(); ) {
                            PublishedSectionData psd = (PublishedSectionData) i.next();
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
                            headerList.add(makeHeader(partString,
                                    sectionSequenceNumber,
                                    questionString,
                                    textString,
                                    questionNumber,
                                    poolString,
                                    poolName));
                            headerList.add(makeHeader(partString, 
                                    sectionSequenceNumber, 
                                    questionString, 
                                    responseString, 
                                    questionNumber, 
                                    poolString, 
                                    poolName));
                            headerList.add(makeHeader(partString, 
                                    sectionSequenceNumber, 
                                    questionString, 
                                    pointsString, 
                                    questionNumber, 
                                    poolString, 
                                    poolName));
                            if (addRationale) {
                                headerList.add(makeHeader(partString,
                                        sectionSequenceNumber,
                                        questionString,
                                        rationaleString,
                                        questionNumber,
                                        poolString,
                                        poolName));
                            }
                            headerList.add(makeHeader(partString,
                                    sectionSequenceNumber,
                                    questionString,
                                    itemGradingCommentsString,
                                    questionNumber,
                                    poolString,
                                    poolName));
                        } else {
                            int numberRows = responsesMap.size();
                            headerList.add(makeHeader(partString, sectionSequenceNumber, questionString, textString, questionNumber, poolString, poolName));
                            for (int i = 0; i < numberRows; i = i + 1) {
                                headerList.add(makeHeaderMatrix(partString,
                                        sectionSequenceNumber,
                                        questionString,
                                        responseString,
                                        questionNumber,
                                        i + 1,
                                        poolString,
                                        poolName));
                            }
                            if (addRationale) {
                                headerList.add(makeHeader(partString, 
                                        sectionSequenceNumber, 
                                        questionString, 
                                        rationaleString, 
                                        questionNumber, 
                                        poolString, 
                                        poolName));
                            }
                            if (addResponseComment) {
                                headerList.add(makeHeader(partString,
                                        sectionSequenceNumber,
                                        questionString,
                                        responseCommentString,
                                        questionNumber,
                                        poolString,
                                        poolName));
                            }
                            headerList.add(makeHeader(partString, sectionSequenceNumber, questionString, itemGradingCommentsString, questionNumber, poolString, poolName));
                        }
                    }
                } // outer for - questions

                if (showPartAndTotalScoreSpreadsheetColumns) {
                    if (sectionScores.size() > 1) {
                        Iterator keys = sectionScores.keySet().iterator();
                        while (keys.hasNext()) {
                            Double partScore = (Double) (sectionScores.get(keys.next()));
                            responseList.add(sectionScoreColumnStart++, partScore);
                        }
                    }
                }

                dataList.add(responseList);

                if (fistItemGradingData) {
                    fistItemGradingData = false;
                }
            }
        } // while

        if (!anonymous && !useridSet.isEmpty()) {
            Iterator iter = useridSet.iterator();
            while (iter.hasNext()) {
                String id = (String) iter.next();
                try {
                    agentEid = userDirectoryService.getUser(id).getEid();
                    firstName = userDirectoryService.getUser(id).getFirstName();
                    lastName = userDirectoryService.getUser(id).getLastName();
                } catch (Exception e) {
                    log.error("Cannot get user");
                }
                responseList = new ArrayList();
                responseList.add(lastName);
                responseList.add(firstName);
                responseList.add(agentEid);
                responseList.add(noSubmissionMessage);
                dataList.add(responseList);
            }
        }
        Collections.sort(dataList, new ResponsesComparator(anonymous));
        finalList.add(dataList);
        finalList.add(headerList);
        return finalList;
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


    /**
     * @param sectionItems
     * @param sectionScores
     * @param grade
     * @return The section sequence number, or zero if the section is not found(unlikely)
     */
    private int updateSectionScore(Map sectionItems, Map sectionScores, Long publishedItemId, double itemScore) {

        for (Iterator it = sectionItems.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            Object sectionSequence = entry.getKey();
            Map itemsForSection = (Map) entry.getValue();

            if (itemsForSection.get(publishedItemId) != null) {
                Double score = ((Double) sectionScores.get(sectionSequence)) + itemScore;
                sectionScores.put(sectionSequence, score);
                return ((Integer) sectionSequence);
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

    /*
	 sort questions in same order presented to users
	 first by section then by question within section
	 hint: "item" things are specific to the user's answer
	 sequence numbers are stored with the published assessment, not
	 separate with each user, so we need to use the hash to find the
	 published question
	 */
    private static class QuestionComparator implements Comparator {

        Map publishedItemHash;

        public QuestionComparator(Map m) {
            publishedItemHash = m;
        }

        public int compare(Object a, Object b) {
            ItemGradingData agrade = (ItemGradingData) ((List) a).get(0);
            ItemGradingData bgrade = (ItemGradingData) ((List) b).get(0);

            ItemDataIfc aitem = (ItemDataIfc) publishedItemHash.get(agrade
                    .getPublishedItemId());
            ItemDataIfc bitem = (ItemDataIfc) publishedItemHash.get(bgrade
                    .getPublishedItemId());

            Integer asectionseq = aitem.getSection().getSequence();
            Integer bsectionseq = bitem.getSection().getSequence();

            if (asectionseq < bsectionseq)
                return -1;
            else if (asectionseq > bsectionseq)
                return 1;

            Integer aitemseq = aitem.getSequence();
            Integer bitemseq = bitem.getSequence();

            if (aitemseq < bitemseq)
                return -1;
            else if (aitemseq > bitemseq)
                return 1;
            else
                return 0;

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
    @Slf4j
    private static class ResponsesComparator implements Comparator {
        boolean anonymous;

        public ResponsesComparator(boolean anony) {
            anonymous = anony;
        }

		public int compare(Object a, Object b) {
			RuleBasedCollator collator_ini = (RuleBasedCollator)Collator.getInstance();
			try{
				RuleBasedCollator collator = new RuleBasedCollator(collator_ini.getRules().replaceAll("<'\u005f'", "<' '<'\u005f'"));
				// For anonymous, it should return after the first element comparison
				if (anonymous) {
					Long aFirstElement = (Long) ((ArrayList) a).get(0);
					Long bFirstElement = (Long) ((ArrayList) b).get(0);
					if (aFirstElement.compareTo(bFirstElement) < 0)
						return -1;
					else if (aFirstElement.compareTo(bFirstElement) > 0)
						return 1;
					else
						return 0;
				}
				// For non-anonymous, it compares last names first, if it is the same,
				// compares first name, and then Eid
				else {
					String aFirstElement = (String) ((ArrayList) a).get(0);
					String bFirstElement = (String) ((ArrayList) b).get(0);
					if (collator.compare(aFirstElement, bFirstElement) < 0)
						return -1;
					else if (collator.compare(aFirstElement, bFirstElement) > 0)
						return 1;
					else {
						String aSecondElement = (String) ((ArrayList) a).get(1);
						String bSecondElement = (String) ((ArrayList) b).get(1);
						if (collator.compare(aSecondElement,bSecondElement) < 0)
							return -1;
						else if (collator.compare(aSecondElement,bSecondElement) > 0)
							return 1;
						else {
							String aThirdElement = (String) ((ArrayList) a).get(2);
							String bThirdElement = (String) ((ArrayList) b).get(2);
							if (collator.compare(aThirdElement,bThirdElement) < 0)
								return -1;
							else if (collator.compare(aThirdElement,bThirdElement) > 0)
								return 1;
						}
					}
					return 0;
				}
			} catch (ParseException e) {
	  			log.error("ERROR compare: ",e);
	  		}
			return Collator.getInstance().compare(a, b);	
		}
	}

    /**
     * A comparator to sort the items first by section sequence
     * and then by item sequence.
     */
    private static class ItemComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            PublishedItemData a = (PublishedItemData) o1;
            PublishedItemData b = (PublishedItemData) o2;
            if (a.getSection().getSequence() < b.getSection().getSequence()) {
                return -1;
            } else if (a.getSection().getSequence() > b.getSection().getSequence()) {
                return 1;
            } else {
                return a.getSequence() - b.getSequence();
            }
        }
    }

    public void removeUnsubmittedAssessmentGradingData(final AssessmentGradingData data) {
        final HibernateCallback<List<AssessmentGradingData>> hcb = session -> {
            Query q = session.createQuery(
                    "from AssessmentGradingData a where a.publishedAssessmentId = :id and a.agentId = :agent " +
                            "and a.forGrade = :forgrade and a.status = :status " +
                            "order by a.submittedDate desc");
            q.setParameter("id", data.getPublishedAssessmentId());
            q.setParameter("agent", data.getAgentId());
            q.setParameter("forgrade", false);
            q.setParameter("status", AssessmentGradingData.NO_SUBMISSION);
            return q.list();
        };
        List<AssessmentGradingData> assessmentGradings = getHibernateTemplate().execute(hcb);
        if (!assessmentGradings.isEmpty()) {
            deleteAll(assessmentGradings);
        }
    }

    public void removeAssessmentGradingData(final AssessmentGradingData data) {
    	data.setStatus(AssessmentGradingData.REMOVED);
    	data.setForGrade(false);
    	saveOrUpdateAssessmentGrading(data);
    }

    public boolean getHasGradingData(final Long publishedAssessmentId) {
        final HibernateCallback<List<AssessmentGradingData>> hcb = session -> {
            Query q = session.createQuery(
                    "from AssessmentGradingData a where a.publishedAssessmentId = :id and a.status > :status");
            q.setParameter("id", publishedAssessmentId);
            q.setParameter("status", AssessmentGradingData.REMOVED);
            return q.list();
        };
        List<AssessmentGradingData> assessmentGradings = getHibernateTemplate().execute(hcb);
        return !assessmentGradings.isEmpty();
    }

    public List<Boolean> getHasGradingDataAndHasSubmission(final Long publishedAssessmentId) {
        final HibernateCallback<List<AssessmentGradingData>> hcb = session -> {
            Query q = session.createQuery(
                    "from AssessmentGradingData a where a.publishedAssessmentId = :id and a.status > :status order by a.agentId asc, a.submittedDate desc");
            q.setParameter("id", publishedAssessmentId);
            q.setParameter("status", AssessmentGradingData.REMOVED);
            return q.list();
        };
        List<AssessmentGradingData> assessmentGradings = getHibernateTemplate().execute(hcb);
        // first element represents hasGradingData
        // second element represents hasSubmission
        List<Boolean> al = new ArrayList<>();
        if (assessmentGradings.size() == 0) {
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
                al.add(Boolean.FALSE);// no submission
            }
        }
        return al;
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

        Object[] values = {itemGradingId, agentId, bindVar.toString()};
        List list = getHibernateTemplate().findByNamedParam(
                "select filename from MediaData m where m.itemGradingData.itemGradingId = :id and m.createdBy = :agent and m.filename like :file",
                new String[]{"id", "agent", "file"},
                new Object[]{itemGradingId, agentId, bindVar.toString()});
        if (list.isEmpty()) {
            return filename;
        }

        HashSet hs = new HashSet();
        Iterator iter = list.iterator();
        String name;
        // Only add the filename which
        // 1. with no extension because the newly updated one has no extention
        // 2. name is same to filename or name like filename(...
        // For example, if the filename is ab. We only want ab, ab(1), ab(2)... and don't want abc to be in
        while (iter.hasNext()) {
            name = ((String) iter.next()).trim();
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
    }

    private String getFilenameWExtesion(Long itemGradingId, String agentId, String filename, int dotIndex) {
        String filenameWithoutExtension = filename.substring(0, dotIndex);
        StringBuilder bindVar = new StringBuilder(filenameWithoutExtension);
        bindVar.append("%");
        bindVar.append(filename.substring(dotIndex));

        List list = getHibernateTemplate().findByNamedParam(
                "select filename from MediaData m where m.itemGradingData.itemGradingId = :id and m.createdBy = :agent and m.filename like :file",
                new String[]{"id", "agent", "file"},
                new Object[]{itemGradingId, agentId, bindVar.toString()});
        if (list.isEmpty()) {
            return filename;
        }

        HashSet hs = new HashSet();
        Iterator iter = list.iterator();
        String name;
        int nameLenght;
        String extension = filename.substring(dotIndex);
        int extensionLength = extension.length();
        while (iter.hasNext()) {
            name = ((String) iter.next()).trim();
            if ((name.equals(filename) || name.startsWith(filenameWithoutExtension + "("))) {
                nameLenght = name.length();
                hs.add(name.substring(0, nameLenght - extensionLength));
            }
        }

        if (hs.isEmpty()) {
            return filename;
        }

        StringBuffer testName = new StringBuffer(filenameWithoutExtension);
        int i = 1;
        while (true) {
            if (!hs.contains(testName.toString())) {
                testName.append(extension);
                return testName.toString();
            } else {
                i++;
                testName = new StringBuffer(filenameWithoutExtension);
                testName.append("(");
                testName.append(i);
                testName.append(")");
            }
        }
    }

    public List getUpdatedAssessmentList(String agentId, String siteId) {
        List finalList = new ArrayList();
        List updatedAssessmentList = new ArrayList();
        List updatedAssessmentNeedResubmitListList = new ArrayList();

        List list = getHibernateTemplate()
                .findByNamedParam(
                        "select a.publishedAssessmentId, a.status from AssessmentGradingData a, AuthorizationData az " +
                                " where a.agentId = :agent and az.agentIdString = :site and az.functionId = :fid " +
                                " and az.qualifierId=a.publishedAssessmentId and a.forGrade = :forgrade and (a.status = :status1 or a.status = :status2) " +
                                " order by a.status",
                        new String[]{"agent", "site", "fid", "forgrade", "status1", "status2"},
                        new Object[]{agentId, siteId, "OWN_PUBLISHED_ASSESSMENT", false, AssessmentGradingData.ASSESSMENT_UPDATED, AssessmentGradingData.ASSESSMENT_UPDATED_NEED_RESUBMIT});
        if (list.isEmpty()) {
            return updatedAssessmentList;
        }

        Iterator iter = list.iterator();
        while (iter.hasNext()) {
            Object o[] = (Object[]) iter.next();
            if (AssessmentGradingData.ASSESSMENT_UPDATED_NEED_RESUBMIT.compareTo((Integer) o[1]) == 0) {
                updatedAssessmentNeedResubmitListList.add(o[0]);
            } else {
                updatedAssessmentList.add(o[0]);
            }
        }
        finalList.add(updatedAssessmentNeedResubmitListList);
        finalList.add(updatedAssessmentList);
        return finalList;
    }

    public List getSiteNeedResubmitList(String siteId) {
        List list = getHibernateTemplate()
                .findByNamedParam(
                        "select distinct a.publishedAssessmentId from AssessmentGradingData a, AuthorizationData au " +
                                "where au.functionId = :fid and au.agentIdString = :site and a.publishedAssessmentId = au.qualifierId " +
                                "and a.forGrade = :forgrade and a.status = :status",
                        new String[]{"fid", "site", "forgrade", "status"},
                        new Object[]{"OWN_PUBLISHED_ASSESSMENT", siteId, false, AssessmentGradingData.ASSESSMENT_UPDATED_NEED_RESUBMIT});
        return list;
    }

    @Override
    public int autoSubmitAssessments() {
        java.util.Date currentTime = new java.util.Date();

        Session session = getHibernateTemplate().getSessionFactory().getCurrentSession();

		Query query = session.createQuery("select new AssessmentGradingData(a.assessmentGradingId, a.publishedAssessmentId, " +
						" a.agentId, a.submittedDate, a.isLate, a.forGrade, a.totalAutoScore, a.totalOverrideScore, " +
						" a.finalScore, a.comments, a.status, a.gradedBy, a.gradedDate, a.attemptDate, a.timeElapsed) " +
						" from AssessmentGradingData a, PublishedAccessControl c " +
						" where a.publishedAssessmentId = c.assessment.publishedAssessmentId " +
						" and ((c.lateHandling = 1 and c.retractDate <= :currentTime) or (c.lateHandling = 2 and c.dueDate <= :currentTime))" +
						" and a.status not in (:status) and (a.hasAutoSubmissionRun = 0 or a.hasAutoSubmissionRun is null) and c.autoSubmit = 1 " +
						" and a.attemptDate is not null " +
						" order by a.publishedAssessmentId, a.agentId, a.forGrade desc, a.assessmentGradingId");
	    
		query.setTimestamp("currentTime",currentTime);
		query.setParameterList("status", Arrays.asList(AssessmentGradingData.REMOVED, AssessmentGradingData.NO_SUBMISSION) );
		query.setTimeout(300);

		List<AssessmentGradingData> list = query.list();

        Iterator<AssessmentGradingData> iter = list.iterator();
        String lastAgentId = "";
        Long lastPublishedAssessmentId = 0L;
        PublishedAssessmentFacade assessment = null;
        AssessmentGradingData adata = null;
        Map<Long, Set<PublishedSectionData>> sectionSetMap = new HashMap();


        PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();

        boolean updateGrades = IntegrationContextFactory.getInstance() != null;
        AutoSubmitFacadeQueriesAPI autoSubmitFacade = PersistenceService.getInstance().getAutoSubmitFacadeQueries();
        int failures = 0;
        
        while (iter.hasNext()) {

            try {
                adata = (AssessmentGradingData) iter.next();

                if (!lastPublishedAssessmentId.equals(adata.getPublishedAssessmentId())) {
                    assessment = publishedAssessmentService.getPublishedAssessmentQuick(adata.getPublishedAssessmentId().toString());
                }

                // this call happens in a separate transaction, so a rollback only affects this iteration
                boolean success = autoSubmitFacade.processAttempt(adata, updateGrades, this, assessment, currentTime, lastAgentId, lastPublishedAssessmentId, sectionSetMap);
                if (!success) {
                    ++failures;
                }

                lastPublishedAssessmentId = adata.getPublishedAssessmentId();
                lastAgentId = adata.getAgentId();
            } catch (Exception e) {
                ++failures;
                if (adata != null) {
                    log.error("Error while auto submitting assessment grade data id: " + adata.getAssessmentGradingId(),
                            e);
                } else {
                    log.error(e.getMessage(), e);
                }
            }
            finally {
                adata = null;
            }
        }

        return failures;
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
        ItemGradingAttachment itemGradingAttachment = (ItemGradingAttachment) getHibernateTemplate()
                .load(ItemGradingAttachment.class, attachmentId);
        ItemGradingData itemGrading = itemGradingAttachment.getItemGrading();
        // String resourceId = assessmentAttachment.getResourceId();
        int retryCount = persistenceHelper.getRetryCount();
        while (retryCount > 0) {
            try {
                if (itemGrading != null) {
                    Set<ItemGradingAttachment> set = itemGrading.getItemGradingAttachmentSet();
                    set.remove(itemGradingAttachment);
                    getHibernateTemplate().delete(itemGradingAttachment);
                    retryCount = 0;
                }
            } catch (Exception e) {
                log.warn("problem delete assessmentAttachment: "
                        + e.getMessage());
                retryCount = persistenceHelper.retryDeadlock(e,
                        retryCount);
            }
        }
    }

    public void removeAssessmentGradingAttachment(Long attachmentId) {
        AssessmentGradingAttachment assessmentGradingAttachment = (AssessmentGradingAttachment) getHibernateTemplate()
                .load(AssessmentGradingAttachment.class, attachmentId);
        AssessmentGradingData assessmentGrading = assessmentGradingAttachment.getAssessmentGrading();
        // String resourceId = assessmentAttachment.getResourceId();
        int retryCount = persistenceHelper.getRetryCount();
        while (retryCount > 0) {
            try {
                if (assessmentGrading != null) {
                    Set set = assessmentGrading.getAssessmentGradingAttachmentSet();
                    set.remove(assessmentGradingAttachment);
                    getHibernateTemplate().delete(assessmentGradingAttachment);
                    retryCount = 0;
                }
            } catch (Exception e) {
                log.warn("problem delete assessmentAttachment: "
                        + e.getMessage());
                retryCount = persistenceHelper.retryDeadlock(e,
                        retryCount);
            }
        }
    }

    public void saveOrUpdateAttachments(List<AttachmentIfc> list) {
        for (AttachmentIfc attachment : list) {
            getHibernateTemplate().saveOrUpdate(attachment);
        }
    }

    public HashMap getInProgressCounts(String siteId) {
        List list = getHibernateTemplate().findByNamedParam(
                "select a.publishedAssessmentId, count(*) from AssessmentGradingData a, AuthorizationData au " +
                        "where au.functionId = :fid and au.agentIdString = :site and a.publishedAssessmentId = au.qualifierId " +
                        "and a.forGrade = :forgrade and (a.status = :status1 or a.status = :status2) group by a.publishedAssessmentId",
                new String[]{"fid", "site", "forgrade", "status1", "status2"},
                new Object[]{"OWN_PUBLISHED_ASSESSMENT", siteId, false, AssessmentGradingData.IN_PROGRESS, AssessmentGradingData.ASSESSMENT_UPDATED});
        Iterator iter = list.iterator();
        HashMap inProgressCountsMap = new HashMap();
        while (iter.hasNext()) {
            Object o[] = (Object[]) iter.next();
            inProgressCountsMap.put(o[0], o[1]);
        }
        return inProgressCountsMap;
    }

    public HashMap getSubmittedCounts(String siteId) {
        List list = getHibernateTemplate().findByNamedParam(
                "select a.publishedAssessmentId, count(distinct a.agentId) " +
                        "from AssessmentGradingData a, AuthorizationData au, PublishedAssessmentData p " +
                        "where au.functionId = :fid and au.agentIdString = :site and a.publishedAssessmentId = au.qualifierId " +
                        "and a.forGrade = :forgrade and a.status > :status and a.publishedAssessmentId = p.publishedAssessmentId and " +
                        "(p.lastNeedResubmitDate is null or a.submittedDate >= p.lastNeedResubmitDate) group by a.publishedAssessmentId",
                new String[]{"fid", "site", "forgrade", "status"},
                new Object[]{"OWN_PUBLISHED_ASSESSMENT", siteId, true, AssessmentGradingData.REMOVED});
        Iterator iter = list.iterator();
        HashMap startedCountsMap = new HashMap();
        while (iter.hasNext()) {
            Object o[] = (Object[]) iter.next();
            startedCountsMap.put(o[0], o[1]);
        }
        return startedCountsMap;
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

        List<PublishedItemData> itemArrayList;
        Long publishedItemId;
        PublishedItemData publishedItemData;
        for (PublishedSectionData publishedSectionData : sectionSet) {
            log.debug("sectionId = " + publishedSectionData.getSectionId());
            itemArrayList = publishedSectionData.getItemArray();
            String authorType = publishedSectionData.getSectionMetaDataByLabel(SectionDataIfc.AUTHOR_TYPE);
            if (authorType != null && (authorType.equals(SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL.toString()) ||
                    authorType.equals(SectionDataIfc.FIXED_AND_RANDOM_DRAW_FROM_QUESTIONPOOL.toString()) || authorType.equals(SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOLS.toString()))) {
                log.debug("Fixed or Random draw from questionpool");
                long seed = (long) AgentFacade.getAgentString().hashCode();

                // If the section has a previous seed we must use it to use the same order.
                String sectionRandomizationSeed = publishedSectionData.getSectionMetaDataByLabel(SectionDataIfc.RANDOMIZATION_SEED);
                if (StringUtils.isNotBlank(sectionRandomizationSeed)) {
                    seed += Long.parseLong(sectionRandomizationSeed);
                }

                if (publishedSectionData.getSectionMetaDataByLabel(SectionDataIfc.RANDOMIZATION_TYPE) != null && publishedSectionData
                        .getSectionMetaDataByLabel(SectionDataIfc.RANDOMIZATION_TYPE)
                        .equals(SectionDataIfc.PER_SUBMISSION)) {
                    seed = (long) (assessmentGradingData.getAssessmentGradingId()
                            .toString() + "_" + publishedSectionData.getSectionId().toString()).hashCode();
                }

                if (authorType.equals(SectionDataIfc.FIXED_AND_RANDOM_DRAW_FROM_QUESTIONPOOL.toString())) {
                    // adding fixed questions
                    List<ItemDataIfc> sortedlist = itemArrayList.stream()
                        .filter(item -> ((PublishedItemData) item).getIsFixed())
                        .collect(Collectors.toList());

                    // removing isFixed questions from itemlist
                    itemArrayList.removeIf(item -> ((PublishedItemData) item).getIsFixed());

                    // getting all hashes from the sortedlist
                    List<String> distinctHashValues = sortedlist.stream()
                        .filter(item -> item instanceof PublishedItemData)
                        .map(item -> ((PublishedItemData) item).getHash())
                        .distinct()
                        .collect(Collectors.toList());

                    // removing from itemlist if there are hashes repeated -> avoid fixed questions on the random draw
                    itemArrayList.removeIf(item -> item instanceof PublishedItemData &&
                                                   distinctHashValues.stream().anyMatch(hash -> hash.equals(item.getHash())));
                }

                Collections.shuffle(itemArrayList, new Random(seed));

                Integer numberToBeDrawn = 0;
                if (publishedSectionData.getSectionMetaDataByLabel(SectionDataIfc.NUM_QUESTIONS_DRAWN) != null) {
                    numberToBeDrawn = Integer.valueOf(publishedSectionData.getSectionMetaDataByLabel(SectionDataIfc.NUM_QUESTIONS_DRAWN));
                }

                int samplesize = numberToBeDrawn;
                for (int i = 0; i < samplesize; i++) {
                    publishedItemData = (PublishedItemData) itemArrayList.get(i);
                    publishedItemId = publishedItemData.getItemId();
                    log.debug("publishedItemId = " + publishedItemId);
                    if (!answeredPublishedItemIdList.contains(publishedItemId)) {
                        saveItemGradingData(assessmentGradingData, publishedItemId);
                    }
                }
            } else {
                log.debug("Not random draw from questionpool");
                for (PublishedItemData pid : itemArrayList) {
                    publishedItemId = pid.getItemId();
                    log.debug("publishedItemId = " + publishedItemId);
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
        Double averageScore = 0.0;
        AssessmentGradingData ag = null;

        final HibernateCallback<List<AssessmentGradingData>> hcb = session -> {
            Query q = session.createQuery(
                    "from AssessmentGradingData a where a.publishedAssessmentId = :id and a.agentId = :agent and a.forGrade = :forgrade and a.status > :status order by  a.submittedDate desc");
            q.setParameter("id", publishedAssessmentId);
            q.setParameter("agent", agentId);
            q.setParameter("forgrade", true);
            q.setParameter("status", AssessmentGradingData.REMOVED);
            return q.list();
        };
        List<AssessmentGradingData> assessmentGradings = getHibernateTemplate().execute(hcb);

        if (!assessmentGradings.isEmpty()) {
            AssessmentGradingData agd;
            Double cumulativeScore = new Double(0);
            Iterator i = assessmentGradings.iterator();

            while (i.hasNext()) {
                agd = (AssessmentGradingData) i.next();
                cumulativeScore += agd.getFinalScore();
            }
            averageScore = cumulativeScore / assessmentGradings.size();

            DecimalFormat df = new DecimalFormat("0.##");
            DecimalFormatSymbols dfs = new DecimalFormatSymbols();
            dfs.setDecimalSeparator('.');
            df.setDecimalFormatSymbols(dfs);

            averageScore = new Double(df.format((double) averageScore));
        }
        return averageScore;
    }

    public List<AssessmentGradingData> getHighestSubmittedAssessmentGradingList(final Long publishedAssessmentId) {

        final HibernateCallback<List<AssessmentGradingData>> hcb = session -> {
            Query q = session.createQuery(
                    "from AssessmentGradingData a where a.publishedAssessmentId = :id and a.forGrade = :forgrade and a.status > :status order by a.agentId asc, a.finalScore desc");
            q.setParameter("id", publishedAssessmentId);
            q.setParameter("forgrade", true);
            q.setParameter("status", AssessmentGradingData.REMOVED);
            return q.list();
        };
        List<AssessmentGradingData> assessmentGradings = getHibernateTemplate().execute(hcb);

        return new ArrayList<>(assessmentGradings.stream()
                .collect(Collectors.toMap(AssessmentGradingData::getAgentId, p -> p, (p, q) -> p))
                .values());
    }

    public Map<Long, List<Long>> getAverageAssessmentGradingByPublishedItem(final Long publishedAssessmentId) {
        Map<Long, List<Long>> h = new HashMap<>();

        final HibernateCallback<List<AssessmentGradingData>> hcb = session -> {
            Query q = session.createQuery(
                    "select new AssessmentGradingData(" +
                            " a.assessmentGradingId, p.itemId, " +
                            " a.agentId, a.finalScore, a.submittedDate) " +
                            " from ItemGradingData i, AssessmentGradingData a," +
                            " PublishedItemData p where " +
                            " i.assessmentGradingId = a.assessmentGradingId and i.publishedItemId = p.itemId and " +
                            " a.publishedAssessmentId = :id and a.status > :status" +
                            " order by a.agentId asc, a.submittedDate desc"
            );
            q.setParameter("id", publishedAssessmentId);
            q.setParameter("status", AssessmentGradingData.REMOVED);
            return q.list();
        };

        List<AssessmentGradingData> assessmentGradings = getHibernateTemplate().execute(hcb);

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
    }

    private Map<Long, Set<ItemGradingAttachment>> getItemGradingAttachmentMap(final Set itemGradingIds) {

        final HibernateCallback<List<ItemGradingAttachment>> hcb = session -> {
            Query q = session.createQuery(
                    "from ItemGradingAttachment a where a.itemGrading.itemGradingId in (:itemGradingIds)");
            q.setParameterList("itemGradingIds", itemGradingIds);
            return q.list();
        };
        Set<ItemGradingAttachment> itemGradingAttachmentList = new HashSet<>(getHibernateTemplate().execute(hcb));
        return processItemGradingAttachment(itemGradingAttachmentList);
    }

    private Map<Long, Set<ItemGradingAttachment>> getItemGradingAttachmentMap(final Long publishedItemId) {

        final HibernateCallback<List<ItemGradingAttachment>> hcb = session -> {
            Query q = session.createQuery(
                    "select a from ItemGradingAttachment a where a.itemGrading.publishedItemId = :publishedItemId ");
            q.setParameter("publishedItemId", publishedItemId);
            return q.list();
        };
        Set<ItemGradingAttachment> itemGradingAttachmentSet = new HashSet<>(getHibernateTemplate().execute(hcb));
        return processItemGradingAttachment(itemGradingAttachmentSet);
    }

    public Map<Long, List<AssessmentGradingAttachment>> getAssessmentGradingAttachmentMap(final Long pubAssessmentId) {

        final HibernateCallback<List<AssessmentGradingAttachment>> hcb = session -> {
            Query q = session.createQuery(
                    "select a from AssessmentGradingAttachment a where a.assessmentGrading.publishedAssessmentId = :pubAssessmentId ");
            q.setParameter("pubAssessmentId", pubAssessmentId);
            return q.list();
        };
        List<AssessmentGradingAttachment> assessmentGradingAttachmentList = getHibernateTemplate().execute(hcb);
        return processAssessmentGradingAttachment(assessmentGradingAttachmentList);
    }

    public Map<Long, Set<ItemGradingAttachment>> getItemGradingAttachmentMapByAssessmentGradingId(final Long assessmentGradingId) {

        final HibernateCallback<List<ItemGradingAttachment>> hcb = session -> {
            Query q = session.createQuery(
                    "select a from ItemGradingAttachment a, ItemGradingData i " +
                            "where a.itemGrading.itemGradingId = i.itemGradingId " +
                            "and i.assessmentGradingId = :assessmentGradingId");
            q.setParameter("assessmentGradingId", assessmentGradingId);
            return q.list();
        };

        Set<ItemGradingAttachment> itemGradingAttachmentList = new HashSet<>(getHibernateTemplate().execute(hcb));
        return processItemGradingAttachment(itemGradingAttachmentList);
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
        final HibernateCallback<List<AssessmentGradingData>> hcb = session -> {
            Query q = session.createQuery(
                    "from AssessmentGradingData a where a.publishedAssessmentId = :id and a.agentId = :agent and a.forGrade = :forgrade and a.status > :status order by a.attemptDate desc");
            q.setParameter("id", publishedAssessmentId);
            q.setParameter("agent", agentIdString);
            q.setParameter("forgrade", false);
            q.setParameter("status", AssessmentGradingData.REMOVED);
            return q.list();
        };
        return getHibernateTemplate().execute(hcb);
    }
    
    public SectionGradingData getSectionGradingData(Long assessmentGradingId, Long sectionId, String agentId) {
        final HibernateCallback<List<SectionGradingData>> hcb = session -> {
            Query q = session.createQuery(
                    "from SectionGradingData s where " +
                        "s.assessmentGradingId = :assessmentGradingId " +
                        "and s.publishedSectionId = :sectionId " +
                        "and s.agentId = :agent");
            q.setParameter("assessmentGradingId", assessmentGradingId);
            q.setParameter("sectionId", sectionId);
            q.setParameter("agent", agentId);
            return q.list();
        };
        List<SectionGradingData> sectionGradings = getHibernateTemplate().execute(hcb);
        if (sectionGradings.isEmpty()) {
            return null;
        }
        return sectionGradings.get(0);
    }

    public void saveSectionGrading(SectionGradingData item) {
        int retryCount = persistenceHelper.getRetryCount();
        while (retryCount > 0) {
            try {
                getHibernateTemplate().saveOrUpdate(item);
                retryCount = 0;
            } catch (Exception e) {
                log.warn("problem saving sectionGrading: " + e.getMessage());
                retryCount = persistenceHelper.retryDeadlock(e, retryCount);
            }
        }
    }
}
