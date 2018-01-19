/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tool.assessment.shared.impl.common;

import java.util.List;

import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.services.shared.TypeService;
import org.sakaiproject.tool.assessment.services.CommonServiceException;
import org.sakaiproject.tool.assessment.shared.api.common.TypeServiceAPI;


/**
 * Declares a shared interface to control type information.
 * @author Ed Smiley <esmiley@stanford.edu>
 */
public class TypeServiceImpl implements TypeServiceAPI
{

  /**
   * Get type for id
   * @param typeId the id
   * @return the type
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
   * Get list of types for authority/domain.
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
