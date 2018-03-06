/**********************************************************************************
 * $URL: $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.poll.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import java.util.Vector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.genericdao.api.search.Order;
import org.sakaiproject.genericdao.api.search.Restriction;
import org.sakaiproject.genericdao.api.search.Search;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.poll.dao.PollDao;
import org.sakaiproject.poll.logic.ExternalLogic;
import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.poll.logic.PollVoteManager;
import org.sakaiproject.poll.model.Option;
import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.poll.model.Vote;
import org.sakaiproject.poll.util.PollUtil;
import org.sakaiproject.site.api.SiteService;

@Slf4j
public class PollListManagerImpl implements PollListManager,EntityTransferrer {

    public static final String REFERENCE_ROOT = Entity.SEPARATOR + "poll";

    private EntityManager entityManager;
    public void setEntityManager(EntityManager em) {
        entityManager = em;
    }


    private IdManager idManager;
    public void setIdManager(IdManager idm) {
        idManager = idm;
    }

    private PollDao dao;
    public void setDao(PollDao dao) {
		this.dao = dao;
	}
    
    
    private PollVoteManager pollVoteManager;    
    public void setPollVoteManager(PollVoteManager pollVoteManager) {
		this.pollVoteManager = pollVoteManager;
	}


	private ExternalLogic externalLogic;    
    public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}

	public void init() {
        try {
            entityManager.registerEntityProducer(this, REFERENCE_ROOT);
        } catch (Exception t) {
            log.warn("init(): ", t);
        }

        externalLogic.registerFunction(PERMISSION_VOTE);
        externalLogic.registerFunction(PERMISSION_ADD);
        externalLogic.registerFunction(PERMISSION_DELETE_OWN);
        externalLogic.registerFunction(PERMISSION_DELETE_ANY);
        externalLogic.registerFunction(PERMISSION_EDIT_ANY);
        externalLogic.registerFunction(PERMISSION_EDIT_OWN);
        log.info(this + " init()");

    }

    public void destroy() {

    }



    public List<Poll> findAllPollsForUserAndSitesAndPermission(String userId, String[] siteIds,
            String permissionConstant) {
        if (userId == null || permissionConstant == null) {
            throw new IllegalArgumentException("userId and permissionConstant must be set");
        }
        List<Poll> polls = null;
        // get all allowed sites for this user
        List<String> allowedSites = externalLogic.getSitesForUser(userId, permissionConstant);

        if(siteIds!=null && siteIds.length>0 && !allowedSites.isEmpty()){
            List<String> requestedSiteIds = Arrays.asList(siteIds);
            // filter down to just the requested ones
            allowedSites.retainAll(requestedSiteIds);
            if(allowedSites.isEmpty()){
                // no sites to search so EXIT here
                return new ArrayList<Poll>();
            }
            String[] siteIdsToSearch = allowedSites.toArray(new String[allowedSites.size()]);
            Search search = new Search();
            if (siteIdsToSearch.length > 0) {
                search.addRestriction(new Restriction("siteId", siteIdsToSearch));
            }
            if (PollListManager.PERMISSION_VOTE.equals(permissionConstant)) {
                // limit to polls which are open
                Date now = new Date();
                search.addRestriction(new Restriction("voteOpen", now, Restriction.LESS));
                search.addRestriction(new Restriction("voteClose", now, Restriction.GREATER));
            } else {
                // show all polls
            }
            search.addOrder(new Order("creationDate"));
            polls = dao.findBySearch(Poll.class, search);
        }
        if (polls == null) {
            polls = new ArrayList<Poll>();
        }
        return polls;
    }



    public boolean savePoll(Poll t) throws SecurityException, IllegalArgumentException {
        boolean newPoll = false;
        
        if (t == null || t.getText() == null || t.getSiteId() == null || t.getVoteOpen() == null|| t.getVoteClose() == null) {
        	throw new IllegalArgumentException("you must supply a question, siteId & open and close dates");
        }
        
        if (!externalLogic.isUserAdmin() && !externalLogic.isAllowedInLocation(PollListManager.PERMISSION_ADD, externalLogic.getSiteRefFromId(t.getSiteId()),
        			externalLogic.getCurrentuserReference())) {
        	throw new SecurityException();
        }
        
        if (t.getId() == null) {
            newPoll = true;
            t.setId(idManager.createUuid());
        }

        try {
            dao.save(t);

        } catch (DataAccessException e) {
            log.error("Hibernate could not save: " + e.toString(), e);
            return false;
        }
        log.debug(" Poll  " + t.toString() + "successfuly saved");
        externalLogic.registerStatement(t.getText(), newPoll);
        if (newPoll)
        	externalLogic.postEvent("poll.add", "poll/site/"
                    + t.getSiteId() + "/poll/" + t.getId(), true);
        else
        	externalLogic.postEvent("poll.update", "poll/site/"
                    + t.getSiteId() + " /poll/" + t.getId(), true);

        return true;
    }

    public boolean deletePoll(Poll t) throws SecurityException, IllegalArgumentException {
    	if (t == null) {
    		throw new IllegalArgumentException("Poll can't be null");
    	}

    	if (t.getPollId() == null) {
    		throw new IllegalArgumentException("Poll id can't be null");
    	}

    	if (!pollCanDelete(t)) {
    		throw new SecurityException("user:" + externalLogic.getCurrentuserReference() + " can't delete poll: " + t.getId());
    	}

    	//Delete the Votes
    	List<Vote> vote = t.getVotes();
    	
    	//We could have a partially populate item
    	if (vote == null || vote.isEmpty()) {
    		log.debug("getting votes as they where null");
    		vote = pollVoteManager.getAllVotesForPoll(t);
    		log.debug("got " + vote.size() + " vote");
    	}
    	
    	
    	Set<Vote> voteSet = new HashSet<Vote>(vote);
    	dao.deleteSet(voteSet);

    	//Delete the Options
    	List<Option> options = t.getPollOptions();
    	//as above we could have a partialy populate item
    	if (options ==  null || options.isEmpty()) {
    		options = getOptionsForPoll(t);
    	}
    	
    	Set<Option> optionSet = new HashSet<Option>(options);
    	dao.deleteSet(optionSet);


    	dao.delete(t);

    	log.info("Poll id " + t.getId() + " deleted");
    	externalLogic.postEvent("poll.delete", "poll/site/"
    			+ t.getSiteId() + "/poll/" + t.getId(), true);
    	return true;
    }

    public boolean userCanDeletePoll(Poll poll) {
        if (externalLogic.isUserAdmin())
            return true;
        if (externalLogic.isAllowedInLocation(PollListManager.PERMISSION_DELETE_ANY, externalLogic.getCurrentLocationReference()))
            return true;
        if (externalLogic.isAllowedInLocation(PollListManager.PERMISSION_DELETE_OWN, externalLogic.getCurrentLocationReference()) && poll.getOwner().equals(externalLogic.getCurrentUserId()))
            return true;

        return false;
    }

    public List<Poll> findAllPolls(String siteId) {
        
        Search search = new Search();
        search.addOrder(new Order("creationDate", false));
        search.addRestriction(new Restriction("siteId", siteId));
        
        List<Poll> polls = dao.findBySearch(Poll.class, search);
        return polls;
    }

    public Poll getPollById(Long pollId) throws SecurityException {
        
       return getPollById(pollId, true);
    }

    public Poll getPollById(Long pollId, boolean includeOptions) throws SecurityException {
    	
    	
    	Search search = new Search();
        search.addRestriction(new Restriction("pollId", pollId));
        Poll poll = dao.findOneBySearch(Poll.class, search);
        if (poll != null) {
            if (includeOptions) {
                List<Option> optionList = getOptionsForPoll(poll);
                poll.setOptions(optionList);
            }
        }
        
         if (poll == null)
        	 return null;
      //user needs at least site visit to read a poll
    	if (!externalLogic.isAllowedInLocation("site.visit", externalLogic.getSiteRefFromId(poll.getSiteId()), externalLogic.getCurrentuserReference()) && !externalLogic.isUserAdmin()) {
    		throw new SecurityException("user:" + externalLogic.getCurrentuserReference() + " can't read poll " + pollId);
    	}
        
        return poll;
    }


    // OPTIONS

    public List<Option> getOptionsForPoll(Poll poll) {
        return getOptionsForPoll(poll.getPollId());
    }

    public List<Option> getOptionsForPoll(Long pollId) {
        Poll poll;
		try {
			poll = getPollById(pollId, false);
		} catch (SecurityException e) {
			throw new SecurityException(e);
		}
        if (poll == null) {
            throw new IllegalArgumentException("Cannot get options for a poll ("+pollId+") that does not exist");
        }
        Search search = new Search();
        search.addRestriction(new Restriction("pollId", pollId));
        search.addOrder(new Order("optionId"));
        List<Option> optionList = dao.findBySearch(Option.class, search);
        return optionList;
    }

	public List<Option> getVisibleOptionsForPoll(Long pollId) {
		List<Option> options = getOptionsForPoll(pollId);
		
		//iterate and remove deleted options
		for (Iterator<Option> i = options.listIterator(); i.hasNext();) {
			Option o = i.next();
			if (o == null || o.getDeleted()) {
				i.remove();
			}
		}
		
		return options;
	}

    public Poll getPollWithVotes(Long pollId) {
    	Search search = new Search();
        search.addRestriction(new Restriction("pollId", pollId));
        return dao.findOneBySearch(Poll.class, search);

    }

    public Option getOptionById(Long optionId) {
    	Search search = new Search();
        search.addRestriction(new Restriction("optionId", optionId));
        Option option = dao.findOneBySearch(Option.class, search);
        // if the id is null set it
        if (option != null && option.getUUId() == null) {
            option.setUUId( UUID.randomUUID().toString() );
            saveOption(option);
        }
        return option;
    }

    public void deleteOption(Option option) {
        try {
            dao.delete(option);
        } catch (DataAccessException e) {
            log.error("Hibernate could not delete: " + e.toString(), e);
            return;
        }
        log.info("Option id " + option.getId() + " deleted");
    }

	public void deleteOption(Option option, boolean soft) {
		if (!soft) {
			deleteOption(option);
			return;
		} else {
			try {
				option.setDeleted(Boolean.TRUE);
				dao.save(option);
				log.info("Option id " + option.getId() + " soft deleted.");
			} catch (DataAccessException e) {
				log.error("Hibernate could not soft delete delete!", e);
	            return;
			}
		}
	}

    public boolean saveOption(Option t) {
        if (t.getUUId() == null 
                || t.getUUId().trim().length() == 0) {
            t.setUUId( UUID.randomUUID().toString() );
        }

        try {
            dao.save(t);
        } catch (DataAccessException e) {
            log.error("Hibernate could not save: " + e.toString(), e);
            return false;
        }
        log.info("Option  " + t.toString() + "successfuly saved");
        return true;
    }

    
    // INTERNAL

    private boolean pollCanDelete(Poll poll) {
        if (externalLogic.isUserAdmin() || this.isSiteOwner(poll.getSiteId()))
            return true;
        if (externalLogic.isAllowedInLocation(PERMISSION_DELETE_ANY, externalLogic.getSiteRefFromId(poll.getSiteId())))
            return true;

        if (externalLogic.isAllowedInLocation(PERMISSION_DELETE_OWN, externalLogic.getSiteRefFromId(poll.getSiteId()))
        		&& poll.getOwner().equals(externalLogic.getCurrentUserId()))
            return true;

        return false;
    }

    private boolean isSiteOwner(String siteId) {
        if (externalLogic.isUserAdmin())
            return true;
        else if (externalLogic.isAllowedInLocation("site.upd", externalLogic.getSiteRefFromId(siteId)))
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

    @Override
    public String archive(String siteId, Document doc, Stack<Element> stack, String archivePath,
            List<Reference> attachments) {
        log.debug("archive: poll " + siteId);
        // prepare the buffer for the results log
        StringBuilder results = new StringBuilder();

        // String assignRef = assignmentReference(siteId, SiteService.MAIN_CONTAINER);
        results.append("archiving " + getLabel() + " context " + Entity.SEPARATOR
                       + siteId + Entity.SEPARATOR + SiteService.MAIN_CONTAINER
                       + ".\n");

        // start with an element with our very own (service) name
        Element element = doc.createElement(PollListManager.class.getName());
        stack.peek().appendChild(element);
        stack.push(element);

        List<Poll> pollsList = findAllPolls(siteId);
        log.debug("got list of " + pollsList.size() + " polls");
        for (Poll poll : pollsList) {
            try {
                log.info("got poll " + poll.getId());

                // archive this assignment
                Element el = poll.toXml(doc, stack);

                // since we aren't archiving votes too, don't worry about archiving the
                // soft-deleted options -- only "visible".
                List<Option> options = getVisibleOptionsForPoll(poll.getPollId());

                for (Option option : options) {
                    Element el2 = PollUtil.optionToXml(option, doc, stack);
                    el.appendChild(el2);
                }

                element.appendChild(el);
            } catch (Exception e) {
                log.error("Failed to archive {} in site {}", poll.getId(), siteId, e);
            }

        } // while

        stack.pop();

        return results.toString();
    }

	public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map<String, String> attachmentNames, Map<String, String> userIdTrans, Set<String> userListAllowImport) {
        /* USERS ARE NOT MERGED */
        NodeList polls = root.getElementsByTagName("poll");
        for (int i=0; i<polls.getLength(); ++i) {
            Element pollElement = (Element) polls.item(i);
            Poll poll = Poll.fromXML(pollElement);
            poll.setSiteId(siteId);
            savePoll(poll);
            NodeList options = pollElement.getElementsByTagName("option");
            for (int j=0; j<options.getLength(); ++j) {
                Element optionElement = (Element) options.item(j);
                Option option = PollUtil.xmlToOption(optionElement);
                option.setPollId(poll.getPollId());
                saveOption(option);
            }
        }
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

		/**
		 * {@inheritDoc}
		 */
		public String[] myToolIds()
		{
			String[] toolIds = { "sakai.poll"};
			return toolIds;
		}


		public void transferCopyEntities(String fromContext, String toContext, List resourceIds, boolean condition){
			transferCopyEntities(fromContext, toContext, resourceIds);
		}

		public void transferCopyEntities(String fromContext, String toContext, List resourceIds){
			try{
				Iterator<Poll> fromPolls = findAllPolls(fromContext).iterator();
				while (fromPolls.hasNext()){
					Poll fromPoll = fromPolls.next();
					Poll fromPollV = getPollWithVotes(fromPoll.getPollId());
					Poll toPoll = (Poll) new Poll();
					toPoll.setOwner(fromPollV.getOwner());
					toPoll.setSiteId(toContext);
			        toPoll.setCreationDate(fromPollV.getCreationDate());
			        toPoll.setText(fromPollV.getText());
			        toPoll.setMinOptions(fromPollV.getMinOptions());
			        toPoll.setMaxOptions(fromPollV.getMaxOptions());
			        toPoll.setVoteOpen(fromPollV.getVoteOpen());		       
			        toPoll.setVoteClose(fromPollV.getVoteClose());		       
			        toPoll.setDisplayResult(fromPollV.getDisplayResult());
			        toPoll.setLimitVoting(fromPollV.getLimitVoting());
			        toPoll.setDetails(fromPollV.getDetails());
			        
			        //Guardamos toPoll para que se puedan ir añandiéndole las opciones y los votos
			        savePoll(toPoll);
			        
			        //Añadimos las opciones
			        List<Option> options = getOptionsForPoll(fromPoll);
			        if (options!=null){
				        Iterator<Option> fromOptions = options.iterator();
				        while (fromOptions.hasNext()){
				        	Option fromOption = (Option) fromOptions.next();
				        	Option toOption = (Option) new Option();
				        	toOption.setOptionText(fromOption.getOptionText());
				        	toOption.setStatus(fromOption.getStatus());
				        	toOption.setPollId(toPoll.getPollId());
				        	toOption.setDeleted(fromOption.getDeleted());
				        	saveOption(toOption);
				        	
				        	toPoll.addOption(toOption);
				        }
			        }

			        //Añadimos los votos
			        List<Vote> votes = fromPollV.getVotes();
			        if (votes!=null){
			        	Iterator<Vote> fromVotes = votes.iterator();
			        	while (fromVotes.hasNext()){
			        		toPoll.addVote((Vote)fromVotes.next());
			        	}
			        }
	
			        //Actualizamos toPoll
			        savePoll(toPoll);
				}
			}catch(Exception e){
				log.error(e.getMessage(), e);
			}
		}


    protected String[] split(String source, String splitter) {
        // hold the results as we find them
        Vector<String> rv = new Vector<String>();
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
        
        Search search = new Search();
        search.addRestriction(new Restriction("id", ref));
        Poll poll = dao.findOneBySearch(Poll.class, search);
        // if the id is null set it
        if (poll.getId() == null) {
            poll.setId(idManager.createUuid());
            savePoll(poll);
        }
        return poll;
    }
    
    
    
	public boolean isAllowedViewResults(Poll poll, String userId) {
		if (externalLogic.isUserAdmin())
			return true;

		if (poll.getDisplayResult().equals("open"))
			return true;

		if (poll.getDisplayResult().equals("afterVoting")) {
			
			Search search = new Search();
	        search.addRestriction(new Restriction("pollId", poll.getPollId()));
	        search.addRestriction(new Restriction("userId", userId));
	        
	        List<Vote> votes = dao.findBySearch(Vote.class, search);		
	        //log.info("got " + pollCollection.size() + "votes for this poll");
	        if (votes.size() > 0)
	        	return true;
		}

		if ((poll.getDisplayResult().equals("afterClosing") || poll.getDisplayResult().equals("afterVoting") )&& poll.getVoteClose().before(new Date()))
			return true;

		//the owner can view the results
		if(poll.getOwner().equals(userId) && !externalLogic.userIsViewingAsRole())
			return true;

		return false;
	}
	
	public boolean isPollPublic(Poll poll) {
		
		//is this poll public?
		if(poll.getIsPublic()){
			return true;
		}
		
		//can the anonymous user vote?
		if(externalLogic.isAllowedInLocation(PollListManager.PERMISSION_VOTE, externalLogic.getSiteRefFromId(poll.getSiteId()))){
			return true;
		}
		return false;
	}
}
