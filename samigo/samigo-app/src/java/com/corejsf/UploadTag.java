/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
* Copyright (c) 2004 Sun Microsystems from the Java Series, Core Java ServerFaces
* source freely distributable.
* see http://www.sun.com/books/java_series.html
 ***********************************************************************************
* Modifications Copyright (c) 2005 Sakai Foundation
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

import javax.faces.component.UIComponent;
import javax.faces.webapp.UIComponentTag;

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
