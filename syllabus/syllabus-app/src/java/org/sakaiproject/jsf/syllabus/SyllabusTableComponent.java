/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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
package org.sakaiproject.jsf.syllabus;

import javax.faces.component.html.HtmlDataTable;

public class SyllabusTableComponent extends HtmlDataTable
{
	public SyllabusTableComponent()
	{
		super();

		setStyleClass("table table-striped table-bordered");
		setCellpadding("0");
		setCellspacing("0");
		setSummary("When used in main edit list - message is: First column holds the syllabus item, second and third column hold links to move the item up or down the list, fourth column indicates if the item has been posted, last column has a checkbox, select to remove. When table is being used to display attachments message needs to be: First column holds the attachment name and a link to remove it from the list. Second column holds the size, third, the type, fourth the author, last column the person to edit the attachment last. Both messages need to come from the bundle.");
		setColumnClasses("item,move,move,status,status");
	}
}



