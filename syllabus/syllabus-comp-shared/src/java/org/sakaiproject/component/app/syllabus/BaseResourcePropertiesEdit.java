/**********************************************************************************
* $URL: https://source.sakaiproject.org/svn/trunk/sakai/legacy/util/src/java/org/sakaiproject/util/resource/BaseResourcePropertiesEdit.java $
* $Id: BaseResourcePropertiesEdit.java 632 2005-07-14 21:22:50Z janderse@umich.edu $
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

// package
package org.sakaiproject.component.app.syllabus;

// imports
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.w3c.dom.Element;

/**
* <p>BaseResourceProperties is the base class for ResourcePropertiesEdit implementations.</p>
* 
* @author University of Michigan, CHEF Software Development Team
* @version $Revision: 632 $
* @see org.chefproject.core.ResourceProperties
*/
public class BaseResourcePropertiesEdit
	extends BaseResourceProperties
	implements ResourcePropertiesEdit
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	* Construct.
	*/
	public BaseResourcePropertiesEdit()
	{
		super();

	}   // BaseResourcePropertiesEdit

	/**
	* Construct from XML.
	* @param el The XML DOM element.
	*/
	public BaseResourcePropertiesEdit(Element el)
	{
		super(el);

	}	// BaseResourcePropertiesEdit

}   // BaseResourcePropertiesEdit



