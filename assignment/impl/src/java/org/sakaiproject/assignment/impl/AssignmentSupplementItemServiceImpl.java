/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation
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

package org.sakaiproject.assignment.impl;

import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.hibernate.query.Query;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentReferenceReckoner;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentAllPurposeItem;
import org.sakaiproject.assignment.api.model.AssignmentAllPurposeItemAccess;
import org.sakaiproject.assignment.api.model.AssignmentModelAnswerItem;
import org.sakaiproject.assignment.api.model.AssignmentNoteItem;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.assignment.api.model.AssignmentSupplementItemAttachment;
import org.sakaiproject.assignment.api.model.AssignmentSupplementItemService;
import org.sakaiproject.assignment.api.model.AssignmentSupplementItemWithAttachment;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
public class AssignmentSupplementItemServiceImpl extends HibernateDaoSupport implements AssignmentSupplementItemService {

	@Override
	public void updateModelAnswer(String assignmentId, String text, int showTo,
			Set<String> attachmentIds, boolean delete) {
		AssignmentModelAnswerItem item = getModelAnswer(assignmentId);
		if (delete) {
			if (item != null) {
				cleanAttachment(item);
				if (item.getAttachmentSet() != null) item.getAttachmentSet().clear();
				removeModelAnswer(item);
			}
			return;
		}
		if (item == null) {
			item = newModelAnswer();
			item.setAssignmentId(assignmentId);
			saveModelAnswer(item);
		}
		item.setText(text);
		item.setShowTo(showTo);
		updateAttachments(item, attachmentIds);
		saveModelAnswer(item);
	}

	@Override
	public void updateNote(String assignmentId, String creatorId, String text, int shareWith,
			boolean delete) {
		AssignmentNoteItem item = getNoteItem(assignmentId);
		if (delete) {
			if (item != null) removeNoteItem(item);
			return;
		}
		if (item == null) item = newNoteItem();
		item.setAssignmentId(assignmentId);
		item.setNote(text);
		item.setShareWith(shareWith);
		item.setCreatorId(creatorId);
		saveNoteItem(item);
	}

	@Override
	public boolean updateAllPurposeItem(String assignmentId, String siteId,
			AssignmentAllPurposeItem values, Set<String> attachmentIds, Set<String> selectedAccess,
			boolean delete) {
		AssignmentAllPurposeItem item = getAllPurposeItem(assignmentId);
		if (delete) {
			if (item != null) {
				cleanAttachment(item);
				if (item.getAttachmentSet() != null) item.getAttachmentSet().clear();
				cleanAllPurposeItemAccess(item);
				if (item.getAccessSet() != null) item.getAccessSet().clear();
				removeAllPurposeItem(item);
			}
			return true;
		}
		if (item == null) {
			item = newAllPurposeItem();
			item.setAssignmentId(assignmentId);
			item.setHide(false);
			saveAllPurposeItem(item);
		}
		item.setTitle(values.getTitle());
		item.setText(values.getText());
		item.setHide(values.getHide());
		item.setReleaseDate(values.getReleaseDate());
		item.setRetractDate(values.getRetractDate());
		updateAttachments(item, attachmentIds);
		if (selectedAccess == null) {
			saveAllPurposeItem(item);
			return true;
		}

		AuthzGroup realm;
		try {
			realm = m_authzGroupService.getAuthzGroup(m_siteService.siteReference(siteId));
		}
		catch (Exception e) {
			log.warn("Could not find authzGroup for site {} while saving All Purpose Item access", siteId, e);
			saveAllPurposeItem(item);
			return false;
		}
		Set<String> accessValues = selectedAllPurposeAccess(realm, selectedAccess);
		saveAllPurposeItemWithAccess(item, accessValues);
		return true;
	}

	private Set<String> selectedAllPurposeAccess(AuthzGroup realm, Set<String> selectedAccess) {
		Set<String> accessValues = new HashSet<>();
		for (Role role : realm.getRoles()) {
			if (selectedAccess.contains(role.getId())) {
				accessValues.add(role.getId());
			} else {
				for (String userId : realm.getUsersHasRole(role.getId())) {
					if (selectedAccess.contains(userId)) accessValues.add(userId);
				}
			}
		}
		return accessValues;
	}

	private void updateAttachments(AssignmentSupplementItemWithAttachment item, Set<String> attachmentIds) {
		Set<AssignmentSupplementItemAttachment> attachments = item.getAttachmentSet();
		if (attachments == null) {
			attachments = new HashSet<>();
			item.setAttachmentSet(attachments);
		}
		attachments.removeIf(attachment -> !attachmentIds.contains(attachment.getAttachmentId()));
		Set<String> existingIds = new HashSet<>();
		for (AssignmentSupplementItemAttachment attachment : attachments) {
			existingIds.add(attachment.getAttachmentId());
		}
		for (String id : attachmentIds) {
			if (!existingIds.contains(id)) {
				AssignmentSupplementItemAttachment attachment = newAttachment();
				attachment.setAssignmentSupplementItemWithAttachment(item);
				attachment.setAttachmentId(id);
				saveAttachment(attachment);
				attachments.add(attachment);
			}
		}
	}

   /** Dependency: UserDirectoryService */
	protected UserDirectoryService m_userDirectoryService = null;

	/**
	 * Dependency: UserDirectoryService.
	 *
	 * @param service
	 *        The UserDirectoryService.
	 */
	public void setUserDirectoryService(UserDirectoryService service)
	{
		m_userDirectoryService = service;
	}
	
	   /** Dependency: AssignmentService */
	protected AssignmentService m_assignmentService = null;

	/**
	 * Dependency: AssignmentService.
	 * 
	 * @param service
	 *        The AssignmentService.
	 */
	public void setAssignmentService(AssignmentService service)
	{
		m_assignmentService = service;
	}
	
	/** Dependency: AuthzGroupService */
	protected AuthzGroupService m_authzGroupService = null;

	/**
	 * Dependency: AuthzGroupService.
	 * 
	 * @param service
	 *        The AuthzGroupService.
	 */
	public void setAuthzGroupService(AuthzGroupService authzService)
	{
		m_authzGroupService = authzService;
	}
	
	/** Dependency: SiteService */
	protected SiteService m_siteService = null;

	/**
	 * Dependency: SiteService.
	 * 
	 * @param service
	 *        The SiteService.
	 */
	public void setSiteService(SiteService siteService)
	{
		m_siteService = siteService;
	}

	/********************** attachment   ************************/
	/**
	 * {@inheritDoc}
	 */
	public AssignmentSupplementItemAttachment newAttachment()
	{
		return new AssignmentSupplementItemAttachment();
	}
	
	public boolean saveAttachment(AssignmentSupplementItemAttachment attachment)
	{
		try 
		{
			getHibernateTemplate().saveOrUpdate(attachment);
			return true;
		}
		catch (DataAccessException e)
		{
			log.warn("{}.saveModelAnswerQuestion() Hibernate could not save attachment {}", this, attachment.getId(), e);
			return false;
		}
	}
	
	/**
	 * {@inheritDoc}}
	 */
	public List<String> getAttachmentListForSupplementItem(final AssignmentSupplementItemWithAttachment item)
	{	
		HibernateCallback<List<String>> hcb = session -> {
          Query q = session.getNamedQuery("findAttachmentBySupplementItem");
          q.setParameter("item", item);
          return q.list();
        };
	        
	    return getHibernateTemplate().execute(hcb);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean cleanAttachment(AssignmentSupplementItemWithAttachment item)
	{
		boolean rv = true;
		Set<AssignmentSupplementItemAttachment> attachmentSet = item.getAttachmentSet();
		if (attachmentSet != null)
		{
			for (AssignmentSupplementItemAttachment attachment : attachmentSet)
			{
				rv &= removeAttachment(attachment);
			}
		}
		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean removeAttachment(AssignmentSupplementItemAttachment attachment)
	{
		try 
		{
			getHibernateTemplate().delete(getHibernateTemplate().merge(attachment));
			return true;
		}
		catch (DataAccessException e)
		{
			log.warn("{}.removeAttachment() Hibernate could not delete attachment {}", this, attachment.getId(), e);
			return false;
		}
	}

   /*********************** model answer ************************/
	/**
	 * {@inheritDoc}}
	 */
	public AssignmentModelAnswerItem newModelAnswer()
	{
		return new AssignmentModelAnswerItem();
	}
	
	/**
	 * {@inheritDoc}}
	 */
	public boolean saveModelAnswer(AssignmentModelAnswerItem mItem)
	{
		try 
		{
			getHibernateTemplate().saveOrUpdate(mItem);
			return true;
		}
		catch (DataAccessException e)
		{
			log.warn("{}.saveModelAnswerQuestion() Hibernate could not save model answer for assignment {}", this, mItem.getAssignmentId(), e);
			return false;
		}
	}
	
	/**
	 * {@inheritDoc}}
	 */
	public boolean removeModelAnswer(AssignmentModelAnswerItem mItem)
	{

		try 
		{
			getHibernateTemplate().delete(getHibernateTemplate().merge(mItem));
			return true;
		}
		catch (DataAccessException e)
		{
			log.warn("{}.removeModelAnswer() Hibernate could not delete ModelAnswer for assignment {}", this, mItem.getAssignmentId(), e);
			return false;
		}
		
	}
	
	/**
	 * {@inheritDoc}}
	 */
	public AssignmentModelAnswerItem getModelAnswer(String assignmentId)
	{
		List<AssignmentModelAnswerItem> rvList = (List<AssignmentModelAnswerItem>) getHibernateTemplate().findByNamedQueryAndNamedParam("findModelAnswerByAssignmentId", "id", assignmentId);
		if (rvList != null && rvList.size() == 1)
		{
			return rvList.get(0);
		}
		return null;
	}
	
	/*********************** private note *****************/
	/**
	 * {@inheritDoc}}
	 */
	public AssignmentNoteItem newNoteItem()
	{
		return new AssignmentNoteItem();
	}
	
	/**
	 * {@inheritDoc}}
	 */
	public boolean saveNoteItem(AssignmentNoteItem nItem)
	{
		try 
		{
			getHibernateTemplate().saveOrUpdate(nItem);
			return true;
		}
		catch (DataAccessException e)
		{
			log.warn("{}.saveNoteItem() Hibernate could not save private note for assignment {}", this, nItem.getAssignmentId(), e);
			return false;
		}
	}
	
	/**
	 * {@inheritDoc}}
	 */
	public boolean removeNoteItem(AssignmentNoteItem mItem)
	{

		try 
		{
			getHibernateTemplate().delete(getHibernateTemplate().merge(mItem));
			return true;
		}
		catch (DataAccessException e)
		{
			log.warn("{}.removeNoteItem() Hibernate could not delete NoteItem for assignment {}", this, mItem.getAssignmentId(), e);
			return false;
		}
		
	}
	
	/**
	 * {@inheritDoc}}
	 */
	public AssignmentNoteItem getNoteItem(String assignmentId)
	{
		List<AssignmentNoteItem> rvList = (List<AssignmentNoteItem>) getHibernateTemplate().findByNamedQueryAndNamedParam("findNoteItemByAssignmentId", "id", assignmentId);
		if (rvList != null && rvList.size() == 1)
		{
			return rvList.get(0);
		}
		return null;
	}
	
	
	/*********************** all purpose item *****************/
	/**
	 * {@inheritDoc}}
	 */
	public AssignmentAllPurposeItem newAllPurposeItem()
	{
		return new AssignmentAllPurposeItem();
	}
	
	/**
	 * {@inheritDoc}}
	 */
	public boolean saveAllPurposeItem(AssignmentAllPurposeItem nItem)
	{
		try 
		{
			getHibernateTemplate().saveOrUpdate(nItem);
			return true;
		}
		catch (DataAccessException e)
		{
			log.warn("{}.saveAllPurposeItem() Hibernate could not save private AllPurpose for assignment {}", this, nItem.getAssignmentId(), e);
			return false;
		}
	}

	@Override
	public void saveAllPurposeItemWithAccess(AssignmentAllPurposeItem item, Set<String> accessValues)
	{
		AssignmentAllPurposeItem managedItem = getHibernateTemplate().merge(item);
		Set<AssignmentAllPurposeItemAccess> accessSet = managedItem.getAccessSet();
		if (accessSet == null)
		{
			accessSet = new HashSet<>();
			managedItem.setAccessSet(accessSet);
		}

		accessSet.removeIf(access -> !accessValues.contains(access.getAccess()));
		Set<String> remainingAccess = new HashSet<>(accessValues);
		accessSet.forEach(access -> remainingAccess.remove(access.getAccess()));

		for (String value : remainingAccess)
		{
			AssignmentAllPurposeItemAccess access = new AssignmentAllPurposeItemAccess();
			access.setAccess(value);
			access.setAssignmentAllPurposeItem(managedItem);
			getHibernateTemplate().save(access);
			accessSet.add(access);
		}
	}
	
	/**
	 * {@inheritDoc}}
	 */
	public boolean removeAllPurposeItem(AssignmentAllPurposeItem mItem)
	{
		try
		{
			getHibernateTemplate().delete(getHibernateTemplate().merge(mItem));
			return true;
		}
		catch (DataAccessException e)
		{
			log.warn("{}.removeAllPurposeItem() Hibernate could not delete AllPurposeItem for assignment {}", this, mItem.getAssignmentId(), e);
			return false;
		}
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean cleanAllPurposeItemAccess(AssignmentAllPurposeItem mItem)
	{
		boolean rv = false;
		Set<AssignmentAllPurposeItemAccess> accessSet = mItem.getAccessSet();
		if (accessSet != null)
		{
			for (AssignmentAllPurposeItemAccess access : accessSet)
			{
				rv = removeAllPurposeItemAccess(access);
			}
		}
		return rv;
	}
	
	/**
	 * {@inheritDoc}}
	 */
	public AssignmentAllPurposeItem getAllPurposeItem(String assignmentId)
	{
		List<AssignmentAllPurposeItem> rvList = (List<AssignmentAllPurposeItem>) getHibernateTemplate().findByNamedQueryAndNamedParam("findAllPurposeItemByAssignmentId", "id", assignmentId);
		if (rvList != null && rvList.size() == 1)
		{
			return rvList.get(0);
		}
		return null;
	}
	
	/**
	 * {@inheritDoc}}
	 */
	public AssignmentAllPurposeItemAccess newAllPurposeItemAccess()
	{
		return new AssignmentAllPurposeItemAccess();
	}
	
	/**
	 * {@inheritDoc}}
	 */
	public boolean saveAllPurposeItemAccess(AssignmentAllPurposeItemAccess access)
	{
		try 
		{
			getHibernateTemplate().saveOrUpdate(access);
			return true;
		}
		catch (DataAccessException e)
		{
			log.warn("{}.saveAllPurposeItemAccess() Hibernate could not save access {} for {}", this, access.getAccess(), access.getAssignmentAllPurposeItem().getTitle(), e);
			return false;
		}
	}
	
	/**
	 * {@inheritDoc}}
	 */
	public boolean removeAllPurposeItemAccess(AssignmentAllPurposeItemAccess access)
	{

		try 
		{
			getHibernateTemplate().delete(getHibernateTemplate().merge(access));
			return true;
		}
		catch (DataAccessException e)
		{
			log.warn("{}.removeAllPurposeItemAccess() Hibernate could not delete access for all purpose item {} for access {}", this, access.getAssignmentAllPurposeItem().getId(), access.getAccess(), e);
			return false;
		}
	}
	
	/**
	 * {@inheritDoc}}
	 */
	public List<String> getAccessListForAllPurposeItem(final AssignmentAllPurposeItem item)
	{	
		HibernateCallback<List<String>> hcb = session -> {
          Query q = session.getNamedQuery("findAccessByAllPurposeItem");
          q.setParameter("item", item);
          return q.list();
        };
	        
	    return getHibernateTemplate().execute(hcb);
	}

    public boolean canViewModelAnswer(Assignment a, AssignmentSubmission s) {
        if (a != null) {
            AssignmentModelAnswerItem m = getModelAnswer(a.getId());
            if (m != null) {
                if (m_assignmentService.allowGradeSubmission(AssignmentReferenceReckoner.reckoner().assignment(a).reckon().getReference())) {
                    // model answer is viewable to all graders
                    return true;
                } else {
                    Integer show = m.getShowTo();
                    if (show != null) {
                        switch (show) {
                            case AssignmentConstants.MODEL_ANSWER_SHOW_TO_STUDENT_BEFORE_STARTS:
                                return true;
                            case AssignmentConstants.MODEL_ANSWER_SHOW_TO_STUDENT_AFTER_SUBMIT:
                                return s != null && s.getUserSubmission() && s.getSubmitted();
                            case AssignmentConstants.MODEL_ANSWER_SHOW_TO_STUDENT_AFTER_GRADE_RETURN:
                                return s != null && s.getGradeReleased();
                            case AssignmentConstants.MODEL_ANSWER_SHOW_TO_STUDENT_AFTER_ACCEPT_UTIL:
                                return a.getCloseDate().isBefore(Instant.now());
                        }
                    }
                }
            }
        }
        return false;
    }

	/**
	 * {@inheritDoc}
	 */
	public boolean canReadNoteItem(Assignment a, String context)
	{
		if (a != null)
		{
			AssignmentNoteItem note = getNoteItem(a.getId());
			if (note != null)
			{
				User u = m_userDirectoryService.getCurrentUser();
				String noteCreatorId = note.getCreatorId();
				if (noteCreatorId.equals(u.getId()))
				{
					return true;
				}
				else if (m_assignmentService.allowGradeSubmission(m_assignmentService.createAssignmentEntity(a.getId()).getReference()))
				{
					// check whether the instructor type can view the note
					int share = note.getShareWith();
					if (share == AssignmentConstants.NOTE_READ_BY_OTHER || share == AssignmentConstants.NOTE_READ_AND_WRITE_BY_OTHER)
					{
						return true;
					}	
				}
			}
		}
		else
		{
			if (m_assignmentService.allowAddAssignment(context))
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean canEditNoteItem(Assignment a)
	{
		String userId = "";
		User u = m_userDirectoryService.getCurrentUser();
		if ( u != null)
		{
			userId = u.getId();
		}
		
		if (a != null)
		{
			AssignmentNoteItem note = getNoteItem(a.getId());
			if (note != null)
			{
				if (note.getCreatorId().equals(userId))
				{
					// being creator can edit
					return true;
				}
				else if (note.getShareWith() == AssignmentConstants.NOTE_READ_AND_WRITE_BY_OTHER && m_assignmentService.allowGradeSubmission(m_assignmentService.createAssignmentEntity(a.getId()).getReference()))
				{
					return true;
				}		
			}
			else
			{
				return true;
			}
		}
		else
		{
			return true;
		}
		
		return false;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean canViewAllPurposeItem(Assignment a)
	{
		boolean rv = false;
		
		if (a != null)
		{
			AssignmentAllPurposeItem aItem = getAllPurposeItem(a.getId());
			if (aItem != null)
			{
				if (!aItem.getHide())
				{
					Instant now = Instant.now();
					Date releaseDate = aItem.getReleaseDate();
					Date retractDate = aItem.getRetractDate();
					
					if (releaseDate == null && retractDate == null)
					{
						// no time limitation on showing the item
						rv = true;
					}
					else if (releaseDate != null && retractDate == null)
					{
						// has relase date but not retract date
						rv = now.toEpochMilli() > releaseDate.getTime();
					}
					else if (releaseDate == null && retractDate != null)
					{
						// has retract date but not release date
						rv = now.toEpochMilli() < retractDate.getTime();
					}
					else if (now != null)
					{
						// both releaseDate and retract date are not null
						// has both release and retract dates
						rv = now.toEpochMilli() > releaseDate.getTime() && now.toEpochMilli() < retractDate.getTime();
					}
				}
				else
				{
					rv = false;
				}
			}
			
			if (rv)
			{
				// reset rv
				rv = false;
				
				// need to check role/user permission only if the above time test returns true
				List<String> access = getAccessListForAllPurposeItem(aItem);
				User u = m_userDirectoryService.getCurrentUser();
				if ( u != null)
				{
					if (access.contains(u.getId()))
						rv = true;
					else 
					{
						try
						{
							String role = m_authzGroupService.getUserRole(u.getId(), m_siteService.siteReference(a.getContext()));
							if (access.contains(role))
								rv = true;
						}
						catch (Exception e)
						{
						 log.warn(this + ".callViewAllPurposeItem() Hibernate cannot access user role for user id= " + u.getId());
							return rv;
						}
						
					}
						
				}
			}
		}
		
		return rv;
	}
}
