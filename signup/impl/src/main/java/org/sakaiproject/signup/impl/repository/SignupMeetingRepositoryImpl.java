/**
 * Copyright (c) 2007-2016 The Apereo Foundation
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
package org.sakaiproject.signup.impl.repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import org.sakaiproject.signup.api.repository.SignupMeetingRepository;
import org.sakaiproject.signup.api.model.SignupMeeting;
import org.sakaiproject.signup.api.model.SignupSite;
import org.sakaiproject.signup.api.model.SignupTimeslot;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

/**
 * Implementation of SignupMeetingRepository using Spring Data pattern.
 * Provides methods to access the database for retrieving, creating, updating
 * and removing SignupMeeting objects.
 */
@Slf4j
@Transactional
public class SignupMeetingRepositoryImpl extends SpringCrudRepositoryImpl<SignupMeeting, Long>
		implements SignupMeetingRepository {

	@Override
	public List<SignupMeeting> findAllBySiteId(String siteId) {
		if (siteId == null) return List.of();

		CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<SignupMeeting> query = cb.createQuery(SignupMeeting.class);
		Root<SignupMeeting> root = query.from(SignupMeeting.class);
		Join<SignupMeeting, SignupSite> signupSites = root.join("signupSites");

		query.select(root)
				.where(cb.equal(signupSites.get("siteId"), siteId))
				.orderBy(cb.asc(root.get("startTime")));

		return sessionFactory.getCurrentSession()
				.createQuery(query)
				.getResultList();
	}

	@Override
	public List<SignupMeeting> findBySiteIdAndStartTimeBefore(String siteId, Date searchEndDate) {
		if (siteId == null || searchEndDate == null) return List.of();

		CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<SignupMeeting> query = cb.createQuery(SignupMeeting.class);
		Root<SignupMeeting> root = query.from(SignupMeeting.class);
		Join<SignupMeeting, SignupSite> signupSites = root.join("signupSites");

		query.select(root)
				.where(
						cb.and(
								cb.equal(signupSites.get("siteId"), siteId),
								cb.lessThanOrEqualTo(root.get("startTime"), searchEndDate)
						)
				)
				.orderBy(cb.asc(root.get("startTime")));

		return sessionFactory.getCurrentSession()
				.createQuery(query)
				.getResultList();
	}

	@Override
	public List<SignupMeeting> findBySiteIdAndDateRange(String siteId, Date startDate, Date endDate) {
		if (siteId == null || startDate == null || endDate == null) return List.of();

		CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<SignupMeeting> query = cb.createQuery(SignupMeeting.class);
		Root<SignupMeeting> root = query.from(SignupMeeting.class);
		Join<SignupMeeting, SignupSite> signupSites = root.join("signupSites");

		query.select(root).distinct(true)
				.where(
						cb.and(
								cb.equal(signupSites.get("siteId"), siteId),
								cb.greaterThanOrEqualTo(root.get("endTime"), startDate),
								cb.lessThan(root.get("startTime"), endDate)
						)
				)
				.orderBy(cb.asc(root.get("startTime")));

		return sessionFactory.getCurrentSession()
				.createQuery(query)
				.getResultList();
	}

	@Override
	public List<SignupMeeting> findInSiteByDateRange(String siteId, Date startDate, Date endDate) {
		if (siteId == null || startDate == null || endDate == null) return List.of();

		CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<SignupMeeting> query = cb.createQuery(SignupMeeting.class);
		Root<SignupMeeting> root = query.from(SignupMeeting.class);
		Join<SignupMeeting, SignupSite> signupSites = root.join("signupSites");

		query.select(root).distinct(true)
				.where(
						cb.and(
								cb.equal(signupSites.get("siteId"), siteId),
								cb.greaterThanOrEqualTo(root.get("endTime"), startDate),
								cb.lessThan(root.get("startTime"), endDate)
						)
				)
				.orderBy(cb.asc(root.get("startTime")));

		return sessionFactory.getCurrentSession()
				.createQuery(query)
				.getResultList();
	}

	@Override
	public List<SignupMeeting> findInSitesByDateRange(List<String> siteIds, Date startDate, Date endDate) {
		if (siteIds == null || siteIds.isEmpty() || startDate == null || endDate == null) return List.of();

		CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<SignupMeeting> query = cb.createQuery(SignupMeeting.class);
		Root<SignupMeeting> root = query.from(SignupMeeting.class);
		Join<SignupMeeting, SignupSite> signupSites = root.join("signupSites");

		query.select(root).distinct(true)
				.where(
						cb.and(
								signupSites.get("siteId").in(siteIds),
								cb.greaterThanOrEqualTo(root.get("endTime"), startDate),
								cb.lessThan(root.get("startTime"), endDate)
						)
				)
				.orderBy(cb.asc(root.get("startTime")));

		return sessionFactory.getCurrentSession()
				.createQuery(query)
				.getResultList();
	}

	@Override
	public List<SignupMeeting> findRecurringMeetings(String siteId, Long recurrenceId, Date currentTime) {
		if (siteId == null || recurrenceId == null || currentTime == null) return List.of();

		CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<SignupMeeting> query = cb.createQuery(SignupMeeting.class);
		Root<SignupMeeting> root = query.from(SignupMeeting.class);
		Join<SignupMeeting, SignupSite> signupSites = root.join("signupSites");

		query.select(root).distinct(true)
				.where(
						cb.and(
								cb.equal(signupSites.get("siteId"), siteId),
								cb.equal(root.get("recurrenceId"), recurrenceId),
								cb.greaterThan(root.get("endTime"), currentTime)
						)
				)
				.orderBy(cb.asc(root.get("startTime")));

		return sessionFactory.getCurrentSession()
				.createQuery(query)
				.getResultList();
	}

	@Override
	public void updateAll(List<SignupMeeting> meetings) {
		if (meetings == null || meetings.isEmpty()) return;

		for (SignupMeeting meeting : meetings) {
			save(meeting);
		}
	}

	@Override
	public void updateMeetingsAndRemoveTimeslots(List<SignupMeeting> meetings, List<SignupTimeslot> removedTimeslots) {
		if (meetings != null && !meetings.isEmpty()) {
			for (SignupMeeting meeting : meetings) {
				save(meeting);
			}
		}

		// Remove the deleted timeslot and related attendees/wait-list people
		if (removedTimeslots != null && !removedTimeslots.isEmpty()) {
			for (SignupTimeslot ts : removedTimeslots) {
				long tsId = ts.getId();
				Optional<SignupTimeslot> sTimeslot = loadSignupTimeslot(tsId);
				sTimeslot.ifPresent(timeslot -> sessionFactory.getCurrentSession().delete(timeslot));
			}
		}
	}

	private Optional<SignupTimeslot> loadSignupTimeslot(Long timeslotId) {
		if (timeslotId == null) return Optional.empty();

		CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<SignupTimeslot> query = cb.createQuery(SignupTimeslot.class);
		Root<SignupTimeslot> root = query.from(SignupTimeslot.class);

		query.select(root).distinct(true)
				.where(cb.equal(root.get("id"), timeslotId));

		SignupTimeslot result = sessionFactory.getCurrentSession()
				.createQuery(query)
				.uniqueResult();

		return Optional.ofNullable(result);
	}

	@Override
	public int countAutoReminderMeetings(Date startDate, Date endDate) {
		if (startDate == null || endDate == null) return 0;

		CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<SignupMeeting> root = query.from(SignupMeeting.class);

		query.select(cb.count(root))
				.where(
						cb.and(
								cb.equal(root.get("autoReminder"), true),
								cb.between(root.get("startTime"), startDate, endDate)
						)
				);

		Long count = sessionFactory.getCurrentSession()
				.createQuery(query)
				.uniqueResult();

		return count != null ? count.intValue() : 0;
	}

	@Override
	public List<SignupMeeting> findAutoReminderMeetings(Date startDate, Date endDate) {
		if (startDate == null || endDate == null) return List.of();

		CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<SignupMeeting> query = cb.createQuery(SignupMeeting.class);
		Root<SignupMeeting> root = query.from(SignupMeeting.class);

		query.select(root).distinct(true)
				.where(
						cb.and(
								cb.equal(root.get("autoReminder"), true),
								cb.lessThanOrEqualTo(root.get("startTime"), endDate),
								cb.greaterThanOrEqualTo(root.get("endTime"), startDate)
						)
				)
				.orderBy(cb.asc(root.get("startTime")));

		return sessionFactory.getCurrentSession()
				.createQuery(query)
				.getResultList();
	}

	@Override
	public List<String> findAllCategoriesBySiteId(String siteId) {
		if (siteId == null) return List.of();

		CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<String> query = cb.createQuery(String.class);
		Root<SignupMeeting> root = query.from(SignupMeeting.class);
		Join<SignupMeeting, SignupSite> signupSites = root.join("signupSites");

		query.select(root.get("category")).distinct(true)
				.where(cb.equal(signupSites.get("siteId"), siteId))
				.orderBy(cb.asc(root.get("category")));

		List<String> categories = sessionFactory.getCurrentSession()
				.createQuery(query)
				.getResultList();

		return categories != null && !categories.isEmpty() ? categories : List.of();
	}

	@Override
	public List<String> findAllLocationsBySiteId(String siteId) {
		if (siteId == null) return List.of();

		CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<String> query = cb.createQuery(String.class);
		Root<SignupMeeting> root = query.from(SignupMeeting.class);
		Join<SignupMeeting, SignupSite> signupSites = root.join("signupSites");

		query.select(root.get("location")).distinct(true)
				.where(cb.equal(signupSites.get("siteId"), siteId))
				.orderBy(cb.asc(root.get("location")));

		List<String> locations = sessionFactory.getCurrentSession()
				.createQuery(query)
				.getResultList();

		return locations != null && !locations.isEmpty() ? locations : List.of();
	}
}
