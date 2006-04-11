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

package org.sakaiproject.jsf.renderer;

import java.io.IOException;
import java.util.Iterator;
import javax.faces.component.UIColumn;
import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import org.sakaiproject.jsf.component.MultiColumnComponent;
import org.sakaiproject.jsf.util.RendererUtil;


public abstract class DynaRendererBase
    extends Renderer {

  public DynaRendererBase() {
    super();
  }

  abstract public void encodeBegin(FacesContext context,
          UIComponent component) throws IOException;
  abstract public void encodeEnd(FacesContext context,
           UIComponent component) throws IOException;

  /**
 * <p>Faces render output method .</p>
 * <p>Encode children column and multicolumn</p>
 *
 *  @param context   <code>FacesContext</code> for the current request
 *  @param component <code>UIComponent</code> being rendered
 *
 * @throws IOException if an input/output error occurs
 */
 public void encodeChildren(FacesContext context, UIComponent component) throws
   IOException
 {
   if (!component.isRendered())
   {
     return;
   }

   renderData(context, component);
 }

  /**
 * This component renders its children
 * @return true
 */
  public boolean getRendersChildren()
  {
    return true;
  }

  /**
 * This is an UIData type component.
 * @param component
 * @return true if UIData
 */
  public boolean supportsComponentType(UIComponent component)
  {
    return (component instanceof UIData);
  }



  /**
   * Core workhouse method of the dynamic renderers.
   * @param context FacesContext
   * @param component UIComponent
   * @throws IOException
   */
  protected void renderData(FacesContext context, UIComponent component) throws
    IOException
  {
    boolean multiColumn = component instanceof MultiColumnComponent;
    ResponseWriter writer = context.getResponseWriter();

    UIData data = (UIData) component;

    int first = data.getFirst();
    int rows = data.getRows();

    for (int i = first, n = 0; n < rows; i++, n++)
    {
      data.setRowIndex(i);
      if (!data.isRowAvailable())
      {
        break;
      }
      ////////////////////////////////////
      //  TR
      ////////////////////////////////////
      writer.startElement("tr", data);

      Iterator iter = data.getChildren().iterator();
      while (iter.hasNext())
      {
        UIComponent child = (UIComponent) iter.next();
        System.out.println("child.getClass().getName()="+child.getClass().getName());

        // we skip if children are not of the type we want, we can add tests as needed
//        if (!(child instanceof UIColumn) && !(child instanceof MultiColumnComponent))
//        {
//          continue;
//        }

        if (child instanceof UIColumn)
        {
          writer.startElement("td", child);
          writer.write("debug UIColumn");
          RendererUtil.encodeRecursive(context, child);
          writer.endElement("td");
        }
        else if (child instanceof UIData)
        {
          writer.write("debug UIData");
          child.encodeBegin(context);
          child.encodeChildren(context);
          child.encodeEnd(context);

//          RendererUtil.encodeRecursive(context, child);

//          UIData multiData = (UIData) child;
//          int mFirst = multiData.getFirst();
//          int mRows = multiData.getRows();
//          String value = RendererUtil.getAttribute(context, )
//          mFirst=0; mRows=2;
//          mFirst=0; mRows=0;
//          System.out.println("mFirst="+mFirst);
//          System.out.println("mRows="+mRows);
//          for (int j = mFirst, m = 0; m < mRows; j++, m++)
//          {
//            System.out.println("j="+j);
//            System.out.println("m="+m);
//            System.out.println("data.isRowAvailable()="+data.isRowAvailable());
//            data.setRowIndex(j);
//            if (!data.isRowAvailable())
//            {
//              break;
//            }
//            Iterator multIter = multiData.getChildren().iterator();
//            while (multIter.hasNext()) {
//              UIComponent multiChild = (UIComponent) multIter.next();
//
//              if (multiChild instanceof UIColumn)
//              {
//                writer.startElement("td", multiChild);
//                RendererUtil.encodeRecursive(context, multiChild);
//                writer.endElement("td");
//              }
//            }
//          }
        }


//        ////////////////////////////////////
//        //  TD
//        ////////////////////////////////////
//        if (child instanceof UIColumn) writer.startElement("td", child);
//        RendererUtil.encodeRecursive(context, child);
//        ////////////////////////////////////
//        //  /TD
//        ////////////////////////////////////
//        if (child instanceof UIColumn) writer.endElement("td");

//        if (child instanceof UIColumn)
//        {
//          ////////////////////////////////////
//          //  TD
//          ////////////////////////////////////
//          writer.startElement("td", child);
//          RendererUtil.encodeRecursive(context, child);
//          ////////////////////////////////////
//          //  /TD
//          ////////////////////////////////////
//          writer.endElement("td");
//        }
//        else if (child instanceof MultiColumnComponent
//                 && !multiColumn) // cannot nest MultiColumnComponents!
//        {
//          renderData(context, child);
//        }
      }
      ////////////////////////////////////
      //  /TR
      ////////////////////////////////////
      writer.endElement("tr");
      writer.write("\n");
    }
  }

}
