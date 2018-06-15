/**********************************************************************************
 *
 * Copyright (c) 2017 The Sakai Foundation
 *
 * Original developers:
 *
 *   Unicon
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.rubrics;

import java.util.Arrays;
import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.impl.SpringCompMgr;
import org.sakaiproject.util.RequestFilter;
import org.sakaiproject.util.ToolListener;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

@Configuration
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@SpringBootApplication
@Slf4j
public class RubricsApplication extends SpringBootServletInitializer {

    /**
     * Required per http://docs.spring.io/spring-boot/docs/current/reference/html/howto-traditional-deployment.html
     * in order produce a traditional deployable war file.
     *
     * @param application
     * @return
     */

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        ConfigurableApplicationContext sharedAc = ((SpringCompMgr) ComponentManager.getInstance()).getApplicationContext();
        application.parent(sharedAc);
        return application.bannerMode(Banner.Mode.OFF).sources(RubricsApplication.class);
    }

    @Bean
    public ServletRegistrationBean rubricsServlet() {
        ServletRegistrationBean srb = new ServletRegistrationBean(new DispatcherServlet(new AnnotationConfigWebApplicationContext())) {
            @Override
            public void onStartup(ServletContext servletContext) throws ServletException {
                super.onStartup(servletContext);
                servletContext.addListener(ToolListener.class);
                // servletContext.addListener(SakaiContextLoaderListener.class);
            }
        };
        srb.setName("sakai.rubrics");
        srb.setLoadOnStartup(0);
        srb.addUrlMappings("/", "/index");
        return srb;
    }

    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        FilterRegistrationBean frb = new FilterRegistrationBean();
        frb.setName("sakai.request");
        frb.setServletNames(Arrays.asList("sakai.rubrics", "dispatcherServlet"));
        frb.setFilter(new RequestFilter());
        frb.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE);
        return frb;
    }
}
