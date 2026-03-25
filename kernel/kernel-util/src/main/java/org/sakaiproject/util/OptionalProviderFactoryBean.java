/**
 * Copyright (c) 2003-2025 The Apereo Foundation
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
package org.sakaiproject.util;

import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;

/**
 * A Spring {@link FactoryBean} that optionally proxies to a target bean if it
 * exists, or falls back to a no-op JDK dynamic proxy when the target is not
 * available. This is a drop-in replacement for {@code ProxyFactoryBean} when
 * the target bean is optional.
 *
 * <p>When the {@code targetName} is an unresolved property placeholder
 * (e.g. {@code ${some.property}}) or refers to a bean that does not exist,
 * the factory returns a no-op proxy whose methods return safe defaults:
 * empty collections, {@code null} for objects, and zero/false for primitives.</p>
 *
 * <p>Example RoleProvider usage in Spring XML:</p>
 * <pre>{@code
 * <bean id="org.sakaiproject.authz.api.RoleProvider"
 *       class="org.sakaiproject.util.OptionalProviderFactoryBean">
 *     <property name="targetName" value="${org.sakaiproject.authz.api.RoleProvider}"/>
 *     <property name="proxyInterface" value="org.sakaiproject.authz.api.RoleProvider"/>
 * </bean>
 * }</pre>
 *
 * Then you will need to activate your custom RoleProvider bean in sakai.properties.
 * <pre>
 * org.sakaiproject.authz.api.RoleProvider=CustomRoleProvider
 * </pre>
 */
@Slf4j
public class OptionalProviderFactoryBean implements FactoryBean<Object>, BeanFactoryAware {

    @Setter private Class<?> proxyInterface;
    @Setter private String targetName;
    @Setter private BeanFactory beanFactory;

    @Override
    public Object getObject() {
        if (targetName != null && !targetName.contains("${") && beanFactory.containsBean(targetName)) {
            log.info("Resolved optional provider [{}] to bean [{}]", proxyInterface.getName(), targetName);
            return beanFactory.getBean(targetName, proxyInterface);
        }

        log.info("No implementation found for optional provider [{}], using no-op proxy", proxyInterface.getName());
        return Proxy.newProxyInstance(
                proxyInterface.getClassLoader(),
                new Class<?>[] { proxyInterface },
                (proxy, method, args) -> getDefaultValue(method.getReturnType()));
    }

    @Override
    public Class<?> getObjectType() {
        return proxyInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    private static Object getDefaultValue(Class<?> returnType) {
        if (returnType == void.class) return null;
        if (returnType == boolean.class) return false;
        if (returnType == byte.class) return (byte) 0;
        if (returnType == short.class) return (short) 0;
        if (returnType == int.class) return 0;
        if (returnType == long.class) return 0L;
        if (returnType == float.class) return 0.0f;
        if (returnType == double.class) return 0.0d;
        if (returnType == char.class) return '\0';
        if (Set.class.isAssignableFrom(returnType)) return Collections.emptySet();
        if (List.class.isAssignableFrom(returnType)) return Collections.emptyList();
        if (Map.class.isAssignableFrom(returnType)) return Collections.emptyMap();
        if (Collection.class.isAssignableFrom(returnType)) return Collections.emptyList();
        return null;
    }
}
