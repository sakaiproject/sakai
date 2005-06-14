package com.corejsf;

import javax.faces.component.UIComponent;
import javax.faces.webapp.UIComponentTag;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class UploadTag extends UIComponentTag {
  private String value;
  private String target;
  private String valueChangeListener;

  public UploadTag() {
  }

  public void setValue(String newValue){ value = newValue;}
  public void setTarget(String newValue){ target = newValue;}
  public void setValueChangeListener(String newValue){ valueChangeListener = newValue;}

  public void setProperties(UIComponent component){
    super.setProperties(component);
    com.corejsf.util.Tags.setString(component, "target", target);
    com.corejsf.util.Tags.setString(component, "value", value);
    com.corejsf.util.Tags.setValueChangeListener(component, valueChangeListener);
  }

  public void release(){
    super.release();
    value = null;
    target = null;
    valueChangeListener = null;
  }

  public String getRendererType(){ return "com.corejsf.Upload";}
  public String getComponentType(){ return "com.corejsf.Upload";}
}
