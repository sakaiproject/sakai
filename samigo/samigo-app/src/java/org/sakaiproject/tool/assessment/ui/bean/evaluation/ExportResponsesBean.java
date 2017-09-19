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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.jsf.model.PhaseAware;
import org.sakaiproject.tool.assessment.jsf.convert.AnswerSurveyConverter;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.util.Validator;
import org.sakaiproject.tool.assessment.ui.listener.evaluation.HistogramListener;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;



/**
 * <p>Description: class form for evaluating total scores</p>
 *
 */
public class ExportResponsesBean implements Serializable, PhaseAware {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2854656853283125977L;
	/**
	 * Marks the beginning of each new sheet.
	 * If absent, treat as a single-sheet workbook. 
	 */
	public static final String NEW_SHEET_MARKER = "<sheet/>";
	public static final String HEADER_MARKER = "<header/>";
	
	public static final String FORMAT = "<format ";
	public static final String FORMAT_BOLD = FORMAT + "bold/>";

	
	private String assessmentId;
	private String assessmentName;
	private boolean anonymous;

	private static Logger log = LoggerFactory.getLogger(ExportResponsesBean.class);

	/**
	 * Creates a new TotalScoresBean object.
	 */
	public ExportResponsesBean() {
		log.debug("Creating a new ExportResponsesBean");
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
	 * set assessment id
	 *
	 * @param passessmentId the id
	 */
	public void setAssessmentId(String assessmentId) {
		this.assessmentId = assessmentId;
	}

	/**
	 * get assessment name
	 *
	 * @return the name
	 */
	public String getAssessmentName() {
		return Validator.check(assessmentName, "N/A");
	}

	/**
	 * set assessment name
	 *
	 * @param passessmentName the name
	 */
	public void setAssessmentName(String assessmentName) {
		this.assessmentName = assessmentName;
	}

	/**
	 * get anonymous
	 *
	 * @return anonymous
	 */
	public boolean getAnonymous() {
		return anonymous;
	}

	/**
	 * set anonymous
	 *
	 * @param anonymous
	 */
	public void setAnonymous(boolean anonymous) {
		this.anonymous = anonymous;
	}
	// Following three methods are for interface PhaseAware
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
        log.debug("exporting as Excel: assessment id =  " + getAssessmentId());
        // allow local customization of spreadsheet output
        FacesContext faces = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse)faces.getExternalContext().getResponse();
        response.reset();	// Eliminate the added-on stuff
        response.setHeader("Pragma", "public");	// Override old-style cache control
        response.setHeader("Cache-Control", "public, must-revalidate, post-check=0, pre-check=0, max-age=0");	// New-style
       	writeDataToResponse(getSpreadsheetData(), getDownloadFileName(), response);
       	faces.responseComplete();
    }
	
    private List<List<Object>> getSpreadsheetData() {
    	TotalScoresBean totalScores = (TotalScoresBean) ContextUtil.lookupBean("totalScores");
    	Map useridMap = totalScores.getUserIdMap(TotalScoresBean.CALLED_FROM_EXPORT_LISTENER);
    	
        HistogramListener histogramListener = new HistogramListener();
  	  	Iterator detailedStats = histogramListener.getDetailedStatisticsSpreadsheetData(assessmentId).iterator(); 
  	  	detailedStats.next();
  	  	boolean showPartAndTotalScoreSpreadsheetColumns = true;
  		boolean showDetailedStatisticsSheet = (Boolean) detailedStats.next();
  	  	
  	  	String audioMessage = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages","audio_message");
    	String fileUploadMessage = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages","file_upload_message");
    	String noSubmissionMessage = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages","export_no_submission");
        GradingService gradingService = new GradingService();
        String poolString = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages","pool");
        String partString = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages","part");
        String questionString = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages","question");
        String responseString = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages","response");
        String rationaleString = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages","rationale");
        String itemGradingCommentsString = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages","grader_comments");
        String responseCommentsString = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages","student_comments");
        
        List exportResponsesDataList = gradingService.getExportResponsesData(assessmentId, anonymous, audioMessage, fileUploadMessage, noSubmissionMessage, 
        		showPartAndTotalScoreSpreadsheetColumns, poolString, partString, questionString, responseString, rationaleString, itemGradingCommentsString, useridMap, responseCommentsString);
        //SAM-1693 the returned list could be null -DH
        List<List<Object>> list = new ArrayList<List<Object>>();
        if (exportResponsesDataList != null) {
        	list = (List<List<Object>>) exportResponsesDataList.get(0);
        }
         

        // Now insert the header line
        ArrayList<Object> headerList = new ArrayList<Object>();
        headerList.add(HEADER_MARKER);
        if (anonymous) {
  		  headerList.add(ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages","sub_id"));
  	  	}
  	  	else {
  		  headerList.add(ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages","last_name"));
  		  headerList.add(ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages","first_name"));
  		  headerList.add(ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages","user_name"));
  		  headerList.add(ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages","num_submission"));
  	  	}

        PublishedAssessmentService pubService = new PublishedAssessmentService();
        if (showPartAndTotalScoreSpreadsheetColumns) {
	  	  	int numberOfSections = pubService.getPublishedSectionCount(Long.valueOf(assessmentId)).intValue();
	  	  	if (numberOfSections > 1) {
		  	  	for (int i = 1; i <= numberOfSections; i++) {
		  	  		headerList.add(partString + " " + i + " " + ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages","score"));
		    	}
	  	  	}
	        
	        headerList.add(ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages","tot"));
	        headerList.add(itemGradingCommentsString);
        }
        //SAM-1693 the returned list could be null -DH
        if (exportResponsesDataList != null) {
        	headerList.addAll((ArrayList) exportResponsesDataList.get(1));
        }
  	  	
  	    list.add(0,headerList);
  	  	
  		ArrayList<Object> newSheetList;
  	  	newSheetList = new ArrayList<Object>();
  	  	newSheetList.add(NEW_SHEET_MARKER);
  	  	newSheetList.add(ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages","responses"));
  	  	list.add(0, newSheetList);

  	  	if (showDetailedStatisticsSheet) {
  	  		newSheetList = new ArrayList<Object>();
  	  		newSheetList.add(NEW_SHEET_MARKER);
  	  		newSheetList.add(ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages","item_analysis"));
  	  		list.add(newSheetList);

        	while (detailedStats.hasNext()) {
        		list.add((List)detailedStats.next());
        	}
        }
  	  	
        return list;
    }
    
    /**
     * Generates a default filename (minus the extension) for a download from this Gradebook. 
     *
	 * @param   prefix for filename
	 * @return The appropriate filename for the export
	 */
    public String getDownloadFileName() {
		Date now = new Date();
		String dateFormat = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages","export_filename_date_format");
		DateFormat df = new SimpleDateFormat(dateFormat, new ResourceLoader().getLocale());
		StringBuilder fileName = new StringBuilder(ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages","assessment"));
        if(StringUtils.trimToNull(assessmentName) != null) {
        	assessmentName = assessmentName.replaceAll("\\s", "_"); // replace whitespace with '_'
            fileName.append("-");
            fileName.append(assessmentName);
        }
		fileName.append("-");
		fileName.append(df.format(now));
		return fileName.toString();
	}
    
    
	public void writeDataToResponse(List<List<Object>> spreadsheetData, String fileName, HttpServletResponse response) {
		String mimetype = "application/vnd.ms-excel;charset=UTF-8";
		String extension = ".xls";
		int columns = findColumnSize(spreadsheetData);
		if (columns >= 255) {
			// allows for greater than 255 columns - SAK-16560
			mimetype = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
			extension = ".xlsx";
			log.info("Samigo export ("+columns+" columns): Using xlsx mimetype: " + mimetype);
		}
		response.setContentType(mimetype);
		
		String escapedFilename = org.sakaiproject.util.Validator.escapeUrl(fileName);
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		String userAgent = request.getHeader("User-Agent"); 
		response.setHeader("Content-disposition", "attachment; filename=" + escapedFilename + extension	+ "; filename*=UTF-8''" + escapedFilename + extension);
		
		OutputStream out = null;
		try {
			out = response.getOutputStream();
			getAsWorkbook(spreadsheetData).write(out);
			out.flush();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} finally {
			try {
				if (out != null) out.close();
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		}
	}
	
	protected Workbook getAsWorkbookTest(List<List<Object>> spreadsheetData) {
		Workbook wb = new HSSFWorkbook();
		Sheet sheet = wb.createSheet();
		Iterator<List<Object>> dataIter = spreadsheetData.iterator();

		// By convention, the first list in the list contains column headers.
		Row headerRow = sheet.createRow((short)0);
		List<Object> headerList = dataIter.next();
		for (short i = 0; i < headerList.size(); i++) {
			createCell(headerRow, i, null).setCellValue(headerList.get(i).toString());
		}
		short rowPos = 1;
		while (dataIter.hasNext()) {
			List<Object> rowData = dataIter.next();
			Row row = sheet.createRow(rowPos++);
			for (short i = 0; i < rowData.size(); i++) {
				Cell cell = createCell(row, i, null);
				Object data = rowData.get(i);
				if (data != null) {
					if (data instanceof Double) {
						cell.setCellValue(((Double)data).doubleValue());
					} 
					else {
						cell.setCellValue(data.toString());
					}
				}
			}
		}
		return wb;
	}
	
	public Workbook getAsWorkbook(List<List<Object>> spreadsheetData) {
        // outer list is rows, inner list is columns (cells in the row)
	    int columns = findColumnSize(spreadsheetData);
		Workbook wb;
		if (columns < 255) {
            log.info("Samigo export ("+columns+" columns): Using xsl format");
		    wb = new HSSFWorkbook();
		} else {
		    // allows for greater than 255 columns - SAK-16560
		    log.info("Samigo export ("+columns+" columns): Using xslx format");
		    wb = new XSSFWorkbook();
		}

		CellStyle boldStyle = wb.createCellStyle();
		Font font = wb.createFont();
		font.setBold(true);
		String fontName = ServerConfigurationService.getString("spreadsheet.font");
		if (fontName != null) {
			font.setFontName(fontName);
		}
		boldStyle.setFont(font);
		CellStyle headerStyle = boldStyle;

		CellStyle cellStyle = null;
		if (fontName != null) {
			font = wb.createFont();
			font.setFontName(fontName);
			cellStyle = wb.createCellStyle();
			cellStyle.setFont(font);
		}
		
		Sheet sheet = null;

		Iterator<List<Object>> dataIter = spreadsheetData.iterator();
		
		short rowPos = 0;
		while (dataIter.hasNext()) {
			List<Object> rowData = dataIter.next();

			if (rowData.get(0).toString().equals(NEW_SHEET_MARKER)) {
				 sheet = wb.createSheet(rowData.get(1).toString());
				 rowPos = 0;
			}
			// By convention, the first list in the list contains column headers.
			// This should only happen once and usually only in a single-sheet workbook
			else if (rowData.get(0).toString().equals(HEADER_MARKER)) {
			    if (sheet == null) {
		              sheet = wb.createSheet("responses"); // avoid NPE
			    }
				Row headerRow = sheet.createRow(rowPos++);
				for (short i = 0; i < rowData.size()-1; i++) {
					createCell(headerRow, i, headerStyle).setCellValue(rowData.get(i+1).toString());
				}
			}
			else {
			    if (sheet == null) {
			        sheet = wb.createSheet("responses"); // avoid NPE
			    }
				Row row = sheet.createRow(rowPos++);
				short colPos = 0;
				Iterator colIter = rowData.iterator();
				while (colIter.hasNext()) {
				//for (short i = 0; i < rowData.size(); i++) {
					Cell cell = null;
					
					//Object data = rowData.get(i);
					Object data = colIter.next();
					if (data != null) {
						if (data.toString().startsWith(FORMAT)) {
							if (data.equals(FORMAT_BOLD)) {
								cell = createCell(row, colPos++, boldStyle);
							}
							data = colIter.next();
						}
						else {
							cell = createCell(row, colPos++, cellStyle);
						}
						if (data != null) {
							if (data instanceof Double) {
								cell.setCellValue(((Double)data).doubleValue());
							} else {
								AnswerSurveyConverter converter = new AnswerSurveyConverter();
								String datac = converter.getAsString(null, null, data.toString());
								// stripping html for export, SAK-17021
								cell.setCellValue(FormattedText.convertFormattedTextToPlaintext(datac));
							}
						}
					}
				}
			}
			
		}
		
		return wb;
	}

	private int findColumnSize(List<List<Object>> spreadsheetData) {
        int columns = 0; // the largest number of columns required for a row
	    for (List<Object> list : spreadsheetData) {
            if (list != null && list.size() > columns) {
                columns = list.size();
            }
        }
        return columns;
    }
	
	private Cell createCell(Row row, short column, CellStyle cellStyle) {
		Cell cell = row.createCell(column);
		//cell.setEncoding(HSSFCell.ENCODING_UTF_16);	
		if (cellStyle != null) {
			cell.setCellStyle(cellStyle);
		}
		
		return cell;
	}
	
    
    
}
