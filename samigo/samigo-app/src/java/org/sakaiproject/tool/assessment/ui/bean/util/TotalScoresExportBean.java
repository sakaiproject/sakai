/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2023 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.ui.bean.util;

import java.io.Serializable;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Iterator;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.sakaiproject.tool.assessment.ui.bean.evaluation.AgentResults;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

@Slf4j
@ManagedBean(name="totalScoresExportBean")
@SessionScoped
public class TotalScoresExportBean implements Serializable {

	private final String EVALUATION_MESSAGES_BUNDLE = "org.sakaiproject.tool.assessment.bundle.EvaluationMessages";

	/**
	 * Creates a new TotalScoresExportBean object.
	 */
	public TotalScoresExportBean() {
		log.debug("Creating a new TotalScoresExportBean");
	}

	public void exportExcel(String assessmentName, List allAgents) {
		// Now insert the header line
		List<String> headerList = new ArrayList<>();
		headerList.add(ContextUtil.getLocalizedString(EVALUATION_MESSAGES_BUNDLE, "first_name"));
		headerList.add(ContextUtil.getLocalizedString(EVALUATION_MESSAGES_BUNDLE, "uid"));
		headerList.add(ContextUtil.getLocalizedString(EVALUATION_MESSAGES_BUNDLE, "role"));
		headerList.add(ContextUtil.getLocalizedString(EVALUATION_MESSAGES_BUNDLE, "submit_date"));
		headerList.add(ContextUtil.getLocalizedString(EVALUATION_MESSAGES_BUNDLE, "status"));
		headerList.add(ContextUtil.getLocalizedString(EVALUATION_MESSAGES_BUNDLE, "tot"));
		headerList.add(ContextUtil.getLocalizedString(EVALUATION_MESSAGES_BUNDLE, "adj"));
		headerList.add(ContextUtil.getLocalizedString(EVALUATION_MESSAGES_BUNDLE, "final"));
		headerList.add(ContextUtil.getLocalizedString(EVALUATION_MESSAGES_BUNDLE, "comment"));

		FacesContext faces = FacesContext.getCurrentInstance();
		HttpServletResponse response = (HttpServletResponse) faces.getExternalContext().getResponse();
		response.reset();	// Eliminate the added-on stuff
		response.setHeader("Cache-Control", "public, must-revalidate, post-check=0, pre-check=0, max-age=0");	// New-style
		writeDataToResponse(headerList, allAgents, getDownloadFileName(assessmentName), response);
		faces.responseComplete();
	}

	private String getDownloadFileName(String assessmentName) {
		Date now = new Date();
		String dateFormat = "yyyyMMdd";
		DateFormat df = new SimpleDateFormat(dateFormat);
		StringBuilder fileName = new StringBuilder(ContextUtil.getLocalizedString(EVALUATION_MESSAGES_BUNDLE, "assessment"));
		if(StringUtils.trimToNull(assessmentName) != null) {
			assessmentName = assessmentName.replaceAll("\\s", "_"); // replace whitespace with '_'
			fileName.append("-");
			fileName.append(assessmentName);
		}
		fileName.append("-");
		fileName.append(df.format(now));
		return fileName.toString();
	}

	private void writeDataToResponse(List<String> headerList, List dataList, String fileName, HttpServletResponse response) {
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xlsx");
		OutputStream out = null;
		try {
			out = response.getOutputStream();
			getAsWorkbook(headerList, dataList).write(out);
			out.flush();
		} catch (IOException e) {
			if (log.isErrorEnabled()) log.error(e.getMessage());
		} finally {
			try {
				if (out != null) out.close();
			} catch (IOException e) {
				if (log.isErrorEnabled()) log.error(e.getMessage());
			}
		}
	}

	private XSSFWorkbook getAsWorkbook(List<String> headerList, List dataList) {
		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFCellStyle boldStyle = wb.createCellStyle();
		XSSFFont font = wb.createFont();
		font.setBold(true);
		boldStyle.setFont(font);

		XSSFSheet sheet = wb.createSheet();

		//The first list in the list contains column headers.
		XSSFRow headerRow = sheet.createRow((short)0);

		for (short i = 0; i < headerList.size(); i++) {
			createCell(headerRow, i, boldStyle).setCellValue(headerList.get(i).toString());
		}
		short rowPos = 1;
		Iterator dataIter = dataList.iterator();
		while (dataIter.hasNext()) {
			AgentResults agent = (AgentResults) dataIter.next();
			XSSFRow row = sheet.createRow(rowPos++);

			//Añadimos la informacion en las celdas
			XSSFCell cell = createCell(row, (short)0, null);
			cell.setCellValue(agent.getLastName() + ", " + agent.getFirstName());

			cell = createCell(row, (short)1, null);
			cell.setCellValue(agent.getAgentEid());

			cell = createCell(row, (short)2, null);
			cell.setCellValue(agent.getRole());

			cell = createCell(row, (short)3, null);
			Date date = agent.getSubmittedDate(); 
			if (date!=null){
				cell.setCellValue(date.toString());
			}else{
				cell.setCellValue(ContextUtil.getLocalizedString(EVALUATION_MESSAGES_BUNDLE,"no_submission"));
			}

			cell = createCell(row, (short)4, null);
			cell.setCellValue(agent.getStatus() != null ? agent.getStatus().toString(): "");

			cell = createCell(row, (short)5, null);
			cell.setCellValue(agent.getRoundedTotalAutoScore());

			cell = createCell(row, (short)6, null);
			cell.setCellValue(agent.getRoundedTotalOverrideScore());

			cell = createCell(row, (short)7, null);
			cell.setCellValue(agent.getRoundedFinalScore());

			cell = createCell(row, (short)8, null);
			cell.setCellValue(agent.getComments());
		}
		return wb;
	}

	private XSSFCell createCell(XSSFRow row, short column, XSSFCellStyle cellStyle) {
		XSSFCell cell = row.createCell(column);

		if (cellStyle != null) {
			cell.setCellStyle(cellStyle);
		}

		return cell;
	}

}
