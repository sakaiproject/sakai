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
        Assert.assertEquals("content doesn't match", expected, fileAsString);
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
