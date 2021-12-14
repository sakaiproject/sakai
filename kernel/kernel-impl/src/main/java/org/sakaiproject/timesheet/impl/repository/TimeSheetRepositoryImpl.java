package org.sakaiproject.timesheet.impl.repository;

import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;
import org.sakaiproject.timesheet.api.TimeSheetEntry;
import org.sakaiproject.timesheet.api.repository.TimeSheetRepository;

public class TimeSheetRepositoryImpl extends SpringCrudRepositoryImpl<TimeSheetEntry, Long> implements TimeSheetRepository {

    public Optional<List<TimeSheetEntry>> findByReference(String reference) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<TimeSheetEntry> cq = cb.createQuery(TimeSheetEntry.class);
        Root<TimeSheetEntry> root = cq.from(TimeSheetEntry.class);
        cq.select(root);
        cq.where(cb.equal(root.get("reference"), reference));

        return Optional.ofNullable(session.createQuery(cq).getResultList());
    }

    @Override
    public Optional<List<TimeSheetEntry>> findAllByUserIdAndReference(String userId, String reference) {
        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<TimeSheetEntry> cq = cb.createQuery(TimeSheetEntry.class);
        Root<TimeSheetEntry> root = cq.from(TimeSheetEntry.class);
        cq.select(root);
        Predicate pUser = cb.equal(root.get("userId"), userId);
        Predicate pReference = cb.equal(root.get("reference"), reference);
        cq.where(cb.and(pUser, pReference));

        return Optional.ofNullable(session.createQuery(cq).getResultList());
    }
}
