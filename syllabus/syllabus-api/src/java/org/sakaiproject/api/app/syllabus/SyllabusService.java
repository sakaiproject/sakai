/**********************************************************************************
*
* $Header: /cvs/sakai2/syllabus/syllabus-api/src/java/org/sakaiproject/api/app/syllabus/SyllabusService.java,v 1.1 2005/05/19 14:25:58 cwen.iupui.edu Exp $
*
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

package org.sakaiproject.api.app.syllabus;

import org.sakaiproject.service.legacy.resource.Resource;
import org.sakaiproject.service.legacy.resource.ResourceService;

public interface SyllabusService extends ResourceService
{
  /** This string can be used to find the service in the service manager. */
	public static final String SERVICE_NAME = SyllabusService.class.getName();
	
	public static final String EVENT_SYLLABUS_POST_NEW = "syllabus.post.new";
	
	public static final String EVENT_SYLLABUS_POST_CHANGE = "syllabus.post.change";
	
	public static final String EVENT_SYLLABUS_DELETE_POST = "syllabus.delete.posted";
	
	public static final String REFERENCE_ROOT = Resource.SEPARATOR + "syllabus";
	
	public void postNewSyllabus(SyllabusData data);
	
	public void postChangeSyllabus(SyllabusData data);
	
	public void deletePostedSyllabus(SyllabusData data);
	
}

/**************************************************************************************************************************************************************************************************************************************************************
 * $Header: /cvs/sakai2/syllabus/syllabus-api/src/java/org/sakaiproject/api/app/syllabus/SyllabusService.java,v 1.1 2005/05/19 14:25:58 cwen.iupui.edu Exp $
 *************************************************************************************************************************************************************************************************************************************************************/
