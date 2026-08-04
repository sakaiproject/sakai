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
package org.apache.pluto.util.install.file.tomcat6;

import java.io.File;

import org.apache.pluto.util.install.InstallationConfig;
import org.apache.pluto.util.install.file.tomcat5.Tomcat5FileSystemInstaller;

public class Tomcat6FileSystemInstaller extends Tomcat5FileSystemInstaller
{

    public boolean isValidInstallationDirectory( File installDir )
    {
        // Tomcat 6 - by default - does away with the classloader
        // directories <tomcat>/server, <tomcat>/common, and <tomcat>/shared.  
        // Everything by default is installed under <tomcat>/lib.
        File libDir = new File( installDir, "lib" );
        File serverConf = new File( installDir, "conf/server.xml" );
        File catalinaProps = new File( installDir, "conf/catalina.properties" );
        
        return libDir.exists() && serverConf.exists() && catalinaProps.exists();
    }
    
    protected File getEndorsedDir( InstallationConfig config )
    {
        // Tomcat 6 uses <tomcat>/endorsed
        return new File( config.getInstallationDirectory(), "endorsed" );
    }

    protected File getSharedDir( InstallationConfig config )
    {
        // Tomcat 6, by default, uses <tomcat>/lib
        return new File( config.getInstallationDirectory(), "lib" );
    }

}
