/**
 * Copyright (c) 2019 The Apereo Foundation
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
package org.sakaiproject.scorm.ui;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.wicket.request.HttpHeaderCollection;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.PartWriterCallback;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.sakaiproject.scorm.service.sakai.impl.ContentPackageSakaiResource;
import org.sakaiproject.scorm.ui.player.util.CompressingContentPackageResourceStream;
import org.sakaiproject.scorm.ui.player.util.ContentPackageWebResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.sakaiproject.scorm.api.ScormConstants.ROOT_DIRECTORY;

/**
 *
 * @author bjones86
 */
@Slf4j
public class ContentPackageResourceReference extends ResourceReference
{
    public ContentPackageResourceReference()
    {
        super( ContentPackageResourceReference.class, "contentPackages" );
    }

    @Override
    public IResource getResource()
    {
        return new ContentPackageResource();
    }

    public class ContentPackageResource extends AbstractResource {

        @Override
        protected ResourceResponse newResourceResponse(Attributes attributes) {
            final String resourceId = attributes.getParameters().get("resourceID").toOptionalString();
            final String resourceName = attributes.getParameters().get("resourceName").toOptionalString();

            log.debug("Process request for resource id/name [{}/{}]", resourceId, resourceName);
            if (StringUtils.isNoneBlank(resourceId, resourceName)) {

                String base = ROOT_DIRECTORY + resourceId + "/" + resourceName;
                PageParameters parameters = attributes.getParameters();
                String suffix = IntStream.range(0, parameters.getIndexedCount())
                        .mapToObj(i -> parameters.get(i).toString())
                        .takeWhile(s -> !"contentpackages".equalsIgnoreCase(s))
                        .map(s -> "/" + s)
                        .collect(Collectors.joining());

                String path = base + suffix;

                // resource not found in shared resources, so attempt to get resource from Sakai
                ContentPackageSakaiResource cpResource = new ContentPackageSakaiResource(path, path);
                ContentPackageWebResource webResource = new ContentPackageWebResource(cpResource);
                IResourceStream stream = webResource.getResourceStream();

                try {
                    long size = stream.length().bytes();
                    ResourceResponse resourceResponse = new ResourceResponse() {
                        @Override
                        public HttpHeaderCollection getHeaders() {
                            HttpHeaderCollection headers = super.getHeaders();
                            if (stream instanceof CompressingContentPackageResourceStream) {
                                headers.addHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
                            }
                            return headers;
                        }
                    };
                    String contentType = stream.getContentType();
                    resourceResponse.setContentType(contentType);
                    if (Strings.CI.startsWith(contentType, "text/")) resourceResponse.setTextEncoding(StandardCharsets.UTF_8.name());
                    resourceResponse.setAcceptRange(AbstractResource.ContentRangeType.BYTES);
                    resourceResponse.setFileName(resourceName);
                    resourceResponse.setLastModified(stream.lastModifiedTime());

                    RequestCycle cycle = RequestCycle.get();
                    Long startbyte = cycle.getMetaData(CONTENT_RANGE_STARTBYTE);
                    Long endbyte = cycle.getMetaData(CONTENT_RANGE_ENDBYTE);

                    // Normalize range only if a start byte was provided
                    if (startbyte != null) {
                        long from = startbyte;
                        long to = (endbyte == null || endbyte >= size) ? (size - 1) : endbyte;

                        if (from < 0 || from >= size || to < from) {
                            // Unsatisfiable range 416
                            resourceResponse.setStatusCode(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE.value());
                            resourceResponse.setContentRange(String.format("bytes */%d", size));
                            resourceResponse.setContentLength(0);
                            return resourceResponse;
                        }

                        // Partial content 206
                        resourceResponse.setStatusCode(HttpStatus.PARTIAL_CONTENT.value());
                        resourceResponse.setContentRange(String.format("bytes %d-%d/%d", from, to, size));
                        resourceResponse.setContentLength(to - from + 1);
                        resourceResponse.setWriteCallback(
                                new PartWriterCallback(stream.getInputStream(), size, from, to).setClose(true)
                        );
                    } else {
                        // Full content 200
                        resourceResponse.setContentLength(size);
                        resourceResponse.setWriteCallback(
                                new PartWriterCallback(stream.getInputStream(), size, null, null).setClose(true)
                        );
                    }
                    return resourceResponse;
                } catch (ResourceStreamNotFoundException rsnfe) {
                    log.debug("Resource not found [{}], {}", path, rsnfe.toString());
                } catch (Exception e) {
                    log.warn("Could not process response for resource [{}]", path, e);
                }
            }
            // if we couldn't serve the requested resource then return a http 404
            log.debug("Could not serve resource [{}], return http 404 Not Found", resourceName);
            ResourceResponse resourceResponse = new ResourceResponse();
            resourceResponse.setError(HttpStatus.NOT_FOUND.value(), "Resource not found");
            return resourceResponse;
        }
    }
}
