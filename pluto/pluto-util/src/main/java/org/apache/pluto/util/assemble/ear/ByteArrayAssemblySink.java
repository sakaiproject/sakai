/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pluto.util.assemble.ear;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Stores assembled bytes in an underlying <code>ByteArrayOutputStream</code>
 */
class ByteArrayAssemblySink extends AssemblySink {
    
    private static final Log LOG = LogFactory.getLog( ByteArrayAssemblySink.class );
    
    ByteArrayAssemblySink(ByteArrayOutputStream out) {
        super(out);
    }
    
    ByteArrayAssemblySink(ByteArrayOutputStream out, int buflen) {
        this.out = new ByteArrayOutputStream( buflen );
    }
    
    public void writeTo(OutputStream out, int buflen) throws IOException {
        byte[] buf = new byte[buflen];
        int read = 0;
        InputStream sinkReader = new BufferedInputStream( 
                new ByteArrayInputStream( ((ByteArrayOutputStream)this.out).toByteArray()), buflen);
        while ( ( read = sinkReader.read(buf) ) != -1 ) {
            out.write(buf, 0, read);
        }
    }
    
}
