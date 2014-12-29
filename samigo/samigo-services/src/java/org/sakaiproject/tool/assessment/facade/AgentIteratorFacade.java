/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.facade;

import java.util.Collection;
import java.util.Iterator;

/**
 * A Section iterator implementation.
 *
 * @author Rachel Gollub <rgollub@stanford.edu>
 */
public class AgentIteratorFacade
{
  private Iterator agentIter;

  /**
   * Creates a new SectionIteratorImpl object.
   *
   * @param psections DOCUMENTATION PENDING
   */
  public AgentIteratorFacade(Collection agents)
  {
    agentIter = agents.iterator();
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   *
   * @throws DataFacadeException DOCUMENTATION PENDING
   */
  public boolean hasNextAgent()
    throws DataFacadeException
  {
    try{
      return agentIter.hasNext();
    }
    catch(Exception e){
      throw new DataFacadeException("No objects to return.");
    }
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   *
   * @throws DataFacadeException DOCUMENTATION PENDING
   */
  public AgentFacade nextAgent()
    throws DataFacadeException
  {
    try
    {
      return (AgentFacade) agentIter.next();
    }
    catch(Exception e)
    {
      throw new DataFacadeException("No objects to return.");
    }
  }
}
