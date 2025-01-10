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
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.util.Timeout;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoolFileConverter {

    public static byte[] convert(String baseUrl, InputStream sourceInputStream) throws IOException {

        int timeoutSeconds = 5;
        RequestConfig config = RequestConfig.custom()
            .setConnectionRequestTimeout(Timeout.ofSeconds(timeoutSeconds))
            .setResponseTimeout(Timeout.ofSeconds(timeoutSeconds))
            .build();

        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .build()) {

            HttpPost httpPost = new HttpPost(baseUrl + "/lool/convert-to/pdf");

            HttpEntity multipart = MultipartEntityBuilder.create()
                .addBinaryBody("data", sourceInputStream, ContentType.MULTIPART_FORM_DATA, "anything")
                .build();

            httpPost.setEntity(multipart);

            return client.execute(httpPost, response -> {
                int status = response.getCode();
                if (status >= 200 && status < 300) {
                    return EntityUtils.toByteArray(response.getEntity());
                } else {
                    String errorBody = EntityUtils.toString(response.getEntity());
                    log.error("LOOL conversion failed with status {}: {}", status, errorBody);
                    return null;
                }
            });
        }
    }
}
