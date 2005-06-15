package org.sakaiproject.component.app.scheduler;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @version $id$
 */
public class ClassFileNameFilter implements FilenameFilter
{

  private static final String CLASS_SUFFIX = ".class";

  /**
   * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
   */
  public boolean accept(File arg0, String arg1)
  {
    if (arg1.toLowerCase().endsWith(CLASS_SUFFIX))
    {
      return true;
    }
    return false;
  }
}