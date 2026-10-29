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

package org.sakaiproject.webapi.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.sakaiproject.webapi.WebMvcConfiguration;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

public class WebApiPolicyInventoryTests {

    @Test
    public void everyRegisteredHandlerHasAReviewedAccessContract() throws Exception {
        try (GenericWebApplicationContext context = new GenericWebApplicationContext()) {
            context.setServletContext(new MockServletContext());
            ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
            URL productionLocation = WebMvcConfiguration.class
                .getProtectionDomain().getCodeSource().getLocation();
            Set<String> productionControllers = new HashSet<>();
            Set<String> excludedTestControllers = new HashSet<>();
            // Match the production component-scan root, including future sibling packages.
            for (BeanDefinition definition : scanner.findCandidateComponents("org.sakaiproject.webapi")) {
                String className = definition.getBeanClassName();
                Class<?> controller = Class.forName(className, false, getClass().getClassLoader());
                if (!productionLocation.equals(controller.getProtectionDomain().getCodeSource().getLocation())) {
                    excludedTestControllers.add(className);
                    continue;
                }
                productionControllers.add(className);
                definition.setLazyInit(true);
                context.registerBeanDefinition(className, definition);
            }
            Assert.assertTrue("Test controllers outside the old controllers package must be discovered and excluded by origin",
                excludedTestControllers.contains(WebApiAccessTests.TestController.class.getName()));
            context.refresh();
            RequestMappingHandlerMapping mapping = new RequestMappingHandlerMapping();
            mapping.setApplicationContext(context);
            mapping.afterPropertiesSet();
            assertEquals("The registry must contain every production RestController under the application scan root",
                productionControllers, mapping.getHandlerMethods().values().stream()
                    .map(handler -> handler.getBeanType().getName()).collect(Collectors.toSet()));
            List<String> actual = new ArrayList<>();
            for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : mapping.getHandlerMethods().entrySet()) {
                HandlerMethod method = entry.getValue();
                PublicApi publicApi = method.getMethodAnnotation(PublicApi.class);
                if (publicApi != null) { assertFalse(publicApi.value().isBlank()); }
                for (String pattern : entry.getKey().getPatternValues()) {
                    String policy = publicApi != null ? "PUBLIC" :
                        pattern.contains("{siteId}") ? "AUTHENTICATED_SITE" : "AUTHENTICATED";
                    actual.add(entry.getKey().getMethodsCondition() + " " + pattern + " " + policy + " " +
                        method.getBeanType().getSimpleName() + "." + method.getMethod().getName());
                }
            }
            actual.sort(String::compareTo);
            String inventory = String.join("\n", actual) + "\n";
            assertEquals(Set.of("LoginController.login", "UserController.checkSuperUser",
                    "EventsController.getPushKey", "SecureDeliveryController.getSebConfig"),
                actual.stream().filter(line -> line.contains(" PUBLIC "))
                    .map(line -> line.substring(line.lastIndexOf(' ') + 1)).collect(Collectors.toSet()));
            try (InputStream expected = getClass().getResourceAsStream("/webapi-access-inventory.txt")) {
                List<String> contracts = new String(expected.readAllBytes(), StandardCharsets.UTF_8).lines().toList();
                for (String contract : contracts) {
                    String[] fields = contract.split(" \\| ", -1);
                    assertEquals("Each route needs operation, owning service, and required actor: " + contract, 4, fields.length);
                    for (String field : fields) { assertFalse(field.isBlank()); }
                }
                assertEquals("Update the reviewed endpoint policy inventory intentionally. Actual:\n" + inventory,
                    contracts.stream().map(line -> line.split(" \\| ", -1)[0]).collect(Collectors.joining("\n", "", "\n")), inventory);
            }
        }
    }
}
