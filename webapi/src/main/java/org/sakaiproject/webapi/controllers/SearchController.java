/******************************************************************************
 * Copyright 2015 sakaiproject.org Licensed under the Educational
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

import org.sakaiproject.search.api.InvalidSearchQueryException;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.webapi.beans.SearchRestBean;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class SearchController extends AbstractSakaiApiController {

	@Autowired private SearchService searchService;
	@Autowired private SiteService siteService;

	@GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SearchRestBean>> search(
            @RequestParam String terms,
            @RequestParam(required = false) String site,
            @RequestParam(required = false) String tool,
            @RequestParam(defaultValue = "0") Integer start,
            @RequestParam(defaultValue = "10") Integer limit) {

        System.out.println(terms);
        System.out.println(site);
        System.out.println(tool);

		Session session = checkSakaiSession();

        try {
            //Set the limit if it hasn't been set already
            if (limit == null) {
                limit = 10;
            }

            List<String> sites = site == null ? null : Arrays.asList(new String[] { site });

            return ResponseEntity.ok(searchService.search(terms, sites, start, limit)
                .stream().map(sr -> SearchRestBean.of(sr, siteService)).collect(Collectors.toList()));

        } catch (InvalidSearchQueryException e) {
            return ResponseEntity.badRequest().build();
        }
	}

	@GetMapping(value = "/search/suggestions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> suggestions(@RequestParam String terms) {
        return ResponseEntity.ok(Arrays.asList(searchService.getSearchSuggestions(terms, null, true)));
    }
}
