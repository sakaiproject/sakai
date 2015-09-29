/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.util.serializable;


import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.LookupOverride;
import org.springframework.beans.factory.support.MethodOverride;
import org.springframework.beans.factory.support.MethodReplacer;
import org.springframework.beans.factory.support.ReplaceOverride;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.support.SimpleInstantiationStrategy;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.CallbackFilter;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.cglib.proxy.NoOp;

/**
 * Largely a copy of the Spring 2.3.2 version of the original EXCEPT that we use a serializable form of the NoOp
 * @see org.springframework.beans.factory.support.CglibSubclassingInstantiationStrategy
 */
public class SakaiCglibSubclassingInstantiationStrategy extends SimpleInstantiationStrategy {
    /**
     * Index in the CGLIB callback array for passthrough behavior,
     * in which case the subclass won't override the original class.
     */
    private static final int PASSTHROUGH = 0;

    /**
     * Index in the CGLIB callback array for a method that should
     * be overridden to provide method lookup.
     */
    private static final int LOOKUP_OVERRIDE = 1;

    /**
     * Index in the CGLIB callback array for a method that should
     * be overridden using generic Methodreplacer functionality.
     */
    private static final int METHOD_REPLACER = 2;

    @Override
    protected Object instantiateWithMethodInjection(RootBeanDefinition beanDefinition, String beanName,
            BeanFactory owner) {

        // Must generate CGLIB subclass.
        return new CglibSubclassCreator(beanDefinition, owner).instantiate(null, null);
    }

    @Override
    protected Object instantiateWithMethodInjection(RootBeanDefinition beanDefinition, String beanName,
            BeanFactory owner, Constructor ctor, Object[] args) {

        return new CglibSubclassCreator(beanDefinition, owner).instantiate(ctor, args);
    }

    /**
     * An inner class created for historical reasons to avoid external CGLIB dependency
     * in Spring versions earlier than 3.2.
     */
    private static class CglibSubclassCreator implements Serializable {

        private static final Log logger = LogFactory.getLog(CglibSubclassCreator.class);

        private final RootBeanDefinition beanDefinition;

        private final BeanFactory owner;

        public CglibSubclassCreator(RootBeanDefinition beanDef, BeanFactory owner) {
            this.beanDefinition = beanDef;
            this.owner = owner;
            //            this.beanDefinition = beanDef;
            //            this.beanDefinition.setConstructorArgumentValues(new SakaiConstructorArgumentValues(beanDef
            //                    .getConstructorArgumentValues()));
            //            this.beanDefinition.setMethodOverrides(new SakaiMethodOverrides(beanDef.getMethodOverrides()));
            //            // update the propertyValues - parse through the original list and then replace it with the revised version.
            //            List<PropertyValue> revisedList = new ArrayList<PropertyValue>();
            //            for (PropertyValue propertyValue : beanDef.getPropertyValues().getPropertyValueList()) {
            //                revisedList.add(new SakaiPropertyValue(propertyValue));
            //            }
            //            // The getPropertyValueList method actually gives you a handle to the actual list used.
            //            beanDef.getPropertyValues().getPropertyValueList().clear();
            //            beanDef.getPropertyValues().getPropertyValueList().addAll(revisedList);
            //            try {
            //                beanDef.setResource(new SakaiFileSystemResource(beanDef.getResource()));
            //            } catch (IOException ioe) {
            //                // If this happened, something bad happened - the resource existed a split second ago...
            //            }
            //         // The locks are private and final, so we have to use reflection to fix the problem
            //            try {
            //                Class<?> clazz = this.beanDefinition.getClass();
            //                Field lock = clazz.getDeclaredField("constructorArgumentLock");
            //                lock.setAccessible(true);
            //                lock.set(this.beanDefinition, new Serializable(){});
            //                lock = clazz.getDeclaredField("postProcessingLock");
            //                lock.setAccessible(true);
            //                lock.set(this.beanDefinition, new Serializable() {
            //                });
            //            } catch (Exception e) {
            //                e.printStackTrace();
            //            }
            //            this.owner = owner;
        }

        /**
         * Create a new instance of a dynamically generated subclasses implementing the
         * required lookups.
         * @param ctor constructor to use. If this is {@code null}, use the
         * no-arg constructor (no parameterization, or Setter Injection)
         * @param args arguments to use for the constructor.
         * Ignored if the ctor parameter is {@code null}.
         * @return new instance of the dynamically generated class
         */
        public Object instantiate(Constructor ctor, Object[] args) {
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(this.beanDefinition.getBeanClass());
            enhancer.setCallbackFilter(new CallbackFilterImpl());
            enhancer.setCallbacks(new Callback[] { SerializableNoOp.INSTANCE, new LookupOverrideMethodInterceptor(),
                    new ReplaceOverrideMethodInterceptor() });

            return (ctor == null) ? enhancer.create() : enhancer.create(ctor.getParameterTypes(), args);
        }

        /**
         * Class providing hashCode and equals methods required by CGLIB to
         * ensure that CGLIB doesn't generate a distinct class per bean.
         * Identity is based on class and bean definition.
         */
        private class CglibIdentitySupport implements Serializable {
            /**
             * Exposed for equals method to allow access to enclosing class field
             */
            protected RootBeanDefinition getBeanDefinition() {
                return beanDefinition;
            }

            @Override
            public boolean equals(Object other) {
                return (other.getClass().equals(getClass()) && ((CglibIdentitySupport) other).getBeanDefinition()
                        .equals(beanDefinition));
            }

            @Override
            public int hashCode() {
                return beanDefinition.hashCode();
            }
        }

        /**
         * CGLIB MethodInterceptor to override methods, replacing them with an
         * implementation that returns a bean looked up in the container.
         */
        private class LookupOverrideMethodInterceptor extends CglibIdentitySupport implements MethodInterceptor {
            public Object intercept(Object obj, Method method, Object[] args, MethodProxy mp) throws Throwable {
                // Cast is safe, as CallbackFilter filters are used selectively.
                LookupOverride lo = (LookupOverride) beanDefinition.getMethodOverrides().getOverride(method);
                return owner.getBean(lo.getBeanName());
            }
        }

        /**
         * CGLIB MethodInterceptor to override methods, replacing them with a call
         * to a generic MethodReplacer.
         */
        private class ReplaceOverrideMethodInterceptor extends CglibIdentitySupport implements MethodInterceptor {

            public Object intercept(Object obj, Method method, Object[] args, MethodProxy mp) throws Throwable {
                ReplaceOverride ro = (ReplaceOverride) beanDefinition.getMethodOverrides().getOverride(method);
                // TODO could cache if a singleton for minor performance optimization
                MethodReplacer mr = (MethodReplacer) owner.getBean(ro.getMethodReplacerBeanName());
                return mr.reimplement(obj, method, args);
            }
        }

        /**
         * CGLIB object to filter method interception behavior.
         */
        private class CallbackFilterImpl extends CglibIdentitySupport implements CallbackFilter {

            public int accept(Method method) {
                MethodOverride methodOverride = beanDefinition.getMethodOverrides().getOverride(method);
                if (logger.isTraceEnabled()) {
                    logger.trace("Override for '" + method.getName() + "' is [" + methodOverride + "]");
                }
                if (methodOverride == null) {
                    return PASSTHROUGH;
                } else if (methodOverride instanceof LookupOverride) {
                    return LOOKUP_OVERRIDE;
                } else if (methodOverride instanceof ReplaceOverride) {
                    return METHOD_REPLACER;
                }
                throw new UnsupportedOperationException("Unexpected MethodOverride subclass: "
                        + methodOverride.getClass().getName());
            }
        }
        
        private interface SerializableNoOp extends NoOp, Serializable {
            public static final NoOp INSTANCE = new SerializableNoOp() {
            };
        }
    }

}