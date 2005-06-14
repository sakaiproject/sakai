/*
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
*/


package org.sakaiproject.tool.assessment.services.shared;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.facade.TypeFacade;
import org.sakaiproject.tool.assessment.services.PersistenceService;


/**
 * The QuestionPoolService calls the service locator to reach the
 * manager on the back end.
 * @author Rachel Gollub <rgollub@stanford.edu>
 */
public class TypeService
{
  private static Log log = LogFactory.getLog(TypeService.class);

  /**
   * Creates a new QuestionPoolService object.
   */
  public TypeService()  {
  }

  public TypeFacade getTypeById(String typeId)
  {
    try{
      return PersistenceService.getInstance().getTypeFacadeQueries().
          getTypeFacadeById(new Long(typeId));
    }
    catch(Exception e)
    {
      log.error(e); throw new Error(e);
    }
  }

  public List getFacadeListByAuthorityDomain(String authority, String domain)
  {
    try{
      return PersistenceService.getInstance().getTypeFacadeQueries().
          getFacadeListByAuthorityDomain(authority,domain);
    }
    catch(Exception e)
    {
      log.error(e); throw new Error(e);
    }
  }

  public List getListByAuthorityDomain(String authority, String domain)
  {
    try{
      return PersistenceService.getInstance().getTypeFacadeQueries().
          getListByAuthorityDomain(authority,domain);
    }
    catch(Exception e)
    {
      log.error(e); throw new Error(e);
    }
  }

  public List getFacadeItemTypes() {
    try{
      return PersistenceService.getInstance().getTypeFacadeQueries().
          getFacadeItemTypes();
    }
    catch(Exception e)
    {
      log.error(e); throw new Error(e);
    }
  }
}
