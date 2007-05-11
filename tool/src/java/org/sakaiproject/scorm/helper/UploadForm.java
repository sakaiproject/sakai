package org.sakaiproject.scorm.helper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.scorm.client.api.ScormClientFacade;

import wicket.markup.html.form.Form;
import wicket.markup.html.form.upload.FileUploadField;
import wicket.spring.injection.annot.SpringBean;
import wicket.util.lang.Bytes;

public abstract class UploadForm extends Form {
	private static Log log = LogFactory.getLog(UploadForm.class);		
	private static final String FILE_UPLOAD_MAX_SIZE_CONFIG_KEY = "content.upload.max";	
	
	@SpringBean
	ScormClientFacade scormClientFacade;
	
	public UploadForm(String id)
	{
		super(id);
		
		// All upload forms need to be encrypted as multipart/form-data
		setMultiPart(true);
		// We need to establish the largest file allowed to be uploaded
		setMaxSize(Bytes.kilobytes(findMaxFileUploadSize()));		
	}
	
	private int findMaxFileUploadSize() {
		String maxSize = null;
		int kiloBytes = 1;
		try {
			maxSize = scormClientFacade.getConfigurationString(FILE_UPLOAD_MAX_SIZE_CONFIG_KEY, "1");
			if (null == maxSize)
				log.warn("The sakai property '" + FILE_UPLOAD_MAX_SIZE_CONFIG_KEY + "' is not set!");
			else
				kiloBytes = Integer.parseInt(maxSize);
		} catch(NumberFormatException nfe) {
			log.error("Failed to parse " + maxSize + " as an integer ", nfe);
		}
		
		return kiloBytes;
	}
	
	
}
