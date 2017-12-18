/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
* Copyright (c) 2004 Sun Microsystems from the Java Series, Core Java ServerFaces
* source freely distributable.
* see http://www.sun.com/books/java_series.html
***********************************************************************************
* Modifications Copyright (c) 2005, 2006, 2007 Sakai Foundation
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
import java.util.Date;

import javax.servlet.ServletContext;
import javax.faces.FacesException;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.faces.render.Renderer;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UploadRenderer extends Renderer {

  private static final String UPLOAD = ".upload";

  public UploadRenderer() {
  }

  public void encodeBegin(FacesContext context, UIComponent component)
    throws IOException {
    if (!component.isRendered()) return;
    ResponseWriter writer = context.getResponseWriter();
    ExternalContext external = context.getExternalContext();
    HttpServletRequest request = (HttpServletRequest) external.getRequest();

    String clientId = component.getClientId(context);
    log.debug("** encodeBegin, clientId ="+clientId);
    encodeUploadField(writer, clientId, component);
  }

  public void encodeUploadField(ResponseWriter writer, String clientId,
                                UIComponent component) throws IOException {
    // write <input type=file> for browsing and upload
    writer.startElement("input", component);
    writer.writeAttribute("type","file","type");
    writer.writeAttribute("name",clientId + UPLOAD,"clientId");
    writer.writeAttribute("size", "50", null);
    writer.endElement("input");
    writer.flush();
  }

  public void decode(FacesContext context, UIComponent component){
    log.debug("**** decode =");
    ExternalContext external = context.getExternalContext();
    HttpServletRequest request = (HttpServletRequest) external.getRequest();
    String clientId = component.getClientId(context);
    FileItem item = (FileItem) request.getAttribute(clientId+UPLOAD);
    // check if file > maxSize allowed
    log.debug("clientId ="+ clientId);
    log.debug("fileItem ="+ item);
    // if (item!=null) log.debug("***UploadRender: fileItem size ="+ item.getSize());
    Long maxSize = (Long)((ServletContext)external.getContext()).getAttribute("FILEUPLOAD_SIZE_MAX");
     // RU - typo. Stanford agrees, so this should be FINR
    if (item!=null && item.getSize()/1000 > maxSize.intValue()){
      ((ServletContext)external.getContext()).setAttribute("TEMP_FILEUPLOAD_SIZE", Long.valueOf(item.getSize()/1000));
      ((EditableValueHolder) component).setSubmittedValue("SizeTooBig:" + item.getName());
      return;
    }

    Object target;
    ValueBinding binding = component.getValueBinding("target");
    if (binding != null) target = binding.getValue(context);
    else target = component.getAttributes().get("target");

    String repositoryPath = (String)((ServletContext)external.getContext()).getAttribute("FILEUPLOAD_REPOSITORY_PATH");
    log.debug("****"+repositoryPath);
    if (target != null){
      File dir = new File(repositoryPath+target.toString()); //directory where file would be saved
      if (!dir.exists())
        dir.mkdirs();
      if (item!= null && !("").equals(item.getName())){
        String fullname = item.getName();
        fullname = fullname.replace('\\','/'); // replace c:\fullname to c:/fullname
        fullname = fullname.substring(fullname.lastIndexOf("/")+1);
	    int dot_index = fullname.lastIndexOf(".");
	    String filename = "";
	    if (dot_index < 0) {
	    	filename = fullname + "_" + (new Date()).getTime();
	    }
	    else {
	    	filename = fullname.substring(0, dot_index) + "_" + (new Date()).getTime() + fullname.substring(dot_index);
	    }
        File file = new File(dir.getPath()+"/"+filename);
        log.debug("**1. filename="+file.getPath());
        try {
          //if (mediaIsValid) item.write(file);
        	item.write(file);
          // change value so we can evoke the listener
          ((EditableValueHolder) component).setSubmittedValue(file.getPath());
        }
        catch (Exception ex){
          throw new FacesException(ex);
        }
      }
    }
  }
}
