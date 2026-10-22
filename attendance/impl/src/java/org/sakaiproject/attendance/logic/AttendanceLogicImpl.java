/*
 *  Copyright (c) 2017, University of Dayton
 *
 *  Licensed under the Educational Community License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *              http://opensource.org/licenses/ecl2
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.sakaiproject.attendance.logic;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import org.sakaiproject.attendance.api.AttendanceGradebookProvider;
import org.sakaiproject.attendance.dao.AttendanceDao;
import org.sakaiproject.attendance.model.AttendanceEvent;
import org.sakaiproject.attendance.model.AttendanceGrade;
import org.sakaiproject.attendance.model.AttendanceItemStats;
import org.sakaiproject.attendance.model.AttendanceRecord;
import org.sakaiproject.attendance.model.AttendanceSite;
import org.sakaiproject.attendance.model.AttendanceStatus;
import org.sakaiproject.attendance.model.AttendanceStats;
import org.sakaiproject.attendance.model.AttendanceUserStats;
import org.sakaiproject.attendance.model.GradingRule;
import org.sakaiproject.attendance.model.Status;
import org.sakaiproject.attendance.util.AttendanceConstants;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.user.api.User;

/**
 * Implementation of {@link AttendanceLogic}
 * 
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
@Setter
@Slf4j
@Transactional
public class AttendanceLogicImpl implements AttendanceLogic, EntityTransferrer {

	/**
	 * {@inheritDoc}
     */
	public AttendanceSite getAttendanceSite(String siteID) {
		AttendanceSite attendanceSite = dao.getAttendanceSite(siteID);

		if (attendanceSite == null) {
			attendanceSite = new AttendanceSite(siteID);

			// This will create the site and add statuses to the new site
			if (!addSite(attendanceSite)) {
				return null;
			}

			// We need to re-load the AttendanceSite because of the status creation above
			attendanceSite = dao.getAttendanceSite(siteID);
		}

		return attendanceSite;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean updateAttendanceSite(AttendanceSite aS) throws IllegalArgumentException {
		if(aS == null) {
			throw new IllegalArgumentException("AttendanceSite must not be null");
		}

		return dao.updateAttendanceSite(aS);
	}

	/**
	 * {@inheritDoc}
	 */
	public AttendanceSite getCurrentAttendanceSite() {
		String currentSiteID = sakaiProxy.getCurrentSiteId();
		return getAttendanceSite(currentSiteID);
	}

    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true)
    public AttendanceEvent getAttendanceEvent(long id) {
        return dao.getAttendanceEvent(id);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true)
    public List<AttendanceEvent> getAttendanceEventsForSite(AttendanceSite aS) {
        return safeAttendanceEventListReturn(dao.getAttendanceEventsForSite(aS));
    }

    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true)
    public List<AttendanceEvent> getAttendanceEventsForCurrentSite(){
        return getAttendanceEventsForSite(getCurrentAttendanceSite());
    }

	/**
	 * {@inheritDoc}
	 */
	public Serializable addAttendanceEventNow(AttendanceEvent e) {
		updateModifier(e);
		return dao.addAttendanceEventNow(e);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean updateAttendanceEvent(AttendanceEvent aE) throws IllegalArgumentException {
		if(aE == null) {
			throw new IllegalArgumentException("AttendanceEvent is null");
		}

		updateModifier(aE);
		for (AttendanceRecord record : aE.getRecords()) {
			if (record.getId() == null) {
				updateModifier(record);
			}
		}
		return dao.updateAttendanceEvent(aE);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean deleteAttendanceEvent(AttendanceEvent aE) throws IllegalArgumentException {
		if(aE == null) {
			throw new IllegalArgumentException("AttendanceEvent is null");
		}

		Set<AttendanceRecord> records = aE.getRecords();
		AttendanceSite site = aE.getAttendanceSite();
		for(AttendanceRecord record : records) {
			AttendanceUserStats userStats = dao.getAttendanceUserStats(record.getUserID(), site);
			if(userStats != null) {
				Status recordStatus = record.getStatus();
				removeStatusFromStats(userStats, recordStatus);

				dao.updateAttendanceUserStats(userStats);
			}
		}

		sakaiProxy.postEvent("attendance.event.delete", aE.getId() + "", true);
		return dao.deleteAttendanceEvent(aE);
	}

    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true)
    public AttendanceRecord getAttendanceRecord(Long id) {
        if(id == null) {
            return null;
        }

		return dao.getStatusRecord(id);
	}

    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true)
    public List<AttendanceRecord> getAttendanceRecordsForUser(String id) {
        return getAttendanceRecordsForUser(id, getCurrentAttendanceSite());
    }

    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true)
    public List<AttendanceRecord> getAttendanceRecordsForUser(String id, AttendanceSite aS) {
        return generateAttendanceRecords(id, aS);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true)
    public List<AttendanceStatus> getActiveStatusesForCurrentSite() {
        return getActiveStatusesForSite(getCurrentAttendanceSite());
    }

    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true)
    public List<AttendanceStatus> getActiveStatusesForSite(AttendanceSite attendanceSite) {
        return safeAttendanceStatusListReturn(dao.getActiveStatusesForSite(attendanceSite));
    }

    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true)
    public List<AttendanceStatus> getAllStatusesForSite(AttendanceSite attendanceSite) {
        return safeAttendanceStatusListReturn(dao.getAllStatusesForSite(attendanceSite));
    }

    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true)
    public AttendanceStatus getAttendanceStatusById(Long id) {
        return dao.getAttendanceStatusById(id);
    }

	/**
	 * {@inheritDoc}
	 */
	public boolean updateAttendanceRecord(AttendanceRecord aR, Status oldStatus) throws IllegalArgumentException {
		if(aR == null) {
			throw new IllegalArgumentException("AttendanceRecord cannot be null");
		}

		updateStats(aR, oldStatus);
		regradeForAttendanceRecord(aR);
		updateModifier(aR);
		return dao.updateAttendanceRecord(aR);
	}

	private void updateModifier(Object obj) {
		Instant now = Instant.now();
		if (obj instanceof AttendanceRecord) {
			AttendanceRecord aR = (AttendanceRecord) obj;
			aR.setLastModifiedBy(sakaiProxy.getCurrentUserId());
			aR.setLastModifiedDate(now);
		} else if (obj instanceof AttendanceEvent) {
			AttendanceEvent aE = (AttendanceEvent) obj;
			aE.setLastModifiedBy(sakaiProxy.getCurrentUserId());
			aE.setLastModifiedDate(now);
		} else if (obj instanceof AttendanceGrade) {
			AttendanceGrade aG = (AttendanceGrade) obj;
			aG.setLastModifiedBy(sakaiProxy.getCurrentUserId());
			aG.setLastModifiedDate(now);
		} else if (obj instanceof GradingRule) {
			GradingRule gR = (GradingRule) obj;
			gR.setLastModifiedBy(sakaiProxy.getCurrentUserId());
			gR.setLastModifiedDate(now);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List<AttendanceRecord> updateAttendanceRecordsForEvent(AttendanceEvent aE, Status s) {
		aE = getAttendanceEvent(aE.getId());
		List<AttendanceRecord> records = new ArrayList<>(aE.getRecords());

		if(records.isEmpty()) {
			return generateAttendanceRecords(aE, s);
		}

		Status oldStatus;
		for(AttendanceRecord aR : records) {
			oldStatus = aR.getStatus();
			aR.setStatus(s);
			updateStats(aR, oldStatus);
		}

		return records;
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateAttendanceRecordsForEvent(AttendanceEvent aE, Status s, String groupId) {
		aE = getAttendanceEvent(aE.getId());	//why are we getting this as a new event when it's a parameter passed in already?
		List<AttendanceRecord> allRecords = new ArrayList<>(aE.getRecords());
		List<AttendanceRecord> recordsToUpdate = new ArrayList<>();

		if(allRecords.isEmpty()) {
			allRecords = generateAttendanceRecords(aE, null);
		} else {
			List<String> ids = sakaiProxy.getCurrentSiteMembershipIds();
			allRecords.forEach(record -> ids.remove(record.getUserID()));
			allRecords.addAll(updateMissingRecordsForEvent(aE, null, ids));
		}

		if(groupId == null || groupId.isEmpty()) {
			recordsToUpdate.addAll(allRecords);
		} else {
			List<String> groupMemberIds = sakaiProxy.getGroupMembershipIds(aE.getAttendanceSite().getSiteID(), groupId);
			for(String userId : groupMemberIds) {
				for(AttendanceRecord record: allRecords) {
					if(record.getUserID().equals(userId)) {
						recordsToUpdate.add(record);
						break;	//once we've found the record, the loop can break.
					}
				}
			}
		}

		Status oldStatus;
		int present =0, unexcused =0, excused =0, late=0, leftEarly =0;
		for(AttendanceRecord aR : recordsToUpdate) {
			oldStatus = aR.getStatus();
			if (oldStatus == Status.PRESENT) {
				present++;
			} else if (oldStatus == Status.UNEXCUSED_ABSENCE) {
				unexcused++;
			} else if (oldStatus == Status.EXCUSED_ABSENCE) {
				excused++;
			} else if (oldStatus == Status.LATE) {
				late++;
			} else if (oldStatus == Status.LEFT_EARLY ) {
				leftEarly++;
			}
			aR.setStatus(s);
			updateUserStats(aR, oldStatus);
			regradeForAttendanceRecord(aR);
			updateModifier(aR);
			dao.updateAttendanceRecord(aR);	//change the actual data for the student we've just iterated past. This saves looping through it again as part of dao.updateAttendanceRecords on a whole array.
		}

		AttendanceItemStats itemStats = getStatsForEvent(aE);
		itemStats.setPresent(itemStats.getPresent() - present);
		itemStats.setUnexcused(itemStats.getUnexcused() - unexcused);
		itemStats.setExcused(itemStats.getExcused() - excused);
		itemStats.setLate(itemStats.getLate() - late);
		itemStats.setLeftEarly(itemStats.getLeftEarly() - leftEarly);

		if (s == Status.PRESENT) {
			itemStats.setPresent(itemStats.getPresent()+ recordsToUpdate.size());
		} else if (s == Status.UNEXCUSED_ABSENCE) {
			itemStats.setUnexcused(itemStats.getUnexcused() + recordsToUpdate.size());
		} else if (s == Status.EXCUSED_ABSENCE) {
			itemStats.setExcused(itemStats.getExcused() + recordsToUpdate.size());
		} else if (s == Status.LATE) {
			itemStats.setLate(itemStats.getLate() + recordsToUpdate.size());
		} else if (s == Status.LEFT_EARLY) {
			itemStats.setLeftEarly(itemStats.getLeftEarly() + recordsToUpdate.size());
		}

		dao.updateAttendanceItemStats(itemStats);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<AttendanceRecord> updateMissingRecordsForEvent(AttendanceEvent attendanceEvent, Status defaultStatus, List<String> missingStudentIds)
	throws IllegalArgumentException {
		if(attendanceEvent == null) {
			throw new IllegalArgumentException("attendanceEvent cannot be null.");
		}

		List<AttendanceRecord> recordList = new ArrayList<>();

		if(defaultStatus == null) {
			defaultStatus = attendanceEvent.getAttendanceSite().getDefaultStatus();
		}

		if(missingStudentIds != null && !missingStudentIds.isEmpty()) {
			for(String studentId : missingStudentIds) {
				AttendanceRecord attendanceRecord = generateAttendanceRecord(attendanceEvent, studentId, defaultStatus);
				recordList.add(attendanceRecord);
			}
		}

		return recordList;
	}

    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true)
    public AttendanceItemStats getStatsForEvent(AttendanceEvent event) {
        AttendanceItemStats itemStats = dao.getAttendanceItemStats(event);

		if(itemStats == null) {
			itemStats = new AttendanceItemStats(event);
		}
		event.setStats(itemStats);

		return itemStats;
	}

    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true)
    public AttendanceUserStats getStatsForUser(String userId) {
        return getStatsForUser(userId, getCurrentAttendanceSite());
    }

    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true)
    public AttendanceUserStats getStatsForUser(String userId, AttendanceSite aS) {
        AttendanceUserStats userStats = dao.getAttendanceUserStats(userId, aS);

		if(userStats == null) {
			userStats = new AttendanceUserStats(userId, aS);
		}

		return userStats;
	}

    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true)
    public List<AttendanceUserStats> getUserStatsForCurrentSite(String group) {
        return getUserStatsForSite(getCurrentAttendanceSite(), group);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true)
    public List<AttendanceUserStats> getUserStatsForSite(AttendanceSite aS, String group) {
        List<AttendanceUserStats> userStatsList = dao.getAttendanceUserStatsForSite(aS);
		List<String> users;
		if(group == null || group.isEmpty()) {
			users = sakaiProxy.getCurrentSiteMembershipIds();
		} else {
			users = sakaiProxy.getGroupMembershipIds(aS.getSiteID(), group);
		}
		List<String> missingUsers = new ArrayList<>();
		List<AttendanceUserStats> returnList = new ArrayList<>(users.size());

		if(userStatsList != null && !userStatsList.isEmpty()) {
			users.forEach(user -> {
				boolean found = false;
				for(AttendanceUserStats userStat : userStatsList) {
					if(user.equals(userStat.getUserID())) {
						found = true;
						returnList.add(userStat);
					}
				}
				if(!found) {
					missingUsers.add(user);
				}
			});
		} else {
			missingUsers.addAll(users);
		}

		if (!missingUsers.isEmpty()) {
			missingUsers.forEach(user -> returnList.add(new AttendanceUserStats(user, aS)));
		}

		return returnList;
	}

    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true)
    public AttendanceGrade getAttendanceGrade(Long id) throws IllegalArgumentException {
        if(id == null) {
            throw new IllegalArgumentException("ID must not be null");
        }

		return dao.getAttendanceGrade(id);
	}

    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true)
    public AttendanceGrade getAttendanceGrade(String uID) throws IllegalArgumentException {
        if(uID == null || uID.isEmpty()) {
            throw new IllegalArgumentException("uID must not be null or empty.");
        }

		final AttendanceSite currentSite = getCurrentAttendanceSite();
		final AttendanceGrade grade = dao.getAttendanceGrade(uID, currentSite);

		if(grade == null) {
			return new AttendanceGrade(currentSite, uID);
		}

		return grade;
	}

    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true)
    public Map<String, AttendanceGrade> getAttendanceGrades() {
        Map<String, AttendanceGrade> aGHashMap = new HashMap<>();
		AttendanceSite aS = getCurrentAttendanceSite();
		List<AttendanceGrade> aGs = dao.getAttendanceGrades(aS);
		if(aGs == null || aGs.isEmpty()) {
			aGs = generateAttendanceGrades(aS);
		} else {
			List<String> userList = sakaiProxy.getCurrentSiteMembershipIds();
			for(AttendanceGrade aG : aGs) {
				userList.remove(aG.getUserID());
			}

			if(!userList.isEmpty()) {
				for(String u : userList) {
					aGs.add(generateAttendanceGrade(u, aS));
				}
			}
		}

		for(AttendanceGrade aG : aGs) {
			aGHashMap.put(aG.getUserID(), aG);
		}

		return aGHashMap;
	}

    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true)
    public Map<String, String> getAttendanceGradeScores() {
        Map<String, AttendanceGrade> gradeMap = getAttendanceGrades();

		Map<String, String> returnMap = new HashMap<>(gradeMap.size());

		for(Map.Entry<String, AttendanceGrade> entry : gradeMap.entrySet())
		{
			Double doubleValue = entry.getValue().getGrade();
			String value = doubleValue == null ? null : doubleValue.toString();
			returnMap.put(entry.getKey(), value);
		}

		return returnMap;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean updateAttendanceGrade(AttendanceGrade aG) throws IllegalArgumentException {
		if(aG == null) {
			throw new IllegalArgumentException("AttendanceGrade cannot be null");
		}

		updateModifier(aG);
		boolean saved = dao.updateAttendanceGrade(aG);

		if(saved && aG.getAttendanceSite().getSendToGradebook()) {
			return attendanceGradebookProvider.sendToGradebook(aG);
		}

		return saved;
	}

    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true)
    public int getStatsForStatus(AttendanceStats stats, Status status) {
        int stat = 0;

		if (status == Status.PRESENT) {
			stat = stats.getPresent();
		} else if (status == Status.UNEXCUSED_ABSENCE) {
			stat = stats.getUnexcused();
		} else if (status == Status.EXCUSED_ABSENCE) {
			stat = stats.getExcused();
		} else if (status == Status.LATE) {
			stat = stats.getLate();
		} else if (status == Status.LEFT_EARLY) {
			stat = stats.getLeftEarly();
		}

		return stat;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean addGradingRule(GradingRule gradingRule) {
		if (gradingRule == null) return false;
		updateModifier(gradingRule);
		return dao.addGradingRule(gradingRule);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean deleteGradingRule(GradingRule gradingRule) {
		if (gradingRule == null) return false;
		sakaiProxy.postEvent("attendance.rule.delete", gradingRule.getId() + "", true);
		return dao.deleteGradingRule(gradingRule);
	}

    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true)
    public List<GradingRule> getGradingRulesForSite(AttendanceSite attendanceSite) {
        return dao.getGradingRulesForSite(attendanceSite);
    }

	/**
	 * {@inheritDoc}
	 */
	public void regradeAll(AttendanceSite attendanceSite) {
		List<String> users = sakaiProxy.getSiteMembershipIds(attendanceSite.getSiteID());
		if (users != null) {
			for (String userId : users) {
				AttendanceGrade grade = getAttendanceGrade(userId);
				if (grade.getOverride() == null || !grade.getOverride()) {
					grade.setGrade(grade(userId, attendanceSite));
					updateAttendanceGrade(grade);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Double regrade(AttendanceGrade attendanceGrade, boolean saveGrade) {
		Double grade = grade(attendanceGrade.getUserID(), attendanceGrade.getAttendanceSite());
		if (saveGrade) {
			attendanceGrade.setGrade(grade);
			updateAttendanceGrade(attendanceGrade);
		}
		return grade;
	}

	/**
	 * init - perform any actions required here for when this bean starts up
	 */
	public void init() {
		log.debug("AttendanceLogicImpl init()");

		entityManager.registerEntityProducer(this, Entity.SEPARATOR + "attendance");
	}

	private boolean addSite(AttendanceSite s) {
		if (dao.addAttendanceSite(s)) {
			generateMissingAttendanceStatusesForSite(s);
			return true;
		}

		return false;
	}

	private List<AttendanceGrade> generateAttendanceGrades(AttendanceSite aS) {
		List<String> userList = sakaiProxy.getCurrentSiteMembershipIds();
		List<AttendanceGrade> aGList = new ArrayList<>(userList.size());

		for(String u : userList){
			aGList.add(generateAttendanceGrade(u, aS));
		}

		return aGList;
	}

	private AttendanceGrade generateAttendanceGrade(String userId, AttendanceSite aS) {
		if(aS == null) {
			aS = getCurrentAttendanceSite();
		}

		return new AttendanceGrade(aS, userId);
	}

	private List<AttendanceRecord> generateAttendanceRecords(String id, AttendanceSite aS) {
		List<AttendanceEvent> aEs = getAttendanceEventsForSite(aS);
		List<AttendanceRecord> records = new ArrayList<>(aEs.size());
		Status s = getCurrentAttendanceSite().getDefaultStatus();
		// Is there a faster way to do this? Would querying the DB be faster?
		for(AttendanceEvent e : aEs) {
			boolean recordPresent = false;
			Set<AttendanceRecord> eRecords = e.getRecords();

			if(!eRecords.isEmpty()) {
				for (AttendanceRecord r : eRecords) {
					if (r.getUserID().equals(id)) {
						recordPresent = true;
						records.add(r);
					}
				}
			}

			if(!recordPresent) {
				records.add(generateAttendanceRecord(e, id, s));
			}
		}

		return records;
	}

	private List<AttendanceRecord> generateAttendanceRecords(AttendanceEvent aE, Status s) {
		if(s == null) {
			s = aE.getAttendanceSite().getDefaultStatus();
		}

		List<AttendanceRecord> recordList = new ArrayList<>();
		List<User> userList = sakaiProxy.getCurrentSiteMembership();

		if(userList.isEmpty()){
			return recordList;
		}

		for(User user : userList) {
			AttendanceRecord attendanceRecord = generateAttendanceRecord(aE, user.getId(), s);
			recordList.add(attendanceRecord);
		}

		return recordList;
	}

	private AttendanceRecord generateAttendanceRecord(AttendanceEvent aE, String id, Status s) {
		if(s == null) {
			s = aE.getAttendanceSite().getDefaultStatus();
		}

		return new AttendanceRecord(aE, id, s);
	}

	private void generateMissingAttendanceStatusesForSite(AttendanceSite attendanceSite) {
		Set<AttendanceStatus> currentAttendanceStatuses = attendanceSite.getAttendanceStatuses();
		List<Status> previouslyCreatedStatuses = new ArrayList<>();
		List<AttendanceStatus> statusesToBeAdded = new ArrayList<>();

		// Get the max sort order
		int nextSortOrder = getNextSortOrder(new ArrayList<>(currentAttendanceStatuses));

		// Generate the list of statuses that already have a record for the site
		for (AttendanceStatus attendanceStatus : currentAttendanceStatuses) {
			previouslyCreatedStatuses.add(attendanceStatus.getStatus());
		}

		// Add attendance status record for each Status that is missing a record
		for (Status status : Status.values()) {
			if (!previouslyCreatedStatuses.contains(status)) {
				AttendanceStatus missingAttendanceStatus = new AttendanceStatus();
				missingAttendanceStatus.setAttendanceSite(attendanceSite);
				missingAttendanceStatus.setStatus(status);
				missingAttendanceStatus.setIsActive(true);
				missingAttendanceStatus.setSortOrder(nextSortOrder);
				nextSortOrder++;
				statusesToBeAdded.add(missingAttendanceStatus);
			}
		}

		dao.updateAttendanceStatuses(statusesToBeAdded);
	}

	private int getNextSortOrder(List<AttendanceStatus> attendanceStatusList) {
		int maxSortOrder = -1;

		for(AttendanceStatus attendanceStatus : attendanceStatusList) {
			if(attendanceStatus.getSortOrder() > maxSortOrder) {
				maxSortOrder = attendanceStatus.getSortOrder();
			}
		}

		return maxSortOrder + 1;
	}

	private List<AttendanceEvent> safeAttendanceEventListReturn(List<AttendanceEvent> l) {
		if(l == null) {
			return new ArrayList<>();
		}

		return l;
	}

	private List<AttendanceStatus> safeAttendanceStatusListReturn(List<AttendanceStatus> l) {
		if(l == null) {
			return new ArrayList<>();
		}

		return l;
	}

	private void updateStats(AttendanceRecord aR, Status oldStatus) {
		if(aR.getStatus() == oldStatus) {
			return;
		}

		updateUserStats(aR, oldStatus);

		AttendanceEvent aE = aR.getAttendanceEvent();
		AttendanceItemStats itemStats = getStatsForEvent(aE);
		updateStats(null, itemStats, oldStatus, aR.getStatus());
	}
	
	private boolean updateUserStats(AttendanceRecord record, Status oldStatus) {
		AttendanceUserStats userStats = dao.getAttendanceUserStats(record.getUserID(), record.getAttendanceEvent().getAttendanceSite());
		if(userStats == null) { // assume null userStats means stats haven't been calculated yet
			userStats = new AttendanceUserStats(record.getUserID(), record.getAttendanceEvent().getAttendanceSite());
		}
		return updateStats(userStats, null, oldStatus, record.getStatus());
	}

	private boolean updateStats(AttendanceUserStats userStats, AttendanceItemStats itemStats, Status oldStatus, Status newStatus) {
		AttendanceStats stats;
		if(userStats != null) {
			stats = userStats;
		} else {
			stats = itemStats;
		}

		if(oldStatus != newStatus) {
			removeStatusFromStats(stats, oldStatus);

			if (newStatus == Status.PRESENT) {
				stats.setPresent(stats.getPresent() + 1);
			} else if (newStatus == Status.UNEXCUSED_ABSENCE) {
				stats.setUnexcused(stats.getUnexcused() + 1);
			} else if (newStatus == Status.EXCUSED_ABSENCE) {
				stats.setExcused(stats.getExcused() + 1);
			} else if (newStatus == Status.LATE) {
				stats.setLate(stats.getLate() + 1);
			} else if (newStatus == Status.LEFT_EARLY) {
				stats.setLeftEarly(stats.getLeftEarly() + 1);
			}
		}

		boolean returnVariable;
		if(userStats != null) {
			returnVariable = dao.updateAttendanceUserStats((AttendanceUserStats) stats);
		} else {
			returnVariable = dao.updateAttendanceItemStats((AttendanceItemStats) stats);
		}

		return returnVariable;
	}

	private void removeStatusFromStats(AttendanceStats stats, Status status) {
		if (status != Status.UNKNOWN) {
			if (status == Status.PRESENT) {
				stats.setPresent(stats.getPresent() - 1);
			} else if (status == Status.UNEXCUSED_ABSENCE) {
				stats.setUnexcused(stats.getUnexcused() - 1);
			} else if (status == Status.EXCUSED_ABSENCE) {
				stats.setExcused(stats.getExcused() - 1);
			} else if (status == Status.LATE) {
				stats.setLate(stats.getLate() - 1);
			} else if (status == Status.LEFT_EARLY) {
				stats.setLeftEarly(stats.getLeftEarly() - 1);
			}
		}
	}

	private void regradeForAttendanceRecord(AttendanceRecord attendanceRecord) {
		final AttendanceSite currentSite = getCurrentAttendanceSite();
		// Auto grade if valid record, maximum points is set, and auto grade is enabled
		if (attendanceRecord != null && currentSite.getMaximumGrade() != null && currentSite.getMaximumGrade() > 0 && currentSite.getUseAutoGrading()) {
			final String userId = attendanceRecord.getUserID();
			AttendanceGrade attendanceGrade = getAttendanceGrade(userId);
			// Only update if not overridden
			if (attendanceGrade.getOverride()== null || !attendanceGrade.getOverride()) {
				attendanceGrade.setGrade(grade(userId, currentSite));
				updateAttendanceGrade(attendanceGrade);
			}
		}
	}

	// Must be non-null user and site
	private Double grade(String userId, AttendanceSite attendanceSite) {
		final List<GradingRule> rules = getGradingRulesForSite(attendanceSite);
		final AttendanceUserStats userStats = getStatsForUser(userId);

		double totalPoints = 0D;
		Double maximumGrade = attendanceSite.getMaximumGrade();
		if (maximumGrade != null && attendanceSite.getGradingMethod() == AttendanceConstants.GRADING_METHOD_SUBTRACT) {
			totalPoints = maximumGrade;
		}

		if (rules != null) {
			for (GradingRule rule : rules) {
				Integer statusTotal = null;
				switch (rule.getStatus()) {
					case PRESENT:
						statusTotal = userStats.getPresent();
						break;
					case UNEXCUSED_ABSENCE:
						statusTotal = userStats.getUnexcused();
						break;
					case EXCUSED_ABSENCE:
						statusTotal = userStats.getExcused();
						break;
					case LATE:
						statusTotal = userStats.getLate();
						break;
					case LEFT_EARLY:
						statusTotal = userStats.getLeftEarly();
						break;
				}

				if (statusTotal == null) {
					log.debug("No status total for rule: {}", rule);
				}
				else if (attendanceSite.getGradingMethod() == AttendanceConstants.GRADING_METHOD_MULTIPLY) {
					// Multiply the number of occurrences of the status by the rule's points and then add to the total points (loop of mutiple rules)
					double newPoints = Math.round(statusTotal * rule.getPoints() * 100.0) / 100.0; // Avoid floating point errors
					totalPoints = Double.sum(totalPoints, newPoints);
					log.debug("Multiply Rule: {} Status: {} Total: {} Points: {} New Points: {} Total Points: {}", rule, rule.getStatus(), statusTotal, rule.getPoints(), newPoints, totalPoints);
				}
				else if (statusTotal >= rule.getStartRange() && (rule.getEndRange() == null || statusTotal <= rule.getEndRange())) {
					totalPoints = Double.sum(totalPoints, rule.getPoints());
					log.debug("Add Rule: {} Status: {} Total: {} Points: {} Total Points: {}", rule, rule.getStatus(), statusTotal, rule.getPoints(), totalPoints);
				}
			}
		}

		// Don't allow negative total points
        return Math.max(totalPoints, 0D);
	}

	@Override
	public Map<String, String> transferCopyEntities(String fromContext, String toContext, List<String> ids, List<String> transferOptions) {
		return transferCopyEntities(fromContext, toContext, ids, transferOptions, false);
	}

	@Override
	public String[] myToolIds() {
		return new String[]{ "sakai.attendance" };
	}

	@Override
	public Map<String, String> transferCopyEntities(String fromContext, String toContext, List<String> ids, List<String> transferOptions, boolean cleanup) {
		Map<String, String> transversalMap = new HashMap<>();
		AttendanceSite fromSite = getAttendanceSite(fromContext);
		AttendanceSite toSite = getAttendanceSite(toContext);

		if (cleanup) {
			// TODO: implement deleting all content in toContext
			// Maybe wait until soft delete is confirmed everywhere
		}

		try {
			// TODO: consider bringing over the statuses from the original site

			List<AttendanceEvent> fromEvents = getAttendanceEventsForSite(fromSite);
			for (AttendanceEvent fromEvent : fromEvents) {
				AttendanceEvent toEvent = new AttendanceEvent();
				toEvent.setAttendanceSite(toSite);
				toEvent.setName(fromEvent.getName());
				toEvent.setIsReoccurring(fromEvent.getIsReoccurring());
				toEvent.setIsRequired(fromEvent.getIsRequired());

				if (fromEvent.getStartDateTime() != null) {
					toEvent.setStartDateTime(fromEvent.getStartDateTime());
				}
				if (fromEvent.getEndDateTime() != null) {
					toEvent.setEndDateTime(fromEvent.getEndDateTime());
				}

				addAttendanceEventNow(toEvent);
				log.info("transferCopyEntities: new attendance event ({})", toEvent.getName());

				transversalMap.put("attendance/" + fromEvent.getId(), "attendance/" + toEvent.getId());
			}
		} catch (Exception e) {
			log.error("transferCopyEntities error", e);
		}

		return transversalMap;
	}

    private AttendanceDao dao;

	private SakaiProxy sakaiProxy;

	private AttendanceGradebookProvider attendanceGradebookProvider;

	private EntityManager entityManager;
}
