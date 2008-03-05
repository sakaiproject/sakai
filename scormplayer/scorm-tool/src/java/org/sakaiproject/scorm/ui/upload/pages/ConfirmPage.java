package org.sakaiproject.scorm.ui.upload.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.scorm.api.ScormConstants;
import org.sakaiproject.scorm.model.api.Archive;
import org.sakaiproject.scorm.service.api.ScormResourceService;
import org.sakaiproject.scorm.ui.console.pages.ConsoleBasePage;

public class ConfirmPage extends ConsoleBasePage implements ScormConstants {

	private static final long serialVersionUID = 1L;

	@SpringBean
	ScormResourceService resourceService;
	
	public ConfirmPage(PageParameters params) {
		final String resourceId = params.getString("resourceId");
		final int status = params.getInt("status");
		
		Archive archive = resourceService.getArchive(resourceId);
		
		if (archive != null) {
			setModel(new CompoundPropertyModel(archive));
		}
	
		info(getNotification(status));
		
		add(new Label("title"));
	}
	
	private String getNotification(int status) {
		String resultKey = getKey(status);
		
		return getLocalizer().getString(resultKey, this);
	}
	
	private String getKey(int status) {
		switch (status) {
		case VALIDATION_SUCCESS:
			return "validate.success";
		case VALIDATION_WRONGMIMETYPE:
			return "validate.wrong.mime.type";
		case VALIDATION_NOFILE:
			return "validate.no.file";
		case VALIDATION_NOMANIFEST:
			return "validate.no.manifest";
		case VALIDATION_NOTWELLFORMED:
			return "validate.not.well.formed";
		case VALIDATION_NOTVALIDROOT:
			return "validate.not.valid.root";
		case VALIDATION_NOTVALIDSCHEMA:
			return "validate.not.valid.schema";
		case VALIDATION_NOTVALIDPROFILE:
			return "validate.not.valid.profile";
		case VALIDATION_MISSINGREQUIREDFILES:
			return "validate.missing.files";
		};
		return "validate.failed";
	}
}
