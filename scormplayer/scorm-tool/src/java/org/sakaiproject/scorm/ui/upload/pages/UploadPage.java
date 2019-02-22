/**
 * Copyright (c) 2007 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.scorm.ui.upload.pages;

import java.io.IOException;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.Component;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.feedback.FeedbackMessages;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.lang.Bytes;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.scorm.api.ScormConstants;
import org.sakaiproject.scorm.exceptions.ResourceStorageException;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.scorm.service.api.ScormResourceService;
import org.sakaiproject.scorm.ui.console.pages.ConsoleBasePage;
import org.sakaiproject.scorm.ui.console.pages.PackageListPage;
import org.sakaiproject.wicket.markup.html.form.CancelButton;

@Slf4j
public class UploadPage extends ConsoleBasePage
{
	@SpringBean( name = "org.sakaiproject.component.api.ServerConfigurationService" )
	ServerConfigurationService serverConfigurationService;

	@SpringBean(name="org.sakaiproject.scorm.service.api.ScormContentService")
	ScormContentService contentService;

	@SpringBean(name="org.sakaiproject.scorm.service.api.ScormResourceService")
	ScormResourceService resourceService;

	@Override
	protected void onInitialize()
	{
		super.onInitialize();
		uploadLink.disable();
	}

	public UploadPage(PageParameters params)
	{
		add(new FileUploadForm("uploadForm"));
	}

	// SCO-124 - this is needed becasue we can't disable the button in Java and enable it in JavaScript, because Wicket will throw a runtime exception.
	// So we have to both enable and disable the button via JavaScript.
	@Override
	public void renderHead( IHeaderResponse response )
	{
		super.renderHead( response );
		String javascript = "document.getElementsByName( \"btnSubmit\" )[0].disabled = true;";
		response.render( OnDomReadyHeaderItem.forScript( javascript ) );
	}

	public class FileUploadForm extends Form<Void>
	{
		private static final long serialVersionUID = 1L;
		private FileUploadField fileUploadField;

		public FileUploadForm(String id)
		{
			super(id);

			// We need to establish the largest file allowed to be uploaded
			setMaxSize(Bytes.megabytes(resourceService.getMaximumUploadFileSize()));

			// create a feedback panel, setMaxMessages not in this version
			final Component feedbackPanel = new FeedbackPanel("feedback").setOutputMarkupPlaceholderTag(true);

			setMultiPart(true);

			// Add JavaScript to enable the submit button only when there is a file selected (this cannot be done via Wicket/Java code)
			fileUploadField = new FileUploadField( "fileInput" );

			fileUploadField.add( new AttributeAppender( "onchange", new Model( "document.getElementsByName( \"btnSubmit\" )[0].disabled = this.value === '';" ), ";" ) );
			add( fileUploadField );

			// SCO-98 - disable buttons on submit, add spinner
			final CancelButton btnCancel = new CancelButton( "btnCancel", PackageListPage.class );
			IndicatingAjaxButton btnSubmit = new IndicatingAjaxButton( "btnSubmit", this )
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void onError(AjaxRequestTarget target)
				{
					FeedbackMessages feedbackMessages = this.getForm().getSession().getFeedbackMessages();
					if (!feedbackMessages.isEmpty())
					{
						log.info("Errors uploading file. {}", feedbackMessages.toString());
					}

					target.add(feedbackPanel);
				}

				@Override
				protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
				{
					super.updateAjaxAttributes(attributes);
					AjaxCallListener listener = new AjaxCallListener()
					{
						@Override
						public CharSequence getAfterHandler(Component component)
						{
							return "this.disabled = true; document.getElementsByName( \"btnCancel\" )[0].disabled = true;";
						}
					};
					attributes.getAjaxCallListeners().add(listener);
				}

				@Override
				protected void onSubmit( AjaxRequestTarget target )
				{
					final List<FileUpload> uploads = fileUploadField.getFileUploads();
					if( uploads != null )
					{
						for (FileUpload upload : uploads)
						{
							try
							{
								String resourceId = resourceService.putArchive( upload.getInputStream(), upload.getClientFileName(), upload.getContentType(), false, NotificationService.NOTI_NONE );
								int status = contentService.storeAndValidate( resourceId, false, serverConfigurationService.getString( "scorm.zip.encoding", "UTF-8" ) );

								if( status == ScormConstants.VALIDATION_SUCCESS )
								{
									setResponsePage( PackageListPage.class );
								}
								else
								{
									PageParameters params = new PageParameters();
									params.add( "resourceId", resourceId );
									params.add( "status", status );
									setResponsePage( ConfirmPage.class, params );
								}
							}
							catch( IOException | ResourceStorageException e )
							{
								UploadPage.this.warn( getLocalizer().getString( "upload.failed", UploadPage.this, new Model( e ) ) );
								log.error( "Failed to upload file {}", e );
							}
						}
					}
				}
			};

			add( btnCancel );
			add( btnSubmit );
			add(feedbackPanel);
		}
	}
}
