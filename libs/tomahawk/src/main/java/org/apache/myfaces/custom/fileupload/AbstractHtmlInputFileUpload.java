/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.custom.fileupload;

import jakarta.el.ValueExpression;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.html.HtmlInputText;
import jakarta.faces.context.FacesContext;

import org.apache.myfaces.component.AlignProperty;
import org.apache.myfaces.component.UserRoleAware;
import org.apache.myfaces.component.UserRoleUtils;
import org.apache.myfaces.shared_tomahawk.util.MessageUtils;
import org.apache.myfaces.tomahawk.util.Constants;

/**
 * Creates a file-selection widget in the rendered page which allows a user to select
 * a file for uploading to the server.
 * <p>
 * When the page is selected (using a command component such as commandButton), the
 * currently selected file contents are included in the data posted to the server.
 * The contents are cached somewhere, and an object of type UploadedFile will then
 * be assigned to the property pointed to by the "value" expression of this component.
 * </p>
 * <p>
 * You must enable the Tomahawk ExtensionsFilter to make this component work (see web.xml).
 * </p> 
 * <p>
 * Also, don't forget to set the form's attribute "enctype" to "multipart/form-data". 
 * See "examples/web/fileupload.jsp" for an example!
 * </p> 
 * <p>
 * Unless otherwise specified, all attributes accept static values or EL expressions.
 * </p>
 * 
 * @JSFComponent
 *   name = "t:inputFileUpload"
 *   class = "org.apache.myfaces.custom.fileupload.HtmlInputFileUpload"
 *   tagClass = "org.apache.myfaces.custom.fileupload.HtmlInputFileUploadTag"
 * @since 1.1.7
 * @author Manfred Geiler (latest modification by $Author: lu4242 $)
 * @version $Revision: 1146523 $ $Date: 2011-07-13 19:20:18 -0500 (Wed, 13 Jul 2011) $
 */
public abstract class AbstractHtmlInputFileUpload
        extends HtmlInputText
        implements UserRoleAware, AlignProperty
{
    private static final String SIZE_LIMIT_EXCEEDED = "sizeLimitExceeded";
    private static final String FILE_SIZE_LIMIT_EXCEEDED = "fileSizeLimitExceeded";
    private static final String FILEUPLOAD_MAX_SIZE = "org.apache.myfaces.custom.fileupload.maxSize";
    private static final String FILEUPLOAD_EXCEPTION = "org.apache.myfaces.custom.fileupload.exception";
    public static final String COMPONENT_TYPE = "org.apache.myfaces.HtmlInputFileUpload";
    public static final String DEFAULT_RENDERER_TYPE = "org.apache.myfaces.FileUpload";
    public static final String SIZE_LIMIT_MESSAGE_ID = "org.apache.myfaces.FileUpload.SIZE_LIMIT";

    public AbstractHtmlInputFileUpload()
    {
        setRendererType(DEFAULT_RENDERER_TYPE);
    }

    public void setUploadedFile(UploadedFile upFile)
    {
        setValue(upFile);
    }

    public UploadedFile getUploadedFile()
    {
        return (UploadedFile)getValue();
    }
    
    /**
     * This setting was intended to allow control over how the contents of the
     * file get temporarily stored during processing.
     * <p> It allows three options</p>
     * <ul>
     * <li>"default": The file is handled on memory while the file size is below 
     * uploadThresholdSize value, otherwise is handled on disk or file storage when
     * decode occur (set submitted value)</li>
     * <li>"memory": The file is loaded to memory when decode occur 
     * (set submitted value). In other words, before set the uploaded file as 
     * submitted value it is loaded to memory. Use with caution, because it
     * could cause OutOfMemory exceptions when the uploaded files are too big. </li>
     * <li>"file": The file is handled on disk or file storage.</li>
     * </ul>
     * 
     * @JSFProperty
     */
    public abstract String getStorage();

    /**
     * This attribute specifies a comma-separated list of content types that 
     * a server processing this form will handle correctly. User agents may 
     * use this information to filter out non-conforming files when prompting 
     * a user to select files to be sent to the server 
     * (cf. the INPUT element when type="file")."
     * 
     * @JSFProperty
     */
    public abstract String getAccept();

    /**
     * An EL expression to which an UploadedFile object will be assigned on postback
     * if the user specified a file to upload to the server.
     * 
     * @JSFProperty
     */
    public Object getValue()
    {
        return super.getValue();
    }

    public boolean isRendered()
    {
        if (!UserRoleUtils.isVisibleOnUserRole(this)) return false;
        return super.isRendered();
    }
    
    protected void validateValue(FacesContext context, Object convertedValue)
    {
        super.validateValue(context, convertedValue);
        
        if (isValid())
        {
              String exception =
                (String) context.getExternalContext().getRequestMap().get(FILEUPLOAD_EXCEPTION);
              
              if(exception != null )
              {
                if(exception.equals(SIZE_LIMIT_EXCEEDED))
                {
                  Integer maxSize =
                    (Integer) context.getExternalContext().getRequestMap().get(FILEUPLOAD_MAX_SIZE);
                  MessageUtils.addMessage(Constants.TOMAHAWK_DEFAULT_BUNDLE, FacesMessage.SEVERITY_ERROR,
                              SIZE_LIMIT_MESSAGE_ID, new Object[] { MessageUtils.getLabel(context, this),
                                      maxSize},
                              getClientId(context), context);
                  setValid(false);
                }
                else if (FILE_SIZE_LIMIT_EXCEEDED.equals(exception))
                {
                    Integer maxSize =
                        (Integer) context.getExternalContext().getRequestMap().get(FILEUPLOAD_MAX_SIZE);
                    if (maxSize != null)
                    {
                        MessageUtils.addMessage(Constants.TOMAHAWK_DEFAULT_BUNDLE, FacesMessage.SEVERITY_ERROR,
                                SIZE_LIMIT_MESSAGE_ID, new Object[] { MessageUtils.getLabel(context, this),
                                        maxSize},
                                getClientId(context), context);
                    }
                    else
                    {
                        maxSize = (Integer) context.getExternalContext().getRequestMap().get(
                                "org.apache.myfaces.custom.fileupload."+this.getClientId(context)+".maxSize");
                        if (maxSize != null)
                        {
                            MessageUtils.addMessage(Constants.TOMAHAWK_DEFAULT_BUNDLE, FacesMessage.SEVERITY_ERROR,
                                    SIZE_LIMIT_MESSAGE_ID, new Object[] { MessageUtils.getLabel(context, this),
                                            maxSize},
                                    getClientId(context), context);
                        }
                        //else
                        //{
                            //Ignore because this exception belongs to other component
                        //}
                    }
                    setValid(false);
                }
                else 
                {
                  throw new IllegalStateException("other exceptions not handled yet, exception : "+exception);
                }
             }
         }
     }
}
