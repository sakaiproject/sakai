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
package org.sakaiproject.scorm.ui.upload;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.lang.Bytes;

import static org.sakaiproject.scorm.api.ScormConstants.*;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdLengthException;
import org.sakaiproject.exception.IdUniquenessException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.scorm.service.api.ScormResourceService;
import org.sakaiproject.scorm.ui.console.pages.ConsoleBasePage;
import org.sakaiproject.scorm.ui.console.pages.PackageListPage;
import org.sakaiproject.wicket.ajax.markup.html.form.SakaiAjaxButton;
import org.sakaiproject.wicket.ajax.markup.html.form.SakaiAjaxCancelButton;

@Slf4j
public class UploadPage extends ConsoleBasePage 
{
    @SpringBean( name = "org.sakaiproject.component.api.ServerConfigurationService" )
    ServerConfigurationService serverConfigurationService;

    @SpringBean( name = "org.sakaiproject.scorm.service.api.ScormContentService" )
    ScormContentService contentService;

    @SpringBean( name = "org.sakaiproject.scorm.service.api.ScormResourceService" )
    ScormResourceService resourceService;

    @SpringBean( name="org.sakaiproject.content.api.ContentHostingService" )
    ContentHostingService contentHostingService;

    @Override
    protected void onInitialize()
    {
        super.onInitialize();
        uploadLink.disable();
    }

    public UploadPage( PageParameters params )
    {
        add( new FileUploadForm( "uploadForm" ) );
    }

    // SCO-124 - this is needed becasue we can't disable the button in Java and enable it in JavaScript, because Wicket will throw a runtime exception.
    // So we have to both enable and disable the button via JavaScript.
    @Override
    public void renderHead( IHeaderResponse response )
    {
        super.renderHead( response );
        String javascript = "document.getElementById( \"btnUpload\" ).disabled = true;";
        response.render( OnDomReadyHeaderItem.forScript( javascript ) );
    }

    public class FileUploadForm extends Form<Void>
    {
        private static final long serialVersionUID = 1L;
        private FileUploadField fileUploadField;

        public FileUploadForm( String id )
        {
            super( id );

            // We need to establish the largest file allowed to be uploaded
            // NOTE: As of Wicket 8.6.1, I can't get this to work. It's supposed to add a feedback message, which if I step through
            // the Wicket source code, it is doing. But for some reason the feedback panel never registers the message, and is
            // always empty when onError() is triggered. The result is that the upload is rejected, the buttons are disabled,
            // and there is no messaging to the user about what happened.
            // As a workaround, we'll do our own file size check in onSubmit and generate our own error(), since we know
            // this is already working for validation failures and exception handling.
            //setMaxSize( Bytes.megabytes( resourceService.getMaximumUploadFileSize() ) );
            setMultiPart( true );

            fileUploadField = new FileUploadField( "fileInput" );

            // Add JavaScript to enable the submit button only when there is a file selected (this cannot be done via Wicket/Java code)
            fileUploadField.add( new AttributeAppender( "onchange", new Model( "document.getElementById( \"btnUpload\" ).disabled = this.value === '';" ), ";" ) );
            add( fileUploadField );

            // SCO-98 - disable buttons on submit, add spinner
            final SakaiAjaxCancelButton btnCancel = new SakaiAjaxCancelButton( "btnCancel", PackageListPage.class );
            btnCancel.setElementsToDisableOnClick( Arrays.asList( new String[] {"btnUpload"} ) );
            btnCancel.setOutputMarkupId( true );
            SakaiAjaxButton btnUpload = new SakaiAjaxButton( "btnUpload", this )
            {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onError( AjaxRequestTarget target )
                {
                    target.add( feedback );
                    target.add( btnCancel );
                    target.add( this );
                    target.appendJavaScript( JS_SCROLL_TO_TOP );
                }

                @Override
                protected void onSubmit( AjaxRequestTarget target )
                {
                    target.appendJavaScript( JS_SCROLL_TO_TOP );

                    final List<FileUpload> uploads = fileUploadField.getFileUploads();
                    if( uploads != null )
                    {
                        for( FileUpload upload : uploads )
                        {
                            String resourceId = "";
                            try
                            {
                                // Check if file size exceeds allowed threshold - defined in sakai.properties as "content.upload.max", measured in megabytes
                                int maxFileSizeInMB = resourceService.getMaximumUploadFileSize();
                                if( Bytes.bytes( upload.getSize() ).megabytes() > maxFileSizeInMB )
                                {
                                    error( MessageFormat.format( getString( "upload.fileTooBig" ), maxFileSizeInMB ) );
                                    onError( target );
                                    return;
                                }

                                resourceId = resourceService.putArchive( upload.getInputStream(), upload.getClientFileName(), upload.getContentType(), false, NotificationService.NOTI_NONE );
                                int status = contentService.storeAndValidate( resourceId, false, serverConfigurationService.getString( "scorm.zip.encoding", "UTF-8" ) );

                                if( status == VALIDATION_SUCCESS )
                                {
                                    PageParameters params = new PageParameters();
                                    params.add( "uploadSuccess", MessageFormat.format( getString( "upload.success" ), upload.getClientFileName() ) );
                                    setResponsePage( PackageListPage.class, params );
                                }
                                else
                                {
                                    error( getNotification( status ) );
                                    onError( target );
                                }
                            }
                            catch( OverQuotaException e )
                            {
                                int quotaInMB = 20;
                                try
                                {
                                    ContentCollection collection = contentHostingService.getCollection( resourceId );
                                    long collectionQuota = contentHostingService.getQuota( collection ); // size in kb
                                    quotaInMB = (int) collectionQuota / 1024;
                                }
                                catch( Exception ex ) { /* ignore */ }

                                error( MessageFormat.format( getString( "upload.OverQuotaException" ), quotaInMB ) );
                                log.error( "File puts user over quota: {}", upload.getClientFileName() );
                            }
                            catch( Exception e )
                            {
                                handleException(e, upload.getClientFileName());
                                onError( target );
                            }
                        }
                    }
                }
            };
            btnUpload.setOutputMarkupId( true );
            btnUpload.setElementsToDisableOnClick( Arrays.asList( new String[] {"btnCancel"} ) );

            add( btnCancel );
            add( btnUpload );
        }
    }

    /**
     * Utility method to grab the appropriate error message from the message bundle based on the type of exception being processed.
     *
     * @param ex the exception to be processed
     * @param fileName the name of the file the client attempted to upload
     */
    private void handleException( Exception ex, String fileName )
    {
        // Default to general unknown error
        String errorKey = "upload.failed";

        // If specific exception type, get the specific error key
        if( ex instanceof IdUnusedException )
        {
            errorKey = "upload.IdUnusedException";
        }
        else if( ex instanceof IdUniquenessException)
        {
            errorKey = "upload.IdUniquenessException";
        }
        else if( ex instanceof IdLengthException )
        {
            errorKey = "upload.IdLengthException";
        }
        else if( ex instanceof IdInvalidException )
        {
            errorKey = "upload.IdInvalidException";
        }
        else if( ex instanceof PermissionException )
        {
            errorKey = "upload.PermissionException";
        }

        // Generate the user facing error message, and print info to logs
        error( getLocalizer().getString( errorKey, UploadPage.this ) );
        log.error( "Failed to upload file: {}", fileName );
        log.debug( "Exception occured while uploading module", ex );
    }

    private String getNotification( int status )
    {
        String resultKey = getKey( status );
        return getLocalizer().getString( resultKey, this );
    }

    private String getKey( int status )
    {
        switch( status )
        {
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
            default:
                return "upload.failed";
        }
    }
}
