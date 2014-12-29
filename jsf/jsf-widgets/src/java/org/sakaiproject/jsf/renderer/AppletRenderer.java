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


package org.sakaiproject.jsf.renderer;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import org.sakaiproject.jsf.util.RendererUtil;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * <p>Description: </p>
 * <p>Render an applet in a browser-indepemdent way
 *
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class AppletRenderer
  extends Renderer
{
  private static final String IE_CODEBASE =
    "http://java.sun.com/products/plugin/1.4/jinstall-14-win32.cab#Version=1,4,0,mn";
  private static final String NS_PLUGINSPAGE =
    "http://java.sun.com/products/plugin/1.4/plugin-install.html";
  private static final String VERSION = "1.4";
  private static final String DEFAULT_WIDTH = "200";
  private static final String DEFAULT_HEIGHT = "200";
  private static final String DEFAULT_VSPACE = "0";
  private static final String DEFAULT_HSPACE = "0";

  public boolean supportsComponentType(UIComponent component)
  {
    return (component instanceof UIOutput);
  }


  /**
   * @todo apply attributes
   * <p>Faces render output method .</p>
   * <p>Method Generator: org.sakaiproject.jsf.devtool.RenderMaker</p>
   *
   *  @param context   <code>FacesContext</code> for the current request
   *  @param component <code>UIComponent</code> being rendered
   *
   * @throws IOException if an input/output error occurs
   */
  public void encodeBegin(FacesContext context, UIComponent component)
    throws IOException
  {
    if (!component.isRendered())
    {
      return;
    }

    String width = (String) RendererUtil.getDefaultedAttribute(context, component, "width", DEFAULT_WIDTH);
    String height = (String) RendererUtil.getDefaultedAttribute(context, component, "height", DEFAULT_HEIGHT);
    String vspace = (String) RendererUtil.getDefaultedAttribute(context, component, "vspace", DEFAULT_VSPACE);
    String hspace = (String) RendererUtil.getDefaultedAttribute(context, component, "hspace", DEFAULT_HSPACE);

    String javaClass = (String) RendererUtil.getAttribute(context, component, "javaClass");
    String javaArchive = (String) RendererUtil.getAttribute(context, component, "javaArchive");
    String codebase = (String) RendererUtil.getAttribute(context, component, "codebase");
    String paramList = (String) RendererUtil.getAttribute(context, component, "paramList");

    String name = (String) RendererUtil.getAttribute(context, component, "name");
    Map appParamMap = makeAppletParameters(paramList);
    
    if(null!=codebase) appParamMap.put("codebase", codebase);
    if(null!=javaArchive) appParamMap.put("archive", javaArchive);
    //if(null!=name) appParamMap.put("id", name);
    appParamMap.put("code", javaClass);

    ResponseWriter writer = context.getResponseWriter();
    renderApplet(width, height, vspace, hspace, codebase, javaClass,
      javaArchive, 
      name, 
      appParamMap, writer);
  }

    /**
    * Accept a comma separated list of name value pairs.
    * @todo quotations to escape out "," and "=".
    * Usage: <sakai:applet ... paramList="param1=value1,param2=value2,..."...
    * @param paramList
    * @return
    */
  private Map makeAppletParameters(String paramList)
  {
    Map appParamMap = new HashMap();
    if (paramList != null){
      StringTokenizer paramsTokenizer = new StringTokenizer(paramList, ",", false);
      while (paramsTokenizer.hasMoreTokens())
      {
        StringTokenizer nameValueTokenizer =
          new StringTokenizer(paramsTokenizer.nextToken(), "=", false);
        if (nameValueTokenizer.countTokens()<2) continue;
        String name = nameValueTokenizer.nextToken();
        String value = nameValueTokenizer.nextToken();
        appParamMap.put(name, value);
      }
    }
    return appParamMap;
  }

  /**
   * Render applet in browser independent way.
    * @param width value of the corresponding tag attribute
    * @param height  value of the corresponding tag attribute
    * @param vspace  value of the corresponding tag attribute
    * @param hspace  value of the corresponding tag attribute
    * @param codebase  value of the corresponding tag attribute
    * @param javaClass  value of the corresponding tag attribute
    * @param javaArchive  value of the corresponding tag attribute
    * @param version  value of the corresponding tag attribute
    * @param appParamMap comma separated list of name value pairs from paramList
    * tag attribute
    *
    * @param writer ResponseWriter
    * @throws IOException
    */
//   <OBJECT classid="clsid:8AD9C840-044E-11D1-B3E9-00805F499D93"
//    width="200" height="200" align="baseline"
//    codebase="http://java.sun.com/products/plugin/1.4/jinstall-14-win32.cab#Version=1,4,0,mn">
//    <PARAM NAME="code" VALUE="XYZApp.class">
//    <PARAM NAME="codebase" VALUE="html/">
//    <PARAM NAME="type" VALUE="application/x-java-applet;jpi-version=1.4">
//    <PARAM NAME="model" VALUE="models/HyaluronicAcid.xyz">
//    <PARAM NAME="scriptable" VALUE="true">
//    <COMMENT>
//        <EMBED type="application/x-java-applet;jpi-version=1.4" width="200"
//           height="200" align="baseline" code="XYZApp.class"
//           codebase="html/" model="models/HyaluronicAcid.xyz"
//           pluginspage="http://java.sun.com/j2se/1.4/download.html">
//            <NOEMBED>
//                No Java 2 SDK, Standard Edition v 1.4 support for APPLET!!
//            </NOEMBED>
//        </EMBED>
//    </COMMENT>
//</OBJECT>

  private void renderApplet(String width, String height, String vspace,
    String hspace, String codebase, String javaClass, String javaArchive,
    String name,
    Map appParamMap, ResponseWriter writer)
    throws IOException
  {
    writer.write("");
    writer.write("<OBJECT ");
    writer.write("    classid=\"clsid:8AD9C840-044E-11D1-B3E9-00805F499D93\" ");
    writer.write("    width=\"" + width + "\" ");
    writer.write("    height=\"" + height + "\" ");
    writer.write("    hspace=\"" + hspace + "\" ");
    writer.write("    vspace=\"" + vspace + "\" ");
    writer.write("    codebase=\"" + IE_CODEBASE + "\"");
    writer.write("    archive=\"" + javaArchive + "\"");
    writer.write("    id=\"" + name + "\"");
    writer.write(">");
//    writer.write("    <PARAM name=\"java_code\" value=\"" + javaClass + "\">");
//    writer.write("    <PARAM name=\"java_archive\" value=\"" + javaArchive +
//      ">");
    writer.write(
      "    <PARAM name=\"type\" value=\"application/x-java-applet;version=" +
      VERSION + "\">");
    renderObjectParams(writer, appParamMap);
    writer.write("<COMMENT>");
    writer.write("<EMBED ");
    writer.write("    type=\"application/x-java-applet;version=" + VERSION +
      "\" ");
    writer.write("    width=\"" + width + "\" ");
    writer.write("    height=\"" + height + "\" ");
    writer.write("    hspace=\"" + hspace + "\" ");
    writer.write("    vspace=\"" + vspace + "\" ");
    writer.write("    pluginspage=\"" + NS_PLUGINSPAGE + "\" ");
//    writer.write("    java_code=\"" + javaClass + "\"");
//    writer.write("    java_archive=\"" + javaArchive + "");
    // if the name is not null duplicate it to the id param
    if(null!=name) {
    	appParamMap.put("id", name);
    	appParamMap.put("name", name);
    }
    renderEmbedParams(writer, appParamMap);
    writer.write("/>");
    writer.write("");
    writer.write("<NOEMBED>");
    writer.write("    <font color=\"red\">");
    writer.write("    Note: Requires Java Plug-in " + VERSION + ".");
    writer.write("    </b></font><br/>");
    writer.write("</NOEMBED>");
    writer.write("</COMMENT>");
    writer.write("</OBJECT>");
  }

    /**
     * Render Map of Embed tag params,
     * @param writer
     * @param appParamMap
     * @throws IOException
     */
  private void renderEmbedParams(ResponseWriter writer, Map appParamMap)
    throws IOException
  {
	  Iterator paramIter = appParamMap.entrySet().iterator();
	  while (paramIter.hasNext())
	  {
		  Entry entrySet = (Entry) paramIter.next();
		  String name = (String) entrySet.getKey();
		  String value = (String) entrySet.getValue();
		  writer.write("    " + name + "=\"" + value + "\" ");
	  }
  }

  /**
   * Render Map of Object PARAMs.
    * @param writer
    * @param appParamMap
    * @throws IOException
    */
  private void renderObjectParams(ResponseWriter writer, Map appParamMap)
    throws IOException
  {
    Iterator iter = appParamMap.entrySet().iterator();
    while (iter.hasNext())
    {
      Entry entrySet = (Entry) iter.next();
      String name = (String) entrySet.getKey();
      String value = (String) entrySet.getValue();
      writer.write("    <PARAM name=\"" +
        name +
        "\" value=\"" +
        value + "\">");
    }
  }
}
