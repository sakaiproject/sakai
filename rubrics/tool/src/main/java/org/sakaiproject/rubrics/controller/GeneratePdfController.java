/**********************************************************************************
 *
 * Copyright (c) 2022 The Apereo Foundation
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

package org.sakaiproject.rubrics.controller;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.NoArgsConstructor;

import org.apache.commons.lang3.StringUtils;

import org.jsoup.Jsoup;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.rubrics.logic.model.Criterion;
import org.sakaiproject.rubrics.logic.model.CriterionOutcome;
import org.sakaiproject.rubrics.logic.model.Evaluation;
import org.sakaiproject.rubrics.logic.model.Metadata;
import org.sakaiproject.rubrics.logic.model.Rating;
import org.sakaiproject.rubrics.logic.model.Rubric;
import org.sakaiproject.rubrics.logic.repository.EvaluationRepository;
import org.sakaiproject.rubrics.logic.repository.RubricRepository;
import org.sakaiproject.rubrics.RubricsConfiguration;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@BasePathAwareController
@NoArgsConstructor
@AllArgsConstructor
@RequestMapping(value="/")
public class GeneratePdfController {

    @Autowired
    RubricsConfiguration rubricsConfiguration;

    @Autowired
    RepositoryEntityLinks repositoryEntityLinks;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private SiteService siteService;

    @Autowired
    private RubricRepository rubricRepository;

    @Autowired
    private EvaluationRepository evaluationRepository;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private PreferencesService preferencesService;

    @Autowired
    private UserDirectoryService userDirectoryService;

    private static Font boldFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD);
    private static Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 7, Font.NORMAL);

    @ResponseBody
    @GetMapping(value = "/getPdf")
    public ResponseEntity<byte[]> getPdf(@RequestParam(name = "sourceId") String sourceId) throws Exception {
        Optional<Rubric> sourceRubric = null;
        String userId = sessionManager.getCurrentSessionUserId();
        Locale locale = preferencesService.getLocale(userId);
        if (locale == null) locale = Locale.getDefault();

        try {
            sourceRubric = rubricRepository.findById(Long.parseLong(sourceId));
            if (!sourceRubric.isPresent()) {
                sourceRubric = Optional.of(rubricsConfiguration.getDefaultLayoutConfiguration(locale.getCountry()).getDefaultRubric());
            }
        } catch (NumberFormatException ex) {
            log.error("{}", ex.getMessage());
        }

        byte[] bytesResult = this.createPdf(sourceRubric, new ArrayList<>(), locale);
        return ResponseEntity.ok().body(bytesResult);
    }

    @ResponseBody
    @GetMapping(value = "/getGradedPdf")
    public ResponseEntity<byte[]> getGradedPdf(@RequestParam(name = "sourceId") String sourceId, @RequestParam("toolId") String toolId,
                @RequestParam("itemId") String itemId, @RequestParam("evaluatedItemId") String evaluatedItemId) throws Exception {
        Optional<Rubric> sourceRubric = null;
        List<Evaluation> evaluationList = new ArrayList<>();
        String userId = sessionManager.getCurrentSessionUserId();
        Locale locale = preferencesService.getLocale(userId);
        if (locale == null) locale = Locale.getDefault();

        try {
            sourceRubric = rubricRepository.findById(Long.parseLong(sourceId));
            if (sourceRubric == null) {
                sourceRubric = Optional.of(rubricsConfiguration.getDefaultLayoutConfiguration(locale.getCountry()).getDefaultRubric());
            }
            evaluationList = evaluationRepository.findByToolIdAndAssociationItemIdAndEvaluatedItemId(toolId, itemId, evaluatedItemId);
        } catch (NumberFormatException ex) {
            log.error("{}", ex.getMessage());
        }

        byte[] bytesResult = createPdf(sourceRubric, evaluationList, locale);
        return ResponseEntity.ok().body(bytesResult);
    }

    public byte[] createPdf(Optional<Rubric> sourceRubric, List<Evaluation> evaluationList, Locale locale) throws DocumentException, IOException {
        // Count points
        Double points = Double.valueOf(0);
        String studentName = "";
        String rubricTitle = "";
        Metadata rubricMetadata = null;
        List<Criterion> criterionList = null;
        final boolean weightedRubric;

        if (sourceRubric.isPresent()) {
            rubricTitle = sourceRubric.get().getTitle();
            rubricMetadata = sourceRubric.get().getMetadata();
            criterionList = sourceRubric.get().getCriterions();
            weightedRubric = sourceRubric.get().getWeighted();
        } else {
            weightedRubric = false;
        }

        if (null != evaluationList && !evaluationList.isEmpty()) {
            points = evaluationList.stream().flatMap(a -> a.getCriterionOutcomes().stream()).mapToDouble(x -> x.getPoints()).sum();
            try {
                User user = userDirectoryService.getUser(evaluationList.get(0).getEvaluatedItemOwnerId());
                studentName = user.getDisplayName();
            } catch(UserNotDefinedException ex) {
                log.error("Cannot get user {}", evaluationList.get(0).getEvaluatedItemOwnerId());
            }
        }

        // Create pdf document
        Document document = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        document.open();

        PdfPTable table = new PdfPTable(1);
        PdfPCell header = new PdfPCell();

        Paragraph paragraph = new Paragraph(messageSource.getMessage("export_rubric_title", new Object[] { rubricTitle + "\n"}, locale), boldFont);
        paragraph.setAlignment(Element.ALIGN_LEFT);
        if (rubricMetadata != null) {
            paragraph.add(messageSource.getMessage("export_rubric_site", new Object[] { this.getCurrentSiteName(rubricMetadata.getOwnerId()) + "\n" }, locale));
        }
        if (StringUtils.isNotBlank(studentName)) {
            paragraph.add(messageSource.getMessage("export_rubric_student", new Object[] { studentName + "\n" }, locale));
        }
        String exportDate = messageSource.getMessage("export_rubric_date", new Object[] { DateFormat.getDateInstance(DateFormat.LONG, locale).format(new Date()) + "\n" }, locale);
        paragraph.add(exportDate);
        header.setBackgroundColor(Color.LIGHT_GRAY);

        if (null != evaluationList && !evaluationList.isEmpty()) {
            paragraph.add(messageSource.getMessage("export_total_points", new Object[] { points }, locale) );
            paragraph.add(Chunk.NEWLINE);
        }
        paragraph.add(Chunk.NEWLINE);
        header.addElement(paragraph);
        table.addCell(header);
        table.completeRow();
        document.add(table);

        if (criterionList != null) {
            for (Criterion cri : criterionList) {
                PdfPCell criterionCell = new PdfPCell();
                PdfPTable criterionTable = new PdfPTable(cri.getRatings().size() + 1);
                String titlePoints = messageSource.getMessage("export_rubrics_points", new Object[] { cri.getTitle(), this.getCriterionPoints(cri, evaluationList) }, locale);
                if (weightedRubric) {
                    titlePoints = messageSource.getMessage("export_rubrics_weight", new Object[] { cri.getTitle(), this.getCriterionPoints(cri, evaluationList), cri.getWeight() }, locale);
                }
                Paragraph criterionParagraph = new Paragraph(titlePoints, boldFont);
                criterionParagraph.add(Chunk.NEWLINE);
                criterionParagraph.add(new Paragraph(cri.getDescription(), normalFont));
                criterionCell.addElement(criterionParagraph);

                criterionTable.addCell(criterionCell);
                List<Rating> ratingList = cri.getRatings();
                for (Rating rating : ratingList) {
                    Paragraph ratingsParagraph = new Paragraph("", boldFont);
                    String ratingPoints = messageSource.getMessage("export_rubrics_points", new Object[] { rating.getTitle(), rating.getPoints() }, locale);
                    ratingsParagraph.add(ratingPoints);
                    ratingsParagraph.add(Chunk.NEWLINE);
                    Paragraph ratingsDesc = new Paragraph("", normalFont);

                    if (StringUtils.isNotEmpty(rating.getDescription())) {
                        ratingsDesc.add(rating.getDescription() + "\n");
                    }
                    ratingsParagraph.add(ratingsDesc);

                    PdfPCell newCell = new PdfPCell();
                    for (Evaluation evaluation : evaluationList) {
                        List<CriterionOutcome> outcomeList = evaluation.getCriterionOutcomes();
                        for (CriterionOutcome outcome : outcomeList) {
                            if (cri.getId().equals(outcome.getCriterionId()) &&
                                    rating.getId().equals(outcome.getSelectedRatingId())) {
                                newCell.setBackgroundColor(Color.LIGHT_GRAY);
                                if (outcome.getComments() != null && !outcome.getComments().isEmpty()) {
                                    ratingsParagraph.add(Chunk.NEWLINE);
                                    ratingsParagraph.add(messageSource.getMessage("export_comments", new Object[] { Jsoup.parse(outcome.getComments()).text() + "\n" }, locale));
                                }
                            }
                        }
                    }
                    newCell.addElement(ratingsParagraph);
                    criterionTable.addCell(newCell);
                }

                criterionTable.completeRow();
                document.add(criterionTable);
            }
        }

        document.close();
        return out.toByteArray();
    }

    private String getCriterionPoints(Criterion cri, List<Evaluation> evaluationList) {
        if (evaluationList == null) return "";

        Double points = Double.valueOf(0);
        List<Rating> ratingList = cri.getRatings();
        for (Rating rating : ratingList) {
            for (Evaluation evaluation : evaluationList) {
                List<CriterionOutcome> outcomeList = evaluation.getCriterionOutcomes();
                for (CriterionOutcome outcome : outcomeList) {
                    if (cri.getId().equals(outcome.getCriterionId())
                            && rating.getId().equals(outcome.getSelectedRatingId())) {
                        points = points + outcome.getPoints();
                    }
                }
            }
        }

        return points.toString();
    }

    private String getCurrentSiteName(String siteId){
        String siteName = "";
        try {
            Site site = siteService.getSite(siteId);
            siteName = site.getTitle();
        } catch (IdUnusedException ex) {
            log.error(ex.getMessage(), ex);
        }
        return siteName;
    }
}
