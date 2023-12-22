/*
 * Copyright (c) 2003-2023 The Apereo Foundation
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
package org.sakaiproject.tool.assessment.ui.servlet.cp;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.tool.assessment.business.entity.ItemStatistics;
import org.sakaiproject.tool.assessment.business.entity.QuestionPoolStatistics;
import org.sakaiproject.tool.assessment.facade.QuestionPoolFacade;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.services.assessment.StatisticsService;
import org.sakaiproject.tool.assessment.ui.servlet.SamigoBaseServlet;
import org.sakaiproject.util.ResourceLoader;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QuestionPoolStatisticsServlet extends SamigoBaseServlet {


    public static final String PARAM_SITE_ID = "siteId";
    public static final String PARAM_QUESTION_POOL_ID = "qpId";

    private static final ResourceLoader EVALUATION_BUNDLE = new ResourceLoader(SamigoConstants.EVAL_BUNDLE);

    private String naMessage = null;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Required
        String questionPoolId = StringUtils.trimToNull(request.getParameter(PARAM_QUESTION_POOL_ID));
        String siteId = StringUtils.trimToNull(request.getParameter(PARAM_SITE_ID));

        // Check if required params are present
        if (StringUtils.isAnyBlank(siteId, questionPoolId) || !NumberUtils.isParsable(questionPoolId)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    String.format("At least one required param is not valid. %s=[%s], %s=[%s]",
                            PARAM_SITE_ID, siteId, PARAM_QUESTION_POOL_ID, questionPoolId));
            return;
        }

        Optional<String> optUserId = getUserId();

        // Check permissions
        if (!canGetStatistics(siteId)) {
            optUserId.ifPresentOrElse(
                userId -> log.warn("User with id [{}] is not allowed to get statistics for question pool with id [{}] on site [{}]",
                        userId, questionPoolId, siteId),
                () -> log.warn("Unauthenticated user tried to get statistics for question pool with id [{}] on site [{}]",
                        questionPoolId, siteId)
            );

            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String userId = optUserId.get();

        // Get assessment
        QuestionPoolService questionPoolService = new QuestionPoolService();

        QuestionPoolFacade questionPool = questionPoolService.getPool(Long.valueOf(questionPoolId), userId);

        // Check if questionPool was found
        if (questionPool == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    String.format("No questionPool with id [%s] found. userId=[%s]", questionPoolId, userId));
            return;
        }

        // Set headers
        response.setContentType(CONTENT_TYPE_JSON);

        StatisticsService statisticsService = new StatisticsService();
        QuestionPoolStatistics qpStatistics = statisticsService.getQuestionPoolStatistics(Long.valueOf(questionPoolId));

        PrintWriter out = response.getWriter();
        writeJson(out, dataMap(qpStatistics));
        out.flush();
        out.close();
    }

    private Map<String, String> dataMap(QuestionPoolStatistics statistics) {
        ItemStatistics itemStatistics = statistics.getAggregatedItemStatistics();

        return Map.of(
            "questions", valueOrNa(statistics.getQuestionCount()),
            "subPools", valueOrNa(statistics.getSubpoolCount()),
            "useCount", valueOrNa(statistics.getUsageCount()),
            "attempts", valueOrNa(itemStatistics.getAttemptedResponses()),
            "correct", valueOrNa(itemStatistics.getCorrectResponses()),
            "incorrect", valueOrNa(itemStatistics.getIncorrectResponses()),
            "blank", valueOrNa(itemStatistics.getBlankResponses()),
            "difficulty", valueOrNa(itemStatistics.getDifficulty())
        );
    }

    private String valueOrNa(Number number) {
        if (number != null) {
            return String.valueOf(number);
        } else {
            if (naMessage == null) {
                naMessage = EVALUATION_BUNDLE.getFormattedMessage("na");
            }

            return naMessage;
        }
    }

    private void writeJson(PrintWriter out, Map<String, String> data) {
        Gson gson = new Gson();
        String json = gson.toJson(data);
        out.write(json);
    }

    private boolean canGetStatistics(String siteId) {
        return hasPrivilege(SamigoConstants.AUTHZ_QUESTIONPOOL_CREATE, siteId)
                && hasPrivilege(SamigoConstants.AUTHZ_QUESTIONPOOL_EDIT_OWN, siteId)
                && hasPrivilege(SamigoConstants.AUTHZ_QUESTIONPOOL_DELETE_OWN, siteId)
                && hasPrivilege(SamigoConstants.AUTHZ_QUESTIONPOOL_COPY_OWN, siteId);
    }
}
