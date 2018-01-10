/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.jsf.renderer;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.component.UIForm;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.faces.render.Renderer;

import lombok.extern.slf4j.Slf4j;


/**
 *
 * <p>Description:  modified from an example in the core jsf book</p>
 * <p>Copyright: Copyright (c) 2004 Sakai</p>
 * <p>Usage:
 * <samigo:pager
 *   controlId="controlerIdOfPagerButtonControl" dataTableId="idOfDataTable" showpages="999"
 *   styleClass="aStyle" selectedStyleClass="anotherStyle"/>
 * </p>
 * @author Lydia Li
 * @author Ed Smiley
 * @version $Id$
 */

@Slf4j
public class PagerRenderer
  extends Renderer
{

  public void encodeBegin(FacesContext context,
    UIComponent component) throws IOException
  {
    String id = component.getClientId(context);
    UIComponent parent = component;
    while (! (parent instanceof UIForm))
    {
      parent = parent.getParent();
    }
    String formId = parent.getClientId(context);

    ResponseWriter writer = context.getResponseWriter();

    String styleClass = (String) get(context, component, "styleClass");
    String selectedStyleClass = (String) get(context, component,
                                "selectedStyleClass");
    String dataTableId = (String) get(context, component, "dataTableId");
    String controlId = (String) get(context, component, "controlId");
    Integer a = (Integer) get(context, component, "showpages");
    int showpages = a == null ? 0 : a.intValue();

    // find the component with the given ID

    UIData data = (UIData) findComponent(context.getViewRoot(),
                  getId(dataTableId, id), context);

    int first = data.getFirst();
    int itemcount = data.getRowCount();
    int pagesize = data.getRows();
    if (pagesize <= 0)
    {
      pagesize = itemcount;

    }
    int pages;
    int currentPage;
    if (pagesize != 0)
    {
      pages = itemcount / pagesize;
      currentPage = first / pagesize;
      if (itemcount % pagesize != 0)
      {
        pages++;
      }
    }
    else
    {
      pages = 1;
      currentPage = 1;
    }

    if (first >= itemcount - pagesize)
    {
      currentPage = pages - 1;
    }
    int startPage = 0;
    int endPage = pages;
    if (showpages > 0)
    {
      startPage = (currentPage / showpages) * showpages;
      endPage = Math.min(startPage + showpages, pages);
    }

    boolean showLinks = true;
    Boolean showThem = (Boolean) get(context, component, "showLinks");
    if (showThem != null)
    {
      showLinks = showThem.booleanValue();
    }

    // links << < # # # > >>
    if (showLinks)
    {
      if (currentPage > 0)
      {
        writeLink(writer, component, formId, controlId, "<", styleClass);

      }
      if (startPage > 0)
      {
        writeLink(writer, component, formId, controlId, "<<", styleClass);

      }
      for (int i = startPage; i < endPage; i++)
      {
        writeLink(writer, component, formId, controlId, "" + (i + 1),
          i == currentPage ? selectedStyleClass : styleClass);
      }

      if (endPage < pages)
      {
        writeLink(writer, component, formId, controlId, ">>", styleClass);

      }
      if (first < itemcount - pagesize)
      {
        writeLink(writer, component, formId, controlId, ">", styleClass);
      }
    }

    // hidden field to hold result
    writeHiddenField(writer, component, controlId, id);
  }

  /**
   *
   * @param writer ResponseWriter
   * @param component UIComponent
   * @param formId String
   * @param id String
   * @param value String
   * @param styleClass String
   * @throws IOException
   */
  private void writeLink(ResponseWriter writer, UIComponent component,
    String formId, String id, String value,
    String styleClass) throws IOException
  {
    writer.writeText(" ", null);
    writer.startElement("a", component);
    writer.writeAttribute("href", "#", null);
    writer.writeAttribute("onclick", onclickCode(formId, id, value), null);
    if (styleClass != null)
    {
      writer.writeAttribute("class", styleClass, "styleClass");
    }
    writer.writeText(value, null);
    writer.endElement("a");
  }

  /**
   *
   * @param formId String
   * @param id String
   * @param value String
   * @return String
   */
  private String onclickCode(String formId, String id, String value)
  {
    StringBuilder buffer = new StringBuilder();
    buffer.append("document.forms[");
    buffer.append("'");
    buffer.append(formId);
    buffer.append("'");
    buffer.append("]['");
    buffer.append(id);
    buffer.append("'].value='");
    buffer.append(value);
    buffer.append("';");
    buffer.append(" document.forms[");
    buffer.append("'");
    buffer.append(formId);
    buffer.append("'");
    buffer.append("].submit()");
    buffer.append("; return false;");
    return buffer.toString();
  }

  /**
   *
   * @param writer ResponseWriter
   * @param component UIComponent
   * @param controlId String
   * @param id String
   * @throws IOException
   */
  private void writeHiddenField(ResponseWriter writer,
    UIComponent component,
    String controlId, String id) throws IOException
  {
    writer.startElement("input", component);
    writer.writeAttribute("type", "hidden", null);
    writer.writeAttribute("name", id, null);
    writer.writeAttribute("id", controlId, null);
    writer.endElement("input");
  }

  public void decode(FacesContext context, UIComponent component)
  {
    String id = component.getClientId(context);
    Map parameters = context.getExternalContext()
                     .getRequestParameterMap();
    String response = (String) parameters.get(id);

    String dataTableId = (String) get(context, component, "dataTableId");
    Integer a = (Integer) get(context, component, "showpages");
    int showpages = a == null ? 0 : a.intValue();

    UIData data = (UIData) findComponent(context.getViewRoot(),
                  getId(dataTableId, id), context);

    int first = data.getFirst();
    int itemcount = data.getRowCount();
    int pagesize = data.getRows();
    if (pagesize <= 0)
    {
      pagesize = itemcount;
    }

    if (response == null)
    {
      first = 0;
    }
    else if ("<".equals(response))
    {
      first -= pagesize;
    }
    else if (">".equals(response))
    {
      first += pagesize;
    }
    else if ("<<".equals(response))
    {
      first -= pagesize * showpages;
    }
    else if (">>".equals(response))
    {
      first += pagesize * showpages;
    }
    else
    {
      int page = 0; // default if cannot be parsed
      try
      {
        page = Integer.parseInt(response);
      }
      catch (NumberFormatException ex)
      {
        // do nothing, leave at zero
        log.debug("do nothing, leave at zero"); 
      }
      first = (page - 1) * pagesize;
    }

    if (first + pagesize > itemcount)
    {
      first = itemcount - pagesize;
    }

    if (first < 0)
    {
      first = 0;
    }
    data.setFirst(first);
  }

  /**
   *
   * @param context FacesContext
   * @param component UIComponent
   * @param name String
   * @return Object
   */
  private static Object get(FacesContext context, UIComponent component,
    String name)
  {
    ValueBinding binding = component.getValueBinding(name);
    if (binding != null)
    {
      return binding.getValue(context);
    }
    else
    {
      return component.getAttributes().get(name);
    }
  }

  /**
   *
   * @param component UIComponent
   * @param id String
   * @param context FacesContext
   * @return UIComponent
   */
  private static UIComponent findComponent(UIComponent component,
    String id,
    FacesContext context)
  {
    String componentId = component.getClientId(context);
    if (componentId.equals(id))
    {
      return component;
    }
    Iterator kids = component.getChildren().iterator();
    while (kids.hasNext())
    {
      UIComponent kid = (UIComponent) kids.next();
      UIComponent found = findComponent(kid, id, context);
      if (found != null)
      {
        return found;
      }
    }
    return null;
  }

  /**
   *
   * @param id String
   * @param baseId String
   * @return String
   */
  private static String getId(String id, String baseId)
  {
    String separator = "" + NamingContainer.SEPARATOR_CHAR;
    String[] idSplit = id.split(separator);
    String[] baseIdSplit = baseId.split(separator);
    StringBuilder buffer = new StringBuilder();
    for (int i = 0; i < baseIdSplit.length - idSplit.length; i++)
    {
      buffer.append(baseIdSplit[i]);
      buffer.append(separator);
    }
    buffer.append(id);
    return buffer.toString();
  }
}
