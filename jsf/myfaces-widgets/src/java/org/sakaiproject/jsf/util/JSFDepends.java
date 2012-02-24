/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
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




