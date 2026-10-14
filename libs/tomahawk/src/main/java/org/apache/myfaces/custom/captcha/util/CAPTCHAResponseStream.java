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
package org.apache.myfaces.custom.captcha.util;

import java.io.IOException;
import java.io.OutputStream;
import jakarta.faces.context.ResponseStream;

/**
 * This class is responsible for wrapping the CAPTCHA Image
 * response stream.
 * 
 * @since 1.1.7
 */
public final class CAPTCHAResponseStream extends ResponseStream
{
    private final OutputStream _out;

    public CAPTCHAResponseStream(OutputStream out)
    {
        _out = out;
    }

    public void close() throws IOException
    {
        _out.flush();
        _out.close();
    }

    public void flush() throws IOException
    {
        _out.flush();
    }

    public void write(byte[] b, int off, int len) throws IOException
    {
        _out.write(b, off, len);
    }

    public void write(byte[] b) throws IOException
    {
        _out.write(b);
    }

    public void write(int b) throws IOException
    {
        _out.write(b);
    }
}
