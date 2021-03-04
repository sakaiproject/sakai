package org.sakaiproject.meetings.tool;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.sakaiproject.util.RequestFilter;
import org.sakaiproject.util.SakaiContextLoaderListener;
import org.sakaiproject.util.ToolListener;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class MeetingsWebAppConfiguration implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {

        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
        rootContext.setServletContext(servletContext);
        rootContext.register(MeetingsWebMvcConfiguration.class);

        servletContext.addListener(new ToolListener());
        servletContext.addListener(new SakaiContextLoaderListener(rootContext));

        servletContext.addFilter("sakai.request", RequestFilter.class)
                .addMappingForUrlPatterns(
                        EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE),
                        true,
                        "/*");

        ServletRegistration.Dynamic servlet = servletContext.addServlet("sakai.meetings", new DispatcherServlet(rootContext));
        servlet.addMapping("/");
        servlet.setLoadOnStartup(1);
    }
}
