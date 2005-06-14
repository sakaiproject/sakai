/**********************************************************************************
 *
 * $Header: /cvs/sakai2/help/help-tool/src/java/org/sakaiproject/tool/help/TOCDisplayTool.java,v 1.2 2005/05/18 15:14:22 jlannan.iupui.edu Exp $
 *
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

package org.sakaiproject.tool.help;

import javax.faces.context.FacesContext;
import javax.faces.el.VariableResolver;

import org.sakaiproject.api.app.help.TableOfContents;

/**
 * toc display tool
 * @version $Id$
 */
public class TOCDisplayTool
{
  private TableOfContents tableOfContents;

  /**
   * get table of contents
   * @return Returns the tableOfContents.
   */
  public TableOfContents getTableOfContents()
  {
    if (tableOfContents == null)
    {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      VariableResolver resolver = facesContext.getApplication()
          .getVariableResolver();
      tableOfContents = ((TableOfContentsTool) resolver.resolveVariable(
          facesContext, "TableOfContentsTool")).getTableOfContents();
    }
    return tableOfContents;
  }

}