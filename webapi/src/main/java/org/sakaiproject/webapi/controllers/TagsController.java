/******************************************************************************
 * Copyright 2023 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.webapi.controllers;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tags.api.Tag;
import org.sakaiproject.tags.api.TagService;
import org.sakaiproject.webapi.exception.ForbiddenAccessException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class TagsController extends AbstractSakaiApiController {

	@Resource
	private TagService tagService;
	@Resource
	private SecurityService securityService;

	@GetMapping(value = "/sites/{siteId}/tools/{tool}/tags/{collectionId}/items/{itemId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public Iterable<Tag> getTagsForItem(@PathVariable String siteId, @PathVariable String tool, @PathVariable String collectionId, @PathVariable String itemId) {
		checkSakaiSession();
		checkAccess(siteId, tool);

		return tagService.getAssociatedTagsForItem(collectionId, itemId);
	}
	
	@GetMapping(value = "/sites/{siteId}/tools/{tool}/tags/{collectionId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public Iterable<Tag> getTagsForCollection(@PathVariable String siteId, @PathVariable String tool, @PathVariable String collectionId) {
		checkSakaiSession();
		checkAccess(siteId, tool);

		return tagService.getTags().getAllInCollection(collectionId);
	}

	private void checkAccess(String siteId, String tool) {
		if (StringUtils.isNotEmpty(siteId) && StringUtils.isNotEmpty(tool)) {
			if (securityService.unlock(TagService.TAGSERVICE_MANAGE_PERMISSION, "/site/" + siteId)) {
				return;
			}
			if (tool.equals(TagService.TOOL_ASSIGNMENTS)) {
				Site site = checkSite(siteId);
				ToolConfiguration tc = site.getToolForCommonId(AssignmentConstants.TOOL_ID);
				String optionTagsValue = tc.getPlacementConfig().getProperty(AssignmentConstants.SHOW_TAGS_STUDENT);
				if (Boolean.TRUE.equals(optionTagsValue)) {
					return;
				}
			}
		}
		throw new ForbiddenAccessException();
	}

}
