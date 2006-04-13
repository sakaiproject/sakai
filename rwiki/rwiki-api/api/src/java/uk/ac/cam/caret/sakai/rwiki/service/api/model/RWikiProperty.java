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
package uk.ac.cam.caret.sakai.rwiki.service.api.model;

/**
 * Represents RWiki properties tied to the db instance
 * 
 * @author ieb
 */
// FIXME: Service
public interface RWikiProperty
{
	/**
	 * The ID
	 * 
	 * @return
	 */
	String getId();

	/**
	 * The ID
	 * 
	 * @param id
	 */
	void setId(String id);

	/**
	 * The name of the property
	 * 
	 * @return
	 */
	String getName();

	/**
	 * The Name of the property
	 * 
	 * @param name
	 */
	void setName(String name);

	/**
	 * The Value of the property
	 * 
	 * @return
	 */
	String getValue();

	/**
	 * The Value of the property
	 * 
	 * @param value
	 */
	void setValue(String value);

}
