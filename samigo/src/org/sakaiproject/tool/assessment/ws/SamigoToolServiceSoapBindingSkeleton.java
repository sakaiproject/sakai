/**
 * SamigoToolServiceSoapBindingSkeleton.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package org.sakaiproject.tool.assessment.ws;

public class SamigoToolServiceSoapBindingSkeleton implements org.sakaiproject.tool.assessment.ws.SamigoTool, org.apache.axis.wsdl.Skeleton {
    private org.sakaiproject.tool.assessment.ws.SamigoTool impl;
    private static java.util.Map _myOperations = new java.util.Hashtable();
    private static java.util.Collection _myOperationsList = new java.util.ArrayList();

    /**
    * Returns List of OperationDesc objects with this name
    */
    public static java.util.List getOperationDescByName(java.lang.String methodName) {
        return (java.util.List)_myOperations.get(methodName);
    }

    /**
    * Returns Collection of OperationDescs
    */
    public static java.util.Collection getOperationDescs() {
        return _myOperationsList;
    }

    static {
        org.apache.axis.description.OperationDesc _oper;
        org.apache.axis.description.FaultDesc _fault;
        org.apache.axis.description.ParameterDesc [] _params;
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("search", _params, new javax.xml.namespace.QName("", "searchReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("urn:samigows", "ArrayOfItem"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:samigows", "search"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("search") == null) {
            _myOperations.put("search", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("search")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:samigows", "ArrayOf_xsd_string"), java.lang.String[].class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("download", _params, new javax.xml.namespace.QName("", "downloadReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:samigows", "download"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("download") == null) {
            _myOperations.put("download", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("download")).add(_oper);
    }

    public SamigoToolServiceSoapBindingSkeleton() {
        this.impl = new org.sakaiproject.tool.assessment.ws.SamigoToolServiceSoapBindingImpl();
    }

    public SamigoToolServiceSoapBindingSkeleton(org.sakaiproject.tool.assessment.ws.SamigoTool impl) {
        this.impl = impl;
    }
    public org.sakaiproject.tool.assessment.ws.Item[] search(java.lang.String in0) throws java.rmi.RemoteException
    {
        org.sakaiproject.tool.assessment.ws.Item[] ret = impl.search(in0);
        return ret;
    }

    public java.lang.String download(java.lang.String[] in0, java.lang.String in1) throws java.rmi.RemoteException
    {
        java.lang.String ret = impl.download(in0, in1);
        return ret;
    }

}
