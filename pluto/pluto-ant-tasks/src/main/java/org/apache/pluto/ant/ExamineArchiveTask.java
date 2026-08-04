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
package org.apache.pluto.ant;

import java.io.File;
import java.io.FileInputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class ExamineArchiveTask extends Task {
    
    private File archive = null;
    
    public void execute() throws BuildException {
        try {
        
            JarInputStream jarIn = new JarInputStream( new FileInputStream( archive ) );
            JarEntry entry;
            while ( ( entry = jarIn.getNextJarEntry() ) != null ) {
                String name = entry.getName();
                long crc = entry.getCrc();
                long size = entry.getSize();
                long compressedSize = entry.getCompressedSize();
                int compressMethod = entry.getMethod();
                long timeStamp = entry.getTime();
                int hashCode = entry.hashCode();
                
                StringBuffer out = new StringBuffer();
                
                out.append( "Name: " + name + "\n" );
                out.append( "  Size: "                  + Long.toHexString( size ) + " " );
                out.append( "  Compressed Size: "       + Long.toHexString( compressedSize ) + " " );
                out.append( "  Compression Method: "    + compressMethod + " " );
                out.append( "  Timestamp: "             + Long.toHexString( timeStamp ) + " " );
                out.append( "  HashCode: "              + Integer.toHexString( hashCode ) + " " );
                out.append( "  CRC: "                   + Long.toHexString( crc ) + " " );
                
                System.out.println( out.toString() );
                
            }
        
        } catch ( Exception e ) {
            throw new BuildException( e.getMessage(), e );
        }
        
    }
    
    public void setArchive( File archive ) {
        this.archive = archive;
    }
    
    public File getArchive( ) {
        return this.archive;
    }

}
