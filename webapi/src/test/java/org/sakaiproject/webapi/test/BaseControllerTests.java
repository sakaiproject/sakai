package org.sakaiproject.webapi.test;

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
