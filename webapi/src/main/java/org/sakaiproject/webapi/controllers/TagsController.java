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

import org.sakaiproject.tags.api.Tag;
import org.sakaiproject.tags.api.TagService;
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

	@GetMapping(value = "/tags/items/{itemId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public Iterable<Tag> getTagsForItem(@PathVariable String itemId) {
		checkSakaiSession();

		return tagService.getAssociatedTagsForItem(itemId);
	}
	
	@GetMapping(value = "/tags/{siteId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public Iterable<Tag> getTagsForSite(@PathVariable String siteId) {
		checkSakaiSession();

		return tagService.getTags().getAllInCollection(siteId);
	}
}
