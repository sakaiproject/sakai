/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
* Copyright (c) 2004 Sun Microsystems from the Java Series, Core Java ServerFaces
* source freely distributable.
* see http://www.sun.com/books/java_series.html
 ***********************************************************************************
* Modifications Copyright (c) 2005
* The Regents of the University of Michigan, Trustees of Indiana University,
* Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/
package com.corejsf;

import java.io.File;
import java.io.IOException;

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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UploadRenderer extends Renderer {

  private static Log log = LogFactory.getLog(UploadRenderer.class);
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
    boolean mediaIsValid = true;
    ExternalContext external = context.getExternalContext();
    HttpServletRequest request = (HttpServletRequest) external.getRequest();
    String clientId = component.getClientId(context);
    FileItem item = (FileItem) request.getAttribute(clientId+UPLOAD);
    // check if file > maxSize allowed
    log.debug("clientId ="+ clientId);
    log.debug("fileItem ="+ item);
    // if (item!=null) log.debug("***UploadRender: fileItem size ="+ item.getSize());
    Long maxSize = (Long)((ServletContext)external.getContext()).getAttribute("FILEUPLOAD_SIZE_MAX");
    if (item.getSize()/1000 > maxSize.intValue()){
      ((ServletContext)external.getContext()).setAttribute("TEMP_FILEUPLOAD_SIZE", new Long(item.getSize()/1000));
      mediaIsValid = false;
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
        String filename = item.getName();
        filename = filename.replace('\\','/'); // replace c:\filename to c:/filename
        filename = filename.substring(filename.lastIndexOf("/")+1);
        File file = new File(dir.getPath()+"/"+filename);
        log.debug("**1. filename="+file.getPath());
        try {
          if (mediaIsValid) item.write(file);
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
