/**
 * Copyright (c) 2003-2019 The Apereo Foundation
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
package org.sakaiproject.acadtermmanage.logic.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.sakaiproject.acadtermmanage.AcademicTermConstants;
import org.sakaiproject.acadtermmanage.exceptions.DuplicateKeyException;
import org.sakaiproject.acadtermmanage.exceptions.NoSuchKeyException;
import org.sakaiproject.acadtermmanage.logic.AcademicSessionLogic;
import org.sakaiproject.acadtermmanage.logic.AcademicSessionSakaiProxy;
import org.sakaiproject.acadtermmanage.model.Semester;
import org.sakaiproject.coursemanagement.api.AcademicSession;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AcademicSessionLogicImpl implements AcademicSessionLogic {

	@Setter
	private AcademicSessionSakaiProxy asSakaiProxy;

	@Setter
	private CourseManagementService cmService;

	// Since I was asked to only use official Sakai service APIs and not access the Sakai tables directly,
	// I can't use DB transactions for updating the term-related properties in Sakai sites.
	// But I can a at least use something to synchronize on so that site updates only run one at a time (per JVM)
	private static final Object LOCK_FOR_SITE_UPDATES = new Object();

	// No application-level caching here: getAcademicSession()/getAcademicSessions()/
	// getCurrentAcademicSessions() are all backed by Hibernate's 2nd-level/query cache
	// (see AcademicSessionCmImpl + default-query-results-region in ignite-components.xml),
	// which is already distributed across all Sakai nodes via Ignite. A separate cache here
	// would just be caching a cache.

	@Override
	public Semester getSemester(String eid) {
		log.debug("get for eid: {}", eid);
		AcademicSession t = cmService.getAcademicSession(eid);
		if (t != null) {
			Semester sem = Semester.createFromAcademicSession(t);
			HashSet<String> currentEids = getCurrentSessionEIDs();
			if (currentEids != null && currentEids.contains(eid)) {
				sem.setCurrent(true);
			}
			return sem;
		}
		else {
			log.debug("didnt find eid {} in the database", eid);
			return null;
		}
	}


	public void init() {
		// just because we've defined it in the components.xml and it
	}

	private HashSet<String> getCurrentSessionEIDs() {
		List<AcademicSession> currents = asSakaiProxy.getCurrentAcademicSessions();
		int curSize = currents != null ? currents.size() : 0;
		if (curSize == 0) {
			return null;
		}
		HashSet<String> currentEids = new HashSet<String>(curSize);
		for (AcademicSession as : currents) {
			currentEids.add(as.getEid());
		}
		return currentEids;
	}

	@Override
	public List<Semester> getSemesters() {
		List<AcademicSession> ass = cmService.getAcademicSessions();

		int size = ass != null ? ass.size() : 0;

		HashSet<String> currentEids = getCurrentSessionEIDs();

		ArrayList<Semester> semesterList = new ArrayList<Semester>(size);
		if (size > 0) {
			for (AcademicSession as : ass) {
				Semester sem = Semester.createFromAcademicSession(as);
				semesterList.add(sem);
				String eid = sem.getEid();
				if (currentEids != null && currentEids.contains(eid)) {
					sem.setCurrent(true);
					log.debug("setting current: {}", eid);
				}
			}
		}
		return semesterList;
	}

	/**
	 * Checks whether the parameter collection contains an AcademicSession whose eid equals the parameter eid.
	 *
	 * @param currents the collection of AcademicSessions to check
	 * @param eid the eid to look for
	 * @return true if the collection contains an AcademicSession with a matching eid
	 */
	private static final boolean containsAcademicSessionWithEID(Collection<AcademicSession> currents, String eid) {
		if (currents != null && eid != null) {
			for (AcademicSession as:currents) {
				if (eid.equals(as.getEid())) {
					return true;
				}
			}
		}
		return false;
	}

	// hack: AcademicSessions don't have a "current" property
	// The only way to change the "current"-status of an AcademicSession via
	// Sakai's Coursemanagement admin service is to (re)set the list of ALL current AcademicSessions
	private void updateCurrentStatus(Semester sem) {
		String eid = sem.getEid();

		List<AcademicSession> currents = asSakaiProxy.getCurrentAcademicSessions();

		boolean oldCurrentStatus = containsAcademicSessionWithEID(currents, eid);
		boolean newCurrentStatus = sem.isCurrent();

		// if the current status is the same, we have nothing to do:
		if (oldCurrentStatus == newCurrentStatus) {
			return;
		}

		// okay, "sem" is either a new Semester or one with a changed "current" value

		int size = currents != null ? currents.size() : 0;
		ArrayList<String> eids = new ArrayList<String>(size + 1); // +1 because we might have to add "sem"
		if (size > 0) { // add all the old eids except that aren't the the eid of "sem"
			for (AcademicSession as : currents) {
				String asEid = as.getEid();
				if (!asEid.equals(eid)) {
					eids.add(asEid);
				}
			}
		}
		// NOW we can add the eid of "sem" (if necessary)
		if (newCurrentStatus) {
			eids.add(sem.getEid());
		}

		// update the DB:
		asSakaiProxy.setCurrentAcademicSessions(eids);
	}

	@Override
	public boolean addSemester(Semester session) throws DuplicateKeyException {
		checkAccess();
		AcademicSession newAs = asSakaiProxy.addAcademicSession(session);
		if (newAs != null) {
			// hack to set the "current" flag of the AcademicSession in the DB because the
			// API's "AcademicSession" doesn't provide direct access to the "current" property.
			updateCurrentStatus(session);
			asSakaiProxy.notifyEventServiceOfInsert(newAs.getEid());
			return true;
		}
		else {
			log.error("API call must've failed to add the following AcademicSession to the DB: {}", session);
			return false;
		}
	}

	private boolean updateAllSites(String oldEID, String newEID, String newTitle) {
		checkAccess();
		boolean didSomething = false;
		synchronized(LOCK_FOR_SITE_UPDATES) {
			List<Site> sites = asSakaiProxy.getSitesForTerm(oldEID);
			int count = sites != null ? sites.size() : 0;
			log.debug("site update: #sites={}", count);
			if (count > 0) {
				for (Site s : sites) {
					String siteID = s.getId();
					try {
						s = asSakaiProxy.getSite(siteID);
					} catch (IdUnusedException e1) {
							log.warn("problem getting site id {}", siteID);
							return false;
					}
					/* !! IMPORTANT: Without this getSite(), saveSite() will wipe out
					things like pages/tool/sections in the site, because the search for sites only returns
					"light" site objects which don't contain all the site's data (?? why didn't they just
					make the Site object keep track of its internal "light version" vs "full version" state
					and opted for possibly data loss instead of making the SiteService throw an Exception
					similar to a LazyInstantiationException ?? ) */
					ResourcePropertiesEdit props = s.getPropertiesEdit();
					String eid = props.getProperty(AcademicTermConstants.PROP_NAME_TERM_EID);
					log.debug("site's term eid: {}", eid);
					if (oldEID.equals(eid)) {
						log.debug("updating site properties for {}", siteID);
						props.addProperty(AcademicTermConstants.PROP_NAME_TERM_EID, newEID);
						props.addProperty(AcademicTermConstants.PROP_NAME_TERM_TITLE, newTitle);

						boolean success = true;
						try {
							asSakaiProxy.saveSite(s);
						}
						catch (IdUnusedException | PermissionException e) {
							log.info("problem saving {}", s.getId());
							success = false;
						}
						if (!didSomething && success) {
							didSomething = true;
						}
					}
				}
			}
		}
		return didSomething;

	}

	@Override
	public void updateSemester(String oldEID, Semester newValues) throws NoSuchKeyException {
		log.debug("update Semester!");
		checkAccess();
		AcademicSession session = cmService.getAcademicSession(oldEID);
		if (session != null) {
			String oldTitle = session.getTitle();
			String newTitle = newValues.getTitle();
			String newEID = newValues.getEid();
			final boolean EID_CHANGED = !newEID.equals(oldEID);
			final boolean TITLE_CHANGED = !newTitle.equals(oldTitle);
			// update DB
			updateAcademicSessionPropertiesFromSemester(session, newValues);
			asSakaiProxy.updateAcademicSession(session);
			updateCurrentStatus(newValues);

			// updates site table
			log.debug("oldEID={} | newEID={} | oldTitle={} | newTitle={}",
				oldEID, newEID, oldTitle, newTitle);

			if (EID_CHANGED || TITLE_CHANGED) {
				updateAllSites(oldEID, newEID, newTitle);
			}

			asSakaiProxy.notifyEventServiceOfUpdate(oldEID);
			if (EID_CHANGED) {
				asSakaiProxy.notifyEventServiceOfUpdate(newEID);
			}


		}
		else {
			throw new NoSuchKeyException("E-ID \"" + oldEID
					+ "\" of the AcademicSession I'm supposed to update doesn't exist in the database");
		}
	}

	private static final void updateAcademicSessionPropertiesFromSemester(AcademicSession target, Semester source) {
		target.setEid(source.getEid());
		target.setDescription(source.getDescription());
		target.setTitle(source.getTitle());
		target.setStartDate(source.getStartDate());
		target.setEndDate(source.getEndDate());
	}

	@Override
	public void removeSemester(String eid) {
		checkAccess();
		asSakaiProxy.removeAcademicSession(eid);
	}

	@Override
	public boolean isAcademicSessionManager() {
		return asSakaiProxy.isSuperUser();
	}

	private final void checkAccess() {
		if (!isAcademicSessionManager()) {
			throw new SecurityException("you need the permission to be an admin to perform this action");
		}
	}
}
