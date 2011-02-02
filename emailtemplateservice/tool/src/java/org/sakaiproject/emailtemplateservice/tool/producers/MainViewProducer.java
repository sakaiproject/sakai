package org.sakaiproject.emailtemplateservice.tool.producers;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.emailtemplateservice.model.EmailTemplate;
import org.sakaiproject.emailtemplateservice.service.EmailTemplateService;
import org.sakaiproject.emailtemplateservice.service.external.ExternalLogic;
import org.sakaiproject.emailtemplateservice.tool.params.EmailTemplateViewParams;

import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.DefaultView;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;

public class MainViewProducer implements ViewComponentProducer, DefaultView {

	public static final String VIEW_ID = "main";

	public String getViewID() {
		// TODO Auto-generated method stub
		return VIEW_ID;
	}

	private static Log log = LogFactory.getLog(MainViewProducer.class);

	private EmailTemplateService emailTemplateService;
	public void setEmailTemplateService(EmailTemplateService ets) {
		emailTemplateService = ets;
	}


	private ExternalLogic externalLogic;
	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}

	private TargettedMessageList messages;
	public void setMessages(TargettedMessageList messages) {
		this.messages = messages;
	}


	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {

		//is this user admin?
		if (!externalLogic.isSuperUser()) {
			messages.addMessage(new TargettedMessage("tool.notAdmin", new Object[]{}, TargettedMessage.SEVERITY_ERROR));
			return;
		}

		UIBranchContainer navIntra = UIBranchContainer.make(tofill, "navintra:");
		UIInternalLink.make(navIntra,"actions-add", UIMessage.make("mainview.new") , new EmailTemplateViewParams(ModifyEmailProducer.VIEW_ID, null));


		UIBranchContainer table = UIBranchContainer.make(tofill, "table:");

		List<EmailTemplate> templates = emailTemplateService.getEmailTemplates(0, 0);
		for (int i =0; i < templates.size(); i++) {
			EmailTemplate template = templates.get(i);
			log.debug("got template: " + template.getKey());
			UIBranchContainer row = UIBranchContainer.make(table, "template-row:", template.getId().toString());
			UIOutput.make(row, "template-key", template.getKey());
			String locale = template.getLocale();
			if (locale == null )
				locale = "";
			UIOutput.make(row, "template-locale", locale);
			UIInternalLink.make(row,"template-edit" , UIMessage.make("mainview.edit"), new EmailTemplateViewParams(ModifyEmailProducer.VIEW_ID, template.getId().toString()));
			//UIInternalLink.make(row,"template-delete" , UIMessage.make("mainview.delete"), new EmailTemplateViewParams(ModifyEmailProducer.VIEW_ID, template.getId().toString()));
		}
	}

}
