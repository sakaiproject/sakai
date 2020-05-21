/**********************************************************************************
 *
 * Copyright (c) 2006, 2008, 2013 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.impl.test;

import static org.junit.runners.MethodSorters.NAME_ASCENDING;

import java.util.List;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.FileConversionService;
import org.sakaiproject.content.api.repository.FileConversionQueueItemRepository;
import org.sakaiproject.content.hbm.FileConversionQueueItem;
import org.sakaiproject.content.impl.test.FileConversionServiceTestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {FileConversionServiceTestConfiguration.class})
@FixMethodOrder(NAME_ASCENDING)
public class FileConversionServiceTest extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired
    private FileConversionService fileConversionService;

    @Autowired
    private FileConversionQueueItemRepository repository;

    @Test
    public void testCanConvert() {

        Assert.isTrue(fileConversionService.canConvert(ContentHostingService.DOCX_MIMETYPE), "DOCX is one of the default convertable types");
        Assert.isTrue(fileConversionService.canConvert(ContentHostingService.DOC_MIMETYPE), "DOC is one of the default convertable types");
        Assert.isTrue(fileConversionService.canConvert(ContentHostingService.ODT_MIMETYPE), "ODT is one of the default convertable types");
        Assert.isTrue(fileConversionService.canConvert(ContentHostingService.ODP_MIMETYPE), "ODP is one of the default convertable types");
        Assert.isTrue(fileConversionService.canConvert(ContentHostingService.PPT_MIMETYPE), "PPT is one of the default convertable types");
        Assert.isTrue(fileConversionService.canConvert(ContentHostingService.PPTX_MIMETYPE), "PPTX is one of the default convertable types");
    }

    @Test
    public void testConvert() {

        String ref = "/attachment/8c563fb1-6bf8-4e01-9e25-8881f4dc35e2/Assignments/6f4244bc-e8fe-48a5-a56b-b3d7bfd1d592/Northern 8 manual.doc";
        fileConversionService.convert(ref);
        List<FileConversionQueueItem> items = repository.findByStatus(FileConversionQueueItem.Status.NOT_STARTED);
        Assert.isTrue(items.size() == 1, "Only one conversion job should be present in the table");
        if (items.size() == 1) {
            Assert.isTrue(items.get(0).getReference().equals(ref), "Our refs should be equal");
        }
    }
}
