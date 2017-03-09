/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Copyright (c) 2010, 2011, 2012, 2013, 2014 Sakai Foundation
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
package org.sakaiproject.content.impl;

import java.util.Map;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import static org.mockito.Mockito.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static org.junit.Assert.assertEquals;

import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentTypeImageService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.impl.BaseContentService;
import org.sakaiproject.test.SakaiKernelTestBase;

/**
 * @author austin48@hawaii.edu
 */
public class BaseContentServiceTest extends SakaiKernelTestBase {
	
    private static final Log log = LogFactory.getLog(BaseContentServiceTest.class);
    
	public static Test suite()
	{
		TestSetup setup = new TestSetup(new TestSuite(BaseContentServiceTest.class))
		{
			protected void setUp() throws Exception 
			{
				log.debug("starting oneTimeSetup");
				oneTimeSetup(null);
				log.debug("finished oneTimeSetup");
			}
			protected void tearDown() throws Exception 
			{
				log.debug("starting tearDown");
				oneTimeTearDown();
				log.debug("finished tearDown");
			}
		};
		return setup;
	}
    
    public void testFixTypeAndId() throws Exception {

        ContentHostingService contentHostingService = getService(ContentHostingService.class);
        BaseContentService baseContentService = (BaseContentService)contentHostingService;
        
        BasicContentTypeImageService mockBasicContentTypeImageService = mock(BasicContentTypeImageService.class);
        baseContentService.setContentTypeImageService(mockBasicContentTypeImageService);            
                        
        Map extType = null;       
        String site_id;
        String id;
        String type;
        
        when(mockBasicContentTypeImageService.getContentTypeExtension("audio/wav")).thenReturn("wav");
        when(mockBasicContentTypeImageService.getContentTypeExtension("image/png")).thenReturn("png");
        
        //
        // no extension in id
        //    
        id = "/attachment/foobarbaz/fckeditor/e3ec10b2-62de-4492-bb39-c582249269e0/9dcf51f8-8889-4ad3-b430-f8ddaaaea461";
        type = "audio/wav";
        extType = baseContentService.fixTypeAndId(id, type);
        assertEquals(id + ".wav", extType.get("id"));        // append extension
        assertEquals(type, extType.get("type"));

        id = "/attachment/foo-bar-baz/fckeditor/e3ec10b2-62de-4492-bb39-c582249269e0/9dcf51f8-8889-4ad3-b430-f8ddaaaea461";
        type = "audio/wav";
        extType = baseContentService.fixTypeAndId(id, type);
        assertEquals(id + ".wav", extType.get("id"));        // append extension
        assertEquals(type, extType.get("type"));
        
        id = "/attachment/foo_bar_baz/fckeditor/e3ec10b2-62de-4492-bb39-c582249269e0/9dcf51f8-8889-4ad3-b430-f8ddaaaea461";
        type = "audio/wav";
        extType = baseContentService.fixTypeAndId(id, type);
        assertEquals(id + ".wav", extType.get("id"));        // append extension
        assertEquals(type, extType.get("type"));
        
        id = "/attachment/foo.bar.baz/fckeditor/e3ec10b2-62de-4492-bb39-c582249269e0/9dcf51f8-8889-4ad3-b430-f8ddaaaea461";
        type = "audio/wav";
        extType = baseContentService.fixTypeAndId(id, type);
        assertEquals(id + ".wav", extType.get("id"));        // append extension
        assertEquals(type, extType.get("type"));

        //
        // no extension in id
        //
        id = "/group/foobarbaz/9dcf51f8-8889-4ad3-b430-f8ddaaaea461";
        type = "image/png";
        extType = baseContentService.fixTypeAndId(id, type);
        assertEquals(id + ".png", extType.get("id"));        // append extension
        assertEquals(type, extType.get("type"));

        id = "/group/foo-bar-baz/9dcf51f8-8889-4ad3-b430-f8ddaaaea461";
        type = "image/png";
        extType = baseContentService.fixTypeAndId(id, type);
        assertEquals(id + ".png", extType.get("id"));        // append extension
        assertEquals(type, extType.get("type"));
        
        id = "/group/foo_bar_baz/9dcf51f8-8889-4ad3-b430-f8ddaaaea461";
        type = "image/png";
        extType = baseContentService.fixTypeAndId(id, type);
        assertEquals(id + ".png", extType.get("id"));        // append extension
        assertEquals(type, extType.get("type"));
        
        id = "/group/foo.bar.baz/9dcf51f8-8889-4ad3-b430-f8ddaaaea461";
        type = "image/png";
        extType = baseContentService.fixTypeAndId(id, type);
        assertEquals(id + ".png", extType.get("id"));        // append extension
        assertEquals(type, extType.get("type"));
                
        //
        // inlcude .wav extension in id
        //
        id = "/attachment/foobarbaz/fckeditor/e3ec10b2-62de-4492-bb39-c582249269e0/9dcf51f8-8889-4ad3-b430-f8ddaaaea461.wav";
        type = "audio/wav";
        extType = baseContentService.fixTypeAndId(id, type);
        assertEquals(id, extType.get("id"));        // don't append extension to id
        assertEquals(type, extType.get("type"));

        id = "/attachment/foo-bar-baz/fckeditor/e3ec10b2-62de-4492-bb39-c582249269e0/9dcf51f8-8889-4ad3-b430-f8ddaaaea461.wav";
        type = "audio/wav";
        extType = baseContentService.fixTypeAndId(id, type);
        assertEquals(id, extType.get("id"));        // don't append extension to id
        assertEquals(type, extType.get("type"));
        
        id = "/attachment/foo_bar_baz/fckeditor/e3ec10b2-62de-4492-bb39-c582249269e0/9dcf51f8-8889-4ad3-b430-f8ddaaaea461.wav";
        type = "audio/wav";
        extType = baseContentService.fixTypeAndId(id, type);
        assertEquals(id, extType.get("id"));        // don't append extension to id
        assertEquals(type, extType.get("type"));
        
        id = "/attachment/foo.bar.baz/fckeditor/e3ec10b2-62de-4492-bb39-c582249269e0/9dcf51f8-8889-4ad3-b430-f8ddaaaea461.wav";
        type = "audio/wav";
        extType = baseContentService.fixTypeAndId(id, type);
        assertEquals(id, extType.get("id"));        // don't append extension to id
        assertEquals(type, extType.get("type"));
        
        //
        // inlcude .png extension in id
        //
        id = "/group/foobarbaz/9dcf51f8-8889-4ad3-b430-f8ddaaaea461.png";
        type = "image/png";
        extType = baseContentService.fixTypeAndId(id, type);
        assertEquals(id, extType.get("id"));        // don't append extension to id
        assertEquals(type, extType.get("type"));

        id = "/group/foo-bar-baz/9dcf51f8-8889-4ad3-b430-f8ddaaaea461.png";
        type = "image/png";
        extType = baseContentService.fixTypeAndId(id, type);
        assertEquals(id, extType.get("id"));        // don't append extension to id
        assertEquals(type, extType.get("type"));
        
        id = "/group/foo_bar_baz/9dcf51f8-8889-4ad3-b430-f8ddaaaea461.png";
        type = "image/png";
        extType = baseContentService.fixTypeAndId(id, type);
        assertEquals(id, extType.get("id"));        // don't append extension to id
        assertEquals(type, extType.get("type"));
        
        id = "/group/foo.bar.baz/9dcf51f8-8889-4ad3-b430-f8ddaaaea461.png";
        type = "image/png";
        extType = baseContentService.fixTypeAndId(id, type);
        assertEquals(id, extType.get("id"));        // don't append extension to id
        assertEquals(type, extType.get("type"));
    }
}