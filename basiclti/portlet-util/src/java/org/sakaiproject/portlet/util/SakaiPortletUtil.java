/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright 2005, 2006 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.portlet.util;

import java.util.List;
import java.util.Vector;

import java.net.URLEncoder;

import javax.xml.namespace.QName;

import org.sakaiproject.portlet.util.XMLMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.portlet.PortletRequest;

public class SakaiPortletUtil
{

    private static boolean debugPrintFlag = true;

    public static String getTag(Element theElement, String elementName) {
        try {
            Node node = theElement.getElementsByTagName(elementName).item(0);

            if (node.getNodeType() == node.TEXT_NODE) {
                return node.getNodeValue();
            } else if (node.getNodeType() == node.ELEMENT_NODE) {
                return node.getFirstChild().getNodeValue();
            }
            return null;
        } catch (Exception e) {
            return null;
        }

    }

        /**
         * Get an InputStream for a particular file name - first check the sakai.home area and then 
         * revert to the classpath.
         *
         * This is a utility method used several places.
         */
        public static java.io.InputStream getConfigStream(String fileName, Class curClass)
        {
                // Within Sakai default path is usually tomcat/sakai/file.properties
                // Sakai deployers can move this.

                // When we area not in Sakai's JVM, this may be several places
                // depending on the JVM/OS, etc
                //  - the directory where we started Tomcat
                //  - the user's hojme directory
                //  - the root directory of the system
                // Also the user can start the portal JVN with -Dsakai.home= to force this path

                String sakaiHome = System.getProperty("sakai.home");
                String filePath = sakaiHome + fileName;
                // System.out.println("filePath="+filePath);

                try
                {
                        java.io.File f = new java.io.File(filePath);
                        if (f.exists())
                        {
                                return new java.io.FileInputStream(f);
                        }
                }
                catch (Throwable t)
                {
                        // Not found in the sakai.home area
                }

                // See if we can find this property file relative to a  class loader
                if ( curClass == null ) return null;

                java.io.InputStream istream = null;

                // TODO: Figure out *where* the file really needs to go to 
                // trigger this first section of code. It would be cool
                // to have this be shared/lib or somewhere - I just cannot
                // figure this out at this point - Chuck

                // Load from the class loader
                istream = curClass.getClassLoader().getResourceAsStream(fileName);
                if ( istream != null ) return istream;

                // Load from the webapp class relative
                // tomcat/webapps/sakai-webapp/WEB-INF/classes/org/sakaiproject/this/class/file.properties
                istream = curClass.getResourceAsStream(fileName);
                if ( istream != null ) return istream;

                // Loading from the webapp class at the root
                // tomcat/webapps/sakai-webapp/WEB-INF/classes/file.properties
                istream = curClass.getResourceAsStream("/"+fileName);
                return istream;
        }

    public static  boolean isSakaiPortal(PortletRequest request)
    {
        String portalInfo = request.getPortalContext().getPortalInfo();
        return portalInfo.toLowerCase().startsWith("sakai-charon") ;
    }

    private static void debugPrint(String str)
    {
        if ( debugPrintFlag )
        {
                System.out.println(str);
        } else {
                // Should do Log.debug here
        }
    }
}
