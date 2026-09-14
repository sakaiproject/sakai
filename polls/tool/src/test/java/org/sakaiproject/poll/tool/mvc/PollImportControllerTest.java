/**********************************************************************************
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************************/
package org.sakaiproject.poll.tool.mvc;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.sakaiproject.poll.api.service.PollImportError;
import org.sakaiproject.poll.api.service.PollImportException;

public class PollImportControllerTest {

    @Test
    public void decodesPlainUtf8WithoutBom() {
        String text = "Question,Option 1,Option 2";
        String decoded = PollImportController.decodeUploadedFile(text.getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals(text, decoded);
    }

    @Test
    public void decodesUtf8WithBomAndStripsIt() {
        String text = "\uFEFF" + "Question,Option 1,Option 2";
        String decoded = PollImportController.decodeUploadedFile(text.getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals("Question,Option 1,Option 2", decoded);
    }

    @Test
    public void decodesAccentedUtf8Correctly() {
        String text = "¿Cuál es tu color favorito?,Azul,Verde,Rojo";
        String decoded = PollImportController.decodeUploadedFile(text.getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals(text, decoded);
    }

    @Test
    public void fallsBackToWindows1252WhenNotValidUtf8() {
        // Excel on Windows saves "CSV (Comma delimited)" files in Windows-1252 by default,
        // not UTF-8, when the sheet contains accented characters.
        String text = "¿Cuál es tu color favorito?,Azul,Verde,Rojo";
        byte[] windows1252Bytes = text.getBytes(Charset.forName("windows-1252"));
        String decoded = PollImportController.decodeUploadedFile(windows1252Bytes);
        Assert.assertEquals(text, decoded);
    }

    @Test
    public void rejectsMalformedBytesAfterUtf8Bom() {
        // A UTF-8 BOM declares the encoding; a decode failure after it means the file is
        // corrupt, not a legitimate Windows-1252 export (Excel never writes a BOM for that).
        byte[] bom = { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
        byte[] invalidUtf8 = { (byte) 0xFF, (byte) 0xFE };
        byte[] bytes = Arrays.copyOf(bom, bom.length + invalidUtf8.length);
        System.arraycopy(invalidUtf8, 0, bytes, bom.length, invalidUtf8.length);

        PollImportException exception = Assert.assertThrows(PollImportException.class, () ->
            PollImportController.decodeUploadedFile(bytes)
        );
        Assert.assertEquals(PollImportError.WRONG_FORMAT, exception.getError());
    }
}
