/*
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
 */

package org.sakaiproject.tool.assessment.jsf.renderer.util;

import java.io.IOException;
import java.util.Iterator;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

/**
 * <p>Place for common utilities for renderers </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2004 Sakai</p>
 * @author Ed Smiley esmiley@stanford.edu
 * @version $Id$
 */

public class RendererUtil
{
  /**
   * Helper method for recursively encoding a component.
   * @param context the given FacesContext
   * @param component the UIComponent to render
   * @throws IOException
   */
  public static void encodeRecursive(FacesContext context,
    UIComponent component) throws IOException
  {
    if (!component.isRendered())
    {
      return;
    }

    component.encodeBegin(context);

    if (component.getRendersChildren())
    {
      component.encodeChildren(context);
    }
    else
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
   * obtain an early exit from decode method.  Good idea to include it anyway,
   * compnent will continue to work when these properties are added.
   * @param component
   * @return
   */
  public static boolean isDisabledOrReadonly(UIComponent component)
  {
    boolean disabled = false;
    boolean readOnly = false;

    Object disabledAttr = component.getAttributes().get("disabled");
    if (disabledAttr != null)
    {
      disabled = disabledAttr.equals(Boolean.TRUE);
    }

    Object readOnlyAttr = component.getAttributes().get("readonly");
    if (readOnlyAttr != null)
    {
      readOnly = readOnlyAttr.equals(Boolean.TRUE);
    }

    return readOnly | disabled;
  }

}