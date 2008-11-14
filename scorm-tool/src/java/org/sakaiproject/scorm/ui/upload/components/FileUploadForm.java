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
/*
 * Contains code copied from org.apache.wicket.examples.upload.UploadPage, 
 * authored by Eelco Hillenius, and originally licensed under the following
 * license:
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.scorm.ui.upload.components;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.request.target.basic.RedirectRequestTarget;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.file.Files;
import org.apache.wicket.util.file.Folder;
import org.apache.wicket.util.lang.Bytes;
import org.sakaiproject.scorm.service.api.ScormResourceService;
import org.sakaiproject.scorm.ui.AbsoluteUrl;
import org.sakaiproject.scorm.ui.player.ScormTool;

public class FileUploadForm extends Form {

	private static final long serialVersionUID = 1L;

	private static Log log = LogFactory.getLog(FileUploadForm.class);			
	private static final String CONTENT_TYPE_APPLICATION_ZIP = "application/zip";
	
	@SpringBean
	ScormResourceService resourceService;
	
	private FileUploadField fileUploadField;
	private boolean fileHidden = false;
	
	public FileUploadForm(String id) {
		super(id);
		
		IModel model = new CompoundPropertyModel(this);
		this.setModel(model);
		
		// We need to establish the largest file allowed to be uploaded
		setMaxSize(Bytes.megabytes(resourceService.getMaximumUploadFileSize()));
		
		setMultiPart(true);
		
		addOrReplace(fileUploadField = new FileUploadField("fileInput"));
		addOrReplace(new CheckBox("fileHidden"));
	}
	
	public boolean isFileAvailable() {
		if (fileUploadField != null) {
			FileUpload upload = fileUploadField.getFileUpload();
	        if (upload != null)
	        	return upload.getSize() != 0;
		}
		
		return false;
	}
	
	protected void onSubmit() {
		if (fileUploadField != null) {
			final FileUpload upload = fileUploadField.getFileUpload();
	        if (upload != null) {
	            try {
	            	resourceService.putArchive(upload.getInputStream(), upload.getClientFileName(), upload.getContentType(), isFileHidden(), 0);
	            }
	            catch (Exception e)
	            {
	            	notify("noFile");
	                log.error("Failed to upload file", e);
	            }
	        }
		}
	}
	
	public String doUpload() {
		if (fileUploadField != null) {
			final FileUpload upload = fileUploadField.getFileUpload();
	        if (upload != null)
	        {
	            // Create a new file
	        	//Folder folder = getUploadFolder();
	            //File newFile = new File(folder.getAbsoluteFile(), upload.getClientFileName());
	            
	            // Check new file, delete if it already existed
	            //checkFileExists(newFile);
	            try
	            {
	                // Save to new file
	                /*newFile.createNewFile();
	                upload.writeTo(newFile);
	
	                return newFile;*/
	            	
	            	
	            	resourceService.putArchive(upload.getInputStream(), upload.getClientFileName(), upload.getContentType(), isFileHidden(), 0);
	            	

	            }
	            catch (Exception e)
	            {
	                throw new IllegalStateException("Unable to write file", e);
	            }
	        }
		}
		notify("noFile");
		return null;
	}
	
	/*
	 * This code is copied from org.apache.wicket.examples.upload.UploadPage
	 * 
	 */
	private void removeExistingFile(File newFile)
    {
        if (newFile.exists())
        {
            // Try to delete the file
            if (!Files.remove(newFile)) {
                throw new IllegalStateException("Unable to overwrite " + newFile.getAbsolutePath());
            }
        }
    }
	
	
	public final void notify(String key) {
		String message = null;
		try {
			message = getLocalizer().getString(key, this);
		} catch (Exception e) {
			log.warn("Unable to find the message for key: " + key);
		}
		
		if (message == null)
			message = key;
		
		Session.get().getFeedbackMessages().warn(this, message);
	}
	
	protected Label newResourceLabel(String id, Component component) {
		return new Label(id, new StringResourceModel(id, component, null));
	}
	
	
	public void exit(String url) {
		if (null != url) {
			AbsoluteUrl absUrl = new AbsoluteUrl(getRequest(), url, true, false);
			String fullUrl = absUrl.toString();
			getRequestCycle().setRequestTarget(new RedirectRequestTarget(absUrl.toString()));
		}
	}
	
	private File getDirectory() {		
		return (File)((WebRequest)getRequest()).getHttpServletRequest().getSession().getServletContext().getAttribute("javax.servlet.context.tempdir");
	}
	
	public File getFile(FileItem fileItem) {
		if (null == fileItem || fileItem.getSize() <= 0) {
			notify("noFile");
			return null;
		}
		

		if (!CONTENT_TYPE_APPLICATION_ZIP.equals(fileItem.getContentType())) {
			notify("wrongContentType");
		}


		String filename = fileItem.getName();
        
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

	 private Folder getUploadFolder() {
	        return ((ScormTool)Application.get()).getUploadFolder();
	 }

	public boolean isFileHidden() {
		return fileHidden;
	}

	public void setFileHidden(boolean fileHidden) {
		this.fileHidden = fileHidden;
	}

}
