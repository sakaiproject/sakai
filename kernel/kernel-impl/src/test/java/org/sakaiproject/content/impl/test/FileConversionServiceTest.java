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


import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.FileConversionService;
import org.sakaiproject.content.api.persistence.FileConversionServiceRepository;
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
    private FileConversionServiceRepository repository;

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
    public void noMultipleSubmits() {

        String ref = "xyz";
        Assert.isTrue(repository.findByReference(ref).size() == 0);
        fileConversionService.submit(ref);
        Assert.isTrue(repository.findByReference(ref).size() == 1);
        fileConversionService.submit(ref);
        Assert.isTrue(repository.findByReference(ref).size() == 1);
        fileConversionService.submit(ref);
        Assert.isTrue(repository.findByReference(ref).size() == 1);
    }
}
