/*
 ***************************************************************************
 * Copyright 2003-2005 Luca Passani, passani at eunet.no                   *
 * Distributed under the Mozilla Public License                            *
 *   http://www.mozilla.org/NPL/MPL-1.1.txt                                *
 ***************************************************************************
 *   $Author$
 *   $Header: /cvsroot/wurfl/tools/java/wurflapi-xom/antbuild/src/net/sourceforge/wurfl/wurflapi/WurflTester.java,v 1.2 2005/02/13 15:11:39 passani Exp $
 */

package net.sourceforge.wurfl.wurflapi;

import java.io.IOException;
import junit.framework.*;

/**
 * @author Luca Passani 
 * Some Junit tests to make sure we do not break anything when
 * refactoring 
 *
 * I declared this as part of the package to gain access to the package
 * internal methods
 */
 
public class WurflTester extends TestCase {

  Wurfl wu = null;

  public static void main(String[] args) throws IOException {
      junit.textui.TestRunner.run(WurflTester.class);
  }
  
  public void setUp() throws IOException{
      wu = ObjectsManager.getWurflInstance();
  }

  public void testWurflMethods() {

    System.out.println("Testing WURFL methods");
    System.out.println("1 -> wurfl.isCapabilityIn(\"colors\");");
    assertEquals(true,wu.isCapabilityIn("colors"));
    System.out.println("2 -> wurfl.isDeviceIn(\"mot_t720_ver1_sub050841r\");");
    assertEquals(true,wu.isDeviceIn("mot_t720_ver1_sub050841r")); 
    assertEquals("generic",wu.getDeviceWhereCapabilityIsDefined("nokia_3650_ver1_subsemicolon","empty_option_value_support")); 
  }

}


