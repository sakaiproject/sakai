package com.corejsf;

import java.io.File;
import java.io.IOException;

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

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

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
    log.debug("** decode =");

    ExternalContext external = context.getExternalContext();
    HttpServletRequest request = (HttpServletRequest) external.getRequest();
    String clientId = component.getClientId(context);
    FileItem item = (FileItem) request.getAttribute(clientId+UPLOAD);

    log.debug("clientId ="+ clientId);
    log.debug("fileItem ="+ item);

    Object target;
    ValueBinding binding = component.getValueBinding("target");
    if (binding != null) target = binding.getValue(context);
    else target = component.getAttributes().get("target");

    if (target != null){
      File dir = new File("/tmp/" + target.toString()); //directory where file would be saved
      if (!dir.exists())
        dir.mkdirs();
      if (item!= null && !("").equals(item.getName())){
        String filename = item.getName();
        filename = filename.replace('\\','/'); // replace c:\filename to c:/filename
        filename = filename.substring(filename.lastIndexOf("/")+1);
        File file = new File(dir.getPath()+"/"+filename);
        log.debug("**1. filename="+file.getPath());
        try {
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
