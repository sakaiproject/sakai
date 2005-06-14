package org.sakaiproject.tool.assessment.facade.authz;

import org.sakaiproject.tool.assessment.data.ifc.authz.QualifierIteratorIfc;
import org.sakaiproject.tool.assessment.data.ifc.authz.QualifierIfc;
import org.sakaiproject.tool.assessment.facade.DataFacadeException;
import java.util.Iterator;
import java.util.Collection;

public class QualifierIteratorFacade
  implements QualifierIteratorIfc
{
  private Iterator qualifierIter;

  public QualifierIteratorFacade(Collection c)
  {
    qualifierIter = c.iterator();
  }

  public boolean hasNextQualifier()
    throws DataFacadeException
  {
    try{
      return qualifierIter.hasNext();
    }
    catch(Exception e){
      throw new DataFacadeException(e.getMessage());
    }
  }

  public QualifierIfc nextQualifier()
    throws DataFacadeException
  {
    try {
      return (QualifierIfc)qualifierIter.next();
    }
    catch (Exception e) {
      throw new DataFacadeException(e.getMessage());
    }
  }

}
