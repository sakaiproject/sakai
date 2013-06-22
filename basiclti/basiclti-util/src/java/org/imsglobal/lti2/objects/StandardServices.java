
package org.imsglobal.lti2.objects;

public class StandardServices {

    public static Service_offered LTI2Registration(String endpoint) {
        return new Service_offered(endpoint, "ltitcp:ToolProxy.collection", "RestService",
            "application/vnd.ims.lti.v2.ToolProxy+json", "POST" ) ;
    }

    public static Service_offered LTI1Outcomes(String endpoint) {
        return new Service_offered(endpoint, "ltitcp:LTI1_1.outcomes", "XMLService", 
            "application/xml", "POST");
    }

}
