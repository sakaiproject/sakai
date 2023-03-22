package org.sakaiproject.microsoft.impl.persistence;

import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.microsoft.api.model.SiteSynchronization;
import org.sakaiproject.microsoft.api.persistence.MicrosoftSiteSynchronizationRepository;
import org.sakaiproject.serialization.BasicSerializableRepository;

public class MicrosoftSiteSynchronizationRepositoryImpl extends BasicSerializableRepository<SiteSynchronization, String> implements MicrosoftSiteSynchronizationRepository {

	public Session getCurrentSession() {
		return sessionFactory.getCurrentSession();
	}
	
	@Override
	public List<SiteSynchronization> findAll(){
		return (List<SiteSynchronization>) startCriteriaQuery()
				.addOrder(Order.asc("status"))
				.list();
	}
	
	@Override
	public Optional<SiteSynchronization> findById(String id) {
		SiteSynchronization siteSynchronization = (SiteSynchronization) startCriteriaQuery().add(Restrictions.eq("id", id)).uniqueResult();
		return Optional.ofNullable(siteSynchronization);
	}
	
	@Override
	public Optional<SiteSynchronization> findBySiteTeam(String siteId, String teamId) {
		SiteSynchronization siteSynchronization = (SiteSynchronization) startCriteriaQuery()
				.add(Restrictions.eq("siteId", siteId))
				.add(Restrictions.eq("teamId", teamId))
				.uniqueResult();
		return Optional.ofNullable(siteSynchronization);
	}
	
	@Override
	public List<SiteSynchronization> findBySite(String siteId) {
		return (List<SiteSynchronization>)startCriteriaQuery()
		.add(Restrictions.eq("siteId", siteId))
		.list();
	}
	
	@Override
	public List<String> findBySiteIdList(List<String> siteIds) {
		return (List<String>)startCriteriaQuery()
				.setProjection(Projections.property("siteId"))
				.add(Restrictions.in("siteId", siteIds))
		.list();
	}
	
	@Override
	public List<SiteSynchronization> findByTeam(String teamId) {
		return (List<SiteSynchronization>)startCriteriaQuery()
		.add(Restrictions.eq("teamId", teamId))
		.list();
	}
	
	@Override
	public long countSiteSynchronizationsByTeamId(String teamId, boolean forced) {
		Criteria criteria = startCriteriaQuery()
				.setProjection(Projections.countDistinct("id"))
				.add(Restrictions.eq("teamId", teamId));
		if(forced) {
			criteria.add(Restrictions.eq("forced", true));
		}
		
		return ((Number) criteria.uniqueResult()).longValue();
	}
	
	@Override
	public Integer deleteSiteSynchronizationsById(List<String> ids) {
		Session session = sessionFactory.getCurrentSession();

		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaDelete<SiteSynchronization> delete = cb.createCriteriaDelete(SiteSynchronization.class);
		Root<SiteSynchronization> siteSynchronization = delete.from(SiteSynchronization.class);
		delete.where(siteSynchronization.get("id").in(ids));
		
		return session.createQuery(delete).executeUpdate();
	}
}
