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
import javax.servlet.http.Part;

import org.apache.commons.fileupload.FileItem;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.api.ServerConfigurationService;

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
    log.debug("** encodeBegin, clientId = {}", clientId);
    encodeUploadField(writer, clientId, component);
  }

  public void encodeUploadField(ResponseWriter writer, String clientId,
                                UIComponent component) throws IOException {
    // write <input type=file> for browsing and upload
    writer.startElement("input", component);
    writer.writeAttribute("type","file","type");
    writer.writeAttribute("name",clientId + UPLOAD,"clientId");
    writer.writeAttribute("id",clientId + UPLOAD,"clientId");
    writer.writeAttribute("size", "50", null);
    writer.endElement("input");
    writer.flush();
  }

  public void decode(FacesContext context, UIComponent component){
    log.debug("**** decode =");
    ExternalContext external = context.getExternalContext();
    HttpServletRequest request = (HttpServletRequest) external.getRequest();
    String clientId = component.getClientId(context);
    WrappedUpload item = wrapUpload(request, clientId + UPLOAD);

    log.debug("clientId = {}", clientId);
    log.debug("wrappedUpload = {}", item);

    ServerConfigurationService serverConfigurationService = ComponentManager.get(ServerConfigurationService.class);
    Long maxSize = Long.valueOf(serverConfigurationService.getString("samigo.sizeMax", "20480"));

    // Check if file > maxSize allowed
    if (item != null && item.getSize()/1000 > maxSize.intValue()){
      ((ServletContext)external.getContext()).setAttribute("TEMP_FILEUPLOAD_SIZE", Long.valueOf(item.getSize()/1000));
      ((EditableValueHolder) component).setSubmittedValue("SizeTooBig:" + item.getName());
      return;
    }

    Object target;
    ValueBinding binding = component.getValueBinding("target");
    if (binding != null) target = binding.getValue(context);
    else target = component.getAttributes().get("target");

    String repositoryPath = serverConfigurationService.getString("samigo.answerUploadRepositoryPath", "${sakai.home}/samigo/answerUploadRepositoryPath/");
    log.debug("****{}", repositoryPath);
    if (target != null){
      File dir = new File(repositoryPath+target.toString()); //directory where file would be saved
      if (!dir.exists())
        dir.mkdirs();
      if (item != null && !("").equals(item.getName())){
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
        String filePath = dir.getPath()+"/"+filename;
        log.debug("**1. filename= {}", filePath);
        try {
          //if (mediaIsValid) item.write(file);
          item.write(filePath);
          // change value so we can evoke the listener
          ((EditableValueHolder) component).setSubmittedValue(filePath);
        }
        catch (Exception ex){
          throw new FacesException(ex);
        }
      }
    }
  }

    /**
     * Unify FileItem and Part instances behind one interface
     */
    private WrappedUpload wrapUpload(HttpServletRequest request, String name) {

        FileItem item = (FileItem) request.getAttribute(name);
        if (item != null) {
            return new WrappedUpload(item);
        }

        try {
            Part part = request.getPart(name);
            if (part != null) return new WrappedUpload(part);
        } catch (Exception e) {
            log.warn("Failed to get upload part from request, NULL will be returned, {}", e.toString());
        }
        return null;
    }

    /**
     * Unify file items and parts in a single interface. The faces servlet digests multipart uploads
     * into the new Servlet 3 Part interface, whereas the RequestFilter digests as FileItem instances.
     */
    private class WrappedUpload {

        private Part part;
        private FileItem fileItem;

        private WrappedUpload(Object upload) {

            if (upload instanceof Part) this.part = (Part) upload;
            if (upload instanceof FileItem) this.fileItem = (FileItem) upload;
        }

        public long getSize() {

            if (part != null) return part.getSize();
            if (fileItem != null) return fileItem.getSize();
            return 0L;
        }

        public String getName() {

            if (part != null) return part.getSubmittedFileName();
            if (fileItem != null) return fileItem.getName();
            return "";
        }

        public void write(String filePath) throws Exception {

            if (part != null) part.write(filePath);
            if (fileItem != null) {
                fileItem.write(new File(filePath));
            }
        }
    }
}
