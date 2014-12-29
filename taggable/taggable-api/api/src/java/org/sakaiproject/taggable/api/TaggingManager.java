/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/syracuse/taggable/branches/oncourse_osp_enhancements/taggable-api/api/src/java/org/sakaiproject/taggable/api/TaggingManager.java $
 * $Id: TaggingManager.java 46822 2008-03-17 16:19:47Z chmaurer@iupui.edu $
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

package org.sakaiproject.taggable.api;

import java.util.List;
import java.util.Map;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.taggable.api.Link;

/**
 * The TaggingManager provides access to available
 * {@link TaggableActivityProducer TaggableActivityProducers} and
 * {@link TaggingProvider TaggingProviders} and the various activities and tags
 * they provide.
 * 
 * @author The Sakai Foundation.
 */
public interface TaggingManager {
	
	/** String for the application id. */
	public static final String APPLICATION_ID = "taggable";

	/** String to identify link references. */
	public static final String LINK_REF = "l";

	public static final String SERVICE_NAME = TaggingManager.class.getName();

	/** This string starts the references to resources in this service. */
	public static final String REFERENCE_ROOT = Entity.SEPARATOR
			+ APPLICATION_ID;
	
	/**
	 * Security lock for modifying links. Checked in activity context and
	 * requires goal context counterpart.
	 */
	public static final String SECURE_MODIFY_LINKS_FROM = APPLICATION_ID
			+ ".modify_links_from";

	/**
	 * Security lock for modifying links. Checked in goal context and requires
	 * activity context counterpart.
	 */
	public static final String SECURE_MODIFY_LINKS_TO = APPLICATION_ID
			+ ".modify_links_to";
	
	/** Security lock for locking links. Checked in activity context. */
	public static final String SECURE_LOCK_LINKS = APPLICATION_ID
			+ ".lock_links";

	/**
	 * Method to get a taggable activity producer by identifier.
	 * 
	 * @param id
	 *            A unique producer identifier.
	 * @return A taggable activity producer.
	 */
	public TaggableActivityProducer findProducerById(String id);

	/**
	 * Method to get the taggable activity producer that handles this reference.
	 * 
	 * @param ref
	 *            A reference to an object provided by a taggable activity
	 *            producer.
	 * @return A taggable activity producer.
	 */
	public TaggableActivityProducer findProducerByRef(String ref);

	/**
	 * Method to get a tagging provider by identifier.
	 * 
	 * @param id
	 *            A unique provider identifier.
	 * @return A tagging provider.
	 */
	public TaggingProvider findProviderById(String id);

	/**
	 * Method for a taggable activity producer to register itself with the
	 * tagging manager.
	 * 
	 * @param producer
	 *            A taggable activity producer.
	 */
	public void registerProducer(TaggableActivityProducer producer);

	/**
	 * Method for a tagging provider to register itself with this tagging
	 * manager.
	 * 
	 * @param provider
	 *            A tagging provider.
	 */
	public void registerProvider(TaggingProvider provider);

	/**
	 * Method to get the context of the object represented by this reference.
	 * 
	 * @param ref
	 *            A reference for an object produced by a taggable activity
	 *            producer.
	 * @return The context of the referenced object.
	 */
	public String getContext(String ref);

	/**
	 * Method to get a list of all taggable activities within the given context.
	 * 
	 * @param context
	 *            The context to search.
	 * @param provider
	 *            The provider that is getting the activities. This allows the
	 *            producers to selectively return different lists of activities
	 *            depending on the given provider.
	 * @return A list, possibly empty, of all taggable activities within the
	 *         given context that are accessible by the given tagging provider.
	 */
	public List<TaggableActivity> getActivities(String context,
			TaggingProvider provider);
	
	
	//public List<TaggableActivity> getActivities(String criteriaRef, TaggingProvider provider);

	/**
	 * Method to get a taggable activity by reference string.
	 * 
	 * @param activityRef
	 *            A reference for the taggable activity.
	 * @param provider
	 *            The provider that is getting the activity. This allows the
	 *            producer to selectively return an activity, or none at all,
	 *            depending on the given provider.
	 * @return The taggable activity, or null if no such activity exists or the
	 *         provider cannot access it.
	 */
	public TaggableActivity getActivity(String activityRef,
			TaggingProvider provider);

	/**
	 * Method to get a list of the currently registered taggable activity
	 * producers.
	 * 
	 * @return List of taggable activity producers.
	 */
	public List<TaggableActivityProducer> getProducers();

	/**
	 * Method to get a list of the currently registered tagging providers.
	 * 
	 * @return List of tagging providers.
	 */
	public List<TaggingProvider> getProviders();

	/**
	 * Method to get a taggable item by reference string.
	 * 
	 * @param itemRef
	 *            The reference for the taggable item.
	 * @param provider
	 *            The provider that is getting the item. This allows the
	 *            producer to selectively return an item, or none at all,
	 *            depending on the given provider.
	 * @param getMyItemsOnly
	 * 			  This flag will return only items where the owner matches userId        
	 * @param taggedItem Reference to an item that can be used for additional permission lookups      
	 * @return The taggable item, or null if no such item exists or the provider
	 *         cannot access it.
	 */
	public TaggableItem getItem(String itemRef, TaggingProvider provider, boolean getMyItemOnly, String taggedItem);

	/**
	 * Method to get a list of taggable items for the activity identified by the
	 * given activity reference.
	 * 
	 * @param activityRef
	 *            The reference for the taggable activity.
	 * @param provider
	 *            The provider that is getting the items. This allows the
	 *            producers to selectively return different lists of items for
	 *            the referenced activity depending on the given provider.
	 * @param getMyItemsOnly
	 * 			  This flag will return only items where the owner matches userId    
	 * @param taggedItem Reference to an item that can be used for additional permission lookups        
	 * @return A list, possibly empty, of taggable items.
	 */
	public List<TaggableItem> getItems(String activityRef,
			TaggingProvider provider, boolean getMyItemsOnly, String taggedItem);

	/**
	 * Method to determine if there are any tagging providers available.
	 * 
	 * @return True if a tagging provider is available, false otherwise.
	 */
	public boolean isTaggable();
	
	/**
	 * Method to add a new link. A {@link RuntimeException} will be thrown if
	 * the goal set of the given goal is not published (see
	 * {@link GoalSet#isPublished()}).
	 * 
	 * @param activityRef
	 *            A reference for the activity to which this link is being
	 *            created.
	 * @param tagCriteriaRef
	 *            The tagCriteriaRef to which this link is being created. It must belong
	 *            to a published parent.
	 * @param rationale
	 *            The rationale for creating this link.
	 * @param rubric
	 *            Some rubric value.
	 * @param visible
	 *            True if this link should be made visible to those with
	 *            appropriate permissions, false otherwise.
	 * @return The link that has been added, or null on error.
	 * @throws PermissionException
	 *             Exception thrown if current user doesn't have permission to
	 *             perform this action.
	 */
	public Link addLink(String activityRef, String tagCriteriaRef, String rationale,
			String rubric, boolean visible) throws PermissionException;

	/**
	 * Method to add a new link.
	 * 
	 * @param activityRef
	 *            A reference for the activity to which this link is being
	 *            created.
	 * @param tagCriteriaRef
	 *            The tagCriteriaRef to which this link is being created. It must belong
	 *            to a published parent.
	 * @param rationale
	 *            The rationale for creating this link.
	 * @param rubric
	 *            Some rubric value.
	 * @param visible
	 *            True if this link should be made visible to those with
	 *            appropriate permissions, false otherwise.
	 * @param locked
	 *            True if this link should be made locked to those with
	 *            appropriate permissions, false otherwise.
	 * @return The link that has been added, or null on error.
	 * @throws PermissionException
	 *             Exception thrown if current user doesn't have permission to
	 *             perform this action in the given context.
	 */
	public Link addLink(String activityRef, String tagCriteriaRef, String rationale,
			String rubric, boolean visible, boolean locked)
			throws PermissionException;
	
	
	public TaggingHelperInfo createTaggingHelperInfoObject(String helperId, String name,
			String description, Map<String, ? extends Object> parameterMap,
			TaggingProvider provider);
	
	public TagList createTagList();
	
	public TagList createTagList(List<TagColumn> columns);
	
	public Tag createTag(Link link);
	
	public void removeLinks(TaggableActivity activity) throws PermissionException;
	
	public TagColumn createTagColumn(String name, String displayName,
			String description, boolean sortable);
}
