/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the"License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/


/**
 * SamigoToolService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package org.sakaiproject.tool.assessment.ws;

public interface SamigoToolService extends javax.xml.rpc.Service {
    public java.lang.String getSamigoToolServiceAddress();

    public org.sakaiproject.tool.assessment.ws.SamigoTool getSamigoToolService() throws javax.xml.rpc.ServiceException;

    public org.sakaiproject.tool.assessment.ws.SamigoTool getSamigoToolService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
