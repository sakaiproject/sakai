/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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



package org.sakaiproject.tool.podcasts.jsf.renderer.util;

import java.io.IOException;
import java.util.Iterator;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

/**
 * <p>Place for common utilities for renderers </p>
 * <p> </p>
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
