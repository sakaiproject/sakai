/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/samigo-app/src/java/org/sakaiproject/tool/assessment/ui/bean/evaluation/TotalScoresBean.java $
 * $Id: TotalScoresBean.java 29431 2007-04-22 05:20:55Z ktsao@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.jsf.model.PhaseAware;
import org.sakaiproject.jsf.spreadsheet.SpreadsheetDataFileWriterXls;
import org.sakaiproject.jsf.spreadsheet.SpreadsheetUtil;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.util.Validator;
import org.sakaiproject.tool.assessment.ui.listener.evaluation.HistogramListener;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

// below added for extended spreadsheet support - gopalrc
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;


/**
 * <p>Description: class form for evaluating total scores</p>
 *
 */
public class ExportResponsesBean implements Serializable, PhaseAware {
	
	/**
	 * gopalrc - Jan 2008
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

	private static Log log = LogFactory.getLog(ExportResponsesBean.class);

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
        
        /*
        SpreadsheetUtil.downloadSpreadsheetData(getSpreadsheetData(), 
        		getDownloadFileName(), 
        		new SpreadsheetDataFileWriterXls());
		*/
        
        // changed from above by gopalrc - Jan 2008
        // to allow local customization of spreadsheet output
        FacesContext faces = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse)faces.getExternalContext().getResponse();
        response.reset();	// Eliminate the added-on stuff
        response.setHeader("Pragma", "public");	// Override old-style cache control
        response.setHeader("Cache-Control", "public, must-revalidate, post-check=0, pre-check=0, max-age=0");	// New-style
       	writeDataToResponse(getSpreadsheetData(), getDownloadFileName(), response);
       	faces.responseComplete();
    }
	
    private List<List<Object>> getSpreadsheetData() {
        // gopalrc Dec 2007
        HistogramListener histogramListener = new HistogramListener();
  	  	Iterator detailedStats = histogramListener.getDetailedStatisticsSpreadsheetData(assessmentId).iterator(); 
  	  	boolean showPartAndTotalScoreSpreadsheetColumns = (Boolean) detailedStats.next();
  	  	//boolean showDiscriminationColumn = (Boolean) detailedStats.next();
  		boolean showDetailedStatisticsSheet = (Boolean) detailedStats.next();
  	  	
  	  	String audioMessage = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages","audio_message");
    	String fileUploadMessage = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages","file_upload_message");
        GradingService gradingService = new GradingService();
        String questionString = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages","question");
        String textString = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages","text");
        String rationaleString = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages","rationale");
        List exportResponsesDataList = gradingService.getExportResponsesData(assessmentId, anonymous, audioMessage, fileUploadMessage, showPartAndTotalScoreSpreadsheetColumns, questionString, textString, rationaleString);
        List<List<Object>> list = (List<List<Object>>) exportResponsesDataList.get(0);

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
  	  	
  	  	// gopalrc - Nov 2007
        if (showPartAndTotalScoreSpreadsheetColumns) {
	  	  	int numberOfSections = pubService.getPublishedSectionCount(Long.valueOf(assessmentId)).intValue();
	  	  	if (numberOfSections > 1) {
		  	  	for (int i = 1; i <= numberOfSections; i++) {
		    		  headerList.add(ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages","part") 
		    				  + " " + i + " " + ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages","score"));
		    	}
	  	  	}
	        
	        headerList.add(ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages","tot"));
        }
        headerList.addAll((ArrayList) exportResponsesDataList.get(1));
  	  	
  	    list.add(0,headerList);
  	  	
        // gopalrc - Jan 2008 - New Sheet Marker
  		ArrayList<Object> newSheetList;
  	  	newSheetList = new ArrayList<Object>();
  	  	newSheetList.add(NEW_SHEET_MARKER);
  	  	newSheetList.add(ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages","responses"));
  	  	list.add(0, newSheetList);

        // gopalrc - Jan 2008 - New Sheet Marker
  	  	if (showDetailedStatisticsSheet) {
  	  		newSheetList = new ArrayList<Object>();
  	  		newSheetList.add(NEW_SHEET_MARKER);
  	  		newSheetList.add(ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages","item_analysis"));
  	  		list.add(newSheetList);

  	  		// gopalrc Dec 2007
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
		DateFormat df = new SimpleDateFormat(dateFormat);
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
		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");

		OutputStream out = null;
		try {
			out = response.getOutputStream();
			getAsWorkbook(spreadsheetData).write(out);
			out.flush();
		} catch (IOException e) {
			if (log.isErrorEnabled()) log.error(e);
		} finally {
			try {
				if (out != null) out.close();
			} catch (IOException e) {
				if (log.isErrorEnabled()) log.error(e);
			}
		}
	}

	private HSSFWorkbook getAsWorkbookTest(List<List<Object>> spreadsheetData) {
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet();
		Iterator<List<Object>> dataIter = spreadsheetData.iterator();

		// By convention, the first list in the list contains column headers.
		HSSFRow headerRow = sheet.createRow((short)0);
		List<Object> headerList = dataIter.next();
		for (short i = 0; i < headerList.size(); i++) {
			createCell(headerRow, i, null).setCellValue(headerList.get(i).toString());
		}
		short rowPos = 1;
		while (dataIter.hasNext()) {
			List<Object> rowData = dataIter.next();
			HSSFRow row = sheet.createRow(rowPos++);
			for (short i = 0; i < rowData.size(); i++) {
				HSSFCell cell = createCell(row, i, null);
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
	
	private HSSFWorkbook getAsWorkbook(List<List<Object>> spreadsheetData) {
		HSSFWorkbook wb = new HSSFWorkbook();

		HSSFCellStyle boldStyle = wb.createCellStyle();
		HSSFFont font = wb.createFont();
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		boldStyle.setFont(font);
		HSSFCellStyle headerStyle = boldStyle;
		
		HSSFSheet sheet = null;

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
				HSSFRow headerRow = sheet.createRow(rowPos++);
				for (short i = 0; i < rowData.size()-1; i++) {
					createCell(headerRow, i, headerStyle).setCellValue(rowData.get(i+1).toString());
				}
			}
			else {
				HSSFRow row = sheet.createRow(rowPos++);
				short colPos = 0;
				Iterator colIter = rowData.iterator();
				while (colIter.hasNext()) {
				//for (short i = 0; i < rowData.size(); i++) {
					HSSFCell cell = null;
					
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
							cell = createCell(row, colPos++, null);
						}
						
						if (cell != null) {
							if (data instanceof Double) {
								cell.setCellValue(((Double)data).doubleValue());
							} else {
								cell.setCellValue(data.toString());
							}
						}
					}
				}
			}
			
		}
		
		return wb;
	}

	
	private HSSFCell createCell(HSSFRow row, short column, HSSFCellStyle cellStyle) {
		HSSFCell cell = row.createCell(column);
		cell.setEncoding(HSSFCell.ENCODING_UTF_16);
		
		if (cellStyle != null) {
			cell.setCellStyle(cellStyle);
		}
		
		return cell;
	}
	
    
    
}
