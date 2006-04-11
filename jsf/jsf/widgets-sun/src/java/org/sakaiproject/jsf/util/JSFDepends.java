/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.jsf.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This source file collects the dependencies of the Sakai tag library
 * on the JSF implementation into one place.
 * This is where JSF tags, renderers, and components that extend the
 * Sun JSF implementation (or the MyFaces implementation) live.
 * To switch between Sun RI vs. MyFaces, just comment/uncomment
 * the appropriate block of inner classes and recompile.
 */
public class JSFDepends
{
    private static final Log logger = LogFactory.getLog(JSFDepends.class);

	  /** Sun JSF RI dependent classes */
	  public static class CommandButtonTag extends com.sun.faces.taglib.html_basic.CommandButtonTag {}
	  public static class InputTextTag extends com.sun.faces.taglib.html_basic.InputTextTag {}
	  public static class OutputTextTag extends com.sun.faces.taglib.html_basic.OutputTextTag {}
	  public static class PanelGridTag extends com.sun.faces.taglib.html_basic.PanelGridTag {}
	  public static class DataTableTag extends com.sun.faces.taglib.html_basic.DataTableTag {}
	  public static class MessagesTag extends com.sun.faces.taglib.html_basic.MessagesTag {}
	  public static class ColumnTag extends com.sun.faces.taglib.html_basic.ColumnTag {}

	  public static class ButtonRenderer extends com.sun.faces.renderkit.html_basic.ButtonRenderer {}
	  public static class CommandLinkRenderer extends com.sun.faces.renderkit.html_basic.CommandLinkRenderer {}

}




