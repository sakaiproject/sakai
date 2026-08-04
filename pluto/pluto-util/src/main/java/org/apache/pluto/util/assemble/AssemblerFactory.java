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
package org.apache.pluto.util.assemble;

import java.io.File;

import org.apache.pluto.util.assemble.ear.EarAssembler;
import org.apache.pluto.util.assemble.file.FileAssembler;
import org.apache.pluto.util.assemble.war.WarAssembler;

/**
 * The pluto assembler factory that creates an assembler.
 * @version 1.0
 * @since Nov 8, 2004
 */
public class AssemblerFactory {

	/** The singleton factory instance. */
    private static final AssemblerFactory FACTORY = new AssemblerFactory();

    /**
     * Private constructor that prevents external instantiation.
     */
    private AssemblerFactory() {
    	// Do nothing.
    }

    /**
     * Returns the singleton factory instance.
     * @return the singleton factory instance.
     */
    public static AssemblerFactory getFactory() {
        return FACTORY;
    }


    // Public Methods ----------------------------------------------------------

    /**
     * Creates an assembler to assemble a portlet app WAR file to a web app WAR
     * file deployable to pluto.
     * @param config  the assembler configuration.
     * @return an assembler instance.
     */
    public Assembler createAssembler(AssemblerConfig config) {
        File source = config.getSource();
        
        if ( source == null ) {
            return new FileAssembler();
        }

        if ( source.getName().toLowerCase().endsWith( ".war" ) ) {
            return new WarAssembler();
        }
        
        if ( source.getName().toLowerCase().endsWith( ".ear" ) ) {
            return new EarAssembler();
        }
        
        return new FileAssembler();
    }

}
