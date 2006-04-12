/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2003 The Apache Software Foundation.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.commons.jrcs.rcs;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.jrcs.diff.Diff;

public class ChangeDeltaTest extends TestCase
{
    private Archive archive = null;

    String[] v1 = new String[]
    {
        "1",
        "2",
        "3",
        "4",
    };
    Object[] v2 = new String[]
    {
        "a0",
        "1",
        // deleted two lines
        // added three lines
        "a1",
        "a2",
        "a3",
        "4"
    };

    public ChangeDeltaTest(String name)
    {
        super (name);
    }

    protected void setUp()
        throws Exception
    {
        archive = new Archive(v1, "original");
        super.setUp();
    }

    protected void tearDown()
        throws Exception
    {
        archive = null;
        super.tearDown();
    }

    public static Test suite()
    {
        return new TestSuite(ArchiveTest.class);
    }

    public void testChangeDelta()
        throws Exception
    {
        archive.addRevision(v2, "applied change delta");
        archive.addRevision(v1, "back to original");

        String[] rcsFile = (String[]) Diff.stringToArray(archive.toString());
        for(int i = 0; i < rcsFile.length && i < expectedFile.length; i++)
        {
            if (! rcsFile[i].startsWith("date"))
                assertEquals("line " + i, expectedFile[i], rcsFile[i]);
        }
        assertEquals("file size", expectedFile.length, rcsFile.length);
    }

    public void testFileSave()
       throws Exception
   {
      this.testChangeDelta();
      String filePath =System.getProperty("user.home") + java.io.File.separator + "jrcs_test.rcs";
      archive.save(filePath);

      Archive newArc = new Archive(filePath);
      new java.io.File(filePath).delete();

      String[] rcsFile = (String[]) Diff.stringToArray(newArc.toString());
      for(int i = 0; i < rcsFile.length && i < expectedFile.length; i++)
      {
          if (! rcsFile[i].startsWith("date"))
              assertEquals("line " + i, expectedFile[i], rcsFile[i]);
      }
      assertEquals("file size", expectedFile.length, rcsFile.length);

      assertEquals(archive.toString(), newArc.toString());
   }


    String[] expectedFile = {
            "head\t1.3;",          // 0
            "access;",             // 1
            "symbols;",            // 2
            "locks; strict;",      // 3
            "comment\t@# @;",      // 4
            "",                    // 5
            "",                    // 6
            "1.3",                 // 7
            "date\t2002.09.28.12.55.36;\tauthor juanca;\tstate Exp;",
            "branches;",           // 9
            "next\t1.2;",          //10
            "",                    //11
            "1.2",                 //12
            "date\t2002.09.28.12.53.53;\tauthor juanca;\tstate Exp;",
            "branches;",           //14
            "next\t1.1;",          //15
            "",                    //16
            "1.1",                 //17
            "date\t2002.09.28.12.52.55;\tauthor juanca;\tstate Exp;",
            "branches;",           //19
            "next\t;",             //20
            "",                    //21
            "",                    //22
            "desc",                //23
            "@@",                  //24
            "",                    //25
            "",                    //26
            "1.3",                 //27
            "log",                 //28
            "@back to original",   //29
            "@",                   //30
            "text",                //31
            "@1",                  //32
            "2",                   //33
            "3",                   //34
            "4",                   //35
            "@",                   //36
            "",                    //37
            "",                    //38
            "1.2",                 //39
            "log",                 //40
            "@applied change delta",  //41
            "@",                   //42
            "text",                //43
            "@a0 1",               //44
            "a0",                  //45
            "d2 2",                //46
            "a3 3",                //47
            "a1",                  //48
            "a2",                  //49
            "a3",                  //50
            "@",                   //51
            "",                    //52
            "",                    //53
            "1.1",                 //54
            "log",                 //55
            "@original",           //56
            "@",                   //57
            "text",
            "@d1 1",
            "d3 3",
            "a5 2",
            "2",
            "3",
            "@"
    };
}
