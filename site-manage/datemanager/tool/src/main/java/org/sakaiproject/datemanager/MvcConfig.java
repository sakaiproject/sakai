package org.sakaiproject.datemanager;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 *
 * @author plukasew
 */
@Configuration
@EnableWebMvc
@ComponentScan("org.sakaiproject.datemanager")
public class MvcConfig extends WebMvcConfigurerAdapter
{
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry)
	{
		registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/js/");
	}
}
