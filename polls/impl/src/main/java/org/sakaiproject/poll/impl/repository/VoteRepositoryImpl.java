/*
 * Copyright (c) 2003-2025 The Apereo Foundation
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
package org.sakaiproject.poll.impl.repository;

import java.util.Collections;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.sakaiproject.poll.api.model.Option;
import org.sakaiproject.poll.api.model.Poll;
import org.sakaiproject.poll.api.model.Vote;
import org.sakaiproject.poll.api.repository.VoteRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;
import org.springframework.stereotype.Repository;

@Repository
public class VoteRepositoryImpl extends SpringCrudRepositoryImpl<Vote, Long> implements VoteRepository {

    @Override
    public List<Vote> findByPollId(String pollId) {
        if (pollId == null) {
            return Collections.emptyList();
        }
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Vote> query = cb.createQuery(Vote.class);
        Root<Vote> root = query.from(Vote.class);
        Join<Vote, Option> optionJoin = root.join("option");
        Join<Option, Poll> pollJoin = optionJoin.join("poll");

        query.select(root)
                .where(cb.equal(pollJoin.get("id"), pollId));

        return sessionFactory.getCurrentSession()
                .createQuery(query)
                .getResultList();
    }

    @Override
    public List<Vote> findByOptionId(Long optionId) {
        if (optionId == null) return Collections.emptyList();

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Vote> query = cb.createQuery(Vote.class);
        Root<Vote> root = query.from(Vote.class);

        Predicate optionPredicate = cb.equal(root.get("option"), optionId);

        query.select(root)
                .where(optionPredicate);

        return sessionFactory.getCurrentSession()
                .createQuery(query)
                .getResultList();
    }

    @Override
    public List<Vote> findByUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return Collections.emptyList();
        }
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Vote> query = cb.createQuery(Vote.class);
        Root<Vote> root = query.from(Vote.class);

        query.select(root)
                .where(cb.equal(root.get("userId"), userId));

        return sessionFactory.getCurrentSession()
                .createQuery(query)
                .getResultList();
    }

    @Override
    public List<Vote> findByUserIdAndPollIds(String userId, List<String> pollIds) {
        if (userId == null || pollIds == null || pollIds.isEmpty()) {
            return Collections.emptyList();
        }
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Vote> query = cb.createQuery(Vote.class);
        Root<Vote> root = query.from(Vote.class);
        Join<Vote, Poll> pollJoin = root.join("poll");

        Predicate userPredicate = cb.equal(root.get("userId"), userId);
        Predicate pollsPredicate = pollJoin.get("id").in(pollIds);

        query.select(root)
                .where(cb.and(userPredicate, pollsPredicate));

        return sessionFactory.getCurrentSession()
                .createQuery(query)
                .getResultList();
    }

    @Override
    public boolean existsByPollIdAndUserId(String pollId, String userId) {
        if (pollId == null || userId == null || userId.trim().isEmpty()) {
            return false;
        }
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Vote> root = query.from(Vote.class);
        Join<Vote, Option> optionJoin = root.join("option");
        Join<Option, Poll> pollJoin = optionJoin.join("poll");

        Predicate pollPredicate = cb.equal(pollJoin.get("id"), pollId);
        Predicate userPredicate = cb.equal(root.get("userId"), userId);

        query.select(cb.count(root))
                .where(cb.and(pollPredicate, userPredicate));

        Long count = sessionFactory.getCurrentSession()
                .createQuery(query)
                .getSingleResult();

        return count > 0;
    }

    @Override
    public int countDistinctSubmissionIds(String pollId) {
        if (pollId == null) {
            return 0;
        }
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Vote> root = query.from(Vote.class);
        Join<Vote, Option> optionJoin = root.join("option");
        Join<Option, Poll> pollJoin = optionJoin.join("poll");

        query.select(cb.countDistinct(root.get("submissionId")))
                .where(cb.equal(pollJoin.get("id"), pollId));

        Long count = sessionFactory.getCurrentSession()
                .createQuery(query)
                .getSingleResult();

        return Math.toIntExact(count);
    }
}
