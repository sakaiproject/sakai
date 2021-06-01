package org.sakaiproject.rubrics.api.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class RubricsTestConfiguration {

    @Autowired
    private Environment environment;
}
