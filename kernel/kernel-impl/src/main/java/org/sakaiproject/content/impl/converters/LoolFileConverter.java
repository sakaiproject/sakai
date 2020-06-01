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

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

public class LoolFileConverter {

    public static byte[] convert(String baseUrl, InputStream sourceInputStream) throws IOException {

        int timeoutMillis = 5000;
        RequestConfig config = RequestConfig.custom()
            .setConnectTimeout(timeoutMillis)
            .setConnectionRequestTimeout(timeoutMillis)
            .setSocketTimeout(timeoutMillis * 1000).build();
        CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

        HttpPost httpPost = new HttpPost(baseUrl + "/lool/convert-to/pdf");

        HttpEntity multipart = MultipartEntityBuilder.create()
            .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
            .addBinaryBody("data", sourceInputStream, ContentType.MULTIPART_FORM_DATA, "anything")
            .build();

        httpPost.setEntity(multipart);
        CloseableHttpResponse response = client.execute(httpPost);
        byte[] convertedFileBytes = EntityUtils.toByteArray(response.getEntity());
        client.close();
        return convertedFileBytes;
    }
}
