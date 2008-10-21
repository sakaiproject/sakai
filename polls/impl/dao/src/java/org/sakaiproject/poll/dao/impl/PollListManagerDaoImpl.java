/**********************************************************************************
 * $URL: $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.poll.dao.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.poll.model.Option;
import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PollListManagerDaoImpl extends HibernateDaoSupport implements PollListManager {

    // use commons logger
    private static Log log = LogFactory.getLog(PollListManagerDaoImpl.class);
    public static final String REFERENCE_ROOT = Entity.SEPARATOR + "poll";

    private AuthzGroupService authzGroupService;
    public void setAuthzGroupService(AuthzGroupService authzGroupService) {
        this.authzGroupService = authzGroupService;
    }

    private SecurityService securityService;
    public void setSecurityService(SecurityService ss) {
        securityService = ss;
    }

    private EventTrackingService eventTrackingService;
    public void setEventTrackingService(EventTrackingService ets) {
        eventTrackingService = ets;
    }

    private ToolManager toolManager;
    public void setToolManager(ToolManager tm) {
        toolManager = tm;
    }

    private EntityManager entityManager;
    public void setEntityManager(EntityManager em) {
        entityManager = em;
    }

    private FunctionManager functionManager;
    public void setFunctionManager(FunctionManager fm) {
        functionManager = fm;
    }

    private IdManager idManager;
    public void setIdManager(IdManager idm) {
        idManager = idm;
    }

    public void init() {
        try {
            entityManager.registerEntityProducer(this, REFERENCE_ROOT);
        } catch (Throwable t) {
            log.warn("init(): ", t);
        }

        functionManager.registerFunction(PERMISSION_VOTE);
        functionManager.registerFunction(PERMISSION_ADD);
        functionManager.registerFunction(PERMISSION_DELETE_OWN);
        functionManager.registerFunction(PERMISSION_DELETE_ANY);
        functionManager.registerFunction(PERMISSION_EDIT_ANY);
        functionManager.registerFunction(PERMISSION_EDIT_OWN);
        log.info(this + " init()");

    }

    public void destroy() {

    }


    @SuppressWarnings("unchecked")
    public List<Poll> findAllPollsForUserAndSitesAndPermission(String userId, String[] siteIds,
            String permissionConstant) {
        if (userId == null || permissionConstant == null) {
            throw new IllegalArgumentException("userId and permissionConstant must be set");
        }
        List<Poll> polls = null;
        // get all allowed sites for this user
        List<String> allowedSites = getSitesForUser(userId, permissionConstant);
        if (! allowedSites.isEmpty()) {
            if (siteIds != null) {
                if (siteIds.length > 0) {
                    // filter down to just the requested ones
                    for (int j = 0; j < allowedSites.size(); j++) {
                        String siteId = allowedSites.get(j);
                        boolean found = false;
                        for (int i = 0; i < siteIds.length; i++) {
                            if (siteId.equals(siteIds[i])) {
                                found = true;
                            }
                        }
                        if (!found) {
                            allowedSites.remove(j);
                        }
                    }
                } else {
                    // no sites to search so EXIT here
                    return new ArrayList<Poll>();
                }
            }
            String[] siteIdsToSearch = allowedSites.toArray(new String[allowedSites.size()]);
            DetachedCriteria d = DetachedCriteria.forClass(Poll.class);
            if (siteIdsToSearch.length > 0) {
                d.add( Restrictions.in("siteId", siteIdsToSearch) );
            }
            if (PollListManager.PERMISSION_VOTE.equals(permissionConstant)) {
                // limit to polls which are open
                Date now = new Date();
                d.add( Restrictions.lt("voteOpen", now));
                d.add( Restrictions.gt("voteClose", now));
            } else {
                // show all polls
            }
            d.addOrder( Order.desc("creationDate") );
            polls = getHibernateTemplate().findByCriteria(d);
        }
        if (polls == null) {
            polls = new ArrayList<Poll>();
        }
        return polls;
    }

    private static final String SAKAI_SITE_TYPE = SiteService.SITE_SUBTYPE;
    @SuppressWarnings("unchecked")
    protected List<String> getSitesForUser(String userId, String permission) {
        log.debug("userId: " + userId + ", permission: " + permission);

        List<String> l = new ArrayList<String>();

        // get the groups from Sakai
        Set<String> authzGroupIds = 
           authzGroupService.getAuthzGroupsIsAllowed(userId, permission, null);
        Iterator<String> it = authzGroupIds.iterator();
        while (it.hasNext()) {
           String authzGroupId = it.next();
           Reference r = entityManager.newReference(authzGroupId);
           if (r.isKnownType()) {
              // check if this is a Sakai Site or Group
              if (r.getType().equals(SiteService.APPLICATION_ID)) {
                 String type = r.getSubType();
                 if (SAKAI_SITE_TYPE.equals(type)) {
                    // this is a Site
                    String siteId = r.getId();
                    l.add(siteId);
                 }
              }
           }
        }

        if (l.isEmpty()) log.info("Empty list of siteIds for user:" + userId + ", permission: " + permission);
        return l;
     }

    public boolean savePoll(Poll t) {
        boolean newPoll = false;
        if (t.getId() == null) {
            newPoll = true;
            t.setId(idManager.createUuid());
        }

        // we may need to truncate the description field
        if (t.getDetails().length() > 254)
            t.setDetails(t.getDetails().substring(0, 254));

        try {
            getHibernateTemplate().saveOrUpdate(t);

        } catch (DataAccessException e) {
            log.error("Hibernate could not save: " + e.toString());
            e.printStackTrace();
            return false;
        }
        log.debug(" Poll  " + t.toString() + "successfuly saved");
        if (newPoll)

            eventTrackingService.post(eventTrackingService.newEvent("poll.add", "poll/site/"
                    + t.getSiteId() + "/poll/" + t.getId(), true));
        else
            eventTrackingService.post(eventTrackingService.newEvent("poll.update", "poll/site/"
                    + t.getSiteId() + " /poll/" + t.getId(), true));

        return true;
    }

    public boolean deletePoll(Poll t) throws PermissionException {
        if (!pollCanDelete(t))
            throw new PermissionException(UserDirectoryService.getCurrentUser().getId(),
                    "poll.delete", "poll." + t.getId().toString());

        try {
            getHibernateTemplate().delete(t);
        } catch (DataAccessException e) {
            log.error("Hibernate could not delete: " + e.toString());
            e.printStackTrace();
            return false;
        }
        log.info("Poll id " + t.getId() + " deleted");
        eventTrackingService.post(eventTrackingService.newEvent("poll.delete", "poll/site/"
                + t.getSiteId() + "/poll/" + t.getId(), true));
        return true;
    }

    public List<Poll> findAllPolls(String siteId) {
        DetachedCriteria d = DetachedCriteria.forClass(Poll.class).add(
                Restrictions.eq("siteId", siteId)).addOrder(Order.desc("creationDate"));
        List<Poll> polls = getHibernateTemplate().findByCriteria(d);
        return polls;
    }

    public Poll getPollById(Long pollId) {
        DetachedCriteria d = DetachedCriteria.forClass(Poll.class).add(
                Restrictions.eq("pollId", pollId));
        Poll poll = (Poll) PollUtil.pollCollectionToList(getHibernateTemplate().findByCriteria(d))
                .get(0);

        // we need to get the options here
        List optionList = getOptionsForPoll(poll);

        poll.setOptions(optionList);

        return poll;
    }

    public Poll getPollById(Long pollId, boolean includeOptions) {
        Poll poll = (Poll) getHibernateTemplate().get(Poll.class, pollId);
        if (poll != null) {
            if (includeOptions) {
                List<Option> optionList = getOptionsForPoll(poll);
                poll.setOptions(optionList);
            }
        }
        return poll;
    }


    // OPTIONS

    public List<Option> getOptionsForPoll(Poll poll) {
        return getOptionsForPoll(poll.getPollId());
    }

    public List<Option> getOptionsForPoll(Long pollId) {
        Poll poll = getPollById(pollId, false);
        if (poll == null) {
            throw new IllegalArgumentException("Cannot get options for a poll ("+pollId+") that does not exist");
        }
        // we need to get the options here
        DetachedCriteria d = DetachedCriteria.forClass(Option.class);
        d.add( Restrictions.eq("pollId", pollId));
        // add an explicit order - needed by Oracle
        d.addOrder(Order.asc("optionId"));
        List<Option> optionList = getHibernateTemplate().findByCriteria(d);
        return optionList;
    }

    public Poll getPollWithVotes(Long pollId) {
        DetachedCriteria d = DetachedCriteria.forClass(Poll.class).add(
                Restrictions.eq("pollId", pollId));
        return (Poll) PollUtil.pollCollectionToList(getHibernateTemplate().findByCriteria(d))
                .get(0);

    }

    public Option getOptionById(Long optionId) {
        Option option = (Option) getHibernateTemplate().get(Option.class, optionId);
        // if the id is null set it
        if (option.getUUId() == null) {
            option.setUUId( UUID.randomUUID().toString() );
            saveOption(option);
        }
        return option;
    }

    public void deleteOption(Option option) {
        try {
            getHibernateTemplate().delete(option);
        } catch (DataAccessException e) {
            log.error("Hibernate could not delete: " + e.toString());
            e.printStackTrace();
            return;
        }
        log.info("Option id " + option.getId() + " deleted");
    }

    public boolean saveOption(Option t) {
        if (t.getUUId() == null 
                || t.getUUId().trim().length() == 0) {
            t.setUUId( UUID.randomUUID().toString() );
        }

        try {
            getHibernateTemplate().saveOrUpdate(t);
        } catch (DataAccessException e) {
            log.error("Hibernate could not save: " + e.toString());
            e.printStackTrace();
            return false;
        }
        log.info("Option  " + t.toString() + "successfuly saved");
        return true;
    }

    
    // INTERNAL

    private boolean pollCanDelete(Poll poll) {
        if (securityService.isSuperUser() || this.isSiteOwner())
            return true;
        if (securityService.unlock(PERMISSION_DELETE_ANY, "/site/"
                + toolManager.getCurrentPlacement().getContext()))
            return true;

        if (securityService.unlock(PERMISSION_DELETE_OWN, "/site/"
                + toolManager.getCurrentPlacement().getContext())
                && poll.getOwner().equals(UserDirectoryService.getCurrentUser().getId()))
            return true;

        return false;
    }

    private boolean isSiteOwner() {
        if (securityService.isSuperUser())
            return true;
        else if (securityService.unlock("site.upd", "/site/"
                + toolManager.getCurrentPlacement().getContext()))
            return true;
        else
            return false;
    }

    /*
     * EntityProducer Methods
     */
    public String getLabel() {
        return "poll";
    }

    public boolean willArchiveMerge() {
        return true;
    }

    public String archive(String siteId, Document doc, Stack stack, String archivePath,
            List attachments) {
        log.debug("archive: poll " + siteId);
        // prepare the buffer for the results log
        StringBuilder results = new StringBuilder();

        // String assignRef = assignmentReference(siteId, SiteService.MAIN_CONTAINER);
        results.append("archiving " + getLabel() + " context " + Entity.SEPARATOR + siteId
                + Entity.SEPARATOR + SiteService.MAIN_CONTAINER + ".\n");

        // start with an element with our very own (service) name
        Element element = doc.createElement(PollListManager.class.getName());
        ((Element) stack.peek()).appendChild(element);
        stack.push(element);

        List pollsList = findAllPolls(siteId);
        log.debug("got list of " + pollsList.size() + " polls");
        for (int i = 0; pollsList.size() > i; i++) {
            try {
                Poll poll = (Poll) pollsList.get(i);
                log.info("got poll " + poll.getId());

                // archive this assignment
                Element el = poll.toXml(doc, stack);

                // get the options
                List options = getOptionsForPoll(poll);

                for (int q = 0; options.size() > q; q++) {
                    Option opt = (Option) options.get(q);
                    Element el2 = PollUtil.optionToXml(opt, doc, stack);
                    el.appendChild(el2);
                }

                element.appendChild(el);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } // while

        stack.pop();

        return results.toString();
    }

    public String merge(String arg0, Element arg1, String arg2, String arg3, Map arg4, Map arg5,
            Set arg6) {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean parseEntityReference(String reference, Reference ref) {
        if (reference.startsWith(REFERENCE_ROOT)) {
            // /syllabus/siteid/syllabusid
            String[] parts = split(reference, Entity.SEPARATOR);

            String subType = "";
            String context = null;
            String id = null;
            String container = "";

            if (parts.length > 2) {
                // the site/context
                context = parts[2];

                // the id
                if (parts.length > 3) {
                    id = parts[3];
                }
            }

            ref.set(PollListManager.class.getName(), subType, id, container, context);

            return true;
        }

        return false;
    }

    public String getEntityDescription(Reference arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public ResourceProperties getEntityResourceProperties(Reference arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Entity getEntity(Reference ref) {
        // TODO Auto-generated method stub
        Entity rv = null;

        if (REF_POLL_TYPE.equals(ref.getSubType())) {
            rv = getPoll(ref.getReference());
        }

        return rv;
    }

    public String getEntityUrl(Reference arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection getEntityAuthzGroups(Reference arg0, String arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public HttpAccess getHttpAccess() {
        // TODO Auto-generated method stub
        return null;
    }

    protected String[] split(String source, String splitter) {
        // hold the results as we find them
        Vector rv = new Vector();
        int last = 0;
        int next = 0;
        do {
            // find next splitter in source
            next = source.indexOf(splitter, last);
            if (next != -1) {
                // isolate from last thru before next
                rv.add(source.substring(last, next));
                last = next + splitter.length();
            }
        } while (next != -1);
        if (last < source.length()) {
            rv.add(source.substring(last, source.length()));
        }

        // convert to array
        return (String[]) rv.toArray(new String[rv.size()]);

    } // split

    public Poll getPoll(String ref) {
        // we need to get the options here
        DetachedCriteria d = DetachedCriteria.forClass(Poll.class).add(Restrictions.eq("id", ref));
        List optionList = PollUtil.optionCollectionToList(getHibernateTemplate().findByCriteria(d));
        Poll poll = (Poll) optionList.get(0);
        // if the id is null set it
        if (poll.getId() == null) {
            poll.setId(idManager.createUuid());
            savePoll(poll);
        }
        return poll;
    }

}
