/**
 * Copyright (c) 2003-2015 The Apereo Foundation
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
package org.sakaiproject.lessonbuildertool.resolver;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.fileupload.FileItem;

import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

/**
 * Created by neelam on 8/3/2015.
 * This class extends CommonsMultipartResolver's parseFileItems() method to upload multiple files coming from same form input name.
 * Used this link to fix the problem : Reference : http://dhruba.name/2008/12/27/implementing-single-and-multiple-file-multipart-uploads-using-spring-25/
 */
@Slf4j
public class MultiCommonsMultipartResolver extends CommonsMultipartResolver {
    public MultiCommonsMultipartResolver(){
    }
    public MultiCommonsMultipartResolver(ServletContext servletContext) {
        super(servletContext);
    }

    @Override
    protected MultipartParsingResult parseFileItems(List<FileItem> fileItems, String encoding) {
        MultiValueMap<String, MultipartFile> multipartFiles = new LinkedMultiValueMap<String, MultipartFile>();
        Map<String, String[]> multipartParameters = new HashMap<String, String[]>();
        Map<String, String> multipartParameterContentTypes = new HashMap<String, String>();

        // Extract multipart files and multipart parameters.
        for (FileItem fileItem : fileItems) {
            if (fileItem.isFormField()) {
                String value;
                String partEncoding = determineEncoding(fileItem.getContentType(), encoding);
                if (partEncoding != null) {
                    try {
                        value = fileItem.getString(partEncoding);
                    }
                    catch (UnsupportedEncodingException ex) {
                        if (log.isWarnEnabled()) {
                            log.warn("Could not decode multipart item '" + fileItem.getFieldName() +
                                    "' with encoding '" + partEncoding + "': using platform default");
                        }
                        value = fileItem.getString();
                    }
                }
                else {
                    value = fileItem.getString();
                }
                String[] curParam = multipartParameters.get(fileItem.getFieldName());
                if (curParam == null) {
                    // simple form field
                    multipartParameters.put(fileItem.getFieldName(), new String[] {value});
                }
                else {
                    // array of simple form fields
                    String[] newParam = StringUtils.addStringToArray(curParam, value);
                    multipartParameters.put(fileItem.getFieldName(), newParam);
                }
                multipartParameterContentTypes.put(fileItem.getFieldName(), fileItem.getContentType());
            }
            else {
                // multipart file field
                CommonsMultipartFile file = new CommonsMultipartFile(fileItem);
                multipartFiles.add(fileItem.getName(), file);
                if (log.isDebugEnabled()) {
                    log.debug("Found multipart file [" + file.getName() + "] of size " + file.getSize() +
                            " bytes with original filename [" + file.getOriginalFilename() + "], stored " +
                            file.getStorageDescription());
                }
            }
        }
        return new MultipartParsingResult(multipartFiles, multipartParameters, multipartParameterContentTypes);
    }

    private String determineEncoding(String contentTypeHeader, String defaultEncoding) {
        if (!StringUtils.hasText(contentTypeHeader)) {
            return defaultEncoding;
        }
        MediaType contentType = MediaType.parseMediaType(contentTypeHeader);
        Charset charset = contentType.getCharSet();
        return (charset != null ? charset.name() : defaultEncoding);
    }
}
