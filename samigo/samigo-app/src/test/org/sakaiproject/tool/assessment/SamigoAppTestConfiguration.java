package org.sakaiproject.tool.assessment;

import static org.mockito.Mockito.mock;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.util.api.FormattedText;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SamigoAppTestConfiguration {

    @Bean(name = "org.sakaiproject.util.api.FormattedText")
    public FormattedText formattedText() {
        return mock(FormattedText.class);
    }

    @Bean(name = "org.sakaiproject.component.api.ServerConfigurationService")
    public ServerConfigurationService serverConfigurationService() {
        return mock(ServerConfigurationService.class);
    }
}
