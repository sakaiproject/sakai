/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.ui.bean.evaluation;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.jsf2.model.PhaseAware;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.CellValue;
import org.sakaiproject.tool.assessment.facade.ExportSection;
import org.sakaiproject.tool.assessment.jsf.convert.AnswerSurveyConverter;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.util.Validator;
import org.sakaiproject.tool.assessment.ui.listener.evaluation.HistogramListener;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.util.api.FormattedText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/* For evaluation: Export Responses backing bean. */
@Slf4j
@ManagedBean(name="exportResponses")
@SessionScoped
public class ExportResponsesBean extends SpringBeanAutowiringSupport implements Serializable, PhaseAware {

	private static final long serialVersionUID = 2854656853283125977L;

	// Marks the beginning of each new sheet. If absent, treat as a single-sheet workbook.
	public static final String NEW_SHEET_MARKER = "<sheet/>";
	public static final String HEADER_MARKER = "<header/>";
	public static final String FORMAT = "<format ";
	public static final String FORMAT_BOLD = FORMAT + "bold/>";
	private static final String MSG_BUNDLE = "org.sakaiproject.tool.assessment.bundle.EvaluationMessages";

	@Autowired
	@Qualifier("org.sakaiproject.util.api.FormattedText")
	private FormattedText formattedText;

	@Autowired
	@Qualifier("org.sakaiproject.component.api.ServerConfigurationService")
	private ServerConfigurationService serverConfigurationService;

	@Setter private String assessmentId;
	@Setter private String assessmentName;
	@Getter @Setter private boolean anonymous;

	/**
	 * Creates a new TotalScoresBean object.
	 */
	public ExportResponsesBean() {
	}

	/**
	 * get assessment id
	 *
	 * @return the assessment id
	 */
	public String getAssessmentId() {
		return Validator.check(assessmentId, "0");
	}

	/**
	 * get assessment name
	 *
	 * @return the name
	 */
	public String getAssessmentName() {
		return Validator.check(assessmentName, "N/A");
	}

	public void endProcessValidators() {
		log.debug("endProcessValidators");
	}

	public void endProcessUpdates() {
		log.debug("endProcessUpdates");
	}

	public void startRenderResponse() {
		log.debug("startRenderResponse");
	}
	
	public void exportExcel(ActionEvent event){
        log.debug("exporting as Excel: assessment id =  {}", getAssessmentId());
        // allow local customization of spreadsheet output
        FacesContext faces = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse)faces.getExternalContext().getResponse();
        response.reset();	// Eliminate the added-on stuff
        response.setHeader("Cache-Control", "no-store");
       	writeDataToResponse(getSpreadsheetData(), getDownloadFileName(), response);
       	faces.responseComplete();
    }
	
    private List<Object> getSpreadsheetData() {
    	TotalScoresBean totalScores = (TotalScoresBean) ContextUtil.lookupBean("totalScores");
    	Map<String, EnrollmentRecord> useridMap = totalScores.getUserIdMap(TotalScoresBean.CALLED_FROM_EXPORT_LISTENER, AgentFacade.getCurrentSiteId());

        HistogramListener histogramListener = new HistogramListener();
  	  	Iterator detailedStats = histogramListener.getDetailedStatisticsSpreadsheetData(assessmentId).iterator();
  	  	detailedStats.next();
  	  	boolean showPartAndTotalScoreSpreadsheetColumns = true;
  	  	boolean isOneSelectionType = totalScores.getIsOneSelectionType();
  		boolean showDetailedStatisticsSheet = (Boolean) detailedStats.next();

        String audioMessage = ContextUtil.getLocalizedString(MSG_BUNDLE,"audio_message");
        String fileUploadMessage = ContextUtil.getLocalizedString(MSG_BUNDLE,"file_upload_message");
        String noSubmissionMessage = ContextUtil.getLocalizedString(MSG_BUNDLE,"export_no_submission");
        GradingService gradingService = new GradingService();
        String poolString = ContextUtil.getLocalizedString(MSG_BUNDLE,"pool");
        String partString = ContextUtil.getLocalizedString(MSG_BUNDLE,"part");
        String questionString = ContextUtil.getLocalizedString(MSG_BUNDLE,"question");
        String textString = ContextUtil.getLocalizedString(MSG_BUNDLE,"question_text");
        String responseString = ContextUtil.getLocalizedString(MSG_BUNDLE,"response");
        String pointsString = ContextUtil.getLocalizedString(MSG_BUNDLE,"points");
        String rationaleString = ContextUtil.getLocalizedString(MSG_BUNDLE,"rationale");
        String itemGradingCommentsString = ContextUtil.getLocalizedString(MSG_BUNDLE,"grader_comments");
        String responseCommentsString = ContextUtil.getLocalizedString(MSG_BUNDLE,"student_comments");
        String startTimeString = ContextUtil.getLocalizedString(MSG_BUNDLE,"start_time");
        String submitTimeString = ContextUtil.getLocalizedString(MSG_BUNDLE,"submit_time");

        Map<ExportSection, List<List<CellValue<?>>>> exportResponsesData = gradingService.getExportResponsesData(assessmentId, anonymous, audioMessage, fileUploadMessage, noSubmissionMessage,
			showPartAndTotalScoreSpreadsheetColumns, poolString, partString, questionString, textString, responseString, pointsString, rationaleString,
			itemGradingCommentsString, useridMap, responseCommentsString, isOneSelectionType);

        // Now build the header line
        List<CellValue<?>> headerCells = new ArrayList<>();
        if (anonymous) {
  		  headerCells.add(CellValue.STRING(ContextUtil.getLocalizedString(MSG_BUNDLE,"sub_id")));
  	  	}
  	  	else {
  		  headerCells.add(CellValue.STRING(ContextUtil.getLocalizedString(MSG_BUNDLE,"last_name")));
  		  headerCells.add(CellValue.STRING(ContextUtil.getLocalizedString(MSG_BUNDLE,"first_name")));
  		  headerCells.add(CellValue.STRING(ContextUtil.getLocalizedString(MSG_BUNDLE,"user_name")));
  		  headerCells.add(CellValue.STRING(ContextUtil.getLocalizedString(MSG_BUNDLE,"num_submission")));
  	  	}

        headerCells.add(CellValue.STRING(startTimeString));
        headerCells.add(CellValue.STRING(submitTimeString));

        PublishedAssessmentService pubService = new PublishedAssessmentService();
        int numberOfSections = pubService.getPublishedSectionCount(Long.valueOf(assessmentId));
        if (numberOfSections > 1) {
            for (int i = 1; i <= numberOfSections; i++) {
                headerCells.add(CellValue.STRING(partString + " " + i + " " + ContextUtil.getLocalizedString(MSG_BUNDLE,"score")));
        	}
        }

        headerCells.add(CellValue.STRING(ContextUtil.getLocalizedString(MSG_BUNDLE,"tot")));

        if (isOneSelectionType) {
              headerCells.add(CellValue.STRING(ContextUtil.getLocalizedString(MSG_BUNDLE, "correct_answers_title")));
              headerCells.add(CellValue.STRING(ContextUtil.getLocalizedString(MSG_BUNDLE, "incorrect_answers_title")));
              headerCells.add(CellValue.STRING(ContextUtil.getLocalizedString(MSG_BUNDLE, "empty_answers_title")));
        }
        headerCells.add(CellValue.STRING(itemGradingCommentsString));
        List<List<CellValue<?>>> exportHeaderRows = exportResponsesData.getOrDefault(ExportSection.HEADER, List.of());
        if (!exportHeaderRows.isEmpty()) {
        	headerCells.addAll(exportHeaderRows.get(0));
        }

        List<Object> rows = new ArrayList<>();
        rows.add(SpreadsheetRow.sheetBreak(ContextUtil.getLocalizedString(MSG_BUNDLE,"responses")));
        rows.add(SpreadsheetRow.header(headerCells));

        for (List<CellValue<?>> row : exportResponsesData.getOrDefault(ExportSection.ROWS, List.of())) {
        	rows.add(SpreadsheetRow.data(row));
        }

  	  	if (showDetailedStatisticsSheet) {
  	  		rows.add(SpreadsheetRow.sheetBreak(ContextUtil.getLocalizedString(MSG_BUNDLE,"item_analysis")));

        	// Legacy rows from HistogramListener predate the CellValue/SpreadsheetRow model and
        	// still use marker-string conventions; getAsWorkbook renders them via a compatibility path.
        	while (detailedStats.hasNext()) {
        		rows.add(detailedStats.next());
        	}
        }

        return rows;
    }
    
    /**
     * Generates a default filename (minus the extension) for a download from this Gradebook. 
     *
	 * @return The appropriate filename for the export
	 */
    public String getDownloadFileName() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        StringBuilder fileName = new StringBuilder(ContextUtil.getLocalizedString(MSG_BUNDLE,"assessment"));
        if(StringUtils.trimToNull(assessmentName) != null) {
        	assessmentName = assessmentName.replaceAll("\\s", "_"); // replace whitespace with '_'
            fileName.append("-");
            fileName.append(assessmentName);
        }
		fileName.append("-");
		fileName.append( LocalDate.now().format(formatter) );
		return fileName.toString();
	}
    
    
	public void writeDataToResponse(List<Object> spreadsheetData, String fileName, HttpServletResponse response) {
		String mimetype = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
		String extension = ".xlsx";
		int columns = findColumnSize(spreadsheetData);
		log.info("Samigo export ({} columns): Using xlsx mimetype: {}", columns, mimetype);
		response.setContentType(mimetype);

		String escapedFilename = org.sakaiproject.util.Validator.escapeUrl(fileName);
		response.setHeader("Content-disposition", "attachment; filename=" + escapedFilename + extension	+ "; filename*=UTF-8''" + escapedFilename + extension);

		try (OutputStream out = response.getOutputStream(); Workbook workbook = getAsWorkbook(spreadsheetData)) {
			workbook.write(out);
			out.flush();
		} catch (IOException e) {
			log.error("Error writing Excel file to response", e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			try {
				response.getWriter().write("An error occurred while generating the Excel file.");
			} catch (IOException ex) {
				log.error("Error writing error message to response", ex);
			}
		}
	}

	/**
	 * @param spreadsheetData each element is either a {@link SpreadsheetRow} (the typed,
	 *                        CellValue-based rows this bean builds itself) or, for backward
	 *                        compatibility, a raw {@code List<Object>} row using the legacy
	 *                        marker-string convention (sourced from HistogramListener's
	 *                        detailed statistics sheet, which predates this typed model).
	 */
	public Workbook getAsWorkbook(List<Object> spreadsheetData) {
		int columns = findColumnSize(spreadsheetData);
		log.info("Samigo export ({} columns): Using xlsx format", columns);
		Workbook wb = new XSSFWorkbook();

		CellStyle boldStyle = wb.createCellStyle();
		Font boldFont = wb.createFont();
		boldFont.setBold(true);
		String fontName = serverConfigurationService.getString("spreadsheet.font", "Calibri");
		boldFont.setFontName(fontName);
		boldStyle.setFont(boldFont);

		// Excel date format
		CellStyle dateFormat = wb.createCellStyle();
		dateFormat.setDataFormat((short) 15);

		Sheet sheet = null;
		short rowPos = 0;

		for (Object rowData : spreadsheetData) {
			if (rowData instanceof SpreadsheetRow row) {
				switch (row.section()) {
					case SHEET_BREAK -> {
						sheet = wb.createSheet(row.cells().get(0).value().toString());
						rowPos = 0;
					}
					case HEADER -> {
						if (sheet == null) {
							sheet = wb.createSheet("responses"); // avoid NPE
						}
						Row headerRow = sheet.createRow(rowPos++);
						short colPos = 0;
						for (CellValue<?> cellValue : row.cells()) {
							Cell cell = headerRow.createCell(colPos++);
							cell.setCellValue(cellValue.value().toString());
							cell.setCellStyle(boldStyle);
						}
					}
					case ROWS -> {
						if (sheet == null) {
							sheet = wb.createSheet("responses"); // avoid NPE
						}
						Row poiRow = sheet.createRow(rowPos++);
						short colPos = 0;
						for (CellValue<?> cellValue : row.cells()) {
							Object data = cellValue.value();
							if (data != null) {
								writeCell(poiRow.createCell(colPos++), data, dateFormat);
							}
						}
					}
				}
				continue;
			}

			// Legacy path: rows from HistogramListener's detailed statistics sheet, which
			// predates the CellValue/SpreadsheetRow model and still uses marker-string
			// conventions (row markers NEW_SHEET_MARKER/HEADER_MARKER, and an inline
			// FORMAT_BOLD marker preceding a cell that should be rendered bold).
			List<?> legacyRow = (List<?>) rowData;

			if (legacyRow.get(0).toString().equals(NEW_SHEET_MARKER)) {
				 sheet = wb.createSheet(legacyRow.get(1).toString());
				 rowPos = 0;
			}
			else if (legacyRow.get(0).toString().equals(HEADER_MARKER)) {
			    if (sheet == null) {
		              sheet = wb.createSheet("responses"); // avoid NPE
			    }
				Row headerRow = sheet.createRow(rowPos++);
				for (short i = 0; i < legacyRow.size()-1; i++) {
					Cell cell = headerRow.createCell(i);
					cell.setCellValue(legacyRow.get(i+1).toString());
					cell.setCellStyle(boldStyle);
				}
			}
			else {
				if (sheet == null) {
					sheet = wb.createSheet("responses"); // avoid NPE
				}
				Row poiRow = sheet.createRow(rowPos++);
				short colPos = 0;
				Iterator<?> colIter = legacyRow.iterator();

				while (colIter.hasNext()) {
					Object data = colIter.next();
					if (data != null) {
						Cell cell = poiRow.createCell(colPos++);

						if (data.toString().startsWith(FORMAT)) {
							if (data.equals(FORMAT_BOLD)) {
								cell.setCellStyle(boldStyle);
							}
							data = colIter.next();
						}

						writeCell(cell, data, dateFormat);
					}
				}
			}
		}

		return wb;
	}

	private void writeCell(Cell cell, Object data, CellStyle dateFormat) {
		if (data instanceof Integer integerValue) {
			cell.setCellValue(integerValue);
		} else if (data instanceof Long longValue) {
			cell.setCellValue(longValue);
		} else if (data instanceof Double doubleValue) {
			// Round the Double to two decimal places
			BigDecimal bd = BigDecimal.valueOf(doubleValue);
			bd = bd.setScale(2, RoundingMode.HALF_UP);
			cell.setCellValue(bd.doubleValue());
		} else if (data instanceof Date dateValue) {
			cell.setCellValue(dateValue);
			cell.setCellStyle(dateFormat);
		} else {
			AnswerSurveyConverter converter = new AnswerSurveyConverter();
			String datac = converter.getAsString(null, null, data.toString());
			cell.setCellValue(formattedText.convertFormattedTextToPlaintext(datac));
		}
	}

	private int findColumnSize(List<Object> spreadsheetData) {
		int columns = 0; // the largest number of columns required for a row
		for (Object rowData : spreadsheetData) {
			int size = (rowData instanceof SpreadsheetRow row) ? row.cells().size() : ((List<?>) rowData).size();
			if (size > columns) {
				columns = size;
			}
		}
		return columns;
	}

}
