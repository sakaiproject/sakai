/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
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


package org.sakaiproject.jsf.tag;

import javax.faces.component.UIComponent;
import javax.faces.webapp.UIComponentTag;
import org.sakaiproject.jsf.util.TagUtil;

/**
 * <p>Description:<br />
 * This class is the tag handler that evaluates the <code>applet</code>
 * custom tag.</p>
 * <p>Display a java applet.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class AppletTag extends UIComponentTag
{
  private String name = null;
  private String version;
  private String codebase;
  private String width;
  private String height;
  private String vspace;
  private String hspace;
  private String javaClass;
  private String javaArchive;
  private String paramList;

  public void setName(String name)
  {
    this.name = name;
  }


  public String getComponentType()
  {
    return ("javax.faces.Output");
  }

  public String getRendererType()
  {
    return "org.sakaiproject.Applet";
  }

  protected void setProperties(UIComponent component)
  {

    super.setProperties(component);
    TagUtil.setString(component, "name", name);
    TagUtil.setString(component, "version", version);
    TagUtil.setString(component, "codebase", codebase);
    TagUtil.setString(component, "width", width);
    TagUtil.setString(component, "height", height);
    TagUtil.setString(component, "vspace", vspace);
    TagUtil.setString(component, "hspace", hspace);
    TagUtil.setString(component, "javaClass", javaClass);
    TagUtil.setString(component, "javaArchive", javaArchive);
    TagUtil.setString(component, "paramList", paramList);
  }
  public void setVersion(String version)
  {
    this.version = version;
  }
  public void setCodebase(String codebase)
  {
    this.codebase = codebase;
  }
  public void setWidth(String width)
  {
    this.width = width;
  }
  public void setHeight(String height)
  {
    this.height = height;
  }
  public void setVspace(String vspace)
  {
    this.vspace = vspace;
  }
  public void setHspace(String hspace)
  {
    this.hspace = hspace;
  }
  public void setJavaClass(String javaClass)
  {
    this.javaClass = javaClass;
  }
  public void setJavaArchive(String javaArchive)
  {
    this.javaArchive = javaArchive;
  }
  public String getParamList()
  {
    return paramList;
  }
  public void setParamList(String paramList)
  {
    this.paramList = paramList;
  }

}
