package org.sakaiproject.ignite;

import org.apache.ignite.IgniteSpringBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.SmartInitializingSingleton;

public class EagerIgniteSpringBean extends IgniteSpringBean implements InitializingBean, SmartInitializingSingleton {

    @Override
    public void afterSingletonsInstantiated() {
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterSingletonsInstantiated();
    }
}
