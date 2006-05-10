/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
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
 * SamigoToolServiceSoapBindingImpl.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package org.sakaiproject.tool.assessment.ws;


public class SamigoToolServiceSoapBindingImpl implements org.sakaiproject.tool.assessment.ws.SamigoTool{
    public org.sakaiproject.tool.assessment.ws.Item[] search(java.lang.String in0) throws java.rmi.RemoteException {
        SamigoToolWebService samigows= new SamigoToolWebService();
        return samigows.getItemObjArrayByKeyword(in0);
    }

    public java.lang.String download(java.lang.String[] in0, java.lang.String qtiVersion) throws java.rmi.RemoteException {
        SamigoToolWebService samigows= new SamigoToolWebService();
        return samigows.download(in0, qtiVersion);
    }

}
