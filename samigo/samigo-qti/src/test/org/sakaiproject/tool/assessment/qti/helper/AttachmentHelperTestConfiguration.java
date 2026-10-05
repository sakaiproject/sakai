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
package org.sakaiproject.tool.assessment.qti.helper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Kernel boundaries for testing the real operation-scoped attachment helper. */
@Configuration
public class AttachmentHelperTestConfiguration {
    @Bean(name = "org.sakaiproject.content.api.ContentHostingService")
    public ContentHostingService contentHostingService() throws Exception {
        ContentHostingService service = mock(ContentHostingService.class);
        when(service.newResourceProperties()).thenReturn(mock(ResourcePropertiesEdit.class));
        when(service.addAttachmentResource(anyString(), anyString(), anyString(), anyString(),
                any(byte[].class), any(ResourcePropertiesEdit.class))).thenAnswer(invocation -> {
            byte[] content = ((byte[]) invocation.getArgument(4)).clone();
            ContentResource resource = mock(ContentResource.class);
            when(resource.getContent()).thenReturn(content);
            return resource;
        });
        return service;
    }

    @Bean(name = "org.sakaiproject.tool.api.ToolManager")
    public ToolManager toolManager() {
        ToolManager manager = mock(ToolManager.class);
        Placement placement = mock(Placement.class);
        when(placement.getContext()).thenReturn("test-site");
        when(manager.getCurrentPlacement()).thenReturn(placement);
        Tool tool = mock(Tool.class);
        when(tool.getTitle()).thenReturn("Tests & Quizzes");
        when(manager.getTool("sakai.samigo")).thenReturn(tool);
        return manager;
    }

    @Bean
    public AttachmentHelper attachmentHelper(ContentHostingService contentHostingService, ToolManager toolManager) {
        return new AttachmentHelper(contentHostingService, toolManager);
    }
}
