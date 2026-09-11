/**********************************************************************************
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************************/

package org.sakaiproject.videotraining.impl.repository;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;
import org.sakaiproject.videotraining.api.model.VideoProviderType;
import org.sakaiproject.videotraining.api.model.VideoPublicationStatus;
import org.sakaiproject.videotraining.api.model.VideoTrainingVideo;
import org.sakaiproject.videotraining.api.model.VideoVisibilityScope;
import org.sakaiproject.videotraining.api.repository.VideoTrainingVideoRepository;

public class VideoTrainingVideoRepositoryImpl extends SpringCrudRepositoryImpl<VideoTrainingVideo, String>
        implements VideoTrainingVideoRepository {

    @Override
    public List<VideoTrainingVideo> findBySiteIdOrderByModifiedOnDesc(String siteId) {
        if (siteId == null) {
            return Collections.emptyList();
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<VideoTrainingVideo> query = cb.createQuery(VideoTrainingVideo.class);
        Root<VideoTrainingVideo> root = query.from(VideoTrainingVideo.class);

        query.select(root)
                .where(cb.equal(root.get("siteId"), siteId))
                .orderBy(cb.desc(root.get("modifiedOn")));

        return sessionFactory.getCurrentSession().createQuery(query).getResultList();
    }

    @Override
    public List<VideoTrainingVideo> findBySiteIdOrderByModifiedOnDesc(String siteId, String searchText, int offset, int limit) {
        if (siteId == null) {
            return Collections.emptyList();
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<VideoTrainingVideo> query = cb.createQuery(VideoTrainingVideo.class);
        Root<VideoTrainingVideo> root = query.from(VideoTrainingVideo.class);

        Predicate predicate = buildSiteSearchPredicate(cb, root, siteId, searchText);

        query.select(root)
                .where(predicate)
                .orderBy(cb.desc(root.get("modifiedOn")));

        TypedQuery<VideoTrainingVideo> typedQuery = sessionFactory.getCurrentSession().createQuery(query);
        if (offset > 0) {
            typedQuery.setFirstResult(offset);
        }
        if (limit > 0) {
            typedQuery.setMaxResults(limit);
        }
        return typedQuery.getResultList();
    }

    @Override
    public List<VideoTrainingVideo> findBySiteIdOrderByModifiedOnDesc(String siteId, String searchText, List<String> categoryIds, int offset, int limit) {
        return findBySiteIdSorted(siteId, searchText, categoryIds, offset, limit, "modifiedOn", false);
    }

    @Override
    public List<VideoTrainingVideo> findBySiteIdAndOwnerIdOrderByModifiedOnDesc(String siteId, String ownerId) {
        if (siteId == null) {
            return Collections.emptyList();
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<VideoTrainingVideo> query = cb.createQuery(VideoTrainingVideo.class);
        Root<VideoTrainingVideo> root = query.from(VideoTrainingVideo.class);

        query.select(root)
                .where(cb.and(cb.equal(root.get("siteId"), siteId), cb.equal(root.get("ownerId"), ownerId)))
                .orderBy(cb.desc(root.get("modifiedOn")));

        return sessionFactory.getCurrentSession().createQuery(query).getResultList();
    }

    @Override
    public List<VideoTrainingVideo> findBySiteIdAndOwnerIdOrderByModifiedOnDesc(String siteId, String ownerId, String searchText, int offset, int limit) {
        if (siteId == null) {
            return Collections.emptyList();
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<VideoTrainingVideo> query = cb.createQuery(VideoTrainingVideo.class);
        Root<VideoTrainingVideo> root = query.from(VideoTrainingVideo.class);

        Predicate base = cb.and(cb.equal(root.get("siteId"), siteId), cb.equal(root.get("ownerId"), ownerId));
        Predicate predicate = buildSiteSearchPredicate(cb, root, siteId, searchText);
        Predicate finalPredicate = predicate == null ? base : cb.and(base, predicate);

        query.select(root)
                .where(finalPredicate)
                .orderBy(cb.desc(root.get("modifiedOn")));

        TypedQuery<VideoTrainingVideo> typedQuery = sessionFactory.getCurrentSession().createQuery(query);
        if (offset > 0) {
            typedQuery.setFirstResult(offset);
        }
        if (limit > 0) {
            typedQuery.setMaxResults(limit);
        }
        return typedQuery.getResultList();
    }

    @Override
    public List<VideoTrainingVideo> findBySiteIdAndOwnerIdOrderByModifiedOnDesc(String siteId, String ownerId, String searchText, List<String> categoryIds, int offset, int limit) {
        return findBySiteIdAndOwnerIdSorted(siteId, ownerId, searchText, categoryIds, offset, limit, "modifiedOn", false);
    }

    @Override
    public List<VideoTrainingVideo> findBySiteIdAndOwnerIdSorted(String siteId, String ownerId, String searchText, int offset, int limit,
            String sortField, boolean ascending) {
        if (siteId == null) {
            return Collections.emptyList();
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<VideoTrainingVideo> query = cb.createQuery(VideoTrainingVideo.class);
        Root<VideoTrainingVideo> root = query.from(VideoTrainingVideo.class);

        Predicate base = cb.and(cb.equal(root.get("siteId"), siteId), cb.equal(root.get("ownerId"), ownerId));
        Predicate searchPredicate = buildSearchPredicate(cb, root, searchText);
        Predicate finalPredicate = searchPredicate == null ? base : cb.and(base, searchPredicate);

        query.select(root)
                .where(finalPredicate)
                .orderBy(buildSortOrders(cb, root, sortField, ascending));

        TypedQuery<VideoTrainingVideo> typedQuery = sessionFactory.getCurrentSession().createQuery(query);
        if (offset > 0) {
            typedQuery.setFirstResult(offset);
        }
        if (limit > 0) {
            typedQuery.setMaxResults(limit);
        }
        return typedQuery.getResultList();
    }

    @Override
    public List<VideoTrainingVideo> findBySiteIdAndOwnerIdSorted(String siteId, String ownerId, String searchText, List<String> categoryIds, int offset, int limit,
            String sortField, boolean ascending) {
        if (siteId == null) {
            return Collections.emptyList();
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<VideoTrainingVideo> query = cb.createQuery(VideoTrainingVideo.class);
        Root<VideoTrainingVideo> root = query.from(VideoTrainingVideo.class);
        Predicate predicate = buildOwnerSearchWithCategoriesPredicate(cb, root, siteId, ownerId, searchText, categoryIds);

        query.select(root)
                .where(predicate)
                .orderBy(buildSortOrders(cb, root, sortField, ascending));

        TypedQuery<VideoTrainingVideo> typedQuery = sessionFactory.getCurrentSession().createQuery(query);
        if (offset > 0) {
            typedQuery.setFirstResult(offset);
        }
        if (limit > 0) {
            typedQuery.setMaxResults(limit);
        }
        return typedQuery.getResultList();
    }

    @Override
    public List<VideoTrainingVideo> findBySiteIdSorted(String siteId, String searchText, int offset, int limit,
            String sortField, boolean ascending) {
        if (siteId == null) {
            return Collections.emptyList();
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<VideoTrainingVideo> query = cb.createQuery(VideoTrainingVideo.class);
        Root<VideoTrainingVideo> root = query.from(VideoTrainingVideo.class);

        Predicate predicate = buildSiteSearchPredicate(cb, root, siteId, searchText);

        query.select(root)
                .where(predicate)
                .orderBy(buildSortOrders(cb, root, sortField, ascending));

        TypedQuery<VideoTrainingVideo> typedQuery = sessionFactory.getCurrentSession().createQuery(query);
        if (offset > 0) {
            typedQuery.setFirstResult(offset);
        }
        if (limit > 0) {
            typedQuery.setMaxResults(limit);
        }
        return typedQuery.getResultList();
    }

    @Override
    public List<VideoTrainingVideo> findBySiteIdSorted(String siteId, String searchText, List<String> categoryIds, int offset, int limit,
            String sortField, boolean ascending) {
        if (siteId == null) {
            return Collections.emptyList();
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<VideoTrainingVideo> query = cb.createQuery(VideoTrainingVideo.class);
        Root<VideoTrainingVideo> root = query.from(VideoTrainingVideo.class);
        Predicate predicate = buildSearchWithCategoriesPredicate(cb, root, siteId, searchText, categoryIds);

        query.select(root)
                .where(predicate)
                .orderBy(buildSortOrders(cb, root, sortField, ascending));

        TypedQuery<VideoTrainingVideo> typedQuery = sessionFactory.getCurrentSession().createQuery(query);
        if (offset > 0) {
            typedQuery.setFirstResult(offset);
        }
        if (limit > 0) {
            typedQuery.setMaxResults(limit);
        }
        return typedQuery.getResultList();
    }

    @Override
    public List<VideoTrainingVideo> findBySiteIdOrderByModifiedOnDescCursor(String siteId, String searchText,
            Instant cursorModifiedOn, String cursorVideoId, int limit) {
        if (siteId == null) {
            return Collections.emptyList();
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<VideoTrainingVideo> query = cb.createQuery(VideoTrainingVideo.class);
        Root<VideoTrainingVideo> root = query.from(VideoTrainingVideo.class);

        Predicate predicate = buildSiteSearchPredicate(cb, root, siteId, searchText);
        predicate = appendCursorPredicate(cb, root, predicate, cursorModifiedOn, cursorVideoId);

        query.select(root)
                .where(predicate)
                .orderBy(cb.desc(root.get("modifiedOn")), cb.desc(root.get("id")));

        TypedQuery<VideoTrainingVideo> typedQuery = sessionFactory.getCurrentSession().createQuery(query);
        if (limit > 0) {
            typedQuery.setMaxResults(limit);
        }
        return typedQuery.getResultList();
    }

    @Override
    public long countBySiteId(String siteId, String searchText) {
        if (siteId == null) {
            return 0;
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<VideoTrainingVideo> root = query.from(VideoTrainingVideo.class);

        Predicate predicate = buildSiteSearchPredicate(cb, root, siteId, searchText);

        query.select(cb.count(root)).where(predicate);
        return sessionFactory.getCurrentSession().createQuery(query).getSingleResult();
    }

    @Override
    public long countBySiteId(String siteId, String searchText, List<String> categoryIds) {
        if (siteId == null) {
            return 0;
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<VideoTrainingVideo> root = query.from(VideoTrainingVideo.class);
        Predicate predicate = buildSearchWithCategoriesPredicate(cb, root, siteId, searchText, categoryIds);

        query.select(cb.countDistinct(root)).where(predicate);
        return sessionFactory.getCurrentSession().createQuery(query).getSingleResult();
    }

    @Override
    public long countBySiteIdAndOwnerId(String siteId, String ownerId, String searchText) {
        if (siteId == null) {
            return 0;
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<VideoTrainingVideo> root = query.from(VideoTrainingVideo.class);

        Predicate base = cb.and(cb.equal(root.get("siteId"), siteId), cb.equal(root.get("ownerId"), ownerId));
        Predicate searchPredicate = buildSearchPredicate(cb, root, searchText);
        Predicate finalPredicate = searchPredicate == null ? base : cb.and(base, searchPredicate);

        query.select(cb.count(root)).where(finalPredicate);
        return sessionFactory.getCurrentSession().createQuery(query).getSingleResult();
    }

    @Override
    public long countBySiteIdAndOwnerId(String siteId, String ownerId, String searchText, List<String> categoryIds) {
        if (siteId == null) {
            return 0;
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<VideoTrainingVideo> root = query.from(VideoTrainingVideo.class);
        Predicate predicate = buildOwnerSearchWithCategoriesPredicate(cb, root, siteId, ownerId, searchText, categoryIds);

        query.select(cb.countDistinct(root)).where(predicate);
        return sessionFactory.getCurrentSession().createQuery(query).getSingleResult();
    }

    @Override
    public long countByGlobal(String searchText) {
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<VideoTrainingVideo> root = query.from(VideoTrainingVideo.class);

        Predicate scopePredicate = cb.equal(root.get("visibilityScope"), VideoVisibilityScope.GLOBAL);
        Predicate publicationPredicate = cb.equal(root.get("publicationStatus"), VideoPublicationStatus.PUBLISHED);

        Predicate releasePredicate = cb.or(
                cb.isNull(root.get("releaseDate")),
                cb.lessThanOrEqualTo(root.get("releaseDate"), Instant.now())
        );

        Predicate retractPredicate = cb.or(
                cb.isNull(root.get("retractDate")),
                cb.greaterThan(root.get("retractDate"), Instant.now())
        );

        Predicate searchPredicate = buildSearchPredicate(cb, root, searchText);

        Predicate finalPredicate = searchPredicate == null
                ? cb.and(scopePredicate, publicationPredicate, releasePredicate, retractPredicate)
                : cb.and(scopePredicate, publicationPredicate, releasePredicate, retractPredicate, searchPredicate);

        query.select(cb.count(root)).where(finalPredicate);

        return sessionFactory.getCurrentSession().createQuery(query).getSingleResult();
    }

    @Override
    public List<VideoTrainingVideo> findBySiteIdAndCategoryIds(String siteId, List<String> categoryIds) {
        if (siteId == null || categoryIds == null || categoryIds.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> filteredCategoryIds = new HashSet<>();
        for (String categoryId : categoryIds) {
            if (StringUtils.isNotBlank(categoryId)) {
                filteredCategoryIds.add(categoryId);
            }
        }
        if (filteredCategoryIds.isEmpty()) {
            return Collections.emptyList();
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<VideoTrainingVideo> query = cb.createQuery(VideoTrainingVideo.class);
        Root<VideoTrainingVideo> root = query.from(VideoTrainingVideo.class);
        Join<Object, Object> categoryJoin = root.join("categories");

        query.select(root)
                .distinct(true)
                .where(cb.and(
                        cb.equal(root.get("siteId"), siteId),
                        categoryJoin.get("id").in(filteredCategoryIds)
                ));

        return sessionFactory.getCurrentSession().createQuery(query).getResultList();
    }

    @Override
    public List<VideoTrainingVideo> findByCategoryIds(List<String> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> filteredCategoryIds = new HashSet<>();
        for (String categoryId : categoryIds) {
            if (StringUtils.isNotBlank(categoryId)) {
                filteredCategoryIds.add(categoryId);
            }
        }
        if (filteredCategoryIds.isEmpty()) {
            return Collections.emptyList();
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<VideoTrainingVideo> query = cb.createQuery(VideoTrainingVideo.class);
        Root<VideoTrainingVideo> root = query.from(VideoTrainingVideo.class);
        Join<Object, Object> categoryJoin = root.join("categories");

        query.select(root)
                .distinct(true)
                .where(categoryJoin.get("id").in(filteredCategoryIds));

        return sessionFactory.getCurrentSession().createQuery(query).getResultList();
    }

    @Override
    public List<VideoTrainingVideo> findVisibleBySiteIdAt(String siteId, Instant now) {
        if (siteId == null || now == null) {
            return Collections.emptyList();
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<VideoTrainingVideo> query = cb.createQuery(VideoTrainingVideo.class);
        Root<VideoTrainingVideo> root = query.from(VideoTrainingVideo.class);

        Predicate sitePredicate = cb.equal(root.get("siteId"), siteId);
        Predicate releasePredicate = cb.or(
                cb.isNull(root.get("releaseDate")),
                cb.lessThanOrEqualTo(root.get("releaseDate"), now)
        );
        Predicate retractPredicate = cb.or(
                cb.isNull(root.get("retractDate")),
                cb.greaterThan(root.get("retractDate"), now)
        );

        query.select(root)
                .where(cb.and(sitePredicate, releasePredicate, retractPredicate))
                .orderBy(cb.desc(root.get("modifiedOn")));

        return sessionFactory.getCurrentSession().createQuery(query).getResultList();
    }

    @Override
    public List<VideoTrainingVideo> findVisibleBySiteIdAt(String siteId, Instant now, String searchText, int offset, int limit) {
        if (siteId == null || now == null) {
            return Collections.emptyList();
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<VideoTrainingVideo> query = cb.createQuery(VideoTrainingVideo.class);
        Root<VideoTrainingVideo> root = query.from(VideoTrainingVideo.class);

        Predicate visibilityPredicate = buildVisiblePredicate(cb, root, siteId, now);
        Predicate searchPredicate = buildSearchPredicate(cb, root, searchText);
        Predicate finalPredicate = searchPredicate == null ? visibilityPredicate : cb.and(visibilityPredicate, searchPredicate);

        query.select(root)
                .where(finalPredicate)
                .orderBy(cb.desc(root.get("modifiedOn")));

        TypedQuery<VideoTrainingVideo> typedQuery = sessionFactory.getCurrentSession().createQuery(query);
        if (offset > 0) {
            typedQuery.setFirstResult(offset);
        }
        if (limit > 0) {
            typedQuery.setMaxResults(limit);
        }
        return typedQuery.getResultList();
    }

    @Override
    public List<VideoTrainingVideo> findVisibleBySiteIdAt(String siteId, Instant now, String searchText, List<String> categoryIds, int offset, int limit) {
        return findVisibleBySiteIdAtSorted(siteId, now, searchText, categoryIds, offset, limit, "modifiedOn", false);
    }

    @Override
    public List<VideoTrainingVideo> findVisibleBySiteIdAtSorted(String siteId, Instant now, String searchText, int offset, int limit,
            String sortField, boolean ascending) {
        if (siteId == null || now == null) {
            return Collections.emptyList();
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<VideoTrainingVideo> query = cb.createQuery(VideoTrainingVideo.class);
        Root<VideoTrainingVideo> root = query.from(VideoTrainingVideo.class);

        Predicate visibilityPredicate = buildVisiblePredicate(cb, root, siteId, now);
        Predicate searchPredicate = buildSearchPredicate(cb, root, searchText);
        Predicate finalPredicate = searchPredicate == null ? visibilityPredicate : cb.and(visibilityPredicate, searchPredicate);

        query.select(root)
                .where(finalPredicate)
                .orderBy(buildSortOrders(cb, root, sortField, ascending));

        TypedQuery<VideoTrainingVideo> typedQuery = sessionFactory.getCurrentSession().createQuery(query);
        if (offset > 0) {
            typedQuery.setFirstResult(offset);
        }
        if (limit > 0) {
            typedQuery.setMaxResults(limit);
        }
        return typedQuery.getResultList();
    }

    @Override
    public List<VideoTrainingVideo> findVisibleBySiteIdAtSorted(String siteId, Instant now, String searchText, List<String> categoryIds, int offset, int limit,
            String sortField, boolean ascending) {
        if (siteId == null || now == null) {
            return Collections.emptyList();
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<VideoTrainingVideo> query = cb.createQuery(VideoTrainingVideo.class);
        Root<VideoTrainingVideo> root = query.from(VideoTrainingVideo.class);

        Predicate predicate = buildVisibleSearchWithCategoriesPredicate(cb, root, siteId, now, searchText, categoryIds);

        query.select(root)
                .where(predicate)
                .orderBy(buildSortOrders(cb, root, sortField, ascending));

        TypedQuery<VideoTrainingVideo> typedQuery = sessionFactory.getCurrentSession().createQuery(query);
        if (offset > 0) {
            typedQuery.setFirstResult(offset);
        }
        if (limit > 0) {
            typedQuery.setMaxResults(limit);
        }
        return typedQuery.getResultList();
    }

    @Override
    public List<VideoTrainingVideo> findVisibleBySiteIdAtCursor(String siteId, Instant now, String searchText,
            Instant cursorModifiedOn, String cursorVideoId, int limit) {
        if (siteId == null || now == null) {
            return Collections.emptyList();
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<VideoTrainingVideo> query = cb.createQuery(VideoTrainingVideo.class);
        Root<VideoTrainingVideo> root = query.from(VideoTrainingVideo.class);

        Predicate visibilityPredicate = buildVisiblePredicate(cb, root, siteId, now);
        Predicate searchPredicate = buildSearchPredicate(cb, root, searchText);
        Predicate predicate = searchPredicate == null ? visibilityPredicate : cb.and(visibilityPredicate, searchPredicate);
        predicate = appendCursorPredicate(cb, root, predicate, cursorModifiedOn, cursorVideoId);

        query.select(root)
                .where(predicate)
                .orderBy(cb.desc(root.get("modifiedOn")), cb.desc(root.get("id")));

        TypedQuery<VideoTrainingVideo> typedQuery = sessionFactory.getCurrentSession().createQuery(query);
        if (limit > 0) {
            typedQuery.setMaxResults(limit);
        }
        return typedQuery.getResultList();
    }

    @Override
    public List<VideoTrainingVideo> findVisibleByGlobal(String searchText, int offset, int size) {
        if (size <= 0) {
            return Collections.emptyList();
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<VideoTrainingVideo> query = cb.createQuery(VideoTrainingVideo.class);
        Root<VideoTrainingVideo> root = query.from(VideoTrainingVideo.class);

        Predicate globalPredicate = cb.equal(root.get("visibilityScope"),
            VideoVisibilityScope.GLOBAL);

        Predicate publishedPredicate = cb.equal(root.get("publicationStatus"),
            VideoPublicationStatus.PUBLISHED);

        Predicate releasePredicate = cb.or(
                cb.isNull(root.get("releaseDate")),
                cb.lessThanOrEqualTo(root.get("releaseDate"), Instant.now())
        );

        Predicate retractPredicate = cb.or(
                cb.isNull(root.get("retractDate")),
                cb.greaterThan(root.get("retractDate"), Instant.now())
        );

        Predicate searchPredicate = buildSearchPredicate(cb, root, searchText);
        Predicate finalPredicate = searchPredicate == null
            ? cb.and(globalPredicate, publishedPredicate, releasePredicate, retractPredicate)
            : cb.and(globalPredicate, publishedPredicate, releasePredicate, retractPredicate, searchPredicate);

        query.select(root)
            .where(finalPredicate)
            .orderBy(cb.desc(root.get("modifiedOn")));

        return sessionFactory.getCurrentSession()
                .createQuery(query)
                .setFirstResult(offset)
                .setMaxResults(size)
                .getResultList();
    }

    @Override
    public List<VideoTrainingVideo> adminFindAllGlobal(String searchText, int offset, int size) {
        if (size <= 0) {
            return Collections.emptyList();
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<VideoTrainingVideo> query = cb.createQuery(VideoTrainingVideo.class);
        Root<VideoTrainingVideo> root = query.from(VideoTrainingVideo.class);

        Predicate scopePredicate = cb.equal(root.get("visibilityScope"), VideoVisibilityScope.GLOBAL);
        Predicate searchPredicate = buildSearchPredicate(cb, root, searchText);

        Predicate finalPredicate = searchPredicate == null ? scopePredicate : cb.and(scopePredicate, searchPredicate);

        query.select(root)
                .where(finalPredicate)
                .orderBy(cb.desc(root.get("modifiedOn")));

        TypedQuery<VideoTrainingVideo> typedQuery = sessionFactory.getCurrentSession().createQuery(query);
        if (offset > 0) {
            typedQuery.setFirstResult(offset);
        }
        if (size > 0) {
            typedQuery.setMaxResults(size);
        }
        return typedQuery.getResultList();
    }

    @Override
    public List<VideoTrainingVideo> findAll(String searchText, int offset, int size) {
        if (size <= 0) {
            return Collections.emptyList();
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<VideoTrainingVideo> query = cb.createQuery(VideoTrainingVideo.class);
        Root<VideoTrainingVideo> root = query.from(VideoTrainingVideo.class);

        Predicate searchPredicate = buildSearchPredicate(cb, root, searchText);

        query.select(root);
        if (searchPredicate != null) {
            query.where(searchPredicate);
        }
        query.orderBy(cb.desc(root.get("modifiedOn")));

        TypedQuery<VideoTrainingVideo> typedQuery = sessionFactory.getCurrentSession().createQuery(query);
        if (offset > 0) {
            typedQuery.setFirstResult(offset);
        }
        if (size > 0) {
            typedQuery.setMaxResults(size);
        }
        return typedQuery.getResultList();
    }

    @Override
    public long adminCountAllGlobal(String searchText) {
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<VideoTrainingVideo> root = query.from(VideoTrainingVideo.class);

        Predicate scopePredicate = cb.equal(root.get("visibilityScope"), VideoVisibilityScope.GLOBAL);
        Predicate searchPredicate = buildSearchPredicate(cb, root, searchText);

        Predicate finalPredicate = searchPredicate == null ? scopePredicate : cb.and(scopePredicate, searchPredicate);

        query.select(cb.count(root)).where(finalPredicate);
        return sessionFactory.getCurrentSession().createQuery(query).getSingleResult();
    }

    @Override
    public long countAll(String searchText) {
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<VideoTrainingVideo> root = query.from(VideoTrainingVideo.class);

        Predicate searchPredicate = buildSearchPredicate(cb, root, searchText);

        query.select(cb.count(root));
        if (searchPredicate != null) {
            query.where(searchPredicate);
        }
        return sessionFactory.getCurrentSession().createQuery(query).getSingleResult();
    }

    @Override
    public long countVisibleBySiteIdAt(String siteId, Instant now, String searchText) {
        if (siteId == null || now == null) {
            return 0;
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<VideoTrainingVideo> root = query.from(VideoTrainingVideo.class);

        Predicate visibilityPredicate = buildVisiblePredicate(cb, root, siteId, now);
        Predicate searchPredicate = buildSearchPredicate(cb, root, searchText);
        Predicate finalPredicate = searchPredicate == null ? visibilityPredicate : cb.and(visibilityPredicate, searchPredicate);

        query.select(cb.count(root)).where(finalPredicate);
        return sessionFactory.getCurrentSession().createQuery(query).getSingleResult();
    }

    @Override
    public long countVisibleBySiteIdAt(String siteId, Instant now, String searchText, List<String> categoryIds) {
        if (siteId == null || now == null) {
            return 0;
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<VideoTrainingVideo> root = query.from(VideoTrainingVideo.class);
        Predicate predicate = buildVisibleSearchWithCategoriesPredicate(cb, root, siteId, now, searchText, categoryIds);

        query.select(cb.countDistinct(root)).where(predicate);
        return sessionFactory.getCurrentSession().createQuery(query).getSingleResult();
    }

    @Override
    public long sumNativeStorageBytesBySiteId(String siteId) {
        if (siteId == null) {
            return 0L;
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<VideoTrainingVideo> root = query.from(VideoTrainingVideo.class);

        Predicate sitePredicate = cb.equal(root.get("siteId"), siteId);
        Predicate nativePredicate = cb.equal(root.get("providerType"), VideoProviderType.NATIVE);
        Expression<Long> fileSize = root.get("fileSizeBytes");

        query.select(cb.coalesce(cb.sum(fileSize), 0L)).where(cb.and(sitePredicate, nativePredicate));
        return sessionFactory.getCurrentSession().createQuery(query).getSingleResult();
    }

    private Predicate buildSiteSearchPredicate(CriteriaBuilder cb, Root<VideoTrainingVideo> root, String siteId, String searchText) {
        Predicate sitePredicate = cb.equal(root.get("siteId"), siteId);
        Predicate searchPredicate = buildSearchPredicate(cb, root, searchText);
        return searchPredicate == null ? sitePredicate : cb.and(sitePredicate, searchPredicate);
    }

    private Predicate buildVisiblePredicate(CriteriaBuilder cb, Root<VideoTrainingVideo> root, String siteId, Instant now) {
        Predicate sitePredicate = cb.equal(root.get("siteId"), siteId);
        Predicate publicationPredicate = cb.or(
            cb.isNull(root.get("publicationStatus")),
            cb.equal(root.get("publicationStatus"), VideoPublicationStatus.PUBLISHED)
        );
        Predicate scopePredicate = cb.or(
            cb.isNull(root.get("visibilityScope")),
            cb.notEqual(root.get("visibilityScope"), VideoVisibilityScope.LESSON)
        );
        Predicate releasePredicate = cb.or(
                cb.isNull(root.get("releaseDate")),
                cb.lessThanOrEqualTo(root.get("releaseDate"), now)
        );
        Predicate retractPredicate = cb.or(
                cb.isNull(root.get("retractDate")),
                cb.greaterThan(root.get("retractDate"), now)
        );
        return cb.and(sitePredicate, publicationPredicate, scopePredicate, releasePredicate, retractPredicate);
    }

    private Predicate buildSearchPredicate(CriteriaBuilder cb, Root<VideoTrainingVideo> root, String searchText) {
        String trimmed = StringUtils.trimToEmpty(searchText);
        if (StringUtils.isBlank(trimmed)) {
            return null;
        }

        String pattern = "%" + trimmed.toLowerCase() + "%";
        Predicate titlePredicate = cb.like(cb.lower(root.get("title")), pattern);
        Predicate descriptionPredicate = cb.like(cb.lower(root.get("description")), pattern);
        Predicate sourcePredicate = cb.like(cb.lower(root.get("sourceReference")), pattern);
        return cb.or(titlePredicate, descriptionPredicate, sourcePredicate);
    }

    private Predicate buildSearchWithCategoriesPredicate(CriteriaBuilder cb, Root<VideoTrainingVideo> root,
            String siteId, String searchText, List<String> categoryIds) {
        Predicate sitePredicate = cb.equal(root.get("siteId"), siteId);
        Predicate searchPredicate = buildSearchPredicate(cb, root, searchText);
        Predicate categoryPredicate = buildCategoryPredicate(root, categoryIds);

        Predicate predicate = cb.and(sitePredicate, categoryPredicate);
        if (searchPredicate != null) {
            predicate = cb.and(predicate, searchPredicate);
        }
        return predicate;
    }

    private Predicate buildOwnerSearchWithCategoriesPredicate(CriteriaBuilder cb, Root<VideoTrainingVideo> root,
            String siteId, String ownerId, String searchText, List<String> categoryIds) {
        Predicate base = cb.and(cb.equal(root.get("siteId"), siteId), cb.equal(root.get("ownerId"), ownerId));
        Predicate searchPredicate = buildSearchPredicate(cb, root, searchText);
        Predicate categoryPredicate = buildCategoryPredicate(root, categoryIds);

        Predicate predicate = cb.and(base, categoryPredicate);
        if (searchPredicate != null) {
            predicate = cb.and(predicate, searchPredicate);
        }
        return predicate;
    }

    private Predicate buildVisibleSearchWithCategoriesPredicate(CriteriaBuilder cb, Root<VideoTrainingVideo> root,
            String siteId, Instant now, String searchText, List<String> categoryIds) {
        Predicate visibilityPredicate = buildVisiblePredicate(cb, root, siteId, now);
        Predicate searchPredicate = buildSearchPredicate(cb, root, searchText);
        Predicate categoryPredicate = buildCategoryPredicate(root, categoryIds);

        Predicate predicate = cb.and(visibilityPredicate, categoryPredicate);
        if (searchPredicate != null) {
            predicate = cb.and(predicate, searchPredicate);
        }
        return predicate;
    }

    private Predicate buildCategoryPredicate(Root<VideoTrainingVideo> root, List<String> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return root.get("id").isNotNull();
        }

        Set<String> filteredCategoryIds = new HashSet<>();
        for (String categoryId : categoryIds) {
            if (StringUtils.isNotBlank(categoryId)) {
                filteredCategoryIds.add(categoryId);
            }
        }

        if (filteredCategoryIds.isEmpty()) {
            return root.get("id").isNotNull();
        }

        Join<Object, Object> categoryJoin = root.join("categories");
        return categoryJoin.get("id").in(filteredCategoryIds);
    }

    private Predicate appendCursorPredicate(CriteriaBuilder cb, Root<VideoTrainingVideo> root,
            Predicate basePredicate, Instant cursorModifiedOn, String cursorVideoId) {
        if (cursorModifiedOn == null || StringUtils.isBlank(cursorVideoId)) {
            return basePredicate;
        }

        Predicate earlierModified = cb.lessThan(root.get("modifiedOn"), cursorModifiedOn);
        Predicate sameModifiedEarlierId = cb.and(
                cb.equal(root.get("modifiedOn"), cursorModifiedOn),
                cb.lessThan(root.get("id"), cursorVideoId)
        );
        return cb.and(basePredicate, cb.or(earlierModified, sameModifiedEarlierId));
    }

    @Override
    public List<VideoTrainingVideo> findGlobalPublishedCursor(String searchText, Instant cursorModifiedOn, String cursorVideoId, int limit) {
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<VideoTrainingVideo> query = cb.createQuery(VideoTrainingVideo.class);
        Root<VideoTrainingVideo> root = query.from(VideoTrainingVideo.class);

        Predicate scopePredicate = cb.equal(root.get("visibilityScope"), VideoVisibilityScope.GLOBAL);
        Predicate publicationPredicate = cb.equal(root.get("publicationStatus"), VideoPublicationStatus.PUBLISHED);
        Predicate searchPredicate = buildSearchPredicate(cb, root, searchText);
        Predicate predicate = searchPredicate == null ? cb.and(scopePredicate, publicationPredicate) : cb.and(scopePredicate, publicationPredicate, searchPredicate);
        predicate = appendCursorPredicate(cb, root, predicate, cursorModifiedOn, cursorVideoId);

        query.select(root)
                .where(predicate)
                .orderBy(cb.desc(root.get("modifiedOn")), cb.desc(root.get("id")));

        TypedQuery<VideoTrainingVideo> typedQuery = sessionFactory.getCurrentSession().createQuery(query);
        if (limit > 0) {
            typedQuery.setMaxResults(limit);
        }
        return typedQuery.getResultList();
    }

    @Override
    public List<VideoTrainingVideo> findGlobalPublishedSorted(String searchText, int offset, int limit,
            String sortField, boolean ascending) {
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<VideoTrainingVideo> query = cb.createQuery(VideoTrainingVideo.class);
        Root<VideoTrainingVideo> root = query.from(VideoTrainingVideo.class);

        Predicate scopePredicate = cb.equal(root.get("visibilityScope"), VideoVisibilityScope.GLOBAL);
        Predicate publicationPredicate = cb.equal(root.get("publicationStatus"), VideoPublicationStatus.PUBLISHED);
        Predicate searchPredicate = buildSearchPredicate(cb, root, searchText);
        Predicate predicate = searchPredicate == null ? cb.and(scopePredicate, publicationPredicate) : cb.and(scopePredicate, publicationPredicate, searchPredicate);

        query.select(root)
                .where(predicate)
                .orderBy(buildSortOrders(cb, root, sortField, ascending));

        TypedQuery<VideoTrainingVideo> typedQuery = sessionFactory.getCurrentSession().createQuery(query);
        if (offset > 0) {
            typedQuery.setFirstResult(offset);
        }
        if (limit > 0) {
            typedQuery.setMaxResults(limit);
        }
        return typedQuery.getResultList();
    }

    private List<Order> buildSortOrders(CriteriaBuilder cb, Root<VideoTrainingVideo> root,
            String sortField, boolean ascending) {
        String effectiveSortField = normalizeSortField(sortField);
        Order primaryOrder = ascending ? cb.asc(root.get(effectiveSortField)) : cb.desc(root.get(effectiveSortField));
        Order tieBreakModified = cb.desc(root.get("modifiedOn"));
        Order tieBreakId = cb.desc(root.get("id"));
        return List.of(primaryOrder, tieBreakModified, tieBreakId);
    }

    private String normalizeSortField(String sortField) {
        if (StringUtils.isBlank(sortField)) {
            return "modifiedOn";
        }

        switch (sortField) {
            case "title":
            case "siteId":
            case "providerType":
            case "visibilityScope":
            case "publicationStatus":
            case "releaseDate":
            case "retractDate":
            case "modifiedOn":
                return sortField;
            default:
                return "modifiedOn";
        }
    }
}
