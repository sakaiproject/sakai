package org.sakaiproject.tool.assessment.integration.helper.integrated;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.emailtemplateservice.service.EmailTemplateService;
import org.sakaiproject.tool.assessment.integration.helper.ifc.EmailServiceHelper;

public class EmailServiceHelperImpl implements EmailServiceHelper {

	private static final Log log = LogFactory.getLog(EmailServiceHelperImpl.class);

	public static final String TEMPLATE_PUBLISHED_ASSESMENT = "samigo.publishAssessment";
	public static final String ADMIN = "admin";

	private EmailService emailService;
	private EmailTemplateService emailTemplateService;
	private List<String> emailTemplates;

	public void init() {
		log.info("init()");
		emailTemplateService.processEmailTemplates(emailTemplates);
	}

	public List<String> getEmailTemplates() {
		return emailTemplates;
	}

	public void setEmailTemplates(List<String> emailTemplates) {
		this.emailTemplates = emailTemplates;
	}

	public EmailService getEmailService() {
		return emailService;
	}

	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}

	public EmailTemplateService getEmailTemplateService() {
		return emailTemplateService;
	}

	public void setEmailTemplateService(EmailTemplateService emailTemplateService) {
		this.emailTemplateService = emailTemplateService;
	}

	
}
