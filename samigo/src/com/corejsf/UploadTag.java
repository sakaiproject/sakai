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
