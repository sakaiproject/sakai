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
package org.sakaiproject.jsf.syllabus;

import javax.faces.component.html.HtmlDataTable;

public class SyllabusTableComponent extends HtmlDataTable
{
	public SyllabusTableComponent()
	{
		super();

		setStyleClass("listHier lines");
		setCellpadding("0");
		setCellspacing("0");
		setSummary("When used in main edit list - message is: First column holds the syllabus item, second and third column hold links to move the item up or down the list, fourth column indicates if the item has been posted, last column has a checkbox, select to remove. When table is being used to display attachments message needs to be: First column holds the attachment name and a link to remove it from the list. Second column holds the size, third, the type, fourth the author, last column the person to edit the attachment last. Both messages need to come from the bundle.");
		setColumnClasses("item,move,move,status,status");
	}
}



