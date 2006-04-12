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

import org.apache.commons.jrcs.util.ToString;
import org.apache.commons.jrcs.diff.Diff;
import org.apache.commons.jrcs.diff.DiffException;

public class ArchiveTest extends TestCase
{


    public ArchiveTest(String testName)
    {
        super(testName);
    }

    public static Test suite()
    {
        return new TestSuite(ArchiveTest.class);
    }

    Object[] v1_1 = new String[]{
        "[1] one",
        "[2] two",
        "[3] three",
        "[4] four",
        "[5] five",
        "[6] six",
    };
    Object[] v1_2 = new String[]{
        "[1] one",
        "[2] two",
        "[3] three",
        "[3.1]", // inserted this
        "[4] four",
        "[5] five"
        // deleted [6]
    };

    String user =  System.getProperty("user.name");

  // WARNING: there apparently uneeded string concatenations
  // are there to prevent CVS from mangling our test data.

    Object[] v1_2_with_keywords = new String[] {
        "",
        "[3] three  $" + "Source: trash #3$",
        "[4] four   $" + "RCSfile:  trash #4$",
        "[5] five   $" + "Revision: trash # 5 $",
        "[7] seven  $" + "Author: trash #7 $",
        "[8] eight  $" + "State: trash 8 $",
        "[9] nine   $Locker:  $",
        "[10] ten   $" + "RCSfile: trash #10 $ "
                 + "$" + "Revision: trash #10 $ "
                 + "$" +  "Author: trash $",
        ""
    };

    Object[] v1_2_with_expanded_keywords = new String[] {
        "",
        "[3] three  $" + "Source: /a/test/path/test_file,v $",
        "[4] four   $" + "RCSfile: test_file,v $",
        "[5] five   $" + "Revision: 1.2 $",
        "[7] seven  $" + "Author: " + user + " $",
        "[8] eight  $" + "State: Exp $",
        "[9] nine   $Locker:  $",
        "[10] ten   $" + "RCSfile: test_file,v $ "
                 + "$" + "Revision: 1.2 $ "
                 + "$" +  "Author: " + user + " $",
        ""
    };

    Object[] v1_3 = new String[]{
        "[1] one changed",
        "[2] two",
        "[3] three",
        "[3.1]",
        "[4] four",
        "[5] five"
    };

    Object[] v1_20 = new String[]{
        "[1:1.20] one changed",
        "[3] three",
        "[3.1]",
        "[5:1.20] five"
    };

    Object[] v1_2_1_1 = new String[]{
        "[1] one",
        "[2] two",
        "[2.1]",
        "[3] three",
        "[3.1]",
        "[4] four changed",
        "[5] five",
        "[5.1]"
    };

    Object[] v1_2_1_2 = new String[]{
        "[1:1.2.1.1] one",
        "[2.1]",
        "[3] three",
        "[3.1]",
        "[4] four changed",
        "[5:1.2.1.1] five",
        "[5.1]"
    };

    Object[] v1_2_8_2 = new String[]{
        "[1:1.2.8.1] one",
        "[2.1]",
        "[3] three",
        "[3.1]",
        "[4] four changed",
        "[5:1.2.8.1] five",
        "[5.1]"
    };

    Object[] v1_2_8_4 = new String[]{
        "[1:1.2.8.1] one",
        "[2.1]",
        "[3.1:1.2.8.2]",
        "[4] four changed",
        "[5:1.2.8.1] five",
        "[5.1]"
    };

    Object[] v1_2_8_5 = new String[]{
        "[1:1.2.8.5] one"
    };

    Archive archive;

    public void setUp()
    {
        archive = new Archive(v1_1, "A simple test file");
        archive.setFileName("/a/test/path/test_file,v");
    }


    public void testEmptyArchive()
    {
        try
        {
            Object[] rev = new Archive().getRevision();
            if (rev != null)
                fail("empty archive, exception should be thrown");
        }
        catch (Exception e)
        {
        }
    }

    public void testAdd1_1()
            throws DiffException,
            RCSException
    {
        Object[] rev = archive.getRevision();
        assertTrue(Diff.compare(v1_1, rev));
        assertNull(archive.addRevision(v1_1, "should not be added"));

        assertEquals(new Version("1.1"), archive.getRevisionVersion());
        assertEquals(new Version("1.1"), archive.getRevisionVersion("1."));

        assertNull(archive.getRevisionVersion("2"));
        assertNull(archive.getRevisionVersion("1.2.1"));

        Node[] log = archive.changeLog();
        assertNotNull("log is null", log);
        assertEquals(    1, log.length);
        assertEquals("1.1", log[0].version.toString());
    }

    public void testAdd1_2()
            throws DiffException,
            RCSException
    {
        testAdd1_1();
        archive.addRevision(v1_2, "Added 3.1, deleted 6");
        Object[] rev = archive.getRevision();
        assertEquals("1.2", archive.head.version.toString());
        assertEquals(ToString.arrayToString(v1_2), ToString.arrayToString(rev));
        assertNull(archive.addRevision(v1_2, "should not be added"));

        assertEquals(new Version("1.2"), archive.getRevisionVersion());
        assertEquals(new Version("1.2"), archive.getRevisionVersion("1."));

        assertNull(archive.getRevisionVersion("2"));
        assertNull(archive.getRevisionVersion("1.2.1"));

        Node[] log = archive.changeLog();
        assertNotNull("log is null", log);
        assertEquals(    2, log.length);
        assertEquals("1.1", log[0].version.toString());
        assertEquals("1.2", log[1].version.toString());
    }

     public void testAdd1_2_with_keywords()
     throws DiffException,
            RCSException
     {
       testAdd1_1();
       archive.addRevision(v1_2_with_keywords, "Added revision with keywords");
       Object[] rev = archive.getRevision();

       assertEquals("1.2", archive.head.version.toString());

       assertEquals(ToString.arrayToString(v1_2_with_expanded_keywords), ToString.arrayToString(rev));
       assertNull(archive.addRevision(v1_2_with_expanded_keywords, "should not be added"));

       assertEquals(new Version("1.2"), archive.getRevisionVersion());
       assertEquals(new Version("1.2"), archive.getRevisionVersion("1."));

       assertNull(archive.getRevisionVersion("2"));
       assertNull(archive.getRevisionVersion("1.2.1"));
     }

    public void testAdd1_3()
            throws DiffException,
            RCSException
    {
        testAdd1_2();
        archive.addRevision(v1_3, "Changed 1");
        Object[] rev = archive.getRevision();
        assertTrue(Diff.compare(v1_3, rev));
        assertNull(archive.addRevision(v1_3, "should not be added"));

        assertEquals(new Version("1.3"), archive.getRevisionVersion());
        assertEquals(new Version("1.3"), archive.getRevisionVersion("1"));

        assertNull(archive.getRevisionVersion("2"));
        assertNull(archive.getRevisionVersion("1.2.1"));

        Node[] log = archive.changeLog();
        assertNotNull("log is null", log);
        assertEquals(    3, log.length);
        assertEquals("1.1", log[0].version.toString());
        assertEquals("1.2", log[1].version.toString());
        assertEquals("1.3", log[2].version.toString());
    }

    public void testAdd1_2_1()
            throws DiffException,
            RCSException
    {
        testAdd1_3();
        archive.addRevision(v1_2_1_1, "1.2.1", "Added 2.1, changed 4, added 5.1");
        String filestr = archive.toString();
        String[] file = (String[]) Diff.stringToArray(filestr);

        for(int i = 0; i < sampleFile.length && i < file.length; i++)
        {
            if(!sampleFile[i].startsWith("date"))
                assertEquals("line " + i, sampleFile[i], file[i]);
        }
        assertEquals("file size", sampleFile.length, file.length);

        Object[] rev = archive.getRevision("1.2.1");
        assertTrue("diffs equal", Diff.compare(v1_2_1_1, rev));
        assertNull("should not be added", archive.addRevision(v1_2_1_1, "1.2.1", "should not be added"));

        assertEquals("dot", new Version("1.2.1.1"), archive.getRevisionVersion("1.2."));
        assertEquals("zero", new Version("1.2.1.1"), archive.getRevisionVersion("1.2.0"));

        assertEquals(new Version("1.2.1.1"), archive.getRevisionVersion("1.2.1"));
        assertEquals(new Version("1.2.1.1"), archive.getRevisionVersion("1.2.1"));

        assertNull(archive.getRevisionVersion("2"));
        assertNull(archive.getRevisionVersion("1.3.1"));

        assertNull(archive.getRevisionVersion("1.2.1.2"));
        assertNull(archive.getRevisionVersion("1.2.2"));

        Node[] log = archive.changeLog(new Version("1.2.1"));
        assertNotNull("log is null", log);
        assertEquals(    3, log.length);
        assertEquals("1.1",     log[0].version.toString());
        assertEquals("1.2",     log[1].version.toString());
        assertEquals("1.2.1.1", log[2].version.toString());

        log = archive.changeLog(new Version("1.2.1"), new Version("1.2"));
        assertNotNull("log is null", log);
        assertEquals(    2, log.length);
        assertEquals("1.2",     log[0].version.toString());
        assertEquals("1.2.1.1", log[1].version.toString());
    }

    public void testBranch()
            throws DiffException,
            RCSException
    {
        testAdd1_3();
        archive.setBranch("1.2.0");
        archive.addRevision(v1_2_1_1, "Added 2.1, changed 4");
        String file = archive.toString();
        Object[] rev = archive.getRevision("1.2.1.1");
        assertTrue(Diff.compare(v1_2_1_1, rev));
        Version v;
        v = archive.addRevision(v1_2_1_2, "1.2.0", "Arbitrary revision number");
        assertEquals("1.2.1.2", v.toString());

        v = archive.addRevision(v1_2_8_2, "1.2.8.2", "Arbitrary revision number");
        assertEquals("1.2.8.2", v.toString());

        try
        {
            v = archive.addRevision(v1_2_8_4, "1.2.8.1", "Added to arbitrary branch");
            fail("could add revision 1.2.8.1 after having added 1.2.8.2");
        }
        catch (InvalidVersionNumberException e)
        {
        }

        v = archive.addRevision(v1_2_8_4, "1.2.8.4", "Added to arbitrary branch");
        assertEquals("1.2.8.4", v.toString());

        v = archive.addRevision(v1_2_8_5, "1.2.0", "Added to arbitrary branch");
        assertEquals("1.2.8.5", v.toString());

        assertEquals(new Version("1.2.8.5"), archive.getRevisionVersion());
        assertEquals(".8", new Version("1.2.8.5"), archive.getRevisionVersion("1.2.8"));
        assertEquals(new Version("1.2.8.5"), archive.getRevisionVersion("1.2."));
        assertEquals(new Version("1.2.8.5"), archive.getRevisionVersion("1.2.0"));

        assertNull(archive.getRevisionVersion("1.2.8.6"));
        assertNull(archive.getRevisionVersion("1.2.3"));
        assertNull(archive.getRevisionVersion("1.2.9"));

        Node[] log = archive.changeLog(new Version("1.2.8"));
        assertNotNull("log is null", log);
        assertEquals(    5, log.length);
        assertEquals("1.1",     log[0].version.toString());
        assertEquals("1.2"    , log[1].version.toString());
        assertEquals("1.2.8.2", log[2].version.toString());
        assertEquals("1.2.8.4", log[3].version.toString());
        assertEquals("1.2.8.5", log[4].version.toString());

        try
        {
            log = archive.changeLog(new Version("1.2.8"), new Version("1.2.1"));
            fail("found change log between 1.2.8.5 and 1.2.1.2");
        }
        catch (NodeNotFoundException e)
        {
        }

    }

    public void testInvalidBranch()
            throws DiffException,
            RCSException
    {
        testAdd1_1();
        try
        {
            archive.setBranch("1.3.1");
            fail("succeeded with invalid branch");
        }
        catch (InvalidVersionNumberException e)
        {
        }
    }

    public void testUnicodeEscapes()
       throws DiffException, RCSException
    {
        Archive archive = new Archive(new String[] { "\\user" }, "original");
        archive.addRevision(new String[] { "user" }, "original");
    }


    String[] sampleFile = {
        "head\t1.3;",
        "access;",
        "symbols;",
        "locks; strict;",
        "comment\t@# @;",
        "",
        "",
        "1.3",
        "date\t99.08.24.16.58.59;\tauthor juanca;\tstate Exp;",
        "branches;",
        "next\t1.2;",
        "",
        "1.2",
        "date\t99.08.24.16.57.54;\tauthor juanca;\tstate Exp;",
        "branches",
        "\t1.2.1.1;",
        "next\t1.1;",
        "",
        "1.1",
        "date\t99.08.24.16.56.51;\tauthor juanca;\tstate Exp;",
        "branches;",
        "next\t;",
        "",
        "1.2.1.1",
        "date\t99.08.24.17.00.30;\tauthor juanca;\tstate Exp;",
        "branches;",
        "next\t;",
        "",
        "",
        "desc",
        "@@",
        "",
        "",
        "1.3",
        "log",
        "@Changed 1",
        "@",
        "text",
        "@[1] one changed",
        "[2] two",
        "[3] three",
        "[3.1]",
        "[4] four",
        "[5] five",
        "@",
        "",
        "",
        "1.2",
        "log",
        "@Added 3.1, deleted 6",
        "@",  // 50
        "text",
        "@d1 1",
        "a1 1",
        "[1] one",
        "@",
        "",
        "",
        "1.2.1.1",
        "log",
        "@Added 2.1, changed 4, added 5.1",  //60
        "@",
        "text",
        "@a2 1",
        "[2.1]",
        "d5 1",
        "a5 1",
        "[4] four changed",
        "a6 1",
        "[5.1]",
        "@", // 70
        "",
        "",
        "1.1",
        "log",
        "@A simple test file",
        "@",
        "text",
        "@d4 1",
        "a6 1",
        "[6] six",
        "@"
    };

}
