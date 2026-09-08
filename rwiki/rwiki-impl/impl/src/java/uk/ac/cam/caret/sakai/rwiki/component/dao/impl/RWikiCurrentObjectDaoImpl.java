/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package uk.ac.cam.caret.sakai.rwiki.component.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Predicate;
import uk.ac.cam.caret.sakai.rwiki.component.Messages;
import uk.ac.cam.caret.sakai.rwiki.model.RWikiCurrentObjectImpl;
import uk.ac.cam.caret.sakai.rwiki.model.RWikiCurrentObjectContentImpl;
import uk.ac.cam.caret.sakai.rwiki.service.api.dao.ObjectProxy;
import uk.ac.cam.caret.sakai.rwiki.service.api.dao.RWikiCurrentObjectDao;
import uk.ac.cam.caret.sakai.rwiki.service.api.dao.RWikiHistoryObjectDao;
import uk.ac.cam.caret.sakai.rwiki.service.api.dao.RWikiObjectContentDao;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiCurrentObject;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiHistoryObject;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
import uk.ac.cam.caret.sakai.rwiki.utils.TimeLogger;

// FIXME: Component
@Slf4j
@Transactional(readOnly = true)
public class RWikiCurrentObjectDaoImpl implements RWikiCurrentObjectDao, ObjectProxy {
	@Setter private SessionFactory sessionFactory;


	protected RWikiObjectContentDao contentDAO = null;

	protected RWikiHistoryObjectDao historyDAO = null;

	private Pattern idPattern = Pattern.compile("(?<and>(!)?(\\S+)\\s+and\\s+(!)?(\\S+))*((?<!^)\\k<and>*and\\s+(!)?(\\S+))*(!)?(\\S+)*\\s*",Pattern.CASE_INSENSITIVE);

	public boolean exists(final String name) {
		long start = System.currentTimeMillis();
		try {
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<Long> cq = cb.createQuery(Long.class);
			Root<RWikiCurrentObjectImpl> root = cq.from(RWikiCurrentObjectImpl.class);
			cq.select(cb.count(root)).where(cb.equal(root.get("name"), name));
			int count = session.createQuery(cq).uniqueResult().intValue();
			return count > 0;
		} finally {
			long finish = System.currentTimeMillis();
			TimeLogger.printTimer("RWikiObjectDaoImpl.exists: " + name, start, finish);
		}
	}

	public RWikiCurrentObject findByGlobalName(final String name) {
		long start = System.currentTimeMillis();
		try {
			// there is no point in sorting by version, since there is only one
			// version in
			// this table.
			// also using like is much slower than eq
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<RWikiCurrentObjectImpl> cq = cb.createQuery(RWikiCurrentObjectImpl.class);
			Root<RWikiCurrentObjectImpl> root = cq.from(RWikiCurrentObjectImpl.class);

			cq.select(root).where(cb.equal(root.get("name"), name));

			List found = session.createQuery(cq).getResultList();
			if (found.size() == 0)
			{
				log.debug("Found {} objects with name {}", found.size(), name);
				return null;
			}
			log.debug("Found {} objects with name {} returning most recent one.", found.size(), name);
			return (RWikiCurrentObject) proxyObject(found.get(0));
		} finally {
			long finish = System.currentTimeMillis();
			TimeLogger.printTimer("RWikiObjectDaoImpl.findByGlobalName: " + name, start, finish);
		}
	}

	public List findByGlobalNameAndContents(final String criteria, final String user, final String realm) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<RWikiCurrentObjectImpl> cq = cb.createQuery(RWikiCurrentObjectImpl.class);
		Root<RWikiCurrentObjectImpl> root = cq.from(RWikiCurrentObjectImpl.class);
		Root<RWikiCurrentObjectContentImpl> content = cq.from(RWikiCurrentObjectContentImpl.class);
		// Hibernate 6 rejects lower(CLOB) during type validation; keep the full content in SQL.
		Expression<String> lowerContent = session.getCriteriaBuilder().sql("lower(?)", String.class, content.get("content"));
		Matcher matcher = idPattern.matcher(criteria);
		List<Predicate> alternatives = new ArrayList<>();
		List<Predicate> exclusions = new ArrayList<>();

		while (matcher.find()) {
			if (matcher.group(0).isEmpty()) {
				continue;
			}
			if (matcher.group("and") != null) {
				alternatives.add(cb.and(
					searchTerm(cb, root, lowerContent, matcher.group(3), matcher.group(2) != null),
					searchTerm(cb, root, lowerContent, matcher.group(5), matcher.group(4) != null)));
			} else if (matcher.group(6) != null) {
				int last = alternatives.size() - 1;
				alternatives.set(last, cb.and(alternatives.get(last),
					searchTerm(cb, root, lowerContent, matcher.group(8), matcher.group(7) != null)));
			} else if (matcher.group(10) != null) {
				Predicate term = searchTerm(cb, root, lowerContent, matcher.group(10), matcher.group(9) != null);
				if (matcher.group(9) != null) {
					exclusions.add(term);
				} else {
					alternatives.add(term);
				}
			}
		}

		Predicate expression = cb.conjunction();
		if (!exclusions.isEmpty()) {
			expression = cb.or(exclusions.toArray(new Predicate[0]));
			if (!alternatives.isEmpty()) {
				// Preserve the original HQL precedence: AND binds to the last OR term.
				int last = alternatives.size() - 1;
				alternatives.set(last, cb.and(alternatives.get(last), expression));
			}
		}
		if (!alternatives.isEmpty()) {
			expression = cb.or(alternatives.toArray(new Predicate[0]));
		}
		cq.select(root).distinct(true).where(
			cb.equal(root.get("realm"), realm),
			expression,
			cb.equal(root.get("id"), content.get("rwikiid")))
			.orderBy(cb.asc(root.get("name")));
		return new ListProxy(session.createQuery(cq).getResultList(), this);
	}

	private Predicate searchTerm(CriteriaBuilder cb, Root<RWikiCurrentObjectImpl> root,
			Expression<String> lowerContent, String term, boolean exclude) {
		String pattern = "%" + term.toLowerCase() + "%";
		if (exclude) {
			return cb.and(cb.notLike(lowerContent, pattern),
				cb.notLike(cb.lower(root.get("name")), pattern));
		}
		return cb.or(cb.like(lowerContent, pattern),
			cb.like(cb.lower(root.get("name")), pattern));
	}

	@Transactional
	public void update(RWikiCurrentObject rwo, RWikiHistoryObject rwho) {
		// should have already checked
		RWikiCurrentObjectImpl impl = (RWikiCurrentObjectImpl) rwo;
		sessionFactory.getCurrentSession().saveOrUpdate(impl);
		// update the history
		if (rwho != null) {
			rwho.setRwikiobjectid(impl.getId());
			historyDAO.update(rwho);
		}
		// remember to save the content, and make certain the contentDAO is set
		// first
		impl.setRwikiObjectContentDao(contentDAO);
		impl.getRWikiObjectContent().setRwikiid(rwo.getId());
		contentDAO.update(impl.getRWikiObjectContent());
	}

	public RWikiCurrentObject createRWikiObject(String name, String realm) {

		RWikiCurrentObjectImpl returnable = new RWikiCurrentObjectImpl();
		proxyObject(returnable);
		returnable.setName(name);
		returnable.setRealm(realm);
		returnable.setVersion(new Date());
		returnable.setRevision(Integer.valueOf(0));

		returnable.setContent(Messages.getString("RWikiCurrentObjectDaoImpl.30") + Messages.getString("RWikiCurrentObjectDaoImpl.31"));
		return returnable;
	}

	public List findChangedSince(final Date since, final String realm) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<RWikiCurrentObjectImpl> cq = cb.createQuery(RWikiCurrentObjectImpl.class);
		Root<RWikiCurrentObjectImpl> root = cq.from(RWikiCurrentObjectImpl.class);

		cq.select(root)
			.where(cb.and(
				cb.greaterThanOrEqualTo(root.get("version"), since),
				cb.equal(root.get("realm"), realm)
			))
		.orderBy(cb.desc(root.get("version")));

		List found = session.createQuery(cq).getResultList();
		return new ListProxy(found, this);
	}

	public List findReferencingPages(final String name) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<String> cq = cb.createQuery(String.class);
		Root<RWikiCurrentObjectImpl> root = cq.from(RWikiCurrentObjectImpl.class);
		cq.select(root.get("name")).where(cb.like(root.get("referenced"), "%::" + name + "::%"));
		return new ListProxy(session.createQuery(cq).getResultList(), this);
	}

	public RWikiCurrentObject getRWikiCurrentObject(final RWikiObject reference) {
		long start = System.currentTimeMillis();
		try {
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<RWikiCurrentObjectImpl> cq = cb.createQuery(RWikiCurrentObjectImpl.class);
			Root<RWikiCurrentObjectImpl> root = cq.from(RWikiCurrentObjectImpl.class);

			cq.select(root).where(cb.equal(root.get("id"), reference.getRwikiobjectid()));

			List found = session.createQuery(cq).getResultList();
			if (found.size() == 0) {
				log.debug("Found {} objects with id {}", found.size(), reference.getRwikiobjectid());
				return null;
			}
			log.debug("Found {} objects with id {} returning most recent one.", found.size(), reference.getRwikiobjectid());
			return (RWikiCurrentObject) proxyObject(found.get(0));
		}
		finally {
			long finish = System.currentTimeMillis();
			TimeLogger.printTimer("RWikiCurrentObjectDaoImpl.getRWikiCurrentObject: " + reference.getName(), start, finish);
		}
	}

	public RWikiObjectContentDao getContentDAO() {
		return contentDAO;
	}

	public void setContentDAO(RWikiObjectContentDao contentDAO) {
		this.contentDAO = contentDAO;
	}

	public Object proxyObject(Object o) {
		if (o != null && o instanceof RWikiCurrentObjectImpl) {
			RWikiCurrentObjectImpl rwCo = (RWikiCurrentObjectImpl) o;
			rwCo.setRwikiObjectContentDao(contentDAO);
		}
		return o;
	}

	public RWikiHistoryObjectDao getHistoryDAO() {
		return historyDAO;
	}

	public void setHistoryDAO(RWikiHistoryObjectDao historyDAO) {
		this.historyDAO = historyDAO;
	}

	public List getAll() {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<RWikiCurrentObjectImpl> cq = cb.createQuery(RWikiCurrentObjectImpl.class);
		Root<RWikiCurrentObjectImpl> root = cq.from(RWikiCurrentObjectImpl.class);

		cq.select(root).orderBy(cb.desc(root.get("version")));

		List found = session.createQuery(cq).getResultList();
		return new ListProxy(found, this);
	}

	@Transactional
	public void updateObject(RWikiObject rwo) {
		sessionFactory.getCurrentSession().saveOrUpdate(rwo);
	}

	public int getPageCount(final String group) {
		long start = System.currentTimeMillis();
		try
		{
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<Long> cq = cb.createQuery(Long.class);
			Root<RWikiCurrentObjectImpl> root = cq.from(RWikiCurrentObjectImpl.class);
			cq.select(cb.count(root)).where(cb.equal(root.get("realm"), group));
			int count = session.createQuery(cq).uniqueResult().intValue();
			return count;
		}
		finally
		{
			long finish = System.currentTimeMillis();
			TimeLogger.printTimer("RWikiObjectDaoImpl.getPageCount: " + group, //$NON-NLS-1$
					start, finish);
		}
	}

	public List findRWikiSubPages(final String globalParentPageName) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<RWikiCurrentObjectImpl> cq = cb.createQuery(RWikiCurrentObjectImpl.class);
		Root<RWikiCurrentObjectImpl> root = cq.from(RWikiCurrentObjectImpl.class);
		String search = globalParentPageName.replaceAll("([A%_])", "A$1") + "%";
		cq.select(root).where(cb.like(root.get("name"), search, 'A')).orderBy(cb.asc(root.get("name")));
		return new ListProxy(session.createQuery(cq).getResultList(), this);
	}

	public RWikiObject findLastRWikiSubPage(final String globalParentPageName) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<RWikiCurrentObjectImpl> cq = cb.createQuery(RWikiCurrentObjectImpl.class);
		Root<RWikiCurrentObjectImpl> root = cq.from(RWikiCurrentObjectImpl.class);
		String search = globalParentPageName.replaceAll("([A%_])", "A$1") + "%";
		cq.select(root).where(cb.like(root.get("name"), search, 'A')).orderBy(cb.desc(root.get("name")));
		List<RWikiCurrentObjectImpl> found = session.createQuery(cq).getResultList();
		if (found.isEmpty()) return null;
		return found.get(0);
	}

	public List findAllChangedSince(final Date time, final String basepath) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<RWikiCurrentObjectImpl> cq = cb.createQuery(RWikiCurrentObjectImpl.class);
		Root<RWikiCurrentObjectImpl> root = cq.from(RWikiCurrentObjectImpl.class);
		String search = basepath.replaceAll("([A%_])", "A$1") + "%";
		cq.select(root).where(cb.like(root.get("name"), search, 'A'),
			cb.greaterThanOrEqualTo(root.get("version"), time))
			.orderBy(cb.desc(root.get("version")), cb.asc(root.get("name")));
		return new ListProxy(session.createQuery(cq).getResultList(), this);
	}

	public List findAllPageNames() {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<String> cq = cb.createQuery(String.class);
		Root<RWikiCurrentObjectImpl> root = cq.from(RWikiCurrentObjectImpl.class);
		cq.select(root.get("name"));
		return session.createQuery(cq).getResultList();
	}

}
