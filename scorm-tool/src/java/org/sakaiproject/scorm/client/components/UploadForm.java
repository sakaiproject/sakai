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
package org.sakaiproject.scorm.client.components;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.request.target.basic.RedirectRequestTarget;
import org.apache.wicket.settings.IApplicationSettings;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.lang.Bytes;
import org.sakaiproject.scorm.client.api.ScormClientFacade;

/*
 * Note: the fileItem accessor is all hacked up here because I can't get RequestFilter to stop 
 * interfering with my multipart requests. Wicket has a much nicer way of doing this. 
 * 
 * Also, you'll note that I'm messing with setMultiPart... also b/c of the request filter stuff
 */
public abstract class UploadForm extends Form {
	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(UploadForm.class);		
	private static final String FILE_UPLOAD_MAX_SIZE_CONFIG_KEY = "content.upload.max";	
	private static final String CONTENT_TYPE_APPLICATION_ZIP = "application/zip";
	
	@SpringBean
	ScormClientFacade clientFacade;
	
	//private FileUpload fileInput;
	private FileItem fileItem;
	private String displayName;
	private String description;
	private String copyright;
	private String userCopyright;
	private boolean copyrightAlert;
	private boolean dontValidateSchema;

	
	/*
	 * Constructor
	 */
	public UploadForm(String id)
	{
		super(id);
		
		IModel model = new CompoundPropertyModel(this);
		this.setModel(model);
		
		// All upload forms need to be encrypted as multipart/form-data
		//setMultiPart(true);
		// We need to establish the largest file allowed to be uploaded
		setMaxSize(Bytes.megabytes(findMaxFileUploadSize()));	
		
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
		//add(new FileUploadField("fileInput"));
		add(detailsPanel);
		add(showDetailsLink);
		add(new Button("submitUpload"));
		Button cancelButton = new Button("cancel") {
			private static final long serialVersionUID = 1L;
			
			public void onSubmit()
			{
				String url = clientFacade.getCompletionURL();
				System.out.println("Cancel: Redirect to " + url);
				exit(url);
			}
		}.setDefaultFormProcessing(false);
		cancelButton.setOutputMarkupId(true);
		add(cancelButton);
	}
	
	public final void notify(String key) {
		String message = getLocalizer().getString(key, this);
		Session.get().getFeedbackMessages().warn(this, message);
	}
	
	protected Label newResourceLabel(String id, Component component) {
		return new Label(id, new StringResourceModel(id, component, null));
	}
	
	protected abstract void onSubmit();
	
	public void exit(String url) {
		if (null != url) {
			String contextPath = "https://psl-220.ucdavis.edu:6443";
			getRequestCycle().setRequestTarget(new RedirectRequestTarget(contextPath + url));
		}
	}
	
	private File getDirectory() {		
		return (File)((WebRequest)getRequest()).getHttpServletRequest().getSession().getServletContext().getAttribute("javax.servlet.context.tempdir");
	}
	
	protected File getFile(FileItem fileItem) {
		if (null == fileItem || fileItem.getSize() <= 0) {
			notify("noFile");
			return null;
		}
		

		if (!CONTENT_TYPE_APPLICATION_ZIP.equals(fileItem.getContentType())) {
			notify("wrongContentType");
		}


		String filename = fileItem.getName();
        
        if (null != getDisplayName() && getDisplayName().trim().length() > 0)
        	filename = getDisplayName();
        
		File file = new File(getDirectory(), filename);

		System.out.println("FILE IS: " + file.getAbsolutePath());
		
		byte[] bytes = fileItem.get();
		
		InputStream in = null;
		FileOutputStream out = null;
				
		try {
			if (null == bytes) {
				in = fileItem.getInputStream();
			} else {
				in = new ByteArrayInputStream(bytes);
			}
		
			out = new FileOutputStream(file);
			
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
		
		
		return file;
	}
		
	public void setMultiPart(boolean mtiPart)
	{
		super.setMultiPart(false);
	}
	
	private int findMaxFileUploadSize() {
		String maxSize = null;
		int megaBytes = 1;
		try {
			maxSize = clientFacade.getConfigurationString(FILE_UPLOAD_MAX_SIZE_CONFIG_KEY, "1");
			if (null == maxSize)
				log.warn("The sakai property '" + FILE_UPLOAD_MAX_SIZE_CONFIG_KEY + "' is not set!");
			else
				megaBytes = Integer.parseInt(maxSize);
		} catch(NumberFormatException nfe) {
			log.error("Failed to parse " + maxSize + " as an integer ", nfe);
		}
		
		return megaBytes;
	}

	public String getCopyright() {
		//return ((WebRequest)getRequest()).getHttpServletRequest().getParameter("copyright");
		return copyright;
	}

	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}

	public boolean getCopyrightAlert() {
		//return Boolean.valueOf(((WebRequest)getRequest()).getHttpServletRequest().getParameter("copyrightAlert"));
		return copyrightAlert;
	}

	public void setCopyrightAlert(boolean copyrightAlert) {
		this.copyrightAlert = copyrightAlert;
	}
	
	public String getDescription() {
		//return ((WebRequest)getRequest()).getHttpServletRequest().getParameter("description");
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDisplayName() {
		//return ((WebRequest)getRequest()).getHttpServletRequest().getParameter("displayName");
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/*public FileUpload getFileInput() {
		return fileInput;
	}

	public void setFileInput(FileUpload fileUpload) {
		this.fileInput = fileUpload;
	}*/

	public String getUserCopyright() {
		//return ((WebRequest)getRequest()).getHttpServletRequest().getParameter("userCopyright");
		return userCopyright;
	}

	public void setUserCopyright(String userCopyright) {
		this.userCopyright = userCopyright;
	}

	public boolean getDontValidateSchema() {
		/*String dontValidateString = ((WebRequest)getRequest()).getHttpServletRequest().getParameter("dontValidateSchema");
		Boolean dontValidate = Boolean.valueOf(((WebRequest)getRequest()).getHttpServletRequest().getParameter("dontValidateSchema"));

		if (null == dontValidate)
			return dontValidateSchema;
		return dontValidate;*/
		return dontValidateSchema;
	}

	public void setDontValidateSchema(boolean dontValidateSchema) {
		this.dontValidateSchema = dontValidateSchema;
	}

	public FileItem getFileItem() {
		return (FileItem)((WebRequest)getRequest()).getHttpServletRequest().getAttribute("fileInput");
	}

	public void setFileItem(FileItem fileItem) {
		this.fileItem = fileItem;
	}
	
	
}
