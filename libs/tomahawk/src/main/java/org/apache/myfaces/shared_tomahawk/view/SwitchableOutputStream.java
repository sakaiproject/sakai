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
package org.apache.myfaces.shared_tomahawk.view;

import java.io.IOException;
import java.io.OutputStream;

import jakarta.servlet.ServletOutputStream;

/**
 * Delegates the standard OutputStream-methods (close, flush, write)
 * to the given ServletOutputStream, if the ResponseSwitch is enabled
 *  
 * @author Werner Punz (latest modification by $Author: lu4242 $)
 * @author Jakob Korherr 
 * @version $Revision: 1151676 $ $Date: 2011-07-27 19:01:24 -0500 (Wed, 27 Jul 2011) $
 */
class SwitchableOutputStream extends ServletOutputStream
{

    OutputStream _delegate = null;
    ResponseSwitch _responseSwitch = null;

    public SwitchableOutputStream(ServletOutputStream delegate, ResponseSwitch responseSwitch)
    {
        _delegate = delegate;
        _responseSwitch = responseSwitch;
    }

    @Override
    public void close() throws IOException
    {
        if (_responseSwitch.isEnabled())
        {
            _delegate.close();
        }
    }

    @Override
    public void flush() throws IOException
    {
        if (_responseSwitch.isEnabled())
        {
            _delegate.flush();
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
        if (_responseSwitch.isEnabled())
        {
            _delegate.write(b, off, len);
        }
    }

    @Override
    public void write(byte[] b) throws IOException
    {
        if (_responseSwitch.isEnabled())
        {
            _delegate.write(b);
        }
    }

    @Override
    public void write(int b) throws IOException
    {
        if (_responseSwitch.isEnabled())
        {
            _delegate.write(b);
        }
    }
    

        // Servlet 3.1 async I/O; Tomahawk predates it and writes synchronously.
        public boolean isReady()
        {
            return true;
        }

        public void setWriteListener(jakarta.servlet.WriteListener writeListener)
        {
        }
}
