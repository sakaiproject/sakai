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
package org.sakaiproject.scorm.ui.helper.pages;

import java.io.File;

import org.adl.validator.IValidatorOutcome;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.scorm.client.api.ScormClientFacade;
import org.sakaiproject.scorm.ui.components.UploadForm;
import org.sakaiproject.wicket.markup.html.SakaiPortletWebPage;

public class UploadContentPackage extends SakaiPortletWebPage {
	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(UploadContentPackage.class);
	
	@SpringBean
	ScormClientFacade clientFacade;
	
	public UploadContentPackage(PageParameters parameters) {		
		final UploadForm form = new UploadForm("uploadForm") {
			private static final long serialVersionUID = 1L;

			protected void onSubmit() {
				FileItem fileItem = getFileItem();		
				File contentPackage = getFile(fileItem);
				
				if (contentPackage == null)
					return;
				
				boolean onlyValidateManifest = getDontValidateSchema();
				IValidatorOutcome outcome = validate(this, contentPackage, onlyValidateManifest);
				
				if (null == outcome)
					return;
				
				String url = clientFacade.getCompletionURL();		
				exit(url);
			}
		
			private IValidatorOutcome validate(UploadForm form, File contentPackage, boolean onlyValidateManifest) {
				IValidatorOutcome validatorOutcome = clientFacade.validateContentPackage(contentPackage, onlyValidateManifest);

				if (!contentPackage.exists()) {
					form.notify("noFile");
					return null;
				}
				
				if (!validatorOutcome.getDoesIMSManifestExist()) {
					form.notify("noManifest");
					return null;
				}
				
				if (!validatorOutcome.getIsWellformed()) {
					form.notify("notWellFormed");
					return null;
				}
				
				if (!validatorOutcome.getIsValidRoot()) {
					form.notify("notValidRoot");
					return null;
				}
				
				if (!onlyValidateManifest) {
					if (!validatorOutcome.getIsValidToSchema()) {
						form.notify("notValidSchema");
						return null;
					}
					
					if (!validatorOutcome.getIsValidToApplicationProfile()) {
						form.notify("notValidApplicationProfile");
						return null;
					}
					
					if (!validatorOutcome.getDoRequiredCPFilesExist()) {
						form.notify("notExistingRequiredFiles");
						return null;
					}
				}
				
				return validatorOutcome;
			}
		
		
		};
		form.setOutputMarkupId(true); 
		
		add(newResourceLabel("title", this));
		add(form); 
		form.add(new CheckBox("dontValidateSchema"));
		form.add(newResourceLabel("validateSchemaCaption", this));
	}
	
	
	
}
