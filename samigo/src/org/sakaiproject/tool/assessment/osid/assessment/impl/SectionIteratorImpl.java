package org.sakaiproject.tool.assessment.osid.assessment.impl;

import java.util.Iterator;
import java.util.Set;
import java.util.List;

import org.osid.assessment.AssessmentException;
import org.osid.assessment.Section;

public class SectionIteratorImpl
  implements org.osid.assessment.SectionIterator
{
  private final static org.apache.log4j.Logger LOG =
    org.apache.log4j.Logger.getLogger(SectionIteratorImpl.class);
  private Iterator sectionIterator;

  private Iterator sectionIter;

  /**
   * Creates a new ItemIteratorImpl object.
   *
   * @param pitems DOCUMENTATION PENDING
   */
  public SectionIteratorImpl(Set sectionSet)
  {
    this.sectionIter = sectionSet.iterator();
  }

  public SectionIteratorImpl(List sectionList)
  {
    this.sectionIter = sectionList.iterator();
  }

  /* (non-Javadoc)
   * @see osid.assessment.SectionIterator#hasNext()
   */
  public boolean hasNextSection()
    throws AssessmentException
  {
    return sectionIter.hasNext();
  }

  /* (non-Javadoc)
   * @see osid.assessment.SectionIterator#next()
   */
  public Section nextSection()
    throws AssessmentException
  {
    return (Section)sectionIter.next();
  }

}
