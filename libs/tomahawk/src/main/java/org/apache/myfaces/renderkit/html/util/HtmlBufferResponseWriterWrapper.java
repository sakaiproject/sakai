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
package org.apache.myfaces.renderkit.html.util;

import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlResponseWriterImpl;

import jakarta.faces.context.ResponseWriter;
import java.io.*;

/**A buffer for content which should not directly be rendered to the page.
 *
 * @author Sylvain Vieujot (latest modification by $Author: grantsmith $)
 * @version $Revision: 169649 $ $Date: 2005-05-11 17:47:12 +0200 (Wed, 11 May 2005) $
 */
public class HtmlBufferResponseWriterWrapper extends HtmlResponseWriterImpl {

    /**
     * Buffer writer to write content to and buffer it.
     *
     * Moved from OutputStream to Writer to account for issue
     * TOMAHAWK-648.
     */
    private StringWriter bufferWriter;
    /**
     * Writer to wrap buffer-writer.
     */
    private PrintWriter wrapperWriter;
    /**
     * Original response writer.
     */
    private ResponseWriter initialWriter;


    /**Get the writer that should have originally been written to.
     *
     * @return The original writer.
     */
    public ResponseWriter getInitialWriter()
    {
        return initialWriter;
    }

    /**Create an instance of the HtmlBufferResponseWriterWrapper
     *
     * @param initialWriter The writer the content should have originally gone to, this will only be used to copy settings.
     * @return A properly initialized writer which stores the output in a buffer; writer is wrapped.
     */
    static public HtmlBufferResponseWriterWrapper getInstance(ResponseWriter initialWriter)
    {
        StringWriter bufferWriter = new StringWriter();
        PrintWriter wrapperWriter = new PrintWriter(bufferWriter, true);

        return new HtmlBufferResponseWriterWrapper(initialWriter, bufferWriter, wrapperWriter);

    }

    /**Constructor for the HtmlBufferResponseWriterWrapper.
     *
     * @param initialWriter The writer the content should have originally gone to, this will only be used to copy settings.
     * @param bufferWriter A buffer to store content to.
     * @param wrapperWriter A wrapper around the buffer.
     */
    private HtmlBufferResponseWriterWrapper(ResponseWriter initialWriter, StringWriter bufferWriter, PrintWriter wrapperWriter)
    {
        super(wrapperWriter, (initialWriter == null) ? null : initialWriter.getContentType(),
                (initialWriter == null) ? null : initialWriter.getCharacterEncoding());

        this.bufferWriter = bufferWriter;
        this.wrapperWriter = wrapperWriter;
        this.initialWriter = initialWriter;
    }


    /**Get the content of the buffer.
     *
     * @return The content of the buffered and wrapped writer.
     */
    public String toString()
    {
        wrapperWriter.flush();
        wrapperWriter.close();
        return bufferWriter.toString();
    }
}
