package org.sakaiproject.tool.assessment.osid.authz.impl;

import java.util.Iterator;
import java.util.Collection;

import org.osid.authorization.AuthorizationException;
import org.osid.authorization.Qualifier;
import org.osid.authorization.QualifierIterator;

/**
 * DOCUMENT ME!
 *
 * @author <a href="mailto:jlannan@iupui.edu">Jarrod Lannan</a>
 * @version $Id: QualifierIteratorImpl.java,v 1.1 2004/10/21 17:10:37 daisyf.stanford.edu Exp $
 */
public class QualifierIteratorImpl
  implements QualifierIterator
{
  private Iterator qualifierIter;

  /**
   * DOCUMENT ME!
   *
   * @param i
   */
  public QualifierIteratorImpl(Collection c)
  {
    qualifierIter = c.iterator();
  }

  /* (non-Javadoc)
   * @see osid.authorization.QualifierIterator#hasNext()
   */
  public boolean hasNextQualifier()
    throws AuthorizationException
  {
    try{
      return qualifierIter.hasNext();
    }
    catch(Exception e){
      throw new AuthorizationException(e.getMessage());
    }
  }

  /* (non-Javadoc)
   * @see osid.authorization.QualifierIterator#next()
   */
  public Qualifier nextQualifier()
    throws AuthorizationException
  {
    try {
      return (Qualifier)qualifierIter.next();
    }
    catch (Exception e) {
      throw new AuthorizationException(e.getMessage());
    }
  }

}
