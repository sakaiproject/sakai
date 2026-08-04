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
package org.apache.pluto.descriptors.services.castor;

import junit.framework.TestCase;
import junit.framework.Assert;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;

import org.apache.pluto.descriptors.servlet.WebAppDD;

/**
 *
 */
public class WebAppDescriptorServiceImplTest extends TestCase {

    private StringBuffer xmlBegin = new StringBuffer();
    private StringBuffer xmlEnd = new StringBuffer();

    private StringBuffer icon = new StringBuffer();
    private StringBuffer misc = new StringBuffer();
    private StringBuffer ctxParams = new StringBuffer();
    private StringBuffer sessionConfig = new StringBuffer();
    private StringBuffer filters = new StringBuffer();
    private StringBuffer listeners = new StringBuffer();
    private StringBuffer servlets = new StringBuffer();
    private StringBuffer filterMappings = new StringBuffer();
    private StringBuffer servletMappings = new StringBuffer();
    private StringBuffer mimeMappings = new StringBuffer();
    private StringBuffer welcomeFiles = new StringBuffer();
    private StringBuffer errorPage = new StringBuffer();
    private StringBuffer taglib = new StringBuffer();
    private StringBuffer resourceRef = new StringBuffer();
    private StringBuffer securityConstraint = new StringBuffer();
    private StringBuffer loginConfig = new StringBuffer();
    private StringBuffer securityRole = new StringBuffer();
    private StringBuffer envEntry = new StringBuffer();
    private StringBuffer ejbRef = new StringBuffer();

    WebAppDescriptorServiceImpl impl = new WebAppDescriptorServiceImpl();

    public void setUp() {
        xmlBegin.append("" +
                "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
                "<web-app xmlns=\"http://java.sun.com/xml/ns/j2ee\"\n" +
                "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "    xsi:schemaLocation=\"http://java.sun.com/xml/ns/j2ee http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd\"\n" +
                "    version=\"2.4\">\n" );
        icon.append("" +
                "  <icon>\n" +
                "      <large-icon>/icon/large.gif</large-icon>\n" +
                "      <small-icon>/icon/small.gif</small-icon>\n" +
                "  </icon>\n" );
        misc.append(""+
                "  <display-name>Test Web XML</display-name>\n" +
                "  <description>This is a test web.xml file.</description>\n" +
                "  <distributable/>\n" );
        ctxParams.append(""+
                "  <context-param>\n" +
                "      <description>Test Parameter 1</description>\n" +
                "      <param-name>TestParam1</param-name>\n" +
                "      <param-value>TestParam1Value</param-value>\n" +
                "  </context-param>\n" +
                "  <context-param>\n" +
                "      <description>Test Parameter 2</description>\n" +
                "      <param-name>TestParam2</param-name>\n" +
                "      <param-value>TestParam2Value</param-value>\n" +
                "  </context-param>\n" );
        filters.append(""+
                "  <filter>\n" +
                "      <icon>\n" +
                "          <large-icon>/icon/large.gif</large-icon>\n" +
                "          <small-icon>/icon/small.gif</small-icon>\n" +
                "      </icon>\n" +
                "      <description>Test Description</description>\n" +
                "      <display-name>TestName</display-name>\n" +
                "      <filter-class>org.apache.pluto.test.TestMe</filter-class>\n" +
                "      <filter-name>test</filter-name>\n" +
                "      <init-param>\n" +
                "          <param-name>Test</param-name>\n" +
                "          <param-value>true</param-value>\n" +
                "      </init-param>\n" +
                "      <init-param>\n" +
                "          <param-name>Test2</param-name>\n" +
                "          <param-value>true</param-value>\n" +
                "      </init-param>\n" +
                "  </filter>\n" );
        listeners.append("" +
                "  <listener>\n" +
                "      <listener-class>org.apache.pluto.test.TestListener</listener-class>\n" +
                "  </listener>\n" +
                "  <listener>\n" +
                "      <listener-class>org.apache.pluto.test.AnotherListener</listener-class>\n" +
                "  </listener>\n" );
        servlets.append(""+
                "  <servlet>\n" +
                "      <servlet-name>default</servlet-name>\n" +
                "      <servlet-class>org.apache.catalina.servlets.DefaultServlet</servlet-class>\n" +
                "      <init-param>\n" +
                "          <param-name>debug</param-name>\n" +
                "          <param-value>0</param-value>\n" +
                "      </init-param>\n" +
                "      <init-param>\n" +
                "          <param-name>listings</param-name>\n" +
                "          <param-value>true</param-value>\n" +
                "      </init-param>\n" +
                "      <load-on-startup>1</load-on-startup>\n" +
                "  </servlet>\n" +
                "  <servlet>\n" +
                "      <servlet-name>jsp</servlet-name>\n" +
                "      <servlet-class>org.apache.jasper.servlet.JspServlet</servlet-class>\n" +
                "      <init-param>\n" +
                "          <param-name>fork</param-name>\n" +
                "          <param-value>false</param-value>\n" +
                "      </init-param>\n" +
                "      <init-param>\n" +
                "          <param-name>xpoweredBy</param-name>\n" +
                "          <param-value>false</param-value>\n" +
                "      </init-param>\n" +
                "      <load-on-startup>3</load-on-startup>\n" +
                "      <security-role-ref>\n" +
                "        <role-name>plutoTestRole</role-name>\n" +
                "        <role-link>tomcat</role-link>\n" +
                "        <description>tomcat</description>\n" +
                "      </security-role-ref>\n" +
                "  </servlet>\n" );
        filterMappings.append(""+
                "  <filter-mapping>\n" +
                "      <filter-name>test</filter-name>\n" +
                "      <url-pattern>/test/*</url-pattern>\n" +
                "      <url-pattern>/some/*</url-pattern>\n" +
                "      <dispatcher>INCLUDE</dispatcher>\n" +
                "      <dispatcher>FORWARD</dispatcher>\n" +
                "  </filter-mapping>\n" +
                "  <filter-mapping>\n" +
                "    <filter-name>test</filter-name>\n" +
                "    <servlet-name>jsp</servlet-name>\n" +
                "    <dispatcher>INCLUDE</dispatcher>\n" +
                "  </filter-mapping>\n" );
        servletMappings.append(""+
                "    <servlet-mapping>\n" +
                "        <servlet-name>default</servlet-name>\n" +
                "        <url-pattern>/</url-pattern>\n" +
                "    </servlet-mapping>\n" +
                "    <servlet-mapping>\n" +
                "        <servlet-name>jsp</servlet-name>\n" +
                "        <url-pattern>*.jsp</url-pattern>\n" +
                "    </servlet-mapping>\n" +
                "    <servlet-mapping>\n" +
                "        <servlet-name>jsp</servlet-name>\n" +
                "        <url-pattern>*.jspx</url-pattern>\n" +
                "    </servlet-mapping>\n" );
        sessionConfig.append(""+
                "    <session-config>\n" +
                "        <session-timeout>30</session-timeout>\n" +
                "    </session-config>\n" );
        mimeMappings.append("" +
                "    <mime-mapping>\n" +
                "        <extension>abs</extension>\n" +
                "        <mime-type>audio/x-mpeg</mime-type>\n" +
                "    </mime-mapping>\n" +
                "    <mime-mapping>\n" +
                "        <extension>ai</extension>\n" +
                "        <mime-type>application/postscript</mime-type>\n" +
                "    </mime-mapping>\n" );
        welcomeFiles.append(""+
                "    <welcome-file-list>\n" +
                "        <welcome-file>index.html</welcome-file>\n" +
                "        <welcome-file>index.htm</welcome-file>\n" +
                "        <welcome-file>index.jsp</welcome-file>\n" +
                "    </welcome-file-list>\n" );
        this.errorPage.append(""+
                "    <error-page>\n" +
                "        <error-code>400</error-code>\n" +
                "        <location>/400.html</location>\n" +
                "    </error-page>\n" +
                "    <error-page>\n" +
                "        <exception-type>java.io.IOException</exception-type>\n" +
                "        <location>/error/io.html</location>\n" +
                "    </error-page>\n" );
        this.taglib.append(""+
                "    <taglib>\n" +
                "        <taglib-location>/WEB-INF/test.tld</taglib-location>\n" +
                "        <taglib-uri>test</taglib-uri>\n" +
                "    </taglib>\n" );
        this.resourceRef.append(""+
                "    <resource-ref>\n" +
                "        <description>Test Resource</description>\n" +
                "        <res-auth>CONTIANER</res-auth>\n" +
                "        <res-ref-name>RefName</res-ref-name>\n" +
                "        <res-sharing-scope>Scope</res-sharing-scope>\n" +
                "        <res-type>java.lang.String</res-type>\n" +
                "    </resource-ref>\n" +
                "    <resource-ref>\n" +
                "        <description>Test Resource</description>\n" +
                "        <res-auth>CONTIANER</res-auth>\n" +
                "        <res-ref-name>RefName2</res-ref-name>\n" +
                "        <res-sharing-scope>Scope</res-sharing-scope>\n" +
                "        <res-type>java.lang.String</res-type>\n" +
                "    </resource-ref>\n" );
        this.securityConstraint.append(""+
                "    <security-constraint>\n" +
                "        <auth-constraint>\n" +
                "            <description>Test Role</description>\n" +
                "            <role-name>TestRole</role-name>\n" +
                "        </auth-constraint>\n" +
                "        <display-name>Test Constraint</display-name>\n" +
                "        <user-data-constraint>\n" +
                "            <description>CONFIDENTIAL</description>\n" +
                "            <transport-guarantee>CONFIDENTIAL</transport-guarantee>\n" +
                "        </user-data-constraint>\n" +
                "        <web-resource-collection>\n" +
                "            <description>TestCollection</description>\n" +
                "            <http-method>GET</http-method>\n" +
                "            <http-method>POST</http-method>\n" +
                "            <url-pattern>/test/*</url-pattern>\n" +
                "            <url-pattern>/secure/*</url-pattern>\n" +
                "            <web-resource-name>secured</web-resource-name>\n" +
                "        </web-resource-collection>\n" +
                "    </security-constraint>\n" );
        this.loginConfig.append(""+
                "    <login-config>\n" +
                "        <auth-method>FORM</auth-method>\n" +
                "        <form-login-config>\n" +
                "            <form-login-page>/login.jsp</form-login-page>\n" +
                "            <form-error-page>/login.jsp?error=true</form-error-page>\n" +
                "        </form-login-config>\n" +
                "        <realm-name>BASIC</realm-name>\n" +
                "    </login-config>\n" );
         this.securityRole.append(""+
                "    <security-role>\n" +
                "        <description>A Test Role</description>\n" +
                "        <role-name>TestRole</role-name>\n" +
                "    </security-role>\n" +
                "    <security-role>\n" +
                "        <description>Another Role</description>\n" +
                "        <role-name>AnotherRole</role-name>\n" +
                "    </security-role>\n" );
        this.envEntry.append(""+
                "    <env-entry>\n" +
                "        <description>Test Environment Entry</description>\n" +
                "        <env-entry-name>Test</env-entry-name>\n" +
                "        <env-entry-type>java.lang.String</env-entry-type>\n" +
                "        <env-entry-value>TestValue</env-entry-value>\n" +
                "    </env-entry>\n" +
                "    <env-entry>\n" +
                "        <description>Another Environment Entry</description>\n" +
                "        <env-entry-name>Another</env-entry-name>\n" +
                "        <env-entry-type>java.lang.String</env-entry-type>\n" +
                "        <env-entry-value>AnotherValue</env-entry-value>\n" +
                "    </env-entry>\n" );
        this.ejbRef.append(""+
                "    <ejb-ref>\n" +
                "        <description>Test EJB</description>\n" +
                "        <ejb-link>test</ejb-link>\n" +
                "        <home>testHomeInterface</home>\n" +
                "        <remote>testRemoteInterface</remote>\n" +
                "        <ejb-ref-name>test</ejb-ref-name>\n" +
                "        <ejb-ref-type>org.apache.pluto.test.ejb.TestEJB</ejb-ref-type>\n" +
                "    </ejb-ref>\n" );
        this.xmlEnd.append("</web-app>");

    }

    public void tearDown() {

    }


    public void testReadFullDescriptor()
    throws IOException {
        StringBuffer xml = new StringBuffer()
        .append(xmlBegin).append(icon).append(misc)
        .append(ctxParams).append(sessionConfig).append(filters)
        .append(listeners).append(servlets).append(filterMappings)
        .append(servletMappings).append(mimeMappings).append(welcomeFiles)
        .append(errorPage).append(taglib).append(resourceRef)
        .append(securityConstraint).append(loginConfig)
        .append(securityRole).append(envEntry).append(ejbRef)
        .append(xmlEnd);

        InputStream in = new ByteArrayInputStream(xml.toString().getBytes());

        WebAppDD dd = impl.read(in);

        Assert.assertEquals("DisplayName not as expected", "Test Web XML", dd.getDisplayName());
        Assert.assertEquals("Description does not match", "This is a test web.xml file.", dd.getDescription());
        //Assert.assertTrue("Distributable not set", dd.isDistributable());

        assertEquals("Context Parameters not as expected", 2, dd.getContextParams().size());
        assertEquals("Filters not as expected", 1, dd.getFilters().size());
        assertEquals("Filter Mappings not as expected", 2, dd.getFilterMappings().size());
        assertEquals("Listeners not as expected", 2, dd.getListeners().size());
        assertEquals("Servlets not as expected", 2, dd.getServlets().size());
        assertEquals("Servlet Mappings not as expected.", 3, dd.getServletMappings().size());
    }

    public void testReadNullDescriptor() throws IOException {
        Assert.assertNull("Null descriptor did not return null", impl.read(null));
    }

    public void testReadPartialDescriptor() {

    }

    public void testReadOutOfOrderDescriptor() {

    }

}
