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

import org.sakaiproject.webapi.beans.GradeRestBean;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.GradeDefinition;
import org.sakaiproject.service.gradebook.shared.SortType;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.user.api.UserNotDefinedException;

import org.springframework.http.MediaType;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.swagger.annotations.ApiOperation;

import lombok.extern.slf4j.Slf4j;

/**
 */
@Slf4j
@RestController
public class GradesController extends AbstractSakaiApiController {

	@Resource(name = "org_sakaiproject_service_gradebook_GradebookService")
	private GradebookService gradebookService;

	@Resource
	private SiteService siteService;

    private Function<Site, List<GradeRestBean>> convert = (s) -> {

        try {
            String gbUrl = "";
            ToolConfiguration tc = s.getToolForCommonId("sakai.gradebookng");
            if (tc != null) {
                gbUrl = "/portal/directtool/" + tc.getId();
            }
            final String url = gbUrl;
            return gradebookService.getAssignments(s.getId()).stream()
                .map(a -> {

                    GradeRestBean gtb = new GradeRestBean(a);
                    List<String> students = new ArrayList<>(s.getUsers());
                    List<GradeDefinition> grades
                        = gradebookService.getGradesForStudentsForItem(s.getId(), a.getId(), students);

                    double total = 0;
                    for (GradeDefinition gd : grades) {
                        if (gd.getGradeEntryType() == GradebookService.GRADE_TYPE_POINTS) {
                            total += Double.parseDouble(gd.getGrade());
                        }
                    }

                    int count = grades.size();
                    gtb.setAverageScore(total > 0 && count > 0 ? total / count : 0);
                    gtb.setUngraded(students.size() - count);
                    gtb.setSiteTitle(s.getTitle());
                    gtb.setUrl(url);
                    return gtb;
                }).collect(Collectors.toList());
        } catch (Exception gnfe) {
            return Collections.<GradeRestBean>emptyList();
        }
    };

    @ApiOperation(value = "Get a particular user's grades data")
	@GetMapping(value = "/users/{userId}/grades", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<GradeRestBean> getUserGrades(@PathVariable String userId) throws UserNotDefinedException {

		checkSakaiSession();
        return siteService.getUserSites().stream().map(convert).flatMap(Collection::stream).collect(Collectors.toList());
	}

    @ApiOperation(value = "Get a particular site's grades data")
	@GetMapping(value = "/sites/{siteId}/grades", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<GradeRestBean> getSiteGrades(@PathVariable String siteId) throws UserNotDefinedException {

		checkSakaiSession();

        try {
            return convert.apply(siteService.getSite(siteId));
        } catch (Exception e) {
            log.error("Failed to get grades for site {}. Returning empty list ...", siteId);
            return Collections.<GradeRestBean>emptyList();
        }
	}
}
