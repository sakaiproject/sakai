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
