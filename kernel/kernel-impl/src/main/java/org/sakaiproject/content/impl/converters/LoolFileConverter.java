/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.content.impl.converters;

import java.io.InputStream;
import java.io.IOException;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoolFileConverter {

    public static byte[] convert(String baseUrl, InputStream sourceInputStream) {
        final HttpPost httpPost = new HttpPost(baseUrl + "/lool/convert-to/pdf");

        final MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addBinaryBody("data", sourceInputStream, ContentType.MULTIPART_FORM_DATA, "anything");
        
        final HttpEntity multipart = builder.build();
        httpPost.setEntity(multipart);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            return client.execute(httpPost, response -> {
                final int statusCode = response.getCode();
                if (statusCode == HttpStatus.SC_OK) {
                    return EntityUtils.toByteArray(response.getEntity());
                } else {
                    log.error("File conversion failed with HTTP status code: {}. URL: {}",
                        statusCode, baseUrl + "/lool/convert-to/pdf");
                    return null;
                }
            });
        } catch (IOException e) {
            log.warn("Error during file conversion: {}", e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.warn("Unexpected error during file conversion: {}", e.getMessage(), e);
            return null;
        }
    }
}
