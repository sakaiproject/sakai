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

import java.util.Date;

import junit.framework.TestCase;

/**
 * Basic test for the formatter.
 *
 * @author <a href="mailto:sbailliez@apache.org">Stephane Bailliez</a>
 */
public class KeywordsFormatTest extends TestCase
{
    private static final KeywordsFormat FORMATTER = new KeywordsFormat();
    private static final String RCS_KEYWORDS =
            "$Id$\n" +
            "$Header: /home/projects/jrcs/scm/jrcs/src/test/org/apache/commons/jrcs/rcs/KeywordsFormatTest.java,v 1.3 2003/10/13 07:58:44 rdonkin Exp $\n" +
            "$Source: /home/projects/jrcs/scm/jrcs/src/test/org/apache/commons/jrcs/rcs/KeywordsFormatTest.java,v $\n" +
            "$RCSfile: KeywordsFormatTest.java,v $\n" +
            "$Revision$\n" +
            "$Date$\n" +
            "$Author$\n" +
            "$State: Exp $\n" +
            "$Locker:  $\n";
    // don't get bitten by rcs keywords it should not be interpreted
    private static final String RCS_CLEAN_KEYWORDS =
            "$" + "Id$\n" +
            "$" + "Header$\n" +
            "$" + "Source$\n" +
            "$" + "RCSfile$\n" +
            "$" + "Revision$\n" +
            "$" + "Date$\n" +
            "$" + "Author$\n" +
            "$" + "State$\n" +
            "$" + "Locker$\n";

    private static final Object[] REVISION_INFO = new Object[]{
            "a/b/c/d/File.ext",
            "File.ext",
            "1.1",
            new Date(),
            "theauthor",
            "thestate",
            "thelocker"
        };

    private static final String RCS_NOW =
        FORMATTER.Id_FORMAT.format(REVISION_INFO) + "\n" +
        FORMATTER.Header_FORMAT.format(REVISION_INFO) + "\n" +
        FORMATTER.Source_FORMAT.format(REVISION_INFO) + "\n" +
        FORMATTER.RCSFile_FORMAT.format(REVISION_INFO) + "\n" +
        FORMATTER.Revision_FORMAT.format(REVISION_INFO) + "\n" +
        FORMATTER.Date_FORMAT.format(REVISION_INFO) + "\n" +
        FORMATTER.Author_FORMAT.format(REVISION_INFO) + "\n" +
        FORMATTER.State_FORMAT.format(REVISION_INFO) + "\n" +
        FORMATTER.Locker_FORMAT.format(REVISION_INFO) + "\n";

    public KeywordsFormatTest(String s)
    {
        super(s);
    }

    public void testReset() throws Exception
    {
        String result = FORMATTER.reset(RCS_KEYWORDS);
        assertEquals(RCS_CLEAN_KEYWORDS, result);
    }

    public void testUpdate() throws Exception
    {
        String result = FORMATTER.update(RCS_KEYWORDS, REVISION_INFO);
        assertEquals(RCS_NOW, result);
    }
}
