package org.sakaiproject.poll.impl.repository;

import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.sakaiproject.poll.api.repository.OptionRepository;
import org.sakaiproject.poll.model.Option;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;
import org.springframework.transaction.annotation.Transactional;

public class OptionRepositoryImpl extends SpringCrudRepositoryImpl<Option, Long> implements OptionRepository {

    private Session session() { return sessionFactory.getCurrentSession(); }

    @Override
    @Transactional(readOnly = true)
    public List<Option> findByPollIdOrderByOptionOrder(Long pollId) {
        CriteriaBuilder cb = session().getCriteriaBuilder();
        CriteriaQuery<Option> cq = cb.createQuery(Option.class);
        Root<Option> root = cq.from(Option.class);
        cq.where(cb.equal(root.get("pollId"), pollId));
        cq.select(root).orderBy(cb.asc(root.get("optionOrder")));
        return session().createQuery(cq).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Option> findByOptionId(Long optionId) {
        CriteriaBuilder cb = session().getCriteriaBuilder();
        CriteriaQuery<Option> cq = cb.createQuery(Option.class);
        Root<Option> root = cq.from(Option.class);
        cq.where(cb.equal(root.get("optionId"), optionId));
        List<Option> rs = session().createQuery(cq).getResultList();
        return rs.isEmpty() ? Optional.empty() : Optional.of(rs.get(0));
    }
}
