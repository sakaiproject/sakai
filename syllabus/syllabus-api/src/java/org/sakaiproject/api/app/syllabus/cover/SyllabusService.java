/**********************************************************************************
 *
 * $Header: /cvs/sakai2/syllabus/syllabus-api/src/java/org/sakaiproject/api/app/syllabus/cover/SyllabusService.java,v 1.1 2005/05/19 14:25:58 cwen.iupui.edu Exp $
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

package org.sakaiproject.api.app.syllabus.cover;

import org.sakaiproject.service.framework.component.cover.ComponentManager;
import org.sakaiproject.service.legacy.resource.ResourceProperties;

public class SyllabusService
{
  private static org.sakaiproject.api.app.syllabus.SyllabusService m_instance = null;

  /**
   * Access the component instance: special cover only method.
   * @return the component instance.
   */
public static org.sakaiproject.api.app.syllabus.SyllabusService getInstance()
	{
	  if (ComponentManager.CACHE_SINGLETONS)
		{
			if(m_instance != null)
			{
				return m_instance;
			}
			else
			{
				synchronized(m_instance)
				{
					if(m_instance == null)
					{
						if (m_instance == null) m_instance = (org.sakaiproject.api.app.syllabus.SyllabusService) ComponentManager.get( org.sakaiproject.api.app.syllabus.SyllabusService.class);
						return m_instance;
					}
					else
					{
					  return m_instance;
					}
				}
			}
		}
		else
		{
			return (org.sakaiproject.api.app.syllabus.SyllabusService) ComponentManager.get(org.sakaiproject.api.app.syllabus.SyllabusService.class);
		}
	}
  public static java.lang.String merge(java.lang.String param0,
      org.w3c.dom.Element param1, java.lang.String param2,
      java.lang.String param3, java.util.Map param4, java.util.HashMap param5,
      java.util.Set param6)
  {
    org.sakaiproject.api.app.syllabus.SyllabusService service = getInstance();
    if (service == null) return null;

    return service
        .merge(param0, param1, param2, param3, param4, param5, param6);
  }

  public static java.lang.String getLabel()
  {
    org.sakaiproject.api.app.syllabus.SyllabusService service = getInstance();
    if (service == null) return null;

    return service.getLabel();
  }

  public static java.lang.String archive(java.lang.String param0,
      org.w3c.dom.Document param1, java.util.Stack param2,
      java.lang.String param3,
      org.sakaiproject.service.legacy.resource.ReferenceVector param4)
  {
    org.sakaiproject.api.app.syllabus.SyllabusService service = getInstance();
    if (service == null) return null;

    return service.archive(param0, param1, param2, param3, param4);

  }

  public static void importResources(java.lang.String param0,
      java.lang.String param1, java.util.List param2)
  {
    org.sakaiproject.api.app.syllabus.SyllabusService service = getInstance();
    if (service == null) return;

    service.importResources(param0, param1, param2);
  }
}

/**************************************************************************************************************************************************************************************************************************************************************
 * $Header: /cvs/sakai2/syllabus/syllabus-api/src/java/org/sakaiproject/api/app/syllabus/cover/SyllabusService.java,v 1.1 2005/05/19 14:25:58 cwen.iupui.edu Exp $
 *************************************************************************************************************************************************************************************************************************************************************/
