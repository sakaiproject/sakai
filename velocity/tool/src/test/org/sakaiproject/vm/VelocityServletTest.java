/*
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.vm;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class VelocityServletTest {

    @Rule
    public TemporaryFolder templates = new TemporaryFolder();

    @Test
    public void rendersSuccessiveIncludesToTheSameResponse() throws Exception {
        ServletContext servletContext = mock(ServletContext.class);
        when(servletContext.getRealPath("/")).thenReturn(templates.getRoot().getAbsolutePath());
        ServletConfig config = mock(ServletConfig.class);
        when(config.getServletContext()).thenReturn(servletContext);
        Servlet servlet = new VelocityServlet();
        servlet.init(config);

        Map<String, Object> attributes = new HashMap<>();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttributeNames()).thenAnswer(invocation -> Collections.enumeration(attributes.keySet()));
        when(request.getAttribute("sakai.vm.path")).thenAnswer(invocation -> attributes.get("sakai.vm.path"));

        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getWriter()).thenReturn(writer);

        for (String content : new String[] { "header", "picker", "footer" }) {
            Path template = templates.getRoot().toPath().resolve(content + ".vm");
            Files.writeString(template, content);
            attributes.put("sakai.vm.path", "/" + template.getFileName());
            servlet.service(request, response);
        }
        writer.flush();

        assertEquals("headerpickerfooter", output.toString());
    }
}
