package org.sakaiproject.tool.assessment.ui.bean.samlite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacadeQueries;
import org.sakaiproject.tool.assessment.facade.AssessmentTemplateFacade;
import org.sakaiproject.tool.assessment.samlite.api.QuestionGroup;
import org.sakaiproject.tool.assessment.samlite.api.SamLiteService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.util.FormattedText;
import org.w3c.dom.Document;

public class SamLiteBean implements Serializable {
	private static Log log = LogFactory.getLog(SamLiteBean.class);
	private static final long serialVersionUID = -3122436861866172596L;
	public static final String DEFAULT_CHARSET = "ascii-us";

	private String name;
	private String description;
	private String data;
	private String assessmentTemplateId;
	
	private boolean isVisible = true;
	
	private AuthorBean authorBean;
	
	public void setAuthorBean(AuthorBean authorBean) {
		this.authorBean = authorBean;
	}
	
	private QuestionGroup questionGroup;
	private SamLiteService samLiteService;

	public void setSamLiteService(SamLiteService samLiteService) {
		this.samLiteService = samLiteService;
	}
	
	public SamLiteBean() {
		String samliteProperty = ServerConfigurationService.getString("samigo.samliteEnabled");
		if (null != samliteProperty && "false".equalsIgnoreCase(samliteProperty)) 
			isVisible = false;
	}
	
	public void parse() {
		questionGroup = samLiteService.parse(FormattedText.escapeHtml(name, false), 
				FormattedText.escapeHtml(description, false), 
				FormattedText.escapeHtml(data, false));
	}
	
	public Document createDocument() {
		return samLiteService.createDocument(questionGroup);
	}
	
	public void createAssessment(AssessmentFacade assessment) {		
	    authorBean.setAssessTitle("");
	    authorBean.setAssessmentDescription("");
	    authorBean.setAssessmentTypeId("");
	    authorBean.setAssessmentTemplateId(AssessmentTemplateFacade.DEFAULTTEMPLATE.toString());

	    AssessmentService assessmentService = new AssessmentService();
	    ArrayList list = assessmentService.getBasicInfoOfAllActiveAssessments(
	    		authorBean.getCoreAssessmentOrderBy(), authorBean.isCoreAscending());
	    Iterator iter = list.iterator();
		while (iter.hasNext()) {
			AssessmentFacade assessmentFacade= (AssessmentFacade) iter.next();
			assessmentFacade.setTitle(FormattedText.unEscapeHtml(assessmentFacade.getTitle()));
		}
	    authorBean.setAssessments(list);
	}
	
	public List getQuestions() {
		return questionGroup.getQuestions();
	}
	
	public QuestionGroup getQuestionGroup() {
		return questionGroup;
	}
	
	public void setQuestionGroup(QuestionGroup questionGroup) {
		this.questionGroup = questionGroup;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setAssessmentTemplateId(String assessmentTemplateId) {
	    this.assessmentTemplateId = assessmentTemplateId;
	}

	public String getAssessmentTemplateId() {
	    return assessmentTemplateId;
	}
	
	public boolean isVisible() {
		return isVisible;
	}
	
	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}
}
