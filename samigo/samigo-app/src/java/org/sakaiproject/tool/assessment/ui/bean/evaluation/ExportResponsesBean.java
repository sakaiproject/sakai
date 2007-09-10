/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/samigo-app/src/java/org/sakaiproject/tool/assessment/ui/bean/evaluation/TotalScoresBean.java $
 * $Id: TotalScoresBean.java 29431 2007-04-22 05:20:55Z ktsao@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the"License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tool.assessment.ui.bean.evaluation;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.faces.event.ActionEvent;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.jsf.model.PhaseAware;
import org.sakaiproject.jsf.spreadsheet.SpreadsheetDataFileWriterXls;
import org.sakaiproject.jsf.spreadsheet.SpreadsheetUtil;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.util.Validator;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Description: class form for evaluating total scores</p>
 *
 */
public class ExportResponsesBean implements Serializable, PhaseAware {
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
        SpreadsheetUtil.downloadSpreadsheetData(getSpreadsheetData(), 
        		getDownloadFileName(), 
        		new SpreadsheetDataFileWriterXls());
    }
	
    private List<List<Object>> getSpreadsheetData() {
    	String audioMessage = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages","audio_message");
    	String fileUploadMessage = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages","file_upload_message");
        GradingService gradingService = new GradingService();
        List<List<Object>> list = gradingService.getExportResponsesData(assessmentId, anonymous, audioMessage, fileUploadMessage);
        
        // Now insert the header line
        ArrayList<Object> headerList = new ArrayList<Object>();
        if (anonymous) {
  		  headerList.add(ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages","sub_id"));
  	  	}
  	  	else {
  		  headerList.add(ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages","last_name"));
  		  headerList.add(ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages","first_name"));
  		  headerList.add(ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages","user_name"));
  		  headerList.add(ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages","num_submission"));
  	  	}
        headerList.add(ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages","tot"));
  	  	PublishedAssessmentService pubService = new PublishedAssessmentService();
  	  	int numberOfQuestions = pubService.getPublishedItemCount(Long.valueOf(assessmentId)).intValue();
  	  	log.debug("numberOfQuestions=" + numberOfQuestions);
  	  	for (int i = 1; i <= numberOfQuestions; i++) {
  		  headerList.add(ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages","question") 
  				  + " " + i + " "
  				  + ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages","text"));
  	  	}
  	  	list.add(0,headerList);
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
}
