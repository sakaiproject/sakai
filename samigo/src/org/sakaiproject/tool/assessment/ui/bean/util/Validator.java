/*
 * Copyright 2004, sakaiproject.org
 *
 * Validator.java
 */

package org.sakaiproject.tool.assessment.ui.bean.util;

/**
 * This class holds methods to make sure the JSF validator is happy.
 */
public class Validator {

  public static String check(String mytest, String mydefault)
  {
    if (mytest == null || mytest.trim().equals(""))
      return mydefault;
    return mytest;
  }

  public static Boolean bcheck(Boolean mytest, boolean mydefault)
  {
    if (mytest == null)
      return new Boolean(mydefault);
    return mytest;
  }
}
