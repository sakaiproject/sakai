package org.sakaiproject.scorm.ui.upload.pages;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.AjaxPostprocessingCallDecorator;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.lang.Bytes;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.scorm.api.ScormConstants;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.scorm.service.api.ScormResourceService;
import org.sakaiproject.scorm.ui.console.pages.ConsoleBasePage;
import org.sakaiproject.scorm.ui.console.pages.PackageListPage;
import org.sakaiproject.wicket.markup.html.form.CancelButton;

public class UploadPage extends ConsoleBasePage implements ScormConstants {

	private static final long serialVersionUID = 1L;
	
	private static ResourceReference PAGE_ICON = new ResourceReference(ConsoleBasePage.class, "res/table_add.png");
	
	private static Log log = LogFactory.getLog(FileUploadForm.class);
	
	// bjones86 - SCO-97 sakai.property to enable/disable (show/hide) email sending (drop down)
	private static final String SAK_PROP_SCORM_ENABLE_EMAIL = "scorm.enable.email";
	@SpringBean( name = "org.sakaiproject.component.api.ServerConfigurationService" )
	ServerConfigurationService serverConfigurationService;
	
	@SpringBean(name="org.sakaiproject.scorm.service.api.ScormContentService")
	ScormContentService contentService;
	@SpringBean(name="org.sakaiproject.scorm.service.api.ScormResourceService")
	ScormResourceService resourceService;
	
	public UploadPage(PageParameters params) {
		add(new FileUploadForm("uploadForm"));
	}
	
	@Override
	protected ResourceReference getPageIconReference() {
		return PAGE_ICON;
	}
	
	public class FileUploadForm extends Form {
		
		private static final long serialVersionUID = 1L;
		
		private FileUploadField fileUploadField;
		private boolean fileHidden = false;
		private int priority = NotificationService.NOTI_NONE;
		private boolean fileValidated = false;
		private FileUpload fileInput;
		
		public FileUpload getFileInput() {
        	return fileInput;
        }

		public void setFileInput(FileUpload fileUpload) {
        	this.fileInput = fileUpload;
        }

		public FileUploadForm(String id) {
			super(id);
			
			IModel model = new CompoundPropertyModel(this);
			this.setModel(model);
			
			// We need to establish the largest file allowed to be uploaded
			setMaxSize(Bytes.megabytes(resourceService.getMaximumUploadFileSize()));
			
			setMultiPart(true);
			
			add(fileUploadField = new FileUploadField("fileInput"));
			add(new CheckBox("fileValidated"));
			
			// bjones86 - SCO-97 sakai.property to enable/disable (show/hide) email sending (drop down)
			@SuppressWarnings( { "unchecked", "rawtypes" } )
			DropDownChoice emailNotificationDropDown = new DropDownChoice( "priority", 
					Arrays.asList( new Integer[] { NotificationService.NOTI_NONE, NotificationService.NOTI_OPTIONAL, 
							NotificationService.NOTI_REQUIRED } ), 
					new IChoiceRenderer()
					{
						private static final long serialVersionUID = 1L;
		
						public Object getDisplayValue( Object object )
						{
			                switch( ((Integer) object) )
			                {
			                    case NotificationService.NOTI_NONE:
				                    return getLocalizer().getString( "NotificationService.NOTI_NONE", UploadPage.this );
			                    case NotificationService.NOTI_OPTIONAL:
				                    return getLocalizer().getString( "NotificationService.NOTI_OPTIONAL", UploadPage.this );
			                    case NotificationService.NOTI_REQUIRED:
				                    return getLocalizer().getString( "NotificationService.NOTI_REQUIRED", UploadPage.this );
		                    }
			                
			                return "";
		                }
		
						public String getIdValue( Object object, int index )
						{
							if( object == null )
								return "";
							return object.toString();
		                }
					} );
			
			// bjones86 - SCO-97 sakai.property to enable/disable (show/hide) email sending (drop down)
			boolean enableEmail = serverConfigurationService.getBoolean( SAK_PROP_SCORM_ENABLE_EMAIL, true );
			Label priorityLabel = new Label( "lblPriority", new ResourceModel( "upload.priority.label" ) );
			if( !enableEmail )
			{
				emailNotificationDropDown.setEnabled( false );
				emailNotificationDropDown.setVisibilityAllowed( false );
				priorityLabel.setEnabled( false );
				priorityLabel.setVisibilityAllowed( false );
			}
			add( priorityLabel );
			add( emailNotificationDropDown );
			
			
			// bjones86 - SCO-98 - disable buttons on submit, add spinner
			final CancelButton btnCancel = new CancelButton( "btnCancel", PackageListPage.class );
			IndicatingAjaxButton btnSubmit = new IndicatingAjaxButton( "btnSubmit", this )
			{
				private static final long serialVersionUID = 1L;
				
				@Override
				protected IAjaxCallDecorator getAjaxCallDecorator()
				{
					return new AjaxPostprocessingCallDecorator( super.getAjaxCallDecorator() )
					{
						private static final long serialVersionUID = 1L;
						
						@Override
						public CharSequence postDecorateScript( CharSequence script )
						{
							// Disable the submit and cancel buttons on click
							return script + "this.disabled = true; document.getElementsByName( \"btnCancel\" )[0].disabled = true;";
						}
					};
				}
				@Override
				protected void onSubmit( AjaxRequestTarget target, Form<?> form )
				{					
					if( fileUploadField != null )
					{
						final FileUpload upload = fileUploadField.getFileUpload();
				        if( upload != null )
				        {
				            try
				            {
				            	String resourceId = resourceService.putArchive( upload.getInputStream(), upload.getClientFileName(),
				            			upload.getContentType(), isFileHidden(), getPriority() );
				            	int status = contentService.storeAndValidate( resourceId, isFileValidated(), 
				            			serverConfigurationService.getString( "scorm.zip.encoding", "UTF-8" ) );
				            	
				            	if( status == VALIDATION_SUCCESS )
				            		setResponsePage( PackageListPage.class );
				            	else
				            	{
					            	PageParameters params = new PageParameters();
					            	params.add( "resourceId", resourceId );
					            	params.put( "status", status );
					            	setResponsePage( ConfirmPage.class, params );
				            	}
				            }
				            catch( Exception e )
				            {
				            	UploadPage.this.warn( getLocalizer().getString( "upload.failed", UploadPage.this, new Model( e ) ) );
				                log.error( "Failed to upload file", e );
				            }
				        }
					}
				}
			};
			add( btnCancel );
			add( btnSubmit );			
		}


		public boolean isFileHidden() {
			return fileHidden;
		}

		public void setFileHidden(boolean fileHidden) {
			this.fileHidden = fileHidden;
		}

		public boolean isFileValidated() {
			return fileValidated;
		}

		public void setFileValidated(boolean fileValidated) {
			this.fileValidated = fileValidated;
		}

		public int getPriority() {
        	return priority;
        }

		public void setPriority(int priority) {
        	this.priority = priority;
        }
	}
	
	
	
}
