package com.corejsf;

import javax.faces.component.UIComponent;
import javax.faces.webapp.UIComponentTag;


public class UploadTag extends UIComponentTag {
   private String value;
   private String target;

   public void setValue(String newValue) { value = newValue; }
   public void setTarget(String newValue) { target = newValue; }

   public void setProperties(UIComponent component) {
      super.setProperties(component);
      com.corejsf.util.Tags.setString(component, "target", target);
      com.corejsf.util.Tags.setString(component, "value", value);
   }

   public void release() {
      super.release();
      value = null;
      target = null;
   }

   public String getRendererType() { return "com.corejsf.Upload"; }
   public String getComponentType() { return "com.corejsf.Upload"; }
}
