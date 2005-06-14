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
