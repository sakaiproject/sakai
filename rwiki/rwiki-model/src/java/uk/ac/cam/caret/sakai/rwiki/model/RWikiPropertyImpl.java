/**********************************************************************************
*
* $Header$
*
***********************************************************************************
*
* Copyright (c) 2005 University of Cambridge
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
package uk.ac.cam.caret.sakai.rwiki.model;

import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiProperty;
/**
* Implementation of an RWiki Property
* @author ieb
*
*/
//FIXME: Component

public class RWikiPropertyImpl implements RWikiProperty {
	private String id;
	private String name;
	private String value;
	/**
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.api.model.RWikiProperty#getId()
	 */
	public String getId() {
		return id;
	}
	/**
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.api.model.RWikiProperty#setId(java.lang.String)
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.api.model.RWikiProperty#getName()
	 */
	public String getName() {
		return name;
	}
	/**
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.api.model.RWikiProperty#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.api.model.RWikiProperty#getValue()
	 */
	public String getValue() {
		return value;
	}
	/**
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.api.model.RWikiProperty#setValue(java.lang.String)
	 */
	public void setValue(String value) {
		this.value = value;
	}
}
