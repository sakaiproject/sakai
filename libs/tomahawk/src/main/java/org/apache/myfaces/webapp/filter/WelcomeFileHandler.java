/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.webapp.filter;

import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * DOCUMENT ME!
 *
 * @author Robert J. Lebowitz (latest modification by $Author: grantsmith $)
 * @version $Revision: 472644 $ $Date: 2006-11-08 16:04:52 -0500 (Wed, 08 Nov 2006) $
 */
public class WelcomeFileHandler
extends DefaultHandler
{
    //~ Instance fields --------------------------------------------------------

    private StringBuffer sb           = new StringBuffer();
    private Vector       welcomeFiles;
    private String[]     files;
    private boolean      fileFlag = false;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new WelcomeFileHandler object.
     */
    public WelcomeFileHandler()
    {
        super();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Accessor method used to get the array of welcome files.
     * @return The string array of welcome files.
     *
     */
    public String[] getWelcomeFiles()
    {
        return files;
    }

    /**
     *
     * Method used to examine, modify or extract text in body of an element
     * @param ch character array containing the tag's body information.
     * @param start starting index of the body text in the character array.
     * @param length length of the text found in the body.
     * @throws SAXException
     *
     */
    public void characters(char[] ch, int start, int length)
    throws SAXException
    {
        if (fileFlag)
        {
            sb.append(ch, start, length);
        }

        super.characters(ch, start, length);
    }

    /**
     * Method called with each end element in an XML Document
     * @param ns The namespace associated with this element
     * @param local The local name of this element
     * @param qName The qualified name of this element
     * @throws SAXException
     *
     */
    public void endElement(String ns, String local, String qName)
    throws SAXException
    {
        if (qName.equals("welcome-file-list"))
        {
            files = new String[welcomeFiles.size()];
            welcomeFiles.toArray(files);
            welcomeFiles = null;
        }

        if (qName.equals("welcome-file"))
        {
            welcomeFiles.add(sb.toString());
            sb.setLength(0);
            fileFlag = false;
        }

        super.endElement(ns, local, qName);
    }

    /**
     *
     *  Method called with each start element in an XML document
     * @param ns The namespace associated with this element
     * @param local The local name of this element
     * @param qName The qualified name of this element
     * @param atts Attributes associated with this element
     * @throws SAXException
     *
     */
    public void startElement(
        String ns, String local, String qName, Attributes atts)
    throws SAXException
    {
        if (qName.equals("welcome-file-list"))
        {
            welcomeFiles = new Vector();
        }

        if (qName.equals("welcome-file"))
        {
            sb.setLength(0);
            fileFlag = true;
        }

        super.startElement(ns, local, qName, atts);
    }
}
