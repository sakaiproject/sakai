/**********************************************************************************
* $URL: https://source.sakaiproject.org/svn/trunk/sakai/sam/src/org/sakaiproject/tool/assessment/services/shared/TypeService.java $
* $Id: TypeService.java 632 2005-07-14 21:22:50Z janderse@umich.edu $
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

package org.sakaiproject.tool.assessment.shared.impl.common;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.services.shared.TypeService;
import org.sakaiproject.tool.assessment.shared.api.common.CommonServiceException;
import org.sakaiproject.tool.assessment.shared.api.common.TypeServiceAPI;


/**
 * Declares a shared interface to control type information.
 * @author Ed Smiley <esmiley@stanford.edu>
 */
public class TypeServiceImpl implements TypeServiceAPI
{
  private static Log log = LogFactory.getLog(TypeServiceImpl.class);

  /**
   * Get type for id
   * @param typeId the id
   * @return teh type
   */
  public TypeIfc getTypeById(String typeId)
  {
    try
    {
      TypeService service = new TypeService();
      return service.getTypeById(typeId);
    }
    catch (Exception ex)
    {
      throw new CommonServiceException(ex);
    }
  }

  /**
   * Get list of types for authority/domain
   * @param authority the authority
   * @param domain the domain
   * @return list of TypeIfc
   */
  public List getListByAuthorityDomain(String authority, String domain)
  {
    try
    {
      TypeService service = new TypeService();
      return service.getListByAuthorityDomain(authority, domain);
    }
    catch (Exception ex)
    {
      throw new CommonServiceException(ex);
    }
  }

  /**
   * Return list of item TypeIfcs.
   * @return list of item TypeIfcs
   */
  public List getItemTypes()
  {
    try
    {
      TypeService service = new TypeService();
      return service.getFacadeItemTypes();// TypeFacades, implement TypeIfc
    }
    catch (Exception ex)
    {
      throw new CommonServiceException(ex);
    }
  }
}
