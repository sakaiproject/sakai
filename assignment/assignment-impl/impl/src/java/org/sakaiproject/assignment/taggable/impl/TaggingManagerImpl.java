/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007 The Sakai Foundation.
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

package org.sakaiproject.assignment.taggable.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.assignment.taggable.api.TaggableActivity;
import org.sakaiproject.assignment.taggable.api.TaggableItem;
import org.sakaiproject.assignment.taggable.api.TaggingManager;
import org.sakaiproject.assignment.taggable.api.TaggableActivityProducer;
import org.sakaiproject.assignment.taggable.api.TaggingProvider;

public class TaggingManagerImpl implements TaggingManager {

	private static final Log logger = LogFactory
			.getLog(TaggingManagerImpl.class);

	protected List<TaggableActivityProducer> taggableActivityProducers = new ArrayList<TaggableActivityProducer>();

	protected List<TaggingProvider> taggingProviders = new ArrayList<TaggingProvider>();

	public void init() {
		logger.info("init()");
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

	public TaggableItem getItem(String itemRef, TaggingProvider provider) {
		return findProducerByRef(itemRef).getItem(itemRef, provider);
	}

	public List<TaggableItem> getItems(String activityRef,
			TaggingProvider provider) {
		List<TaggableItem> items = new ArrayList<TaggableItem>();
		TaggableActivityProducer producer = findProducerByRef(activityRef);
		if (producer != null) {
			items = producer.getItems(getActivity(activityRef, provider),
					provider);
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
}
