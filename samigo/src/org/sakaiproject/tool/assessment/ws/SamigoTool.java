/**
 * SamigoTool.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package org.sakaiproject.tool.assessment.ws;

public interface SamigoTool extends java.rmi.Remote {
    public org.sakaiproject.tool.assessment.ws.Item[] search(java.lang.String in0) throws java.rmi.RemoteException;
    public java.lang.String download(java.lang.String[] in0, java.lang.String in1) throws java.rmi.RemoteException;
}
