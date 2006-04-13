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
package uk.ac.cam.caret.sakai.rwiki.service.api;

/**
 * A page link render renders links for the render engine
 * 
 * @author andrew
 */
// FIXME: Service
public interface PageLinkRenderer
{
	/**
	 * create a view link by appending it to the buffer
	 * 
	 * @param buffer
	 * @param name
	 * @param view
	 */
	void appendLink(StringBuffer buffer, String name, String view);

	/**
	 * Create a view link with an anchor by appending it to the buffer
	 * 
	 * @param buffer
	 * @param name
	 * @param view
	 * @param anchor
	 */
	void appendLink(StringBuffer buffer, String name, String view, String anchor);

	/**
	 * append a create link to the buffer
	 * 
	 * @param buffer
	 * @param name
	 * @param view
	 */
	void appendCreateLink(StringBuffer buffer, String name, String view);

	/**
	 * @param cachable
	 */
	void setCachable(boolean cachable);

	/**
	 * After rendering is the result cachable
	 * 
	 * @return
	 */
	boolean isCachable();

	/**
	 * Before rendering, can we use a chached version
	 * 
	 * @return
	 */
	boolean canUseCache();

	/**
	 * If true the rendered may use the cache, if false it should not
	 * 
	 * @param b
	 */
	void setUseCache(boolean b);

}
