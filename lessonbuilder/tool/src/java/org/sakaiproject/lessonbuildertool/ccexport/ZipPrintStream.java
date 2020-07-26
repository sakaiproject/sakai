/**
 * Copyright (c) 2003-2013 The Apereo Foundation
 * <p>
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://opensource.org/licenses/ecl2
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.lessonbuildertool.ccexport;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipOutputStream;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ZipPrintStream extends ZipOutputStream {

    private static String lineSeparator = System.lineSeparator();

    public ZipPrintStream(OutputStream out) {
        super(out);
    }

    public void print(String text) {
        try {
            write(text.getBytes());
        } catch (IOException ioe) {
            log.warn("Could not write [{}], {}", text, ioe.toString());
        }
    }

    public void println(String text) {
        try {
            write(text.getBytes());
            write(lineSeparator.getBytes());
        } catch (IOException ioe) {
            log.warn("Could not write [{}], {}", text, ioe.toString());
        }
    }
}
