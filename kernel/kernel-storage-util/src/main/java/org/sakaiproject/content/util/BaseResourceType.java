/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.content.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ExpandableResourceType;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.i18n.InternationalizedMessages;
import org.sakaiproject.user.api.User;
import org.sakaiproject.util.ResourceLoader;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public abstract class BaseResourceType implements ResourceType
{
	protected static final String DEFAULT_RESOURCECLASS = "org.sakaiproject.localization.util.TypeProperties";
	protected static final String DEFAULT_RESOURCEBUNDLE = "org.sakaiproject.localization.bundle.type.types";
	protected static final String RESOURCECLASS = "resource.class.type";
	protected static final String RESOURCEBUNDLE = "resource.bundle.type";

	@Setter private InternationalizedMessages messageSource;

	final protected EnumMap<ResourceToolAction.ActionType, List<ResourceToolAction>> actionMap;
	final protected Map<String, ResourceToolAction> actions;

	public BaseResourceType() {
		actionMap = new EnumMap<>(ResourceToolAction.ActionType.class);
		actions = new HashMap<>();
	}

	@Override
	public boolean hasAvailabilityDialog() 
	{
		return true;
	}

	@Override
	public boolean hasDescription() 
	{
		return true;
	}

	@Override
	public boolean hasGroupsDialog() 
	{
		return true;
	}

	@Override
	public boolean hasNotificationDialog() 
	{
		return true;
	}

	@Override
	public boolean hasOptionalPropertiesDialog() 
	{
		return true;
	}

	@Override
	public boolean hasPublicDialog() 
	{
		return true;
	}

	@Override
	public boolean hasRightsDialog() 
	{
		return true;
	}

	@Override
	public String getIconLocation(ContentEntity entity) {
		return null;
	}

	@Override
	public String getIconClass(ContentEntity entity) {
		return null;
	}

	@Override
	public boolean isExpandable()
    {
	    return (this instanceof ExpandableResourceType);
    }	

	/**
	 * Returns null to indicate that the Resources tool should display the byte count 
	 * or member count for the entity (depending on whether the entity is a 
	 * ContentResource or ContentCollection). If a different measure of the "size" of 
	 * the entity is needed, overrid this method to return a short string (no more than 
	 * 25 characters) describing the "size" of the entity as appropriate.
	 */
	@Override
	public String getSizeLabel(ContentEntity entity)
	{
		return null;
	}

	/**
	 * Returns null to indicate that the Resources tool should display the byte count 
	 * or member count for the entity (depending on whether the entity is a 
	 * ContentResource or ContentCollection). If a different measure of the "size" of 
	 * the entity is needed, overrid this method to return a short string (no more than 
	 * 80 characters) describing the "size" of the entity as appropriate.
	 */
	@Override
	public String getLongSizeLabel(ContentEntity entity)
	{
		return null;
	}

	@Override
	public ResourceToolAction getAction(String actionId) {
		return actions.get(actionId);
	}

	@Override
	public List<ResourceToolAction> getActions(ResourceToolAction.ActionType type) {
		List<ResourceToolAction> list = actionMap.computeIfAbsent(type, k -> new ArrayList<>());
		return Collections.unmodifiableList(list);
	}

	@Override
	public List<ResourceToolAction> getActions(List<ResourceToolAction.ActionType> types) {
		List<ResourceToolAction> list = new ArrayList<>();
		if (types != null) {
			for (ResourceToolAction.ActionType type : types) {
				list.addAll(actionMap.computeIfAbsent(type, k -> new ArrayList<>()));
			}
		}
		return Collections.unmodifiableList(list);
	}

	public Collection<ResourceToolAction> getActions(Reference entityRef, Set permissions) {
		// TODO: use entityRef to filter actions
		return Collections.unmodifiableCollection(actions.values());
	}

	public Collection<ResourceToolAction> getActions(Reference entityRef, User user, Set permissions) {
		// TODO: use entityRef and user to filter actions
		return Collections.unmodifiableCollection(actions.values());
	}

	protected BaseResourceAction.Localizer localizer(final String key) {
		if (messageSource != null) {
			return () -> messageSource.getString(key);
		}
		log.warn("MessageSource has not been initialized for this ResourceType, ");
		return null;
	}

	protected BaseResourceAction.FormattedLocalizer formattedLocalizer(final String key, Object[] args) {
		if (messageSource != null) {
			return () -> messageSource.getFormattedMessage(key, args);
		}
		log.warn("MessageSource has not been initialized for this ResourceType, ");
		return null;
	}
}
