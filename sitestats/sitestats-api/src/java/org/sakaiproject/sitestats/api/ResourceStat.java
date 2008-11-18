/**********************************************************************************
 *
 * Copyright (c) 2006 Universidade Fernando Pessoa
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
package org.sakaiproject.sitestats.api;


/**
 * Represents a record from the SST_RESOURCES table.
 * @author Nuno Fernandes
 */
public interface ResourceStat extends Stat{
	/** Get the the resource reference (eg. '/content/group/site_id/filename.txt') this record refers to. */
	public String getResourceRef();
	/** Set the the resource reference (eg. '/content/group/site_id/filename.txt') this record refers to. */
	public void setResourceRef(String resourceRef);

	/** Get the the resource action (one of 'new','read','revise','delete') this record refers to. */
	public String getResourceAction();
	/** Set the the resource action (one of 'new','read','revise','delete') this record refers to. */
	public void setResourceAction(String resourceAction);
}
