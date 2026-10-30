/**
 * Copyright (c) 2026 The Apereo Foundation
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

package org.sakaiproject.webapi;

import org.sakaiproject.webapi.lti.LtiBearerTokenInterceptor;
import org.sakaiproject.webapi.security.WebApiAccessInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Shared production registration, also used by the MVC authorization integration tests. */
@Configuration
public class WebApiAccessConfiguration implements WebMvcConfigurer {

    private final LtiBearerTokenInterceptor bearerInterceptor;
    private final WebApiAccessInterceptor accessInterceptor;

    public WebApiAccessConfiguration(LtiBearerTokenInterceptor bearerInterceptor,
            WebApiAccessInterceptor accessInterceptor) {
        this.bearerInterceptor = bearerInterceptor;
        this.accessInterceptor = accessInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(bearerInterceptor)
            .excludePathPatterns("/login", "/docs/**", "/webjars/**");
        registry.addInterceptor(accessInterceptor);
    }
}
