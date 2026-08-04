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
package org.apache.pluto.util.cli;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.HelpFormatter;
import org.apache.pluto.util.assemble.Assembler;
import org.apache.pluto.util.assemble.AssemblerFactory;
import org.apache.pluto.util.assemble.AssemblerConfig;
import org.apache.pluto.util.UtilityException;

/**
 * Command Line Interface to the Pluto Assembler.
 *
 * @version 1.0
 * @since Oct 15, 2004
 */
public class AssemblerCLI {

    private final Options options;
    private final String[] args;

    public AssemblerCLI(String[] args) {
        this.args = args;
        options = new Options();
        Option destination =
            new Option("d" , "destination", true,
                       "specify where the resulting webapp should be written ");
        destination.setArgName("file");

        Option debug =
            new Option("debug", false, "print debug information.");
        options.addOption(destination);
        options.addOption(debug);
    }

    public void run() throws ParseException, IOException, UtilityException {
        CommandLineParser parser = new PosixParser();
        CommandLine line = parser.parse(options, args);

        String[] args = line.getArgs();
        if(args.length != 1) {
            abort();
            return;
        }

        String dest = line.getOptionValue("file");
        if(dest == null) {
            dest = args[0];
        }

        File source = new File(args[0]);
        File result   = new File(dest);
        result.getParentFile().mkdirs();

        if(!source.exists()) {
            System.out.println("File does not exist: "+source.getCanonicalPath());
        }


        System.out.println("-----------------------------------------------");
        System.out.println("Assembling: "+source.getCanonicalPath());
        System.out.println("        to: "+result.getCanonicalPath());

        File webXml = new File(args[0], Assembler.SERVLET_XML);
        File portletXml = new File(args[0], Assembler.PORTLET_XML);

        AssemblerConfig config = new AssemblerConfig();
        config.setWebappDescriptor(webXml);
        config.setPortletDescriptor(portletXml);
        config.setDestination(webXml);

        Assembler assembler = AssemblerFactory.getFactory()
            .createAssembler(config);

        config.setPortletDescriptor(portletXml);
        config.setWebappDescriptor(webXml);

        assembler.assemble(config);

        System.out.println("Complete!");
    }

    public void abort() {
        HelpFormatter help = new HelpFormatter();
        help.defaultArgName = "webapp";
        help.defaultWidth = 60;
        help.printHelp("assemble", options);
    }

    public static void main(String[] args)
    throws ParseException, IOException, UtilityException {
        new AssemblerCLI(args).run();
    }
}

