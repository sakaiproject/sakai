/**
 * Copyright (c) 2003 The Apereo Foundation
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
package org.sakaiproject.component.app.scheduler.jobs.cm.util;

import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.text.SimpleDateFormat;

public class FileArchiveUtil {

    @Getter
    @Setter
    private String directory;

    @Getter
    @Setter
    private String dateFormat;

    public File createArchiveFolder() {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        String newFolderName = sdf.format(new java.util.Date());
        String path = directory + System.getProperty("file.separator") + newFolderName;
        File f = new File(path);

        if (!f.exists()) {
            f.mkdir();
        }

        return f;
    }
}
