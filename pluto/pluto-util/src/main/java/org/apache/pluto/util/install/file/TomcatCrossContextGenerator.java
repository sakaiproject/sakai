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
package org.apache.pluto.util.install.file;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TODO JavaDoc
 *
 * @version 1.0
 * @since Dec 11, 2005
 */
public class TomcatCrossContextGenerator {
	private static final String PLUTO_TEMP_DIR = "PlutoDomain";
	private static Log LOG = LogFactory.getLog(TomcatCrossContextGenerator.class);

    public static void main(String[] args) throws IOException {

        File tomcatHome = new File(args[0]);
        File webapps = new File(tomcatHome, "webapps");
        File conf = new File(tomcatHome, "conf/Catalina/localhost");

        File[] files = webapps.listFiles(new PortletFileNameFilter());
        for(int i=0; i < files.length; i++) {
            String fileName = files[i].getName();
            String contextName = fileName.substring(0, fileName.indexOf(".war"));

            createContextFile(conf, fileName, contextName);
        }
    }

    /**
     * Creates a tomcat-specific context deployment descriptor
     * and deploys it.
     *
     * @param confDir Tomcat conf directory
     * @param fileName File name of the war
     * @param contextName The root name of the context file
     * @throws IOException If there is a problem
     */
	public static void createContextFile(File confDir, String fileName, String contextName) throws IOException {
		PrintWriter out = null;
		try {
			StringBuffer contents = new StringBuffer();
			contents.append("<Context ")
			        .append("path=\"").append(contextName).append("\" ")
			        .append("docBase=\"../").append(PLUTO_TEMP_DIR).append("/").append(fileName).append("\" ")
			        .append("crossContext=\"true\">").append("</Context>");
			File confFile = new File(confDir, contextName+".xml");
			if (LOG.isInfoEnabled()) {
				LOG.info("Writing file: "+ confFile.getAbsolutePath());
			}
			out = new PrintWriter(new FileWriter(confFile));
			out.println(contents.toString());
		} finally {
			if (out != null) {
				out.flush();
				out.close();
			}
		}
	}

    public static class PortletFileNameFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            if(name.startsWith("portlet") && name.endsWith(".war")) {
                return true;
            }
            return false;
        }
    }


}
