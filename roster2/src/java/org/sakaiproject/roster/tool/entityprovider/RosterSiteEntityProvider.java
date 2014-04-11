/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational
* Community License, Version 2.0 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of the
* License at:
*
* http://opensource.org/licenses/ecl2.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.roster.tool.entityprovider;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.roster.api.RosterMember;
import org.sakaiproject.roster.api.RosterMemberComparator;
import org.sakaiproject.roster.api.SakaiProxy;
import org.sakaiproject.roster.impl.SakaiProxyImpl;

/**
 * <code>EntityProvider</code> to allow Roster to access site, membership, and
 * enrollment data for the current user. The provider respects Roster
 * permissions, so shouldn't expose any data the current user should not have
 * access to.
 * 
 * @author d.b.robinson@lancaster.ac.uk
 */
public class RosterSiteEntityProvider extends AbstractEntityProvider implements
		AutoRegisterEntityProvider, ActionsExecutable, Outputable {

	@SuppressWarnings("unused")
	private static final Log log = LogFactory.getLog(RosterSiteEntityProvider.class);
	
	public final static String ENTITY_PREFIX		= "roster-membership";
	public final static String DEFAULT_ID			= ":ID:";
	
	public final static String ERROR_INVALID_SITE	= "Invalid site ID";
	
	// key passed as parameters
	public final static String KEY_SORTED						= "sorted";
	public final static String KEY_SORT_FIELD					= "sortField";
	public final static String KEY_SORT_DIRECTION				= "sortDirection";
	public final static String KEY_GROUP_ID						= "groupId";
	public final static String KEY_ENROLLMENT_SET_ID			= "enrollmentSetId";
	public final static String KEY_INCLUDE_CONNECTION_STATUS	= "includeConnectionStatus";
	
	// defaults
	public final static boolean DEFAULT_SORTED			= false;
	public final static String DEFAULT_SORT_FIELD		= RosterMemberComparator.SORT_NAME;
	public final static int DEFAULT_SORT_DIRECTION		= RosterMemberComparator.SORT_ASCENDING;
	
	private SakaiProxy sakaiProxy;
	
	public RosterSiteEntityProvider() {
		sakaiProxy = SakaiProxyImpl.instance();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}
	
	@EntityCustomAction(action = "get-membership", viewKey = EntityView.VIEW_SHOW)
	public Object getMembership(EntityReference reference, Map<String, Object> parameters) {

		if (null == reference.getId() || DEFAULT_ID.equals(reference.getId())) {
			throw new EntityException(ERROR_INVALID_SITE, reference.getReference());
		}

		String groupId = null;
		if (parameters.containsKey(KEY_GROUP_ID)) {
			groupId = parameters.get(KEY_GROUP_ID).toString();
		}
		
		Boolean includeConnectionStatus = false;
		if (parameters.containsKey(KEY_INCLUDE_CONNECTION_STATUS)) {
			includeConnectionStatus = Boolean.valueOf(parameters.get(
					KEY_INCLUDE_CONNECTION_STATUS).toString());
		}

		List<RosterMember> membership = null;
		// if no group ID specified, retrieve site membership, else retrieve group
		if (null == groupId) {
			membership = sakaiProxy.getSiteMembership(reference.getId(), includeConnectionStatus);
		} else {
			membership = sakaiProxy.getGroupMembership(reference.getId(), groupId);
		}
		
		if (null == membership) {
			throw new EntityException("Unable to retrieve membership", reference.getReference());
		}

		if (true == getSortedValue(parameters)) {

			Collections.sort(membership, new RosterMemberComparator(
					getSortFieldValue(parameters),
					getSortDirectionValue(parameters), sakaiProxy
							.getFirstNameLastName()));
		}

		return membership;
	}
			
	@EntityCustomAction(action = "get-site", viewKey = EntityView.VIEW_SHOW)
	public Object getSite(EntityReference reference) {
		
		if (null == reference.getId() || DEFAULT_ID.equals(reference.getId())) {
			throw new EntityException(ERROR_INVALID_SITE, reference.getReference());
		}
		
		return sakaiProxy.getRosterSite(reference.getId());
	}
		
	@EntityCustomAction(action = "get-enrollment", viewKey = EntityView.VIEW_SHOW)
	public Object getEnrollment(EntityReference reference, Map<String, Object> parameters) {
		
		if (null == reference.getId() || DEFAULT_ID.equals(reference.getId())) {
			throw new EntityException(ERROR_INVALID_SITE, reference.getReference());
		}
		
		String enrollmentSetId = null;
		if (parameters != null && parameters.containsKey(KEY_ENROLLMENT_SET_ID)) {
			enrollmentSetId = parameters.get(KEY_ENROLLMENT_SET_ID).toString();
		}
		
		return sakaiProxy.getEnrollmentMembership(reference.getId(), enrollmentSetId);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String[] getHandledOutputFormats() {
		return new String[] { Formats.JSON };
	}
	
	private Boolean getSortedValue(Map<String, Object> parameters) {
		
		if (null != parameters.get(KEY_SORTED)) {
			return Boolean.parseBoolean(parameters.get(KEY_SORTED).toString());
		} else {
			return DEFAULT_SORTED;
		}
	}
	
	private int getSortDirectionValue(Map<String, Object> parameters) {

		if (null != parameters.get(KEY_SORT_DIRECTION)) {
			return Integer.parseInt(parameters.get(KEY_SORT_DIRECTION)
					.toString());
		} else {
			return DEFAULT_SORT_DIRECTION;
		}
		
	}
	
	private String getSortFieldValue(Map<String, Object> parameters) {
		String sortField;
		if (null != parameters.get(KEY_SORT_FIELD)) {
			sortField = parameters.get(KEY_SORT_FIELD).toString();
		} else {
			sortField = DEFAULT_SORT_FIELD;
		}
		return sortField;
	}
}
