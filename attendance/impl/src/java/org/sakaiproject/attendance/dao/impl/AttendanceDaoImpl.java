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

package org.sakaiproject.attendance.dao.impl;

import org.hibernate.HibernateException;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.sakaiproject.attendance.dao.AttendanceDao;
import org.sakaiproject.attendance.model.AttendanceEvent;
import org.sakaiproject.attendance.model.AttendanceGrade;
import org.sakaiproject.attendance.model.AttendanceItemStats;
import org.sakaiproject.attendance.model.AttendanceRecord;
import org.sakaiproject.attendance.model.AttendanceSite;
import org.sakaiproject.attendance.model.AttendanceStatus;
import org.sakaiproject.attendance.model.AttendanceUserStats;
import org.sakaiproject.attendance.model.GradingRule;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

/**
 * Implementation of AttendanceDao
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
@Slf4j
public class AttendanceDaoImpl extends HibernateDaoSupport implements AttendanceDao {

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public AttendanceSite getAttendanceSite(final String siteID) {
		log.debug("getSiteBySite_ID ");

		HibernateCallback hcb = new HibernateCallback() {
			@Override
			public Object doInHibernate(Session session) throws HibernateException {
				CriteriaBuilder cb = session.getCriteriaBuilder();
				CriteriaQuery<AttendanceSite> cq = cb.createQuery(AttendanceSite.class);
				Root<AttendanceSite> root = cq.from(AttendanceSite.class);
				cq.select(root).where(cb.equal(root.get("siteID"), siteID));
				return session.createQuery(cq).uniqueResult();
			}
		};

		return (AttendanceSite) getHibernateTemplate().execute(hcb);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public AttendanceSite getAttendanceSite(final Long id) {
		log.debug("getAttendanceSite by ID: {}", id);

		try {
			return getHibernateTemplate().execute(session -> session.get(AttendanceSite.class, id));
		} catch (DataAccessException e) {
			log.error("getAttendanceSite by ID failed", e);
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public boolean addAttendanceSite(AttendanceSite aS) {
		log.debug("addAttendanceSite ( {})", aS.toString());

		try {
			getHibernateTemplate().save(aS);
			return true;
		} catch (DataAccessException de) {
			log.error("addAttendanceSite failed", de);
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
    public boolean updateAttendanceSite(AttendanceSite aS) {
        try{
            getHibernateTemplate().merge(aS);
            return true;
        } catch (DataAccessException e) {
            log.error("updateAttendanceSite aS '" + aS.getSiteID() + "' failed.", e);
            return false;
        }
    }

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public AttendanceEvent getAttendanceEvent(final long id) {
		log.debug("getAttendanceEvent(){}", id);

		try {
			return getHibernateTemplate().execute(session -> session.get(AttendanceEvent.class, id));
		} catch (DataAccessException e) {
			log.error("getAttendanceEvent failed", e);
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<AttendanceEvent> getAttendanceEventsForSite(final AttendanceSite aS) {
		log.debug("getAttendanceEventsForSite(AttendanceSite id)");

		return getEventsForAttendanceSiteHelper(aS);
	}

	/**
	 * {@inheritDoc}
	 */
	public Serializable addAttendanceEventNow(AttendanceEvent attendanceEvent) {
		log.debug("addAttendanceEventNow( {})", attendanceEvent.toString());
		requireAuditFields(attendanceEvent);

		try{
			return getHibernateTemplate().save(attendanceEvent);
		} catch (DataAccessException de) {
			log.error("addAttendanceEventNow failed.", de);
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
    public boolean updateAttendanceEvent(AttendanceEvent aE) {
        log.debug("updateAttendanceEvent aE: {}", aE.getName());
		requireAuditFields(aE);

        try{
            getHibernateTemplate().merge(aE);
            return true;
        } catch (DataAccessException e){
            log.error("updateAttendanceEvent failed.", e);
            return false;
        }
    }

	/**
	 * {@inheritDoc}
	 */
	public boolean deleteAttendanceEvent(AttendanceEvent aE) {
		log.debug("deleteAttendanceEvent aE: {}", aE.getName());

		if(aE.getStats() !=null && aE.getStats().getId() == null){
			aE.setStats(null);
		}

		try {
			getHibernateTemplate().delete(getHibernateTemplate().merge(aE));
			return true;
		} catch (DataAccessException e) {
    		log.error("deleteAttendanceEvent, {}, failed.", aE.getId(), e);
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public AttendanceRecord getStatusRecord(final long id) {
		log.debug("getAttendanceRecord(){}", id);

		try {
			return getHibernateTemplate().execute(session -> session.get(AttendanceRecord.class, id));
		} catch (DataAccessException e) {
			log.error("getAttendanceRecord by id failed", e);
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
    public boolean updateAttendanceRecord(AttendanceRecord aR) {
		requireAuditFields("AttendanceRecord", aR.getLastModifiedBy(), aR.getLastModifiedDate());

        try {
            // Guard against duplicates on unique key (userID + attendanceEvent)
            if (aR.getId() == null && aR.getAttendanceEvent() != null && aR.getUserID() != null) {
                AttendanceRecord existing = (AttendanceRecord) getHibernateTemplate().execute(session -> {
                    CriteriaBuilder cb = session.getCriteriaBuilder();
                    CriteriaQuery<AttendanceRecord> cq = cb.createQuery(AttendanceRecord.class);
                    Root<AttendanceRecord> root = cq.from(AttendanceRecord.class);
                    cq.select(root).where(
                        cb.equal(root.get("attendanceEvent"), aR.getAttendanceEvent()),
                        cb.equal(root.get("userID"), aR.getUserID())
                    );
                    return session.createQuery(cq).uniqueResult();
                });
                if (existing != null) {
                    aR.setId(existing.getId());
                }
            }
            getHibernateTemplate().merge(aR);
            return true;
        } catch (Exception e) {
            log.error("update attendanceRecord failed.", e);
            return false;
        }
    }

	/**
	 * {@inheritDoc}
     */
    public void updateAttendanceStatuses(List<AttendanceStatus> attendanceStatusList) {
        for(AttendanceStatus aS : attendanceStatusList) {
            try {
                getHibernateTemplate().merge(aS);
                log.debug("AttendanceStatus saved, id: " + aS.getId());
            } catch (Exception e) {
                log.error("update attendanceStatuses failed.", e);
            }
        }
    }

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<AttendanceStatus> getActiveStatusesForSite(final AttendanceSite attendanceSite) {
		log.debug("getActiveStatusesForSite(AttendanceSite {} )", attendanceSite.getSiteID());

		try {
			return getHibernateTemplate().execute(session -> {
				CriteriaBuilder cb = session.getCriteriaBuilder();
				CriteriaQuery<AttendanceStatus> cq = cb.createQuery(AttendanceStatus.class);
				Root<AttendanceStatus> root = cq.from(AttendanceStatus.class);
				root.fetch("attendanceSite", JoinType.INNER);
				cq.select(root).where(
					cb.equal(root.get("attendanceSite"), attendanceSite),
					cb.isTrue(root.get("isActive"))
				);
				return session.createQuery(cq).getResultList();
			});
		} catch (DataAccessException e) {
			log.error("getActiveStatusesForSite failed", e);
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<AttendanceStatus> getAllStatusesForSite(final AttendanceSite attendanceSite) {
		log.debug("getAllStatusesForSite(AttendanceSite attendanceSite)");

		try {
			return getHibernateTemplate().execute(session -> {
				CriteriaBuilder cb = session.getCriteriaBuilder();
				CriteriaQuery<AttendanceStatus> cq = cb.createQuery(AttendanceStatus.class);
				Root<AttendanceStatus> root = cq.from(AttendanceStatus.class);
				root.fetch("attendanceSite", JoinType.INNER);
				cq.select(root).where(cb.equal(root.get("attendanceSite"), attendanceSite));
				return session.createQuery(cq).getResultList();
			});
		} catch (DataAccessException e) {
			log.error("getAllStatusesForSite failed", e);
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public AttendanceStatus getAttendanceStatusById(final Long id) {
		log.debug("getAttendanceStatus(){}", id);

		try {
			return getHibernateTemplate().execute(session -> session.get(AttendanceStatus.class, id));
		} catch (DataAccessException e) {
			log.error("getAttendanceStatus by id failed", e);
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public AttendanceGrade getAttendanceGrade(final Long id) {
        log.debug("getAttendanceGrade, id: {}", id.toString());

		try {
			return getHibernateTemplate().execute(session -> session.get(AttendanceGrade.class, id));
		} catch (DataAccessException e) {
			log.error("getAttendanceGrade by id failed", e);
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public AttendanceGrade getAttendanceGrade(final String userID, final AttendanceSite aS) {
		log.debug("getAttendanceGrades for user {} in site {}", userID, aS.getSiteID());

		try{
			return (AttendanceGrade) getHibernateTemplate().execute(session -> {
				CriteriaBuilder cb = session.getCriteriaBuilder();
				CriteriaQuery<AttendanceGrade> cq = cb.createQuery(AttendanceGrade.class);
				Root<AttendanceGrade> root = cq.from(AttendanceGrade.class);
				root.fetch("attendanceSite", JoinType.INNER);
				cq.select(root).where(
					cb.equal(root.get("attendanceSite"), aS),
					cb.equal(root.get("userID"), userID)
				);
				return session.createQuery(cq).uniqueResult();
			});
		} catch (DataAccessException e) {
            log.error("Failed to get AttendanceGrade for {} in {}", userID, aS.getSiteID());
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<AttendanceGrade> getAttendanceGrades(final AttendanceSite aS) {
		log.debug("getAttendanceGrades for: {}", aS.getSiteID());

		try{
			return getHibernateTemplate().execute(session -> {
				CriteriaBuilder cb = session.getCriteriaBuilder();
				CriteriaQuery<AttendanceGrade> cq = cb.createQuery(AttendanceGrade.class);
				Root<AttendanceGrade> root = cq.from(AttendanceGrade.class);
				root.fetch("attendanceSite", JoinType.INNER);
				cq.select(root).where(cb.equal(root.get("attendanceSite"), aS));
				return session.createQuery(cq).getResultList();
			});
		} catch (DataAccessException e) {
            log.error("DataAccessException getting AttendanceGrades for {}. E:", aS.getSiteID(), e);
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
    public boolean updateAttendanceGrade(AttendanceGrade aG) {
        log.debug("updateAttendanceGrade for User '{}' grade {} for site  {}", aG.getUserID(), aG.getGrade(), aG.getAttendanceSite().getSiteID());
		requireAuditFields("AttendanceGrade", aG.getLastModifiedBy(), aG.getLastModifiedDate());

        try {
            getHibernateTemplate().merge(aG);
            return true;
        } catch (DataAccessException de) {
            log.error("updateAttendanceGrade failed.", de);
            return false;
        }
    }

	/**
	 * {@inheritDoc}
	 */
	public AttendanceUserStats getAttendanceUserStats(final String userId, final AttendanceSite aS) {
		log.debug("getAttendanceUserStats for User '{}' and Site: '{}'.", userId, aS.getSiteID());

		try{
			return (AttendanceUserStats) getHibernateTemplate().execute(session -> {
				CriteriaBuilder cb = session.getCriteriaBuilder();
				CriteriaQuery<AttendanceUserStats> cq = cb.createQuery(AttendanceUserStats.class);
				Root<AttendanceUserStats> root = cq.from(AttendanceUserStats.class);
				root.fetch("attendanceSite", JoinType.INNER);
				cq.select(root).where(
					cb.equal(root.get("attendanceSite"), aS),
					cb.equal(root.get("userID"), userId)
				);
				return session.createQuery(cq).uniqueResult();
			});
		} catch (DataAccessException e) {
    		log.error("DataAccessException getting AttendanceUserStats for User '{}' and Site: '{}'.", userId, aS.getSiteID(), e);
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<AttendanceUserStats> getAttendanceUserStatsForSite(final AttendanceSite aS) {
		log.debug("getAttendanceUserStatsForSite for site: {}", aS.getSiteID());

		try{
			return getHibernateTemplate().execute(session -> {
				CriteriaBuilder cb = session.getCriteriaBuilder();
				CriteriaQuery<AttendanceUserStats> cq = cb.createQuery(AttendanceUserStats.class);
				Root<AttendanceUserStats> root = cq.from(AttendanceUserStats.class);
				root.fetch("attendanceSite", JoinType.INNER);
				cq.select(root).where(cb.equal(root.get("attendanceSite"), aS));
				return session.createQuery(cq).getResultList();
			});
		} catch (DataAccessException e) {
			log.error("DataAccessException getting AttendanceUserStats for Site: " + aS.getSiteID() + ".", e);
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
    public boolean updateAttendanceUserStats(AttendanceUserStats aUS) {
        log.debug("updateAttendanceUserStats for User '{}' and Site: '{}'.", aUS.getUserID(), aUS.getAttendanceSite().getSiteID());

        try {
            getHibernateTemplate().merge(aUS);
            return true;
        } catch (DataAccessException e) {
            log.error("updateAttendanceUserStats, id: '{}' failed.", aUS.getId(), e);
            return false;
        }
    }

	/**
	 * {@inheritDoc}
	 */
	public boolean addGradingRule(GradingRule gradingRule) {
			log.debug("add grading rule to site {} status: {} range: {} - {} points: {}", gradingRule.getAttendanceSite().getSiteID(), gradingRule.getStatus(), gradingRule.getStartRange(), gradingRule.getEndRange(), gradingRule.getPoints());
		requireAuditFields("GradingRule", gradingRule.getLastModifiedBy(), gradingRule.getLastModifiedDate());

		try {
			getHibernateTemplate().save(gradingRule);
			return true;
		} catch (DataAccessException dae) {
			log.error("addGradingRule failed.", dae);
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean deleteGradingRule(GradingRule gradingRule) {
			log.debug("Delete grading rule from site {} grading rule: {}", gradingRule.getAttendanceSite().getSiteID(), gradingRule.getId());

		try {
			getHibernateTemplate().delete(getHibernateTemplate().merge(gradingRule));
			return true;
		} catch (DataAccessException e) {
			log.error("deleteGradingRule, {}, failed.", gradingRule.getId(), e);
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
public AttendanceItemStats getAttendanceItemStats(AttendanceEvent aE) {
			log.debug("getAttendanceUserStats for Event '{}' and Site: '{}'.", aE.getName(), aE.getAttendanceSite().getSiteID());

		try{
			return (AttendanceItemStats) getHibernateTemplate().execute(session -> {
				CriteriaBuilder cb = session.getCriteriaBuilder();
				CriteriaQuery<AttendanceItemStats> cq = cb.createQuery(AttendanceItemStats.class);
				Root<AttendanceItemStats> root = cq.from(AttendanceItemStats.class);
				cq.select(root).where(cb.equal(root.get("attendanceEvent"), aE));
				return session.createQuery(cq).uniqueResult();
			});
		} catch (DataAccessException e) {
			log.error("DataAccessException getting AttendanceItemStats for Event '{}' and Site: '{}'.", aE.getName(), aE.getAttendanceSite().getSiteID(), e);
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
    public boolean updateAttendanceItemStats(AttendanceItemStats aIS) {
            log.debug("updateAttendanceItemStats, '{}', for Event '{}' and site: '{}'.", aIS.getId(), aIS.getAttendanceEvent().getName(), aIS.getAttendanceEvent().getAttendanceSite().getSiteID());

        try {
            getHibernateTemplate().execute(session -> {
                AttendanceEvent event = aIS.getAttendanceEvent();
                if (event != null && event.getId() != null && !session.contains(event)) {
                    session.buildLockRequest(LockOptions.NONE).lock(event);
                }

                // For shared PK one-to-one (foreign id) mapping, use saveOrUpdate to
                // let Hibernate assign id from the associated AttendanceEvent.
                session.saveOrUpdate(aIS);
                return null;
            });
            return true;
        } catch (DataAccessException e) {
            log.error("updateAttendanceItemStats, '" + aIS.getId() + "' failed.", e);
            return false;
        }
    }

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<GradingRule> getGradingRulesForSite(AttendanceSite attendanceSite) {
			log.debug("getGradingRulesForSite(AttendanceSite {} )", attendanceSite.getSiteID());

		try {
			return getHibernateTemplate().execute(session -> {
				CriteriaBuilder cb = session.getCriteriaBuilder();
				CriteriaQuery<GradingRule> cq = cb.createQuery(GradingRule.class);
				Root<GradingRule> root = cq.from(GradingRule.class);
				root.fetch("attendanceSite", JoinType.INNER);
				cq.select(root).where(cb.equal(root.get("attendanceSite"), attendanceSite));
				return session.createQuery(cq).getResultList();
			});
		} catch (DataAccessException e) {
			log.error("getGradingRulesForSite failed", e);
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<Long> getAttendanceSiteBatch(final Instant syncTime, final Long lastId) {
		final HibernateCallback<List<Long>> hcb = new HibernateCallback<List<Long>>() {
			@Override
			public List<Long> doInHibernate(Session session) throws HibernateException {
				CriteriaBuilder cb = session.getCriteriaBuilder();
				CriteriaQuery<Long> cq = cb.createQuery(Long.class);
				Root<AttendanceSite> root = cq.from(AttendanceSite.class);
				cq.select(root.get("id"))
					.where(
						cb.and(
							cb.or(cb.isNull(root.get("syncTime")), cb.lessThan(root.get("syncTime"), syncTime)),
							cb.greaterThan(root.get("id"), lastId)
						)
					)
					.orderBy(cb.asc(root.get("id")));
				return session.createQuery(cq).setMaxResults(5).getResultList();
			}
		};

		return getHibernateTemplate().execute(hcb);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<Long> getAttendanceSitesInSync() {
		final HibernateCallback<List<Long>> hcb = new HibernateCallback<List<Long>>() {
			@Override
			public List<Long> doInHibernate(Session session) throws HibernateException {
				CriteriaBuilder cb = session.getCriteriaBuilder();
				CriteriaQuery<Long> cq = cb.createQuery(Long.class);
				Root<AttendanceSite> root = cq.from(AttendanceSite.class);
				cq.select(root.get("id")).where(cb.isTrue(root.get("isSyncing")));
				return session.createQuery(cq).getResultList();
			}
		};

		return getHibernateTemplate().execute(hcb);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public boolean markAttendanceSiteForSync(final List<Long> ids, final Instant syncTime) {
		final HibernateCallback hcb = new HibernateCallback() {
			@Override
			public Integer doInHibernate(Session session) throws HibernateException {
				CriteriaBuilder cb = session.getCriteriaBuilder();
				CriteriaUpdate<AttendanceSite> cu = cb.createCriteriaUpdate(AttendanceSite.class);
				Root<AttendanceSite> root = cu.from(AttendanceSite.class);
				cu.set(root.get("isSyncing"), true)
				  .set(root.get("syncTime"), syncTime)
				  .where(root.get("id").in(ids));
				return session.createQuery(cu).executeUpdate();
			}
		};

		return getHibernateTemplate().execute(hcb).equals(ids.size());
	}

	/**
	 * init
	 */
	public void init() {
		log.debug("AttendanceDaoImpl init()");
	}

	private void requireAuditFields(AttendanceEvent attendanceEvent) {
		requireAuditFields("AttendanceEvent", attendanceEvent.getLastModifiedBy(), attendanceEvent.getLastModifiedDate());
		for (AttendanceRecord record : attendanceEvent.getRecords()) {
			requireAuditFields("AttendanceRecord", record.getLastModifiedBy(), record.getLastModifiedDate());
		}
	}

	private void requireAuditFields(String entityName, String lastModifiedBy, Instant lastModifiedDate) {
		if (lastModifiedBy == null || lastModifiedDate == null) {
			throw new IllegalArgumentException(entityName + " audit fields must be set before persistence");
		}
	}

	@SuppressWarnings("unchecked")
	private List<AttendanceEvent> getEventsForAttendanceSiteHelper(final AttendanceSite aS){
		log.debug("getAttendanceEventsForSiteHelper()");

		try {
			return getHibernateTemplate().execute(session -> {
				CriteriaBuilder cb = session.getCriteriaBuilder();
				CriteriaQuery<AttendanceEvent> cq = cb.createQuery(AttendanceEvent.class);
				Root<AttendanceEvent> root = cq.from(AttendanceEvent.class);
				root.fetch("attendanceSite", JoinType.INNER);
				cq.select(root).where(cb.equal(root.get("attendanceSite"), aS));
				return session.createQuery(cq).getResultList();
			});

		} catch (DataAccessException e) {
			log.error("getEventsForAttendanceSiteHelper failed", e);
			return null;
		}
	}

	// Generic Function to get something by it's ID.


}
