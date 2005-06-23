/**********************************************************************************
* $HeadURL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

/**
 * SamigoToolServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package org.sakaiproject.tool.assessment.ws;

public class SamigoToolServiceLocator extends org.apache.axis.client.Service implements org.sakaiproject.tool.assessment.ws.SamigoToolService {

    // Use to get a proxy class for SamigoToolService
    private final java.lang.String SamigoToolService_address = "http://sakai-dev5.stanford.edu:8080/samigo/services/SamigoToolService";

    public java.lang.String getSamigoToolServiceAddress() {
        return SamigoToolService_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String SamigoToolServiceWSDDServiceName = "SamigoToolService";

    public java.lang.String getSamigoToolServiceWSDDServiceName() {
        return SamigoToolServiceWSDDServiceName;
    }

    public void setSamigoToolServiceWSDDServiceName(java.lang.String name) {
        SamigoToolServiceWSDDServiceName = name;
    }

    public org.sakaiproject.tool.assessment.ws.SamigoTool getSamigoToolService() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(SamigoToolService_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getSamigoToolService(endpoint);
    }

    public org.sakaiproject.tool.assessment.ws.SamigoTool getSamigoToolService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            org.sakaiproject.tool.assessment.ws.SamigoToolServiceSoapBindingStub _stub = new org.sakaiproject.tool.assessment.ws.SamigoToolServiceSoapBindingStub(portAddress, this);
            _stub.setPortName(getSamigoToolServiceWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (org.sakaiproject.tool.assessment.ws.SamigoTool.class.isAssignableFrom(serviceEndpointInterface)) {
                org.sakaiproject.tool.assessment.ws.SamigoToolServiceSoapBindingStub _stub = new org.sakaiproject.tool.assessment.ws.SamigoToolServiceSoapBindingStub(new java.net.URL(SamigoToolService_address), this);
                _stub.setPortName(getSamigoToolServiceWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        String inputPortName = portName.getLocalPart();
        if ("SamigoToolService".equals(inputPortName)) {
            return getSamigoToolService();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("urn:samigows", "SamigoToolService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("SamigoToolService"));
        }
        return ports.iterator();
    }

}
