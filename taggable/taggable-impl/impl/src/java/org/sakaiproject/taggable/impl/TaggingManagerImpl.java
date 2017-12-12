/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/syracuse/taggable/branches/oncourse_osp_enhancements/taggable-impl/impl/src/java/org/sakaiproject/taggable/impl/TaggingManagerImpl.java $
 * $Id: TaggingManagerImpl.java 46822 2008-03-17 16:19:47Z chmaurer@iupui.edu $
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

package org.sakaiproject.taggable.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.taggable.api.Link;
import org.sakaiproject.taggable.api.LinkManager;
import org.sakaiproject.taggable.api.Tag;
import org.sakaiproject.taggable.api.TagColumn;
import org.sakaiproject.taggable.api.TagList;
import org.sakaiproject.taggable.api.TaggableActivity;
import org.sakaiproject.taggable.api.TaggableItem;
import org.sakaiproject.taggable.api.TaggingHelperInfo;
import org.sakaiproject.taggable.api.TaggingManager;
import org.sakaiproject.taggable.api.TaggableActivityProducer;
import org.sakaiproject.taggable.api.TaggingProvider;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;

@Slf4j
public class TaggingManagerImpl implements TaggingManager {

	protected List<TaggableActivityProducer> taggableActivityProducers = new ArrayList<TaggableActivityProducer>();

	protected List<TaggingProvider> taggingProviders = new ArrayList<TaggingProvider>();
	
	private LinkManager linkManager;

	public void init() {
		log.info("init()");
	}

	public TaggableActivityProducer findProducerByRef(String ref) {
		TaggableActivityProducer producer;
		for (Iterator<TaggableActivityProducer> i = taggableActivityProducers
				.iterator(); i.hasNext();) {
			producer = i.next();
			if (producer.checkReference(ref)) {
				return producer;
			}
		}
		return null;
	}

	public String getContext(String ref) {
		return findProducerByRef(ref).getContext(ref);
	}

	public TaggableActivityProducer findProducerById(String id) {
		TaggableActivityProducer producer = null;
		for (TaggableActivityProducer p : taggableActivityProducers) {
			if (p.getId().equals(id)) {
				producer = p;
			}
		}
		return producer;
	}

	public TaggingProvider findProviderById(String id) {
		TaggingProvider provider = null;
		for (TaggingProvider p : taggingProviders) {
			if (p.getId().equals(id)) {
				provider = p;
			}
		}
		return provider;
	}

	public List<TaggableActivity> getActivities(String context,
			TaggingProvider provider) {
		List<TaggableActivity> activities = new ArrayList<TaggableActivity>();
		for (TaggableActivityProducer producer : taggableActivityProducers) {
			activities.addAll(producer.getActivities(context, provider));
		}
		return activities;
	}

	public TaggableActivity getActivity(String activityRef,
			TaggingProvider provider) {
		TaggableActivity activity = null;
		TaggableActivityProducer producer = findProducerByRef(activityRef);
		if (producer != null) {
			activity = producer.getActivity(activityRef, provider);
		}
		return activity;
	}

	public List<TaggableActivityProducer> getProducers() {
		return taggableActivityProducers;
	}

	public List<TaggingProvider> getProviders() {
		return taggingProviders;
	}

	public TaggableItem getItem(String itemRef, TaggingProvider provider, boolean getMyItemOnly, String taggedItem) {
		return findProducerByRef(itemRef).getItem(itemRef, provider, getMyItemOnly, taggedItem);
	}

	public List<TaggableItem> getItems(String activityRef,
			TaggingProvider provider, boolean getMyItemsOnly, String taggedItem) {
		List<TaggableItem> items = new ArrayList<TaggableItem>();
		TaggableActivityProducer producer = findProducerByRef(activityRef);
		if (producer != null) {
			items = producer.getItems(getActivity(activityRef, provider),
					provider, getMyItemsOnly, taggedItem);
		}
		return items;
	}

	public void registerProducer(TaggableActivityProducer producer) {
		taggableActivityProducers.add(producer);
	}

	public void registerProvider(TaggingProvider provider) {
		taggingProviders.add(provider);
	}

	public boolean isTaggable() {
		return !taggingProviders.isEmpty();
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * This method calls
	 * {@link #addLink(String, Goal, String, String, boolean, boolean)} with
	 * locked set to false.
	 */
	public Link addLink(String activityRef, String tagCriteriaRef, String rationale,
			String rubric, boolean visible) throws PermissionException {
		return addLink(activityRef, tagCriteriaRef, rationale, rubric, visible, false);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * A {@link RuntimeException} will be thrown if the goal set of the given
	 * goal is not published (see {@link GoalSet#isPublished()}).
	 * 
	 * {@link #SECURE_MODIFY_LINKS_FROM} is checked in the activity context and
	 * {@link #SECURE_MODIFY_LINKS_TO} is checked in the goal context. If locked
	 * is true, {@link #SECURE_LOCK_LINKS} is also checked in in the activity
	 * context.
	 */
	public Link addLink(String activityRef, String tagCriteriaRef, String rationale,
			String rubric, boolean visible, boolean locked)
			throws PermissionException {
		//if (!tagCriteria.isPublished()) {
		//	throw new RuntimeException(
		//			"Trying to create a link to a goal in an unpublished goal set!");
		//}
		//TODO TEMPORARY PERM BYPASS
		/*
		if (locked) {
			unlock(SECURE_LOCK_LINKS, gmtReference(null, tagCriteria.getContext(), null));
		}
		unlock(SECURE_MODIFY_LINKS_FROM, gmtReference(null, getContext(activityRef), null));
		unlock(SECURE_MODIFY_LINKS_TO, gmtReference(null, tagCriteria.getContext(), null));
		*/
		return linkManager.persistLink(activityRef, tagCriteriaRef, rationale, rubric,
				visible, locked);
	}
	
	public TaggingHelperInfo createTaggingHelperInfoObject(String helperId, String name,
			String description, Map<String, ? extends Object> parameterMap,
			TaggingProvider provider) {
		return new TaggingHelperInfoImpl(helperId, name, description, parameterMap, provider);
	}
	
	public TagList createTagList() {
		return new TagListImpl();
	}
	
	public TagList createTagList(List<TagColumn> columns)
	{
		return new TagListImpl(columns);
	}
	
	public Tag createTag(Link link) {
		return new TagImpl(link);
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * Permission is checked against the producer of the activity via
	 * {@link TaggableActivityProducer#allowRemoveTags(TaggableActivity)}.
	 */
	public void removeLinks(TaggableActivity activity)
			throws PermissionException
	{
		TaggableActivityProducer producer = activity.getProducer();
		if (producer.allowRemoveTags(activity)) {
			getLinkManager().removeLinks(activity.getReference());
		}
		else {
			throw new PermissionException(getUser().getEid(), producer
					.getClass().getName()
					+ ".allowRemoveTags()", activity.getReference());
		}
	}
	
	public TagColumn createTagColumn(String name, String displayName, String description,
			boolean sortable)
	{
		return new TagColumnImpl(name, displayName, description, sortable);
	}

	protected User getUser() {
		return UserDirectoryService.getCurrentUser();
	}

	public LinkManager getLinkManager()
	{
		return linkManager;
	}

	public void setLinkManager(LinkManager linkManager)
	{
		this.linkManager = linkManager;
	}
	
}
