/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.config.impl;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.util.BasicConfigItem;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.Descriptor;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ReflectionException;
import javax.management.modelmbean.DescriptorSupport;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.sakaiproject.component.api.ServerConfigurationService.ConfigItem;

/**
 * Allows editing of configuration at runtime through a JMX configuration.
 * It doesn't display any configuration that is marked as secure back to the caller.
 */
public class ConfigurationMBean extends NotificationBroadcasterSupport implements DynamicMBean {

    private ServerConfigurationService serverConfigurationService;
    private AtomicLong notificationSeq = new AtomicLong(0);

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    @Override
    public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
        ConfigItem configItem = serverConfigurationService.getConfigItem(attribute);
        if (configItem.isSecured()) {
            return null;
        }
        return configItem.getValue();
    }

    @Override
    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        ConfigItem config = new BasicConfigItem(attribute.getName(), attribute.getValue(), null, null, null, true);
        serverConfigurationService.registerConfigItem(config);
    }

    @Override
    public AttributeList getAttributes(String[] attributes) {
        AttributeList list = new AttributeList();
        for (ConfigItem item: serverConfigurationService.getConfigData().getItems()) {
            String name = item.getName();
            Object value = item.getValue();
            Attribute attribute;
            if (item.isSecured()) {
                attribute = new Attribute(name, null);
            } else {
                attribute = new Attribute(name, value);
            }
            list.add(attribute);
        }
        return list;

    }

    @Override
    public AttributeList setAttributes(AttributeList attributes) {
        AttributeList successful = new AttributeList();
        for (Attribute attribute: attributes.asList()) {
            try {
                setAttribute(attribute);
                successful.add(attribute);
            } catch (AttributeNotFoundException | InvalidAttributeValueException | MBeanException | ReflectionException e) {
                // Ignore as we just won't return it in the result.
            }
        }
        return successful;
    }

    @Override
    public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
        List<Class<?>> signatureClasses = new ArrayList<>();
        for (String classString : signature) {
            try {
                signatureClasses.add(Class.forName(classString));
            } catch (ClassNotFoundException e) {
                throw new ReflectionException(e);
            }
        }
        try {
            Method method = getClass().getMethod(actionName, signatureClasses.toArray(new Class<?>[]{}));
            return method.invoke(this, params);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new ReflectionException(e);
        }
    }

    /**
     * Called through reflection on this class to add a new attribute
     * @param key Configuration key.
     * @param value Configuration value.
     */
    public void addAttribute(String key, String value) {
        ConfigItem config = new BasicConfigItem(key, value, null, null, null, true);
        serverConfigurationService.registerConfigItem(config);

        Notification notification = new Notification("jmx.mbean.info.changed", this, notificationSeq.getAndIncrement());
        notification.setUserData(getMBeanInfo());
        sendNotification(notification);
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        List<MBeanAttributeInfo> attributeInfos = new ArrayList<>();
        for (ConfigItem item : serverConfigurationService.getConfigData().getItems()) {
            String type = item.getType();
            switch (type) {
                case ServerConfigurationService.TYPE_BOOLEAN:
                    type = "boolean";
                    break;
                case ServerConfigurationService.TYPE_INT:
                    type = "int";
                    break;
                case ServerConfigurationService.TYPE_STRING:
                    type = "java.lang.String";
                    break;
            }
            attributeInfos.add(new MBeanAttributeInfo(item.getName(), type, item.getDescription(), !item.isSecured(), true, false));
        }
        List<MBeanOperationInfo> operationInfos = new ArrayList<>();
        try {
            Method method = getClass().getMethod("addAttribute", String.class, String.class);
            operationInfos.add(new MBeanOperationInfo("addAttribute", method));
        } catch (NoSuchMethodException e) {
            // Ignore
        }
        Descriptor descriptor = new DescriptorSupport();
        descriptor.setField("immutableInfo", "false");
        return new MBeanInfo(getClass().getName(), "Sakai Server Configuration",
            attributeInfos.toArray(new MBeanAttributeInfo[]{}),
            null,
            operationInfos.toArray(new MBeanOperationInfo[]{}),
            null,
            descriptor);
    }
}
