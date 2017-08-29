/**
 * Copyright (c) 2003-2013 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.citation.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sakaiproject.citation.api.Citation;
import org.sakaiproject.citation.api.CitationCollection;
import org.sakaiproject.citation.api.CitationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.exception.EntityNotFoundException;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.user.api.UserDirectoryService;

/**
 * Citations service is built on top of resources. All permissions checks are
 * handled by resources (ContentHostingService). Nothing that accepts the
 * Citations List ID should be exposed as it would allow security checks to be
 * bypassed.
 * 
 */
public class CitationEntityProvider extends AbstractEntityProvider implements
		AutoRegisterEntityProvider, ActionsExecutable, Outputable {

	private CitationService citationService;
	private ContentHostingService contentHostingService;
	private UserDirectoryService userDirectoryService;

	public void setCitationService(CitationService citationService) {
		this.citationService = citationService;
	}

	public void setContentHostingService(
			ContentHostingService contentHostingService) {
		this.contentHostingService = contentHostingService;
	}

	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}

	public String getEntityPrefix() {
		return "citation";
	}

	@EntityCustomAction(action = "list", viewKey = EntityView.VIEW_LIST)
	public DecoratedCitationCollection getCitationList(EntityView view,
			Map<String, Object> params) {
		StringBuilder resourceId = new StringBuilder();
		String[] segments = view.getPathSegments();
		for (int i = 2; i < segments.length; i++) {
			resourceId.append("/");
			resourceId.append(segments[i]);
		}
		if (resourceId.length() == 0) {
			throw new EntityNotFoundException(
					"You must supply a path to the citation list.", null);
		}
		try {
			ContentResource resource = contentHostingService
					.getResource(resourceId.toString());

			if (!CitationService.CITATION_LIST_ID.equals(resource
					.getResourceType())) {
				throw new EntityException("Not a citation list",
						resourceId.toString(), 400);
			}
			if (resource.getContentLength() > 1024) {
				throw new EntityException("Bad citation list",
						resourceId.toString(), 400);
			}
			String citationCollectionId = new String(resource.getContent());
			CitationCollection collection = citationService
					.getCollection(citationCollectionId);

			ResourceProperties properties = resource.getProperties();

			String title = properties
					.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
			String description = properties
					.getProperty(ResourceProperties.PROP_DESCRIPTION);

			DecoratedCitationCollection dCollection = new DecoratedCitationCollection(
					collection.getId(), title, description);

			for (Citation citation : (List<Citation>) collection.getCitations()) {
				dCollection.addCitation(new DecoratedCitation(
						citation.getId(), citation.getSchema().getIdentifier(),
						citation.getCitationProperties()));
			}
			return dCollection;
		} catch (PermissionException e) {
			if (userDirectoryService.getAnonymousUser().equals(userDirectoryService.getCurrentUser())) {
				throw new EntityException("Login required", resourceId.toString(), 401);
			} else {
				throw new EntityException("Permission denied", resourceId.toString(), 403);
			}
		} catch (IdUnusedException e) {
			throw new EntityException("Not found", resourceId.toString(), 404);
		} catch (TypeException e) {
			throw new EntityException("Wrong type", resourceId.toString(), 400);
		} catch (ServerOverloadException e) {
			throw new EntityException("Server Overloaded",
					resourceId.toString(), 500);
		}

	}

	//
	/**
	 * This wraps fields from a citation for entity broker.
	 * 
	 * @author buckett
	 * 
	 */
	public class DecoratedCitation {
		private String type;
		private Map<String, String> values;
		private String id;

		public DecoratedCitation(String id, String type, Map<String, String> values) {
			this.id = id;
			this.type = type;
			this.values = values;
		}
		
		public String getId() {
			return id;
		}

		public String getType() {
			return type;
		}

		public Map<String, String> getValues() {
			return values;
		}
	}

	public class DecoratedCitationCollection {
		private String id;
		private String name;
		private String description;
		private List<DecoratedCitation> citations;

		public DecoratedCitationCollection(String id, String name, String description) {
			this.id = id;
			this.name = name;
			this.description = description;
			this.citations = new ArrayList<DecoratedCitation>();
		}

		public void addCitation(DecoratedCitation citation) {
			citations.add(citation);
		}
		
		public String getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public String getDescription() {
			return description;
		}

		public List<DecoratedCitation> getCitations() {
			return citations;
		}
	}

	public String[] getHandledOutputFormats() {
		return new String[] { JSON, FORM, HTML, XML, TXT };
	}

}
