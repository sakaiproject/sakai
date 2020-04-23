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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
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
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

@Slf4j
public class AcademicSessionLogicImpl implements AcademicSessionLogic {
	
	@Setter
	private AcademicSessionSakaiProxy asSakaiProxy;
	
	@Setter
	private CourseManagementService cmService;
	
	// Used to cache Semesters by their E-IDs and also a couple of semester lists.
	// Using two different types of objects as cache keys is rather ugly, but should work because
	// the cache's internal HashMap isn't parameterized i.e. it's simply using objects of type "Object" as keys.	
	@Setter 
	private Cache cache;	 

	
	// Not using Strings as cache keys for Semester lists, because the cache also stores single Semesters 
	// and those single Semesters use their user-supplied EID-Strings as cache keys. 
	// So if I use something else than Strings as keys, I can use the cache for both types of values and don't
	// have to care about the problem of someone entering an EID which is the same as one of those two
	// hard-coded cache keys here.
	//
	// Also, using a numeric type instead of a String is probably faster anyway should the cache be backed by a HashMap
	// ;-)
	private static final Short CACHE_KEY_LIST = new Short((short) 10);
	private static final Short CACHE_KEY_CURRENTS = new Short((short) 20);

	
	// Since I was asked to only use official Sakai service APIs and not access the Sakai tables directly, 
	// I can't use DB transactions for updating the term-related properties in Sakai sites. 
	// But I can a at least use something to synchronize on so that site updates only run one at a time (per JVM) 
	private static final Object LOCK_FOR_SITE_UPDATES = new Object();
	
	@Override
	public Semester getSemester(String eid) {
		log.debug("get for eid: {}", eid);
		// check cache
		synchronized (cache) { // getSemesters() might be updating the cache at the same time..
			Element element = cache.get(eid);
			if (element != null) {
				log.debug("cache hit for {}", eid);
				return (Semester) element.getObjectValue();
			}
		}
		// Usually, getSemesters() will be called first by the Wicket tool and this call will have added 
		// all semesters to the cache, so right now it's unlikely that the following code will ever be executed.
		// (Though, in theory, a Semester's cache entry could expire between being created by a call to getSemesters()
		// and this call to getSemester(eid) which we're currently processing.)
		
		log.debug("cache fail for {}", eid);
		AcademicSession t = cmService.getAcademicSession(eid);
		if (t != null) {
			Semester sem = Semester.createFromAcademicSession(t);
			HashSet<String> currentEids = getCurrentSessionEIDs();
			if (currentEids != null && currentEids.contains(eid)) {
				sem.setCurrent(true);
			}
			log.debug("Adding item to cache for: {}", eid);
			synchronized (cache) {
				// It's theoretically possible that the above cache lookup failed because the Semester's cache entry has
				// expired.
				// PROBLEM: the list of all Semesters might still be cached and already (still) contain a Semester
				// object with the same EID.
				// => If we simply put our freshly created "sem" into the cache, we'll have two different semester
				// objects for the same ID: the one we just created and the one in the list returned by getSemesters()
				Element cachedList = cache.get(CACHE_KEY_LIST);
				if (cachedList == null) {
					// no cached list yet => we can cache our object and let getSemesters() worry
					// about keeping everything in sync
					cache.put(new Element(eid, sem));
				}
				else { // problem case as described above: WAT DO? Two ideas:
						// a) look up the Semester object with the matching EID in the list, update it and then cache
						//    the old, but updated Semester object instead of the one we created above
						// b) simply toss the cached list out of the cache and let getSemesters() create a new one the
						//    next time it's called
					//
					// Lets do a)
					@SuppressWarnings("unchecked")
					List<Semester> semList = (List<Semester>) cachedList.getObjectValue();
					// lets look for the old Semester in O(n)..
					boolean foundIt = false;
					for (Semester semester : semList) {
						if (eid.equals(semester.getEid())) {
							log.debug("No new, fix old! -> updating and caching the old object");
							try {
								BeanUtils.copyProperties(semester, sem);
							} catch (IllegalAccessException | InvocationTargetException e) {
								// TODO Auto-generated catch block
								log.warn("Could not copy properties", e);
							}
							cache.put(new Element(eid, semester));
							foundIt = true;
							break;
						}
					}
					if (!foundIt) { // not sure if that can actually happen, but lets play it safe and just add it
						log.debug("didn't find the old instance -> adding and caching the new object");
						semList.add(sem);
						cache.put(new Element(eid, sem));
					}
					// What b) might look like: 
					// cache.put(new Element(eid,sem));
					// cache.remove(CACHE_KEY_LIST);
					// ..well, it certainly beats a) on simplicity. 
				}
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

	private HashSet<String> getCurrentSessionEIDsFromCache() {
		Element element = cache.get(CACHE_KEY_CURRENTS);
		if (element != null) {
			log.debug("Getting list of current sessions from cache");
			@SuppressWarnings("unchecked")
			HashSet<String> objectValue = (HashSet<String>) element.getObjectValue();
			return objectValue;
		}
		else {
			return null;
		}

	}

	private HashSet<String> getCurrentSessionEIDs() {
		HashSet<String> currentEids = getCurrentSessionEIDsFromCache();
		if (currentEids == null) {
			List<AcademicSession> currents = asSakaiProxy.getCurrentAcademicSessions();
			int curSize = currents != null ? currents.size() : 0;
			if (curSize > 0) {
				currentEids = new HashSet<String>(curSize);
				for (AcademicSession as : currents) {
					String eid = as.getEid();
					currentEids.add(eid);
				}
				cache.put(new Element(CACHE_KEY_CURRENTS, currentEids));
			}
		}
		return currentEids;
	}

	@Override
	public List<Semester> getSemesters() {
		// using a cache for the list because this method is called all the time
		// by the webapp (Wicket)-objects doing the sorting and paging of the list.
		Element element = cache.get(CACHE_KEY_LIST);
		if (element != null) {
			log.debug("Getting list of semesters from cache");
			@SuppressWarnings("unchecked")
			List<Semester> objectValue = (List<Semester>) element.getObjectValue();
			return objectValue;
		}
		log.debug("Loading semester list from database");
		List<AcademicSession> ass = cmService.getAcademicSessions();

		int size = ass != null ? ass.size() : 0;

		HashSet<String> currentEids = getCurrentSessionEIDs();

		ArrayList<Semester> semesterList = new ArrayList<Semester>(size);
		if (size > 0) {
			synchronized (cache) { // synchronized so that getSemester() can't add a semester instance which isn't also
									// in the cached list
				for (AcademicSession as : ass) {
					Semester sem = Semester.createFromAcademicSession(as);
					semesterList.add(sem);
					String eid = sem.getEid();
					if (currentEids != null && currentEids.contains(eid)) {
						sem.setCurrent(true);
						log.debug("setting current: {}", eid);
					}
					cache.put(new Element(eid, sem)); // ensuring that getSemester(eid) will return the same semester instance
				}
				cache.put(new Element(CACHE_KEY_LIST, semesterList));
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

		// getting currently current sessions from DB instead of cache
		List<AcademicSession> currents = asSakaiProxy.getCurrentAcademicSessions(); // not using a possibly outdated, cached list is
																   // probably better

		boolean oldCurrentStatus = containsAcademicSessionWithEID(currents, eid);
		boolean newCurrentStatus = sem.isCurrent();

		// if the current status is the same, we have nothing to do:
		if (oldCurrentStatus == newCurrentStatus) {
			// => nothing to change in the DB,
			// but let's make sure that our cached entry for "sem" is up-to-date, too :
			HashSet<String> cachedEids = getCurrentSessionEIDsFromCache();
			if (cachedEids != null) {
				if (newCurrentStatus) {
					cachedEids.add(sem.getEid());
				}
				else {
					cachedEids.remove(sem.getEid());
				}
			}
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

		// updating the possibly existing cached list of currently current sessions
		HashSet<String> currentlyCached = getCurrentSessionEIDsFromCache();
		if (currentlyCached != null) {
			// let's toss away and replace everything to make really, really sure
			// that the cached list matches the database
			currentlyCached.clear();
			currentlyCached.addAll(eids);
		}
	}

	@Override
	public boolean addSemester(Semester session) throws DuplicateKeyException {
		checkAccess();
		AcademicSession newAs = asSakaiProxy.addAcademicSession(session);
		if (newAs != null) {
			// hack to set the "current" flag of the AcademicSession in the DB because the
			// API's "AcademicSession" doesn't provide direct access to the "current" property.
			updateCurrentStatus(session);

			// adding the newly created Semester to a possibly cached list of semesters
			Element element = cache.get(CACHE_KEY_LIST);
			if (element != null) {
				log.debug("adding the new semester to the cached semester list");
				@SuppressWarnings("unchecked")
				List<Semester> semlist = (List<Semester>) element.getObjectValue();
				semlist.add(session);
			}
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
			Semester oldData = getSemester(oldEID);

			// updates site table
			log.debug("oldEID={} | newEID={} | oldTitle={} | newTitle={}",
				oldEID, newEID, oldTitle, newTitle);
			
			if (EID_CHANGED || TITLE_CHANGED) {
				updateAllSites(oldEID, newEID, newTitle);
			}
			
			
			// update cache
			if (!oldData.equals(newValues)) {
				log.debug("updating the cached Semester instance with new values");
				// it might be a bit safer to use the "newValues" object itself, but in theory
				// it should be enough to copy over the values to update the caches..
				try {
					BeanUtils.copyProperties(oldData, newValues);
				} catch (IllegalAccessException | InvocationTargetException e) {
					// TODO Auto-generated catch block
					log.warn("Could not copy properties", e);
				}
				// ..unless, of course, it was the EID that has been updated.
				// because that is also used as the cache key for the single element cache.
				// Shouldn't affect the cached list, though.
				cache.remove(oldEID);
				cache.put(new Element(newValues.getEid(), oldData));

			}
			else {
				log.debug("was given a reference to the cached Semester instance, i.e. the cache is already updated");

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
