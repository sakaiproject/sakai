package org.sakaiproject.poll.dao.impl;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import org.sakaiproject.poll.dao.PollDao;
import org.sakaiproject.poll.model.Option;
import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.poll.model.Vote;

@Slf4j
public class PollDaoHibernateImpl implements PollDao {

    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    private Session currentSession() {
        return sessionFactory.getCurrentSession();
    }

    // Persistence
    @Override
    public void save(Object entity) {
        // Use merge to avoid NonUniqueObjectException when another instance with the same id
        // is already associated with the current Session (common in service flows that read then save).
        Object managed = currentSession().merge(entity);
        // Propagate generated identifiers back to the caller's instance when needed
        if (entity != managed) {
            if (entity instanceof Poll && managed instanceof Poll) {
                Poll src = (Poll) managed;
                Poll dst = (Poll) entity;
                dst.setPollId(src.getPollId());
                dst.setId(src.getId());
            } else if (entity instanceof Option && managed instanceof Option) {
                Option src = (Option) managed;
                Option dst = (Option) entity;
                dst.setOptionId(src.getOptionId());
            } else if (entity instanceof Vote && managed instanceof Vote) {
                Vote src = (Vote) managed;
                Vote dst = (Vote) entity;
                dst.setId(src.getId());
            }
        }
    }

    @Override
    public void delete(Object entity) {
        if (entity == null) return;
        Session s = currentSession();
        if (entity instanceof Poll) {
            Long id = ((Poll) entity).getPollId();
            if (id != null) {
                Poll managed = s.get(Poll.class, id);
                if (managed != null) s.delete(managed);
                return;
            }
        } else if (entity instanceof Option) {
            Long id = ((Option) entity).getOptionId();
            if (id != null) {
                Option managed = s.get(Option.class, id);
                if (managed != null) s.delete(managed);
                return;
            }
        } else if (entity instanceof Vote) {
            Long id = ((Vote) entity).getId();
            if (id != null) {
                Vote managed = s.get(Vote.class, id);
                if (managed != null) s.delete(managed);
                return;
            }
        }
        // Fallback: if we cannot determine id, try deleting the instance if attached
        if (s.contains(entity)) {
            s.delete(entity);
        }
    }

    @Override
    public void deleteSet(Collection<?> entities) {
        if (entities == null) return;
        for (Object e : entities) {
            delete(e);
        }
    }

    // Polls
    @Override
    public List<Poll> findAllPolls() {
        return currentSession().createQuery("from Poll p", Poll.class).list();
    }

    @Override
    public List<Poll> findPollsBySite(String siteId, boolean creationDateAsc) {
        String order = creationDateAsc ? "asc" : "desc";
        Query<Poll> q = currentSession().createQuery(
            "from Poll p where p.siteId = :siteId order by p.creationDate " + order, Poll.class);
        q.setParameter("siteId", siteId);
        return q.list();
    }

    @Override
    public List<Poll> findPollsForSites(String[] siteIds, boolean creationDateAsc) {
        if (siteIds == null || siteIds.length == 0) return List.of();
        String order = creationDateAsc ? "asc" : "desc";
        Query<Poll> q = currentSession().createQuery(
            "from Poll p where p.siteId in (:siteIds) order by p.creationDate " + order, Poll.class);
        q.setParameterList("siteIds", siteIds);
        return q.list();
    }

    @Override
    public List<Poll> findOpenPollsForSites(String[] siteIds, Date now, boolean creationDateAsc) {
        if (siteIds == null || siteIds.length == 0) return List.of();
        String order = creationDateAsc ? "asc" : "desc";
        Query<Poll> q = currentSession().createQuery(
            "from Poll p where p.siteId in (:siteIds) " +
            "and (p.voteOpen is null or p.voteOpen <= :now) " +
            "and (p.voteClose is null or p.voteClose >= :now) " +
            "order by p.creationDate " + order,
            Poll.class);
        q.setParameterList("siteIds", siteIds);
        q.setParameter("now", now);
        return q.list();
    }
    }

    @Override
    public Poll findPollById(Long pollId) {
        if (pollId == null) return null;
        Query<Poll> q = currentSession().createQuery(
            "from Poll p where p.pollId = :pollId", Poll.class);
        q.setParameter("pollId", pollId);
        return q.uniqueResult();
    }

    @Override
    public Poll findPollByUuid(String uuid) {
        if (uuid == null) return null;
        Query<Poll> q = currentSession().createQuery(
            "from Poll p where p.id = :uuid", Poll.class);
        q.setParameter("uuid", uuid);
        return q.uniqueResult();
    }

    // Options
    @Override
    public List<Option> findOptionsByPollId(Long pollId) {
        Query<Option> q = currentSession().createQuery(
            "from Option o where o.pollId = :pollId order by o.optionOrder asc", Option.class);
        q.setParameter("pollId", pollId);
        return q.list();
    }

    @Override
    public Option findOptionById(Long optionId) {
        if (optionId == null) return null;
        Query<Option> q = currentSession().createQuery(
            "from Option o where o.optionId = :optionId", Option.class);
        q.setParameter("optionId", optionId);
        return q.uniqueResult();
    }

    // Votes
    @Override
    public List<Vote> findVotesByPollId(Long pollId) {
        Query<Vote> q = currentSession().createQuery(
            "from Vote v where v.pollId = :pollId", Vote.class);
        q.setParameter("pollId", pollId);
        return q.list();
    }

    @Override
    public List<Vote> findVotesByPollIdAndOption(Long pollId, Long optionId) {
        Query<Vote> q = currentSession().createQuery(
            "from Vote v where v.pollId = :pollId and v.pollOption = :optionId", Vote.class);
        q.setParameter("pollId", pollId);
        q.setParameter("optionId", optionId);
        return q.list();
    }

    @Override
    public List<Vote> findVotesByUserAndPollIds(String userId, Long[] pollIds) {
        if (userId == null || pollIds == null || pollIds.length == 0) return List.of();
        Query<Vote> q = currentSession().createQuery(
            "from Vote v where v.userId = :userId and v.pollId in (:pollIds)", Vote.class);
        q.setParameter("userId", userId);
        q.setParameterList("pollIds", pollIds);
        return q.list();
    }

    @Override
    public List<Vote> findVotesByUserAndPollId(String userId, Long pollId) {
        if (userId == null || pollId == null) return List.of();
        Query<Vote> q = currentSession().createQuery(
            "from Vote v where v.userId = :userId and v.pollId = :pollId", Vote.class);
        q.setParameter("userId", userId);
        q.setParameter("pollId", pollId);
        return q.list();
    }

    @Override
    public Vote findVoteById(Long voteId) {
        if (voteId == null) return null;
        Query<Vote> q = currentSession().createQuery(
            "from Vote v where v.id = :id", Vote.class);
        q.setParameter("id", voteId);
        return q.uniqueResult();
    }

    // Aggregates
    @Override
    public int getDisctinctVotersForPoll(Poll poll) {
        if (poll == null || poll.getPollId() == null) return 0;
        Query<Long> q = currentSession().createQuery(
            "select count(distinct v.submissionId) from Vote v where v.pollId = :pollId", Long.class);
        q.setParameter("pollId", poll.getPollId());
        Long count = q.uniqueResult();
        return count == null ? 0 : count.intValue();
    }
}
