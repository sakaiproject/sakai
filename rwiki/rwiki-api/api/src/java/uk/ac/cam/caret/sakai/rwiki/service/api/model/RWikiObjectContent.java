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

// FIXME: Service

public interface RWikiObjectContent
{
	/**
	 * The Id of the content
	 * 
	 * @return
	 */
	String getRwikiid();

	/**
	 * The Id of the content
	 * 
	 * @param rwikiid
	 */
	void setRwikiid(String rwikiid);

	/**
	 * The record ID
	 * 
	 * @return
	 */
	String getId();

	/**
	 * The record ID
	 * 
	 * @param id
	 */
	void setId(String id);

	/**
	 * The content
	 * 
	 * @return
	 */
	String getContent();

	/**
	 * The content
	 * 
	 * @param content
	 */
	void setContent(String content);

}
