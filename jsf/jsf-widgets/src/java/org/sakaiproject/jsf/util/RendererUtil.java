/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
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

package org.sakaiproject.jsf.util;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

/**
 * Common static utility methods that help in implementing JSF tags.
 */
public class RendererUtil
{

    /** This class is meant for static use only */
    private RendererUtil()
    {
    }

    /**
     * Sets component attribute value - if a ValueBinding exists for that
     * attribute, set through the binding; otherwise, set the value directly on
     * the component.
     */
    public static void setAttribute(FacesContext context, UIComponent component, String name,
            Object value)
    {
        ValueBinding binding = component.getValueBinding(name);
        if (binding != null)
        {
            try
            {
                binding.setValue(context, value);
            } catch (IllegalArgumentException e)
            {
                // try setting the value as a String
                binding.setValue(context, String.valueOf(value));
            }
        } else
        {
            component.getAttributes().put(name, value);
        }
    }

    /**
     * Return the attribute value - handles getting the value from a
     * ValueBinding if necessary. This is necessary because of a difference in
     * the Sun JSF RI versus the MyFaces RI. The Sun RI
     * component.getAttributes().get(attrName) will automatically return value
     * bindings, whereas the MyFaces implmentation requires getting values from
     * ValueBinding seperately.
     */
    public static Object getAttribute(FacesContext context, UIComponent component, String name)
    {
        // first check the attributes
        Object ret = component.getAttributes().get(name);
        if (ret != null)
            return ret;

        // next check the value bindings
        ValueBinding vb = component.getValueBinding(name);
        if (vb != null)
            ret = vb.getValue(context);

        return ret;
    }

    /**
     * Same as getAttribute, but if not found, we return a default value.
     */
    public static Object getDefaultedAttribute(FacesContext context, UIComponent component,
            String name, Object defaultValue)
    {
        Object o = getAttribute(context, component, name);
        if (o == null)
            o = defaultValue;
        return o;
    }

    /**
     * Helper method for recursively encoding a component.
     * 
     * @param context
     *            the given FacesContext
     * @param component
     *            the UIComponent to render
     * @throws IOException
     */
    public static void encodeRecursive(FacesContext context, UIComponent component)
            throws IOException
    {
        if (!component.isRendered())
        {
            return;
        }

        component.encodeBegin(context);

        if (component.getRendersChildren())
        {
            component.encodeChildren(context);
        } else
        {
            Iterator iter = component.getChildren().iterator();

            while (iter.hasNext())
            {
                UIComponent child = (UIComponent) iter.next();
                encodeRecursive(context, child);
            }
        }
        component.encodeEnd(context);
    }

    /**
     * If renderer supports disabled or readonly attributes use this method to
     * obtain an early exit from decode method. Good idea to include it anyway,
     * compnent will continue to work when these properties are added.
     */
    public static boolean isDisabledOrReadonly(FacesContext context, UIComponent component)
    {
        boolean disabled = false;
        boolean readOnly = false;

        Object disabledAttr = getAttribute(context, component, "disabled");
        if (disabledAttr != null)
        {
            disabled = disabledAttr.equals(Boolean.TRUE);
        }

        Object readOnlyAttr = getAttribute(context, component, "readonly");
        if (readOnlyAttr != null)
        {
            readOnly = readOnlyAttr.equals(Boolean.TRUE);
        }

        return readOnly | disabled;
    }

    /**
     * Write default HTML passthrough attributes
     */
    public static void writePassthroughs(FacesContext context, UIComponent component)
            throws IOException
    {
        String[] passthrus = { "ondblclick", "onclick", "onkeydown", "onkeypress", "onkeyup",
                "onmousedown", "onmousemove", "onmouseout", "onmouseover", "onmouseup" };
        writePassthroughAttributes(passthrus, true, context, component);
    }

    /**
     * write passthough attributes on the current element
     */
    public static void writePassthroughAttributes(String[] passthrus, boolean writeNullAttrs,
            FacesContext context, UIComponent component) throws IOException
    {
        ResponseWriter writer = context.getResponseWriter();
        for (int i = 0; i < passthrus.length; i++)
        {
            String key = passthrus[i];
            String value = (String) getAttribute(context, component, key);
            if (writeNullAttrs && value == null)
                value = "";
            if (value != null)
                writer.writeAttribute(key, value, null);
        }
    }

    /**
     * @param attributeMap
     *            String key/value pairs
     * @param writer
     *            response writer
     */
    public static void writeAttributes(Map attributeMap, ResponseWriter writer) throws IOException
    {
        Iterator iter = attributeMap.entrySet().iterator();
        while (iter.hasNext())
        {
            Entry entry = (Entry) iter.next();
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (value == null)
                value = "";
            writer.writeAttribute(key, value, key);
        }

    }

    /**
     * @param attributeMap
     *            String key/value pairs
     * @param context
     *            Faces context
     * @param component
     *            the UIComponent
     * @throws IOException
     */
    public static void writeAttributes(Map attributeMap, FacesContext context) throws IOException
    {
        ResponseWriter writer = context.getResponseWriter();
        writeAttributes(attributeMap, writer);
    }

    /**
     * Renders a script that includes an external JavaScript that gets added to
     * the document through a document.write() if a gatekeeper value is NOT set.
     * This effectively makes the script inclusion a per request JavaScript
     * singleton.
     * 
     * @param gateKey
     *            for key value pair
     * @param gateValue
     *            value for key value pair for gatekeeper
     * @param contextBasePath
     *            the web app with the script
     * @param scriptPath
     *            the webapp-relative path
     * @throws IOException
     */
    public static void writeSmartExternalScripts(FacesContext context, String gateKey,
            String gateValue, String contextBasePath, String[] scriptPaths) throws IOException
    {
        ResponseWriter writer = context.getResponseWriter();
        writeSmartExternalScripts(writer, gateKey, gateValue, contextBasePath, scriptPaths);
    }

    /**
     * Renders a script that includes an external JavaScript that gets added to
     * the document through a document.write() if a gatekeeper value is NOT set.
     * This effectively makes the script inclusion a per request JavaScript
     * singleton.
     * 
     * @param writer
     *            the ResponseWriter
     * @param gateKey
     *            for key value pair
     * @param gateValue
     *            value for key value pair for gatekeeper
     * @param contextBasePath
     *            the web app with the script
     * @param scriptPath
     *            the webapp-relative path
     * @throws IOException
     */
    public static void writeSmartExternalScripts(ResponseWriter writer, String gateKey,
            String gateValue, String contextBasePath, String[] scriptPaths) throws IOException
    {
        writer.write("<script>");
        writer.write("  if (typeof window['" + gateKey + "'] == '" + gateValue + "')");
        writer.write("  {");

        for (int i = 0; i < scriptPaths.length; i++)
        {
            writer.write("    document.write(");
            writer.write("   \"<\" + \"script type='text/javascript' src='/'\" + "
                    + contextBasePath + " +");
            writer.write("   \"'" + scriptPaths[i] + "'><\" + \"/script>);");
        }

        writer.write("   var " + gateKey + " = '" + gateValue + "';");

        writer.write("  }");
        writer.write("</script>");
        writer.write("");
        writer.write("");
    }

    /**
     * Get a Map of String key/value pairs from a UIComponent for all attributes
     * keys in a collection
     * 
     * @param collection
     * @param component
     * @return Map of String key/value pairs from a UIComponent for all keys in
     *         a collection
     */
    public static Map mapComponentAttributes(Collection collection, UIComponent component)
    {
        Map attributeMap = new HashMap();
        if (collection == null)
            return attributeMap;
        String[] attributeNames = new String[collection.size()];
        Object[] objs = collection.toArray();
        for (int i = 0; i < objs.length; i++)
        {
            attributeNames[i] = (String) objs[i];
        }
        return mapComponentAttributes(attributeNames, component);
    }

    /**
     * Get String key/value pairs from a UIComponent for all attributes keys in
     * an array
     * 
     * @param attributeNames
     * @param component
     * @return Map of String key/value pairs from a UIComponent for all keys in
     *         a collection
     */
    public static Map mapComponentAttributes(String[] attributeNames, UIComponent component)
    {
        Map attributeMap = new HashMap();
        for (int i = 0; i < attributeNames.length; i++)
        {
            attributeMap.put(attributeNames[i], (String) component.getAttributes().get(
                    attributeNames[i]));
        }
        return attributeMap;
    }

    /**
     * Switch handling utility.
     * 
     * @param rawSwitch
     *            String input string
     * @param supportOnOff
     *            boolean can input string be on, off?
     * @param supportTrueFalse
     *            boolean can input string be true, false?
     * @param supportYesNo
     *            boolean can input string be yes, no?
     * @param returnOnOff
     *            boolean output on, off instead of true false
     * @param returnYesNo
     *            boolean output yes, no instead of true false
     * @param defaultValue
     *            boolean if unknown, return true or false?
     * @return String raw swrich sring translated to correct switch value or
     *         default
     */
    public static String makeSwitchString(String rawSwitch, boolean supportOnOff,
            boolean supportTrueFalse, boolean supportYesNo, boolean returnOnOff,
            boolean returnYesNo, boolean defaultValue)
    {
        boolean switchValue = defaultValue;

        String trueString = "true";
        String falseString = "false";

        if (returnOnOff)
        {
            trueString = "on";
            falseString = "off";
        } else if (returnYesNo)
        {
            trueString = "yes";
            falseString = "no";
        }

        if (supportOnOff)
        {
            if ("on".equalsIgnoreCase(rawSwitch))
                switchValue = true;
            if ("off".equalsIgnoreCase(rawSwitch))
                switchValue = false;
        } 
        if (supportTrueFalse)
        {
            if ("true".equalsIgnoreCase(rawSwitch))
                switchValue = true;
            if ("false".equalsIgnoreCase(rawSwitch))
                switchValue = false;
        } 
        if (supportYesNo)
        {
            if ("yes".equalsIgnoreCase(rawSwitch))
                switchValue = true;
            if ("no".equalsIgnoreCase(rawSwitch))
                switchValue = false;
        }

        if (switchValue)
        {
            return trueString;
        } else
        {
            return falseString;
        }
    }

    /**
     * Given a List of SelectItems render the select options
     * 
     * @param out
     * @param items
     *            List of SelectItems
     * @param selected
     *            seelcted choice
     * @param clientId
     *            the id
     * @param styleClass
     *            the optional style class
     * @param component
     *            the component being rendered
     * @throws IOException
     */

    public static void renderMenu(ResponseWriter out, List items, int selected, String clientId,
            String styleClass, UIComponent component) throws IOException
    {
        // // debug lines
        // out.writeText("startElement select", null);
        // if (true) return;
        out.startElement("select", component);
        out.writeAttribute("name", clientId, "id");
        out.writeAttribute("id", clientId, "id");
        if (styleClass != null)
        {
            out.writeAttribute("class", styleClass, "styleClass");
        }

        Iterator iter = items.iterator();
        while (iter.hasNext())
        {
            SelectItem si = (SelectItem) iter.next();
            Integer value = (Integer) si.getValue();
            String label = si.getLabel();
            out.startElement("option", component);
            out.writeAttribute("value", value, null);
            if (value.intValue() == selected)
            {
                out.writeAttribute("selected", "selected", null);
            }
            out.writeText(label, null);
        }
        out.endElement("select");
    }

    /** Return the form ID of the form containing the given component */
    public static String getFormId(FacesContext context, UIComponent component)
    {
        while (component != null && !(component instanceof UIForm))
        {
            component = component.getParent();
        }
        if (component != null)
            return ((UIForm) component).getId();
        return null;
    }
    
    
    /**
     * @param context FacesContext for the request we are processing
     * @param writer ResponseWriter to be used
     * @param key key to use to look up the value in the request
     * @param path path to the file
     * @exception IOException if an input/output error occurs while rendering
     */
    public static void writeExternalJSDependencies(FacesContext context, 
          ResponseWriter writer, String key, String path) throws IOException
    {
       HttpServletRequest req = (HttpServletRequest) context.getExternalContext().getRequest();
       String jsInclude= (String) req.getAttribute(key);
       
       if (jsInclude == null || jsInclude.length() == 0)
       {
          // include default stylesheet
          jsInclude = "<script type=\"text/javascript\" src=\"" + path + "\"></script>\n";
          req.setAttribute(key, jsInclude);
          writer.write(jsInclude);
       }
    }
    
    
    /**
     * @param context FacesContext for the request we are processing
     * @param writer ResponseWriter to be used
     * @param key key to use to look up the value in the request
     * @param path path to the file
     * @exception IOException if an input/output error occurs while rendering
     */
    public static void writeExternalCSSDependencies(FacesContext context, 
          ResponseWriter writer, String key, String path) throws IOException
    {
       HttpServletRequest req = (HttpServletRequest) context.getExternalContext().getRequest();
       String cssInclude = (String) req.getAttribute(key);
       
       if (cssInclude == null || cssInclude.length() == 0)
       {
          // include default stylesheet
          cssInclude = "<link href=\"" + path + "\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n";
          req.setAttribute(key, cssInclude);
          writer.write(cssInclude);
       }
    }
    
    public static void writeAttr(Writer inWriter, String inAttr, String inAttrValue)
    throws IOException
    {
       if(inWriter == null || inAttr == null || inAttrValue == null)
          return;

       inWriter.write(" ");
       inWriter.write(inAttr);
       inWriter.write("=\"");
       inWriter.write(inAttrValue);
       inWriter.write("\" ");
    }

}
