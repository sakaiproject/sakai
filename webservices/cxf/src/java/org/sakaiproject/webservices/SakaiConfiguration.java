/**
 * Copyright (c) 2005 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.webservices;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.api.ServerConfigurationService.ConfigItem;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.util.BasicConfigItem;
import org.sakaiproject.util.Xml;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: 9/19/11
 * Time: 10:21 AM
 * To change this template use File | Settings | File Templates.
 */
@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC, use = SOAPBinding.Use.LITERAL)
@Slf4j
public class SakaiConfiguration extends AbstractWebService {

    @WebMethod
    @Path("/adjustLogLevel")
    @Produces("text/plain")
    @GET
    public String adjustLogLevel(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "packageName", partName = "packageName") @QueryParam("packageName") String packageName,
            @WebParam(name = "level", partName = "level") @QueryParam("level") String level) {
        Session session = establishSession(sessionid);
        if (!securityService.isSuperUser()) {
            log.warn("NonSuperUser trying to collect configuration: " + session.getUserId());
            throw new RuntimeException("NonSuperUser trying to collect configuration: " + session.getUserId());
        }
        Properties props = new Properties();
        try {

            // ok, yes I know this is fragile and totally tomcat specific, but it works and avoids having to
            // configure up a new version of the log4j file somehow.   For sure this will break in tomcat 6, as
            // I think classloading is much different there.
            // This finds the log4j file in kernel common:
            // common/lib/sakai-kernel-common-x.x.x.jar!/log4j.properties
            InputStream configStream = this.getClass().getClassLoader().getParent().getParent().getResourceAsStream("log4j.properties");
            props.load(configStream);
            configStream.close();
            props.setProperty("log4j.logger." + packageName, level);
            LogManager.resetConfiguration();
            PropertyConfigurator.configure(props);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return "success";
    }

    @WebMethod
    @Path("/getProperty")
    @Produces("text/plain")
    @GET
    public String getProperty(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "propName", partName = "propName") @QueryParam("propName") String propName) {
        Session session = establishSession(sessionid);
        if (!securityService.isSuperUser()) {
            log.warn("NonSuperUser trying to collect configuration: " + session.getUserId());
            throw new RuntimeException("NonSuperUser trying to collect configuration: " + session.getUserId());
        }
        return lookupConfigValue(propName);
    }


    @WebMethod
    @Path("/setProperty")
    @Produces("text/plain")
    @GET
    public String setProperty(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "propName", partName = "propName") @QueryParam("propName") String propName,
            @WebParam(name = "propType", partName = "propType") @QueryParam("propType") String propType,
            @WebParam(name = "propValue", partName = "propValue") @QueryParam("propValue") String propValue) {
        if (StringUtils.isBlank(sessionid) || StringUtils.isBlank(propName) || StringUtils.isBlank(propType) || StringUtils.isBlank(propValue)) {
            log.warn("IllegalArgument: One or more of the parameters were empty or null");
            throw new RuntimeException("IllegalArgument: One or more of the parameters were empty or null");
        }

        Session session = establishSession(sessionid);
        if (!securityService.isSuperUser()) {
            log.warn("NonSuperUser trying to adjust configuration: " + session.getUserId());
            throw new RuntimeException("NonSuperUser trying to adjust configuration: " + session.getUserId());
        }
        return changeConfigValue(propName, propType, propValue);
    }

    /**
     * @param sessionid
     * @param propNames - comma separated list of property names
     * @return
     */
    @WebMethod
    @Path("/getProperties")
    @Produces("text/plain")
    @GET
    public String getProperties(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "propNames", partName = "propNames") @QueryParam("propNames") String propNames) {
        Session session = establishSession(sessionid);
        if (!securityService.isSuperUser()) {
            log.warn("NonSuperUser trying to collect configuration: " + session.getUserId());
            throw new RuntimeException("NonSuperUser trying to collect configuration: " + session.getUserId());
        }
        String[] propNamesArray = propNames.split(",");
        Map<String, String> propertyMap = new HashMap();
        for (int i = 0; i < propNamesArray.length; i++) {
            String propValue = lookupConfigValue(propNamesArray[i]);
            String propName = propNamesArray[i].trim().replace("@", "-").replace(".", "_");
            propertyMap.put(propName, propValue);
        }
        log.debug(getXML("properties", propertyMap));
        return getXML("properties", propertyMap);

    }

    private String changeConfigValue(String name, String type, String value) {
        String propName = name.trim();
        String propType = type.trim();
        Object propValue = null;
        if (propName.contains("@")) {
            log.error("UnSupported: Bean setting is not supported for, " + propName + "=" + propValue);
            throw new RuntimeException("UnSupported: Bean setting is not supported for, " + propName + "=" + propValue);
        }

        // convert value to a SCS type
        if (ServerConfigurationService.TYPE_STRING.equalsIgnoreCase(propType)) {
            propValue = new String(value.trim());
        } else if (ServerConfigurationService.TYPE_BOOLEAN.equalsIgnoreCase(propType)) {
            propValue = Boolean.valueOf(value.trim());
        } else if (ServerConfigurationService.TYPE_INT.equalsIgnoreCase(propType)) {
            propValue = Integer.valueOf(value.trim());
        } else if (ServerConfigurationService.TYPE_ARRAY.equalsIgnoreCase(propType)) {
            propValue = value.split(",");
        } else {
            log.error("UnSupported: type, " + propType);
            throw new RuntimeException("UnSupported: type, " + propType);
        }
        ConfigItem item = BasicConfigItem.makeConfigItem(propName, propValue, SakaiConfiguration.class.getName());
        serverConfigurationService.registerConfigItem(item);
        return "success";
    }

    private String lookupConfigValue(String s) {
        String propName = s.trim();
        String propValue = null;
        if (propName.contains("@")) {
            propValue = getValueFromBean(propName);
            log.debug(propName + "=" + propValue);
        } else {
            propValue = serverConfigurationService.getString(propName);
        }
        if (propValue != null && propValue.length() > 0) {
            if (isSecureProperty(propName)) {
                return "XXXXXX";
            } else {
                return propValue;
            }
        }
        return "";
    }

    protected String getValueFromBean(String propName) {
        String beanName = propName.trim().split("@")[1];
        Object bean = ComponentManager.get(beanName.trim());
        if (bean != null) {
            String methodName = propName.trim().split("@")[0];
            Class clazz = bean.getClass();
            String methodNameEnd = Character.toUpperCase(methodName.charAt(0)) + methodName.substring(1);
            Method method = null;

            try {
                method = clazz.getMethod("get" + methodNameEnd, null);
            } catch (NoSuchMethodException e) {

                log.info("can't find method called " + " get" + methodNameEnd +
                        " in class " + clazz.getName());
                try {

                    method = clazz.getMethod("is" + methodNameEnd, null);
                } catch (NoSuchMethodException e1) {
                    log.info("can't find method called " + " is" + methodNameEnd +
                            " in class " + clazz.getName());
                }
            }

            if (method != null) {
                try {
                    Object returnValue = method.invoke(bean, null);
                    return returnValue.toString();
                } catch (Exception e) {
                    log.error("error calling accessor on bean :" + beanName + " msg: " + e.getMessage(), e);
                }
            }
            log.error("couldn't find config value for propName: " + propName);
        } else {
            log.error("can't find bean with id: " + beanName);
        }
        return null;
    }

    private boolean isSecureProperty(String propName) {
        // ok not the best, but better than nothing...
        if (propName.toLowerCase().contains("password") || propName.toLowerCase().contains("secret")
                || propName.toLowerCase().contains("pwd") || propName.toLowerCase().contains("key")) {
            return true;
        }
        return false;
    }

    private String getXML(String rootNodeName, Map<String, String> propertyMap) {
        Document doc = Xml.createDocument();
        Node results = doc.createElement(rootNodeName);
        doc.appendChild(results);

        for (String key : propertyMap.keySet()) {

            Node itemChild = doc.createElement(key);
            itemChild.appendChild(doc.createTextNode(propertyMap.get(key)));
            results.appendChild(itemChild);

        }

        return Xml.writeDocumentToString(doc);
    }

}
