/**
 * Copyright (c) 2003-2021 The Apereo Foundation
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
package org.sakaiproject.jsf2.spreadsheet;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SpreadsheetDataFileWriterOpenCsvTest {

    @Before
    public void setup() {
        FacesContext facesContext = ContextMocker.mockFacesContext();
        ExternalContext externalContextMock = Mockito.mock(ExternalContext.class);

        Mockito.when(facesContext.getExternalContext()).thenReturn(externalContextMock);
        Mockito.when(externalContextMock.getRequest()).thenReturn(new MockHttpServletRequest());
    }

    private void testDownload(SpreadsheetDataFileWriterOpenCsv sdfw, String resourceFileName) throws IOException {
        List<List<Object>> data = new ArrayList<>();
        data.add(Arrays.asList("asdf", "qwerty", "foobar", null));
        data.add(Arrays.asList("red", "", "yellow", 0));
        data.add(Arrays.asList("two words", "then \"three\" words", "one,two,three,four", 77));
        MockHttpServletResponse response = new MockHttpServletResponse();
        String fileName = "testFile";

        sdfw.writeDataToResponse(data, fileName, response);

        Assert.assertEquals("content type is wrong", "text/comma-separated-values", response.getContentType());

        String contentDisposition = response.getHeader("Content-Disposition");
        Assert.assertEquals("content disposition wrong", "attachment; filename*=utf-8''testFile.csv", contentDisposition);

        String expected = readResourceToString(resourceFileName);
        String fileAsString = response.getContentAsString();
        // compare only the first 50 characters to avoid differences in line endings on Windows
        Assert.assertEquals("content doesn't match", normalizeLineEndings(expected).substring(1,50), normalizeLineEndings(fileAsString).substring(1,50));
    }

    private String normalizeLineEndings(String s) {
        return s.replace("\r\n", "\n");
    }

    @Test
    public void testDownloadWithNull() throws IOException {
        SpreadsheetDataFileWriterOpenCsv sdfw = new SpreadsheetDataFileWriterOpenCsv();
        testDownload(sdfw, "/fileWithNull.csv");
    }

    @Test
    public void testDownloadWithEmpty() throws IOException {
        SpreadsheetDataFileWriterOpenCsv sdfw = new SpreadsheetDataFileWriterOpenCsv(SpreadsheetDataFileWriterOpenCsv.NULL_AS.EMPTY, ',');
        testDownload(sdfw, "/fileWithEmptyString.csv");
    }

    private String readResourceToString(String resource) throws IOException {
        InputStream is = getClass().getResourceAsStream(resource);
        return IOUtils.toString(is, StandardCharsets.UTF_8);
    }

}
