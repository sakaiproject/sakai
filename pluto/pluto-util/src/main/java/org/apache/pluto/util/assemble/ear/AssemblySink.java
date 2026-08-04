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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * Used as a temporary container for assembled bytes, and for tracking 
 * metadata specific to <code>JarEntry</code> objects, especially the
 * size of the <code>JarEntry</code> and its checksum.
 */
abstract class AssemblySink extends FilterOutputStream {
    
    /**
     * The default buffer length used when reading and
     * writing to the sink.  By default the value is
     * 4096 bytes.
     */
    protected static int DEFAULT_BUFLEN = 4 * 1024; // 4kb
    
    /**
     * The CRC-32 calculator.
     */
    protected final Checksum CRC = new CRC32();
    
    /**
     * The number of bytes written to the sink.
     */
    protected long count = 0;
    
    /**
     * Constructs a sink with <i>null</i> as an
     * underlying output stream.
     */
    AssemblySink( ) {
        super(null);
    }
    
    /**
     * Constructs a sink with <i>out</i> as the
     * underlying output stream.  Method calls
     * on the sink are delegated to the underlying
     * output stream.
     * 
     * @param out the underlying output stream.
     */
    AssemblySink( OutputStream out ) {
        super(out);
    }
    
    /**
     * Obtain the number of bytes written to the sink.
     * 
     * @return the number of bytes written to the sink
     */
    long getByteCount() {
        return count;
    }

    /**
     * Obtain the checksum of the bytes written to 
     * the sink to this point.
     * 
     * @return the CRC-32 checksum of the bytes in the sink 
     */
    long getCrc() {
        return CRC.getValue();
    }

    /**
     * Write out the bytes in the sink to the supplied
     * output stream, using the default buffer length.
     * 
     * @param out the OutputStream the sink should be copied to.
     * @throws IOException
     */
    void writeTo( OutputStream out ) throws IOException {
        writeTo( out, DEFAULT_BUFLEN );     
    }
    
    /**
     * Write out the bytes in the sink to the supplied
     * output stream, using the supplied buffer length.
     * 
     * @param out the OutputStream the sink should be copied to.
     * @param buflen the buffer length used when copying bytes
     * @throws IOException
     */
    abstract void writeTo( OutputStream out, int buflen ) throws IOException;

    public synchronized void write(byte[] b) throws IOException {
        try {
            out.write(b);
            count++;
            CRC.update( b, 0, b.length );
        } catch (IOException e) {
            count = 0;
            CRC.reset();
            throw e;
        }
    }
    
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        try {
            out.write(b, off, len);
            count += len;
            CRC.update(b, off, len);
        } catch (IOException e) {
            count = 0;
            CRC.reset();
            throw e;
        }
    }

    public synchronized void write(int b) throws IOException {
        try {
            out.write(b);
            count++;
            CRC.update(b);
        } catch (IOException e) {
            count = 0;
            CRC.reset();
            throw e;
        }
    }
    
    /**
     * Closes the underlying output stream, resets the
     * checksum calculator, and clears the byte count.
     */
    public void close() throws IOException {
        try {
            out.close();
        } finally {
            CRC.reset();
            count = 0;
        }
    }

}
