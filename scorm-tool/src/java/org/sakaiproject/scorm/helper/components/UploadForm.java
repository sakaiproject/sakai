/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
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
package org.sakaiproject.scorm.helper.components;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.adl.validator.IValidatorOutcome;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.request.target.basic.RedirectRequestTarget;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.lang.Bytes;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ResourceToolActionPipe;
import org.sakaiproject.scorm.client.api.ScormClientFacade;
import org.sakaiproject.util.Xml;
import org.w3c.dom.Document;

/*
 * Note: the accessors are all hacked up here because I can't get RequestFilter to stop 
 * interfering with my multipart requests. Wicket has a much nicer way of doing this, and if
 * RequestFilter wasn't there it would populate all these member variables via the setters. 
 */
public class UploadForm extends Form {
	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(UploadForm.class);		
	private static final String FILE_UPLOAD_MAX_SIZE_CONFIG_KEY = "content.upload.max";	
	private static final String CONTENT_TYPE_APPLICATION_ZIP = "application/zip";
	private static final String MANIFEST_FILE_LOCATION = "imsmanifest.xml";
	
	
	@SpringBean
	ScormClientFacade clientFacade;
	
	private FileUpload fileInput;
	private String displayName;
	private String description;
	private String copyright;
	private String userCopyright;
	private boolean copyrightAlert;

	
	/*
	 * Constructor
	 */
	public UploadForm(String id)
	{
		super(id);
	
		IModel model = new CompoundPropertyModel(this);
		this.setModel(model);
		
		// All upload forms need to be encrypted as multipart/form-data
		setMultiPart(true);
		// We need to establish the largest file allowed to be uploaded
		setMaxSize(Bytes.kilobytes(findMaxFileUploadSize()));	
		
		final DetailsPanel detailsPanel = new DetailsPanel("details", this, model);
		// Control visibility
		detailsPanel.setVisible(false);
		
		final Component parent = this;
		
		AjaxFallbackLink showDetailsLink = new AjaxFallbackLink("showDetails") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				detailsPanel.setVisible(!detailsPanel.isVisible());
				StringBuffer labelName = new StringBuffer().append("detailsLabel-")
					.append(String.valueOf(detailsPanel.isVisible()));
				
				this.addOrReplace(new Label("detailsLabel", new StringResourceModel(labelName.toString(), parent, null)));
				target.appendJavascript("setMainFrameHeight( window.name )");
				target.addComponent(parent);
			}
		};
		
		showDetailsLink.add(new Label("detailsLabel", new StringResourceModel("detailsLabel-false", this, null)));
		
		add(new NotificationPanel("uploadFeedback")); 		
		add(newResourceLabel("fileToUploadLabel", this));
		add(newResourceLabel("displayNameLabel", this));
		add(new TextField("displayName"));
		add(new FileUploadField("fileInput"));
		add(detailsPanel);
		add(showDetailsLink);
	}
	
	public final void notify(String key) {
		String message = getLocalizer().getString(key, this);
		Session.get().getFeedbackMessages().warn(this, message);
	}
	
	protected Label newResourceLabel(String id, Component component) {
		return new Label(id, new StringResourceModel(id, component, null));
	}
	
	private boolean validate(FileItem fileItem) {
		File contentPackage = getContentPackage(fileItem);
		IValidatorOutcome validatorOutcome = clientFacade.validateContentPackage(contentPackage);

		if (!contentPackage.exists()) {
			notify("noFile");
			return false;
		}
		
		if (!validatorOutcome.getDoesIMSManifestExist()) {
			notify("noManifest");
			return false;
		}
		
		if (!validatorOutcome.getIsWellformed()) {
			notify("notWellFormed");
			return false;
		}
		
		if (!validatorOutcome.getIsValidRoot()) {
			notify("notValidRoot");
			return false;
		}
		
		if (!validatorOutcome.getIsValidToSchema()) {
			notify("notValidSchema");
			return false;
		}
		
		if (!validatorOutcome.getIsValidToApplicationProfile()) {
			notify("notValidApplicationProfile");
			return false;
		}
		
		if (!validatorOutcome.getDoRequiredCPFilesExist()) {
			notify("notExistingRequiredFiles");
			return false;
		}
		
		Document doc = validatorOutcome.getDocument();
		
		String str = Xml.writeDocumentToString(doc);
		
		System.out.println(str);
				
		return true;
	}
	
	
	protected void onSubmit() {
		FileItem fileItem = (FileItem)((WebRequest)getRequest()).getHttpServletRequest().getAttribute("fileInput");

		//validate(fileItem);
		if (!validate(fileItem))
			return;
		
		ResourceToolActionPipe pipe = clientFacade.getResourceToolActionPipe();
		
		ContentEntity entity = pipe.getContentEntity();
		ContentCollection containingCollection = null;
		
		if(entity != null && entity instanceof ContentCollection)
		{
			containingCollection = (ContentCollection) entity;
		}
		
		InputStream stream = null;
		try {
			byte[] bytes = fileItem.get();
			
			if (null != bytes) {
				pipe.setRevisedContent(bytes);
			} else {
				stream = fileItem.getInputStream();
				pipe.setRevisedContentStream(stream);
			}
						
			String contentType = fileItem.getContentType();
            pipe.setRevisedMimeType(contentType);
			
            String filename = fileItem.getName();
            
            if (null != getDisplayName() && getDisplayName().trim().length() > 0)
            	filename = getDisplayName();
            
            pipe.setFileName(filename);
            
            //parseManifest(fileItem);
                        
            pipe.setActionCanceled(false);
            pipe.setErrorEncountered(false);
            pipe.setActionCompleted(true); 
		} catch (IOException ioe) {
			log.error("Caught an io exception trying to upload file!", ioe);
			info("Unable to save this file...");
		} finally {
			if (null != pipe)
				clientFacade.closePipe(pipe);
			if (null != stream)
				try { 
					stream.close();
				} catch (IOException nioe) {
					log.info("Caught an io exception trying to close stream!", nioe);
				}
		}
		
		String url = clientFacade.getCompletionURL();
		
		if (null != url) {
			getRequestCycle().setRequestTarget(new RedirectRequestTarget(url));
		}
	}
	
	private File getDirectory() {		
		return (File)((WebRequest)getRequest()).getHttpServletRequest().getSession().getServletContext().getAttribute("javax.servlet.context.tempdir");
	}
	
	private File getContentPackage(FileItem fileItem) {
		File contentPackage = new File(getDirectory(), fileItem.getName());

		byte[] bytes = fileItem.get();
		
		InputStream in = null;
		FileOutputStream out = null;
		
		try {
			if (null == bytes) {
				in = fileItem.getInputStream();
			} else {
				in = new ByteArrayInputStream(bytes);
			}
		
			out = new FileOutputStream(contentPackage);
			
			byte[] buffer = new byte[1024];
			int length;
			
			while ((length = in.read(buffer)) > 0) {  
    			out.write(buffer, 0, length);
            }
			
		} catch (IOException ioe) {
			log.error("Caught an io exception retrieving the uploaded content package!", ioe);
		} finally {
			if (null != out)
				try {
					out.close();
				} catch (IOException nioe) {
					log.info("Caught an io exception closing the output stream!", nioe);
				}
		}
		
		return contentPackage;
	}
	
	
	private void parseManifest(FileItem item) {
		String contentType = item.getContentType();
		
		if (!CONTENT_TYPE_APPLICATION_ZIP.equals(contentType))
			return;
		
		byte[] bytes = item.get();
		
		File manifestFile = findManifestFile(bytes);
		
		System.out.println("MANIFEST at " + manifestFile.getAbsolutePath());
	}
	
	private File findManifestFile(byte[] archive) {
		File manifestFile = null; 
		
		ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream(archive));
		ZipEntry entry;
		String entryName;
		byte[] buffer = new byte[1024];
		int length;
		
		FileOutputStream out = null;
		
		try {
			manifestFile = File.createTempFile("manifest", ".xml");
			entry = (ZipEntry) zipStream.getNextEntry();
			out = new FileOutputStream(manifestFile);
			while (entry != null) {
		    	entryName = entry.getName();
		    	if (entryName.endsWith(MANIFEST_FILE_LOCATION)) {
		    		while ((length = zipStream.read(buffer)) > 0) {  
		    			out.write(buffer, 0, length);
		            }
		    		
		    		out.close();
		    		zipStream.closeEntry();
		    		zipStream.close();
		    		return manifestFile;
		    	}
		    	entry = (ZipEntry) zipStream.getNextEntry();
		    }
		} catch (IOException ioe) {
			log.error("Caught an io exception writing manifest file to temp space!", ioe);
			return null;
		} finally {
			try {
				if (null != out)
					out.close();
				if (null != zipStream)
					zipStream.close();
			} catch (IOException noie) {
				log.info("Caught an io exception closing streams!");
			}
		}
		
		return null;
	}
	
	

	
	
	/*protected void onSubmit()
	{	
		
		FileUpload fileUpload = fileUploadField.getFileUpload();
		
		if (null == fileUpload) {
			log.error("File upload object is null!");
			return;
		}
		
		ResourceToolActionPipe pipe = clientFacade.getResourceToolActionPipe();
		
		if (null == pipe) {
			log.error("No pipe found when uploading file!");
			return;
		}
		
		String fileName = "Test"; //getDisplayName();
		if (null == fileName || fileName.trim().length() <= 0) {
			fileName = fileUpload.getClientFileName();
		
			if (null == fileName || fileName.trim().length() <= 0) {
				log.error("No file name provided!");
				return;
			}
		}
				
		try {
			// First, let's check to see if the data is available via the stream class
			InputStream inputStream = fileUpload.getInputStream();
			// If not, grab it from bytes
			if (null == inputStream) {
				byte[] bytes = fileUpload.getBytes();
				pipe.setRevisedContent(bytes);
			} else {
				pipe.setRevisedContentStream(inputStream);
			}
			String contentType = fileUpload.getContentType();
			pipe.setRevisedMimeType(contentType);
			pipe.setFileName(fileName);
			//pipe.setFileName((String)fileNameInput.getValue());
		} catch (IOException ioe) {
			log.warn("Caught an io exception trying to write input stream to content api", ioe);
		} finally {
			fileUpload.closeStreams();
		}
	}*/
	
	
	private int findMaxFileUploadSize() {
		String maxSize = null;
		int kiloBytes = 1;
		try {
			maxSize = clientFacade.getConfigurationString(FILE_UPLOAD_MAX_SIZE_CONFIG_KEY, "1");
			if (null == maxSize)
				log.warn("The sakai property '" + FILE_UPLOAD_MAX_SIZE_CONFIG_KEY + "' is not set!");
			else
				kiloBytes = Integer.parseInt(maxSize);
		} catch(NumberFormatException nfe) {
			log.error("Failed to parse " + maxSize + " as an integer ", nfe);
		}
		
		return kiloBytes;
	}

	public String getCopyright() {
		return ((WebRequest)getRequest()).getHttpServletRequest().getParameter("copyright");
	}

	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}

	public boolean getCopyrightAlert() {
		return Boolean.valueOf(((WebRequest)getRequest()).getHttpServletRequest().getParameter("copyrightAlert"));
	}

	public void setCopyrightAlert(boolean copyrightAlert) {
		this.copyrightAlert = copyrightAlert;
	}
	
	public String getDescription() {
		return ((WebRequest)getRequest()).getHttpServletRequest().getParameter("description");
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDisplayName() {
		return ((WebRequest)getRequest()).getHttpServletRequest().getParameter("displayName");
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public FileUpload getFileInput() {
		return fileInput;
	}

	public void setFileInput(FileUpload fileUpload) {
		this.fileInput = fileUpload;
	}

	public String getUserCopyright() {
		return ((WebRequest)getRequest()).getHttpServletRequest().getParameter("userCopyright");
	}

	public void setUserCopyright(String userCopyright) {
		this.userCopyright = userCopyright;
	}
	
	
}
