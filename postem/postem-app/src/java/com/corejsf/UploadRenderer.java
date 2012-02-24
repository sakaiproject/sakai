/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package com.corejsf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.faces.FacesException;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.faces.render.Renderer;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import javax.faces.application.FacesMessage;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.tool.postem.PostemTool;

public class UploadRenderer extends Renderer {
    
    /**
     * Property set via sakai.properties to limit the file size allowed for
     * uploads. This property is in MB and defaults to {@value #FILE_SIZE_MAX_DEFAULT}.
     */
    public static final String PROPERTY_FILE_SIZE_MAX = "postem.upload.max";

    /**
     * The default max file size, in MB, for an upload.
     */
    public static final int FILE_SIZE_MAX_DEFAULT = 1;
    
	public void encodeBegin(FacesContext context, UIComponent component)
			throws IOException {
		if (!component.isRendered())
			return;
		ResponseWriter writer = context.getResponseWriter();

		String clientId = component.getClientId(context);

		writer.startElement("input", component);
		writer.writeAttribute("type", "file", "type");
		writer.writeAttribute("name", clientId, "clientId");
		writer.endElement("input");
		writer.flush();
	}

	public void decode(FacesContext context, UIComponent component) {
		ExternalContext external = context.getExternalContext();
		HttpServletRequest request = (HttpServletRequest) external.getRequest();
		String clientId = component.getClientId(context);
		FileItem item = (FileItem) request.getAttribute(clientId);
		
		int maxFileSizeInMB;
        try {
            maxFileSizeInMB = ServerConfigurationService.getInt(PROPERTY_FILE_SIZE_MAX, FILE_SIZE_MAX_DEFAULT);
        } catch (NumberFormatException nfe) {
           maxFileSizeInMB = FILE_SIZE_MAX_DEFAULT;
        }
        long maxFileSizeInBytes = 1024L * 1024L * maxFileSizeInMB;

		Object newValue;
		ValueBinding binding = component.getValueBinding("value");
		if (binding != null) {
			// sometimes item is null, due to being hidden
			if (item.getSize() == 0 && item.getName().equals("")){
				//no file uploaded at all
				PostemTool.populateMessage(FacesMessage.SEVERITY_ERROR,
						"missing_csv", new Object[] {});
				newValue = "";
			}
			else if (item != null && item.getSize() > maxFileSizeInBytes) {
			    PostemTool.populateMessage(FacesMessage.SEVERITY_ERROR,
                        "upload_file_size_error", new Object[] {maxFileSizeInMB});
                newValue = "";
			}
			else if (item != null && item.getName().endsWith(".csv")){
				if (binding.getType(context) == byte[].class) {
					newValue = item.get();
				}
				if (binding.getType(context) == InputStream.class) {
					try {
						newValue = item.getInputStream();
					} catch (IOException ex) {
						throw new FacesException(ex);
					}
				} else {
					String encoding = request.getCharacterEncoding();
					if (encoding != null)
						try {
							newValue = item.getString(encoding);
						} catch (UnsupportedEncodingException ex) {
							newValue = item.getString();
						}
					else
						newValue = item.getString();
				}
				//check to make sure file was not empty
				if (newValue.equals("")){
					PostemTool.populateMessage(FacesMessage.SEVERITY_ERROR,
							"missing_csv", new Object[] {});
					newValue = "";
				}
			} 
			else if (item != null && !item.getName().endsWith(".csv")){
				//item does not have correct extension
				PostemTool.populateMessage(FacesMessage.SEVERITY_ERROR,
						"invalid_ext", new Object[] {item.getName()});
				newValue = "";
			}
			else
				newValue = "";
			
			((EditableValueHolder) component).setSubmittedValue(newValue);
		}

		Object target;
		binding = component.getValueBinding("target");
		if (binding != null)
			target = binding.getValue(context);
		else
			target = component.getAttributes().get("target");

		if (target != null) {
			File file;
			if (target instanceof File)
				file = (File) target;
			else {
				ServletContext servletContext = (ServletContext) external.getContext();
				file = new File(servletContext.getRealPath("/"), target.toString());
			}

			try { // ugh--write is declared with "throws Exception"
				item.write(file);
			} catch (Exception ex) {
				throw new FacesException(ex);
			}
		}
	}
}
