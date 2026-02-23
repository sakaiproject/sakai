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

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.wicket.request.HttpHeaderCollection;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.AbstractResource.WriteCallback;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.PartWriterCallback;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import static org.sakaiproject.scorm.api.ScormConstants.ROOT_DIRECTORY;
import org.sakaiproject.scorm.service.sakai.impl.ContentPackageSakaiResource;
import org.sakaiproject.scorm.ui.player.util.CompressingContentPackageResourceStream;
import org.sakaiproject.scorm.ui.player.util.ContentPackageWebResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author bjones86
 */
@Slf4j
public class ContentPackageResourceReference extends ResourceReference
{
    private final ServerConfigurationService serverConfigurationService;
    private final ContentHostingService contentHostingService;

    public ContentPackageResourceReference(ServerConfigurationService serverConfigurationService,
                                           ContentHostingService contentHostingService)
    {
        super(ContentPackageResourceReference.class, "contentPackages");
        this.serverConfigurationService = Objects.requireNonNull(serverConfigurationService,
            "ServerConfigurationService must be available");
        this.contentHostingService = Objects.requireNonNull(contentHostingService,
            "ContentHostingService must be available");
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

            log.debug("Process request for resource id/resource [{}/{}]", resourceId, resourceName);
            if (StringUtils.isNoneBlank(resourceId, resourceName)) {

                String base = ROOT_DIRECTORY + resourceId + "/" + resourceName;
                PageParameters parameters = attributes.getParameters();
                String suffix = IntStream.range(0, parameters.getIndexedCount())
                        .mapToObj(i -> parameters.get(i).toString())
                        .takeWhile(s -> !"contentpackages".equalsIgnoreCase(s))
                        .map(s -> "/" + s)
                        .collect(Collectors.joining());

                String path = base + suffix;

                String rawFileName = path.substring(path.lastIndexOf('/') + 1);
                String fileName = StringUtils.defaultIfBlank(rawFileName, resourceName);

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
                    URI directLink = resolveDirectLink(cpResource);
                    if (directLink != null) {
                        resourceResponse.setAcceptRange(AbstractResource.ContentRangeType.NONE);
                        resourceResponse.setContentType(stream.getContentType());
                        resourceResponse.setFileName(fileName);
                        resourceResponse.setContentLength(0);
                        resourceResponse.setWriteCallback(NO_OP_WRITE_CALLBACK);

                        if (serverConfigurationService.getBoolean("cloud.content.sendfile", false)) {
                            int hostLength = (directLink.getScheme() + "://" + directLink.getHost()).length();
                            String redirectPath = "/sendfile" + directLink.toString().substring(hostLength);
                            log.debug("Serving SCORM asset via sendfile path [{}]", redirectPath);
                            resourceResponse.getHeaders().addHeader("X-Accel-Redirect", redirectPath);
                            resourceResponse.getHeaders().addHeader("X-Sendfile", redirectPath);
                            return resourceResponse;
                        }

                        if (serverConfigurationService.getBoolean("cloud.content.directurl", true)) {
                            log.debug("Redirecting SCORM asset to [{}]", directLink);
                            resourceResponse.setStatusCode(HttpStatus.TEMPORARY_REDIRECT.value());
                            resourceResponse.getHeaders().addHeader(HttpHeaders.LOCATION, directLink.toString());
                            return resourceResponse;
                        }
                    }

                    String contentType = stream.getContentType();
                    resourceResponse.setContentType(contentType);
                    if (Strings.CI.startsWith(contentType, "text/")) resourceResponse.setTextEncoding(StandardCharsets.UTF_8.name());
                    resourceResponse.setAcceptRange(AbstractResource.ContentRangeType.NONE);
                    resourceResponse.setFileName(fileName);
                    resourceResponse.setLastModified(stream.lastModifiedTime());

                    resourceResponse.setContentLength(size);
                    resourceResponse.setWriteCallback(
                            new PartWriterCallback(stream.getInputStream(), size, null, null).setClose(true)
                    );
                    return resourceResponse;
                } catch (ResourceStreamNotFoundException rsnfe) {
                    log.debug("Resource not found [{}], {}", path, rsnfe.toString());
                } catch (Exception e) {
                    log.warn("Could not process response for resource [{}]", path, e);
                }
            }
            // if we couldn't serve the requested resource then return a http 404
            log.debug("Could not serve resource from [{}], return http 404 Not Found", resourceName);
            ResourceResponse resourceResponse = new ResourceResponse();
            resourceResponse.setError(HttpStatus.NOT_FOUND.value(), "Resource not found");
            return resourceResponse;
        }
    }

    private static final WriteCallback NO_OP_WRITE_CALLBACK = new WriteCallback() {
        @Override
        public void writeData(IResource.Attributes attributes) {
            // Intentionally empty; direct-link responses do not stream content from Sakai.
        }
    };

    private URI resolveDirectLink(ContentPackageSakaiResource resource) {
        if (resource == null) {
            return null;
        }
        ContentResource contentResource = resource.getContentResource();
        if (contentResource == null) {
            return null;
        }
        try {
            return contentHostingService.getDirectLinkToAsset(contentResource);
        } catch (Exception e) {
            log.debug("Unable to obtain direct link for resource [{}]", contentResource.getId(), e);
            return null;
        }
    }
}
