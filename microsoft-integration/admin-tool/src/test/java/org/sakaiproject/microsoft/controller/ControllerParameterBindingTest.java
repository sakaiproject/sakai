/*
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
package org.sakaiproject.microsoft.controller;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Collections;
import org.junit.Test;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.annotation.RequestParamMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.ServletRequestDataBinderFactory;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/** Verify HTTP parameter binding before the real controller's business logic runs. */
public class ControllerParameterBindingTest {
    @Test
    public void bindsOptionalSynchronizationDateUsingItsDeclaredParameterName() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("date", "2026-10-07");
        assertEquals(LocalDate.of(2026, 10, 7), resolveDate(request));
    }

    @Test
    public void acceptsOmittedOptionalSynchronizationDate() throws Exception {
        assertNull(resolveDate(new MockHttpServletRequest()));
    }

    private Object resolveDate(MockHttpServletRequest request) throws Exception {
        Method handler = MainController.class.getMethod("updateSiteSynchronizationDate",
                String.class, String.class, LocalDate.class, Model.class);
        MethodParameter parameter = new MethodParameter(handler, 2);
        parameter.initParameterNameDiscovery(new DefaultParameterNameDiscoverer());
        ConfigurableWebBindingInitializer initializer = new ConfigurableWebBindingInitializer();
        initializer.setConversionService(new DefaultFormattingConversionService());
        ServletRequestDataBinderFactory binder = new ServletRequestDataBinderFactory(Collections.emptyList(), initializer);
        return new RequestParamMethodArgumentResolver(false).resolveArgument(parameter,
                new ModelAndViewContainer(), new ServletWebRequest(request), binder);
    }
}
