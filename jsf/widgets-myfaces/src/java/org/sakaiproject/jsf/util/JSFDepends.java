/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

	  /** MyFaces RI dependent classes - MyFaces version 1.0.9 */
	  public static class CommandButtonTag extends org.apache.myfaces.taglib.html.HtmlCommandButtonTag {}
	  public static class InputTextTag extends org.apache.myfaces.taglib.html.HtmlInputTextTag {}
	  public static class OutputTextTag extends org.apache.myfaces.taglib.html.HtmlOutputTextTag {}
	  public static class PanelGridTag extends org.apache.myfaces.taglib.html.HtmlPanelGridTag {}
	  public static class DataTableTag extends org.apache.myfaces.taglib.html.HtmlDataTableTag {}
	  public static class MessagesTag extends org.apache.myfaces.taglib.html.HtmlMessagesTag {}
	  public static class ColumnTag extends org.apache.myfaces.taglib.html.HtmlColumnTag {}

	  public static class ButtonRenderer extends org.apache.myfaces.renderkit.html.HtmlButtonRenderer {}
	  public static class CommandLinkRenderer extends org.apache.myfaces.renderkit.html.HtmlLinkRenderer {}


}




