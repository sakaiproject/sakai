/**
 * Copyright (c) 2023 The Apereo Foundation
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

package org.sakaiproject.tool.assessment.ui.servlet.evaluation;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.HistogramBarBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.HistogramScoresBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.QuestionScoresBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.TotalScoresBean;
import org.sakaiproject.tool.assessment.ui.listener.evaluation.HistogramListener;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.ui.model.AssessmentReport;
import org.sakaiproject.tool.assessment.ui.model.AssessmentReportSection;
import org.sakaiproject.tool.assessment.ui.model.AssessmentReport.AssessmentReportOrientation;
import org.sakaiproject.tool.assessment.ui.model.AssessmentReport.AssessmentReportType;
import org.sakaiproject.tool.assessment.ui.model.AssessmentReportSection.TableLayout;
import org.sakaiproject.tool.assessment.ui.servlet.SamigoBaseServlet;
import org.sakaiproject.tool.assessment.util.ExcelExportUtil;
import org.sakaiproject.tool.assessment.util.PdfExportUtil;
import org.sakaiproject.util.ResourceLoader;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExportReportServlet extends SamigoBaseServlet {


    public static final String PARAM_SITE_ID = "siteId";
    public static final String PARAM_ASSESSMENT_ID = "assessmentId";
    public static final String PARAM_FORMAT = "format";
    public static final String PARAM_TYPE = "type";
    public static final String EXPORT_TYPE_ITEM_ANALYSIS = "item_analysis";
    public static final String EXPORT_TYPE_STATISTICS = "statistics";

    private static final ResourceLoader EVALUATION_BUNDLE = new ResourceLoader(SamigoConstants.EVAL_BUNDLE);


    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Required
        String siteId = StringUtils.trimToNull(request.getParameter(PARAM_SITE_ID));
        String assessmentId = request.getParameter(PARAM_ASSESSMENT_ID);
        String format = request.getParameter(PARAM_FORMAT);
        String type = request.getParameter(PARAM_TYPE);

        // Check if required params are present
        if (StringUtils.isAnyBlank(siteId, assessmentId)
                || !StringUtils.equalsAny(type, EXPORT_TYPE_ITEM_ANALYSIS, EXPORT_TYPE_STATISTICS)
                || !StringUtils.equalsAny(format, EXPORT_FORMAT_XLSX, EXPORT_FORMAT_PDF)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    String.format("At least one required param is not present. %s=[%s], %s=[%s], %s=[%s], %s=[%s]",
                            PARAM_SITE_ID, siteId, PARAM_ASSESSMENT_ID, assessmentId, PARAM_TYPE, type, PARAM_FORMAT, format));
            return;
        }

        // Check permissions
        if (!canExportReport(siteId)) {
            getUserId().ifPresentOrElse(
                userId -> log.warn("User with id [{}] is not allowed to export assessment report for assessment with id [{}] on site [{}]",
                        userId, assessmentId, siteId),
                () -> log.warn("Unauthenticated user tried to export assessment report for assessment with id [{}] on site [{}]",
                        assessmentId, siteId)
            );

            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Get assessment
        PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
        PublishedAssessmentFacade assessment = publishedAssessmentService.getPublishedAssessment(assessmentId);

        // Check if assessment was found
        if (assessment == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No published assessment with id [" + assessmentId + "] found");
            return;
        }

        PublishedAssessmentData assessmentData = (PublishedAssessmentData) assessment.getData();

        // Initial checks done, get data and build response
        HistogramScoresBean highestSubmissionHsBean = getHistogramScores(assessmentData, TotalScoresBean.HIGHEST_SUBMISSION, request, response).orElse(null);
        HistogramScoresBean allSubmissionsHsBean = getHistogramScores(assessmentData, TotalScoresBean.ALL_SUBMISSIONS, request, response).orElse(null);

        if (ObjectUtils.anyNull(highestSubmissionHsBean, highestSubmissionHsBean.getDetailedStatistics(),
               allSubmissionsHsBean, allSubmissionsHsBean.getDetailedStatistics())) {
            log.error("Could not get detailedStatistics from histogram scores");
            log.debug("highestSubmissionHsBean {}", highestSubmissionHsBean);
            log.debug("highestSubmissionHsBean.detailedStatistics {}",
                    highestSubmissionHsBean != null ? highestSubmissionHsBean.getDetailedStatistics() : "N/A");
            log.debug("allSubmissionsHsBean {}", allSubmissionsHsBean);
            log.debug("allSubmissionsHsBean.detailedStatistics {}",
                    allSubmissionsHsBean != null ? allSubmissionsHsBean.getDetailedStatistics() : "N/A");
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        String reportTitle = EVALUATION_BUNDLE.getFormattedMessage("item_analysis") + ": " + highestSubmissionHsBean.getAssessmentName();
        Optional<String> optSiteTitle = getSite(assessment.getOwnerSiteId()).map(Site::getTitle);
        String reportSubject = optSiteTitle.map(siteTitle -> EVALUATION_BUNDLE.getFormattedMessage("export_site") + ": " + siteTitle).orElse(null);

        String typeLabel;
        AssessmentReport report;
        switch(type) {
            case EXPORT_TYPE_ITEM_ANALYSIS:
                typeLabel = EVALUATION_BUNDLE.getFormattedMessage("item_analysis");
                report = itemAnalysisReport(reportTitle, reportSubject, highestSubmissionHsBean, allSubmissionsHsBean);
                break;
            case EXPORT_TYPE_STATISTICS:
                typeLabel = EVALUATION_BUNDLE.getFormattedMessage("stat_view");
                report = statisticsReport(reportTitle, reportSubject, highestSubmissionHsBean, allSubmissionsHsBean);
                break;
            default:
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                log.error("Unhandled export type {}", type);
                return;
        }

        String content;
        String contentType;
        String fileExtension;
        switch(format) {
            case EXPORT_FORMAT_XLSX:
                content = ExcelExportUtil.assessmentReportToXslx(report);
                contentType = CONTENT_TYPE_XLSX;
                fileExtension = FILE_EXT_XLSX;
                break;
            case EXPORT_FORMAT_PDF:
                content = PdfExportUtil.assessmentReportToPdf(report);
                contentType = CONTENT_TYPE_PDF;
                fileExtension = FILE_EXT_PDF;
                break;
            default:
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                log.error("Unhandled export format {}", format);
                return;
        }

        // Set headers
        response.setContentType(contentType);

        String assessmentName = cleanAssessmentTitle(assessmentData);
        String filename = cleanFilename(typeLabel + " " + assessmentName + fileExtension);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString());

        PrintWriter out = response.getWriter();
        out.write(content);
        out.flush();
        out.close();
    }

    public static String exportReportUrl(String assessmentId, String type, String format) {
        log.debug("assessmentId [{}], type [{}] format [{}]", assessmentId, type, format);
        PublishedAssessmentService assessmentService = new PublishedAssessmentService();
        PublishedAssessmentFacade assessment = assessmentService.getPublishedAssessment(assessmentId);

        if (assessment == null) {
            return null;
        }

        return UriComponentsBuilder.fromPath(SamigoConstants.SERVLET_MAPPING_EXPORT_REPORT)
                .queryParam(ExportReportServlet.PARAM_ASSESSMENT_ID, assessmentId)
                .queryParam(ExportReportServlet.PARAM_SITE_ID, assessment.getOwnerSiteId())
                .queryParam(ExportReportServlet.PARAM_TYPE, type)
                .queryParam(ExportReportServlet.PARAM_FORMAT, format)
                .build().toUriString();
    }

    private boolean canExportReport(String siteId) {
        return hasPrivilege(SamigoConstants.AUTHZ_QUESTIONPOOL_CREATE, siteId)
                && hasPrivilege(SamigoConstants.AUTHZ_QUESTIONPOOL_EDIT_OWN, siteId)
                && hasPrivilege(SamigoConstants.AUTHZ_QUESTIONPOOL_DELETE_OWN, siteId)
                && hasPrivilege(SamigoConstants.AUTHZ_QUESTIONPOOL_COPY_OWN, siteId);
    }

    private Optional<HistogramScoresBean> getHistogramScores(PublishedAssessmentData assessment, String scope, HttpServletRequest request, HttpServletResponse response) {
        String assessmentId = assessment.getPublishedAssessmentId().toString();
        HistogramListener histogramListener = new HistogramListener();

        // Prepare histogramScoresBean based on new instance to handle multiple scopes
        HistogramScoresBean histogramScoresBean = new HistogramScoresBean();

        histogramScoresBean.setAllSubmissions(scope);

        // Prepare totalScoresBean
        TotalScoresBean totalScoresBean = (TotalScoresBean) ContextUtil.lookupBeanFromExternalServlet("totalScores", request, response);
        totalScoresBean.setAllSubmissions(scope);
        totalScoresBean.setReleaseToAnonymous(false);
        totalScoresBean.setPublishedId(assessmentId);
        totalScoresBean.setPublishedAssessment(assessment);

        // Prepare questionScoresBean
        QuestionScoresBean questionScoresBean = (QuestionScoresBean) ContextUtil.lookupBeanFromExternalServlet("questionScores", request, response);
        questionScoresBean.setAllSubmissions(scope);

        // Process listener logic
        histogramListener.histogramScores(histogramScoresBean, totalScoresBean);

        return Optional.of(histogramScoresBean);
    }

    private AssessmentReport itemAnalysisReport(String title, String subject, HistogramScoresBean highestSubmissionHsBean, HistogramScoresBean allSubmissionsHsBean) {
        return AssessmentReport.type(AssessmentReportType.ITEM_ANALYSIS)
                .title(title)
                .subject(subject)
                .orientation(AssessmentReportOrientation.LANDSCAPE)
                .section(AssessmentReportSection.builder()
                        .title(EVALUATION_BUNDLE.getFormattedMessage("highest_sub"))
                        .tableLayout(TableLayout.HORIZONTAL)
                        .tableHeader(itemAnalysisHeader(highestSubmissionHsBean))
                        .tableData(itemAnalysisData(highestSubmissionHsBean))
                        .build())
                .section(AssessmentReportSection.builder()
                        .title(EVALUATION_BUNDLE.getFormattedMessage("all_sub"))
                        .tableLayout(TableLayout.HORIZONTAL)
                        .tableHeader(itemAnalysisHeader(allSubmissionsHsBean))
                        .tableData(itemAnalysisData(allSubmissionsHsBean))
                        .build())
                .build();
    }

    private List<List<String>> itemAnalysisData(HistogramScoresBean histogramScoresBean) {
        int maxNumberOfAnswers = histogramScoresBean.getMaxNumberOfAnswers();

        return histogramScoresBean.getDetailedStatistics().stream()
                .map(itemStatistics -> {
                    List<String> dataRow = new ArrayList<>();

                    dataRow.add(itemStatistics.getQuestionLabel());
                    dataRow.add(String.valueOf(itemStatistics.getNumResponses()));
                    dataRow.add(itemStatistics.getPercentCorrect());

                    if (histogramScoresBean.getShowDiscriminationColumn()) {
                        dataRow.add(itemStatistics.getPercentCorrectFromUpperQuartileStudents());
                        dataRow.add(itemStatistics.getPercentCorrectFromLowerQuartileStudents());
                        dataRow.add(itemStatistics.getDiscrimination());
                    }

                    if (maxNumberOfAnswers > 0) {
                        dataRow.add(toCellValue(itemStatistics.getDifficulty()));
                        dataRow.add(toCellValue(itemStatistics.getNumberOfStudentsWithCorrectAnswers()));
                        dataRow.add(toCellValue(itemStatistics.getNumberOfStudentsWithIncorrectAnswers()));
                        dataRow.add(String.valueOf(itemStatistics.getNumberOfStudentsWithZeroAnswers()));
                    }

                    HistogramBarBean[] histogramBars = Optional.ofNullable(itemStatistics.getHistogramBars())
                            .orElse(new HistogramBarBean[0]);
                    for (int i = 0; i < maxNumberOfAnswers; i++) {
                        if (!itemStatistics.getShowIndividualAnswersInDetailedStatistics()
                                || i >= histogramBars.length || histogramBars[i] == null) {
                            dataRow.add("");
                            continue;
                        }
                        dataRow.add(String.valueOf(histogramBars[i].getNumStudents()));
                    }

                    return dataRow;
                }).collect(Collectors.toList());
    }

    private String toCellValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private List<String> itemAnalysisHeader(HistogramScoresBean histogramScoresBean) {
        int itemCount = histogramScoresBean.getDetailedStatistics().size();
        List<String> header = new ArrayList<>();

        header.add(EVALUATION_BUNDLE.getFormattedMessage("question"));
        header.add(histogramScoresBean.isRandomType() ? "N(" + itemCount + ")" : "N");
        header.add(EVALUATION_BUNDLE.getFormattedMessage("pct_correct_of") + " " + EVALUATION_BUNDLE.getFormattedMessage("whole_group"));
        if (histogramScoresBean.getShowDiscriminationColumn()) {
            header.add(EVALUATION_BUNDLE.getFormattedMessage("pct_correct_of") + " " + EVALUATION_BUNDLE.getFormattedMessage("upper_pct"));
            header.add(EVALUATION_BUNDLE.getFormattedMessage("pct_correct_of") + " " + EVALUATION_BUNDLE.getFormattedMessage("lower_pct"));
            header.add(EVALUATION_BUNDLE.getFormattedMessage("discrim_abbrev"));
        }
        if (histogramScoresBean.getMaxNumberOfAnswers() > 0) {
            header.add(EVALUATION_BUNDLE.getFormattedMessage("difficulty"));
            header.add(EVALUATION_BUNDLE.getFormattedMessage("total_correct"));
            header.add(EVALUATION_BUNDLE.getFormattedMessage("total_incorrect"));
            header.add(EVALUATION_BUNDLE.getFormattedMessage("no_answer"));
        }

        for (int i = 0; i < histogramScoresBean.getMaxNumberOfAnswers(); i++) {
            header.add(String.valueOf(answerLabel(i)));
        }

        return header;
    }

    private AssessmentReport statisticsReport(String title, String subject, HistogramScoresBean highestSubmissionHsBean, HistogramScoresBean allSubmissionsHsBean) {
        return AssessmentReport.type(AssessmentReportType.ASSESSMENT_STATISTICS)
                .title(title)
                .subject(subject)
                .section(AssessmentReportSection.builder()
                        .title(EVALUATION_BUNDLE.getFormattedMessage("highest_sub"))
                        .tableLayout(TableLayout.VERTICAL)
                        .tableHeader(statisticsHeader(highestSubmissionHsBean))
                        .tableData(statisticsData(highestSubmissionHsBean))
                        .build())
                .section(AssessmentReportSection.builder()
                        .title(EVALUATION_BUNDLE.getFormattedMessage("all_sub"))
                        .tableLayout(TableLayout.VERTICAL)
                        .tableHeader(statisticsHeader(allSubmissionsHsBean))
                        .tableData(statisticsData(allSubmissionsHsBean))
                        .build())
                .build();
    }

    private List<String> statisticsHeader(HistogramScoresBean histogramScoresBean) {
        if(histogramScoresBean.isTrackingQuestion()) {
            return Stream.of(
                    "sub_view",
                    "tot_score_possible",
                    "mean_eq",
                    "median",
                    "mode",
                    "range_eq",
                    "time_min",
                    "time_avg",
                    "time_max",
                    "qtile_1_eq",
                    "qtile_3_eq",
                    "std_dev",
                    "skew_coef"
            ).map(EVALUATION_BUNDLE::getFormattedMessage).collect(Collectors.toList());
        }
        
        return Stream.of(
                "sub_view",
                "tot_score_possible",
                "mean_eq",
                "median",
                "mode",
                "range_eq",
                "qtile_1_eq",
                "qtile_3_eq",
                "std_dev",
                "skew_coef"
        ).map(EVALUATION_BUNDLE::getFormattedMessage).collect(Collectors.toList());
    }

    private List<List<String>> statisticsData(HistogramScoresBean histogramScoresBean) {
        List<String> dataRow = new ArrayList<>();

        dataRow.add(String.valueOf(histogramScoresBean.getNumResponses()));
        dataRow.add(histogramScoresBean.getRoundedTotalPossibleScore());
        dataRow.add(histogramScoresBean.getMean());
        dataRow.add(histogramScoresBean.getMedian());
        dataRow.add(histogramScoresBean.getMode());
        dataRow.add(histogramScoresBean.getRange());
        if (histogramScoresBean.isTrackingQuestion()) {
            String[] timeStats = histogramScoresBean.getTimeStats();
            dataRow.add(timeStats[0]);
            dataRow.add(timeStats[1]);
            dataRow.add(timeStats[2]);
        }
        dataRow.add(histogramScoresBean.getQ1());
        dataRow.add(histogramScoresBean.getQ3());
        dataRow.add(histogramScoresBean.getStandDev());
        dataRow.add(histogramScoresBean.getSkewnessCoefficient());

        return Collections.singletonList(dataRow);
    }

    private static char answerLabel(int sequence) {
        String alphabet = ItemDataIfc.ANSWER_OPTION_LABELS;
        return alphabet.charAt(sequence % alphabet.length());
    }
}
