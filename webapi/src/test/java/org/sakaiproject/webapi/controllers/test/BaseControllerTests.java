/*
 * Copyright (c) 2003-2025 The Apereo Foundation
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
package org.sakaiproject.webapi.controllers.test;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.hamcrest.CoreMatchers.*;

import java.net.URI;
import java.util.Collection;

import org.springframework.restdocs.operation.OperationRequest;
import org.springframework.restdocs.operation.OperationRequestPart;
import org.springframework.restdocs.operation.Parameters;
import org.springframework.restdocs.operation.RequestCookie;
import org.springframework.restdocs.operation.preprocess.OperationRequestPreprocessor;

import static org.mockito.Mockito.*;

public abstract class BaseControllerTests {

    protected OperationRequestPreprocessor preprocessor;

    public BaseControllerTests() {

        preprocessor = new OperationRequestPreprocessor() {

            public OperationRequest preprocess(OperationRequest request) {

                URI uri = request.getUri();

                return new OperationRequest() {

                    public byte[] getContent() { return request.getContent(); }
                    public String getContentAsString() { return request.getContentAsString(); }
                    public Collection<RequestCookie> getCookies() { return request.getCookies(); }
                    public org.springframework.http.HttpHeaders getHeaders() { return request.getHeaders(); }
                    public org.springframework.http.HttpMethod getMethod() { return request.getMethod(); }
                    public Parameters getParameters() { return request.getParameters(); }
                    public Collection<OperationRequestPart> getParts() { return request.getParts(); }

                    public URI getUri() {
                        try {
                            return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), "/api" + uri.getPath(), uri.getQuery(), uri.getFragment());
                        } catch (Exception e) {
                        }
                        return uri;
                    }
                };
            }
        };
    }
}
